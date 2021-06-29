package org.taymyr.play.repository.test

import com.google.inject.Guice
import com.google.inject.Module
import io.kotest.core.listeners.TestListener
import io.kotest.core.spec.Spec
import play.Application
import play.ApplicationLoader
import play.Environment
import play.inject.guice.GuiceApplicationLoader
import play.test.Helpers

class PlayListener(private val module: Module) : TestListener {

    private lateinit var application: Application

    override suspend fun beforeSpec(spec: Spec) {
        val builder = GuiceApplicationLoader()
            .builder(ApplicationLoader.Context(Environment.simple()))
            .configure(Helpers.inMemoryDatabase() as Map<String, Any>)
            .overrides(module)
        application = builder.build()
        Guice.createInjector(builder.applicationModule()).injectMembers(spec)
        Helpers.start(application)
    }

    override suspend fun afterSpec(spec: Spec) {
        Helpers.stop(application)
    }
}
