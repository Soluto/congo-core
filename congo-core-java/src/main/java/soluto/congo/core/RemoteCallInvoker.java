package soluto.congo.core;

import rx.Observable;

public interface RemoteCallInvoker {
    <TResult> Observable<TResult> invoke(RemoteCall remoteCall, Class<TResult> tResultClass);
}
