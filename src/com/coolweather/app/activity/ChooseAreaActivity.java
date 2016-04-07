package com.coolweather.app.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.coolweather.app.R;
import com.coolweather.app.db.CoolWeatherDB;
import com.coolweather.app.model.City;
import com.coolweather.app.model.Country;
import com.coolweather.app.model.Province;
import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;

public class ChooseAreaActivity extends Activity {

	
		public static final int  LEVEL_PROVINCE=0;
		public static final int  LEVEL_CITY=1;
		public static final int  LEVEL_COUNTRY=2;
		
		private ProgressDialog progressDialog;
		private TextView titleView;
		private ListView listView;
		private ArrayAdapter<String> adapter;
		private CoolWeatherDB coolWeatherDB;
		private List<String> dataList=new ArrayList<String>();
		private List<Province> provinceList;
		private List<City> cityList;
		private List<Country> countryList;
		private Province selectedProvince;
		private City selectedCity;
		private int currnetLevel;
		
		@Override
		protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		listView=(ListView)findViewById(R.id.list_View);
		titleView=(TextView)findViewById(R.id.title_text);
		adapter= new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,dataList);
		listView.setAdapter(adapter);
		coolWeatherDB=CoolWeatherDB.getInstance(this);
		            listView.setOnItemClickListener(new OnItemClickListener() {
		            	@Override
		            	public void onItemClick(AdapterView<?> arg0, View view,
		            			int index, long arg3) {
		            		if(currnetLevel==LEVEL_PROVINCE){
		            			selectedProvince=provinceList.get(index);
		            			queryCities();
		            			
		            		}else if (currnetLevel==LEVEL_CITY) {
		            			selectedCity=cityList.get(index);
		            			queryCounties();
								
							}
		            		
		            	}
					});
               queryProvinces();
		}
		
		private void queryProvinces(){
			provinceList=coolWeatherDB.loadProvinces();
			if(provinceList.size()>0){
				dataList.clear();
				 for(Province province:provinceList){
					 dataList.add(province.getProvinceName());
				 }
				 adapter.notifyDataSetChanged();
				 listView.setSelection(0);
				 titleView.setText("�й�");
				 currnetLevel=LEVEL_PROVINCE;
			}else{
				queryFromServer(null,"province");
			}
		}
		
		private void queryCities(){
			cityList=coolWeatherDB.loadCities(selectedProvince.getId());
			if(cityList.size()>0){
				dataList.clear();
				 for(City city : cityList){
					 dataList.add(city.getCityName());
				 }
				 adapter.notifyDataSetChanged();
				 listView.setSelection(0);
				 titleView.setText(selectedProvince.getProvinceName());
				 currnetLevel=LEVEL_CITY;
			}else{
				queryFromServer(selectedProvince.getProvinceCode(),"city");
			}
		}
		
		
		private void queryCounties(){
			countryList=coolWeatherDB.loadCounties(selectedCity.getId());
			if(countryList.size()>0){
				dataList.clear();
				 for(Country country:countryList){
					 dataList.add(country.getCountryName());
				 }
				 adapter.notifyDataSetChanged();
				 listView.setSelection(0);
				 titleView.setText(selectedCity	.getCityName());
				 currnetLevel=LEVEL_COUNTRY;
			}else{
				queryFromServer(selectedCity.getCityCode(),"country");
			}
		}
		
		
		private void queryFromServer(final String code,final String type){
			String address;
			if(!TextUtils.isEmpty(code)){
				address="http://www.weather.com.cn/data/list3/city"+code+".xml";
			}else{
				address="http://www.weather.com.cn/data/list3/city.xml";
			}
			showProgressDialog();
			HttpUtil.sendHttpRequest(address,new HttpCallbackListener() {
				
				@Override
				public void onFinish(String response) {
					boolean result=false;
					if("province".equals(type)){
						result=Utility.handleProvincesResponse(coolWeatherDB, response);
					} else if("city".equals(type)){
						result=Utility.handleCitiesResponse(coolWeatherDB, response, selectedProvince.getId());
					}else if("country".equals(type)){
						result=Utility.handleCountiesResponse(coolWeatherDB, response, selectedCity.getId());
					}
					if(result){
						runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								closeProgressDialog();
								if("province".equals(type)){
									queryProvinces();
								}else if ("city".equals(type)) {
									queryCities();
								}else if("country".equals(type)){
									queryCounties();
								}
								
							}
						});
					}
				}
				@Override
				public void onError (Exception e){
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							closeProgressDialog();
							Toast.makeText(ChooseAreaActivity.this, "����ʧ��", Toast.LENGTH_SHORT).show();
						}
					});
				}
				});
			}
			
				
				private void showProgressDialog(){
					if(progressDialog==null){
						
						progressDialog=new ProgressDialog(this);
						progressDialog.setMessage("���ڼ���.....");
						progressDialog.setCanceledOnTouchOutside(false);
					}
					
					progressDialog.show();
				}
				
				
				private void closeProgressDialog(){
					if(progressDialog!=null){
						progressDialog.dismiss();
					}
				
					
				}
				
				public void onBackPressed(){
					if(currnetLevel==LEVEL_COUNTRY){
						queryCities();
					}else if (currnetLevel==LEVEL_CITY) {
						queryProvinces();
					}else{
						finish();
					}
						
					}
					
				}
				
				
				
			
		
		
	
