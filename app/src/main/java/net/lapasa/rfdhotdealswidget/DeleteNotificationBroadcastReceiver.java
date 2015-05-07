package net.lapasa.rfdhotdealswidget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import net.lapasa.rfdhotdealswidget.model.entities.NotificationNewsItemRecord;
import net.lapasa.rfdhotdealswidget.model.entities.NotificationRecord;

import java.util.List;

public class DeleteNotificationBroadcastReceiver extends BroadcastReceiver
{
    public static final String NOTIFICATION_CANCELLED = "notification_cancelled";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.i(DeleteNotificationBroadcastReceiver.class.getName(), "DeleteNotificationBroadcastReceiver");
        if (intent.getAction().equals(NOTIFICATION_CANCELLED))
        {
            int id = intent.getIntExtra(NotificationRecord._ID, -1);
            if (id >= 0)
            {
                Log.i(DeleteNotificationBroadcastReceiver.class.getName(), "Deleting record id " + id);
                NotificationRecord notificationRecord = NotificationRecord.getById(id);

                if (notificationRecord != null)
                {
                    List<NotificationNewsItemRecord> notificationNewsItemRecords = notificationRecord.fetchNewsItems();
                    for (NotificationNewsItemRecord newsItem : notificationNewsItemRecords)
                    {
                        newsItem.delete();
                    }
                    notificationRecord.delete();
                }
            }
        }
    }
}
