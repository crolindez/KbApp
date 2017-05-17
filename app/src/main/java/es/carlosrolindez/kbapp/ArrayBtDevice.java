package es.carlosrolindez.kbapp;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

import es.carlosrolindez.btcomm.BtDevice;

public class ArrayBtDevice extends ArrayList<BtDevice> implements Parcelable{
    public static String TAG = "ArrayBtDevice";
	
	public void addSorted(BtDevice newDevice) {
		if (isEmpty()) {
			add(newDevice);
			return;
		}
		int position=0;
		for (BtDevice device : this) {
			if (newDevice.deviceName.compareToIgnoreCase(device.deviceName)<=0) {
				add(position, newDevice);
				return;
			}
			position++;
		}
		add(newDevice);
	}


	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int arg1)
	{

		parcel.writeInt(this.size());
		for (BtDevice device:this) {
			parcel.writeParcelable(device, arg1);
		}

	}

	public static final Creator<ArrayBtDevice> CREATOR = new Creator<ArrayBtDevice>() {

		@Override
		public ArrayBtDevice createFromParcel(Parcel parcel)
		{
            ArrayBtDevice list = new ArrayBtDevice();

			int size = parcel.readInt();
			for (int i=0; i<size ; ++i) {
				BtDevice device = parcel.readParcelable(getClass().getClassLoader());
                list.addSorted(device);
			}
			return list;
		}

		@Override
		public ArrayBtDevice[] newArray(int size)
		{
			return new ArrayBtDevice[size];
		}

	};

}
