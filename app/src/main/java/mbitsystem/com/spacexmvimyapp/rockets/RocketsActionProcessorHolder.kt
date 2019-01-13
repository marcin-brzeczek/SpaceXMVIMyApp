package mbitsystem.com.spacexmvimyapp.rockets

import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import mbitsystem.com.spacexmvimyapp.data.source.RocketsRepository
import mbitsystem.com.spacexmvimyapp.rockets.RocketsResult.LoadRocketsResult
import mbitsystem.com.spacexmvimyapp.rockets.RocketsAction.LoadRocketsAction

class RocketsActionProcessorHolder(
    private val rocketsRepository: RocketsRepository
) {

    internal val loadTasksProcessor =
        ObservableTransformer<LoadRocketsAction, LoadRocketsResult> { actions ->
            actions.flatMap { action ->
                rocketsRepository.getAllRockets()
                    // Transform the Single to an Observable to allow emission of multiple
                    // events down the stream (e.g. the InFlight event)
                    .toObservable()
                    // Wrap returned data into an immutable object
                    .map { tasks -> LoadRocketsResult.Success(tasks) }
                    .cast(LoadRocketsResult::class.java)
                    // Wrap any error into an immutable object and pass it down the stream
                    // without crashing.
                    // Because errors are data and hence, should just be part of the stream.
                    .onErrorReturn(LoadRocketsResult::Failure)
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    // Emit an InFlight event to notify the subscribers (e.g. the UI) we are
                    // doing work and waiting on a response.
                    // We emit it after observing on the UI thread to allow the event to be emitted
                    // on the current frame and avoid jank.
                    .startWith(LoadRocketsResult.ProgressState)
            }
        }  /**
     * Splits the [Observable] to match each type of [MviAction] to
     * its corresponding business logic processor. Each processor takes a defined [MviAction],
     * returns a defined [MviResult]
     * The global actionProcessor then merges all [Observable] back to
     * one unique [Observable].
     *
     *
     * The splitting is done using [Observable.publish] which allows almost anything
     * on the passed [Observable] as long as one and only one [Observable] is returned.
     *
     *
     * An security layer is also added for unhandled [MviAction] to allow early crash
     * at runtime to easy the maintenance.
     */
    internal var actionProcessor =
        ObservableTransformer<RocketsAction, RocketsResult> { actions ->
            actions.publish() { shared ->

                    shared.ofType(RocketsAction.LoadRocketsAction::class.java).compose(loadTasksProcessor)

                        // Error for not implemented actions
                        shared.filter { v ->
                            v !is RocketsAction.LoadRocketsAction
                        }.flatMap { w ->
                            Observable.error<RocketsResult>(
                                IllegalArgumentException("Unknown Action type: $w"))
                        }

            }
        }
}
