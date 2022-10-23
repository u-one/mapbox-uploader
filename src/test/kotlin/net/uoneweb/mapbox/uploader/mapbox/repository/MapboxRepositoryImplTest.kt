package net.uoneweb.mapbox.uploader.mapbox.repository

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.http.Body
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import com.google.gson.JsonObject
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import net.uoneweb.mapbox.uploader.MapboxConfig
import net.uoneweb.mapbox.uploader.mapbox.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.web.client.RestTemplate

class MapboxRepositoryImplTest {

    private lateinit var mapboxRepository: MapboxRepositoryImpl

    @RegisterExtension
    val mockMapboxApi: WireMockExtension = WireMockExtension.newInstance()
        .options(
            WireMockConfiguration.wireMockConfig()
                .port(8080)
        )
        .failOnUnmatchedRequests(false)
        .build()

    @BeforeEach
    fun setUp() {
        val restTemplate = RestTemplate()
        val mapboxConfig = MapboxConfig().apply {
            host = "http://localhost:8080/mapbox"
            user = "test-user"
            token = "test-token"
        }
        mapboxRepository = MapboxRepositoryImpl(restTemplate, mapboxConfig)
    }

    @Test
    fun createTileset_Success_ReturnsTileset() {
        mockMapboxApi.stubFor(
            post("/mapbox/tilesets/v1/test-user.tileset-test-1?access_token=test-token")
                .withHeader("Content-Type", containing("application/json"))
                .withRequestBody(
                    equalToJson(
                        "{" +
                                "  \"recipe\": {" +
                                "    \"version\": 1," +
                                "    \"layers\": {" +
                                "      \"test-layer-1\": {" +
                                "        \"source\": \"mapbox://tileset-source/test-user/tileset-source-test-1\"," +
                                "        \"minzoom\": 0," +
                                "        \"maxzoom\": 5" +
                                "      }" +
                                "    }" +
                                "  }," +
                                "  \"name\": \"tileset-name-test-1\"," +
                                "  \"description\": \"\"," +
                                "  \"attribution\": []" +
                                "}"
                    )
                )
                .willReturn(
                    okJson(
                        "{" +
                                "\"message\": \"Successfully created empty tileset tileset-test-1. " +
                                "Publish your tileset to begin processing your data into vector tiles.\"" +
                                "}"
                    ).withHeader("Connection", "close") // to avoid i/o error sometimes occurs in test
                )
        )

        mapboxRepository.createTileset(
            "tileset-test-1",
            "tileset-name-test-1",
            Recipe(
                1, mapOf(
                    Pair("test-layer-1", Layer("mapbox://tileset-source/test-user/tileset-source-test-1", 0, 5))
                )
            )
        )
    }

    @Test
    fun listTileset_Success_ReturnsTilesetList() {
        mockMapboxApi.stubFor(
            get("/mapbox/tilesets/v1/test-user?access_token=test-token").willReturn(
                okJson(
                    "[" +
                            "  {" +
                            "    \"type\": \"vector\"," +
                            "    \"id\": \"test-user.tileset-test-1\"," +
                            "    \"name\": \"tileset-name-test-1\"," +
                            "    \"center\": [" +
                            "      139.7625732421875," +
                            "      35.679609609368576," +
                            "      5" +
                            "    ]," +
                            "    \"created\": \"2022-10-02T15:55:28.113Z\"," +
                            "    \"modified\": \"2022-10-22T07:24:33.210Z\"," +
                            "    \"visibility\": \"private\"," +
                            "    \"description\": \"This is description.\"," +
                            "    \"filesize\": 123," +
                            "    \"status\": \"available\"," +
                            "    \"tileset_precisions\": {" +
                            "      \"free\": 0" +
                            "    }," +
                            "    \"created_by_client\": null" +
                            "  }\n" +
                            "]"
                ).withHeader("Connection", "close") // to avoid i/o error sometimes occurs in test
            )
        )
        val tileSets = mapboxRepository.listTileset()
        assertThat(tileSets).isNotEmpty
        assertThat(tileSets).containsAll(
            listOf(
                Tileset(
                    "vector",
                    arrayOf(139.7625732421875, 35.679609609368576, 5.0),
                    "2022-10-02T15:55:28.113Z",
                    "This is description.",
                    123,
                    "test-user.tileset-test-1",
                    "2022-10-22T07:24:33.210Z",
                    "tileset-name-test-1",
                    "private",
                    "available"
                )
            )
        )
    }

