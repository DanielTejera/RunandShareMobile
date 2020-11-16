package com.example.runandshare;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class ChangePasswordActivity extends Activity implements OnClickListener {

	private Button continueChangeButton;
	private Button cancelChangeButton;
	private ImageButton menuButton;

	private EditText changePasswordEditText;
	private EditText repeatChangePasswordEditText;

	private TextView changePasswordTextView;
	private TextView repeatChangePasswordTextView;

	private String changePassword;
	private String repeatChangePassword;

	public Object lock;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_change_password);
		lock = new Object();

		continueChangeButton = (Button) findViewById(R.id.continueChangeButton);
		cancelChangeButton = (Button) findViewById(R.id.cancelChangeButton);
		menuButton = (ImageButton) findViewById(R.id.menuButton);

		continueChangeButton.setOnClickListener(this);
		cancelChangeButton.setOnClickListener(this);
		menuButton.setOnClickListener(this);

		changePasswordEditText = (EditText) findViewById(R.id.changePasswordEditText);
		repeatChangePasswordEditText = (EditText) findViewById(R.id.repeatChangePasswordEditText);

		changePasswordTextView = (TextView) findViewById(R.id.changePasswordTextView);
		repeatChangePasswordTextView = (TextView) findViewById(R.id.repeatChangePasswordTextView);

	}

	public void onClick(View v) {

		changePassword = changePasswordEditText.getText().toString();
		repeatChangePassword = repeatChangePasswordEditText.getText().toString();

		switch (v.getId()) {
		case R.id.menuButton:
			Intent menuIntent = new Intent(ChangePasswordActivity.this, NavigationActivity.class);
			startActivity(menuIntent);
			break;
		case R.id.continueChangeButton:

			if (changePassword.equals("")) {
				changePasswordTextView.setTypeface(null, Typeface.BOLD_ITALIC);
				break;
			}

			if (repeatChangePassword.equals("")) {
				repeatChangePasswordTextView.setTypeface(null, Typeface.BOLD_ITALIC);
				break;
			}

			if (!changePassword.equals(repeatChangePassword)) {

				changePasswordEditText.setText("");
				repeatChangePasswordEditText.setText("");
				changePasswordTextView.setTypeface(null, Typeface.BOLD_ITALIC);
				repeatChangePasswordTextView.setTypeface(null, Typeface.BOLD_ITALIC);
				break;
			}

			changePassword = convertPasswordToMD5Hash(changePassword);
			repeatChangePassword = convertPasswordToMD5Hash(repeatChangePassword);

			new ChangePassword().execute();
			synchronized (lock) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			changePasswordTextView.setTypeface(null, Typeface.NORMAL);
			repeatChangePasswordTextView.setTypeface(null, Typeface.NORMAL);

			Intent intentTraining = new Intent(ChangePasswordActivity.this, ProfileActivity.class);
			startActivity(intentTraining);
			finish();
			break;

		case R.id.cancelRButton:
			Intent intentMenu = new Intent(ChangePasswordActivity.this, ProfileActivity.class);
			startActivity(intentMenu);

			break;

		default:
			break;
		}
	}

	// Clase asíncrona utilizada para llamar al servicio web changePassword
	class ChangePassword extends AsyncTask<String, String, String> {
		@Override
		protected String doInBackground(String... params) {
			changePassword();
			synchronized (lock) {

				lock.notify();
			}
			return null;
		}

	}

	// Método donde se ejecuta el servicio web changePassword
	private void changePassword() {
		try {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://IP_SERVIDOR/webservices/changePassword.php");
			List<NameValuePair> pairs = new ArrayList<NameValuePair>(2);
			pairs.add(new BasicNameValuePair("username", ProfileActivity.user.getUserName()));
			pairs.add(new BasicNameValuePair("password", changePassword));
			post.setEntity(new UrlEncodedFormEntity(pairs));
			client.execute(post);
		} catch (ClientProtocolException e) {
			Log.i("HTTPCLIENT", e.getLocalizedMessage());
		} catch (IOException e) {
			Log.i("HTTPCLIENT", e.getLocalizedMessage());
		}
	}

	public static String convertPasswordToMD5Hash(String string) {
		byte[] hash;

		try {
			hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("MD5 not supported?", e);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("UTF-8 not supported?", e);
		}

		StringBuilder hex = new StringBuilder(hash.length * 2);

		for (byte b : hash) {
			int i = (b & 0xFF);
			if (i < 0x10)
				hex.append('0');
			hex.append(Integer.toHexString(i));
		}

		return hex.toString();
	}
}
