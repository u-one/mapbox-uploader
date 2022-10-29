package net.uoneweb.mapbox.uploader.mapbox

interface MapboxJobRepository {
    fun listJobs(tilesetId: String)

    fun getJob(tilesetId: String, jobId: String)
}