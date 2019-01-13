package mbitsystem.com.spacexmvimyapp.rockets

import android.arch.lifecycle.ViewModel
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import mbitsystem.com.spacexmvimyapp.mvibase.MainView
import mbitsystem.com.spacexmvimyapp.data.model.Rocket
import mbitsystem.com.spacexmvimyapp.mvibase.MainViewModel
import mbitsystem.com.spacexmvimyapp.notOfType
import mbitsystem.com.spacexmvimyapp.rockets.RocketsResult.LoadRocketsResult
import java.util.concurrent.TimeUnit

class RocketsViewModel(
    private val actionProcessorHolder: RocketsActionProcessorHolder
) : ViewModel(), MainViewModel<RocketsIntent, RocketsViewState> {


    private val intentSubject: PublishSubject<RocketsIntent> = PublishSubject.create()
    private val statesObservable: Observable<RocketsViewState> = compose()



    override fun processIntents(intents: Observable<RocketsIntent>) {
        intents.subscribe(intentSubject)
    }

    override fun states(): Observable<RocketsViewState> = statesObservable

    /**
     * Translate an [MviIntent] to an [MviAction].
     * Used to decouple the UI and the business logic to allow easy testings and reusability.
     */
    private fun actionFromIntent(intent: RocketsIntent): RocketsAction {
        return when (intent) {
            is RocketsIntent.LoadRocketsIntent -> RocketsAction.LoadRocketsAction(true)
            is RocketsIntent.InitialIntent -> RocketsAction.LoadRocketsAction(true)
        }
    }

    /**
     * take only the first ever InitialIntent and all intents of other types
     * to avoid reloading data on config changes
     */
    private val intentFilter: ObservableTransformer<RocketsIntent, RocketsIntent>
        get() = ObservableTransformer { intents ->
            intents.publish { shared ->
                Observable.merge<RocketsIntent>(
                    shared.ofType(RocketsIntent.InitialIntent::class.java).take(1),
                    shared.notOfType(RocketsIntent.InitialIntent::class.java)
                )
            }
        }
    /**
     * Compose all components to create the stream logic
     */
    private fun compose(): Observable<RocketsViewState> {
        return intentSubject
            .compose(intentFilter)
            .map(this::actionFromIntent)
            .compose(actionProcessorHolder.actionProcessor)
            // Cache each state and pass it to the reducer to create a new state from
            // the previous cached one and the latest Result emitted from the action processor.
            // The Scan operator is used here for the caching.
            .scan(RocketsViewState.idle(), reducer)
            // When a reducer just emits previousState, there's no reason to call render. In fact,
            // redrawing the UI in cases like this can cause jank (e.g. messing up snackbar animations
            // by showing the same snackbar twice in rapid succession).
            .distinctUntilChanged()
            // Emit the last one event of the stream on subscription
            // Useful when a View rebinds to the ViewModel after rotation.
            .replay(1)
            // Create the stream on creation without waiting for anyone to subscribe
            // This allows the stream to stay alive even when the UI disconnects and
            // match the stream's lifecycle to the ViewModel's one.
            .autoConnect(0)
    }

    companion object {
        /**
         * The Reducer is where [MviViewState], that the [MviView] will use to
         * render itself, are created.
         * It takes the last cached [MviViewState], the latest [MviResult] and
         * creates a new [MviViewState] by only updating the related fields.
         * This is basically like a big switch statement of all possible types for the [MviResult]
         */
        private val reducer = BiFunction { previousState: RocketsViewState, result: RocketsResult ->
            when (result) {
                is LoadRocketsResult -> when (result) {
                    is LoadRocketsResult.Success -> {
                        val rockets = result.rocketList
                        previousState.copy(
                            isLoading = false,
                            rocketList = rockets)
                    }
                    is LoadRocketsResult.Failure -> previousState.copy(isLoading = false, error = result.error)
                    is LoadRocketsResult.ProgressState -> previousState.copy(isLoading = true)
                }
            }
        }
    }
    }