package mbitsystem.com.spacexmvimyapp.mvibase

import io.reactivex.Observable

interface MainViewModel<I : MainIntent, S : MainViewState> {

    fun processIntents(intents: Observable<I>)

    fun states(): Observable<S>
}