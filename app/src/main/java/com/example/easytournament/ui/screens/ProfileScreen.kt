package com.example.easytournament.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.easytournament.data.model.Profile
import com.example.easytournament.data.repository.AuthRepository
import kotlinx.coroutines.launch

/* Pantalla de Perfil */
@Composable
fun ProfileScreen(
    repository: AuthRepository,
    userId: String?,
    onBack: (() -> Unit)? = null
) {
    /* Gestión de estados y corrutinas */
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var profile by remember { mutableStateOf<Profile?>(null) }

    /* Estados para el modo edición y persistencia temporal en formularios */
    var isEditing by remember { mutableStateOf(false) }
    var editUsername by remember { mutableStateOf("") }
    var editSteam by remember { mutableStateOf("") }
    var editRiot by remember { mutableStateOf("") }

    /* Determina si el usuario tiene permisos de escritura */
    val isMyProfile = userId == null || userId == repository.getCurrentUser()?.id

    /* Carga de perfil de usuario */
    fun loadData() {
        scope.launch {
            val p = if (isMyProfile) repository.getCurrentProfile()
            else repository.getProfileById(userId!!)

            profile = p

            p?.let {
                editUsername = it.username
                editSteam = it.steam_username ?: ""
                editRiot = it.riot_username ?: ""
            }
        }
    }

    /* Sincronización de Ui tras cargar usuario*/
    LaunchedEffect(userId) {
        loadData()
    }

    profile?.let { p ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(100.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.padding(20.dp).size(64.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            /* Edicion de datos del perfil */
            if (isEditing) {
                OutlinedTextField(
                    value = editUsername,
                    onValueChange = { editUsername = it },
                    label = { Text("Nombre de Usuario") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = editSteam,
                    onValueChange = { editSteam = it },
                    label = { Text("Steam ID") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = editRiot,
                    onValueChange = { editRiot = it },
                    label = { Text("Riot ID") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        scope.launch {
                            /* Validación de tamaño de nombre de cliente */
                            if (editUsername.length < 4) {
                                Toast.makeText(context, "Nombre demasiado corto", Toast.LENGTH_SHORT).show()
                                return@launch
                            }
                            /* Persistencia de cambios */
                            val success = repository.updateProfile(
                                username = editUsername,
                                steam = editSteam,
                                riot = editRiot
                            )

                            if (success) {
                                /* Actualización del perfil para reflejar cambios */
                                profile = p.copy(
                                    username = editUsername,
                                    steam_username = editSteam.ifBlank { null },
                                    riot_username = editRiot.ifBlank { null }
                                )

                                isEditing = false
                                Toast.makeText(context, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Error al guardar cambios", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }) {
                        Text("Guardar")
                    }

                    /* En caso de cancelación se restauran valores anteriores */
                    TextButton(onClick = {
                        editUsername = p.username
                        editSteam = p.steam_username ?: ""
                        editRiot = p.riot_username ?: ""
                        isEditing = false
                    }) {
                        Text("Cancelar")
                    }
                }
            } else {

                /* Visualización del perfil */
                Text(p.username, style = MaterialTheme.typography.headlineMedium)

                /* Reputación cambiante de color en funcion de si es positiva o negativa */
                Text("Reputación: ${p.reputation}",
                    color = if (p.reputation >= 0) Color(0xFF006400) else Color.Red)
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                /* Nombre de usuario de Steam y Riot */
                InfoRow(label = "Steam ID", value = p.steam_username ?: "No vinculado")
                InfoRow(label = "Riot ID", value = p.riot_username ?: "No vinculado")
                /* Botón de edición en caso de que sea su perfil */
                if (isMyProfile) {
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = { isEditing = true }) {
                        Text("Editar Perfil")
                    }
                }
                /* En caso de acceder desde la pestaña de gestión de usuario habrá un botón de retorno */
                if (userId != null && onBack != null) {
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Cerrar")
                    }
                }
            }
        }
    }
}

/* Componente reutilizable para filas de información en el perfil */
@Composable
fun InfoRow(label: String, value: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = FontWeight.Bold)
        Text(value)
    }
}