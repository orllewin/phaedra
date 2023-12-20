package oppen.phaedra.bookmarks;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.ArrayList;
import oppen.phaedra.R;


public class BookmarksDialog {

    public interface Listener{
        void go(String address);
    }

    private final Dialog dialog;
    private final ListView bookmarksListView;
    private final Listener listener;
    private Bookmarks bm;

    public BookmarksDialog(Context context, SharedPreferences prefs, Listener listener){
        this.listener = listener;
        dialog = new Dialog(context, R.style.oppen_dialog);
        dialog.setContentView(R.layout.dialog_bookmarks);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

        ImageButton close = dialog.findViewById(R.id.close_button);
        close.setOnClickListener(v -> dialog.dismiss());

        bookmarksListView = dialog.findViewById(R.id.bookamrks_list_view);

        bm = new Bookmarks(prefs, new Bookmarks.Listener() {
            @Override
            public void onBookmarkAdded() {
                //unused
            }

            @Override
            public void onBookmarks(ArrayList<Bookmark> bookmarks) {
                setAdapter(bookmarks);
            }

            @Override
            public void onBookmarkDeleted() {
                bm.getBookmarks();
            }
        });

        bm.getBookmarks();
    }

    private void setAdapter(ArrayList<Bookmark> bookmarks) {
        BookmarksAdapter adapter = new BookmarksAdapter(dialog.getContext(), bookmarks, new BookmarksAdapter.Listener() {
            @Override
            public void go(String address) {
                listener.go(address);
                dialog.dismiss();
            }

            @Override
            public void overflow(Bookmark bookmark, View view) {
                PopupWindow overflow = new PopupWindow(dialog.getContext(), null, R.style.Theme_Phaedra);
                LinearLayout layout = (LinearLayout) View.inflate(dialog.getContext(), R.layout.bookmark_overflow, null);
                overflow.setContentView(layout);
                TextView delete = layout.findViewById(R.id.delete);
                delete.setOnClickListener(v -> {
                    overflow.dismiss();
                    bm.deleteBookmark(bookmark);
                });
                overflow.showAsDropDown(view);
            }
        });

        bookmarksListView.setAdapter(adapter);
    }

    public void show(){
        dialog.show();
    }
}
