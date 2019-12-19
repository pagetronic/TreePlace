package com.agroneo.treeplace.auth;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.agroneo.treeplace.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnSuccessListener;


//https://developers.google.com/identity/sign-in/android/backend-auth
class GoogleOauth {

    public static void sign(Activity ctx) {
        String serverClientId = ctx.getString(R.string.google_client_id);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(serverClientId)
                .requestEmail()
                .build();
        Intent intent = GoogleSignIn.getClient(ctx, gso).getSignInIntent();
        ctx.startActivityForResult(intent, 100);

    }

    public static void result(Intent data) {
        GoogleSignIn.getSignedInAccountFromIntent(data).addOnSuccessListener(new OnSuccessListener<GoogleSignInAccount>() {
            @Override
            public void onSuccess(GoogleSignInAccount account) {
                String idToken = account.getIdToken();
                Log.w("agr", idToken);
            }
        });
    }

}
