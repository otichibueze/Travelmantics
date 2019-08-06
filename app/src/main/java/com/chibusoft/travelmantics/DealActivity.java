package com.chibusoft.travelmantics;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.storage.UploadTask.TaskSnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class DealActivity extends AppCompatActivity {

    private static final int RC_PHOTO_PICKER = 42 ;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    EditText txtTitle;
    EditText txtDescription;
    EditText txtPrice;
    ImageView imageView;
    Button btnImage;


    public TravelDeal deal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       /// FirebaseUtil.openFbReference("traveldeals",this);
        mFirebaseDatabase = FirebaseUtil.mFirebaseDatabase;
        mDatabaseReference = FirebaseUtil.mDatabaseReference;

        txtTitle = (EditText) findViewById(R.id.txtTitle);
        txtDescription  = (EditText) findViewById(R.id.txtDescription);
        txtPrice =  (EditText) findViewById(R.id.txtPrice);
        imageView = (ImageView) findViewById(R.id.image);




        Intent intent = getIntent();
        TravelDeal deal = (TravelDeal) intent.getSerializableExtra("Deal");
        if(deal == null){
            deal = new TravelDeal();
        }

        this.deal = deal;

        txtDescription.setText(deal.getDescription());
        txtPrice.setText(deal.getPrice());
        txtTitle.setText(deal.getTitle());
        showImage(deal.getImageUrl());



        btnImage = findViewById(R.id.btn_image);
        btnImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Insert Picture"), RC_PHOTO_PICKER);
            }
        });
    }

    private void showImage(String url){
        if(url != null && url.isEmpty() == false){
            int width = Resources.getSystem().getDisplayMetrics().widthPixels;

            Picasso.get()
                    .load(url)
                    .centerCrop()
                    .resize(width, width * 2/3)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                       // holder.mMediaEvidencePb.setVisibility(View.GONE);
                        Log.d("PICASO","Success");
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.d("PICASO",e.getMessage().toString());
                    }

                    });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int itemselected = item.getItemId();

        switch(item.getItemId())
        {
            case R.id.save_menu:
                saveDeal();
                Toast.makeText(this, "Deal saved", Toast.LENGTH_LONG).show();
                clean();
                backToList();
                return true;
            case R.id.delete_menu:
                deleteDeal();
                Toast.makeText(this, "Deal Deleted", Toast.LENGTH_LONG).show();
                backToList();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }


    }

    private void saveDeal()
    {
        deal.setTitle(txtTitle.getText().toString());
        deal.setDescription(txtDescription.getText().toString());
        deal.setPrice(txtPrice.getText().toString());

        if(deal.getId() == null){
            mDatabaseReference.push().setValue(deal);
        }
        else {
            mDatabaseReference.child(deal.getId()).setValue(deal);
        }

       // TravelDeal deal = new TravelDeal(title,description,price,"");
       // mDatabaseReference.push().setValue(deal);

    }

    private void deleteDeal() {
        if(deal ==null){
            Toast.makeText(this, "Please save the deal before deleting", Toast.LENGTH_SHORT).show();
            return;
        }
        mDatabaseReference.child(deal.getId()).removeValue();

        if(deal.getImageName() != null && deal.getImageName().isEmpty() == false){
            // Create a storage reference to folder in storage and then child which will be image name
            StorageReference storageRef = FirebaseUtil.mFirebaseStorage.getReference()
                    .child("deals_pictures").child(deal.getImageName());

            storageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("Storage", "Success");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("Storage", e.getMessage().toString());
                }
            });
        }
    }


    private void backToList(){
        Intent intent = new Intent(this, ListActivity.class);
        startActivity(intent);
    }

    private void clean()
    {
        txtTitle.setText("");
        txtPrice.setText("");
        txtDescription.setText("");
        txtTitle.requestFocus();

    }

    private void enableText(boolean isEnabled)
    {
        txtTitle.setEnabled(isEnabled);
        txtPrice.setEnabled(isEnabled);
        txtDescription.setEnabled(isEnabled);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.save_menu, menu);
        if(FirebaseUtil.isAdmin == true){
           menu.findItem(R.id.delete_menu).setVisible(true);
            menu.findItem(R.id.save_menu).setVisible(true);
            enableText(true);
            btnImage.setEnabled(true);
        }
        else{
            menu.findItem(R.id.delete_menu).setVisible(false);
            menu.findItem(R.id.save_menu).setVisible(false);
            enableText(false);
            btnImage.setEnabled(false);

        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FirebaseUtil.RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // Sign-in succeeded, set up the UI
                Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                // Sign in was canceled by the user, finish the activity
                Toast.makeText(this, "Sign in canceled", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        //The photo picker result uri
        else if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {

            //choosen photo will come as a uri
            Uri selectedImageUri = data.getData();

            //child making reference to last child of uri path
             final StorageReference photoRef = FirebaseUtil.mPhotosStorageReference.child(selectedImageUri.getLastPathSegment());

            //Upload file to firebase storage
            photoRef.putFile(selectedImageUri).continueWithTask(new Continuation<TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    //return FirebaseUtil.mPhotosStorageReference.getDownloadUrl();
                    return photoRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        // When the image has successfully uploaded, we get its download URL
                        Uri downloadUri = task.getResult();
                        //Long tsLong = System.currentTimeMillis()/1000;
                        String pictureName = photoRef.getName();

                        //Here deal is just a pojo class name
                        //We save picture url so we can display image
                        deal.setImageUrl(downloadUri.toString());
                        //we save image name so that we can delete image without the name we can delete it from path
                        deal.setImageName(pictureName);

                        Log.d("Storage url", downloadUri.toString());
                        Log.d("Storage name", pictureName);
                        showImage(downloadUri.toString());

                    }
                }
            });
        }
    }
}
