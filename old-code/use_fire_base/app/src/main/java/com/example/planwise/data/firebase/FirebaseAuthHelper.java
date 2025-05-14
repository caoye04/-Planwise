package com.example.planwise.data.firebase;

import androidx.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class FirebaseAuthHelper {
    private static final String TAG = "FirebaseAuthHelper";
    private FirebaseAuth mAuth;
    private static FirebaseAuthHelper instance;
    private AuthCallback mCallback;

    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onFailure(Exception e);
    }

    private FirebaseAuthHelper() {
        mAuth = FirebaseAuth.getInstance();
    }

    public static synchronized FirebaseAuthHelper getInstance() {
        if (instance == null) {
            instance = new FirebaseAuthHelper();
        }
        return instance;
    }

    public void setAuthCallback(AuthCallback callback) {
        this.mCallback = callback;
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public boolean isUserLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }

    public void signInWithEmailAndPassword(String email, String password) {
        Log.d(TAG, "尝试登录: " + email);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmail:success");
                            if (mCallback != null) {
                                mCallback.onSuccess(mAuth.getCurrentUser());
                            }
                        } else {
                            Exception e = task.getException();
                            String errorMsg = e != null ? e.getMessage() : "未知错误";
                            Log.w(TAG, "signInWithEmail:failure: " + errorMsg, e);
                            if (mCallback != null) {
                                mCallback.onFailure(e);
                            }
                        }
                    }
                });
    }

    public void createUserWithEmailAndPassword(String email, String password, final String username) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(username)
                                    .build();

                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "User profile updated.");
                                                if (mCallback != null) {
                                                    mCallback.onSuccess(user);
                                                }
                                            }
                                        }
                                    });
                        } else {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            if (mCallback != null) {
                                mCallback.onFailure(task.getException());
                            }
                        }
                    }
                });
    }

    public void signOut() {
        mAuth.signOut();
    }
}