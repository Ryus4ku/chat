package com.example.chat.Chat;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.chat.R;

import java.util.List;

class ChatMessageAdapter extends ArrayAdapter<ChatMessage> {
    private List<ChatMessage> chatmessages;
    private Activity act;

    public ChatMessageAdapter(Activity context, int res, List<ChatMessage> messagesList) {
        super(context, res, messagesList);
        act = context;
        chatmessages = messagesList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) act.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        int layoutRes = R.layout.own_bubble;

        ChatMessage chatMessage = getItem(position);
        String text = chatMessage.getText();
        String sender = chatMessage.getSender();
        String time = chatMessage.getTime();

        layoutRes = !chatMessage.isOwned() ? R.layout.foreign_bubble : layoutRes;

        if (convertView == null) {
            convertView = layoutInflater.inflate(layoutRes, parent, false);

            TextView textView = convertView.findViewById(R.id.msg);
            textView.setText(text);

            TextView timeView = convertView.findViewById(R.id.time_stamp);
            timeView.setText(time);
            if(!chatMessage.isOwned()) {
                TextView senderView = convertView.findViewById(R.id.sender);
                senderView.setText(sender);
            }
        } else {
            TextView textView = convertView.findViewById(R.id.msg);
            textView.setText(text);

            TextView timeView = convertView.findViewById(R.id.time_stamp);
            timeView.setText(time);

            if(!chatMessage.isOwned()) {
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
