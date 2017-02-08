package com.eduardorascon.luminarias;

import android.app.DownloadManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.eduardorascon.luminarias.sqlite.DatabaseHandler;
import com.eduardorascon.luminarias.sqlite.Luminaria;

import java.util.ArrayList;
import java.util.List;

public class CloudSavingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_saving);
    }

    String imgPath;
    private void getImages() {

        DatabaseHandler db = DatabaseHandler.getInstance(this);
        List<Luminaria> luminariaList = db.getAllLuminarias();

        for (Luminaria l : luminariaList) {
        	imgPath = l.getImage();
        	if(imgPath!=null && !imgPath.isEmpty()){
        		//encodeImageToString();
        		makeHTTPCall();
        	}
        }

    }

	String encodedString, fileName;
    public void encodeImagetoString() {
        new AsyncTask<Void, Void, String>() {
 
            protected void onPreExecute() {
            	String fileNameSegments[] = imgPath.split("/");
            	fileName = fileNameSegments[fileNameSegments.length - 1];
            };
 
            @Override
            protected String doInBackground(Void... params) {
                BitmapFactory.Options options = null;
                options = new BitmapFactory.Options();
                options.inSampleSize = 3;
                bitmap = BitmapFactory.decodeFile(imgPath, options);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                // Must compress the Image to reduce image size to make upload easy
                bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream); 
                byte[] byte_arr = stream.toByteArray();
                // Encode Image to String
                encodedString = Base64.encodeToString(byte_arr, 0);
                return "";
            }
 
            @Override
            protected void onPostExecute(String msg) {

            }
        }.execute(null, null, null);
    }

    public void makeHTTPCall() {    
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost("http://luminarias.todoslosbits.com.mx/upload_image.php");

        try{
        	post.setEntity( new FileEntity(new File(imgPath), "application/octet-stream"));
        	HttpResponse response = client.execute(post);
        }
        catch(ClientProtocolException e){

        }
    }
}