package com.example.myapplms.ui.student.course_detail.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.myapplms.ui.student.course_detail.CurriculumFragment;
import com.example.myapplms.ui.student.course_detail.OverviewFragment;
import com.example.myapplms.ui.student.course_detail.ReviewsFragment;

public class CoursePagerAdapter extends FragmentStateAdapter {

    public CoursePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new OverviewFragment();
            case 1:
                return new CurriculumFragment(); // Đã viết code ở phần trước
            case 2:
                return new ReviewsFragment();
            default:
                return new OverviewFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3; // Chúng ta có 3 tab
    }
}