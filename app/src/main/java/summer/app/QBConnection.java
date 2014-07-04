package summer.app;

import android.app.Application;

import com.quickblox.module.users.model.QBUser;

/**
 * Created by x4devil
 */
public class QBConnection extends Application{
    QBUser user;

    @Override
    public void onCreate() {
        super.onCreate();
        user = null;
    }
    public QBUser getUser() {
        return user;
    }
    public void setUser(QBUser user) {
        this.user = user;
    }
}
