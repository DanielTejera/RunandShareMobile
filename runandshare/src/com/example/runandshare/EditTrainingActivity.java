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
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

public class EditTrainingActivity extends Activity implements OnClickListener {

	private Button continueChangeButton;
	private Button cancelChangeButton;
	private ImageButton menuButton;

	private EditText changeTrainingNameEditText;

	private String changeTrainingName;

	public Object lock;
	private Training training;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_edit_training);
		lock = new Object();

		continueChangeButton = (Button) findViewById(R.id.continueChangeButton);
		cancelChangeButton = (Button) findViewById(R.id.cancelChangeButton);
		menuButton = (ImageButton) findViewById(R.id.menuButton);

		continueChangeButton.setOnClickListener(this);
		cancelChangeButton.setOnClickListener(this);
		menuButton.setOnClickListener(this);

		changeTrainingNameEditText = (EditText) findViewById(R.id.changeTrainingNameEditText);

		training = new Training();
		loadTraining();

	}

	private void loadTraining() {
		if (UserTrainingsActivity.training1.getEdit()) {
			training.setTrainingName(UserTrainingsActivity.training1.getTrainingName());
			return;
		}
		if (UserTrainingsActivity.training2.getEdit()) {
			Log.i("trainingname", UserTrainingsActivity.training2.getTrainingName());
			training.setTrainingName(UserTrainingsActivity.training2.getTrainingName());
			return;
		}
		if (UserTrainingsActivity.training3.getEdit()) {
			training.setTrainingName(UserTrainingsActivity.training3.getTrainingName());
			return;
		}
		if (UserTrainingsActivity.training4.getEdit()) {
			training.setTrainingName(UserTrainingsActivity.training4.getTrainingName());
			return;
		}
		if (UserTrainingsActivity.training5.getEdit()) {
			training.setTrainingName(UserTrainingsActivity.training5.getTrainingName());
			return;
		}
	}

	public void onClick(View v) {

		changeTrainingName = changeTrainingNameEditText.getText().toString();

		switch (v.getId()) {
		case R.id.menuButton:
			Intent menuIntent = new Intent(EditTrainingActivity.this, NavigationActivity.class);
			startActivity(menuIntent);
			break;
		case R.id.continueChangeButton:

			if (changeTrainingName.equals("")) {
				changeTrainingNameEditText.setText("Indique un nombre");
				break;
			}

			if (changeTrainingName.indexOf(' ') != -1) {
				changeTrainingNameEditText.setText("Espacios no permitidos");
				break;
			}
			new CheckTrainingName().execute();
			synchronized (lock) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			if (!training.getCount().equals("0")) {
				changeTrainingNameEditText.setText("En uso, escoja otro");
				break;
			}

			new ChangeTrainingName().execute();
			synchronized (lock) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			Intent intentUserTrainings = new Intent(EditTrainingActivity.this, UserTrainingsActivity.class);
			startActivity(intentUserTrainings);
			finish();
			break;

		case R.id.cancelChangeButton:
			Intent intentUserTrainings2 = new Intent(EditTrainingActivity.this, UserTrainingsActivity.class);
			startActivity(intentUserTrainings2);
			break;

		default:
			break;
		}
	}

	// Clase asíncrona utilizada para llamar al servicio web changeTrainingName
	class ChangeTrainingName extends AsyncTask<String, String, String> {
		@Override
		protected String doInBackground(String... params) {
			changeTrainingName();
			synchronized (lock) {

				lock.notify();
			}
			return null;
		}

	}

	// Método donde se ejecuta el servicio web changeTrainingName
	private void changeTrainingName() {
		try {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://IP_SERVIDOR/webservices/changeTrainingName.php");
			List<NameValuePair> pairs = new ArrayList<NameValuePair>(2);
			Log.i("trainingname", training.getTrainingName());
			Log.i("new", changeTrainingName);
			pairs.add(new BasicNameValuePair("trainingname", training.getTrainingName()));
			pairs.add(new BasicNameValuePair("new_trainingname", changeTrainingName));
			post.setEntity(new UrlEncodedFormEntity(pairs));
			client.execute(post);
		} catch (ClientProtocolException e) {
			Log.i("HTTPCLIENT", e.getLocalizedMessage());
		} catch (IOException e) {
			Log.i("HTTPCLIENT", e.getLocalizedMessage());
		}
	}

	class CheckTrainingName extends AsyncTask<String, String, String> {
		@Override
		protected String doInBackground(String... params) {
			checkTrainingName();
			synchronized (lock) {

				lock.notify();
			}
			return null;
		}
	}

	private void checkTrainingName() {
		String request = "";
		try {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://IP_SERVIDOR/webservices/checkTrainingName.php");
			List<NameValuePair> pairs = new ArrayList<NameValuePair>(1);
			pairs.add(new BasicNameValuePair("trainingname", changeTrainingName));
			post.setEntity(new UrlEncodedFormEntity(pairs));
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			request = client.execute(post, responseHandler);
			String[] datas = request.split("/");
			if (!(datas.length == 0)) {
				for (int i = datas.length - 1; i >= datas.length - 1; i--) {
					training.setCount(datas[datas.length - 1].trim());
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

}
