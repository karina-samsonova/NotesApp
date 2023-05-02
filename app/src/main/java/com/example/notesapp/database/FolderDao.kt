package com.example.notesapp.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.notesapp.model.Folder

@Dao
interface FolderDao {
    @Query("SELECT * FROM folder ORDER BY id DESC")
    fun getAllFolders() : LiveData<List<Folder>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFolder(folder:Folder)

    @Delete
    suspend fun deleteFolder(folder:Folder)

    @Update
    suspend fun updateFolder(folder:Folder)
}