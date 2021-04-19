package ba.grbo.currencyconverter.data.models

data class Currency(
    val name: Name,
    val code: Code,
) {
    fun getUiName(type: UiName) = when (type) {
        UiName.CODE -> name.code
        UiName.NAME -> name.name
        UiName.CODE_AND_NAME -> name.codeAndName
        UiName.NAME_AND_CODE -> name.nameAndCode
    }

    enum class UiName {
        CODE,
        NAME,
        CODE_AND_NAME,
        NAME_AND_CODE
    }

    data class Name(
        val code: String,
        val name: String,
        val codeAndName: String = "$code — $name",
        val nameAndCode: String = "$name — $code",
    )
}