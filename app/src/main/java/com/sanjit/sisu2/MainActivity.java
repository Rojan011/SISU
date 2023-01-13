package com.sanjit.sisu2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sanjit.sisu2.adapters.sec_doc;
import com.sanjit.sisu2.databinding.ActivityMainBinding;
import com.sanjit.sisu2.ui.Setting;
import com.sanjit.sisu2.ui.agalleryf.agallery;
import com.sanjit.sisu2.ui.login_register_user.Doctor_info;
import com.sanjit.sisu2.ui.login_register_user.Login;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements sec_doc.sec_doc_listener {

    //declarations
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    //declarations


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        //binding is replacement of view for easier access to layout

        setContentView(binding.getRoot());
        setSupportActionBar(binding.appBarMain.toolbar);
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        mAuth = FirebaseAuth.getInstance();

        db.collection("Users").document(mAuth.getCurrentUser().getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                       try {
                           String spec_doc = documentSnapshot.getString("spec");
                           String dName = documentSnapshot.getString("Fullname");
                           String dEmail = documentSnapshot.getString("Email");
                           Log.v("TAG", "onSuccess: " + spec_doc + dName + dEmail);
                           if (spec_doc == null) {

                               // making a new collection with information of doctor in firebase
                               Doctor_info doctor_info = new Doctor_info(dName, dEmail, "null");
                               db.collection("Doctors").document(mAuth.getCurrentUser().getUid()).set(doctor_info)
                                       .addOnSuccessListener(new OnSuccessListener<Void>() {
                                           @Override
                                           public void onSuccess(Void aVoid) {
                                               Toast.makeText(MainActivity.this, "Doctor info added", Toast.LENGTH_SHORT).show();
                                           }
                                       }).addOnFailureListener(new OnFailureListener() {
                                           @Override
                                           public void onFailure(@NonNull Exception e) {
                                               Toast.makeText(MainActivity.this, "Doctor info not added", Toast.LENGTH_SHORT).show();
                                           }
                               });

                               //calling a dialog box to get specialization of doctor
                               sec_doc sec_doc = new sec_doc();
                               sec_doc.show(getSupportFragmentManager(), "sec_doc");

                           }
                       }
                       catch (Exception e){
                           Toast.makeText(MainActivity.this, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                       }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                    }
                });

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home,
                R.id.nav_disease_information,
                R.id.nav_vaccine_information,
                R.id.nav_personal_records,
                R.id.nav_childcare_centres,
                R.id.nav_appointments,
                R.id.nav_about_us)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        db.collection("Users").document(mAuth.getCurrentUser().getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String user_mode = documentSnapshot.getString("user_mode");
                        if (!user_mode.equals("Doctor")){
                            Menu menu = navigationView.getMenu();
                            MenuItem nav_appointments = menu.findItem(R.id.nav_appointments);
                            nav_appointments.setVisible(false);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openDialog() {
        sec_doc sec_doc = new sec_doc();
        sec_doc.show(getSupportFragmentManager(),"sec_doc");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, Setting.class);
                startActivity(intent);
                return true;

            case R.id.action_logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), Login.class));
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void applyTexts(String spec_doc) {
        Toast.makeText(this, "Specialist Doctor: "+spec_doc, Toast.LENGTH_SHORT).show();
        db.collection("Users").document(mAuth.getCurrentUser().getUid()).update("spec",spec_doc);
        db.collection("Doctors").document(mAuth.getCurrentUser().getUid()).update("spec",spec_doc);
    }
}