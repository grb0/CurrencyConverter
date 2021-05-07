package ba.grbo.currencyconverter.data.models.domain

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import ba.grbo.currencyconverter.data.models.database.UnexchangeableCurrency
import ba.grbo.currencyconverter.data.models.shared.Evaluable
import ba.grbo.currencyconverter.data.models.shared.Exchangeable
import ba.grbo.currencyconverter.util.Constants
import ba.grbo.currencyconverter.util.Constants.STRING

data class ExchangeableCurrency(
    override val code: String,
    override val symbol: String,
    override val nameResourceName: String,
    override val flagResourceName: String,
    override var isFavorite: Boolean,
    override var exchangeRate: Evaluable,
    private val context: Context // safe to hold, because its app's context, nothing can outlive it
) : Exchangeable {
    val name: Name
    val flag: Drawable

    init {
        name = initName(context)
        flag = initFlag(context)
    }

    fun getUiName(type: UiName) = when (type) {
        UiName.CODE -> name.code
        UiName.NAME -> name.name
        UiName.CODE_AND_NAME -> name.codeAndName
        UiName.NAME_AND_CODE -> name.nameAndCode
    }

    fun toDatabase() = UnexchangeableCurrency(
        code,
        symbol,
        nameResourceName,
        flagResourceName,
        isFavorite
    )

    private fun initName(context: Context) = Name(
        code,
        context.getString(
            context.resources.getIdentifier(
                nameResourceName,
                STRING,
                context.packageName
            )
        )
    )

    private fun initFlag(context: Context) = ContextCompat.getDrawable(
        context,
        context.resources.getIdentifier(flagResourceName, Constants.DRAWABLE, context.packageName)
    )!!

    data class Name(
        val code: String,
        val name: String,
        val codeAndName: String = "$code — $name",
        val nameAndCode: String = "$name — $code",
    )

    enum class UiName {
        CODE,
        NAME,
        CODE_AND_NAME,
        NAME_AND_CODE
    }
}