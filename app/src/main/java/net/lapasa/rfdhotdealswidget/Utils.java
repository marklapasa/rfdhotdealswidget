package net.lapasa.rfdhotdealswidget;


public class Utils
{
    private static int sdkVer = android.os.Build.VERSION.SDK_INT;

    public static int getNewsItemLayout()
    {
        int targetLayoutId;
        if (sdkVer == 14 || sdkVer >= 19)
        {
            targetLayoutId = R.layout.news_item_no_thumbnail;
        }
        else
        {
            targetLayoutId = R.layout.news_item;
        }
        return targetLayoutId;
    }
}
