package neuman.orchidclient;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import neuman.orchidclient.content.ContentQueryMaker;
import neuman.orchidclient.content.ObjectTypes;
import neuman.orchidclient.models.Location;
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


    private ListView listView;
    private ArrayList<Record> items = new ArrayList<Record>();
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_DEST = "param1";

    // TODO: Rename and change types of parameters
    private Boolean drafts;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment OutboxFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static OutboxFragment newInstance(String param1) {
        OutboxFragment fragment = new OutboxFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DEST, param1);
        fragment.setArguments(args);
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
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View inflatedView = inflater.inflate(R.layout.fragment_outbox, container, false);

        contentQueryMaker = new ContentQueryMaker(getActivity().getContentResolver());
        //we only care about scores in the non draft outbox
        if(drafts != true) {
            //get scores
            Cursor mCursor = contentQueryMaker.get_all_of_object_type_cursor(ObjectTypes.TYPE_SCORE);
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
                        items.add(newItem);
                    } catch (JSONException e) {
                        Log.d(TAG, e.toString());
                        e.printStackTrace();
                    }
                }
            }
        }

        Cursor mCursor = contentQueryMaker.get_all_of_object_type_cursor(ObjectTypes.TYPE_RECORD);
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
                    if(record_json.getBoolean("draft")==drafts){
                        record_json.put("row_id", mCursor.getInt(0));
                        Record newItem = new Record(record_json);
                        items.add(newItem);}
                }catch(JSONException e){
                    Log.d(TAG, e.toString());
                    e.printStackTrace();
                }
            }
        }
        button_sync = (Button) inflatedView.findViewById(R.id.button_sync);
        if(drafts){
            ((MainActivity)getActivity()).set_action_bar_title("Drafts");
            button_sync.setVisibility(View.GONE);
        }else {
            ((MainActivity)getActivity()).set_action_bar_title("Outbox");
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

        listView = (ListView) inflatedView.findViewById(R.id.listView);

        // Define a new Adapter
        // First parameter - Context
        // Second parameter - Layout for the row
        // Third parameter - ID of the TextView to which the data is written
        // Forth - the Array of data



        JSONArrayAdapter adapter = new JSONArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, items);

        // Assign adapter to ListView
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position,
                                    long arg3) {

                try{
                    Record item = (Record) adapter.getItemAtPosition(position);
                    item.setContentQueryMaker(contentQueryMaker);
                    Log.d(TAG, "Clicked " + item.getJSON().get("title").toString());
                    Log.d(TAG, "Clicked JSON" + item.getJSON().toString());
                    FragmentManager fragmentManager = getFragmentManager();
                    Location location = item.getLocation();
                    String location_json_string = location.getJSON().toString();
                    String indicator_json_string = item.getIndicator().getJSON().toString();

                    fragmentManager.beginTransaction().replace(R.id.content_frame, FormFragment.newInstance(location_json_string,indicator_json_string, item.getJSON().toString())).addToBackStack(null).commit();

                }catch(JSONException e){
                    Log.d(TAG, e.toString());
                }
            }
        });

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
