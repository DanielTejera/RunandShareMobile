package com.example.runandshare;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

public class NavigationActivity extends Activity implements OnClickListener {

	private ImageButton newTraining;
	private ImageButton searchTraining;
	private ImageButton profile;
	private ImageButton userTrainings;

	private Object lock;
	public static User user;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_navigation);

		newTraining = (ImageButton) findViewById(R.id.newTrainingButton);
		searchTraining = (ImageButton) findViewById(R.id.searchTrainingButton);
		profile = (ImageButton) findViewById(R.id.profileButton);
		userTrainings = (ImageButton) findViewById(R.id.userTrainingsButton);

		newTraining.setOnClickListener(this);
		searchTraining.setOnClickListener(this);
		profile.setOnClickListener(this);
		userTrainings.setOnClickListener(this);

		user = new User();
		lock = new Object();

		new LoadUser().execute();
		synchronized (lock) {
			try {
				lock.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.newTrainingButton:
			Intent intentTraining = new Intent(NavigationActivity.this, TrainingActivity.class);
			startActivity(intentTraining);
			break;

		case R.id.searchTrainingButton:
			Intent intentSearch = new Intent(NavigationActivity.this, SearchTrainingActivity.class);
			startActivity(intentSearch);
			break;

		case R.id.profileButton:
			Intent intentProfile = new Intent(NavigationActivity.this, ProfileActivity.class);
			startActivity(intentProfile);
			break;

		case R.id.userTrainingsButton:
			Intent intentLoginRegisterMenu = new Intent(NavigationActivity.this, UserTrainingsActivity.class);
			startActivity(intentLoginRegisterMenu);
			break;
		default:
			break;
		}

	}

	// Clase asíncrona utilizada para llamar al servicio web loadUser
	class LoadUser extends AsyncTask<String, String, String> {
		@Override
		protected String doInBackground(String... params) {
			loadUser();
			synchronized (lock) {

				lock.notify();
			}
			return null;
		}

	}

	// Método donde se ejecuta el servicio web loadUser, donde cargamos los
	// datos del usuario
	private void loadUser() {
		String request = "";
		try {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://IP_SERVIDOR/webservices/loadUser.php");
			List<NameValuePair> pairs = new ArrayList<NameValuePair>(1);
			pairs.add(new BasicNameValuePair("idandroid",
					Secure.getString(getBaseContext().getContentResolver(), Secure.ANDROID_ID)));
			post.setEntity(new UrlEncodedFormEntity(pairs));
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			request = client.execute(post, responseHandler);
			String[] datas = request.split("/");
			if (!(datas.length == 0)) {
				for (int i = datas.length - 1; i >= datas.length - 1; i--) {
					user.setUserName(datas[datas.length - 1].trim());
				}
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onBackPressed() {
	}
}
