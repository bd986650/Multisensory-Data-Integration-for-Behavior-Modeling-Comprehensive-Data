package com.example.eventcycles

import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType

object GoogleFitHelper {
    fun getFitnessOptions(): FitnessOptions {
        return FitnessOptions.builder()
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.AGGREGATE_HEART_RATE_SUMMARY, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_MOVE_MINUTES, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.AGGREGATE_MOVE_MINUTES, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_SLEEP_SEGMENT, FitnessOptions.ACCESS_READ)
            .build()
    }
}
