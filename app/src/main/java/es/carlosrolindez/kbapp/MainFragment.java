package es.carlosrolindez.kbapp;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import es.carlosrolindez.btcomm.btsppcomm.BtSppCommManager;

public class MainFragment extends Fragment {

    private static final String TAG = "MainFragment";

    private static final String DEVICE_LIST = "device_list";

    private ArrayKbDevice deviceList;
    private BtDeviceListAdapter deviceListAdapter = null;
    private ListView mListView = null;
    private BtConnectionInterface mBtInterface;
//    private ProgressBarInterface pbInterface;
    private SelectBtInterface mSelectBtInterface;
/*    private BtSppCommManager mBtSppCommManager = null;

    public BtSppCommManager getBtSppCommManager() {
        return mBtSppCommManager;
    }*/

//    public static MainFragment newInstance(BtSppCommManager manager) {
//        MainFragment fragment = new MainFragment();
////        fragment.mBtSppCommManager = manager;
//        return fragment;
//    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof BtConnectionInterface) {
            mBtInterface = (BtConnectionInterface) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement BtConnectionInterface.");
        }

  /*      if (context instanceof MainFragment.ProgressBarInterface) {
            pbInterface = (MainFragment.ProgressBarInterface) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement MainFragment.ProgressBarInterface.");
        }*/

        if (context instanceof SelectBtInterface) {
            mSelectBtInterface = (SelectBtInterface) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement SelectBtInterface.");
        }
    }

/*    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }*/



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
        deviceListAdapter = new BtDeviceListAdapter(activity, deviceList, mBtInterface, mSelectBtInterface );
        mListView.setAdapter(deviceListAdapter);

 /*       mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pbInterface.stopProgressBar();
                KbDevice device = (KbDevice)parent.getItemAtPosition(position);
                mBtInterface.toggleBluetoothA2dp(device);
            }
        });*/

    }

    @Override
    public void onResume() {
        super.onResume();
        mBtInterface.showKnownBluetoothA2dpDevices();
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putParcelable(DEVICE_LIST,deviceList);
    }

/*
    public interface ProgressBarInterface {
        void stopProgressBar();
    }
*/
    public void addBtDevice(String name, KbDevice device) {

        for (KbDevice listDevice : deviceList)
            if (listDevice.getAddress().equals(device.getAddress())) {
                if (listDevice.deviceName.equals(device.deviceName)) return;
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
                listDevice.setConnectionInProcessState(true);
                deviceListAdapter.notifyDataSetChanged();
                return;
            }
        }

    }

    public void hideInProcess(BluetoothDevice device) {
        for (KbDevice listDevice : deviceList) {
            if (device.getAddress().equals(listDevice.getAddress())) {
                listDevice.setConnectionInProcessState(false);
                deviceListAdapter.notifyDataSetChanged();
                return;
            }
        }
    }

    public void showBonded(BluetoothDevice device) {

        for (KbDevice listDevice : deviceList)
        {
            if (device.getAddress().equals(listDevice.getAddress())) {
                listDevice.deviceBonded = true;
                deviceListAdapter.notifyDataSetChanged();
                return;

            }
        }
    }

    public void showConnected(BluetoothDevice device) {
        for (KbDevice listDevice : deviceList)
        {
            if (device.getAddress().equals(listDevice.getAddress())) {
                listDevice.deviceConnected = true;
                listDevice.setConnectionInProcessState(false);
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
                listDevice.setConnectionInProcessState(false);
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

    public void showSelectBtReady(BluetoothDevice device) {
        for (KbDevice listDevice : deviceList)
        {
            if (device.getAddress().equals(listDevice.getAddress())) {
                listDevice.setSppConnectionState(true);
                deviceListAdapter.notifyDataSetChanged();
                return;

            }
        }
    }

    public void hideSelectBtReady() {
        for (KbDevice listDevice : deviceList) {
            listDevice.setSppConnectionState(false);
            deviceListAdapter.notifyDataSetChanged();
        }
    }

}
