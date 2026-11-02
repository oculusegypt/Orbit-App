package com.example.di

import android.content.Context
import com.example.data.local.OrbitDatabase
import com.example.data.repository.OrbitRepository
import com.example.data.repository.OrbitRoomRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt DI graph for Orbit Learn.
 *
 * Replaces the former service-locator pattern (static `getInstance` / `getDatabase`
 * singletons) with constructor injection, while preserving the exact same instances:
 * both providers delegate to the existing idempotent accessors, so any legacy call
 * site and any injected consumer always share ONE database and ONE catalog instance.
 *
 * This keeps the migration zero-risk: no behavior changes, only ownership changes —
 * the object graph is now declared in one place and can be swapped in tests via
 * `@UninstallModules` or a test module override.
 */
@Module
@InstallIn(SingletonComponent::class)
object OrbitModule {

    @Provides
    @Singleton
    fun provideOrbitDatabase(@ApplicationContext context: Context): OrbitDatabase =
        OrbitDatabase.getDatabase(context)

    @Provides
    @Singleton
    fun provideOrbitRoomRepository(
        @ApplicationContext context: Context,
        database: OrbitDatabase
    ): OrbitRoomRepository = OrbitRoomRepository.getInstance(context)

    @Provides
    @Singleton
    fun provideOrbitCatalogRepository(): OrbitRepository = OrbitRepository
}
