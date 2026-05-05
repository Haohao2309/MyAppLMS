package com.example.myapplms.ui.student;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplms.R;
import com.example.myapplms.model.Notification;
import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView rvNotifications;
    private NotificationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        rvNotifications = findViewById(R.id.rvNotifications);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));

        List<Notification> list = new ArrayList<>();
        list.add(new Notification("Achievement Unlocked! 🏆", 
            "You've completed 5 lessons in a row! You earned the 'On a Roll' badge.", 
            "Just now", true, Notification.Type.ACHIEVEMENT));
        
        list.add(new Notification("Quiz Results Available", 
            "Your HTML Fundamentals quiz has been graded. You scored 90/100! Great job!", 
            "15 min ago", true, Notification.Type.QUIZ));

        list.add(new Notification("New Message from Dr. Sarah Chen", 
            "Your certificate has been issued! Download it from My Courses.", 
            "1 hour ago", true, Notification.Type.MESSAGE));

        list.add(new Notification("Course Update: Web Dev Bootcamp", 
            "New lessons on React 19 features have been added to Section 7. Check them out!", 
            "2 hours ago", false, Notification.Type.UPDATE));

        adapter = new NotificationAdapter(list);
        rvNotifications.setAdapter(adapter);
    }
}
