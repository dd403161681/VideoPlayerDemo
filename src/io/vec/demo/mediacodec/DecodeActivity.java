package io.vec.demo.mediacodec;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

import android.app.Activity;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.RelativeLayout;
import io.vec.demo.logger.LocalLogger;

public class DecodeActivity extends Activity implements SurfaceHolder.Callback {
//	private static final String SAMPLE = Environment.getExternalStorageDirectory() + "/XM/xmlib" + "/167.mp4";
	private static final String SAMPLE = Environment.getExternalStorageDirectory() + "/video.mov";
	private PlayerThread mPlayer = null;
	private SurfaceView sv = null;
	private RelativeLayout relativeLayout = null;
	private Handler handler = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LocalLogger.LOGGER.info("***************MainActivity***************");
		sv = new SurfaceView(this);
		sv.getHolder().addCallback(this);
		LocalLogger.LOGGER.info("***************new object of SurfaceView created***************");
		relativeLayout = new RelativeLayout(this);
		handler = new Handler();
		relativeLayout.addView(sv);
		setContentView(relativeLayout);
	}

	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			if (mPlayer == null) {
				LocalLogger.LOGGER.info("surfaceCreated");
				LocalLogger.LOGGER.info("mPlayer Thread created.");
				mPlayer = new PlayerThread(holder.getSurface());
				mPlayer.start();
			} else{
				LocalLogger.LOGGER.info("mPlayer Thread already created");
				mPlayer.start();
			}
		} catch (Exception e) {
			LocalLogger.LOGGER.error("Error in surfaceCreated : ", e);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		LocalLogger.LOGGER.info("surfaceDestroyed");
		
		try {
			if (mPlayer != null) {
				mPlayer.interrupt();
				mPlayer = null;
				LocalLogger.LOGGER.info("mPlayer interrupt");
			} else {
				LocalLogger.LOGGER.info("mPlayer already interrupt");
			}
		} catch (Exception e) {
			LocalLogger.LOGGER.error("Error surfaceDestroyed : ", e);
		}
	}
	
	private Runnable destroySurface=new Runnable() {		
		@Override
		public void run() {
			try {
				handler.removeCallbacks(destroySurface);
				LocalLogger.LOGGER.info("Runnable destroySurface IN");
				relativeLayout.removeView(sv);
				sv = null;
				LocalLogger.LOGGER.info("***************Recreating new object of SurfaceView created***************");
				sv = new SurfaceView(DecodeActivity.this);
				sv.getHolder().addCallback(DecodeActivity.this);
				relativeLayout.addView(sv);
			} catch (Exception e) {
				LocalLogger.LOGGER.error("Error : destroySurface runnable ", e);
			}
		}
	};

	private class PlayerThread extends Thread {
		private MediaExtractor extractor;
		private MediaCodec decoder;
		private Surface surface;

		public PlayerThread(Surface surface) {
			this.surface = surface;
		}

		@Override
		public void run() {
			extractor = new MediaExtractor();
//			 FileInputStream fileInputStream = null;
//			try {
//				fileInputStream = new FileInputStream(new File(
//							SAMPLE));
//			} catch (FileNotFoundException e2) {
//				// TODO Auto-generated catch block
//				e2.printStackTrace();
//			}
//			
//			FileDescriptor fd = null;
//			try {
//				fd = fileInputStream.getFD();
//			} catch (IOException e2) {
//				// TODO Auto-generated catch block
//				e2.printStackTrace();
//			}
		        
			try {
				extractor.setDataSource(SAMPLE);
//				extractor.setDataSource(fd);
			} catch (IOException e1) {
				LocalLogger.LOGGER.error("IOException : ", e1);
			}
			
			LocalLogger.LOGGER.info("count : "+extractor.getTrackCount());
			for (int i = 0; i < extractor.getTrackCount(); i++) {
				MediaFormat format = extractor.getTrackFormat(i);
				String mime = format.getString(MediaFormat.KEY_MIME);
				if (mime.startsWith("video/")) {
					extractor.selectTrack(i);
					LocalLogger.LOGGER.info("Mime : " + mime);
					decoder = MediaCodec.createDecoderByType(mime);
					LocalLogger.LOGGER.info("new decoder object created..");
					decoder.configure(format, surface, null, 0);
					break;
				}
			}

			if (decoder == null) {
				LocalLogger.LOGGER.warn("Can't find video info!");
				return;
			}

			decoder.start();

			ByteBuffer[] inputBuffers = decoder.getInputBuffers();
			ByteBuffer[] outputBuffers = decoder.getOutputBuffers();
			BufferInfo info = new BufferInfo();
			boolean isEOS = false;
			long startMs = System.currentTimeMillis();

			while (!Thread.interrupted()) {
				if (!isEOS) {
					int inIndex = decoder.dequeueInputBuffer(10000);
					if (inIndex >= 0) {
						ByteBuffer buffer = inputBuffers[inIndex];
						int sampleSize = extractor.readSampleData(buffer, 0);
						if (sampleSize < 0) {
							// We shouldn't stop the playback at this point,
							// just pass the EOS
							// flag to decoder, we will get it again from the
							// dequeueOutputBuffer
							LocalLogger.LOGGER.debug("InputBuffer BUFFER_FLAG_END_OF_STREAM");
							decoder.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
							isEOS = true;
						} else {
							decoder.queueInputBuffer(inIndex, 0, sampleSize, extractor.getSampleTime(), 0);
							extractor.advance();
						}
					}
				}

				int outIndex = decoder.dequeueOutputBuffer(info, 10000);
				switch (outIndex) {
				case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
					LocalLogger.LOGGER.debug("INFO_OUTPUT_BUFFERS_CHANGED");
					outputBuffers = decoder.getOutputBuffers();
					break;
				case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
					LocalLogger.LOGGER.debug("New format " + decoder.getOutputFormat());
					break;
				case MediaCodec.INFO_TRY_AGAIN_LATER:
					LocalLogger.LOGGER.debug("dequeueOutputBuffer timed out!");
					break;
				default:
					ByteBuffer buffer = outputBuffers[outIndex];
//					LocalLogger.LOGGER.warn("We can't use this buffer but render it due to the API limit, " + buffer);

					// We use a very simple clock to keep the video FPS, or the
					// video
					// playback will be too fast
					while (info.presentationTimeUs / 1000 > System.currentTimeMillis() - startMs) {
						try {
							sleep(10);
						} catch (InterruptedException e) {
							LocalLogger.LOGGER.error("InterruptedException : ", e);
							break;
						}
					}
					decoder.releaseOutputBuffer(outIndex, true);
					break;
				}

				// All decoded frames have been rendered, we can stop playing
				// now
				if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
					LocalLogger.LOGGER.debug("OutputBuffer BUFFER_FLAG_END_OF_STREAM");
					break;
				}
			}

			LocalLogger.LOGGER.info("Disposing decoder and extracter....");
			decoder.stop();
			decoder.release();
			extractor.release();
			decoder = null;
			extractor = null;
			LocalLogger.LOGGER.info("decoder stop, decoder release and extractor release ");
			playAgain();
		}

		private void playAgain() {
			try {
				LocalLogger.LOGGER.info("play again IN");
//				if (mPlayer != null) {
//					mPlayer.interrupt();
//					mPlayer = null;
//					LocalLogger.LOGGER.info("Dispose thread..");
					
					handler.postDelayed(destroySurface, 500);

//				} else {
//					LocalLogger.LOGGER.warn("mPlayer is null");
//				}
			} catch (Exception e) {
				LocalLogger.LOGGER.error("playAgain : ", e);
			}
		}
	}
}