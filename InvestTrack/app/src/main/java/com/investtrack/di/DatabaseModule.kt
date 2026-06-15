package com.investtrack.di

import android.content.Context
import androidx.room.Room
import com.investtrack.data.dao.AccountDao
import com.investtrack.data.dao.SecurityDao
import com.investtrack.data.dao.TransactionDao
import com.investtrack.data.database.InvestTrackDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): InvestTrackDatabase =
        Room.databaseBuilder(context, InvestTrackDatabase::class.java, "investtrack.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideAccountDao(db: InvestTrackDatabase): AccountDao = db.accountDao()

    @Provides
    fun provideSecurityDao(db: InvestTrackDatabase): SecurityDao = db.securityDao()

    @Provides
    fun provideTransactionDao(db: InvestTrackDatabase): TransactionDao = db.transactionDao()
}
