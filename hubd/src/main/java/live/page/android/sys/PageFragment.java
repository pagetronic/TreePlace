package live.page.android.sys;

import androidx.fragment.app.Fragment;

import live.page.android.api.Json;
import live.page.android.auth.Accounts;

public class PageFragment extends Fragment {
    protected Json user;

    public PageFragment() {
        user = Accounts.getProfile(getContext());
    }
}
