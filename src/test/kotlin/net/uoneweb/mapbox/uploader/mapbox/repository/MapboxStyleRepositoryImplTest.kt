package net.uoneweb.mapbox.uploader.mapbox.repository

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import net.uoneweb.mapbox.uploader.MapboxConfig
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.web.client.RestTemplate

class MapboxStyleRepositoryImplTest {
    private lateinit var repository: MapboxStyleRepositoryImpl

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
        repository = MapboxStyleRepositoryImpl(restTemplate, mapboxConfig)
    }

    @Test
    fun listTilesetSource_Success_ReturnsTilesetSourceList() {
        mockMapboxApi.stubFor(
            WireMock.get("/mapbox/styles/v1/test-user?access_token=test-token").willReturn(
                WireMock.okJson(
                    "[" +
                            "  {" +
                            "    \"version\": 8," +
                            "    \"bearing\": 0," +
                            "    \"created\": \"2022-09-19T06:10:29.390Z\"," +
                            "    \"visibility\": \"private\"," +
                            "    \"sources\": {" +
                            "      \"composite\": {" +
                            "        \"url\": \"mapbox://mapbox.mapbox-terrain-v2,mapbox.mapbox-bathymetry-v2,mapbox.mapbox-streets-v8,test-user.tileset-test-1\"," +
                            "        \"type\": \"vector\"" +
                            "      }" +
                            "    }," +
                            "    \"name\": \"Streets\"," +
                            "    \"protected\": false," +
                            "    \"center\": [" +
                            "      139.12345678901234," +
                            "      35.12345678901234" +
                            "    ]," +
                            "    \"pitch\": 0," +
                            "    \"zoom\": 14.123456789012345," +
                            "    \"owner\": \"test-user\"," +
                            "    \"id\": \"0123456789abcdefghijklmno\"," +
                            "    \"modified\": \"2022-10-02T16:38:18.774Z\"" +
                            "  }" +
                            "]"
                )
            )
        )

        assertDoesNotThrow { repository.listStyles() }
    }
}