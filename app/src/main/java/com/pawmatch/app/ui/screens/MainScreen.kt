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
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

sealed class BottomNavItem(var title: String, var icon: androidx.compose.ui.graphics.vector.ImageVector, var screen_route: String) {
    object Explorar : BottomNavItem("Explorar", Icons.Outlined.ContentCopy, "swipe")
    object Matches : BottomNavItem("Matches", Icons.Default.FavoriteBorder, "matches")
    object Eventos : BottomNavItem("Eventos", Icons.Default.DateRange, "events")
    object Chats : BottomNavItem("Chats", Icons.Default.ChatBubbleOutline, "chats")
    object Perfil : BottomNavItem("Perfil", Icons.Outlined.PersonOutline, "profile")
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground
            ) {
                val navBackStackEntry = navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry.value?.destination?.route
                val items = listOf(
                    BottomNavItem.Explorar,
                    BottomNavItem.Matches,
                    BottomNavItem.Eventos,
                    BottomNavItem.Chats,
                    BottomNavItem.Perfil
                )
                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(text = item.title, style = MaterialTheme.typography.labelSmall) },
                        alwaysShowLabel = true,
                        selected = currentRoute == item.screen_route,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.background,
                            unselectedIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            unselectedTextColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        ),
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
            startDestination = BottomNavItem.Explorar.screen_route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Explorar.screen_route) {
                SwipeScreen(
                    onNavigateToFilters = {
                        navController.navigate("matching_prefs")
                    }
                    // Ruta del detalle de chat. Recibe el chatId por argumento de navegación.
                    // El popBackStack devuelve al usuario a la lista de conversaciones.
                    composable("chat_detail/{chatId}") { backStackEntry ->
                        val chatId = backStackEntry.arguments?.getString("chatId")
                        if (chatId != null) {
                            ChatDetailScreen(
                        chatId = chatId,
                        onNavigateBack = { navController.popBackStack() },
                    )
                }
            }
                    
                )
            }
            composable(BottomNavItem.Matches.screen_route) {
                MatchesScreen(
                    onNavigateToPublicProfile = { userId ->
                        navController.navigate("public_profile/$userId")
                    }
                )
            }
            composable(BottomNavItem.Eventos.screen_route) {
                EventsScreen()
            }
            composable(BottomNavItem.Chats.screen_route) {
                ChatsScreen()
            }
            composable(BottomNavItem.Perfil.screen_route) {
                SettingsScreen(
                    onNavigateToEditProfile = {
                        navController.navigate("edit_profile")
                    },
                    onNavigateToMatchingPrefs = {
                        navController.navigate("matching_prefs")
                    },
                    onNavigateToPrivacy = {
                        navController.navigate("privacy_security")
                    }
                )
            }
            composable("edit_profile") {
                ProfileScreen()
            }
            composable("matching_prefs") {
                MatchingPreferencesScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("privacy_security") {
                PrivacySecurityScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
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
