package uoc.miquel.pac3.services;

import android.app.IntentService;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by mucl on 02/12/2016.
 */

public class NService extends IntentService {
    public NService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
    }
}
