package net.uoneweb.mapbox.uploader

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.github.tomakehurst.wiremock.http.Body
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.JobRepositoryTestUtils
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource

@SpringBatchTest
@EnableAutoConfiguration
@ContextConfiguration(classes = [AppConfig::class, BatchConfig::class, MapboxRepositoryImpl::class])
@EnableConfigurationProperties(MapboxConfig::class)
@TestPropertySource(properties = ["mapbox.user = test-user", "mapbox.token: test-token", "mapbox.host: http://localhost:8080/mapbox"])
class MapboxUploaderApplicationTests {

    @Autowired
    lateinit var jobLauncerTestUtils: JobLauncherTestUtils

    @Autowired
    lateinit var jobRepositoryTestUtils: JobRepositoryTestUtils

    @BeforeEach
    fun clearJobExecutions() {
        jobRepositoryTestUtils.removeJobExecutions()
    }

    @RegisterExtension
    val mockMapboxApi: WireMockExtension = WireMockExtension.newInstance()
        .options(wireMockConfig().port(8080)).failOnUnmatchedRequests(false).build()

    @Test
    fun testJob_NewTilesetSource_SuccessToUpdate() {
        //mockMapboxApi.stubFor(WireMock.any(WireMock.anyUrl()).withHost(equalTo("localhost")).willReturn(WireMock.okJson("{}")))

        // list styles
        mockMapboxApi.stubFor(
            WireMock.get("/mapbox/styles/v1/test-user?access_token=test-token").willReturn(okJson("{}"))
        )

        // list tileset sources
        mockMapboxApi.stubFor(
            get("/mapbox/tilesets/v1/sources/test-user?access_token=test-token").willReturn(
                okJson(
                    "[\n" +
                            "  {\n" +
                            "    \"id\": \"mapbox://tileset-source/test-user/tileset-source-test-1\",\n" +
                            "    \"size\": 119,\n" +
                            "    \"files\": 1\n" +
                            "  }\n" +
                            "]"
                )
            )
        )

        // list tilesets
        mockMapboxApi.stubFor(
            get("/mapbox/tilesets/v1/test-user?access_token=test-token").willReturn(
                okJson(
                    "[\n" +
                            "  {\n" +
                            "    \"type\": \"vector\",\n" +
                            "    \"id\": \"test-user.tileset-test-1\",\n" +
                            "    \"name\": \"tileset-name-test-1\",\n" +
                            "    \"center\": [\n" +
                            "      139.7625732421875,\n" +
                            "      35.679609609368576,\n" +
                            "      5\n" +
                            "    ],\n" +
                            "    \"created\": \"2022-10-02T15:55:28.113Z\",\n" +
                            "    \"modified\": \"2022-10-22T07:24:33.210Z\",\n" +
                            "    \"visibility\": \"private\",\n" +
                            "    \"description\": \"\",\n" +
                            "    \"filesize\": 119,\n" +
                            "    \"status\": \"available\",\n" +
                            "    \"tileset_precisions\": {\n" +
                            "      \"free\": 0\n" +
                            "    },\n" +
                            "    \"created_by_client\": null\n" +
                            "  }\n" +
                            "]"
                )
            )
        )

        // get tileset source
        mockMapboxApi.stubFor(
            get("/mapbox/tilesets/v1/sources/test-user/tileset-source-test-1?access_token=test-token").willReturn(
                notFound().withResponseBody(
                    Body(
                        "{\n" +
                                "  \"message\": \"mapbox://tileset-source/backflip/tileset-source-test-1 does not exist.\"\n" +
                                "}"
                    )
                )
            )
        )

        // create tileset source
        mockMapboxApi.stubFor(
            post("/mapbox/tilesets/v1/sources/test-user/tileset-source-test-1?access_token=test-token").willReturn(
                okJson(
                    "{\n" +
                            "  \"file_size\": 10000,\n" +
                            "  \"files\": 1,\n" +
                            "  \"id\": \"mapbox://tileset-source/test-user/tileset-source-test-1\",\n" +
                            "  \"source_size\": 10592\n" +
                            "}"
                )
            )
        )

        // create tileset
        mockMapboxApi.stubFor(
            post("/mapbox/tilesets/v1/test-user.tileset-test-1?access_token=test-token")
                .withHeader("Content-Type", containing("application/json"))
                .withRequestBody(
                    equalToJson(
                        "{" +
                                "  \"recipe\": {" +
                                "    \"version\": 1," +
                                "    \"layers\": {" +
                                "      \"layer-1\": {" +
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
                    )
                )
        )

        // publish tileset
        mockMapboxApi.stubFor(
            post("/mapbox/tilesets/v1/test-user.tileset-test-1/publish?access_token=test-token").willReturn(
                okJson(
                    "{\n" +
                            "  \"message\": \"Processing tileset-test-1\",\n" +
                            "  \"jobId\": \"0123456789abcdefghijklmno\"\n" +
                            "}"
                )
            )
        )

        // get job
        mockMapboxApi.stubFor(
            get("/mapbox/tilesets/v1/test-user.tileset-test-1/jobs/0123456789abcdefghijklmno?access_token=test-token").willReturn(
                okJson(
                    "{\n" +
                            "\"id\":\"0123456789abcdefghijklmno\"," +
                            "\"stage\":\"processing\"," +
                            "\"created\":1666493358168,\"created_nice\":\"Sun Oct 23 2022 02:49:18 GMT+0000 (Coordinated Universal Time)\", \n" +
                            "\"published\":1666493358168," +
                            "\"tilesetId\":\"test-user.tileset-test-1\"," +
                            "\"errors\":[],\"warnings\":[],\"completed\":null," +
                            "\"recipe\":{\"version\":1," +
                            "  \"layers\":{\"layer-1\":" +
                            "    {\"source\":\"mapbox://tileset-source/test-user/tileset-source-test-1\"," +
                            "\"minzoom\":0,\"maxzoom\":5}" +
                            "    }" +
                            "  }" +
                            "}"
                )
            )
        )


        val jobExecution = jobLauncerTestUtils.launchJob()

        assertEquals(ExitStatus.COMPLETED, jobExecution.exitStatus)
    }

