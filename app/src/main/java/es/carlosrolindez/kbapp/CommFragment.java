package es.carlosrolindez.kbapp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import es.carlosrolindez.btcomm.btsppcomm.BtSppCommManager;


public class CommFragment extends Fragment {
    public final static String TAG = "CommFragment";

    private BtSppCommManager mBtSppCommManager = null;

    public BtSppCommManager getSppCommManager() {
        return mBtSppCommManager;
    }

    public boolean isSockedConnected()  {
        if (mBtSppCommManager != null)
            return (mBtSppCommManager.isSocketConnected());
        return false;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mBtSppCommManager = new BtSppCommManager(getActivity().getApplicationContext());
    }
}
