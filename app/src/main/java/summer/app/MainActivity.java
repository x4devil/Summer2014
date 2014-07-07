package summer.app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.quickblox.core.QBCallback;
import com.quickblox.core.QBSettings;
import com.quickblox.core.result.Result;
import com.quickblox.module.auth.QBAuth;
import com.quickblox.module.users.model.QBUser;
import org.jivesoftware.smack.ConnectionListener;


public class MainActivity extends ActionBarActivity implements QBCallback{

    private final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_CODE_LOGIN = 0;

    private static final String APP_ID = "11841";
    private static final String AUTH_KEY = "EsVps7wEgBHcabU";
    private static final String AUTH_SECRET = "46QU9T6XR2Sm7ZL";

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Подключение к серверу");
        QBSettings.getInstance().fastConfigInit(APP_ID, AUTH_KEY, AUTH_SECRET);
        progressDialog.show();
        QBAuth.createSession(this);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        progressDialog.dismiss();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings || super.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onComplete(Result result) {
        if(result.isSuccess()) {
            progressDialog.dismiss();
            QBUser user = ((QBConnection) getApplication()).getUser();
            if (user != null) {
                //Показать список контактов
            } else {
                Intent intent = new Intent(MainActivity.this, SignIn.class);
                startActivityForResult(intent, REQUEST_CODE_LOGIN);
            }
        } else {
            Log.e(TAG, "--Не удалось создать сессию. Ошибка:" + result.getErrors().toString());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CODE_LOGIN) {
            switch (resultCode) {
                case RESULT_OK : {
                    //Показать список контактов
                } break;
                case RESULT_CANCELED : {
                    //Вход отменен
                } break;
            }
        }
    }

    private class ChatConnectionListener implements ConnectionListener {

        @Override
        public void connectionClosed() {

        }

        @Override
        public void connectionClosedOnError(Exception e) {
            Intent intent = new Intent(MainActivity.this, SignIn.class);
            startActivityForResult(intent, REQUEST_CODE_LOGIN);
            Log.e(TAG, "Потеряно соединение с сервером. Ошибка: " + e.getLocalizedMessage());
        }

        @Override
        public void reconnectingIn(int i) {

        }

        @Override
        public void reconnectionSuccessful() {

        }

        @Override
        public void reconnectionFailed(Exception e) {

        }
    }
    @Override
    public void onComplete(Result result, Object o) {

    }
}
