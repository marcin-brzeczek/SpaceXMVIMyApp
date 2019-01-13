package mbitsystem.com.spacexmvimyapp.rockets

import mbitsystem.com.spacexmvimyapp.mvibase.MainAction

sealed class RocketsAction : MainAction {
    data class LoadRocketsAction(val isClickedLoadRocketsButton: Boolean) : RocketsAction()
}