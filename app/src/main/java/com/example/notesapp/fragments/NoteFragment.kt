package com.example.notesapp.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.notesapp.R
import com.example.notesapp.databinding.FragmentNoteBinding
import com.example.notesapp.viewModel.NoteViewModel
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.notesapp.MainActivity
import com.example.notesapp.adapters.NotesAdapter
import com.example.notesapp.utils.SwipeToDelete
import com.example.notesapp.utils.hideKeyboard
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialElevationScale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


class NoteFragment : Fragment(R.layout.fragment_note) {

    @SuppressLint("StaticFieldLeak")
    private lateinit var binding: FragmentNoteBinding
    private val noteViewModel: NoteViewModel by activityViewModels()
    private lateinit var notesAdapter: NotesAdapter

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        exitTransition = MaterialElevationScale(false).apply{
            duration=350
        }
        enterTransition = MaterialElevationScale(true).apply{
            duration=350
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentNoteBinding.bind(view)
        val activity = activity as MainActivity
        val navController = Navigation.findNavController(view)
        requireView().hideKeyboard()

        CoroutineScope(Dispatchers.Main).launch{
            delay(10)
            //activity.window.statusBarColor = Color.WHITE
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            activity.window.statusBarColor = Color.parseColor("#9e9d9d")
        }

        binding.addNote.setOnClickListener {
            binding.logo.visibility = View.INVISIBLE
            binding.settings.visibility = View.INVISIBLE
            navController.navigate(NoteFragmentDirections.actionNoteFragmentToUpdateFragment())
        }

        recyclerViewDisplay()
        swipeToDelete(binding.recyclerView)

        binding.searchView.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                binding.noData.isVisible=false
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(s.toString().isNotEmpty()){
                    val text=s.toString()
                    val query="%$text%"
                    if(query.isNotEmpty()){
                        noteViewModel.searchNote(query).observe(viewLifecycleOwner){
                            notesAdapter.submitList(it)
                        }
                    }
                    else{
                        observerDataChanges()
                    }
                }
                else{
                    observerDataChanges()
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })
        binding.searchView.setOnEditorActionListener { v, actionId, _ ->
            if(actionId==EditorInfo.IME_ACTION_SEARCH){
                v.clearFocus()
                requireView().hideKeyboard()
            }
            return@setOnEditorActionListener true
        }
    }

    private fun swipeToDelete(recyclerView: RecyclerView) {
        val swipeToDeleteCallback=object: SwipeToDelete(){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position=viewHolder.absoluteAdapterPosition
                val note=notesAdapter.currentList[position]
                var actionBtnTapped = false
                noteViewModel.deleteNote(note)
                binding.searchView.apply{
                    hideKeyboard()
                    clearFocus()
                }
                if(binding.searchView.text.toString().isEmpty()){
                    observerDataChanges()
                }
                val snackBar = Snackbar.make(requireView(), "Note Deleted", Snackbar.LENGTH_LONG)
                    .addCallback(object: BaseTransientBottomBar.BaseCallback<Snackbar>(){
                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            super.onDismissed(transientBottomBar, event)
                        }

                        override fun onShown(transientBottomBar: Snackbar?) {
                            transientBottomBar?.setAction("UNDO"){
                                noteViewModel.saveNote(note)
                                actionBtnTapped=true
                                binding.noData.isVisible=false
                            }
                            super.onShown(transientBottomBar)
                        }
                    }).apply{
                        animationMode=Snackbar.ANIMATION_MODE_FADE
                        setAnchorView(R.id.addNote)
                    }
                    snackBar.setActionTextColor(ContextCompat.getColor(
                        requireContext(), R.color.yellowOrange
                    ))
                snackBar.show()
            }

        }
        val itemTouchHelper= ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun observerDataChanges() {
        noteViewModel.getAllNotes().observe(viewLifecycleOwner){ list->
            binding.noData.isVisible=list.isEmpty()
            notesAdapter.submitList(list)
        }
    }

    private fun recyclerViewDisplay() {
        when(resources.configuration.orientation){
            Configuration.ORIENTATION_PORTRAIT -> setUpRecyclerView(2)
            Configuration.ORIENTATION_LANDSCAPE -> setUpRecyclerView(3)
        }
    }

    private fun setUpRecyclerView(spanCount: Int) {
        binding.recyclerView.apply{
            layoutManager=StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL)
            setHasFixedSize(true)
            notesAdapter = NotesAdapter()
            notesAdapter.stateRestorationPolicy=
                RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
            adapter = notesAdapter
            postponeEnterTransition(300L, TimeUnit.MILLISECONDS)
            viewTreeObserver.addOnPreDrawListener {
                startPostponedEnterTransition()
                true
            }
        }
        observerDataChanges()
    }

}