package com.wwm.todo

data class TaskItem(
    val id: String?,
    val title: String,
    val description: String?,
    val status: String?,
    val _version: Int?,
    val _deleted: Boolean?
)