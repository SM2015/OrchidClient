package neuman.orchidclient;
//icon from http://raindropmemory.deviantart.com

import android.app.ActionBar;
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private List fieldList = new ArrayList();

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FormFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FormFragment newInstance(String param1, String param2) {
        FormFragment fragment = new FormFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
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
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View inflatedView = inflater.inflate(R.layout.fragment_form, container, false);


        LinearLayout layout = (LinearLayout) inflatedView.findViewById(R.id.FieldsLinearLayout);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        String[] switch_names = {"Alpha", "Beta", "Gamma"};
        for (String name : switch_names)
        {
            Switch new_switch = new Switch(getActivity());
            new_switch.setText(name);
            new_switch.setTag(name);
            layout.addView(getLinearLayout(new_switch, ""),layoutParams);

            fieldList.add(new_switch);
        }
        String[] text_names = {"Delta", "Epsilon", "Zeta"};
        for (String name : text_names)
        {
            EditText new_edit = new EditText(getActivity());
            new_edit.setTag(name);
            layout.addView(getLinearLayout(new_edit, name),layoutParams);

            fieldList.add(new_edit);
        }
        String[] multi_names = {"Eta", "Theta", "Iota"};
        for (String name : multi_names)
        {
            EditText new_edit = new EditText(getActivity());
            new_edit.setTag(name);
            new_edit.setMaxLines(5);
            new_edit.setLines(5);
            new_edit.setSingleLine(false);
            layout.addView(getLinearLayout(new_edit, name),layoutParams);

            fieldList.add(new_edit);
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
                valueMap.put("value", old_switch.isChecked());
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
        JSONObject outputJSON = new JSONObject(outputMap);
        Log.d("valueJSON", outputJSON.toString());
    }

}
