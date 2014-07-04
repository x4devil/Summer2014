package summer.app;

import android.app.ProgressDialog;
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


public class Registration extends ActionBarActivity implements QBCallback, View.OnClickListener{

    private QBUser user;
    private String login;
    private String password;
    private ProgressDialog progressDialog;
    private SmackAndroid smackAndroid;
    private Button regBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Подключение");

        regBtn = (Button)findViewById(R.id.regBtn);
        regBtn.setOnClickListener(this);

        smackAndroid = SmackAndroid.init(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.registration, menu);
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

    @Override
    public void onComplete(Result result) {
        if(result.isSuccess()) {
            ((QBConnection)getApplication()).setUser(user);
            QBChatService.getInstance().loginWithUser(user, new SessionCallback() {
                public void onLoginSuccess() {
                    progressDialog.dismiss();
                    Intent intent = new Intent();
                    finish();
                }

                @Override
                public void onLoginError(String s) {
                    progressDialog.dismiss();
                    Log.e(this.getClass().getSimpleName(), "onComplete() -- " + s);
                }
            });
        } else {
            int size = result.getErrors().size();

            for (int i = 0; i < size; i++) {
                Log.e(this.getClass().getSimpleName(), ".onComplete() -- " + result.getErrors().get(i));
            }
        }
    }

    @Override
    public void onComplete(Result result, Object o) {

    }

    @Override
    public void onClick(View view) {
        login = ((EditText) findViewById(R.id.regLoginEdit)).getText().toString();
        password = ((EditText) findViewById(R.id.regPasswordEdit)).getText().toString();

        user = new QBUser(login, password);
        progressDialog.show();
        QBUsers.signUpSignInTask(user, this);
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
        finish();
    }
}
