package mbitsystem.com.spacexmvimyapp.mvibase

import io.reactivex.Observable

interface MainView<I : MainIntent, in S : MainViewState> {

    fun intents(): Observable<I>

    fun render(state: S)
}