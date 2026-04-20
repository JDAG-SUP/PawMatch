/*
 * Copyright 2026 Vincent Tsen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pawmatch.app.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

sealed class BottomNavItem(var title: String, var icon: androidx.compose.ui.graphics.vector.ImageVector, var screen_route: String) {
    object Swipe : BottomNavItem("Swipe", Icons.Filled.Search, "swipe")
    object Matches : BottomNavItem("Matches", Icons.Filled.List, "matches")
    object Profile : BottomNavItem("Perfil", Icons.Filled.Person, "profile")
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                val navBackStackEntry = navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry.value?.destination?.route
                val items = listOf(
                    BottomNavItem.Swipe,
                    BottomNavItem.Matches,
                    BottomNavItem.Profile
                )
                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(text = item.title) },
                        alwaysShowLabel = true,
                        selected = currentRoute == item.screen_route,
                        onClick = {
                            navController.navigate(item.screen_route) {
                                navController.graph.startDestinationRoute?.let { screen_route ->
                                    popUpTo(screen_route) {
                                        saveState = true
                                    }
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Swipe.screen_route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Swipe.screen_route) {
                SwipeScreen()
            }
            composable(BottomNavItem.Matches.screen_route) {
                MatchesScreen(
                    onNavigateToPublicProfile = { userId ->
                        navController.navigate("public_profile/$userId")
                    }
                )
            }
            composable(BottomNavItem.Profile.screen_route) {
                ProfileScreen(modifier = Modifier.padding(innerPadding))
            }
            composable("public_profile/{userId}") { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId")
                if (userId != null) {
                    PublicProfileScreen(userId = userId)
                }
            }
        }
    }
}
