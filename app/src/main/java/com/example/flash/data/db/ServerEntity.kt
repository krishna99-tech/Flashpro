package com.example.flash.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.flash.RemoteServer

@Entity(tableName = "remote_servers")
data class ServerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val host: String,
    val user: String,
    val password: String,
    val port: Int
)

fun ServerEntity.toDomain() = RemoteServer(
    id = id,
    name = name,
    host = host,
    user = user,
    password = password,
    port = port
)

fun RemoteServer.toEntity() = ServerEntity(
    id = id,
    name = name,
    host = host,
    user = user,
    password = password,
    port = port
)
