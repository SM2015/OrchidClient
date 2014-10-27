package neuman.orchidclient;

import android.app.Activity;
import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONException;
import org.json.JSONObject;

import neuman.orchidclient.content.ContentQueryMaker;


/**
 * A simple {@link android.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link neuman.orchidclient.LocationVisualizeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link neuman.orchidclient.LocationVisualizeFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class LocationVisualizeFragment extends Fragment {
    private String TAG = getClass().getSimpleName();
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "location_json_string";
    private ContentQueryMaker contentQueryMaker;
    private WebView view_chart;
    private View view_main;

    // TODO: Rename and change types of parameters
    private String location_json_string;
    private JSONObject location_json;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param location_json_string Location object's json.
     * @return A new instance of fragment LocationDetailFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LocationVisualizeFragment newInstance(String location_json_string) {
        LocationVisualizeFragment fragment = new LocationVisualizeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, location_json_string);
        fragment.setArguments(args);
        return fragment;
    }
    public LocationVisualizeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            location_json_string = getArguments().getString(ARG_PARAM1);
            try{
                location_json = new JSONObject(location_json_string);
                ((MainActivity)getActivity()).set_action_bar_title(location_json.get("title").toString()+": Select Indicator");
            }catch(JSONException e){
                Log.d(TAG, e.toString());
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View inflatedView = inflater.inflate(R.layout.fragment_location_visualize, container, false);
        view_main = (View) inflatedView.findViewById(R.id.mainView);
        view_chart = (WebView) inflatedView.findViewById(R.id.chartView);

        String html = "<html><body>Hello, World!</body></html>";
        String mime = "text/html";
        String encoding = "utf-8";


        view_chart.getSettings().setJavaScriptEnabled(true);
        view_chart.getSettings().setAllowUniversalAccessFromFileURLs(true);
        view_chart.getSettings().setAllowFileAccessFromFileURLs(true);
        view_chart.loadDataWithBaseURL(null, html, mime, encoding, null);
        //view_chart.loadUrl("file:///android_asset/charts.html");
        //view_chart.loadDataWithBaseURL( "file:///android_asset/",, "text/html","utf-8", null );
        view_chart.getSettings().setJavaScriptEnabled(true);
        view_chart.loadUrl("file:///android_asset/charts.html");
        view_chart.setWebViewClient(new WebViewClient());

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
