package com.lex.qr.utils

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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

}
sealed class UiEvent {
    data class ShowToast(val message: String) : UiEvent()
    data class ChangeTitle(val title: String) : UiEvent()
    data class Login(val claims: Claims) : UiEvent()
}