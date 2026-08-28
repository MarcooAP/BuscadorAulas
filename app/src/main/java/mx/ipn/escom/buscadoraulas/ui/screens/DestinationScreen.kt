package mx.ipn.escom.buscadoraulas.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import mx.ipn.escom.buscadoraulas.data.model.EscomLocation
import mx.ipn.escom.buscadoraulas.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DestinationScreen(
    viewModel: MainViewModel,
    onDestinationSelected: (destinationId: String) -> Unit,
    onBack: () -> Unit
) {
    val currentLocation by viewModel.currentLocation.collectAsState()
    val pois  = remember { viewModel.getAllLocations() }
    val rooms = remember { viewModel.getAllRooms() }
    var searchQuery by remember { mutableStateOf("") }
    val originId = currentLocation?.id

    val filteredPois = remember(searchQuery, originId) {
        val q = searchQuery.trim().uppercase()
        pois.filter { it.id != originId }
            .filter { q.isEmpty() || it.displayName.uppercase().contains(q) ||
                    it.keywords.any { k -> k.uppercase().contains(q) } }
    }

    val filteredRooms = remember(searchQuery, originId) {
        val q = searchQuery.trim().uppercase()
        rooms.filter { it.id != originId }
            .filter { q.isEmpty() || it.id.contains(q) ||
                    it.displayName.uppercase().contains(q) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("¿A dónde quieres ir?") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            currentLocation?.let { loc ->
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.MyLocation,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                "Estás en:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                loc.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                placeholder = { Text("Ej. 2110, Biblioteca…") },
                label = { Text("Buscar salón o lugar") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                        }
                    }
                },
                singleLine = true
            )

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (filteredPois.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title  = "Puntos de interés",
                            count  = filteredPois.size,
                            icon   = Icons.Default.Place
                        )
                    }
                    items(filteredPois, key = { it.id }) { dest ->
                        DestinationCard(
                            location  = dest,
                            isPoi     = true,
                            onClick   = {
                                viewModel.setDestination(dest)
                                onDestinationSelected(dest.id)
                            }
                        )
                    }
                }

                if (filteredRooms.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        SectionHeader(
                            title = "Salones",
                            count = filteredRooms.size,
                            icon  = Icons.Default.School
                        )
                    }

                    if (searchQuery.isBlank()) {
                        val grouped = filteredRooms.groupBy { "${it.building} - ${it.description.split(",").last().trim()}" }
                        for ((groupName, roomsInGroup) in grouped) {
                            item(key = "header_$groupName") {
                                Text(
                                    text = groupName,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                                )
                            }
                            items(roomsInGroup, key = { it.id }) { dest ->
                                DestinationCard(
                                    location = dest,
                                    isPoi    = false,
                                    onClick  = {
                                        viewModel.setDestination(dest)
                                        onDestinationSelected(dest.id)
                                    }
                                )
                            }
                        }
                    } else {
                        items(filteredRooms, key = { it.id }) { dest ->
                            DestinationCard(
                                location = dest,
                                isPoi    = false,
                                onClick  = {
                                    viewModel.setDestination(dest)
                                    onDestinationSelected(dest.id)
                                }
                            )
                        }
                    }
                }

                if (filteredPois.isEmpty() && filteredRooms.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Sin resultados para \"$searchQuery\"",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
    ) {
        Icon(icon, contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = "$title ($count)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DestinationCard(
    location: EscomLocation,
    isPoi: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = if (isPoi) Icons.Default.Place else Icons.Default.MeetingRoom,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = location.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = location.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Seleccionar",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
