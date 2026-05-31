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

/* Pantalla de login y registro */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    repository: AuthRepository,
    onLoginSuccess: () -> Unit
) {
    /* Gestión de Estados */
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    /* Alterna entre Login y Registro */
    var isLogin by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    /* Estados para el cumplimiento legal (RGPD) */
    var acceptedTerms by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    /* Lógica de validación y envío de datos */
    val onAuthSubmit = {
        if (email.isNotBlank() && password.isNotBlank()) {
            var isValid = true

            /* Validación de formato de email */
            val emailPattern = android.util.Patterns.EMAIL_ADDRESS
            if (!emailPattern.matcher(email.trim()).matches()) {
                Toast.makeText(context, "Email inválido", Toast.LENGTH_SHORT).show()
                isValid = false
            }

            /* Reglas a la hora de registrarse */
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

                /* Bloqueo por cumplimiento normativo */
                if (!acceptedTerms) {
                    Toast.makeText(context, "Debes aceptar los términos legales", Toast.LENGTH_SHORT).show()
                    isValid = false
                }
            }

            if (isValid) {
                isLoading = true
                scope.launch {
                    val errorMsg = try {
                    /* Interacción con el repositorio de Supabase */
                        if (isLogin) {
                            repository.login(email.trim(), password.trim())
                        } else {
                            repository.signUp(email.trim(), password.trim(), username.trim())
                        }
                    } catch (e: Exception) {
                        "Error de conexión"
                    }

                    isLoading = false
                    if (errorMsg == null) {
                        onLoginSuccess()
                    } else {
                        /* Mapeo de errores técnicos a mensajes amigables para el usuario */
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

    /* Diseño de la Interfaz */
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

            /* Estilo personalizado para los campos de texto */
            val textFieldColors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedBorderColor = Color.Red,
                focusedLabelColor = Color.Red,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )

            /* Renderizado condicional de campos según el modo Autenticación/Registro */
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

            /* Sección de confirmación de terminos y condiciones */
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

            /* Feedback de carga */
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Button(onClick = { onAuthSubmit() }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (isLogin) "Entrar" else "Registrarse")
                }

                /* Limpieza de estados al conmutar entre pantallas */
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

/* Dialog de términos y condiciones */
@Composable
fun TermsAndConditionsDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Política de Privacidad y Términos") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = """
                        Política de Privacidad y Términos de Uso
                        De conformidad con el Reglamento (UE) 2016/679 del Parlamento Europeo y del Consejo (RGPD) y la Ley Orgánica 3/2018 (LOPDGDD), se informa al usuario de lo siguiente:
                        1. Responsable del tratamiento
                            EasyTournament Inc.
                        2. Finalidad del tratamiento
                            Los datos personales facilitados serán utilizados para:
                            Gestión de cuentas de usuario.
                            Organización y administración de torneos.
                            Autenticación y acceso a la plataforma.
                            Comunicación relacionada con el funcionamiento de la aplicación.
                        3. Datos recopilados
                            La aplicación podrá recopilar:
                                Nombre de usuario.
                                Correo electrónico.
                                Información relacionada con torneos y participación.
                        4. Legitimación
                            La base legal para el tratamiento de los datos es el consentimiento del usuario al registrarse en la plataforma.
                        5. Conservación de los datos
                            Los datos se conservarán mientras el usuario mantenga activa su cuenta o hasta que solicite su eliminación.
                        6. Cesión de datos
                            No se cederán datos personales a terceros salvo obligación legal o necesidad técnica derivada de servicios utilizados por la aplicación (por ejemplo, servicios de almacenamiento o autenticación).
                        7. Derechos del usuario
                            El usuario puede ejercer en cualquier momento sus derechos de:
                                Acceso.
                                Rectificación.
                                Supresión.
                                Limitación del tratamiento.
                                Portabilidad.
                                Oposición.
                        8. Seguridad
                            EasyTournament adopta medidas técnicas y organizativas adecuadas para proteger los datos personales frente a accesos no autorizados, pérdida o alteración.
                        9. Edad mínima
                            El usuario declara ser mayor de 14 años o contar con autorización de sus representantes legales.
                        10. Aceptación
                            Al registrarse en la plataforma, el usuario acepta expresamente esta política de privacidad y el tratamiento de sus datos para el correcto funcionamiento de la aplicación.
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