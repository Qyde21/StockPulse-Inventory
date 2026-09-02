package com.example.data.db

import androidx.room.TypeConverter
import com.example.data.model.MovementType

class Converters {
    @TypeConverter
    fun fromMovementType(type: MovementType): String = type.name

    @TypeConverter
    fun toMovementType(value: String): MovementType = MovementType.fromString(value)
}
