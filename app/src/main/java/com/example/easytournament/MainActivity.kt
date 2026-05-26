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

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val authRepository = AuthRepository()
        val tournamentRepository = TournamentRepository()
        val userRepository = UserRepository()
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        setContent {
            var isDarkMode by remember {
                mutableStateOf(prefs.getBoolean("dark_mode", false))
            }
            EasyTournamentTheme(darkTheme = isDarkMode){
                val navController = rememberNavController()
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                var isLoggedIn by remember { mutableStateOf(false) }
                var currentUserProfile by remember { mutableStateOf<com.example.easytournament.data.model.Profile?>(null) }
                LaunchedEffect(isLoggedIn) {
                    val user = authRepository.getCurrentUser()
                    if (user != null) {
                        isLoggedIn = true
                        currentUserProfile = authRepository.getCurrentProfile()
                    }
                }
                if (!isLoggedIn) {
                    AuthScreen(
                        repository = authRepository,
                        onLoginSuccess = { isLoggedIn = true }
                    )
                } else {
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
                                NavigationDrawerItem(
                                    label = { Text("Mi Perfil") },
                                    selected = navController.currentBackStackEntryAsState().value?.destination?.route == "profile",
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
                                NavigationDrawerItem(
                                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                                    label = { Text("Torneos") },
                                    selected = navController.currentBackStackEntryAsState().value?.destination?.route == "home",
                                    onClick = {
                                        navController.navigate("home") {
                                            popUpTo("home") { inclusive = true }
                                        }
                                        scope.launch { drawerState.close() }
                                    }
                                )
                                if (currentUserProfile?.is_admin == true) {
                                    NavigationDrawerItem(
                                        icon = {
                                            Icon(
                                                Icons.Default.People,
                                                contentDescription = null
                                            )
                                        },
                                        label = { Text("Gestión de Usuarios") },
                                        selected = navController.currentBackStackEntryAsState().value?.destination?.route == "users",
                                        onClick = {
                                            navController.navigate("users")
                                            scope.launch { drawerState.close() }
                                        }
                                    )
                                }
                                Spacer(Modifier.weight(1f))
                                NavigationDrawerItem(
                                    label = { Text("Ajustes") },
                                    selected = navController.currentBackStackEntryAsState().value?.destination?.route == "settings",
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
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        ) { paddingValues ->
                            Box(modifier = Modifier.padding(paddingValues)) {
                                NavHost(navController = navController, startDestination = "home") {
                                    composable("home") {
                                        HomeScreen(authRepository,tournamentRepository)
                                    }
                                    composable("users") {
                                        UsersScreen(userRepository =userRepository, currentUserProfile = currentUserProfile,onUserClick = { userId ->
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
