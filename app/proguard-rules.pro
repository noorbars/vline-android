# Keep LibXray native/JNI and wrapper classes
-keep class libxray.** { *; }
-keep interface libxray.** { *; }
-keep class io.nekohasekai.sagernet.** { *; }

# Keep Network DTOs for GSON serialization
-keepclassmembers class com.vpnclient.app.data.api.models.** { <fields>; }
-keep class com.vpnclient.app.data.api.models.** { *; }

# Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory { *; }
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler { *; }
