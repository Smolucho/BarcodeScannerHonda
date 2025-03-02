package pl.Smoluch.barcodescannerhonda.data

import java.util.UUID

data class BarcodeCount(
    val id: String = UUID.randomUUID().toString(),
    val value: String,
    var count: Int = 1,
    var categoryId: String? = null
) 