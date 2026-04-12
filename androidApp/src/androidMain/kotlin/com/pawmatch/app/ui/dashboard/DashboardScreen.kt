package com.pawmatch.app.ui.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Email
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.pawmatch.app.ui.discovery.DiscoveryScreen
import com.pawmatch.app.ui.matches.MatchesScreen
import com.pawmatch.app.ui.profile.ProfileScreen
import com.pawmatch.app.ui.theme.PrimaryPink

@Composable
fun DashboardScreen() {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            BottomNavigation(
                backgroundColor = Color.White,
                contentColor = PrimaryPink
            ) {
                BottomNavigationItem(
                    icon = { Icon(Icons.Default.Favorite, contentDescription = "Descubrir") },
                    label = { Text("Descubrir") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    selectedContentColor = PrimaryPink,
                    unselectedContentColor = Color.Gray
                )
                BottomNavigationItem(
                    icon = { Icon(Icons.Default.Email, contentDescription = "Matches") },
                    label = { Text("Matches") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    selectedContentColor = PrimaryPink,
                    unselectedContentColor = Color.Gray
                )
                BottomNavigationItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Mi Perfil") },
                    label = { Text("Mi Perfil") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    selectedContentColor = PrimaryPink,
                    unselectedContentColor = Color.Gray
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> DiscoveryScreen()
                1 -> MatchesScreen()
                2 -> ProfileScreen()
            }
        }
    }
}
