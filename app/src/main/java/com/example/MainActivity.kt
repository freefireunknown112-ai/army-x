package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.ArmoryScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.FriendsScreen
import com.example.ui.screens.GameScreen
import com.example.ui.screens.MainMenuScreen
import com.example.ui.screens.MatchmakingScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ResultScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ShopScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.GunmetalDark
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.ScreenState

class MainActivity : ComponentActivity() {

    private val gameViewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = GunmetalDark
                ) {
                    ArmyXApp(viewModel = gameViewModel)
                }
            }
        }
    }
}

@Composable
fun ArmyXApp(viewModel: GameViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()

    when (currentScreen) {
        ScreenState.SPLASH -> {
            SplashScreen(
                onSplashFinished = {
                    viewModel.navigateTo(ScreenState.MAIN_MENU)
                }
            )
        }

        ScreenState.AUTH -> {
            BackHandler {
                viewModel.navigateTo(ScreenState.MAIN_MENU)
            }
            AuthScreen(viewModel = viewModel)
        }

        ScreenState.MAIN_MENU -> {
            MainMenuScreen(viewModel = viewModel)
        }

        ScreenState.MATCHMAKING -> {
            BackHandler {
                viewModel.navigateTo(ScreenState.MAIN_MENU)
            }
            MatchmakingScreen(viewModel = viewModel)
        }

        ScreenState.IN_GAME -> {
            GameScreen(viewModel = viewModel)
        }

        ScreenState.RESULT -> {
            BackHandler {
                viewModel.navigateTo(ScreenState.MAIN_MENU)
            }
            ResultScreen(viewModel = viewModel)
        }

        ScreenState.PROFILE -> {
            BackHandler {
                viewModel.navigateTo(ScreenState.MAIN_MENU)
            }
            ProfileScreen(viewModel = viewModel)
        }

        ScreenState.ARMORY -> {
            BackHandler {
                viewModel.navigateTo(ScreenState.MAIN_MENU)
            }
            ArmoryScreen(viewModel = viewModel)
        }

        ScreenState.SHOP -> {
            BackHandler {
                viewModel.navigateTo(ScreenState.MAIN_MENU)
            }
            ShopScreen(viewModel = viewModel)
        }

        ScreenState.FRIENDS -> {
            BackHandler {
                viewModel.navigateTo(ScreenState.MAIN_MENU)
            }
            FriendsScreen(viewModel = viewModel)
        }

        ScreenState.SETTINGS -> {
            BackHandler {
                viewModel.navigateTo(ScreenState.MAIN_MENU)
            }
            SettingsScreen(viewModel = viewModel)
        }
    }
}
