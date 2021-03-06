package jp.geocore.android.promise;



import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import org.jdeferred.Deferred;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;

import java.util.List;

import jp.geocore.android.Geocore;
import jp.geocore.android.GeocoreCallback;
import jp.geocore.android.GeocoreServerError;
import jp.geocore.android.model.GeocorePlace;
import jp.geocore.android.model.GeocoreTag;
import jp.geocore.android.model.GeocoreUser;

/**
 * Created by kakkar on 2015/08/19.
 */
public class Promises {

    // =============================================================================================
    // Debug
    // =============================================================================================

    private static final String TAG = "Promises";
    private static boolean DEBUG = true;

    public static void d(String message) {
        if (DEBUG) {
            Log.d(TAG, message);
        }
    }

    public static void d(String message, Exception e) {
        if (DEBUG) {
            Log.d(TAG, message, e);
        }
    }

    public static void e(String message, Exception e) {
        Log.e(TAG, message, e);
    }

    public static void e(String message) {
        Log.e(TAG, message);
    }

    public static void setDebug(boolean debug) {
        DEBUG = debug;
    }

    private static Promises instance;
    private Context context;

    private Promises(Context context) {
        this.context = context;
    }


    public static synchronized Promises getInstance(Context context) {
        if (Promises.instance == null) {
            Promises.instance = new Promises(context);
        }
        return Promises.instance;
    }

    public static synchronized Promises getInstance() {
        return Promises.instance;
    }

    // =============================================================================================
    //
    // =============================================================================================

    public Promise<GeocoreUser, Exception, Void> login(final String userId, final String password) {
        // name <- userId
        // email <- userId@geocore.jp

        // try to get Geocore.getInstance()
        // if geocore is initialized then do login
        // if geocore is NOT initialized, then
        //    try to initialize by getting settings from manifest
        //    now that geocore is initialized, try to login
        //    if login is not successful with Auth.0001 error, register

        final Deferred<GeocoreUser, Exception, Void> deferred = new DeferredObject<>();

        //Initializing geocore
        Geocore geocore = null;
        try {
            geocore = Geocore.getInstance();
        } catch (IllegalStateException ignore) {
        }

        if (geocore == null) {
            // get geocore settings
            String apiServer, projectId;

            try {
                ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(
                        context.getPackageName(),
                        PackageManager.GET_META_DATA);
                apiServer = (String) appInfo.metaData.get("GEOCORE_API_SERVER");
                projectId = (String) appInfo.metaData.get("GEOCORE_PROJECT_ID");

            } catch (PackageManager.NameNotFoundException e) {
                // shouldn't happen
                Log.e(TAG, e.getMessage(), e);
                return null;
            }

            if (geocore == null) {
                // initialize geocore
                geocore = Geocore.getInstance(context, apiServer, projectId);
            }

            if (apiServer == null || projectId == null) {
                String errMessage = "Expecting GEOCORE_API_SERVER and GEOCORE_PROJECT_ID to be defined in manifest.";
                Log.e(TAG, errMessage);
                return null;
            }
            return null;
        }

        //login
        geocore.login(userId, password, new GeocoreCallback<GeocoreUser>() {
            @Override
            public void onComplete(GeocoreUser geocoreUser, Exception e) {
                if (e != null) {
                    if (e instanceof GeocoreServerError) {
                        GeocoreServerError gse = (GeocoreServerError) e;
                        if ("Auth.0001".equals(gse.getCode())) {
                            Promises.d("registeration is needed");
                            Geocore.getInstance().register(userId, password, userId, userId + "@geocore.com", new GeocoreCallback<GeocoreUser>() {
                                @Override
                                public void onComplete(GeocoreUser geocoreUser, Exception e) {
                                    if (e != null)
                                        deferred.reject(e);
                                    else
                                        deferred.resolve(geocoreUser);
                                }
                            });
                        }
                    }
                    deferred.reject(e);
                } else
                    deferred.resolve(geocoreUser);
            }
        });
        return deferred.promise();
    }


