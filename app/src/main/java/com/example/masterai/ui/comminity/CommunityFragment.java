package com.example.masterai.ui.comminity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
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
import com.example.masterai.utils.ViewsUtils;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommunityFragment extends Fragment {

    private RecyclerView rvPosts;
    private PostAdapter postAdapter;
    private User currentUser;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_community, container, false);

        initData(view);
        rvPosts = view.findViewById(R.id.rvPosts);
        //ẩn hiện bottom nav
        ViewsUtils.controlBottomNavigationView(rvPosts, this);
        // Cấu hình RecyclerView
        rvPosts.setLayoutManager(new LinearLayoutManager(getContext()));

        postAdapter = new PostAdapter(new ArrayList<>());
        rvPosts.setAdapter(postAdapter);

        view.findViewById(R.id.btnNotification).setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new NotificationFragment())
                .addToBackStack(null)
                .commit();
        });

        // Tải danh sách bài viết từ API
        fetchPosts();

        return view;
    }

    private void initData(View view) {
        ImageView ivUserAvatar = view.findViewById(R.id.ivUserAvatar);
        currentUser = UserManager.getInstance(requireContext()).getUser();
        if (currentUser != null) {
            Glide.with(requireContext()).load(currentUser.getAvatarUrl()).circleCrop().into(ivUserAvatar);
        }

        ivUserAvatar.setOnClickListener(v -> showAvatarMenu(v));
    }

    private void showAvatarMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.community_avatar_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_profile) {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ProfileFragment())
                        .addToBackStack(null)
                        .commit();
                return true;
            } else if (id == R.id.menu_logout) {
                performLogout();
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    private void performLogout() {
        UserManager.getInstance(requireContext()).logout();
        Toast.makeText(getContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show();
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
    }
}
