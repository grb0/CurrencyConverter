package ba.grbo.currencyconverter.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CurrencyName

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class FilterBy