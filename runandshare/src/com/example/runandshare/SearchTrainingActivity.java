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
import android.widget.TextView;

public class SearchTrainingActivity extends Activity implements OnClickListener {

	private EditText trainingNameEditText;
	private Button search;

	private TextView visitsTextView;
	private TextView trainingTextView;

	private TextView visits1;
	private TextView visits2;
	private TextView visits3;
	private TextView visits4;
	private TextView visits5;

	private Button training1;
	private Button training2;
	private Button training3;
	private Button training4;
	private Button training5;
	private ImageButton menuButton;

	private String trainingName;
	private String trainingToAdd;

	public static Training searchTraining;
	public static Training training_1;
	public static Training training_2;
	public static Training training_3;
	public static Training training_4;
	public static Training training_5;

	private Object lock;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_search_training);

		visitsTextView = (TextView) findViewById(R.id.visitsTextView);
		visits1 = (TextView) findViewById(R.id.visits1);
		visits2 = (TextView) findViewById(R.id.visits2);
		visits3 = (TextView) findViewById(R.id.visits3);
		visits4 = (TextView) findViewById(R.id.visits4);
		visits5 = (TextView) findViewById(R.id.visits5);

		trainingTextView = (TextView) findViewById(R.id.trainingTextView);
		training1 = (Button) findViewById(R.id.training1);
		training2 = (Button) findViewById(R.id.training2);
		training3 = (Button) findViewById(R.id.training3);
		training4 = (Button) findViewById(R.id.training4);
		training5 = (Button) findViewById(R.id.training5);
		menuButton = (ImageButton) findViewById(R.id.menuButton);

		search = (Button) findViewById(R.id.searchButton);

		trainingNameEditText = (EditText) findViewById(R.id.trainingNameEditText);

		training1.setOnClickListener(this);
		training2.setOnClickListener(this);
		training3.setOnClickListener(this);
		training4.setOnClickListener(this);
		training5.setOnClickListener(this);

		search.setOnClickListener(this);
		menuButton.setOnClickListener(this);

		lock = new Object();
		searchTraining = new Training();
		training_1 = new Training();
		training_2 = new Training();
		training_3 = new Training();
		training_4 = new Training();
		training_5 = new Training();

		new GetTop().execute();
		synchronized (lock) {
			try {
				lock.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		training1.setText(training_1.getTrainingName());
		visits1.setText(training_1.getVisits());

		training2.setText(training_2.getTrainingName());
		visits2.setText(training_2.getVisits());

		training3.setText(training_3.getTrainingName());
		visits3.setText(training_3.getVisits());

		training4.setText(training_4.getTrainingName());
		visits4.setText(training_4.getVisits());

		training5.setText(training_5.getTrainingName());
		visits5.setText(training_5.getVisits());
	}

	public void onClick(View v) {
		trainingName = trainingNameEditText.getText().toString();

		switch (v.getId()) {
		case R.id.menuButton:
			Intent menuIntent = new Intent().setClass(SearchTrainingActivity.this, NavigationActivity.class);
			startActivity(menuIntent);
			break;
		case R.id.training1:
			training_1.setLoad(true);
			trainingToAdd = training_1.getTrainingName();
			new AddVisit().execute();
			synchronized (lock) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			Intent followIntent1 = new Intent().setClass(SearchTrainingActivity.this, FollowTrainingActivity.class);
			startActivity(followIntent1);
			break;
		case R.id.training2:
			training_2.setLoad(true);
			trainingToAdd = training_2.getTrainingName();
			new AddVisit().execute();
			synchronized (lock) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			Intent followIntent2 = new Intent().setClass(SearchTrainingActivity.this, FollowTrainingActivity.class);
			startActivity(followIntent2);
			break;
		case R.id.training3:
			training_3.setLoad(true);
			trainingToAdd = training_3.getTrainingName();
			new AddVisit().execute();
			synchronized (lock) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			Intent followIntent3 = new Intent().setClass(SearchTrainingActivity.this, FollowTrainingActivity.class);
			startActivity(followIntent3);
			break;
		case R.id.training4:
			training_4.setLoad(true);
			trainingToAdd = training_4.getTrainingName();
			new AddVisit().execute();
			synchronized (lock) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			Intent followIntent4 = new Intent().setClass(SearchTrainingActivity.this, FollowTrainingActivity.class);
			startActivity(followIntent4);
			break;
		case R.id.training5:
			training_5.setLoad(true);
			trainingToAdd = training_5.getTrainingName();
			new AddVisit().execute();
			synchronized (lock) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			Intent followIntent5 = new Intent().setClass(SearchTrainingActivity.this, FollowTrainingActivity.class);
			startActivity(followIntent5);
			break;
		case R.id.searchButton:

			if (trainingName.equals("")) {
				trainingNameEditText.setText("Indique un nombre");
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
			
			
			if (searchTraining.getCount().equals("0")) {
				trainingNameEditText.setText("Entrenamiento inexistente");
				break;
			}

			searchTraining.setTrainingName(trainingName);
			searchTraining.setLoad(true);
			
			trainingToAdd = searchTraining.getTrainingName();
			new AddVisit().execute();
			synchronized (lock) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			Intent intentTraining = new Intent(SearchTrainingActivity.this, FollowTrainingActivity.class);
			startActivity(intentTraining);
			finish();
			break;

		default:
			break;
		}
	}

	// Clase asíncrona utilizada para llamar al servicio web chekTrainingName
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

	// Método donde recibimos el número de veces que aparece en la base de datos el nombre de usuario
	private void checkTrainingName() {
		String request = "";
		try {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://IP_SERVIDOR/webservices/checkTrainingName.php");
			List<NameValuePair> pairs = new ArrayList<NameValuePair>(1);
			pairs.add(new BasicNameValuePair("trainingname", trainingName));
			post.setEntity(new UrlEncodedFormEntity(pairs));
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			request = client.execute(post, responseHandler);
			String[] datas = request.split("/");
			if (!(datas.length == 0)) {
				for (int i = datas.length - 1; i >= datas.length - 1; i--) {
					searchTraining.setCount(datas[datas.length - 1].trim());
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

	// Clase asíncrona utilizada para llamar al servicio web getTop
	class GetTop extends AsyncTask<String, String, String> {
		@Override
		protected String doInBackground(String... params) {
			getTop();
			synchronized (lock) {

				lock.notify();
			}
			return null;
		}
	}

	// Método donde recibimos el top 5 de las carreras mas visitadas
	private void getTop() {
		String request = "";
		try {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://IP_SERVIDOR/webservices/getTop.php");
			List<NameValuePair> pairs = new ArrayList<NameValuePair>(0);
			post.setEntity(new UrlEncodedFormEntity(pairs));
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			request = client.execute(post, responseHandler);
			String[] datas = request.split("<br>");
			Training trainingZero = new Training();
			if (!(datas.length == 0)) {
				Log.i("-----", "entro en el primer if");
				for (int i = datas.length - 1; i >= datas.length - 5; i--) {
					String[] sections = datas[i].split("/");
					Log.i("--------", String.valueOf(sections.length));

					trainingZero.setTrainingName(sections[0]);
					trainingZero.setVisits(sections[1]);

					Log.i("Nombre", trainingZero.getTrainingName());
					Log.i("Visitas", trainingZero.getVisits());

					if (training_5.getVisits().equals("")) {
						training_5 = trainingZero;
						trainingZero = new Training();
					} else if (training_4.getVisits().equals("")) {
						training_4 = trainingZero;
						trainingZero = new Training();
					} else if (training_3.getVisits().equals("")) {
						training_3 = trainingZero;
						trainingZero = new Training();
					} else if (training_2.getVisits().equals("")) {
						training_2 = trainingZero;
						trainingZero = new Training();
					} else if (training_1.getVisits().equals("")) {
						training_1 = trainingZero;
						trainingZero = new Training();
					}

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

	// Clase asíncrona donde se llama al servicio web addVisit()
	class AddVisit extends AsyncTask<String, String, String> {
		@Override
		protected String doInBackground(String... params) {
			addVisit();
			synchronized (lock) {

				lock.notify();
			}
			return null;
		}
	}

	// Método donde se llama al servicio web addVisit
	private void addVisit() {
		String request = "";
		try {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://IP_SERVIDOR/webservices/addVisit.php");
			List<NameValuePair> pairs = new ArrayList<NameValuePair>(1);
			pairs.add(new BasicNameValuePair("trainingname", trainingToAdd));
			post.setEntity(new UrlEncodedFormEntity(pairs));
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			request = client.execute(post, responseHandler);

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
