package net.uoneweb.mapbox.uploader.mapbox

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TilesetSourceTest {
    @Test
    fun getSimpleIdSuccessReturnsSimpleId() {
        val tilesetSorce = TilesetSource(
            "mapbox://tileset-source/test-user/tileset-source-id",
            1,
            119,
            "119B"
        )
        assertThat(tilesetSorce.getSimpleId()).isEqualTo("tileset-source-id")
    }
}