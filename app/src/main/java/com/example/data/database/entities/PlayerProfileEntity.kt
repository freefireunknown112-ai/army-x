package com.example.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_profile")
data class PlayerProfileEntity(
    @PrimaryKey val playerId: String,
    val username: String,
    val email: String,
    val avatarUrl: String,
    val level: Int,
    val currentXp: Int,
    val xpForNextLevel: Int,
    val coins: Int,
    val diamonds: Int,
    val totalMatches: Int,
    val totalWins: Int,
    val totalKills: Int,
    val totalLosses: Int,
    val selectedWeaponId: String,
    val selectedCharacterSkin: String,
    val isDevelopmentMode: Boolean
)

@Entity(tableName = "match_history")
data class MatchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val mode: String,
    val placement: Int,
    val totalPlayers: Int,
    val kills: Int,
    val damageDealt: Int,
    val xpEarned: Int,
    val coinsEarned: Int,
    val isVictory: Boolean
)

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val cameraSensitivity: Float = 1.0f,
    val aimSensitivity: Float = 0.7f,
    val buttonScale: Float = 1.0f,
    val soundVolume: Float = 0.8f,
    val musicVolume: Float = 0.6f,
    val graphicsQuality: String = "MEDIUM", // LOW, MEDIUM, HIGH
    val shadowsEnabled: Boolean = true,
    val targetFps: Int = 60
)
