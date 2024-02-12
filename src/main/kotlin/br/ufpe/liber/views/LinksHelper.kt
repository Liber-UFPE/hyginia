package br.ufpe.liber.views

import io.micronaut.core.util.StringUtils
import java.util.Optional

// DO NOT EDIT: this file is automatically synced from the template repository
// in https://github.com/Liber-UFPE/project-starter.

object LinksHelper {
    const val UFPE: String = "https://ufpe.br/"
    const val HOLANDAEVOCE: String = "https://www.holandaevoce.nl/"
    const val DCI: String = "https://www.ufpe.br/dci"
    const val IAHGP: String = "https://arqueologico.com.br"
    const val FUNCULTURA: String = "https://www.cultura.pe.gov.br/pagina/funcultura/"
    const val FACEPE: String = "https://www.facepe.br/"
    const val CEPE: String = "https://www.cepe.com.br//"
    const val REDE_MEMORIAL: String = "https://redememorialpernambuco.blogspot.com/"

    object Liber {
        const val SITE: String = "http://www.liber.ufpe.br/"
        const val INSTAGRAM = "https://www.instagram.com/liberufpe/"
        const val FACEBOOK = "https://www.facebook.com/liberufpe"
        const val YOUTUBE = "https://www.youtube.com/@liberufpe"
        const val LINKTREE = "https://linktr.ee/liberufpe"
        const val GITHUB = "https://github.com/Liber-UFPE/"

        fun link(path: String): String = "$SITE${path.trim()}"
    }

    @JvmStatic
    fun baseUrl(): String = Optional.ofNullable(System.getenv("PROJECT_STARTER_PATH")).orElse("/")

    fun linkTo(path: String): String = StringUtils.prependUri(baseUrl(), path)
}
