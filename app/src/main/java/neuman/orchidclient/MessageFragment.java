package neuman.orchidclient;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MessageFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MessageFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class MessageFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_LOCATION = "param1";
    private static final String ARG_INDICATOR = "param2";
    private static final String ARG_SCORE = "param3";
    private static final String ARG_DESTINATION = "param4";

    // TODO: Rename and change types of parameters
    private String incoming_indicator_string;
    private String incoming_location_string;
    private String score;
    private String destination;
    private Button button_right;
    private Button button_left;
    private TextView topmessage_text;
    private TextView middlemessage_text;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param incoming_score_string Parameter 1.
     * @param incoming_destination_string Parameter 2.
     * @return A new instance of fragment MessageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MessageFragment newInstance(String location_json_string, String incoming_indicator_string, String incoming_score_string, String incoming_destination_string) {
        MessageFragment fragment = new MessageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SCORE, incoming_score_string);
        args.putString(ARG_DESTINATION, incoming_destination_string);
        args.putString(ARG_LOCATION, location_json_string);
        args.putString(ARG_INDICATOR, incoming_indicator_string);
        fragment.setArguments(args);
        return fragment;
    }
    public MessageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            incoming_indicator_string = getArguments().getString(ARG_INDICATOR);
            incoming_location_string = getArguments().getString(ARG_LOCATION);
            score = getArguments().getString(ARG_SCORE);
            destination = getArguments().getString(ARG_DESTINATION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_message, container, false);

        topmessage_text = (TextView) rootView.findViewById(R.id.topmessage_text);
        topmessage_text.setText("RECORD SAVED IN "+destination);

        middlemessage_text = (TextView) rootView.findViewById(R.id.middlemessage_text);
        middlemessage_text.setText("SCORE "+score);

        button_left = (Button) rootView.findViewById(R.id.button_left);
        button_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.popBackStack();
            }
        });

        button_right = (Button) rootView.findViewById(R.id.button_right);
        button_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.popBackStack();
                fragmentManager.popBackStack();
                //fragmentManager.beginTransaction().replace(R.id.content_frame, FormFragment.newInstance(incoming_location_string, incoming_indicator_string, "")).commit();
            }
        });

        return rootView;
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
