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
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import net.lapasa.rfdhotdealswidget.DealWatchActivity;
import net.lapasa.rfdhotdealswidget.MarkNotificationAsReadBroadcastReceiver;
import net.lapasa.rfdhotdealswidget.R;
import net.lapasa.rfdhotdealswidget.model.NewsItem;
import net.lapasa.rfdhotdealswidget.model.NewsItemSQLHelper;
import net.lapasa.rfdhotdealswidget.model.NewsItemsDTO;
import net.lapasa.rfdhotdealswidget.model.Spans;
import net.lapasa.rfdhotdealswidget.model.entities.NotificationNewsItemRecord;
import net.lapasa.rfdhotdealswidget.model.entities.NotificationRecord;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DispatchNotificationCommand
{
    public static final int HIGHLIGHT = 200;
    public static final int BOLD = 201;
    private final NotificationManager notificationManager;
    private NotificationRecord notificationRecord;
    private Context context;
    private static final int NOTIFICATION_ID = 20150515;


    public static Pattern pricePattern = Pattern.compile("\\$\\d+(\\.\\d{1,2})?"); // \$\d+(\.\d{1,2})?
    private PendingIntent deleteIntent;

    public DispatchNotificationCommand(Context context, NotificationRecord notificationRecord)
    {
        this.notificationRecord = notificationRecord;
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

    }

    /**
     * Displays a single notification of a deal or many deals to the user
     */
    public void execute()
    {
        deleteIntent = getDeleteIntent(notificationRecord);

        Uri soundFx = Uri.parse("android.resource://" + context.getPackageName() + "/raw/mario_coin_sound");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentText(bold(notificationRecord.getBody()))
                .setCategory(Notification.CATEGORY_EMAIL)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setVibrate(new long[]{300,300,300})
                .setSound(soundFx)
                .setColor(0xff0000)
                .setAutoCancel(true)
                .setLights(0xff0000, 300, 1000)
                .setContentIntent(getPendingIntent(notificationRecord))
                .setDeleteIntent(deleteIntent);

        builder.setNumber(safeLongToInt(notificationRecord.getCount()));

        // Get body items
        String whereClause = "owner = ?";
        String[] whereArgs = new String[]{String.valueOf(notificationRecord.getId())};
        List<NotificationNewsItemRecord> notificationNewsItemRecords = NotificationNewsItemRecord.find(NotificationNewsItemRecord.class, whereClause, whereArgs);


        CharSequence title = notificationRecord.getTitle();
        builder.setContentTitle(title);


        if (notificationNewsItemRecords.size() == 1)
        {
            builder.setSmallIcon(R.drawable.ic_money2);
            builder.setContentTitle(notificationRecord.getTitle());
            NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle(builder);
            bigTextStyle.setBigContentTitle(notificationRecord.getTitle());
            bigTextStyle.bigText(getContentText(notificationRecord));
            bigTextStyle.setSummaryText("Open RFD Thread");
            builder.setStyle(bigTextStyle);
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
        else
        {
            builder.setSmallIcon(R.drawable.ic_multiple_deals);
            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle(builder);
            inboxStyle.setBigContentTitle(title);

            for (NotificationNewsItemRecord notificationNewsItemRecord : notificationNewsItemRecords)
            {
                inboxStyle.addLine(getMultiNewsItemText(context, notificationNewsItemRecord));
            }
            CharSequence summaryTxt = "Open Deal Watch List";
            inboxStyle.setSummaryText(summaryTxt);
            builder.setContentText(summaryTxt);
            notificationManager.notify(NOTIFICATION_ID, inboxStyle.build());

        }




    }




/*    public void executeOLD()
    {
        notificationRecords = dedupe(notificationRecords);
        for (NotificationRecord notificationData : notificationRecords)
        {

            int notificationId = NOTIFICATION_ID;
//            Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
            Uri soundFx = Uri.parse("android.resource://" + context.getPackageName() + "/raw/mario_coin_sound");
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.ic_money2)
                    .setContentText(bold(notificationData.getSubTitle() + "\n" + notificationData.getBody()))
                    .setCategory(Notification.CATEGORY_EMAIL)
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                    .setVibrate(new long[]{300,300,300})
                    .setSound(soundFx)
                    .setColor(0xff0000)
                    .setAutoCancel(true)
                    .setLights(0xff0000, 300, 1000)
                    .setContentIntent(getPendingIntent(notificationData))
                    .setDeleteIntent(getDeleteIntent(notificationId));
//                    .setLargeIcon(bmp);


            List<NotificationNewsItemRecord> newsItemIds = notificationData.getRecentNewsItems();

            if (newsItemIds != null && newsItemIds.size() > 1)
            {
                builder.setNumber(safeLongToInt(notificationData.getCount()));
                NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle(builder);
                CharSequence title = newsItemIds.size() + " deals found for \"" + notificationData.getOwner().keywords + "\"";
                builder.setContentTitle(title);
                inboxStyle.setBigContentTitle(title);
                for (int i = 0; i < newsItemIds.size(); i++)
                {
                    inboxStyle.addLine(getMultiNewsItemText(context, newsItemIds.get(i)));
                }
                CharSequence summaryTxt = "Open Deal Watch List";
                inboxStyle.setSummaryText(summaryTxt);

                builder.setContentTitle(title);
                builder.setContentText(summaryTxt);

                notificationManager.notify(notificationId, inboxStyle.build());
            }
            else
            {
                builder.setContentTitle(notificationData.getTitle());
                NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle(builder);
                bigTextStyle.setBigContentTitle(notificationData.getTitle());
                bigTextStyle.bigText(getContentText(notificationData));
                bigTextStyle.setSummaryText("Open RFD Thread");
                builder.setStyle(bigTextStyle);
                notificationManager.notify(notificationId, builder.build());
            }
        }
    }*/

    /**
     * Remove pending Notifications that share the same DealWatchRecord
     * @param notificationRecords
     * @return
     */
    private List<NotificationRecord> dedupe(List<NotificationRecord> notificationRecords)
    {
        HashSet<String> ownerIdSet = new HashSet<String>();
        List<NotificationRecord> blacklist = new ArrayList<NotificationRecord>();
        for (NotificationRecord notificationRecord : notificationRecords)
        {
            if (notificationRecord.getOwner() != null)
            {
                String id = String.valueOf(notificationRecord.getOwner().getId());
                if (ownerIdSet.contains(id))
                {
                    blacklist.add(notificationRecord);
                }
                else
                {
                    ownerIdSet.add(id);
                }
            }
        }

        if (!blacklist.isEmpty())
        {
            for (NotificationRecord notificationRecord : blacklist)
            {
                notificationRecords.remove(notificationRecord);
            }
        }

        return notificationRecords;
    }

    public static CharSequence getMultiNewsItemText(Context context, NotificationNewsItemRecord notificationNewsItemRecord)
    {
        // Price or percentage + News Item Title
        CharSequence output = null;
        NewsItemsDTO newsItemsDTO = new NewsItemsDTO(context);
        List<NewsItem> newsItems = newsItemsDTO.find(NewsItemSQLHelper.ID + " = " + notificationNewsItemRecord.getNewsItemId());

        if (newsItems != null && newsItems.size() > 0)
        {

            NewsItem newsItem = newsItems.get(0);
            String prefix = getPrice(newsItem.getBody());
            if (prefix == null)
            {
                prefix = getDiscount(newsItem.getBody());

                if (prefix == null)
                {
                    prefix = "?";
                }
            }

            Spannable formattedTitle = new SpannableString(" - " + newsItem.getTitle());
            formattedTitle = getKeywordSpannableString(notificationNewsItemRecord.getKeywords(), formattedTitle);
            output = TextUtils.concat(bold(prefix), formattedTitle);
        }

        return output;
    }

    private static String getPrice(String str)
    {
        Matcher m = pricePattern.matcher(str);
        while(m.find())
        {
            return m.group();
        }
        return null;
    }

    private static String getDiscount(String str)
    {
        Matcher m = NotificationService.discountPattern.matcher(str);
        while(m.find())
        {
            return m.group() + " off";
        }
        return null;
    }


    private PendingIntent getPendingIntent(NotificationRecord notificationData)
    {
        List<NotificationNewsItemRecord> notificationNewsItemRecords = notificationData.fetchNewsItemsIds();
        Intent resultIntent = null;
        if (notificationNewsItemRecords.size() == 1)
        {
            String url = notificationData.getUrl();
            resultIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            return PendingIntent.getActivity(context, 0, resultIntent, 0);
        }
        else
        {
            resultIntent = new Intent(context, DealWatchActivity.class);
            if (notificationData.getOwner() != null)
            {
                long id = notificationData.getOwner().getId();
                resultIntent.putExtra(DealWatchActivity.RECORD_ID, id);

            }
            return PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }


    }


    public static int safeLongToInt(long l)
    {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE)
        {
            l /= 2;
        }
        return (int) l;
    }

    private CharSequence getContentText(NotificationRecord notificationData)
    {
        String subTitle = notificationData.getSubTitle();
        String tmpBody = null;
        if (notificationData.getBody().length() > 280)
        {
            tmpBody = notificationData.getBody().substring(0, 280) + "...";
        }
        else
        {
            tmpBody = notificationData.getBody();
        }

        Spannable output = new SpannableString(subTitle + "\n\n" + tmpBody);

        // Bold just the title
        setStyle(output, 0, subTitle.length(), BOLD);

        // Highlight prices
        List<String> highlightTargets = new ArrayList<String>();
        Matcher m = pricePattern.matcher(output);
        while (m.find())
        {
            highlightTargets.add(m.group());
        }

        output = highlight(output, highlightTargets);


        // Highlight keywords
        output = getKeywordSpannableString(notificationData.getOwner().keywords, output);


        // Highlight percentage
        highlightTargets.clear();
        m = NotificationService.discountPattern.matcher(output);
        while (m.find())
        {
            highlightTargets.add(m.group());
        }
        output = highlight(output, highlightTargets);

        return output;
    }

    public static Spannable getKeywordSpannableString(String keywords, Spannable output)
    {

        List<String> highlightTargets = new ArrayList<String>();
        String disjunctionRegEx = Spans.getDisjunctionRegEx(keywords);
        Pattern disjunctionPattern = Pattern.compile(disjunctionRegEx, Pattern.CASE_INSENSITIVE);
        Matcher m = disjunctionPattern.matcher(output);
        while (m.find())
        {
            highlightTargets.add(m.group());
        }

        output = highlight(output, highlightTargets);

        return output;
    }

    private void setKeywordSpannableString(String keywords, Spannable output)
    {
        List<String> highlightTargets = new ArrayList<String>();

        String disjunctionRegEx = Spans.getDisjunctionRegEx(keywords);

        Pattern disjunctionPattern = Pattern.compile(disjunctionRegEx, Pattern.CASE_INSENSITIVE);
        Matcher m = disjunctionPattern.matcher(output);
        while (m.find())
        {
            highlightTargets.add(m.group());
        }

        output = highlight(output, highlightTargets);
    }

    private static CharSequence bold(String str)
    {
        SpannableString spannableStr = new SpannableString(str);
        setStyle(spannableStr, 0, str.length(), BOLD);
        return spannableStr;
    }

    public static Spannable highlight(Spannable src, List<String> targetStrs)
    {
        for (String targetStr : targetStrs)
        {
            Pattern targetStrPattern = Pattern.compile("\\Q" + targetStr + "\\E");
            Matcher matcher = targetStrPattern.matcher(src);

            while(matcher.find())
            {
                setStyle(src, matcher.start(), matcher.end(), HIGHLIGHT);
            }
        }

        return src;
    }

    public static void setStyle(Spannable sb, int startIndex, int endIndex, int styleCode)
    {
        if (endIndex > sb.length())
        {
            return;
        }

        if (styleCode == BOLD)
        {
            sb.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        else if (styleCode == HIGHLIGHT)
        {
            sb.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            sb.setSpan(new ForegroundColorSpan(Color.RED), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    /**
     * When the user dismisses a notification, delete the corresponding Notification Record
     * @return
     * @param notificationId
     */
    public PendingIntent getDeleteIntent(int notificationId)
    {
        Intent deleteIntent = new Intent(context, MarkNotificationAsReadBroadcastReceiver.class);
        deleteIntent.setAction(MarkNotificationAsReadBroadcastReceiver.NOTIFICATION_READ);
        deleteIntent.putExtra(NotificationRecord._ID, notificationId);
        return PendingIntent.getBroadcast(context, notificationId, deleteIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private PendingIntent getDeleteIntent(List<NotificationRecord> listOfNotificationRecords)
    {
        StringBuilder listOfIdsStr = new StringBuilder();
        int size = listOfNotificationRecords.size();
        for (int i = 0; i < size; i++)
        {
            NotificationRecord record = listOfNotificationRecords.get(i);
            listOfIdsStr.append(record.getId());

            if (i < size - 1)
            {
                listOfIdsStr.append(",");
            }
        }

        Intent deleteIntent = new Intent(context, MarkNotificationAsReadBroadcastReceiver.class);
        deleteIntent.setAction(MarkNotificationAsReadBroadcastReceiver.NOTIFICATION_READ);
        deleteIntent.putExtra(NotificationRecord._IDLIST, listOfIdsStr.toString());
        return PendingIntent.getBroadcast(context, 0, deleteIntent, PendingIntent.FLAG_CANCEL_CURRENT);

    }


    private PendingIntent getDeleteIntent(NotificationRecord notificationRecord)
    {
        List<NotificationRecord> list = new ArrayList<NotificationRecord>();
        list.add(notificationRecord);
        return getDeleteIntent(list);
    }


}
