package reti.com.passwordmanager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import reti.com.passwordmanager.models.PasswordEntry;

public class PasswordAdapter extends ArrayAdapter<PasswordEntry> {


    public PasswordAdapter(Context context, int resource) {
        super(context, resource);
    }

    public PasswordAdapter(Context context, int resource, List<PasswordEntry> list){
        super(context, resource,list);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.listview_adapter_passwordentry, null);
        }
        PasswordEntry p = getItem(position);

        if (p != null) {
            TextView domain = (TextView) v.findViewById(R.id.tv_domain_adapter);
            TextView username = (TextView) v.findViewById(R.id.tv_username_adapter);
            ImageView iv_backDomain = (ImageView) v.findViewById(R.id.iv_backDomain);
            ImageView iv_backUsername = (ImageView)v.findViewById(R.id.iv_backUser);
            //TextView password = (TextView) v.findViewById(R.id.tv_password_adapter);

            if (domain != null) {
                domain.setText(p.getDominio());
            }

            if (username != null) {
                username.setText(p.getUsername());
            }

/*            if (password != null) {
                password.setText(p.getPassword());
            }*/
        }

        return v;
    }
}
