package com.example.notesapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.notesapp.model.Note
import com.example.notesapp.model.Folder

@Database(entities = [Note::class, Folder::class], version = 1, exportSchema = false)
abstract class NoteDatabase : RoomDatabase() {

    companion object {
        @Volatile
        private var noteDatabase: NoteDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = noteDatabase?: synchronized(LOCK){
            noteDatabase?: createDatabase(context).also{
                noteDatabase = it
            }
        }

        private fun createDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            NoteDatabase::class.java,
            "note_database"
        ).build()
    }

    abstract fun getNoteDao(): NoteDao

    abstract fun getFolderDao(): FolderDao

}