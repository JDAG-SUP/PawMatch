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
package com.pawmatch.app.models

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val city: String = "", // Usará una lista predefinida en la UI para evitar problemas
    val whatsappNumber: String = "",
    val bio: String = "",
    val hobbies: String = "",
    val preferenceAnimalType: String = "" // e.g. "Perro", "Gato", "Ave"
)

data class Pet(
    val id: String = "",
    val ownerId: String = "",
    val name: String = "",
    val animalType: String = "",
    val breed: String = "",
    val age: String = "",
    val city: String = "", // Se heredará del dueño y servirá de filtro local
    val shortDescription: String = "",
    val imageUrls: List<String> = emptyList()
)

data class Match(
    val id: String = "",
    val userAId: String = "",
    val userBId: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
