# Keep LibXray native/JNI and wrapper classes
-keep class libxray.** { *; }
-keep interface libxray.** { *; }
-keep class io.nekohasekai.sagernet.** { *; }

# Keep Go and Libv2ray JNI runtime classes (CRITICAL: prevents NoClassDefFoundError on launch)
-keep class go.** { *; }
-keep interface go.** { *; }
-keepclassmembers class go.** { *; }

-keep class libv2ray.** { *; }
-keep interface libv2ray.** { *; }
-keepclassmembers class libv2ray.** { *; }

# Keep all native methods across the entire app
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep Network DTOs for GSON serialization
-keepclassmembers class com.vpnclient.app.data.api.models.** { <fields>; }
-keep class com.vpnclient.app.data.api.models.** { *; }
-keep class com.vpnclient.app.data.model.** { *; }
-keep class com.vpnclient.app.ui.dashboard.RenewalPlan { *; }
-keep class com.vpnclient.app.vpn.core.RouteCategory { *; }

# Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory { *; }
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler { *; }

# Google Tink & Errorprone warnings
-dontwarn com.google.errorprone.annotations.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-dontwarn go.**
-dontwarn libv2ray.**
