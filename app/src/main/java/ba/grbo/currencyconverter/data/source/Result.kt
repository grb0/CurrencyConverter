package ba.grbo.currencyconverter.data.source

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

sealed interface DatabaseResult<out R>

sealed interface NetworkResult<out R> {
    override fun toString(): String
}

data class Success<out T>(val data: T) : DatabaseResult<T>, NetworkResult<T>

data class Error(val exception: Exception) : DatabaseResult<Nothing>, NetworkResult<Nothing>

object Loading : NetworkResult<Nothing> {
    override fun toString(): String = "Loading"
}

suspend fun <T> toDatabaseResult(input: suspend () -> T): DatabaseResult<T> = try {
    Success(input())
} catch (e: Exception) {
    Error(e)
}

fun <R> Flow<R>.toDatabaseResult(): Flow<DatabaseResult<R>> = map {
    try {
        Success(it)
    } catch (e: Exception) {
        Error(e)
    }
}