package net.uoneweb.mapbox.uploader.mapbox

// Somehow jackson does not serialize Recipe when this is data class
class Recipe(val version: Int, val layers: Map<String, Layer>)

data class Layer(val source: String, val minzoom: Int, val maxzoom: Int)