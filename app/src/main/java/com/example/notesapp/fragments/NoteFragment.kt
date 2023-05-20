package com.example.notesapp.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.notesapp.MainActivity
import com.example.notesapp.R
import com.example.notesapp.adapters.FoldersAdapter
import com.example.notesapp.adapters.NotesAdapter
import com.example.notesapp.databinding.FragmentNoteBinding
import com.example.notesapp.model.Folder
import com.example.notesapp.utils.SwipeToDelete
import com.example.notesapp.utils.hideKeyboard
import com.example.notesapp.viewModel.NoteViewModel
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
    private lateinit var foldersAdapter: FoldersAdapter

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

        binding.mainPart.addNote.setOnClickListener {
            binding.mainPart.logo.visibility = View.INVISIBLE
            binding.mainPart.menu.visibility = View.INVISIBLE
            navController.navigate(NoteFragmentDirections.actionNoteFragmentToUpdateFragment())
        }

        binding.mainPart.menu.setOnClickListener{
            binding.drawerLayout.openDrawer(binding.navView)
            setUpFolderView()
        }

        binding.addFolder.setOnClickListener {
            addFolder()
        }
        binding.syncBtn.setOnClickListener {
            binding.drawerLayout.closeDrawer(binding.navView)
            recyclerViewDisplay()
        }

        recyclerViewDisplay()
        swipeToDelete(binding.mainPart.recyclerView)

        binding.mainPart.searchView.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                binding.mainPart.noData.isVisible=false
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
        binding.mainPart.searchView.setOnEditorActionListener { v, actionId, _ ->
            if(actionId==EditorInfo.IME_ACTION_SEARCH){
                v.clearFocus()
                requireView().hideKeyboard()
            }
            return@setOnEditorActionListener true
        }
    }

    private fun addFolder() {
        val bindingDialog = layoutInflater.inflate(R.layout.dialog_layout, null)
        val dialog = Dialog(requireContext())
        dialog.setContentView(bindingDialog)
        dialog.setCancelable(true)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
        val colorPicker = bindingDialog.findViewById<RadioGroup>(R.id.colorPicker)
        colorPicker.check(colorPicker.getChildAt(0).getId())

        val itemName = bindingDialog.findViewById<EditText>(R.id.itemName)
        val cancel_btn = bindingDialog.findViewById<TextView>(R.id.cancel)
        cancel_btn.setOnClickListener{
            dialog.dismiss()
        }
        val save_btn = bindingDialog.findViewById<TextView>(R.id.save)
        save_btn.setOnClickListener{
            if(itemName.text.isEmpty()){
                Toast.makeText(activity,"Name field is empty", Toast.LENGTH_SHORT).show()
            }
            else{
                noteViewModel.saveFolder(
                    Folder(
                        0,
                        itemName.text.toString(),
                        colorPicker.indexOfChild(bindingDialog.findViewById(colorPicker.getCheckedRadioButtonId()))
                    )
                )
                dialog.dismiss()
            }
        }
    }

    fun updateFolder(folder: Folder) {
        val bindingDialog = layoutInflater.inflate(R.layout.dialog_layout, null)
        val dialog = Dialog(requireContext())
        dialog.setContentView(bindingDialog)
        dialog.setCancelable(true)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()

        val title = bindingDialog.findViewById<TextView>(R.id.textView)
        title.setText("Редактирование категории")
        val colorPicker = bindingDialog.findViewById<RadioGroup>(R.id.colorPicker)
        colorPicker.check(colorPicker.getChildAt(folder.color).getId())

        val itemName = bindingDialog.findViewById<EditText>(R.id.itemName)
        itemName.setText(folder.name)
        val cancel_btn = bindingDialog.findViewById<TextView>(R.id.cancel)
        cancel_btn.setOnClickListener{
            dialog.dismiss()
        }
        val save_btn = bindingDialog.findViewById<TextView>(R.id.save)
        save_btn.setOnClickListener{
            if(itemName.text.isEmpty()){
                Toast.makeText(activity,"Name field is empty", Toast.LENGTH_SHORT).show()
            }
            else{
                noteViewModel.updateFolder(
                    Folder(
                        folder.id,
                        itemName.text.toString(),
                        colorPicker.indexOfChild(bindingDialog.findViewById(colorPicker.getCheckedRadioButtonId()))
                    )
                )
                dialog.dismiss()
                noteViewModel.getAllFolders().observe(viewLifecycleOwner){ list->
                    foldersAdapter.submitList(null)
                    foldersAdapter.submitList(list)
                }
            }
        }
    }

    private fun swipeToDelete(recyclerView: RecyclerView) {
        val swipeToDeleteCallback=object: SwipeToDelete(){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position=viewHolder.absoluteAdapterPosition
                val note=notesAdapter.currentList[position]
                var actionBtnTapped = false
                noteViewModel.deleteNote(note)
                binding.mainPart.searchView.apply{
                    hideKeyboard()
                    clearFocus()
                }
                if(binding.mainPart.searchView.text.toString().isEmpty()){
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
                                binding.mainPart.noData.isVisible=false
                            }
                            super.onShown(transientBottomBar)
                        }
                    }).apply{
                        animationMode=Snackbar.ANIMATION_MODE_FADE
                        setAnchorView(R.id.addNote)
                    }
                    snackBar.setActionTextColor(ContextCompat.getColor(
                        requireContext(), R.color.yellow
                    ))
                snackBar.show()
            }

        }
        val itemTouchHelper= ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun swipeToDeleteFolder(recyclerView: RecyclerView) {
        val swipeToDeleteCallback=object: SwipeToDelete(){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position=viewHolder.absoluteAdapterPosition
                val folder=foldersAdapter.currentList[position]
                var actionBtnTapped = false
                noteViewModel.deleteFolder(folder)
                noteViewModel.getAllFolders().observe(viewLifecycleOwner){ list->
                    foldersAdapter.submitList(list)
                    Handler(Looper.getMainLooper()).postDelayed(Runnable {
                        binding.rvNavDrawer.invalidate()
                        binding.rvNavDrawer.requestLayout()
                    }, 1500)
                }
                val snackBar = Snackbar.make(requireView(), "Folder Deleted", Snackbar.LENGTH_LONG)
                    .addCallback(object: BaseTransientBottomBar.BaseCallback<Snackbar>(){
                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            super.onDismissed(transientBottomBar, event)
                        }

                        override fun onShown(transientBottomBar: Snackbar?) {
                            transientBottomBar?.setAction("UNDO"){
                                noteViewModel.saveFolder(folder)
                                actionBtnTapped=true
                            }
                            super.onShown(transientBottomBar)
                        }
                    }).apply{
                        animationMode=Snackbar.ANIMATION_MODE_FADE
                        setAnchorView(R.id.addNote)
                    }
                snackBar.setActionTextColor(ContextCompat.getColor(
                    requireContext(), R.color.yellow
                ))
                snackBar.show()
            }
        }
        val itemTouchHelper= ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun observerDataChanges() {
        noteViewModel.getAllNotes().observe(viewLifecycleOwner){ list->
            binding.mainPart.noData.isVisible=list.isEmpty()
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
        binding.mainPart.recyclerView.apply{
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
        noteViewModel.getAllNotes().observe(viewLifecycleOwner){ list->
            binding.mainPart.noData.isVisible=list.isEmpty()
            notesAdapter.submitList(list)
        }
    }

    private fun recyclerViewDisplayFolder(folder_id: Int) {
        when(resources.configuration.orientation){
            Configuration.ORIENTATION_PORTRAIT -> setUpRecyclerViewFolder(2, folder_id)
            Configuration.ORIENTATION_LANDSCAPE -> setUpRecyclerViewFolder(3, folder_id)
        }
    }

    private fun setUpRecyclerViewFolder(spanCount: Int, folder_id: Int) {
        binding.drawerLayout.closeDrawer(binding.navView)
        binding.mainPart.recyclerView.apply{
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
        noteViewModel.getFolderNotes(folder_id).observe(viewLifecycleOwner){ list->
            binding.mainPart.noData.isVisible=list.isEmpty()
            notesAdapter.submitList(list)
        }
    }

    private fun setUpFolderView() {
        binding.rvNavDrawer.apply{
            layoutManager= LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            setHasFixedSize(true)
            foldersAdapter = FoldersAdapter(::updateFolder, ::recyclerViewDisplayFolder, noteViewModel)
            foldersAdapter.stateRestorationPolicy=
                RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
            adapter = foldersAdapter
        }
        noteViewModel.getAllFolders().observe(viewLifecycleOwner){ list->
            foldersAdapter.submitList(list)
            Handler(Looper.getMainLooper()).postDelayed(Runnable {
                binding.rvNavDrawer.invalidate()
                binding.rvNavDrawer.requestLayout()
            }, 10)
        }
        swipeToDeleteFolder(binding.rvNavDrawer)
    }
}