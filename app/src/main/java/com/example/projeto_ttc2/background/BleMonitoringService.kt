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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            if (checkSelfPermission(Manifest.permission.FOREGROUND_SERVICE_CONNECTED_DEVICE) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "Permissão FOREGROUND_SERVICE_CONNECTED_DEVICE não concedida. Não foi possível iniciar o serviço em primeiro lugar.")
                stopSelf()
                return
            }
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }


        if (!bluetoothAdapter.isEnabled) {
            Log.e(TAG, "Bluetooth não está habilitado. Parando o serviço.")
            stopSelf()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "Permissões BLUETOOTH_SCAN ou BLUETOOTH_CONNECT não concedidas. Parando o serviço.")
                stopSelf()
                return
            }
        } else {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "Permissão ACCESS_FINE_LOCATION não concedida. Parando o serviço.")
                stopSelf()
                return
            }
        }

        if (!isScanning) {
            serviceScope.launch {
                Log.d(TAG, "Atrasando 1 segundo antes de iniciar a varredura BLE para dar tempo ao sistema.")
                delay(1000)
                if (!isScanning) {
                    Log.d(TAG, "Iniciando scan BLE para 'R0' devices.")
                    bleScanner.startScan()
                    isScanning = true
                } else {
                    Log.d(TAG, "Scan BLE já está em andamento (iniciado durante o atraso).")
                }
            }
        } else {
            Log.d(TAG, "Scan BLE já está em andamento.")
        }
    }

    private fun onScanResultReceived(result: ScanResult) {
        Log.d(TAG, "ScanResult recebido. Anel Colmi R06 encontrado: ${result.device.name}. MAC: ${result.device.address}")
        bleScanner.stopScan()
        isScanning = false

        colmiRingConnector = ColmiRingConnector(applicationContext, result.device)
        colmiRingConnector?.connect()
        isConnected = true
        Log.d(TAG, "Tentando conectar ao anel Colmi R06 com MAC: ${result.device.address}.")

        startCollectingAndSendingCommands()
    }

    private fun startCollectingAndSendingCommands() {
        colmiRingConnector?.let { connector ->
            Log.d(TAG, "startCollectingAndSendingCommands() - Iniciando coletores e lógica de comandos. (VERSÃO DEPURADA)")

            serviceScope.launch {
                Log.d(TAG, ">>> INICIANDO COLETOR DE ACELERÔMETRO (ISOLADO E SEM FILTRO) <<<")
                connector.accelerometerData
                    .collect { (x, y, z) ->
                        Log.d(TAG, "DEBUG_ACCEL_NO_FILTER: Recebido X=${x}, Y=${y}, Z=${z} de ColmiRingConnector (SEM FILTRO DE PREFERÊNCIA).")
                        val xFloat = x.toFloat()
                        val yFloat = y.toFloat()
                        val zFloat = z.toFloat()

                        Log.d(TAG, "DEBUG_ACCEL_UPDATE_REPO: Enviando X=${xFloat}, Y=${yFloat}, Z=${zFloat} para BleSensorDataRepository.")

                        bleSensorDataRepository.updateAccelerometerData(xFloat, yFloat, zFloat)
                        val xG = x / 512.0
                        val yG = y / 512.0
                        val zG = z / 512.0
                        Log.d(TAG, "Dados do Acelerômetro do Anel: RAW(X=$x, Y=$y, Z=$z) | G-force(X=%.2fG, Y=%.2fG, Z=%.2fG)".format(xG, yG, zG))
                    }
            }


            serviceScope.launch {
                Log.d(TAG, "Iniciando rotina de envio de comandos: Atraso inicial de 4s para GATT.")
                delay(4000)

                val initialPrefsCombined = combine(
                    bleSensorPreferencesRepository.accelerometerEnabled,
                    bleSensorPreferencesRepository.heartRateEnabled,
                    bleSensorPreferencesRepository.spo2Enabled,
                    bleSensorPreferencesRepository.stressEnabled,
                    bleSensorPreferencesRepository.stepsGeneralEnabled
                ) { it }.first()

                val initialAccelEnabled = initialPrefsCombined[0] as Boolean
                val initialHeartRateEnabled = initialPrefsCombined[1] as Boolean
                val initialSpo2Enabled = initialPrefsCombined[2] as Boolean
                val initialStressEnabled = initialPrefsCombined[3] as Boolean
                val initialStepsGeneralEnabled = initialPrefsCombined[4] as Boolean

                sendCommandsBasedOnPreferences(
                    connector,
                    initialAccelEnabled,
                    initialHeartRateEnabled,
                    initialSpo2Enabled,
                    initialStressEnabled,
                    initialStepsGeneralEnabled
                )

                combine(
                    bleSensorPreferencesRepository.accelerometerEnabled,
                    bleSensorPreferencesRepository.heartRateEnabled,
                    bleSensorPreferencesRepository.spo2Enabled,
                    bleSensorPreferencesRepository.stressEnabled,
                    bleSensorPreferencesRepository.stepsGeneralEnabled
                ) { preferencesArray ->
                    val currentAccelEnabled = preferencesArray[0] as Boolean
                    val currentHeartRateEnabled = preferencesArray[1] as Boolean
                    val currentSpo2Enabled = preferencesArray[2] as Boolean
                    val currentStressEnabled = preferencesArray[3] as Boolean
                    val currentStepsGeneralEnabled = preferencesArray[4] as Boolean

                    Log.d(TAG, "Preferências de sensores BLE combinadas atualizadas (reativo). Reavaliando comandos.")
                    sendCommandsBasedOnPreferences(
                        connector,
                        currentAccelEnabled,
                        currentHeartRateEnabled,
                        currentSpo2Enabled,
                        currentStressEnabled,
                        currentStepsGeneralEnabled
                    )
                }.collect { }
            }

        } ?: Log.e(TAG, "ColmiRingConnector não está inicializado ao tentar iniciar coleta e comandos.")
    }

    private suspend fun sendCommandsBasedOnPreferences(
        connector: ColmiRingConnector,
        accelEnabled: Boolean,
        heartRateEnabled: Boolean,
        spo2Enabled: Boolean,
        stressEnabled: Boolean,
        stepsGeneralEnabled: Boolean
    ) {
        Log.d(TAG, "sendCommandsBasedOnPreferences() chamado com:")
        Log.d(TAG, "  Acelerômetro habilitado? $accelEnabled")
        Log.d(TAG, "  Frequência Cardíaca habilitado? $heartRateEnabled")
        Log.d(TAG, "  SpO2 habilitado? $spo2Enabled")
        Log.d(TAG, "  Stress habilitado? $stressEnabled")
        Log.d(TAG, "  Passos/Geral habilitado? $stepsGeneralEnabled")


        val anyRawDataEnabled = accelEnabled
        if (anyRawDataEnabled) {
            Log.d(TAG, "Enviando comando 'enableAllRawData' (0xA104) para o anel.")
            connector.sendCommand(hexStringToCmdBytes("a104"))
        } else {
            Log.d(TAG, "Enviando comando 'disableAllRawData' (0xA102) para o anel.")
            connector.sendCommand(hexStringToCmdBytes("a102"))
        }
        delay(500)

        if (heartRateEnabled) {
            Log.d(TAG, "Enviando comando 'requestHeartRate' (0x6901) para o anel.")
            connector.sendCommand(hexStringToCmdBytes("6901"))
        }
        delay(500)

        if (spo2Enabled) {
            Log.d(TAG, "Enviando comando 'requestSpO2' (0x6903) para o anel.")
            connector.sendCommand(hexStringToCmdBytes("6903"))
        }
        delay(500)

        if (stressEnabled) {
            Log.d(TAG, "Enviando comando 'requestStress' (0x6908) para o anel.")
            connector.sendCommand(hexStringToCmdBytes("6908"))
        }
        delay(500)
    }


    private suspend fun getUserId(): String? {
        val userId = authRepository.getCurrentUserFlow().first()?.uid
        if (userId == null) {
            Log.w(TAG, "UserID nulo, não é possível salvar dados.")
        }
        return userId
    }

    private fun stopBleMonitoring() {
        Log.d(TAG, "stopBleMonitoring() chamado. Desativando dados brutos e desconectando.")
        serviceScope.launch {
            Log.d(TAG, "Enviando comando 'disableAllRawData' (0xA102) antes de desconectar.")
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
        Log.d(TAG, "onDestroy do BleMonitoringService. Garantindo que tudo esteja parado.")
        super.onDestroy()
        serviceScope.cancel()
        colmiRingConnector?.disconnect()
        bleScanner.stopScan()
    }
}