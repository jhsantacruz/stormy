package jhsc.com.stormy;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class MainActivity extends Activity {
  public static final String TAG = MainActivity.class.getSimpleName();

  private CurrentWeather mCurrentWeather;

  @InjectView(R.id.timeLabel) TextView mTimeLabel;
  @InjectView(R.id.temperatureLabel) TextView mTemperatureLabel;
  @InjectView(R.id.humidityValue) TextView mHumidityValue;
  @InjectView(R.id.precipValue) TextView mPrecipValue;
  @InjectView(R.id.summaryLabel) TextView mSummaryLabel;
  @InjectView(R.id.iconImageView) ImageView mIconImageView;
  @InjectView(R.id.refreshImageView) ImageView mRefreshImageView;
  @InjectView(R.id.progressBar) ProgressBar mProgressBar;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.inject(this);


    mProgressBar.setVisibility(View.INVISIBLE);
    ActionBar actionBar =  getActionBar();
    actionBar.setDisplayShowHomeEnabled(false);
    actionBar.setDisplayShowTitleEnabled(false);

    mRefreshImageView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        getForecast();
      }
    });

    getForecast();
  }

  private void getForecast() {
    String apiKey = Setting.FORECAST_API_KEY;
    double latitude = 33.9830556;
    double longitude = -118.0958333;

    String forecastUrl = "https://api.forecast.io/forecast/" + apiKey +
            "/" + latitude +  "," + longitude;

    if (isNetworkAvailable()) {
      toggleRefresh();

      OkHttpClient client = new OkHttpClient();
      Request request = new Request.Builder().url(forecastUrl).build();

      Call call = client.newCall(request);
      call.enqueue(new Callback() {
        @Override
        public void onFailure(Request request, IOException e) {
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              toggleRefresh();
            }
          });
          alertUserABoutError();
        }

        @Override
        public void onResponse(Response response) throws IOException {
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              toggleRefresh();
            }
          });

          try {
            String jsonData = response.body().string();

            Log.v(TAG, jsonData);
            if (response.isSuccessful()) {
              mCurrentWeather = getCurrentDetails(jsonData);
              runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  updateDisplay();
                }
              });
            } else {
              alertUserABoutError();
            }
          } catch (IOException e) {
            Log.e(TAG, "Exception caught: ", e);
          } catch (JSONException e){
            Log.e(TAG, "Exception caught: ", e);
          }
        }
      });
    } else {
      Toast.makeText(this, getString(R.string.network_unavailable_text), Toast.LENGTH_SHORT).show();
    }
  }

  private void toggleRefresh() {
    if (mProgressBar.getVisibility() == View.INVISIBLE){
      mProgressBar.setVisibility(View.VISIBLE);
      mRefreshImageView.setVisibility(View.INVISIBLE);
    } else {
      mProgressBar.setVisibility(View.INVISIBLE);
      mRefreshImageView.setVisibility(View.VISIBLE);
    }
  }

  private void updateDisplay() {
    mTemperatureLabel.setText(mCurrentWeather.getTemperature() + "");
    YoYo.with(Techniques.RubberBand)
            .duration(700)
            .playOn(findViewById(R.id.temperatureLabel));

    mTimeLabel.setText("At " + mCurrentWeather.getFormattedTime() + " it will be");
    mHumidityValue.setText(mCurrentWeather.getHumidity() + "");
    mPrecipValue.setText(mCurrentWeather.getPrecipChance() + "%");
    mSummaryLabel.setText(mCurrentWeather.getSummary());

    Drawable drawable = getResources().getDrawable(mCurrentWeather.getIconId());
    mIconImageView.setImageDrawable(drawable);
  }

  private CurrentWeather getCurrentDetails(String jsonData) throws JSONException{
    JSONObject forecast = new JSONObject(jsonData);
    String timezone = forecast.getString("timezone");

    JSONObject currently = forecast.getJSONObject("currently");

    CurrentWeather currentWeather = new CurrentWeather();
    currentWeather.setHumidity(currently.getDouble("humidity"));
    currentWeather.setTime(currently.getLong("time"));
    currentWeather.setIcon(currently.getString("icon"));
    currentWeather.setPrecipChance(currently.getDouble("precipProbability"));
    currentWeather.setSummary(currently.getString("summary"));
    currentWeather.setTemperature(currently.getDouble("temperature"));
    currentWeather.setTimeZone(timezone);

    return  currentWeather;
  }

  private boolean isNetworkAvailable() {
    ConnectivityManager manager = (ConnectivityManager)
            getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = manager.getActiveNetworkInfo();
    boolean isAvailable = false;

    if (networkInfo != null && networkInfo.isConnected()){
      isAvailable = true;
    }
    return  isAvailable;
  }

  private void alertUserABoutError() {
    AlertDialogFragment dialog = new AlertDialogFragment();
    dialog.show(getFragmentManager(), "error_dialog");
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()){
      case R.id.action_settings:
      return true;
    }
    return super.onOptionsItemSelected(item);
  }
}
