package neuman.orchidclient;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import neuman.orchidclient.content.ContentQueryMaker;
import neuman.orchidclient.content.ObjectTypes;
import neuman.orchidclient.models.Location;
import neuman.orchidclient.models.Photo;
import neuman.orchidclient.models.Record;
import neuman.orchidclient.util.JSONArrayAdapter;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ScoringFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ScoringFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class OutboxFragment extends Fragment {
    private String TAG = getClass().getSimpleName();
    private ContentQueryMaker contentQueryMaker;
    private Button button_sync;
    private Button button_score;


    private ListView recordListView;
    private ListView photoListView;
    private ArrayList<Record> recordItems = new ArrayList<Record>();
    private ArrayList<Photo> photoItems = new ArrayList<Photo>();
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_DEST = "param1";
    private static final String ARG_LOCATION = "location_json_string";

    // TODO: Rename and change types of parameters
    private Boolean drafts;
    private String location_json_string;
    private Location location;

    private Time selected_time = new Time();

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment OutboxFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static OutboxFragment newInstance(String param1, String location_json_string) {
        OutboxFragment fragment = new OutboxFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DEST, param1);
        fragment.setArguments(args);
        if(location_json_string!=null) {
            args.putString(ARG_LOCATION, location_json_string);
            fragment.setArguments(args);
        }
        return fragment;
    }
    public OutboxFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            if(getArguments().getString(ARG_DEST).equals("DRAFTS")){
                drafts = true;
            }
            else{
                drafts = false;
            }
            if (getArguments().getString(ARG_LOCATION) != null) {
                try {

                    JSONObject location_json = new JSONObject(getArguments().getString(ARG_LOCATION));
                    location = new Location(location_json);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            }
        }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //wipe the item list so we don't get doubles
        recordItems = new ArrayList<Record>();
        photoItems = new ArrayList<Photo>();
        //set time to now
        selected_time.setToNow();
        // Inflate the layout for this fragment
        View inflatedView = inflater.inflate(R.layout.fragment_outbox, container, false);
        contentQueryMaker = new ContentQueryMaker(getActivity().getContentResolver());
        //we only care about scores in the non draft outbox
        if(drafts != true) {
            //get scores
            Cursor mCursor = contentQueryMaker.get_all_of_model_type_cursor(ObjectTypes.TYPE_SCORE);
            // Some providers return null if an error occurs, others throw an exception
            if (null == mCursor) {
                // If the Cursor is empty, the provider found no matches
                Log.d(TAG, "Cursor Error");
            } else if (mCursor.getCount() < 1) {
                Log.d(TAG, "No results");

            } else {
                // Insert code here to do something with the results
                while (mCursor.moveToNext()) {
                    String jsonString = mCursor.getString(2);
                    try {
                        JSONObject score_json = new JSONObject(jsonString);
                        score_json.put("row_id", mCursor.getInt(0));
                        Record newItem = new Record(score_json);
                        recordItems.add(newItem);
                    } catch (JSONException e) {
                        Log.d(TAG, e.toString());
                        e.printStackTrace();
                    }
                }
            }
        }

        //GET AND SET ALL RECORDS TO LIST
        get_and_set_records();
        get_and_set_photos();

        //GET AND SET ALL PHOTOS TO LIST

        button_sync = (Button) inflatedView.findViewById(R.id.button_sync);
        button_score = (Button) inflatedView.findViewById(R.id.button_score);
        if(drafts){
            ((MainActivity)getActivity()).set_action_bar_title("Drafts: "+location.getTitle());
            button_sync.setVisibility(View.GONE);
            button_score.setVisibility(View.VISIBLE);
            button_score.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //view_main.setVisibility(View.INVISIBLE);
                    //view_datepicker.setVisibility(View.VISIBLE);
                    createDialogWithoutDateField().show();

                }
            });
        }else {
            ((MainActivity)getActivity()).set_action_bar_title("All Locations Outbox");
            button_score.setVisibility(View.GONE);
            button_sync.setVisibility(View.VISIBLE);
            button_sync = (Button) inflatedView.findViewById(R.id.button_sync);
            button_sync.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((MainActivity) getActivity()).check_network_and_sync();
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.popBackStack();
                }
            });
        }

        recordListView = (ListView) inflatedView.findViewById(R.id.recordListView);
        photoListView = (ListView) inflatedView.findViewById(R.id.photoListView);

        // Define a new Adapter
        // First parameter - Context
        // Second parameter - Layout for the row
        // Third parameter - ID of the TextView to which the data is written
        // Forth - the Array of data



        JSONArrayAdapter recordAdapter = new JSONArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, recordItems);
        JSONArrayAdapter photoAdapter = new JSONArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, photoItems);

        // Assign adapter to ListView
        recordListView.setAdapter(recordAdapter);
        //only turn on the click function if we are looking at drafts
        if(drafts==true) {
            recordListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapter, View v, int position,
                                        long arg3) {

                    try {
                        Record item = (Record) adapter.getItemAtPosition(position);
                        item.setContentQueryMaker(contentQueryMaker);
                        Log.d(TAG, "Clicked " + item.getJSON().get("title").toString());
                        Log.d(TAG, "Clicked JSON" + item.getJSON().toString());
                        FragmentManager fragmentManager = getFragmentManager();
                        Location location = item.getLocation();
                        String location_json_string = location.getJSON().toString();
                        String indicator_json_string = item.getIndicator().getJSON().toString();

                        fragmentManager.beginTransaction().replace(R.id.content_frame, FormFragment.newInstance(location_json_string, indicator_json_string, item.getJSON().toString())).addToBackStack(null).commit();

                    } catch (JSONException e) {
                        Log.d(TAG, e.toString());
                    }
                }
            });
        }

        // Assign adapter to ListView
        photoListView.setAdapter(photoAdapter);

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

    private void get_and_set_records(){

        //GET AND SET ALL RECORDS TO LIST
        Cursor mCursor = contentQueryMaker.get_all_of_model_type_cursor(ObjectTypes.TYPE_RECORD);
        // Some providers return null if an error occurs, others throw an exception
        if (null == mCursor) {
            // If the Cursor is empty, the provider found no matches
            Log.d(TAG, "Cursor Error");
        } else if (mCursor.getCount() < 1) {
            Log.d(TAG,"No results");

        } else {
            // Insert code here to do something with the results
            while (mCursor.moveToNext()) {
                String jsonString = mCursor.getString(2);
                try{
                    JSONObject record_json = new JSONObject(jsonString);
                    //only display records that are drafts from this location
                    if((location!=null)){
                        if((record_json.getInt("location_id")==location.getId())&&(record_json.getBoolean("draft")==true)){
                            record_json.put("row_id", mCursor.getInt(0));
                            Record newItem = new Record(record_json);
                            recordItems.add(newItem);
                        }
                    }else if(record_json.getBoolean("draft")==false){
                        record_json.put("row_id", mCursor.getInt(0));
                        Record newItem = new Record(record_json);
                        recordItems.add(newItem);
                    }
                }catch(JSONException e){
                    Log.d(TAG, e.toString());
                    e.printStackTrace();
                }
            }
        }
    }

    private void get_and_set_photos(){

        //GET AND SET ALL RECORDS TO LIST
        Cursor mCursor = contentQueryMaker.get_all_of_model_type_cursor(ObjectTypes.TYPE_PHOTO);
        // Some providers return null if an error occurs, others throw an exception
        if (null == mCursor) {
            // If the Cursor is empty, the provider found no matches
            Log.d(TAG, "Cursor Error");
        } else if (mCursor.getCount() < 1) {
            Log.d(TAG,"No results");

        } else {
            // Insert code here to do something with the results
            while (mCursor.moveToNext()) {
                String jsonString = mCursor.getString(2);
                try{
                    JSONObject photo_json = new JSONObject(jsonString);
                    //only display records that are drafts from this location
                    if((location!=null)){
                        if(photo_json.getInt("location_id")==location.getId()){
                            photo_json.put("row_id", mCursor.getInt(0));
                            Photo newItem = new Photo(photo_json);
                            photoItems.add(newItem);
                        }
                    }
                    else{
                        photo_json.put("row_id", mCursor.getInt(0));
                        Photo newItem = new Photo(photo_json);
                        photoItems.add(newItem);
                    }
                }catch(JSONException e){
                    Log.d(TAG, e.toString());
                    e.printStackTrace();
                }
            }
        }
    }

    private DatePickerDialog createDialogWithoutDateField(){

        DatePickerDialog dpd = new DatePickerDialog(getActivity(), myDateSelectedListener,selected_time.year,selected_time.month, 1)
        {
            @Override
            protected void onCreate(Bundle savedInstanceState)
            {
                super.onCreate(savedInstanceState);
                int year = getContext().getResources()
                        .getIdentifier("android:id/day", null, null);
                if(year != 0){
                    View yearPicker = findViewById(year);
                    if(yearPicker != null){
                        yearPicker.setVisibility(View.GONE);
                    }
                }
            }
        };
        dpd.setTitle("Select Year/Month You Want To Score");
        dpd.getDatePicker().setCalendarViewShown(false);
        return dpd;

    }

    private DatePickerDialog.OnDateSetListener myDateSelectedListener
            = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
            // TODO Auto-generated method stub
            // arg1 = year
            // arg2 = month
            // arg3 = day
            selected_time.year = arg1;
            selected_time.month = arg2;
            location.put("year", selected_time.year);
            location.put("month", selected_time.month+1);
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, ScoringFragment.newInstance(location.getJSON().toString())).addToBackStack(null).commit();

        }
    };


}
