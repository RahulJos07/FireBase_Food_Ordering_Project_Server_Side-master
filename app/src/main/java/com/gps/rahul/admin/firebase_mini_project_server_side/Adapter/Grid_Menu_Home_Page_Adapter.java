package com.gps.rahul.admin.firebase_mini_project_server_side.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gps.rahul.admin.firebase_mini_project_server_side.Model.CategoryModel;
import com.gps.rahul.admin.firebase_mini_project_server_side.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class Grid_Menu_Home_Page_Adapter extends BaseAdapter{
    Context context;
    int menu_item;
    List<CategoryModel> categoryModels;
    public Grid_Menu_Home_Page_Adapter(Context context, int menu_item, List<CategoryModel> categoryModels) {
        this.context=context;
        this.menu_item=menu_item;
        this.categoryModels=categoryModels;
    }

    @Override
    public int getCount() {
        return categoryModels.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view= LayoutInflater.from(context).inflate(menu_item,viewGroup,false);
        TextView menu_name=(TextView)view.findViewById(R.id.menu_name);
        ImageView menu_image=(ImageView)view.findViewById(R.id.menu_image);
        menu_name.setText(categoryModels.get(i).getName());
        Picasso.with(context).load(categoryModels.get(i).getImage()).into(menu_image);
        return view;
    }
}
