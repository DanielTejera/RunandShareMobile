package com.example.runandshare;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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
import android.view.Window;

public class MainActivity extends Activity {

	private static final long SPLASH_DELAY = 3000;
	public Object lock;
	public static User user;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_main);

		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				new CheckID().execute();
				synchronized (lock) {
					try {
						lock.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				if (checkCode()) {
					Intent mainIntent = new Intent().setClass(MainActivity.this, TrainingActivity.class);
					startActivity(mainIntent);

				} else {
					Intent mainIntent = new Intent().setClass(MainActivity.this, LoginRegisterMenuActivity.class);
					startActivity(mainIntent);

				}
			}
		};

		Timer timer = new Timer();
		timer.schedule(task, SPLASH_DELAY);

		user = new User();
		lock = new Object();

	}

	// Clase asíncrona utilizada para llamar al servicio web chekID
	class CheckID extends AsyncTask<String, String, String> {
		@Override
		protected String doInBackground(String... params) {
			checkID();
			synchronized (lock) {

				lock.notify();
			}
			return null;
		}
	}

	// Método donde se ejecuta el servicio web checkID, donde se comprueba el
	// identificador Android del dispositivo
	private void checkID() {
		String request = "";
		try {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://IP_SERVIDOR/webservices/checkID.php");
			List<NameValuePair> pairs = new ArrayList<NameValuePair>(1);
			pairs.add(new BasicNameValuePair("idandroid",
					Secure.getString(getBaseContext().getContentResolver(), Secure.ANDROID_ID)));
			post.setEntity(new UrlEncodedFormEntity(pairs));
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			request = client.execute(post, responseHandler);
			String[] datas = request.split("/");
			if (!(datas.length == 0)) {
				for (int i = datas.length - 1; i >= datas.length - 3; i--) {
					user.setActive(datas[datas.length - 1].trim());
					user.setIDAndroid(datas[datas.length - 2].trim());
					user.setUserName(datas[datas.length - 3].trim());
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

	private boolean checkCode() {
		String code = Secure.getString(getBaseContext().getContentResolver(), Secure.ANDROID_ID);
		if (code.equals(user.getIDAndroid())) {
			if (user.getActive().equals("1")) {
				return true;
			}
		}
		return false;

	}
}