    @Test
    fun testJob_ExistingTilesetSource_SuccessToUpdate() {
        //mockMapboxApi.stubFor(WireMock.any(WireMock.anyUrl()).withHost(equalTo("localhost")).willReturn(WireMock.okJson("{}")))

        // list styles
        mockMapboxApi.stubFor(
            WireMock.get("/mapbox/styles/v1/test-user?access_token=test-token").willReturn(okJson("{}"))
        )

        // list tileset sources
        mockMapboxApi.stubFor(
            get("/mapbox/tilesets/v1/sources/test-user?access_token=test-token").willReturn(
                okJson(
                    "[\n" +
                            "  {\n" +
                            "    \"id\": \"mapbox://tileset-source/test-user/tileset-source-test-1\",\n" +
                            "    \"size\": 119,\n" +
                            "    \"files\": 1\n" +
                            "  }\n" +
                            "]"
                )
            )
        )

        // list tilesets
        mockMapboxApi.stubFor(
            get("/mapbox/tilesets/v1/test-user?access_token=test-token").willReturn(
                okJson(
                    "[\n" +
                            "  {\n" +
                            "    \"type\": \"vector\",\n" +
                            "    \"id\": \"test-user.tileset-test-1\",\n" +
                            "    \"name\": \"tileset-name-test-1\",\n" +
                            "    \"center\": [\n" +
                            "      139.7625732421875,\n" +
                            "      35.679609609368576,\n" +
                            "      5\n" +
                            "    ],\n" +
                            "    \"created\": \"2022-10-02T15:55:28.113Z\",\n" +
                            "    \"modified\": \"2022-10-22T07:24:33.210Z\",\n" +
                            "    \"visibility\": \"private\",\n" +
                            "    \"description\": \"\",\n" +
                            "    \"filesize\": 119,\n" +
                            "    \"status\": \"available\",\n" +
                            "    \"tileset_precisions\": {\n" +
                            "      \"free\": 0\n" +
                            "    },\n" +
                            "    \"created_by_client\": null\n" +
                            "  }\n" +
                            "]"
                )
            )
        )

        // get tileset source
        mockMapboxApi.stubFor(
            get("/mapbox/tilesets/v1/sources/test-user/tileset-source-test-1?access_token=test-token").willReturn(
                okJson(
                    "{\n" +
                            "  \"id\": \"mapbox://tileset-source/test-user/tileset-source-test-1\",\n" +
                            "  \"files\": 1,\n" +
                            "  \"size\": 119,\n" +
                            "  \"size_nice\": \"119B\"\n" +
                            "}"
                )
            )
        )

        // update tileset source
        mockMapboxApi.stubFor(
            put("/mapbox/tilesets/v1/sources/test-user/tileset-source-test-1?access_token=test-token").willReturn(
                okJson(
                    "{\n" +
                            "  \"file_size\": 10000,\n" +
                            "  \"files\": 1,\n" +
                            "  \"id\": \"mapbox://tileset-source/test-user/tileset-source-test-1\",\n" +
                            "  \"source_size\": 10592\n" +
                            "}"
                )
            )
        )

        // publish tileset
        mockMapboxApi.stubFor(
            post("/mapbox/tilesets/v1/test-user.tileset-test-1/publish?access_token=test-token").willReturn(
                okJson(
                    "{\n" +
                            "  \"message\": \"Processing tileset-test-1\",\n" +
                            "  \"jobId\": \"0123456789abcdefghijklmno\"\n" +
                            "}"
                )
            )
        )


        // get job
        mockMapboxApi.stubFor(
            get("/mapbox/tilesets/v1/test-user.tileset-test-1/jobs/0123456789abcdefghijklmno?access_token=test-token").willReturn(
                okJson(
                    "{\n" +
                            "\"id\":\"0123456789abcdefghijklmno\"," +
                            "\"stage\":\"processing\"," +
                            "\"created\":1666493358168,\"created_nice\":\"Sun Oct 23 2022 02:49:18 GMT+0000 (Coordinated Universal Time)\", \n" +
                            "\"published\":1666493358168," +
                            "\"tilesetId\":\"test-user.tileset-test-1\"," +
                            "\"errors\":[],\"warnings\":[],\"completed\":null," +
                            "\"recipe\":{\"version\":1," +
                            "  \"layers\":{\"layer-1\":" +
                            "    {\"source\":\"mapbox://tileset-source/test-user/tileset-source-test-1\"," +
                            "\"minzoom\":0,\"maxzoom\":5}" +
                            "    }" +
                            "  }" +
                            "}"
                )
            )
        )


        val jobExecution = jobLauncerTestUtils.launchJob()

        assertEquals(ExitStatus.COMPLETED, jobExecution.exitStatus)
    }

}

