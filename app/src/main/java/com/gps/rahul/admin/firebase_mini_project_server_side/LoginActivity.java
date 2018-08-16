package com.gps.rahul.admin.firebase_mini_project_server_side;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.gps.rahul.admin.firebase_mini_project_server_side.Common.Common;
import com.gps.rahul.admin.firebase_mini_project_server_side.Model.LoginModel;

public class LoginActivity extends AppCompatActivity {
    EditText edt_mobile_number,edt_password;
    Button btn_submit;
    DatabaseReference databaseReference;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        edt_mobile_number=(EditText)findViewById(R.id.edt_mobile_number);
        edt_password=(EditText)findViewById(R.id.edt_password);
        btn_submit=(Button)findViewById(R.id.btn_submit);
        databaseReference= FirebaseDatabase.getInstance().getReference("User");
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Loginsubmit(edt_mobile_number.getText().toString(),edt_password.getText().toString());
            }
        });
    }
    private void Loginsubmit(String mb, String pwd) {

        if(edt_mobile_number.getText().toString().isEmpty())
        {
            Toast.makeText(this, "Please Enter the Mobile Number", Toast.LENGTH_SHORT).show();
        }
        else if(edt_password.getText().toString().isEmpty())
        {
            Toast.makeText(this, "Please Enter the Password", Toast.LENGTH_SHORT).show();
        }
        else {
            progressDialog = new ProgressDialog(LoginActivity.this);
            progressDialog.setMessage("Please Waiting");
            progressDialog.show();

            final String localPhone = mb;
            final String localPassword = pwd;
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //Check if user not exits in database
                    if (dataSnapshot.child(localPhone).exists()) {
                        progressDialog.dismiss();
                        //Get User Information
                        LoginModel loginModel = dataSnapshot.child(localPhone).getValue(LoginModel.class);
                        loginModel.setPhone(localPhone); //Set Phone
                        if (Boolean.parseBoolean(loginModel.getIsStaff())) // IsStaff == true
                        {
                            if (loginModel.getPassword().equals(localPassword)) {
                                Toast.makeText(LoginActivity.this, "Login Successfully", Toast.LENGTH_SHORT).show();
                                edt_mobile_number.setText("");
                                edt_password.setText("");
                                Intent i = new Intent(LoginActivity.this, Home_Navigation.class);
                                Common.currentuser = loginModel;
                                startActivity(i);
                            } else {
                                Toast.makeText(LoginActivity.this, "Wrong Password", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "Please Login With Staff Account", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, "User Not Exits In Database", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
}
