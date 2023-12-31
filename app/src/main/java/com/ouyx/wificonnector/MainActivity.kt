package com.ouyx.wificonnector

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.ouyx.wificonnector.data.ConnectFailType
import com.ouyx.wificonnector.data.ScanFailType
import com.ouyx.wificonnector.data.WifiCipherType
import com.ouyx.wificonnector.databinding.ActivityMainBinding
import com.ouyx.wificonnector.databinding.ScanningBinding
import com.ouyx.wificonnector.databinding.ScanningFailBinding
import com.ouyx.wificonnector.databinding.ScanningSuccessBinding
import com.ouyx.wificonnector.launch.WiFiOptions
import com.ouyx.wificonnector.launch.WifiConnector


class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    private var mChipType = WifiCipherType.WPA2
    private var mListAdapter: ScanListAdapter = ScanListAdapter()

    private val log: DefaultLogger = DefaultLogger()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        viewBinding.recy.apply {
            adapter = mListAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }


        val options =
            WiFiOptions.Builder().setDebug(true).connectTimeoutMsBeforeQ(10000)
                .isAndroidQAndEarlierConnectivityAPI(true)
                .build()
        WifiConnector.get().init(application, options = options)

        viewBinding.radiosCipher.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                viewBinding.radioWep.id -> {
                    mChipType = WifiCipherType.WEP
                }

                viewBinding.radioWpa2.id -> {
                    mChipType = WifiCipherType.WPA2
                }

                viewBinding.radioWpa3.id -> {
                    mChipType = WifiCipherType.WPA3
                }

                viewBinding.radioNoPass.id -> {
                    mChipType = WifiCipherType.NO_PASS
                }
            }
        }
        viewBinding.btnConnect.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                requestPermission(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), agree = {
                    startConnect()
                }, disAgree = {
                    log.info(message = "权限拒绝")
                })
            } else {
                requestPermission(
                    arrayOf(
                        Manifest.permission.CHANGE_WIFI_STATE,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ), agree = {
                        startConnect()
                    }, disAgree = {
                        log.info(message = "权限拒绝")
                    })
            }
        }

        viewBinding.btnScan.setOnClickListener {
            requestPermission(
                arrayOf(Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.ACCESS_FINE_LOCATION),
                agree = {
                    startScan()
                },
                disAgree = {
                    log.error(message = "没有权限!")
                })
        }

        viewBinding.btnGetConnectedInfo.setOnClickListener {

            val connectInfo = WifiConnector.get().getConnectedInfo()
            viewBinding.txtLogWifiinfo.text = connectInfo.toString()


        }

        mListAdapter.setOnItemClickListener { _, _, position ->
            val wifiScanItem = mListAdapter.data[position]
            viewBinding.editSsid.setText(wifiScanItem.ssid)
            viewBinding.editPassword.setText("")
            when (wifiScanItem.cipherType) {
                WifiCipherType.WEP -> viewBinding.radioWep.isChecked = true
                WifiCipherType.WPA2 -> viewBinding.radioWpa2.isChecked = true
                WifiCipherType.WPA3 -> viewBinding.radioWpa3.isChecked = true
                WifiCipherType.NO_PASS -> viewBinding.radioNoPass.isChecked = true
            }
        }

        viewBinding.butListenWifi.setOnClickListener {
            WifiConnector.get().setWifiConnectionStatusListener {
                onConnected {
                    log.info(message = "连接上WiFi设备 $it")
                    viewBinding.imgWifiState.setImageResource(R.drawable.online_circle)
                }
                onDisConnected {
                    log.info(message = "WiFi设备断开")
                    viewBinding.imgWifiState.setImageResource(R.drawable.offline_circle)
                }
            }
        }

        viewBinding.butCancelListenWifi.setOnClickListener {
            WifiConnector.get().cancelWifiConnectionStatusListener()
        }
    }


    @SuppressLint("SetTextI18n")
    private fun startConnect() {
        WifiConnector.get()
            .connect(viewBinding.editSsid.text.toString(), viewBinding.editPassword.text.toString(), mChipType) {
                onConnectStart {
                    log.debug(message = "onConnectStart>>>")
                    viewBinding.txtLog.text = "连接中..."
                }
                onConnectSuccess {
                    log.debug(message = "onConnectSuccess\n $it")
                    viewBinding.txtLog.text = "onConnectSuccess\n$it"
                }
                onConnectFail {
                    val cause: String = when (it) {
                        ConnectFailType.CancelByChoice -> "用户主动取消"
                        ConnectFailType.ConnectTimeout -> "超时"
                        ConnectFailType.ConnectingInProgress -> "正在连接中..."
                        ConnectFailType.PermissionNotEnough -> "权限不够"
                        is ConnectFailType.SSIDConnected -> "目标SSID 已连接[${it.wifiConnectInfo}]"
                        ConnectFailType.WifiNotEnable -> "WIFI未开启"
                        ConnectFailType.ConnectUnavailable -> "连接不可达"
                        ConnectFailType.EncryptionPasswordNotNull -> "加密时密码不能为空"
                        ConnectFailType.PasswordMustASCIIEncoded -> "秘密必须被ASCII编码"
                        ConnectFailType.SsidInvalid -> "SSID 无效"
                    }
                    log.debug(message = "onConnectFail: $cause")
                    viewBinding.txtLog.text = "onConnectFail: $cause"
                }
            }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun startScan() {
        mListAdapter.data.clear()
        mListAdapter.notifyDataSetChanged()

        WifiConnector.get().scan {
            onScanStart {
                mListAdapter.setHeaderView(getScanningView())

                log.debug(message = "onScanStart")
            }
            onScanSuccess { scanResults, parsedScanResults ->
                mListAdapter.setHeaderView(getSuccessView())

                log.debug(message = "onScanSuccess: $scanResults")
                parsedScanResults.forEach {
                    val ssid = it.ssid
                    val level = it.level
                    val capabilities = it.cipherType
                    log.info(message = "ssid = $ssid   level = $level   capabilities = $capabilities ")
                }
                mListAdapter.setList(parsedScanResults)
            }
            onScanFail {
                mListAdapter.data.clear()
                mListAdapter.notifyDataSetChanged()

                val errorMsg = when (it) {
                    ScanFailType.LocationNotEnable -> "位置信息未开启"
                    ScanFailType.PermissionNotGranted -> "需要ACCESS_FINE_LOCATION 和 CHANGE_WIFI_STATE 权限，参考https://developer.android.com/guide/topics/connectivity/wifi-scan?hl=zh-cn"
                    ScanFailType.ScanningInProgress -> "当前正在扫描，请稍后再试.."
                    ScanFailType.StartScanFail -> "由于短时间扫描过多，扫描请求可能遭到节流"
                    ScanFailType.ResultNotUpdated -> "WiFi扫描列表未更新"
                }
                mListAdapter.setHeaderView(getErrorView(errorMsg))

                log.debug(message = "onScanFail: $errorMsg")
            }
        }
    }


    private fun getScanningView(): View {
        val scanningBinding = ScanningBinding.inflate(layoutInflater)
        return scanningBinding.root
    }

    @SuppressLint("SetTextI18n")
    private fun getErrorView(msg: String): View {
        val scanningErrorBinding = ScanningFailBinding.inflate(layoutInflater)
        scanningErrorBinding.tvError.text = "扫描失败：$msg"
        return scanningErrorBinding.root
    }

    private fun getSuccessView(): View {
        val scanningSuccessBinding = ScanningSuccessBinding.inflate(layoutInflater)
        return scanningSuccessBinding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        WifiConnector.get().release()
    }
}


