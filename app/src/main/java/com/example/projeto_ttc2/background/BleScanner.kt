package com.example.projeto_ttc2.background

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresPermission
import java.util.regex.Pattern

class BleScanner(private val bluetoothAdapter: BluetoothAdapter, private val scanCallback: (ScanResult) -> Unit) : ScanCallback() {

    private val bluetoothLeScanner: BluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
    private val handler = Handler(Looper.getMainLooper())
    private var scanning = false
    private val SCAN_PERIOD: Long = 5000 
    private val advertisedNamePattern = Pattern.compile("^R0\\d_[0-9A-Z]{4}$") 

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun startScan() {
        if (!scanning) {

            handler.postDelayed({
                stopScan()
            }, SCAN_PERIOD)

            val scanSettings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build()

            scanning = true
            bluetoothLeScanner.startScan(null, scanSettings, this)
        }
    }

    fun stopScan() {
        if (scanning) {
            scanning = false
            bluetoothLeScanner.stopScan(this)
        }
    }

    override fun onScanResult(callbackType: Int, result: ScanResult) {
        super.onScanResult(callbackType, result)
        if (result.device.name != null && advertisedNamePattern.matcher(result.device.name).matches()) {
            stopScan()
            scanCallback(result)
        }
    }

    override fun onBatchScanResults(results: MutableList<ScanResult>?) {
        super.onBatchScanResults(results)
        results?.forEach { result ->
            if (result.device.name != null && advertisedNamePattern.matcher(result.device.name).matches()) {
                stopScan()
                scanCallback(result)
            }
        }
    }

    override fun onScanFailed(errorCode: Int) {
        super.onScanFailed(errorCode)

    }
}