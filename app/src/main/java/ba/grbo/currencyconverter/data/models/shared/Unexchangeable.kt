package ba.grbo.currencyconverter.data.models.shared

interface Unexchangeable {
    val code: String
    val symbol: String
    val nameResourceName: String
    val flagResourceName: String
    var isFavorite: Boolean
}