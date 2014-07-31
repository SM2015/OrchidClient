package neuman.orchidclient;

import android.app.Activity;
import android.app.Fragment;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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
    private Button button_sync;
    ArrayList<Indicator> indicators;
    private ListView listView;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_LOCATION = "location_json_string";

    // TODO: Rename and change types of parameters
    private Boolean drafts;

    private String location_json_string;
    private JSONObject location_json;
    private ArrayList<Integer> indicator_ids;

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
                ((MainActivity)getActivity()).set_action_bar_title(location_json.get("title").toString()+": Select Indicator");
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
        Cursor indicatorCursor = contentQueryMaker.get_all_of_object_type(ObjectTypes.TYPE_INDICATOR);
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
        //get first n record for the indicator in order of date input
        Cursor recordCursor = contentQueryMaker.get_all_of_object_type(ObjectTypes.TYPE_RECORD);
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
                    //ignore any records not from this location
                    if((record_json.getBoolean("draft")==false) && (record_json.getInt("location_id")==location_json.getInt("id"))){
                        record_json.put("row_id", recordCursor.getInt(0));
                        Record record = new Record(record_json);
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

        button_sync = (Button) inflatedView.findViewById(R.id.button_sync);
        ((MainActivity)getActivity()).set_action_bar_title("Score");

        listView = (ListView) inflatedView.findViewById(R.id.listView);

        Log.d(TAG, "Indicators arraylist: "+indicators.toString());
        ArrayList<Indicator> items = new ArrayList<Indicator>();
        for(Indicator i : indicators){
            //Float percentage = i.getPercentage();
            //Indicator temp_item = new Indicator(percentage.toString()+" | "+i.getTitle());
            if(i.getPercentage()>i.getPassing_percentage()){
                i.color =ObjectTypes.COLOR_GREEN;
            }else{
                i.color =ObjectTypes.COLOR_RED;
            }
            //items.add(temp_item);
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


}
