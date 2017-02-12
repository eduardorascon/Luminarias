package com.eduardorascon.luminarias;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.eduardorascon.luminarias.sqlite.DatabaseHandler;
import com.eduardorascon.luminarias.sqlite.Luminaria;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class CloudSavingActivity extends AppCompatActivity {

    EditText editTextUser, editTextPass;
    Button buttonLogin, buttonSave;
    String user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_saving);

        editTextUser = (EditText) findViewById(R.id.editTextUser);
        editTextPass = (EditText) findViewById(R.id.editTextPass);

        buttonSave = (AppCompatButton) findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getImages(view);
            }
        });

        buttonLogin = (AppCompatButton) findViewById(R.id.buttonLogin);
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkUser(view);
            }
        });
    }

    private void checkUser(View view) {
        validateUser(editTextUser.getText().toString(), editTextPass.getText().toString());
    }

    private void validateResult(String result) {
        if (result.equals("s")) {
            editTextUser.setEnabled(false);
            editTextPass.setEnabled(false);
            buttonLogin.setEnabled(false);
            user = editTextUser.getText().toString();
            buttonSave.setEnabled(true);
        } else {
            editTextUser.setText("");
            editTextPass.setText("");
        }
    }

    private void validateUser(final String user, final String pass) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                String response = "";
                try {
                    URL url = new URL("http://luminarias.todoslosbits.com.mx/check_user.php");

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(15000);
                    conn.setConnectTimeout(15000);
                    conn.setRequestMethod("POST");
                    conn.setDoInput(true);
                    conn.setDoOutput(true);

                    OutputStream os = conn.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

                    StringBuilder result = new StringBuilder();
                    result.append(URLEncoder.encode("user", "UTF-8")).append("=");
                    result.append(URLEncoder.encode(user, "UTF-8")).append("&");
                    result.append(URLEncoder.encode("pass", "UTF-8")).append("=");
                    result.append(URLEncoder.encode(pass, "UTF-8"));
                    writer.write(result.toString());

                    writer.flush();
                    writer.close();
                    os.close();

                    BufferedReader is = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    return is.readLine();

                    //return conn.getResponseMessage();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return response;
            }

            @Override
            protected void onPostExecute(String s) {
                validateResult(s);
            }
        }.execute(null, null, null);
    }

    public void getImages(View view) {
        view.setEnabled(false);

        DatabaseHandler db = DatabaseHandler.getInstance(this);
        List<Luminaria> luminariaList = db.getAllLuminarias();

        for (Luminaria luminaria : luminariaList)
            makeHTTPCall(luminaria);

        //Close activity when finished.
        finish();
    }

    private void makeHTTPCall(final Luminaria luminaria) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                String responseFile = sendDataToServer(luminaria);
               /* if (luminaria.getRespaldoImagen() == 0)
                    responseFile = sendFileToServer(luminaria.getImagen());

                if (luminaria.getRespaldoImagen() > 0 || responseFile.equals("200")) {
                    DatabaseHandler db = DatabaseHandler.getInstance(getApplicationContext());

                    if (luminaria.getRespaldoImagen() == 0)
                        db.updateLuminariaRespladoImagen(luminaria);

                    String responseData = sendDataToServer(luminaria);
                    if (responseData.equals("200"))
                        db.updateLuminariaRespaldoDatos(luminaria);
                }*/

                return responseFile;
            }
        }.execute(null, null, null);
    }

    private String sendDataToServer(Luminaria luminaria) {
        URL url;
        String response = "";
        try {
            url = new URL("http://luminarias.todoslosbits.com.mx/upload_data.php");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(getPostDataString(luminaria));

            writer.flush();
            writer.close();
            os.close();
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                response = String.valueOf(responseCode);
            } else {
                response = "error";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }

    private String getPostDataString(Luminaria luminaria) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        result.append(URLEncoder.encode("lat", "UTF-8")).append("=");
        result.append(URLEncoder.encode(luminaria.getLat(), "UTF-8")).append("&");
        result.append(URLEncoder.encode("lon", "UTF-8")).append("=");
        result.append(URLEncoder.encode(luminaria.getLon(), "UTF-8")).append("&");
        result.append(URLEncoder.encode("fecha_hora", "UTF-8")).append("=");
        result.append(URLEncoder.encode(luminaria.getFechaHora(), "UTF-8")).append("&");
        result.append(URLEncoder.encode("tipo_lampara", "UTF-8")).append("=");
        result.append(URLEncoder.encode(luminaria.getTipoLampara(), "UTF-8")).append("&");
        result.append(URLEncoder.encode("tipo_poste", "UTF-8")).append("=");
        result.append(URLEncoder.encode(luminaria.getTipoPoste(), "UTF-8")).append("&");
        result.append(URLEncoder.encode("imagen", "UTF-8")).append("=");
        result.append(URLEncoder.encode(luminaria.getImagen(), "UTF-8")).append("&");
        result.append(URLEncoder.encode("user", "UTF-8")).append("=");
        result.append(URLEncoder.encode(user, "UTF-8"));
        return result.toString();
    }

    private String sendFileToServer(String filename) {
        String response = "error";
        Log.e("Image filename", filename);
        HttpURLConnection connection = null;
        DataOutputStream outputStream = null;

        String pathToOurFile = filename;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024;
        try {
            FileInputStream fileInputStream = new FileInputStream(new File(pathToOurFile));
            URL url = new URL("http://luminarias.todoslosbits.com.mx/upload_image.php");
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
            outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            // Responses from the server (code and message)
            int serverResponseCode = connection.getResponseCode();
            if (serverResponseCode == HttpURLConnection.HTTP_OK)
                response = String.valueOf(serverResponseCode);

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