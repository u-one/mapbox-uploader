package net.uoneweb.mapbox.uploader.mapbox.repository

import net.uoneweb.mapbox.uploader.mapbox.TilesetSource
import net.uoneweb.mapbox.uploader.mapbox.TilesetSourceId

interface MapboxTilesetSourceRepository {
    fun listTilesetSources(): List<TilesetSource>

    fun getTilesetSource(tilesetSourceId: TilesetSourceId): TilesetSource?

    fun createTilesetSource(tilesetSourceId: TilesetSourceId, body: String): TilesetSource?

    fun updateTilesetSource(tilesetSourceId: TilesetSourceId, body: String): TilesetSource?

    fun deleteTilesetSource(tilesetSourceId: TilesetSourceId)
}