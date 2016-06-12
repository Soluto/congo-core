package soluto.congo.core;

import rx.Observable;

public interface RmoteCallInvoker {
    <TResult> Observable<TResult> invoke(RemoteCall remoteCall, Class<TResult> tResultClass);
}
