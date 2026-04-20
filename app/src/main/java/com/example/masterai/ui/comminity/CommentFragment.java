package com.example.masterai.ui.comminity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.masterai.MainActivity;
import com.example.masterai.R;
import com.example.masterai.api.RetrofitClient;
import com.example.masterai.model.Comment;
import com.example.masterai.model.Notification;
import com.example.masterai.model.User;
import com.example.masterai.utils.UserManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommentFragment extends Fragment {
    
    private RecyclerView rvComments;
    private CommentAdapter adapter;
    private String postId;
    private String postUserId; // ID của chủ bài viết
    private EditText etComment;
    private ImageView btnSend;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comment, container, false);
        
        if (getArguments() != null) {
            postId = getArguments().getString("post_id");
            postUserId = getArguments().getString("post_user_id");
        }

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setBottomNavVisibility(View.GONE);
        }

        initViews(view);
        
        if (postId != null) {
            fetchComments();
        }
        
        return view;
    }

    private void initViews(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            });
        }

        rvComments = view.findViewById(R.id.rvComments);
        rvComments.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CommentAdapter(new ArrayList<>());
        rvComments.setAdapter(adapter);

        etComment = view.findViewById(R.id.etComment);
        btnSend = view.findViewById(R.id.btnSend);

        btnSend.setOnClickListener(v -> sendComment());
    }

    private void fetchComments() {
        RetrofitClient.getApiService().getComments(postId).enqueue(new Callback<List<Comment>>() {
            @Override
            public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setComments(response.body());
                }
            }
            @Override
            public void onFailure(Call<List<Comment>> call, Throwable t) {}
        });
    }

    private void sendComment() {
        String content = etComment.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            return;
        }

        User currentUser = UserManager.getInstance(requireContext()).getUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để bình luận", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("user_id", currentUser.getId());
        body.put("content", content);
        body.put("parent", null);

        RetrofitClient.getApiService().addComment(postId, body).enqueue(new Callback<Comment>() {
            @Override
            public void onResponse(Call<Comment> call, Response<Comment> response) {
                if (response.isSuccessful() && response.body() != null) {
                    etComment.setText("");
                    fetchComments();
                    Toast.makeText(getContext(), "Đã gửi bình luận", Toast.LENGTH_SHORT).show();
                    
                    // Gửi thông báo cho chủ bài viết
                    if (postUserId != null && !postUserId.equals(currentUser.getId())) {
                        sendNotification(postUserId, currentUser.getUsername() + " đã bình luận bài viết của bạn: " + content, "comment");
                    }
                } else {
                    Toast.makeText(getContext(), "Lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Comment> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendNotification(String recipientId, String content, String type) {
        Notification notification = new Notification(recipientId, content, type);
        RetrofitClient.getApiService().createNotification(notification).enqueue(new Callback<Notification>() {
            @Override
            public void onResponse(Call<Notification> call, Response<Notification> response) {}
            @Override
            public void onFailure(Call<Notification> call, Throwable t) {}
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setBottomNavVisibility(View.VISIBLE);
        }
    }
}
