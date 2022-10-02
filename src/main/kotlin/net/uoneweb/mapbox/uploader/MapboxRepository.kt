package net.uoneweb.mapbox.uploader

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import mu.KotlinLogging
import net.uoneweb.mapbox.uploader.mapbox.PublishResponse
import net.uoneweb.mapbox.uploader.mapbox.Tileset
import net.uoneweb.mapbox.uploader.mapbox.TilesetSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Repository
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate
import java.util.Arrays.stream
import java.util.stream.Collectors

@Repository
class MapboxRepository {
    @Autowired
    private lateinit var restTemplate: RestTemplate

    @Autowired
    private lateinit var mapboxConfig: MapboxConfig

    private val logger = KotlinLogging.logger { }

    init {
        ObjectMapper().registerModule(KotlinModule.Builder().build())
    }

    fun listStyles() {
        val res = restTemplate.getForObject(
            "https://api.mapbox.com/styles/v1/{username}?access_token={token}", String::class.java,
            mapboxConfig.user,
            mapboxConfig.token
        )
        logger.info { res.toString() }
    }

    fun listTilesetSources(): List<TilesetSource> {
        val res = restTemplate.getForEntity(
            "https://api.mapbox.com/tilesets/v1/sources/{username}?access_token={token}",
            Array<TilesetSource>::class.java,
            mapboxConfig.user,
            mapboxConfig.token
        )
        logger.info {
            stream(res.body).map(TilesetSource::toString)
                .collect(Collectors.joining(","))
        }
        return res.body?.toList() ?: listOf()
    }

    fun getTilesetSource(tilesetSourceId: String): TilesetSource? {
        try {
            val res = restTemplate.getForEntity(
                "https://api.mapbox.com/tilesets/v1/sources/{username}/{id}?access_token={token}",
                TilesetSource::class.java,
                mapboxConfig.user,
                tilesetSourceId,
                mapboxConfig.token
            )
            return res.body
        } catch (e: RestClientResponseException) {
            if (e.rawStatusCode == HttpStatus.NOT_FOUND.value()) {
                return null
            }
            throw e
        }
    }

    fun createTilesetSource(tilesetSourceId: String, body: Any): TilesetSource? {
        val multiParts = LinkedMultiValueMap<String, Any>()
        multiParts.add("file", body)

        val request = RequestEntity.post(
            "https://api.mapbox.com/tilesets/v1/sources/{username}/{id}?access_token={token}",
            mapboxConfig.user,
            tilesetSourceId,
            mapboxConfig.token
        ).contentType(MediaType.MULTIPART_FORM_DATA)
            .body(multiParts)
        val res = restTemplate.exchange(request, TilesetSource::class.java)

        logger.info { res.body.toString() }
        return res.body
    }

    fun updateTilesetSource(tilesetSourceId: String, body: Any): TilesetSource? {
        val multiParts = LinkedMultiValueMap<String, Any>()
        multiParts.add("file", body)

        val request = RequestEntity.put(
            "https://api.mapbox.com/tilesets/v1/sources/{username}/{id}?access_token={token}",
            mapboxConfig.user,
            tilesetSourceId,
            mapboxConfig.token
        ).contentType(MediaType.MULTIPART_FORM_DATA)
            .body(multiParts)
        val res = restTemplate.exchange(request, TilesetSource::class.java)

        logger.info { res.body.toString() }
        return res.body
    }

    fun deleteTilesetSource(tilesetSourceId: String) {
        val res = restTemplate.delete(
            "https://api.mapbox.com/tilesets/v1/sources/{username}/{id}?access_token={token}",
            mapboxConfig.user,
            tilesetSourceId,
            mapboxConfig.token
        )
        logger.info { res.toString() }
    }

    fun createTileset(tilesetId: String, tilesetName: String, recipe: Recipe) {
        val createTilesetRequest = CreateTilesetRequest(recipe, tilesetName)

        val request = RequestEntity.post(
            "https://api.mapbox.com/tilesets/v1/{tileset}?access_token={token}",
            String.format("%s.%s", mapboxConfig.user, tilesetId),
            mapboxConfig.token
        ).contentType(MediaType.APPLICATION_JSON)
            .body(createTilesetRequest)

        logger.info {
            val objectMapper = ObjectMapper()
            objectMapper.writeValueAsString(createTilesetRequest)
        }
        val res = restTemplate.exchange(request, Void::class.java)

        logger.info { res.body.toString() }
    }

    fun listTileset(): List<Tileset> {
        val res = restTemplate.getForEntity(
            "https://api.mapbox.com/tilesets/v1/{username}?access_token={token}", Array<Tileset>::class.java,
            mapboxConfig.user,
            mapboxConfig.token
        )
        logger.info { stream(res.body).map(Tileset::toString).collect(Collectors.joining(",")) }
        return res.body?.toList() ?: listOf()
    }

    fun deleteTileset(tilesetId: String) {
        val res = restTemplate.delete(
            "https://api.mapbox.com/tilesets/v1/{tileset}?access_token={token}",
            String.format("%s.%s", mapboxConfig.user, tilesetId),
            mapboxConfig.token
        )
        logger.info { res.toString() }
    }

    fun publishTileset(tilesetId: String): String {
        val request = RequestEntity.post(
            "https://api.mapbox.com/tilesets/v1/{tileset}/publish?access_token={token}",
            String.format("%s.%s", mapboxConfig.user, tilesetId),
            mapboxConfig.token
        ).build()

        val res = restTemplate.exchange(request, PublishResponse::class.java)

        logger.info { res.toString() }
        return res.body?.jobId ?: ""
    }

    fun listJobs(tilesetId: String) {
        val res = restTemplate.getForObject<String>(
            "https://api.mapbox.com/tilesets/v1/{tileset}/jobs?access_token={token}", String::class.java,
            String.format("%s.%s", mapboxConfig.user, tilesetId),
            mapboxConfig.token
        )
        logger.info { res.toString() }
    }

    fun getJob(tilesetId: String, jobId: String) {
        val res = restTemplate.getForObject<String>(
            "https://api.mapbox.com/tilesets/v1/{tileset}/jobs/{job_id}?access_token={token}", String::class.java,
            String.format("%s.%s", mapboxConfig.user, tilesetId),
            jobId,
            mapboxConfig.token
        )
        logger.info { res.toString() }
    }
}