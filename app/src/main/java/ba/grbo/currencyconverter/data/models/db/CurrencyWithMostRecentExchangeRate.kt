package ba.grbo.currencyconverter.data.models.db

import android.content.Context
import androidx.room.Embedded
import androidx.room.Relation
import ba.grbo.currencyconverter.data.models.domain.ExchangeRate
import ba.grbo.currencyconverter.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ba.grbo.currencyconverter.data.models.domain.Currency as DomainCurrency

data class CurrencyWithMostRecentExchangeRate(
    @Embedded val currency: Currency,

    @Relation(
        parentColumn = Constants.PARENT_COLUMNS,
        entityColumn = Constants.CHILD_COLUMNS,
        entity = MostRecentExchangeRate::class
    )
    val mostRecentExchangeRate: MostRecentExchangeRate
) {
    fun toDomain(context: Context) = DomainCurrency(
        currency.code,
        ExchangeRate(
            mostRecentExchangeRate.exchangeRate.value,
            mostRecentExchangeRate.exchangeRate.date
        ),
        currency.isFavorite,
        context,
        currency.nameResourceName,
        currency.flagResourceName
    )

}

fun List<CurrencyWithMostRecentExchangeRate>.toDomain(context: Context) = map {
    it.toDomain(context)
}

fun Flow<List<CurrencyWithMostRecentExchangeRate>>.toDomain(context: Context) = this.map {
    it.toDomain(context)
}