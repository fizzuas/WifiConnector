/*
 * Copyright (c) 2022-2032 ouyx
 * 不能修改和删除上面的版权声明
 * 此代码属于ouyx编写，在未经允许的情况下不得传播复制
 */
package com.ouyx.wificonnector.callback

import com.ouyx.wificonnector.core.dispatcher.WifiRequestDispatcher
import com.ouyx.wificonnector.data.ConnectFailType
import com.ouyx.wificonnector.data.WifiConnectInfo
import com.ouyx.wificonnector.core.request.WifiConnectRequest
import kotlinx.coroutines.launch


/**
 *  Wifi连接回调
 *
 * @author ouyx
 * @date 2023年07月06日 15时07分
 */
class WifiConnectCallback {

    private val mainScope = WifiRequestDispatcher.getInstance().getMainScope()


    private var start: (() -> Unit)? = null


    private var connectSuccess: ((wifiConnectInfo: WifiConnectInfo) -> Unit)? = null


    private var connectFail: ((connectFailType: ConnectFailType) -> Unit)? = null


    /**
     * 开始连接
     */
    fun onConnectStart(onStart: () -> Unit) {
        start = onStart
    }

    /**
     * 连接成功
     */
    fun onConnectSuccess(onSuccess: ((wifiConnectInfo: WifiConnectInfo) -> Unit)) {
        connectSuccess = onSuccess
    }

    /**
     * 连接失败
     */
    fun onConnectFail(onFail: (connectFailType: ConnectFailType) -> Unit) {
        connectFail = onFail
    }


    internal fun callConnectStart() {
        mainScope.launch { start?.invoke() }
    }


    internal fun callConnectSuccess(wifiConnectInfo: WifiConnectInfo) {
        mainScope.launch { connectSuccess?.invoke(wifiConnectInfo) }
    }


    internal fun callConnectFail(connectFailType: ConnectFailType) {
        mainScope.launch { connectFail?.invoke(connectFailType) }
    }


}