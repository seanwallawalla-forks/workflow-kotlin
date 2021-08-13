package com.squareup.sample.mainactivity

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.idling.CountingIdlingResource
import com.squareup.sample.authworkflow.AuthService
import com.squareup.sample.authworkflow.AuthService.AuthRequest
import com.squareup.sample.authworkflow.AuthService.AuthResponse
import com.squareup.sample.authworkflow.AuthService.SecondFactorRequest
import com.squareup.sample.authworkflow.AuthWorkflow
import com.squareup.sample.authworkflow.RealAuthService
import com.squareup.sample.authworkflow.RealAuthWorkflow
import com.squareup.sample.gameworkflow.RealGameLog
import com.squareup.sample.gameworkflow.RealRunGameWorkflow
import com.squareup.sample.gameworkflow.RealTakeTurnsWorkflow
import com.squareup.sample.gameworkflow.RunGameWorkflow
import com.squareup.sample.gameworkflow.TakeTurnsWorkflow
import com.squareup.sample.mainworkflow.TicTacToeWorkflow
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import timber.log.Timber

/**
 * Pretend generated code of a pretend DI framework.
 */
class TicTacToeComponent : ViewModel() {
  private val countingIdlingResource = CountingIdlingResource("AuthServiceIdling")
  val idlingResource: IdlingResource = countingIdlingResource

  private val realAuthService = RealAuthService()

  private val authService = object : AuthService {
    override suspend fun login(request: AuthRequest): AuthResponse {
      return realAuthService.login(request)
    }

    override suspend fun secondFactor(request: SecondFactorRequest): AuthResponse {
      return realAuthService.secondFactor(request)
    }
  }

  private fun authWorkflow(): AuthWorkflow = RealAuthWorkflow(authService)

  private fun gameLog() = RealGameLog()

  private fun gameWorkflow(): RunGameWorkflow = RealRunGameWorkflow(takeTurnsWorkflow(), gameLog())

  private fun takeTurnsWorkflow(): TakeTurnsWorkflow = RealTakeTurnsWorkflow()

  private val ticTacToeWorkflow = TicTacToeWorkflow(authWorkflow(), gameWorkflow())

  fun ticTacToeModelFactory(owner: AppCompatActivity): TicTacToeModel.Factory =
    TicTacToeModel.Factory(owner, ticTacToeWorkflow, traceFilesDir = owner.filesDir)

  companion object {
    init {
      Timber.plant(Timber.DebugTree())

      val stock = Thread.getDefaultUncaughtExceptionHandler()
      Thread.setDefaultUncaughtExceptionHandler { thread, error ->
        Timber.e(error)
        stock?.uncaughtException(thread, error)
      }
    }
  }
}
