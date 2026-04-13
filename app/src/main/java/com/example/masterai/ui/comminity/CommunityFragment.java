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
import com.example.masterai.model.Post;
import com.example.masterai.utils.MockDataGenerator;
import java.util.ArrayList;

public class CommunityFragment extends Fragment {

    private RecyclerView recyclerView;
    private PostAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_community, container, false);

        recyclerView = view.findViewById(R.id.rvPosts);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Lấy dữ liệu ảo
        ArrayList<Post> postList = MockDataGenerator.getMockPosts();

        // Thiết lập Adapter
        adapter = new PostAdapter(postList);
        recyclerView.setAdapter(adapter);

        return view;
    }
}
