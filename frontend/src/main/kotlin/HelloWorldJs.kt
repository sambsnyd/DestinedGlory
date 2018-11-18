package com.github.sambsnyd.destinedglory

import kotlin.browser.document

fun main(vararg args: String) {
    val body = document.body!!
    body.textContent = "Hello world from javascript"
}
