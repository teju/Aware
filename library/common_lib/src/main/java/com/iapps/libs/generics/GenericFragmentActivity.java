package com.iapps.libs.generics;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

public class GenericFragmentActivity extends AppCompatActivity {
	protected GenericFragmentActivity getActivityInstance(){
		return this;
	}

	@Override
	protected void onNewIntent(Intent intent) {
		this.setIntent(intent);
		super.onNewIntent(intent);
	}

}
