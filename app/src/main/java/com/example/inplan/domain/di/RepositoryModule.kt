package com.example.inplan.domain.di

import com.example.inplan.domain.repository.ExpenseRepository
import com.example.inplan.domain.repository.PollRepository
import com.example.inplan.domain.repository.ProfileRepository
import com.example.inplan.domain.repository.TripMemberRepository
import com.example.inplan.domain.repository.TripRepository
import com.example.inplan.data.repository.SupabaseExpenseRepository
import com.example.inplan.data.repository.SupabasePollRepository
import com.example.inplan.data.repository.SupabaseProfileRepository
import com.example.inplan.data.repository.SupabaseTripMemberRepository
import com.example.inplan.data.repository.SupabaseTripRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

// Assumes Hilt (per the existing @Inject/@Singleton usage and SupabaseModule
// providing SupabaseClient). ConvertPollToExpenseUseCase needs no binding here
// since it's a concrete class Hilt can construct directly via @Inject.
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindTripRepository(impl: SupabaseTripRepository): TripRepository

    @Binds
    abstract fun bindTripMemberRepository(impl: SupabaseTripMemberRepository): TripMemberRepository

    @Binds
    abstract fun bindExpenseRepository(impl: SupabaseExpenseRepository): ExpenseRepository

    @Binds
    abstract fun bindPollRepository(impl: SupabasePollRepository): PollRepository

    @Binds
    abstract fun bindProfileRepository(impl: SupabaseProfileRepository): ProfileRepository
}