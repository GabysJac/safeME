package com.example.safeme.view


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings

import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.safeme.model.AlertHistoryItem
import com.example.safeme.model.EmergencyContact
import com.example.safeme.ui.theme.kaushanTypography
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewMain() {
    MainScreen()
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    Scaffold(
        topBar = { MainTopBar(onSettingsClick = {  }) },
        bottomBar = { MainBottomBar(navController) }
    ) { innerPadding ->
        NavHost(navController, startDestination = "home", Modifier.padding(innerPadding)) {
            composable("home") {
                HomeScreen(
                    onPanicClicked = {  },

                    )
            }
            composable("map") { MapScreen() }
            composable("contacts") {
                val contacts = listOf<EmergencyContact>()
                ContactsScreen(
                    contacts = contacts,
                    onAddContact = { },
                    onEditContact = { }
                )
            }
            composable("history") {
                val alerts = listOf<AlertHistoryItem>()
                HistoryScreen(alerts = alerts)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopBar(onSettingsClick: () -> Unit) {
    LargeTopAppBar(
        title = {
            Text(
                "Safe ME",
                style = kaushanTypography.headlineMedium,
                fontSize = 48.sp,
                color = Color.White
            )
        },
        actions = {
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.largeTopAppBarColors(
            containerColor = Color.Black
        )
    )
}

@Composable
fun MainBottomBar(navController: NavController) {

    NavigationBar(
        containerColor = Color(0xFFFC5108),
        contentColor = Color.White

    ){

        val items = listOf("home", "map", "contacts", "history")
        val icons = listOf(
            Icons.Filled.Home,
            Icons.Filled.Map,
            Icons.Filled.Contacts,
            Icons.Filled.History
        )

        val labels = listOf(
            "Home",
            "Map",
            "Contacts",
            "History"
        )

        items.forEachIndexed { index, screen ->
            val isSelected = screen == navController.currentDestination?.route
            NavigationBarItem(
                icon = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = icons[index],
                            contentDescription = labels[index],
                            modifier = Modifier.size(35.dp),
                            tint = Color.White
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = labels[index],
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                },
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        navController.navigate(screen) {

                            navController.graph.startDestinationRoute?.let { route ->
                                popUpTo(route) { saveState = true }
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    contacts: List<EmergencyContact>,
    onAddContact: () -> Unit,
    onEditContact: (EmergencyContact) -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Contactos de Emergencia") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddContact) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Agregar contacto")
            }
        }
    ) { paddingValues ->
        LazyColumn(contentPadding = paddingValues) {
            items(contacts) { contact ->
                ContactItem(contact, onEditContact)
            }
        }
    }
}

@Composable
fun HistoryScreen(alerts: List<AlertHistoryItem>) {
    LazyColumn {
        items(alerts) { alert ->
            HistoryItem(alert)
        }
    }
}

@Composable
fun HistoryItem(alert: AlertHistoryItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Alerta: ${alert.type}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Fecha: ${formatTimestamp(alert.timestamp)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Ubicación: ${alert.location}",
                    style = MaterialTheme.typography.bodySmall
                )
                if (alert.description.isNotEmpty()) {
                    Text(
                        text = "Descripción: ${alert.description}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

fun formatTimestamp(timestamp: Long): String {
    val instant = Instant.ofEpochMilli(timestamp)
    val zonedDateTime = instant.atZone(ZoneId.systemDefault())
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    return zonedDateTime.format(formatter)
}

@Composable
fun ContactItem(
    contact: EmergencyContact,
    onEditContact: (EmergencyContact) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = contact.phone,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            IconButton(onClick = { onEditContact(contact) }) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar contacto")
            }
        }
    }
}
