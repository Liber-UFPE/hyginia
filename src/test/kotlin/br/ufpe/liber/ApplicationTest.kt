package br.ufpe.liber

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.micronaut.context.ApplicationContext
import io.micronaut.http.client.HttpClient
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest

// DO NOT EDIT: this file is automatically synced from the template repository
// in https://github.com/Liber-UFPE/project-starter.

@MicronautTest
class ApplicationTest(
    private val server: EmbeddedServer,
    private val context: ApplicationContext,
) : BehaviorSpec({
    given("Application") {
        `when`("application starts") {
            then("server is running") { server.isRunning shouldBe true }
        }
        `when`("has a health check") {
            then("it should return UP") {
                val client = context.createBean(HttpClient::class.java, server.url).toBlocking()
                client.exchange<Any>("/health").status.code shouldBe 200
            }
        }
    }
})
