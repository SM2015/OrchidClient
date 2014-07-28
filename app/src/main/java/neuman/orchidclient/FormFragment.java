package neuman.orchidclient;
//icon from http://raindropmemory.deviantart.com

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
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
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_LOCATION = "param1";
    private static final String ARG_INDICATOR = "param2";
    private static final String ARG_RECORD = "param3";

    private String incoming_indicator_string;
    private JSONObject incoming_indicator;
    private String record_json_string;
    private Record incoming_record;
    private String incoming_location_json_string;
    private JSONObject location_json;
    private List fieldList;
    private Integer visible_checkboxes = 0;
    private OnFragmentInteractionListener mListener;

    private Button button_outbox;
    private Button button_draft;

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
            incoming_location_json_string = getArguments().getString(ARG_LOCATION);
            record_json_string = getArguments().getString(ARG_RECORD);
            try{
                incoming_indicator = new JSONObject(incoming_indicator_string);
                location_json = new JSONObject(incoming_location_json_string);
                if (record_json_string != "") {
                    incoming_record = new Record(new JSONObject(record_json_string));
                }
                else{
                    incoming_record = null;
                }
                getActivity().getActionBar().setTitle(incoming_indicator.get("title").toString());
            }catch(JSONException e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View inflatedView = inflater.inflate(R.layout.fragment_form, container, false);

        button_outbox = (Button) inflatedView.findViewById(R.id.button_outbox);
        button_outbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitForm(false);
            }
        });

        button_draft = (Button) inflatedView.findViewById(R.id.button_draft);
        button_draft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitForm(true);
            }
        });

        fieldList = new ArrayList();


        LinearLayout layout = (LinearLayout) inflatedView.findViewById(R.id.FieldsLinearLayout);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);

        JSONObject locationObject = new JSONObject();
        JSONArray locationList = new JSONArray();
        visible_checkboxes = 0;

        try{
            JSONArray fieldsList = incoming_indicator.getJSONArray("fields");
            Log.d(TAG, "fieldsList json: "+fieldsList.toString());
            Integer visible_fields = 0;

            for(int i=0; i < fieldsList.length(); i++) {
                int colorPos = i % ObjectTypes.colors.length;
                JSONObject field = fieldsList.getJSONObject(i);
                Log.d(TAG, "field: " + field.toString());
                if (field.getBoolean("visible")) {
                    visible_fields +=1;
                    String label = field.getString("label");
                    String field_id = field.getString("id");
                    String field_type = field.getString("field_type");
                    Log.d(TAG, "input_type: " + field_type);
                    if (field_type.equals("CHECKBOX")) {
                        Log.d(TAG, "IS CHECKBOX");
                        visible_checkboxes +=1;
                        Switch new_switch = new Switch(getActivity());
                        new_switch.setText(visible_fields+". "+label);
                        new_switch.setTag(field_id);
                        if (incoming_record != null) {
                            Boolean value = (Boolean) incoming_record.getFieldValue(field_id);
                            if (value.equals(true)) {
                                new_switch.setChecked(true);
                            }
                        }
                        layout.addView(getLinearLayout(new_switch, "",ObjectTypes.colors[colorPos]), layoutParams);
                        fieldList.add(new_switch);
                    } else if (field_type.equals("TEXT")) {
                        Log.d(TAG, "IS TEXT");
                        EditText new_edit = new EditText(getActivity());
                        new_edit.setTag(field_id);
                        if (incoming_record != null) {
                            new_edit.setText((String) incoming_record.getFieldValue(field_id));
                        }
                        layout.addView(getLinearLayout(new_edit, visible_fields+". "+label,ObjectTypes.colors[colorPos]), layoutParams);
                        fieldList.add(new_edit);
                    } else if (field_type.equals("TEXTAREA")) {
                        Log.d(TAG, "IS TEXTAREA");
                        EditText new_textarea = new EditText(getActivity());
                        new_textarea.setTag(field_id);
                        new_textarea.setMaxLines(5);
                        new_textarea.setLines(5);
                        new_textarea.setSingleLine(false);
                        if (incoming_record != null) {
                            new_textarea.setText((String) incoming_record.getFieldValue(field_id));
                        }
                        layout.addView(getLinearLayout(new_textarea, visible_fields+". "+label,ObjectTypes.colors[colorPos]), layoutParams);
                        fieldList.add(new_textarea);
                    }
                }
            }


        }catch(JSONException e){
            e.printStackTrace();
        }



        return inflatedView;
    }

    private LinearLayout getLinearLayout(View field, String name, Integer color){
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout outputLayout = new LinearLayout(getActivity());
        outputLayout.setOrientation(LinearLayout.VERTICAL);
        TextView label = new TextView(getActivity());
        label.setText(name);
        outputLayout.addView(label, layoutParams);
        outputLayout.addView(field);
        outputLayout.setBackgroundColor(color);
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

    private void submitForm(Boolean draft){
        List switch_values = new ArrayList();
        Integer visible_checkboxes_checked = 0;

        for (Object f : fieldList)
        {
            if (f instanceof Switch) {
                Switch old_switch = (Switch) f;
                Map valueMap = new HashMap();
                valueMap.put("field_id", (String) old_switch.getTag());
                if(old_switch.isChecked()){
                    valueMap.put("value", true);
                    visible_checkboxes_checked +=1;
                }
                else{
                    valueMap.put("value", false);
                }

                switch_values.add(valueMap);
            }
            else if (f instanceof EditText) {
                EditText old_edit = (EditText) f;
                Map valueMap = new HashMap();
                valueMap.put("field_id", (String) old_edit.getTag());
                valueMap.put("value", old_edit.getText().toString());
                switch_values.add(valueMap);
            }

        }
        float score = (visible_checkboxes_checked * 100.0f) / visible_checkboxes;
        Map outputMap = new HashMap();
        outputMap.put("values", switch_values);
        try{
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String hostname = settings.getString("example_text", "NO HOSTNAME");
            outputMap.put("outgoing_url", hostname+"/location/"+new Integer(location_json.getInt("id")).toString()+"/indicator/"+new Integer(incoming_indicator.getInt("id")).toString()+"/record/upload/");
            outputMap.put("indicator_id", incoming_indicator.getInt("id"));
            outputMap.put("location_id", location_json.getInt("id"));
            outputMap.put("score", score);
            outputMap.put("draft", draft);
            outputMap.put("title", incoming_indicator.getString("title")+" "+contentQueryMaker.getCurrentTimeStamp());
            //add the row_id so we can update instead of insert if there was a pre-existing record (aka we are editing)
            if(incoming_record != null){
                outputMap.put("row_id", (Integer) incoming_record.get("row_id"));
            }
            }catch(JSONException e){
            e.printStackTrace();
        }
        JSONObject outputJSON = new JSONObject(outputMap);

        Log.d("valueJSON", outputJSON.toString());
        save_to_provider(getActivity().getContentResolver().acquireContentProviderClient(Contract.Entry.CONTENT_URI), outputJSON.toString(), ObjectTypes.TYPE_RECORD, -1);
        FragmentManager fragmentManager = getFragmentManager();
        String destination = "OUTBOX";
        if(draft){
            destination = "DRAFTS";
        }
        fragmentManager.beginTransaction().replace(R.id.content_frame, MessageFragment.newInstance(incoming_location_json_string,incoming_indicator_string,Float.toString(score), destination)).addToBackStack(null).commit();

    }

    public void save_to_provider(ContentProviderClient provider, String json_string, Integer object_type, Integer model_id){
        try {

            Integer row_id = null;
            String mSelectionClause = null;
            // Defines a new Uri object that receives the result of the insertion
            Uri mNewUri;

            // Defines an object to contain the new values to insert
            ContentValues mNewValues = new ContentValues();


            try {
                JSONObject json = new JSONObject(json_string);
                Log.d(TAG, "save_to_provider: "+json_string);
                row_id = json.getInt("row_id");
                mSelectionClause =
                        Contract.Entry.COLUMN_NAME_OBJECTTYPE+" = "+object_type
                                +" AND "+"_ID"+" = "+row_id;
                Log.d(TAG, "Trying to save to :"+mSelectionClause);
            }catch(JSONException e){
                e.printStackTrace();
            }



        /*
         * Sets the values of each column and inserts the word. The arguments to the "put"
         * method are "column name" and "value"
         */
            mNewValues.put(Contract.Entry.COLUMN_NAME_OBJECTTYPE, object_type);
            mNewValues.put(Contract.Entry.COLUMN_NAME_JSON, json_string);
            mNewValues.put(Contract.Entry.COLUMN_NAME_MODEL_ID, model_id);


            if(row_id != null){
                provider.update( Contract.Entry.CONTENT_URI, mNewValues, mSelectionClause, null );
            }
            else{
                provider.insert(Contract.Entry.CONTENT_URI, mNewValues);
            }

        } catch (RemoteException e) {
            Log.e(TAG, "Error updating database: " + e.toString());
            e.printStackTrace();
            return;
        }
    }

}
