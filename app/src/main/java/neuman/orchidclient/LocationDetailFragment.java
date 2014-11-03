package neuman.orchidclient;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import neuman.orchidclient.content.ContentQueryMaker;
import neuman.orchidclient.content.ObjectTypes;
import neuman.orchidclient.models.Location;
import neuman.orchidclient.models.ModelItem;
import neuman.orchidclient.models.Photo;
import neuman.orchidclient.util.JSONArrayAdapter;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LocationDetailFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LocationDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class LocationDetailFragment extends Fragment {
    private String TAG = getClass().getSimpleName();
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "location_json_string";
    private ContentQueryMaker contentQueryMaker;
    private ListView listView;
    private ArrayList<ModelItem> modelItems = new ArrayList<ModelItem>();
    private Button button_drafts;
    private Button button_visualize;
    private Button button_photo;
    private View view_main;
    private View view_datepicker;

    // TODO: Rename and change types of parameters
    private String location_json_string;
    private JSONObject location_json;
    private Location location_model;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param location_json_string Location object's json.
     * @return A new instance of fragment LocationDetailFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LocationDetailFragment newInstance(String location_json_string) {
        LocationDetailFragment fragment = new LocationDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, location_json_string);
        fragment.setArguments(args);
        return fragment;
    }
    public LocationDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            location_json_string = getArguments().getString(ARG_PARAM1);
            try{
                location_json = new JSONObject(location_json_string);
                location_model = new Location(location_json);
                contentQueryMaker = new ContentQueryMaker(getActivity().getContentResolver());
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
        View inflatedView = inflater.inflate(R.layout.fragment_location_detail, container, false);
        view_main = (View) inflatedView.findViewById(R.id.mainView);
        view_datepicker = (View) inflatedView.findViewById(R.id.datePickerView);
        button_photo = (Button) inflatedView.findViewById(R.id.button_take_photo);
        button_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });

        button_drafts = (Button) inflatedView.findViewById(R.id.button_drafts);
        button_drafts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //view_main.setVisibility(View.INVISIBLE);
                //view_datepicker.setVisibility(View.VISIBLE);
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.content_frame, OutboxFragment.newInstance("DRAFTS", location_json.toString())).addToBackStack(null).commit();
            }
        });
        button_visualize = (Button) inflatedView.findViewById(R.id.button_visualize);
        button_visualize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //view_main.setVisibility(View.INVISIBLE);
                //view_datepicker.setVisibility(View.VISIBLE);
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.content_frame, LocationVisualizeFragment.newInstance(location_json_string)).addToBackStack(null).commit();
            }
        });
        if(modelItems.isEmpty()) {
            Cursor mCursor = contentQueryMaker.get_all_of_model_type_cursor(ObjectTypes.TYPE_INDICATOR);
            // Some providers return null if an error occurs, others throw an exception
            if (null == mCursor) {
                // If the Cursor is empty, the provider found no matches
                Log.d(TAG, "Cursor Error");
            } else if (mCursor.getCount() < 1) {
                Log.d(TAG, "No results");

            } else {
                JSONArray indicator_ids_json;
                ArrayList<Integer> indicator_ids = new ArrayList<Integer>();
                try {
                    indicator_ids_json = location_json.getJSONArray("indicator_ids");
                    // ignore the case of a non-array value.
                    if (indicator_ids_json != null) {

                        // Extract numbers from JSON array.
                        for (int i = 0; i < indicator_ids_json.length(); ++i) {
                            indicator_ids.add(indicator_ids_json.optInt(i));
                        }
                    }

                } catch (JSONException e) {
                    Log.d(TAG, e.toString());
                    e.printStackTrace();
                }
                // Insert code here to do something with the results
                while (mCursor.moveToNext()) {
                    Log.d(TAG, "*****CURSOR MOVED*****");
                    Log.d(TAG, mCursor.getColumnName(0) + ": " + mCursor.getString(0));
                    Log.d(TAG, mCursor.getColumnName(1) + ": " + mCursor.getString(1));
                    String jsonString = mCursor.getString(2);
                    Log.d(TAG, mCursor.getColumnName(2) + ": " + jsonString);
                    try {
                        JSONObject indicator_json = new JSONObject(jsonString);
                        //only add the indicator to the list if it exists within the location indicator_ids list
                        if (indicator_ids.contains(indicator_json.getInt("id"))) {
                            ModelItem newModelItem = new ModelItem(indicator_json.get("title").toString(), indicator_json);
                            modelItems.add(newModelItem);
                        }
                    } catch (JSONException e) {
                        Log.d(TAG, e.toString());
                        e.printStackTrace();
                    }
                }
            }
        }



        listView = (ListView) inflatedView.findViewById(R.id.listView);


        // Define a new Adapter
        // First parameter - Context
        // Second parameter - Layout for the row
        // Third parameter - ID of the TextView to which the data is written
        // Forth - the Array of data

        JSONArrayAdapter adapter = new JSONArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, modelItems);

        // Assign adapter to ListView
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position,
                                    long arg3) {

                try{
                    ModelItem modelItem = (ModelItem) adapter.getItemAtPosition(position);
                    Log.d(TAG, "Clicked " + modelItem.getJSON().get("title").toString());
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.content_frame, FormFragment.newInstance(location_json.toString(), modelItem.getJSON().toString(),"")).addToBackStack(null).commit();

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

    String mCurrentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        Log.d(TAG, "mCurrentPhotoPath: "+mCurrentPhotoPath);
        return image;
    }

    static final int REQUEST_TAKE_PHOTO = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                // Error occurred while creating the File
                Log.d(TAG, "Image save failure!");
                Log.d(TAG, e.toString());
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri new_image_uri = Uri.fromFile(photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,new_image_uri);
                //save to content provider
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
                String hostname = settings.getString("example_text", "NO HOSTNAME");
                Photo new_photo = new Photo("Photo alpha");
                new_photo.put("path",new_image_uri.getPath());
                SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy, hh:mm aaa");
                Date dt = new Date();
                new_photo.put("title","Photo "+sdf.format(dt).toString());
                new_photo.put("location_id", location_model.getId());
                new_photo.put("outgoing_url", hostname + "/location/" + new Integer(location_model.getId()).toString() + "/image/create/");
                contentQueryMaker.save_to_provider(new_photo.getJSON().toString(),ObjectTypes.TYPE_PHOTO,-1);

                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                //
            }
        }
    }

    private void setFullImageFromFilePath(String imagePath, ImageView imageView) {
        // Get the dimensions of the View
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/1024, photoH/400);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, bmOptions);
        imageView.setImageBitmap(bitmap);
    }

}
