package com.example.projeto_ttc2.background

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import com.example.projeto_ttc2.R
import com.example.projeto_ttc2.database.entities.BatimentoCardiaco
import com.example.projeto_ttc2.database.entities.OxigenacaoSanguinea
import com.example.projeto_ttc2.database.entities.Passos
import com.example.projeto_ttc2.database.entities.RingAccelerometerData
import com.example.projeto_ttc2.database.repository.BleSensorDataRepository
import com.example.projeto_ttc2.database.repository.BleSensorPreferencesRepository
import com.example.projeto_ttc2.database.repository.HeartRateRepository
import com.example.projeto_ttc2.database.repository.OxygenSaturationRepository
import com.example.projeto_ttc2.database.repository.StepsRepository
import com.example.projeto_ttc2.presentation.MainActivity
import com.example.projeto_ttc2.sensors.ColmiRingConnector
import com.example.projeto_ttc2.sensors.Command
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import kotlin.math.sqrt

@AndroidEntryPoint
class BleMonitoringService : Service() {

    @Inject
    lateinit var heartRateRepository: HeartRateRepository
    @Inject
    lateinit var stepsRepository: StepsRepository
    @Inject
    lateinit var oxygenSaturationRepository: OxygenSaturationRepository
    @Inject
    lateinit var authRepository: com.example.projeto_ttc2.database.repository.AuthRepository
    @Inject
    lateinit var bleSensorPreferencesRepository: BleSensorPreferencesRepository
    @Inject
    lateinit var bleSensorDataRepository: BleSensorDataRepository

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var commandJob: Job? = null
    private var dataCollectorJob: Job? = null

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bleScanner: BleScanner
    private var colmiRingConnector: ColmiRingConnector? = null
    private var isScanning = false

    private var fallDetectionJob: Job? = null
    private var potentialFallTimestamp: Long = 0
    private val FREE_FALL_THRESHOLD = 2.5
    private val IMPACT_THRESHOLD = 30.0
    private val FALL_CONFIRMATION_WINDOW = 1000L

    companion object {
        const val ACTION_START_BLE_MONITORING = "ACTION_START_BLE_MONITORING"
        const val ACTION_STOP_BLE_MONITORING = "ACTION_STOP_BLE_MONITORING"
        private const val NOTIFICATION_ID = 5
        private const val CHANNEL_ID = "BleMonitoringChannel"
        private const val TAG = "BleMonitoringService"
    }

