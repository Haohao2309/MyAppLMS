package com.example.myapplms.ui.student.course_detail.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.myapplms.ui.student.course_detail.CurriculumFragment;
import com.example.myapplms.ui.student.course_detail.OverviewFragment;
import com.example.myapplms.ui.student.course_detail.ReviewsFragment;
import com.example.myapplms.ui.teacher.grading.StudentSubmissionListFragment;

/**
 * Adapter cho ViewPager2 trong CourseDetailActivity.
 *
 * Nếu isTeacher = true:
 *   Tab 0 → OverviewFragment          (xem thông tin khóa học)
 *   Tab 1 → StudentSubmissionListFragment  (← thay Curriculum)
 *   Tab 2 → ReviewsFragment
 *
 * Nếu isTeacher = false (học sinh):
 *   Tab 0 → OverviewFragment
 *   Tab 1 → CurriculumFragment
 *   Tab 2 → ReviewsFragment
 */
public class CoursePagerAdapter extends FragmentStateAdapter {

    private final int courseId;
    private final boolean isTeacher;

    public CoursePagerAdapter(@NonNull FragmentActivity activity, int courseId, boolean isTeacher) {
        super(activity);
        this.courseId  = courseId;
        this.isTeacher = isTeacher;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return OverviewFragment.newInstance(courseId);
            case 1:
                if (isTeacher) {
                    // Giảng viên → màn danh sách học sinh nộp bài
                    return StudentSubmissionListFragment.newInstance(courseId);
                } else {
                    // Học sinh → chương trình học bình thường
                    return CurriculumFragment.newInstance(courseId);
                }
            case 2:
                return ReviewsFragment.newInstance(courseId);
            default:
                return OverviewFragment.newInstance(courseId);
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}