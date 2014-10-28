package neuman.orchidclient;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import neuman.orchidclient.content.ContentQueryMaker;
import neuman.orchidclient.content.ObjectTypes;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GetLocationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GetLocationFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class GetLocationFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    private String TAG = this.getClass().getSimpleName();
    private ContentQueryMaker contentQueryMaker;
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private View mProgressView;
    private View mButtonView;
    private TextView mQuestionView;
    private Button button_yes;
    private Button button_no;
    private Location current_gps_location;

    neuman.orchidclient.models.Location closest_orchid_location;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GetLocationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GetLocationFragment newInstance(String param1, String param2) {
        GetLocationFragment fragment = new GetLocationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public GetLocationFragment() {
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
        ((MainActivity)getActivity()).set_action_bar_title("Getting GPS Location");
        contentQueryMaker = new ContentQueryMaker(getActivity().getContentResolver());
        // Inflate the layout for this fragment
        View inflatedView = inflater.inflate(R.layout.fragment_get_location, container, false);
        mButtonView = (LinearLayout) inflatedView.findViewById(R.id.button_layout);
        mQuestionView = (TextView) inflatedView.findViewById(R.id.question_view);
        mProgressView = (ProgressBar) inflatedView.findViewById(R.id.location_progress);

        button_yes = (Button) inflatedView.findViewById(R.id.button_yes);
        button_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if it's the right location go to indicator selection
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.content_frame, LocationDetailFragment.newInstance(closest_orchid_location.getJSON().toString())).addToBackStack(null).commit();
            }
        });

        button_no = (Button) inflatedView.findViewById(R.id.button_no);
        button_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if not go back to the locationpick screen
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.popBackStack();
            }
        });

        // Acquire a reference to the system Location Manager
        final LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        final LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                //showProgress(false);
                Log.d(TAG, "LOCATION FOUND: "+location.toString());
                current_gps_location = location;
                closest_orchid_location = get_nearest_location(location);
                mQuestionView.setText("Are you at "+closest_orchid_location.getTitle()+"?");
                mProgressView.setVisibility(View.INVISIBLE);
                mButtonView.setVisibility(View.VISIBLE);

                locationManager.removeUpdates(this);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d(TAG, "onStatusChanged: "+status);
            }

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}

        };

// Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);



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

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mButtonView.setVisibility(show ? View.GONE : View.VISIBLE);
            mButtonView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mButtonView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mButtonView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


    private neuman.orchidclient.models.Location get_nearest_location(Location gps_location){
        Cursor mCursor = contentQueryMaker.get_all_of_object_type_cursor(ObjectTypes.TYPE_LOCATION);
        neuman.orchidclient.models.Location closest_orchid_location=null;

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
                    neuman.orchidclient.models.Location orchid_location = new neuman.orchidclient.models.Location(location_data);
                    if((closest_orchid_location == null) || (closest_orchid_location.get_distance_to(current_gps_location)> orchid_location.get_distance_to(current_gps_location))){
                        closest_orchid_location = orchid_location;
                    }
                }catch(JSONException e){
                    Log.d(TAG, e.toString());
                    e.printStackTrace();
                }
            }
        }
        return closest_orchid_location;
    }

    /**
     * Created by neuman on 10/27/14.
     */

}
