package mbitsystem.com.spacexmvimyapp.rockets

import mbitsystem.com.spacexmvimyapp.data.model.Rocket
import mbitsystem.com.spacexmvimyapp.mvibase.MainResult

sealed class RocketsResult : MainResult {
    sealed class LoadRocketsResult : RocketsResult() {
        data class Failure(val error: Throwable) : LoadRocketsResult()
        data class Success(val rocketList: List<Rocket>) : LoadRocketsResult()
        object ProgressState : LoadRocketsResult()
    }
}