package org.koin.compiler.type

fun String.clearPackageSymbols() = replace("`","").replace("'","")

val forbiddenKeywords = listOf("in","by","interface")