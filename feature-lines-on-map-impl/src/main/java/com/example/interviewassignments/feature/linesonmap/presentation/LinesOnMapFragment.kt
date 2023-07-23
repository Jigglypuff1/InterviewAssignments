package com.example.interviewassignments.feature.linesonmap.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.core_utils.context.getColorFromAttr
import com.example.core_utils.livedata.observeNonNullOnView
import com.example.core_utils.ui.isNightModeEnabled
import com.example.interviewassignments.core.resources.R as R
import com.example.interviewassignments.feature.lines.on.map.databinding.LinesOnMapFragmentBinding
import com.example.interviewassignments.feature.linesonmap.presentation.LinesOnMapViewModel.Command
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.MapWindow
import com.yandex.mapkit.map.SizeChangedListener
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import javax.inject.Inject

class LinesOnMapFragment : Fragment() {

    private val targetLocation = Point(59.945933, 30.320045)

    private lateinit var mapObjects: MapObjectCollection
    private lateinit var mapView: MapView
    private lateinit var mapWindow: MapWindow

    @Inject
    lateinit var viewModel: LinesOnMapViewModel

    private val inputListener = object : InputListener {
        override fun onMapTap(map: Map, point: Point) =
            viewModel.addPoint(point, mapWindow.worldToScreen(point))

        override fun onMapLongTap(map: Map, point: Point) =
            viewModel.addPoint(point, mapWindow.worldToScreen(point))
    }

    private val cameraListener =
        CameraListener { _, cameraPosition, _, finished ->
            if (finished) {
                viewModel.setMapZoom(cameraPosition.zoom)

                val pointsList = viewModel.pointsList.map {
                    Pair(
                        it.first,
                        mapWindow.worldToScreen(it.first)
                    )
                }
                viewModel.onChangingPosition(pointsList)
            }
        }

    private val sizeChangedListener =
        SizeChangedListener { _, newWidth, newHeight ->
            viewModel.setScreenSize(newHeight, newWidth)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createLinesOnMapComponent(this).inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = LinesOnMapFragmentBinding.inflate(inflater, container, false).also { binding ->

        binding.lifecycleOwner = viewLifecycleOwner
        binding.model = BindingModel()

        mapView = binding.mapview
        mapObjects = mapView.map.mapObjects
        mapWindow = mapView.mapWindow

        mapView.map.isNightModeEnabled = isNightModeEnabled()
        mapView.map.move(
            CameraPosition(targetLocation, 14.0f, 0.0f, 0.0f),
            Animation(Animation.Type.SMOOTH, 2f),
            null,
        )

        viewModel.setScreenSize(
            mapWindow.height(),
            mapWindow.width(),
        )
        viewModel.setMapZoom(mapView.map.cameraPosition.zoom)
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val colorLines = requireContext().getColorFromAttr(R.attr.colorLines)
        observeNonNullOnView(viewModel.commands) { cmd ->
            when (cmd) {
                is Command.AddPointsOnMap -> {
                    mapObjects.addPlacemarks(
                        cmd.points,
                        ImageProvider.fromResource(requireContext(), R.drawable.ic_mark),
                        IconStyle().setScale(1f)
                    )
                }
                is Command.AddPolylineOnMap -> {
                    mapView.map.mapObjects.clear()

                    mapObjects.addPlacemarks(
                        cmd.points,
                        ImageProvider.fromResource(requireContext(), R.drawable.ic_mark),
                        IconStyle().setScale(1f)
                    )

                    cmd.screenPointsList.forEach { screenPoints ->
                        val points = screenPoints.map {
                            mapWindow.screenToWorld(it)
                        }

                        mapObjects.addPolyline(Polyline(points)).apply {
                            strokeWidth = 1f
                            setStrokeColor(colorLines)
                        }
                    }
                }
                is Command.ClearMap -> mapView.map.mapObjects.clear()
            }
        }

        mapView.map.addInputListener(inputListener)
        mapView.map.addCameraListener(cameraListener)
        mapWindow.addSizeChangedListener(sizeChangedListener)
    }

    override fun onStart() {
        super.onStart()

        mapView.onStart()
        MapKitFactory.getInstance().onStart()
    }

    override fun onStop() {
        super.onStop()

        mapView.onStop()
        MapKitFactory.getInstance().onStop()
    }

    inner class BindingModel : LinesOnMapBindingModel {
        override fun clearMap() = viewModel.clearMap()
    }
}