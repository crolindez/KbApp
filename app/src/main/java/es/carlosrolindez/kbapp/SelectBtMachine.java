package es.carlosrolindez.kbapp;

import android.os.Handler;

class SelectBtMachine {
    private static final String TAG = "SelectBtMachine";

    static final int NO_CHANNEL = 0;
    static final int FM_CHANNEL = 1;
    static final int BT_CHANNEL = 2;
    static final int DAB_CHANNEL = 3;
    static final int MONITOR_CHANNEL = 10;

    static final int EQ_OFF = 0;
    static final int EQ_SOFT = 1;
    static final int EQ_BASS = 2;
    static final int EQ_TREBLE = 3;
    static final int EQ_CLASSICAL = 4;
    static final int EQ_ROCK = 5;
    static final int EQ_JAZZ = 6;
    static final int EQ_POP = 7;
    static final int EQ_DANCE = 8;
    static final int EQ_RNB = 9;
    static final int EQ_USER = 10;


    static final int MAX_VOLUME_FM = 15;

    private static final int NO_QUESTION = 0;
    private static final int QUESTION_ALL = 1;
    //	public static final int RDS = 2;
    //	public static final int BTID = 3;
    //	public static final int FREQUENCY = 4;
    private static final int QUESTION_MON = 5;

    boolean onOff;
    int channel;
    int volumeFM;
    String name;
    final FmStation fmStation;

    int equalization;
    int fmSensitivity;


    private int questionPending;

    private SelectBtInterface mSelectBtInterface;
    private final SppComm mSppComm;


    interface SppComm {
        void sendSppMessage(String message);
    }

    interface SelectBtInterface {
        void updateName(String name);
        void updateOnOff(boolean onOff);
        void updateChannel();
        void updateVolume();
        void updateFmStation(FmStation station);
        //        void updateTrackName(String name);
        void updateForceMono(boolean forced);
        void updateMessage(String message);
        void updateSensitivity();
    }

    void setSelectBtInterface(SelectBtInterface selectBtInterface) {
        mSelectBtInterface = selectBtInterface;
    }

    boolean isSetSelectInterface() {
        return (mSelectBtInterface!=null);
    }
    SelectBtMachine(SppComm sppComm) {
        onOff = false;
        channel = NO_CHANNEL;
        volumeFM = MAX_VOLUME_FM/2;
        name = "SelectBtMachine";
        fmStation = new FmStation();
        equalization = EQ_OFF;
        fmSensitivity = 1;

        questionPending = NO_QUESTION;

        mSppComm = sppComm;
        mSelectBtInterface=null;
    }



    //**********************************************
    // Change of state values + update of BT device
    //**********************************************
    void setOnOff(boolean on) {
        onOff = on;
        writeOnOff(onOff);
    }


    void setChannel(int numChannel) {
        writeChannel(numChannel);
        channel = numChannel;
    }

    void setVolumeFM(int volume) {
        volumeFM = volume;
        writeVolumeFM(volumeFM);
    }

    void setFmFrequency(String frequency) {
        fmStation.setFrequency(frequency);
        writeFMFrequency(frequency);
    }

    void setForcedMono(boolean forced) {
        fmStation.setForcedMono(forced);
        writeForcedMono(forced);
    }

    void scanFmUp() {
        writeScanUp();
    }

    void scanFmDown() {
        writeScanDown();
    }

    void setFmSensitivity(int sens) {
        fmSensitivity = sens;
        writeSensitivity(fmSensitivity);
    }

    void setBtIdeal(int equ) {
        channel = BT_CHANNEL;
        equalization = equ;
        writeIdeal(volumeFM,"BT",fmStation.getFrequency(),equ);
    }

