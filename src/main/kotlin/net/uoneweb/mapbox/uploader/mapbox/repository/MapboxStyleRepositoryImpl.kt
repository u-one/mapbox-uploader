package net.uoneweb.mapbox.uploader.mapbox.repository

import mu.KotlinLogging
import net.uoneweb.mapbox.uploader.MapboxConfig
import net.uoneweb.mapbox.uploader.mapbox.MapboxStyleRepository
import org.springframework.web.client.RestTemplate

class MapboxStyleRepositoryImpl(private val restTemplate: RestTemplate, private val mapboxConfig: MapboxConfig) :
    MapboxStyleRepository {

    private val logger = KotlinLogging.logger { }

    override fun listStyles() {
        val res = restTemplate.getForObject(
            mapboxConfig.host + "/styles/v1/{username}?access_token={token}", String::class.java,
            mapboxConfig.user,
            mapboxConfig.token
        )
        logger.info { res.toString() }
    }
}