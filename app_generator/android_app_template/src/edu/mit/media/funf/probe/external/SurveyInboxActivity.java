/**
 * 
 * Funf: Open Sensing Framework
 * Copyright (C) 2010-2011 Nadav Aharony, Wei Pan, Alex Pentland.
 * Acknowledgments: Alan Gardner
 * Contact: nadav@media.mit.edu
 * 
 * Author(s): Swetank Kumar Saha (swetank.saha@gmail.com)
 * 
 * This file is part of Funf.
 * 
 * Funf is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * Funf is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with Funf. If not, see <http://www.gnu.org/licenses/>.
 *    
 */
package edu.mit.media.funf.probe.external;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.gson.JsonObject;

import edu.mit.media.funf.FunfManager;
import edu.mit.media.funf.json.IJsonObject;
import edu.mit.media.funf.json.JsonUtils;
import edu.mit.media.funf.pipeline.BasicPipeline;
import edu.mit.media.funf.pipeline.Pipeline;
import edu.mit.media.funf.time.TimeUtil;
import edu.mit.media.funf.util.UuidUtil;
import funfinabox.__ID__.R;

public class SurveyInboxActivity extends Activity{
	
	public static final String PIPELINE_NAME = "__NAME__";
	public static final String UUID_KEYWORD = "UUID";
	
	private FunfManager funfMgr = null;
	private Pipeline pipeline = null;
	private BasicPipeline basicPipeline = null;
	private ServiceConnection funfMgrConn = new ServiceConnection() {
	      
	    @Override
	    public void onServiceConnected(ComponentName name, IBinder service) {
	      funfMgr = ((FunfManager.LocalBinder)service).getManager();
	      pipeline = funfMgr.getRegisteredPipeline(PIPELINE_NAME);
	      
	      basicPipeline = (BasicPipeline) pipeline;
	    }

		@Override
		public void onServiceDisconnected(ComponentName name) {
			funfMgr = null;
		}
	};
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindService(new Intent(this, FunfManager.class), funfMgrConn, BIND_AUTO_CREATE);
        
        final Intent intent = getIntent();
        
		final JsonObject config = new JsonObject();
	    config.addProperty("@type", "edu.mit.media.funf.probe.external.SurveyProbe");
	    JsonObject data = new JsonObject();
	    data.addProperty("timestamp", TimeUtil.getTimestamp());
	    data.addProperty("name", intent.getStringExtra("name"));
	    data.addProperty("description", intent.getStringExtra("description"));
	    data.addProperty("url", intent.getStringExtra("url"));
		data.addProperty("type", "notification_open");
		  
	    basicPipeline.onDataReceived((IJsonObject)JsonUtils.immutable(config), (IJsonObject)JsonUtils.immutable(data));
        
        setContentView(R.layout.survey_list);
        
        ListView SurveyListView = (ListView) findViewById(R.id.survey_list);
        final ArrayList<Survey> SurveyList = new ArrayList<Survey>();
        
        SurveyDatabaseHelper SurveyDB = new SurveyDatabaseHelper(getApplicationContext(), 
																SurveyDatabaseHelper.DATABASE_NAME, 
																SurveyDatabaseHelper.CURRENT_VERSION);
        
        ArrayList<ArrayList<String>> Data = SurveyDB.GetAllSurveys();
        for(ArrayList<String> survey: Data){
        	if(isActive(survey.get(3))){
        		SurveyList.add(new Survey(survey.get(0),survey.get(1),survey.get(2),survey.get(3)));
        	}
        }
        
        SurveyListAdapter Surveys = new SurveyListAdapter(this, SurveyList);
        SurveyListView.setAdapter(Surveys);
        
		SurveyListView.setTextFilterEnabled(true);
 
		SurveyListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				JsonObject data = new JsonObject();
			    data.addProperty("timestamp", TimeUtil.getTimestamp());
			    data.addProperty("name", intent.getStringExtra("name"));
			    data.addProperty("description", intent.getStringExtra("description"));
			    data.addProperty("url", intent.getStringExtra("url"));
				data.addProperty("type", "survey_open");
				
			    basicPipeline.onDataReceived((IJsonObject)JsonUtils.immutable(config), (IJsonObject)JsonUtils.immutable(data));
			    
			    String URL = SurveyList.get(position).getUrl();
			    InflateSurveyWebView(URL.replace(UUID_KEYWORD, 
			    		UuidUtil.getInstallationId(getApplicationContext())));
			}
		});
	}
	
	private void InflateSurveyWebView(String URL){
		
		LinearLayout view = new LinearLayout(getApplicationContext());
		view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		view.setOrientation(LinearLayout.VERTICAL);

		WebView web = new WebView(this);
		//Button b = new Button(this);
		web.setLayoutParams(new LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		web.getSettings().setJavaScriptEnabled(true);
		web.setWebViewClient(new WebViewClient());
		
		web.loadUrl(URL);
		
		view.addView(web);
		setContentView(view);
	}
	
	private boolean isActive(String EndTimestamp){
		boolean active = true;
		
		String DT_FORMAT = "dd-MM-yyyy HH:mm:ss";
		
		SimpleDateFormat DateFormat = new SimpleDateFormat(DT_FORMAT);
		DateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		try {
			Date END = DateFormat.parse(EndTimestamp);
			Date CURRENT = (Date) DateFormat.parse(DateFormat.format(new Date()));
			
			if( END.after(CURRENT) ){
				active = false;
			}
			
		} catch (ParseException e) {return active;}
		
		return active;
	}
	
	public class Survey{
		
		private String Name;
		private String Description;
		private String Url;
		private String Endtimestamp;
		
		public Survey(String Name, String Description, String Url, String Endtimestamp){
			this.Name = Name;
			this.Description = Description;
			this.Url = Url;
			this.Endtimestamp = Endtimestamp;
		}
		
		public String getName(){
			return this.Name;
		}
		
		public String getDescription(){
			return this.Description;
		}
		
		public String getUrl(){
			return this.Url;
		}
		
		public String getEndtimestamp(){
			return this.Endtimestamp;
		}
	}
	
}