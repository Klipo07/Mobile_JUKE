package com.example.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.File;
import java.util.List;

public class AuthorAdapter extends BaseAdapter {
    private Context context;
    private List<Author> authors;

    public AuthorAdapter(Context context, List<Author> authors) {
        this.context = context;
        this.authors = authors;
    }

    @Override
    public int getCount() {
        return authors.size();
    }

    @Override
    public Object getItem(int position) {
        return authors.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_author, parent, false);
            holder = new ViewHolder();
            holder.photo = convertView.findViewById(R.id.imageViewAuthorPhoto);
            holder.name = convertView.findViewById(R.id.textViewAuthorName);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Author author = authors.get(position);
        

        if (author.photoPath != null && !author.photoPath.isEmpty()) {

            File imageFile = new File(author.photoPath);
            if (imageFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(author.photoPath);
                if (bitmap != null) {
                    holder.photo.setImageBitmap(bitmap);
                } else {
                    holder.photo.setImageResource(author.photoResId);
                }
            } else {
                holder.photo.setImageResource(author.photoResId);
            }
        } else {
            holder.photo.setImageResource(author.photoResId);
        }
        
        holder.name.setText(author.name);

        return convertView;
    }

    static class ViewHolder {
        ImageView photo;
        TextView name;
    }
}
