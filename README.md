# VLine VPN Android Client

یک کلاینت اندرویدی مدرن، فوق‌العاده سبک و مینیمال بر پایه **Kotlin**، **Jetpack Compose (Material 3)** و **LibXray**، طراحی شده به طور اختصاصی برای سرویس‌های VPN مبتنی بر پنل **3X-UI (سنایی)** با قابلیت تفکیک خودکار ترافیک داخلی ایران.

---

## ویژگی‌های اصلی

1. **طراحی مینیمال و کاملاً فارسی (RTL Native)**:
   - رابط کاربری تک صفحه‌ای بسیار ساده و بدون پیچیدگی‌های فنی.
   - دکمه اتصال نئونی با افکت‌های تنفسی (Pulsing Glow) برای نمایش وضعیت اتصال.
   - نمایش حجم باقی‌مانده و روزهای اعتبار کاربر بر اساس اطلاعات پنل ۳X-UI.
   - تست پینگ زنده (Latency Ping) بر حسب میلی‌ثانیه با امکان تست دستی با یک لمس.
   - حذف کامل منوهای اضافه، اسکنرهای پیچیده QR، و دستکاری‌های پروتکلی برای راحتی کاربر نهایی.

2. **احراز هویت با نام کاربری و کلمه عبور**:
   - کاربر تنها با وارد کردن نام کاربری و رمز عبور اختصاصی خود وارد برنامه می‌شود.
   - دریافت خودکار کانفیگ‌ها و لینک سابسکریپشن و ذخیره امن در `EncryptedSharedPreferences`.
   - بروزرسانی خودکار کانفیگ‌ها در زمان باز شدن برنامه و همچنین دکمه بروزرسانی دستی.

3. **تفکیک خودکار ترافیک ایران (Iran GeoIP / GeoSite Whitelist)**:
   - اتصال مستقیم (Direct) برای کلیه وب‌سایت‌های ایرانی (`geosite:ir` و `geosite:category-ir`).
   - اتصال مستقیم برای تمامی رنج آی‌پی‌های ایران (`geoip:ir` و `geoip:private`).
   - بانک‌ها، درگاه‌های پرداخت، و سایت‌های دولتی و بومی بدون نیاز به خاموش کردن فیلترشکن باز می‌شوند و ترافیک آن‌ها نیم‌بها محاسبه می‌شود.
   - استراتژی مسیریابی سخت‌گیرانه `domainStrategy: "IPIfNonMatch"`.

---

## ساختار پروژه (Clean Architecture + MVVM)

```
VpnClientAndroid/
├── app/
│   ├── build.gradle.kts
│   ├── libs/
│   │   └── libxray.aar                     # موتور هسته Xray (اختیاری یا بیلد شده)
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── assets/
│       │   ├── geoip.dat                   # دیتابیس رنج آی‌پی‌های ایران
│       │   └── geosite.dat                 # دیتابیس دامنه‌های .ir و بومی
│       ├── java/com/vpnclient/app/
│       │   ├── VpnApplication.kt
│       │   ├── MainActivity.kt
│       │   ├── data/
│       │   │   ├── api/                    # لایه شبکه Retrofit، لاگین، و مدل‌ها
│       │   │   ├── local/                  # رمزنگاری توکن و استخراج فایل‌های Geo
│       │   │   └── repository/             # احراز هویت و دریافت اشتراک
│       │   ├── vpn/
│       │   │   ├── core/
│       │   │   │   ├── XrayConfigBuilder.kt# سازنده JSON پیکربندی Xray با رول‌های ایران
│       │   │   │   ├── VlessParser.kt      # پارسر پروتکل VLESS / Reality
│       │   │   │   └── PingTester.kt       # تستر پینگ SOCKS محلی
│       │   │   └── service/
│       │   │       ├── VpnState.kt         # وضعیت‌های اتصال (متصل، در حال اتصال، قطع)
│       │   │       └── XrayVpnService.kt   # مدیریت اینترفیس TUN و لایف‌سایکل VPN
│       │   └── ui/
│       │       ├── theme/                  # رنگ‌ها، تایپوگرافی و تم RTL
│       │       ├── components/             # دکمه ضربان‌دار، کارت مصرف حجم، ارقام فارسی
│       │       ├── login/                  # صفحه لاگین با نام کاربری و رمز عبور
│       │       └── dashboard/              # داشبورد یکپارچه مینیمال فارسی
```

---

## تنظیم آدرس سرور احراز هویت (Backend API)

در فایل `app/src/main/java/com/vpnclient/app/data/api/ApiClient.kt`، آدرس سرور میدل‌ویر یا بات خود را وارد کنید:

```kotlin
object ApiClient {
    var BASE_URL = "https://api.yourdomain.com/"
}
```

### نمونه اندپوینت‌های مورد نیاز در بک‌اند شما:

1. **لاگین (`POST /api/v1/auth/login`)**:
   - ورودی: `{ "username": "user1", "password": "pass" }`
   - خروجی:
     ```json
     {
       "token": "secure_session_token",
       "subUrl": "https://your-3xui-panel.com/sub/TOKEN",
       "profile": {
         "username": "user1",
         "trafficLimitBytes": 53687091200,
         "trafficUsedBytes": 12884901888,
         "expireTimestamp": 1782345600000
       }
     }
     ```

2. **بروزرسانی وضعیت و کانفیگ (`GET /api/v1/user/subscription`)**:
   - هدر: `Authorization: Bearer <token>`
   - خروجی: اطلاعات حجم باقیمانده و کانفیگ‌های بروزرسانی شده.

---

## نحوه بیلد و خروجی در Android Studio

1. پوشه `C:\Users\Sid\Documents\Coding\VpnClientAndroid` را در **Android Studio** باز کنید (`File -> Open`).
2. اجازه دهید Gradle فرآیند Sync را به اتمام برساند.
3. فایل‌های `geoip.dat` و `geosite.dat` را در مسیر `app/src/main/assets/` قرار دهید (دستورالعمل دانلود در همان پوشه قرار دارد).
4. فایل `libxray.aar` را در `app/libs/` کپی کنید.
5. از منوی بالا گزینه **Build -> Build Bundle(s) / APK(s) -> Build APK(s)** را انتخاب کنید.
