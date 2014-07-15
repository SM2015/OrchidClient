package neuman.orchidclient.authentication;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import neuman.orchidclient.R;
import neuman.orchidclient.content.Contract;

public class LocationCursorAdapter extends CursorAdapter {

    private LayoutInflater mInflater;

    public LocationCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView content = (TextView) view.findViewById(R.id.listView);
        content.setText(cursor.getString(cursor.getColumnIndex(Contract.Entry._ID)));

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return mInflater.inflate(R.layout.fragment_location_pick, parent, false);
    }

}