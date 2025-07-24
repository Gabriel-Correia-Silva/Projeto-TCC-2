package com.example.projeto_ttc2.sensors

import android.Manifest
import android.bluetooth.*
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

enum class Uuid(val str128: String) {
    CMD_SERVICE("6e40fff0-b5a3-f393-e0a9-e50e24dcca9e"),
    CMD_WRITE_CHAR("6e400002-b5a3-f393-e0a9-e50e24dcca9e"),
    CMD_NOTIFY_CHAR("6e400003-b5a3-f393-e0a9-e50e24dcca9e"),
    FW_SERVICE("de5bf728-d711-4e47-af26-65e3012a5dc7"),
    FW_WRITE_CHAR("de5bf72a-d711-4e47-af26-65e3012a5dc7"),
    FW_NOTIFY_CHAR("de5bf729-d711-4e47-af26-65e3012a5dc7");

    fun toUUID(): UUID = UUID.fromString(str128)
}

enum class Command(val hex: String) {
    SET_DATE_TIME("01"),
    ENABLE_WAVE_GESTURE("0204"),
    WAITING_FOR_WAVE_GESTURE("0205"),
    DISABLE_WAVE_GESTURE("0206"),
    GET_BATTERY_STATE("03"),
    SET_PHONE_NAME("04"),
    KEEP_ALIVE("39"),
    REBOOT("08"),
    SET_UNITS_METRIC("0a0200"),
    SET_UNITS_IMPERIAL("0a0201"),
    BLINK_TWICE("10"),
    SYNC_HISTORICAL_HEART_RATE("15"),
    SET_HEART_RATE_MONITORING_INTERVAL("160201"),
    DISABLE_SPO2_MONITORING("2c0200"),
    ENABLE_SPO2_MONITORING("2c0201"),
    DISABLE_STRESS_MONITORING("360200"),
    ENABLE_STRESS_MONITORING("360201"),
    SYNC_HISTORICAL_STRESS("37"),
    SYNC_HISTORICAL_STEPS("43"),
    GREEN_LIGHT_10_SEC("5055aa"),
    REQUEST_HEART_RATE("6901"),
    REQUEST_SPO2("6903"),
    REQUEST_STRESS("6908"),
    DISABLE_ALL_RAW_DATA("a102"),
    GET_ALL_RAW_DATA("a103"),
    ENABLE_ALL_RAW_DATA("a104"),
    SYNC_HISTORICAL_SLEEP("bc27"),
    SYNC_HISTORICAL_SPO2("bc2a"),
    RESET_DEFAULTS("ff");

    val bytes: ByteArray by lazy { hexStringToCmdBytes(hex) }
}

enum class Notification(val code: Int) {
    DATETIME(0x01),
    WAVE_GESTURE(0x02),
    BATTERY(0x03),
    PHONE_NAME(0x04),
    UNITS_PREFERENCE(0x0a),
    BLINK_TWICE(0x10),
    HEART_RATE_MONITORING_INTERVAL(0x16),
    SPO2_MONITORING_PREFERENCE(0x2c),
    UNKNOWN(0x2f),
    STRESS_MONITORING_PREFERENCE(0x36),
    GREEN_LIGHT_10_SEC(0x50),
    HEART_SPO2_STRESS(0x69),
    GENERAL(0x73),
    RAW_SENSOR(0xa1);

    companion object {
        fun fromCode(code: Int) = values().find { it.code == code }
    }
}

enum class GeneralSubtype(val code: Int) {
    HEART_RATE_SYNC_REQUIRED(0x01),
    SINGLE_BP_SYNC(0x02),
    SPO2_SYNC_REQUIRED(0x03),
    SINGLE_STEP_DETAIL_SYNC(0x04),
    TEMPERATURE(0x05),
    SYNC_TODAY_SPORT(0x06),
    SPORT_ENDED(0x07),
    TARGET_SETTING_RESPONSE(0x10),
    BATTERY(0x0c),
    BLOOD_SUGAR(0x0d),
    STEPS_CALORIES_DISTANCE(0x12);

    companion object {
        fun fromCode(code: Int) = values().find { it.code == code }
    }
}

enum class RawSensorSubtype(val code: Int) {
    SPO2(0x01),
    PPG(0x02),
    ACCELEROMETER(0x03);

