package com.deepak.filescompare;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.deepak.filescompare.databinding.ActivityMainBinding;
import com.deepak.filescompare.utils.Constants;
import com.deepak.filescompare.utils.Util;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int WRITE_EXTERNAL_STORAGE = 0;
    private static final int PATH_1_CODE = 1;
    private static final int PATH_2_CODE = 2;
    private boolean isFileSaved;
    private ActivityMainBinding activityMainBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = DataBindingUtil.setContentView(MainActivity.this,
                R.layout.activity_main);

        activityMainBinding.btnPickFile1.setOnClickListener(this);
        activityMainBinding.btnPickFile2.setOnClickListener(this);
        activityMainBinding.btnCompareFiles.setOnClickListener(this);
        activityMainBinding.btnSendEmail.setOnClickListener(this);
    }

    /**
     * compare two .txt files
     */
    private void compareTwoTextFiles() {
        BufferedReader bufferedReader1 = null;
        BufferedReader bufferedReader2 = null;
        String sCurrentLine;
        List<String> listFile1 = new ArrayList<>();
        List<String> listFile2 = new ArrayList<>();
        try {
            bufferedReader1 = new BufferedReader(new FileReader(activityMainBinding.tvFilePath1.getText().toString()));
            bufferedReader2 = new BufferedReader(new FileReader(activityMainBinding.tvFilePath2.getText().toString()));
        } catch (FileNotFoundException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
        }
        try {
            if(bufferedReader1 != null) {
                while ((sCurrentLine = bufferedReader1.readLine()) != null) {
                    listFile1.add(sCurrentLine);
                }
            }
            if(bufferedReader2 != null) {
                while ((sCurrentLine = bufferedReader2.readLine()) != null) {
                    listFile2.add(sCurrentLine);
                }
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        List<String> tmpList = new ArrayList<>(listFile1);
        tmpList.removeAll(listFile2);

        tmpList = listFile2;
        tmpList.removeAll(listFile1);
        String extraText = "";
        for (int i = 0; i < tmpList.size(); i++) {
            extraText = extraText.concat(tmpList.get(i)); //content from test2.txt which is not there in test.txt
        }

        generateNewTextFileOnSD(MainActivity.this, Constants.FILE_NAME, extraText);
    }

    /**
     * genrate new file after compared
     *
     * @param context   context reference
     * @param sFileName new genrated file name
     * @param sBody     data stored in new file
     */
    public void generateNewTextFileOnSD(Context context, String sFileName, String sBody) {
        try {
            File root = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, sFileName);
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(sBody);
            writer.flush();
            writer.close();
            isFileSaved = true;
            Toast.makeText(context, getString(R.string.file_saved), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * show text file
     *
     * @param requestCode request code
     */
    private void showFileChooser(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("text/plain");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select txt file"),
                    requestCode);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            ex.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == PATH_1_CODE && resultCode == RESULT_OK) {
            Uri selectedFileUri = intent.getData();
            if (selectedFileUri != null) {
                File file1 = new File(selectedFileUri.getPath());
                activityMainBinding.tvFilePath1.setText(file1.getPath());
            }
//            File myFile_test2 = new File(selectedFileUri.getEncodedPath());
        } else if (requestCode == PATH_2_CODE && resultCode == RESULT_OK) {
            Uri selectedFileUri = intent.getData();
            if (selectedFileUri != null) {
                File file2 = new File(selectedFileUri.getPath());
                activityMainBinding.tvFilePath2.setText(file2.getPath());
            }
        }
    }

    /**
     * Send Email
     */
    public void sendEmail() {
        try {
            final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
            emailIntent.setType("plain/text");
            emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{""});
            emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Genrated File");

            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());

            String fileName = Constants.FILE_NAME;
            File fileLocation = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), fileName);
            Uri path = Uri.fromFile(fileLocation);

            if (path != null) {
                emailIntent.putExtra(Intent.EXTRA_STREAM, path);
            }
            emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, getString(R.string.mail_body));
            startActivity(Intent.createChooser(emailIntent, getString(R.string.sending_email)));
        } catch (Throwable t) {
            Toast.makeText(this, "Request failed try again: " + t.toString(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_pick_file1:
                showFileChooser(PATH_1_CODE);
                break;
            case R.id.btn_pick_file2:
                showFileChooser(PATH_2_CODE);
                break;
            case R.id.btn_compare_files:
                if (!activityMainBinding.tvFilePath1.getText().toString().equals(Constants.BLANK)
                        && !activityMainBinding.tvFilePath2.getText().toString().equals(Constants.BLANK)) {
                    if (!Util.hasPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        Util.accessPermission(MainActivity.this, new String[]{
                                Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE);
                    } else {
                        compareTwoTextFiles();
                    }
                } else {
                    Toast.makeText(MainActivity.this, R.string.select_files_to_compare, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_send_email:
                if(isFileSaved) {
                    sendEmail();
                }else {
                    Toast.makeText(MainActivity.this, getString(R.string.compare_files_to_send), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


    /**
     * Callback to take permission for usage of application
     *
     * @param requestCode  request code for the result
     * @param permissions  permission to be accessed
     * @param grantResults result of permission accessed
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // If user gives permission for storage
        if (requestCode == WRITE_EXTERNAL_STORAGE // request code for the Camera Permission
                && grantResults.length > 0  // If user gives permission for camera
                && grantResults[WRITE_EXTERNAL_STORAGE] == PackageManager.PERMISSION_GRANTED) {
            if (Util.hasPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                compareTwoTextFiles();
            }
        }
    }


//    /*
//     * This is for sending email without using launcher.
//     */
//    private void sendEmail() {
//        new SendMail().execute();
////        new Thread(new Runnable() {
////            public void run() {
////                try {
////                    GMailSender sender = new GMailSender(
////                            "",
////                            "");
////                    sender.addAttachment(Environment.getExternalStorageDirectory().getPath()+"/FileCompare/new_file.txt");
////                    sender.sendMail("Test mail", "This mail has been sent from android app along with attachment",
////                            "",
////                            "");
////                } catch (Exception e) {
////                    Toast.makeText(getApplicationContext(),"Error",Toast.LENGTH_LONG).show();
////                }
////            }
////        }).start();
//    }

//    @SuppressLint("StaticFieldLeak")
   /* private class SendMail extends AsyncTask<String, Integer, Void> {
        //private ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            progressDialog = UIUtil.showProgressDialog(activity, "Sending mail");
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
//            progressDialog.dismiss();
        }

        protected Void doInBackground(String... params) {

            Mail mail = new Mail("", "");
            String[] toArr = {""};
            mail.setTo(toArr);
            mail.setFrom("");
            mail.setSubject("test");
            mail.setBody("body");

            try {
                if (mail.send()) {
                      Toast.makeText(MainActivity.this, "Email was sent successfully.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "Email was not sent.", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Log.e("MailApp", "Could not send email", e);
            }
            return null;
        }
    }*/

}
