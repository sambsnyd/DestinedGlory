import kotlinx.html.dom.create
import kotlinx.html.js.p
import kotlin.browser.document

fun main(vararg args: String) {
    val body = document.body!!
    body.append(document.create.p { +"Hello world" })
}