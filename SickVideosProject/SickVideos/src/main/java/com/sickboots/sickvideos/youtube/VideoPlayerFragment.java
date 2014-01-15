package com.sickboots.sickvideos.youtube;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.sickboots.sickvideos.misc.AppUtils;
import com.sickboots.sickvideos.misc.Auth;
import com.sickboots.sickvideos.misc.Debug;
import com.sickboots.sickvideos.misc.Preferences;
import com.sickboots.sickvideos.misc.SoundManager;
import com.sickboots.sickvideos.misc.Utils;

import java.util.Timer;
import java.util.TimerTask;

public final class VideoPlayerFragment extends YouTubePlayerFragment {

  public interface TimeRemainingListener {
    // call this on the main thread
    public void setTimeRemainingText(final String timeRemaining);

    public void setSeekFlashText(final String seekFlash);
  }

  public interface VideoFragmentListener {
    public void onFullScreen(boolean fullscreen);
  }

  private YouTubePlayer mPlayer;
  private String mVideoId;
  private String mTitle;
  private boolean mMutedForAd = false;
  private Timer mTimer;
  private TimeRemainingListener mTimeRemainingListener;
  private boolean mFullscreen = false;
  private VideoFragmentListener mFragmentListener;
  private boolean mInitializingPlayer = false;
  private SoundManager mSoundManager;

  // added for debugging, remove this shit once we know it's solid
  private String mLastTimeString;

  public static VideoPlayerFragment newInstance() {
    return new VideoPlayerFragment();
  }

  public void setVideoFragmentListener(VideoFragmentListener l) {
    mFragmentListener = l;
  }

  public void closingPlayer() {
    // pause immediately on click for better UX
    pause();

    // action bar gets hidden by the fullscreen mode, need to show it again when closing player
    Activity activity = getActivity();
    if (activity != null) {
      activity.getActionBar().show();
      activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }

    destroyPlayer();
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);

