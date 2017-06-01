package es.carlosrolindez.kbapp;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import es.carlosrolindez.btcomm.btsppcomm.BtSppCommManager;


public class SelectBtFragment extends Fragment {
    public final static String TAG = "SelectBtFragment";

 /*   private BtSppCommManager mBtSppCommManager = null;

    public BtSppCommManager getBtSppCommManager() {
        return mBtSppCommManager;
    }*/

 /*   public static SelectBtFragment newInstance(BtSppCommManager manager) {
        SelectBtFragment fragment = new SelectBtFragment();
 //       fragment.mBtSppCommManager = manager;
        return fragment;
    }*/

/*    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.select_bt_fragment, container, false);
        // TODO try linearLayout to hide components
        // TODO or reinflate view

    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        final Activity activity = getActivity();

        TextView mTextStatus = (TextView) activity.findViewById(R.id.TEXT_STATUS_ID);
        final ScrollView mScrollView = (ScrollView) activity.findViewById(R.id.SCROLLER_ID);
        mScrollView.fullScroll(View.FOCUS_DOWN);

        String s="";
        for(int x=0; x<=100; x++) {
            s += "Line: " + String.valueOf(x) + "\n";
            mScrollView.smoothScrollTo(0, mTextStatus.getBottom());

        }

        mTextStatus.setText(s);

        mScrollView.post(new Runnable() {
            @Override
            public void run() {
                mScrollView.fullScroll(View.FOCUS_DOWN);
            }
        });


    }


/*
    public void sppMessage(String message) {
        sendSppMessage(message);
    }

    private void sendSppMessage(String message) {
        service.write(message);
        resetI2dpCounter();
    }

    private void askAll() {
        sendSppMessage("ALL ?\r");
        questionPending = QUESTION_ALL;
    }

    private void askForcedMono() {
        sendSppMessage("MON ?\r");
        questionPending = QUESTION_MON;
    }

    private void writeOnOffState(boolean onOff) {
        if (onOff) 	sendSppMessage("STB ON\r");
        else 		sendSppMessage("STB OFF\r");
    }

    private void writeChannelState(int channel) {
        if (channel == BT_CHANNEL) 	sendSppMessage("CHN BT\r");
        else 						sendSppMessage("CHN FM\r");
    }

    private void writeVolumeFMState(int volumeFM) {
        sendSppMessage("VOL " + String.valueOf(volumeFM) +"\r");
    }

    // TODO RE-CHECK
    public class MessageExtractor {
        private String message;

        public MessageExtractor(String m) {
            message = m;
        }

        public String getStringFromMessage() {
            if (message.isEmpty()) return "";
            String arr[] = message.trim().split(" ", 2);
            if(arr.length==1)
                message="";
            else
                message = arr[1];
            return arr[0];
        }

        public String getIdentifierFromMessage() {
            if (message.isEmpty()) return "";   // first "
            String arr[] = message.trim().split("\"", 2);
            message = arr[1];
            if (message.isEmpty()) return "";	// second "
            arr = message.split("\"", 2);
            message = arr[1];
            return arr[0];
        }

        public String getRDSFromMessage() {
            int len = message.length();
            String RDS;
            if (len == 0) return "";
            if (len>8) {
                RDS = message.substring(0, 8);
                message = message.substring(8, message.length());
            } else {
                RDS = message;
                message="";
            }

            return RDS;
        }

        public void removeCR() {
            if (message.length()>0)
                message = message.substring(0, message.length()-1);
        }

    }


    private void interpreter(String m) {
        Log.e(TAG,"Interpreter "+m);
        MessageExtractor messageExtractor = new MessageExtractor(m);

        String header = messageExtractor.getStringFromMessage();
        if (header.equals("RDS"))
            selectBtState.updateRds(messageExtractor.getRDSFromMessage());
        else if (header.equals("FMS"))
            selectBtState.updateFrequency(messageExtractor.getStringFromMessage());
        else {
            messageExtractor = new MessageExtractor(m);
            switch (questionPending) {
                case QUESTION_ALL:

                    String password = messageExtractor.getStringFromMessage();
                    selectBtState.updateName(messageExtractor.getIdentifierFromMessage());

                    selectBtState.updateOnOff(messageExtractor.getStringFromMessage());

                    String standByMasterSettings = messageExtractor.getStringFromMessage();
                    String standBySlaveSettings = messageExtractor.getStringFromMessage();

                    String autoPowerMaster = messageExtractor.getStringFromMessage();
                    String autoPowerSlave = messageExtractor.getStringFromMessage();
                    String autoPowerVolume = messageExtractor.getStringFromMessage();
                    String autoPowerFM = messageExtractor.getStringFromMessage();
                    String autoPowerEQ = messageExtractor.getStringFromMessage();

                    selectBtState.updateChannel(messageExtractor.getStringFromMessage());

                    selectBtState.updateFrequency(messageExtractor.getStringFromMessage());

                    selectBtState.updateRds(messageExtractor.getRDSFromMessage());

                    String tunerSensitivity = messageExtractor.getStringFromMessage();
                    String equalizationMode = messageExtractor.getStringFromMessage();


                    selectBtState.updateVolumeFM(messageExtractor.getStringFromMessage());
                    String keepFmOn = messageExtractor.message;
                    questionPending = NO_QUESTION;
                    askForcedMono();

                    break;

                case QUESTION_MON:
                    messageExtractor.removeCR();
                    selectBtState.updateForceMono(messageExtractor.message);
                    questionPending = NO_QUESTION;
                    break;

                default:
                    questionPending = NO_QUESTION;
            }

        }


    }

    private class SelectBtState {
        public boolean onOff;
        public int channel;
        public int volumeFM;
        public int volumeBT;
        public String name;
        public String frequency;
        public String rds;
        public String songName;

        public static final int MAX_VOLUME_FM = 15;

        private Context mContext;


        public SelectBtState(Context context) {
            onOff = false;
            channel = FM_CHANNEL;
            volumeBT = 0;
            volumeFM = 0;
            frequency = "87.5";
            songName = "";
            rds = "";
            mContext = context;
        }

        public void updateName(String n) {
            name = n;
            nameText.setText(name);
        }


        public void updateOnOff(String onOffString) {
            if (onOffString.equals("OFF")) {
                mainButton.setBackground(ContextCompat.getDrawable(mContext, R.drawable.power_off_selector));
                volumeSeekBar.setVisibility(View.INVISIBLE);
                windowLayout.setVisibility(View.INVISIBLE);
                onOff = false;
                changeStateI2dp(false);
            }
            else {
                onOff = true;
                mainButton.setBackground(ContextCompat.getDrawable(mContext, R.drawable.power_on_selector));
                volumeSeekBar.setVisibility(View.VISIBLE);
                windowLayout.setVisibility(View.VISIBLE);
                if (channel == BT_CHANNEL)
                    changeStateI2dp(true);
                else
                    changeStateI2dp(false);
            }

        }

        public void switchOnOff() {
            setOnOff(!onOff);
        }

        public void setOnOff(boolean on) {
            onOff = on;
            writeOnOffState(onOff);
            if (onOff){
                mainButton.setBackground(ContextCompat.getDrawable(mContext, R.drawable.power_on_selector));
                volumeSeekBar.setVisibility(View.VISIBLE);
                windowLayout.setVisibility(View.VISIBLE);
                if (channel == BT_CHANNEL)
                    changeStateI2dp(true);
                else
                    changeStateI2dp(false);
            } else  {
                mainButton.setBackground(ContextCompat.getDrawable(mContext, R.drawable.power_off_selector));
                volumeSeekBar.setVisibility(View.INVISIBLE);
                windowLayout.setVisibility(View.INVISIBLE);
                changeStateI2dp(onOff);
            }
        }

        public void updateChannel(String channelString) {

            if (channelString.equals("BT")) {
                channel = BT_CHANNEL;
                mPager.setCurrentItem(1, false);
                ((BtFragment)mAdapter.getItem(1)).setSongName(songName);
                changeStateI2dp(onOff);
            } else {
                channel = FM_CHANNEL;
                mPager.setCurrentItem(0, false);
                ((FmFragment)mAdapter.getItem(0)).setFrequency(frequency);
                changeStateI2dp(false);
            }
        }

        public void setChannel(int numChannel) {
            writeChannelState(numChannel);
            channel = numChannel;
            if (numChannel == BT_CHANNEL) {
                ((BtFragment)mAdapter.getItem(1)).setSongName(songName);
                changeStateI2dp(onOff);
            } else {
                changeStateI2dp(false);
                ((FmFragment)mAdapter.getItem(0)).setFrequency(frequency);
            }
        }

        public void updateVolumeFM(String volumeString) {
//           	final AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            volumeFM = Integer.parseInt(volumeString);
            if (channel == FM_CHANNEL) {

                volumeSeekBar.setProgress(volumeFM);
            }
        }

        public void setVolumeFM(int volume) {
            volumeFM = volume;
            writeVolumeFMState(volumeFM);
        }

        public void updateFrequency(String frequencyString) {
            frequency = frequencyString;
            if (mPager.getCurrentItem()==0) {
                ((FmFragment)mAdapter.getItem(0)).setFrequency(frequency);
            }
        }

        public void updateRds(String rdsString) {
            rds = rdsString;
            if (mPager.getCurrentItem()==0) {
                ((FmFragment)mAdapter.getItem(0)).setRDS(rds);
            }
        }

        public void updateTrackName(String name) {
            songName = name;
            if (mPager.getCurrentItem()==1) {
                ((BtFragment)mAdapter.getItem(1)).setSongName(songName);
            }
        }

        public void updateForceMono(String state) {
            if (state.equals("OFF")) {
                ((FmFragment)mAdapter.getItem(0)).setStereo(true);
            } else {
                ((FmFragment)mAdapter.getItem(0)).setStereo(false);
            }
        }


    }*/

}
