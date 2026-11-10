package com.sudo.finanapp.data


import android.content.Context
import androidx.room.*
import com.cuadre.app.data.Category
import com.cuadre.app.data.Movement
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Entity(tableName = "movements")
data class MovementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String,   // nombre del enum
    val amount: Long,
    val epochDay: Long,     // fecha como número de días
)

fun MovementEntity.toModel() =
    Movement(id, title, Category.valueOf(category), amount, LocalDate.ofEpochDay(epochDay))

fun Movement.toEntity() =
    MovementEntity(id, title, category.name, amount, date.toEpochDay())

@Dao
interface MovementDao {
    @Query("SELECT * FROM movements ORDER BY epochDay DESC, id DESC")
    fun observeAll(): Flow<List<MovementEntity>>

    @Upsert                      // inserta si id = 0, actualiza si ya existe
    suspend fun upsert(e: MovementEntity)

    @Query("DELETE FROM movements WHERE id = :id")
    suspend fun delete(id: Long)
}

@Database(entities = [MovementEntity::class], version = 1)
abstract class AppDb : RoomDatabase() {
    abstract fun dao(): MovementDao

    companion object {
        @Volatile private var instance: AppDb? = null
        fun get(ctx: Context): AppDb = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(ctx.applicationContext, AppDb::class.java, "cuadre.db")
                .build().also { instance = it }
        }
    }
}