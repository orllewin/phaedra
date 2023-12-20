package oppen.phaedra.bookmarks;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import oppen.phaedra.R;


public class BookmarksAdapter extends ArrayAdapter<Bookmark> {

    public interface Listener{
        void go(String address);
        void overflow(Bookmark bookmark, View view);
    }

    private Listener listener;

    public BookmarksAdapter(Context context, ArrayList<Bookmark> bookmarks, Listener listener){
        super(context, 0, bookmarks);
        this.listener = listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Bookmark bookmark = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_bookmark, parent, false);
        }

        TextView title = convertView.findViewById(R.id.title);
        title.setText(bookmark.title);

        TextView address = convertView.findViewById(R.id.address);
        address.setText(bookmark.address);

        ImageButton overflow = convertView.findViewById(R.id.overflow);
        overflow.setOnClickListener(v -> listener.overflow(bookmark, overflow));

        convertView.setOnClickListener(v -> listener.go(bookmark.address));

        return convertView;
    }
}
