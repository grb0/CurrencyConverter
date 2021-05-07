package ba.grbo.currencyconverter.data.models.database

import ba.grbo.currencyconverter.data.models.shared.Evaluable
import java.util.*

data class EssentialExchangeRate(
    override val value: Double,
    override val date: Date
) : Evaluable
