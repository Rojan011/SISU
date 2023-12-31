package com.sanjit.sisu2.ui.appointments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sanjit.sisu2.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Map;
import java.lang.String;


public class appointments extends Fragment implements doctor_appointment_interface {

    ArrayList<appointment_model> appointment_model_arr = new ArrayList<>();
    private final FirebaseAuth mAuth=FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    public ArrayList<String> appointment_id = new ArrayList<>();
    private RecyclerView recyclerView;
    private final doctor_appointment_interface doctor_appointment_interface = this;

    public appointments() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_appointments, container, false);
        TextView doctor_name = view.findViewById(R.id.doctor_name);
        TextView doctor_spec = view.findViewById(R.id.doctor_spec);
        TextView doctor_email = view.findViewById(R.id.doctor_email);
        TextView doctor_phone = view.findViewById(R.id.doctor_phone);
        ImageView doctor_photo = view.findViewById(R.id.doctor_image);

        //setting up values from shared preferences

        SharedPreferences sharedPreferences = this.getActivity().getSharedPreferences("User", Context.MODE_PRIVATE);
        String Email = sharedPreferences.getString("Email", "Not Specified");
        String FullName = sharedPreferences.getString("FullName", "Not Specified");
        String ProfilePic = sharedPreferences.getString("ProfilePic", "Not Specified");
        String Specialization = sharedPreferences.getString("Specialization", "Not Specified");
        String Phone = sharedPreferences.getString("Phone", "Not Specified");

        //end of setting up values from shared preferences

        //here we are displaying the doctor details above the recycler view

        doctor_name.setText(FullName);
        doctor_spec.setText(Specialization);
        doctor_email.setText(Email);
        doctor_phone.setText(Phone);
        Picasso.get().load(ProfilePic).into(doctor_photo);

        //here we are displaying the doctor details above the recycler view

        //here we are fetching the appointments from the database and displaying them in the recycler view
        recyclerView = view.findViewById(R.id.appointments_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        db.collection("Doctors").document(mAuth.getCurrentUser().getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {

                    //getting appointment id from firebase
                    if(!(documentSnapshot==null)){
                        //log the document snapshot
                        Log.d("DocumentSnapshot", documentSnapshot.toString());
                        Map<String, Object> data = documentSnapshot.getData();

                        //check if appointment_id field is empty
                        if (data.get("appointment_id") != null) {
                            appointment_id = (ArrayList<String>) data.get("appointment_id");
                            Log.d("appointment_id", appointment_id.toString());
                        }

                    //getting appointment id from firebase
                    if(appointment_id != null)
                    {
                        Log.d("appointment_id not null", appointment_id.toString());
                        int count = 0;
                        for (String appointment : appointment_id) {

                            Log.d("2appointments", "fetching ids: " + appointment);

                            int finalCount = count;
                            db.collection("Users").document(appointment).get()
                                    .addOnSuccessListener(documentSnapshot1 -> {

                                        Map<String, Object> data1 = documentSnapshot1.getData();
                                        assert data1 != null;
                                        String name = (String) data1.get("Fullname");
                                        String phone = (String) data1.get("Telephone");
                                        String profile_pic = (String) data1.get("ProfilePic");
                                        String id= appointment;

                                        appointment_model_arr.add(new appointment_model(name, phone, profile_pic,id));
                                        Log.d("appointments", "onSuccess: " + name + " " + phone);

                                        //set adapter at the end of iteration

                                        if(finalCount == appointment_id.size()-1){
                                            appointmentsAdapter adapter = new appointmentsAdapter(getContext(), appointment_model_arr, doctor_appointment_interface);
                                            recyclerView.setAdapter(adapter);
                                            Log.d("adapter", "after success: " + appointment_model_arr);
                                        }

                                    })
                                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show());
                            count++;
                        }



                        Log.d("appointment_id", "is this first? " + appointment_id);
                    }
                }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show());

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onDoctorClick(int position) {
        Toast.makeText(getContext(), "Doctor Clicked", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCompletedClick(int position) {
        //send a notification to the patient via firebase about the appointment completion

        //remove the appointment from the doctor's appointment list
        removeAppointment(position);


    }

    private void removeAppointment(int position) {
        //remove the patient appointment from the doctor's appointment list

       String id = appointment_model_arr.get(position).getAppointment_id();
        appointment_id.remove(id);

        db.collection("Doctors").document(mAuth.getCurrentUser().getUid()).update("appointment_id", appointment_id);
        appointment_model_arr.remove(position);
        appointmentsAdapter adapter = new appointmentsAdapter(getContext(), appointment_model_arr, doctor_appointment_interface);
        recyclerView.setAdapter(adapter);


    }

    @Override
    public void onCancelClick(int position) {
        //send a notification to the patient via firebase about the appointment cancellation
        //remove the appointment from the doctor's appointment list
        removeAppointment(position);

    }
}