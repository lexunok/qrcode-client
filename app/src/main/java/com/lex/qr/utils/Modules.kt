package com.lex.qr.utils

import android.app.Activity
import android.content.Context
import androidx.work.WorkManager
import com.lex.qr.pages.Page
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideApiService(): API = API()

    @Provides
    @Singleton
    fun provideUserPreferences(@ApplicationContext context: Context) = UserPreferences(context)

    @Provides
    @Singleton
    fun provideGeolocationClient() = GeolocationClient()

    @Provides
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }
}
sealed class UiEvent {
    data class ShowToast(val message: String) : UiEvent()
    data class ChangeTitle(val title: String) : UiEvent()
    data class ChangePage(val page: Page) : UiEvent()
    data class CodeCreated(val code: CodeResponse) : UiEvent()
    data class ChangeUser(val user: Claims?) : UiEvent()
}