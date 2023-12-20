package oppen.phaedra;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

public class MainMenu implements View.OnClickListener{

    private Context context;
    private SharedPreferences prefs;
    private PopupWindow window;
    private View anchor;
    private Listener listener;

    private LinearLayout layout;

    public MainMenu(Context context, SharedPreferences prefs, View anchor, Listener listener){
        this.context = context;
        this.prefs = prefs;
        this.anchor = anchor;
        this.listener = listener;

        window = new PopupWindow(context, null, R.style.Theme_Phaedra);
        layout = (LinearLayout) View.inflate(context, R.layout.main_menu, null);
        window.setContentView(layout);

        TextView addBookmark = layout.findViewById(R.id.add_bookmark);
        addBookmark.setOnClickListener(this);

        TextView bookmarks = layout.findViewById(R.id.bookmarks);
        bookmarks.setOnClickListener(this);

        TextView preferences = layout.findViewById(R.id.preferences);
        preferences.setOnClickListener(this);

        TextView about = layout.findViewById(R.id.about);
        about.setOnClickListener(this);

    }

    public void show(){
        layout.measure( View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED );
        int height = layout.getMeasuredHeight();
        int width = layout.getMeasuredWidth();
        window.setWidth( width );
        window.setHeight( height );
        window.showAsDropDown(anchor);
    }

    public boolean isShowing(){
        return window.isShowing();
    }

    public void dismiss(){
        window.dismiss();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.add_bookmark:
                listener.addBookmark();
                break;
            case R.id.bookmarks:
                listener.showBookmarks();
                dismiss();
                break;
            case R.id.preferences:
                listener.showSettings();
                break;
            case R.id.about:
                listener.showAbout();
                break;

        }

        dismiss();
    }

    interface Listener{
        void showBookmarks();
        void addBookmark();
        void showSettings();
        void showAbout();
    }
}
