package es.carlosrolindez.kbapp;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Carlos on 16/05/2017.
 */

public interface BtConnectionInterface {
    void forgetBluetoothA2dp(KbDevice device);
    void toggleBluetoothA2dp(KbDevice device);
    void showKnownBluetoothA2dpDevices();
    void connectBtSpp(BluetoothDevice device);
}
