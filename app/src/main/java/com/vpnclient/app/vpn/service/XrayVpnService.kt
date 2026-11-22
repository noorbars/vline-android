package com.vpnclient.app.vpn.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import com.vpnclient.app.MainActivity
import com.vpnclient.app.R
import com.vpnclient.app.VpnApplication
import com.vpnclient.app.vpn.core.LatencyTester
import com.vpnclient.app.vpn.core.RealDelayProbe
import com.vpnclient.app.vpn.core.RouteCategory
import com.vpnclient.app.vpn.core.RouteClassifier
import com.vpnclient.app.vpn.core.VlessConfig
import com.vpnclient.app.vpn.core.VlessParser
import com.vpnclient.app.vpn.core.XrayConfigBuilder
import go.Seq
import libv2ray.CoreCallbackHandler
import libv2ray.CoreController
import libv2ray.Libv2ray
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class XrayVpnService : VpnService() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private var connectJob: Job? = null
    private var tunInterface: ParcelFileDescriptor? = null
    private var coreController: CoreController? = null

    private var activeConfig: VlessConfig? = null
    private var activeCategory: RouteCategory? = null

    private class CoreCallback : CoreCallbackHandler {
        override fun startup(): Long = 0L
        override fun shutdown(): Long = 0L
        override fun onEmitStatus(l: Long, s: String?): Long {
            Log.d(TAG, "XrayCore [$l]: $s")
            return 0L
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "XrayVpnService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val app = application as VpnApplication
        when (intent?.action) {
            ACTION_CONNECT -> {
                val rawConfig = intent.getStringExtra(EXTRA_CONFIG) ?: app.preferenceManager.cachedSubscriptionConfig
                val routeKey = intent.getStringExtra(EXTRA_ROUTE_KEY) ?: app.preferenceManager.selectedRouteCategory
                if (!rawConfig.isNullOrBlank()) {
                    startTunnel(rawConfig, routeKey)
                } else {
                    _vpnState.value = VpnState.Error("پیکربندی سرور یافت نشد. لطفاً بروزرسانی کنید.")
                }
            }
            ACTION_DISCONNECT -> {
                stopTunnel()
            }
            ACTION_RELOAD_CONFIG, ACTION_RELOAD_ROUTE -> {
                val rawConfig = intent.getStringExtra(EXTRA_CONFIG) ?: app.preferenceManager.cachedSubscriptionConfig
                val routeKey = intent.getStringExtra(EXTRA_ROUTE_KEY) ?: app.preferenceManager.selectedRouteCategory
                if (!rawConfig.isNullOrBlank()) {
                    startTunnel(rawConfig, routeKey)
                }
            }
            ACTION_RETEST_METRICS -> {
                retestMetrics()
            }
        }
        return START_NOT_STICKY
    }

    private fun startTunnel(rawConfigString: String, routeKey: String?) {
        connectJob?.cancel()
        _vpnState.value = VpnState.Connecting
        startForeground(NOTIFICATION_ID, buildNotification("در حال اتصال به سرور..."))

        connectJob = serviceScope.launch {
            try {
                val app = application as VpnApplication
                val prefRouteKey = routeKey ?: app.preferenceManager.selectedRouteCategory
                val selectedCategory = RouteCategory.fromKey(prefRouteKey)
                val bypassIran = app.preferenceManager.isIranBypassEnabled

                // 1. Extract and parse all VLESS links from subscription
                val allConfigs = VlessParser.parseAll(rawConfigString)
                if (allConfigs.isEmpty()) {
                    _vpnState.value = VpnState.Error("پیکربندی معتبری در اشتراک شما یافت نشد")
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    return@launch
                }

                // 2. Establish TUN interface (if not already opened)
                if (tunInterface == null) {
                    val builder = Builder()
                        .setSession(getString(R.string.app_name))
                        .setMtu(1500)
                        .addAddress("26.26.26.1", 30)
                        .addRoute("0.0.0.0", 0)
                        .addDnsServer("1.1.1.1")
                        .addDnsServer("8.8.8.8")

                    try {
                        builder.addAddress("fdfe:dcba:9876::1", 126)
                        builder.addRoute("::", 0)
                    } catch (e: Exception) {
                        Log.w(TAG, "IPv6 route warning", e)
                    }

                    // Exclude client app to prevent routing loops
                    try {
                        builder.addDisallowedApplication(packageName)
                    } catch (e: Exception) {
                        Log.w(TAG, "addDisallowedApplication warning", e)
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        builder.setMetered(false)
                    }

                    val tun = builder.establish()
                    if (tun == null) {
                        _vpnState.value = VpnState.Error("مجوز ایجاد اینترفیس VPN صادر نشد")
                        stopForeground(STOP_FOREGROUND_REMOVE)
                        return@launch
                    }
                    tunInterface = tun
                }

                val tunFd = tunInterface!!.fd
                val assetDir = File(filesDir, "xray_assets").apply { if (!exists()) mkdirs() }

                // 3. Connect according to selected mode
                var finalConfig: VlessConfig
                var finalCategory: RouteCategory
                var initialRealDelay: Long? = null

                if (selectedCategory == RouteCategory.SMART) {
                    // Smart Connection: Test in order: TLS (Inbound 3) -> Reality (Inbound 1) -> IPv6 (Inbound 2)
                    val candidates = RouteClassifier.getSmartCandidates(allConfigs)
                    var settledConfig: VlessConfig? = null
                    var settledCategory: RouteCategory? = null

                    updateNotification("در حال سنجش هوشمند مسیرها...")

                    for ((candidateCategory, candidateConfig) in candidates) {
                        Log.i(TAG, "Smart probe testing: ${candidateCategory.title} (${candidateConfig.host}:${candidateConfig.port})")

                        val candidateJson = XrayConfigBuilder.buildConfig(
                            vlessConfig = candidateConfig,
                            socksPort = SOCKS_PORT,
                            httpPort = HTTP_PORT,
                            bypassIran = bypassIran
                        )

                        try {
                            stopXrayEngine()
                            startXrayEngine(candidateJson, assetDir.absolutePath, tunFd)

                            // Measure HTTP Real Delay with 2500ms timeout
                            val delay = RealDelayProbe.testRealDelay(httpPort = HTTP_PORT, timeoutMs = 2500L)
                            Log.i(TAG, "Smart probe result for ${candidateCategory.name}: $delay ms")

                            if (delay > 0) {
                                settledConfig = candidateConfig
                                settledCategory = candidateCategory
                                initialRealDelay = delay
                                break
                            } else {
                                Log.w(TAG, "Candidate ${candidateCategory.name} timed out or failed. Trying next priority route...")
                                stopXrayEngine()
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "Candidate ${candidateCategory.name} failed during startup/probe", e)
                            stopXrayEngine()
                        }
                    }

                    if (settledConfig != null && settledCategory != null) {
                        finalConfig = settledConfig
                        finalCategory = settledCategory
                    } else {
                        // All candidates timed out on 2500ms probe: connect to Priority 1 (TLS / STABLE) as fallback
                        val fallback = candidates.firstOrNull()?.second ?: allConfigs.first()
                        finalConfig = fallback
                        finalCategory = candidates.firstOrNull()?.first ?: RouteCategory.STABLE
                        Log.w(TAG, "All probes timed out. Falling back to primary stable route.")
                        val fallbackJson = XrayConfigBuilder.buildConfig(
                            vlessConfig = fallback,
                            socksPort = SOCKS_PORT,
                            httpPort = HTTP_PORT,
                            bypassIran = bypassIran
                        )
                        stopXrayEngine()
                        startXrayEngine(fallbackJson, assetDir.absolutePath, tunFd)
                    }
                } else {
                    // Manual Route: Connect directly to the specific inbound
                    val classified = RouteClassifier.classify(allConfigs)
                    finalConfig = classified[selectedCategory] ?: allConfigs.first()
                    finalCategory = selectedCategory

                    val xrayJson = XrayConfigBuilder.buildConfig(
                        vlessConfig = finalConfig,
                        socksPort = SOCKS_PORT,
                        httpPort = HTTP_PORT,
                        bypassIran = bypassIran
                    )

                    stopXrayEngine()
                    startXrayEngine(xrayJson, assetDir.absolutePath, tunFd)
                }

                activeConfig = finalConfig
                activeCategory = finalCategory

                // 4. Measure initial metrics (Ping & Real Delay)
                val ping = LatencyTester.testTcpPing(finalConfig.host, finalConfig.port, 2500)
                val realDelay = initialRealDelay ?: RealDelayProbe.testRealDelay(httpPort = HTTP_PORT, timeoutMs = 2500L)

                val details = buildConnectionDetails(finalConfig, finalCategory, realDelay, ping, isTesting = false)
                _vpnState.value = VpnState.Connected(details)
                updateNotification("متصل هستی (${details.protocol})")
                Log.i(TAG, "VPN Connected: ${details.protocol} | Ping: ${ping}ms | RealDelay: ${realDelay}ms")

            } catch (e: Exception) {
                val msg = e.localizedMessage ?: e.message ?: "خطا در اتصال به هسته VPN"
                Log.e(TAG, "Error starting tunnel", e)

                try {
                    tunInterface?.close()
                } catch (ignored: Exception) {}
                tunInterface = null
                stopXrayEngine()

                _vpnState.value = VpnState.Error(msg)
                stopForeground(STOP_FOREGROUND_REMOVE)
            }
        }
    }

    private fun retestMetrics() {
        val config = activeConfig ?: return
        val category = activeCategory ?: RouteCategory.SMART
        val currentConnected = _vpnState.value as? VpnState.Connected ?: return

        _vpnState.value = VpnState.Connected(currentConnected.details.copy(isTesting = true))

        serviceScope.launch {
            val ping = LatencyTester.testTcpPing(config.host, config.port, 2500)
            val realDelay = RealDelayProbe.testRealDelay(httpPort = HTTP_PORT, timeoutMs = 2500L)
            val updated = buildConnectionDetails(config, category, realDelay, ping, isTesting = false)
            _vpnState.value = VpnState.Connected(updated)
            Log.i(TAG, "Metrics refreshed: Ping: ${ping}ms | RealDelay: ${realDelay}ms")
        }
    }

    private fun buildConnectionDetails(
        config: VlessConfig,
        category: RouteCategory,
        realDelay: Long,
        ping: Long,
        isTesting: Boolean
    ): ConnectionDetails {
        val proto = when {
            config.port == 36689 || config.host.contains(":") -> "VLESS • IPv6"
            config.security.equals("reality", true) -> "VLESS • Reality"
            config.security.equals("tls", true) -> "VLESS • TLS"
            else -> "VLESS • ${config.security.uppercase()}"
        }

        val inboundTag = when (category) {
            RouteCategory.STABLE -> "مسیر اصلی"
            RouteCategory.FAST -> "مسیر پرسرعت"
            RouteCategory.SPECIAL_IR -> "مسیر ویژه همراه"
            RouteCategory.SMART -> "اتصال هوشمند"
        }

        return ConnectionDetails(
            protocol = proto,
            inboundRemark = inboundTag,
            serverAddress = "${config.host}:${config.port}",
            security = config.security.uppercase(),
            realDelayMs = if (realDelay > 0) realDelay else null,
            pingMs = if (ping > 0) ping else null,
            isTesting = isTesting
        )
    }

    private fun startXrayEngine(configJson: String, datDir: String, tunFd: Int) {
        try {
            Seq.setContext(applicationContext)
            Libv2ray.initCoreEnv(datDir, "")

            val controller = Libv2ray.newCoreController(CoreCallback())
                ?: throw IllegalStateException("ایجاد کنترلر هسته Xray ناموفق بود")

            coreController = controller
            controller.startLoop(configJson, tunFd)

            Log.i(TAG, "Libv2ray CoreController started successfully with TUN fd: $tunFd")
        } catch (e: Exception) {
            val errorMsg = e.localizedMessage ?: e.message ?: e.toString()
            Log.e(TAG, "Error starting Xray engine", e)
            throw IllegalStateException("خطا در هسته Xray: $errorMsg")
        }
    }

    private fun stopXrayEngine() {
        try {
            coreController?.stopLoop()
            Log.i(TAG, "Libv2ray CoreController stopped")
        } catch (e: Exception) {
            Log.w(TAG, "Notice on stopLoop: ${e.message}")
        } finally {
            coreController = null
        }
    }

    private fun stopTunnel() {
        connectJob?.cancel()
        stopXrayEngine()

        try {
            tunInterface?.close()
        } catch (e: Exception) {
            Log.w(TAG, "Error closing tun interface", e)
        }
        tunInterface = null
        activeConfig = null
        activeCategory = null

        _vpnState.value = VpnState.Disconnected
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildNotification(statusText: String): Notification {
        val launchIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val disconnectIntent = Intent(this, XrayVpnService::class.java).apply {
            action = ACTION_DISCONNECT
        }
        val disconnectPendingIntent = PendingIntent.getService(
            this, 1, disconnectIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, VpnApplication.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(statusText)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "قطع اتصال", disconnectPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(statusText: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(statusText))
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTunnel()
        serviceScope.cancel()
    }

    companion object {
        private const val TAG = "XrayVpnService"
        private const val NOTIFICATION_ID = 1001
        private const val SOCKS_PORT = 10808
        private const val HTTP_PORT = 10809

        const val ACTION_CONNECT = "com.vpnclient.app.action.CONNECT"
        const val ACTION_DISCONNECT = "com.vpnclient.app.action.DISCONNECT"
        const val ACTION_RELOAD_CONFIG = "com.vpnclient.app.action.RELOAD_CONFIG"
        const val ACTION_RELOAD_ROUTE = "com.vpnclient.app.action.RELOAD_ROUTE"
        const val ACTION_RETEST_METRICS = "com.vpnclient.app.action.RETEST_METRICS"
        const val EXTRA_CONFIG = "extra_config"
        const val EXTRA_ROUTE_KEY = "extra_route_key"

        private val _vpnState = MutableStateFlow<VpnState>(VpnState.Disconnected)
        val vpnState: StateFlow<VpnState> = _vpnState.asStateFlow()

        fun start(context: Context, rawConfig: String, routeKey: String? = null) {
            val intent = Intent(context, XrayVpnService::class.java).apply {
                action = ACTION_CONNECT
                putExtra(EXTRA_CONFIG, rawConfig)
                if (routeKey != null) putExtra(EXTRA_ROUTE_KEY, routeKey)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, XrayVpnService::class.java).apply {
                action = ACTION_DISCONNECT
            }
            context.startService(intent)
        }

        fun reloadConfig(context: Context, rawConfig: String) {
            val intent = Intent(context, XrayVpnService::class.java).apply {
                action = ACTION_RELOAD_CONFIG
                putExtra(EXTRA_CONFIG, rawConfig)
            }
            context.startService(intent)
        }

        fun reloadRoute(context: Context, rawConfig: String, routeKey: String) {
            val intent = Intent(context, XrayVpnService::class.java).apply {
                action = ACTION_RELOAD_ROUTE
                putExtra(EXTRA_CONFIG, rawConfig)
                putExtra(EXTRA_ROUTE_KEY, routeKey)
            }
            context.startService(intent)
        }

        fun retestMetrics(context: Context) {
            val intent = Intent(context, XrayVpnService::class.java).apply {
                action = ACTION_RETEST_METRICS
            }
            context.startService(intent)
        }
    }
}
