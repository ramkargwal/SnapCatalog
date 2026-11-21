package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val name: String,
  val folderName: String,
  val colorHex: String = "#026A75",
  val iconName: String = "folder",
  val description: String = "",
  val createdAt: Long = System.currentTimeMillis(),
)

@Entity(
  tableName = "items",
  foreignKeys = [
    ForeignKey(
      entity = CategoryEntity::class,
      parentColumns = ["id"],
      childColumns = ["categoryId"],
      onDelete = ForeignKey.CASCADE,
    )
  ],
  indices = [Index(value = ["categoryId"])],
)
data class ItemEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val categoryId: Long,
  val name: String,
  val imageFileName: String,
  val relativeLocation: String,
  val notes: String = "",
  val quantity: Int = 1,
  val createdAt: Long = System.currentTimeMillis(),
)

data class CategoryWithItemCount(
  val id: Long,
  val name: String,
  val folderName: String,
  val colorHex: String,
  val iconName: String,
  val description: String,
  val createdAt: Long,
  val itemCount: Int,
  val latestImageFileName: String?,
)
