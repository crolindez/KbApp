package es.carlosrolindez.kbapp;
// TODO Does not rotate well
// TODO Save in Github
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import es.carlosrolindez.btcomm.BtDevice;


class BtDeviceListAdapter extends BaseAdapter {
	private static String TAG = "BtDeviceListAdapter";

	private final LayoutInflater inflater;
	private final ArrayBtDevice mBtDeviceList;
    private final Context mContext;
    private final BtConnectionInterface mBtInterface;

    private boolean viewLocked = false;
    private int numViewLocked = 0;
    private RelativeLayout lockedLayout = null;
    private ImageView lockedButton = null;

    private final int mShortAnimationDuration;
    private final int mLongAnimationDuration;

	
	public BtDeviceListAdapter(Context context, ArrayBtDevice deviceList, BtConnectionInterface btInterface)
	{
		mBtDeviceList = deviceList;
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
        mBtInterface = btInterface;
        mShortAnimationDuration = mContext.getResources().getInteger(android.R.integer.config_shortAnimTime);
        mLongAnimationDuration = mContext.getResources().getInteger(android.R.integer.config_longAnimTime);
	}
	
	@Override
	public int getCount()
	{
		if (mBtDeviceList == null)
			return 0;
		else
			return mBtDeviceList.size();
	}
	
	@Override
	public Object getItem(int position)
	{
		if (mBtDeviceList == null)
			return 0;
		else			
			return mBtDeviceList.get(position);
	}
	
	@Override
	public long getItemId(int position)
	{
			return position;
	}
	

    @Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
	    if (mBtDeviceList == null)
	    	return null;

		final BtDevice device =  mBtDeviceList.get(position);
	    
		View localView = convertView;
	
		if (localView==null)
		{
			localView = inflater.inflate(R.layout.device_list_row, parent, false);
		}

		TextView deviceName = (TextView)localView.findViewById(R.id.device_name);
		TextView deviceAddress = (TextView)localView.findViewById(R.id.device_address);
        ImageView bluetoothIcon = (ImageView) localView.findViewById(R.id.bluetooth_icon);
        AnimatedVectorDrawable animatedBluetooth= (AnimatedVectorDrawable) mContext.getDrawable(R.drawable.animated_bluetooth);

		RelativeLayout mainLayout = (RelativeLayout)localView.findViewById(R.id.device_list_layout);
		deviceName.setText(device.deviceName);
		deviceAddress.setText(device.getAddress());


        if (device.deviceConnected) {
            mainLayout.setBackgroundResource(R.drawable.connected_selector);
        } else {
            mainLayout.setBackgroundResource(R.drawable.notconnected_selector);
            if (device.deviceBonded) {
                deviceName.setTypeface(Typeface.DEFAULT_BOLD);
                deviceAddress.setTypeface(Typeface.DEFAULT_BOLD);
            } else {
                deviceName.setTypeface(Typeface.DEFAULT);
                deviceAddress.setTypeface(Typeface.DEFAULT);
            }
        }
        if (device.getDeviceInProcess()) {
            bluetoothIcon.setVisibility(View.VISIBLE);
            bluetoothIcon.setImageDrawable(animatedBluetooth);
            animatedBluetooth.start();
        }
        else {
            bluetoothIcon.setVisibility(View.INVISIBLE);
            animatedBluetooth.stop();
        }


        ImageView deleteButton = (ImageView) localView.findViewById(R.id.device_delete);
        RelativeLayout deleteLayout = (RelativeLayout)localView.findViewById(R.id.delete_list_layout);

