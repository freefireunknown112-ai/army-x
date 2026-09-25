package com.example.data.models

enum class GameMode(
    val title: String,
    val description: String,
    val maxPlayers: Int,
    val teamSize: Int,
    val isAvailable: Boolean = true
) {
    SOLO_BR("Solo Battle Royale", "Every operative for themselves. 20 operatives drop, 1 survivor wins.", 20, 1),
    DUO_BR("Duo Squad", "Pair up with an ally. Revive teammates and coordinate strikes.", 20, 2),
    SQUAD_BR("4-Man Strike Team", "Full squad tactical warfare with team voice/ping.", 20, 4),
    TRAINING("Combat Training Range", "Practice weapon recoil, bot AI difficulty, and target shooting.", 1, 1)
}

data class UserProfile(
    val playerId: String,
    val username: String,
    val email: String,
    val avatarUrl: String = "avatar_tactical_01",
    val level: Int = 1,
    val currentXp: Int = 350,
    val xpForNextLevel: Int = 1000,
    val coins: Int = 2500,
    val diamonds: Int = 120,
    val totalMatches: Int = 14,
    val totalWins: Int = 3,
    val totalKills: Int = 28,
    val totalLosses: Int = 11,
    val selectedWeaponId: String = "wpn_ar_01",
    val selectedCharacterSkin: String = "skin_stealth_specops",
    val isDevelopmentMode: Boolean = true
) {
    val winRate: Float
        get() = if (totalMatches > 0) (totalWins.toFloat() / totalMatches) * 100f else 0f

    val kdRatio: Float
        get() = if (totalLosses > 0) totalKills.toFloat() / totalLosses else totalKills.toFloat()
}

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val rewardCoins: Int,
    val rewardXp: Int,
    val currentProgress: Int,
    val targetProgress: Int,
    val isClaimed: Boolean = false
) {
    val isCompleted: Boolean get() = currentProgress >= targetProgress
}

enum class ShopCategory {
    SOLDIER_SKINS,
    WEAPON_CAMOS,
    AVATARS,
    BANNERS
}

data class ShopItem(
    val id: String,
    val name: String,
    val category: ShopCategory,
    val priceCoins: Int = 0,
    val priceDiamonds: Int = 0,
    val isOwned: Boolean = false,
    val rarity: String = "Rare",
    val description: String = ""
)

data class Vehicle(
    val id: String,
    val name: String,
    var x: Float,
    var y: Float,
    var z: Float,
    var rotationY: Float = 0f,
    var speed: Float = 0f,
    val maxSpeed: Float = 25f,
    var health: Float = 500f,
    val maxHealth: Float = 500f,
    var isOccupied: Boolean = false,
    var driverId: String? = null
)
