package com.example.myapplication.data.map

/**
 * Static campus tour stops for UPM Campus Sur, Madrid.
 * Coordinates sourced from OpenStreetMap.
 */
object CampusTourData {
    val markers = listOf(
        CampusMarker(
            id = 1,
            title = "ETSISI",
            description = "Escuela Técnica Superior de Ingeniería de Sistemas Informáticos.",
            latitude = 40.38967,
            longitude = -3.62872
        ),
        CampusMarker(
            id = 2,
            title = "Futsal Outdoor Courts",
            description = "Outdoor futsal and sports courts on Campus Sur.",
            latitude = 40.38870,
            longitude = -3.62835
        ),
        CampusMarker(
            id = 3,
            title = "ETSIST",
            description = "Escuela Técnica Superior de Ingeniería y Sistemas de Telecomunicación.",
            latitude = 40.38950,
            longitude = -3.62680
        )
    )
}
