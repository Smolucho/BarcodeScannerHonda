package pl.Smoluch.barcodescannerhonda.data

import java.util.UUID

data class Category(
    val id: String = UUID.randomUUID().toString(),
    var name: String
) 