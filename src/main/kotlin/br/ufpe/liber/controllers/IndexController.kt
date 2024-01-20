package br.ufpe.liber.controllers

import br.ufpe.liber.Templates
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces

@Controller("/")
@Produces(MediaType.TEXT_HTML)
class IndexController(private val templates: Templates) : KteController {
    @Get
    fun index() = ok(templates.index())

    @Get("/equipe")
    fun staff() = ok(templates.staff())

    @Get("/projeto")
    fun project() = ok(templates.project())

    @Get("/jose-hyginio")
    fun who() = ok(templates.who())

    @Get("/contato")
    fun contact() = ok(templates.contact())
}
