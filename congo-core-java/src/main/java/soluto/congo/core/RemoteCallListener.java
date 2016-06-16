package soluto.congo.core;

import rx.Observable;

public interface RemoteCallListener {
    Observable<RemoteCall> getRemoteCalls();
}
