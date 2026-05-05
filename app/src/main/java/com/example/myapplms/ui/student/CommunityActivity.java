package com.example.myapplms.ui.student;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplms.R;
import com.example.myapplms.model.Post;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommunityActivity extends AppCompatActivity {

    private RecyclerView rvPosts;
    private PostAdapter postAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);

        rvPosts = findViewById(R.id.rvPosts);
        rvPosts.setLayoutManager(new LinearLayoutManager(this));

        List<Post> dummyPosts = new ArrayList<>();
        dummyPosts.add(new Post("Nguyễn Văn A", "5 phút trước", true, 
            "Tips học React hiệu quả cho người mới bắt đầu", 
            Arrays.asList("#React", "#JavaScript", "#Beginner"), 24, 56, 342));
        
        dummyPosts.add(new Post("Trần Thị B", "15 phút trước", false, 
            "Hỏi về khóa học Python cho Data Science", 
            Arrays.asList("#Python", "#Data Science"), 18, 34, 198));

        dummyPosts.add(new Post("Lê Văn C", "30 phút trước", true, 
            "Chia sẻ kinh nghiệm thi chứng chỉ AWS Solutions Architect", 
            Arrays.asList("#AWS", "#Certificate", "#Cloud"), 42, 89, 567));

        postAdapter = new PostAdapter(dummyPosts);
        rvPosts.setAdapter(postAdapter);
    }
}
