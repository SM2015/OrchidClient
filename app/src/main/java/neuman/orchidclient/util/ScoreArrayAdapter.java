package neuman.orchidclient.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import neuman.orchidclient.R;
import neuman.orchidclient.content.ObjectTypes;
import neuman.orchidclient.models.Indicator;
import neuman.orchidclient.models.ModelItem;

/**
 * Created by neuman on 7/31/14.
 */
public class ScoreArrayAdapter extends ArrayAdapter<ModelItem> {
    // declaring our ArrayList of items
    protected ArrayList objects;

    public ScoreArrayAdapter(Context context, int textViewResourceId, ArrayList objects) {
        super(context, textViewResourceId, objects);
        this.objects = objects;
    }

    public View getView(int position, View convertView, ViewGroup parent){

        // assign the view we are converting to a local variable
        View v = convertView;

        // first check to see if the view is null. if so, we have to inflate it.
        // to inflate it basically means to render, or show, the view.
        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.score_list_item, null);
        }

		/*
		 * Recall that the variable position is sent in as an argument to this method.
		 * The variable simply refers to the position of the current object in the list. (The ArrayAdapter
		 * iterates through the list we sent it)
		 *
		 * Therefore, i refers to the current Item object.
		 */
        Indicator i = (Indicator) objects.get(position);

        if (i != null) {

            // This is how you obtain a reference to the TextViews.
            // These TextViews are created in the XML files we defined.

            TextView tt = (TextView) v.findViewById(R.id.titleView);
            TextView st = (TextView) v.findViewById(R.id.scoreView);

            // check to see if each individual textview is null.
            // if not, assign some text!
            if (tt != null){
                tt.setText(i.getTitle());
                if (i.color != -1){
                    tt.setBackgroundColor(i.color);
                    st.setBackgroundColor(i.color);
                }
                else {
                    //set background from colors
                    int colorPos = position % ObjectTypes.colors.length;
                    tt.setBackgroundColor(ObjectTypes.colors[colorPos]);
                    st.setBackgroundColor(ObjectTypes.colors[colorPos]);
                }
                if(i.getTotal_records()<=0){
                    st.setText("No Data");
                }else {
                    st.setText(i.getPercentage().toString() + "%");
                }
                //tt.setLayoutParams(new LinearLayout.LayoutParams(AbsListView.LayoutParams.WRAP_CONTENT, AbsListView.LayoutParams.WRAP_CONTENT));
            }
        }

        // the view must be returned to our activity
        return v;

    }
}
