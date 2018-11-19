package com.github.sambsnyd.destinedglory


import kotlinx.coroutines.launch
import io.ktor.http.encodeURLPath
import kotlinx.coroutines.GlobalScope
import kotlinx.html.*
import kotlinx.html.dom.create
import kotlinx.html.js.div
import kotlinx.html.js.onSubmitFunction
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLInputElement
import org.w3c.xhr.XMLHttpRequest
import kotlin.browser.document


fun main(vararg args: String) {
    val resultDivId = "resultDiv"
    val resultDiv = document.create.div {
        id = resultDivId
    }
    val formDiv = document.create.div {
        p {
            + "Destined Glory"
        }
        p {
            + "Dumping your Crucible stats into an impenetrable blob of JSON since 2018"
        }
        val formName = "inputForm"
        val inputId = "inputId"
        val inputName = "guardian"
        form {
            name = formName

            label {
                htmlFor = inputId
                + "Enter your Bungie account: "
            }
            textInput {
                name = inputName
                id = inputId
                value = "castor#11308"
            }
            submitInput {
                value="submit"
            }
            onSubmitFunction = {
                it.preventDefault()

                GlobalScope.launch {
                    val field = document.getElementById(inputId) as HTMLInputElement
                    val guardianName = field.value
                    val resultArea = document.getElementById(resultDivId) as HTMLDivElement
                    resultArea.textContent = "Looking up $guardianName"
                    val request = XMLHttpRequest()
                    request.open("GET", "http://localhost:8008/guardian/${guardianName.encodeURLPath()}")
                    request.onreadystatechange = {
                        if(request.readyState == 4.toShort()) {
                            resultArea.textContent = request.responseText
                        }
                        Unit
                    }
                    request.send()
                }
            }
        }
    }

    document.body!!.apply {
        style.apply {
            background = "#000000"
            color = "#FFFFFF"
        }
        appendChild(formDiv)
        appendChild(resultDiv)
    }
}
