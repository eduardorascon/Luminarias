package com.eduardorascon.luminarias;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.eduardorascon.luminarias.sqlite.DatabaseHandler;
import com.eduardorascon.luminarias.sqlite.Luminaria;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CloudSavingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_saving);
    }

    public void getImages(View view) {
        view.setEnabled(false);

        DatabaseHandler db = DatabaseHandler.getInstance(this);
        List<Luminaria> luminariaList = db.getAllLuminarias();

        for (Luminaria luminaria : luminariaList) {
            makeHTTPCall(luminaria);
        }
    }

    private void makeHTTPCall(final Luminaria luminaria) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                String responseFile = "";
                if (luminaria.getRespaldoImagen() == 0)
                    responseFile = sendFileToServer(luminaria.getImagen(), "http://luminarias.todoslosbits.com.mx/upload_image.php");

                if (luminaria.getRespaldoImagen() > 0 || responseFile == "200") {
                    DatabaseHandler db = DatabaseHandler.getInstance(getApplicationContext());

                    if (luminaria.getRespaldoImagen() == 0)
                        db.updateLuminariaRespladoImagen(luminaria);

                    //Falta revisar como se va a mandar la info al servidor... que parametros tiene que tener...
                    String responseData = sendDataToServer();
                    if (responseData == "200")
                        db.updateLuminariaRespaldoDatos(luminaria);
                }

                return "";
            }
        }.execute(null, null, null);
    }

    private String sendDataToServer() {
        return "";
    }

    private String sendFileToServer(String filename, String targetUrl) {
        String response = "error";
        Log.e("Image filename", filename);
        Log.e("url", targetUrl);
        HttpURLConnection connection = null;
        DataOutputStream outputStream = null;

        String pathToOurFile = filename;
        String urlServer = targetUrl;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        DateFormat df = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss");

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024;
        try {
            FileInputStream fileInputStream = new FileInputStream(new File(pathToOurFile));

            URL url = new URL(urlServer);
            connection = (HttpURLConnection) url.openConnection();

            // Allow Inputs & Outputs
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setChunkedStreamingMode(1024);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("ENCTYPE", "multipart/form-data");
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            outputStream = new DataOutputStream(connection.getOutputStream());
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);

            String extension_withname = pathToOurFile.substring(pathToOurFile.lastIndexOf("/") + 1);
            String connstr = "Content-Disposition: form-data; name=\"image\";filename=\"" + extension_withname + "\"" + lineEnd;
            Log.i("Connstr", connstr);

            outputStream.writeBytes(connstr);
            outputStream.writeBytes(lineEnd);

            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            // Read file
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            Log.e("Image length", bytesAvailable + "");
            try {
                while (bytesRead > 0) {
                    try {
                        outputStream.write(buffer, 0, bufferSize);
                    } catch (OutOfMemoryError e) {
                        e.printStackTrace();
                        response = "outofmemoryerror";
                        return response;
                    }
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }
            } catch (Exception e) {
                e.printStackTrace();
                response = "error";
                return response;
            }
            outputStream.writeBytes(lineEnd);
            outputStream.writeBytes(twoHyphens + boundary + twoHyphens
                    + lineEnd);

            // Responses from the server (code and message)
            int serverResponseCode = connection.getResponseCode();
            String serverResponseMessage = connection.getResponseMessage();
            Log.i("Server Response Code ", "" + serverResponseCode);
            Log.i("Server Response Message", serverResponseMessage);

            if (serverResponseCode == 200) {
                response = "true";
            }

            String CDate = null;
            Date serverTime = new Date(connection.getDate());
            try {
                CDate = df.format(serverTime);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("Date Exception", e.getMessage() + " Parse Exception");
            }
            Log.i("Server Response Time", CDate + "");

            filename = CDate + filename.substring(filename.lastIndexOf("."), filename.length());
            Log.i("File Name in Server : ", filename);

            fileInputStream.close();
            outputStream.flush();
            outputStream.close();
            outputStream = null;
        } catch (Exception ex) {
            // Exception handling
            response = "error";
            Log.e("Send file Exception", ex.getMessage() + "");
            ex.printStackTrace();
        }
        return response;
    }
}