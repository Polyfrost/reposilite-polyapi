package cc.polyfrost.plugin.polyapi

import cc.polyfrost.plugin.polyapi.infra.PolyEndpoints
import com.reposilite.maven.MavenFacade
import com.reposilite.plugin.api.Facade
import com.reposilite.plugin.api.Plugin
import com.reposilite.plugin.api.ReposilitePlugin
import com.reposilite.plugin.event
import com.reposilite.plugin.facade
import com.reposilite.web.api.RoutingSetupEvent

@Plugin(name = "polyapi", dependencies = ["maven"])
class PolyPlugin : ReposilitePlugin() {
    override fun initialize(): Facade {
        val mavenFacade = facade<MavenFacade>()

        val polyFacade = PolyFacade(
            journalist = this,
            mavenFacade = mavenFacade
        )

        event { event: RoutingSetupEvent ->
            event.registerRoutes(PolyEndpoints(polyFacade))
        }

        return polyFacade
    }
}