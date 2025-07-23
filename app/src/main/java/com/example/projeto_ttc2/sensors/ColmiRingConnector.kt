package com.example.projeto_ttc2.sensors

import android.Manifest
import android.bluetooth.*
import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.sqrt


data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)


fun Int.toSigned(numBits: Int): Int {
    val maxVal = 1 shl (numBits - 1)
    val mask = (1 shl numBits) - 1
    val value = this and mask
    return if (value >= maxVal) value - (1 shl numBits) else value
}


fun hexStringToCmdBytes(hexString: String): ByteArray {
    require(hexString.length <= 30 && hexString.length % 2 == 0) { "hex string must be an even number of hex digits [0-f] less than or equal to 30 chars" }
    val bytes = ByteArray(16) { 0 }
    for (i in 0 until hexString.length / 2) {
        bytes[i] = hexString.substring(2 * i, 2 * i + 2).toInt(16).toByte()
    }

    bytes[15] = bytes.foldIndexed(0) { index, previous, current -> if (index < 15) previous + (current.toInt() and 0xFF) else previous }.toByte()
    return bytes
}


class ColmiRingConnector(private val context: Context, private val device: BluetoothDevice) {

    private var bluetoothGatt: BluetoothGatt? = null
    private var cmdWriteCharacteristic: BluetoothGattCharacteristic? = null
    private var cmdNotifyCharacteristic: BluetoothGattCharacteristic? = null




    private val CMD_SERVICE_UUID = UUID.fromString("6e40fff0-b5a3-f393-e0a9-e50e24dcca9e")
    private val CMD_WRITE_CHAR_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e")
    private val CMD_NOTIFY_CHAR_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e")
    private val CCCD_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")


    var isConnected: Boolean = false
        private set
    var isReady: Boolean = false
        private set


    private val _connectionStatus = MutableSharedFlow<Boolean>(replay = 1)
    val connectionStatus: SharedFlow<Boolean> = _connectionStatus


    private var connectionTimeoutJob: Job? = null
    private val connectorScope = CoroutineScope(Dispatchers.IO)


    private val _batteryData = MutableSharedFlow<Pair<Int, Boolean>>(replay = 1)
    val batteryData: SharedFlow<Pair<Int, Boolean>> = _batteryData

    private val _stepsCaloriesDistanceData = MutableSharedFlow<Triple<Int, Int, Int>>(replay = 1)
    val stepsCaloriesDistanceData: SharedFlow<Triple<Int, Int, Int>> = _stepsCaloriesDistanceData

    private val _heartRateData = MutableSharedFlow<Int>(replay = 1)
    val heartRateData: SharedFlow<Int> = _heartRateData

    private val _spO2PercentageData = MutableSharedFlow<Int>(replay = 1)
    val spO2PercentageData: SharedFlow<Int> = _spO2PercentageData

    private val _stressData = MutableSharedFlow<Int>(replay = 1)
    val stressData: SharedFlow<Int> = _stressData

    private val _accelerometerData = MutableSharedFlow<Triple<Float, Float, Float>>(replay = 2)
    val accelerometerData: SharedFlow<Triple<Float, Float, Float>> = _accelerometerData


    private val _rawSpO2Data = MutableSharedFlow<Quadruple<Int, Int, Int, Int>>(replay = 2)
    val rawSpO2Data: SharedFlow<Quadruple<Int, Int, Int, Int>> = _rawSpO2Data

