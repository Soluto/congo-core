package soluto.congo.core;

import rx.Completable;

public interface RemoteCallResponder {
    Completable respond(RemoteCallResult remoteCallResult);
}
