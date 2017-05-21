package es.carlosrolindez.kbapp;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

/**
 * Created by Carlos on 16/05/2017.
 */

public class MainFragment extends Fragment {

    private static final String TAG = "MainFragment";

    private static final String DEVICE_LIST = "device_list";

    private ArrayKbDevice deviceList;
    private BtDeviceListAdapter deviceListAdapter = null;
    private ListView mListView = null;
    private BtConnectionInterface mBtInterface;
    private ProgressBarInterface pbInterface;

    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof BtConnectionInterface) {
            mBtInterface = (BtConnectionInterface) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement BtConnectionInterface.");
        }

        if (context instanceof MainFragment.ProgressBarInterface) {
            pbInterface = (MainFragment.ProgressBarInterface) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement MainFragment.ProgressBarInterface.");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        final Activity activity = getActivity();

        if (savedInstanceState != null) {
            deviceList = savedInstanceState.getParcelable(DEVICE_LIST);
            if (deviceList==null) {
                deviceList = new ArrayKbDevice();
            }
        } else {
            deviceList = new ArrayKbDevice();
        }

        mListView = (ListView)activity.findViewById(R.id.list);
        deviceListAdapter = new BtDeviceListAdapter(activity, deviceList, mBtInterface );
        mListView.setAdapter(deviceListAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pbInterface.stopProgressBar();
                KbDevice device = (KbDevice)parent.getItemAtPosition(position);
                mBtInterface.toggleBluetoothA2dp(device);
            }
        });

    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putParcelable(DEVICE_LIST,deviceList);
    }


    public interface ProgressBarInterface {
        void stopProgressBar();
    }

    public void addBtDevice(String name, KbDevice device) {

        for (KbDevice listDevice : deviceList)
            if (listDevice.getAddress().equals(device.getAddress())) {
                listDevice.deviceName = name;
                deviceListAdapter.notifyDataSetChanged();
                return;
            }

        deviceList.addSorted(device);
        deviceListAdapter.notifyDataSetChanged();
    }

    public void showInProgress(BluetoothDevice device) {
        for (KbDevice listDevice : deviceList)
        {
            if (device.getAddress().equals(listDevice.getAddress())) {
                listDevice.deviceConnected = false;
                listDevice.setDeviceInProcess(true);
                deviceListAdapter.notifyDataSetChanged();
                return;
            }
        }

    }

    public void hideInProcess(BluetoothDevice device) {
        for (KbDevice listDevice : deviceList) {
            if (device.getAddress().equals(listDevice.getAddress())) {
                listDevice.setDeviceInProcess(false);
                deviceListAdapter.notifyDataSetChanged();
                return;
            }
        }
    }

    public boolean showBonded(BluetoothDevice device) {

        for (KbDevice listDevice : deviceList)
        {
            if (device.getAddress().equals(listDevice.getAddress())) {
                listDevice.deviceBonded = true;
                deviceListAdapter.notifyDataSetChanged();
                return true;

            }
        }

        return false;
    }

    public void showConnected(BluetoothDevice device) {
        for (KbDevice listDevice : deviceList)
        {
            if (device.getAddress().equals(listDevice.getAddress())) {
                listDevice.deviceConnected = true;
                listDevice.setDeviceInProcess(false);
                deviceListAdapter.notifyDataSetChanged();
                return;
            }
        }
    }

    public void showDisconnected(BluetoothDevice device) {
        for (KbDevice listDevice : deviceList)
        {
            if (device.getAddress().equals(listDevice.getAddress())) {
                listDevice.deviceConnected = false;
                listDevice.setDeviceInProcess(false);
                deviceListAdapter.notifyDataSetChanged();
                return;
            }
        }
    }

    public void hideConnection() {
        for (KbDevice listDevice : deviceList)
            listDevice.deviceConnected = false;

        deviceListAdapter.notifyDataSetChanged();
    }
}
