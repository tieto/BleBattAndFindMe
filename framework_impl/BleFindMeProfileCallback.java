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

/**
 * This abstract class is used to implement {@link BleFindMeProfile} callbacks.
 */
public abstract class BleFindMeProfileCallback {

    /**
     * Callback indicating when FindMe client has connected/disconnected to/from
     * a remote FindMe device.
     *
     * @param status Status of the connect or disconnect operation.
     *               {@link BluetoothGatt#GATT_SUCCESS} if the operation succeeds.
     * @param newState Returns the new connection state. Can be one of
    {@link BluetoothProfile#STATE_DISCONNECTED} or
    {@link BluetoothProfile#STATE_CONNECTED}
     */
    public void onConnectionStateChanged(int status, int newState) {

    }
}
