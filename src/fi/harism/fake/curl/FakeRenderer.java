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

import java.nio.ByteBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;

public final class FakeRenderer implements GLSurfaceView.Renderer {

	private static final int TEXTURE_COUNT = 2;

	private Context mContext;
	private float mCurlX, mCurlY, mCurlDirX, mCurlDirY;
	private int mShaderProgram = -1;
	private final byte[][] mTextureData = new byte[TEXTURE_COUNT][];

	private final int[] mTextureIds = new int[TEXTURE_COUNT];
	private boolean mTextureLoadFlag = false;
	private ByteBuffer mVertexBuffer;
	private int mWidth, mHeight;

	public FakeRenderer(Context ctx) {
		mContext = ctx;

		byte[] coords = { -1, 1, -1, -1, 1, 1, 1, -1 };
		mVertexBuffer = ByteBuffer.allocateDirect(2 * 4);
		mVertexBuffer.position(0);
		mVertexBuffer.put(coords);
	}

	private int loadProgram(String vs, String fs) {
		int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vs);
		int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fs);
		int program = GLES20.glCreateProgram();
		if (program != 0) {
			GLES20.glAttachShader(program, vertexShader);
			GLES20.glAttachShader(program, fragmentShader);
			GLES20.glLinkProgram(program);
			int[] linkStatus = new int[1];
			GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
			if (linkStatus[0] != GLES20.GL_TRUE) {
				String error = GLES20.glGetProgramInfoLog(program);
				GLES20.glDeleteProgram(program);
				throw new RuntimeException(error);
			}
		}
		return program;
	}

	private int loadShader(int shaderType, String source) {
		int shader = GLES20.glCreateShader(shaderType);
		if (shader != 0) {
			GLES20.glShaderSource(shader, source);
			GLES20.glCompileShader(shader);
			int[] compiled = new int[1];
			GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
			if (compiled[0] == 0) {
				String error = GLES20.glGetShaderInfoLog(shader);
				GLES20.glDeleteShader(shader);
				throw new RuntimeException(error);
			}
		}
		return shader;
	}

	@Override
	public void onDrawFrame(GL10 unused) {
		if (mTextureLoadFlag) {
			for (int i = 0; i < TEXTURE_COUNT; ++i) {
				if (mTextureData[i] != null) {
					Bitmap bitmap = BitmapFactory.decodeByteArray(
							mTextureData[i], 0, mTextureData[i].length);
					GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureIds[i]);
					GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
					bitmap.recycle();
				}
			}
			mTextureLoadFlag = false;
		}

		GLES20.glDisable(GLES20.GL_CULL_FACE);
		GLES20.glDisable(GLES20.GL_DEPTH_TEST);

		GLES20.glUseProgram(mShaderProgram);
		int sTextureTop = GLES20.glGetUniformLocation(mShaderProgram,
				"sTextureTop");
		int sTextureBottom = GLES20.glGetUniformLocation(mShaderProgram,
				"sTextureBottom");
		int uCurlPos = GLES20.glGetUniformLocation(mShaderProgram, "uCurlPos");
		int uCurlDir = GLES20.glGetUniformLocation(mShaderProgram, "uCurlDir");
		int uAspectRatio = GLES20.glGetUniformLocation(mShaderProgram,
				"uAspectRatio");
		int uAspectRatioInv = GLES20.glGetUniformLocation(mShaderProgram,
				"uAspectRatioInv");

		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureIds[0]);
		GLES20.glUniform1i(sTextureTop, 0);
		GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureIds[1]);
		GLES20.glUniform1i(sTextureBottom, 1);

		GLES20.glUniform2f(uCurlPos, mCurlX, mCurlY);
		GLES20.glUniform2f(uCurlDir, mCurlDirX, mCurlDirY);
		GLES20.glUniform2f(uAspectRatio, mWidth, mHeight);
		GLES20.glUniform2f(uAspectRatioInv, 1f / mWidth, 1f / mHeight);

		mVertexBuffer.position(0);
		int aPosition = GLES20.glGetAttribLocation(mShaderProgram, "aPosition");
		GLES20.glVertexAttribPointer(aPosition, 2, GLES20.GL_BYTE, false, 2,
				mVertexBuffer);
		GLES20.glEnableVertexAttribArray(aPosition);

		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
	}

	@Override
	public void onSurfaceChanged(GL10 unused, int width, int height) {
		GLES20.glViewport(0, 0, width, height);
		mWidth = width;
		mHeight = height;
	}

	@Override
	public void onSurfaceCreated(GL10 unused, EGLConfig config) {
		GLES20.glGenTextures(TEXTURE_COUNT, mTextureIds, 0);
		for (int i = 0; i < TEXTURE_COUNT; ++i) {
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureIds[i]);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
					GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
					GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
					GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
					GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
		}

		mShaderProgram = loadProgram(
				mContext.getString(R.string.shader_transition_vs),
				mContext.getString(R.string.shader_transition_fs));
	}

	public void setCurl(float x, float y, float dx, float dy) {
		mCurlX = x;
		mCurlY = y;
		mCurlDirX = dx;
		mCurlDirY = dy;
	}

	public void setTextures(byte[] top, byte[] bottom) {
		mTextureData[0] = top;
		mTextureData[1] = bottom;
		mTextureLoadFlag = true;
	}

}