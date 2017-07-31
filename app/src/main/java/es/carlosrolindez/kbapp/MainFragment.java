package es.carlosrolindez.kbapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Locale;

public class MainFragment extends Fragment {

    private static final String TAG = "MainFragment";

    private static final String DEVICE_LIST = "device_list";

    private ArrayKbDevice deviceList;
    private BtDeviceListAdapter deviceListAdapter = null;
    private ListView mListView = null;
    private BtConnectionInterface mBtInterface;
    private BtDeviceListAdapter.TransitionInterface mTransitionInterface;

    private FloatingActionButton fab;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof BtConnectionInterface) {
            mBtInterface = (BtConnectionInterface) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement BtConnectionInterface.");
        }



        if (context instanceof BtDeviceListAdapter.TransitionInterface) {
            mTransitionInterface = (BtDeviceListAdapter.TransitionInterface) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement SelectBtInterface.");
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
        deviceListAdapter = new BtDeviceListAdapter(activity, deviceList, mBtInterface, mTransitionInterface );
        mListView.setAdapter(deviceListAdapter);

        fab = (FloatingActionButton) activity.findViewById(R.id.keyButton);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater li = LayoutInflater.from(getContext());
                View promptsView = li.inflate(R.layout.password_layout, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());

                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(promptsView);

                final EditText userInput = (EditText) promptsView
                        .findViewById(R.id.editTextDialogUserInput);

                // set dialog message
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        // get user input and set it to result
                                        // edit text
                                        String MAC = userInput.getText().toString();
                                        if (MAC.length()==12) {
                                            String fullMAC = MAC.substring(0, 2) + ":" +
                                                    MAC.substring(2, 4) + ":" +
                                                    MAC.substring(4, 6) + ":" +
                                                    MAC.substring(6, 8) + ":" +
                                                    MAC.substring(8, 10) + ":" +
                                                    MAC.substring(10, 12);
                                            Toast.makeText(getContext(), String.format(Locale.US, "%04d", SecretClass.password(fullMAC)), Toast.LENGTH_LONG).show();
                                        }
                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        dialog.cancel();
                                    }
                                });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();



            }
        });

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
