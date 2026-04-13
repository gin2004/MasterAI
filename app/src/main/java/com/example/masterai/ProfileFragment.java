package com.example.masterai;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private TextView tvToolbarName, tvProfileName, tvProfileBio;
    private TextView tvPostCount, tvFollowerCount, tvFollowingCount;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Ánh xạ View
        tvToolbarName = view.findViewById(R.id.tvToolbarName);
        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileBio = view.findViewById(R.id.tvProfileBio);
        tvPostCount = view.findViewById(R.id.tvPostCount);
        tvFollowerCount = view.findViewById(R.id.tvFollowerCount);
        tvFollowingCount = view.findViewById(R.id.tvFollowingCount);

        // Giả sử chúng ta lấy thông tin của user vừa đăng nhập
        // Trong thực tế, bạn sẽ lấy username từ SharedPreferences
        loadUserProfile("admin"); 

        return view;
    }

    private void loadUserProfile(String username) {
        RetrofitClient.getApiService().getUsers().enqueue(new Callback<java.util.List<User>>() {
            @Override
            public void onResponse(Call<java.util.List<User>> call, Response<java.util.List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Tìm user trong danh sách (Đây là ví dụ, tốt nhất nên có API lấy 1 user)
                    for (User user : response.body()) {
                        if (user.getUsername().equals(username)) {
                            updateUI(user);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<java.util.List<User>> call, Throwable t) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi tải profile", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateUI(User user) {
        tvToolbarName.setText(user.getUsername());
        tvProfileName.setText(user.getUsername());
        tvProfileBio.setText(user.getEmail()); // Hiển thị email thay cho bio nếu backend chưa có bio
        // Các thông số count có thể lấy từ object User nếu backend của bạn trả về
    }
}
