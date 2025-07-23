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
import com.example.projeto_ttc2.sensors.hexStringToCmdBytes
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

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bleScanner: BleScanner
    private var colmiRingConnector: ColmiRingConnector? = null
    private var isScanning = false
    private var isConnected = false

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
        Log.d(TAG, "onCreate do BleMonitoringService. Inicializando adaptadores.")
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        bleScanner = BleScanner(bluetoothAdapter, this::onScanResultReceived)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand. Ação: ${intent?.action}")
        when (intent?.action) {
            ACTION_START_BLE_MONITORING -> startBleMonitoring()
            ACTION_STOP_BLE_MONITORING -> stopBleMonitoring()
            else -> {
                Log.d(TAG, "Serviço já em execução ou ação desconhecida. Reconfigurando coleta de dados se conectado.")
                if (isConnected) {
                    colmiRingConnector?.let {
                        serviceScope.launch {
                            Log.d(TAG, "Reconfigurando coleta de dados baseada nas preferências atualizadas.")
                            it.sendCommand(hexStringToCmdBytes("a102"))
                            delay(200)
                        }
                    }
                }
            }
        }
        return START_STICKY
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    private fun startBleMonitoring() {
        Log.d(TAG, "startBleMonitoring() chamado.")
        val notification = createNotification().build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        if (!bluetoothAdapter.isEnabled) {
            Log.e(TAG, "Bluetooth não está habilitado. Parando o serviço.")
            stopSelf()
            return
        }

        if (checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Permissão BLUETOOTH_SCAN não concedida. Parando o serviço.")
            stopSelf()
            return
        }

        if (!isScanning) {
            bleScanner.startScan()
            isScanning = true
            Log.d(TAG, "Iniciando scan BLE.")
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun onScanResultReceived(result: ScanResult) {
        Log.d(TAG, "Anel Colmi R06 encontrado: ${result.device.name}. MAC: ${result.device.address}")
        bleScanner.stopScan()
        isScanning = false

        colmiRingConnector = ColmiRingConnector(applicationContext, result.device)
        colmiRingConnector?.connect()
        isConnected = true
        Log.d(TAG, "Tentando conectar ao anel Colmi R06.")

        startCollectingAndSendingCommands()
    }

    private fun startCollectingAndSendingCommands() {
        colmiRingConnector?.let { connector ->
            Log.d(TAG, "Iniciando coletores de dados e lógica de comandos.")


            startFallDetectionCollector()


            serviceScope.launch {
                connector.heartRateData
                    .combine(bleSensorPreferencesRepository.heartRateEnabled) { bpm, enabled -> Pair(bpm, enabled) }
                    .filter { it.second }
                    .collect { (bpm, _) ->
                        val userId = getUserId()
                        val heartRateObject = BatimentoCardiaco(
                            timestamp = System.currentTimeMillis(),
                            healthConnectId = "BLE_${UUID.randomUUID()}",
                            bpm = bpm.toLong(),
                            userId = userId ?: ""
                        )
                        bleSensorDataRepository.updateHeartRate(heartRateObject)
                    }
            }



        } ?: Log.e(TAG, "ColmiRingConnector não inicializado.")
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
            Log.d(TAG, "Queda Livre Potencial Detectada! Magnitude: $magnitude")
        }

        if (potentialFallTimestamp > 0 && (currentTime - potentialFallTimestamp) < FALL_CONFIRMATION_WINDOW) {
            if (magnitude > IMPACT_THRESHOLD) {
                Log.d(TAG, "Impacto Detectado! Magnitude: $magnitude. QUEDA CONFIRMADA!")
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
        Log.d(TAG, "Parando monitoramento BLE.")
        serviceScope.launch {
            colmiRingConnector?.sendCommand(hexStringToCmdBytes("a102")) 
            delay(500)
            colmiRingConnector?.disconnect()
        }
        bleScanner.stopScan()
        isScanning = false
        isConnected = false
        serviceScope.cancel()
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
            .setContentText("Coletando dados do seu anel Colmi R06.")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        colmiRingConnector?.disconnect()
        bleScanner.stopScan()
        Log.d(TAG, "Serviço BLE destruído.")
    }
}