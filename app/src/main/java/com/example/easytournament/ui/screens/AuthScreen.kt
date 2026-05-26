package com.example.easytournament.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.easytournament.data.repository.AuthRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    repository: AuthRepository,
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var isLogin by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    var acceptedTerms by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val onAuthSubmit = {
        if (email.isNotBlank() && password.isNotBlank()) {
            var isValid = true

            val emailPattern = android.util.Patterns.EMAIL_ADDRESS
            if (!emailPattern.matcher(email.trim()).matches()) {
                Toast.makeText(context, "Email inválido", Toast.LENGTH_SHORT).show()
                isValid = false
            }

            if (!isLogin) {
                if (username.trim().length < 5) {
                    Toast.makeText(context, "Usuario mínimo 5 caracteres", Toast.LENGTH_SHORT)
                        .show()
                    isValid = false
                } else if (password.length < 6) {
                    Toast.makeText(context, "Password mínima 6 caracteres", Toast.LENGTH_SHORT)
                        .show()
                    isValid = false
                }
                else if (password != confirmPassword) {
                    Toast.makeText(context, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                    isValid = false
                }
                if (!acceptedTerms) {
                    Toast.makeText(context, "Debes aceptar los términos legales", Toast.LENGTH_SHORT).show()
                    isValid = false
                }
            }

            if (isValid) {
                isLoading = true
                scope.launch {
                    val errorMsg = try {
                        if (isLogin) {
                            repository.login(email.trim(), password.trim())
                        } else {
                            repository.signUp(email.trim(), password.trim(), username.trim())
                        }
                    } catch (e: Exception) {
                        "Error de conexión" // Esto atrapa fallos de Wi-Fi/Servidor
                    }

                    isLoading = false
                    if (errorMsg == null) {
                        onLoginSuccess()
                    } else {
                        val friendlyMessage = when {
                            errorMsg.contains("Error de conexión", true) ||
                                    errorMsg.contains("Unable to resolve host", true) ||
                                    errorMsg.contains("HttpRequestException", true) ||
                                    errorMsg.contains("ConnectException", true) -> {
                                "Sin conexión: Revisa tu internet o el estado del servidor"
                            }
                            errorMsg.contains("already registered", true) -> "Este email ya está en uso"
                            errorMsg.contains("Invalid login credentials", true) -> "Email o contraseña incorrectos"

                            else -> "Error técnico: $errorMsg"
                        }
                        Toast.makeText(context, friendlyMessage, Toast.LENGTH_LONG).show()
                    }
                }
            }
        } else {
            Toast.makeText(context, "Rellena todos los campos", Toast.LENGTH_SHORT).show()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isLogin) "Iniciar Sesión" else "Crear Cuenta",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            val textFieldColors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedBorderColor = Color.Red,
                focusedLabelColor = Color.Red,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )

            if (!isLogin) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { if (!it.contains("\n")) username = it },
                    label = { Text("Nombre de Usuario") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            OutlinedTextField(
                value = email,
                onValueChange = { if (!it.contains("\n")) email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = textFieldColors,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { if (!it.contains("\n")) password = it },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    onAuthSubmit()
                })
            )

            if (!isLogin) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { if (!it.contains("\n")) confirmPassword = it },
                    label = { Text("Confirmar Contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = textFieldColors,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                        onAuthSubmit()
                    })
                )
            }

            if (!isLogin) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Checkbox(
                        checked = acceptedTerms,
                        onCheckedChange = { acceptedTerms = it }
                    )
                    TextButton(onClick = { showTermsDialog = true }) {
                        Text(
                            "Acepto los términos y el uso de mis datos (RGPD)",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (acceptedTerms) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (showTermsDialog) {
                TermsAndConditionsDialog(onDismiss = { showTermsDialog = false })
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Button(onClick = { onAuthSubmit() }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (isLogin) "Entrar" else "Registrarse")
                }

                TextButton(onClick = {
                    isLogin = !isLogin
                    email = ""; password = ""; username = ""; confirmPassword = ""
                    acceptedTerms = false
                }) {
                    Text(if (isLogin) "¿No tienes cuenta? Regístrate" else "¿Ya tienes cuenta? Inicia sesión")
                }
            }
        }
    }
}

@Composable
fun TermsAndConditionsDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Política de Privacidad y Términos") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = """
                        De conformidad con el RGPD (UE) 2016/679 y la LOPDGDD 3/2018, le informamos:
                        
                        1. Responsable: EasyTournament Inc.
                        2. Finalidad: Gestión de usuarios y organización de torneos.
                        3. Legitimación: Consentimiento del interesado.
                        4. Derechos: Puede acceder, rectificar y suprimir sus datos en cualquier momento.
                        5. Conservación: Sus datos se conservarán mientras no solicite la baja de la cuenta.
                        
                        Al registrarse, acepta que tratemos sus datos para el correcto funcionamiento de la plataforma. No cedemos datos a terceros salvo obligación legal.
                    """.trimIndent(),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}