package com.example.notesapp.fragments

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.ACTION_IMAGE_CAPTURE
import android.speech.RecognizerIntent
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
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
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Math.max
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
    private val REQUEST_CODE_SPEECH_INPUT = 1
    private val REQUEST_CODE_IMAGE_INPUT = 2
    private val REQUEST_CODE_CAMERA = 3
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

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

        binding.voiceNote.setOnClickListener {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )

            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault()
            )

            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text")

            try {
                startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT)
            } catch (e: Exception) {
                Toast
                    .makeText(
                        activity, " " + e.message,
                        Toast.LENGTH_SHORT
                    )
                    .show()
            }
        }

        binding.imgNote.setOnClickListener{
            openGalleryForImage()
        }

        binding.camNote.setOnClickListener{
            openCameraForImage()
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

    private fun openCameraForImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_IMAGE_INPUT)
    }

    private fun openGalleryForImage() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_CODE_CAMERA)
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
                            currentDate,
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
                    currentDate,
                )
            )
        }
    }

    private fun textRecognition(inpImage: InputImage){
        val result = recognizer.process(inpImage)
            .addOnSuccessListener { text ->
                // Task completed successfully
                val res = text.text
                binding.noteContent.renderMD(
                    binding.noteContent.getMD() + "\n" + res
                )
            }
            .addOnFailureListener { e ->
                // Task failed with an exception
                Toast.makeText(activity,"Failed to recognise text due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && data != null) {
                val res: ArrayList<String> =
                    data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) as ArrayList<String>

                binding.noteContent.renderMD(
                    binding.noteContent.getMD() + "\n" + Objects.requireNonNull(res)[0]
                )
            }
        }
        if (requestCode == REQUEST_CODE_IMAGE_INPUT) {
            if (resultCode == RESULT_OK && data != null) {
                val imageUri: Uri = data.data!!
                val inpImage = InputImage.fromFilePath(requireContext(), imageUri)

                textRecognition(inpImage)
            }
        }
        if (requestCode == REQUEST_CODE_CAMERA) {
            if (resultCode == RESULT_OK && data != null) {
                val photo = data.extras!!["data"] as Bitmap

                val inpImage = InputImage.fromBitmap(photo, 0)

                textRecognition(inpImage)
            }
        }
    }
}