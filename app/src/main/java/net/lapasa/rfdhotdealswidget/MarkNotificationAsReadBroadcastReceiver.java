package net.lapasa.rfdhotdealswidget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import net.lapasa.rfdhotdealswidget.model.entities.NotificationRecord;

public class MarkNotificationAsReadBroadcastReceiver extends BroadcastReceiver
{
    public static final String NOTIFICATION_READ = "notification_read";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.i(MarkNotificationAsReadBroadcastReceiver.class.getName(), "MarkNotificationAsReadBroadcastReceiver");

        if (intent.getAction().equals(NOTIFICATION_READ))
        {
            int id = intent.getIntExtra(NotificationRecord._ID, -1);
            if (id >= 0)
            {
                Log.i(MarkNotificationAsReadBroadcastReceiver.class.getName(), "Deleting record id " + id);
                NotificationRecord notificationRecord = NotificationRecord.getById(id);

                if (notificationRecord != null)
                {
                    notificationRecord.setReadFlag(NotificationRecord.READ);
                    notificationRecord.save();
                }
            }
        }
    }
}
