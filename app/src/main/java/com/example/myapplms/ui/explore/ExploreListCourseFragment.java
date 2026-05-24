//package com.example.myapplms.ui.explore;
//
//import android.view.LayoutInflater;
//import android.view.ViewGroup;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.recyclerview.widget.LinearLayoutManager;
//
//import com.example.myapplms.model.Course;
//import com.example.myapplms.ui.course.adapter.CourseAdapter;
//import com.example.myapplms.R;
//import com.example.myapplms.databinding.FragmentExploreListCourseBinding;
//import com.example.myapplms.ui.base.BaseFragment;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class ExploreListCourseFragment extends BaseFragment<FragmentExploreListCourseBinding> {
//
//    private CourseAdapter adapter;
//    private List<Course> courseList;
//
//    @NonNull
//    @Override
//    protected FragmentExploreListCourseBinding inflateBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
//        return FragmentExploreListCourseBinding.inflate(inflater, container, false);
//    }
//
//    @Override
//    protected void setupViews() {
//        // Khởi tạo danh sách
//        courseList = new ArrayList<>();
//        adapter = new CourseAdapter(courseList);
//
//        // Thiết lập RecyclerView
//        getBinding().rvCourses.setLayoutManager(new LinearLayoutManager(getContext()));
//        getBinding().rvCourses.setAdapter(adapter);
//
//        // Load dữ liệu mẫu
//        loadSampleData();
//    }
//
//    private void loadSampleData() {
//        // Dữ liệu mẫu khớp với giao diện yêu cầu
//        courseList.add(new Course(
//                1,
//                "E-Learning Platform Overview",
//                "by Dr. Sarah Chen",
//                "4.9 (1.2k)",
//                "125k students",
//                "18 lessons",
//                "2h 30m",
//                "FREE",
//                "Development",
//                "Beginner",
//                R.drawable.ic_launcher_background
//        ));
//
//        courseList.add(new Course(2,
//                "Advanced Android Development",
//                "by John Doe",
//                "4.8 (850)",
//                "50k students",
//                "24 lessons",
//                "5h 45m",
//                "$49.99",
//                "Development",
//                "Advanced",
//                R.drawable.ic_launcher_background
//        ));
//
//        courseList.add(new Course(3,
//                "UI/UX Design Fundamentals",
//                "by Jane Smith",
//                "4.7 (2.1k)",
//                "200k students",
//                "12 lessons",
//                "3h 15m",
//                "FREE",
//                "Design",
//                "Beginner",
//                R.drawable.ic_launcher_background
//        ));
//
//        adapter.notifyDataSetChanged();
//
//        // Cập nhật số lượng khóa học tìm thấy
//        getBinding().tvCourseCount.setText(courseList.size() + " courses found");
//    }
//
//    @Override
//    protected void setupListeners() {
//        // Các sự kiện click vào lọc, tìm kiếm có thể thêm ở đây
//        getBinding().layoutSearch.setOnClickListener(v -> {
//            // Xử lý tìm kiếm
//        });
//    }
//}
