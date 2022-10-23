package net.uoneweb.mapbox.uploader

import mu.KotlinLogging
import net.uoneweb.mapbox.uploader.mapbox.Layer
import net.uoneweb.mapbox.uploader.mapbox.Recipe
import net.uoneweb.mapbox.uploader.mapbox.TilesetSource
import net.uoneweb.mapbox.uploader.mapbox.TilesetSourceId
import net.uoneweb.mapbox.uploader.mapbox.repository.MapboxRepository
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.core.io.FileSystemResource
import org.springframework.stereotype.Component
import java.io.File

@Component
class UploadTasklet(private val mapboxRepository: MapboxRepository) : Tasklet {

    private val logger = KotlinLogging.logger { }
    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus? {
        logger.info { "start" }
        val tilesetSources = mapboxRepository.listTilesetSources()
        val tilesets = mapboxRepository.listTileset()


        val tilesetSourceId = TilesetSourceId("tileset-source-test-1")
        val tilesetId = "tileset-test-1"
        val tilesetName = "tileset-name-test-1"

        return update(tilesetSourceId, tilesetId, tilesetName)
    }

    private fun update(tilesetSourceId: TilesetSourceId, tilesetId: String, tilesetName: String): RepeatStatus {
        val tilesetSource = mapboxRepository.getTilesetSource(tilesetSourceId)

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
        val body =
            "{\"type\":\"Feature\",\"id\":1,\"geometry\":{\"type\":\"Point\",\"coordinates\":[139.76293,35.67871]},\"properties\":{\"name\":\"tokyo\"}}\n" // line-delimited GeoJson
        val file = File("test.geojson")
        file.writeBytes(body.toByteArray())
        val geoJson = FileSystemResource(file)

        return mapboxRepository.createTilesetSource(tilesetSourceId, geoJson)
    }

    private fun updateTilesetSource(tilesetSource: TilesetSource): TilesetSource? {
        val body =
            "{\"type\":\"Feature\",\"id\":1,\"geometry\":{\"type\":\"Point\",\"coordinates\":[139.76293,35.67871]},\"properties\":{\"name\":\"tokyo\"}}\n" // line-delimited GeoJson
        val file = File("test.geojson")
        file.writeBytes(body.toByteArray())
        val geoJson = FileSystemResource(file)

        return mapboxRepository.updateTilesetSource(tilesetSource.id, geoJson)
    }

    private fun createRecipe(
        version: Int,
        layerName: String,
        tilesetSource: TilesetSource,
        minZoom: Int,
        maxZoom: Int
    ): Recipe {
        val layers = mapOf(Pair(layerName, Layer(tilesetSource.id.getCanonicalId(), minZoom, maxZoom)))
        return Recipe(version, layers)
    }

    private fun createTileset(tilesetId: String, tilesetName: String, recipe: Recipe) {
        mapboxRepository.createTileset(tilesetId, tilesetName, recipe)
    }
}