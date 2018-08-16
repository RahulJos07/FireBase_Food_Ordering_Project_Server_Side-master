package com.gps.rahul.admin.firebase_mini_project_server_side;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.gps.rahul.admin.firebase_mini_project_server_side.Common.Common;
import com.gps.rahul.admin.firebase_mini_project_server_side.Interface.ItemClickListener;
import com.gps.rahul.admin.firebase_mini_project_server_side.Model.CategoryModel;
import com.gps.rahul.admin.firebase_mini_project_server_side.Model.FoodModel;
import com.gps.rahul.admin.firebase_mini_project_server_side.ViewHolder.FoodViewHolder;
import com.gps.rahul.admin.firebase_mini_project_server_side.ViewHolder.ItemOffsetDecoration;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FoodListActivity extends AppCompatActivity {
    RecyclerView food_recycler_view;
    DatabaseReference databaseReference;
    String CategoryId="";
    FirebaseRecyclerAdapter<FoodModel,FoodViewHolder> adapter;
    RelativeLayout rootLayout;
    //Search Functionality
    FirebaseRecyclerAdapter<FoodModel,FoodViewHolder> searchadapter;
    List<String> suggestList=new ArrayList<>();
    //MaterialSearchBar materialSearchBar;

    //For Add Menu
    StorageReference storageReference;
    EditText edtName_food,edtDescription_food,edtPrice_food,edtDiscount_food;
    Button btn_select_food,btn_upload_food;
    FoodModel newFoodModel;
    Uri saveUri;
    DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);
        getSupportActionBar().hide();
        databaseReference= FirebaseDatabase.getInstance().getReference("Food");
        storageReference= FirebaseStorage.getInstance().getReference();

        food_recycler_view=(RecyclerView) findViewById(R.id.food_recycler_view);
        food_recycler_view.setHasFixedSize(true);
        food_recycler_view.setLayoutManager(new GridLayoutManager(FoodListActivity.this, 2));
        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(FoodListActivity.this, R.dimen.activity_horizontal_margin);
        food_recycler_view.addItemDecoration(itemDecoration);

        rootLayout=(RelativeLayout)findViewById(R.id.rootLayout);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_food);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });

        //GetIntent
        if(getIntent()!=null)
        {
            CategoryId=getIntent().getStringExtra("CategoryId");
        }
        if(!CategoryId.isEmpty() && CategoryId!=null)
        {
            loadFood(CategoryId);
        }
    }
    private void showDialog() {
        AlertDialog.Builder alertDialog=new AlertDialog.Builder(FoodListActivity.this);
        alertDialog.setTitle("Add New Food");
        alertDialog.setMessage("Please Fill Full Information");
        LayoutInflater inflater=this.getLayoutInflater();
        View add_menu_layout=inflater.inflate(R.layout.add_new_food,null);
        edtName_food=add_menu_layout.findViewById(R.id.edtName_food);
        edtDescription_food=add_menu_layout.findViewById(R.id.edtDescription_food);
        edtPrice_food=add_menu_layout.findViewById(R.id.edtPrice_food);
        edtDiscount_food=add_menu_layout.findViewById(R.id.edtDiscount_food);

        btn_select_food=add_menu_layout.findViewById(R.id.btn_select_food);
        btn_upload_food=add_menu_layout.findViewById(R.id.btn_upload_food);

        btn_select_food.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage(); // Let User Select image from Gallery and save Uri From of this image
            }
        });
        btn_upload_food.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
            }
        });
        alertDialog.setView(add_menu_layout);
        alertDialog.setIcon(R.drawable.shopping_cart);
        //Set Button
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                //Here Just Create New Category
                if(newFoodModel!=null)
                {
                    databaseReference.push().setValue(newFoodModel);
                    Snackbar.make(rootLayout,"New Food "+newFoodModel.getName()+" was added",Snackbar.LENGTH_SHORT).show();
                }
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
    private void uploadImage() {
        if(saveUri!=null)
        {
            final ProgressDialog dialog=new ProgressDialog(FoodListActivity.this);
            dialog.setMessage("Uploading");
            dialog.show();
            String imageName= UUID.randomUUID().toString();
            final StorageReference imageFolder=storageReference.child("Food_images/"+imageName);
            imageFolder.putFile(saveUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    dialog.dismiss();
                    Toast.makeText(FoodListActivity.this, "Uploaded !!!", Toast.LENGTH_SHORT).show();
                    imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            //Set Value for newCategory if image Upload and we can get download link
                            newFoodModel=new FoodModel();
                            newFoodModel.setName(edtName_food.getText().toString());
                            newFoodModel.setDescription(edtDescription_food.getText().toString());
                            newFoodModel.setPrice(edtPrice_food.getText().toString());
                            newFoodModel.setDiscount(edtDiscount_food.getText().toString());
                            newFoodModel.setMenuId(CategoryId);
                            newFoodModel.setImage(uri.toString());
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    dialog.dismiss();
                    Toast.makeText(FoodListActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress=(100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    dialog.setMessage("Uploaded "+progress+"%");
                }
            });
        }
    }

    // Press Ctrl + O

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Common.PICK_IMAGE_REQUEST && resultCode==RESULT_OK  && data!=null && data.getData()!=null)
        {
            saveUri=data.getData();
            btn_select_food.setText("Image Selected");
        }
    }

    private void chooseImage() {
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"SelectPicture"),Common.PICK_IMAGE_REQUEST);
    }

    private void loadFood(String categoryId) {
        adapter=new FirebaseRecyclerAdapter<FoodModel, FoodViewHolder>(FoodModel.class,
                R.layout.food_item,
                FoodViewHolder.class,
                databaseReference.orderByChild("menuId").equalTo(CategoryId) // like : Select * from Foods where MenuId =
            ) {
            @Override
            protected void populateViewHolder(FoodViewHolder viewHolder, FoodModel model, int position) {
                viewHolder.food_name.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage()).into(viewHolder.food_image);
                final FoodModel local=model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onclick(View view, int position, boolean isLongClick) {
                        //Toast.makeText(FoodListActivity.this, ""+local.getName(), Toast.LENGTH_SHORT).show();
                        /*Intent fooddetail=new Intent(FoodListActivity.this,FoodDetails.class);
                        fooddetail.putExtra("FoodId",adapter.getRef(position).getKey());
                        startActivity(fooddetail);*/
                    }
                });
            }
        };
        adapter.notifyDataSetChanged();
        food_recycler_view.setAdapter(adapter);
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

    private void deleteCategory(String key) {
        databaseReference.child(key).removeValue();
        Toast.makeText(this, "Item Deleted !!!!", Toast.LENGTH_SHORT).show();
    }

    private void showUpdateDialog(final String key, final FoodModel item) {
        //Just copy code from showAddDialog and Modify
        AlertDialog.Builder alertDialog=new AlertDialog.Builder(FoodListActivity.this);
        alertDialog.setTitle("Update Food");
        alertDialog.setMessage("Please Fill Full Information");
        LayoutInflater inflater=this.getLayoutInflater();
        View add_menu_layout=inflater.inflate(R.layout.add_new_food,null);
        edtName_food=add_menu_layout.findViewById(R.id.edtName_food);
        edtDescription_food=add_menu_layout.findViewById(R.id.edtDescription_food);
        edtPrice_food=add_menu_layout.findViewById(R.id.edtPrice_food);
        edtDiscount_food=add_menu_layout.findViewById(R.id.edtDiscount_food);

        btn_select_food=add_menu_layout.findViewById(R.id.btn_select_food);
        btn_upload_food=add_menu_layout.findViewById(R.id.btn_upload_food);

        //Set Default Name
        edtName_food.setText(item.getName());
        edtDescription_food.setText(item.getDescription());
        edtPrice_food.setText(item.getPrice());
        edtDiscount_food.setText(item.getDiscount());

        btn_select_food.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage(); // Let User Select image from Gallery and save Uri From of this image
            }
        });
        btn_upload_food.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeImage(item);
            }
        });
        alertDialog.setView(add_menu_layout);
        alertDialog.setIcon(R.drawable.shopping_cart);
        //Set Button
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                //Here Just Create New Category
                //Update Information
                item.setName(edtName_food.getText().toString());
                item.setDescription(edtDescription_food.getText().toString());
                item.setPrice(edtPrice_food.getText().toString());
                item.setDiscount(edtDiscount_food.getText().toString());
                databaseReference.child(key).setValue(item);
                Snackbar.make(rootLayout,"Food "+item.getName()+" was edited",Snackbar.LENGTH_SHORT).show();
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

    private void changeImage(final FoodModel item) {
        if(saveUri!=null)
        {
            final ProgressDialog dialog=new ProgressDialog(FoodListActivity.this);
            dialog.setMessage("Uploading");
            dialog.show();
            String imageName= UUID.randomUUID().toString();
            final StorageReference imageFolder=storageReference.child("Food_images/"+imageName);
            imageFolder.putFile(saveUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    dialog.dismiss();
                    Toast.makeText(FoodListActivity.this, "Uploaded !!!", Toast.LENGTH_SHORT).show();
                    imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            //Set Value for newCategory if image Upload and we can get download link
                            item.setImage(uri.toString());
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    dialog.dismiss();
                    Toast.makeText(FoodListActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress=(100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    dialog.setMessage("Uploaded "+progress+"%");
                }
            });
        }
    }
}