    override fun onCreate() {
        super.onCreate()
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        bleScanner = BleScanner(bluetoothAdapter, this::onScanResultReceived)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_BLE_MONITORING -> startBleMonitoring()
            ACTION_STOP_BLE_MONITORING -> stopBleMonitoring()
        }
        return START_STICKY
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    private fun startBleMonitoring() {
        val notification = createNotification().build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        if (!bluetoothAdapter.isEnabled || checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            stopSelf()
            return
        }

        if (!isScanning && colmiRingConnector?.isConnected != true) {
            isScanning = true
            bleScanner.startScan()
            Log.d(TAG, "A iniciar scan BLE.")
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun onScanResultReceived(result: ScanResult) {
        if (isScanning) {
            isScanning = false
            bleScanner.stopScan()
            Log.d(TAG, "Dispositivo encontrado: ${result.device.name}. A tentar conectar...")

            colmiRingConnector = ColmiRingConnector(applicationContext, result.device)
            colmiRingConnector?.connect()

            startDataJobs()
        }
    }

    private fun startDataJobs() {
        val connector = colmiRingConnector ?: return

        dataCollectorJob?.cancel()
        commandJob?.cancel()

        dataCollectorJob = serviceScope.launch {
            launch { startHeartRateCollector(connector) }
            launch { startAccelerometerCollector(connector) }
            launch { startSpo2Collector(connector) }
            launch { startStepsCollector(connector) }
            launch { startStressCollector(connector) }
            launch { startBatteryCollector(connector) }
            launch { startFallDetectionCollector() }
        }

        commandJob = serviceScope.launch {
            connector.connectionStatus.filter { it }.first()
            Log.d(TAG, "Conexão pronta. A iniciar ciclo de comandos.")

            if (bleSensorPreferencesRepository.accelerometerEnabled.value) {
                connector.sendCommand(Command.ENABLE_ALL_RAW_DATA)
            } else {
                connector.sendCommand(Command.DISABLE_ALL_RAW_DATA)
            }
            delay(200)

            var lastHrRequestTime = 0L
            var lastSpo2RequestTime = 0L
            var lastGeneralRequestTime = 0L

            while (isActive) {
                val currentTime = System.currentTimeMillis()

                val hrInterval = bleSensorPreferencesRepository.heartRateInterval.value * 1000L
                val spo2Interval = bleSensorPreferencesRepository.spo2Interval.value * 1000L

                if (bleSensorPreferencesRepository.heartRateEnabled.value && (currentTime - lastHrRequestTime) >= hrInterval) {
                    connector.sendCommand(Command.REQUEST_HEART_RATE)
                    lastHrRequestTime = currentTime
                    delay(200)
                }

                if (bleSensorPreferencesRepository.spo2Enabled.value && (currentTime - lastSpo2RequestTime) >= spo2Interval) {
                    connector.sendCommand(Command.REQUEST_SPO2)
                    lastSpo2RequestTime = currentTime
                    delay(200)
                }

                if ((currentTime - lastGeneralRequestTime) >= 30000) { // General data every 30 seconds
                    if (bleSensorPreferencesRepository.stepsGeneralEnabled.value) {
                        connector.sendCommand(Command.SYNC_HISTORICAL_STEPS)
                        delay(200)
                    }
                    connector.sendCommand(Command.GET_BATTERY_STATE)
                    lastGeneralRequestTime = currentTime
                }

                delay(1000)
            }
        }
    }

    private fun CoroutineScope.startStressCollector(connector: ColmiRingConnector) = launch {
        connector.stressData.collect { stress ->
            bleSensorDataRepository.updateStress(stress)
        }
    }

    private fun CoroutineScope.startBatteryCollector(connector: ColmiRingConnector) = launch {
        connector.batteryData.collect { (level, isCharging) ->
            bleSensorDataRepository.updateBattery(level, isCharging)
        }
    }

    private fun CoroutineScope.startHeartRateCollector(connector: ColmiRingConnector) = launch {
        connector.heartRateData.collect { bpm ->
            val userId = getUserId()
            val heartRateObject = BatimentoCardiaco(System.currentTimeMillis(), "BLE_${UUID.randomUUID()}", bpm.toLong(), userId = userId ?: "")
            bleSensorDataRepository.updateHeartRate(heartRateObject)
        }
    }

    private fun CoroutineScope.startAccelerometerCollector(connector: ColmiRingConnector) = launch {
        connector.accelerometerData.collect { (x, y, z) ->
            bleSensorDataRepository.updateRingAccelerometerData(x, y, z)
        }
    }

    private fun CoroutineScope.startSpo2Collector(connector: ColmiRingConnector) = launch {
        connector.spO2PercentageData.collect { spo2 ->
            val userId = getUserId()
            val spo2Object = OxigenacaoSanguinea(System.currentTimeMillis(), "BLE_${UUID.randomUUID()}", spo2.toDouble(), userId = userId ?: "")
            bleSensorDataRepository.updateSpo2(spo2Object)
        }
    }

    private fun CoroutineScope.startStepsCollector(connector: ColmiRingConnector) = launch {
        connector.stepsCaloriesDistanceData.collect { (steps, _, _) ->
            val userId = getUserId()
            val passos = Passos(LocalDate.now().toString(), steps.toLong(), userId = userId ?: "")
            stepsRepository.upsertStepsFromBle(passos)
        }
    }

    private fun startFallDetectionCollector() {
        fallDetectionJob?.cancel()
        fallDetectionJob = serviceScope.launch {
            bleSensorDataRepository.newRingAccelerometerReading.collectLatest { reading ->
                analyzeMovementForFall(reading)
            }
        }
    }

    private fun analyzeMovementForFall(data: RingAccelerometerData) {
        val magnitude = sqrt(data.x * data.x + data.y * data.y + data.z * data.z)
        val currentTime = System.currentTimeMillis()

        if (magnitude < FREE_FALL_THRESHOLD) {
            potentialFallTimestamp = currentTime
        }

        if (potentialFallTimestamp > 0 && (currentTime - potentialFallTimestamp) < FALL_CONFIRMATION_WINDOW) {
            if (magnitude > IMPACT_THRESHOLD) {
                triggerFallAlertUI()
                potentialFallTimestamp = 0
            }
        }

        if (currentTime - potentialFallTimestamp > FALL_CONFIRMATION_WINDOW) {
            potentialFallTimestamp = 0
        }
    }

    private fun triggerFallAlertUI() {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("SHOW_FALL_DIALOG", true)
        }
        startActivity(intent)
    }

    private suspend fun getUserId(): String? {
        return authRepository.getCurrentUserFlow().first()?.uid
    }

    private fun stopBleMonitoring() {
        Log.d(TAG, "A parar o monitoramento BLE.")
        commandJob?.cancel()
        dataCollectorJob?.cancel()
        serviceScope.launch {
            colmiRingConnector?.sendCommand(Command.DISABLE_ALL_RAW_DATA)
            delay(500)
            colmiRingConnector?.disconnect()
        }
        if(isScanning) {
            bleScanner.stopScan()
            isScanning = false
        }
        stopForeground(true)
        stopSelf()
    }

    private fun createNotification(): NotificationCompat.Builder {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Monitoramento de Anel BLE",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Monitoramento do Anel Ativo")
            .setContentText("A conectar e a coletar dados do seu anel Colmi.")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        commandJob?.cancel()
        dataCollectorJob?.cancel()
        serviceScope.cancel()
        colmiRingConnector?.disconnect()
        bleScanner.stopScan()
        Log.d(TAG, "Serviço BLE destruído.")
    }
}