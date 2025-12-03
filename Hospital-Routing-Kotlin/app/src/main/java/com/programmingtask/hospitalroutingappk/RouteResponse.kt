package com.programmingtask.hospitalroutingappk

import com.google.gson.annotations.SerializedName

data class RouteResponse(
    @SerializedName("routes") val routes: List<Route>
)

data class Route(
    @SerializedName("overview_polyline") val overviewPolyline: OverviewPolyline,
    @SerializedName("legs") val legs: List<Leg>
)

data class OverviewPolyline(
    @SerializedName("points") val points: String
)

data class Leg(
    @SerializedName("distance") val distance: Distance,
    @SerializedName("duration") val duration: Duration,
    @SerializedName("end_address") val endAddress: String,
    @SerializedName("start_address") val startAddress: String,
    @SerializedName("end_location") val endLocation: Location,
    @SerializedName("start_location") val startLocation: Location,
    @SerializedName("steps") val steps: List<Step>
)

data class Distance(
    @SerializedName("text") val text: String,
    @SerializedName("value") val value: Int
)

data class Duration(
    @SerializedName("text") val text: String,
    @SerializedName("value") val value: Int
)

data class Location(
    @SerializedName("lat") val lat: Double,
    @SerializedName("lng") val lng: Double
)

data class Step(
    @SerializedName("distance") val distance: Distance,
    @SerializedName("duration") val duration: Duration,
    @SerializedName("end_location") val endLocation: Location,
    @SerializedName("html_instructions") val htmlInstructions: String,
    @SerializedName("polyline") val polyline: PolylineEncoded,  // ← aquí cambias
    @SerializedName("start_location") val startLocation: Location,
    @SerializedName("travel_mode") val travelMode: String
)

data class PolylineEncoded(
    @SerializedName("points") val points: String
)