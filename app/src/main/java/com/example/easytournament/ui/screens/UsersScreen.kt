import androidx.activity.result.launch
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import com.example.easytournament.data.model.Profile
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.easytournament.data.repository.UserRepository
import kotlinx.coroutines.launch
import kotlin.text.contains

@Composable
fun UsersScreen(userRepository: UserRepository = remember { UserRepository() }, currentUserProfile: Profile?, onUserClick: (String) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var allUsers by remember { mutableStateOf<List<Profile>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val refreshUsers = {
        scope.launch {
            isLoading = true
            allUsers = userRepository.getAllProfiles()
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        refreshUsers()
    }

    val filteredUsers = allUsers.filter {
        it.username.contains(searchQuery, ignoreCase = true)
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Buscar usuario...") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            leadingIcon = { Icon(Icons.Default.Search, null) }
        )

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            LazyColumn {
                items(filteredUsers) { profile ->
                    ListItem(
                        headlineContent = { Text(profile.username) },
                        supportingContent = { Text("Reputación: ${"%.1f".format(profile.reputation)}") },
                        leadingContent = { Icon(Icons.Default.Person, null) },
                        trailingContent = {
                            if (currentUserProfile?.is_admin == true) {
                                Row {
                                    IconButton(onClick = {
                                        scope.launch {
                                            if (userRepository.updateReputation(profile.id, 0.5f)) {
                                                allUsers =
                                                    userRepository.getAllProfiles() // Refrescar lista
                                            }
                                        }
                                    }) {
                                        Text(
                                            "-0.5",
                                            color = Color.Red,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                    IconButton(onClick = {
                                        scope.launch {
                                            if (userRepository.updateReputation(
                                                    profile.id,
                                                    -0.5f
                                                )
                                            ) {
                                                allUsers =
                                                    userRepository.getAllProfiles() // Refrescar lista
                                            }
                                        }
                                    }) {
                                        Text(
                                            "+0.5",
                                            color = Color(0xFF2E7D32),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                        },
                        modifier = Modifier.clickable { onUserClick(profile.id) }
                    )
                }
            }
        }
    }
}