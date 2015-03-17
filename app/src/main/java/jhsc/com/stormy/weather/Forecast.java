package jhsc.com.stormy.weather;

/**
 * Created by xmortumx on 3/16/15.
 */
public class Forecast {
  public Current getCurrent() {
    return mCurrent;
  }

  public void setCurrent(Current current) {
    mCurrent = current;
  }

  public Hour[] getHourlyForecast() {
    return mHourlyForecast;
  }

  public void setHourlyForecast(Hour[] hourlyForecast) {
    mHourlyForecast = hourlyForecast;
  }

  public Day[] getDailyForecast() {
    return mDailyForecast;
  }

  public void setDailyForecast(Day[] dailyForecast) {
    mDailyForecast = dailyForecast;
  }

  private Current mCurrent;
  private Hour[] mHourlyForecast;
  private Day [] mDailyForecast;
}
