package net.uoneweb.mapbox.uploader

import net.uoneweb.mapbox.uploader.mapbox.repository.MapboxRepository
import net.uoneweb.mapbox.uploader.mapbox.repository.MapboxTilesetSourceRepository
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableBatchProcessing
class BatchConfig(
    @Autowired val jobBuilderFactory: JobBuilderFactory,
    @Autowired val stepBuilderFactory: StepBuilderFactory,

    ) {

    @Autowired
    private lateinit var mapboxRepository: MapboxRepository

    @Autowired
    private lateinit var mapboxTilesetSourceRepository: MapboxTilesetSourceRepository

    @Bean
    fun job(): Job {
        return jobBuilderFactory
            .get("defaultJob")
            .start(step())
            .build()
    }

    @Bean
    fun step(): Step {
        return stepBuilderFactory
            .get("step1")
            .tasklet(UploadTasklet(mapboxRepository, mapboxTilesetSourceRepository))
            .build()
    }
}