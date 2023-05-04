package com.example.notesapp.adapters

import android.R.attr.data
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.notesapp.R
import com.example.notesapp.databinding.NoteFolderItemBinding
import com.example.notesapp.model.Folder
import com.google.android.material.checkbox.MaterialCheckBox


class FolderNoteAdapter(val folders: List<Int>): ListAdapter<Folder, FolderNoteAdapter.FolderNoteViewHolder>(DiffUtilCallbackFolder()) {
    inner class FolderNoteViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val binding = NoteFolderItemBinding.bind(itemView)
        val name: TextView = binding.itemName
        val icon: MaterialCheckBox = binding.itemIcon
        val parent: CardView = binding.itemParent
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderNoteViewHolder {
        return FolderNoteViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.note_folder_item, parent, false))
    }

    override fun onBindViewHolder(holder: FolderNoteViewHolder, position: Int) {
        getItem(position).let{ folder ->
            holder.apply{
                name.text = folder.name
                val palette = parent.context.getResources().getIntArray(R.array.color_picker)
                icon.buttonTintList = ColorStateList.valueOf(palette[folder.color])
                if(folders.contains(folder.id)){
                    icon.isChecked = true
                }
            }
        }
    }

    fun getId(position: Int): Int {
        return getItem(position).id
    }
}