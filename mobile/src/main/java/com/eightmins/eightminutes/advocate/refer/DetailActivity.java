package com.eightmins.eightminutes.advocate.refer;

import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.eightmins.eightminutes.R;
import com.mikepenz.iconics.context.IconicsContextWrapper;

import butterknife.ButterKnife;
import icepick.Icepick;

public class DetailActivity extends AppCompatActivity {

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    Icepick.saveInstanceState(this, outState);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Icepick.restoreInstanceState(this, savedInstanceState);

    setContentView(R.layout.activity_refer_detail);
    ButterKnife.bind(this);
  }

  @Override
  protected void attachBaseContext(Context newBase) {
    super.attachBaseContext(IconicsContextWrapper.wrap(newBase));
  }
}
