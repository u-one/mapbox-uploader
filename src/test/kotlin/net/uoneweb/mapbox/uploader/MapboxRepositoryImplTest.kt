package net.uoneweb.mapbox.uploader

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import net.uoneweb.mapbox.uploader.mapbox.TilesetSource
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
    fun getTilesetSourceSuccessReturnsTilesetSource() {
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
}