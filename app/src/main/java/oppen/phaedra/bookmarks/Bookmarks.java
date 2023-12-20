package oppen.phaedra.bookmarks;

import android.content.SharedPreferences;
import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Bookmarks {

    interface Listener{
        void onBookmarkAdded();
        void onBookmarks(ArrayList<Bookmark> bookmarks);
        void onBookmarkDeleted();
    }

    private final SharedPreferences prefs;
    private final Listener listener;

    public Bookmarks(SharedPreferences prefs, Listener listener){
        this.prefs = prefs;
        this.listener = listener;
    }

    public void getBookmarks(){
        String bookmarksRaw = prefs.getString("bookmarks", "{\"bookmarks\": []}");
        try {
            JSONObject bookmarksJson = new JSONObject(bookmarksRaw);
            JSONArray bookmarksArray = bookmarksJson.getJSONArray("bookmarks");

            ArrayList<Bookmark> bookmarks = new ArrayList<>();
            int count = bookmarksArray.length();
            for(int i = 0 ; i < count ; i++){
                JSONObject bookmarkJSON = bookmarksArray.getJSONObject(i);
                Bookmark bookmark = new Bookmark();
                bookmark.title = bookmarkJSON.getString("title");
                bookmark.address = bookmarkJSON.getString("address");
                bookmarks.add(bookmark);
            }
            listener.onBookmarks(bookmarks);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void addBookmark(String address, String title){
        String bookmarksRaw = prefs.getString("bookmarks", "{\"bookmarks\": []}");
        try {
            JSONObject bookmarksJson = new JSONObject(bookmarksRaw);
            JSONArray bookmarksArray = bookmarksJson.getJSONArray("bookmarks");

            JSONObject newBookmark = new JSONObject();
            newBookmark.put("title", title);
            newBookmark.put("address", address);

            bookmarksArray.put(newBookmark);

            bookmarksJson.put("bookmarks", bookmarksArray);

            System.out.println("After deleting: Bookmark json is now: " + bookmarksJson.toString(5));

            prefs.edit().putString("bookmarks", bookmarksJson.toString()).commit();

            listener.onBookmarkAdded();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * JSONArray.remove was only added in API 19... so we have to create a duplicate array and omit the bookmark
     * @param bookmark
     */
    public void deleteBookmark(Bookmark bookmark){
        System.out.println("deleteBookmark: " + bookmark.title);
        String bookmarksRaw = prefs.getString("bookmarks", "{\"bookmarks\": []}");
        try {
            JSONObject bookmarksJson = new JSONObject(bookmarksRaw);
            JSONArray bookmarksArray = bookmarksJson.getJSONArray("bookmarks");
            JSONArray newArray = new JSONArray();
            int count = bookmarksArray.length();

            for(int i = 0 ; i < count ; i++){
                JSONObject bookmarkJSON = bookmarksArray.getJSONObject(i);
                Bookmark aBookmark = new Bookmark();
                aBookmark.title = bookmarkJSON.getString("title");
                aBookmark.address = bookmarkJSON.getString("address");

                if(!aBookmark.title.equals(bookmark.title) && !aBookmark.address.equals(bookmark.address)){
                    newArray.put(bookmarkJSON);
                }
            }

            JSONObject newBookmarks = new JSONObject();
            newBookmarks.put("bookmarks", newArray);
            prefs.edit().putString("bookmarks", newBookmarks.toString()).commit();

            listener.onBookmarkDeleted();
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
