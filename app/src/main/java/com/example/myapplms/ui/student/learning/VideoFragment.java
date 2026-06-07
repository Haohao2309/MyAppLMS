package com.example.myapplms.ui.student.learning;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private int courseId;
    private String lessonId;
    private String videoId;

    private float currentSeconds = 0f;
    private float totalSeconds = 0f;
    private boolean isPlaying = false;

    private Handler syncHandler = new Handler(Looper.getMainLooper());
    private Runnable syncRunnable;

    public static VideoFragment newInstance(int courseId, String lessonId, String videoId) {
        VideoFragment fragment = new VideoFragment();
        Bundle args = new Bundle();
        args.putInt("COURSE_ID", courseId);
        args.putString("LESSON_ID", lessonId);
        args.putString("VIDEO_ID", videoId);
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
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video, container, false);
        youTubePlayerView = view.findViewById(R.id.youtube_player_view);
        getLifecycle().addObserver(youTubePlayerView);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializePlayer();
        setupSyncTimer();
    }

    private void initializePlayer() {
        IFramePlayerOptions options = new IFramePlayerOptions.Builder()
                .controls(1)
                .origin("https://google.com")
                .build();

        youTubePlayerView.initialize(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                youTubePlayer.loadVideo(videoId, 0f);
            }

            @Override
            public void onStateChange(@NonNull YouTubePlayer youTubePlayer, @NonNull PlayerConstants.PlayerState state) {
                isPlaying = (state == PlayerConstants.PlayerState.PLAYING);

                // Khi xem xong 100% -> Ép đồng bộ cuối cùng và hiện tích xanh
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
            }

            @Override
            public void onVideoDuration(@NonNull YouTubePlayer youTubePlayer, float duration) {
                totalSeconds = duration;
            }
        }, options);
    }

    private void setupSyncTimer() {
        syncRunnable = new Runnable() {
            @Override
            public void run() {
                if (isPlaying && totalSeconds > 0) {
                    Log.d("VideoSync", "Đang xem: " + currentSeconds + "/" + totalSeconds + "s");

                    LearningActivity parentActivity = (LearningActivity) getActivity();
                    if (parentActivity != null) {
                        // 1. Gửi api đồng bộ giây hiện tại lên Server
                        SyncVideoRequest request = new SyncVideoRequest((int) currentSeconds, (int) totalSeconds);
                        parentActivity.getViewModel().syncVideoProgress(courseId, lessonId, request);

                        // 2. Tải lại tiến độ để % trên Toolbar nhảy số liên tục khi đang xem
                        new Handler(Looper.getMainLooper()).postDelayed(() -> parentActivity.loadProgress(), 500);
                    }
                }
                syncHandler.postDelayed(this, 10000);
            }
        };
        syncHandler.postDelayed(syncRunnable, 10000); // 10 giây chạy 1 lần
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