package in.nashapp.apicontroller;

import android.app.NotificationManager;
import android.content.Context;
import android.net.http.HttpResponseCache;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Avinash on 19-11-2015.
 */

public class ApiController{
    Context C;
    int notify_id=0;
    public ApiController(Context context){
        this.C = context;
    }

    public String GetRequest(String urlString,HashMap<String, String> params){
        return GetRequest(urlString,params,true);
    }
    public String GetRequest(String urlString,HashMap<String, String> params,boolean cached){
        String charset = "UTF-8";
        DataOutputStream wr = null;
        Log.i("APICGetRequest", urlString);
        String webPage = "";
        CookieManager cookieManager = new CookieManager( new PersistentCookieStore(C), CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);
        try {
            File httpCacheDir;
            if(C.getExternalCacheDir()!=null)
                httpCacheDir = new File(C.getExternalCacheDir(), "http");
            else
                httpCacheDir = new File(C.getCacheDir(), "http");
            long httpCacheSize = 10 * 1024 * 1024; // 10 MiB
            HttpResponseCache.install(httpCacheDir, httpCacheSize);
        }catch (IOException e) {
            Log.i("APICGetRequest", "HTTP response cache installation failed:" + e.getMessage());
        }
        int i=0;
       String request_url=urlString+"?";
        for (String key : params.keySet()) {
            if (i != 0){request_url+="&";}
            try {
                request_url += key + "=" + URLEncoder.encode(params.get(key), charset);
            }catch (UnsupportedEncodingException e){
                request_url += key + "=" + params.get(key);
            }
            i++;
        }

        try {
            URL url = new URL(request_url);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("GET");
            connection.setReadTimeout(30000);
            if(cached)
                connection.addRequestProperty("Cache-Control", "max-stale=" + 60 * 60 *3);
            InputStream is = connection.getInputStream();
            BufferedReader reader =new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String data="";
            while ((data = reader.readLine()) != null){
                webPage += data + "\n";
            }
        } catch (Exception e) {
            Log.e("APICGetRequest",e.getStackTrace()+"");
            webPage = "";
        }
        Log.i("APICResult", webPage);
        return webPage;
    }
    public String PostRequest(String url,HashMap<String, String> params){
        return PostRequest(url, params, true);
    }
    public String PostRequest(String url,HashMap<String, String> params,Boolean cached){
        Log.i("APICGetRequest", url);
        StringBuilder result = new StringBuilder();
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary =  "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1*1024*1024;
        CookieManager cookieManager = new CookieManager( new in.nashapp.apicontroller.PersistentCookieStore(C), CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);
        try {
            File httpCacheDir;
            if(C.getExternalCacheDir()!=null)
                httpCacheDir = new File(C.getExternalCacheDir(), "http");
            else
                httpCacheDir = new File(C.getCacheDir(), "http");
            long httpCacheSize = 100 * 1024 * 1024; // 10 MiB
            HttpResponseCache.install(httpCacheDir, httpCacheSize);
        }catch (IOException e) {
            Log.i("APIConnector", "HTTP response cache installation failed:" + e.getStackTrace());
        }
        String PostResult  ="0";
        HttpURLConnection conn =null;
        try {
        URL Url = new URL(url);
        String charset = "UTF-8";
        conn = (HttpURLConnection)Url.openConnection();
        DataOutputStream wr = null;
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Accept-Charset", charset);
        conn.setReadTimeout(30000);
        conn.setConnectTimeout(30000);
        conn.setDoInput(true);
        conn.setDoOutput(true);
        if(cached)
            conn.addRequestProperty("Cache-Control", "max-stale=" + 60 * 60 *3);
        conn.connect();
        wr = new DataOutputStream(conn.getOutputStream());
        int i = 0;
        for (String key : params.keySet()) {
                Log.d("PostVariable",key+" "+params.get(key));
                if (i != 0){wr.writeBytes("&");}
                if(params.get(key).startsWith("file:///")){
                    String FileLocation=params.get(key).replace("file://","");
                    FileInputStream fileInputStream = new FileInputStream(new File(FileLocation) );
                    wr.writeBytes(twoHyphens + boundary + lineEnd);
                    wr.writeBytes("Content-Disposition: form-data; name=\""+key+"\";filename=\"" + FileLocation +"\"" + lineEnd);
                    wr.writeBytes(lineEnd);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    while (bytesRead > 0)
                    {
                        wr.write(buffer, 0, bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    }

                    wr.writeBytes(lineEnd);
                    wr.writeBytes(twoHyphens + boundary + lineEnd);
                    fileInputStream.close();
                }else {
                    wr.writeBytes(twoHyphens + boundary + lineEnd);
                    wr.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"" + lineEnd);
                    wr.writeBytes("Content-Type: text/plain; charset=" + charset+lineEnd);
                    wr.writeBytes(lineEnd+URLEncoder.encode(params.get(key),charset));
                    wr.writeBytes(lineEnd);
                    wr.writeBytes(twoHyphens + boundary + lineEnd);
                }
            i++;
        }
        wr.flush();
        wr.close();
        } catch (Exception e) {
            Log.e("ApiController",e.getStackTrace()+"");
        }
        try {
            InputStream in = new BufferedInputStream(conn.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }
            Log.d("PostRequest", "result: " + result.toString());
            PostResult  = result.toString();
        } catch (IOException e) {
            Log.e("ApiController", e.getStackTrace() + "");
            PostResult="0";
        }
        conn.disconnect();
        return PostResult;
    }

