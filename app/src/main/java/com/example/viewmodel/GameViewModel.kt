package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundManager
import com.example.data.database.ArmyXDatabase
import com.example.data.database.entities.UserSettingsEntity
import com.example.data.models.GameMode
import com.example.data.models.UserProfile
import com.example.data.repository.GameRepository
import com.example.game.GameSession
import com.example.game.GameState
import com.example.network.MultiplayerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ScreenState {
    SPLASH,
    AUTH,
    MAIN_MENU,
    PROFILE,
    ARMORY,
    SHOP,
    FRIENDS,
    SETTINGS,
    MATCHMAKING,
    IN_GAME,
    RESULT
}

class GameViewModel(application: Application) : AndroidViewModel(application) {

    val database = ArmyXDatabase.getInstance(application)
    val repository = GameRepository(database)
    val soundManager = SoundManager(application)
    val multiplayerService = MultiplayerService()

    private val _currentScreen = MutableStateFlow(ScreenState.SPLASH)
    val currentScreen: StateFlow<ScreenState> = _currentScreen.asStateFlow()

    private val _selectedMode = MutableStateFlow(GameMode.SOLO_BR)
    val selectedMode: StateFlow<GameMode> = _selectedMode.asStateFlow()

    private val _activeSession = MutableStateFlow<GameSession?>(null)
    val activeSession: StateFlow<GameSession?> = _activeSession.asStateFlow()

    val profile: StateFlow<UserProfile?> = repository.profileFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val settings: StateFlow<UserSettingsEntity> = repository.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserSettingsEntity()
    )

    val achievements = repository.achievements
    val shopCatalog = repository.shopCatalog
    val matchHistory = repository.matchHistoryFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Result Screen temporary data
    private val _lastMatchResult = MutableStateFlow<Triple<Boolean, Int, Int>?>(null) // (isVictory, kills, placement)
    val lastMatchResult = _lastMatchResult.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getOrCreateDefaultProfile()
        }
    }

    fun navigateTo(screen: ScreenState) {
        soundManager.playSound("click")
        _currentScreen.value = screen
    }

    fun selectGameMode(mode: GameMode) {
        _selectedMode.value = mode
        soundManager.playSound("click")
    }

    fun startMatchmaking() {
        soundManager.playSound("click")
        _currentScreen.value = ScreenState.MATCHMAKING
    }

    fun launchGameSession() {
        val session = GameSession(soundManager, _selectedMode.value)
        _activeSession.value = session
        _currentScreen.value = ScreenState.IN_GAME
    }

    fun leaveMatch() {
        _activeSession.value = null
        _currentScreen.value = ScreenState.MAIN_MENU
        soundManager.playSound("click")
    }

    fun onMatchFinished(isVictory: Boolean, kills: Int, damage: Int, placement: Int) {
        _lastMatchResult.value = Triple(isVictory, kills, placement)
        viewModelScope.launch {
            repository.recordMatchResult(
                isVictory = isVictory,
                kills = kills,
                damage = damage,
                placement = placement,
                totalPlayers = 20,
                mode = _selectedMode.value.title
            )
        }
        _currentScreen.value = ScreenState.RESULT
    }

    fun restartMatch() {
        launchGameSession()
    }

    fun updateSettings(newSettings: UserSettingsEntity) {
        viewModelScope.launch {
            repository.saveSettings(newSettings)
            soundManager.soundVolume = newSettings.soundVolume
            soundManager.musicVolume = newSettings.musicVolume
        }
    }

    fun purchaseItem(itemId: String): Boolean {
        soundManager.playSound("click")
        return repository.purchaseShopItem(itemId)
    }

    fun claimAchievement(id: String) {
        soundManager.playSound("click")
        repository.claimAchievement(id)
    }

    fun loginGuest() {
        viewModelScope.launch {
            repository.getOrCreateDefaultProfile()
            _currentScreen.value = ScreenState.MAIN_MENU
        }
    }

    fun loginWithEmail(email: String, user: String) {
        viewModelScope.launch {
            val prof = repository.getOrCreateDefaultProfile().copy(
                email = email,
                username = user,
                isDevelopmentMode = true
            )
            repository.saveProfile(prof)
            _currentScreen.value = ScreenState.MAIN_MENU
        }
    }
}
