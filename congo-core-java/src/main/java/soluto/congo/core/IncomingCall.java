package soluto.congo.core;

import rx.Observable;

public interface IncomingCall<TResponse> {
    public Object[] getArgs();
    public Observable<Void> respondWith(TResponse responseModel);
}
