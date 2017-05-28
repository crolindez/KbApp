package es.carlosrolindez.kbapp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import es.carlosrolindez.btcomm.btsppcomm.BtSppCommManager;


public class SelectBtFragment extends Fragment {
    public final static String TAG = "SelectBtFragment";

    private BtSppCommManager mBtSppCommManager = null;

    public BtSppCommManager getBtSppCommManager() {
        return mBtSppCommManager;
    }

    public static SelectBtFragment newInstance(BtSppCommManager manager) {
        SelectBtFragment fragment = new SelectBtFragment();
        fragment.mBtSppCommManager = manager;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.select_bt_fragment, container, false);
    }
}
