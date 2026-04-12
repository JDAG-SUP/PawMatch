package com.pawmatch.app.shared.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SwipeDto(
    @SerialName("id") val id: String? = null,
    @SerialName("swiper_pet_id") val swiperPetId: String,
    @SerialName("target_pet_id") val targetPetId: String,
    @SerialName("liked") val liked: Boolean,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class MatchDto(
    @SerialName("id") val id: String,
    @SerialName("pet1_id") val pet1Id: String,
    @SerialName("pet2_id") val pet2Id: String,
    @SerialName("created_at") val createdAt: String
)
