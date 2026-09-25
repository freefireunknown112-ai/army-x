package com.example.data.repository

import com.example.data.database.ArmyXDatabase
import com.example.data.database.entities.MatchHistoryEntity
import com.example.data.database.entities.PlayerProfileEntity
import com.example.data.database.entities.UserSettingsEntity
import com.example.data.models.Achievement
import com.example.data.models.ShopCategory
import com.example.data.models.ShopItem
import com.example.data.models.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID

class GameRepository(private val database: ArmyXDatabase) {

    private val _achievements = MutableStateFlow(
        listOf(
            Achievement("ach_first_blood", "First Blood", "Eliminate your first opponent in Battle Royale", 500, 200, 1, 1, true),
            Achievement("ach_sharpshooter", "Sharpshooter", "Achieve 10 eliminations across matches", 1000, 450, 10, 10, true),
            Achievement("ach_champion", "Battle Royale Champion", "Secure a #1 Victory Royale", 2500, 1000, 1, 1, false),
            Achievement("ach_veteran", "Veteran Operative", "Play 25 complete matches", 1500, 600, 14, 25, false),
            Achievement("ach_collector", "Armory Collector", "Collect 4 distinct tactical weapons in match", 800, 300, 3, 4, false)
        )
    )
    val achievements: StateFlow<List<Achievement>> = _achievements.asStateFlow()

    private val _shopCatalog = MutableStateFlow(
        listOf(
            ShopItem("skin_specops", "Night Ops Infiltrator", ShopCategory.SOLDIER_SKINS, priceCoins = 2000, rarity = "Epic", isOwned = true, description = "Tactical matte black combat suit with thermal goggles."),
            ShopItem("skin_desert", "Desert Marauder", ShopCategory.SOLDIER_SKINS, priceCoins = 3500, rarity = "Legendary", isOwned = false, description = "Camo tactical armor engineered for arid skirmishes."),
            ShopItem("skin_arctic", "Frostbite Ghost", ShopCategory.SOLDIER_SKINS, priceDiamonds = 80, rarity = "Mythic", isOwned = false, description = "Ultra-rare winter spec-ops exosuit with neon cyan trims."),
            ShopItem("camo_cyber_ar", "Neon Apex AR-X", ShopCategory.WEAPON_CAMOS, priceCoins = 1800, rarity = "Epic", isOwned = false, description = "Animated pulsing cyan camo for AR-X Phantom."),
            ShopItem("camo_gold_sniper", "Dragonfire .50 Cal", ShopCategory.WEAPON_CAMOS, priceDiamonds = 120, rarity = "Mythic", isOwned = false, description = "Gilded dragon relief custom barrel for Valkyrie .50."),
            ShopItem("avatar_skull", "Cyber Skull Crest", ShopCategory.AVATARS, priceCoins = 800, rarity = "Rare", isOwned = true, description = "Esports crest avatar with glowing eyes.")
        )
    )
    val shopCatalog: StateFlow<List<ShopItem>> = _shopCatalog.asStateFlow()

    val profileFlow: Flow<UserProfile?> = database.playerProfileDao().getProfileFlow().map { entity ->
        entity?.let {
            UserProfile(
                playerId = it.playerId,
                username = it.username,
                email = it.email,
                avatarUrl = it.avatarUrl,
                level = it.level,
                currentXp = it.currentXp,
                xpForNextLevel = it.xpForNextLevel,
                coins = it.coins,
                diamonds = it.diamonds,
                totalMatches = it.totalMatches,
                totalWins = it.totalWins,
                totalKills = it.totalKills,
                totalLosses = it.totalLosses,
                selectedWeaponId = it.selectedWeaponId,
                selectedCharacterSkin = it.selectedCharacterSkin,
                isDevelopmentMode = it.isDevelopmentMode
            )
        }
    }

    val settingsFlow: Flow<UserSettingsEntity> = database.userSettingsDao().getSettingsFlow().map {
        it ?: UserSettingsEntity()
    }

    val matchHistoryFlow: Flow<List<MatchHistoryEntity>> = database.matchHistoryDao().getAllMatchesFlow()

