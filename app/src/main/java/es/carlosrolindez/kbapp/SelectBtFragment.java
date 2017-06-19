package es.carlosrolindez.kbapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.transition.TransitionManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;


// TODO Sometimes RDS arrives to late.



public class SelectBtFragment extends Fragment implements SelectBtMachine.SelectBtInterface {
    private final static String TAG = "SelectBtFragment";
    private final static int NUM_FM_MEMORIES = 6;

    private SelectBtMachine mSelectBtMachine;

    // Views

    private LinearLayout selectBtLayout;
    private TextView nameSelectBt;
    private Switch onOffSwitch;

    private Button fmButton;
    private RelativeLayout mFmLayout;
    private TextView mFmStation;
    private Button mButtonUp;
    private Button mButtonDown;
    private Button mButtonForcedMono;
    private Button mButtonSensitivity;
    private boolean forcedMonoState;
    private Button[] mButtonMemFm;
    private Button dabButton;
    private RelativeLayout mDabLayout;
    private ScrollView mMenuLayout;

    private Button btButton;
    private RelativeLayout mBtLayout;

    private Button monitorButton;
    private ScrollView mScrollView;
    private String monitorText;
    private TextView mTextStatus;

    private SeekBar volumeBar;

    private FloatingActionButton fab;

    private Switch masterOnOffSwitch;
    private Switch slaveOnOffSwitch;
    private Switch autoPowerMasterSwitch;
    private Switch autoPowerSlaveSwitch;
    private Switch keepFmOnSwitch;

    private TextView mName;
    private TextView mFirmware;
    private TextView mProductName;
    private TextView mModel;

    private Spinner spinner;

    private boolean monitorActive;

    private FmStation[] fmMemories;

    private int idealChannel;
    private int idealFmVolume;
    private FmStation idealFmStation;
    private int idealEqualization;

    private SharedPreferences preferencesFmMemories;
    private SharedPreferences preferencesIdeal;

    public static SelectBtFragment newInstance(SelectBtMachine machine) {
        SelectBtFragment selectBtFragment = new SelectBtFragment();
        selectBtFragment.setSelectMachine(machine);
        return selectBtFragment;
    }

    public void setSelectMachine (SelectBtMachine selectBtMachine) {
        mSelectBtMachine = selectBtMachine;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.select_bt_fragment, container, false);

    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final Activity activity = getActivity();
        preferencesFmMemories = activity.getSharedPreferences("FM_MEMORIES", Context.MODE_PRIVATE);
        fmMemories = new FmStation[NUM_FM_MEMORIES];
        for (int i=0;i<NUM_FM_MEMORIES;i++) {
            fmMemories[i]=loadFmMemory(i);
        }

        preferencesIdeal = activity.getSharedPreferences("IDEAL", Context.MODE_PRIVATE);
        loadIdeal();

        selectBtLayout = (LinearLayout) activity.findViewById(R.id.selectBt);
        monitorActive = false;

        mMenuLayout = (ScrollView) activity.findViewById(R.id.menuLayout);

        nameSelectBt = (TextView) activity.findViewById(R.id.selectBtName);
        onOffSwitch = (Switch) activity.findViewById(R.id.switch_on_off);

        onOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                switchOnOff(isChecked);
                updateChannel();
            }
        });

        fmButton = (Button) activity.findViewById(R.id.fm_button);
        mFmLayout = (RelativeLayout) activity.findViewById(R.id.layout_fm_button);
        mFmStation = (TextView) activity.findViewById(R.id.fm_station);
        mButtonUp = (Button) activity.findViewById(R.id.button_up);
        mButtonDown = (Button) activity.findViewById(R.id.button_down);
        mButtonForcedMono = (Button) activity.findViewById(R.id.button_forced_mono);
        forcedMonoState = true;
        mButtonSensitivity = (Button) activity.findViewById(R.id.button_sensitivity);

        mButtonMemFm = new Button[NUM_FM_MEMORIES];
        mButtonMemFm[0] = (Button) activity.findViewById(R.id.button_mem_fm_1);
        mButtonMemFm[1] = (Button) activity.findViewById(R.id.button_mem_fm_2);
        mButtonMemFm[2] = (Button) activity.findViewById(R.id.button_mem_fm_3);
        mButtonMemFm[3] = (Button) activity.findViewById(R.id.button_mem_fm_4);
        mButtonMemFm[4] = (Button) activity.findViewById(R.id.button_mem_fm_5);
        mButtonMemFm[5] = (Button) activity.findViewById(R.id.button_mem_fm_6);
        showFmMemories();

        dabButton = (Button) activity.findViewById(R.id.dab_button);
        mDabLayout = (RelativeLayout) activity.findViewById(R.id.layout_dab_button);

        btButton = (Button) activity.findViewById(R.id.bt_button);
        mBtLayout = (RelativeLayout) activity.findViewById(R.id.layout_bt_button);

        monitorButton = (Button) activity.findViewById(R.id.monitor_button);
        mTextStatus = (TextView) activity.findViewById(R.id.text_monitor);
        mTextStatus.setText("");
        mScrollView = (ScrollView) activity.findViewById(R.id.scroll_monitor);
        mScrollView.fullScroll(View.FOCUS_DOWN);
        //mScrollView.smoothScrollTo(0, mTextStatus.getBottom());
        mScrollView.post(new Runnable() {
            @Override
            public void run() {
                mScrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
        monitorText = "";

        volumeBar = (SeekBar) activity.findViewById(R.id.volumeControl);
        volumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar,int progressValue, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mSelectBtMachine.channel==SelectBtMachine.FM_CHANNEL) {
                    mSelectBtMachine.setVolumeFM(volumeBar.getProgress());
                } else if (mSelectBtMachine.channel==SelectBtMachine.BT_CHANNEL) {
                    AudioManager am = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
                    am.setStreamVolume(AudioManager.STREAM_MUSIC, volumeBar.getProgress(),	0);
                }
            }
        });

        fab = (FloatingActionButton) activity.findViewById(R.id.idealButton);

        fmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                monitorActive = false;
                if (mSelectBtMachine.onOff) selectChannel(SelectBtMachine.FM_CHANNEL);
            }
        });

        dabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                monitorActive = false;
                if (mSelectBtMachine.onOff) selectChannel(SelectBtMachine.DAB_CHANNEL);
            }
        });


        btButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                monitorActive = false;
                if (mSelectBtMachine.onOff) selectChannel(SelectBtMachine.BT_CHANNEL);
            }
        });

        monitorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                monitorActive = true;
                showLayout ();
            }
        });

        mButtonUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectBtMachine.setFmFrequency(mSelectBtMachine.fmStation.stepUpFrequency());
                updateFmStation(mSelectBtMachine.fmStation);
            }
        });

        mButtonUp.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mSelectBtMachine.scanFmUp();
                return true;
            }
        });

        mButtonDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectBtMachine.setFmFrequency(mSelectBtMachine.fmStation.stepDownFrequency());
                updateFmStation(mSelectBtMachine.fmStation);
            }
        });

        mButtonDown.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mSelectBtMachine.scanFmDown();
                return true;
            }
        });

        for (int i=0;i<NUM_FM_MEMORIES;i++) {
            final int memoryCounter = i;
            mButtonMemFm[memoryCounter].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (fmMemories[memoryCounter] == null) return;
                    mSelectBtMachine.fmStation.copyStation(fmMemories[memoryCounter]);
                    mSelectBtMachine.setFmFrequency(mSelectBtMachine.fmStation.getFrequency());
                    updateFmStation(mSelectBtMachine.fmStation);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mSelectBtMachine.setForcedMono(mSelectBtMachine.fmStation.isForcedMono());
                            updateForceMono(mSelectBtMachine.fmStation.isForcedMono());
                        }
                    }, 250);

                }
            });
        }

        for (int i=0;i<NUM_FM_MEMORIES;i++) {
            final int memoryCounter=i;
            mButtonMemFm[memoryCounter].setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
 //                   beep();
                    if (fmMemories[memoryCounter] == null) fmMemories[memoryCounter] = new FmStation();
                    fmMemories[memoryCounter].copyStation(mSelectBtMachine.fmStation);
                    saveFmMemory(fmMemories[memoryCounter],memoryCounter);
                    showFmMemories();
                    return true;
                }
            });
        }

        mButtonForcedMono.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectBtMachine.setForcedMono(!forcedMonoState);
                updateForceMono(!forcedMonoState);
            }
        });

        mButtonSensitivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (mSelectBtMachine.fmSensitivity) {
                    case 1:
                        mSelectBtMachine.setFmSensitivity(2);
                        updateSensitivity();
                        break;
                    case 2:
                        mSelectBtMachine.setFmSensitivity(3);
                        updateSensitivity();
                        break;
                    case 3:
                    default:
                        mSelectBtMachine.setFmSensitivity(1);
                        updateSensitivity();
                        break;

                }
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (idealChannel) {
                    case SelectBtMachine.FM_CHANNEL:
                        mSelectBtMachine.setFmIdeal(idealFmVolume, idealFmStation, idealEqualization);
                        updateChannel();
                        updateFmStation(idealFmStation);
                        updateVolume();
                        updateForceMono(idealFmStation.isForcedMono());
                        break;
                    case SelectBtMachine.BT_CHANNEL:
                        mSelectBtMachine.setBtIdeal(idealEqualization);
                        updateChannel();
                        updateVolume();
                        break;
                    case SelectBtMachine.DAB_CHANNEL:
                    case SelectBtMachine.NO_CHANNEL:
                    default:
                }
            }
        });

        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                saveIdeal();
                return true;
            }
        });

        /***********************************************************************
        /  Configuration sheet
        ***********************************************************************/

        masterOnOffSwitch = (Switch) activity.findViewById(R.id.master_on_off);
        masterOnOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mSelectBtMachine.isSetSelectInterface()) {
                    mSelectBtMachine.setMasterOnOff(isChecked);
                }
            }
        });

        slaveOnOffSwitch = (Switch) activity.findViewById(R.id.slave_on_off);
        slaveOnOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mSelectBtMachine.isSetSelectInterface()) {
                    mSelectBtMachine.setSlaveOnOff(isChecked);
                }
            }
        });

        autoPowerMasterSwitch = (Switch) activity.findViewById(R.id.auto_master_on_off);
        autoPowerMasterSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mSelectBtMachine.isSetSelectInterface()) {
                    mSelectBtMachine.setAutoMasterOnOff(isChecked);
                }
            }
        });


        autoPowerSlaveSwitch = (Switch) activity.findViewById(R.id.auto_slave_on_off);
        autoPowerSlaveSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mSelectBtMachine.isSetSelectInterface()) {
                    mSelectBtMachine.setAutoSlaveOnOff(isChecked);
                }
            }
        });

        keepFmOnSwitch = (Switch) activity.findViewById(R.id.keep_fm_on);
        keepFmOnSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mSelectBtMachine.isSetSelectInterface()) {
                    mSelectBtMachine.setKeepFmOn(isChecked);
                }
            }
        });


        mName = (TextView) activity.findViewById(R.id.bt_name);
        mName.setText(activity.getResources().getString(R.string.device_name));

        mFirmware = (TextView) activity.findViewById(R.id.firmware);
        mFirmware.setText(activity.getResources().getString(R.string.firmware));

        mProductName = (TextView) activity.findViewById(R.id.product_name);
        mProductName.setText(activity.getResources().getString(R.string.product_name));

        mModel = (TextView) activity.findViewById(R.id.model_number);
        mModel.setText(activity.getResources().getString(R.string.model_number));


        spinner = (Spinner) getActivity().findViewById(R.id.spinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.equalization_values, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }


    @Override
    public void onResume() {
        super.onResume();
        mSelectBtMachine.setSelectBtInterface(this);
        mSelectBtMachine.askAll();
    }

    @Override
    public void onPause() {
        super.onPause();
        mSelectBtMachine.setSelectBtInterface(null);
    }

 /*   private void beep() {
        ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP,150);
    }*/

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    void showMenu() {
        if (mMenuLayout!=null) {
            mMenuLayout.setVisibility(View.VISIBLE);

            onOffSwitch.setClickable(false);

            fmButton.setClickable(false);
            mButtonUp.setClickable(false);
            mButtonDown.setClickable(false);
            mButtonForcedMono.setClickable(false);
            mButtonSensitivity.setClickable(false);
            for (int i = 0;i<NUM_FM_MEMORIES; i++) {
                mButtonMemFm[i].setClickable(false);
            }
            dabButton.setClickable(false);
            btButton.setClickable(false);
            monitorButton.setClickable(false);

            volumeBar.setVisibility(View.INVISIBLE);
            fab.setVisibility(View.INVISIBLE);
        }
    }

    void hideMenu() {
        if (mMenuLayout!=null) {
            mMenuLayout.setVisibility(View.INVISIBLE);

            onOffSwitch.setClickable(true);

            fmButton.setClickable(true);
            mButtonUp.setClickable(true);
            mButtonDown.setClickable(true);
            mButtonForcedMono.setClickable(true);
            mButtonSensitivity.setClickable(true);
            for (int i = 0;i<NUM_FM_MEMORIES; i++) {
                mButtonMemFm[i].setClickable(true);
            }
            dabButton.setClickable(true);
            btButton.setClickable(true);
            monitorButton.setClickable(true);

            showVolumeBar();
        }
    }

    private void saveFmMemory(FmStation station, int numMemory) {
        SharedPreferences.Editor edit = preferencesFmMemories.edit();
        if (station!=null) {
            edit.putString("Frequency"+numMemory, station.getFrequency());
            if (station.getName()!=null)
                edit.putString("RDS"+numMemory, station.getName());
            else
                edit.putString("RDS"+numMemory,"");
            edit.putBoolean("ForcedMono"+numMemory,station.isForcedMono());

        }
        else {
            edit.putString("Frequency"+numMemory, "");
            edit.putString("RDS"+numMemory,"");
            edit.putBoolean("ForcedMono"+numMemory,false);
        }

        edit.apply();
    }

    private FmStation loadFmMemory(int numMemory) {
        FmStation station=null;
        String freq = preferencesFmMemories.getString("Frequency" + numMemory, "");
        if (!freq.equals("")) {
            station = new FmStation(freq);
            String RDS = preferencesFmMemories.getString("RDS" + numMemory, "");
            if (!RDS.equals(""))
                station.setName(RDS);
            station.setForcedMono(preferencesFmMemories.getBoolean("ForcedMono" + numMemory, false));
        }
        return station;
    }

    private void saveIdeal() {
        SharedPreferences.Editor edit = preferencesIdeal.edit();
        idealChannel = mSelectBtMachine.channel;
        edit.putInt("IdealChannel", idealChannel);

        if  (idealChannel==SelectBtMachine.FM_CHANNEL) {
            idealFmStation.copyStation(mSelectBtMachine.fmStation);
            edit.putString("IdealFrequency", idealFmStation.getFrequency());
            edit.putString("IdealRDS",idealFmStation.getName());
            edit.putBoolean("IdealForcedMono",idealFmStation.isForcedMono());

            idealFmVolume = mSelectBtMachine.volumeFM;
            edit.putInt("IdealFmVolume",idealFmVolume);
        }

        idealEqualization = mSelectBtMachine.equalization;
        edit.putInt("IdealEqualization",idealEqualization);
        showColorSelectedChannel();
        edit.apply();
    }

    private void loadIdeal() {
        idealChannel  = preferencesIdeal.getInt("IdealChannel", SelectBtMachine.FM_CHANNEL);

        String freq = preferencesIdeal.getString("IdealFrequency", "87.5");
        idealFmStation = new FmStation(freq);
        String RDS = preferencesIdeal.getString("IdealRDS", "");
        idealFmStation.setName(RDS);
        boolean forced = preferencesIdeal.getBoolean("IdealForcedMono", false);
        idealFmStation.setForcedMono(forced);

        idealFmVolume = preferencesIdeal.getInt("IdealFmVolume", 5);

        idealEqualization = preferencesIdeal.getInt("IdealEqualization", SelectBtMachine.EQ_OFF);


    }

    //*************************************************************
    //   Members for changing mSelectBtMachine states
    //*************************************************************


    private void switchOnOff(boolean state) {
        if (mSelectBtMachine.isSetSelectInterface())
            if (mSelectBtMachine.onOff != state) {
                mSelectBtMachine.setOnOff(state);
                updateOnOff(mSelectBtMachine.onOff);
            }
    }

    private void selectChannel(int channel) {
        if (mSelectBtMachine.channel!=channel) {
            mSelectBtMachine.setChannel(channel);
        }
        updateChannel();
    }


    //*************************************************************
    //   Members for updating view
    //*************************************************************



    private void showLayout(/*int channel*/) {
        TransitionManager.beginDelayedTransition(selectBtLayout);
        LinearLayout.LayoutParams paramsFmLayout = (LinearLayout.LayoutParams) mFmLayout.getLayoutParams();
        LinearLayout.LayoutParams paramsDabLayout = (LinearLayout.LayoutParams) mDabLayout.getLayoutParams();
        LinearLayout.LayoutParams paramsBtLayout = (LinearLayout.LayoutParams) mBtLayout.getLayoutParams();
        LinearLayout.LayoutParams paramsScrollLayout = (LinearLayout.LayoutParams) mScrollView.getLayoutParams();

        paramsFmLayout.weight = 0;
        paramsDabLayout.weight = 0;
        paramsBtLayout.weight = 0;
        paramsScrollLayout.weight = 0;

        for (int i=0;i<NUM_FM_MEMORIES; i++) {
            mButtonMemFm[i].setVisibility(View.INVISIBLE);
        }
        mFmStation.setVisibility(View.INVISIBLE);

        if (monitorActive)
            paramsScrollLayout.weight = 1;
        else {
            if (mSelectBtMachine.onOff) {
                switch (mSelectBtMachine.channel) {
                    case SelectBtMachine.FM_CHANNEL:
                        for (int i=0;i<NUM_FM_MEMORIES; i++) {
                            mButtonMemFm[i].setVisibility(View.VISIBLE);
                        }
                        mFmStation.setVisibility(View.VISIBLE);
                        paramsFmLayout.weight = 1;
                        break;
                    case SelectBtMachine.DAB_CHANNEL:
                        paramsDabLayout.weight = 1;
                        break;
                    case SelectBtMachine.BT_CHANNEL:
                        paramsBtLayout.weight = 1;
                        break;
                    case SelectBtMachine.MONITOR_CHANNEL:
                        paramsScrollLayout.weight = 1;
                        break;

                    case SelectBtMachine.NO_CHANNEL:
                    default:
                        break;
                }
            }
        }

        mFmLayout.setLayoutParams(paramsFmLayout);
        mDabLayout.setLayoutParams(paramsDabLayout);
        mBtLayout.setLayoutParams(paramsBtLayout);
        mScrollView.setLayoutParams(paramsScrollLayout);
    }

    private void showColorSelectedChannel() {
        if (mSelectBtMachine.onOff) {
            switch (mSelectBtMachine.channel) {
                case SelectBtMachine.FM_CHANNEL:
                    fmButton.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorAccent));
                    dabButton.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorPrimaryDark));
                    btButton.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorPrimaryDark));
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            fmButton.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorPrimary));
                        }
                    }, 500);
                    break;
                case SelectBtMachine.DAB_CHANNEL:
                    fmButton.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorPrimaryDark));
                    dabButton.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorAccent));
                    btButton.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorPrimaryDark));
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dabButton.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorPrimary));
                        }
                    }, 500);
                    break;
                case SelectBtMachine.BT_CHANNEL:
                    fmButton.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorPrimaryDark));
                    dabButton.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorPrimaryDark));
                    btButton.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorAccent));
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            btButton.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorPrimary));
                        }
                    }, 500);
                    break;
                case SelectBtMachine.NO_CHANNEL:
                default:
                    fmButton.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorPrimaryDark));
                    dabButton.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorPrimaryDark));
                    btButton.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorPrimaryDark));
                    break;

            }
        } else {
            fmButton.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorPrimaryDark));
            dabButton.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorPrimaryDark));
            btButton.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorPrimaryDark));
        }
    }

    private void showVolumeBar() {

        if (mSelectBtMachine.onOff) {
            switch (mSelectBtMachine.channel) {
                case SelectBtMachine.FM_CHANNEL:
                    volumeBar.setVisibility(View.VISIBLE);
                    volumeBar.setProgress(mSelectBtMachine.volumeFM);
                    fab.setVisibility(View.VISIBLE);
                    break;
                case SelectBtMachine.BT_CHANNEL:
                    AudioManager am = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
                    volumeBar.setVisibility(View.VISIBLE);
                    volumeBar.setProgress(am.getStreamVolume(AudioManager.STREAM_MUSIC));
                    fab.setVisibility(View.VISIBLE);
                    break;
                case SelectBtMachine.DAB_CHANNEL:
                case SelectBtMachine.NO_CHANNEL:
                default:
                    volumeBar.setVisibility(View.INVISIBLE);
                    fab.setVisibility(View.INVISIBLE);
                    break;

            }
        } else {
            volumeBar.setVisibility(View.INVISIBLE);
            fab.setVisibility(View.INVISIBLE);
        }

    }

    private void showFmMemories() {
        for (int i=0;i<NUM_FM_MEMORIES;i++) {
            if (fmMemories[i] == null) mButtonMemFm[i].setText("---.-");
            else mButtonMemFm[i].setText(fmMemories[i].showName());
        }

    }

    //*************************************************************
    //   Implementation of SelectBt Interface
    //*************************************************************

    @Override
    public void updateName(String name) {
        nameSelectBt.setText(name);
        mName.setText(getActivity().getResources().getString(R.string.device_name) + name);
    }

    @Override
    public void updateOnOff(boolean onOff) {
        onOffSwitch.setChecked(onOff);
        updateChannel();
    }

    @Override
    public void updateChannel() {
        showLayout();
        showColorSelectedChannel();
        showVolumeBar();
    }

    @Override
    public void updateVolume() {
        if (mSelectBtMachine.channel==SelectBtMachine.BT_CHANNEL) {
            AudioManager am = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
            volumeBar.setProgress(am.getStreamVolume(AudioManager.STREAM_MUSIC));
        } else if (mSelectBtMachine.channel==SelectBtMachine.FM_CHANNEL){
            volumeBar.setProgress(mSelectBtMachine.volumeFM);
        }


    }

    @Override
    public void updateSensitivity() {
        AnimatedVectorDrawable animationPlaySens;
        switch (mSelectBtMachine.fmSensitivity) {

            case 2:
                animationPlaySens= (AnimatedVectorDrawable) getActivity().getDrawable(R.drawable.animated_sensitivity_2);
                mButtonSensitivity.setBackground(animationPlaySens);
                if (animationPlaySens != null) animationPlaySens.start();
                break;
            case 3:
                animationPlaySens= (AnimatedVectorDrawable) getActivity().getDrawable(R.drawable.animated_sensitivity_3);
                mButtonSensitivity.setBackground(animationPlaySens);
                if (animationPlaySens != null) animationPlaySens.start();
                break;
            case 1:
            default:
                animationPlaySens= (AnimatedVectorDrawable) getActivity().getDrawable(R.drawable.animated_sensitivity_1);
                mButtonSensitivity.setBackground(animationPlaySens);
                if (animationPlaySens != null) animationPlaySens.start();
                break;

        }
    }

    @Override
    public void updateFmStation(FmStation station) {
        mFmStation.setText(station.showName());
        for (int i=0; i<NUM_FM_MEMORIES; i++) {
            if (fmMemories[i]!=null)
                if (fmMemories[i].getFrequency().equals(station.getFrequency()))
                    if (station.getName()!=null) {
                        fmMemories[i].setName(station.getName());
                        mButtonMemFm[i].setText(fmMemories[i].showName());
                        saveFmMemory(fmMemories[i],i);
                    }
        }
    }


    @Override
    public void updateForceMono(boolean forced) {
        if (forced == forcedMonoState) return;
        forcedMonoState = forced;
        if (forcedMonoState) {
            AnimatedVectorDrawable animationPlayForcedMono= (AnimatedVectorDrawable) getActivity().getDrawable(R.drawable.animated_forced_mono);
            mButtonForcedMono.setBackground(animationPlayForcedMono);
            if (animationPlayForcedMono != null) animationPlayForcedMono.start();

        } else {
            AnimatedVectorDrawable animationPlayStereo= (AnimatedVectorDrawable) getActivity().getDrawable(R.drawable.animated_stereo);
            mButtonForcedMono.setBackground(animationPlayStereo);
            if (animationPlayStereo != null) animationPlayStereo.start();
        }
    }

    @Override
    public void updateMessage(String message) {
        monitorText += message + "\n";
        mScrollView.smoothScrollTo(0, mTextStatus.getBottom());
        mTextStatus.setText(monitorText);
        mScrollView.post(new Runnable() {
            @Override
            public void run() {
                mScrollView.fullScroll(View.FOCUS_DOWN);
            }
        });

    }

    @Override
    public void updateMasterOnOff(){
        masterOnOffSwitch.setChecked(mSelectBtMachine.masterOnOff);
    }

    @Override
    public void updateSlaveOnOff(){
        slaveOnOffSwitch.setChecked(mSelectBtMachine.slaveOnOff);
    }

    @Override
    public void updateAutoPowerMaster(){
        autoPowerMasterSwitch.setChecked(mSelectBtMachine.autoPowerMaster);
    }

    @Override
    public void updateAutoPowerSlave(){
        autoPowerSlaveSwitch.setChecked(mSelectBtMachine.autoPowerSlave);
    }

    @Override
    public void updateKeepFmOn(){
        keepFmOnSwitch.setChecked(mSelectBtMachine.keepFmOn);
    }

    @Override
    public void updateFirmware(){
        mFirmware.setText(getActivity().getResources().getString(R.string.firmware) + mSelectBtMachine.firmware);
    }

    @Override
    public void updateProductName(){
        mProductName.setText(getActivity().getResources().getString(R.string.product_name) + mSelectBtMachine.productName);
    }

    @Override
    public void updateModel() {
        mModel.setText(getActivity().getResources().getString(R.string.model_number) + mSelectBtMachine.model);
    }


}
