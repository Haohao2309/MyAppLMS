package com.example.myapplms.ui.explore;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.myapplms.Course;
import com.example.myapplms.CourseAdapter;
import com.example.myapplms.R;
import com.example.myapplms.databinding.FragmentExploreListCourseBinding;
import com.example.myapplms.ui.base.BaseFragment;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale; // Import Locale

public class ExploreFragment extends BaseFragment<FragmentExploreListCourseBinding> {

    private CourseAdapter adapter;
    private List<Course> courseList;

    @NonNull
    @Override
    protected FragmentExploreListCourseBinding inflateBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentExploreListCourseBinding.inflate(inflater, container, false);
    }

    @Override
    protected void setupViews() {
        courseList = new ArrayList<>();
        adapter = new CourseAdapter(courseList);
        getBinding().rvCourses.setLayoutManager(new LinearLayoutManager(getContext()));
        getBinding().rvCourses.setAdapter(adapter);
        loadSampleData();
    }

    private void loadSampleData() {
        courseList.add(new Course("E-Learning Platform Overview", "by Dr. Sarah Chen", "4.9 (1.2k)", "125k students", "18 lessons", "2h 30m", "FREE", "Development", "Beginner", R.drawable.ic_launcher_background));
        courseList.add(new Course("Advanced Android Development", "by John Doe", "4.8 (850)", "50k students", "24 lessons", "5h 45m", "$49.99", "Development", "Advanced", R.drawable.ic_launcher_background));
        courseList.add(new Course("UI/UX Design Fundamentals", "by Jane Smith", "4.7 (2.1k)", "200k students", "12 lessons", "3h 15m", "FREE", "Design", "Beginner", R.drawable.ic_launcher_background));
        adapter.notifyDataSetChanged();
        
        // Hiển thị số lượng khóa học, sử dụng Locale.getDefault()
        getBinding().tvCourseCount.setText(String.format(Locale.getDefault(), "%d courses found", courseList.size()));
    }
}
