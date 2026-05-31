package com.example.easytournament

import UsersScreen
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.easytournament.data.repository.AuthRepository
import com.example.easytournament.data.repository.TournamentRepository
import com.example.easytournament.data.repository.UserRepository
import com.example.easytournament.ui.screens.AuthScreen
import com.example.easytournament.ui.screens.HomeScreen
import com.example.easytournament.ui.screens.ProfileScreen
import com.example.easytournament.ui.screens.SettingsScreen
import com.example.easytournament.ui.theme.EasyTournamentTheme
import kotlinx.coroutines.launch
import androidx.core.content.edit
import com.example.easytournament.ui.theme.MMBordeaux

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*Inicialización de repositorios*/

        val authRepository = AuthRepository()
        val tournamentRepository = TournamentRepository()
        val userRepository = UserRepository()
        /*Persistencia ligera para configuración*/
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        setContent {
            /* Estado para el control del tema (Claro/Oscuro) */
            var isDarkMode by remember {
                mutableStateOf(prefs.getBoolean("dark_mode", false))
            }
            EasyTournamentTheme(darkTheme = isDarkMode){
                val navController = rememberNavController()
                /*Estado de la barra lateral*/
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                /* Estados globales de la sesión y el perfil del usuario activo */
                var isLoggedIn by remember { mutableStateOf(false) }
                var currentUserProfile by remember { mutableStateOf<com.example.easytournament.data.model.Profile?>(null) }
                /* Comprobación automática de sesión al iniciar la aplicación */
                LaunchedEffect(isLoggedIn) {
                    val user = authRepository.getCurrentUser()
                    if (user != null) {
                        isLoggedIn = true
                        /* Sincronización del perfil para obtener roles y reputación */
                        currentUserProfile = authRepository.getCurrentProfile()
                    }
                }
                /* Observador del BackStack: se extrae la ruta actual para simplificar la lógica de selección del menú */
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                /* Flujo de entrada: Si no hay sesión, se fuerza la pantalla de autenticación */
                if (!isLoggedIn) {
                    AuthScreen(
                        repository = authRepository,
                        onLoginSuccess = { isLoggedIn = true }
                    )
                } else {
                    /* Estructura principal con Menú Lateral de navegación */
                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            ModalDrawerSheet {
                                Text(
                                    "EasyTournament",
                                    modifier = Modifier.padding(20.dp),
                                    style = MaterialTheme.typography.headlineMedium
                                )
                                HorizontalDivider()
                                /* Item de navegación: Mi Perfil */
                                NavigationDrawerItem(
                                    label = { Text("Mi Perfil") },
                                    selected = currentRoute == "profile",
                                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                                    onClick = {
                                        navController.navigate("profile") {
                                            popUpTo("home") { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                        scope.launch { drawerState.close() }
                                    }
                                )
                                /* Opción: Explorar Torneos */
                                NavigationDrawerItem(
                                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                                    label = { Text("Torneos") },
                                    selected = currentRoute == "home",
                                    onClick = {
                                        navController.navigate("home") {
                                            popUpTo("home") { inclusive = true }
                                        }
                                        scope.launch { drawerState.close() }
                                    }
                                )
                                /* Opción: Gestión de Usuarios */
                                /* Control de acceso basado en roles: Vista exclusiva para Administradores */
                                if (currentUserProfile?.is_admin == true) {
                                    NavigationDrawerItem(
                                        icon = {
                                            Icon(
                                                Icons.Default.People,
                                                contentDescription = null
                                            )
                                        },
                                        label = { Text("Gestión de Usuarios") },
                                        selected = currentRoute == "users",
                                        onClick = {
                                            navController.navigate("users")
                                            scope.launch { drawerState.close() }
                                        }
                                    )
                                }
                                Spacer(Modifier.weight(1f))
                                /* Opción: Ajustes */
                                NavigationDrawerItem(
                                    label = { Text("Ajustes") },
                                    selected = currentRoute == "settings",
                                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                    onClick = {
                                        navController.navigate("settings") {
                                            popUpTo("home") { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                        scope.launch { drawerState.close() }
                                    }
                                )
                                /* Acción de Logout: Limpieza de estados y desconexión del backend */
                                TextButton(
                                    onClick = {
                                        scope.launch {
                                            authRepository.logout()
                                            isLoggedIn = false
                                        }
                                    },
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text("Cerrar Sesión", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    ) {
                        Scaffold(
                            /* Barra superior con personalización según modo oscuro o claro */
                            topBar = {
                                CenterAlignedTopAppBar(
                                    title = {
                                        Text(
                                            "EasyTournament",
                                            style = MaterialTheme.typography.titleLarge,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    },
                                    actions = {
                                        IconButton(onClick = {
                                            scope.launch {
                                                authRepository.logout()
                                                isLoggedIn = false
                                            }
                                        }) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                                contentDescription = "Cerrar Sesión",
                                                tint = MaterialTheme.colorScheme.onPrimary
                                            )
                                        }
                                    },
                                    navigationIcon = {
                                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                            Icon(
                                                imageVector = Icons.Default.Menu,
                                                contentDescription = "Menú",
                                                tint = MaterialTheme.colorScheme.onPrimary
                                            )
                                        }
                                    },
                                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                        containerColor = if (isDarkMode) {
                                            MMBordeaux
                                        } else {
                                            MaterialTheme.colorScheme.primary
                                        },
                                        titleContentColor = if (isDarkMode) Color.White else MaterialTheme.colorScheme.onPrimary,
                                        navigationIconContentColor = if (isDarkMode) Color.White else MaterialTheme.colorScheme.onPrimary,
                                        actionIconContentColor = if (isDarkMode) Color.White else MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        ) { paddingValues ->
                            Box(modifier = Modifier.padding(paddingValues)) {
                                /* Gestor de navegación para la gestión de pantallas */
                                NavHost(navController = navController, startDestination = "home") {
                                    composable("home") {
                                        HomeScreen(authRepository,tournamentRepository, isDarkMode)
                                    }
                                    composable("users") {
                                        UsersScreen(userRepository =userRepository, currentUserProfile = currentUserProfile,onUserClick = { userId ->
                                            /* Navegación hacia perfiles específicos mediante uso de id */
                                            navController.navigate("profile/$userId")
                                        })
                                    }
                                    composable("profile/{userId}") { backStackEntry ->
                                        val userId = backStackEntry.arguments?.getString("userId")
                                        ProfileScreen(authRepository, userId, onBack = { navController.popBackStack() })
                                    }
                                    composable("profile") {
                                        ProfileScreen(authRepository, null)
                                    }
                                    composable("settings") {
                                        SettingsScreen(
                                            isDarkMode = isDarkMode,
                                            onDarkModeChange = { newValue ->
                                                isDarkMode = newValue
                                                /* Persistencia de la preferencia de usuario en SharedPreferences */
                                                prefs.edit { putBoolean("dark_mode", newValue) } }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
