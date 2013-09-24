package edu.mit.media.funf.probe.external;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import funfinabox.__ID__.R;
import edu.mit.media.funf.probe.external.SurveyInboxActivity.Survey;


public class SurveyListAdapter extends ArrayAdapter<Survey>{
	
	private final Context context;
	private final ArrayList<Survey> survey;

	public SurveyListAdapter(Context context, ArrayList<Survey> survey) {
		super(context, R.layout.survey_row, survey);
		// TODO Auto-generated constructor stub
		this.context = context;
	    this.survey = survey;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
	    LayoutInflater inflater = (LayoutInflater) context
	        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    
	    View rowView = inflater.inflate(R.layout.survey_row, parent, false);
	    
	    TextView NameView = (TextView) rowView.findViewById(R.id.Name);
	    TextView DescView = (TextView) rowView.findViewById(R.id.Description);
	    TextView UrlView = (TextView) rowView.findViewById(R.id.Url);
	    
	    NameView.setText(survey.get(position).getName());
	    DescView.setText(survey.get(position).getDescription());
	    UrlView.setText(survey.get(position).getUrl());
	    
	    return rowView;
	}

}