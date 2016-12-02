package uoc.miquel.pac3.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import uoc.miquel.pac3.BookDetailActivity;
import uoc.miquel.pac3.BookListActivity;
import uoc.miquel.pac3.R;

/**
 * Created by mucl on 02/12/2016.
 */

public class NService extends IntentService {

    private NotificationCompat.Builder builder;
    private NotificationManager mNotificationManager;
    //private String mMessage;


    public NService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String mMessage = intent.getStringExtra(MyFirebaseMessagingService.BODY);

        String action = intent.getAction();

        if (action.equals(MyFirebaseMessagingService.ACTION_NOTIFICATION)) {
            enviaNotificacio(mMessage); //Creem la notificació amb els botons i tota la mandanga...
        } else if (action.equals(MyFirebaseMessagingService.ACTION_DETAIL)) {
            //Executem la accio de detall
        } else if (action.equals(MyFirebaseMessagingService.ACTION_ERASE)) {
            //Executem la accio de borrat
        }






    }

    private void enviaNotificacio(String mMessage) {

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        Intent eraseIntent = new Intent(this, BookListActivity.class);
        eraseIntent.setAction(MyFirebaseMessagingService.ACTION_ERASE);
        PendingIntent piErase = PendingIntent.getActivity(this, 0, eraseIntent, 0);

        Intent detailIntent = new Intent(this, BookDetailActivity.class);
        detailIntent.setAction(MyFirebaseMessagingService.ACTION_DETAIL);
        PendingIntent piDetail = PendingIntent.getActivity(this, 0, detailIntent, 0);



        //Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Exemple Firebase")
                .setContentText(mMessage)
                //.setAutoCancel(true)
                //.setSound(defaultSoundUri)
                //.setStyle(new NotificationCompat.BigTextStyle().bigText(messageBody))
                .addAction(R.mipmap.ic_launcher,"Eliminar",piErase)
                .addAction(R.mipmap.ic_launcher,"Ver",piDetail)
        ;

        builder.setContentIntent(piErase);


        //NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(0, builder.build());



    }
}