    companion object {
        fun fromCode(code: Int) = values().find { it.code == code }
    }
}

enum class HeartSpO2StressSubtype(val code: Int) {
    HEART_RATE(0x01),
    SPO2(0x03),
    STRESS(0x08);

    companion object {
        fun fromCode(code: Int) = values().find { it.code == code }
    }
}

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
            val deviceAddress = gatt?.device?.address
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        Log.d("ColmiRingConnector", "SUCCESS: Conectado ao dispositivo $deviceAddress")
                        isConnected = true
                        bluetoothGatt = gatt
                        gatt?.discoverServices()
                    } else {
                        Log.e("ColmiRingConnector", "ERROR: Falha na conexão com $deviceAddress. Status: $status")
                        isConnected = false
                        isReady = false
                        _connectionStatus.tryEmit(false)
                        cleanup()
                    }
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.d("ColmiRingConnector", "INFO: Desconectado do dispositivo $deviceAddress. Status: $status")
                    isConnected = false
                    isReady = false
                    connectionTimeoutJob?.cancel()
                    _connectionStatus.tryEmit(false)
                    cleanup()
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("ColmiRingConnector", "SUCCESS: Serviços descobertos para ${gatt?.device?.address}")
                val service = gatt?.getService(Uuid.CMD_SERVICE.toUUID())
                service?.let {
                    cmdWriteCharacteristic = it.getCharacteristic(Uuid.CMD_WRITE_CHAR.toUUID())
                    cmdNotifyCharacteristic = it.getCharacteristic(Uuid.CMD_NOTIFY_CHAR.toUUID())
                    cmdNotifyCharacteristic?.let { notifyChar ->
                        gatt.setCharacteristicNotification(notifyChar, true)
                        val descriptor = notifyChar.getDescriptor(CCCD_UUID)
                        descriptor?.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        gatt.writeDescriptor(descriptor)
                    } ?: run {
                        Log.e("ColmiRingConnector", "ERROR: Característica de notificação não encontrada.")
                        isConnected = false
                        isReady = false
                        _connectionStatus.tryEmit(false)
                        cleanup()
                    }
                } ?: run {
                    Log.e("ColmiRingConnector", "ERROR: Serviço de comando não encontrado.")
                    isConnected = false
                    isReady = false
                    _connectionStatus.tryEmit(false)
                    cleanup()
                }
            } else {
                Log.e("ColmiRingConnector", "ERROR: Falha na descoberta de serviços. Status: $status")
                isConnected = false
                isReady = false
                _connectionStatus.tryEmit(false)
                cleanup()
            }
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e("ColmiRing-WRITE", "ERROR: Falha na escrita da característica ${characteristic?.uuid}: $status")
            }
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
            super.onDescriptorWrite(gatt, descriptor, status)
            if (status == BluetoothGatt.GATT_SUCCESS && descriptor?.uuid == CCCD_UUID) {
                Log.d("ColmiRingConnector", "SUCCESS: Descritor escrito. O dispositivo está pronto.")
                connectionTimeoutJob?.cancel()
                isReady = true
                _connectionStatus.tryEmit(true)
            } else {
                Log.e("ColmiRingConnector", "ERROR: Falha ao escrever descritor. Status: $status")
                isReady = false
                _connectionStatus.tryEmit(false)
                cleanup()
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            super.onCharacteristicChanged(gatt, characteristic)
            characteristic?.let {
                if (it.uuid == Uuid.CMD_NOTIFY_CHAR.toUUID()) {
                    handleNotificationData(it.value)
                }
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun connect() {
        if (isConnected) {
            Log.w("ColmiRingConnector", "WARN: Já conectado, a ignorar nova tentativa de conexão.")
            return
        }
        Log.d("ColmiRingConnector", "INFO: A iniciar conexão com ${device.name} (${device.address})")
        bluetoothGatt = device.connectGatt(context, false, gattCallback)
        connectionTimeoutJob = connectorScope.launch {
            delay(10000)
            if (!isReady) {
                Log.e("ColmiRingConnector", "ERROR: Timeout na conexão. O dispositivo não ficou pronto a tempo.")
                disconnect()
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun disconnect() {
        Log.d("ColmiRingConnector", "INFO: A chamar disconnect no GATT.")
        connectionTimeoutJob?.cancel()
        bluetoothGatt?.disconnect()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun cleanup() {
        Log.d("ColmiRingConnector", "INFO: A executar cleanup: fechar GATT e limpar recursos.")
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
            Log.e("ColmiRingConnector", "ERROR: Dispositivo não está pronto para enviar comandos.")
            return false
        }
        val gatt = bluetoothGatt
        val writeChar = cmdWriteCharacteristic
        if (gatt == null || writeChar == null) {
            Log.e("ColmiRingConnector", "ERROR: GATT ou Característica de escrita nulos.")
            return false
        }
        writeChar.value = commandBytes
        return gatt.writeCharacteristic(writeChar)
    }

    fun isReadyToSendCommands(): Boolean = isConnected && isReady

    fun sendCommand(command: Command): Boolean {
        return sendCommand(command.bytes)
    }

    private fun handleNotificationData(data: ByteArray) {
        if (data.size != 16) return

        val notificationType = Notification.fromCode(data[0].toInt() and 0xFF)
        val subType = data[1].toInt() and 0xFF

        when (notificationType) {
            Notification.BATTERY -> {
                val (level, isCharging) = parseBatteryData(data)
                _batteryData.tryEmit(Pair(level, isCharging))
            }
            Notification.GENERAL -> {
                when (GeneralSubtype.fromCode(subType)) {
                    GeneralSubtype.STEPS_CALORIES_DISTANCE -> {
                        val (steps, calories, distance) = parseNotifStepsCaloriesDistanceData(data)
                        _stepsCaloriesDistanceData.tryEmit(Triple(steps, calories, distance))
                    }
                    GeneralSubtype.BATTERY -> {
                        val (level, isCharging) = parseNotifBatteryData(data)
                        _batteryData.tryEmit(Pair(level, isCharging))
                    }
                    else -> {}
                }
            }
            Notification.RAW_SENSOR -> {
                when (RawSensorSubtype.fromCode(subType)) {
                    RawSensorSubtype.SPO2 -> {
                        val (raw, a, b, c) = parseRawSpO2SensorData(data)
                        _rawSpO2Data.tryEmit(Quadruple(raw, a, b, c))
                    }
                    RawSensorSubtype.PPG -> {
                        val (raw, max, min, diff) = parseRawPpgSensorData(data)
                        _rawPpgData.tryEmit(Quadruple(raw, max, min, diff))
                    }
                    RawSensorSubtype.ACCELEROMETER -> {
                        val (accelTriple, _) = parseRawAccelerometerSensorData(data)
                        _accelerometerData.tryEmit(accelTriple)
                    }
                    else -> {}
                }
            }
            Notification.HEART_SPO2_STRESS -> {
                when (HeartSpO2StressSubtype.fromCode(subType)) {
                    HeartSpO2StressSubtype.HEART_RATE -> {
                        val heartRate = data[3].toInt() and 0xFF
                        if (heartRate > 0) _heartRateData.tryEmit(heartRate)
                    }
                    HeartSpO2StressSubtype.SPO2 -> {
                        val spo2 = data[3].toInt() and 0xFF
                        if (spo2 > 0) _spO2PercentageData.tryEmit(spo2)
                    }
                    HeartSpO2StressSubtype.STRESS -> {
                        val stress = data[3].toInt() and 0xFF
                        if (stress > 0) _stressData.tryEmit(stress)
                    }
                    else -> {}
                }
            }
            else -> {}
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
        val rawX = ((data[6].toInt() and 0xFF shl 4) or ((data[7].toInt() and 0xF0) shr 4)).toSigned(12) - bias.first
        val rawY = ((data[2].toInt() and 0xFF shl 4) or ((data[3].toInt() and 0xF0) shr 4)).toSigned(12) - bias.second
        val rawZ = (((data[4].toInt() and 0x0F) shl 8) or (data[5].toInt() and 0xFF)).toSigned(12) - bias.third

        val countsPerG = 2048f / 4f
        val mps2PerG  = 9.80665f
        val scale     = mps2PerG / countsPerG

        val accelX = rawX * scale
        val accelY = rawY * scale
        val accelZ = rawZ * scale

        val magnitude = sqrt(accelX * accelX + accelY * accelY + accelZ * accelZ)
        return Pair(Triple(accelX, accelY, accelZ), magnitude)
    }
}