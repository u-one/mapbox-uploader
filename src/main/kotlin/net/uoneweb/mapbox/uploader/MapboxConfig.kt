package net.uoneweb.mapbox.uploader

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "mapbox")
class MapboxConfig() {
    lateinit var user: String

    lateinit var token: String

    lateinit var host: String
}