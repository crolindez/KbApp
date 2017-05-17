package es.carlosrolindez.btcomm;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

import es.carlosrolindez.rfcomm.RfDevice;

import static android.bluetooth.BluetoothDevice.BOND_NONE;

public class BtDevice extends RfDevice<BluetoothDevice> implements Parcelable{
    private final static String TAG = "BtDevice";

    public boolean deviceBonded;
    private boolean deviceInProcess;

    public BtDevice(String name, BluetoothDevice device) {
        super(name,device,0);
        deviceBonded = (device.getBondState()!=BOND_NONE);
        deviceInProcess = false;
    }

    public void setDeviceInProcess(boolean state) {
        this.deviceInProcess = state;
    }

    public boolean getDeviceInProcess() {
        return this.deviceInProcess;
    }

    @Override
    public String getAddress() {
        if (mDevice!=null) return mDevice.getAddress();
        return null;
    }
    
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int arg1)
    {
        parcel.writeParcelable(this.mDevice,arg1);
        parcel.writeString(this.deviceName);
    }

    public static final Creator<BtDevice> CREATOR = new Creator<BtDevice>() {

        @Override
        public BtDevice createFromParcel(Parcel parcel)
        {
            BluetoothDevice device = parcel.readParcelable(getClass().getClassLoader());
            String name = parcel.readString();
            return new BtDevice(name,device);
        }

        @Override
        public BtDevice[] newArray(int size)
        {
            return new BtDevice[size];
        }

    };
}
