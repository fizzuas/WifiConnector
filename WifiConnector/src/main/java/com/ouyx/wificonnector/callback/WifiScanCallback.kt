/*
 * Copyright (c) 2022-2032 ouyx
 * 不能修改和删除上面的版权声明
 * 此代码属于ouyx编写，在未经允许的情况下不得传播复制
 */
package com.ouyx.wificonnector.callback

import android.net.wifi.ScanResult
import com.ouyx.wificonnector.core.dispatcher.WifiRequestDispatcher
import com.ouyx.wificonnector.data.ScanFailType
import com.ouyx.wificonnector.data.WifiScanResult
import kotlinx.coroutines.launch


/**
 * Wifi 扫描结果 回调
 *
 * @author ouyx
 * @date 2023年07月10日 10时29分
 */
class WifiScanCallback {

    private val mainScope = WifiRequestDispatcher.getInstance().getMainScope()


    private var scanStart: (() -> Unit)? = null


    private var scanSuccess: ((scanResults: MutableList<ScanResult>,parsedScanResult: List<WifiScanResult>) -> Unit)? = null


    private var scanFail: ((scanFailType: ScanFailType) -> Unit)? = null


    /**
     * 开始扫描
     */
    fun onScanStart(onStart: () -> Unit) {
        scanStart = onStart
    }

    /**
     * 扫描成功
     */
    fun onScanSuccess(onSuccess: ((scanResults: MutableList<ScanResult>,parsedScanResult: List<WifiScanResult>) -> Unit)) {
        scanSuccess = onSuccess
    }

    /**
     * 扫描 发生错误
     */
    fun onScanFail(onFail: (scanFailType: ScanFailType) -> Unit) {
        scanFail = onFail
    }


    internal fun callScanStart() {
        mainScope.launch { scanStart?.invoke() }
    }


    internal fun callScanSuccess(systemScanResult: MutableList<ScanResult>, parsedScanResult: List<WifiScanResult>) {
        mainScope.launch { scanSuccess?.invoke(systemScanResult,parsedScanResult) }
    }


    internal fun callScanFail(scanFailType: ScanFailType) {
        mainScope.launch { scanFail?.invoke(scanFailType) }
    }
}