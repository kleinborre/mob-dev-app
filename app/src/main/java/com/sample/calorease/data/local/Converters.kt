package com.sample.calorease.data.local

import androidx.room.TypeConverter
import com.sample.calorease.domain.model.ActivityLevel
import com.sample.calorease.domain.model.Gender
import com.sample.calorease.domain.model.WeightGoal

class Converters {
    
    @TypeConverter
    fun fromGender(value: Gender): String {
        return value.name
    }
    
    @TypeConverter
    fun toGender(value: String): Gender {
        return Gender.valueOf(value)
    }
    
    @TypeConverter
    fun fromActivityLevel(value: ActivityLevel): String {
        return value.name
    }
    
    @TypeConverter
    fun toActivityLevel(value: String): ActivityLevel {
        return ActivityLevel.valueOf(value)
    }
    
    @TypeConverter
    fun fromWeightGoal(value: WeightGoal): String {
        return value.name
    }
    
    @TypeConverter
    fun toWeightGoal(value: String): WeightGoal {
        return WeightGoal.valueOf(value)
    }
}
