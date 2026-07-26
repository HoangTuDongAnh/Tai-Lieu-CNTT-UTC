package com.example.contactadvance.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contactadvance.R;
import com.example.contactadvance.models.Contact;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> implements Filterable {

    private Context context;
    private List<Contact> contactList;
    private List<Contact> contactListFull;

    public ContactAdapter(Context context, List<Contact> contactList) {
        this.context = context;
        this.contactList = contactList;
        this.contactListFull = new ArrayList<>(contactList);
    }

    public void updateData(List<Contact> newList) {
        contactList.clear();
        contactList.addAll(newList);

        contactListFull.clear();
        contactListFull.addAll(newList);

        notifyDataSetChanged();
    }

    public List<Contact> getContactList() {
        return contactList;
    }

    public void sortByName() {
        Collections.sort(contactList, new Comparator<Contact>() {
            @Override
            public int compare(Contact c1, Contact c2) {
                return c1.getName().compareToIgnoreCase(c2.getName());
            }
        });
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact contact = contactList.get(position);

        holder.tvName.setText(contact.getName());
        holder.tvPhone.setText(contact.getPhone());

        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(contact.isChecked());

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            contact.setChecked(isChecked);
        });

        holder.imgCall.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + contact.getPhone()));
            context.startActivity(intent);
        });

        holder.imgSms.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("sms:" + contact.getPhone()));
            context.startActivity(intent);
        });

        holder.imgAvatar.setImageResource(android.R.drawable.ic_menu_report_image);
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar, imgCall, imgSms;
        TextView tvName, tvPhone;
        CheckBox checkBox;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            imgCall = itemView.findViewById(R.id.imgCall);
            imgSms = itemView.findViewById(R.id.imgSms);
            tvName = itemView.findViewById(R.id.tvName);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            checkBox = itemView.findViewById(R.id.chkSelect);
        }
    }

    @Override
    public Filter getFilter() {
        return contactFilter;
    }

    private final Filter contactFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Contact> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(contactListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Contact contact : contactListFull) {
                    if (contact.getName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(contact);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            contactList.clear();
            contactList.addAll((List<Contact>) results.values);
            notifyDataSetChanged();
        }
    };
}