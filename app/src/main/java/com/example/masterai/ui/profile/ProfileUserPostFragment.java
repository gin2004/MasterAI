package com.example.masterai.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.masterai.api.RetrofitClient;
import com.example.masterai.databinding.FragmentProfileUserPostBinding;
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

public class ProfileUserPostFragment extends Fragment {

    private FragmentProfileUserPostBinding binding;
    private List<Post> postList = new ArrayList<>();
    private PostUserAdapter adapter;

    private String userId = null;

    public ProfileUserPostFragment() {
    }
    //pattern newInstance
    public static ProfileUserPostFragment newInstance(String userId) {
        ProfileUserPostFragment fragment = new ProfileUserPostFragment();
        Bundle args = new Bundle();
        args.putString("USER_ID", userId);
        fragment.setArguments(args); // Nhét data vào "balo" của Fragment
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileUserPostBinding.inflate(inflater, container, false);

        if(getArguments() != null){
            userId = getArguments().getString("USER_ID");
        }

        if(userId == null ){
            this.userId = UserManager.getInstance(requireContext()).getUser().getId();
        }
        setupRecyclerView();
        loadUserPosts();
        
        binding.swipeRefresh.setOnRefreshListener(this::loadUserPosts);
        
        return binding.getRoot();
    }

    private void setupRecyclerView() {
        adapter = new PostUserAdapter(postList, post -> {
            Intent intent = new Intent(getActivity(), PostDetailActivity.class);
            intent.putExtra("post", post);
            startActivity(intent);
        });
        
        binding.rvUserPosts.setLayoutManager(new GridLayoutManager(getContext(), 3));
        binding.rvUserPosts.setAdapter(adapter);
    }

    private void loadUserPosts() {
        if (userId == null) return;

        adapter.setLoading(true);
        RetrofitClient.getApiService().getUserPosts(userId, 1, 30)
                .enqueue(new Callback<PaginatedPostResponse<Post>>() {
                    @Override
                    public void onResponse(Call<PaginatedPostResponse<Post>> call, Response<PaginatedPostResponse<Post>> response) {
                        binding.swipeRefresh.setRefreshing(false);
                        adapter.setLoading(false);
                        if (response.isSuccessful() && response.body() != null) {
                            postList.clear();
                            postList.addAll(response.body().getResults());
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onFailure(Call<PaginatedPostResponse<Post>> call, Throwable t) {
                        binding.swipeRefresh.setRefreshing(false);
                        adapter.setLoading(false);
                    }
                });
    }
}