    void setFmIdeal(int volume,FmStation station,int equ) {
        channel = FM_CHANNEL;
        volumeFM = volume;
        fmStation.copyStation(station);
        equalization = equ;
        writeIdeal(volumeFM,"FM",fmStation.getFrequency(),equ);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                writeForcedMono(fmStation.isForcedMono());
            }
        }, 250);
    }


    //**********************************************
    // Access to BT Spp by means of sendSppMessage
    //**********************************************

    void askAll() {
        mSppComm.sendSppMessage("ALL ?\r");
        if (mSelectBtInterface!=null)  mSelectBtInterface.updateMessage("<< ALL ?");
        questionPending = QUESTION_ALL;
    }

    void askForcedMono() {
        mSppComm.sendSppMessage("MON ?\r");
        if (mSelectBtInterface!=null)  mSelectBtInterface.updateMessage("<< MON ?");
        questionPending = QUESTION_MON;
    }

    private void writeOnOff(boolean onOff) {
        if (onOff) {
            mSppComm.sendSppMessage("STB ON\r");
            if (mSelectBtInterface!=null)  mSelectBtInterface.updateMessage("<< STB ON");
        } else {
            mSppComm.sendSppMessage("STB OFF\r");
            if (mSelectBtInterface!=null)  mSelectBtInterface.updateMessage("<< STB OFF");
        }
    }

    private void writeChannel(int channel) {
        if (channel == BT_CHANNEL) {
            mSppComm.sendSppMessage("CHN BT\r");
            if (mSelectBtInterface!=null)  mSelectBtInterface.updateMessage("<< CHN BT");
        } else if (channel == FM_CHANNEL){
            mSppComm.sendSppMessage("CHN FM\r");
            if (mSelectBtInterface!=null)  mSelectBtInterface.updateMessage("<< CHN FM");
        } else {
            mSppComm.sendSppMessage("CHN DAB\r");
            if (mSelectBtInterface!=null)  mSelectBtInterface.updateMessage("<< CHN DAB");
        }
    }

    private void writeVolumeFM(int volumeFM) {
        mSppComm.sendSppMessage("VOL " + String.valueOf(volumeFM) +"\r");
        if (mSelectBtInterface!=null)  mSelectBtInterface.updateMessage("<< VOL " + String.valueOf(volumeFM));
    }


    private void writeSensitivity(int sensitivity) {
        mSppComm.sendSppMessage("SNS " + String.valueOf(sensitivity) +"\r");
        if (mSelectBtInterface!=null)  mSelectBtInterface.updateMessage("<< SNS " + String.valueOf(sensitivity));
    }

    private void writeFMFrequency(String  FMFrequency) {
        String freq[] = FMFrequency.trim().split("\\.");
        final String newFreq[] = {freq[0], freq[1]};
        mSppComm.sendSppMessage("TUN "+newFreq[0]+"."+newFreq[1]+"\r");
        if (mSelectBtInterface!=null)  mSelectBtInterface.updateMessage("<< TUN "+newFreq[0]+"."+newFreq[1]);
    }

    private void writeForcedMono(boolean forced) {
        if (forced) {
            mSppComm.sendSppMessage("MON ON\r");
            if (mSelectBtInterface!=null)  mSelectBtInterface.updateMessage("<< MON ON");
        } else {
            mSppComm.sendSppMessage("MON OFF\r");
            if (mSelectBtInterface!=null)  mSelectBtInterface.updateMessage("<< MON OFF");
        }
    }

    private void writeScanUp() {
        mSppComm.sendSppMessage("SCN UP\r");
        if (mSelectBtInterface != null) mSelectBtInterface.updateMessage("<< SCN UP");
    }


    private void writeScanDown() {
        mSppComm.sendSppMessage("SCN DOWN\r");
        if (mSelectBtInterface!=null)  mSelectBtInterface.updateMessage("<< SCN DOWN");
    }

    private void writeIdeal(int volumen,String channel,String frequency, int equalization) {
        mSppComm.sendSppMessage("AUD " + String.valueOf(volumen) + " " + channel + " " + frequency +
                " " + String.valueOf(equalization) + "\r");
        if (mSelectBtInterface!=null)  mSelectBtInterface.updateMessage("<< AUD " + String.valueOf(volumen) + " " + channel + " " + frequency +
                " " + String.valueOf(equalization));

    }

    //**********************************************
    // Interpreter of Bt Spp Message.
    // Translates message -> changes state value -> updates view by means of SelectBtInterface
    //**********************************************

    void interpreter(String m) {
        MessageExtractor messageExtractor = new MessageExtractor(m);
        if (mSelectBtInterface!=null)  mSelectBtInterface.updateMessage(">> " + m);

        String header = messageExtractor.getStringFromMessage();
        switch (header) {
            case "RDS":
                SelectBtMachine.this.fmStation.setName(messageExtractor.getRDSFromMessage());
                if (mSelectBtInterface != null) mSelectBtInterface.updateFmStation(fmStation);
                break;
            case "FMS":
                SelectBtMachine.this.fmStation.setFrequency(messageExtractor.getStringFromMessage());
                if (mSelectBtInterface != null) mSelectBtInterface.updateFmStation(fmStation);
                break;
            default:
                messageExtractor = new MessageExtractor(m);
                switch (questionPending) {
                    case QUESTION_ALL:

                        String password = messageExtractor.getStringFromMessage();

                        name = messageExtractor.getIdentifierFromMessage();

                        String onOffString = messageExtractor.getStringFromMessage();
                        onOff = !onOffString.equals("OFF");

                        String standByMasterSettings = messageExtractor.getStringFromMessage();
                        String standBySlaveSettings = messageExtractor.getStringFromMessage();
                        String autoPowerMaster = messageExtractor.getStringFromMessage();
                        String autoPowerSlave = messageExtractor.getStringFromMessage();
                        String autoPowerVolume = messageExtractor.getStringFromMessage();
                        String autoPowerFM = messageExtractor.getStringFromMessage();
                        String autoPowerEQ = messageExtractor.getStringFromMessage();

                        String channelString = messageExtractor.getStringFromMessage();
                        if (channelString.equals("BT")) {
                            channel = BT_CHANNEL;
                        } else if (channelString.equals("DAB")) {
                            channel = DAB_CHANNEL;
                        } else {
                            channel = FM_CHANNEL;
                        }

                        SelectBtMachine.this.fmStation.setFrequency(messageExtractor.getStringFromMessage());


                        SelectBtMachine.this.fmStation.setName(messageExtractor.getRDSFromMessage());

                        fmSensitivity = Integer.parseInt(messageExtractor.getStringFromMessage());
                        equalization = Integer.parseInt(messageExtractor.getStringFromMessage());
                        volumeFM = Integer.parseInt(messageExtractor.getStringFromMessage());

                        String keepFmOn = messageExtractor.message;

                        if (mSelectBtInterface != null) {
                            mSelectBtInterface.updateName(name);
                            mSelectBtInterface.updateOnOff(onOff);
                            mSelectBtInterface.updateChannel();
                            mSelectBtInterface.updateFmStation(fmStation);
                            mSelectBtInterface.updateVolume();
                            mSelectBtInterface.updateSensitivity();
                        }

                        questionPending = NO_QUESTION;

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                askForcedMono();
                            }
                        }, 250);


                        break;

                    case QUESTION_MON:
                        messageExtractor.removeCR();
                        String forcedMonoString = messageExtractor.message;
                        if (forcedMonoString.equals("ON")) {
                            SelectBtMachine.this.fmStation.setForcedMono(true);
                            if (mSelectBtInterface != null)
                                mSelectBtInterface.updateForceMono(true);
                        } else {
                            SelectBtMachine.this.fmStation.setForcedMono(false);
                            if (mSelectBtInterface != null)
                                mSelectBtInterface.updateForceMono(false);
                        }


                        questionPending = NO_QUESTION;
                        break;

                    default:
                        questionPending = NO_QUESTION;
                }

                break;
        }



    }

    private class MessageExtractor {
        private String message;

        MessageExtractor(String m) {
            message = m;
        }

        String getStringFromMessage() {
            if (message.isEmpty()) return "";
            String arr[] = message.trim().split(" ", 2);
            if(arr.length==1)
                message="";
            else
                message = arr[1];
            return arr[0];
        }

        String getIdentifierFromMessage() {
            if (message.isEmpty()) return "";   // first "
            String arr[] = message.trim().split("\"", 2);
            message = arr[1];
            if (message.isEmpty()) return "";	// second "
            arr = message.split("\"", 2);
            message = arr[1];
            return arr[0];
        }

        String getRDSFromMessage() {
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

        void removeCR() {
            if (message.length()>0)
                message = message.substring(0, message.length()-1);
        }

    }
}