    private val _rawPpgData = MutableSharedFlow<Quadruple<Int, Int, Int, Int>>(replay = 2)
    val rawPpgData: SharedFlow<Quadruple<Int, Int, Int, Int>> = _rawPpgData


    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)

            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        Log.d("ColmiRing", "Dispositivo conectado com sucesso. Descobrindo serviços...")
                        isConnected = true
                        bluetoothGatt = gatt
                        gatt?.discoverServices()
                    } else {
                        Log.e("ColmiRing", "Conexão falhada. Status: $status")
                        isConnected = false
                        isReady = false
                        _connectionStatus.tryEmit(false)
                        cleanup()
                    }
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.d("ColmiRing", "Dispositivo desconectado. Status: $status")
                    isConnected = false
                    isReady = false
                    connectionTimeoutJob?.cancel()
                    _connectionStatus.tryEmit(false)
                    cleanup()
                }
                else -> {
                    Log.d("ColmiRing", "Estado da conexão: $newState, Status GATT: $status")
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("ColmiRing", "Serviços descobertos com sucesso. Total de serviços: ${gatt?.services?.size}.")
                val service = gatt?.getService(CMD_SERVICE_UUID)
                service?.let {
                    Log.d("ColmiRing", "Serviço de comando (CMD_SERVICE_UUID) encontrado. UUID: ${it.uuid}")
                    cmdWriteCharacteristic = it.getCharacteristic(CMD_WRITE_CHAR_UUID)
                    cmdNotifyCharacteristic = it.getCharacteristic(CMD_NOTIFY_CHAR_UUID)

                    if (cmdWriteCharacteristic == null) {
                        Log.e("ColmiRing", "Característica de escrita (CMD_WRITE_CHAR_UUID) NÃO encontrada. UUID: $CMD_WRITE_CHAR_UUID")
                    } else {
                        Log.d("ColmiRing", "Característica de escrita (CMD_WRITE_CHAR_UUID) encontrada. UUID: ${cmdWriteCharacteristic?.uuid}")
                    }

                    cmdNotifyCharacteristic?.let { notifyChar ->
                        Log.d("ColmiRing", "Característica de notificação (CMD_NOTIFY_CHAR_UUID) encontrada. UUID: ${notifyChar.uuid}. Tentando configurar notificações...")

                        val setNotificationSuccess = gatt.setCharacteristicNotification(notifyChar, true)
                        Log.d("ColmiRing", "setCharacteristicNotification retornado: $setNotificationSuccess")

                        val descriptor = notifyChar.getDescriptor(CCCD_UUID)
                        if (descriptor == null) {
                            Log.e("ColmiRing", "Descritor CCCD para notificação NÃO encontrado. UUID: $CCCD_UUID")
                        } else {
                            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            val writeDescriptorSuccess = gatt.writeDescriptor(descriptor)
                            Log.d("ColmiRing", "Escrita do descritor de notificação iniciada. writeDescriptor retornado: $writeDescriptorSuccess. Descritor UUID: ${descriptor.uuid}")
                        }
                    } ?: run {
                        Log.e("ColmiRing", "Característica de notificação (CMD_NOTIFY_CHAR_UUID) NÃO encontrada. UUID: $CMD_NOTIFY_CHAR_UUID")
                        isConnected = false
                        isReady = false
                        _connectionStatus.tryEmit(false)
                        cleanup()
                    }
                } ?: run {
                    Log.e("ColmiRing", "Serviço de comando (CMD_SERVICE_UUID) NÃO encontrado. UUID: $CMD_SERVICE_UUID")
                    isConnected = false
                    isReady = false
                    _connectionStatus.tryEmit(false)
                    cleanup()
                }
            } else {
                Log.w("ColmiRing", "Falha na descoberta de serviços: $status")
                isConnected = false
                isReady = false
                _connectionStatus.tryEmit(false)
                cleanup()
            }
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("ColmiRing", "Escrita na característica bem-sucedida para UUID: ${characteristic?.uuid}. Valor: ${characteristic?.value?.joinToString { String.format("%02X", it) }}")
            } else {
                Log.e("ColmiRing", "Falha na escrita da característica ${characteristic?.uuid}: $status. Valor: ${characteristic?.value?.joinToString { String.format("%02X", it) }}")
            }
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
            super.onDescriptorWrite(gatt, descriptor, status)
            if (status == BluetoothGatt.GATT_SUCCESS && descriptor?.uuid == CCCD_UUID) {
                Log.d("ColmiRing", "Descritor ${descriptor?.uuid} escrito com sucesso. Status: $status. Notificações configuradas. Pronto para enviar comandos.")
                connectionTimeoutJob?.cancel()
                isReady = true
                _connectionStatus.tryEmit(true)
            } else {
                Log.e("ColmiRing", "Falha ao escrever descritor ${descriptor?.uuid}: $status")
                isReady = false
                _connectionStatus.tryEmit(false)
                cleanup()
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            super.onCharacteristicChanged(gatt, characteristic)
            characteristic?.let {
                if (it.uuid == CMD_NOTIFY_CHAR_UUID) {
                    val data = it.value
                    Log.d("ColmiRing-RAW-NOTIF", "Notificação recebida (RAW HEX): ${data.joinToString { String.format("%02X", it) }}")
                    handleNotificationData(data)
                }
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun connect() {
        if (isConnected) {
            Log.w("ColmiRing", "Já conectado, ignorando nova tentativa de conexão.")
            return
        }

        Log.d("ColmiRing", "Iniciando conexão com ${device.name} (${device.address})")
        bluetoothGatt = device.connectGatt(context, false, gattCallback)

        connectionTimeoutJob = connectorScope.launch {
            delay(10000)
            if (!isReady) {
                Log.e("ColmiRing", "Timeout na conexão: Dispositivo ${device.address} não ficou pronto a tempo.")
                disconnect()
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun disconnect() {
        Log.d("ColmiRing", "Chamando disconnect no GATT.")
        connectionTimeoutJob?.cancel()
        bluetoothGatt?.disconnect()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun cleanup() {
        Log.d("ColmiRing", "Executando cleanup: Fechando GATT e limpando recursos.")
        bluetoothGatt?.close()
        bluetoothGatt = null
        cmdWriteCharacteristic = null
        cmdNotifyCharacteristic = null
        isConnected = false
        isReady = false
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun sendCommand(commandBytes: ByteArray): Boolean {
        if (!isReadyToSendCommands()) {
            Log.e("ColmiRing", "Dispositivo não está pronto para enviar comandos. Status: conectado=$isConnected, pronto=$isReady. Comando: ${commandBytes.joinToString { String.format("%02X", it) }}")
            return false
        }
        val gatt = bluetoothGatt
        val writeChar = cmdWriteCharacteristic
        if (gatt == null || writeChar == null) {
            Log.e("ColmiRing", "GATT ou Característica de escrita nulos inesperadamente. Comando: ${commandBytes.joinToString { String.format("%02X", it) }}")
            return false
        }

        writeChar.value = commandBytes
        val writeSuccess = gatt.writeCharacteristic(writeChar)
        Log.d("ColmiRing", "Tentando enviar comando: ${commandBytes.joinToString { String.format("%02X", it) }}. Sucesso na escrita (método): $writeSuccess")
        return writeSuccess
    }

    fun isReadyToSendCommands(): Boolean = isConnected && isReady

    fun sendCommand(hexString: String): Boolean {
        return try {
            val commandBytes = hexStringToCmdBytes(hexString)
            sendCommand(commandBytes)
        } catch (e: Exception) {
            Log.e("ColmiRing", "Erro ao converter comando hex '$hexString': ${e.message}", e)
            false
        }
    }

    private fun handleNotificationData(data: ByteArray) {
        if (data.size != 16) {
            Log.e("ColmiRing", "Tamanho de mensagem inválido: ${data.size}")
            return
        }

        val firstByte = data[0].toInt() and 0xFF
        val secondByte = data[1].toInt() and 0xFF

        when (firstByte) {
            0x03 -> {
                val (level, isCharging) = parseBatteryData(data)
                _batteryData.tryEmit(Pair(level, isCharging))
                Log.d("ColmiRing", "Bateria: Nível=${level}%, Carregando=${isCharging}")
            }
            0x73 -> {
                Log.d("ColmiRing", "Tipo de Notificação: Geral (0x73)")
                when (secondByte) {
                    0x12 -> {
                        val (steps, calories, distance) = parseNotifStepsCaloriesDistanceData(data)
                        _stepsCaloriesDistanceData.tryEmit(Triple(steps, calories, distance))
                        Log.d("ColmiRing", "Passos: ${steps}, Calorias: ${calories}kcal, Distância: ${distance}m")
                    }
                    0x0C -> {
                        val (level, isCharging) = parseNotifBatteryData(data)
                        _batteryData.tryEmit(Pair(level, isCharging))
                        Log.d("ColmiRing", "Bateria (Geral): Nível=${level}%, Carregando=${isCharging}")
                    }
                    else -> Log.d("ColmiRing", "Subtipo de Notificação Geral desconhecido: 0x${secondByte.toString(16)}")
                }
            }
            0xA1 -> {
                Log.d("ColmiRing", "Tipo de Notificação: Sensor Bruto (0xA1)")
                when (secondByte) {
                    0x01 -> {
                        val (raw, a, b, c) = parseRawSpO2SensorData(data)
                        _rawSpO2Data.tryEmit(Quadruple(raw, a, b, c))
                        Log.d("ColmiRing", "SpO2 Bruto: RAW=${raw}, A=${a}, B=${b}, C=${c}")
                    }
                    0x02 -> {
                        val (raw, max, min, diff) = parseRawPpgSensorData(data)
                        _rawPpgData.tryEmit(Quadruple(raw, max, min, diff))
                        Log.d("ColmiRing", "PPG Bruto: RAW=${raw}, Max=${max}, Min=${min}, Diff=${diff}")
                    }
                    0x03 -> {
                        val (accelTriple, magnitude) = parseRawAccelerometerSensorData(data)
                        val (rawX, rawY, rawZ) = accelTriple
                        val emitted = _accelerometerData.tryEmit(Triple(rawX, rawY, rawZ))
                        Log.d(TAG, "Magnitude do vetor: $magnitude m/s²")
                        Log.d(TAG, "DEBUG_ACCEL_EMIT: Emitindo X=$rawX, Y=$rawY, Z=$rawZ. Sucesso: $emitted")
                    }
                    else -> Log.d("ColmiRing", "Subtipo de Sensor Bruto desconhecido: 0x${secondByte.toString(16)}")
                }
            }
            0x69 -> {
                Log.d("ColmiRing", "Tipo de Notificação: Frequência/SpO2/Estresse (0x69)")
                when (secondByte) {
                    0x01 -> {
                        val heartRate = data[3].toInt() and 0xFF
                        if (heartRate != 0) {
                            _heartRateData.tryEmit(heartRate)
                            Log.d("ColmiRing", "Frequência Cardíaca: ${heartRate}bpm")
                        }
                    }
                    0x03 -> {
                        val spo2Percentage = data[3].toInt() and 0xFF
                        if (spo2Percentage != 0) {
                            _spO2PercentageData.tryEmit(spo2Percentage)
                            Log.d("ColmiRing", "SpO2: ${spo2Percentage}%")
                        }
                    }
                    0x08 -> {
                        val stress = data[3].toInt() and 0xFF
                        if (stress != 0) {
                            _stressData.tryEmit(stress)
                            Log.d("ColmiRing", "Stress: ${stress}")
                        }
                    }
                    else -> Log.d("ColmiRing", "Subtipo de Frequência/SpO2/Estresse desconhecido: 0x${secondByte.toString(16)}")
                }
            }
            0x02 -> {
                Log.d("ColmiRing", "Tipo de Notificação: Gesto de Onda (0x02)")
                if (secondByte == 0x02) {
                    Log.d("ColmiRing", "Subtipo Gesto de Onda: Onda Detectada (0x02)")
                }
            }
            else -> Log.d("ColmiRing", "Tipo de Notificação desconhecido: 0x${firstByte.toString(16)}. Dados: ${data.joinToString { String.format("%02X", it) }}")
        }
    }

    private fun parseBatteryData(data: ByteArray): Pair<Int, Boolean> {
        return Pair(data[1].toInt() and 0xFF, data[2].toInt() == 1)
    }

    private fun parseNotifBatteryData(data: ByteArray): Pair<Int, Boolean> {
        return Pair(data[2].toInt() and 0xFF, data[3].toInt() == 1)
    }

    private fun parseNotifStepsCaloriesDistanceData(data: ByteArray): Triple<Int, Int, Int> {
        val steps = (data[2].toInt() and 0xFF shl 16) or (data[3].toInt() and 0xFF shl 8) or (data[4].toInt() and 0xFF)
        val calories = ((data[5].toInt() and 0xFF shl 16) or (data[6].toInt() and 0xFF shl 8) or (data[7].toInt() and 0xFF)) / 1000
        val distance = (data[8].toInt() and 0xFF shl 16) or (data[9].toInt() and 0xFF shl 8) or (data[10].toInt() and 0xFF)
        return Triple(steps, calories, distance)
    }

    private fun parseRawSpO2SensorData(data: ByteArray): Quadruple<Int, Int, Int, Int> {
        val blood = (data[2].toInt() and 0xFF shl 8) or data[3].toInt()
        val max1 = data[5].toInt() and 0xFF
        val max2 = data[7].toInt() and 0xFF
        val max3 = data[9].toInt() and 0xFF
        return Quadruple(blood, max1, max2, max3)
    }

    private fun parseRawPpgSensorData(data: ByteArray): Quadruple<Int, Int, Int, Int> {
        val raw = (data[2].toInt() and 0xFF shl 8) or data[3].toInt()
        val max = (data[4].toInt() and 0xFF shl 8) or data[5].toInt()
        val min = (data[6].toInt() and 0xFF shl 8) or data[7].toInt()
        val diff = (data[8].toInt() and 0xFF shl 8) or data[9].toInt()
        return Quadruple(raw, max, min, diff)
    }

    private fun parseRawAccelerometerSensorData(
        data: ByteArray,
        bias: Triple<Int, Int, Int> = Triple(0, 0, 0)
    ): Pair<Triple<Float, Float, Float>, Float> {
        val rawXcounts = (
                (data[2].toInt() and 0xFF shl 4) or
                        ((data[3].toInt() and 0xF0) shr 4)
                ).toSigned(12) - bias.first

        val rawYcounts = (
                ((data[3].toInt() and 0x0F) shl 8) or
                        (data[4].toInt() and 0xFF)
                ).toSigned(12) - bias.second

        val rawZcounts = (
                (data[5].toInt() and 0xFF shl 4) or
                        ((data[6].toInt() and 0xF0) shr 4)
                ).toSigned(12) - bias.third


        val countsPerG = 2048f / 2f
        val mps2PerG  = 9.80665f
        val scale     = mps2PerG / countsPerG


        val accelX = rawXcounts * scale
        val accelY = rawYcounts * scale
        val accelZ = rawZcounts * scale


        val magnitude = sqrt(accelX * accelX + accelY * accelY + accelZ * accelZ)

        return Pair(Triple(accelX, accelY, accelZ), magnitude)
    }
}