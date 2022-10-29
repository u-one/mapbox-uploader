package net.uoneweb.mapbox.uploader.mapbox.repository

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import net.uoneweb.mapbox.uploader.MapboxConfig
import net.uoneweb.mapbox.uploader.mapbox.Layer
import net.uoneweb.mapbox.uploader.mapbox.Recipe
import net.uoneweb.mapbox.uploader.mapbox.Tileset
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.client.RestTemplate

class MapboxRepositoryImplTest {

    private lateinit var mapboxRepository: MapboxRepositoryImpl

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
        restTemplate.requestFactory = HttpComponentsClientHttpRequestFactory()

        val mockInfo = mockMapboxApi.runtimeInfo

        val mapboxConfig = MapboxConfig().apply {
            host = String.format("http://localhost:%d/mapbox", mockInfo.httpPort)
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
    fun updateTilesetRecipe_Success() {
        mockMapboxApi.stubFor(
            patch(WireMock.urlPathEqualTo("/mapbox/tilesets/v1/test-user.tileset-test-1/recipe"))
                .withQueryParam("access_token", equalTo("test-token"))
                .withHeader("Content-Type", containing("application/json"))
                .withRequestBody(
                    equalToJson(
                        "{" +
                                "  \"version\": 1," +
                                "  \"layers\": {" +
                                "    \"test-layer-1\": {" +
                                "      \"source\": \"mapbox://tileset-source/test-user/tileset-source-test-1\"," +
                                "      \"minzoom\": 0," +
                                "      \"maxzoom\": 5" +
                                "    }" +
                                "  }" +
                                "}"
                    )
                )
                .willReturn(
                    okJson(
                        "{" +
                                "\"message\": \"Successfully updated recipe. " +
                                "}"
                    ).withHeader("Connection", "close") // to avoid i/o error sometimes occurs in test
                )
        )

        mapboxRepository.updateTilesetRecipe(
            "tileset-test-1",
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


}