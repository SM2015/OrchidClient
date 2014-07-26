package neuman.orchidclient.authentication;

import android.accounts.Account;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created with IntelliJ IDEA.
 * User: Udini
 * Date: 19/03/13
 * Time: 19:10
 */
public class OrchidAuthenticatorService extends Service {

    private static final String TAG = "UdinicAuthenticatorService";
    private static final String ACCOUNT_TYPE = "neuman.orchidclient.authentication.UdinicAuthenticatorService";
    public static final String ACCOUNT_NAME = "orchidclientaccount";
    @Override
    public IBinder onBind(Intent intent) {

        OrchidAuthenticator authenticator = new OrchidAuthenticator(this);
        return authenticator.getIBinder();
    }

    public static Account GetAccount() {
        // Note: Normally the account name is set to the user's identity (username or email
        // address). However, since we aren't actually using any user accounts, it makes more sense
        // to use a generic string in this case.
        //
        // This string should *not* be localized. If the user switches locale, we would not be
        // able to locate the old account, and may erroneously register multiple accounts.
        final String accountName = ACCOUNT_NAME;
        return new Account(accountName, ACCOUNT_TYPE);
    }
}
