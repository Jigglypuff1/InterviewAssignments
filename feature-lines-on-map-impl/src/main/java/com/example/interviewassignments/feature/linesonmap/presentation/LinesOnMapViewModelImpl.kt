package com.example.interviewassignments.feature.linesonmap.presentation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.interviewassignments.feature.linesonmap.domain.OffsetDirection
import com.example.interviewassignments.feature.linesonmap.domain.PointRatio
import com.example.interviewassignments.feature.linesonmap.presentation.LinesOnMapViewModel.Command
import com.yandex.mapkit.ScreenPoint
import com.yandex.mapkit.geometry.Point
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sqrt

const val POINT_LIMIT = 2
const val OFFSET_IN_METER = 200

class LinesOnMapViewModelImpl @Inject constructor(): ViewModel(), LinesOnMapViewModel {

    override val commands = MutableLiveData<Command>()
    override val pointsList = mutableListOf<Pair<Point, ScreenPoint?>>()

    private var screenMapHeight = 0F
    private var screenMapWidth = 0F
    private var screenZoom = 0F
    private var latitude = 0.0
    private var offsetPx = 0F

    override fun addPoint(point: Point, screenPoint: ScreenPoint?) {
        if (pointsList.size >= POINT_LIMIT) return

        pointsList.add(Pair(point, screenPoint))
        commands.value = Command.AddPointsOnMap(pointsList.map { it.first })

        latitude = point.latitude
        calculateOffset()
        if (pointsList.size >= POINT_LIMIT) {
            calculateCoordinatesLines()
        }
    }

    private fun calculateOffset() {
        val metersInPx = abs(10000 / (2F.pow(screenZoom + 8F) * cos(latitude))) * 1000
        offsetPx = (OFFSET_IN_METER / metersInPx).toFloat()
    }

    override fun clearMap() {
        pointsList.clear()
        commands.value = Command.ClearMap
    }

    override fun setScreenSize(height: Int, width: Int) {
        screenMapHeight = height.toFloat()
        screenMapWidth = width.toFloat()
    }

    override fun setMapZoom(zoom: Float) {
        this.screenZoom = zoom
    }

    private fun calculateCoordinatesLines(){
        val pointA = pointsList.first().second?: return
        val pointB = pointsList.last().second?: return

        val mainLineCoordinates = calculateCoordinates(pointA, pointB)

        val additionalLineCoordinatesLeft = calculateCoordinatesWithOffset(
            mainLineCoordinates.first(),
            mainLineCoordinates.last(),
            OffsetDirection.Left,
            mutableListOf(mainLineCoordinates),
        )

        val additionalLineCoordinatesRight = calculateCoordinatesWithOffset(
            mainLineCoordinates.first(),
            mainLineCoordinates.last(),
            OffsetDirection.Right,
        )

        commands.value =
            Command.AddPolylineOnMap(
                pointsList.map { it.first },
                additionalLineCoordinatesLeft + additionalLineCoordinatesRight
            )
    }

    private fun calculateCoordinatesWithOffset(
        pointA: ScreenPoint,
        pointB: ScreenPoint,
        offsetDirection: OffsetDirection,
        screenPointList: MutableList<List<ScreenPoint>> = mutableListOf(),
    ): MutableList<List<ScreenPoint>> {
        if (offScreen(pointA) && offScreen(pointB)) return screenPointList

        val newPoints = calculateCoordinates(
            getPointWithOffset(pointA, getPointRatio(pointA, pointB), offsetDirection),
            getPointWithOffset(pointB, getPointRatio(pointB, pointA), offsetDirection),
        )
        screenPointList.add(newPoints)

        return calculateCoordinatesWithOffset(
            newPoints.first(),
            newPoints.last(),
            offsetDirection,
            screenPointList,
        )
    }

    private fun getPointRatio(currentPoint: ScreenPoint, secondPoint: ScreenPoint): PointRatio {
        return if (currentPoint.x > secondPoint.x) {
            if (currentPoint.y > secondPoint.y) PointRatio.RightBottom else PointRatio.RightTop
        } else {
            if (currentPoint.y > secondPoint.y) PointRatio.LeftBottom else PointRatio.LeftTop
        }
    }

    private fun offScreen(point: ScreenPoint): Boolean {
        return (point.x <= 0F || point.x >= screenMapWidth)
                && (point.y <= 0F || point.y >= screenMapHeight)
                || point.x.isNaN()
                || point.y.isNaN()
    }

    private fun getPointWithOffset(
        point: ScreenPoint,
        ratio: PointRatio,
        offsetDirection: OffsetDirection,
    ): ScreenPoint {

        val leg = offsetPx * 2 / sqrt(2F)
        var x = 0F
        var y = 0F

        when (ratio) {
            PointRatio.LeftTop -> {
                x = point.x + leg * offsetDirection.multiplier
                y = point.y - leg * offsetDirection.multiplier
            }

            PointRatio.LeftBottom -> {
                x = point.x + leg * offsetDirection.multiplier
                y = point.y + leg * offsetDirection.multiplier
            }

            PointRatio.RightTop -> {
                x = point.x + leg * offsetDirection.multiplier
                y = point.y + leg * offsetDirection.multiplier
            }

            PointRatio.RightBottom -> {
                x = point.x + leg * offsetDirection.multiplier
                y = point.y - leg * offsetDirection.multiplier
            }
        }

        return ScreenPoint(x, y)
    }

    private fun calculateCoordinates(pointA: ScreenPoint, pointB: ScreenPoint): List<ScreenPoint> {

        var x1 = (0F - pointA.y) / (pointB.y - pointA.y) * (pointB.x - pointA.x) + pointA.x
        val y1 = if (x1 < 0F) {
            x1 = 0F
            (0F - pointA.x) / (pointB.x - pointA.x) * (pointB.y - pointA.y) + pointA.y
        } else if (x1 > screenMapWidth) {
            x1 = screenMapWidth
            (screenMapWidth - pointA.x) / (pointB.x - pointA.x) * (pointB.y - pointA.y) + pointA.y
        } else {
            0F
        }

        var x2 =
            (screenMapHeight - pointA.y) / (pointB.y - pointA.y) * (pointB.x - pointA.x) + pointA.x
        val y2 = if (x2 < 0F) {
            x2 = 0F
            (0F - pointA.x) / (pointB.x - pointA.x) * (pointB.y - pointA.y) + pointA.y
        } else if (x2 > screenMapWidth) {
            x2 = screenMapWidth
            (screenMapWidth - pointA.x) / (pointB.x - pointA.x) * (pointB.y - pointA.y) + pointA.y
        } else {
            screenMapHeight
        }

        return listOf(ScreenPoint(x1, y1), ScreenPoint(x2, y2))
    }

    override fun onChangingPosition(newPointsList: PointsList) {
        commands.value = Command.AddPointsOnMap(newPointsList.map { it.first })

        pointsList.clear()
        pointsList.addAll(newPointsList)

        calculateOffset()
        if (pointsList.size >= POINT_LIMIT) {
            calculateCoordinatesLines()
        }
    }
}