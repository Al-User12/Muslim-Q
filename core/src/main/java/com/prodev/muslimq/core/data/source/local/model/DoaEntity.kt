package com.prodev.muslimq.core.data.source.local.model

data class DoaEntity(
    val id: String,
    val title: String,
    val arab: String,
    val latin: String,
    val translate: String,
    var isExpanded: Boolean = false
)
