package net.lapasa.rfdhotdealswidget.services;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import net.lapasa.rfdhotdealswidget.DealWatchActivity;
import net.lapasa.rfdhotdealswidget.R;
import net.lapasa.rfdhotdealswidget.model.entities.NotificationNewsItemRecord;
import net.lapasa.rfdhotdealswidget.model.entities.NotificationRecord;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DispatchNotificationCommand
{
    private final NotificationManager notificationManager;
    private int NOTIFICATION_ID = 4112015;
    private List<NotificationRecord> notificationRecords;
    private Context context;
    String s = "Neque porro quisquam est qui $99.87 dolorem ipsum quia dolor sit amet, consectetur, adipisci velit. Neque porro quisquam est $45.22 qui dolorem ipsum quia dolor sit $1 amet, consectetur, adipisci velit. Neque porro quisquam est qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit. Neque porro quisquam est qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit. Neque porro quisquam est qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit.";
    private String price;


    private Pattern pricePattern = Pattern.compile("\\$\\d+(\\.\\d{1,2})?"); // \$\d+(\.\d{1,2})?
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
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(notificationData.getTitle())
                    .setCategory(Notification.CATEGORY_EMAIL)
                    .setAutoCancel(true).setLights(0xff0000, 300, 1000)
                    .setContentIntent(getPendingIntent(notificationData))
                    .setDeleteIntent(getDeleteIntent());


            List<NotificationNewsItemRecord> newsItemIds = notificationData.fetchNewsItems();
            if (newsItemIds.size() == 1)
            {
                NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
                bigTextStyle.bigText(notificationData.getBody());
                builder.setStyle(bigTextStyle);
            }

            int id = safeLongToInt(notificationData.getId());
            notificationManager.notify(id, builder.build());
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
            int id = getRecordId();
            resultIntent = new Intent(context, DealWatchActivity.class);
            resultIntent.putExtra(DealWatchActivity.RECORD_ID, id);
        }

        return PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private int getRecordId()
    {
        // TODO: Iterate through
        return safeLongToInt(new Date().getTime());
    }

    private int safeLongToInt(long l)
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
            List<String> targets = new ArrayList<>();

            Matcher m = pricePattern.matcher(s);

            while(m.find())
            {
                targets.add(m.group());
            }

            return highlight(s, targets);

        }
        else
        {
            // TODO: Iterate through the dealWatch collection and get the keywords
            StringBuilder builder = new StringBuilder();
            builder.append("\"ssd\"");
            builder.append(", ");
            builder.append("\"newegg\"");
            builder.append(", ");
            builder.append("\"walmart\"");

            return "Keywords: " +builder.toString();
        }
    }

    private String getContentTitle()
    {
        if (notificationRecords.size() == 1)
        {
            Object dealAlert = notificationRecords.get(0);
            String keyword = "\"ssd\"" ; //dealAlert.getKeywords();

            if (price != null)
            {
                keyword += " for " + price;
            }

            return "New Deal Alert: " + keyword;
        }
        else
        {
            return notificationRecords.size() + " Deals Found";
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
        sb.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        sb.setSpan(new ForegroundColorSpan(Color.RED), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    public PendingIntent getDeleteIntent()
    {
        return PendingIntent.getActivity(context, 0, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
