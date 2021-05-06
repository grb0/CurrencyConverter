package ba.grbo.currencyconverter.data.models.db

import androidx.room.DatabaseView
import androidx.room.Embedded
import ba.grbo.currencyconverter.data.models.domain.ExchangeRate as DomainExchangeRate

@DatabaseView("SELECT * FROM exchange_rates_table ORDER BY date ASC")
data class MostRecentExchangeRate(
    @Embedded val exchangeRate: ExchangeRate
) {
    fun toDomain(): DomainExchangeRate {
        return DomainExchangeRate(exchangeRate.value, exchangeRate.date)
    }
}