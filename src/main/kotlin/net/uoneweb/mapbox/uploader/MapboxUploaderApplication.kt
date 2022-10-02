package net.uoneweb.mapbox.uploader

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class])
class MapboxUploaderApplication

fun main(args: Array<String>) {
	runApplication<MapboxUploaderApplication>(*args)
}
