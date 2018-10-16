package org.taymyr.play.repository.test

import com.google.inject.Guice
import com.google.inject.Module
import io.kotlintest.Description
import io.kotlintest.Spec
import io.kotlintest.extensions.TestListener
import play.Application
import play.ApplicationLoader
import play.Environment
import play.inject.guice.GuiceApplicationLoader
import play.test.Helpers

class PlayListener(private val module: Module) : TestListener {

    var application: Application? = null

    override fun beforeSpec(description: Description, spec: Spec) {
        val builder = GuiceApplicationLoader()
            .builder(ApplicationLoader.Context(Environment.simple()))
            .configure(Helpers.inMemoryDatabase() as Map<String, Any>)
            .overrides(module)
        application = builder.build()
        Guice.createInjector(builder.applicationModule()).injectMembers(spec)
        Helpers.start(application)
    }

    override fun afterSpec(description: Description, spec: Spec) {
        Helpers.stop(application)
    }
}