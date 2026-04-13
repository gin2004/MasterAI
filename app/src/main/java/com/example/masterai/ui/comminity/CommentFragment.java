package com.example.masterai.ui.comminity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.masterai.R;
import com.example.masterai.model.Comment;
import java.util.ArrayList;
import java.util.List;

public class CommentFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comment, container, false);
        
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            });
        }

        RecyclerView rvComments = view.findViewById(R.id.rvComments);
        rvComments.setLayoutManager(new LinearLayoutManager(getContext()));

        List<Comment> sampleComments = new ArrayList<>();
        sampleComments.add(new Comment("Alex Johnson", "Wow, this looks amazing! How did you make it?", "2m", ""));
        sampleComments.add(new Comment("Sarah Miller", "I love the colors in this post. So vibrant!", "5m", ""));
        sampleComments.add(new Comment("David Chen", "Can you share more details about the process?", "15m", ""));
        sampleComments.add(new Comment("Emily White", "Great work as always!", "1h", ""));
        sampleComments.add(new Comment("Michael Brown", "This is very inspiring for my next project.", "3h", ""));

        CommentAdapter adapter = new CommentAdapter(sampleComments);
        rvComments.setAdapter(adapter);
        
        return view;
    }
}