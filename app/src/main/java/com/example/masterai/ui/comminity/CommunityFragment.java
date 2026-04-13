package com.example.masterai.ui.comminity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.masterai.R;
import com.example.masterai.model.Media;
import com.example.masterai.model.Post;
import java.util.ArrayList;
import java.util.List;

public class CommunityFragment extends Fragment {

    private RecyclerView rvPosts;
    private PostAdapter postAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_community, container, false);

        rvPosts = view.findViewById(R.id.rvPosts);
        rvPosts.setLayoutManager(new LinearLayoutManager(getContext()));

        view.findViewById(R.id.btnNotification).setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new NotificationFragment())
                .addToBackStack(null)
                .commit();
        });

        // Sample data for posts
        List<Post> samplePosts = new ArrayList<>();
        
        Post post1 = new Post();
        post1.setUserId("1");
        post1.setContent("Loving the new AI image generation features! #AI #Art");
        List<Media> media1 = new ArrayList<>();
        Media m1 = new Media();
        m1.setUrl("https://example.com/image1.jpg");
        media1.add(m1);
        post1.setMedia(media1);
        
        Post post2 = new Post();
        post2.setUserId("2");
        post2.setContent("Check out this amazing landscape I generated today.");
        
        samplePosts.add(post1);
        samplePosts.add(post2);

        postAdapter = new PostAdapter(samplePosts);
        rvPosts.setAdapter(postAdapter);

        return view;
    }
}
