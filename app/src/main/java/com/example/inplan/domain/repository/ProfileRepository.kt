package com.example.inplan.domain.repository

import com.example.inplan.data.model.Profile

interface ProfileRepository {
    suspend fun getProfiles(userIds: List<String>): List<Profile>
    suspend fun updateUpiId(userId: String, upiId: String): Result<Unit>
}