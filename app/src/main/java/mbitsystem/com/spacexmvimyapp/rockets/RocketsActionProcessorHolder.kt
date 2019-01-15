package mbitsystem.com.spacexmvimyapp.rockets

import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import mbitsystem.com.spacexmvimyapp.data.source.RocketsRepository
import mbitsystem.com.spacexmvimyapp.rockets.RocketsResult.LoadRocketsResult
import java.util.concurrent.TimeUnit

class RocketsActionProcessorHolder(
    private val rocketsRepository: RocketsRepository
) {

    internal val loadTasksProcessor =
        ObservableTransformer<RocketsAction, RocketsResult> { actions ->
            actions.flatMap { action ->
                rocketsRepository.getAllRockets()
                    .toObservable()

                    // Wrap returned data into an immutable object
                    .map { tasks -> LoadRocketsResult.Success(tasks) }
                    .cast(LoadRocketsResult::class.java)
                    // Wrap any error into an immutable object and pass it down the stream
                    // without crashing.
                    // Because errors are data and hence, should just be part of the stream.
                    .onErrorReturn(LoadRocketsResult::Failure)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    // Emit an InFlight event to notify the subscribers (e.g. the UI) we are
                    // doing work and waiting on a response.
                    // We emit it after observing on the UI thread to allow the event to be emitted
                    // on the current frame and avoid jank.
                    .startWith(LoadRocketsResult.ProgressState)
            }
        }
}
