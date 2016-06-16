package soluto.congo.core;

import com.google.gson.Gson;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import rx.Completable;
import rx.Observable;
import rx.Subscription;
import rx.functions.Func1;
import rx.observables.ConnectableObservable;
import rx.subscriptions.CompositeSubscription;

public class ActionHandler<T> implements Func1<RemoteCall, Observable<Object>> {
    private final String serviceName;
    private final String methodName;
    private Func1<Object[], Observable<T>> function;

    public ActionHandler(String serviceName, String methodName, Func1<Object[], Observable<T>> function)
    {
        this.serviceName = serviceName;
        this.methodName = methodName;
        this.function = function;
    }

    @Override
    public Observable<Object> call(RemoteCall remoteCall) {
        if (!remoteCall.method.equals(methodName) || !remoteCall.service.equals(serviceName)){
            return Observable.empty();
        }
        return function.call(remoteCall.args).cast(Object.class);
    }
}
