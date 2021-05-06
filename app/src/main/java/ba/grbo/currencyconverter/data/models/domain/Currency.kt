package ba.grbo.currencyconverter.data.models.domain

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import ba.grbo.currencyconverter.util.Constants
import ba.grbo.currencyconverter.util.Constants.STRING
import ba.grbo.currencyconverter.data.models.db.Currency as DatabaseCurrency

data class Currency(
    val code: Code,
    val exchangeRate: ExchangeRate,
    var isFavorite: Boolean,
    private val context: Context, // safe to hold, because its app's context, nothing can outlive it
    private val nameResourceName: String,
    private val flagResourceName: String
) {
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

    fun toDatabase() = DatabaseCurrency(
        code,
        nameResourceName,
        flagResourceName,
        isFavorite
    )

    private fun initName(context: Context) = Name(
        code.name,
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

    enum class Code {
        AED,
        AFN,
        ALL,
        AMD,
        ANG,
        AOA,
        ARS,
        AUD,
        AWG,
        AZN,
        BAM,
        BBD,
        BDT,
        BGN,
        BHD,
        BIF,
        BMD,
        BND,
        BOB,
        BRL,
        BSD,
        BTC,
        BTN,
        BWP,
        BYN,
        BZD,
        CAD,
        CDF,
        CHF,
        CLF,
        CLP,
        CNH,
        CNY,
        COP,
        CRC,
        CUC,
        CUP,
        CVE,
        CZK,
        DJF,
        DKK,
        DOP,
        DZD,
        EGP,
        ERN,
        ETB,
        EUR,
        FJD,
        FKP,
        GBP,
        GEL,
        GGP,
        GHS,
        GIP,
        GMD,
        GNF,
        GTQ,
        GYD,
        HKD,
        HNL,
        HRK,
        HTG,
        HUF,
        IDR,
        ILS,
        IMP,
        INR,
        IQD,
        IRR,
        ISK,
        JEP,
        JMD,
        JOD,
        JPY,
        KES,
        KGS,
        KHR,
        KMF,
        KPW,
        KRW,
        KWD,
        KYD,
        KZT,
        LAK,
        LBP,
        LKR,
        LRD,
        LSL,
        LYD,
        MAD,
        MDL,
        MGA,
        MKD,
        MMK,
        MNT,
        MOP,
        MRU,
        MUR,
        MVR,
        MWK,
        MXN,
        MYR,
        MZN,
        NAD,
        NGN,
        NIO,
        NOK,
        NPR,
        NZD,
        OMR,
        PAB,
        PEN,
        PGK,
        PHP,
        PKR,
        PLN,
        PYG,
        QAR,
        RON,
        RSD,
        RUB,
        RWF,
        SAR,
        SBD,
        SCR,
        SDG,
        SEK,
        SGD,
        SLL,
        SOS,
        SRD,
        SSP,
        STN,
        SVC,
        SYP,
        SZL,
        THB,
        TJS,
        TMT,
        TND,
        TOP,
        TRY,
        TTD,
        TWD,
        TZS,
        UAH,
        UGX,
        USD,
        UYU,
        UZS,
        VES,
        VND,
        VUV,
        WST,
        XAG,
        XAU,
        XPD,
        XPT,
        YER,
        ZAR,
        ZMW,
        ZWL
    }
}