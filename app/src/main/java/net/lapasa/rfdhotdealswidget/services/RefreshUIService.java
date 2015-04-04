/*******************************************************************************
 * Copyright 2014 Mark Lapasa
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package net.lapasa.rfdhotdealswidget.services;

import java.text.SimpleDateFormat;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class RefreshUIService extends RemoteViewsService
{
	private static final String TAG = RefreshUIService.class.getName();
	public static SimpleDateFormat sdfFull = new SimpleDateFormat("MMM d h:mm aa");

	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent)
	{
		return new DealsRemoteViewsFactory(this.getApplicationContext(), intent);
	}
}
