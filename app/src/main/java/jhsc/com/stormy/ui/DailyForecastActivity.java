package jhsc.com.stormy.ui;

import android.app.ListActivity;
import android.os.Bundle;

import jhsc.com.stormy.R;

public class DailyForecastActivity extends ListActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_daily_forecast);

    String[] daysOfTheWeek = { "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday",
            "Friday", "Saturday" };
  }
}
