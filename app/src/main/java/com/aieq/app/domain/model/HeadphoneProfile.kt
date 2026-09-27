package com.aieq.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class HeadphoneProfile(
    val id: String,
    val manufacturer: String,
    val model: String,
    val displayName: String = "$manufacturer $model",
    val formFactor: String = "Over-Ear", // Over-Ear, In-Ear, Earbuds
    val measurementSource: String = "oratory1990", // oratory1990, crinacle, rtings
    val aliases: List<String> = emptyList(),
    val hasAutoEq: Boolean = true
) {
    fun matchesQuery(query: String): Boolean {
        val q = query.trim().lowercase()
        if (q.isBlank()) return true
        if (displayName.lowercase().contains(q)) return true
        if (manufacturer.lowercase().contains(q)) return true
        if (model.lowercase().contains(q)) return true
        return aliases.any { it.lowercase().contains(q) }
    }
}
