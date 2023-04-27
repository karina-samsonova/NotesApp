package com.example.notesapp.repository

import com.example.notesapp.database.NoteDatabase
import com.example.notesapp.model.Note

class NoteRepository(private val noteDatabase: NoteDatabase) {

    fun getNote() = noteDatabase.getNoteDao().getAllNotes()

    fun searchNote(query: String) = noteDatabase.getNoteDao().searchNote(query)

    suspend fun insertNote(note: Note) = noteDatabase.getNoteDao().insertNote(note)

    suspend fun deleteNote(note: Note) = noteDatabase.getNoteDao().deleteNote(note)

    suspend fun updateNote(note: Note) = noteDatabase.getNoteDao().updateNote(note)
}