package com.example.runandshare;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import android.view.View.OnClickListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

public class ProfileActivity extends Activity implements OnClickListener {

	private ImageButton logout;
	private ImageButton changePasswordButton;
	private ImageButton deleteProfileButton;
	private ImageButton menuButton;

	public Object lock;
	public static User user;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_profile);

		logout = (ImageButton) findViewById(R.id.logoutButton);
		changePasswordButton = (ImageButton) findViewById(R.id.changePasswordButton);
		deleteProfileButton = (ImageButton) findViewById(R.id.deleteProfileButton);
		menuButton = (ImageButton) findViewById(R.id.menuButton);

		logout.setOnClickListener(this);
		changePasswordButton.setOnClickListener(this);
		deleteProfileButton.setOnClickListener(this);
		menuButton.setOnClickListener(this);

		lock = new Object();
		user = new User();

		new LoadUser().execute();
		synchronized (lock) {
			try {
				lock.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.menuButton:
			Intent menuIntent = new Intent(ProfileActivity.this, NavigationActivity.class);
			startActivity(menuIntent);
			break;
		case R.id.logoutButton:
			new LogOut().execute();
			synchronized (lock) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			Intent intentUserTrainings = new Intent(ProfileActivity.this, LoginRegisterMenuActivity.class);
			startActivity(intentUserTrainings);
			break;

		case R.id.changePasswordButton:
			Intent intentChangePassword = new Intent(ProfileActivity.this, ChangePasswordActivity.class);
			startActivity(intentChangePassword);
			break;

		case R.id.deleteProfileButton:
			alertDeleteProfile();
			break;

		default:
			break;
		}

	}

	public void alertDeleteProfile() {
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:

					new DeleteProfile().execute();
					synchronized (lock) {
						try {
							lock.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					Intent intent = new Intent(ProfileActivity.this, LoginRegisterMenuActivity.class);
					startActivity(intent);
					break;

				case DialogInterface.BUTTON_NEGATIVE:

					break;
				}
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Precaución");
		builder.setMessage("Perderá todos sus datos y su cuenta ¿Desea continuar?")
				.setPositiveButton("Sí", dialogClickListener).setNegativeButton("No", dialogClickListener).show();
	}

	// Clase asíncrona utilizada para llamar al servicio web deleteProfile
	private class DeleteProfile extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			deleteProfile();
			synchronized (lock) {

				lock.notify();
			}
			return null;
		}
	}

	// Método donde se ejecuta el servicio web deleteProfile, que borra el perfil del usuario
	private void deleteProfile() {
		try {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://IP_SERVIDOR/webservices/deleteProfile.php");
			List<NameValuePair> pairs = new ArrayList<NameValuePair>(1);
			pairs.add(new BasicNameValuePair("username", user.getUserName()));
			post.setEntity(new UrlEncodedFormEntity(pairs));
			client.execute(post);
		} catch (ClientProtocolException e) {
			Log.i("HTTPCLIENT", e.getLocalizedMessage());
		} catch (IOException e) {
			Log.i("HTTPCLIENT", e.getLocalizedMessage());
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

	// Método donde se ejecuta el servicio web loadUser, donde cargamos los datos del usuario
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

	// Clase asíncrona utilizada para llamar al servicio web logOut
	class LogOut extends AsyncTask<String, String, String> {
		@Override
		protected String doInBackground(String... params) {
			logout();
			synchronized (lock) {

				lock.notify();
			}
			return null;
		}

	}

	// Método donde se ejecuta el servicio web logOut, donde se ejecuta el cierre de sesión del usuario
	private void logout() {
		try {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://IP_SERVIDOR/webservices/logout.php");
			List<NameValuePair> pairs = new ArrayList<NameValuePair>(1);
			pairs.add(new BasicNameValuePair("username", user.getUserName()));
			post.setEntity(new UrlEncodedFormEntity(pairs));
			client.execute(post);
		} catch (ClientProtocolException e) {
			Log.i("HTTPCLIENT", e.getLocalizedMessage());
		} catch (IOException e) {
			Log.i("HTTPCLIENT", e.getLocalizedMessage());
		}
	}
}
