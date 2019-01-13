package mbitsystem.com.spacexmvimyapp.rockets

import mbitsystem.com.spacexmvimyapp.mvibase.MainIntent

sealed class RocketsIntent : MainIntent {
    object InitialIntent : RocketsIntent()
    data class LoadRocketsIntent(val isClickedLoadRocketsButton: Boolean) : RocketsIntent()
}