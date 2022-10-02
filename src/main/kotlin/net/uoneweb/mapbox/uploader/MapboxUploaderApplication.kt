package net.uoneweb.mapbox.uploader

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MapboxUploaderApplication

fun main(args: Array<String>) {
	runApplication<MapboxUploaderApplication>(*args)
}
