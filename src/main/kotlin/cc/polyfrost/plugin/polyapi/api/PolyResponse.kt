package cc.polyfrost.plugin.polyapi.api

data class PolyResponse(
    val oneconfig: ReleaseResponse,
    val loader: ReleaseResponse
)

data class ReleaseResponse(
    val url: String,
    val checksum: String,
    val verify: String?
)