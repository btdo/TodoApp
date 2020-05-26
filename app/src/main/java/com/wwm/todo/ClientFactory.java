package com.wwm.todo;

import android.content.Context;

import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.wwm.todo.auth.AuthenticationServiceImpl;

class ClientFactory {
    private static volatile AWSAppSyncClient client;

    synchronized static AWSAppSyncClient getInstance(Context context) {
        if (client == null) {
            AWSConfiguration awsConfig = new AWSConfiguration(context);

            client = AWSAppSyncClient.builder()
                    .context(context)
                    .oidcAuthProvider(() -> AuthenticationServiceImpl.INSTANCE.getIdToken())
                    .awsConfiguration(awsConfig)
                    .build();
        }
        return client;
    }
}
