package com.example.notesapp.adapters

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.notesapp.R
import com.example.notesapp.databinding.DrawerMenuItemBinding
import com.example.notesapp.fragments.NoteFragment
import com.example.notesapp.model.Folder
import com.example.notesapp.viewModel.NoteViewModel

class FoldersAdapter(val updateFolder: (Folder) -> Unit): ListAdapter<Folder, FoldersAdapter.FoldersViewHolder>(DiffUtilCallbackFolder()) {
    inner class FoldersViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val binding = DrawerMenuItemBinding.bind(itemView)
        val name: TextView = binding.itemName
        val icon: ImageView = binding.itemIcon
        val parent: CardView = binding.itemParent
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoldersViewHolder {
        return FoldersViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.drawer_menu_item, parent, false))
    }

    override fun onBindViewHolder(holder: FoldersViewHolder, position: Int) {
        getItem(position).let{ folder ->
            holder.apply{
                name.text = folder.name
                val palette = parent.context.getResources().getIntArray(R.array.color_picker)
                icon.setColorFilter(palette[folder.color])

                parent.setOnClickListener {
                    Toast.makeText(parent.context, "Сlick detected", Toast.LENGTH_SHORT).show()
                }
                parent.setOnLongClickListener{
                    updateFolder(folder)
                    true
                }
            }
        }
    }
}