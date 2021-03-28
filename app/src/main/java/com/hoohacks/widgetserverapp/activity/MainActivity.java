package com.hoohacks.widgetserverapp.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hoohacks.widgetserverapp.BuildConfig;
import com.hoohacks.widgetserverapp.R;
import com.hoohacks.widgetserverapp.data.CaliberateData;
import com.hoohacks.widgetserverapp.data.DeviceData;
import com.hoohacks.widgetserverapp.data.ExcelData;
import com.hoohacks.widgetserverapp.data.WebsiteData;
import com.hoohacks.widgetserverapp.network.APIClient;
import com.hoohacks.widgetserverapp.network.Api;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    boolean isProcessing = false;
    ArrayList<DeviceData> dataList;

    boolean isDataGenerated = false;

    ArrayList<ExcelData> excelDataList;

    Button btnExcel;

    File csvFileFinal;
    WritableWorkbook workbook;
    String fileTimeStamp = "";

    double finalAccDiff = 0.0;

    String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dataList = new ArrayList<>();
        excelDataList = new ArrayList<>();
        btnExcel = findViewById(R.id.btnExcel);

        btnExcel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isDataGenerated) {
                    List<String> permList = new ArrayList<>();
                    for (String perm : permissions) {
                        int res = ActivityCompat.checkSelfPermission(MainActivity.this, perm);
                        if (res == PackageManager.PERMISSION_DENIED) {
                            permList.add(perm);
                        }
                    }

                    if (permList.isEmpty()) {
                        createSheet();
                    } else {
                        ActivityCompat.requestPermissions(MainActivity.this, permList.toArray(new String[permList.size()]), 101);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Data not generated.", Toast.LENGTH_LONG).show();
                }
            }
        });
        getActivityLevel();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            createSheet();
        }
    }

    private void createSheet() {
        try {
            csvFileFinal = getExcelFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        WorkbookSettings wbSettings = new WorkbookSettings();
        wbSettings.setLocale(new Locale("en", "EN"));
        try {
            workbook = Workbook.createWorkbook(csvFileFinal, wbSettings);
            createFirstSheet();
            //closing cursor
            workbook.write();
            workbook.close();


            Uri excelURI = FileProvider.getUriForFile(MainActivity.this,
                    BuildConfig.APPLICATION_ID + ".fileprovider",
                    csvFileFinal);

            Intent intentShareFile = new Intent(Intent.ACTION_SEND);
            intentShareFile.setType("text/*");
            intentShareFile.putExtra(Intent.EXTRA_STREAM, excelURI);
            startActivity(intentShareFile);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void sendNotification() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("to", "/topics/active");
            JSONObject notificationBody = new JSONObject();
            notificationBody.put("title", "High Activity Detected!!!");
            notificationBody.put("body", "High Activity Detected from the fidget. You can refer our website for more details.");

            jsonObject.put("data", notificationBody);

            RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), jsonObject.toString());

            Call<ResponseBody> call = APIClient.getClient().create(Api.class).sendNotification("key=" + getString(R.string.fcm_key), body);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Log.d("hello", "Res : " + response.code() + " " + response.message());
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.d("hello", "Res Failed : " + t.getMessage());
                }
            });


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void createFirstSheet() {
        WritableSheet sheet = workbook.createSheet("records", 0);
        try {
            sheet.addCell(new Label(0, 0, "accX"));
            sheet.addCell(new Label(1, 0, "accY"));
            sheet.addCell(new Label(2, 0, "accZ"));
            sheet.addCell(new Label(3, 0, "accfINAL"));
            sheet.addCell(new Label(4, 0, "flex"));
            sheet.addCell(new Label(5,0,"re"));
            sheet.addCell(new Label(6, 0, "calibrationAcc"));
            sheet.addCell(new Label(7, 0, "calibrationFlex"));
            sheet.addCell(new Label(8,0,"calibrationRE"));
            sheet.addCell(new Label(9, 0, "modAcc"));
            sheet.addCell(new Label(10, 0, "modFlex"));
            sheet.addCell(new Label(11,0,"modRE"));
            sheet.addCell(new Label(12, 0, "activity"));

            for (int i = 0; i < excelDataList.size(); i++) {
                sheet.addCell(new Label(0, i + 1, excelDataList.get(i).getAccX()));
                sheet.addCell(new Label(1, i + 1, excelDataList.get(i).getAccY()));
                sheet.addCell(new Label(2, i + 1, excelDataList.get(i).getAccZ()));
                sheet.addCell(new Label(3, i + 1, excelDataList.get(i).getAccFinal()));
                sheet.addCell(new Label(4, i + 1, excelDataList.get(i).getFlex()));
                sheet.addCell(new Label(5, i + 1, excelDataList.get(i).getRe()));
                sheet.addCell(new Label(6, i + 1, excelDataList.get(i).getDiffAcc()));
                sheet.addCell(new Label(7, i + 1, excelDataList.get(i).getDiffFlex()));
                sheet.addCell(new Label(8, i + 1, excelDataList.get(i).getDiffRe()));
                sheet.addCell(new Label(9, i + 1, excelDataList.get(i).getModAcc()));
                sheet.addCell(new Label(10, i + 1, excelDataList.get(i).getModFlex()));
                sheet.addCell(new Label(11, i + 1, excelDataList.get(i).getModRe()));
                sheet.addCell(new Label(12, i + 1, excelDataList.get(i).getActivity()));
            }

        } catch (WriteException e) {
            e.printStackTrace();
        }
    }

    private File getExcelFile() throws IOException {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMyyyy", Locale.ENGLISH);
        fileTimeStamp = simpleDateFormat.format(calendar.getTime());
        String pictureFile = fileTimeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(pictureFile, ".xls", storageDir);
        return image;
    }

    private void getActivityLevel() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("Calibration");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                    CaliberateData caliberateData = dataSnapshot.getValue(CaliberateData.class);
                    double accXDiff = caliberateData.getAcc().getX().getHigh() - caliberateData.getAcc().getX().getLow();
                    double accYDiff = caliberateData.getAcc().getY().getHigh() - caliberateData.getAcc().getY().getLow();
                    double accZDiff = caliberateData.getAcc().getZ().getHigh() - caliberateData.getAcc().getZ().getLow();

                    double flexDiff = caliberateData.getFlex().getHigh() - caliberateData.getFlex().getLow();
                    double reDiff = caliberateData.getFlex().getHigh() - caliberateData.getFlex().getLow();

                    DatabaseReference databaseReference1 = firebaseDatabase.getReference("Devices/DeviceId1");

                    databaseReference1.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if (dataList.size() < 5) {
                                DeviceData deviceData = snapshot.getValue(DeviceData.class);
                                Log.d("helloAcc", "Acc : " + "x - " + deviceData.getAcc().getX() + " " + "y - " + deviceData.getAcc().getY() + " z - " + deviceData.getAcc().getZ());
                                Log.d("helloFlex", "Flex : " + deviceData.getFlex());
                                Log.d("helloRE", "RE : " + deviceData.getRE());

                                if (deviceData.getAcc().getX() >= deviceData.getAcc().getY() && deviceData.getAcc().getX() >= deviceData.getAcc().getZ()) {
                                    finalAccDiff = deviceData.getAcc().getX();
                                } else if (deviceData.getAcc().getY() >= deviceData.getAcc().getX() && deviceData.getAcc().getY() >= deviceData.getAcc().getZ()) {
                                    finalAccDiff = deviceData.getAcc().getY();
                                } else {
                                    finalAccDiff = deviceData.getAcc().getZ();
                                }

                                ExcelData excelData = new ExcelData(String.valueOf(deviceData.getAcc().getX()), String.valueOf(deviceData.getAcc().getY()), String.valueOf(deviceData.getAcc().getZ()), "NA", String.valueOf(deviceData.getFlex()),String.valueOf(deviceData.getRE()), "NA", "NA", "NA","NA", "NA", "NA","NA");
                                excelDataList.add(excelData);

                                dataList.add(deviceData);
                            }

                            if (dataList.size() == 5 && !isProcessing) {
                                isProcessing = true;
                                double modAcc = 0;
                                double modFlex = 0;
                                double modRE = 0;

                                double[] temp = new double[dataList.size()];
                                int[] accType = new int[dataList.size()];

                                for (int i = 0; i < dataList.size(); i++) {
                                    if (dataList.get(i).getAcc().getX() >= dataList.get(i).getAcc().getY() && dataList.get(i).getAcc().getX() >= dataList.get(i).getAcc().getZ()) {
                                        modAcc = dataList.get(i).getAcc().getX();
                                        accType[i] = 1;
                                    } else if (dataList.get(i).getAcc().getY() >= dataList.get(i).getAcc().getX() && dataList.get(i).getAcc().getY() >= dataList.get(i).getAcc().getZ()) {
                                        modAcc = dataList.get(i).getAcc().getY();
                                        accType[i] = 2;
                                    } else {
                                        modAcc = dataList.get(i).getAcc().getZ();
                                        accType[i] = 3;
                                    }

                                    temp[i] = modAcc;
                                    if(i<dataList.size()-1) {
                                        modFlex = modFlex + Math.abs(dataList.get(i).getFlex() - dataList.get(i + 1).getFlex());
                                        modRE = modRE + Math.abs(dataList.get(i).getRE() - dataList.get(i + 1).getRE());
                                    }
                                }
                                double tempValue = 0.0;
                                int tempAccType = 0;
                                for (int i = 0; i < temp.length; i++) {
                                    for (int j = i + 1; j < temp.length; j++) {
                                        if (temp[i] > temp[j]) {
                                            tempAccType = accType[i];
                                            accType[i] = accType[j];
                                            accType[j] = tempAccType;

                                            tempValue = temp[i];
                                            temp[i] = temp[j];
                                            temp[j] = tempValue;
                                        }
                                    }
                                }

                                modAcc = temp[temp.length - 1];
                                modFlex = modFlex / 4;
                                modRE = modRE/4;

                                Log.d("helloMod", modAcc + " " + modFlex);


                                int statAcc = 0, statFlex = 0, statRE = 0;

                                double accFinalDiff = 0.0;

                                if (accType[accType.length - 1] == 1) {
                                    accFinalDiff = accXDiff;
                                    if (modAcc >= accXDiff) {
                                        statAcc = 3;
                                    } else if (modAcc >= (accXDiff / 2)) {
                                        statAcc = 2;
                                    } else {
                                        statAcc = 1;
                                    }
                                } else if (accType[accType.length - 1] == 2) {
                                    accFinalDiff = accYDiff;
                                    if (modAcc >= accYDiff) {
                                        statAcc = 3;
                                    } else if (modAcc >= (accYDiff / 2)) {
                                        statAcc = 2;
                                    } else {
                                        statAcc = 1;
                                    }
                                } else {
                                    accFinalDiff = accZDiff;
                                    if (modAcc >= accZDiff) {
                                        statAcc = 3;
                                    } else if (modAcc >= (accZDiff / 2)) {
                                        statAcc = 2;
                                    } else {
                                        statAcc = 1;
                                    }
                                }

                                if (modFlex >= flexDiff) {
                                    statFlex = 3;
                                } else if (modFlex >= (flexDiff / 2)) {
                                    statFlex = 2;
                                } else {
                                    statFlex = 1;
                                }

                                if (modRE >= reDiff) {
                                    statRE = 3;
                                } else if (modRE >= (reDiff / 2)) {
                                    statRE = 2;
                                } else {
                                    statRE = 1;
                                }

                                Log.d("helloStat", statAcc + " " + statFlex +" "+ statRE);


                                if (statAcc >= statFlex && statAcc >= statRE) {
                                    pushDataToFirebase(dataList, statFlex);
                                    ExcelData excelData = new ExcelData("NA", "NA", "NA","NA","NA","NA",String.valueOf(accFinalDiff), String.valueOf(flexDiff),String.valueOf(reDiff), String.valueOf(modFlex), String.valueOf(modAcc),String.valueOf(modRE), String.valueOf(statAcc));
                                    excelDataList.add(excelData);
                                } else if (statFlex >= statAcc && statFlex >= statRE) {
                                    pushDataToFirebase(dataList, statFlex);
                                    ExcelData excelData = new ExcelData("NA", "NA", "NA","NA","NA","NA",String.valueOf(accFinalDiff), String.valueOf(flexDiff),String.valueOf(reDiff), String.valueOf(modFlex), String.valueOf(modAcc),String.valueOf(modRE), String.valueOf(statFlex));
                                    excelDataList.add(excelData);
                                } else {
                                    pushDataToFirebase(dataList, statAcc);
                                    ExcelData excelData = new ExcelData("NA", "NA", "NA","NA","NA","NA",String.valueOf(accFinalDiff), String.valueOf(flexDiff),String.valueOf(reDiff), String.valueOf(modFlex), String.valueOf(modAcc),String.valueOf(modRE), String.valueOf(statRE));
                                    excelDataList.add(excelData);
                                }

                                if (statFlex == 3 || statAcc == 3) {
                                    sendNotification();
                                    Log.d("helloActivity", "High Activity Detected");
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.d("hello", "I was here error : " + error.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void pushDataToFirebase(ArrayList<DeviceData> dataList, long status) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("Website");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long ctr = (long) snapshot.child("ctr").getValue();
                double avgFlex = 0.0;
                double avgAcc = 0.0;
                double avgRE = 0.0;
                for (int i = 0; i < dataList.size(); i++) {
                    avgFlex = avgFlex + dataList.get(i).getFlex();
                    avgAcc = avgAcc + dataList.get(i).getAcc().getX()+dataList.get(i).getAcc().getY()+dataList.get(i).getAcc().getZ();
                    avgRE = avgRE + dataList.get(i).getRE();
                }

                avgAcc = avgAcc / (dataList.size()*3);
                avgFlex = avgFlex / dataList.size();
                avgRE = avgRE / dataList.size();

                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");

                String timeStamp = simpleDateFormat.format(calendar.getTime());

                DatabaseReference databaseReference1 = firebaseDatabase.getReference("Website/DeviceId1");

                WebsiteData websiteData = new WebsiteData(avgAcc, avgFlex, status, avgRE, timeStamp);
                databaseReference1.child(String.valueOf(ctr + 1)).setValue(websiteData);
                databaseReference.child("ctr").setValue(ctr + 1);
                resetData();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void resetData() {
        dataList = new ArrayList<>();
        isProcessing = false;
    }
}