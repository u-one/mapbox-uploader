package net.uoneweb.mapbox.uploader.mapbox

import org.apache.commons.lang3.StringUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class TilesetSourceIdTest {

    @ParameterizedTest
    @MethodSource("simpleIdValidCases")
    fun testConstructorValidIdSuccess(input: String) {
        val obj = TilesetSourceId(input)
        assertThat(obj).isNotNull
    }

    @ParameterizedTest
    @MethodSource("simpleIdInvalidCases")
    fun testConstructorInvalidIdThrowsException(input: String) {
        assertThrows<IllegalArgumentException> { TilesetSourceId(input) }
    }

    companion object {
        @JvmStatic
        fun simpleIdValidCases() = listOf(
            "1",
            "a",
            "A",
            StringUtils.repeat('a', 32),
            "-",
            "_",
            "1bC-_"
        )

        @JvmStatic
        fun simpleIdInvalidCases() = listOf(
            "",
            StringUtils.repeat('a', 33),
            "@"
        )
    }

    @Test
    fun getSimpleIdFromSimpleIdReturnsSimpleId() {
        val id = TilesetSourceId("simple-id")
        assertThat(id.getSimpleId()).isEqualTo("simple-id")
    }

    @Test
    fun getSimpleIdFromUserIdAndSimpleIdReturnsSimpleId() {
        val id = TilesetSourceId("test-user.simple-id")
        assertThat(id.getSimpleId()).isEqualTo("simple-id")
    }

    @Test
    fun getSimpleIdFromMapboxSchemeReturnsSimpleId() {
        val id = TilesetSourceId("mapbox://tileset-source/test-user/simple-id")
        assertThat(id.getSimpleId()).isEqualTo("simple-id")
    }

    @ParameterizedTest
    @ArgumentsSource(GetCanonicalIdCases::class)
    fun testGetCanonicalId(input: String, expected: String) {
        val obj = TilesetSourceId(input)
        assertThat(obj.getCanonicalId()).isEqualTo(expected)
    }

    class GetCanonicalIdCases : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> =
            Stream.of(
                arguments(
                    "mapbox://tileset-source/test-user/tileset-source-id",
                    "mapbox://tileset-source/test-user/tileset-source-id"
                ),
                arguments(
                    "test-user.tileset-source-id",
                    "mapbox://tileset-source/test-user/tileset-source-id"
                ),
                arguments(
                    "tileset-source-id",
                    "mapbox://tileset-source//tileset-source-id"
                )
            )
    }
}