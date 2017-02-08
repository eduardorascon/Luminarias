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

import static android.R.attr.data;

public class CloudSavingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_saving);
    }

    RequestParams params = new RequestParam();
    private void getImages() {

        DatabaseHandler db = DatabaseHandler.getInstance(this);
        List<Luminaria> luminariaList = db.getAllLuminarias();

        for (Luminaria l : luminariaList) {

        }

        Uri selectedImage = data.getData();
        String[] filePathColumn = {MediaStore.Images.Media.DATA};

        // Get the cursor
        Cursor cursor = getContentResolver().query(selectedImage,
                filePathColumn, null, null, null);
        // Move to first row
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        imgPath = cursor.getString(columnIndex);
        cursor.close();
        ImageView imgView = (ImageView) findViewById(R.id.imgView);
        // Set the Image in ImageView
        imgView.setImageBitmap(BitmapFactory
                .decodeFile(imgPath));
        // Get the Image's file name
        String fileNameSegments[] = imgPath.split("/");
        fileName = fileNameSegments[fileNameSegments.length - 1];
        // Put file name in Async Http Post Param which will used in Php web app
        params.put("filename", fileName);

    }
}
