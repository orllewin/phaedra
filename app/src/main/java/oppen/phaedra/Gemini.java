package oppen.phaedra;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

public class Gemini {

    public interface GeminiListener {
        void showProgress();
        void message(String message);
        void gemtextReady(String address, ArrayList<String> lines);
        void imageReady(Drawable image);
        void cacheLastVisited(String address);
    }

    private final File cacheDir;
    private SSLSocketFactory socketFactory;
    private GeminiListener listener;
    private Uri prevUri = null;
    public final ArrayList<Uri> history = new ArrayList<>();

    public Gemini(GeminiListener listener, File cacheDir){
        this.listener = listener;
        initialiseSSL();
        this.cacheDir = cacheDir;
    }

    public void requestThread(String link){
        listener.showProgress();
        new Thread() {
            @Override
            public void run() {
                super.run();

                if(prevUri != null && !link.startsWith("gemini://")){
                    if(prevUri.getPathSegments().size() > 0 && prevUri.getLastPathSegment().contains(".")){
                        prevUri = Uri.parse(prevUri.toString().substring(0, prevUri.toString().lastIndexOf("/")));
                    }
                    prevUri = prevUri.buildUpon().appendPath(link).build();
                }else{
                    prevUri = Uri.parse(link);
                }

                request(prevUri);
            }
        }.start();
    }

    private void initialiseSSL(){

        /*
         * Add Conscrypt TLS - available from SDK 9/Gingerbread only
         */
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            SecurityProvider.addConscryptIfAvailable();
        }

        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            X509TrustManager[] trustManagers = {new DummyTrustManager()};
            sslContext.init(null, trustManagers, null);
            socketFactory = sslContext.getSocketFactory();
        } catch (NoSuchAlgorithmException e) {
            listener.message(e.toString());
            e.printStackTrace();
        } catch (KeyManagementException e) {
            listener.message(e.toString());
            e.printStackTrace();
        }
    }

    private void request(Uri uri){
        l("******* GEMINI REQ: " + uri);
        try {
            SSLSocket socket = (SSLSocket) socketFactory.createSocket(uri.getHost(), 1965);
            socket.startHandshake();

            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
            PrintWriter outWriter = new PrintWriter(bufferedWriter);

            //Not sure where this is coming from - but remove the encoded slash
            String cleanedEntity = uri.toString().replace("%2F", "/").trim();
            String requestEntity = cleanedEntity + "\r\n";

            l("Ariane socket requesting $requestEntity");
            outWriter.print(requestEntity);
            outWriter.flush();

            if (outWriter.checkError()) {
                listener.message( "Print Writer Error");
                closeAll(null, null, null, outWriter, bufferedWriter, outputStreamWriter);
                return;
            }

            InputStream inputStream = socket.getInputStream();
            InputStreamReader headerInputReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(headerInputReader);
            String headerLine = bufferedReader.readLine();

            l("Ariane: response header: " + headerLine);

            if(headerLine == null){
                listener.message("Server did not respond with a Gemini header");
                closeAll(bufferedReader, headerInputReader, inputStream, outWriter, bufferedWriter, outputStreamWriter);
                return;
            }

            int responseCode = Character.getNumericValue(headerLine.charAt(0));
            String meta = headerLine.substring(headerLine.indexOf(" ")).trim();
            switch (responseCode){
                case 2: {
                    if(meta.startsWith("text/gemini")){
                        addHistory(cleanedEntity);
                        ArrayList lines = new ArrayList <String>();
                        String line = bufferedReader.readLine();

                        boolean inCodeBlock = false;
                        StringBuffer sb = new StringBuffer();
                        while (line!= null){
                            if(line.startsWith("```")){
                                if(inCodeBlock){
                                    inCodeBlock = false;
                                    lines.add("```" + sb.toString());
                                    sb = new StringBuffer();
                                }else{
                                    inCodeBlock = true;
                                }
                            }else if(inCodeBlock){
                                sb.append(line);
                                sb.append("\n");
                                l("Gemtext line: " + line);
                            }else{
                                lines.add(line);
                                l("Gemtext line: " + line);
                            }

                            line = bufferedReader.readLine();
                        }

                        prevUri = Uri.parse(cleanedEntity);
                        listener.gemtextReady(cleanedEntity, lines);

                    }else if(meta.startsWith("image/")){
                        File cachedImage = saveFileToCache(socket, Uri.parse(cleanedEntity));
                        Drawable image = Drawable.createFromPath(cachedImage.getPath());
                        listener.imageReady(image);
                    }else if(meta.equals("application/vnd.android.package-archive")){
                        listener.message(".apk download not available in Phaedra");
                        socket.close();
                    }
                    break;
                }
            }
            closeAll(bufferedReader, headerInputReader, inputStream, outWriter, bufferedWriter, outputStreamWriter);
        } catch (IOException e) {
            listener.message(e.toString());
            e.printStackTrace();
        }
    }

    private File saveFileToCache(SSLSocket socket, Uri uri){
        return download(socket, uri, true);
    }

    private File download(SSLSocket socket, Uri uri, boolean isCache){
        String filename = uri.getLastPathSegment();

        File downloadFile = new File(cacheDir, filename);

        if(downloadFile.exists()) downloadFile.delete();
        try {
            downloadFile.createNewFile();

            FileOutputStream outputStream = new FileOutputStream(downloadFile);
            byte[] buffer = new byte[1024];
            int len = socket.getInputStream().read(buffer);
            while (len != -1) {
                outputStream.write(buffer, 0, len);
                len = socket.getInputStream().read(buffer);
            }
            socket.close();
        }catch (IOException e) {
            e.printStackTrace();
        }

        return downloadFile;
    }

    private void closeAll(BufferedReader bufferedReader, InputStreamReader inputStreamReader, InputStream inputStream, PrintWriter outWriter, BufferedWriter bufferedWriter, OutputStreamWriter outputStreamWriter){
        try {
            if(bufferedReader != null) bufferedReader.close();
            if(inputStreamReader != null) inputStreamReader.close();
            if(inputStream != null) inputStream.close();
            if(outWriter != null) outWriter.close();
            if(bufferedWriter != null) bufferedWriter.close();
            if(outputStreamWriter != null) outputStreamWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void addHistory(String address){
        if(!history.isEmpty() && history.get(history.size() - 1).toString().equals(address)){
            l("Address already in history: " + address);
        }else{
            history.add(Uri.parse(address));
            listener.cacheLastVisited(address);
        }
    }
    
    private class DummyTrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException { }
        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException { }
        @Override
        public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
    }

    private void l(String message){ System.out.println("Phaedra Gemini: " + message); }
}
