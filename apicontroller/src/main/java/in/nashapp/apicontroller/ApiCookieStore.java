package in.nashapp.apicontroller.function;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Avinash on 19-11-2015.
 */
public class ApiCookieStore implements CookieStore{

        private static final String LOGTAG = "API CookieStore";

        /*
         * The memory storage of the cookies
         */
        private Map<String, Map<String,String>> mapCookies = new HashMap<String, Map<String,String>>();
        /*
         * The instance of the shared preferences
         */
        private final SharedPreferences sharedPrefs;

        /*
         * @see java.net.CookieStore#add(java.net.URI, java.net.HttpCookie)
         */
        public void add(URI uri, HttpCookie cookie) {

            String domain = cookie.getDomain();

            // Log.i(LOGTAG, "adding ( " + domain +", " + cookie.toString() );

            Map<String,String> cookies = mapCookies.get(domain);
            if (cookies == null) {
                cookies = new HashMap<String, String>();
                mapCookies.put(domain, cookies);
            }
            cookies.put(cookie.getName(), cookie.getValue());

            if (cookie.getName().contentEquals("rememberme") && !cookie.getValue().equals("")){
                // Log.i(LOGTAG, "Saving rememberMeCookie = " + cookie.getValue() );
                // Update in Shared Preferences
                SharedPreferences.Editor e = sharedPrefs.edit();
                e.putString("rememberme", cookie.toString());
                e.commit(); // save changes
            }

        }

        /*
         * Constructor
         *
         * @param  ctxContext the context of the Activity
         */
        public ApiCookieStore(Context ctxContext) {
            sharedPrefs = ctxContext.getSharedPreferences("QUIKLRN_Cookies", Context.MODE_PRIVATE);
        }

        /*
         * @see java.net.CookieStore#get(java.net.URI)
         */
        public List<HttpCookie> get(URI uri) {

            List<HttpCookie> cookieList = new ArrayList<HttpCookie>();

            String domain = uri.getHost();

            // Log.i(LOGTAG, "getting ( " + domain +" )" );

            Map<String,String> cookies = mapCookies.get(domain);
            if (cookies == null) {
                cookies = new HashMap<String, String>();
                mapCookies.put(domain, cookies);
            }

            for (Map.Entry<String, String> entry : cookies.entrySet()) {
                cookieList.add(new HttpCookie(entry.getKey(), entry.getValue()));
                // Log.i(LOGTAG, "returning cookie: " + entry.getKey() + "="+ entry.getValue());
            }
            return cookieList;
        }

        /*
         * @see java.net.CookieStore#removeAll()
         */
        public boolean removeAll() {

            // Log.i(LOGTAG, "removeAll()" );

            mapCookies.clear();
            return true;

        }

        /*
         * @see java.net.CookieStore#getCookies()
         */
        public List<HttpCookie> getCookies() {

            Log.i(LOGTAG, "getCookies()" );

            Set<String> mapKeys = mapCookies.keySet();

            List<HttpCookie> result = new ArrayList<HttpCookie>();
            for (String key : mapKeys) {
                Map<String,String> cookies =    mapCookies.get(key);
                for (Map.Entry<String, String> entry : cookies.entrySet()) {
                    result.add(new HttpCookie(entry.getKey(), entry.getValue()));
                    Log.i(LOGTAG, "returning cookie: " + entry.getKey() + "=" + entry.getValue());
                }
            }

            return result;

        }

        /*
         * @see java.net.CookieStore#getURIs()
         */
        public List<URI> getURIs() {

            Log.i(LOGTAG, "getURIs()" );

            Set<String> keys = mapCookies.keySet();
            List<URI> uris = new ArrayList<URI>(keys.size());
            for (String key : keys){
                URI uri = null;
                try {
                    uri = new URI(key);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                uris.add(uri);
            }
            return uris;

        }

        /*
         * @see java.net.CookieStore#remove(java.net.URI, java.net.HttpCookie)
         */
        public boolean remove(URI uri, HttpCookie cookie) {

            String domain = cookie.getDomain();

            Log.i(LOGTAG, "remove( " + domain +", " + cookie.toString() );

            Map<String,String> lstCookies = mapCookies.get(domain);

            if (lstCookies == null)
                return false;

            return lstCookies.remove(cookie.getName()) != null;

        }

}
