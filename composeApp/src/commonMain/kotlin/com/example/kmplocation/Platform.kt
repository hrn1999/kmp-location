package com.example.kmplocation

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform