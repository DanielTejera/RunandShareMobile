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
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View;
import android.view.View.OnClickListener;

public class LoginActivity extends Activity implements OnClickListener {

	private Button continueButton;

	private EditText userNameEditText;
	private EditText passwordEditText;

	private TextView passwordTextView;

	private String userName;
	private String password;

	public static User user;
	public Object lock;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_login);

		continueButton = (Button) findViewById(R.id.continueButton);

		userNameEditText = (EditText) findViewById(R.id.userNameEditText);
		passwordEditText = (EditText) findViewById(R.id.passwordEditText);

		passwordTextView = (TextView) findViewById(R.id.passwordTextView);

		continueButton.setOnClickListener(this);

		user = new User();
		lock = new Object();

	}

	public void onClick(View v) {
		userName = userNameEditText.getText().toString();
		password = passwordEditText.getText().toString();

		switch (v.getId()) {
		case R.id.continueButton:

			if (userName.equals("")) {
				userNameEditText.setText("Indique un nombre");
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

			if (user.getCount().equals("0")) {
				userNameEditText.setText("El usuario no existe");
				break;
			}

			if (password.equals("")) {
				passwordTextView.setTypeface(null, Typeface.BOLD_ITALIC);
				break;
			}

			new Login().execute();
			synchronized (lock) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			if (!checkCredentials()) {
				passwordTextView.setTypeface(null, Typeface.BOLD_ITALIC);
				passwordEditText.setText("");
				break;
			}
			passwordTextView.setTypeface(null, Typeface.NORMAL);

			new SetActive().execute();
			synchronized (lock) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			Intent intentTraining = new Intent(LoginActivity.this, TrainingActivity.class);
			startActivity(intentTraining);
			break;

		default:
			break;
		}
	}

	// Clase asíncrona utilizada para llamar al servicio web login
	class Login extends AsyncTask<String, String, String> {
		@Override
		protected String doInBackground(String... params) {
			login();
			synchronized (lock) {

				lock.notify();
			}
			return null;
		}
	}

	// Método donde se ejecuta el servicio web login, donde se produce el inicio
	// de sesión del usuario
	private void login() {
		String request = "";
		try {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://IP_SERVIDOR/webservices/login.php");
			List<NameValuePair> pairs = new ArrayList<NameValuePair>(1);
			pairs.add(new BasicNameValuePair("username", userName));
			post.setEntity(new UrlEncodedFormEntity(pairs));
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			request = client.execute(post, responseHandler);
			String[] datas = request.split("/");
			if (!(datas.length == 0)) {
				for (int i = datas.length - 1; i >= datas.length - 2; i--) {
					user.setPassword(datas[datas.length - 1].trim());
					user.setUserName(datas[datas.length - 2].trim());
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

	private boolean checkCredentials() {
		String passwordHash = RegisterActivity.convertPasswordToMD5Hash(password);
		if (passwordHash.equals(user.getPassword())) {
			return true;
		}

		return false;
	}

	// Clase asíncrona utilizada para llamar al servicio web chekUserName
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

	// Método donde se ejecuta el servicio web chekUserName, donde se comprueba
	// la existencia del nombre de usuario
	private void checkUserName() {
		String request = "";
		try {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://IP_SERVIDOR/webservices/checkUserName.php");
			List<NameValuePair> pairs = new ArrayList<NameValuePair>(1);
			pairs.add(new BasicNameValuePair("username", userName));
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
			pairs.add(new BasicNameValuePair("username", MainActivity.user.getUserName()));
			post.setEntity(new UrlEncodedFormEntity(pairs));
			client.execute(post);
		} catch (ClientProtocolException e) {
			Log.i("HTTPCLIENT", e.getLocalizedMessage());
		} catch (IOException e) {
			Log.i("HTTPCLIENT", e.getLocalizedMessage());
		}
	}
}
