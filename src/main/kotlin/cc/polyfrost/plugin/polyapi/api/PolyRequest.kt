package cc.polyfrost.plugin.polyapi.api

import com.reposilite.token.AccessTokenIdentifier

data class PolyRequest(
    val token: AccessTokenIdentifier?,
    val verify: String?,
    val version: String,
    val platform: String,
)