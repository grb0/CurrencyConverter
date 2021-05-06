package ba.grbo.currencyconverter.data.source

sealed class Result<out R> {
    val succeeded
        get() = this is Success && data != null

    data class Success<out T>(val data: T) : Result<T>()
    object Loading : Result<Nothing>()
    data class Error(val exception: Exception) : Result<Nothing>()

    override fun toString(): String = when (this) {
        is Success<*> -> "Success[data=$data]"
        is Loading -> "Loading"
        is Error -> "Error[exception=$exception]"
    }
}