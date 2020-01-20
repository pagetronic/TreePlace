package live.page.android.sys;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import live.page.android.api.Json;
import live.page.android.auth.Accounts;

public abstract class PageFragment extends Fragment {
    protected Json user;
    private View view;

    protected PageFragment() {
    }

    protected abstract int layout();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        user = Accounts.getProfile(getContext());
        if (view == null) {
            view = inflater.inflate(layout(), container, false);
        }
        return view;
    }
}
