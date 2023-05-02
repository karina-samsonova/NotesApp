package com.example.notesapp.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesapp.model.Folder
import com.example.notesapp.model.Note
import com.example.notesapp.repository.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoteViewModel(private val repository: NoteRepository): ViewModel() {

    fun saveNote(note: Note) = viewModelScope.launch(Dispatchers.IO){
        repository.insertNote(note)
    }

    fun updateNote(note: Note) = viewModelScope.launch(Dispatchers.IO){
        repository.updateNote(note)
    }

    fun deleteNote(note: Note) = viewModelScope.launch(Dispatchers.IO){
        repository.deleteNote(note)
    }

    fun searchNote(query: String): LiveData<List<Note>> {
        return repository.searchNote(query)
    }

    fun getAllNotes(): LiveData<List<Note>> = repository.getNote()

    fun saveFolder(folder: Folder) = viewModelScope.launch(Dispatchers.IO){
        repository.insertFolder(folder)
    }

    fun updateFolder(folder: Folder) = viewModelScope.launch(Dispatchers.IO){
        repository.updateFolder(folder)
    }

    fun deleteFolder(folder: Folder) = viewModelScope.launch(Dispatchers.IO){
        repository.deleteFolder(folder)
    }

    fun getAllFolders(): LiveData<List<Folder>> = repository.getFolder()
}