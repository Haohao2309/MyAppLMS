package com.example.myapplms.ui.student.learning;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplms.R;
import com.example.myapplms.data.remote.dto.request.SyncVideoRequest;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

public class VideoFragment extends Fragment {

    private YouTubePlayerView youTubePlayerView;
    private View viewCenterClick;
    private ImageView btnPlayPause;
    private TextView tvCurrentTime;
    private TextView tvTotalTime;
    private SeekBar seekbarVideo;
    private TextView tvLessonTitle;
    private YouTubePlayer mYouTubePlayer;

    private int courseId;
    private String lessonId;
    private String videoId;
    private String lessonTitle;

    private float currentSeconds = 0f;
    private float totalSeconds = 0f;
    private boolean isPlaying = false;

    private Handler syncHandler = new Handler(Looper.getMainLooper());
    private Runnable syncRunnable;

    public static VideoFragment newInstance(int courseId, String lessonId, String videoId, String lessonTitle) {
        VideoFragment fragment = new VideoFragment();
        Bundle args = new Bundle();
        args.putInt("COURSE_ID", courseId);
        args.putString("LESSON_ID", lessonId);
        args.putString("VIDEO_ID", videoId);
        args.putString("LESSON_TITLE", lessonTitle);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            courseId = getArguments().getInt("COURSE_ID");
            lessonId = getArguments().getString("LESSON_ID");
            videoId = getArguments().getString("VIDEO_ID");
            lessonTitle = getArguments().getString("LESSON_TITLE");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_video, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        youTubePlayerView = view.findViewById(R.id.youtube_player_view);
        viewCenterClick = view.findViewById(R.id.view_center_click);
        btnPlayPause = view.findViewById(R.id.btn_play_pause);
        tvCurrentTime = view.findViewById(R.id.tv_current_time);
        tvTotalTime = view.findViewById(R.id.tv_total_time);
        seekbarVideo = view.findViewById(R.id.seekbar_video);
        tvLessonTitle = view.findViewById(R.id.tv_lesson_title);

        if (lessonTitle != null && tvLessonTitle != null) {
            tvLessonTitle.setText(lessonTitle);
        }

        // Ẩn tiêu đề nếu đang xoay ngang để Fullscreen tuyệt đối
        if (getResources().getConfiguration().orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
            if (tvLessonTitle != null) tvLessonTitle.setVisibility(View.GONE);
        }

        getLifecycle().addObserver(youTubePlayerView);
        initializePlayer();
        setupSyncTimer();
    }

    private void initializePlayer() {
        IFramePlayerOptions options = new IFramePlayerOptions.Builder()
                .controls(0)
                .origin("https://google.com")
                .build();

        youTubePlayerView.initialize(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                mYouTubePlayer = youTubePlayer;
                setupCustomUi();
                youTubePlayer.loadVideo(videoId, 0f);
            }

            @Override
            public void onStateChange(@NonNull YouTubePlayer youTubePlayer, @NonNull PlayerConstants.PlayerState state) {
                isPlaying = (state == PlayerConstants.PlayerState.PLAYING);
                if (isPlaying) {
                    if (btnPlayPause != null) btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
                } else {
                    if (btnPlayPause != null) btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
                }

                if (state == PlayerConstants.PlayerState.ENDED) {
                    LearningActivity parentActivity = (LearningActivity) getActivity();
                    if (parentActivity != null && totalSeconds > 0) {
                        SyncVideoRequest request = new SyncVideoRequest((int) totalSeconds, (int) totalSeconds);
                        parentActivity.getViewModel().syncVideoProgress(courseId, lessonId, request);
                        new Handler(Looper.getMainLooper()).postDelayed(() -> parentActivity.loadProgress(), 500);
                    }
                }
            }

            @Override
            public void onCurrentSecond(@NonNull YouTubePlayer youTubePlayer, float second) {
                currentSeconds = second;
                if (tvCurrentTime != null) tvCurrentTime.setText(formatTime(second));
                if (seekbarVideo != null) seekbarVideo.setProgress((int) second);
            }

            @Override
            public void onVideoDuration(@NonNull YouTubePlayer youTubePlayer, float duration) {
                totalSeconds = duration;
                if (tvTotalTime != null) tvTotalTime.setText(formatTime(duration));
                if (seekbarVideo != null) seekbarVideo.setMax((int) duration);
            }
        }, options);
    }

    private void setupCustomUi() {
        View.OnClickListener togglePlay = v -> {
            if (mYouTubePlayer != null) {
                if (isPlaying) mYouTubePlayer.pause();
                else mYouTubePlayer.play();
            }
        };
        if(viewCenterClick != null) viewCenterClick.setOnClickListener(togglePlay);
        if(btnPlayPause != null) btnPlayPause.setOnClickListener(togglePlay);

        ImageView btnFullscreen = getView() != null ? getView().findViewById(R.id.btn_fullscreen) : null;
        if (btnFullscreen != null) {
            btnFullscreen.setOnClickListener(v -> {
                if (getActivity() instanceof LearningActivity) {
                    ((LearningActivity) getActivity()).toggleFullscreen();
                }
            });
        }

        if(seekbarVideo != null) {
            seekbarVideo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser && mYouTubePlayer != null) {
                        mYouTubePlayer.seekTo(progress);
                    }
                }
                @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        }
    }

    private String formatTime(float timeInSeconds) {
        int minutes = (int) (timeInSeconds / 60);
        int seconds = (int) (timeInSeconds % 60);
        return String.format("%d:%02d", minutes, seconds);
    }

    private void setupSyncTimer() {
        syncRunnable = new Runnable() {
            @Override
            public void run() {
                if (isPlaying && totalSeconds > 0) {
                    LearningActivity parentActivity = (LearningActivity) getActivity();
                    if (parentActivity != null) {
                        SyncVideoRequest request = new SyncVideoRequest((int) currentSeconds, (int) totalSeconds);
                        parentActivity.getViewModel().syncVideoProgress(courseId, lessonId, request);
                        new Handler(Looper.getMainLooper()).postDelayed(() -> parentActivity.loadProgress(), 500);
                    }
                }
                syncHandler.postDelayed(this, 10000);
            }
        };
        syncHandler.postDelayed(syncRunnable, 10000);
    }

    public void setFullscreenState(boolean isFullscreen) {
        if (tvLessonTitle != null) {
            tvLessonTitle.setVisibility(isFullscreen ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        syncHandler.removeCallbacks(syncRunnable);
        if (youTubePlayerView != null) {
            youTubePlayerView.release();
        }
    }
}