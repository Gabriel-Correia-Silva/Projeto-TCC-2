package com.example.projeto_ttc2.presentation.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(placeholder: String) {
    var text by remember { mutableStateOf("") }

    TextField(
        value = text,
        onValueChange = { text = it },
        placeholder = {
            Text(
                text = placeholder,
                color = Color(0xFF007C91)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Pesquisar",
                tint = Color(0xFF007C91)
            )
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor     = Color.White,
            unfocusedContainerColor   = Color.White,
            disabledContainerColor    = Color.White,
            errorContainerColor       = Color.White,
            focusedIndicatorColor     = Color.Transparent,
            unfocusedIndicatorColor   = Color.Transparent,
            disabledIndicatorColor    = Color.Transparent,
            errorIndicatorColor       = Color.Transparent,
            focusedPlaceholderColor   = Color(0xFF007C91),
            unfocusedPlaceholderColor = Color(0xFF007C91),
            disabledPlaceholderColor  = Color(0xFF007C91),
            errorPlaceholderColor     = Color(0xFF007C91)
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .padding(horizontal = 16.dp)
    )
}
