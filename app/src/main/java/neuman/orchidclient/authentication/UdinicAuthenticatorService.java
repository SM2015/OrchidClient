package neuman.orchidclient.authentication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import neuman.orchidclient.authentication.*;

/**
 * Created with IntelliJ IDEA.
 * User: Udini
 * Date: 19/03/13
 * Time: 19:10
 */
public class UdinicAuthenticatorService extends Service {
    @Override
    public IBinder onBind(Intent intent) {

        neuman.orchidclient.authentication.UdinicAuthenticator authenticator = new neuman.orchidclient.authentication.UdinicAuthenticator(this);
        return authenticator.getIBinder();
    }
}
