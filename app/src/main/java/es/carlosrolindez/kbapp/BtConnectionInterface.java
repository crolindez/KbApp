package es.carlosrolindez.kbapp;

import es.carlosrolindez.btcomm.BtDevice;

/**
 * Created by Carlos on 16/05/2017.
 */

public interface BtConnectionInterface {
    void forgetBluetoothA2dp(BtDevice device);
    void toggleBluetoothA2dp(BtDevice device);
}
