package com.example.myapplms.ui.teacher.grading;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplms.R;
import com.example.myapplms.data.RetrofitClient;
import com.example.myapplms.data.remote.api.LmsApiService;
import com.example.myapplms.data.remote.dto.response.GradingListResponse;
import com.example.myapplms.data.repository.GradingRepository;
import com.example.myapplms.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class StudentSubmissionListFragment extends Fragment {

    private static final String ARG_COURSE_ID = "course_id";

    private int courseId;
    private GradingViewModel viewModel;
    private RecyclerView recyclerView;
    private SubmissionListAdapter adapter;
    private TextView tvTotal, tvPending, tvGraded;
    private View emptyState;

    public static StudentSubmissionListFragment newInstance(int courseId) {
        StudentSubmissionListFragment f = new StudentSubmissionListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COURSE_ID, courseId);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) courseId = getArguments().getInt(ARG_COURSE_ID, -1);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_student_submission_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvTotal   = view.findViewById(R.id.tv_total_count);
        tvPending = view.findViewById(R.id.tv_pending_count);
        tvGraded  = view.findViewById(R.id.tv_graded_count);
        recyclerView = view.findViewById(R.id.rv_submissions);
        emptyState   = view.findViewById(R.id.empty_state);

        setupViewModel();
        setupRecyclerView();
        observeData();

        viewModel.loadGradingList(courseId);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) viewModel.loadGradingList(courseId);
    }

    private void setupViewModel() {
        SessionManager sessionManager = new SessionManager(requireContext());
        LmsApiService apiService = RetrofitClient.getInstance(sessionManager).create(LmsApiService.class);
        GradingRepository repository = new GradingRepository(apiService);
        viewModel = new ViewModelProvider(this, new GradingViewModelFactory(repository))
                .get(GradingViewModel.class);
    }

    private void setupRecyclerView() {
        adapter = new SubmissionListAdapter(new ArrayList<>(), item ->
                GradingActivity.start(requireContext(), item, courseId));
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private void observeData() {
        viewModel.gradingList.observe(getViewLifecycleOwner(), resource -> {
            switch (resource.status) {
                case LOADING:
                    break;
                case SUCCESS:
                    GradingListResponse response = resource.data;
                    List<GradingListResponse.StudentGradingItem> students =
                            response != null && response.getStudents() != null
                                    ? response.getStudents() : new ArrayList<>();

                    // Cập nhật stats từ response.getStats()
                    if (response != null && response.getStats() != null) {
                        GradingListResponse.GradingStats stats = response.getStats();
                        tvTotal.setText(String.valueOf(stats.getTotalStudents()));
                        tvPending.setText(String.valueOf(stats.getUngradedCount()));
                        tvGraded.setText(String.valueOf(stats.getGradedCount()));
                    }

                    adapter.updateData(students);
                    emptyState.setVisibility(students.isEmpty() ? View.VISIBLE : View.GONE);
                    recyclerView.setVisibility(students.isEmpty() ? View.GONE : View.VISIBLE);
                    break;
                case ERROR:
                    break;
            }
        });
    }
}