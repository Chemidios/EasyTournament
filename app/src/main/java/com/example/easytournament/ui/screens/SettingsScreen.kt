package com.example.easytournament.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text("Ajustes", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(20.dp))

        ListItem(
            headlineContent = { Text("Notificaciones") },
            supportingContent = { Text("Recibir avisos de nuevos torneos") },
            trailingContent = { Switch(checked = true, onCheckedChange = {}) }
        )

        ListItem(
            headlineContent = { Text("Idioma") },
            supportingContent = { Text("Español") },
            modifier = Modifier.padding(top = 8.dp)
        )

        ListItem(
            headlineContent = { Text("Modo Oscuro") },
            supportingContent = { Text("Cambiar la apariencia de la aplicación") },
            leadingContent = {
                Icon(
                    imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                    contentDescription = null
                )
            },
            trailingContent = {
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = { onDarkModeChange(it) }
                )
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
    }
}