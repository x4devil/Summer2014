package summer.app;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.quickblox.core.QBCallback;
import com.quickblox.core.QBSettings;
import com.quickblox.core.result.Result;
import com.quickblox.module.auth.QBAuth;
import com.quickblox.module.chat.QBChatService;
import com.quickblox.module.users.model.QBUser;
import org.jivesoftware.smack.ConnectionListener;


public class MainActivity extends ActionBarActivity implements QBCallback{

    private final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_CODE_LOGIN = 0;

    private static final String APP_ID = "11841";
    private static final String AUTH_KEY = "EsVps7wEgBHcabU";
    private static final String AUTH_SECRET = "46QU9T6XR2Sm7ZL";

    private ProgressDialog progressDialog;

    private TextView myLogin;

    private ConnectionListener connectionListener = new ConnectionListener() {
        @Override
        public void connectionClosed() {
            Log.i(TAG, "Соедиинение закрыто пользователем");
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
    };

    private View.OnClickListener signOut = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.i(TAG, " -- Смена пользователя");
            if( QBChatService.getInstance().logout()) {
                Log.i(TAG, " -- Пользователь вышел");
                ((QBConnection)getApplication()).setUser(null);
                Intent intent = new Intent(MainActivity.this, SignIn.class);
                startActivityForResult(intent, REQUEST_CODE_LOGIN);
            } else {
                Toast toast = Toast.makeText(MainActivity.this, "Не удалось выйти", Toast.LENGTH_SHORT);
                toast.show();
                Log.e(TAG, " -- Не удалось сменить пользователя");
            }

        }
    };


    public void startSession() {
        if(progressDialog == null) {
            progressDialog = new ProgressDialog(this);
        }
        progressDialog.setMessage("Подключение к серверу");
        progressDialog.show();
        Log.i(TAG, " -- Создаем сессию");
        QBAuth.createSession(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ((Button) findViewById(R.id.signOutBtn)).setOnClickListener(signOut);
        myLogin = (TextView) findViewById(R.id.myLoginView);

        QBSettings.getInstance().fastConfigInit(APP_ID, AUTH_KEY, AUTH_SECRET);
        startSession();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        progressDialog.dismiss();
        finish();
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
        progressDialog.dismiss();

        if(result.isSuccess()) {
            Log.i(TAG, " -- Сессия создана");

            Intent intent = new Intent(MainActivity.this, SignIn.class);
            startActivityForResult(intent, REQUEST_CODE_LOGIN);

        } else {
            Log.i(TAG, " -- Сессия не создана. Ошибка: " + result.getErrors().toString());

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Ошибка")
                    .setMessage("Не удалось подключится к серверу.\nПовторить попытку?")
                    .setCancelable(false)
                    .setPositiveButton("Повторить", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Log.i(TAG, " -- Повторное создание сессии");
                            startSession();
                        }
                    })
                    .setNegativeButton("Выход", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Log.i(TAG, " -- Закрытие сессии");
                            if(progressDialog == null) {
                                progressDialog = new ProgressDialog(MainActivity.this);
                            }
                            progressDialog.setMessage("Закрытие приложения");
                            progressDialog.show();

                            QBAuth.deleteSession(new QBCallback() {
                                @Override
                                public void onComplete(Result result) {
                                    progressDialog.dismiss();
                                    if(result.isSuccess()) {
                                        Log.i(TAG, " -- Сессия закрыта");
                                    } else {
                                        Log.e(TAG, " -- Не удалось закрыть сессию. Ошибка: " + result.getErrors().toString());
                                    }
                                    finish();
                                }

                                @Override
                                public void onComplete(Result result, Object o) {

                                }
                            });
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CODE_LOGIN) {
            switch (resultCode) {
                case RESULT_OK : {
                    QBChatService.getInstance().startAutoSendPresence(60);
                    myLogin.setText(((QBConnection) getApplication()).getUser().getLogin());

                    //Показать список контактов
                } break;
                case RESULT_CANCELED : {
                    Log.i(TAG, " -- Закрытие сессии");
                    if(progressDialog == null) {
                        progressDialog = new ProgressDialog(MainActivity.this);
                    }
                    progressDialog.setMessage("Закрытие приложения");
                    progressDialog.show();

                    QBAuth.deleteSession(new QBCallback() {
                        @Override
                        public void onComplete(Result result) {
                            progressDialog.dismiss();
                            if(result.isSuccess()) {
                                Log.i(TAG, " -- Сессия закрыта");
                            } else {
                                Log.e(TAG, " -- Не удалось закрыть сессию. Ошибка: " + result.getErrors().toString());
                            }
                            finish();
                        }

                        @Override
                        public void onComplete(Result result, Object o) {

                        }
                    });
                } break;
            }
        }
    }

    @Override
    public void onComplete(Result result, Object o) {

    }
}
