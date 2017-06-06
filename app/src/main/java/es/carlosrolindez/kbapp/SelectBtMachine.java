package es.carlosrolindez.kbapp;

import android.os.Handler;

public class SelectBtMachine {

    public static final int NO_CHANNEL = 0;
    public static final int FM_CHANNEL = 1;
    public static final int BT_CHANNEL = 2;
    public static final int DAB_CHANNEL = 3;
    public static final int MONITOR_CHANNEL = 10;

    public static final int MAX_VOLUME_FM = 15;

    private static final int NO_QUESTION = 0;
    private static final int QUESTION_ALL = 1;
    //	public static final int RDS = 2;
    //	public static final int BTID = 3;
    //	public static final int FREQUENCY = 4;
    private static final int QUESTION_MON = 5;

    public boolean onOff;
    public int channel;
    public int volumeFM;
 //   public int volumeBT;
    public String name;
    public FmStation fmStation;
 //   public String songName;
    public boolean forcedMono;

    private int questionPending;

    private SelectBtInterface mSelectBtInterface;
    private final SppComm mSppComm;


    public interface SppComm {
        void sendSppMessage(String message);
    }

    public interface SelectBtInterface {
        void updateName(String name);
        void updateOnOff(boolean onOff);
        void updateChannel();
        void updateVolume(int volume);
        void updateFmStation(FmStation station);
//        void updateFrequency(String frequencyString);
//        void updateRds(String rdsString);
//        void updateTrackName(String name);
        void updateForceMono(boolean forced);
        void updateMessage(String message);
    }

    public void setSelectBtInterface(SelectBtInterface selectBtInterface) {
        mSelectBtInterface = selectBtInterface;
    }


    public SelectBtMachine(SppComm sppComm) {
        onOff = false;
        channel = NO_CHANNEL;
        volumeFM = MAX_VOLUME_FM/2;
        name = "SelectBtMachine";
        fmStation = new FmStation();
        forcedMono = false;

        questionPending = NO_QUESTION;

        mSppComm = sppComm;
        mSelectBtInterface=null;
    }



    //**********************************************
    // Change of state values + update of BT device
    //**********************************************
    public void setOnOff(boolean on) {
        onOff = on;
        writeOnOff(onOff);
    }


    public void setChannel(int numChannel) {
        writeChannel(numChannel);
        channel = numChannel;
    }

    public void setVolumeFM(int volume) {
        volumeFM = volume;
        writeVolumeFM(volumeFM);
    }

    public void setFmFrequency(String frequency) {
        fmStation.setFrequency(frequency);
        writeFMFrequency(frequency);
    }

    public void setForcedMono(boolean forced) {
        fmStation.setForcedMono(forced);
        writeForcedMono(forced);
    }

    public void scanFmUp() {
        writeScanUp();
    }

    public void scanFmDown() {
        writeScanDown();
    }




    //**********************************************
    // Access to BT Spp by means of sendSppMessage
    //**********************************************

    protected void askAll() {
        mSppComm.sendSppMessage("ALL ?\r");
        if (mSelectBtInterface!=null)  mSelectBtInterface.updateMessage("<< ALL ?");
        questionPending = QUESTION_ALL;
    }

    protected void askForcedMono() {
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
        if (mSelectBtInterface!=null)  mSelectBtInterface.updateMessage("<< SCN UP");
    }

    private void writeScanDown() {
        mSppComm.sendSppMessage("SCN DOWN\r");
        if (mSelectBtInterface!=null)  mSelectBtInterface.updateMessage("<< SCN DOWN");
    }

    //**********************************************
    // Interpreter of Bt Spp Message.
    // Translates message -> changes state value -> updates view by means of SelectBtInterface
    //**********************************************

    public void interpreter(String m) {
        MessageExtractor messageExtractor = new MessageExtractor(m);
        if (mSelectBtInterface!=null)  mSelectBtInterface.updateMessage(">> " + m);

        String header = messageExtractor.getStringFromMessage();
        if (header.equals("RDS")) {
            SelectBtMachine.this.fmStation.setName(messageExtractor.getRDSFromMessage());
            if (mSelectBtInterface!=null)  mSelectBtInterface.updateFmStation(fmStation);
        }
        else if (header.equals("FMS")) {
            SelectBtMachine.this.fmStation.setFrequency(messageExtractor.getStringFromMessage());
            if (mSelectBtInterface!=null)  mSelectBtInterface.updateFmStation(fmStation);
        }
        else {
            messageExtractor = new MessageExtractor(m);
            switch (questionPending) {
                case QUESTION_ALL:

                    String password = messageExtractor.getStringFromMessage();

                    name = messageExtractor.getIdentifierFromMessage();

                    String onOffString = messageExtractor.getStringFromMessage();
                    if (onOffString.equals("OFF")) onOff = false;
                    else onOff = true;

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

                    String tunerSensitivity = messageExtractor.getStringFromMessage();
                    String equalizationMode = messageExtractor.getStringFromMessage();

                    volumeFM = Integer.parseInt(messageExtractor.getStringFromMessage());

                    String keepFmOn = messageExtractor.message;

                    if (mSelectBtInterface!=null) {
                        mSelectBtInterface.updateName(name);
                        mSelectBtInterface.updateOnOff(onOff);
                        mSelectBtInterface.updateChannel();
                        mSelectBtInterface.updateFmStation(fmStation);
                        mSelectBtInterface.updateVolume(volumeFM);
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
                        forcedMono = true;
                    } else {
                        forcedMono = false;
                    }
                    if (mSelectBtInterface!=null) mSelectBtInterface.updateForceMono(forcedMono);

                    questionPending = NO_QUESTION;
                    break;

                default:
                    questionPending = NO_QUESTION;
            }

        }



    }

    private class MessageExtractor {
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


}
