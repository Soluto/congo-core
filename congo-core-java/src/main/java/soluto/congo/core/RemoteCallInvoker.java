package soluto.congo.core;

import rx.Observable;

public interface RemoteCallInvoker {
    Observable<Object> invoke(RemoteCall remoteCall);
}
