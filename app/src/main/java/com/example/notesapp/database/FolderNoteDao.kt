package com.example.notesapp.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.notesapp.model.FolderNote
import com.example.notesapp.model.Note

@Dao
interface FolderNoteDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFolderNote(folderNote: FolderNote)

    @Delete
    suspend fun deleteFolderNote(folderNote: FolderNote)

    @Query("SELECT folder_id FROM folder_note WHERE note_id = :query")
    fun getNoteFolders(query: Int): LiveData<List<Int>>

    @Query("SELECT id, title, content, date FROM " +
            "(SELECT note_id FROM folder_note WHERE folder_id = :query) a " +
            "INNER JOIN note b ON b.id = a.note_id ORDER BY id DESC")
    fun getFolderNotes(query: Int): LiveData<List<Note>>

    @Query("SELECT COUNT(folder_id) FROM folder_note WHERE folder_id = :query")
    fun folderCount(query: Int): LiveData<Int>
}