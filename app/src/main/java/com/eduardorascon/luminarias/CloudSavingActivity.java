package com.eduardorascon.luminarias;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.eduardorascon.luminarias.sqlite.DatabaseHandler;
import com.eduardorascon.luminarias.sqlite.Imagen;
import com.eduardorascon.luminarias.sqlite.Luminaria;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
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
    LinearLayout llLogin;
    ProgressBar progressBar;

    @Override
    protected void onResume() {
        super.onResume();
        checkMobileInternetConn();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_saving);

        editTextUser = (EditText) findViewById(R.id.editTextUser);
        editTextPass = (EditText) findViewById(R.id.editTextPass);

        llLogin = (LinearLayout) findViewById(R.id.linearLayoutLogin);

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

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
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
            buttonSave.setVisibility(View.VISIBLE);
            buttonSave.setEnabled(true);
            buttonLogin.setEnabled(false);
            llLogin.setVisibility(View.GONE);
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
        makeHTTPCall();

        //Close activity when finished.
        finish();
    }

    private void makeHTTPCall() {
        new AsyncTask<Void, Void, String>() {
            @OnPreExecute(){
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            protected String doInBackground(Void... voids) {

                DatabaseHandler db = DatabaseHandler.getInstance(getApplicationContext());
                List<Luminaria> luminariaList = db.getAllLuminarias();

                for (Luminaria luminaria : luminariaList){

                    List<Imagen> imagenesList = null;
                    if (luminaria.getRespaldoImagen() == 0) {
                        imagenesList = db.getAllImagenesFromLuminaria(luminaria);
                        for (Imagen imagen : imagenesList) {
                            String responseFile = sendFileToServer(imagen);
                            if (responseFile.equals("200"))
                                db.updateLuminariaRespladoImagen(luminaria);
                        }
                    }

                    String imagenes = "";
                    for (Imagen imagen : imagenesList) {
                        if (imagenes.equals(""))
                            imagenes = imagen.getNombreImagen();
                        else
                            imagenes += "|" + imagen.getNombreImagen();
                    }

                    if (luminaria.getRespladoDatos() == 0) {
                        String responseData = sendDataToServer(luminaria, imagenes);
                        if (responseData.equals("200"))
                            db.updateLuminariaRespaldoDatos(luminaria);
                    }
                }
                return "";
            }

            @Override
            protected void onPostExecute(String s) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), "INFORMACION ENVIADA AL SERVIDOR", Toast.LENGTH_LONG).show();
            }
        }.execute(null, null, null);
    }

    private String sendDataToServer(Luminaria luminaria, String imagenes) {
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

            writer.write(getPostDataString(luminaria, imagenes));

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

    private String getPostDataString(Luminaria luminaria, String nombre_imagen) throws UnsupportedEncodingException {
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
        result.append(URLEncoder.encode("numero_lamparas", "UTF-8")).append("=");
        result.append(URLEncoder.encode(luminaria.getNumeroLamparas(), "UTF-8")).append("&");
        result.append(URLEncoder.encode("imagen", "UTF-8")).append("=");
        result.append(URLEncoder.encode(nombre_imagen, "UTF-8")).append("&");
        result.append(URLEncoder.encode("user", "UTF-8")).append("=");
        result.append(URLEncoder.encode(user, "UTF-8"));
        return result.toString();
    }

    private String sendFileToServer(Imagen imagen) {
        String response = "error";
        HttpURLConnection connection = null;
        DataOutputStream outputStream = null;

        String pathToOurFile = imagen.getNombreImagen();
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024;
        try {
            InputStream inputStream = new ByteArrayInputStream(imagen.getImagen());
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

            bytesAvailable = inputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];
            // Read file
            bytesRead = inputStream.read(buffer, 0, bufferSize);

            try {
                while (bytesRead > 0) {
                    try {
                        outputStream.write(buffer, 0, bufferSize);
                    } catch (OutOfMemoryError e) {
                        e.printStackTrace();
                        response = "outofmemoryerror";
                        return response;
                    }
                    bytesAvailable = inputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = inputStream.read(buffer, 0, bufferSize);
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

            inputStream.close();
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

    private boolean isWifiEnabled() {
        //Create object for ConnectivityManager class which returns network related info
        ConnectivityManager connectivity = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            return false;
        }
        //Get network info - WIFI internet access
        NetworkInfo info = connectivity.getActiveNetworkInfo();
        if (info == null) {
            return false;
        }
        //Check if network is WIFI
        if (info.getType() != ConnectivityManager.TYPE_WIFI) {
            return false;
        }
        //Look for whether device is currently connected to WIFI network
        return info.isConnected();
    }

    private void checkMobileInternetConn() {
        if (isWifiEnabled() == false) {
            Toast.makeText(this, "CONEXION WIFI NO DISPONIBLE...", Toast.LENGTH_LONG).show();
            finish();
        }
    }
}