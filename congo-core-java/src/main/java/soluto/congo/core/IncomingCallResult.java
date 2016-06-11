package soluto.congo.core;

import rx.Observable;

public class IncomingCallResult {

    public Observable<Object> value;

    public IncomingCallResult(Object methodInvocationResult) {
        if (!(methodInvocationResult instanceof Observable))  {
            value = Observable.just(methodInvocationResult);
        }
        else {
            value = (Observable)methodInvocationResult;
        }
    }

    public IncomingCallResult(Throwable exception) {
        this.value = Observable.error(exception);
    }
}