    @Test
    fun createTilesetSource_Success_ReturnsTilesetSource() {
        mockMapboxApi.stubFor(
            post("/mapbox/tilesets/v1/sources/test-user/test-tileset-source-id?access_token=test-token")
                .withMultipartRequestBody(
                    aMultipart()
                        .withName("file")
                        .withHeader("Content-Disposition", containing("filename=\"file.geojson\""))
                        .withHeader("Content-Type", containing("application/octet-stream"))
                        .withBody(equalToJson("{\"type\":\"Feature\",\"id\":\"1\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[139.76293,35.67871]},\"properties\":{\"name\":\"tokyo\"}}\n"))
                )
                .willReturn(
                    okJson(
                        "{\n" +
                                "  \"file_size\": 12345,\n" +
                                "  \"files\": 1,\n" +
                                "  \"id\": \"mapbox://tileset-source/test-user/tileset-source-test-1\",\n" +
                                "  \"source_size\": 12345\n" +
                                "}"
                    ).withHeader("Connection", "close") // to avoid i/o error sometimes occurs in test
                )
        )

        val properties = JsonObject()
        properties.addProperty("name", "tokyo")
        val feature = Feature.fromGeometry(Point.fromLngLat(139.76293, 35.67871), properties, "1")

        val tilesetSource =
            mapboxRepository.createTilesetSource(TilesetSourceId("test-tileset-source-id"), feature.toJson())
        assertThat(tilesetSource).isEqualTo(
            TilesetSource(
                TilesetSourceId("mapbox://tileset-source/test-user/tileset-source-test-1"),
                1,
                12345,
                "12345"
            )
        )
    }

    @Test
    fun updateTilesetSource_Success_ReturnsTilesetSource() {
        mockMapboxApi.stubFor(
            put("/mapbox/tilesets/v1/sources/test-user/tileset-source-test-1?access_token=test-token")
                .withMultipartRequestBody(
                    aMultipart()
                        .withName("file")
                        .withHeader("Content-Disposition", containing("filename=\"file.geojson\""))
                        .withHeader("Content-Type", containing("application/octet-stream"))
                        .withBody(equalToJson("{\"type\":\"Feature\",\"id\":\"1\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[139.76293,35.67871]},\"properties\":{\"name\":\"tokyo\"}}"))
                )
                .willReturn(
                    okJson(
                        "{" +
                                "  \"file_size\": 12345," +
                                "  \"files\": 1," +
                                "  \"id\": \"mapbox://tileset-source/test-user/tileset-source-test-1\"," +
                                "  \"source_size\": 12345" +
                                "}"
                    ).withHeader("Connection", "close") // to avoid i/o error sometimes occurs in test
                )
        )

        val properties = JsonObject()
        properties.addProperty("name", "tokyo")
        val feature = Feature.fromGeometry(Point.fromLngLat(139.76293, 35.67871), properties, "1")

        val tilesetSource =
            mapboxRepository.updateTilesetSource(TilesetSourceId("tileset-source-test-1"), feature.toJson())

        assertThat(tilesetSource).isEqualTo(
            TilesetSource(
                TilesetSourceId("mapbox://tileset-source/test-user/tileset-source-test-1"),
                1,
                12345,
                "12345"
            )
        )
    }

    @Test
    fun getTilesetSource_Success_ReturnsTilesetSource() {
        mockMapboxApi.stubFor(
            WireMock.get("/mapbox/tilesets/v1/sources/test-user/test-tileset-source-id?access_token=test-token")
                .willReturn(
                    WireMock.okJson(
                        "{\n" +
                                "  \"id\": \"mapbox://tileset-source/test-user/tileset-source-test-1\",\n" +
                                "  \"files\": 1,\n" +
                                "  \"size\": 119,\n" +
                                "  \"size_nice\": \"119B\"\n" +
                                "}"
                    ).withHeader("Connection", "close") // to avoid i/o error sometimes occurs in test
                )
        )

        val tilesetSource = mapboxRepository.getTilesetSource(TilesetSourceId("test-tileset-source-id"))
        assertThat(tilesetSource).isEqualTo(
            TilesetSource(
                TilesetSourceId("mapbox://tileset-source/test-user/tileset-source-test-1"),
                1,
                119,
                "119B"
            )
        )
    }

    @Test
    fun getTilesetSource_NotFound_ReturnsNull() {
        mockMapboxApi.stubFor(
            WireMock.get("/mapbox/tilesets/v1/sources/test-user/test-tileset-source-id?access_token=test-token")
                .willReturn(
                    notFound().withResponseBody(
                        Body(
                            "{\n" +
                                    "  \"message\": \"mapbox://tileset-source/backflip/tileset-source-test-1 does not exist.\"\n" +
                                    "}"
                        )
                    ).withHeader("Connection", "close") // to avoid i/o error sometimes occurs in test
                )
        )

        val tilesetSource = mapboxRepository.getTilesetSource(TilesetSourceId("test-tileset-source-id"))
        assertThat(tilesetSource).isNull()
    }

    @Test
    fun listTilesetSource_Success_ReturnsTilesetSourceList() {
        mockMapboxApi.stubFor(
            get("/mapbox/tilesets/v1/sources/test-user?access_token=test-token").willReturn(
                okJson(
                    "[" +
                            "  {" +
                            "    \"id\": \"mapbox://tileset-source/test-user/tileset-source-test-1\"," +
                            "    \"size\": 123," +
                            "    \"files\": 1" +
                            "  }" +
                            "]"
                ).withHeader("Connection", "close") // to avoid i/o error sometimes occurs in test
            )
        )

        val tileSetSrcs = mapboxRepository.listTilesetSources()
        assertThat(tileSetSrcs).isNotEmpty
        assertThat(tileSetSrcs).containsAll(
            listOf(
                TilesetSource(
                    TilesetSourceId("mapbox://tileset-source/test-user/tileset-source-test-1"),
                    1,
                    123
                )
            )
        )
    }
}