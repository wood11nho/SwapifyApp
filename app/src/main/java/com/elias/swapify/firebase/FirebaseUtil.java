package com.elias.swapify.firebase;

import com.google.firebase.auth.FirebaseAuth;

public class FirebaseUtil {
    private static FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    public static boolean isUserLoggedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }

    public static boolean isEmailVerified() {
        if (firebaseAuth.getCurrentUser() != null) {
            return firebaseAuth.getCurrentUser().isEmailVerified();
        }
        return false;
    }

    public static void signOut() {
        firebaseAuth.signOut();
    }

    public static String getCurrentUserId(){
        if (firebaseAuth.getCurrentUser() != null) {
            return firebaseAuth.getCurrentUser().getUid();
        }
        return null;
    }
}