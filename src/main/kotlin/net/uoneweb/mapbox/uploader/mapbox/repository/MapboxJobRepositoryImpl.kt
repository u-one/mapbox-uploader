package net.uoneweb.mapbox.uploader.mapbox.repository

import mu.KotlinLogging
import net.uoneweb.mapbox.uploader.MapboxConfig
import org.springframework.web.client.RestTemplate

class MapboxJobRepositoryImpl(private val restTemplate: RestTemplate, private val mapboxConfig: MapboxConfig) :
    MapboxJobRepository {

    private val logger = KotlinLogging.logger { }
   
    override fun listJobs(tilesetId: String) {
        val res = restTemplate.getForObject<String>(
            mapboxConfig.host + "/tilesets/v1/{tileset}/jobs?access_token={token}", String::class.java,
            String.format("%s.%s", mapboxConfig.user, tilesetId),
            mapboxConfig.token
        )
        logger.info { res.toString() }
    }

    override fun getJob(tilesetId: String, jobId: String) {
        val res = restTemplate.getForObject<String>(
            mapboxConfig.host + "/tilesets/v1/{tileset}/jobs/{job_id}?access_token={token}", String::class.java,
            String.format("%s.%s", mapboxConfig.user, tilesetId),
            jobId,
            mapboxConfig.token
        )
        logger.info { res.toString() }
    }
}