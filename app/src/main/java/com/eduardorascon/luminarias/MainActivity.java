package com.eduardorascon.luminarias;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.eduardorascon.luminarias.sqlite.DatabaseHandler;
import com.eduardorascon.luminarias.sqlite.Luminaria;

public class MainActivity extends AppCompatActivity {
    String currentPhotoPath;
    LocationManager locationManager;
    ImageView imageView;
    TextView textViewTipoLampara;
    Uri photoUri;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("PHOTO_URI_BUNDLE", photoUri);
        outState.putString("CURRENT_PHOTO_PATH_BUNDLE", currentPhotoPath);
    }

    @Override
    protected void onResume() {
        super.onResume();
        askForLocationPermission();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            photoUri = savedInstanceState.getParcelable("PHOTO_URI_BUNDLE");
            currentPhotoPath = savedInstanceState.getString("CURRENT_PHOTO_PATH_BUNDLE");
        }

        setContentView(R.layout.activity_main);

        DatabaseHandler db = DatabaseHandler.getInstance(this);
        db.getWritableDatabase();

        Button button = (AppCompatButton) findViewById(R.id.buttonSaveInfo);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                guardarLuminaria(view);
            }
        });
        imageView = (ImageView) findViewById(R.id.imageView);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        tipoPosteSpinner = (Spinner) findViewById(R.id.spinnerTipoPoste);
        tipoLamparaSpinner = (Spinner) findViewById(R.id.spinnerTipoLampara);
        textViewTipoLampara = (TextView) findViewById(R.id.textViewTipoLampara);

        loadTipoLamparaSpinner();
        loadTipoPosteSpinner();
    }

    public void guardarLuminaria(View view) {

        if (validateInput() == false)
            return;

        try{
            Luminaria luminaria = new Luminaria();
            luminaria.setLat(String.valueOf(latitudeGPS));
            luminaria.setLon(String.valueOf(longitudeGPS));
            luminaria.setTipoPoste(tipoPosteSpinner.getSelectedItem().toString());
            luminaria.setTipoLampara(tipoLamparaSpinner.getSelectedItem().toString());
            luminaria.setImagen(currentPhotoPath);

            DatabaseHandler db = DatabaseHandler.getInstance(view.getContext());
            long registro = db.insertLuminaria(luminaria);

            if(registro > 0) {
                Imagen imagen = new Imagen();
                imagen.setLuminaria(registro);
                imagen.setNombre("L_" + System.currentTimeMillis() + ".jpg");
                imagen.setBlob(getImageBlob());

                long imagen = db.insertImagen(imagen);
            }
        }
        catch(Excepciont e){
            e.printStackTrace();
            return;
        }

        resetInput();
        Toast.makeText(this, "Luminaria (" + registro + ") guardada con éxito", Toast.LENGTH_LONG).show();
    }

    private void resetInput() {
        tipoPosteSpinner.setSelection(0);
        tipoLamparaSpinner.setSelection(0);
        latitudeGPS = 0.0d;
        longitudeGPS = 0.0d;
        textViewTipoLampara.setText("LAMPARA:");
        imageView.setImageDrawable(null);
    }

    private boolean validateInput() {
        if (tipoPosteSpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Tipo de poste no seleccionado", Toast.LENGTH_LONG).show();
            return false;
        }

        if (tipoLamparaSpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Tipo de lampara no seleccionado", Toast.LENGTH_LONG).show();
            return false;
        }

        if (latitudeGPS == 0.0d || longitudeGPS == 0.0d) {
            toggleGPSUpdates();
            Toast.makeText(this, "La ubicación aun no esta calculada", Toast.LENGTH_LONG).show();
            return false;
        }

        if (imageView.getDrawable() == null) {
            Toast.makeText(this, "La fotografia no ha sido tomada", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    public void launchCamera(View view) {
        if (askForCameraPermission() == false || askForStoragePermission() == false)
            return;

        Intent chooseImageIntent = ImagePicker.getPickImageIntent(this);
        startActivityForResult(chooseImageIntent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode != RESULT_OK)
            return;

        if (requestCode != 1)
            return;

        Bitmap bitmap = ImagePicker.getImageFromResult(this, resultCode, intent);
        imageView.setImageBitmap(bitmap);
        return;
    }

    private boolean isLocationEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private boolean checkLocation() {
        if (isLocationEnabled() == false)
            showAlert();

        return isLocationEnabled();
    }

    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Enable location")
                .setMessage("Your location settings is set to 'Off'")
                .setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent locationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(locationIntent);
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
        dialog.show();
    }

    public void toggleGPSUpdates() {
        if (checkLocation() == false)
            return;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10 * 1000, 5, locationListenerGPS);
        }
    }

    private double longitudeGPS = 0.0d, latitudeGPS = 0.0d;
    private Spinner tipoLamparaSpinner, tipoPosteSpinner;
    private final LocationListener locationListenerGPS = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            longitudeGPS = location.getLongitude();
            latitudeGPS = location.getLatitude();
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    public void loadTipoLamparaSpinner() {
        tipoLamparaSpinner.setAdapter(new WrapAdapter(this, getResources().getStringArray(R.array.tipo_lampara_array)));
        tipoLamparaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                calculateWatt();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public void loadTipoPosteSpinner() {
        tipoPosteSpinner.setAdapter(new WrapAdapter(this, getResources().getStringArray(R.array.tipo_poste_array)));
        tipoPosteSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                calculateWatt();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private boolean askForCameraPermission() {
        boolean isCameraPermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        if (isCameraPermissionGranted == false) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
            }
        }
        return isCameraPermissionGranted;
    }

    private boolean askForStoragePermission() {
        boolean isStoragePermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        if (isStoragePermissionGranted == false) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
        return isStoragePermissionGranted;
    }

    private boolean askForLocationPermission() {
        boolean isLocationPermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (isLocationPermissionGranted) {
            toggleGPSUpdates();
            return true;
        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
        }

        return isLocationPermissionGranted;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1://CAMERA
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(MainActivity.this, "Es necesario contar con el permiso solicitado", Toast.LENGTH_LONG).show();
                }
                break;
            case 2://ACCESS_FINE_LOCATION
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(MainActivity.this, "Es necesario contar con el permiso solicitado", Toast.LENGTH_LONG).show();
                }
                toggleGPSUpdates();
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.app_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_cloud_save:
                Intent intent = new Intent(this, CloudSavingActivity.class);
                this.startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void calculateWatt() {
        int selectedLampara = tipoLamparaSpinner.getSelectedItemPosition();
        int selectedPost = tipoPosteSpinner.getSelectedItemPosition();

        if (selectedLampara == 0 || selectedPost == 0) {
            textViewTipoLampara.setText("LAMPARA:");
            return;
        }

        String watts = "";
        switch (selectedPost) {
            case 1:
                switch (selectedLampara) {
                    case 1:
                        watts = "400W";
                        break;
                    case 2:
                        watts = "275W";
                        break;
                    case 3:
                        watts = "150W";
                        break;
                    case 4:
                        watts = "175W";
                        break;
                }
                break;
            case 2:
                switch (selectedLampara) {
                    case 1:
                        watts = "400W";
                        break;
                    case 2:
                        watts = "275W";
                        break;
                    case 3:
                        watts = "150W";
                        break;
                    case 4:
                        watts = "175W";
                        break;
                }
                break;
            case 3:
                switch (selectedLampara) {
                    case 1:
                        watts = "250W";
                        break;
                    case 2:
                        watts = "150W";
                        break;
                    case 3:
                        watts = "100W";
                        break;
                    case 4:
                        watts = "125W";
                        break;
                }
                break;
            case 4:
                switch (selectedLampara) {
                    case 1:
                        watts = "250W";
                        break;
                    case 2:
                        watts = "150W";
                        break;
                    case 3:
                        watts = "100W";
                        break;
                    case 4:
                        watts = "125W";
                        break;
                }
                break;
            case 5:
                switch (selectedLampara) {
                    case 1:
                        watts = "100W";
                        break;
                    case 2:
                        watts = "75W";
                        break;
                    case 3:
                        watts = "45W";
                        break;
                    case 4:
                        watts = "50W";
                        break;
                }
                break;
            case 6:
                switch (selectedLampara) {
                    case 1:
                        watts = "100W";
                        break;
                    case 2:
                        watts = "75W";
                        break;
                    case 3:
                        watts = "45W";
                        break;
                    case 4:
                        watts = "50W";
                        break;
                }
                break;
            case 7:
                switch (selectedLampara) {
                    case 1:
                        watts = "400W";
                        break;
                    case 2:
                        watts = "275W";
                        break;
                    case 3:
                        watts = "150W";
                        break;
                    case 4:
                        watts = "175W";
                        break;
                }
                break;
            case 8:
                switch (selectedLampara) {
                    case 1:
                        watts = "400W";
                        break;
                    case 2:
                        watts = "275W";
                        break;
                    case 3:
                        watts = "150W";
                        break;
                    case 4:
                        watts = "175W";
                        break;
                }
                break;
            case 9:
                switch (selectedLampara) {
                    case 1:
                        watts = "250W";
                        break;
                    case 2:
                        watts = "150W";
                        break;
                    case 3:
                        watts = "100W";
                        break;
                    case 4:
                        watts = "125W";
                        break;
                }
                break;
            case 10:
                switch (selectedLampara) {
                    case 1:
                        watts = "250W";
                        break;
                    case 2:
                        watts = "150W";
                        break;
                    case 3:
                        watts = "100W";
                        break;
                    case 4:
                        watts = "125W";
                        break;
                }
                break;
            case 11:
                switch (selectedLampara) {
                    case 1:
                        watts = "100W";
                        break;
                    case 2:
                        watts = "75W";
                        break;
                    case 3:
                        watts = "45W";
                        break;
                    case 4:
                        watts = "50W";
                        break;
                }
                break;
            case 12:
                switch (selectedLampara) {
                    case 1:
                        watts = "100W";
                        break;
                    case 2:
                        watts = "75W";
                        break;
                    case 3:
                        watts = "45W";
                        break;
                    case 4:
                        watts = "50W";
                        break;
                }
                break;
            case 13:
                switch (selectedLampara) {
                    case 1:
                        watts = "100W";
                        break;
                    case 2:
                        watts = "75W";
                        break;
                    case 3:
                        watts = "45W";
                        break;
                    case 4:
                        watts = "50W";
                        break;
                }
                break;
            case 14:
                switch (selectedLampara) {
                    case 1:
                        watts = "100W";
                        break;
                    case 2:
                        watts = "75W";
                        break;
                    case 3:
                        watts = "45W";
                        break;
                    case 4:
                        watts = "50W";
                        break;
                }
                break;
            case 15:
                switch (selectedLampara) {
                    case 1:
                        watts = "250W";
                        break;
                    case 2:
                        watts = "150W";
                        break;
                    case 3:
                        watts = "100W";
                        break;
                    case 4:
                        watts = "125W";
                        break;
                }
                break;
        }

        textViewTipoLampara.setText("LAMPARA (" + watts + ")");
    }

    class WrapAdapter extends ArrayAdapter<String> {
        private Context mContext;
        private String[] mItems;

        public WrapAdapter(Context context, String[] items) {
            super(context, 0, items);
            mContext = context;
            mItems = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView = (TextView) LayoutInflater.from(mContext)
                    .inflate(R.layout.spinner_item, parent, false);
            textView.setText(mItems[position]);
            return textView;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            final TextView textView = (TextView) LayoutInflater.from(mContext)
                    .inflate(R.layout.spinner_item, parent, false);
            textView.setText(mItems[position]);
            textView.post(new Runnable() {
                @Override
                public void run() {
                    textView.setSingleLine(false);
                }
            });
            return textView;
        }
    }
}
