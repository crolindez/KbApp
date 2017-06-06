package es.carlosrolindez.kbapp;

public class FmStation {
    private String name;
    private String frequency;
    private boolean forcedMono;

    public FmStation() {
        name = null;
        frequency = "87.5";
        forcedMono = false;
    }

    public FmStation(String freq) {
        super();
        frequency = freq;
    }

    public boolean isForcedMono() {
        return forcedMono;
    }

    public String showName() {
        if (name==null) return (frequency + " MHz");
        if (name.length()==0) return (frequency + " MHz");
        if (name.equals("        ")) return (frequency + " MHz");
        return name;
    }

    public void setName(String newName) {
        name = newName;
    }

    public String getFrequency() {
        return frequency;
    }

    public String getName() {
        return name;
    }

    public void copyStation(FmStation station) {
        this.frequency = station.getFrequency();
        this.setName(station.getName());
        this.setForcedMono(station.isForcedMono());
    }

    public void setFrequency(String newFreq) {
        if (frequency.equals(newFreq)) return;
        frequency = newFreq;
        name = null;
    }

    public void setForcedMono(boolean state) {
        forcedMono = state;
    }

    public String stepUpFrequency(){
        if ( frequency.equals("108.0")) {
            return "87.5";
        } else {
            Float floatFreq = Float.parseFloat(frequency);
            floatFreq += 0.1f;
            floatFreq = Math.round(floatFreq * 10) / 10f;
            return floatFreq.toString();
        }
    }

    public String stepDownFrequency(){
        if ( frequency.equals("87.5")) {
            return "108.0";
        } else {
            Float floatFreq = Float.parseFloat(frequency);
            floatFreq -= 0.1f;
            floatFreq = Math.round(floatFreq * 10) / 10f;
            return floatFreq.toString();
        }
    }
}
