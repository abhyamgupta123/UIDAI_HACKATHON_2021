package com.resident.app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.WriterException;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class QRGneratorActivity extends AppCompatActivity {

    // For Logging Purpose and traceback
    private static final String TAG = MainActivity.class.getName();

    private ImageView qrCodeIV;

    private Bitmap bitmap;
    private QRGEncoder qrgEncoder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrgnerator);

        // initializing all variables.
        qrCodeIV = findViewById(R.id.idIVQrcode);

        Intent intent = getIntent();
        String pass = intent.getStringExtra("pass");
        String link = intent.getStringExtra("url");
        String usernName = intent.getStringExtra("userName");
        String eventId = intent.getStringExtra("eventUID");

        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();

        Point point = new Point();
        display.getSize(point);

        int width = point.x;
        int height = point.y;

        // generating dimension from width and height.
        int dimen = width < height ? width : height;
        dimen = dimen * 3 / 4;

        String qrStringToSend = usernName + "," + eventId + "," + link + "," + pass;
        qrgEncoder = new QRGEncoder(qrStringToSend, null, QRGContents.Type.TEXT, dimen);
        try {
            bitmap = qrgEncoder.encodeAsBitmap();
            qrCodeIV.setImageBitmap(bitmap);
        } catch (WriterException e) {
            Log.e("Tag", e.toString());
        }
    }
}