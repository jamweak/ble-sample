package com.example.android.bluetoothadvertisements;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.sample.ble.library.common.Constants;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Manages BLE Advertising independent of the main app.
 * If the app goes off screen (or gets killed completely) advertising can continue because this
 * Service is maintaining the necessary Callback in memory.
 */
public class AdvertiserService extends Service {
    private static final String TAG = AdvertiserService.class.getSimpleName();
    private static final int FOREGROUND_NOTIFICATION_ID = 1;

    /**
     * A global variable to let AdvertiserFragment check if the Service is running without needing
     * to start or bind to it.
     * This is the best practice method as defined here:
     * https://groups.google.com/forum/#!topic/android-developers/jEvXMWgbgzE
     */
    public static boolean running = false;

    public static final String ADVERTISING_FAILED =
            "com.example.android.bluetoothadvertisements.advertising_failed";

    public static final String ADVERTISING_FAILED_EXTRA_CODE = "failureCode";

    public static final int ADVERTISING_TIMED_OUT = 6;

    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private BluetoothGattServer mGattServer;
    private BluetoothGattServerCallback mGattServerCallback;

    private AdvertiseCallback mAdvertiseCallback;

    private Handler mHandler;

    private Runnable timeoutRunnable;

    @Override
    public void onCreate() {
        running = true;
        initialize();
        startAdvertising();
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        /**
         * Note that onDestroy is not guaranteed to be called quickly or at all. Services exist at
         * the whim of the system, and onDestroy can be delayed or skipped entirely if memory need
         * is critical.
         */
        running = false;
        stopAdvertising();
        mHandler.removeCallbacks(timeoutRunnable);
        stopForeground(true);
        super.onDestroy();
    }

