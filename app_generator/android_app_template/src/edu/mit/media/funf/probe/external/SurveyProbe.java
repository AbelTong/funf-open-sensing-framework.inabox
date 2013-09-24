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

import java.util.ArrayList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.google.gson.JsonObject;

import edu.mit.media.funf.Schedule;
import edu.mit.media.funf.config.Configurable;
import edu.mit.media.funf.probe.Probe.RequiredPermissions;
import edu.mit.media.funf.probe.builtin.ImpulseProbe;

@Schedule.DefaultSchedule(interval=3600)
@RequiredPermissions(android.Manifest.permission.VIBRATE)
public class SurveyProbe extends ImpulseProbe{
	
	@Configurable
	private String name = null;
	
	@Configurable
	private String description = null;
	
	@Configurable
	private String url = null;
	
	@Configurable
	private String endtimestamp = null;
	
	@Override
	protected void onStart() {
		super.onStart();
		
		final Context CONTEXT = getContext();
		
		SurveyDatabaseHelper SurveyDB = new SurveyDatabaseHelper(CONTEXT, 
																SurveyDatabaseHelper.DATABASE_NAME, 
																SurveyDatabaseHelper.CURRENT_VERSION);
		
		ArrayList<String> Data = SurveyDB.GetSurvey(this.name, this.description, this.url, this.endtimestamp);
		if( Data == null ){
			SurveyDB.InsertSurvey(this.name, this.description, this.url, this.endtimestamp);
		}
		
		JsonObject data = new JsonObject();
		
		NotificationManager notificationManager = (NotificationManager) CONTEXT.getSystemService(Context.NOTIFICATION_SERVICE);
		
		PendingIntent OpenActivity = null;
		
		if( url != null ){
			Intent popup = new Intent( CONTEXT, SurveyInboxActivity.class );
			popup.putExtra("name", name);
			popup.putExtra("description", description);
			popup.putExtra("url", url);
			popup.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			OpenActivity = PendingIntent.getActivity(CONTEXT, 0, popup, PendingIntent.FLAG_UPDATE_CURRENT);
		}
		
		Notification Notify = new Notification( android.R.drawable.ic_dialog_alert, 
												"Funf",
								  				System.currentTimeMillis());
		
        Notify.setLatestEventInfo(CONTEXT, "Funf Survey Notification", "Touch to open!", OpenActivity);
        
        Notify.flags |= Notification.FLAG_AUTO_CANCEL;
        Notify.defaults |= Notification.DEFAULT_ALL;
        
		notificationManager.notify(0, Notify);
		data.addProperty("SurveyName", name);
		data.addProperty("SurveyDescription", description);
		data.addProperty("SurveyUrl", url);
		data.addProperty("type", "notification_show");
		
		sendData(data);
		
		stop();
	}

}