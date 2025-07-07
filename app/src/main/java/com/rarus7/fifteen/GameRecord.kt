package com.rarus7.fifteen

import kotlinx.serialization.Serializable

@Serializable
data class GameRecord(
    val dateTime: String,
    val moves: Int
)
