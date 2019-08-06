package com.chibusoft.travelmantics;

import android.app.Activity;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.AuthUI.IdpConfig;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class FirebaseUtil {

    public static FirebaseDatabase mFirebaseDatabase;
    public static DatabaseReference mDatabaseReference;
    public static FirebaseUtil firebaseUtil;
    public static FirebaseAuth mFirebaseAuth;
    public static FirebaseStorage mFirebaseStorage;
    public static StorageReference mPhotosStorageReference;
    public static FirebaseAuth.AuthStateListener mAuthListener;
    public static ArrayList<TravelDeal> mDeals;
    public static ListActivity caller;
    public static final int RC_SIGN_IN = 1;
    public static boolean isAdmin;

    private FirebaseUtil(){};

    public static void openFbReference(String ref, final ListActivity callerActivity) {
        if (firebaseUtil == null) {
            firebaseUtil = new FirebaseUtil();
            mFirebaseDatabase = FirebaseDatabase.getInstance();
        }
        mDeals = new ArrayList<TravelDeal>();
        mDatabaseReference = mFirebaseDatabase.getReference().child(ref);

        caller = callerActivity;
        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //FirebaseUtil.signIn();
                if(firebaseAuth.getCurrentUser() == null){
                    FirebaseUtil.signIn();
                    Toast.makeText(caller, "Welcome back", Toast.LENGTH_LONG).show();
                }
                else
                {
                    String userId = firebaseAuth.getUid();
                    checkAdmin(userId);
                }

            }


        };

        connectStorage();
    }

    private static void checkAdmin(String uid){
        FirebaseUtil.isAdmin=false;
        DatabaseReference ref = mFirebaseDatabase.getReference().child("administrators")
                .child(uid);
        ChildEventListener listener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                FirebaseUtil.isAdmin = true;
                caller.showMenu();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        ref.addChildEventListener(listener);
    }

    private static void signIn()
    {
        List<IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());




        caller.startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(), RC_SIGN_IN);
    }

    public static void attachListener(){
        mFirebaseAuth.addAuthStateListener(mAuthListener);
    }

    public static void detachListener(){
        mFirebaseAuth.removeAuthStateListener(mAuthListener);
    }


    public static void connectStorage(){
        //Initialized database on create
        mFirebaseStorage = FirebaseStorage.getInstance();

        //Initialized database storage reference
        //child name must be the same with folder name created in Firebase web console "USING STORAGE"
        mPhotosStorageReference = mFirebaseStorage.getReference().child("deals_pictures");
    }


    }
