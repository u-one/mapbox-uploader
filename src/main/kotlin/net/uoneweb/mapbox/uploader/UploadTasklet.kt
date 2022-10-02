package net.uoneweb.mapbox.uploader

import mu.KotlinLogging
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus

class UploadTasklet : Tasklet {
    private val logger = KotlinLogging.logger { }
    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus? {
        logger.info { "hello" }
        return RepeatStatus.FINISHED
    }
}