    mSoundManager = new SoundManager(getActivity());
  }

  @Override
  public void onPause() {
    super.onPause();
    mSoundManager.restoreMuteIfNeeded();
  }

  @Override
  public void onDestroy() {
    destroyPlayer();

    mSoundManager.restoreMuteIfNeeded();

    super.onDestroy();
  }

  private void destroyPlayer() {
    stopElapsedTimer();

    // fixes case where you start a video, stop it, then replay the same video.  if not reset, it would think the video was already running and ignore it.
    mVideoId = null;

    if (mPlayer != null) {
      mPlayer.release();
      mPlayer = null;
    }
  }

  public String getTitle() {
    return mTitle;
  }

  public void setVideo(String videoId, String title) {
    if (videoId != null && !videoId.equals(mVideoId)) {

      mVideoId = videoId;
      mTitle = title;

      if (mPlayer != null)
        mPlayer.loadVideo(mVideoId);
      else
        initializePlayer();
    }
  }

  public void pause() {
    if (mPlayer == null) {
      Debug.log("mPlayer is null inside: " + Debug.currentMethod());
      return;
    }

    mPlayer.pause();
  }

  public void mute(boolean muteState) {
    mSoundManager.mute(muteState);
  }

  public boolean isMute() {
    return mSoundManager.isMute();
  }

  public void setFullscreen(boolean state) {
    if (mPlayer == null) {
      Debug.log("mPlayer is null inside: " + Debug.currentMethod());
      return;
    }

    mPlayer.setFullscreen(state);
  }

  public void seekRelativeSeconds(int seconds) {
    if (mPlayer == null) {
      Debug.log("mPlayer is null inside: " + Debug.currentMethod());
      return;
    }

    mPlayer.seekRelativeMillis(seconds * 1000);
  }

  public void setTimeRemainingListener(TimeRemainingListener listener) {
    mTimeRemainingListener = listener;
  }

  private void setupFullscreenListener() {
    if (mPlayer == null) {
      Debug.log("mPlayer is null inside: " + Debug.currentMethod());
      return;
    }

    mPlayer.setOnFullscreenListener(new YouTubePlayer.OnFullscreenListener() {
      public void onFullscreen(boolean isFullscreen) {
        Debug.log("setOnFullscreenListener: " + (isFullscreen ? "yes" : "no"));
        VideoPlayerFragment.this.mFullscreen = isFullscreen;

        mFragmentListener.onFullScreen(isFullscreen);
      }

    });
  }

  public int getCurrentTimeMillis() {
    if (mPlayer == null) {
      Debug.log("mPlayer is null inside: " + Debug.currentMethod());
      return 0;
    }

    return mPlayer.getCurrentTimeMillis();
  }

  public int getDurationMillis() {
    if (mPlayer == null) {
      Debug.log("mPlayer is null inside: " + Debug.currentMethod());
      return 0;
    }

    return mPlayer.getDurationMillis();
  }

  public void seekToMillis(int i) {
    if (mPlayer == null) {
      Debug.log("mPlayer is null inside: " + Debug.currentMethod());
      return;
    }

    mPlayer.seekToMillis(i);
  }

  private void setupStateChangeListener() {
    if (mPlayer == null) {
      Debug.log("mPlayer is null inside: " + Debug.currentMethod());
      return;
    }

    mPlayer.setPlayerStateChangeListener(new YouTubePlayer.PlayerStateChangeListener() {
      @Override
      public void onLoading() {

      }

      @Override
      public void onLoaded(String s) {

      }

      @Override
      public void onAdStarted() {
        if (!isMute()) {
          boolean muteAds = AppUtils.preferences(VideoPlayerFragment.this.getActivity()).getBoolean(Preferences.MUTE_ADS, false);
          if (muteAds) {
            mMutedForAd = true;
            mute(true);
          }
        }
      }

      @Override
      public void onVideoStarted() {
        if (mMutedForAd) {
          mMutedForAd = false;
          mute(false);
        }
      }

      @Override
      public void onVideoEnded() {
        boolean autorepeat = AppUtils.preferences(getActivity()).getBoolean(Preferences.REPEAT_VIDEO, false);
        if (autorepeat) {
          if (mPlayer != null)
            mPlayer.play();  // back to the start
          else
            Debug.log("repeat canceled, player is null");
        }
      }

      @Override
      public void onError(YouTubePlayer.ErrorReason errorReason) {

      }
    });
  }

  private void setupPlaybackEventListener() {
    if (mPlayer == null) {
      Debug.log("mPlayer is null inside: " + Debug.currentMethod());
      return;
    }

    mPlayer.setPlaybackEventListener(new YouTubePlayer.PlaybackEventListener() {
      @Override
      public void onPlaying() {
        startElapsedTimer();
      }

      @Override
      public void onPaused() {
        stopElapsedTimer();
      }

      @Override
      public void onStopped() {
        stopElapsedTimer();
      }

      @Override
      public void onBuffering(boolean b) {
//        Utils.log("buffering: " + ((b) ? "yes" : "no"));
      }

      @Override
      public void onSeekTo(int newPositionMillis) {
//        Utils.log("seeking: " + newPositionMillis / 1000 + " seconds");

        final String seekString = Utils.millisecondsToDuration(newPositionMillis);
        AppUtils.instance(VideoPlayerFragment.this.getActivity()).runOnMainThread(new Runnable() {
          @Override
          public void run() {
            // we're on the main thread...
            mTimeRemainingListener.setSeekFlashText(seekString);
          }
        });

      }
    });

  }

  private void stopElapsedTimer() {
    if (mTimer != null) {
      mTimer.cancel();
      mTimer = null;
    }
  }

  private void startElapsedTimer() {
    if (mTimer == null) {
      mTimer = new Timer();
      TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
          if (mTimeRemainingListener != null) {
            long millis = getCurrentTimeMillis();

            final String timeString = Utils.millisecondsToDuration(millis);

            // added for debugging, remove this shit once we know it's solid
            mLastTimeString = (mLastTimeString == null) ? "" : mLastTimeString;
            if (timeString.equals(mLastTimeString))
              Debug.log("equal to last");
            else {
              mLastTimeString = timeString;
            }

            // activity can go null on configuration change
            Activity activity = VideoPlayerFragment.this.getActivity();
            if (activity != null) {
              AppUtils.instance(activity).runOnMainThread(new Runnable() {
                @Override
                public void run() {
                  // we're on the main thread...
                  mTimeRemainingListener.setTimeRemainingText(timeString);
                }
              });
            }
          }
        }
      };

      mTimer.schedule(timerTask, 0, 1000);
    }
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);

//    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//      if (mPlayer != null)
//        mPlayer.setFullscreen(true);
//    }
//    if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
//      if (mPlayer != null)
//        mPlayer.setFullscreen(false);
//    }
  }

  private void initializePlayer() {
    if (!mInitializingPlayer) {
      mInitializingPlayer = true;

      // creates a player async, not sure why
      initialize(Auth.devKey(), new YouTubePlayer.OnInitializedListener() {
        @Override
        public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean restored) {
          mPlayer = player;

          player.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);
          player.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);

          // this handles landscape perfectly, nothing more to do
          int controlFlags = mPlayer.getFullscreenControlFlags();
          controlFlags |= YouTubePlayer.FULLSCREEN_FLAG_ALWAYS_FULLSCREEN_IN_LANDSCAPE;
          mPlayer.setFullscreenControlFlags(controlFlags);

          setupFullscreenListener();
          setupStateChangeListener();
          setupPlaybackEventListener();

          if (!restored && mVideoId != null) {
            player.loadVideo(mVideoId);
          }

          mInitializingPlayer = false;
        }

        @Override
        public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult result) {
          mPlayer = null;
          mInitializingPlayer = false;

          Debug.log("initializePlayer: failed" + result);
        }
      });
    }
  }

}