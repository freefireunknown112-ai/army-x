package com.example.network

import com.example.game.engine.Vector3
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class NetworkStatus {
    CONNECTED,
    CONNECTING,
    DISCONNECTED,
    OFFLINE_DEV_MODE
}

data class PlayerNetworkState(
    val playerId: String,
    val position: Vector3,
    val yaw: Float,
    val pitch: Float,
    val isFiring: Boolean,
    val health: Float,
    val sequenceNumber: Long
)

class MultiplayerService {
    private val _networkStatus = MutableStateFlow(NetworkStatus.OFFLINE_DEV_MODE)
    val networkStatus: StateFlow<NetworkStatus> = _networkStatus.asStateFlow()

    private val _pingMs = MutableStateFlow(24)
    val pingMs: StateFlow<Int> = _pingMs.asStateFlow()

    var isDevelopmentMode: Boolean = true
        private set

    private var lastValidatedTimestamp: Long = 0L
    private var lastShotTimestamp: Long = 0L

    fun setMode(devMode: Boolean) {
        isDevelopmentMode = devMode
        _networkStatus.value = if (devMode) NetworkStatus.OFFLINE_DEV_MODE else NetworkStatus.CONNECTED
    }

    // Server-Authoritative Anti-Cheat Validation
    fun validatePlayerMove(lastPos: Vector3, newPos: Vector3, deltaTimeSec: Float): Boolean {
        val maxSpeedAllowed = 12.0f // Max speed with sprint + vehicle tolerance
        val dist = lastPos.distanceTo(newPos)
        val calculatedSpeed = if (deltaTimeSec > 0f) dist / deltaTimeSec else 0f
        return calculatedSpeed <= maxSpeedAllowed
    }

    fun validateFireRate(minFireIntervalMs: Long): Boolean {
        val now = System.currentTimeMillis()
        if (now - lastShotTimestamp < minFireIntervalMs * 0.85f) { // 15% latency tolerance
            return false // Rate limit violation
        }
        lastShotTimestamp = now
        return true
    }

    fun validateDamageAuthority(shooterId: String, targetId: String, claimedDamage: Float): Float {
        // Authoritative clamp: never let client claim more than 150 damage per single shot
        return claimedDamage.coerceIn(0f, 150f)
    }

    fun simulateReconnect() {
        _networkStatus.value = NetworkStatus.CONNECTING
        // Quick recovery back to active state
        _networkStatus.value = if (isDevelopmentMode) NetworkStatus.OFFLINE_DEV_MODE else NetworkStatus.CONNECTED
    }
}
