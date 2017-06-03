package es.carlosrolindez.kbapp;

import android.bluetooth.BluetoothDevice;


public interface BtConnectionInterface {
    void forgetBluetoothA2dp(KbDevice device);
    void toggleBluetoothA2dp(KbDevice device);
    void showKnownBluetoothA2dpDevices();
    void connectBtSpp(BluetoothDevice device);
}
