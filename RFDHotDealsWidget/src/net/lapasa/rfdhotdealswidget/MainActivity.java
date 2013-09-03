package net.lapasa.rfdhotdealswidget;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{

		super.onCreate(savedInstanceState);
		sendBroadcast(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME));
		
//		"Your widget is now ready for use! Look for it under widgets and drag it to your home screen!""
//		Intent intent = new Intent();
//		intent.setAction("net.lapasa.rfdhotdealswidget.ACTION_WIDGET_RECEIVER");
//		sendBroadcast(intent); 		

		setContentView(R.layout.main);
	}
}
