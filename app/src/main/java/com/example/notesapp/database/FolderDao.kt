package com.example.notesapp.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.notesapp.model.Folder

@Dao
interface FolderDao {
    @Query("SELECT * FROM folder ORDER BY id DESC")
    fun getAllNotes() : LiveData<List<Folder>>

    @Query("SELECT * FROM folder WHERE title LIKE :query")
    fun searchNote(query:String) : LiveData<List<Folder>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNote(note: Folder)

    @Delete
    suspend fun deleteNote(note: Folder)

    @Update
    suspend fun updateNote(note: Folder)
}