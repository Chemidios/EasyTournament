package com.example.easytournament.ui.screens

import android.app.DatePickerDialog
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.easytournament.data.model.Profile
import com.example.easytournament.data.model.Tournament
import com.example.easytournament.data.repository.AuthRepository
import com.example.easytournament.data.repository.TournamentRepository
import com.example.easytournament.data.model.Review
import com.example.easytournament.utils.DateUtils
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    repository: AuthRepository,
    tournamentRepo: TournamentRepository
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Estados de Perfil y Carga
    var profile by remember { mutableStateOf<Profile?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }

    // Estados de Diálogos
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedTournamentDetails by remember { mutableStateOf<Tournament?>(null) }

    // Función para priorizar estados
    val statusPriority = { status: String ->
        when (status.lowercase()) {
            "abierto" -> 1
            "cerrado" -> 2
            "finalizado" -> 3
            else -> 4
        }
    }

    // Estados de Torneos
    var allTournaments by remember { mutableStateOf<List<Tournament>>(emptyList()) }
    var myTournaments by remember { mutableStateOf<List<Tournament>>(emptyList()) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Mis Torneos", "Inscritos", "Explorar")

    // Filtros
    var selectedCategoryFilter by remember { mutableStateOf("Todos") }
    val categoryOptions = listOf("Todos", "Videojuegos", "Juegos de Mesa", "Deportes", "Otros")
    var hideClosed by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val canCreate = profile?.is_admin == true || (profile?.reputation ?: -1f) >= 0f

    val refreshData = {
        scope.launch {
            isRefreshing = true
            val deferredAll = tournamentRepo.getAllTournaments()
            val deferredMy = repository.getMyEnrolledTournaments()

            val now = LocalDate.now()
            deferredAll.forEach { tournament ->
                val tDate = ZonedDateTime.parse(tournament.start_date).toLocalDate()

                if (now.isAfter(tDate) && tournament.status != "finalizado") {
                    tournamentRepo.updateTournamentStatus(tournament.id!!, "finalizado")
                }
            }
            val finalAll = tournamentRepo.getAllTournaments()

            allTournaments = finalAll
            myTournaments = deferredMy

            val updatedProfile = repository.getCurrentProfile()
            profile = updatedProfile

            selectedTournamentDetails?.let { current ->
                selectedTournamentDetails = finalAll.find { it.id == current.id }
            }
            isRefreshing = false
        }
    }

    val handleEnroll = { t: Tournament ->
        scope.launch {
            val error = repository.enrollInTournament(t)
            if (error == null) {
                Toast.makeText(context, "¡Inscrito con éxito!", Toast.LENGTH_SHORT).show()
                refreshData()
                selectedTournamentDetails = null
            } else {
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(Unit) {
        profile = repository.getCurrentProfile()
        refreshData()
        isLoading = false
    }

    LaunchedEffect(selectedTab) {
        refreshData()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                color = if (selectedTab == index)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                    )
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                if (isLoading || isRefreshing) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    when (selectedTab) {
                        0 -> { // MIS TORNEOS CREADOS
                            val myCreated = allTournaments
                                .filter { it.creator_id == profile?.id }
                                .sortedWith(
                                    compareBy<Tournament> { statusPriority(it.status) }
                                        .thenBy { it.start_date }
                                )
                            TournamentList(
                                list = myCreated,
                                emptyMessage = "No has creado torneos todavía",
                                myEnrolled = myTournaments,
                                isAdmin = profile?.is_admin == true,
                                currentUserId = profile?.id,
                                onItemClick = { selectedTournamentDetails = it },
                                onEnrollClick = { handleEnroll(it) }
                            )
                        }
                        1 -> {
                            val enrichedMyTournaments = allTournaments
                                .filter { allT -> myTournaments.any { myT -> myT.id == allT.id } }
                                .sortedWith(
                                    compareBy<Tournament> { statusPriority(it.status) }
                                        .thenBy { it.start_date }
                                )
                            TournamentList(
                                list = enrichedMyTournaments,
                                emptyMessage = "No estás inscrito en nada",
                                myEnrolled = myTournaments,
                                currentUserId = profile?.id,
                                isAdmin = profile?.is_admin == true,
                                onItemClick = { selectedTournamentDetails = it },
                                onEnrollClick = { handleEnroll(it) }
                            )
                        }
                        2 -> {
                            Column(modifier = Modifier.fillMaxSize()) {

                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    placeholder = { Text("Buscar torneo o juego...") },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                    trailingIcon = {
                                        if (searchQuery.isNotEmpty()) {
                                            IconButton(onClick = { searchQuery = "" }) {
                                                Icon(Icons.Default.Clear, contentDescription = "Borrar")
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    )
                                )
                                ScrollableTabRow(
                                    selectedTabIndex = categoryOptions.indexOf(
                                        selectedCategoryFilter
                                    ),
                                    edgePadding = 16.dp,
                                    divider = {}
                                ) {
                                    categoryOptions.forEach { category ->
                                        FilterChip(
                                            modifier = Modifier.padding(
                                                horizontal = 4.dp,
                                                vertical = 8.dp
                                            ),
                                            selected = selectedCategoryFilter == category,
                                            onClick = { selectedCategoryFilter = category },
                                            label = { Text(category) }
                                        )
                                    }
                                }
                                val filteredTournaments = allTournaments.filter { t ->
                                    val matchesCategory = selectedCategoryFilter == "Todos" || t.category == selectedCategoryFilter
                                    val matchesClosed = if (hideClosed) t.status == "abierto" else true

                                    // --- NUEVA LÓGICA DE BÚSQUEDA ---
                                    val matchesSearch = t.title.contains(searchQuery, ignoreCase = true) ||
                                            t.game_name.contains(searchQuery, ignoreCase = true)

                                    matchesCategory && matchesClosed && matchesSearch // Añadimos matchesSearch
                                }.sortedWith(
                                    compareBy<Tournament> { statusPriority(it.status) }
                                        .thenBy { it.start_date }
                                )
                                TournamentList(
                                    list = filteredTournaments,
                                    emptyMessage = "No hay torneos disponibles",
                                    myEnrolled = myTournaments,
                                    currentUserId = profile?.id,
                                    isAdmin = profile?.is_admin == true,
                                    onItemClick = { selectedTournamentDetails = it },
                                    onEnrollClick = { handleEnroll(it) }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (!isLoading && canCreate) {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Crear Torneo")
            }
        }
    }

    if (showCreateDialog) {
        CreateTournamentDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { title, game, participants, category, modality, desc, date ->
                scope.launch {
                    val newTournament = Tournament(
                        creator_id = profile!!.id,
                        title = title,
                        game_name = game,
                        max_participants = participants,
                        start_date = date,
                        category = category,
                        modality = modality,
                        description = desc
                    )

                    val error = tournamentRepo.createTournament(newTournament)

                    if (error == null) {
                        showCreateDialog = false
                        refreshData()
                        Toast.makeText(context, "Torneo creado con éxito", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }

    if (selectedTournamentDetails != null) {
        TournamentDetailDialog(
            tournament = selectedTournamentDetails!!,
            isAdmin = profile?.is_admin == true,
            isEnrolled = myTournaments.any { it.id == selectedTournamentDetails!!.id },
            currentUserId = profile?.id,
            onDismiss = { selectedTournamentDetails = null },
            onDelete = {
                scope.launch {
                    val today = LocalDate.now()
                    val tournamentDate =
                        ZonedDateTime.parse(selectedTournamentDetails!!.start_date).toLocalDate()

                    val daysUntil = ChronoUnit.DAYS.between(today, tournamentDate)

                    val error = tournamentRepo.deleteTournament(
                        tournament = selectedTournamentDetails!!,
                        isAdmin = profile?.is_admin == true
                    )

                    if (error == null) {
                        val limit = selectedTournamentDetails!!.max_participants / 2

                        val wasPenalized = !(profile?.is_admin == true) &&
                                (daysUntil <= 1 || selectedTournamentDetails!!.current_participants > limit)

                        val msg = if (wasPenalized) {
                            "Torneo borrado. Se ha restado 1.0 de reputación por cancelación tardía/concurrida."
                        } else {
                            "Torneo borrado correctamente."
                        }

                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        selectedTournamentDetails = null
                        refreshData()
                    } else {
                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                    }
                }
            },
            onUnenroll = {
                scope.launch {
                    val error = tournamentRepo.unenrollFromTournament(selectedTournamentDetails!!)
                    if (error == null) {
                        refreshData()
                        selectedTournamentDetails = null
                    } else {
                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                    }
                }
            },
            onEnroll = { handleEnroll(it) },
            onRate = { stars, comment ->
                // 1. CAPTURAMOS los valores ANTES de entrar al scope
                val tournamentToRate = selectedTournamentDetails
                val userProfile = profile

                scope.launch {
                    // Log para confirmar que ahora NO son nulos
                    println("Debug: Ejecutando valoración. Profile: ${userProfile?.username}, Tournament: ${tournamentToRate?.title}")

                    if (userProfile != null && tournamentToRate != null) {
                        val review = Review(
                            tournament_id = tournamentToRate.id!!,
                            reviewer_id = userProfile.id,
                            rating = stars,
                            comment = comment
                        )

                        // Intentar guardar en Supabase
                        val error = tournamentRepo.addReview(review)

                        if (error == null) {
                            refreshData()
                            selectedTournamentDetails =
                                null // Cerramos el diálogo después del éxito
                            Toast.makeText(
                                context,
                                "¡Gracias por tu valoración!",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            // Si hay error (ej: ya valoraste), mostramos el error real de Supabase
                            Toast.makeText(context, "No se pudo guardar: $error", Toast.LENGTH_LONG)
                                .show()
                        }
                    } else {
                        // Este mensaje ahora solo saldría si realmente fallara la sesión
                        val motivo =
                            if (userProfile == null) "Sesión perdida" else "Error técnico con el torneo"
                        Toast.makeText(context, "Error: $motivo", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            getReviews = { id -> tournamentRepo.getTournamentReviews(id) },
            getUsername = { userId -> tournamentRepo.getUsernameById(userId) },
            getParticipants = { tournamentId -> tournamentRepo.getParticipantsIds(tournamentId) } // <--- CAMBIO AQUÍ
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TournamentList(
    list: List<Tournament>,
    emptyMessage: String,
    myEnrolled: List<Tournament>,
    currentUserId: String?,
    isAdmin: Boolean,
    onItemClick: (Tournament) -> Unit,
    onEnrollClick: (Tournament) -> Unit
) {
    if (list.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(emptyMessage)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(list, key = { it.id!! }) { tournament ->
                TournamentCard(
                    tournament = tournament,
                    currentUserId = currentUserId,
                    isEnrolled = myEnrolled.any { it.id == tournament.id },
                    isAdmin = isAdmin,
                    onCardClick = { onItemClick(tournament) },
                    onEnrollClick = { onEnrollClick(tournament) }
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TournamentCard(
    tournament: Tournament,
    currentUserId: String?,
    isAdmin: Boolean,
    isEnrolled: Boolean,
    onCardClick: () -> Unit,
    onEnrollClick: () -> Unit
) {
    val isOwner = currentUserId == tournament.creator_id
    val isFull = tournament.current_participants >= tournament.max_participants

    val today = LocalDate.now()
    val tournamentDate = ZonedDateTime.parse(tournament.start_date).toLocalDate()
    val isPast = today.isAfter(tournamentDate)
    val displayStatus = if (isPast) "finalizado" else tournament.status
    val isOpen = displayStatus == "abierto"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = tournament.title, style = MaterialTheme.typography.titleLarge)
                SuggestionChip(onClick = {}, label = { Text(tournament.category) })
            }
            Text("Juego: ${tournament.game_name}")
            Text(
                text = "Participantes: ${tournament.current_participants} / ${tournament.max_participants}",
                color = if (isFull) Color.Red else Color.Unspecified
            )
            Text(
                text = "📍 ${tournament.modality} | 📅 ${DateUtils.formatStandardDate(tournament.start_date)}",
                style = MaterialTheme.typography.bodySmall
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Por: ${tournament.profiles?.username ?: "Desconocido"}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = displayStatus.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = when(displayStatus) {
                        "abierto" -> Color(0xFF2E7D32)
                        "finalizado" -> Color.Gray
                        else -> Color.Red
                    }
                )
            }

            if (!isOwner) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onEnrollClick,
                    modifier = Modifier.align(Alignment.End),
                    // Mantenemos esta lógica: deshabilitado si ya pasó, si está lleno o si no está abierto
                    enabled = !isEnrolled && isOpen && !isFull
                ) {
                    Text(
                        when {
                            // Prioridad 1: Si ya pasó de fecha o la BDD dice finalizado
                            displayStatus == "finalizado" -> "Finalizado"
                            // Prioridad 2: Si el usuario ya está dentro
                            isEnrolled -> "Ya inscrito"
                            // Prioridad 3: Si no hay hueco y no estamos inscritos
                            isFull -> "Lleno"
                            // Prioridad 4: Si el estado es cerrado (pero no finalizado)
                            displayStatus == "cerrado" -> "Cerrado"
                            // Por defecto
                            else -> "Inscribirse"
                        }
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TournamentDetailDialog(
    tournament: Tournament,
    isAdmin: Boolean,
    isEnrolled: Boolean,
    currentUserId: String?,
    onDismiss: () -> Unit,
    onDelete: (String) -> Unit,
    onUnenroll: (String) -> Unit,
    onEnroll: (Tournament) -> Unit,
    onRate: (Int, String) -> Unit,
    getReviews: suspend (String) -> List<Review>,
    getUsername: suspend (String) -> String,
    getParticipants: suspend (String) -> List<String> // Recibe lista de IDs
) {
    val isOwner = currentUserId == tournament.creator_id
    val isFull = tournament.current_participants >= tournament.max_participants
    val today = LocalDate.now()
    val tournamentDate = ZonedDateTime.parse(tournament.start_date).toLocalDate()
    val isPast = today.isAfter(tournamentDate)
    val displayStatus = if (isPast) "finalizado" else tournament.status
    val isOpen = displayStatus == "abierto"

    var userRating by remember { mutableIntStateOf(0) }
    var userComment by remember { mutableStateOf("") }

    // Estados para Reseñas
    var showReviews by remember { mutableStateOf(false) }
    var reviewsWithNames by remember { mutableStateOf<List<Pair<Review, String>>>(emptyList()) }

    // Estados para Participantes
    var showParticipants by remember { mutableStateOf(false) }
    var participantsNames by remember { mutableStateOf<List<String>>(emptyList()) }

    val scope = rememberCoroutineScope()

    // --- DIÁLOGO DE RESEÑAS ---
    if (showReviews) {
        AlertDialog(
            onDismissRequest = { showReviews = false },
            title = { Text("Reseñas del Torneo") },
            text = {
                if (reviewsWithNames.isEmpty()) {
                    Text("Aún no hay reseñas.")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(reviewsWithNames) { (review, username) ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(text = username, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                                    Row {
                                        repeat(5) { index ->
                                            Icon(
                                                imageVector = if (index < review.rating) Icons.Default.Star else Icons.Default.StarBorder,
                                                contentDescription = null,
                                                tint = Color(0xFFFFD700),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    review.comment?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showReviews = false }) { Text("Volver") } }
        )
    }

    // --- NUEVO: DIÁLOGO DE PARTICIPANTES ---
    // --- DIÁLOGO DE LISTA DE PARTICIPANTES ---
    if (showParticipants) {
        AlertDialog(
            onDismissRequest = { showParticipants = false },
            title = { Text("Lista de Participantes") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (participantsNames.isEmpty()) {
                        Text("Cargando participantes...")
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.heightIn(max = 400.dp) // Para que no ocupe toda la pantalla
                        ) {
                            items(participantsNames) { name ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Text(
                                            text = name,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showParticipants = false }) { Text("Cerrar") }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(tournament.title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(tournament.description ?: "Sin descripción")
                HorizontalDivider()
                Text("Organizador: ${tournament.profiles?.username ?: "No disponible"}")
                Text("Fecha: ${DateUtils.formatStandardDate(tournament.start_date)}")
                Text("Participantes: ${tournament.current_participants} / ${tournament.max_participants}")

                // --- BOTÓN VER PARTICIPANTES ---
                // Dentro del Column de TournamentDetailDialog, debajo de la información del torneo:
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            // 1. Obtenemos los IDs de la tabla enrollments
                            val ids = getParticipants(tournament.id!!)
                            // 2. Convertimos esos IDs en nombres reales usando la función getUsername
                            participantsNames = ids.map { id -> getUsername(id) }
                            showParticipants = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Groups, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Ver Inscritos (${tournament.current_participants})")
                }

                Text("Modalidad: ${tournament.modality}")
                Text("Estado: ${displayStatus.uppercase()}")

                if (isOwner || isAdmin) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                val rawReviews = getReviews(tournament.id!!)
                                reviewsWithNames = rawReviews.map { it to getUsername(it.reviewer_id) }
                                showReviews = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Ver ${tournament.rating_avg} ⭐ (Reseñas)")
                    }
                }

                // Sección de valoración (mismo código que ya tenías)
                if (displayStatus == "finalizado" && isEnrolled && !isOwner) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text("¿Qué te pareció el torneo?", style = MaterialTheme.typography.titleSmall)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        (1..5).forEach { index ->
                            IconButton(onClick = { userRating = index }) {
                                Icon(
                                    imageVector = if (index <= userRating) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = null,
                                    tint = if (index <= userRating) Color(0xFFFFD700) else Color.Gray
                                )
                            }
                        }
                    }
                    if (userRating > 0) {
                        OutlinedTextField(
                            value = userComment,
                            onValueChange = { userComment = it },
                            label = { Text("Escribe un comentario (opcional)") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3,
                            textStyle = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { onRate(userRating, userComment); onDismiss() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Enviar Valoración ($userRating/5)")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!isOwner && !isEnrolled && isOpen && !isFull) {
                    Button(onClick = { onEnroll(tournament) }, modifier = Modifier.padding(end = 8.dp)) {
                        Text("Inscribirse")
                    }
                }
                TextButton(onClick = onDismiss) { Text("Cerrar", color = Color.Red) }
            }
        },
        dismissButton = {
            if (isEnrolled && !isPast) {
                TextButton(onClick = { onUnenroll(tournament.id!!) }) {
                    Text("Desinscribirse", color = Color.Red)
                }
            }
        }
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTournamentDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, game: String, participants: Int, category: String, modality: String, desc: String, date: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var game by remember { mutableStateOf("") }
    var participants by remember { mutableStateOf("16") }
    var desc by remember { mutableStateOf("") }
    var modality by remember { mutableStateOf("Online") }
    var selectedDate by remember { mutableStateOf("Seleccionar Fecha") }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val categories = listOf("Videojuegos", "Juegos de Mesa", "Deportes", "Otros")
    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(categories[0]) }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val formattedMonth = (month + 1).toString().padStart(2, '0')
            val formattedDay = dayOfMonth.toString().padStart(2, '0')
            selectedDate = "$year-$formattedMonth-$formattedDay"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply {
        datePicker.minDate = System.currentTimeMillis() + 86400000
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Crear Nuevo Torneo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = game, onValueChange = { game = it }, label = { Text("Juego") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = participants, onValueChange = { participants = it }, label = { Text("Máx Participantes") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = selectedDate, onValueChange = {}, readOnly = true, label = { Text("Fecha de Inicio") },
                    trailingIcon = { IconButton(onClick = { datePickerDialog.show() }) { Icon(Icons.Default.DateRange, null) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePickerDialog.show() }
                )
                // Selector de categoría (Exposed Dropdown Menu)
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = selectedCategory, onValueChange = {}, readOnly = true, label = { Text("Categoría") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        categories.forEach { category ->
                            DropdownMenuItem(text = { Text(category) }, onClick = {
                                selectedCategory = category
                                expanded = false
                            })
                        }
                    }
                }
                OutlinedTextField(value = modality, onValueChange = { modality = it }, label = { Text("Modalidad (Online/Presencial)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            }
        },

        confirmButton = {
            Button(onClick = {
                if (title.isNotBlank() && game.isNotBlank() && selectedDate != "Seleccionar Fecha") {
                    try {
                        // 1. Obtener la fecha actual y la seleccionada para comparar
                        val today = LocalDate.now()
                        val selectedLocalDate = LocalDate.parse(selectedDate)

                        // 2. Validar que la fecha sea posterior a hoy
                        if (!selectedLocalDate.isAfter(today)) {
                            Toast.makeText(
                                context,
                                "La fecha debe ser posterior a hoy",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            onConfirm(
                                title,
                                game,
                                participants.toIntOrNull() ?: 16,
                                selectedCategory,
                                modality,
                                desc,
                                selectedDate
                            )
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error en el formato de fecha", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Toast.makeText(
                        context,
                        "Rellena todos los campos obligatorios",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }) { Text("Crear") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

