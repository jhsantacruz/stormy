package jhsc.com.stormy.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import jhsc.com.stormy.R;
import jhsc.com.stormy.Setting;
import jhsc.com.stormy.weather.Current;
import jhsc.com.stormy.weather.Day;
import jhsc.com.stormy.weather.Forecast;
import jhsc.com.stormy.weather.Hour;


public class MainActivity extends Activity {
  public static final String TAG = MainActivity.class.getSimpleName();

  private Forecast mForecast;

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

            if (response.isSuccessful()) {
              mForecast = parseForcastDetails(jsonData);
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
    Current current = mForecast.getCurrent();

    mTemperatureLabel.setText(current.getTemperature() + "");
    YoYo.with(Techniques.RubberBand)
            .duration(700)
            .playOn(findViewById(R.id.temperatureLabel));

    mTimeLabel.setText("At " + current.getFormattedTime() + " it will be");
    mHumidityValue.setText(current.getHumidity() + "");
    mPrecipValue.setText(current.getPrecipChance() + "%");
    mSummaryLabel.setText(current.getSummary());

    Drawable drawable = getResources().getDrawable(current.getIconId());
    mIconImageView.setImageDrawable(drawable);
  }

  private Forecast parseForcastDetails(String jsonData) throws JSONException{
    Forecast forecast = new Forecast();

    forecast.setCurrent(getCurrentDetails(jsonData));
    forecast.setHourlyForecast(getHourlyForecast(jsonData));
    forecast.setDailyForecast(getDailyForecast(jsonData));

    return forecast;
  }

  private Day[] getDailyForecast(String jsonData) throws JSONException{
    JSONObject forecast = new JSONObject(jsonData);
    String timezone = forecast.getString("timezone");

    JSONObject daily = forecast.getJSONObject("daily");
    JSONArray data = daily.getJSONArray("data");

    Day[] days = new Day[data.length()];

    for(int i=0; i < data.length(); i++){
      JSONObject jsonDay = data.getJSONObject(i);
      Day day = new Day();

      day.setSummary(jsonDay.getString("summary"));
      day.setIcon(jsonDay.getString("icon"));
      day.setTemperatureMax(jsonDay.getDouble("temperatureMax"));
      day.setTime(jsonDay.getLong("time"));
      day.setTimezone(timezone);

      days[i] = day;
    }

    return days;
  }

  private Hour[] getHourlyForecast(String jsonData) throws JSONException{
    JSONObject forecast = new JSONObject(jsonData);
    String timezone = forecast.getString("timezone");
    JSONObject hourly = forecast.getJSONObject("hourly");
    JSONArray data = hourly.optJSONArray("data");

    Hour[] hours = new Hour[data.length()];

    for(int i=0; i < data.length(); i++){
      JSONObject jsonHour = data.getJSONObject(i);
      Hour hour = new Hour();

      hour.setSummary(jsonHour.getString("summary"));
      hour.setTemperature(jsonHour.getDouble("temperature"));
      hour.setIcon(jsonHour.getString("icon"));
      hour.setTime(jsonHour.getLong("time"));
      hour.setTimezone(timezone);

      hours[i] = hour;
    }

    return hours;
  }

  private Current getCurrentDetails(String jsonData) throws JSONException{
    JSONObject forecast = new JSONObject(jsonData);
    String timezone = forecast.getString("timezone");

    JSONObject currently = forecast.getJSONObject("currently");

    Current current = new Current();
    current.setHumidity(currently.getDouble("humidity"));
    current.setTime(currently.getLong("time"));
    current.setIcon(currently.getString("icon"));
    current.setPrecipChance(currently.getDouble("precipProbability"));
    current.setSummary(currently.getString("summary"));
    current.setTemperature(currently.getDouble("temperature"));
    current.setTimeZone(timezone);

    return current;
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
}
