package com.example.runandshare;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class RegisterActivity extends Activity implements OnClickListener {

	private Button continueRegisterButton;
	private Button cancelRegisterButton;

	private EditText userNameRegisterEditText;
	private EditText passwordRegisterEditText;
	private EditText repeatPasswordEditText;

	private TextView passwordRTextView;
	private TextView repeatPasswordTextView;

	private String userNameRegister;
	private String passwordRegister;
	private String repeatPasswordRegister;

	public Object lock;
	public static User user;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_register);

		continueRegisterButton = (Button) findViewById(R.id.continueRButton);
		cancelRegisterButton = (Button) findViewById(R.id.cancelRButton);
		continueRegisterButton.setOnClickListener(this);
		cancelRegisterButton.setOnClickListener(this);

		userNameRegisterEditText = (EditText) findViewById(R.id.userNameREditText);
		passwordRegisterEditText = (EditText) findViewById(R.id.passwordREditText);
		repeatPasswordEditText = (EditText) findViewById(R.id.repeatPasswordEditText);

		passwordRTextView = (TextView) findViewById(R.id.passwordRTextView);
		repeatPasswordTextView = (TextView) findViewById(R.id.repeatPasswordTextView);

		lock = new Object();
		user = new User();
	}

	public void onClick(View v) {
		userNameRegister = userNameRegisterEditText.getText().toString();
		passwordRegister = passwordRegisterEditText.getText().toString();
		repeatPasswordRegister = repeatPasswordEditText.getText().toString();

		switch (v.getId()) {
		case R.id.continueRButton:

			if (userNameRegister.equals("")) {
				userNameRegisterEditText.setText("Indique un nombre");
				break;
			}

			if (userNameRegister.indexOf(' ') != -1) {
				userNameRegisterEditText.setText("Espacios no permitidos");
				break;
			}

			if (passwordRegister.equals("")) {
				passwordRTextView.setTypeface(null, Typeface.BOLD_ITALIC);
				break;
			}

			if (repeatPasswordRegister.equals("")) {
				repeatPasswordTextView.setTypeface(null, Typeface.BOLD_ITALIC);
				break;
			}

			new CheckUserName().execute();
			synchronized (lock) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			if (!user.getCount().equals("0")) {
				userNameRegisterEditText.setText("En uso, escoja otro");
				break;
			}

			if (!passwordRegister.equals(repeatPasswordRegister)) {
				repeatPasswordEditText.setText("");
				passwordRegisterEditText.setText("");
				passwordRTextView.setTypeface(null, Typeface.BOLD_ITALIC);
				repeatPasswordTextView.setTypeface(null, Typeface.BOLD_ITALIC);
				break;
			}

			passwordRegister = convertPasswordToMD5Hash(passwordRegister);
			repeatPasswordRegister = convertPasswordToMD5Hash(repeatPasswordRegister);

			user.setUserName(userNameRegister);

			new Register().execute();
			synchronized (lock) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			new SetActive().execute();
			synchronized (lock) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			passwordRTextView.setTypeface(null, Typeface.NORMAL);
			repeatPasswordTextView.setTypeface(null, Typeface.NORMAL);

			Intent intentTraining = new Intent(RegisterActivity.this, TrainingActivity.class);
			startActivity(intentTraining);
			break;

		case R.id.cancelRButton:
			Intent intentMenu = new Intent(RegisterActivity.this, LoginRegisterMenuActivity.class);
			startActivity(intentMenu);

			break;

		default:
			break;
		}
	}

	// Clase asíncrona utilizada para llamar al servicio web register
	class Register extends AsyncTask<String, String, String> {
		@Override
		protected String doInBackground(String... params) {
			register();
			synchronized (lock) {

				lock.notify();
			}
			return null;
		}

	}

	// Método donde se ejecuta el servicio web register, donde se efectua el
	// registro del usuario
	private void register() {
		try {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://IP_SERVIDOR/webservices/registerUser.php");
			List<NameValuePair> pairs = new ArrayList<NameValuePair>(4);
			pairs.add(new BasicNameValuePair("username", userNameRegister));
			pairs.add(new BasicNameValuePair("password", passwordRegister));
			pairs.add(new BasicNameValuePair("idandroid",
					Secure.getString(getBaseContext().getContentResolver(), Secure.ANDROID_ID)));
			pairs.add(new BasicNameValuePair("active", "1"));
			post.setEntity(new UrlEncodedFormEntity(pairs));
			client.execute(post);

		} catch (ClientProtocolException e) {
			Log.i("HTTPCLIENT", e.getLocalizedMessage());
		} catch (IOException e) {
			Log.i("HTTPCLIENT", e.getLocalizedMessage());
		}
	}

	// Clase asíncrona utilizada para llamar al servicio web checkUserName
	class CheckUserName extends AsyncTask<String, String, String> {
		@Override
		protected String doInBackground(String... params) {
			checkUserName();
			synchronized (lock) {

				lock.notify();
			}
			return null;
		}
	}

	// Método donde se ejecuta el servicio web checkUserName, donde se comprueba
	// el nombre del usuario
	private void checkUserName() {
		String request = "";
		try {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://IP_SERVIDOR/webservices/checkUserName.php");
			List<NameValuePair> pairs = new ArrayList<NameValuePair>(1);
			pairs.add(new BasicNameValuePair("username", userNameRegister));
			post.setEntity(new UrlEncodedFormEntity(pairs));
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			request = client.execute(post, responseHandler);
			String[] datas = request.split("/");
			if (!(datas.length == 0)) {
				for (int i = datas.length - 1; i >= datas.length - 1; i--) {
					user.setCount(datas[datas.length - 1].trim());
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

	// Clase asíncrona utilizada para llamar al servicio web setActive
	class SetActive extends AsyncTask<String, String, String> {
		@Override
		protected String doInBackground(String... params) {
			setActive();
			synchronized (lock) {

				lock.notify();
			}
			return null;
		}

	}

	// Método donde se ejecuta el servicio web setActive, donde se activa la
	// sesión automática para el usuario
	private void setActive() {
		try {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://IP_SERVIDOR/webservices/setActive.php");
			List<NameValuePair> pairs = new ArrayList<NameValuePair>(1);
			pairs.add(new BasicNameValuePair("username", userNameRegister));
			post.setEntity(new UrlEncodedFormEntity(pairs));
			client.execute(post);
		} catch (ClientProtocolException e) {
			Log.i("HTTPCLIENT", e.getLocalizedMessage());
		} catch (IOException e) {
			Log.i("HTTPCLIENT", e.getLocalizedMessage());
		}
	}

}
