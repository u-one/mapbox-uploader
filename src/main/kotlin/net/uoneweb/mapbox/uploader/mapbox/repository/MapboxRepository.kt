package net.uoneweb.mapbox.uploader.mapbox.repository

import net.uoneweb.mapbox.uploader.mapbox.Recipe
import net.uoneweb.mapbox.uploader.mapbox.Tileset

interface MapboxRepository {


    fun createTileset(tilesetId: String, tilesetName: String, recipe: Recipe)

    fun updateTilesetRecipe(tilesetId: String, recipe: Recipe)

    fun listTileset(): List<Tileset>

    fun deleteTileset(tilesetId: String)

    fun publishTileset(tilesetId: String): String

    fun listJobs(tilesetId: String)

    fun getJob(tilesetId: String, jobId: String)
}