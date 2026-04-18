package com.example.masterai.utils;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.masterai.MainActivity;

public class ViewsUtils {
    private static boolean isVisible = true;

    public static void controlBottomNavigationView(RecyclerView recyclerView, Fragment fragment) {

        recyclerView.clearOnScrollListeners();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                MainActivity activity = (MainActivity) fragment.requireActivity();

                // lọc scroll rất nhỏ
                if (Math.abs(dy) < 5) return;

                if (dy > 0 && isVisible) {
                    activity.hideBottomNav();
                    isVisible = false;

                } else if (dy < 0 && !isVisible) {
                    activity.showBottomNav();
                    isVisible = true;
                }
            }
        });
    }
}
