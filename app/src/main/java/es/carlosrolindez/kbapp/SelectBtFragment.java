package es.carlosrolindez.kbapp;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.transition.TransitionManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;


public class SelectBtFragment extends Fragment implements SelectBtMachine.SelectBtInterface {
    public final static String TAG = "SelectBtFragment";

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

    private Button dabButton;
    private LinearLayout mDabLayout;

    private Button btButton;
    private LinearLayout mBtLayout;

    private Button monitorButton;
    private ScrollView mScrollView;
    private String monitorText;
    private TextView mTextStatus;

    private SeekBar volumeBar;

    private boolean monitorActive;




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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final Activity activity = getActivity();

        selectBtLayout = (LinearLayout) activity.findViewById(R.id.selectBt);
        monitorActive = false;

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


        dabButton = (Button) activity.findViewById(R.id.dab_button);
        mDabLayout = (LinearLayout) activity.findViewById(R.id.layout_dab_button);

        btButton = (Button) activity.findViewById(R.id.bt_button);
        mBtLayout = (LinearLayout) activity.findViewById(R.id.layout_bt_button);

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

    //*************************************************************
    //   Members for changing mSelectBtMachine states
    //*************************************************************


    private void switchOnOff(boolean state) {
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

        if (monitorActive)
            paramsScrollLayout.weight = 1;
        else {
            if (mSelectBtMachine.onOff) {
                switch (mSelectBtMachine.channel) {
                    case SelectBtMachine.FM_CHANNEL:
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
                    fmButton.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorPrimary));
                    dabButton.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorPrimaryDark));
                    btButton.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorPrimaryDark));
                    break;
                case SelectBtMachine.DAB_CHANNEL:
                    fmButton.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorPrimaryDark));
                    dabButton.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorPrimary));
                    btButton.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorPrimaryDark));
                    break;
                case SelectBtMachine.BT_CHANNEL:
                    fmButton.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorPrimaryDark));
                    dabButton.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorPrimaryDark));
                    btButton.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorPrimary));
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
                    break;
                case SelectBtMachine.BT_CHANNEL:
                    AudioManager am = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
                    volumeBar.setVisibility(View.VISIBLE);
                    volumeBar.setProgress(am.getStreamVolume(AudioManager.STREAM_MUSIC));
                    break;
                case SelectBtMachine.DAB_CHANNEL:
                case SelectBtMachine.NO_CHANNEL:
                default:
                    volumeBar.setVisibility(View.INVISIBLE);
                    break;

            }
        } else {
            volumeBar.setVisibility(View.INVISIBLE);
        }

    }

    //*************************************************************
    //   Implementation of SelectBt Interface
    //*************************************************************

    @Override
    public void updateName(String name) {
        nameSelectBt.setText(name);
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
    public void updateVolume(int volume) {
        volumeBar.setProgress(volume);
    }

    @Override
    public void updateFmStation(FmStation station) {
        Log.e(TAG,station.showName());
        mFmStation.setText(station.showName());
    }


    @Override
    public void updateForceMono(boolean forced) {


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
}
