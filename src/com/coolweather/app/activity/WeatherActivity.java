package com.coolweather.app.activity;

import android.R.string;
import android.app.Activity;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coolweather.app.R;
import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;

public class WeatherActivity extends Activity implements android.view.View.OnClickListener{
	private LinearLayout weatherInfoLayout;
	private TextView cityName;
	private TextView publishText;
	private TextView temp1;
	private TextView temp2;
	private TextView currentDateText;
	private TextView weatherDespText;
	private Button switchCity;
	private Button  refresh;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	requestWindowFeature(Window.FEATURE_NO_TITLE);
	setContentView(R.layout.weather_layout);
	weatherInfoLayout=(LinearLayout)findViewById(R.id.weather_info_layout);
	cityName=(TextView)findViewById(R.id.city_name);
	publishText=(TextView)findViewById(R.id.publish_text);
	temp1=(TextView)findViewById(R.id.temp1);
	temp2=(TextView)findViewById(R.id.temp2);
	switchCity=(Button)findViewById(R.id.switch_city);
	refresh=(Button)findViewById(R.id.refresh_weather);
	switchCity.setOnClickListener(this);
	refresh.setOnClickListener(this);
	currentDateText=(TextView)findViewById(R.id.current_data);
	weatherDespText=(TextView)findViewById(R.id.weather_desp);
	String countryCode=getIntent().getStringExtra("country_code");
	
	if(!TextUtils.isEmpty(countryCode)){
		publishText.setText("同步中.....");
		weatherInfoLayout.setVisibility(View.INVISIBLE);
		queryWeatherCode(countryCode);
	}else{
		showWeather();
	}
	
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {		
		case R.id.switch_city:
			 Intent intent=new Intent (this,ChooseAreaActivity.class);
			 intent.putExtra("from_weather_activity", true);
			 startActivity(intent);
			 finish();
			
			break;
		case R.id.refresh_weather:
			publishText.setText("同步中.....");
			SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
			String weatherCode=prefs.getString("weather_code", "");
			if (!TextUtils.isEmpty(weatherCode)) {
				queryWeatherInfo(weatherCode);
				
			} 
			break;

		default:
			break;
		}
		
	}
	
	private void queryWeatherCode(String countryCode){
		String address ="http://www.weather.com.cn/data/list3/city"+countryCode+".xml";
	   queryFromServer(address,"countryCode");
	}
	
	private void queryWeatherInfo(String weatherCode){
		String address ="http://www.weather.com.cn/data/cityinfo/"+weatherCode+".html";
	   queryFromServer(address,"weatherCode");
	}
	private void queryFromServer(final String address,final String type){
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(final String response) {
				if("countryCode".equals(type)){
					String[] array =response.split("\\|");
					if(array!=null&&array.length==2){
						String weatherCode=array[1];
						queryWeatherInfo(weatherCode);
					}
				}else if ("weatherCode".equals(type)) {
					Utility.handleWeatherResponse(WeatherActivity.this, response);
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							showWeather();
							
						}
					});
				}
				
			}
			
			@Override
			public void onError(Exception e) {
				// TODO Auto-generated method stub
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						publishText.setText("同步失败");
						
					}
				});
				
				
			}
		});
		
		
	}
	      private void showWeather(){
	    	  SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
	    	  cityName.setText(prefs.getString("city", ""));
	    	  temp1.setText(prefs.getString("temp1",""));
	    	  temp2.setText(prefs.getString("temp2",""));
	    	  weatherDespText.setText(prefs.getString("weather_desp",""));
	    	  publishText.setText("今天"+prefs.getString("publish_time","")+"发布");
	    	  currentDateText.setText(prefs.getString("current_date",""));
	    	  weatherInfoLayout.setVisibility(View.VISIBLE);
	    	  cityName.setVisibility(View.VISIBLE);
	      }
	     
	
}
