package oppen.phaedra;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import static java.sql.DriverManager.println;

public class AboutDialog {

    private final Dialog dialog;

    public AboutDialog(Context context){
        dialog = new Dialog(context, R.style.oppen_dialog);
        dialog.setContentView(R.layout.dialog_about);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

        ImageButton close = dialog.findViewById(R.id.close_button);
        close.setOnClickListener(v -> dialog.dismiss());

        TextView appVersion = dialog.findViewById(R.id.app_version);
        appVersion.setText(getVersionName(context));

        Button openWebsiteButton = dialog.findViewById(R.id.oppenlab_button);
        openWebsiteButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://oppen.digital"));
            context.startActivity(intent);
        });

        TextView tlsInfo = dialog.findViewById(R.id.tls_info);
        buildTLSInfo(tlsInfo);
    }

    private void buildTLSInfo(TextView view){
        new Thread() {
            @Override
            public void run() {
                super.run();

                StringBuilder sb = new StringBuilder();

                sb.append("\n");
                sb.append("Supported Keystores on this device:\n\n");

                for(Provider provider : Security.getProviders()){
                    sb.append("" + provider.getName() + " version: " + provider.getName() + "\n");
                    for(Provider.Service service : provider.getServices()){
                        if(service.getType().equals("KeyStore")){
                            sb.append("     " + service.getAlgorithm() + "\n");
                        }
                    }
                }

                sb.append("\n");

                try {
                    SSLContext sslContext = SSLContext.getInstance("TLS");
                    sslContext.init(null, null, null);
                    SSLSocketFactory factory = sslContext.getSocketFactory();
                    SSLSocket socket = (SSLSocket) factory.createSocket();

                    sb.append("\n");
                    sb.append("Supported TLS protocols on this device:\n\n");

                    for(String protocol : socket.getSupportedProtocols()){
                        sb.append("" + protocol + " \n");
                    }


                    sb.append("\n\n");

                    sb.append("Enabled cipher suites on this device:\n\n");

                    for(String cipherSuite : factory.getDefaultCipherSuites()){
                        sb.append("" + cipherSuite + " \n");
                    }

                    sb.append("\n");
                    new Handler(Looper.getMainLooper()).post(() -> view.setText(sb.toString()));

                } catch (NoSuchAlgorithmException | KeyManagementException | IOException e) {
                    e.printStackTrace();
                }


            }
        }.start();
    }

    public void show(){
        dialog.show();
    }

    private String getVersionName(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }
    }
}
