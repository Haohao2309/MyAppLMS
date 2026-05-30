package com.example.myapplms.ui.community;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplms.LMSApplication;
import com.example.myapplms.R;
import com.example.myapplms.data.remote.dto.response.community_response.PostResponse;
import com.example.myapplms.data.remote.dto.response.community_response.CommunityActionResponse;
import com.example.myapplms.data.repository.CommunityRepository;
import com.example.myapplms.ui.community.adapter.PostAdapter;
import com.example.myapplms.ui.community.adapter.SortSpinnerAdapter;
import com.example.myapplms.utils.SessionManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommunityFragment extends Fragment {

    private CommunityViewModel viewModel;
    private PostAdapter adapter;
    private final List<PostResponse> postList = new ArrayList<>();

    private final ActivityResultLauncher<Intent> detailLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null && data.hasExtra("deletedPostId")) {
                        String deletedId = data.getStringExtra("deletedPostId");
                        
                        // 1. Cập nhật ViewModel (Source of truth)
                        viewModel.removePostLocally(deletedId);
                        
                        // 2. Cập nhật trực tiếp List và Adapter để biến mất ngay lập tức (Force UI)
                        for (int i = 0; i < postList.size(); i++) {
                            if (postList.get(i).id.equals(deletedId)) {
                                postList.remove(i);
                                adapter.notifyItemRemoved(i);
                                break;
                            }
                        }
                    } else if (data != null && data.hasExtra("searchTag")) {
                        String tag = data.getStringExtra("searchTag");
                        onTagClickFromDetail(tag);
                    } else {
                        // Trường hợp 404 hoặc refresh chung
                        viewModel.refreshPosts();
                    }
                }
            }
    );

    private void onTagClickFromDetail(String tag) {
        View fragmentView = getView();
        if (fragmentView != null) {
            EditText etSearch = fragmentView.findViewById(R.id.etSearch);
            if (etSearch != null) {
                etSearch.setText(tag);
            }
        }
        viewModel.searchPosts(tag);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_community, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupViewModel();
        setupRecyclerView(view);
        setupListeners(view);
        setupSortSpinner(view);
        observeViewModel();

        viewModel.fetchPosts(null, null, "newest", false);
    }

    private void setupSortSpinner(View view) {
        Spinner spinnerSort = view.findViewById(R.id.spinnerSort);
        String[] sortOptions = {"Mới nhất", "Trending", "Nhiều phản hồi"};
        SortSpinnerAdapter sortAdapter = new SortSpinnerAdapter(requireContext(), sortOptions);
        spinnerSort.setAdapter(sortAdapter);

        spinnerSort.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                sortAdapter.setSelectedPosition(position);
                String sortBy;
                switch (position) {
                    case 1: sortBy = "trending"; break;
                    case 2: sortBy = "replies"; break;
                    default: sortBy = "newest"; break;
                }
                viewModel.sortPosts(sortBy);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void setupViewModel() {
        LMSApplication app = (LMSApplication) requireActivity().getApplication();
        CommunityRepository repository = new CommunityRepository(app.getRetrofitClient().getApiService());
        CommunityViewModelFactory factory = new CommunityViewModelFactory(repository);
        viewModel = new ViewModelProvider(this, factory).get(CommunityViewModel.class);
    }

    private void setupListeners(View view) {
        View btnAddPost = view.findViewById(R.id.btnAddPost);
        if (btnAddPost != null) {
            btnAddPost.setOnClickListener(v -> showCreatePostDialog());
        }

        if (view.findViewById(R.id.btnBack) != null) {
            view.findViewById(R.id.btnBack).setOnClickListener(v -> requireActivity().onBackPressed());
        }

        // Search logic
        EditText etSearch = view.findViewById(R.id.etSearch);
        if (etSearch != null) {
            etSearch.setOnEditorActionListener((v, actionId, event) -> {
                String query = v.getText().toString().trim();
                viewModel.searchPosts(query);
                return true;
            });
        }

        // Category filtering
        View btnAll = view.findViewById(R.id.tabAll);
        View btnCourse = view.findViewById(R.id.tabCourse);
        View btnTech = view.findViewById(R.id.tabTech);
        View btnQna = view.findViewById(R.id.tabQna);
        View btnDiscussion = view.findViewById(R.id.tabDiscussion);

        if (btnAll != null) btnAll.setOnClickListener(v -> {
            viewModel.selectCategory(null);
            updateTabUI(null, view);
        });

        if (btnCourse != null) btnCourse.setOnClickListener(v -> {
            viewModel.selectCategory("Khóa học");
            updateTabUI("Khóa học", view);
        });

        if (btnTech != null) btnTech.setOnClickListener(v -> {
            viewModel.selectCategory("Lập trình");
            updateTabUI("Lập trình", view);
        });

        if (btnQna != null) btnQna.setOnClickListener(v -> {
            viewModel.selectCategory("Hỏi đáp");
            updateTabUI("Hỏi đáp", view);
        });

        if (btnDiscussion != null) btnDiscussion.setOnClickListener(v -> {
            viewModel.selectCategory("Thảo luận");
            updateTabUI("Thảo luận", view);
        });

        // Thiết lập trạng thái mặc định ban đầu (Tất cả được chọn)
        updateTabUI(null, view);
    }

    /**
     * Cập nhật giao diện cho toàn bộ các Tab dựa trên Category đang chọn
     */
    private void updateTabUI(String selectedCategory, View view) {
        View btnAll = view.findViewById(R.id.tabAll);
        View btnCourse = view.findViewById(R.id.tabCourse);
        View btnTech = view.findViewById(R.id.tabTech);
        View btnQna = view.findViewById(R.id.tabQna);
        View btnDiscussion = view.findViewById(R.id.tabDiscussion);

        updateTabState(btnAll, selectedCategory == null);
        updateTabState(btnCourse, "Khóa học".equals(selectedCategory));
        updateTabState(btnTech, "Lập trình".equals(selectedCategory));
        updateTabState(btnQna, "Hỏi đáp".equals(selectedCategory));
        updateTabState(btnDiscussion, "Thảo luận".equals(selectedCategory));
    }

    /**
     * Cập nhật trạng thái cho từng Tab riêng lẻ (Background, Icon color, Text color)
     */
    private void updateTabState(View tab, boolean isSelected) {
        if (tab == null) return;

        // 1. Cập nhật Background
        tab.setBackgroundResource(isSelected ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);

        // 2. Cập nhật màu sắc cho các con (ImageView và TextView)
        if (tab instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) tab;
            int color = isSelected ?
                    ContextCompat.getColor(requireContext(), R.color.white) :
                    ContextCompat.getColor(requireContext(), R.color.text_secondary);

            for (int i = 0; i < group.getChildCount(); i++) {
                View child = group.getChildAt(i);
                if (child instanceof ImageView) {
                    ((ImageView) child).setColorFilter(color);
                } else if (child instanceof TextView) {
                    ((TextView) child).setTextColor(color);
                }
            }
        }
    }

    private void setupRecyclerView(View view) {
        RecyclerView rvPosts = view.findViewById(R.id.rvPosts);
        SessionManager sessionManager = new SessionManager(requireContext());
        String currentUserId = sessionManager.getUserId();
        
        adapter = new PostAdapter(postList, new PostAdapter.OnPostClickListener() {
            @Override
            public void onPostClick(PostResponse post) {
                Intent intent = new Intent(requireContext(), PostDetailActivity.class);
                intent.putExtra("POST_ID", post.id);
                detailLauncher.launch(intent);
            }

            @Override
            public void onLikeClick(PostResponse post) {
                viewModel.toggleLike(post.id);
            }

            @Override
            public void onDeleteClick(PostResponse post) {
                showDeletePostDialog(post);
            }

            @Override
            public void onTagClick(String tag) {
                View fragmentView = getView();
                if (fragmentView != null) {
                    EditText etSearch = fragmentView.findViewById(R.id.etSearch);
                    if (etSearch != null) {
                        etSearch.setText(tag);
                    }
                }
                viewModel.searchPosts(tag);
            }

            @Override
            public void onPinClick(PostResponse post) {
                viewModel.togglePin(post.id);
            }
        }, currentUserId);
        rvPosts.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvPosts.setAdapter(adapter);

        rvPosts.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && layoutManager.findLastVisibleItemPosition() == postList.size() - 1) {
                    viewModel.loadMorePosts();
                }
            }
        });
    }


    private void observeViewModel() {
        viewModel.posts.observe(getViewLifecycleOwner(), posts -> {
            postList.clear();
            postList.addAll(posts);
            adapter.notifyDataSetChanged();
        });

        viewModel.stats.observe(getViewLifecycleOwner(), stats -> {
            if (stats != null) {
                View v = getView();
                if (v != null) {
                    ((TextView) v.findViewById(R.id.tvStatMembers)).setText(String.valueOf(stats.totalMembers));
                    ((TextView) v.findViewById(R.id.tvStatTopics)).setText(String.valueOf(stats.totalTopics));
                    ((TextView) v.findViewById(R.id.tvStatReplies)).setText(String.valueOf(stats.totalReplies));
                    ((TextView) v.findViewById(R.id.tvStatOnline)).setText(String.valueOf(stats.liveOnlineCount));

                    TextView tvTopicCount = v.findViewById(R.id.tvTopicCount);
                    if (tvTopicCount != null) {
                        tvTopicCount.setText(stats.totalTopics + " chủ đề");
                    }
                }
            }
        });

        viewModel.errorMessage.observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.postCreated.observe(getViewLifecycleOwner(), created -> {
            if (Boolean.TRUE.equals(created)) {
                Toast.makeText(requireContext(), "Đăng bài thành công!", Toast.LENGTH_SHORT).show();
                viewModel.refreshPosts();
                viewModel.resetPostCreated();
            }
        });
    }

    private void showDeletePostDialog(PostResponse post) {
        SessionManager sessionManager = new SessionManager(requireContext());
        String userRole = sessionManager.getRole();
        
        List<String> options = new ArrayList<>();
        options.add("Sửa bài viết");
        options.add("Xóa bài viết");
        
        // Chỉ hiện tùy chọn ghim cho ADMIN/TEACHER
        boolean canPin = "ADMIN".equalsIgnoreCase(userRole) || "TEACHER".equalsIgnoreCase(userRole);
        if (canPin) {
            options.add(post.pinned ? "Bỏ ghim bài viết" : "Ghim bài viết");
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Lựa chọn")
                .setItems(options.toArray(new String[0]), (dialog, which) -> {
                    String selected = options.get(which);
                    if (selected.equals("Sửa bài viết")) {
                        showEditPostDialog(post);
                    } else if (selected.equals("Xóa bài viết")) {
                        confirmDeletePost(post.id);
                    } else if (selected.contains("ghim")) {
                        viewModel.togglePin(post.id);
                    }
                })
                .show();
    }

    // VIẾT MỚI HÀM MỞ DIALOG CHỈNH SỬA BÀI VIẾT
    private void showEditPostDialog(PostResponse currentPost) {
        // Tái sử dụng lại layout dialog_create_post có sẵn của bạn để không phải thiết kế lại giao diện xml
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_create_post, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext(), android.R.style.Theme_Material_Light_NoActionBar_Fullscreen)
                .setView(dialogView)
                .create();

        EditText etTitle = dialogView.findViewById(R.id.etTitle);
        EditText etContent = dialogView.findViewById(R.id.etContent);
        EditText etTags = dialogView.findViewById(R.id.etTags);
        Spinner spinner = dialogView.findViewById(R.id.spinnerCategory);

        // 1. Đổ dữ liệu cũ của bài viết vào các ô nhập liệu để người dùng thấy dữ liệu cũ
        etTitle.setText(currentPost.title);
        etContent.setText(currentPost.content);

        if (currentPost.tags != null && !currentPost.tags.isEmpty()) {
            etTags.setText(String.join(", ", currentPost.tags));
        }

        String[] categories = {"Khóa học", "Lập trình", "Hỏi đáp", "Thảo luận"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories);
        spinner.setAdapter(categoryAdapter);

        // Thiết lập Spinner nhảy đúng vào danh mục cũ của bài viết
        for (int i = 0; i < categories.length; i++) {
            if (categories[i].equalsIgnoreCase(currentPost.category)) {
                spinner.setSelection(i);
                break;
            }
        }

        // Thay đổi chữ của nút tạo thành nút "Lưu thay đổi"
        android.widget.Button btnCreate = dialogView.findViewById(R.id.btnCreate);
        btnCreate.setText("Lưu thay đổi");

        dialogView.findViewById(R.id.btnClose).setOnClickListener(v -> dialog.dismiss());

        btnCreate.setOnClickListener(v -> {
            String newTitle = etTitle.getText().toString().trim();
            String newContent = etContent.getText().toString().trim();
            String newCategory = spinner.getSelectedItem().toString();
            String tagsString = etTags.getText().toString().trim();

            if (newTitle.isEmpty() || newContent.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập đầy đủ tiêu đề và nội dung", Toast.LENGTH_SHORT).show();
                return;
            }

            List<String> newTags = new java.util.ArrayList<>();
            if (!tagsString.isEmpty()) {
                newTags = java.util.Arrays.asList(tagsString.split("\\s*,\\s*"));
            }

            // 2. Tiến hành gọi API Update đẩy dữ liệu lên Spring Boot
            LMSApplication app = (LMSApplication) requireActivity().getApplication();
            com.example.myapplms.data.repository.CommunityRepository repository = new com.example.myapplms.data.repository.CommunityRepository(app.getRetrofitClient().getApiService());

            com.example.myapplms.data.remote.dto.request.CreatePostRequest updateReq = new com.example.myapplms.data.remote.dto.request.CreatePostRequest(newTitle, newContent, newCategory, newCategory, newTags);

            repository.updatePost(currentPost.id, updateReq).enqueue(new retrofit2.Callback<PostResponse>() {
                @Override
                public void onResponse(retrofit2.Call<PostResponse> call, retrofit2.Response<PostResponse> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(requireContext(), "Cập nhật bài viết thành công!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        viewModel.refreshPosts(); // Tải lại danh sách mới nhất từ Page 1 để cập nhật UI bài viết vừa sửa
                    } else {
                        Toast.makeText(requireContext(), "Lỗi cập nhật: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<PostResponse> call, Throwable t) {
                    Toast.makeText(requireContext(), "Lỗi kết nối hệ thống", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private void confirmDeletePost(String postId) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa bài viết")
                .setMessage("Bạn có chắc chắn muốn xóa bài viết này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    // --- OPTIMISTIC UI REMOVE ---
                    viewModel.removePostLocally(postId);

                    // --- CALL API ---
                    LMSApplication app = (LMSApplication) requireActivity().getApplication();
                    CommunityRepository repository = new CommunityRepository(app.getRetrofitClient().getApiService());
                    repository.deletePost(postId).enqueue(new retrofit2.Callback<CommunityActionResponse>() {
                        @Override
                        public void onResponse(retrofit2.Call<CommunityActionResponse> call, retrofit2.Response<CommunityActionResponse> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(requireContext(), "Đã xóa bài viết", Toast.LENGTH_SHORT).show();
                                // Refresh completely from page 1 using the current category
                                viewModel.refreshPosts();
                            } else {
                                Toast.makeText(requireContext(), "Lỗi xóa: " + response.code(), Toast.LENGTH_SHORT).show();
                                // Refresh to restore list if deletion failed
                                viewModel.refreshPosts();
                            }
                        }

                        @Override
                        public void onFailure(retrofit2.Call<CommunityActionResponse> call, Throwable t) {
                            Toast.makeText(requireContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                            viewModel.refreshPosts();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showCreatePostDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_create_post, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext(), android.R.style.Theme_Material_Light_NoActionBar_Fullscreen)
                .setView(dialogView)
                .create();

        Spinner spinner = dialogView.findViewById(R.id.spinnerCategory);
        String[] categories = {"Khóa học", "Lập trình", "Hỏi đáp", "Thảo luận"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories);
        spinner.setAdapter(categoryAdapter);

        dialogView.findViewById(R.id.btnClose).setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.btnCreate).setOnClickListener(v -> {
            String title = ((EditText) dialogView.findViewById(R.id.etTitle)).getText().toString().trim();
            String content = ((EditText) dialogView.findViewById(R.id.etContent)).getText().toString().trim();
            String category = spinner.getSelectedItem().toString();
            String tagsString = ((EditText) dialogView.findViewById(R.id.etTags)).getText().toString().trim();

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập đầy đủ tiêu đề và nội dung", Toast.LENGTH_SHORT).show();
                return;
            }

            List<String> tags = new ArrayList<>();
            if (!tagsString.isEmpty()) {
                tags = Arrays.asList(tagsString.split(","));
            }

            viewModel.createPost(category, title, content, tags);
            dialog.dismiss();
        });

        dialog.show();
    }
}
