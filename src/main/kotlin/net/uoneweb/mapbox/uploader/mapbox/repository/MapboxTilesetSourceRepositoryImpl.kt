package net.uoneweb.mapbox.uploader.mapbox.repository

import mu.KotlinLogging
import net.uoneweb.mapbox.uploader.MapboxConfig
import net.uoneweb.mapbox.uploader.mapbox.MapboxTilesetSourceRepository
import net.uoneweb.mapbox.uploader.mapbox.TilesetSource
import net.uoneweb.mapbox.uploader.mapbox.TilesetSourceId
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate
import java.io.File
import java.util.*
import java.util.stream.Collectors
import kotlin.streams.toList

class MapboxTilesetSourceRepositoryImpl(
    private val restTemplate: RestTemplate,
    private val mapboxConfig: MapboxConfig
) : MapboxTilesetSourceRepository {

    private val logger = KotlinLogging.logger { }

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

    override fun createTilesetSource(tilesetSourceId: TilesetSourceId, body: String): TilesetSource {
        val request = RequestEntity.post(
            mapboxConfig.host + "/tilesets/v1/sources/{username}/{id}?access_token={token}",
            mapboxConfig.user,
            tilesetSourceId.getSimpleId(),
            mapboxConfig.token
        ).contentType(MediaType.MULTIPART_FORM_DATA)
            .body(createMultiPartFile(body))

        val res = restTemplate.exchange(request, CreateTilesetSourceResponse::class.java)

        logger.info { res.body.toString() }

        res.body?.let {
            return TilesetSource(TilesetSourceId(it.id), it.files, it.fileSize, it.sourceSize.toString())
        }
        throw RuntimeException("createTilesetSource response is null")
    }

    override fun updateTilesetSource(tilesetSourceId: TilesetSourceId, body: String): TilesetSource? {
        val request = RequestEntity.put(
            mapboxConfig.host + "/tilesets/v1/sources/{username}/{id}?access_token={token}",
            mapboxConfig.user,
            tilesetSourceId.getSimpleId(),
            mapboxConfig.token
        ).contentType(MediaType.MULTIPART_FORM_DATA)
            .body(createMultiPartFile(body))

        val res = restTemplate.exchange(request, CreateTilesetSourceResponse::class.java)

        logger.info { res.body.toString() }

        res.body?.let {
            return TilesetSource(TilesetSourceId(it.id), it.files, it.fileSize, it.sourceSize.toString())
        }
        throw RuntimeException("updateTilesetSource response is null")
    }

    private fun createMultiPartFile(text: String): LinkedMultiValueMap<String, Any> {
        val file = File("file.geojson")
        file.writeBytes(text.toByteArray())

        val multiParts = LinkedMultiValueMap<String, Any>()
        multiParts.add("file", FileSystemResource(file))
        return multiParts
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

}