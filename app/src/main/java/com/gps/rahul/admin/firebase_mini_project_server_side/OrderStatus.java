package com.gps.rahul.admin.firebase_mini_project_server_side;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.gps.rahul.admin.firebase_mini_project_server_side.Common.Common;
import com.gps.rahul.admin.firebase_mini_project_server_side.Interface.ItemClickListener;
import com.gps.rahul.admin.firebase_mini_project_server_side.Model.CategoryModel;
import com.gps.rahul.admin.firebase_mini_project_server_side.Model.RequestModel;
import com.gps.rahul.admin.firebase_mini_project_server_side.ViewHolder.OrderViewHolder;
import com.jaredrummler.materialspinner.MaterialSpinner;

public class OrderStatus extends AppCompatActivity {
    public RecyclerView listorders;
    public RecyclerView.LayoutManager layoutManager;
    DatabaseReference databaseReference;
    FirebaseRecyclerAdapter<RequestModel,OrderViewHolder> adapter;
    MaterialSpinner status_Spinner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);
        databaseReference= FirebaseDatabase.getInstance().getReference("Requests");
        listorders=(RecyclerView)findViewById(R.id.listorders);
        listorders.setHasFixedSize(true);
        listorders.setLayoutManager(new LinearLayoutManager(OrderStatus.this));

        loadOrders();
    }

    private void loadOrders() {
        adapter=new FirebaseRecyclerAdapter<RequestModel, OrderViewHolder>(
                RequestModel.class,
                R.layout.order_layout,
                OrderViewHolder.class,
                databaseReference
        ) {
            @Override
            protected void populateViewHolder(OrderViewHolder viewHolder, final RequestModel model, int position) {
                viewHolder.order_id.setText(adapter.getRef(position).getKey());
                viewHolder.order_status.setText(Common.convertCodeToStatus(model.getStatus()));
                viewHolder.order_phone.setText(model.getPhone());
                viewHolder.order_address.setText(model.getAddress());
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onclick(View view, int position, boolean isLongClick) {
                        Intent i=new Intent(OrderStatus.this,TrackingOrder.class);
                        Common.currentRequest=model;
                        startActivity(i);
                    }
                });
            }
        };
        adapter.notifyDataSetChanged();
        listorders.setAdapter(adapter);
    }


    //Update / Delete
    //Press Ctrl + o

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getTitle().equals(Common.UPDATE))
        {
            showUpdateDialog(adapter.getRef(item.getOrder()).getKey(),adapter.getItem(item.getOrder()));
        }
        else if(item.getTitle().equals(Common.DELETE))
        {
            deleteCategory(adapter.getRef(item.getOrder()).getKey());
        }
        return super.onContextItemSelected(item);
    }

    private void showUpdateDialog(final String key, final RequestModel item) {
        //Just copy code from showAddDialog and Modify
        AlertDialog.Builder alertDialog=new AlertDialog.Builder(OrderStatus.this);
        alertDialog.setTitle("Update Order");
        alertDialog.setMessage("Please choose status");
        LayoutInflater inflater=this.getLayoutInflater();
        View add_menu_layout=inflater.inflate(R.layout.update_order_layout,null);
        status_Spinner=add_menu_layout.findViewById(R.id.status_Spinner);
        status_Spinner.setItems("Placed","On the way","Shipped");

        alertDialog.setView(add_menu_layout);
        final String localkey=key;
        //alertDialog.setIcon(R.drawable.shopping_cart);
        //Set Button
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                //Update Information
                item.setStatus(String.valueOf(status_Spinner.getSelectedIndex()));
                databaseReference.child(localkey).setValue(item);
            }
        });
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog.show();
    }

    private void deleteCategory(String key) {
        databaseReference.child(key).removeValue();
        Toast.makeText(this, "Item Deleted !!!!", Toast.LENGTH_SHORT).show();
    }


}
