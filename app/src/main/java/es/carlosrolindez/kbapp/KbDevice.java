package es.carlosrolindez.kbapp;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;

import es.carlosrolindez.btcomm.BtDevice;


/**
 * Created by Carlos on 21/05/2017.
 */

public class KbDevice extends BtDevice {
    //  BT device type
    private static String TAG = "KBdevice";

    public static final int OTHER = 0;
    public static final int ISELECT = 1;
    public static final int SELECTBT = 2;
    public static final int IN_WALL_BT = 3;
    public static final int IN_WALL_WIFI = 4;

    private static final String iSelectFootprint = "00:08:F4";
    private static final String inWallFootprint = "00:0D:18";
    private static final String inWallFootprint2 = "5C:0E:23";
    private static final String selectBtFootprint = "8C:DE:52";
    private static final String selectBtFootprint2 = "34:81:F4";
    private static final String inWallWiFiFootprint = "12:05:12";
    private static final String inWallWiFiFootprint2 = "11:07:16";

    protected static final int WAITING = 0;
    protected static final int PROGRESSING_UP = 1;
    protected static final int CONNECTED = 2;
    protected static final int PROGRESSING_DOWN = 3;

    protected final int deviceType;
    protected int btVisualState;
    protected boolean connectionInProcess;

    public KbDevice(String name, BluetoothDevice device) {
        super(name,device);
        deviceType = getDeviceType(device.getAddress());
        connectionInProcess = false;
        btVisualState = WAITING;

    }

    public void setConnectionInProcessState(boolean state) {
        connectionInProcess = state;
    }

    public boolean getConnectionInProcessState() {
        return connectionInProcess;
    }



    public static int getDeviceType(String deviceMAC) {
        String MAC = deviceMAC.substring(0,8);
        if (MAC.equals(iSelectFootprint)) return ISELECT;
        if (MAC.equals(inWallFootprint)) return IN_WALL_BT;
        if (MAC.equals(inWallFootprint2)) return IN_WALL_BT;
        if (MAC.equals(selectBtFootprint)) return SELECTBT;
        if (MAC.equals(selectBtFootprint2)) return SELECTBT;
        if (MAC.equals(inWallWiFiFootprint)) return IN_WALL_WIFI;
        if (MAC.equals(inWallWiFiFootprint2)) return IN_WALL_WIFI;
        return OTHER;
    }

}
