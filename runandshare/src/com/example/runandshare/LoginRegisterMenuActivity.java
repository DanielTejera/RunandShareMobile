package com.example.runandshare;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

public class LoginRegisterMenuActivity extends Activity {

	private Button loginButton;
	private Button registerButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_login_register_menu);

		loginButton = (Button) findViewById(R.id.loginButton);
		loginButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(LoginRegisterMenuActivity.this, LoginActivity.class);
				startActivity(intent);
			}
		});

		registerButton = (Button) findViewById(R.id.registerButton);
		registerButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Start the LocalizationActivity
				Intent intent = new Intent(LoginRegisterMenuActivity.this, RegisterActivity.class);
				startActivity(intent);
			}
		});
	}

	@Override
	public void onBackPressed() {
	}
}
