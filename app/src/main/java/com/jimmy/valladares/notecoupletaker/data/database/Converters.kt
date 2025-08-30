package com.jimmy.valladares.notecoupletaker.data.database

import androidx.room.TypeConverter
import com.jimmy.valladares.notecoupletaker.domain.model.CommitmentCategory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Convertidores de tipo para Room que permiten almacenar tipos complejos en la base de datos
 */
class Converters {

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): String? {
        return dateTime?.format(formatter)
    }

    @TypeConverter
    fun toLocalDateTime(dateTimeString: String?): LocalDateTime? {
        return dateTimeString?.let {
            LocalDateTime.parse(it, formatter)
        }
    }

    @TypeConverter
    fun fromCommitmentCategory(category: CommitmentCategory): String {
        return category.name
    }

    @TypeConverter
    fun toCommitmentCategory(categoryName: String): CommitmentCategory {
        return CommitmentCategory.valueOf(categoryName)
    }
}
