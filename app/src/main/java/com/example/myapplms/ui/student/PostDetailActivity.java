package com.example.myapplms.ui.student;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplms.R;
import com.example.myapplms.model.Comment;
import java.util.ArrayList;
import java.util.List;

public class PostDetailActivity extends AppCompatActivity {

    private RecyclerView rvComments;
    private CommentAdapter commentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        rvComments = findViewById(R.id.rvComments);
        rvComments.setLayoutManager(new LinearLayoutManager(this));

        List<Comment> dummyComments = new ArrayList<>();
        dummyComments.add(new Comment("Lê Văn C", "2 phút trước", 
            "Mình thêm tip: Nên học TypeScript luôn khi học React, nó giúp code rõ ràng và ít bug hơn nhiều!", 12, false, false));
        
        dummyComments.add(new Comment("Nguyễn Văn A", "1 phút trước", 
            "@Trần Thị B useEffect dùng để xử lý side effects như fetch data, subscribe events, update DOM. Cú pháp cơ bản:\n\n```javascript\nuseEffect(() => {\n// Code chạy sau mỗi lần render\nreturn () => {\n// Cleanup function\n}\n}, [dependencies]) // Chỉ chạy khi dependencies thay đổi\n...\n\nMình sẽ làm một bài hướng dẫn chi tiết về hooks nhé!", 15, true, true));

        dummyComments.add(new Comment("Bạn", "Vừa xong", "hehe", 0, false, false));

        commentAdapter = new CommentAdapter(dummyComments);
        rvComments.setAdapter(commentAdapter);
    }
}
