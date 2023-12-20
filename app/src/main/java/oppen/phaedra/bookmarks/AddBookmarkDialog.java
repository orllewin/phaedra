package oppen.phaedra.bookmarks;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import oppen.phaedra.R;


public class AddBookmarkDialog {

    private final Dialog dialog;

    public AddBookmarkDialog(Context context, String title, String address, SharedPreferences prefs){
        dialog = new Dialog(context, R.style.oppen_dialog);
        dialog.setContentView(R.layout.dialog_add_bookmark);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

        ImageButton close = dialog.findViewById(R.id.close_button);
        close.setOnClickListener(v -> dialog.dismiss());

        EditText titleEdit = dialog.findViewById(R.id.bookmark_title);
        titleEdit.setText(title);

        EditText addressEdit = dialog.findViewById(R.id.bookmark_address);
        addressEdit.setText(address);

        ImageButton save = dialog.findViewById(R.id.save_button);
        save.setOnClickListener(v -> {
            Bookmarks bookmarks = new Bookmarks(prefs, new Bookmarks.Listener() {
                @Override
                public void onBookmarkAdded() {
                    Toast.makeText(context, "Bookmark saved", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }

                @Override
                public void onBookmarks(ArrayList<Bookmark> bookmarks) {
                    //unused
                }

                @Override
                public void onBookmarkDeleted() {
                    //unused
                }
            });
            bookmarks.addBookmark(addressEdit.getText().toString(), titleEdit.getText().toString());
        });
    }

    public void show(){
        dialog.show();
    }
}
