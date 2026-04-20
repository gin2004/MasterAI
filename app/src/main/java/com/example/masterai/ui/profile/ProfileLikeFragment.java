package com.example.masterai.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.masterai.api.RetrofitClient;
import com.example.masterai.databinding.FragmentProfileLikeBinding;
import com.example.masterai.model.PaginatedPostResponse;
import com.example.masterai.model.Post;
import com.example.masterai.model.User;
import com.example.masterai.ui.comminity.PostDetailActivity;
import com.example.masterai.utils.UserManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileLikeFragment extends Fragment {

    private FragmentProfileLikeBinding binding;
    private List<Post> likedPosts = new ArrayList<>();
    private PostUserAdapter adapter;
    private String userId = null;

    public ProfileLikeFragment() {
    }

    public static ProfileLikeFragment newInstance(String userId) {
        ProfileLikeFragment fragment = new ProfileLikeFragment();
        Bundle args = new Bundle();
        args.putString("USER_ID", userId);
        fragment.setArguments(args); // Nhét data vào "balo" của Fragment
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileLikeBinding.inflate(inflater, container, false);

        if(getArguments() != null){
            userId = getArguments().getString("USER_ID");
        }

        if(userId == null ){
            this.userId = UserManager.getInstance(requireContext()).getUser().getId();
        }
        setupRecyclerView();
        loadLikedPosts();
        
        binding.swipeRefresh.setOnRefreshListener(this::loadLikedPosts);
        
        return binding.getRoot();
    }

    private void setupRecyclerView() {
        adapter = new PostUserAdapter(likedPosts, post -> {
            Intent intent = new Intent(getActivity(), PostDetailActivity.class);
            intent.putExtra("post", post);
            startActivity(intent);
        });
        
        binding.rvLikedPosts.setLayoutManager(new GridLayoutManager(getContext(), 3));
        binding.rvLikedPosts.setAdapter(adapter);
    }

    private void loadLikedPosts() {
        if (userId == null) return;

        adapter.setLoading(true);

        RetrofitClient.getApiService().getLikedUserPosts(userId, 1, 30) // Tạm thời dùng chung demo
                .enqueue(new Callback<PaginatedPostResponse<Post>>() {
                    @Override
                    public void onResponse(Call<PaginatedPostResponse<Post>> call, Response<PaginatedPostResponse<Post>> response) {
                        binding.swipeRefresh.setRefreshing(false);
                        adapter.setLoading(false);
                        if (response.isSuccessful() && response.body() != null) {
                            likedPosts.clear();
                            likedPosts.addAll(response.body().getResults());
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onFailure(Call<PaginatedPostResponse<Post>> call, Throwable t) {
                        binding.swipeRefresh.setRefreshing(false);
                        adapter.setLoading(false);
                        Toast.makeText(getContext(), "Lỗi tải bài viết yêu thích", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
