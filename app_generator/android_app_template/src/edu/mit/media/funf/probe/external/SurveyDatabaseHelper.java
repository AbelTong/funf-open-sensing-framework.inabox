package edu.mit.media.funf.probe.external;

import java.util.ArrayList;
import java.util.Arrays;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import edu.mit.media.funf.storage.NameValueDatabaseHelper.Column;
import edu.mit.media.funf.storage.NameValueDatabaseHelper.Table;

public class SurveyDatabaseHelper extends SQLiteOpenHelper{
	
	public static final int CURRENT_VERSION = 1;
	public static final String DATABASE_NAME = "surveydb";
	
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_DESCRIPTION = "description";
	public static final String COLUMN_URL = "url";
	public static final String COLUMN_ENDTIMESTAMP = "endtimestamp";
	public static final Table SURVEY_TABLE = new Table("survey", 
			Arrays.asList(new Column(COLUMN_NAME, "TEXT"),
					      new Column(COLUMN_DESCRIPTION, "TEXT"),
					      new Column(COLUMN_URL, "TEXT"),
					      new Column(COLUMN_ENDTIMESTAMP, "TEXT")));
	
	private static final String SELECT_SQL = "SELECT NAME, DESCRIPTION FROM "+SURVEY_TABLE;
	private static final String SELECT_WHERE_SQL = "SELECT NAME, DESCRIPTION FROM "+SURVEY_TABLE+" WHERE"+
												   COLUMN_NAME+" = ? AND "+
												   COLUMN_DESCRIPTION+" = ? AND "+
												   COLUMN_URL+" = ? AND"+
												   COLUMN_ENDTIMESTAMP+" = ?";
	
	public SurveyDatabaseHelper(Context context, String name, int version) {
		super(context, name, null, version);
	}

	@Override
	public void onCreate(SQLiteDatabase DBHandle) {
		DBHandle.execSQL(SURVEY_TABLE.getCreateTableSQL());
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}
	
	public ArrayList<ArrayList<String>> GetAllSurveys(){
		SQLiteDatabase DBHandle = this.getReadableDatabase();
		
		ArrayList<ArrayList<String>> Result = new ArrayList<ArrayList<String>>();
		ArrayList<String> Data = new ArrayList<String>();
		
		Cursor iterator = DBHandle.rawQuery(SELECT_SQL,new String [] {});
		iterator.moveToFirst();
		
		while(!iterator.isLast()){
			Data.add(iterator.getString(iterator.getColumnIndex(COLUMN_NAME)));
			Data.add(iterator.getString(iterator.getColumnIndex(COLUMN_DESCRIPTION)));
			Data.add(iterator.getString(iterator.getColumnIndex(COLUMN_URL)));
			Data.add(iterator.getString(iterator.getColumnIndex(COLUMN_ENDTIMESTAMP)));
			Result.add(Data);
		}
		
		return Result;
	}
	
	public ArrayList<String> GetSurvey(String name, String description, String url, String endtimestamp){
		SQLiteDatabase DBHandle = this.getReadableDatabase();
		
		ArrayList<String> Result = null;
		
		Cursor iterator = DBHandle.rawQuery(SELECT_WHERE_SQL, new String[] {name,description,url,endtimestamp});
		
		
		if( iterator != null ){
			Result = new ArrayList<String>();
			iterator.moveToFirst();
			Result.add(iterator.getString(iterator.getColumnIndex(COLUMN_NAME)));
			Result.add(iterator.getString(iterator.getColumnIndex(COLUMN_DESCRIPTION)));
		}
		
		return Result;
	}

	public void InsertSurvey(String name, String description, String url, String endtimestamp) {
		SQLiteDatabase DBHandle = this.getWritableDatabase();
		
		ContentValues survey = new ContentValues();
	    survey.put(COLUMN_NAME, name);
	    survey.put(COLUMN_DESCRIPTION, description);
	    survey.put(COLUMN_URL, url);
	    survey.put(COLUMN_ENDTIMESTAMP, endtimestamp);
	    
	    DBHandle.insert(SURVEY_TABLE.name, null, survey);
	}
}