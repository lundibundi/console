package com.metarhia.lundibundi.console.dagger.modules;

import javax.inject.Named;
import javax.inject.Singleton;

import android.content.SharedPreferences;
import android.content.res.Resources;
import com.metarhia.jstp.Connection.JSTPConnection;
import com.metarhia.lundibundi.console.R;
import com.metarhia.lundibundi.console.utils.Constants;
import dagger.Module;
import dagger.Provides;

/**
 * Created by lundibundi on 7/23/16.
 */
@Module()
public class JSTPConnectionModule {
        private static final String sHost = "192.168.0.103"; //"46.101.171.180";
//    private static final String sHost = "192.168.43.90"; //"46.101.171.180";
    //    private static final String sHost = "192.168.88.80"; //"46.101.171.180";
    private static final int sPort = 2500; //3000;

    @Provides
    @Singleton
    public JSTPConnection provideJSTPConnection(@Named("serverHost") String host,
                                                @Named("serverPort") int port) {
        return new JSTPConnection(host, port, true);
    }

    @Provides
    @Named("serverHost")
    public String provideServerHost(Resources resources,
                                    @Named(Constants.DEFAULT_PREFS) SharedPreferences prefs) {
        String key = resources.getString(R.string.key_server_host);
        return prefs.getString(key, sHost);
    }

    @Provides
    @Named("serverPort")
    public int provideServerPort(Resources resources,
                                 @Named(Constants.DEFAULT_PREFS) SharedPreferences prefs) {
        final String key = resources.getString(R.string.key_server_port);
        final String port = prefs.getString(key, "");
        return port.isEmpty() ? sPort : Integer.parseInt(port);
    }
}
