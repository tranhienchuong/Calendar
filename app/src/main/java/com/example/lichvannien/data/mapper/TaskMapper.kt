package com.example.lichvannien.data.mapper

import com.example.lichvannien.data.local.entity.TaskEntity
import com.example.lichvannien.domain.model.Task

fun TaskEntity.toDomain(): Task = Task(
    id = id,
    title = title,
    date = date,
    startTime = startTime,
    endTime = endTime,
    deadline = deadline,
    location = location,
    isCompleted = isCompleted,
    category = category,
    colorHex = colorHex,
    colorTag = colorTag,
    dueTime = dueTime,
    repeatType = repeatType,
    reminderType = reminderType
)

fun Task.toEntity(): TaskEntity = TaskEntity(
    id = id,
    title = title,
    date = date,
    startTime = startTime,
    endTime = endTime,
    deadline = deadline,
    location = location,
    isCompleted = isCompleted,
    category = category,
    colorHex = colorHex,
    colorTag = colorTag,
    dueTime = dueTime,
    repeatType = repeatType,
    reminderType = reminderType
)

fun List<TaskEntity>.toDomain(): List<Task> = map { it.toDomain() }
fun List<Task>.toEntity(): List<TaskEntity> = map { it.toEntity() }
