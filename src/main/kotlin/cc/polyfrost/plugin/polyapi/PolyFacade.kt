package cc.polyfrost.plugin.polyapi

import cc.polyfrost.plugin.polyapi.api.PolyRequest
import cc.polyfrost.plugin.polyapi.api.PolyResponse
import cc.polyfrost.plugin.polyapi.api.ReleaseResponse
import com.reposilite.journalist.Journalist
import com.reposilite.journalist.Logger
import com.reposilite.maven.MavenFacade
import com.reposilite.maven.api.LatestVersionResponse
import com.reposilite.maven.api.VersionLookupRequest
import com.reposilite.plugin.api.Facade
import com.reposilite.shared.ErrorResponse
import com.reposilite.shared.toErrorResponse
import com.reposilite.storage.api.Location
import com.reposilite.token.AccessTokenIdentifier
import io.javalin.http.HttpStatus
import panda.std.Result

class PolyFacade internal constructor(
    private val journalist: Journalist,
    val mavenFacade: MavenFacade,
) : Journalist, Facade {
    fun resolvePolyRequest(request: PolyRequest): Result<PolyResponse, ErrorResponse> {
        val (token, verify, version, platform) = request
        var response: PolyResponse? = null

        try {
            response = PolyResponse(
                oneconfig = getOneConfigResp(token, platform, version),
                loader = getLoaderResp(token, platform, version)
            )
        } catch (_: Exception) {
        }

        return response
            .let {
                Result.`when`(
                    it !== null,
                    { it },
                    { HttpStatus.NOT_FOUND.toErrorResponse("Specified platform and version could not be found.") })
            }
    }

    private fun getOneConfigResp(token: AccessTokenIdentifier?, platform: String, version: String): ReleaseResponse {
        val oneConfigPlatform = "oneconfig-$version-$platform"
        val oneConfigVersion = getReleaseVersion(token, platform).get().version
        return ReleaseResponse(
            url = "https://repo.polyfrost.cc/releases/cc/polyfrost/$oneConfigPlatform/$oneConfigVersion/$oneConfigPlatform-$oneConfigVersion-full.jar",
            checksum = "",
            verify = null
        )
    }

    private fun getLoaderResp(token: AccessTokenIdentifier?, platform: String, version: String): ReleaseResponse {
        val loaderPlatform = "oneconfig-loader-launchwrapper"
        val loaderVersion = getReleaseVersion(token, loaderPlatform)
        return ReleaseResponse(
            url = "https://repo.polyfrost.cc/releases/cc/polyfrost/$loaderPlatform/$loaderVersion/$loaderPlatform-$loaderVersion.jar",
            checksum = "",
            verify = null
        )
    }

    private fun getReleaseVersion(
        token: AccessTokenIdentifier?,
        platform: String
    ): Result<LatestVersionResponse, ErrorResponse> {
        return mavenFacade.findLatestVersion(
            VersionLookupRequest(
                accessToken = token,
                repository = mavenFacade.getRepository("releases")!!,
                gav = Location.of("cc.polyfrost.$platform")
            )
        ).onError {
            logger.error("Could not get version for platform!")
        }
    }

    override fun getLogger(): Logger =
        journalist.logger
}