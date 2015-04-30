package net.lapasa.rfdhotdealswidget.model.entities;


import com.orm.SugarRecord;
import com.orm.dsl.Table;

/**
 * Persist the notification for a DealWatchRecordMatch. Delete when the user
 * has dealt with this record.
 */
@Table(name = "PENDING_NOTIFICATIO_RECORD")
public class PendingNotificationRecord extends SugarRecord
{
    DealWatchRecord owner;
    private String title;
    private String body;

    public PendingNotificationRecord()
    {
        // Required empty constructor
    }
}