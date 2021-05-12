package ba.grbo.currencyconverter.di

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import ba.grbo.currencyconverter.R
import ba.grbo.currencyconverter.data.models.domain.ExchangeableCurrency
import ba.grbo.currencyconverter.util.FilterBy
import ba.grbo.currencyconverter.util.Language
import ba.grbo.currencyconverter.util.Theme
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.FragmentScoped
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.components.SingletonComponent
import java.util.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApplicationScopedPreferences {
    @Singleton
    @Provides
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

    @Singleton
    @Provides
    fun provideTheme(
        sharedPreferences: SharedPreferences,
        @ApplicationContext context: Context
    ): Theme {
        val theme = sharedPreferences.getString(
            context.getString(R.string.key_theme),
            Theme.DEVICE.name
        ) ?: Theme.DEVICE.name

        return Theme.valueOf(theme)
    }

    @Provides
    fun provideLanguage(
        sharedPreferences: SharedPreferences,
        @ApplicationContext context: Context
    ): Locale {
        val language = sharedPreferences.getString(
            context.getString(R.string.key_language),
            Language.ENGLISH.name
        ) ?: Language.ENGLISH.name

        return Language.valueOf(language).toLocale()
    }
}

@Module
@InstallIn(ActivityRetainedComponent::class)
object ActivityScopedPreferences {
    @Provides
    fun provideUiName(
        sharedPreferences: SharedPreferences,
        @ApplicationContext context: Context,
    ): ExchangeableCurrency.UiName {
        val uiName = sharedPreferences.getString(
            context.getString(R.string.key_ui_name),
            ExchangeableCurrency.UiName.CODE_AND_NAME.name
        ) ?: ExchangeableCurrency.UiName.CODE_AND_NAME.name

        return ExchangeableCurrency.UiName.valueOf(uiName)
    }

    @Provides
    fun provideFilterBy(
        sharedPreferences: SharedPreferences,
        @ApplicationContext context: Context
    ): FilterBy {
        val filterBy = sharedPreferences.getString(
            context.getString(R.string.key_filter_by),
            FilterBy.BOTH.name
        ) ?: FilterBy.BOTH.name

        return FilterBy.valueOf(filterBy)
    }
}

@Module
@InstallIn(FragmentComponent::class)
object FragmentScoped {
    @FragmentScoped
    @ShowScrollbar
    @Provides
    fun provideShowScrollbar(
        sharedPreferences: SharedPreferences,
        @ApplicationContext context: Context
    ) = sharedPreferences.getBoolean(
        context.getString(R.string.key_show_scrollbar),
        true
    )

    @FragmentScoped
    @AutohideScrollbar
    @Provides
    fun provideAutohideScrollbar(
        sharedPreferences: SharedPreferences,
        @ApplicationContext context: Context
    ) = sharedPreferences.getBoolean(
        context.getString(R.string.key_auto_hide_scrollbar),
        false
    )

    @FragmentScoped
    @ExtendDropdownMenuInLandscape
    @Provides
    fun provideExtendDropdownMenuInLandscape(
        sharedPreferences: SharedPreferences,
        @ApplicationContext context: Context
    ) = sharedPreferences.getBoolean(
        context.getString(R.string.key_extend_chooser_landscape),
        true
    )
}

@Module
@InstallIn(ViewModelComponent::class)
object ViewModelScoped {
    @ViewModelScoped
    @IgnoreFailedDbUpdates
    @Provides
    fun provideIgnoreDatabaseUpdateFailedNotifications(
        sharedPreferences: SharedPreferences,
        @ApplicationContext context: Context
    ) = sharedPreferences.getBoolean(
        context.getString(R.string.key_ignore_failed_db_update),
        false
    )
}