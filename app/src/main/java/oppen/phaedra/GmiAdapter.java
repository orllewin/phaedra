package oppen.phaedra;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class GmiAdapter extends ArrayAdapter<String> {

    int textColour = Color.BLACK;

    public interface Listener{
        void openBrowser(String address);
        void geminiRequest(String address);
    }

    private Listener listener;

    public GmiAdapter(Context context, ArrayList<String> lines, Listener listener) {
        super(context, 0, lines);
        this.listener = listener;
    }

    public void setForeground(String foreground) {
        textColour = Color.parseColor(foreground);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String line = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.simple_line, parent, false);
        }

        TextView simpleLine = convertView.findViewById(R.id.gemtext_line);

        simpleLine.setTextColor(textColour);

        boolean inCodeBlock = false;

        if(line.startsWith("# ")){
            simpleLine.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 36);
            simpleLine.setText(line.substring(2));
            simpleLine.setTypeface(Typeface.DEFAULT_BOLD);
            simpleLine.setCompoundDrawables(null, null, null, null);
        }else if(line.startsWith("## ")){
            simpleLine.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30);
            simpleLine.setText(line.substring(3));
            simpleLine.setTypeface(Typeface.DEFAULT_BOLD);
            simpleLine.setCompoundDrawables(null, null, null, null);
        }else if(line.startsWith("### ")) {
            simpleLine.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24);
            simpleLine.setText(line.substring(4));
            simpleLine.setTypeface(Typeface.DEFAULT_BOLD);
            simpleLine.setCompoundDrawables(null, null, null, null);
        }else if(line.startsWith("```")){
            inCodeBlock = true;
            simpleLine.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            simpleLine.setText(line.substring(3));
            simpleLine.setTypeface(Typeface.MONOSPACE);
            simpleLine.setCompoundDrawables(null, null, null, null);
        }else if(line.startsWith("=>")) {
            simpleLine.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
            simpleLine.setTypeface(Typeface.DEFAULT);

            String[] linkSegments = line.substring(3).split(" ");

            if(linkSegments.length == 1) {
                SpannableString content = new SpannableString(linkSegments[0]);
                content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
                simpleLine.setText(content);
            }else{
                String link = linkSegments[0].trim();
                SpannableString content = new SpannableString(line.substring(line.indexOf("=> " + link) + link.length() + 4));
                content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
                simpleLine.setText(content);
            }
            convertView.setOnClickListener(v -> {
                String uri = linkSegments[0];
                if(uri.startsWith("http")){
                    listener.openBrowser(uri);
                }else {
                    listener.geminiRequest(uri);
                }
            });

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
            String link = linkSegments[0].trim();
            if(link.endsWith(".png") || link.endsWith(".jpg") || link.endsWith(".jpeg")) {
                simpleLine.setCompoundDrawablesWithIntrinsicBounds(null, null, getContext().getResources().getDrawable(R.drawable.inline_image_icon), null);
            }else if(link.startsWith("http")){
                simpleLine.setCompoundDrawablesWithIntrinsicBounds(null, null, getContext().getResources().getDrawable(R.drawable.browser), null);
            }else{
                simpleLine.setCompoundDrawables(null, null, null, null);}
            }

        }else if(line.startsWith("*")){
            simpleLine.setTypeface(Typeface.DEFAULT);
            simpleLine.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
            simpleLine.setText("• " + line.substring(2));
            simpleLine.setCompoundDrawables(null, null, null, null);
        }else{
            simpleLine.setTypeface(Typeface.DEFAULT);
            simpleLine.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
            simpleLine.setText(line);
            simpleLine.setCompoundDrawables(null, null, null, null);
        }

        if(!inCodeBlock && line.length() == 0){
            simpleLine.setVisibility(View.GONE);
        }else{
            simpleLine.setVisibility(View.VISIBLE);
        }

        return convertView;
    }
}