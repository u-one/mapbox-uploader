package net.uoneweb.mapbox.uploader.mapbox.repository

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import net.uoneweb.mapbox.uploader.MapboxConfig
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.web.client.RestTemplate

class MapboxJobRepositoryImplTest {
    private lateinit var repository: MapboxJobRepositoryImpl

    @RegisterExtension
    val mockMapboxApi: WireMockExtension = WireMockExtension.newInstance()
        .options(
            WireMockConfiguration.wireMockConfig().dynamicPort()
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
        repository = MapboxJobRepositoryImpl(restTemplate, mapboxConfig)
    }

    @Test
    fun getJob_Success() {
        mockMapboxApi.stubFor(
            WireMock.get("/mapbox/tilesets/v1/test-user.tileset-test-1/jobs/job_1_id?access_token=test-token")
                .willReturn(
                    WireMock.okJson(
                        "{\n" +
                                "  \"id\": \"unique_hash\",\n" +
                                "  \"stage\": \"success\",\n" +
                                "  \"created\": 1560981902377,\n" +
                                "  \"created_nice\": \"Wed Jun 19 2019 22:05:02 GMT+0000 (UTC)\",\n" +
                                "  \"published\": 1560982158721,\n" +
                                "  \"tilesetId\": \"user.id\",\n" +
                                "  \"errors\": [],\n" +
                                "  \"warnings\": [],\n" +
                                "  \"tileset_precisions\": { \"1m\": 658731.7540137176 },\n" +
                                "  \"layer_stats\": {\n" +
                                "    \"sample_pois\": {\n" +
                                "      \"total_tiles\": 71,\n" +
                                "      \"linestring_count\": 0,\n" +
                                "      \"capped\": 15,\n" +
                                "      \"maxzoom\": 12,\n" +
                                "      \"zooms\": {\n" +
                                "        \"0\": {\n" +
                                "          \"ymin\": 0,\n" +
                                "          \"ymax\": 0,\n" +
                                "          \"xmin\": 0,\n" +
                                "          \"xmax\": 0,\n" +
                                "          \"tiles\": 1,\n" +
                                "          \"capped\": 1,\n" +
                                "          \"capped_list\": [ { \"layer_size\": 1337, \"tile\": \"0/0/0\" } ],\n" +
                                "          \"sum_size\": 512036,\n" +
                                "          \"sum_area\": 508164394.24620897,\n" +
                                "          \"avg_size\": 512036,\n" +
                                "          \"max_size\": 512036,\n" +
                                "          \"size_histogram\": [ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 ]\n" +
                                "        }\n" +
                                "      }\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"recipe\": {\n" +
                                "    \"version\": 1,\n" +
                                "    \"layers\": {\n" +
                                "      \"sample_pois\": {\n" +
                                "        \"minzoom\": 1,\n" +
                                "        \"maxzoom\": 12,\n" +
                                "        \"source\": \"mapbox://tileset-source/user/source\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  }\n" +
                                "}"
                    ).withHeader("Connection", "close") // to avoid i/o error sometimes occurs in test
                )
        )

        repository.getJob("tileset-test-1", "job_1_id")
    }

    @Test
    fun listJob_Success() {
        mockMapboxApi.stubFor(
            WireMock.get("/mapbox/tilesets/v1/test-user.tileset-test-1/jobs?access_token=test-token").willReturn(
                WireMock.okJson(
                    "[\n" +
                            "  {\n" +
                            "    \"id\": \"job_1_id\",\n" +
                            "    \"stage\": \"success\",\n" +
                            "    \"created\": 1560981902377,\n" +
                            "    \"created_nice\": \"Wed Jun 19 2019 22:05:02 GMT+0000 (UTC)\",\n" +
                            "    \"published\": 1560982158721,\n" +
                            "    \"tilesetId\": \"user.id\",\n" +
                            "    \"errors\": [],\n" +
                            "    \"warnings\": [],\n" +
                            "    \"tileset_precisions\": { \"1m\": 658731.7540137176 },\n" +
                            "    \"recipe\": {\n" +
                            "      \"version\": 1,\n" +
                            "      \"layers\": {\n" +
                            "        \"sample_pois\": {\n" +
                            "          \"minzoom\": 1,\n" +
                            "          \"maxzoom\": 12,\n" +
                            "          \"source\": \"mapbox://tileset-source/test-user/tilset-test-1\"\n" +
                            "        }\n" +
                            "      }\n" +
                            "    }\n" +
                            "  },\n" +
                            "  {\n" +
                            "    \"id\": \"job_2_id\",\n" +
                            "    \"stage\": \"processing\",\n" +
                            "    \"created\": 1560982159327,\n" +
                            "    \"created_nice\": \"Wed Jun 19 2019 22:09:19 GMT+0000 (UTC)\",\n" +
                            "    \"published\": 1560985238565,\n" +
                            "    \"tilesetId\": \"user.id\",\n" +
                            "    \"errors\": [],\n" +
                            "    \"warnings\": [],\n" +
                            "    \"recipe\": {\n" +
                            "      \"version\": 1,\n" +
                            "      \"layers\": {\n" +
                            "        \"sample_pois\": {\n" +
                            "          \"minzoom\": 1,\n" +
                            "          \"maxzoom\": 12,\n" +
                            "          \"source\": \"mapbox://tileset-source/test-user/tilset-test-2\"\n" +
                            "        }\n" +
                            "      }\n" +
                            "    }\n" +
                            "  }\n" +
                            "]"
                ).withHeader("Connection", "close") // to avoid i/o error sometimes occurs in test
            )
        )

        repository.listJobs("tileset-test-1")
    }
}