    suspend fun getOrCreateDefaultProfile(): UserProfile {
        val existing = database.playerProfileDao().getProfile()
        if (existing != null) {
            return UserProfile(
                playerId = existing.playerId,
                username = existing.username,
                email = existing.email,
                avatarUrl = existing.avatarUrl,
                level = existing.level,
                currentXp = existing.currentXp,
                xpForNextLevel = existing.xpForNextLevel,
                coins = existing.coins,
                diamonds = existing.diamonds,
                totalMatches = existing.totalMatches,
                totalWins = existing.totalWins,
                totalKills = existing.totalKills,
                totalLosses = existing.totalLosses,
                selectedWeaponId = existing.selectedWeaponId,
                selectedCharacterSkin = existing.selectedCharacterSkin,
                isDevelopmentMode = existing.isDevelopmentMode
            )
        }
        val guestId = "AX-" + UUID.randomUUID().toString().take(6).uppercase()
        val defaultProfile = UserProfile(
            playerId = guestId,
            username = "Operative_$guestId",
            email = "guest@armyx.local",
            level = 1,
            currentXp = 350,
            xpForNextLevel = 1000,
            coins = 3000,
            diamonds = 150,
            totalMatches = 0,
            totalWins = 0,
            totalKills = 0,
            totalLosses = 0,
            isDevelopmentMode = true
        )
        saveProfile(defaultProfile)
        return defaultProfile
    }

    suspend fun saveProfile(profile: UserProfile) {
        val entity = PlayerProfileEntity(
            playerId = profile.playerId,
            username = profile.username,
            email = profile.email,
            avatarUrl = profile.avatarUrl,
            level = profile.level,
            currentXp = profile.currentXp,
            xpForNextLevel = profile.xpForNextLevel,
            coins = profile.coins,
            diamonds = profile.diamonds,
            totalMatches = profile.totalMatches,
            totalWins = profile.totalWins,
            totalKills = profile.totalKills,
            totalLosses = profile.totalLosses,
            selectedWeaponId = profile.selectedWeaponId,
            selectedCharacterSkin = profile.selectedCharacterSkin,
            isDevelopmentMode = profile.isDevelopmentMode
        )
        database.playerProfileDao().insertOrUpdateProfile(entity)
    }

    suspend fun recordMatchResult(
        isVictory: Boolean,
        kills: Int,
        damage: Int,
        placement: Int,
        totalPlayers: Int,
        mode: String = "Solo Battle Royale"
    ) {
        val currentProfile = getOrCreateDefaultProfile()
        val earnedXp = if (isVictory) 500 + kills * 50 else 150 + kills * 40
        val earnedCoins = if (isVictory) 800 + kills * 70 else 250 + kills * 50

        var newXp = currentProfile.currentXp + earnedXp
        var newLevel = currentProfile.level
        var nextLevelXp = currentProfile.xpForNextLevel

        while (newXp >= nextLevelXp) {
            newXp -= nextLevelXp
            newLevel++
            nextLevelXp = (nextLevelXp * 1.25f).toInt()
        }

        val updatedProfile = currentProfile.copy(
            totalMatches = currentProfile.totalMatches + 1,
            totalWins = if (isVictory) currentProfile.totalWins + 1 else currentProfile.totalWins,
            totalLosses = if (!isVictory) currentProfile.totalLosses + 1 else currentProfile.totalLosses,
            totalKills = currentProfile.totalKills + kills,
            coins = currentProfile.coins + earnedCoins,
            currentXp = newXp,
            level = newLevel,
            xpForNextLevel = nextLevelXp
        )
        saveProfile(updatedProfile)

        database.matchHistoryDao().insertMatch(
            MatchHistoryEntity(
                timestamp = System.currentTimeMillis(),
                mode = mode,
                placement = placement,
                totalPlayers = totalPlayers,
                kills = kills,
                damageDealt = damage,
                xpEarned = earnedXp,
                coinsEarned = earnedCoins,
                isVictory = isVictory
            )
        )
    }

    suspend fun saveSettings(settings: UserSettingsEntity) {
        database.userSettingsDao().saveSettings(settings)
    }

    fun purchaseShopItem(itemId: String): Boolean {
        val list = _shopCatalog.value.toMutableList()
        val index = list.indexOfFirst { it.id == itemId }
        if (index != -1 && !list[index].isOwned) {
            list[index] = list[index].copy(isOwned = true)
            _shopCatalog.value = list
            return true
        }
        return false
    }

    fun claimAchievement(id: String) {
        val list = _achievements.value.toMutableList()
        val index = list.indexOfFirst { it.id == id }
        if (index != -1 && !list[index].isClaimed && list[index].isCompleted) {
            list[index] = list[index].copy(isClaimed = true)
            _achievements.value = list
        }
    }
}
