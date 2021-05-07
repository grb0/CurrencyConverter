package ba.grbo.currencyconverter.data.models.database

import ba.grbo.currencyconverter.data.models.shared.Evaluable
import ba.grbo.currencyconverter.data.models.shared.Unexchangeable

data class MultiExchangeableCurrency(
    override val code: String,
    override val symbol: String,
    override val nameResourceName: String,
    override val flagResourceName: String,
    override var isFavorite: Boolean,
    val exchangeRates: List<Evaluable>
) : Unexchangeable
