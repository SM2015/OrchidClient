package neuman.orchidclient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import neuman.orchidclient.content.ContentQueryMaker;
import neuman.orchidclient.content.ObjectTypes;
import neuman.orchidclient.models.Indicator;
import neuman.orchidclient.models.Record;
import neuman.orchidclient.util.ScoreArrayAdapter;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ScoringFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ScoringFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class ScoringFragment extends Fragment {
    private String TAG = getClass().getSimpleName();
    private ContentQueryMaker contentQueryMaker;
    private Button button_submit;
    ArrayList<Indicator> indicators;
    Integer total_passing_indicators = 0;
    ArrayList<Record> scored_records;
    private ListView listView;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_LOCATION = "location_json_string";

    // TODO: Rename and change types of parameters
    private Boolean drafts;

    private String location_json_string;
    private JSONObject location_json;
    private ArrayList<Integer> indicator_ids;
    private JSONObject outgoing_score;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param location_json_string Parameter
     * @return A new instance of fragment OutboxFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ScoringFragment newInstance(String location_json_string) {
        ScoringFragment fragment = new ScoringFragment();
        Bundle args = new Bundle();
        args.putString(ARG_LOCATION, location_json_string);
        fragment.setArguments(args);
        return fragment;
    }
    public ScoringFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            location_json_string = getArguments().getString(ARG_LOCATION);
            try{
                location_json = new JSONObject(location_json_string);
                ((MainActivity)getActivity()).set_action_bar_title("Score: "+location_json.getString("title"));
                JSONArray indicator_ids_json;
                indicator_ids = new ArrayList<Integer>();
                indicator_ids_json = location_json.getJSONArray("indicator_ids");
                // ignore the case of a non-array value.
                if (indicator_ids_json != null) {

                    // Extract numbers from JSON array.
                    for (int i = 0; i < indicator_ids_json.length(); ++i) {

                        indicator_ids.add(indicator_ids_json.optInt(i));
                    }
                }
            }catch(JSONException e){
                Log.d(TAG, e.toString());
            }
        }
    }

    private Indicator get_indicator(ArrayList<Indicator> indicators, Integer indicator_id){
        for (Indicator i : indicators) {
            if (i.getId().equals(indicator_id)){
                return i;
            }
        }
        return null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View inflatedView = inflater.inflate(R.layout.fragment_scoring, container, false);

        contentQueryMaker = new ContentQueryMaker(getActivity().getContentResolver());
        //get each indicator

        ArrayList<Integer> checkbox_field_ids = new ArrayList<Integer>();
        indicators = new ArrayList<Indicator>();
        Cursor indicatorCursor = contentQueryMaker.get_all_of_model_type_cursor(ObjectTypes.TYPE_INDICATOR);
        // Some providers return null if an error occurs, others throw an exception
        if (null == indicatorCursor) {
            // If the Cursor is empty, the provider found no matches
            Log.d(TAG, "Cursor Error");
        } else if (indicatorCursor.getCount() < 1) {

            Log.d(TAG,"No Indicator results");

        } else {
            // Insert code here to do something with the results
            while (indicatorCursor.moveToNext()) {
                String jsonString = indicatorCursor.getString(2);
                try{
                    JSONObject indicator_json = new JSONObject(jsonString);
                    Indicator indicator = new Indicator(indicator_json);
                    //add the indicator if it exists in the given location's indicator list
                    if(indicator_ids.contains(indicator.getId())) {
                        indicators.add(indicator);
                    }
                    //ad the indicators checkbox field ids to the array list for later reference
                    checkbox_field_ids.addAll(indicator.get_boolean_field_ids());

                }catch(JSONException e){
                    Log.d(TAG, e.toString());
                    e.printStackTrace();
                }
            }
        }
        scored_records = new ArrayList<Record>();
        //get first n record for the indicator in order of date input
        Cursor recordCursor = contentQueryMaker.get_all_of_model_type_cursor(ObjectTypes.TYPE_RECORD);
        // Some providers return null if an error occurs, others throw an exception
        if (null == recordCursor) {
            // If the Cursor is empty, the provider found no matches
            Log.d(TAG, "Cursor Error");
        } else if (recordCursor.getCount() < 1) {

           Log.d(TAG,"No results");

        } else {
            while (recordCursor.moveToNext()) {
                String jsonString = recordCursor.getString(2);
                try{
                    JSONObject record_json = new JSONObject(jsonString);
                    Record record = new Record(record_json);
                    //ignore any records not from this location that are not drafts or not in the selected month
                    if((record.is_scored()!=true)&&(record_json.getBoolean("draft")==true)
                            && (record_json.getInt("location_id")==location_json.getInt("id"))
                            && (record_json.getInt("month")==location_json.getInt("month"))
                            && (record_json.getInt("year")==location_json.getInt("year"))
                            ){
                        record_json.put("row_id", recordCursor.getInt(0));
                        //stick this in the json so if we store it back to the db it will remember having been scored already
                        record.put("scored", true);
                        record.put("draft", false);
                        //add to list for later marking as permanently scored
                        scored_records.add(record);
                        Indicator record_indicator = get_indicator(indicators, record.getIndicatorID());
                        record_indicator.incrementTotal_records();
                        if(record.is_passing(checkbox_field_ids)){
                            //if it passes, add it to the tally for percentages later
                            record_indicator.incrementPassing_records();
                        }else{
                            Log.d(TAG, "record didn't pass");
                        }
                    }
                }catch(JSONException e){
                    Log.d(TAG, e.toString());
                    e.printStackTrace();
                }
            }
        }

        button_submit = (Button) inflatedView.findViewById(R.id.button_submit_score);
        button_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //make sure they are ready to submit
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                mark_rows_as_scored();
                                submit_score();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Are you sure you're ready to submit this score? If you select yes there is no way to undo this action.").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();

            }
        });

        listView = (ListView) inflatedView.findViewById(R.id.listView);

        Log.d(TAG, "Indicators arraylist: "+indicators.toString());
        outgoing_score = new JSONObject();
        JSONArray outgoing_scores = new JSONArray();
        Indicator percent_of_goals_met_indicator = new Indicator("Percent of Goals Met: ");

        for(Indicator i : indicators){
            //we don't want to score indicators with 0 records as 0% so don't score 0 record indicators at all

                Float percentage = i.getPercentage();
                Boolean is_passing = percentage > i.getPassing_percentage();
                percent_of_goals_met_indicator.incrementTotal_records();
                try {
                    Time today = new Time(Time.getCurrentTimezone());
                    JSONObject indicator_score = new JSONObject();
                    indicator_score.put("percentage", percentage);
                    indicator_score.put("location_id", location_json.getString("id"));
                    indicator_score.put("indicator_id", i.getId());
                    indicator_score.put("total_record_count", i.getTotal_records());
                    indicator_score.put("passing_record_count", i.getPassing_records());
                    indicator_score.put("passing", is_passing);
                    //months are 0 indexed in java so add 1
                    indicator_score.put("month", location_json.getInt("month"));
                    indicator_score.put("year", location_json.getInt("year"));
                    outgoing_scores.put(indicator_score);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            if(i.getTotal_records()>0) {
                //Indicator temp_item = new Indicator(percentage.toString()+" | "+i.getTitle());
                if (is_passing) {
                    i.color = ObjectTypes.COLOR_GREEN;
                    percent_of_goals_met_indicator.incrementPassing_records();
                } else {
                    i.color = ObjectTypes.COLOR_RED;
                }
                //items.add(temp_item);
            }else{
                //if indicator has no records make it yellow
                i.color = ObjectTypes.COLOR_YELLOW;
            }
        }
        //store the array of indicators in the outgoing_score json for later saving to the db if submitted
        try {
            indicators.add(percent_of_goals_met_indicator);

            //build the outgoing score object
            outgoing_score.put("scores", outgoing_scores);
            outgoing_score.put("year", location_json.getInt("year"));
            outgoing_score.put("month", location_json.getInt("month"));
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String hostname = settings.getString("example_text", "NO HOSTNAME");
            outgoing_score.put("title", "(SCORE) "+location_json.get("title")+" Timestamp:"+contentQueryMaker.getCurrentTimeStamp());
            outgoing_score.put("outgoing_url", hostname+"/location/"+new Integer(location_json.getInt("id")).toString()+"/score/upload/");
        }catch (JSONException e){
            e.printStackTrace();
        }

        ScoreArrayAdapter adapter = new ScoreArrayAdapter(getActivity(), R.layout.score_list_item, indicators);

        // Assign adapter to ListView
        listView.setAdapter(adapter);
        return inflatedView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    private void mark_rows_as_scored(){
        for(Record r : scored_records) {

            contentQueryMaker.update_row_json(r.getJSON().toString(), r.get_row_id());
        }
    }
    private void submit_score(){
        contentQueryMaker.save_to_provider(outgoing_score.toString(), ObjectTypes.TYPE_SCORE,null);
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, OutboxFragment.newInstance("OUTBOX", null)).addToBackStack(null).commit();
    }






}
