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
import android.widget.EditText;

import com.quickblox.core.QBCallback;
import com.quickblox.core.result.Result;
import com.quickblox.module.chat.QBChatService;
import com.quickblox.module.chat.listeners.SessionCallback;
import com.quickblox.module.chat.smack.SmackAndroid;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;


public class SignIn extends ActionBarActivity implements QBCallback, View.OnClickListener{

    private final String TAG = SignIn.class.getSimpleName();

    private QBUser user;
    private String login;
    private String password;
    private ProgressDialog progressDialog;
    private SmackAndroid smackAndroid;
    private Button loginBtn;
    private Button regBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Подключение к серверу");

        loginBtn = (Button)findViewById(R.id.loginBtn);
        loginBtn.setOnClickListener(this);

        regBtn = (Button) findViewById(R.id.regBtn);
        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignIn.this, Registration.class);
                startActivity(intent);
            }
        });

        smackAndroid =  SmackAndroid.init(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.sign_in, menu);
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
    public void onClick(View view) {
        login = ((EditText)findViewById(R.id.loginEdit)).getText().toString();
        password = ((EditText) findViewById(R.id.passwordEdit)).getText().toString();
        user = new QBUser(login, password);
        progressDialog.show();

        QBUsers.signIn(user, new QBCallback() {
            @Override
            public void onComplete(Result result) {
                if(result.isSuccess()) {
                    QBChatService.getInstance().loginWithUser(user, new SessionCallback(){

                        @Override
                        public void onLoginSuccess() {
                            progressDialog.dismiss();
                            ((QBConnection)getApplication()).setUser(user);
                            Log.d(TAG, " -- Вход выполнен успешно");
                            Intent intent = new Intent();
                            setResult(RESULT_OK, intent);
                            finish();
                        }

                        @Override
                        public void onLoginError(String s) {
                            progressDialog.dismiss();
                            Log.e(TAG, " -- Не удалось войти. Ошибка: " + s);
                            AlertDialog.Builder builder = new AlertDialog.Builder(SignIn.this);

                            builder.setTitle("Ошибка")
                                    .setMessage("Не удалось войти. Повторить попытку?")
                                    .setPositiveButton("Повторить", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            ((EditText) findViewById(R.id.loginEdit)).setText("");
                                            ((EditText) findViewById(R.id.passwordEdit)).setText("");
                                        }
                                    })
                                    .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            Intent intent = new Intent();
                                            setResult(RESULT_CANCELED, intent);
                                            finish();
                                        }
                                    });
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                    });
                } else {
                    progressDialog.dismiss();
                    Log.e(TAG, " -- Не удалось войти. Ошибка: " + result.getErrors().toString());
                    AlertDialog.Builder builder = new AlertDialog.Builder(SignIn.this);

                    builder.setTitle("Ошибка")
                            .setMessage("Не удалось войти. Повторить попытку?")
                            .setPositiveButton("Повторить", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    ((EditText) findViewById(R.id.loginEdit)).setText("");
                                    ((EditText) findViewById(R.id.passwordEdit)).setText("");

                                }
                            })
                            .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent = new Intent();
                                    setResult(RESULT_CANCELED, intent);
                                    finish();
                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }

            @Override
            public void onComplete(Result result, Object o) {

            }
        });
    }
    @Override
    public void onDestroy() {
        smackAndroid.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    @Override
    public void onComplete(Result result) {

    }

    @Override
    public void onComplete(Result result, Object o) {

    }
}
