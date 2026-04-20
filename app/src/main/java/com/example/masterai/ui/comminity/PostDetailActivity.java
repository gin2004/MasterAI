package com.example.masterai.ui.comminity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.masterai.R;
import com.example.masterai.api.RetrofitClient;
import com.example.masterai.databinding.ActivityPostDetailBinding;
import com.example.masterai.model.Comment;
import com.example.masterai.model.Post;
import com.example.masterai.model.User;
import com.example.masterai.utils.UserManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostDetailActivity extends AppCompatActivity {

    private ActivityPostDetailBinding binding;
    private Post post;
    private User currentUser;;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityPostDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        post = (Post) getIntent().getSerializableExtra("post");
        if (post == null) {
            finish();
            return;
        }
        currentUser = UserManager.getInstance(this).getUser();
        setupBack();
        displayPostDetail();
        loadComments();
    }

    private void setupBack() {
        binding.back.setOnClickListener(v->{
            finish();
        });
    }

    private void displayPostDetail() {
        // Ánh xạ các view từ include layout
        View postView = binding.includePost.getRoot();

        // Bạn có thể tái sử dụng logic bind data
        binding.includePost.tvContent.setText(post.getContent());
        binding.includePost.tvLikeCount.setText(String.valueOf(post.getLikeCount()));
        binding.includePost.tvCommentCount.setText(String.valueOf(post.getCommentCount()));

        if (post.getMedia() != null && !post.getMedia().isEmpty()) {
            binding.includePost.rvPostMedia.setVisibility(View.VISIBLE);
            MediaAdapter mediaAdapter = new MediaAdapter(post.getMedia());
            binding.includePost.rvPostMedia.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            binding.includePost.rvPostMedia.setAdapter(mediaAdapter);
        }
        if( !post.getUserId().equals(currentUser.getId())){
            RetrofitClient.getApiService().getUserById(post.getUserId(), null).enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        User user = response.body();
                        binding.includePost.tvUserName.setText(user.getUsername());
                        Glide.with(binding.includePost.ivAvatar)
                                .load(user.getAvatarUrl())
                                .circleCrop()
                                .into(binding.includePost.ivAvatar);
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {}
            });
        } else {
            binding.includePost.tvUserName.setText(currentUser.getUsername());
            Glide.with(PostDetailActivity.this)
                    .load(currentUser.getAvatarUrl())
                    .circleCrop()
                    .into(binding.includePost.ivAvatar);
        }

    }

    private void loadComments() {
        binding.rvComments.setLayoutManager(new LinearLayoutManager(this));
        // Gọi API lấy comment và set adapter cho rvComments
        if(post.getComments() != null && !post.getComments().isEmpty()){
            CommentAdapter commentAdapter = new CommentAdapter(post.getComments());
            binding.rvComments.setAdapter(commentAdapter);
        } else {
            binding.rvComments.setVisibility(View.GONE);
        }
    }
}
