package es.carlosrolindez.kbapp;

class FmStation {
    private String name;
    private String frequency;
    private boolean forcedMono;

    FmStation() {
        name = null;
        frequency = "87.5";
        forcedMono = false;
    }

    FmStation(String freq) {
        super();
        frequency = freq;
    }

    boolean isForcedMono() {
        return forcedMono;
    }

    String showName() {
        if (name==null) return (frequency + " MHz");
        if (name.length()==0) return (frequency + " MHz");
        if (name.equals("        ")) return (frequency + " MHz");
        return name;
    }

    void setName(String newName) {
        name = newName;
    }

    String getFrequency() {
        return frequency;
    }

    String getName() {
        return name;
    }

    void copyStation(FmStation station) {
        this.frequency = station.getFrequency();
        this.setName(station.getName());
        this.setForcedMono(station.isForcedMono());
    }

    void setFrequency(String newFreq) {
        if (frequency.equals(newFreq)) return;
        frequency = newFreq;
        name = null;
    }

    void setForcedMono(boolean state) {
        forcedMono = state;
    }

    String stepUpFrequency(){
        if ( frequency.equals("108.0")) {
            return "87.5";
        } else {
            Float floatFreq = Float.parseFloat(frequency);
            floatFreq += 0.1f;
            floatFreq = Math.round(floatFreq * 10) / 10f;
            return floatFreq.toString();
        }
    }

    String stepDownFrequency(){
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
