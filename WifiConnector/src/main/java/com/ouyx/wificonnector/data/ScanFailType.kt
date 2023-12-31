/*
 * Copyright (c) 2022-2032 ouyx
 * 不能修改和删除上面的版权声明
 * 此代码属于ouyx编写，在未经允许的情况下不得传播复制
 */
package com.ouyx.wificonnector.data


/**
 * wifi 扫描 失败情况
 *
 * @author ouyx
 * @date 2023年07月10日 10时40分
 */
sealed class ScanFailType {

    /**
     * 缺乏权限
     */
    object PermissionNotGranted : ScanFailType()

    /**
     * 位置信息未开启
     */
    object LocationNotEnable : ScanFailType()

    /**
     * 由于短时间扫描过多，扫描请求可能遭到节流。
     * 设备处于空闲状态，扫描已停用。
     *  WLAN 硬件报告扫描失败。
     */
    object StartScanFail : ScanFailType()

    /**
     * 正在扫描
     */
    object ScanningInProgress : ScanFailType()

    /**
     * 收到 SCAN_RESULTS_AVAILABLE_ACTION 广播 后，ScanResult WiFi扫描列表未更新
     */
    object ResultNotUpdated : ScanFailType()

}