        TextView password = (TextView)localView.findViewById(R.id.password);
		password.setText(""+password(device.getAddress()));
        localView.setOnTouchListener(new SwipeView(mainLayout, deleteLayout, (ListView) parent, device, position, deleteButton));
		return localView;
	}

    @Override
    public void notifyDataSetChanged() {
        if (lockedLayout!=null) {
            restoreAnimatedLayout(lockedLayout);
            unlock();
        }
        super.notifyDataSetChanged();
    }


    private void unlock() {
        viewLocked = false;
        if (lockedButton!=null) lockedButton.setClickable(false);
        lockedButton = null;
        lockedLayout = null;
    }

    private void restoreAnimatedLayout(final RelativeLayout layout) {
        if (layout==null) return;
        final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout.getLayoutParams();

        layout.animate()
                .translationX(params.rightMargin)
                .setDuration(mShortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        params.rightMargin = 0;
                        params.leftMargin = 0;
                        layout.setLayoutParams(params);
                        layout.setTranslationX(0);
                    }
                });
    }

    private void deleteAnimatedLayout(final RelativeLayout layout, final BtDevice btDevice) {

        if (layout==null) return;

        final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout.getLayoutParams();
        final int right = layout.getRight();

        ObjectAnimator moveUp = ObjectAnimator.ofInt(layout, "Left", right);
        moveUp.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                params.rightMargin = 0;
                params.leftMargin = 0;
                layout.setLayoutParams(params);
                layout.setRight(right);
                if (btDevice!=null)
                    mBtDeviceList.remove(btDevice);
                notifyDataSetChanged();
            }
        });
        moveUp.start();

  /*      layout.animate()
                .scaleX(0f)
                .scaleY(0f)
                .setDuration(mLongAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        params.rightMargin = 0;
                        params.leftMargin = 0;
                        layout.setLayoutParams(params);
                        layout.setScaleX(1f);
                        layout.setScaleY(1f);
                        if (btDevice!=null)
                            mBtDeviceList.remove(btDevice);
                        notifyDataSetChanged();
                    }
                });*/
    }


    public class SwipeView implements View.OnTouchListener {

        private final int mSlop;
        private float mDownX;
        private boolean motionInterceptDisallowed;

        private final RelativeLayout mainLayout;
        private final RelativeLayout deleteLayout;
        private final ListView mListView;
        private final BtDevice btDevice;
        private final int limitWidth;
        private final int localPosition;
        private final ImageView mDeleteButton;
        private final boolean moveable;




        public SwipeView(RelativeLayout main, RelativeLayout delete, ListView list, BtDevice device, int position, ImageView deleteButton) {
            ViewConfiguration vc = ViewConfiguration.get(mContext);
            mSlop = vc.getScaledTouchSlop();
            mainLayout = main;
            deleteLayout = delete;
            mListView = list;
            btDevice = device;
            localPosition = position;
            limitWidth = (int)mContext.getResources().getDimension(R.dimen.delete_button_size);
            mDeleteButton = deleteButton;
            moveable = !device.deviceConnected;
        }



        private void lock() {
            viewLocked = true;
            numViewLocked = localPosition;
            lockedLayout = mainLayout;
            lockedButton = mDeleteButton;
            lockedButton.setClickable(true);
            lockedButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mBtInterface.forgetBluetoothA2dp(btDevice);
                    deleteAnimatedLayout(lockedLayout,btDevice);
                    unlock();

                }
            });

        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mainLayout.getLayoutParams();

            switch (motionEvent.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                {
                    if (viewLocked && (numViewLocked!=localPosition)) {  //undo lock
                        restoreAnimatedLayout(lockedLayout);
                        unlock();
                    }
                    mDownX = motionEvent.getRawX();
                    motionInterceptDisallowed = false;
                    view.setPressed(true);
                }

                return true;

                case MotionEvent.ACTION_MOVE:
                {
                    float deltaX = motionEvent.getRawX() - mDownX;
                    if ( (Math.abs(deltaX) > mSlop) && !motionInterceptDisallowed ) {
                        mListView.requestDisallowInterceptTouchEvent(true);
                        motionInterceptDisallowed = true;
                        view.setPressed(false);
                    }
                    if (!moveable) return true;

                    if (deltaX>0) { // Right
                        deleteLayout.setVisibility(View.VISIBLE);
                        if (deltaX>limitWidth) {
                            if (!viewLocked) {
                                lock();
                            }
                            params.rightMargin = -limitWidth;
                            params.leftMargin = limitWidth;
                            mainLayout.setLayoutParams(params);

                        } else {
                            unlock();
                            params.rightMargin = -(int) deltaX;
                            params.leftMargin = (int) deltaX;
                            mainLayout.setLayoutParams(params);
                        }

                    } else { // left
                        unlock();
                        params.rightMargin = -(int)deltaX;
                        params.leftMargin = (int)deltaX;
                        mainLayout.setLayoutParams(params);
                        deleteLayout.setVisibility(View.INVISIBLE);
                    }



                    return true;
                }

                case MotionEvent.ACTION_UP:
                {
                    view.setPressed(false);

                    if (motionInterceptDisallowed) {
                        if (!viewLocked) {
                            restoreAnimatedLayout(mainLayout);
                        }
                        mListView.requestDisallowInterceptTouchEvent(false);
                        motionInterceptDisallowed = false;
                    } else {
                        restoreAnimatedLayout(mainLayout);
                        unlock();
                        mBtInterface.toggleBluetoothA2dp(btDevice);
                    }

                    return true;

                }

                case MotionEvent.ACTION_CANCEL:
                {
                    restoreAnimatedLayout(mainLayout);
                    unlock();
                    mListView.requestDisallowInterceptTouchEvent(false);
                    motionInterceptDisallowed = false;

                    return false;

                }
            }
            return true;
        }


    }



    private long password(String MAC) {

		String[] macAddressParts = MAC.split(":");
		long littleMac = 0;
		int rotation;
		long code = 0;
		long pin;

		for(int i=2; i<6; i++) {
			Long hex = Long.parseLong(macAddressParts[i], 16);
			littleMac *= 256;
			littleMac += hex;
		}

		rotation = Integer.parseInt(macAddressParts[5], 16) & 0x0f;

		for(int i=0; i<4; i++) {
			Long hex =  Long.parseLong(macAddressParts[i], 16);
			code *= 256;
			code += hex;
		}
		code = code >> rotation;
		code &= 0xffff;

		littleMac &= 0xffff;

		pin = littleMac ^ code;
		pin %= 10000;

		return pin;

	}


}
