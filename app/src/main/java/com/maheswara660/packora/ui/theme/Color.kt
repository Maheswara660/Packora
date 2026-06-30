package com.maheswara660.packora.ui.theme

import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650A4)
val PurpleGrey40 = Color(0xFF625B71)
val Pink40 = Color(0xFF7D5260)

data class CustomAccent(
    val name: String,
    val primary: Color,
    val secondary: Color
)

val CustomAccents = listOf(
    CustomAccent("Teal", Color(0xFF008080), Color(0xFF009688)),
    CustomAccent("Blue", Color(0xFF0D47A1), Color(0xFF1565C0)),
    CustomAccent("Green", Color(0xFF1B5E20), Color(0xFF2E7D32)),
    CustomAccent("Orange", Color(0xFFE65100), Color(0xFFEF6C00)),
    CustomAccent("Violet", Color(0xFF4A148C), Color(0xFF6A1B9A)),
    CustomAccent("Red", Color(0xFFB71C1C), Color(0xFFC62828))
)