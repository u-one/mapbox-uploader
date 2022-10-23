package net.uoneweb.mapbox.uploader

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.http.Body
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import net.uoneweb.mapbox.uploader.mapbox.TilesetSource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.web.client.RestTemplate
import java.io.File

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
    fun createTilesetSource_Success_ReturnsTilesetSource() {
        mockMapboxApi.stubFor(
            post("/mapbox/tilesets/v1/sources/test-user/test-tileset-source-id?access_token=test-token")
                .withMultipartRequestBody(
                    aMultipart()
                        .withName("file")
                        .withHeader("Content-Type", containing("text/plain"))
                        .withBody(equalToJson("{\"type\":\"Feature\",\"id\":1,\"geometry\":{\"type\":\"Point\",\"coordinates\":[139.76293,35.67871]},\"properties\":{\"name\":\"tokyo\"}}\n"))
                )
                .willReturn(
                    okJson(
                        "{\n" +
                                "  \"file_size\": 12345,\n" +
                                "  \"files\": 1,\n" +
                                "  \"id\": \"mapbox://tileset-source/test-user/tileset-source-test-1\",\n" +
                                "  \"source_size\": 12345\n" +
                                "}"
                    )
                )
        )

        val body =
            "{\"type\":\"Feature\",\"id\":1,\"geometry\":{\"type\":\"Point\",\"coordinates\":[139.76293,35.67871]},\"properties\":{\"name\":\"tokyo\"}}\n" // line-delimited GeoJson
        val file = File("test.geojson")
        file.writeBytes(body.toByteArray())

        val tilesetSource = mapboxRepository.createTilesetSource("test-tileset-source-id", body)
        assertThat(tilesetSource).isEqualTo(
            TilesetSource(
                "mapbox://tileset-source/test-user/tileset-source-test-1",
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
                    )
                )
        )

        val tilesetSource = mapboxRepository.getTilesetSource("test-tileset-source-id")
        assertThat(tilesetSource).isEqualTo(
            TilesetSource(
                "mapbox://tileset-source/test-user/tileset-source-test-1",
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
                    )
                )
        )

        val tilesetSource = mapboxRepository.getTilesetSource("test-tileset-source-id")
        assertThat(tilesetSource).isNull()
    }
}