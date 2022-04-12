package com.android.nordicbluetooth.bleLiveData

import android.content.Context
import android.os.Handler
import androidx.lifecycle.LiveData
import com.android.nordicbluetooth.bleLiveData.state.BondState
import com.android.nordicbluetooth.bleLiveData.state.ConnectionState
import no.nordicsemi.android.ble.BleManager

/**
 * The Observable Ble Manager extends [BleManager] and adds support for observing
 * connection and bond state using AndroidX [LiveData].
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
abstract class ObservableBleManager: BleManager {
    val state: LiveData<ConnectionState> = ConnectionStateLiveData()
    val bondingState: LiveData<BondState> = BondingStateLiveData()

    constructor(context: Context) : super(context)
    constructor(context: Context, handler: Handler) : super(context, handler)


    init {
        setConnectionObserver(state as ConnectionStateLiveData)
        setBondingObserver(bondingState as BondingStateLiveData)
    }

}