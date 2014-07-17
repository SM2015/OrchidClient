package neuman.orchidclient;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import neuman.orchidclient.content.Contract;
import neuman.orchidclient.content.ObjectTypes;
import neuman.orchidclient.util.Item;
import neuman.orchidclient.util.JSONArrayAdapter;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LocationPickFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LocationPickFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class LocationPickFragment extends Fragment {
    private String TAG = getClass().getSimpleName();
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private ListView listView;
    private ArrayList<Item> list_text = new ArrayList<Item>();

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LocationPickFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LocationPickFragment newInstance(String param1, String param2) {
        LocationPickFragment fragment = new LocationPickFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public LocationPickFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View inflatedView = inflater.inflate(R.layout.fragment_location_pick, container, false);

        listView = (ListView) inflatedView.findViewById(R.id.listView);

        // Define a new Adapter
        // First parameter - Context
        // Second parameter - Layout for the row
        // Third parameter - ID of the TextView to which the data is written
        // Forth - the Array of data

        JSONArrayAdapter adapter = new JSONArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, list_text);

        // Assign adapter to ListView
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position,
                                    long arg3) {

                try{
                    Item item = (Item) adapter.getItemAtPosition(position);
                    Log.d(TAG, "Clicked " + item.getJSON().get("title").toString());
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.content_frame,LocationDetailFragment.newInstance(item.getJSON().toString())).addToBackStack(null).commit();

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
        String TAG = "sync";
        Log.d("location pick", "Made it");

        // A "projection" defines the columns that will be returned for each row
        String[] mProjection =
                {
                        Contract.Entry._ID,    // Contract class constant for the _ID column name
                        Contract.Entry.COLUMN_NAME_OBJECTTYPE,
                        Contract.Entry.COLUMN_NAME_JSON,   // Contract class constant for the word column name
                };

        // Defines a string to contain the selection clause
        String mSelectionClause =  Contract.Entry.COLUMN_NAME_OBJECTTYPE+" = "+ ObjectTypes.TYPE_LOCATION;

        // Initializes an array to contain selection arguments
        String[] mSelectionArgs = {""};

        // Remember to insert code here to check for invalid or malicious input.


        ContentResolver contentResolver = getActivity().getContentResolver();
        // Does a query against the table and returns a Cursor object

        Log.d(TAG, Contract.Entry.CONTENT_URI.toString());
        Log.d(TAG,"mProjection: "+mProjection.toString());
        Cursor mCursor = getActivity().getContentResolver().query(
                Contract.Entry.CONTENT_URI,  // The content URI of the words table
                mProjection,                       // The columns to return for each row
                mSelectionClause,                   // Either null, or the word the user entered
                null,                    // Either empty, or the string the user entered
                Contract.Entry.COLUMN_NAME_OBJECTTYPE);                       // The sort order for the returned rows

        // Some providers return null if an error occurs, others throw an exception
        if (null == mCursor) {
    /*
     * Insert code here to handle the error. Be sure not to use the cursor! You may want to
     * call android.util.Log.e() to log this error.
     *
     */
        // If the Cursor is empty, the provider found no matches
            Log.d(TAG,"Cursor Error");
        } else if (mCursor.getCount() < 1) {

    /*
     * Insert code here to notify the user that the search was unsuccessful. This isn't necessarily
     * an error. You may want to offer the user the option to insert a new row, or re-type the
     * search term.
     */
            Log.d(TAG,"No results");

        } else {
            // Insert code here to do something with the results
            while (mCursor.moveToNext()) {
                Log.d(TAG,"*****CURSOR MOVED*****");
                Log.d(TAG, mCursor.getColumnName(0)+": "+mCursor.getString(0));
                Log.d(TAG, mCursor.getColumnName(1)+": "+mCursor.getString(1));
                String jsonString = mCursor.getString(2);
                Log.d(TAG, mCursor.getColumnName(2)+": "+jsonString);
                try{
                    JSONObject location_data = new JSONObject(jsonString);
                    Item newItem = new Item(location_data.get("title").toString(),location_data);
                    list_text.add(newItem);
                }catch(JSONException e){
                    Log.d(TAG, e.toString());
                    e.printStackTrace();
                }
            }
        }

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
