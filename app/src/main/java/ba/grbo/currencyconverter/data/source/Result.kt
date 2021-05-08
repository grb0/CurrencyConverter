package ba.grbo.currencyconverter.data.source

sealed interface DatabaseResult<out R>

sealed interface NetworkResult<out R> {
    override fun toString(): String
}

data class Success<out T>(val data: T) : DatabaseResult<T>, NetworkResult<T>

data class Error(val exception: Exception) : DatabaseResult<Nothing>, NetworkResult<Nothing>

object Loading : NetworkResult<Nothing> {
    override fun toString(): String = "Loading"
}