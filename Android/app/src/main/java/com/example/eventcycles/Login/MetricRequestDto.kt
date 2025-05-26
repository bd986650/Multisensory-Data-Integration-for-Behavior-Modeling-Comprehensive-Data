package com.example.eventcycles

data class MetricRequestDto(
    val type: String,
    val value: Int,
    val timestamp: Long
)
