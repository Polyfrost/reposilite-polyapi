package cc.polyfrost.plugin.polyapi.infra

import cc.polyfrost.plugin.polyapi.PolyFacade
import cc.polyfrost.plugin.polyapi.api.PolyRequest
import com.reposilite.maven.infrastructure.MavenRoutes
import com.reposilite.web.api.ReposiliteRoute
import com.reposilite.web.routing.RouteMethod

internal class PolyEndpoints(polyFacade: PolyFacade) : MavenRoutes(polyFacade.mavenFacade) {
    private val releaseRoute = ReposiliteRoute<Any>("/poly/release", RouteMethod.GET) {
        accessed {
            val platform = requireParameter("platform")
            val version = requireParameter("version")

            response = PolyRequest(this?.identifier, null, version, platform)
                .let { polyFacade.resolvePolyRequest(it) }
        }
    }

    override val routes = routes(releaseRoute)
}