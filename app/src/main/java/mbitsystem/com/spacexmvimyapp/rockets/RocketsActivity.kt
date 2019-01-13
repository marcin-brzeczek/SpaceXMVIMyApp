package mbitsystem.com.spacexmvimyapp.rockets

import android.arch.lifecycle.ViewModelProviders
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*
import mbitsystem.com.spacexmvimyapp.mvibase.MainView
import mbitsystem.com.spacexmvimyapp.R
import mbitsystem.com.spacexmvimyapp.adapter.RocketsAdapter
import mbitsystem.com.spacexmvimyapp.data.model.Rocket
import mbitsystem.com.spacexmvimyapp.data.source.RocketsRepository

class RocketsActivity : AppCompatActivity(), MainView<RocketsIntent, RocketsViewState> {

    private val rocketsAdapter = RocketsAdapter()

    private lateinit var viewModel: RocketsViewModel
    private lateinit var actionProcessorHolder: RocketsActionProcessorHolder
    private lateinit var rocketsRepository: RocketsRepository



    // Used to manage the data flow lifecycle and avoid memory leak.
    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initRecyclerView()
        rocketsRepository = RocketsRepository()
        actionProcessorHolder = RocketsActionProcessorHolder(rocketsRepository)
        viewModel = RocketsViewModel(actionProcessorHolder)
    }

    override fun onStart() {
        super.onStart()
        bind()
    }

    override fun onStop() {
        disposables.clear()
        super.onStop()
    }

    /**
     * Connect the [MviView] with the [MviViewModel]
     * We subscribe to the [MviViewModel] before passing it the [MviView]'s [MviIntent]s.
     * If we were to pass [MviIntent]s to the [MviViewModel] before listening to it,
     * emitted [MviViewState]s could be lost
     */

    private fun bind() {
        // Subscribe to the ViewModel and call render for every emitted state
        disposables.add(viewModel.states().subscribe(this::render))
        // Pass the UI's intents to the ViewModel
        viewModel.processIntents(intents())
    }

    private fun initRecyclerView() {
        rocketsRecyclerView.layoutManager = LinearLayoutManager(this)
        rocketsRecyclerView.adapter = rocketsAdapter
    }


    override fun intents(): Observable<RocketsIntent> {
        return clickFetchButtonIntent()
    }

    private fun clickFetchButtonIntent(): Observable<RocketsIntent> {
        return RxView.clicks(showMeRocketsButton).map { RocketsIntent.LoadRocketsIntent(true) }
    }

    override fun render(mainViewState: RocketsViewState) {
        with(mainViewState) {
            showProgressBar(isLoading)
            showError(error)
            showRocketList(rocketList)
        }
    }

    private fun showProgressBar(show: Boolean) {
        if (show) {
            rocketsRecyclerView.visibility = View.GONE
            errorTextView.visibility = View.GONE
            progressBar.visibility = View.VISIBLE
        } else {
            rocketsRecyclerView.visibility = View.VISIBLE
            errorTextView.visibility = View.GONE
            progressBar.visibility = View.GONE
        }
    }

    private fun showError(show: Throwable?) {
        if (show!=null) {
            rocketsRecyclerView.visibility = View.GONE
            errorTextView.visibility = View.VISIBLE
        } else {
            errorTextView.visibility = View.GONE
        }
    }

    private fun showRocketList(rocketList: List<Rocket>) {
        rocketsAdapter.setRocketList(rocketList)
    }
}