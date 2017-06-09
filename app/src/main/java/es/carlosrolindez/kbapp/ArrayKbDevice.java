package es.carlosrolindez.kbapp;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

class ArrayKbDevice extends ArrayList<KbDevice> implements Parcelable{
    public static String TAG = "ArrayKbDevice";
	
	void addSorted(KbDevice newDevice) {
		if (isEmpty()) {
			add(newDevice);
			return;
		}
		int position=0;
		for (KbDevice device : this) {
			if (newDevice.deviceType>device.deviceType) {
				add(position, newDevice);
				return;
			} else if (newDevice.deviceType==device.deviceType) {
				if (newDevice.deviceName.compareToIgnoreCase(device.deviceName)<=0) {
					add(position, newDevice);
					return;
				}
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
		for (KbDevice device:this) {
			parcel.writeParcelable(device, arg1);
		}

	}

	public static final Creator<ArrayKbDevice> CREATOR = new Creator<ArrayKbDevice>() {

		@Override
		public ArrayKbDevice createFromParcel(Parcel parcel)
		{
            ArrayKbDevice list = new ArrayKbDevice();

			int size = parcel.readInt();
			for (int i=0; i<size ; ++i) {
				KbDevice device = parcel.readParcelable(getClass().getClassLoader());
                list.addSorted(device);
			}
			return list;
		}

		@Override
		public ArrayKbDevice[] newArray(int size)
		{
			return new ArrayKbDevice[size];
		}
	};
}
