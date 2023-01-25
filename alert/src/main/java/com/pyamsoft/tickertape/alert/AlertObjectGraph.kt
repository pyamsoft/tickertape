package com.pyamsoft.tickertape.alert

import android.app.Application
import android.content.Context
import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.requireNotNull
import timber.log.Timber

object AlertObjectGraph {

  object WorkerScope {

    private val trackingMap = mutableMapOf<Application, AlertWorkComponent>()

    /** Called from Application */
    fun install(
        application: Application,
        component: AlertWorkComponent,
    ) {
      trackingMap[application] = component
      Timber.d("Track ApplicationScoped install: $application $component")
    }

    @CheckResult
    internal fun retrieve(context: Context): AlertWorkComponent {
      return retrieve(context.applicationContext as Application)
    }

    @CheckResult
    internal fun retrieve(application: Application): AlertWorkComponent {
      return trackingMap[application].requireNotNull {
        "Could not find ApplicationScoped internals for Application: $application"
      }
    }
  }
}
