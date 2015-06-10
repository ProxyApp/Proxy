/*Copyright 2013 Jake Wharton

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package com.shareyourproxy;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

import com.crashlytics.android.Crashlytics;
import com.firebase.client.Firebase;
import com.shareyourproxy.api.CommandIntentService;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.RxBusDriver;
import com.shareyourproxy.api.rx.command.BaseCommand;
import com.shareyourproxy.api.rx.command.callback.CommandEvent;
import com.squareup.leakcanary.LeakCanary;

import java.util.ArrayList;

import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import rx.functions.Action1;
import timber.log.Timber;

import static com.shareyourproxy.api.rx.RxQuery.queryUser;

/**
 * Plant a logging tree.
 */
public class ProxyApplication extends Application {

    private User _currentUser;
    private RxBusDriver _bus = RxBusDriver.getInstance();

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.USE_CRASHLYTICS) {
            Fabric.with(this, new Crashlytics());
        }
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
            LeakCanary.install(this);
        }
        Firebase.setAndroidContext(this);
        Realm.deleteRealmFile(this);
        _bus.toObserverable().subscribe(getRequest());
    }

    public Action1<Object> getRequest() {
        return new Action1<Object>() {
            @Override
            public void call(Object event) {
                if (event instanceof BaseCommand) {
                    Timber.i("BaseCommand:" + event);
                    Intent intent = new Intent(ProxyApplication.this, CommandIntentService.class);
                    intent.putExtra(CommandIntentService.ARG_COMMAND_CLASS, (BaseCommand) event);
                    intent.putExtra(
                        CommandIntentService.ARG_RESULT_RECEIVER, new ResultReceiver(null) {
                            @Override
                            protected void onReceiveResult(int resultCode, Bundle resultData) {
                                if (resultCode == Activity.RESULT_OK) {
                                    updateApplicationUser();

                                    ArrayList<CommandEvent> events =
                                        resultData.getParcelableArrayList(
                                            CommandIntentService.ARG_RESULT_BASE_EVENTS);

                                    for (CommandEvent event : events) {
                                        Timber.i("CommandEvent:" + event);
                                        getRxBus().post(event);
                                    }
                                } else {
                                    Timber.e("Error receiving result");
                                }
                            }
                        });
                    startService(intent);
                }
            }
        };
    }

    private void updateApplicationUser() {
        setCurrentUser(queryUser(
            ProxyApplication.this, _currentUser.id().value()).single());
    }

    /**
     * Getter.
     *
     * @return currerntly logged in user
     */
    public User getCurrentUser() {
        return _currentUser;
    }

    /**
     * Setter.
     *
     * @param currentUser currently logged in user
     */
    public void setCurrentUser(User currentUser) {
        _currentUser = currentUser;
    }

    public RxBusDriver getRxBus() {
        return _bus;
    }
}
