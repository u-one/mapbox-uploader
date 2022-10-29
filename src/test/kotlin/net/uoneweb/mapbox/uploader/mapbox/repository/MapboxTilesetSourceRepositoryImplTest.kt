package net.uoneweb.mapbox.uploader.mapbox.repository

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.http.Body
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import com.google.gson.JsonObject
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import net.uoneweb.mapbox.uploader.MapboxConfig
import net.uoneweb.mapbox.uploader.mapbox.TilesetSource
import net.uoneweb.mapbox.uploader.mapbox.TilesetSourceId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.web.client.RestTemplate

class MapboxTilesetSourceRepositoryImplTest {
    private lateinit var repository: MapboxTilesetSourceRepositoryImpl

    @RegisterExtension
    val mockMapboxApi: WireMockExtension = WireMockExtension.newInstance()
        .options(
            WireMockConfiguration.wireMockConfig()
                .dynamicPort()
        )
        .failOnUnmatchedRequests(false)
        .build()

    @BeforeEach
    fun setUp() {
        val restTemplate = RestTemplate()

        val mockInfo = mockMapboxApi.runtimeInfo

        val mapboxConfig = MapboxConfig().apply {
            host = String.format("http://localhost:%d/mapbox", mockInfo.httpPort)
            user = "test-user"
            token = "test-token"
        }
        repository = MapboxTilesetSourceRepositoryImpl(restTemplate, mapboxConfig)
    }

    @Test
    fun createTilesetSource_Success_ReturnsTilesetSource() {
        mockMapboxApi.stubFor(
            WireMock.post("/mapbox/tilesets/v1/sources/test-user/test-tileset-source-id?access_token=test-token")
                .withMultipartRequestBody(
                    WireMock.aMultipart()
                        .withName("file")
                        .withHeader("Content-Disposition", WireMock.containing("filename=\"file.geojson\""))
                        .withHeader("Content-Type", WireMock.containing("application/octet-stream"))
                        .withBody(WireMock.equalToJson("{\"type\":\"Feature\",\"id\":\"1\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[139.76293,35.67871]},\"properties\":{\"name\":\"tokyo\"}}\n"))
                )
                .willReturn(
                    WireMock.okJson(
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
            repository.createTilesetSource(TilesetSourceId("test-tileset-source-id"), feature.toJson())
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
            WireMock.put("/mapbox/tilesets/v1/sources/test-user/tileset-source-test-1?access_token=test-token")
                .withMultipartRequestBody(
                    WireMock.aMultipart()
                        .withName("file")
                        .withHeader("Content-Disposition", WireMock.containing("filename=\"file.geojson\""))
                        .withHeader("Content-Type", WireMock.containing("application/octet-stream"))
                        .withBody(WireMock.equalToJson("{\"type\":\"Feature\",\"id\":\"1\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[139.76293,35.67871]},\"properties\":{\"name\":\"tokyo\"}}"))
                )
                .willReturn(
                    WireMock.okJson(
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
            repository.updateTilesetSource(TilesetSourceId("tileset-source-test-1"), feature.toJson())

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

        val tilesetSource = repository.getTilesetSource(TilesetSourceId("test-tileset-source-id"))
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
                    WireMock.notFound().withResponseBody(
                        Body(
                            "{\n" +
                                    "  \"message\": \"mapbox://tileset-source/backflip/tileset-source-test-1 does not exist.\"\n" +
                                    "}"
                        )
                    ).withHeader("Connection", "close") // to avoid i/o error sometimes occurs in test
                )
        )

        val tilesetSource = repository.getTilesetSource(TilesetSourceId("test-tileset-source-id"))
        assertThat(tilesetSource).isNull()
    }

    @Test
    fun listTilesetSource_Success_ReturnsTilesetSourceList() {
        mockMapboxApi.stubFor(
            WireMock.get("/mapbox/tilesets/v1/sources/test-user?access_token=test-token").willReturn(
                WireMock.okJson(
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

        val tileSetSrcs = repository.listTilesetSources()
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