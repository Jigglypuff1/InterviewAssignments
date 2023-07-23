package com.example.interviewassignments.feature.linesonmap.domain


enum class OffsetDirection(val multiplier: Int) {
    Left(1),
    Right(-1),
}

enum class PointRatio {
    LeftTop,
    LeftBottom,
    RightTop,
    RightBottom,
}