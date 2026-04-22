package com.example.masterai.utils;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.masterai.MainActivity;

public class ViewsUtils {
    private static boolean isVisible = true;
    private static GifDrawable gifDrawable;

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


    // Load GIF
    public static void load(ImageView imageView, Object source) {
        Glide.with(imageView.getContext())
                .asGif()
                .load(source)
                .into(new CustomTarget<GifDrawable>() {
                    @Override
                    public void onResourceReady(@NonNull GifDrawable resource, @Nullable Transition<? super GifDrawable> transition) {
                        gifDrawable = resource;
                        imageView.setImageDrawable(resource);
                        resource.start(); // auto play
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        imageView.setImageDrawable(placeholder);
                    }
                });
    }

    // Play
    public static void play() {
        if (gifDrawable != null && !gifDrawable.isRunning()) {
            gifDrawable.start();
        }
    }

    // Pause
    public static void pause() {
        if (gifDrawable != null && gifDrawable.isRunning()) {
            gifDrawable.stop();
        }
    }

    // Stop + reset
    public static void stop() {
        if (gifDrawable != null) {
            gifDrawable.stop();
            gifDrawable.setLoopCount(0);
        }
    }

    // Check trạng thái
    public static boolean isPlaying() {
        return gifDrawable != null && gifDrawable.isRunning();
    }

    // Clear tránh leak
    public static void clear() {
        if (gifDrawable != null) {
            gifDrawable.stop();
            gifDrawable = null;
        }
    }
}
