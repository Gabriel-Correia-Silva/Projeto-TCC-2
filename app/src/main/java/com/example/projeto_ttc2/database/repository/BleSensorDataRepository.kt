package com.example.projeto_ttc2.database.repository

import com.example.projeto_ttc2.database.entities.AccelerometerData
import com.example.projeto_ttc2.database.entities.BatimentoCardiaco
import com.example.projeto_ttc2.database.entities.OxigenacaoSanguinea
import com.example.projeto_ttc2.database.entities.RingAccelerometerData
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BleSensorDataRepository @Inject constructor() {

    private val _latestBleAccelerometerData = MutableStateFlow(RingAccelerometerData(x = 0f, y = 0f, z = 0f))
    val latestBleAccelerometerData: StateFlow<RingAccelerometerData> = _latestBleAccelerometerData

    private val _latestHeartRate = MutableStateFlow(0L)
    val latestHeartRate: StateFlow<Long> = _latestHeartRate

    private val _latestSpo2 = MutableStateFlow(0.0)
    val latestSpo2: StateFlow<Double> = _latestSpo2


    private val _newRingAccelerometerReading = MutableSharedFlow<RingAccelerometerData>()
    val newRingAccelerometerReading: SharedFlow<RingAccelerometerData> = _newRingAccelerometerReading


    private val _bufferedRingAccelerometerData = MutableStateFlow<MutableList<RingAccelerometerData>>(mutableListOf())
    val bufferedRingAccelerometerData: StateFlow<MutableList<RingAccelerometerData>> = _bufferedRingAccelerometerData

    private val _bufferedHeartRateData = MutableStateFlow<MutableList<BatimentoCardiaco>>(mutableListOf())
    val bufferedHeartRateData: StateFlow<MutableList<BatimentoCardiaco>> = _bufferedHeartRateData

    private val _bufferedSpo2Data = MutableStateFlow<MutableList<OxigenacaoSanguinea>>(mutableListOf())
    val bufferedSpo2Data: StateFlow<MutableList<OxigenacaoSanguinea>> = _bufferedSpo2Data

    data class BleRawSpO2Data(val timestamp: Long, val raw: Int, val a: Int, val b: Int, val c: Int)
    private val _bufferedRawSpO2Data = MutableStateFlow<MutableList<BleRawSpO2Data>>(mutableListOf())
    val bufferedRawSpO2Data: StateFlow<MutableList<BleRawSpO2Data>> = _bufferedRawSpO2Data

    data class BleRawPpgData(val timestamp: Long, val raw: Int, val max: Int, val min: Int, val diff: Int)
    private val _bufferedRawPpgData = MutableStateFlow<MutableList<BleRawPpgData>>(mutableListOf())
    val bufferedRawPpgData: StateFlow<MutableList<BleRawPpgData>> = _bufferedRawPpgData


    fun updateRingAccelerometerData(x: Float, y: Float, z: Float) {
        val newReading = RingAccelerometerData(x = x, y = y, z = z, timestamp = System.currentTimeMillis())
        _latestBleAccelerometerData.value = newReading
        _bufferedRingAccelerometerData.value.add(newReading)

        _newRingAccelerometerReading.tryEmit(newReading)
    }

    fun getAndClearBufferedRingAccelerometerData(): List<RingAccelerometerData> {
        val currentBuffer = _bufferedRingAccelerometerData.value
        _bufferedRingAccelerometerData.value = mutableListOf()
        return currentBuffer
    }

    fun updateHeartRate(heartRateObject: BatimentoCardiaco) {
        _latestHeartRate.value = heartRateObject.bpm
        _bufferedHeartRateData.value.add(heartRateObject)
    }

    fun getAndClearBufferedHeartRateData(): List<BatimentoCardiaco> {
        val currentBuffer = _bufferedHeartRateData.value
        _bufferedHeartRateData.value = mutableListOf()
        return currentBuffer
    }

    fun updateSpo2(spo2Object: OxigenacaoSanguinea) {
        _latestSpo2.value = spo2Object.spo2
        _bufferedSpo2Data.value.add(spo2Object)
    }

    fun getAndClearBufferedSpo2Data(): List<OxigenacaoSanguinea> {
        val currentBuffer = _bufferedSpo2Data.value
        _bufferedSpo2Data.value = mutableListOf()
        return currentBuffer
    }

    fun updateRawSpO2Data(raw: Int, a: Int, b: Int, c: Int) {
        val newReading = BleRawSpO2Data(timestamp = System.currentTimeMillis(), raw = raw, a = a, b = b, c = c)
        _bufferedRawSpO2Data.value.add(newReading)
    }

    fun getAndClearBufferedRawSpO2Data(): List<BleRawSpO2Data> {
        val currentBuffer = _bufferedRawSpO2Data.value
        _bufferedRawSpO2Data.value = mutableListOf()
        return currentBuffer
    }

    fun updateRawPpgData(raw: Int, max: Int, min: Int, diff: Int) {
        val newReading = BleRawPpgData(timestamp = System.currentTimeMillis(), raw = raw, max = max, min = min, diff = diff)
        _bufferedRawPpgData.value.add(newReading)
    }

    fun getAndClearBufferedRawPpgData(): List<BleRawPpgData> {
        val currentBuffer = _bufferedRawPpgData.value
        _bufferedRawPpgData.value = mutableListOf()
        return currentBuffer
    }
}