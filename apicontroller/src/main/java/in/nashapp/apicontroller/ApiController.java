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
    public ApiController(Context context){
        this.C = context;
    }

    public String GetRequest(String urlString,HashMap<String, String> params){
        return GetRequest(urlString,params,true);
    }
    public String GetRequest(String urlString,HashMap<String, String> params,boolean cached){
        String charset = "UTF-8";
        DataOutputStream wr = null;
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
            Log.i("ApiController", "HTTP response cache installation failed:" + Log.getStackTraceString(e));
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
                if(data.equals(""))
                    webPage += data ;
                else
                    webPage += "\n" +data ;
            }
        } catch (Exception e) {
            Log.e("ApiController",Log.getStackTraceString(e));
            webPage = "";
        }
        return webPage;
    }
    public String PostRequest(String url,HashMap<String, String> params){
        return PostRequest(url, params, true);
    }
    public String PostRequest(String url,HashMap<String, String> params,Boolean cached){
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
            Log.i("ApiController", "HTTP response cache installation failed:" + Log.getStackTraceString(e));
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
                    wr.writeBytes(lineEnd + params.get(key));
                    wr.writeBytes(lineEnd);
                    wr.writeBytes(twoHyphens + boundary + lineEnd);
                }
            i++;
        }
        wr.flush();
        wr.close();
        } catch (Exception e) {
            Log.e("ApiController",Log.getStackTraceString(e));
        }
        try {
            InputStream in = new BufferedInputStream(conn.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            String line;
            while ((line = reader.readLine()) != null) {
                if(result.toString().equals(""))
                    result.append(line);
                else
                    result.append("\n").append(line);
            }
            //Log.d("PostRequest", "result: " + result.toString());
            PostResult  = result.toString();
        } catch (Exception e) {
            Log.e("ApiController",Log.getStackTraceString(e));
            PostResult="";
        }
        conn.disconnect();
        return PostResult;
    }
    public String PostDownload(String url,String dst,HashMap<String, String> params,Boolean cached){
        if(!new File(dst.substring(0,dst.lastIndexOf("/"))).exists())
            new File(dst.substring(0,dst.lastIndexOf("/"))).mkdirs();
        String filePath ="";
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
            Log.i("ApiController", "HTTP response cache installation failed:" + Log.getStackTraceString(e));
        }
        String PostResult  ="0";
        HttpURLConnection conn =null;
        try {
            URL Url = new URL(url);
            String charset = "UTF-8";
            conn = (HttpURLConnection)Url.openConnection();
            DataOutputStream wr = null;
            conn.setInstanceFollowRedirects(true);
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept-Charset", charset);
            conn.setReadTimeout(300000);
            conn.setConnectTimeout(300000);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            if(cached)
                conn.addRequestProperty("Cache-Control", "max-stale=" + 60 * 60 *3);
            conn.connect();
            wr = new DataOutputStream(conn.getOutputStream());
            int i = 0;
            for (String key : params.keySet()) {
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
                    wr.writeBytes(lineEnd + params.get(key));
                    wr.writeBytes(lineEnd);
                    wr.writeBytes(twoHyphens + boundary + lineEnd);
                }
                i++;
            }
            wr.flush();
            wr.close();
            String raw  = "";
            String type = "";
            String extension="";
            int status = conn.getResponseCode();
            while(status != HttpURLConnection.HTTP_OK&&(status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM || status == HttpURLConnection.HTTP_SEE_OTHER)){
                String newUrl = conn.getHeaderField("Location");
                conn = (HttpURLConnection) new URL(newUrl).openConnection();
                status = conn.getResponseCode();
            }
            try{raw = conn.getHeaderField("Content-Disposition");}catch (Exception e){raw="";}
            try{type = conn.getContentType();}catch (Exception e){type="";}
            Log.d("ApiController",raw+"\t"+type);
            if(!type.equals("")&&!type.equals("application/octet-stream")){
                MimeType mimeType = new MimeType();
                extension = mimeType.get_extension_from_mimetye(type);
            }else if(raw!=null&&!raw.equals("")&& raw.contains("=")) {
                extension = raw.split("=")[1].replace("\"","");
                extension = extension.substring(extension.indexOf(".")+1);
            }else if(url.substring(url.lastIndexOf("/")+1).contains(".")) {
                extension =url.substring(url.lastIndexOf("/")+1);
            }else{
                extension = "png";
            }
            if(dst.substring(dst.lastIndexOf("/")+1).contains("."))
                filePath = dst;
            else
                filePath = dst+"."+extension;
            InputStream is = conn.getInputStream();
            FileOutputStream outputStream = new FileOutputStream(filePath);
            bytesRead = -1;
            buffer = new byte[1024000];
            while ((bytesRead = is.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            is.close();
            conn.disconnect();
        } catch (Exception e) {
            Log.e("ApiController",Log.getStackTraceString(e));
        }
    return filePath;
    }

    public String DownloadFile(String urlString,String dst){
        if(!new File(dst.substring(0,dst.lastIndexOf("/"))).exists())
            new File(dst.substring(0,dst.lastIndexOf("/"))).mkdirs();
        CookieManager cookieManager = new CookieManager( new PersistentCookieStore(C), CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);
        String filePath ="";
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(30000);
            connection.setDoOutput(false);
            connection.setInstanceFollowRedirects(true);
            int status = connection.getResponseCode();
            while(status != HttpURLConnection.HTTP_OK&&(status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM || status == HttpURLConnection.HTTP_SEE_OTHER)){
                String newUrl = connection.getHeaderField("Location");
                connection = (HttpURLConnection) new URL(newUrl).openConnection();
                status = connection.getResponseCode();
            }
            if(status==HttpURLConnection.HTTP_NOT_FOUND)
                return "";
            String raw  = "";
            String type = "";
            String extension="";
            try{raw = connection.getHeaderField("Content-Disposition");}catch (Exception e){raw="";}
            try{type = connection.getContentType();}catch (Exception e){type="";}
            if(!type.equals("")&&!type.equals("application/octet-stream")){
                MimeType mimeType = new MimeType();
                extension = mimeType.get_extension_from_mimetye(type);
            }else if(raw!=null&&!raw.equals("")&& raw.contains("=")) {
                extension = raw.split("=")[1].replace("\"","");
                extension = extension.substring(extension.indexOf(".")+1);
            }else if(urlString.substring(urlString.lastIndexOf("/")+1).contains(".")) {
                extension =urlString.substring(urlString.lastIndexOf("/")+1);
            }else{
                extension = "png";
            }
            if(dst.substring(dst.lastIndexOf("/")+1).contains("."))
                filePath = dst;
            else
                filePath = dst+"."+extension;
            InputStream is = connection.getInputStream();
            FileOutputStream outputStream = new FileOutputStream(filePath);
            int bytesRead = -1;
            byte[] buffer = new byte[65536];
            while ((bytesRead = is.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            is.close();
            connection.disconnect();
        } catch (Exception e) {
            Log.e("ApiController",Log.getStackTraceString(e));
            return null;
        }
        return filePath;
    }
    public String DownloadFileNotify(String urlString,String dst,String title,int notify_id){
        if(!new File(dst.substring(0,dst.lastIndexOf("/"))).exists())
            new File(dst.substring(0,dst.lastIndexOf("/"))).mkdirs();
        CookieManager cookieManager = new CookieManager( new PersistentCookieStore(C), CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);
        String filePath ="";
        final NotificationManager mNotifyManager =(NotificationManager) C.getSystemService(Context.NOTIFICATION_SERVICE);
        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(C);
        mBuilder.setContentTitle("Download")
                .setContentText("Download in progress")
                .setSmallIcon(R.drawable.ic_file_download_black);
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setInstanceFollowRedirects(true);
            connection.setRequestMethod("GET");
            connection.setReadTimeout(30000);
            connection.setDoOutput(false);
            int status = connection.getResponseCode();
            while(status != HttpURLConnection.HTTP_OK&&(status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM || status == HttpURLConnection.HTTP_SEE_OTHER)){
                String newUrl = connection.getHeaderField("Location");
                connection = (HttpURLConnection) new URL(newUrl).openConnection();
                status = connection.getResponseCode();
            }
            if(status==HttpURLConnection.HTTP_NOT_FOUND)
                return "";
            int lenghtOfFile = connection.getContentLength();
            String raw  = "";
            String type = "";
            String extension="";
            try{raw = connection.getHeaderField("Content-Disposition");}catch (Exception e){raw="";}
            try{type = connection.getContentType();}catch (Exception e){type="";}
            Log.d("ApiController",raw+" "+type);
            if(!type.equals("")&&!type.equals("application/octet-stream")){
                MimeType mimeType = new MimeType();
                extension = mimeType.get_extension_from_mimetye(type);
            }else if(raw!=null&&!raw.equals("")&& raw.contains("=")) {
                extension = raw.split("=")[1].replace("\"","");
                extension = extension.substring(extension.indexOf(".")+1);
            }else if(urlString.substring(urlString.lastIndexOf("/")+1).contains(".")) {
                extension =urlString.substring(urlString.lastIndexOf("/")+1);
            }else{
                extension = "png";
            }
            if(dst.substring(dst.lastIndexOf("/")+1).contains("."))
                filePath = dst;
            else
                filePath = dst+"."+extension;
            InputStream is = connection.getInputStream();
            FileOutputStream outputStream = new FileOutputStream(filePath);
            int bytesRead = -1;
            byte[] buffer = new byte[65536];
            int count=0;
            mBuilder.setContentTitle(title);
            mBuilder.setContentText("0% Complete");
            mBuilder.setProgress(lenghtOfFile,0, false);
            mNotifyManager.notify(notify_id, mBuilder.build());
            while ((bytesRead = is.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                count = count+bytesRead;
                if(((float) (count*100 + 1) / (float) (lenghtOfFile))>0) {
                    mBuilder.setContentTitle(title);
                    mBuilder.setContentText((int) ((float) (count * 100 + 1) / (float) (lenghtOfFile)) + "% Complete");
                    mBuilder.setProgress(lenghtOfFile, count + 1, false);
                    mNotifyManager.notify(notify_id, mBuilder.build());
                }
            }
            mNotifyManager.cancel(notify_id);
            outputStream.close();
            is.close();
            connection.disconnect();
        } catch (Exception e) {
            Log.e("ApiController",Log.getStackTraceString(e));
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
