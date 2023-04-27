package com.example.notesapp

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.example.notesapp.database.NoteDatabase
import com.example.notesapp.databinding.ActivityMainBinding
import com.example.notesapp.repository.NoteRepository
import com.example.notesapp.viewModel.NoteViewModel
import com.example.notesapp.viewModel.NoteViewModelFactory


class MainActivity : AppCompatActivity() {

    lateinit var noteViewModel: NoteViewModel
    @SuppressLint("StaticFieldLeak")
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityMainBinding.inflate(layoutInflater)

        try{
            setContentView(binding.root)
            val noteRepository = NoteRepository(NoteDatabase(this))
            val noteViewModelFactory = NoteViewModelFactory(noteRepository)
            noteViewModel = ViewModelProvider(this,
                noteViewModelFactory)[NoteViewModel::class.java]
        }
        catch(e: Exception){
            Log.d("TAG", "Error")
        }
    }
}