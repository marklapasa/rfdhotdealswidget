package net.lapasa.rfdhotdealswidget;


public class Utils
{

    private static int sdkVer = android.os.Build.VERSION.SDK_INT;
    private static int targetLayoutId = -1;


    public Utils()
    {
        if (sdkVer == 14 || sdkVer >= 19)
        {
            targetLayoutId = R.layout.news_item_no_thumbnail;
        }
        else
        {
            targetLayoutId = R.layout.news_item;
        }
    }

    public int getNewsItemLayout()
    {
        return targetLayoutId;
    }
}
