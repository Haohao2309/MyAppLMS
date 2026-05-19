package com.example.myapplms.ui.community;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplms.LMSApplication;
import com.example.myapplms.R;
import com.example.myapplms.data.remote.dto.response.PostResponse;
import com.example.myapplms.data.repository.CommunityRepository;
import com.example.myapplms.ui.community.adapter.PostAdapter;
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
                    } else {
                        // Trường hợp 404 hoặc refresh chung
                        viewModel.refreshPosts();
                    }
                }
            }
    );

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
        observeViewModel();

        viewModel.fetchPosts(null, null, false);
    }

    private void setupViewModel() {
        LMSApplication app = (LMSApplication) requireActivity().getApplication();
        CommunityRepository repository = new CommunityRepository(app.getRetrofitClient().getApiService());
        CommunityViewModelFactory factory = new CommunityViewModelFactory(repository);
        viewModel = new ViewModelProvider(this, factory).get(CommunityViewModel.class);
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
                showDeletePostDialog(post.id);
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

        // Category filtering (assuming chips or tabs)
        View btnAll = view.findViewById(R.id.tabAll);
        if (btnAll != null) btnAll.setOnClickListener(v -> viewModel.selectCategory(null));
        
        View btnCourse = view.findViewById(R.id.tabCourse);
        if (btnCourse != null) btnCourse.setOnClickListener(v -> viewModel.selectCategory("Khóa học"));
        
        View btnTech = view.findViewById(R.id.tabTech);
        if (btnTech != null) btnTech.setOnClickListener(v -> viewModel.selectCategory("Lập trình"));
    }

    private void observeViewModel() {
        viewModel.posts.observe(getViewLifecycleOwner(), posts -> {
            postList.clear();
            postList.addAll(posts);
            adapter.notifyDataSetChanged();
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

    private void showDeletePostDialog(String postId) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Lựa chọn")
                .setItems(new String[]{"Sửa bài viết", "Xóa bài viết"}, (dialog, which) -> {
                    if (which == 0) {
                        Toast.makeText(requireContext(), "Chức năng Sửa đang phát triển", Toast.LENGTH_SHORT).show();
                    } else {
                        confirmDeletePost(postId);
                    }
                })
                .show();
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
                    repository.deletePost(postId).enqueue(new retrofit2.Callback<com.example.myapplms.data.remote.dto.response.CommunityActionResponse>() {
                        @Override
                        public void onResponse(retrofit2.Call<com.example.myapplms.data.remote.dto.response.CommunityActionResponse> call, retrofit2.Response<com.example.myapplms.data.remote.dto.response.CommunityActionResponse> response) {
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
                        public void onFailure(retrofit2.Call<com.example.myapplms.data.remote.dto.response.CommunityActionResponse> call, Throwable t) {
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
