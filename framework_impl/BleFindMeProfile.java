/*
 * Copyright (C) 2015 Tieto Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BleFindMeProfileCallback;
import android.content.Context;
import android.util.Log;

import java.util.List;
import java.util.UUID;

/**
 * This class provides the public APIs to control the Bluetooth FindMe profile.
 *
 *<p>BleFindMeProfile is a wrap object based on {@link BluetoothGatt}.
 */
public class BleFindMeProfile implements BluetoothProfile {

    private static final String TAG = "BleFindMeProfile";
    private static final String EX_MSG_ALERT_LEVEL_OUT_OF_RANGE="alert level out of range";

    private static final UUID IMMEDIATE_ALERT_SERVICE_UUID=UUID.fromString("00001802-0000-1000-8000-00805f9b34fb");
    private static final UUID ALERT_LEVEL_CHARACTER_UUID=UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb");

    public static final int ALERT_LEVEL_NO_ALERT=0;
    public static final int ALERT_LEVEL_MID=1;
    public static final int ALERT_LEVEL_HIGH=2;

    private Context mContext=null;
    private BluetoothDevice mBluetoothDevice=null;
    private BluetoothGatt mBluetoothGatt=null;
    private BleFindMeProfileCallback mBleFindMeProfileCallback=null;
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                if (mBluetoothGatt != null) {
                    mBluetoothGatt.discoverServices();
                }
            } else if(newState==BleFindMeProfile.STATE_DISCONNECTED){
                mAlertLevel=ALERT_LEVEL_NO_ALERT;
            }
            if (mBleFindMeProfileCallback != null) {
                mBleFindMeProfileCallback.onConnectionStateChanged(status, newState);
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

    private int mAlertLevel=ALERT_LEVEL_NO_ALERT;

    /**
     * Get current alert level
     * after it is done with this client.
     *
     * @return alert level.
     */
    public int getAlertLevel() {
        return mAlertLevel;
    }

    /**
     * Create a new BluetoothHrp
     *
     * @param callback the handler that will receive asynchronous callbacks.
     */
    public BleFindMeProfile(Context context, BleFindMeProfileCallback callback) {
        mContext = context;
        mBleFindMeProfileCallback = callback;
    }

    /**
     * Connect to a remote FindMe device.
     *
     * <p>Requires {@link android.Manifest.permission#BLUETOOTH} permission.
     *
     * @param device The remote FindMe device to connect to.
     * @param autoConnect Whether to directly connect to the remote device (false)
     *                    or to automatically connect as soon as the remote
     *                    device becomes available (true).
     *
     * @return true, if the connection attempt was initiated successfully.
     */
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

    /**
     * Disconnect an established connection, or cancel a connection attemp
     * currently in progress.
     *
     * <p>Requires {@link android.Manifest.permission#BLUETOOTH} permission.
     */
    public void disconnect() {
        if (mBluetoothGatt == null) {
            return;
        }

        mAlertLevel=ALERT_LEVEL_NO_ALERT;
        mBluetoothGatt.disconnect();
    }

    /**
     * Close FindMe client. Application should call this method as early as possible
     * after it is done with this client.
     *
     * @return true, if FindMe client was closed successfully.
     */
    public boolean close() {
        if (mBluetoothGatt == null) {
            return false;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
        mBluetoothDevice = null;
        mAlertLevel=ALERT_LEVEL_NO_ALERT;
        return true;
    }

    /**
     * Set alert level of remote FindMe device
     *
     * @param alertLevel Set to true to enable notification.
     * @return true, if alert level was set successfully..
     */
    public boolean findMe(int alertLevel) {
        if(alertLevel<ALERT_LEVEL_NO_ALERT || alertLevel>ALERT_LEVEL_HIGH) {
            throw new IllegalArgumentException(EX_MSG_ALERT_LEVEL_OUT_OF_RANGE);
        }

        BluetoothGattService immediateAlertService =
                mBluetoothGatt.getService(IMMEDIATE_ALERT_SERVICE_UUID);
        if (null == immediateAlertService) {
            return false;
        }

        BluetoothGattCharacteristic alertCharacter =
                immediateAlertService.getCharacteristic(ALERT_LEVEL_CHARACTER_UUID);
        if (null == alertCharacter) {
            return false;
        }

        alertCharacter.setValue(alertLevel, BluetoothGattCharacteristic.FORMAT_UINT8, 0);

        if (mBluetoothGatt.writeCharacteristic(alertCharacter)) {
            mAlertLevel=alertLevel;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Not supported - please use {@link BluetoothManager#getConnectedDevices(int)}
     * with {@link BluetoothProfile#GATT} as argument
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public List<BluetoothDevice> getConnectedDevices() {
        throw new UnsupportedOperationException
                ("Use BluetoothManager#getConnectedDevices instead.");
    }

    /**
     * Not supported - please use
     * {@link BluetoothManager#getDevicesMatchingConnectionStates(int, int[])}
     * with {@link BluetoothProfile#GATT} as first argument
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public List<BluetoothDevice> getDevicesMatchingConnectionStates(int[] states) {
        throw new UnsupportedOperationException
                ("Use BluetoothManager#getDevicesMatchingConnectionStates instead.");
    }

    /**
     * Not supported - please use {@link BluetoothManager#getConnectedDevices(int)}
     * with {@link BluetoothProfile#GATT} as argument
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public int getConnectionState(BluetoothDevice device) {
        throw new UnsupportedOperationException
                ("Use BluetoothManager#getConnectionState instead.");
    }
}
