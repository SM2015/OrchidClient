package neuman.orchidclient;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import neuman.orchidclient.sync.SyncService;

public class SpinnerActivity extends Activity {
    private String TAG = this.getClass().getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spinner);
        registerReceiver(syncFinishedReceiver, new IntentFilter(SyncService.SYNC_FINISHED));
        set_action_bar_title("Synchronizing, please wait...");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.spinner, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private BroadcastReceiver syncFinishedReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "SPINNER RECEIVED SYNC_FINISHED");
            unregisterReceiver(syncFinishedReceiver);
            finish();
        }
    };

    @Override
    public void onBackPressed() {
        //make sure they are ready to submit
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_NEUTRAL:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Please wait until synchronization is complete.").setNeutralButton("Ok", dialogClickListener).show();

    }

    public void finish() {
        super.finish();
    };

    public void set_action_bar_title(String title){
        getActionBar().setTitle(title);
    }

}
