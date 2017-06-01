package es.carlosrolindez.kbapp;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Carlos on 16/05/2017.
 */

public interface SelectBtInterface {
    void enterSelectBtFragment();
    void connectBtSpp(BluetoothDevice device);
}
