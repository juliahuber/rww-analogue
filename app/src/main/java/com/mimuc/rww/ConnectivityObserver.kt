package com.mimuc.rww

import kotlinx.coroutines.flow.Flow

interface ConnectivityObserver {

    fun observe(): Flow<ConnectivityObserver.Status>

    enum class Status {
        Available, Unavailable, Losing, Lost, Changed
    }

}