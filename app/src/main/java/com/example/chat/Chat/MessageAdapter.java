package com.example.chat.Chat;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.chat.R;

import java.util.List;

class MessageAdapter extends ArrayAdapter<Message> {
    private List<Message> chatmessages;
    private Activity act;

    public MessageAdapter(Activity context, int res, List<Message> messagesList) {
        super(context, res, messagesList);
        act = context;
        chatmessages = messagesList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) act.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        int layoutRes = R.layout.own_bubble;

        Message message = getItem(position);
        String text = message.getText();
        String sender = message.getSender();
        String time = message.getTime();

        layoutRes = !message.isOwned() ? R.layout.foreign_bubble : layoutRes;

        if (convertView == null) {
            convertView = layoutInflater.inflate(layoutRes, parent, false);

            TextView textView = convertView.findViewById(R.id.msg);
            textView.setText(text);

            TextView timeView = convertView.findViewById(R.id.time_stamp);
            timeView.setText(time);
            if(!message.isOwned()) {
                TextView senderView = convertView.findViewById(R.id.sender);
                senderView.setText(sender);
            }
        } else {
            TextView textView = convertView.findViewById(R.id.msg);
            textView.setText(text);

            TextView timeView = convertView.findViewById(R.id.time_stamp);
            timeView.setText(time);

            if(!message.isOwned()) {
                TextView senderView = convertView.findViewById(R.id.sender);
                senderView.setText(sender);
            }
        }

        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).isOwned() ? 0 : 1;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }
}
