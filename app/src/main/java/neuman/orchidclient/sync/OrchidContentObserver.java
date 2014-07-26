package neuman.orchidclient.sync;

import android.content.Context;
import android.database.ContentObserver;
import android.widget.Toast;
import android.os.Handler;

/**
 * Created by neuman on 7/24/14.
 */
public class OrchidContentObserver extends ContentObserver
{
    public Context Contexto=null; //This is for displaying Toasts

    public OrchidContentObserver(Handler handler)
    {  super(handler);
    }


    @Override public boolean deliverSelfNotifications()
    {
        return super.deliverSelfNotifications();
    }


    @Override public void onChange(boolean selfChange)
    {  super.onChange(selfChange);

        //How do I get more info here?????
        ShowToast("Internal Database Updated");
    }


    private void ShowToast(String strMensaje)
    {  Toast toast1 = Toast.makeText(Contexto, strMensaje, Toast.LENGTH_SHORT);
        toast1.show();
    };

}