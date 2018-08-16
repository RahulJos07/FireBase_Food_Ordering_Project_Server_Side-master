package com.gps.rahul.admin.firebase_mini_project_server_side;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.gps.rahul.admin.firebase_mini_project_server_side.Common.Common;
import com.gps.rahul.admin.firebase_mini_project_server_side.Interface.ItemClickListener;
import com.gps.rahul.admin.firebase_mini_project_server_side.Model.CategoryModel;
import com.gps.rahul.admin.firebase_mini_project_server_side.Service.ListenOrder;
import com.gps.rahul.admin.firebase_mini_project_server_side.ViewHolder.ItemOffsetDecoration;
import com.gps.rahul.admin.firebase_mini_project_server_side.ViewHolder.MenuViewHolder;
import com.squareup.picasso.Picasso;

import java.util.UUID;


public class Home_Navigation extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    TextView txt_title;
    RecyclerView home_page_recycler_view;
    RecyclerView.LayoutManager layoutManager;
    DatabaseReference databaseReference;
    String mCurrent_user_id;
    FirebaseRecyclerAdapter<CategoryModel,MenuViewHolder> adapter;

    //For Add Menu
    StorageReference storageReference;
    EditText edtName;
    Button btn_select,btn_upload;
    CategoryModel newCategoryModel;
    Uri saveUri;
    DrawerLayout drawer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home__navigation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Menu Management");
        setSupportActionBar(toolbar);

        databaseReference= FirebaseDatabase.getInstance().getReference("Category");
        storageReference=FirebaseStorage.getInstance().getReference();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Set Name For user
        txt_title=(TextView)navigationView.getHeaderView(0).findViewById(R.id.txt_title);
        txt_title.setText(Common.currentuser.getName());

        //Load Menu
        home_page_recycler_view=(RecyclerView) findViewById(R.id.home_page_recycler_view);
        home_page_recycler_view.setHasFixedSize(true);

        home_page_recycler_view.setLayoutManager(new GridLayoutManager(Home_Navigation.this, 2));
        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(Home_Navigation.this, R.dimen.activity_horizontal_margin);
        home_page_recycler_view.addItemDecoration(itemDecoration);

        //home_page_recycler_view.setLayoutManager(new LinearLayoutManager(Home_Page_Navigation.this));
        //home_page_recycler_view.setLayoutManager(new GridLayoutManager(this, 2));
        loadMenu();

        //Call Service

        Intent service=new Intent(Home_Navigation.this, ListenOrder.class);
        startService(service);

    }

    private void showDialog() {
        AlertDialog.Builder alertDialog=new AlertDialog.Builder(Home_Navigation.this);
        alertDialog.setTitle("Add New Category");
        alertDialog.setMessage("Please Fill Full Information");
        LayoutInflater inflater=this.getLayoutInflater();
        View add_menu_layout=inflater.inflate(R.layout.add_new_menu,null);
        edtName=add_menu_layout.findViewById(R.id.edtName);
        btn_select=add_menu_layout.findViewById(R.id.btn_select);
        btn_upload=add_menu_layout.findViewById(R.id.btn_upload);

        btn_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage(); // Let User Select image from Gallery and save Uri From of this image
            }
        });
        btn_upload.setOnClickListener(new View.OnClickListener() {
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
                if(newCategoryModel!=null)
                {
                    databaseReference.push().setValue(newCategoryModel);
                    Snackbar.make(drawer,"New category "+newCategoryModel.getName()+" was added",Snackbar.LENGTH_SHORT).show();
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
            final ProgressDialog dialog=new ProgressDialog(Home_Navigation.this);
            dialog.setMessage("Uploading");
            dialog.show();
            String imageName= UUID.randomUUID().toString();
            final StorageReference imageFolder=storageReference.child("Menu_images/"+imageName);
            imageFolder.putFile(saveUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    dialog.dismiss();
                    Toast.makeText(Home_Navigation.this, "Uploaded !!!", Toast.LENGTH_SHORT).show();
                    imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            //Set Value for newCategory if image Upload and we can get download link
                            newCategoryModel=new CategoryModel(edtName.getText().toString(),uri.toString());
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    dialog.dismiss();
                    Toast.makeText(Home_Navigation.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
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
            btn_select.setText("Image Selected");
        }
    }

    private void chooseImage() {
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"SelectPicture"),Common.PICK_IMAGE_REQUEST);
    }

    private void loadMenu() {
        adapter=new FirebaseRecyclerAdapter<CategoryModel, MenuViewHolder>(CategoryModel.class,R.layout.menu_item,MenuViewHolder.class,databaseReference) {
            @Override
            protected void populateViewHolder(MenuViewHolder viewHolder, CategoryModel model, int position) {
                viewHolder.menu_name.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage()).into(viewHolder.menu_image);
                final CategoryModel categoryModel=model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onclick(View view, int position, boolean isLongClick) {
                        //Toast.makeText(Home_Page_Navigation.this, ""+categoryModel.getName(), Toast.LENGTH_SHORT).show();
                        Intent foodlist=new Intent(Home_Navigation.this,FoodListActivity.class);
                        foodlist.putExtra("CategoryId",adapter.getRef(position).getKey());
                        startActivity(foodlist);
                    }
                });
            }
        };
        adapter.notifyDataSetChanged();
        home_page_recycler_view.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        if(!drawer.isDrawerOpen(GravityCompat.START)) {
            new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Exit")
                    .setMessage("Are you sure you want to exit?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            moveTaskToBack(true);
                            android.os.Process.killProcess(android.os.Process.myPid());
                            System.exit(1);
                        }
                    }).setNegativeButton("No", null).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home__navigation, menu);
        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_menu) {
            // Handle the camera action
        } else if (id == R.id.nav_cart) {
            /*Intent i=new Intent(Home_Page_Navigation.this,Cart.class);
            startActivity(i);*/

        } else if (id == R.id.nav_orders) {
            Intent i=new Intent(Home_Navigation.this,OrderStatus.class);
            startActivity(i);
        } else if (id == R.id.nav_log_out) {
            /*Intent i=new Intent(Home_Page_Navigation.this,LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);*/
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
        DatabaseReference foods=FirebaseDatabase.getInstance().getReference("Foods");
        Query foodsInCategory=foods.orderByChild("menuId").equalTo(key);
        foodsInCategory.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               for(DataSnapshot postSnapShot:dataSnapshot.getChildren())
                {
                    postSnapShot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        databaseReference.child(key).removeValue();
        Toast.makeText(this, "Item Deleted !!!!", Toast.LENGTH_SHORT).show();
    }

    private void showUpdateDialog(final String key, final CategoryModel item) {
        //Just copy code from showAddDialog and Modify
        AlertDialog.Builder alertDialog=new AlertDialog.Builder(Home_Navigation.this);
        alertDialog.setTitle("Update Category");
        alertDialog.setMessage("Please Fill Full Information");
        LayoutInflater inflater=this.getLayoutInflater();
        View add_menu_layout=inflater.inflate(R.layout.add_new_menu,null);
        edtName=add_menu_layout.findViewById(R.id.edtName);
        btn_select=add_menu_layout.findViewById(R.id.btn_select);
        btn_upload=add_menu_layout.findViewById(R.id.btn_upload);

        //Set Default Name
        edtName.setText(item.getName());

        btn_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage(); // Let User Select image from Gallery and save Uri From of this image
            }
        });
        btn_upload.setOnClickListener(new View.OnClickListener() {
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
                //Update Information
                item.setName(edtName.getText().toString());
                databaseReference.child(key).setValue(item);
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

    private void changeImage(final CategoryModel item) {
        if(saveUri!=null)
        {
            final ProgressDialog dialog=new ProgressDialog(Home_Navigation.this);
            dialog.setMessage("Uploading");
            dialog.show();
            String imageName= UUID.randomUUID().toString();
            final StorageReference imageFolder=storageReference.child("Menu_images/"+imageName);
            imageFolder.putFile(saveUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    dialog.dismiss();
                    Toast.makeText(Home_Navigation.this, "Uploaded !!!", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(Home_Navigation.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
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
