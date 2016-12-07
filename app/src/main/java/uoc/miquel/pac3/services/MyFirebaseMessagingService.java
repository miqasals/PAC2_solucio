package uoc.miquel.pac3.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import uoc.miquel.pac3.BookListActivity;
import uoc.miquel.pac3.R;
import uoc.miquel.pac3.model.BookContent;
import uoc.miquel.pac3.model.CommonConstants;


/**
 * Created by mucl on 30/11/2016.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        int nBookPosition;

        ///////////// DATA RECEPTION /////////////////
        Map<String,String> data = remoteMessage.getData();

        ///////////// DATA PROCESSING /////////////////
        if (!data.isEmpty()) {
            nBookPosition = Integer.parseInt(data.get(CommonConstants.POSITION_KEY));
        } else {
            nBookPosition = -1;
        }

        /////////////// SENDING THE NOTIFICATION ////////////
        sendNotification(remoteMessage.getNotification().getBody(),nBookPosition);

    }



    private void sendNotification(String messageBody, int position) {


        ///////////////// INTENTS  /////////////////////
        // Creem un intent per quan vulguem veure el detall del llibre
        Intent detailIntent = new Intent(this, BookListActivity.class);
        detailIntent.setAction(CommonConstants.ACTION_DETAIL);
        detailIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        detailIntent.putExtra(CommonConstants.POSITION_KEY, position);
        PendingIntent piDetail = PendingIntent.getActivity(this,(int) System.currentTimeMillis(),detailIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        //Creem un intent per quan vulguem borrar el llibre de la based dades local.
        Intent eraseIntent = new Intent(this, BookListActivity.class);
        eraseIntent.setAction(CommonConstants.ACTION_ERASE);
        eraseIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        eraseIntent.putExtra(CommonConstants.POSITION_KEY, position);
        PendingIntent piErase = PendingIntent.getActivity(this,(int) System.currentTimeMillis(), eraseIntent,  PendingIntent.FLAG_UPDATE_CURRENT);

        //Creem un intent que ens conduirà a l'aplicació en cas que es premi al missatge de l'aplicació.
        //TODO: Mirar de crear una finestra d'actuació dins de l'aplicació per quan s'activi aquesta opció.
        Intent mainIntent = new Intent(this, BookListActivity.class);
        mainIntent.setAction(CommonConstants.ACTION_MAIN);
        PendingIntent piMain = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), mainIntent,  PendingIntent.FLAG_UPDATE_CURRENT);
        ///////////////////////////////////////////////

        //Es crea un so per la notificacio
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        /////////////////  NOTIFICATION BUILDER //////////
        //Es crea el builder de la notificació.
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                //Configurem la icona de la barra d'estat
                .setSmallIcon(R.drawable.ic_import_contacts_black_24dp)
                //Titol de la notificacio visible quan esta extesa.
                .setContentTitle("Notificació Firebase")
                //Text de la notificacio visible quan esta extesa.
                .setContentText(BookContent.getBooks().get(position).getTitle())
                //Indiquem a la notificació que es pot tancar despres de presionar-la
                //.setAutoCancel(true)
                //Asigna el so que hem creat anteriorment.
                .setSound(defaultSoundUri)
                // Set the vibration of the device to two vibrations of one second.
                .setVibrate(new long[] { 1000, 1000 })
                // Set the led light in blue when receive a notification.
                .setLights(Color.BLUE,3000,3000)

                .setStyle(new NotificationCompat.BigTextStyle().bigText(BookContent.getBooks().get(position).getTitle()))
                .addAction(new NotificationCompat.Action(R.drawable.ic_delete_black_24dp,"Eliminar",piErase))
                .addAction(new NotificationCompat.Action(R.drawable.ic_search_black_24dp,"Veure",piDetail))
                .setContentIntent(piMain)
                ;
        ////////////////////////////////////////////////////


        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(BookListActivity.class);


        //Es crea una instancia del gestor de notificacions.
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //S'executa la notificació.
        notificationManager.notify(CommonConstants.NOTIFICATION_ID, notificationBuilder.build());
    }


}
