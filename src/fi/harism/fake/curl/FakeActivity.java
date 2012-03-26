/*
   Copyright 2012 Harri Smatt & YOUR NAME HERE

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package fi.harism.fake.curl;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import android.app.Activity;
import android.os.Bundle;

public class FakeActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		FakeView fakeView = new FakeView(this);
		setContentView(fakeView);

		try {
			byte[] topBm = loadRawResource(R.raw.top);
			byte[] bottomBm = loadRawResource(R.raw.bottom);
			fakeView.setBitmaps(topBm, bottomBm);
		} catch (Exception ex) {
			// TODO: Ooops.
		}
	}

	private byte[] loadRawResource(int resourceId) throws Exception {
		InputStream is = getResources().openRawResource(resourceId);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		int len;
		while ((len = is.read(buf)) != -1) {
			baos.write(buf, 0, len);
		}
		return baos.toByteArray();
	}
}