package br.ufpe.liber.controllers

import br.ufpe.liber.assets.AssetsResolver
import br.ufpe.liber.get
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import io.micronaut.context.ApplicationContext
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.DefaultHttpClientConfiguration
import io.micronaut.http.client.HttpClient
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest

@MicronautTest
class IndexControllerTest(private val server: EmbeddedServer, private val context: ApplicationContext) :
    BehaviorSpec({
        val client = context
            .createBean(
                HttpClient::class.java,
                server.url,
                DefaultHttpClientConfiguration().apply { isExceptionOnErrorStatus = false },
            )
            .toBlocking()

        beforeSpec {
            // AssetsResolver initializes a lateinit property used by the view helpers
            context.getBean(AssetsResolver::class.java)
        }

        given("IndexController") {
            `when`("navigating to pages") {
                forAll(
                    row("/", HttpStatus.OK),
                    row("/projeto", HttpStatus.OK),
                    row("/equipe", HttpStatus.OK),
                    row("/jose-hyginio", HttpStatus.OK),
                    row("/contato", HttpStatus.OK),
                    row("/does-not-exists", HttpStatus.NOT_FOUND),
                ) { path, expectedStatus ->
                    then("GET $path should return $expectedStatus") {
                        client.get(path).status() shouldBe expectedStatus
                    }
                }
            }
        }
    })
