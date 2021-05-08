package ba.grbo.currencyconverter.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ShowScrollbar

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AutohideScrollbar

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ExtendDropdownMenuInLandscape

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DispatcherIO

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DispatcherDefault