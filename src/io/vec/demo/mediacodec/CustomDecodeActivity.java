package io.vec.demo.mediacodec;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;

public class CustomDecodeActivity extends Activity {
	private static final String SAMPLE = Environment.getExternalStorageDirectory() + "/XM/xmlib" + "/554.mp4";
//	private PlayerThread mPlayer = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
//		ExtractMpegFramesWrapper wrapper = new ExtractMpegFramesWrapper(obj);
//        Thread th = new Thread(wrapper, "codec test");
//        th.start();
//        th.join();
//        if (wrapper.mThrowable != null) {
//            throw wrapper.mThrowable;
//        }

	}
	
	
	private static class ExtractMpegFramesWrapper implements Runnable {
        private Throwable mThrowable;
        private ExtractMpegFramesTest mTest;

        private ExtractMpegFramesWrapper(ExtractMpegFramesTest test) {
            mTest = test;
        }

        @Override
        public void run() {
            try {
//                mTest.extractMpegFrames();
            } catch (Throwable th) {
                mThrowable = th;
            }
        }

        /** Entry point. */
        public static void runTest(ExtractMpegFramesTest obj) throws Throwable {
            ExtractMpegFramesWrapper wrapper = new ExtractMpegFramesWrapper(obj);
            Thread th = new Thread(wrapper, "codec test");
            th.start();
            th.join();
            if (wrapper.mThrowable != null) {
                throw wrapper.mThrowable;
            }
        }
    }

	protected void onDestroy() {
		super.onDestroy();
	}

}