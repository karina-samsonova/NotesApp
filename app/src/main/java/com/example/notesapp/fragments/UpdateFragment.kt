package com.example.notesapp.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.example.notesapp.R
import com.example.notesapp.MainActivity
import com.example.notesapp.databinding.FragmentUpdateBinding
import com.example.notesapp.model.Note
import com.example.notesapp.utils.hideKeyboard
import com.example.notesapp.viewModel.NoteViewModel
import com.google.android.material.transition.MaterialContainerTransform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class UpdateFragment : Fragment(R.layout.fragment_update) {

    private lateinit var navController: NavController
    private lateinit var binding: FragmentUpdateBinding
    private var note: Note?=null
    private lateinit var result: String
    private val noteViewModel: NoteViewModel by activityViewModels()
    private val sdf = SimpleDateFormat("E, dd MMM yyyy HH:mm:ss", Locale("ru", "RU"))
    private val currentDate = sdf.format(Date())
    private val job = CoroutineScope(Dispatchers.Main)
    private val args: UpdateFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val animation = MaterialContainerTransform().apply{
            drawingViewId=R.id.fragment
            scrimColor= Color.TRANSPARENT
            duration=300L
        }
        sharedElementEnterTransition=animation
        sharedElementReturnTransition=animation

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding=FragmentUpdateBinding.bind(view)
        navController= Navigation.findNavController(view)
        val activity=activity as MainActivity

        binding.backBtn.setOnClickListener{
            requireView().hideKeyboard()
            navController.popBackStack()
        }

        binding.noteDate.text = currentDate.toString()

        binding.saveBtn.setOnClickListener{
            saveNote()
        }

        try{
            binding.noteContent.setOnFocusChangeListener{_,hasFocus->
                if(hasFocus){
                    binding.bottomBar.visibility=View.VISIBLE
                    binding.noteContent.setStylesBar(binding.styleBar)
                } else binding.bottomBar.visibility=View.GONE
            }
        } catch(e: Throwable){
            Log.d("TAG", e.stackTraceToString())
        }

        ViewCompat.setTransitionName(binding.fragmentUpdateLayout, "recyclerView_${args.note?.id}")

        setUpNote()
    }

    private fun setUpNote() {
        val note = args.note
        val title = binding.noteTitle
        val content = binding.noteContent
        val date = binding.noteDate

        if(note==null){
            binding.noteDate.text = currentDate.toString()
        }
        if(note!=null){
            title.setText(note.title)
            content.renderMD(note.content)
            date.text = note.date
            binding.apply{
                job.launch {
                    delay(10)
                }
            }
        }
    }

    private fun saveNote() {
        if(binding.noteContent.text.toString().isEmpty() ||
            binding.noteTitle.text.toString().isEmpty()){
            Toast.makeText(activity,"Something is empty", Toast.LENGTH_SHORT).show()
        } else{
            note=args.note
            when(note){
                null-> {
                    noteViewModel.saveNote(
                        Note(
                            0,
                            binding.noteTitle.text.toString(),
                            binding.noteContent.getMD(),
                            currentDate
                        )
                    )
                    result = "noteSaved"
                    setFragmentResult("key", bundleOf("bundleKey" to result))

                    navController.navigate(UpdateFragmentDirections.actionUpdateFragmentToNoteFragment())
                } else ->{
                    updateNote()
                    navController.popBackStack()
                }
            }
        }
    }

    private fun updateNote() {
        if(note!=null){
            noteViewModel.updateNote(
                Note(
                    note!!.id,
                    binding.noteTitle.text.toString(),
                    binding.noteContent.getMD(),
                    currentDate
                )
            )
        }
    }

}