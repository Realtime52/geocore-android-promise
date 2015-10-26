package jp.geocore.android.promise;



import android.util.Log;

import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;

import java.util.concurrent.CountDownLatch;

import jp.geocore.android.model.GeocoreUser;

/**
 * Created by kakkar on 2015/08/19.
 */
public class TestPromiseAuthBase extends TestPromiseBase {


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // login
        final CountDownLatch signal = new CountDownLatch(1);
        promise.login(GEOCORE_USERID, GEOCORE_USERPWD)
                .then(new DoneCallback<GeocoreUser>() {
                    @Override
                    public void onDone(GeocoreUser result) {
                        signal.countDown();
                    }
                })
                .fail(new FailCallback<Exception>() {
                    @Override
                    public void onFail(Exception result) {
                        Log.e(TAG, "Error logging in", result);
                        signal.countDown();
                    }
                });
        signal.await();
    }

}
