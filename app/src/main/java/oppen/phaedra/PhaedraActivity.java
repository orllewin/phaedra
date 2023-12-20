package oppen.phaedra;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;

import oppen.phaedra.bookmarks.AddBookmarkDialog;
import oppen.phaedra.bookmarks.BookmarksDialog;
import oppen.phaedra.settings.SettingsActivity;

public class PhaedraActivity extends Activity {

    private static final int PREFS_REQ = 10;
    private RelativeLayout root;
    private RelativeLayout header;
    private EditText addressEditText;
    private ListView gemtextListView;
    private ProgressBar progress;
    private RelativeLayout imageViewer;
    private ImageView imageView;
    private SharedPreferences prefs;

    private boolean viewingImage = false;
    private String currentTitle;
    private String currentAddress;

    private MainMenu menu;
    private Gemini gemini;
    private GmiAdapter.Listener gemtextListener;

    private GmiAdapter adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        gemini = new Gemini(new Gemini.GeminiListener() {
            @Override
            public void showProgress() {
                progress.setVisibility(View.VISIBLE);
            }

            @Override
            public void message(String message) {
                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    dialog(message);
                });
            }

            @Override
            public void gemtextReady(String address, ArrayList<String> lines) {
                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    updateAddress(address);
                    extractTitle(address, lines);
                    adapter = new GmiAdapter(PhaedraActivity.this, lines, gemtextListener);
                    String foreground = prefs.getString("foreground", "#000000");
                    adapter.setForeground(foreground);
                    gemtextListView.setAdapter(adapter);
                });
            }

            @Override
            public void imageReady(Drawable image) {
                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    imageViewer.setVisibility(View.VISIBLE);
                    imageView.setImageDrawable(image);
                    viewingImage = true;
                });
            }

            @Override
            public void cacheLastVisited(String address) {
                prefs.edit().putString("uri", address).commit();
            }

        }, getCacheDir());

        gemtextListener = new GmiAdapter.Listener(){
            @Override
            public void openBrowser(String address) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                browserIntent.setData(Uri.parse(address));
                startActivity(browserIntent);
            }

            @Override
            public void geminiRequest(String address) {
                runOnUiThread(() -> {
                    if(menu.isShowing()) menu.dismiss();
                    progress.setVisibility(View.VISIBLE);
                    gemini.requestThread(address);
                });
            }
        };

        root = findViewById(R.id.root);

        String backgroundColour = prefs.getString("background", "#ffffff");

        if(backgroundColour.equals("#XXXXXX")){
            root.setBackgroundColor(Color.WHITE);
        }else{
            root.setBackgroundColor(Color.parseColor(backgroundColour));
        }

        header = findViewById(R.id.header);

        progress = findViewById(R.id.progress);
        imageViewer = findViewById(R.id.image_viewer);
        imageView = findViewById(R.id.image_view);
        addressEditText = findViewById(R.id.address_edit);
        
        ImageButton homeButton = findViewById(R.id.home_button);
        homeButton.setOnClickListener(v -> gemini.requestThread(prefs.getString("home", "gemini://oppen.digital/phaedra/")));

        ImageButton menuButton = findViewById(R.id.menu_button);

        menu = new MainMenu(this, prefs, menuButton, new MainMenu.Listener(){

            @Override
            public void showBookmarks() {
                BookmarksDialog bookmarksDialog = new BookmarksDialog(PhaedraActivity.this, prefs, address -> gemini.requestThread(address));
                bookmarksDialog.show();
            }

            @Override
            public void addBookmark() {
                new AddBookmarkDialog(PhaedraActivity.this, currentTitle, currentAddress, prefs).show();
            }

            @Override
            public void showSettings() {
                startActivityForResult(new Intent(PhaedraActivity.this, SettingsActivity.class), PREFS_REQ);
            }

            @Override
            public void showAbout() {
               new AboutDialog(PhaedraActivity.this).show();
            }
        });

        menuButton.setOnClickListener(v -> {
            if(menu.isShowing()){
                menu.dismiss();
            }else{
                menu.show();
            }
        });

        RelativeLayout goButton = findViewById(R.id.go_button);
        goButton.setOnClickListener(v -> go());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
            //If there's an IME action available remove the Go arrow and adjust the spacing to the right of the address bar:
            goButton.setVisibility(View.GONE);
            addressEditText.setOnEditorActionListener((v, actionId, event) -> {
                go();
                return true;
            });
        }

        Button closeViewerButton = findViewById(R.id.close_viewer_button);
        closeViewerButton.setOnClickListener(v -> imageViewer.setVisibility(View.GONE));
        imageViewer.setOnClickListener(v -> imageViewer.setVisibility(View.GONE));

        gemtextListView = findViewById(R.id.gemtext_listview);

        gemtextListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) { }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(menu.isShowing()) menu.dismiss();
            }
        });

        String lastAddress = prefs.getString("uri", "gemini://oppen.digital/phaedra/");
        updateAddress(lastAddress);
        gemini.requestThread(lastAddress);
    }

    private void extractTitle(String address, ArrayList<String> lines) {
        currentTitle = address.replace("gemini://", "");
        currentAddress = address;
        if(lines.size() >= 5) {
            for (int i = 0; i < 5; i++) {
                String line = lines.get(i);
                if (line.startsWith("# ")) {
                    currentTitle = line.substring(2);
                }
            }
        }
    }

    private void go(){
        if(menu.isShowing()) menu.dismiss();

        gemini.requestThread(addressEditText.getText().toString());

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.CUPCAKE) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(addressEditText.getWindowToken(), 0);
        }
    }

    private void dialog(String message){
        runOnUiThread(() -> {
            progress.setVisibility(View.GONE);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });
    }
    
    private void updateAddress(String address){
        addressEditText.setText(address);
        addressEditText.setSelection(addressEditText.getText().length());
    }

    @Override
    public void onBackPressed() {
        if(viewingImage){
            imageViewer.setVisibility(View.GONE);
            viewingImage = false;
        }else if(gemini.history.size() > 1){
            gemini.history.remove(gemini.history.size() - 1);//Remove last, which should be current uri we're looking at
            Uri previous = gemini.history.get(gemini.history.size() -1);
            if(menu.isShowing()) menu.dismiss();
            gemini.requestThread(previous.toString());
        }else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PREFS_REQ && resultCode == RESULT_OK){
            root.setBackgroundColor(Color.parseColor(prefs.getString("background", "#ffffff")));
            if(adapter != null){
                String foreground = prefs.getString("foreground", "#000000");
                adapter.setForeground(foreground);
            }
        }
    }
}