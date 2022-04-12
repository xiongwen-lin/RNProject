package com.afar.osaio.smart.smartlook.profile.manager;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import com.nooie.common.utils.log.NooieLog;

import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.BleManagerCallbacks;
import no.nordicsemi.android.log.LogContract;
import no.nordicsemi.android.log.LogSession;
import no.nordicsemi.android.log.Logger;

public abstract class BaseBleManager<E extends BleManagerCallbacks> extends BleManager {

    private LogSession mLogSession;

    public BaseBleManager(final Context context) {
        super(context);
    }

    /**
     * Sets the log session to be used for low level logging.
     *
     * @param session the session, or null, if nRF Logger is not installed.
     */
    public void setLogger(@Nullable final LogSession session) {
        this.mLogSession = session;
    }

    @Override
    public void log(final int priority, @NonNull final String message) {
        // The priority is a Log.X constant, while the Logger accepts it's log levels.
        Log.println(priority, "BaseBleManager", message);
        NooieLog.d("-->> BaseBleManager log BlinkyManager msg=" + message);
        Logger.log(mLogSession, LogContract.Log.Level.fromPriority(priority), message);
    }
}
