package com.example.masterai.ui.comminity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.masterai.R;
import com.example.masterai.api.RetrofitClient;
import com.example.masterai.model.Post;
import com.example.masterai.model.User;
import com.example.masterai.ui.login.LoginActivity;
import com.example.masterai.ui.profile.ProfileFragment;
import com.example.masterai.utils.UserManager;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommunityFragment extends Fragment {

    private RecyclerView rvPosts;
    private PostAdapter postAdapter;
    private ImageView ivUserAvatar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_community, container, false);

        rvPosts = view.findViewById(R.id.rvPosts);
        ivUserAvatar = view.findViewById(R.id.ivUserAvatar);

        rvPosts.setLayoutManager(new LinearLayoutManager(getContext()));

        postAdapter = new PostAdapter(new ArrayList<>());
        rvPosts.setAdapter(postAdapter);

        view.findViewById(R.id.btnNotification).setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new NotificationFragment())
                .addToBackStack(null)
                .commit();
        });

        ivUserAvatar.setOnClickListener(this::showUserMenu);

        // Load current user avatar
        loadCurrentUserAvatar();

        // Tải danh sách bài viết từ API
        fetchPosts();

        return view;
    }

    private void loadCurrentUserAvatar() {
        User currentUser = UserManager.getInstance(requireContext()).getUser();
        if (currentUser != null && currentUser.getAvatarUrl() != null && !currentUser.getAvatarUrl().isEmpty()) {
            Glide.with(this)
                .load(currentUser.getAvatarUrl())
                .placeholder(android.R.drawable.ic_menu_report_image)
                .circleCrop()
                .into(ivUserAvatar);
        }
    }

    private void showUserMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(getContext(), v);
        popupMenu.getMenu().add(0, 1, 0, "Trang cá nhân");
        popupMenu.getMenu().add(0, 2, 1, "Đăng xuất");

        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1:
                    getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ProfileFragment())
                        .addToBackStack(null)
                        .commit();
                    return true;
                case 2:
                    logout();
                    return true;
                default:
                    return false;
            }
        });
        popupMenu.show();
    }

    private void logout() {
        UserManager.getInstance(requireContext()).logout();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    private void fetchPosts() {
        RetrofitClient.getApiService().getPosts().enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    postAdapter.setPosts(response.body());
                } else {
                    Toast.makeText(getContext(), "Không thể tải bài viết", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Cập nhật lại danh sách mỗi khi fragment hiển thị lại (ví dụ sau khi đăng bài xong quay lại)
        fetchPosts();
        loadCurrentUserAvatar();
    }
}
