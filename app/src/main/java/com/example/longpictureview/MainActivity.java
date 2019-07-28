package com.example.longpictureview;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_show).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                        "://" + getResources().getResourcePackageName(R.drawable.img_long) +
                        "/" + getResources().getResourceTypeName(R.drawable.img_long) +
                        "/" + getResources().getResourceEntryName(R.drawable.img_long));

                LongPictureDialog.newInstance()
                        .setPictureUrl(uri.toString())
                        .show(getSupportFragmentManager());
            }
        });
    }
}
