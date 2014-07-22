package neuman.orchidclient;
//icon from http://raindropmemory.deviantart.com

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import neuman.orchidclient.content.ContentQueryMaker;
import neuman.orchidclient.content.Contract;
import neuman.orchidclient.content.ObjectTypes;
import neuman.orchidclient.models.Record;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FormFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FormFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class FormFragment extends Fragment {
    private String TAG = getClass().getSimpleName();
    private ContentQueryMaker contentQueryMaker;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_LOCATION = "param1";
    private static final String ARG_INDICATOR = "param2";
    private static final String ARG_RECORD = "param3";

    // TODO: Rename and change types of parameters
    private String incoming_indicator_string;
    private JSONObject incoming_indicator;

    private String record_json_string;
    private Record incoming_record;

    private String location_json_string;
    private JSONObject location_json;


    private List fieldList = new ArrayList();

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param location_json_string Parameter 1.
     * @param incoming_indicator_string Parameter 2.
     * @return A new instance of fragment FormFragment.
     */

    public static FormFragment newInstance(String location_json_string, String incoming_indicator_string, String incoming_record) {
        FormFragment fragment = new FormFragment();
        Bundle args = new Bundle();
        args.putString(ARG_LOCATION, location_json_string);
        args.putString(ARG_INDICATOR, incoming_indicator_string);
        args.putString(ARG_RECORD, incoming_record);
        fragment.setArguments(args);
        return fragment;
    }

    public FormFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            incoming_indicator_string = getArguments().getString(ARG_INDICATOR);
            location_json_string = getArguments().getString(ARG_LOCATION);
            record_json_string = getArguments().getString(ARG_RECORD);
            try{
                incoming_indicator = new JSONObject(incoming_indicator_string);
                location_json = new JSONObject(location_json_string);
                if (record_json_string != "") {
                    incoming_record = new Record(new JSONObject(record_json_string));
                }
                else{
                    incoming_record = null;
                }
                getActivity().getActionBar().setTitle(incoming_indicator.get("title").toString());
            }catch(JSONException e){
                Log.d(TAG, e.toString());
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View inflatedView = inflater.inflate(R.layout.fragment_form, container, false);


        LinearLayout layout = (LinearLayout) inflatedView.findViewById(R.id.FieldsLinearLayout);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);

        try{
            JSONObject form_json = incoming_indicator.getJSONObject("form");
            Log.d(TAG, "form_json: "+form_json.toString());
            JSONObject fieldsList = form_json.getJSONObject("fields");
            Log.d(TAG, "fieldsList json: "+fieldsList.toString());


            Iterator<?> keys = fieldsList.keys();

            while( keys.hasNext() ){
                String key = (String)keys.next();
                if( fieldsList.get(key) instanceof JSONObject ){
                    JSONObject field = (JSONObject) fieldsList.get(key);
                    Log.d(TAG, "field: "+fieldsList.get(key).toString());
                    String label = field.getString("label");
                    String input_type = field.getJSONObject("widget").getString("input_type");
                    Log.d(TAG, "input_type: "+input_type);
                    if (input_type.equals("checkbox")){
                        Log.d(TAG, "IS CHECKBOX");
                        Switch new_switch = new Switch(getActivity());
                        new_switch.setText(label);
                        new_switch.setTag(key);
                        if(incoming_record != null){
                            String value = incoming_record.getFieldStringValue(key);
                            if (value.equals("on")){
                                new_switch.setChecked(true);
                            }
                        }
                        layout.addView(getLinearLayout(new_switch, ""),layoutParams);
                        fieldList.add(new_switch);
                    }
                    else if (input_type.equals("text")){
                        Log.d(TAG, "IS TEXT");
                        EditText new_edit = new EditText(getActivity());
                        new_edit.setTag(key);
                        if(incoming_record != null){
                            new_edit.setText(incoming_record.getFieldStringValue(key));
                        }
                        layout.addView(getLinearLayout(new_edit, label),layoutParams);
                        fieldList.add(new_edit);
                    }
                    else if (input_type.equals("textarea")){
                        Log.d(TAG, "IS TEXTAREA");
                        EditText new_textarea = new EditText(getActivity());
                        new_textarea.setTag(key);
                        new_textarea.setMaxLines(5);
                        new_textarea.setLines(5);
                        new_textarea.setSingleLine(false);
                        if(incoming_record != null){
                            new_textarea.setText(incoming_record.getFieldStringValue(key));
                        }
                        layout.addView(getLinearLayout(new_textarea, label),layoutParams);
                        fieldList.add(new_textarea);
                    }
                }
            }

        }catch(JSONException e){
            Log.d(TAG, e.toString());
        }

        Button submit_button = new Button(getActivity());
        submit_button.setText("Submit");
        layout.addView(submit_button, layoutParams);
        submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitForm();
            }
        });
        return inflatedView;
    }

    private LinearLayout getLinearLayout(View field, String name){
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout outputLayout = new LinearLayout(getActivity());
        outputLayout.setOrientation(LinearLayout.VERTICAL);
        TextView label = new TextView(getActivity());
        label.setText(name);
        outputLayout.addView(label, layoutParams);
        outputLayout.addView(field);
        return outputLayout;
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
        contentQueryMaker = new ContentQueryMaker(getActivity().getContentResolver());

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

    private void submitForm(){
        List switch_values = new ArrayList();

        for (Object f : fieldList)
        {
            if (f instanceof Switch) {
                Switch old_switch = (Switch) f;
                Map valueMap = new HashMap();
                valueMap.put("name", (String) old_switch.getTag());
                if(old_switch.isChecked()){
                    valueMap.put("value", "on");
                }
                else{
                    valueMap.put("value", "off");
                }

                switch_values.add(valueMap);
            }
            else if (f instanceof EditText) {
                EditText old_edit = (EditText) f;
                Map valueMap = new HashMap();
                valueMap.put("name", (String) old_edit.getTag());
                valueMap.put("value", old_edit.getText().toString());
                switch_values.add(valueMap);
            }

        }
        Map outputMap = new HashMap();
        outputMap.put("values", switch_values);
        try{
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String hostname = settings.getString("example_text", "NO HOSTNAME");
            outputMap.put("outgoing_url", hostname+"/location/"+new Integer(location_json.getInt("id")).toString()+"/indicator/"+new Integer(incoming_indicator.getInt("id")).toString()+"/record/create/");
            outputMap.put("indicator_id", incoming_indicator.getInt("id"));
            outputMap.put("location_id", location_json.getInt("id"));
            outputMap.put("title", incoming_indicator.getString("title")+" "+contentQueryMaker.getCurrentTimeStamp());
        }catch(JSONException e){
            Log.d(TAG, e.toString());
        }
        JSONObject outputJSON = new JSONObject(outputMap);

        Log.d("valueJSON", outputJSON.toString());
        insert_into_provider(getActivity().getContentResolver().acquireContentProviderClient(Contract.Entry.CONTENT_URI),outputJSON.toString(), ObjectTypes.TYPE_RECORD,-1);

    }

    public void insert_into_provider(ContentProviderClient provider, String json, Integer objecttype, Integer model_id){
        try {
            // Defines a new Uri object that receives the result of the insertion
            Uri mNewUri;

            // Defines an object to contain the new values to insert
            ContentValues mNewValues = new ContentValues();

        /*
         * Sets the values of each column and inserts the word. The arguments to the "put"
         * method are "column name" and "value"
         */
            mNewValues.put(Contract.Entry.COLUMN_NAME_OBJECTTYPE, objecttype);
            mNewValues.put(Contract.Entry.COLUMN_NAME_JSON, json);
            mNewValues.put(Contract.Entry.COLUMN_NAME_MODEL_ID, model_id);


            mNewUri = provider.insert(
                    Contract.Entry.CONTENT_URI,   // the user dictionary content URI
                    mNewValues                          // the values to insert
            );

        } catch (RemoteException e) {
            Log.e(TAG, "Error updating database: " + e.toString());
            return;
        }
    }



}
