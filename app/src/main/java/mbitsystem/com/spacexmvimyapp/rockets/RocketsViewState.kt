package mbitsystem.com.spacexmvimyapp.rockets

import mbitsystem.com.spacexmvimyapp.data.model.Rocket
import mbitsystem.com.spacexmvimyapp.mvibase.MainViewState

data class RocketsViewState(
    val isLoading: Boolean,
    val error: Throwable? ,
    val rocketList: List<Rocket>
) : MainViewState {

    companion object {
        fun idle(): RocketsViewState {
            return RocketsViewState(
                isLoading = false,
                error = null,
                rocketList = emptyList()
            )
        }
    }
}