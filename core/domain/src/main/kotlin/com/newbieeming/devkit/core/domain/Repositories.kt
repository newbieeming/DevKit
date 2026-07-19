package com.newbieeming.devkit.core.domain

import com.newbieeming.devkit.core.model.AppInfo
import com.newbieeming.devkit.core.model.DeviceInfo
import com.newbieeming.devkit.core.model.MicState
import com.newbieeming.devkit.core.model.NetworkSpeedSnapshot
import com.newbieeming.devkit.core.model.RecordingFile
import com.newbieeming.devkit.core.model.TimeServerConfig
import kotlinx.coroutines.flow.Flow

// ── Repository 接口（由 :core:data 实现，:core:domain 仅持有接口）────────────

interface MicRepository {
    fun observeMicState(): Flow<MicState>
    suspend fun setMicEnabled(enabled: Boolean)
}

interface AudioRecordRepository {
    fun observeRecordings(): Flow<List<RecordingFile>>
    suspend fun deleteRecording(id: Long)
}

interface AppRepository {
    fun observeInstalledApps(): Flow<List<AppInfo>>
    suspend fun requestUninstall(packageName: String)
}

interface NetworkSpeedRepository {
    fun observeNetworkSpeed(): Flow<NetworkSpeedSnapshot>
}

interface DeviceInfoRepository {
    suspend fun getDeviceInfo(): DeviceInfo
}

interface TimeServerRepository {
    fun observeConfig(): Flow<TimeServerConfig>
    suspend fun updateConfig(config: TimeServerConfig)
    suspend fun syncTime(): Long  // 返回同步后的时间戳
}

// ── Use Case 基类 ─────────────────────────────────────────────────────────────

/** 有参数、单次执行的 Use Case */
interface UseCase<in P, out R> {
    suspend operator fun invoke(params: P): R
}

/** 无参数的 Flow Use Case */
interface FlowUseCase<out R> {
    operator fun invoke(): Flow<R>
}
