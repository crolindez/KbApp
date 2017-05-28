package es.carlosrolindez.kbapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import es.carlosrolindez.btcomm.BtListenerManager;
import es.carlosrolindez.btcomm.bta2dpcomm.BtA2dpConnectionManager;
import es.carlosrolindez.btcomm.btsppcomm.BtSppClientSocket;
import es.carlosrolindez.btcomm.btsppcomm.BtSppCommManager;
import es.carlosrolindez.rfcomm.RfCommManager;


public class MainActivity extends AppCompatActivity implements BtListenerManager.RfListener<BluetoothDevice,BtListenerManager.BtEvent>,
                                                                BtA2dpConnectionManager.BtA2dpProxyListener,
                                                                BtConnectionInterface,
                                                                MainFragment.ProgressBarInterface {
    private static final String TAG = "MainActivity";

    private static final int REQUEST_ENABLE_BT = 1;

    private enum ActivityState {NOT_SCANNING, SCANNING, CONNECTED}
    private final ActivityState activityState = ActivityState.NOT_SCANNING;

    private BluetoothAdapter mBluetoothAdapter = null;
    private BtListenerManager mBtListenerManager = null;
    private BtA2dpConnectionManager mBtA2dpConnectionManager = null;

    private MenuItem scanButton;
    private MenuItem mActionProgressItem;

    private MainFragment mainFragment;
    private SelectBtFragment mSelectBtFragment;

    private BtSppCommManager mBtSppCommManager = null;
    private LocalBroadcastManager mLocalBroadcastManager;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(RfCommManager.STARTED)) {
                if (mBtSppCommManager.isSocketConnected()) {
                    mainFragment.showSelectBtReady(mBtSppCommManager.getConnectedDevice());
                }
            } else if (intent.getAction().equals(RfCommManager.MESSAGE)) {
                String readMessage = intent.getStringExtra(RfCommManager.message_content);
                //TODO MESSAGE                    if (readMessage.equals("Ping")) mp.start();
            } else if (intent.getAction().equals(RfCommManager.STOPPED)) {
                if (mBtSppCommManager.isSocketConnected())
                    mBtSppCommManager.closeSocket();
            } else if (intent.getAction().equals(RfCommManager.CLOSED)) {
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, getString(R.string.bt_not_available), Toast.LENGTH_LONG).show();
            finish();
        }

        FragmentManager fm = getSupportFragmentManager();

        if (savedInstanceState == null) {
            mainFragment = MainFragment.newInstance();
            fm.beginTransaction()
                    .add(R.id.root_layout, mainFragment, "mainFragment")
                    .commit();
        } else {
            mainFragment = (MainFragment) getSupportFragmentManager()
                    .findFragmentByTag("mainFragment");
        }


        mSelectBtFragment = (SelectBtFragment) fm.findFragmentByTag(SelectBtFragment.TAG);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (mSelectBtFragment == null) {
            mSelectBtFragment = new SelectBtFragment();
            fm.beginTransaction().add(mSelectBtFragment, SelectBtFragment.TAG).commit();
        }


        mBtA2dpConnectionManager = new BtA2dpConnectionManager(getApplication(),this);

        mBtSppCommManager = new BtSppCommManager(getApplicationContext());
        initializeBtSppListener();

        setProgressBar(ActivityState.NOT_SCANNING);


    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode != Activity.RESULT_OK) {
  //                  startRfListening();
    //            } else {
                    Toast.makeText(this, R.string.bt_not_enabled,Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        else
            startRfListening();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if  (mBtListenerManager!=null) mBtListenerManager.closeService();

        mainFragment.hideConnection();
        if (mBluetoothAdapter!=null) mBluetoothAdapter.cancelDiscovery();

    }

    @Override
    protected void onStop() {
        super.onStop();
        if  (mBtA2dpConnectionManager!=null) mBtA2dpConnectionManager.closeManager();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Store instance of the menu item containing progress
        mActionProgressItem = menu.findItem(R.id.mActionProgress);
        scanButton = menu.findItem(R.id.bt_scan);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.bt_scan) {
            if (activityState== ActivityState.NOT_SCANNING)
                setProgressBar(ActivityState.SCANNING);
        }

        return super.onOptionsItemSelected(item);
    }

    private void setProgressBar(ActivityState state) {

        switch (state) {
            case NOT_SCANNING:
            case CONNECTED:
                mBluetoothAdapter.cancelDiscovery();
                if (mActionProgressItem!=null)  mActionProgressItem.setVisible(false);
                if (scanButton!=null)           scanButton.setVisible(true);
                break;
            case SCANNING:
                mBluetoothAdapter.startDiscovery();
                if (mActionProgressItem!=null)  mActionProgressItem.setVisible(true);
                if (scanButton!=null)           scanButton.setVisible(false);
                break;

        }
    }

    private void startRfListening() {
        mBtListenerManager = new BtListenerManager(getApplication(),this);
        mBtA2dpConnectionManager.openManager();

        mBtListenerManager.knownBtDevices();
        mBtListenerManager.searchBtDevices();

    }

    public void addRfDevice(String name, BluetoothDevice device) {
        KbDevice newDevice = new KbDevice(name,device);
        if (newDevice.deviceType==KbDevice.OTHER) return;

        mainFragment.addBtDevice(name, newDevice);
    }

    public void notifyRfEvent(BluetoothDevice device,  BtListenerManager.BtEvent event) {

        switch (event) {
            case DISCOVERY_FINISHED:
                setProgressBar(ActivityState.NOT_SCANNING);
                break;

            case CONNECTED:
                break;
            case DISCONNECTED:
//                mainFragment.hideInProcess(device);
                break;


            case BONDED:
                if (mainFragment.showBonded(device))
                    if (mBtA2dpConnectionManager != null)
                        mBtA2dpConnectionManager.connectBluetoothA2dp(device);
                break;

            case CHANGING:
                mainFragment.showInProgress(device);
                break;
        }
    }

    public void notifyBtA2dpEvent(BluetoothDevice device,  BtA2dpConnectionManager.BtA2dpEvent event) {


        switch (event) {
            case CONNECTED:
                mainFragment.showConnected(device);
                if (KbDevice.getDeviceType(device.getAddress())==KbDevice.SELECTBT) {
                    connectBtSpp(device);
                }
                break;

            case DISCONNECTED:
                mainFragment.showDisconnected(device);
                break;

            case CHANGING:
                mainFragment.showInProgress(device);
                break;

        }


    }

    @Override
    public void forgetBluetoothA2dp(KbDevice device) {
        if (mBtA2dpConnectionManager!=null) {
            mBtA2dpConnectionManager.unbondBluetoothA2dp(device.mDevice);
        }
    }

    @Override
    public void toggleBluetoothA2dp(KbDevice device) {
        if (mBtSppCommManager!=null) {
            if (mBtSppCommManager.isSocketConnected()) {
                mBtSppCommManager.stopSocket();
                mainFragment.hideSelectBtReady();
            }
        }

        if (mBtA2dpConnectionManager!=null) {
            mBtA2dpConnectionManager.toggleBluetoothA2dp(device.mDevice);
        }
    }

    @Override
    public void stopProgressBar() {
        setProgressBar(ActivityState.CONNECTED);
    }

    private void connectBtSpp(BluetoothDevice device) {
        new BtSppClientSocket(mBtSppCommManager,device).start();
    }

    private void initializeBtSppListener() {
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);

        // We are going to watch for interesting local broadcasts.
        IntentFilter filter = new IntentFilter();
        filter.addAction(RfCommManager.STARTED);
        filter.addAction(RfCommManager.MESSAGE);
        filter.addAction(RfCommManager.STOPPED);
        filter.addAction(RfCommManager.CLOSED);

        mLocalBroadcastManager.registerReceiver(mReceiver, filter);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLocalBroadcastManager!=null)
            mLocalBroadcastManager.unregisterReceiver(mReceiver);
    }
}
