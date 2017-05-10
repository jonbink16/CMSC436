package edu.umd.cmsc436.sheets;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.sheets.v4.SheetsScopes;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

/**
 * Class to instantiate and hook into parts of the normal app
 */

public class Sheets implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private final String[] PERMISSIONS = new String[] {
            Manifest.permission.GET_ACCOUNTS
    };

    private final String SHARED_PREFS_NAME = "edu.umd.cmsc436.sheets";
    private final String PREF_ACCOUNT_NAME = "account name";

    private Host host;
    private Activity hostActivity;
    private GoogleAccountCredential credentials;

    private ServiceType cache_service;
    private String cache_userId;
    private String cache_folderId;
    private OnPrescriptionFetchedListener cache_prescriptionlistener;
    private String appName;
    private String spreadsheetId;
    private String privateSpreadsheetId;

    private Map<String, Float> cache_versionmap;
    private DriveApkTask.OnFinishListener cache_finishlistener;
    private List<UploadToDriveTask.DrivePayload> cache_imageQueue;
    private WriteDataTask.WriteData[] cache_spreadsheet_payload;

    private enum ServiceType {
        UploadPhoto,
        WriteData,
        WriteTrials,
        FetchPrescription,
        FetchApks,
    }

    public Sheets(Host host, Activity hostActivity, String appName, String spreadsheetId, String privateSpreadsheetId) {
        this.host = host;
        this.hostActivity = hostActivity;
        this.appName = appName;
        this.spreadsheetId = spreadsheetId;
        this.privateSpreadsheetId = privateSpreadsheetId;
        this.cache_imageQueue = new LinkedList<>();

        credentials = GoogleAccountCredential.usingOAuth2(hostActivity,
                Arrays.asList(SheetsScopes.SPREADSHEETS, DriveScopes.DRIVE)).setBackOff(new ExponentialBackOff());
    }

    public void writeData (TestType testType, String userId, float value) {
        writeDataBatch(new TestType[]{testType}, new String[]{userId}, new float[]{value});
    }

    public void writeDataBatch(TestType[] testTypes, String[] userIds, float[] values) {
        cache_service = ServiceType.WriteData;
        int length = Math.min(testTypes.length, Math.min(userIds.length, values.length));

        cache_spreadsheet_payload = new WriteDataTask.WriteData[length];
        for (int i = 0; i < length; i++) {
            cache_spreadsheet_payload[i] = new WriteDataTask.WriteData(testTypes[i], userIds[i], values[i]);
        }

        resumeWriteData();
    }

    private void resumeWriteData() {
        if (checkConnection()) {
            WriteDataTask writeDataTask = new WriteDataTask(credentials, spreadsheetId, appName, host, hostActivity);
            writeDataTask.execute(cache_spreadsheet_payload);
        }
    }

    public void fetchPrescription (String patientId, OnPrescriptionFetchedListener listener) {
        cache_service = ServiceType.FetchPrescription;
        cache_userId = patientId;
        cache_prescriptionlistener = listener;

        if (checkConnection()) {
            ReadPrescriptionTask readPrescriptionTask = new ReadPrescriptionTask(credentials, spreadsheetId, appName, host, hostActivity, listener);
            readPrescriptionTask.execute(patientId);
        }
    }

    public void fetchApks (String folderId, Map<String, Float> versionMap, DriveApkTask.OnFinishListener listener) {
        cache_service = ServiceType.FetchApks;
        cache_folderId = folderId;
        cache_versionmap = versionMap;
        cache_finishlistener = listener;

        if (checkConnection()) {
            launchDriveApkTask();
        }
    }

    public void writeTrials (TestType testType, String userId, float[] trials) {
        writeTrialsBatch(new TestType[]{testType}, new String[]{userId}, new float[][]{trials});
    }

    public void writeTrialsBatch(TestType[] testTypes, String[] userIds, float[][] trialsBatch) {
        cache_service = ServiceType.WriteTrials;
        int length = Math.min(testTypes.length, Math.min(userIds.length, trialsBatch.length));

        cache_spreadsheet_payload = new WriteDataTask.WriteData[length];
        for (int i = 0; i < length; i++) {
            cache_spreadsheet_payload[i] = new WriteDataTask.WriteData(testTypes[i], userIds[i], trialsBatch[i]);
        }

        resumeWriteTrials();
    }

    private void resumeWriteTrials() {
        if (checkConnection()) {
            WriteDataTask writeDataTask = new WriteDataTask(credentials, privateSpreadsheetId, appName, host, hostActivity);
            writeDataTask.execute(cache_spreadsheet_payload);
        }
    }

    public void uploadToDrive(String folderId, String fileName, Bitmap image) {
        uploadToDriveBatch(new String[]{folderId}, new String[]{fileName}, new Bitmap[]{image});
    }

    public void uploadToDriveBatch(String[] folderIds, String[] fileNames, Bitmap[] images) {
        cache_service = ServiceType.UploadPhoto;

        int length = Math.min(folderIds.length, Math.min(fileNames.length, images.length));
        for (int i = 0; i < length; i++) {
            UploadToDriveTask.DrivePayload payload = new UploadToDriveTask.DrivePayload(folderIds[i], fileNames[i], images[i]);
            cache_imageQueue.add(payload);
        }

        resumeUploadToDrive();
    }

    private void resumeUploadToDrive() {
        if (checkConnection()) {
            new GoogleApiClient.Builder(hostActivity)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build()
                    .connect();
        }
    }

    private void launchDriveApkTask () {
        cache_service = null;
        DriveApkTask driveApkTask = new DriveApkTask(credentials, appName, host, hostActivity);
        driveApkTask
                .setVersionMap(cache_versionmap)
                .setOnFinishListener(cache_finishlistener)
                .execute(new UploadToDriveTask.DrivePayload(cache_folderId, null, null));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        launchUploadToDriveTask();
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(hostActivity, host.getRequestCode(Action.REQUEST_CONNECTION_RESOLUTION));
            } catch (IntentSender.SendIntentException e) {
                // Unable to resolve, message user appropriately
                host.notifyFinished(e);
            }
        } else if (connectionResult.getErrorCode() == ConnectionResult.INTERNAL_ERROR) {
            // API reference suggests retrying.
            resume();
        } else {
            Log.e(getClass().getCanonicalName(), "Connection error " + connectionResult.getErrorCode() + ": " + connectionResult.getErrorMessage());
        }
    }

    private void launchUploadToDriveTask() {
        UploadToDriveTask uploadToDriveTask = new UploadToDriveTask(credentials, appName, host, hostActivity);
        UploadToDriveTask.DrivePayload[] payloads = new UploadToDriveTask.DrivePayload[cache_imageQueue.size()];

        // Unload image queue to the async task.
        payloads = cache_imageQueue.toArray(payloads);
        cache_imageQueue.clear();

        if (payloads.length > 0) {
            uploadToDriveTask.execute(payloads);
        }
    }

    public void onRequestPermissionsResult (int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == host.getRequestCode(Action.REQUEST_PERMISSIONS)) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                resume();
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == host.getRequestCode(Action.REQUEST_ACCOUNT_NAME)) {
            String accountName =
                    data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            if (accountName != null) {
                SharedPreferences settings =
                        hostActivity.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
                settings.edit().putString(PREF_ACCOUNT_NAME, accountName).apply();
                resume();
            }
        } else if (requestCode == host.getRequestCode(Action.REQUEST_PLAY_SERVICES)) {
            if (resultCode != RESULT_OK) {
                Log.e(this.getClass().getSimpleName(), "Requires Google Play Services");
            } else {
                resume();
            }
        } else if (requestCode == host.getRequestCode(Action.REQUEST_AUTHORIZATION)) {
            if (resultCode == RESULT_OK) {
                resume();
            }
        } else if (requestCode == host.getRequestCode(Action.REQUEST_CONNECTION_RESOLUTION)) {
            if (resultCode == RESULT_OK) {
                if (cache_service == ServiceType.FetchApks) {
                    launchDriveApkTask();
                } else {
                    launchUploadToDriveTask();
                }
            }
        }
    }

    private void resume() {
        switch (cache_service) {
            case WriteData:
                resumeWriteData();
                break;
            case WriteTrials:
                resumeWriteTrials();
                break;
            case UploadPhoto:
                resumeUploadToDrive();
                break;
            case FetchPrescription:
                fetchPrescription(cache_userId, cache_prescriptionlistener);
                break;
            case FetchApks:
                fetchApks(cache_folderId, cache_versionmap, cache_finishlistener);
                break;
            default:
                break;
        }
    }

    private boolean checkConnection() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int statusCode = apiAvailability.isGooglePlayServicesAvailable(hostActivity);
        if (statusCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(statusCode)) {
                showGooglePlayErrorDialog(host, hostActivity);
            }

            return false;
        }

        if (credentials.getSelectedAccountName() == null) {
            // if not on Marshmallow then the manifest takes care of the permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    && hostActivity.checkSelfPermission(PERMISSIONS[0]) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(hostActivity, PERMISSIONS, host.getRequestCode(Action.REQUEST_PERMISSIONS));

                return false;
            }

            SharedPreferences prefs = hostActivity.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
            String accountName = prefs.getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                credentials.setSelectedAccountName(accountName);
            } else {
                hostActivity.startActivityForResult(credentials.newChooseAccountIntent(), host.getRequestCode(Action.REQUEST_ACCOUNT_NAME));
                return false;
            }
        }

        ConnectivityManager connectivityManager =
                (ConnectivityManager) hostActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            host.notifyFinished(new NoNetworkException());
            return false;
        }

        return true;
    }

    static void showGooglePlayErrorDialog(Host host, Activity hostActivity) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int statusCode = apiAvailability.isGooglePlayServicesAvailable(hostActivity);
        apiAvailability.getErrorDialog(hostActivity, statusCode, host.getRequestCode(Action.REQUEST_PLAY_SERVICES)).show();
    }

    public interface Host {

        int getRequestCode(Action action);

        void notifyFinished(Exception e);
    }

    public interface OnPrescriptionFetchedListener {
        void onPrescriptionFetched (@Nullable List<String> raw_data);
    }

    public enum Action {
        REQUEST_PERMISSIONS,
        REQUEST_ACCOUNT_NAME,
        REQUEST_PLAY_SERVICES,
        REQUEST_AUTHORIZATION,
        REQUEST_CONNECTION_RESOLUTION
    }

    @SuppressWarnings("WeakerAccess")
    public static class NoNetworkException extends Exception {}

    public enum TestType {
        LH_TAP("'Tapping Test (LH)'"),
        RH_TAP("'Tapping Test (RH)'"),
        LF_TAP("'Tapping Test (LF)'"),
        RF_TAP("'Tapping Test (RF)'"),
        LH_SPIRAL("'Spiral Test (LH)'"),
        RH_SPIRAL("'Spiral Test (RH)'"),
        LH_LEVEL("'Level Test (LH)'"),
        RH_LEVEL("'Level Test (RH)'"),
        LH_POP("'Balloon Test (LH)'"),
        RH_POP("'Balloon Test (RH)'"),
        LH_CURL("'Curling Test (LH)'"),
        RH_CURL("'Curling Test (RH)'"),
        LF_CURL("'Curling Test (LF)'"),
        RF_CURL("'Curling Test (RF)'"),
        SWAY_OPEN_APART("'Swaying Test (Legs apart eyes open)'"),
        SWAY_OPEN_TOGETHER("'Swaying Test (Legs closed eyes open)'"),
        SWAY_CLOSED("'Swaying Test (Legs closed eyes closed)'"),
        INDOOR_WALKING("'Indoor Walking Test'"),
        OUTDOOR_WALKING("'Outdoor Walking Test'"),
        MEMORY("'Memory Test'"),
        VIBRATION("'Vibration Test'");

        private final String id;

        TestType(String sheetId) {
            id = sheetId;
        }

        public String toId() {
            return id;
        }
    }


}
