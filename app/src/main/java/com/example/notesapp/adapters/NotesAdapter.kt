package com.example.notesapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.navigation.Navigation
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.notesapp.R
import com.example.notesapp.databinding.NoteItemLayoutBinding
import com.example.notesapp.fragments.NoteFragmentDirections
import com.example.notesapp.model.Note
import com.example.notesapp.utils.hideKeyboard
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonVisitor
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import org.commonmark.node.SoftLineBreak

class NotesAdapter: ListAdapter<Note, NotesAdapter.NotesViewHolder>(DiffUtilCallback()) {
    inner class NotesViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val binding = NoteItemLayoutBinding.bind(itemView)
        val title: TextView = binding.itemTitle
        val content: TextView = binding.itemContent
        val date: TextView = binding.itemDate
        val parent: CardView = binding.itemParent
        val markWon = Markwon.builder(itemView.context)
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(TaskListPlugin.create(itemView.context))
            .usePlugin(object: AbstractMarkwonPlugin(){
                override fun configureVisitor(builder: MarkwonVisitor.Builder) {
                    super.configureVisitor(builder)
                    builder.on(
                        SoftLineBreak::class.java
                    ){visitor , _ -> visitor.forceNewLine()}
                }
            })
            .build()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {
        return NotesViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.note_item_layout, parent, false))
    }

    override fun onBindViewHolder(holder: NotesViewHolder, position: Int) {
        getItem(position).let{ note ->
            holder.apply{
                parent.transitionName = "recyclerView_${note.id}"
                title.text = note.title
                markWon.setMarkdown(content, note.content)
                date.text = note.date

                itemView.setOnClickListener {
                    val action=NoteFragmentDirections.actionNoteFragmentToUpdateFragment()
                        .setNote(note)
                    val extras= FragmentNavigatorExtras(parent to "recyclerView_${note.id}")
                    it.hideKeyboard()
                    Navigation.findNavController(it).navigate(action, extras)
                }
                content.setOnClickListener {
                    val action=NoteFragmentDirections.actionNoteFragmentToUpdateFragment()
                        .setNote(note)
                    val extras= FragmentNavigatorExtras(parent to "recyclerView_${note.id}")
                    it.hideKeyboard()
                    Navigation.findNavController(it).navigate(action, extras)
                }
            }
        }
    }
}