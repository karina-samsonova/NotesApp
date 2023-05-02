package com.example.notesapp.adapters

import androidx.recyclerview.widget.DiffUtil
import com.example.notesapp.model.Folder

class DiffUtilCallbackFolder: DiffUtil.ItemCallback<Folder>() {
    override fun areItemsTheSame(oldItem: Folder, newItem: Folder): Boolean {
        return oldItem.id==newItem.id
    }

    override fun areContentsTheSame(oldItem: Folder, newItem: Folder): Boolean {
        return oldItem.id==newItem.id
    }
}