    public String download_file(String urlString,String dst){
        if(!new File(dst.substring(0,dst.lastIndexOf("/"))).exists())
            new File(dst.substring(0,dst.lastIndexOf("/"))).mkdirs();
        Log.d("download_file",dst+" "+urlString);
        CookieManager cookieManager = new CookieManager( new PersistentCookieStore(C), CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);
        String filePath ="";

        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(30000);
            connection.setDoOutput(false);
            String raw  = "";
            String type = "";
            String extension="";
            try{raw = connection.getHeaderField("Content-Disposition");}catch (Exception e){raw="";}
            try{type = connection.getContentType();}catch (Exception e){type="";}
            if(raw!=null&&!raw.equals("")&& raw.contains("=")) {
                extension = raw.split("=")[1].replace("\"","");
                extension = extension.substring(extension.indexOf(".")+1);
            }else if(!type.equals("")){
                MimeType mimeType = new MimeType();
                extension = mimeType.get_extension_from_mimetye(type);
            } else if(urlString.substring(urlString.lastIndexOf("/")+1).contains(".")) {
                extension =urlString.substring(urlString.lastIndexOf("/")+1);
            }else{
                extension = "png";
            }
            if(dst.substring(dst.lastIndexOf("/")+1).contains("."))
                filePath = dst;
            else
                filePath = dst+"."+extension;
            Log.d("download_file",filePath);
            InputStream is = connection.getInputStream();
            FileOutputStream outputStream = new FileOutputStream(filePath);
            int bytesRead = -1;
            byte[] buffer = new byte[1024000];
            while ((bytesRead = is.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            is.close();
            connection.disconnect();
        } catch (Exception e) {
            Log.e("APICDownloadFile",e.getStackTrace()+"");
            return null;
        }
        return filePath;
    }
    public String download_file_notify(String urlString,String dst){
        if(!new File(dst.substring(0,dst.lastIndexOf("/"))).exists())
            new File(dst.substring(0,dst.lastIndexOf("/"))).mkdirs();
        Log.d("download_file",dst+" "+urlString);
        CookieManager cookieManager = new CookieManager( new PersistentCookieStore(C), CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);
        String filePath ="";
        final NotificationManager mNotifyManager =(NotificationManager) C.getSystemService(Context.NOTIFICATION_SERVICE);
        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(C);
        mBuilder.setContentTitle("Download")
                .setContentText("Download in progress")
                .setSmallIcon(R.drawable.ic_file_download_black);
        int current_notify_id =notify_id;
        notify_id++;
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(30000);
            connection.setDoOutput(false);
            int lenghtOfFile = connection.getContentLength();
            String raw  = "";
            String type = "";
            String extension="";
            try{raw = connection.getHeaderField("Content-Disposition");}catch (Exception e){raw="";}
            try{type = connection.getContentType();}catch (Exception e){type="";}
            if(raw!=null&&!raw.equals("")&& raw.contains("=")) {
                extension = raw.split("=")[1].replace("\"","");
                extension = extension.substring(extension.indexOf(".")+1);
            }else if(!type.equals("")){
                MimeType mimeType = new MimeType();
                extension = mimeType.get_extension_from_mimetye(type);
            } else if(urlString.substring(urlString.lastIndexOf("/")+1).contains(".")) {
                extension =urlString.substring(urlString.lastIndexOf("/")+1);
            }else{
                extension = "png";
            }
            if(dst.substring(dst.lastIndexOf("/")+1).contains("."))
                filePath = dst;
            else
                filePath = dst+"."+extension;
            Log.d("download_file",filePath);
            InputStream is = connection.getInputStream();
            FileOutputStream outputStream = new FileOutputStream(filePath);
            int bytesRead = -1;
            byte[] buffer = new byte[1024000];
            int count=0;

            while ((bytesRead = is.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                count = count+bytesRead;
                mBuilder.setContentTitle(dst.substring(dst.lastIndexOf("/") + 1));
                mBuilder.setContentText((int) ((float) (count*100 + 1) / (float) (lenghtOfFile)) + "% Complete");
                mBuilder.setProgress(lenghtOfFile, count + 1, false);
                mNotifyManager.notify(current_notify_id, mBuilder.build());
            }
            mNotifyManager.cancel(current_notify_id);
            outputStream.close();
            is.close();
            connection.disconnect();
        } catch (Exception e) {
            Log.e("APICDownloadFile",e.getStackTrace()+"");
            return null;
        }
        return filePath;
    }
    private void LogCookies(CookieManager cookieManager,int var) {
        Log.d("COOKIE "+var+":","");
        CookieStore cookieStore = cookieManager.getCookieStore();
        List<HttpCookie> cookieList = cookieStore.getCookies();
        for (HttpCookie cookie : cookieList)
        {
            Log.d("COOKIE "+var+": Domain: ",cookie.getDomain()+"");
            Log.d("COOKIE "+var+": max age: " , cookie.getMaxAge()+"");
            Log.d("COOKIE "+var+": name: " , cookie.getName()+"");
            Log.d("COOKIE "+var+": server path: " , cookie.getPath()+"");
            Log.d("COOKIE "+var+": is secure: " , cookie.getSecure()+"");
            Log.d("COOKIE "+var+": value: " , cookie.getValue()+"");
            Log.d("COOKIE "+var+": version: " , cookie.getVersion()+"");
        }
    }

}
