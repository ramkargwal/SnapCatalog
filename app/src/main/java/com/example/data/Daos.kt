package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
  @Query("""
    SELECT c.id, c.name, c.folderName, c.colorHex, c.iconName, c.description, c.createdAt,
           COUNT(i.id) as itemCount,
           (SELECT i2.imageFileName FROM items i2 WHERE i2.categoryId = c.id ORDER BY i2.createdAt DESC LIMIT 1) as latestImageFileName
    FROM categories c
    LEFT JOIN items i ON c.id = i.categoryId
    GROUP BY c.id
    ORDER BY c.createdAt DESC
  """)
  fun getAllCategoriesWithCount(): Flow<List<CategoryWithItemCount>>

  @Query("SELECT * FROM categories WHERE id = :categoryId LIMIT 1")
  suspend fun getCategoryById(categoryId: Long): CategoryEntity?

  @Query("SELECT * FROM categories WHERE id = :categoryId LIMIT 1")
  fun observeCategoryById(categoryId: Long): Flow<CategoryEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCategory(category: CategoryEntity): Long

  @Update
  suspend fun updateCategory(category: CategoryEntity)

  @Delete
  suspend fun deleteCategory(category: CategoryEntity)

  @Query("SELECT COUNT(*) FROM categories")
  suspend fun getCategoryCount(): Int
}

@Dao
interface ItemDao {
  @Query("SELECT * FROM items WHERE categoryId = :categoryId ORDER BY createdAt DESC")
  fun getItemsForCategory(categoryId: Long): Flow<List<ItemEntity>>

  @Query("SELECT * FROM items WHERE categoryId = :categoryId ORDER BY createdAt DESC")
  suspend fun getItemsForCategorySync(categoryId: Long): List<ItemEntity>

  @Query("SELECT * FROM items WHERE categoryId = :categoryId AND name LIKE '%' || :query || '%' ORDER BY createdAt DESC")
  fun searchItemsInCategory(categoryId: Long, query: String): Flow<List<ItemEntity>>

  @Query("SELECT * FROM items WHERE id = :itemId LIMIT 1")
  suspend fun getItemById(itemId: Long): ItemEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertItem(item: ItemEntity): Long

  @Update
  suspend fun updateItem(item: ItemEntity)

  @Delete
  suspend fun deleteItem(item: ItemEntity)

  @Query("DELETE FROM items WHERE categoryId = :categoryId")
  suspend fun deleteItemsForCategory(categoryId: Long)

  @Query("SELECT COUNT(*) FROM items")
  fun getTotalItemCount(): Flow<Int>
}
