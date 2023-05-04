package com.example.notesapp.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey

@Entity(tableName="folder_note",
    primaryKeys = ["folder_id", "note_id"],
    foreignKeys = [ForeignKey(entity = Folder::class,
    childColumns = ["folder_id"],
    parentColumns = ["id"],
    onDelete = CASCADE),
    ForeignKey(entity = Note::class,
    childColumns = ["note_id"],
    parentColumns = ["id"],
    onDelete = CASCADE),])
class FolderNote (
    @ColumnInfo(name = "folder_id")
    var folder_id: Int,

    @ColumnInfo(name = "note_id")
    var note_id: Int,
)