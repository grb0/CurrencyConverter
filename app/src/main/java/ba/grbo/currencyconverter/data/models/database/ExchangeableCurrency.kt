package ba.grbo.currencyconverter.data.models.database

import android.content.Context
import ba.grbo.currencyconverter.data.models.domain.ExchangeableCurrency
import ba.grbo.currencyconverter.data.models.shared.Evaluable
import ba.grbo.currencyconverter.data.models.shared.Exchangeable

data class ExchangeableCurrency(
    override val code: String,
    override val symbol: String,
    override val nameResourceName: String,
    override val flagResourceName: String,
    override var isFavorite: Boolean,
    override var exchangeRate: Evaluable
) : Exchangeable {
    fun toDomain(context: Context) = ExchangeableCurrency(
        code,
        symbol,
        nameResourceName,
        flagResourceName,
        isFavorite,
        exchangeRate,
        context
    )
}