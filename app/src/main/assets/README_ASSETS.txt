========================================================================
GeoIP & GeoSite Routing Assets for Iran Whitelist
========================================================================

Place the following two binary files in this directory (`app/src/main/assets/`):
1. `geoip.dat`
2. `geosite.dat`

Recommended Sources for Iran Whitelist (Updated Weekly):
- Iran GeoSite: https://github.com/bootmortis/iran-hosted-domains/releases/latest/download/geosite.dat
- Iran GeoIP: https://github.com/v2fly/geoip/releases/latest/download/geoip.dat
- Loyalsoldier Rules: https://github.com/Loyalsoldier/v2ray-rules-dat/releases

When the application launches for the first time, `AssetManagerHelper.kt` will
automatically extract both files into the app's internal private storage directory
(`context.filesDir/xray_assets/`), which Xray-core accesses directly for resolving
`geosite:ir`, `geosite:category-ir`, and `geoip:ir`.
========================================================================
