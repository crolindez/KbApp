package es.carlosrolindez.kbapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
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
                                                                BtDeviceListAdapter.TransitionInterface,
                                                                SelectBtMachine.SppComm{
    private static final String TAG = "MainActivity";

    private static final String MAIN_FRAGMENT = "mainFragment";
    private static final String SELECT_BT_FRAGMENT = "selectBtFragment";
    private static final String COMM_FRAGMENT = "commFragment";


    private static final int REQUEST_ENABLE_BT = 1;

    private enum ActivityState {NOT_SCANNING, SCANNING, CONNECTED}
    private ActivityState activityState = ActivityState.NOT_SCANNING;

    private BluetoothAdapter mBluetoothAdapter = null;
    private BtListenerManager mBtListenerManager = null;
    private BtA2dpConnectionManager mBtA2dpConnectionManager = null;

    private MenuItem scanButton;
    private MenuItem mActionProgressItem;
    private MenuItem mMenuItem;

    private MainFragment mainFragment;
    private SelectBtFragment mSelectBtFragment;
    private CommFragment mCommFragment;

    private BtSppCommManager mBtSppCommManager = null;

    private SelectBtMachine mSelectBtMachine;

    private LocalBroadcastManager mLocalBroadcastManager;

    private boolean menuActivated;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(RfCommManager.STARTED)) {
                if (mBtSppCommManager.isSocketConnected()) {
                    mainFragment.showSelectBtReady(mBtSppCommManager.getConnectedDevice());
                    setProgressBar(ActivityState.CONNECTED);
                }
            } else if (intent.getAction().equals(RfCommManager.MESSAGE)) {
                String readMessage = intent.getStringExtra(RfCommManager.message_content);
                mSelectBtMachine.interpreter(readMessage);
            } else if (intent.getAction().equals(RfCommManager.STOPPED)) {
 //               Log.e(TAG,"Socket stopped");
                if (mBtSppCommManager.isSocketConnected())
                    mBtSppCommManager.closeSocket();
            } else if (intent.getAction().equals(RfCommManager.CLOSED)) {
 //               Log.e(TAG,"Socket Closed");
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

        ActionBar ab = getSupportActionBar();
        if (ab!=null) {
            ab.setDisplayHomeAsUpEnabled(false);
            ab.setTitle(getResources().getString(R.string.ListDevicesTitle));
        }

        mSelectBtMachine = new SelectBtMachine(this);

        if (savedInstanceState!=null) {
            mCommFragment = (CommFragment) fm.findFragmentByTag(COMM_FRAGMENT);
            mBtSppCommManager = mCommFragment.getSppCommManager();
            mSelectBtFragment = (SelectBtFragment) fm.findFragmentByTag(SELECT_BT_FRAGMENT);
            mainFragment = null;
            if (mSelectBtFragment!=null) {
                if (ab!=null) {
                    ab.setDisplayHomeAsUpEnabled(true);
                    ab.setTitle(getResources().getString(R.string.SelectBtTitle));
                }
                mSelectBtFragment.setSelectMachine(mSelectBtMachine);
            } else {
                mainFragment = (MainFragment) fm.findFragmentByTag(MAIN_FRAGMENT);
                if (ab!=null) {
                    ab.setDisplayHomeAsUpEnabled(false);
                    ab.setTitle(getResources().getString(R.string.ListDevicesTitle));

                }

            }

        } else {
            mBtSppCommManager = new BtSppCommManager(getApplicationContext());
            mCommFragment = CommFragment.newInstance(mBtSppCommManager);
            fm.beginTransaction()
                    .add(mCommFragment, COMM_FRAGMENT)
                    .commit();

            mainFragment = new MainFragment();
            mSelectBtFragment = null;
            fm.beginTransaction()
                    .add(R.id.root_layout, mainFragment, MAIN_FRAGMENT)
                    .commit();

            if (ab!=null) {
                ab.setDisplayHomeAsUpEnabled(false);
                ab.setTitle(getResources().getString(R.string.ListDevicesTitle));
            }
        }
        menuActivated = false;

        mBtA2dpConnectionManager = new BtA2dpConnectionManager(getApplication(),this);

        initializeBtSppListener();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode != Activity.RESULT_OK) {
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
        if (mSelectBtFragment!=null)
            if (!mBtSppCommManager.isSocketConnected())
                onBackPressed();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if  (mBtListenerManager!=null) mBtListenerManager.closeService();

        if (mainFragment!=null) mainFragment.hideConnection();
        if (mBluetoothAdapter!=null) mBluetoothAdapter.cancelDiscovery();

        if (mSelectBtFragment!=null) {
            mSelectBtFragment.hideMenu();
            menuActivated = false;
        }

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
        mMenuItem = menu.findItem(R.id.menu);


        if (mBtSppCommManager.isSocketConnected()) {
            setProgressBar(ActivityState.CONNECTED);
        } else {
            setProgressBar(ActivityState.NOT_SCANNING);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.bt_scan:
                if (activityState== ActivityState.NOT_SCANNING) {
                    setProgressBar(ActivityState.SCANNING);
                }
                break;
            case R.id.menu:
                if (mSelectBtFragment!=null) {
                    if (menuActivated) {
                        mSelectBtFragment.hideMenu();
                        menuActivated = false;
                    } else {
                        mSelectBtFragment.showMenu();
                        menuActivated = true;
                    }
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setProgressBar(ActivityState state) {

        switch (state) {
            case NOT_SCANNING:
                mBluetoothAdapter.cancelDiscovery();
                if (mActionProgressItem!=null) {
                    mActionProgressItem.setVisible(false);
                }
                if (scanButton!=null) {
                    scanButton.setVisible(true);
                }
                if (mMenuItem!=null) {
                    mMenuItem.setVisible(false);
                }
                break;
            case SCANNING:
                mBluetoothAdapter.startDiscovery();
                if (mActionProgressItem!=null) {
                    mActionProgressItem.setVisible(true);
                }
                if (scanButton!=null)  {
                    scanButton.setVisible(false);
                }
                if (mMenuItem!=null) {
                    mMenuItem.setVisible(false);
                }
                break;
            case CONNECTED:
                mBluetoothAdapter.cancelDiscovery();
                if (mActionProgressItem!=null) {
                    mActionProgressItem.setVisible(false);
                }
                if (scanButton!=null) {
                    scanButton.setVisible(false);
                }
                if (mSelectBtFragment!=null) {
                    if (mMenuItem != null) {
                        mMenuItem.setVisible(true);
                    }
                } else {
                    if (mMenuItem != null) {
                        mMenuItem.setVisible(false);
                    }
                }
                break;

        }
        activityState=state;
    }

    private void startRfListening() {
        mBtListenerManager = new BtListenerManager(getApplication(),this);
        mBtA2dpConnectionManager.openManager();

        mBtListenerManager.knownBtDevices();
        mBtListenerManager.setListenerBtDevices();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLocalBroadcastManager!=null)
            mLocalBroadcastManager.unregisterReceiver(mReceiver);
    }



    @Override
    public void onBackPressed() {

        if (mSelectBtFragment!=null) {
            if (menuActivated) {
                mSelectBtFragment.hideMenu();
                menuActivated = false;
                return;
            }
        }

        FragmentManager fm = getSupportFragmentManager();
        int count = fm.getBackStackEntryCount();
        ActionBar ab = getSupportActionBar();
        if (ab!=null) {
//            ab.setHomeAsUpIndicator(R.drawable.ic_space);
            ab.setDisplayHomeAsUpEnabled(false);
            ab.setTitle(getResources().getString(R.string.ListDevicesTitle));
        }


        if (count == 0) {
            super.onBackPressed();
        } else {
            fm.popBackStack();
            mainFragment = (MainFragment) fm.findFragmentByTag(MAIN_FRAGMENT);
            mSelectBtFragment = null;
        }

        setProgressBar(ActivityState.CONNECTED);

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event)
    {
        if (mSelectBtFragment==null) return super.dispatchKeyEvent(event);

        if (event.getAction() == KeyEvent.ACTION_DOWN)
        {
            switch (event.getKeyCode())
            {
                case KeyEvent.KEYCODE_VOLUME_UP:
                    if (!mSelectBtMachine.onOff) return true;
                    if (mSelectBtMachine.channel==SelectBtMachine.BT_CHANNEL) {
                        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                        int volume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                        if  (volume < am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) {
                            am.setStreamVolume(AudioManager.STREAM_MUSIC, volume+1,	0);
                        }
                    } else if (mSelectBtMachine.channel==SelectBtMachine.FM_CHANNEL){
                        if  (mSelectBtMachine.volumeFm < SelectBtMachine.MAX_VOLUME_FM) {
                            mSelectBtMachine.setVolumeFm(mSelectBtMachine.volumeFm +1);
                        }
                    }
                    mSelectBtFragment.updateVolume();
                    return true;

                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    if (!mSelectBtMachine.onOff) return true;
                    if (mSelectBtMachine.channel==SelectBtMachine.BT_CHANNEL) {
                        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                        int volume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                        if  (volume > 0) {
                            am.setStreamVolume(AudioManager.STREAM_MUSIC, volume-1,	0);
                        }
                    } else if (mSelectBtMachine.channel==SelectBtMachine.FM_CHANNEL){
                        if  (mSelectBtMachine.volumeFm > 0) {
                            mSelectBtMachine.setVolumeFm(mSelectBtMachine.volumeFm -1);
                        }
                    }
                    mSelectBtFragment.updateVolume();
                    return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }


    //*************************************************************
    // Implementation of RfListener
    //*************************************************************
    @Override
    public void addRfDevice(String name, BluetoothDevice device) {
        if (mainFragment!=null) {
            KbDevice newDevice = new KbDevice(name, device);
            if (newDevice.deviceType == KbDevice.OTHER) return;
            mainFragment.addBtDevice(name, newDevice);
        }
    }

    @Override
    public void notifyRfEvent(BluetoothDevice device,  BtListenerManager.BtEvent event) {

        switch (event) {
            case DISCOVERY_FINISHED:
                if (activityState==ActivityState.SCANNING) {
                    setProgressBar(ActivityState.NOT_SCANNING);
                }
                break;

            case CONNECTED:
                break;
            case DISCONNECTED:
//                mainFragment.hideInProcess(device);
                break;
            case NOT_BONDED:
                mainFragment.hideInProcess(device);
                break;


            case BONDED:
                if (mainFragment!=null)  mainFragment.showBonded(device);
                if (mBtA2dpConnectionManager != null)
                    mBtA2dpConnectionManager.connectBluetoothA2dp(device);
                break;

            case CHANGING:
                if (mainFragment!=null) mainFragment.showInProgress(device);
                break;
        }
    }

    //*************************************************************
    // Implementation of BtA2dpProxyListener
    //*************************************************************
    @Override
    public void notifyBtA2dpEvent(final BluetoothDevice device,  BtA2dpConnectionManager.BtA2dpEvent event) {


        switch (event) {
            case CONNECTED:
                setProgressBar(ActivityState.CONNECTED);
                if (mainFragment!=null) mainFragment.showConnected(device);
                if (KbDevice.getDeviceType(device.getAddress())==KbDevice.SELECTBT) {
                    if (!mBtSppCommManager.isSocketConnected()) {

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                connectBtSpp(device);
                            }
                        }, 250);


                    } else {
                        if (mainFragment != null) mainFragment.showSelectBtReady(device);

                    }
                }
                break;

            case DISCONNECTED:
                setProgressBar(ActivityState.NOT_SCANNING);

                if (mainFragment!=null) mainFragment.showDisconnected(device);
                break;

            case CHANGING:
                if (mainFragment!=null) mainFragment.showInProgress(device);
                break;

        }


    }



    //*************************************************************
    // Implementation of BtConnectionInterface
    //*************************************************************

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
                if (mainFragment!=null) mainFragment.hideSelectBtReady();
            }
        }

        if (mBtA2dpConnectionManager!=null) {
            mBtA2dpConnectionManager.toggleBluetoothA2dp(device.mDevice);
        }
    }

    @Override
    public void showKnownBluetoothA2dpDevices(){
        if (mBtListenerManager != null) mBtListenerManager.knownBtDevices();
        if (mBtA2dpConnectionManager!=null) {
            mBtA2dpConnectionManager.refreshBluetoothA2dp();
        }

    }

    @Override
    public void connectBtSpp(BluetoothDevice device) {
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


    //*************************************************************
    // Implementation of BtDeviceListAdapter.TransitionInterface
    //*************************************************************

    @Override
    public void enterSelectBtFragment() {
        if (mBtSppCommManager!=null) {
            ActionBar ab = getSupportActionBar();
            if (ab!=null) {
                ab.setDisplayHomeAsUpEnabled(true);
                ab.setTitle(getResources().getString(R.string.SelectBtTitle));
//                ab.setHomeAsUpIndicator(R.drawable.ic_back);
            }

            mSelectBtFragment = SelectBtFragment.newInstance(mSelectBtMachine);
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
                    .replace(R.id.root_layout, mSelectBtFragment, SELECT_BT_FRAGMENT)
                    .addToBackStack(null)
                    .commit();
            setProgressBar(ActivityState.CONNECTED);
        }
    }

    //*************************************************************
    // Implementation of SppComm interface
    //*************************************************************

    @Override
    public void sendSppMessage(String message){
        if (mBtSppCommManager!=null) {
            if (mBtSppCommManager.isSocketConnected()) {
                mBtSppCommManager.write(message);
            }
        }
    }
}