    /**
     * Required for extending service, but this will be a Started Service only, so no need for
     * binding.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Get references to system Bluetooth objects if we don't have them already.
     */
    private void initialize() {
        mGattServerCallback = new BluetoothGattServerCallbackImpl();
        if (mBluetoothLeAdvertiser == null) {
            BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager != null) {
                BluetoothAdapter mBluetoothAdapter = mBluetoothManager.getAdapter();
                if (mBluetoothAdapter != null) {
                    mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
                    prepareGattServices(mBluetoothManager);
                } else {
                    Toast.makeText(this, getString(R.string.bt_null), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, getString(R.string.bt_null), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void prepareGattServices(BluetoothManager bluetoothManager) {
        int retryCount = 0;
        // Retry only 3 times which is less than ANR threshold 5 seconds.
        while (mGattServer == null && retryCount++ < 3) {
            Log.w(TAG, "Gatt server is null, try to get it");
            SystemClock.sleep(1000);
            mGattServer = bluetoothManager.openGattServer(this, mGattServerCallback);

            if (mGattServer != null) {
                Log.d(TAG, "prepare gatt server");
                try {
                    BluetoothGattService service = new BluetoothGattService(
                            UUID.fromString(Constants.SBM_Service_UUID),
                            BluetoothGattService.SERVICE_TYPE_PRIMARY);
                    service.addCharacteristic(new BluetoothGattCharacteristic(
                            UUID.fromString(Constants.SBM_WRITE_CHARACTERISTIC_UUID),
                            BluetoothGattCharacteristic.PROPERTY_WRITE,
                            BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED));
                    BluetoothGattCharacteristic readCharacteristic = new BluetoothGattCharacteristic(
                            UUID.fromString(Constants.SBM_READ_CHARACTERISTIC_UUID),
                            BluetoothGattCharacteristic.PROPERTY_NOTIFY
                                    | BluetoothGattCharacteristic.PROPERTY_READ,
                            BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED);
                    readCharacteristic.addDescriptor(
                            new BluetoothGattDescriptor(UUID.fromString("00002902-0000-1000-8000-00805F9B34FB"),
                                    BluetoothGattDescriptor.PERMISSION_READ_ENCRYPTED
                                            | BluetoothGattDescriptor.PERMISSION_WRITE_ENCRYPTED));
                    service.addCharacteristic(readCharacteristic);
                    mGattServer.addService(service);
                } catch (Exception e) {
                    Log.w(TAG, "Failed to add service", e);
                    mGattServer = null;
                }
            }
        }
    }

    /**
     * Starts BLE Advertising.
     */
    private void startAdvertising() {
        goForeground();

        Log.d(TAG, "Service: Starting Advertising ");

        if (mAdvertiseCallback == null) {
            AdvertiseSettings settings = buildAdvertiseSettings();
            AdvertiseData data = buildAdvertiseData();
            mAdvertiseCallback = new SampleAdvertiseCallback();

            if (mBluetoothLeAdvertiser != null) {
                mBluetoothLeAdvertiser.startAdvertising(settings, data,
                        mAdvertiseCallback);
            }
        }
    }

    /**
     * Move service to the foreground, to avoid execution limits on background processes.
     * <p>
     * Callers should call stopForeground(true) when background work is complete.
     */
    @SuppressWarnings("NewApi")
    private void goForeground() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            CharSequence name = "test";
            int importance = NotificationManager.IMPORTANCE_MIN;
            NotificationChannel channel = new NotificationChannel("1", name, importance);
            manager.createNotificationChannel(channel);
        }
        Notification n = new Notification.Builder(this)
                .setContentTitle("Advertising device via Bluetooth")
                .setContentText("This device is discoverable to others nearby.")
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pendingIntent)
                .setChannelId("1")
                .build();
        startForeground(FOREGROUND_NOTIFICATION_ID, n);
    }

    /**
     * Stops BLE Advertising.
     */
    private void stopAdvertising() {
        Log.d(TAG, "Service: Stopping Advertising");
        if (mBluetoothLeAdvertiser != null) {
            mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
            mAdvertiseCallback = null;
        }
    }

    /**
     * Returns an AdvertiseData object which includes the Service UUID and Device Name.
     */
    private AdvertiseData buildAdvertiseData() {
        /**
         * Note: There is a strict limit of 31 Bytes on packets sent over BLE Advertisements.
         *  This includes everything put into AdvertiseData including UUIDs, device info, &
         *  arbitrary service or manufacturer data.
         *  Attempting to send packets over this limit will result in a failure with error code
         *  AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE. Catch this error in the
         *  onStartFailure() method of an AdvertiseCallback implementation.
         */

        AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();
        dataBuilder.addServiceUuid(ParcelUuid.fromString(Constants.SBM_Service_UUID));
        dataBuilder.setIncludeDeviceName(true);

        /* For example - this will cause advertising to fail (exceeds size limit) */
        //String failureData = "asdghkajsghalkxcjhfa;sghtalksjcfhalskfjhasldkjfhdskf";
        //dataBuilder.addServiceData(Constants.Service_UUID, failureData.getBytes());

        return dataBuilder.build();
    }

    /**
     * Returns an AdvertiseSettings object set to use low power (to help preserve battery life)
     * and disable the built-in timeout since this code uses its own timeout runnable.
     */
    private AdvertiseSettings buildAdvertiseSettings() {
        AdvertiseSettings.Builder settingsBuilder = new AdvertiseSettings.Builder();
        settingsBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER);
        settingsBuilder.setTimeout(0);
        return settingsBuilder.build();
    }

    /**
     * Custom callback after Advertising succeeds or fails to start. Broadcasts the error code
     * in an Intent to be picked up by AdvertiserFragment and stops this Service.
     */
    private class SampleAdvertiseCallback extends AdvertiseCallback {

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);

            Log.d(TAG, "Advertising failed");
            sendFailureIntent(errorCode);
            stopSelf();
        }

        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            Log.d(TAG, "Advertising successfully started");
        }
    }

    /**
     * Builds and sends a broadcast intent indicating Advertising has failed. Includes the error
     * code as an extra. This is intended to be picked up by the {@code AdvertiserFragment}.
     */
    private void sendFailureIntent(int errorCode) {
        Intent failureIntent = new Intent();
        failureIntent.setAction(ADVERTISING_FAILED);
        failureIntent.putExtra(ADVERTISING_FAILED_EXTRA_CODE, errorCode);
        sendBroadcast(failureIntent);
    }
}
