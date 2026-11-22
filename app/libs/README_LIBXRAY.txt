========================================================================
LibXray AAR Binary Integration Guide
========================================================================

Place the compiled `libxray.aar` inside this `app/libs/` directory.

How to obtain or build LibXray for Android:
1. Pre-built AAR releases:
   Download the latest release from:
   https://github.com/XTLS/libxray/releases
   or
   https://github.com/2dust/v2rayNG (extract `libv2ray.aar` from their libs directory)

2. Building from source via gomobile:
   git clone https://github.com/XTLS/libxray.git
   cd libxray
   gomobile bind -target=android -androidapi 26 -ldflags="-s -w" ./...

Note:
The application uses reflection-based invocation in `XrayVpnService.kt`, so the project
compiles and runs in Android Studio without throwing unresolved symbol errors even before
the `.aar` is dropped into this folder. Once `libxray.aar` is added to this directory,
Xray-core is loaded automatically at runtime.
========================================================================
