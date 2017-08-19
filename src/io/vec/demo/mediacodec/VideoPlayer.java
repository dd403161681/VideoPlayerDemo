package io.vec.demo.mediacodec;


import java.io.IOException;

import io.vec.demo.logger.LocalLogger;
import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.RelativeLayout;

public class VideoPlayer extends Activity implements SurfaceHolder.Callback {

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
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		
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
	public void surfaceDestroyed(SurfaceHolder arg0) {
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
				sv = new SurfaceView(VideoPlayer.this);
				sv.getHolder().addCallback(VideoPlayer.this);
				relativeLayout.addView(sv);
			} catch (Exception e) {
				LocalLogger.LOGGER.error("Error : destroySurface runnable ", e);
			}
		}
		};
		private static final String SAMPLE = Environment.getExternalStorageDirectory() + "/video.mov";
		private class PlayerThread extends Thread {
			private Surface surface;
			private MediaPlayer mMediaPlayer = null;
		
			public PlayerThread(Surface surface) {
				this.surface = surface;
			}
		
			@Override
			public void run() {
			mMediaPlayer = new MediaPlayer();
			try {
				mMediaPlayer.setDataSource(SAMPLE);
			} catch (IOException e1) {
				LocalLogger.LOGGER.error("IOException : ", e1);
			}
	
			try {
				mMediaPlayer.prepare();
				mMediaPlayer.start();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} 
			mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
				
				@Override
				public void onCompletion(MediaPlayer mp) {
					LocalLogger.LOGGER.error("setOnCompletionListener  is complet start again");
					mMediaPlayer.release();
					mMediaPlayer = null;
					playAgain();
				}
			});
		}
		
		
			private void playAgain() {
				try {
					LocalLogger.LOGGER.info("play again IN");			
						handler.postDelayed(destroySurface, 500);
				} catch (Exception e) {
					LocalLogger.LOGGER.error("playAgain : ", e);
				}
			}
		}
}
