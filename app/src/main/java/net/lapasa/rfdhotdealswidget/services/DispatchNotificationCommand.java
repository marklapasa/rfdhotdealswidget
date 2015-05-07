package net.lapasa.rfdhotdealswidget.services;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.text.Spannable;
import android.text.SpannableString;

import net.lapasa.rfdhotdealswidget.DealWatchActivity;
import net.lapasa.rfdhotdealswidget.DeleteNotificationBroadcastReceiver;
import net.lapasa.rfdhotdealswidget.R;
import net.lapasa.rfdhotdealswidget.model.entities.NotificationNewsItemRecord;
import net.lapasa.rfdhotdealswidget.model.entities.NotificationRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DispatchNotificationCommand
{
    private final NotificationManager notificationManager;
    private List<NotificationRecord> notificationRecords;
    private Context context;


    public static Pattern pricePattern = Pattern.compile("\\$\\d+(\\.\\d{1,2})?"); // \$\d+(\.\d{1,2})?
    private PendingIntent deleteIntent;


    public DispatchNotificationCommand(Context context, List<NotificationRecord> notificationRecords)
    {
        this.notificationRecords = notificationRecords;
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void execute()
    {
        for (NotificationRecord notificationData : notificationRecords)
        {
            int notificationId = safeLongToInt(notificationData.getId());
            Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.ic_money2)
                    .setContentTitle(notificationData.getTitle())
                    .setContentText(getContentText())
                    .setCategory(Notification.CATEGORY_EMAIL)
                    .setAutoCancel(true).setLights(0xff0000, 300, 1000)
                    .setContentIntent(getPendingIntent(notificationData))
                    .setDeleteIntent(getDeleteIntent(notificationId))
                    .setTicker("Deal Alert: " + notificationData.getTitle())
                    .setLargeIcon(bmp);


            List<NotificationNewsItemRecord> newsItemIds = notificationData.fetchNewsItems();
//            if (newsItemIds.size() == 1)
//            {
//                NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
//                bigTextStyle.bigText(notificationData.getBody());
//                builder.setStyle(bigTextStyle);
//            }


            notificationManager.notify(notificationId, builder.build());
        }
    }



    private PendingIntent getPendingIntent(NotificationRecord notificationData)
    {
        List<NotificationNewsItemRecord> notificationNewsItemRecords = notificationData.fetchNewsItems();
        Intent resultIntent = null;
        if (notificationNewsItemRecords.size() == 1)
        {
            String url = notificationData.getUrl();
            resultIntent = new Intent(Intent.ACTION_VIEW);
            resultIntent.setData(Uri.parse(url));
        }
        else
        {
            // Dummy int data
            long id = notificationData.getOwner().getId();
            resultIntent = new Intent(context, DealWatchActivity.class);
            resultIntent.putExtra(DealWatchActivity.RECORD_ID, id);
        }

        return PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }


    public static int safeLongToInt(long l)
    {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE)
        {
            l /= 2;
        }
        return (int) l;
    }

    private CharSequence getContentText()
    {
        if (notificationRecords.size() == 1)
        {
            NotificationRecord notificationRecord = notificationRecords.get(0);
            List<String> targets = new ArrayList<>();

            Matcher m = pricePattern.matcher(notificationRecord.getBody());

            while(m.find())
            {
                targets.add(m.group());
            }

            return highlight(notificationRecord.getBody(), targets);

        }
        else
        {
            return "Tap to open Deal Watch List";
        }
    }


    public static CharSequence highlight(String src, List<String> targetStrs)
    {
        Spannable sb = new SpannableString(src);

        for (String targetStr : targetStrs)
        {
            Pattern targetStrPattern = Pattern.compile("\\Q" + targetStr + "\\E");
            Matcher matcher = targetStrPattern.matcher(sb);

            while(matcher.find())
            {
                setStyle(sb, matcher.start(), matcher.end());
            }
        }

        return sb;
    }

    public static void setStyle(Spannable sb, int startIndex, int endIndex)
    {
//        sb.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        sb.setSpan(new ForegroundColorSpan(Color.RED), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    /**
     * When the user dismisses a notification, delete the corresponding Notification Record
     * @return
     * @param notificationId
     */
    public PendingIntent getDeleteIntent(int notificationId)
    {
        Intent deleteIntent = new Intent(context, DeleteNotificationBroadcastReceiver.class);
        deleteIntent.setAction(DeleteNotificationBroadcastReceiver.NOTIFICATION_CANCELLED);
        deleteIntent.putExtra(NotificationRecord._ID, notificationId);
        return PendingIntent.getBroadcast(context, notificationId, deleteIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    }
}
