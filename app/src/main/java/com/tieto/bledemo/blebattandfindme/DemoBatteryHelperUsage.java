package com.tieto.bledemo.blebattandfindme;


import android.bluetooth.BleBatteryLevelHelper;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

/**
 * Created by chennhua on 4/27/15.
 */
public class DemoBatteryHelperUsage {

    private static final String TAG = "DemoBatteryHelperUsage";

    private Context mContext=null;
    private BluetoothDevice mBluetoothDevice=null;
    private BluetoothGatt mBluetoothGatt=null;
    private BleBatteryLevelHelper mBleBatteryLevelHelper=null;
    private DemoBatteryHelperUsageCallback mClientCallback=null;
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                if (mBluetoothGatt != null) {
                    mBluetoothGatt.discoverServices();
                }
            }
            if (mClientCallback != null) {
                mClientCallback.onConnectionStateChanged(status, newState);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d(TAG, "onServicesDiscovered");
        }

        @Override
        public void onDescriptorRead(android.bluetooth.BluetoothGatt gatt, android.bluetooth.BluetoothGattDescriptor descriptor, int status) {
            Log.d(TAG, "onDescriptorRead");
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                                      int status) {
            Log.d(TAG, "onDescriptorWrite");
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG, "onCharacteristicWrite");
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG, "onCharacteristicRead");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            BleBatteryLevelHelper.BatteryLevelData data=
                    mBleBatteryLevelHelper.readBatteryLevel(gatt, characteristic);
            if(null!=mClientCallback&&null!=data) {
                mClientCallback.onBatteryLevelChanged(data.getBatteryLevel(),
                        data.getNamespace(),data.getDescription());
            }
            Log.d(TAG, "onCharacteristicChanged");
        }

        @Override
        public void onReliableWriteCompleted(android.bluetooth.BluetoothGatt gatt, int status) {
            Log.d(TAG, "onReliableWriteCompleted");
        }

        @Override
        public void onReadRemoteRssi(android.bluetooth.BluetoothGatt gatt, int rssi, int status) {
            Log.d(TAG, "onReadRemoteRssi");
        }

        @Override
        public void onMtuChanged(android.bluetooth.BluetoothGatt gatt, int mtu, int status) {
            Log.d(TAG, "onMtuChanged");
        }
    };

    public DemoBatteryHelperUsage(Context context, DemoBatteryHelperUsageCallback callback) {
        mContext = context;
        mClientCallback = callback;
        mBleBatteryLevelHelper=new BleBatteryLevelHelper();
    }

    public boolean connect(BluetoothDevice device, boolean autoConnect) {
        if (device == null) {
            return false;
        }

        if (mBluetoothDevice != null && device.equals(mBluetoothDevice) &&
                mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                return true;
            } else {
                return false;
            }
        }

        mBluetoothGatt = device.connectGatt(mContext, autoConnect, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDevice = device;
        return true;
    }

    public void disconnect() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.disconnect();
    }

    public boolean close() {
        if (mBluetoothGatt == null) {
            return false;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
        mBluetoothDevice = null;

        return true;
    }

    public boolean setBattNotification(boolean enable) {
        return mBleBatteryLevelHelper.setNotification(mBluetoothGatt, enable);
    }
}