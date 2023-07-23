package com.example.interviewassignments.feature.linesonmap.presentation

import androidx.lifecycle.LiveData
import com.yandex.mapkit.ScreenPoint
import com.yandex.mapkit.geometry.Point

typealias PointsList = List<Pair<Point, ScreenPoint?>>


interface LinesOnMapViewModel {

    sealed class Command {
        class AddPointsOnMap(val points: List<Point>) : Command()
        class AddPolylineOnMap(
            val points: List<Point>,
            val screenPointsList: List<List<ScreenPoint>>
        ) : Command()
        object ClearMap : Command()
    }

    val commands: LiveData<Command>
    val pointsList: PointsList

    fun addPoint(point: Point, screenPoint: ScreenPoint?)
    fun clearMap()
    fun setScreenSize(height: Int, width: Int)
    fun setMapZoom(zoom: Float)
    fun onChangingPosition(newPointsList: PointsList)
}