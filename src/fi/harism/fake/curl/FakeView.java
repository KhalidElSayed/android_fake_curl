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

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

public class FakeView extends GLSurfaceView {

	private FakeRenderer mHostRenderer;
	private float mTouchDiffX, mTouchDiffY;
	private float mTouchStartX, mTouchStartY;

	public FakeView(Context ctx) {
		super(ctx);
		mHostRenderer = new FakeRenderer(ctx);
		setEGLContextClientVersion(2);
		setRenderer(mHostRenderer);
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
	}

	@Override
	public boolean onTouchEvent(MotionEvent me) {
		switch (me.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mTouchStartX = me.getX();
			mTouchStartY = me.getY();
			mTouchDiffX = mTouchDiffY = 0;
			updateTouchPos();
			requestRender();
			return true;
		case MotionEvent.ACTION_MOVE:
			mTouchDiffX = me.getX() - mTouchStartX;
			mTouchDiffY = me.getY() - mTouchStartY;
			updateTouchPos();
			requestRender();
			return true;
		case MotionEvent.ACTION_UP:
			// TODO: Animation goes here.
			return true;
		}
		return false;
	}

	public void setBitmaps(byte[] top, byte[] bottom) {
		mHostRenderer.setTextures(top, bottom);
	}

	private void updateTouchPos() {
		float touchX = mTouchDiffX / getWidth();
		float touchY = -mTouchDiffY / getHeight();
		if (mTouchStartX > getWidth() / 2) {
			touchX += 1.0f;
			if (mTouchStartY < getHeight() / 2) {
				touchY += 1.0;
			}
		} else {
			touchY = (1.0f + touchY) * 0.5f;
		}

		float curlDirX = touchY - 0.5f;
		float curlDirY = -touchX;
		float vecLen = (1.0f - touchY) / curlDirY;
		if (touchX + curlDirX * vecLen < 0.0f) {
			curlDirX = -touchX;
			curlDirY = 1.0f - touchY;
		} else {
			vecLen = -touchY / curlDirY;
			if (touchX + curlDirX * vecLen < 0.0f) {
				curlDirX = touchX;
				curlDirY = touchY;
			}
		}

		mHostRenderer.setCurl(touchX, touchY, curlDirX, curlDirY);
	}

}