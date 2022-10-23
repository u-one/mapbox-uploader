package net.uoneweb.mapbox.uploader.mapbox.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import mu.KotlinLogging
import net.uoneweb.mapbox.uploader.MapboxConfig
import net.uoneweb.mapbox.uploader.mapbox.*
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Repository
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate
import java.util.*
import java.util.stream.Collectors
import kotlin.streams.toList

@Repository
class MapboxRepositoryImpl(private val restTemplate: RestTemplate, private val mapboxConfig: MapboxConfig) :
    MapboxRepository {
    private val logger = KotlinLogging.logger { }

    init {
        ObjectMapper().registerModule(KotlinModule.Builder().build())
    }

    override fun listTilesetSources(): List<TilesetSource> {
        val res = restTemplate.getForEntity(
            mapboxConfig.host + "/tilesets/v1/sources/{username}?access_token={token}",
            Array<TilesetSourceResponse>::class.java,
            mapboxConfig.user,
            mapboxConfig.token
        )
        logger.info {
            Arrays.stream(res.body).map(TilesetSourceResponse::toString)
                .collect(Collectors.joining(","))
        }
        return res.body?.toList()?.stream()?.map {
            TilesetSource(TilesetSourceId(it.id), it.files, it.size)
        }?.toList() ?: listOf()

    }

    override fun getTilesetSource(tilesetSourceId: TilesetSourceId): TilesetSource? {
        try {
            val resp = restTemplate.getForObject(
                mapboxConfig.host + "/tilesets/v1/sources/{username}/{id}?access_token={token}",
                TilesetSourceResponse::class.java,
                mapboxConfig.user,
                tilesetSourceId.getSimpleId(),
                mapboxConfig.token
            ) ?: return null
            return TilesetSource(TilesetSourceId(resp.id), resp.files, resp.size, resp.sizeNice)
        } catch (e: RestClientResponseException) {
            if (e.rawStatusCode == HttpStatus.NOT_FOUND.value()) {
                return null
            }
            throw e
        }
    }

    override fun createTilesetSource(tilesetSourceId: TilesetSourceId, body: Any): TilesetSource {
        val multiParts = LinkedMultiValueMap<String, Any>()
        multiParts.add("file", body)

        val request = RequestEntity.post(
            mapboxConfig.host + "/tilesets/v1/sources/{username}/{id}?access_token={token}",
            mapboxConfig.user,
            tilesetSourceId.getSimpleId(),
            mapboxConfig.token
        ).contentType(MediaType.MULTIPART_FORM_DATA)
            .body(multiParts)
        val res = restTemplate.exchange(request, CreateTilesetSourceResponse::class.java)

        logger.info { res.body.toString() }

        res.body?.let {
            return TilesetSource(TilesetSourceId(it.id), it.files, it.fileSize, it.sourceSize.toString())
        }
        throw RuntimeException("createTilesetSource response is null")
    }

    override fun updateTilesetSource(tilesetSourceId: TilesetSourceId, body: Any): TilesetSource? {
        val multiParts = LinkedMultiValueMap<String, Any>()
        multiParts.add("file", body)

        val request = RequestEntity.put(
            mapboxConfig.host + "/tilesets/v1/sources/{username}/{id}?access_token={token}",
            mapboxConfig.user,
            tilesetSourceId.getSimpleId(),
            mapboxConfig.token
        ).contentType(MediaType.MULTIPART_FORM_DATA)
            .body(multiParts)
        val res = restTemplate.exchange(request, TilesetSource::class.java)

        logger.info { res.body.toString() }
        return res.body
    }

    override fun deleteTilesetSource(tilesetSourceId: TilesetSourceId) {
        val res = restTemplate.delete(
            mapboxConfig.host + "/tilesets/v1/sources/{username}/{id}?access_token={token}",
            mapboxConfig.user,
            tilesetSourceId,
            mapboxConfig.token
        )
        logger.info { res.toString() }
    }

    override fun createTileset(tilesetId: String, tilesetName: String, recipe: Recipe) {
        val createTilesetRequest = CreateTilesetRequest(recipe, tilesetName)

        val request = RequestEntity.post(
            mapboxConfig.host + "/tilesets/v1/{tileset}?access_token={token}",
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

    override fun listTileset(): List<Tileset> {
        val res = restTemplate.getForEntity(
            mapboxConfig.host + "/tilesets/v1/{username}?access_token={token}", Array<Tileset>::class.java,
            mapboxConfig.user,
            mapboxConfig.token
        )
        logger.info { Arrays.stream(res.body).map(Tileset::toString).collect(Collectors.joining(",")) }
        return res.body?.toList() ?: listOf()
    }

    override fun deleteTileset(tilesetId: String) {
        val res = restTemplate.delete(
            mapboxConfig.host + "/tilesets/v1/{tileset}?access_token={token}",
            String.format("%s.%s", mapboxConfig.user, tilesetId),
            mapboxConfig.token
        )
        logger.info { res.toString() }
    }

    override fun publishTileset(tilesetId: String): String {
        val request = RequestEntity.post(
            mapboxConfig.host + "/tilesets/v1/{tileset}/publish?access_token={token}",
            String.format("%s.%s", mapboxConfig.user, tilesetId),
            mapboxConfig.token
        ).build()

        val res = restTemplate.exchange(request, PublishResponse::class.java)

        logger.info { res.toString() }
        return res.body?.jobId ?: ""
    }

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