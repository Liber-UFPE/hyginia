package br.ufpe.liber

import io.micronaut.context.env.Environment
import io.micronaut.kotlin.runtime.startApplication

object Application {
    @JvmStatic
    @Suppress("detekt:SpreadOperator")
    fun main(args: Array<String>) {
        startApplication<Application>(*args) {
            // Fallback to dev environment if none is specified.
            environments(System.getenv().getOrDefault("MICRONAUT_ENVIRONMENTS", Environment.DEVELOPMENT))
            eagerInitAnnotated(EagerInProduction::class.java)
        }
    }
}