    public Promise<List<GeocoreTag>, Exception, Void> tags() {
        final Deferred<List<GeocoreTag>, Exception, Void> deferred = new DeferredObject<>();
        Geocore.getInstance().tags.get(new GeocoreCallback<List<GeocoreTag>>() {
            @Override
            public void onComplete(List<GeocoreTag> geocoreTags, Exception e) {
                if (e != null)
                    deferred.reject(e);
                else
                    deferred.resolve(geocoreTags);
            }
        });
        return deferred.promise();
    }

    /*
    public Promise<List<GeocoreEvent>, Exception, Void> events() {
        //Geocore.getInstance().events.g
    }
    */


    public Promise<List<GeocorePlace>, Exception, Void> places() {
        final Deferred<List<GeocorePlace>, Exception, Void> deferred = new DeferredObject<>();
        Geocore.getInstance().places.get(new GeocoreCallback<List<GeocorePlace>>() {
            @Override
            public void onComplete(List<GeocorePlace> geocorePlaces, Exception e) {
                if (e != null)
                    deferred.reject(e);
                else
                    deferred.resolve(geocorePlaces);
            }
        });
        return deferred.promise();
    }

/**
 * <span class="en">Login to Geocore.</span>
 * <span class="ja">Geocoreにログインする。</span>
 *
 * @param userId User's ID.
 * @param userName User's name.
 * @param fbId facebook ID.
 * @param url url for the server. "http://dev1-geocore-jp-5m76jmgs7ocx.runscope.net/api
 * @param serverName server name, ie. "PRO-TEST-1"
 *
 */

    /*
    public Promise<GeocoreUser, Exception, Void> geocoreLogin(
            final Context context,
            final String fbId,
            final String userName,
            final String userId,
            final String url,
            final String serverName) {

        final Deferred<GeocoreUser, Exception, Void> deferred = new DeferredObject<>();
        final String userPassword = (new StringBuilder(fbId)).reverse().toString();
        Promises.d(url);
        Promises.d("fbId = " + fbId + "userName = " + userName + "userId = " + userName);

        Geocore.getInstance(context, url, serverName ).login(userId, userPassword, new GeocoreCallback<GeocoreUser>() {
            @Override
            public void onComplete(GeocoreUser geocoreUser, Exception e) {
                if (e != null) {
                    if (e instanceof GeocoreServerError) {
                        GeocoreServerError gse = (GeocoreServerError) e;
                        if ("Auth.0001".equals(gse.getCode())) {
                            Promises.d("registeration is needed");
                            Geocore.getInstance().register(userId, userPassword, userId, fbId + "@geocore.com", new GeocoreCallback<GeocoreUser>() {
                                @Override
                                public void onComplete(GeocoreUser geocoreUser, Exception e) {
                                    if (e != null) {
                                        deferred.reject(e);
                                    } else
                                        deferred.resolve(geocoreUser);
                                }
                            });
                        }
                    }
                    deferred.reject(e);
                } else
                    deferred.resolve(geocoreUser);
            }
        });
        return deferred.promise();
    }


    public Promise<GeocoreTrackPoint, Exception, Void> LocationLog(Location location) {
        final Deferred<GeocoreTrackPoint, Exception, Void> deferred = new DeferredObject<>();
        Log.d(TAG, "geocoreLocationLog");
        try {
            Geocore.getInstance().tracks.sendLocationLog(location, new GeocoreCallback<GeocoreTrackPoint>() {
                @Override
                public void onComplete(GeocoreTrackPoint geocoreTrackPoint, Exception e) {
                    if (e != null){
                        deferred.reject(e);
                    }
                    else {
                        deferred.resolve(geocoreTrackPoint);
                    }
                }
            });
        } catch (UnsupportedEncodingException e) {
            deferred.reject(e);
        } catch (JSONException e) {
            deferred.reject(e);
        }
        return null;
    }

    public Promise<GeocoreUser, Exception, Void> PushRegistration(String token) {
        Log.d(TAG, "push Registration");
        final Deferred<GeocoreUser, Exception, Void> deferred = new DeferredObject<>();
        Geocore.getInstance().users.registerForPushNotification(token, new GeocoreCallback<GeocoreUser>() {
            @Override
            public void onComplete(GeocoreUser geocoreUser, Exception e) {
                if (e != null) {
                    deferred.reject(e);
                } else {
                    deferred.resolve(geocoreUser);
                }
            }
        });
        return deferred.promise();

    }

*/
}