package com.agroneo.droid.specimens;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.agroneo.droid.R;

import live.page.android.sys.PageActivity;

public class SpecimenView extends PageActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.specimen_view);
    }
}
