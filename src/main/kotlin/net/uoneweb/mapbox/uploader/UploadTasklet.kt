package net.uoneweb.mapbox.uploader

import com.google.gson.JsonObject
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import mu.KotlinLogging
import net.uoneweb.mapbox.uploader.mapbox.Layer
import net.uoneweb.mapbox.uploader.mapbox.Recipe
import net.uoneweb.mapbox.uploader.mapbox.TilesetSource
import net.uoneweb.mapbox.uploader.mapbox.TilesetSourceId
import net.uoneweb.mapbox.uploader.mapbox.repository.MapboxRepository
import net.uoneweb.mapbox.uploader.mapbox.repository.MapboxTilesetSourceRepository
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.stereotype.Component

@Component
class UploadTasklet(
    private val mapboxRepository: MapboxRepository,
    private val mapboxTilesetSourceRepository: MapboxTilesetSourceRepository
) : Tasklet {

    private val logger = KotlinLogging.logger { }
    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus? {
        logger.info { "start" }
        val tilesetSources = mapboxTilesetSourceRepository.listTilesetSources()
        val tilesets = mapboxRepository.listTileset()


        val tilesetSourceId = TilesetSourceId("tileset-source-test-1")
        val tilesetId = "tileset-test-1"
        val tilesetName = "tileset-name-test-1"

        return update(tilesetSourceId, tilesetId, tilesetName)
    }

    private fun update(tilesetSourceId: TilesetSourceId, tilesetId: String, tilesetName: String): RepeatStatus {
        val tilesetSource = mapboxTilesetSourceRepository.getTilesetSource(tilesetSourceId)

        if (tilesetSource != null) {
            updateTilesetSource(tilesetSource) ?: return RepeatStatus.FINISHED
        } else {
            val newTilesetSource = createTilesetSource(tilesetSourceId) ?: return RepeatStatus.FINISHED
            // composed of your username followed by a period and the tileset's unique name
            // (username.tileset_id).
            val recipe = createRecipe(1, "layer-1", newTilesetSource, 0, 5)
            createTileset(tilesetId, tilesetName, recipe)
        }

        val jobId = mapboxRepository.publishTileset(tilesetId)
        mapboxRepository.getJob(tilesetId, jobId)
        //mapboxRepository.listJobs(tilesetId)
        return RepeatStatus.FINISHED
    }

    private fun createTilesetSource(tilesetSourceId: TilesetSourceId): TilesetSource? {
        val properties = JsonObject()
        properties.addProperty("name", "tokyo")
        val feature = Feature.fromGeometry(Point.fromLngLat(139.76293, 35.67871), properties, "1")

        return mapboxTilesetSourceRepository.createTilesetSource(tilesetSourceId, feature.toJson())
    }

    private fun updateTilesetSource(tilesetSource: TilesetSource): TilesetSource? {
        val properties = JsonObject()
        properties.addProperty("name", "tokyo")
        val feature = Feature.fromGeometry(Point.fromLngLat(139.76293, 35.67871), properties, "1")

        return mapboxTilesetSourceRepository.updateTilesetSource(tilesetSource.id, feature.toJson())
    }

    private fun createRecipe(
        version: Int,
        layerName: String,
        tilesetSource: TilesetSource,
        minZoom: Int,
        maxZoom: Int
    ): Recipe {
        val layers = mapOf(
            Pair(layerName, Layer(tilesetSource.id.getCanonicalId(), minZoom, maxZoom)),
        )
        return Recipe(version, layers)
    }

    private fun createTileset(tilesetId: String, tilesetName: String, recipe: Recipe) {
        mapboxRepository.createTileset(tilesetId, tilesetName, recipe)
    }
}