package template.core.database.room.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "examples")
data class ExampleEntity(
    @PrimaryKey val id: String,
    val tag: String,
)
