package neuman.orchidclient;
//icon from http://raindropmemory.deviantart.com

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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
    private Time selected_time = new Time();
    private OnFragmentInteractionListener mListener;

    private Button button_outbox;

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
        selected_time.setToNow();
        createDialogWithoutDateField().show();
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
                //save this record as a draft for future scoring
                attempt_form_submission(true);
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
                        //layout.addView(getLinearLayout(new_switch, "",ObjectTypes.colors[colorPos]), layoutParams);
                        RadioGroup new_radioGroup = new RadioGroup(getActivity());
                        new_radioGroup.setTag(field_id);

                        RadioButton radio_no = new RadioButton(getActivity());
                        radio_no.setText("No");
                        new_radioGroup.addView(radio_no);

                        RadioButton radio_na = new RadioButton(getActivity());
                        radio_na.setText("N/A");
                        new_radioGroup.addView(radio_na);


                        RadioButton radio_yes = new RadioButton(getActivity());
                        radio_yes.setText("Yes");
                        new_radioGroup.addView(radio_yes);

                        if (incoming_record != null) {
                            Boolean value = (Boolean) incoming_record.getFieldValue(field_id);
                            if(value == null){
                                //this can happen if the value was N/A
                                radio_na.setChecked(true);
                            }
                            else if (value.equals(true)) {
                                //get the yes button and set it to active
                                radio_yes.setChecked(true);
                            }else if(value.equals(false)){
                                radio_no.setChecked(true);
                            }
                        }

                        layout.addView(getLinearLayout(new_radioGroup, visible_fields+". "+label,ObjectTypes.colors[colorPos]), layoutParams);

                        fieldList.add(new_radioGroup);
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

    private void attempt_form_submission(Boolean draft){
        //check to see if there are enough records already
        ArrayList<JSONObject> record_jsons = contentQueryMaker.get_all_of_object_type(ObjectTypes.TYPE_RECORD);
        Integer same_location_same_indicator_record_count =0;
        for(JSONObject j :record_jsons){
            try{
                if((j.getInt("indicator_id")==incoming_indicator.getInt("id"))&&(j.getInt("location_id")==location_json.getInt("id"))){
                    same_location_same_indicator_record_count++;
                }
            }catch(JSONException e){
                e.printStackTrace();
            }
        }
        try {
            Integer max_monthly_records = incoming_indicator.getInt("maximum_monthly_records");
            if (same_location_same_indicator_record_count > max_monthly_records) {
                //if there's too many throw an error and break out of this
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_NEUTRAL:
                                //Yes button clicked
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("You have reached the maximum number of records this month ("+max_monthly_records.toString()+") for this indicator at this location.").setNeutralButton("Ok", dialogClickListener).show();
            }else{
                submitForm(draft);
            }
        }catch(JSONException e){
            e.printStackTrace();
        }
    }

    private void submitForm(Boolean draft){
        JSONArray switch_values = new JSONArray();
        Integer visible_checkboxes_checked = 0;
        Integer na_count = 0;
        Boolean all_radiobuttons_filled = true;

        for (Object f : fieldList)
        {
            try {
                if (f instanceof Switch) {
                    Switch old_switch = (Switch) f;
                    JSONObject valueJSON = new JSONObject();
                    valueJSON.put("field_id", (String) old_switch.getTag());
                    if (old_switch.isChecked()) {
                        valueJSON.put("value", true);
                        visible_checkboxes_checked += 1;
                    } else {
                        valueJSON.put("value", false);
                    }

                    switch_values.put(valueJSON);
                }else if (f instanceof RadioGroup) {
                    RadioGroup old_radioGroup = (RadioGroup) f;
                    JSONObject valueJSON = new JSONObject();
                    valueJSON.put("field_id", (String) old_radioGroup.getTag());
                    //get the checked radio button's index
                    int radioButtonID = old_radioGroup.getCheckedRadioButtonId();
                    View radioButton = old_radioGroup.findViewById(radioButtonID);
                    int checked_radioButton_index = old_radioGroup.indexOfChild(radioButton);
                    if (checked_radioButton_index==0) {
                        //this means No
                        valueJSON.put("value", false);
                    } else if (checked_radioButton_index==1){
                        //this means N/A so add one na
                        na_count+=1;

                    }else if (checked_radioButton_index==2){
                        //this means yes
                        valueJSON.put("value", true);
                        visible_checkboxes_checked += 1;
                    }else{
                        //make a note that a radiobutton field was skipped
                        //prevent submission at the end
                        all_radiobuttons_filled = false;
                    }

                    switch_values.put(valueJSON);
                } else if (f instanceof EditText) {
                    EditText old_edit = (EditText) f;
                    JSONObject valueJSON = new JSONObject();
                    valueJSON.put("field_id", (String) old_edit.getTag());
                    String old_edit_text_value = old_edit.getText().toString();
                    valueJSON.put("value", old_edit_text_value);
                    switch_values.put(valueJSON);
                }
            }catch(JSONException e){
                e.printStackTrace();
            }

        }
        if(all_radiobuttons_filled) {
            float score = (visible_checkboxes_checked * 100.0f) / (visible_checkboxes - na_count);
            JSONObject outputJSON = new JSONObject();
            try {
                outputJSON.put("values", switch_values);
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
                String hostname = settings.getString("example_text", "NO HOSTNAME");
                outputJSON.put("outgoing_url", hostname + "/location/" + new Integer(location_json.getInt("id")).toString() + "/indicator/" + new Integer(incoming_indicator.getInt("id")).toString() + "/record/upload/");
                outputJSON.put("indicator_id", incoming_indicator.getInt("id"));
                outputJSON.put("location_id", location_json.getInt("id"));
                //don't add a score if there are only text boxes
                if(Float.isNaN(score)){
                    outputJSON.put("text_only", true);
                }else {
                    outputJSON.put("score", score);
                }
                outputJSON.put("draft", draft);
                outputJSON.put("year", selected_time.year);
                outputJSON.put("month", selected_time.month+1);
                outputJSON.put("title", "(RECORD) Location: " + location_json.getString("title") + " Indicator: " + incoming_indicator.getString("title") + " Timestamp:" + contentQueryMaker.getCurrentTimeStamp() + " PERCENT: " + Float.toString(score) + "%");
                //add the row_id so we can update instead of insert if there was a pre-existing record (aka we are editing)
                if (incoming_record != null) {
                    outputJSON.put("row_id", (Integer) incoming_record.get("row_id"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.d("valueJSON", outputJSON.toString());
            save_to_provider(getActivity().getContentResolver().acquireContentProviderClient(Contract.Entry.CONTENT_URI), outputJSON.toString(), ObjectTypes.TYPE_RECORD, -1);
            FragmentManager fragmentManager = getFragmentManager();
            String destination = "OUTBOX";
            if (draft) {
                destination = "DRAFTS";
            }
            fragmentManager.beginTransaction().replace(R.id.content_frame, MessageFragment.newInstance(incoming_location_json_string, incoming_indicator_string, Float.toString(score), destination)).addToBackStack(null).commit();
        }else{
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Looks like you left a required field blank. Please go back and fill in all fields.").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    //do things
                }
            }).show();
        }
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

    private DatePickerDialog createDialogWithoutDateField(){
        DatePickerDialog dpd = new DatePickerDialog(getActivity(), myDateSelectedListener,selected_time.year,selected_time.month, 1);
        dpd.setTitle("Select Record Date");
        try{
            /*
            java.lang.reflect.Field[] datePickerDialogFields = dpd.getClass().getDeclaredFields();
            for (java.lang.reflect.Field datePickerDialogField : datePickerDialogFields) {
                if (datePickerDialogField.getName().equals("mDatePicker")) {
                    datePickerDialogField.setAccessible(true);
                    DatePicker datePicker = (DatePicker) datePickerDialogField.get(dpd);
                    java.lang.reflect.Field[] datePickerFields = datePickerDialogField.getType().getDeclaredFields();
                    for (java.lang.reflect.Field datePickerField : datePickerFields) {
                        Log.i("test", datePickerField.getName());
                        if ("mDaySpinner".equals(datePickerField.getName())) {
                            datePickerField.setAccessible(true);
                            Object dayPicker = new Object();
                            dayPicker = datePickerField.get(datePicker);
                            ((View) dayPicker).setVisibility(View.GONE);
                        }
                    }
                }

            }
            */
            dpd.setOnCancelListener(myDateCanceledListener);
            dpd.setButton(
                    DialogInterface.BUTTON_NEGATIVE, "Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DatePickerDialog.BUTTON_NEGATIVE:
                                    //No button clicked
                                    getFragmentManager().popBackStack();
                                    break;
                            }
                        }
                    }
            );

        }catch(Exception ex){
        }
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
        }
    };

    private DatePickerDialog.OnCancelListener myDateCanceledListener
            = new DatePickerDialog.OnCancelListener() {

        @Override
        public void onCancel(DialogInterface dialogInterface) {
            //go back
            getFragmentManager().popBackStack();
        }
    };

}
