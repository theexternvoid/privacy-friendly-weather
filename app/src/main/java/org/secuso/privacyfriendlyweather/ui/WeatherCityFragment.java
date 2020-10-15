package org.secuso.privacyfriendlyweather.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import org.secuso.privacyfriendlyweather.R;
import org.secuso.privacyfriendlyweather.database.AppDatabase;
import org.secuso.privacyfriendlyweather.database.data.CurrentWeatherData;
import org.secuso.privacyfriendlyweather.database.data.Forecast;
import org.secuso.privacyfriendlyweather.database.data.WeekForecast;
import org.secuso.privacyfriendlyweather.ui.RecycleList.CityWeatherAdapter;
import org.secuso.privacyfriendlyweather.ui.updater.IUpdateableCityUI;
import org.secuso.privacyfriendlyweather.ui.updater.ViewUpdater;

import java.util.List;

public class WeatherCityFragment extends Fragment implements IUpdateableCityUI {

    private int mCityId = -1;
    private int[] mDataSetTypes = new int[]{};

    private CityWeatherAdapter mAdapter;

    private RecyclerView recyclerView;


    public void setAdapter(CityWeatherAdapter adapter) {
        mAdapter = adapter;

        if (recyclerView != null) {
            recyclerView.setAdapter(mAdapter);
            recyclerView.setFocusable(false);
            recyclerView.setLayoutManager(getLayoutManager(getContext()));  //fixes problems with StaggeredGrid: After refreshing data only empty space shown below tab
        }
    }

    public void loadData() {
        CurrentWeatherData currentWeatherData = AppDatabase.getInstance(getContext()).currentWeatherDao().getCurrentWeatherByCityId(mCityId);

        if(currentWeatherData == null) {
            return;
        }

        if (currentWeatherData.getCity_id() == 0) {
            currentWeatherData.setCity_id(mCityId);
        }

        mAdapter = new CityWeatherAdapter(currentWeatherData, mDataSetTypes, getContext());

        ((Activity) getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setAdapter(mAdapter);
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        ViewUpdater.addSubscriber(this);
    }

    @Override
    public void onDetach() {
        ViewUpdater.removeSubscriber(this);

        super.onDetach();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_weather_forecast_city_overview, container, false);

        recyclerView = v.findViewById(R.id.weatherForecastRecyclerView);
        recyclerView.setLayoutManager(getLayoutManager(getContext()));

        Bundle args = getArguments();
        mCityId = args.getInt("city_id");
        mDataSetTypes = args.getIntArray("dataSetTypes");

        loadData();

        return v;
    }

    public RecyclerView.LayoutManager getLayoutManager(Context context) {
        int widthPixels = context.getResources().getDisplayMetrics().widthPixels;
        float density = context.getResources().getDisplayMetrics().density;
        float width = widthPixels / density;

        if (width > 500) {
            return new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        } else {
            return new LinearLayoutManager(context);
        }
    }

    @Override
    public void processNewWeatherData(CurrentWeatherData data) {
        if (data.getCity_id() == mCityId) {
            setAdapter(new CityWeatherAdapter(data, mDataSetTypes, getContext()));
        }
    }

    @Override
    public void updateForecasts(List<Forecast> forecasts) {
        if (forecasts != null && forecasts.size() > 0 && forecasts.get(0).getCity_id() == mCityId) {
            if (mAdapter != null) {
                mAdapter.updateForecastData(forecasts);
            }
        }
        //TODO Update Titlebar Text
    }

    @Override
    public void updateWeekForecasts(List<WeekForecast> forecasts) {
        if (forecasts != null && forecasts.size() > 0 && forecasts.get(0).getCity_id() == mCityId) {
            if (mAdapter != null) {
                mAdapter.updateWeekForecastData(forecasts);
            }
        }
        //TODO Update Titlebar Text
    }
}
