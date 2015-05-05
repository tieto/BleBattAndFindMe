package com.tieto.bledemo.blebattandfindme;

import android.app.Activity;
import android.bluetooth.BleFindMeProfile;
import android.bluetooth.BleFindMeProfileCallback;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.os.Handler;

public class DeviceControlActivity extends Activity {

    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private BluetoothManager mBluetoothManager=null;
    private BluetoothAdapter mBluetoothAdapter=null;
    private BluetoothDevice mDevice=null;
    private BleFindMeProfile mBleFindMeProfile=null;
    private TextView mTxtConnectionState=null;
    private TextView mTxtDeviceAddress=null;
    private Spinner mSpinnerAlertLevel=null;
    private Button mBtnConnFindMe=null;
    private Button mBtnConnBatt=null;
    private TextView mTxtBattryLevel=null;

    private boolean mConnected = false;
    private boolean mBattConnected = false;
    private boolean mFindMeConnected = false;
    private boolean mNotificationOn = false;
    private Handler callbackHandler=null;

    private DemoBatteryHelperUsage mDemoBatteryHelperUsage=null;
    private DemoBatteryHelperUsageCallback mDemoBatteryCallback=new DemoBatteryHelperUsageCallback(){

        @Override
        public void onConnectionStateChanged(final int status,final int newState) {
            callbackHandler.postDelayed(
                    new Runnable() {
                        @Override
                        public void run() {
                            if (newState == BluetoothProfile.STATE_CONNECTED) {
                                mConnected = true;
                                mBattConnected=true;
                                updateConnectionState(R.string.connected);
                                invalidateOptionsMenu();

                                boolean ret=mDemoBatteryHelperUsage.setBattNotification(true);
                                Log.i(TAG, "batt notification status="+(ret?"on":"off"));

                            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                                mConnected = false;
                                mBattConnected=false;
                                updateConnectionState(R.string.disconnected);
                                invalidateOptionsMenu();
                            }

                            invalidateUIState();
                        }
                    }
                    ,10);
        }

        @Override
        public void onBatteryLevelChanged(final int batteryLevel,final int namespace,final int description) {

            callbackHandler.postDelayed(            new Runnable(){
                @Override
                public void run() {
                    if(null!=mTxtBattryLevel) {
                        mTxtBattryLevel.setText(batteryLevel+"%");
                    }
                }
            },10);
        }
    };

    public class AlertLevelSelectedListener implements OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
            if(mBleFindMeProfile!=null&&mFindMeConnected) {
                switch (pos) {
                    case 1:
                        mBleFindMeProfile.findMe(BleFindMeProfile.ALERT_LEVEL_MID);
                        break;
                    case 2:
                        mBleFindMeProfile.findMe(BleFindMeProfile.ALERT_LEVEL_HIGH);
                        break;
                    default:
                        mBleFindMeProfile.findMe(BleFindMeProfile.ALERT_LEVEL_NO_ALERT);
                        break;
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub
        }
    }

    private final BleFindMeProfileCallback
            mBleFindMeProfileCallback = new BleFindMeProfileCallback() {
        @Override
        public void onConnectionStateChanged(final int status,final int newState) {

            callbackHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        mConnected = true;
                        mFindMeConnected=true;
                        updateConnectionState(R.string.connected);
                        invalidateOptionsMenu();
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        mConnected = false;
                        mFindMeConnected=false;
                        updateConnectionState(R.string.disconnected);
                        invalidateOptionsMenu();
                    }

                    Log.i(TAG, "onConnectionStateChanged.");

                    invalidateUIState();
                }
            } ,10);
        }
    };

    private String mDeviceAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_control);

        callbackHandler=new Handler();

        mSpinnerAlertLevel=(Spinner) findViewById(R.id.spin_alert_level);
        mSpinnerAlertLevel.setOnItemSelectedListener(new AlertLevelSelectedListener());

        mTxtConnectionState = (TextView) findViewById(R.id.connection_state);
        mTxtDeviceAddress = (TextView) findViewById(R.id.device_address);

        mBtnConnFindMe=(Button) findViewById(R.id.btnConnFindMe);
        mBtnConnFindMe.setOnClickListener(new OnClickListener(){
            public void onClick(android.view.View view)  {
                disableUI();
                connectFindMe(mDeviceAddress);
            }
        });
        mBtnConnBatt=(Button) findViewById(R.id.btnConnBatt);
        mBtnConnBatt.setOnClickListener(new OnClickListener(){
            public void onClick(android.view.View view)  {
                disableUI();
                connectBattService(mDeviceAddress);
            }
        });
        mTxtBattryLevel=(TextView) findViewById(R.id.battery_level);

        final Intent intent = getIntent();
        String deviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                //return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            //return false;
        }

        mTxtDeviceAddress.setText(mDeviceAddress);

        mBleFindMeProfile=new BleFindMeProfile(getBaseContext(), mBleFindMeProfileCallback);
        mDemoBatteryHelperUsage=new DemoBatteryHelperUsage(getBaseContext(),mDemoBatteryCallback);


        getActionBar().setTitle(deviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_device_control, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if( id == android.R.id.home) {
            Intent intent =new Intent();
            intent.setClass(this,MainActivity.class);
            startActivity(intent);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean connectFindMe(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        mBleFindMeProfile.connect(device, false);
        Log.d(TAG, "Trying to create a connection.");
        return true;
    }

    private boolean connectBattService(final String address) {

        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        mDemoBatteryHelperUsage.connect(device,false);
        Log.d(TAG, "Trying to create a connection.");
        return true;
    }

    private void invalidateUIState() {

        if(mFindMeConnected){
            mBtnConnFindMe.setEnabled(false);
        } else if(!mBattConnected){
            mBtnConnFindMe.setEnabled(true);
        } else {
            mBtnConnFindMe.setEnabled(false);
        }

        if(mBattConnected){
            mBtnConnBatt.setEnabled(false);
        } else if(!mFindMeConnected) {
            mBtnConnBatt.setEnabled(true);
        } else {
            mBtnConnBatt.setEnabled(false);
        }
    }

    private void disableUI() {
        mBtnConnFindMe.setEnabled(false);
        mBtnConnBatt.setEnabled(false);
    }

    private void enableUI() {
        mBtnConnFindMe.setEnabled(true);
        mBtnConnFindMe.setText(R.string.button_findme_connect);
        mBtnConnBatt.setEnabled(true);
        mBtnConnBatt.setText(R.string.button_batt_connect);
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTxtConnectionState.setText(resourceId);
            }
        });
    }
}
