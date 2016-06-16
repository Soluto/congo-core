package soluto.congo.core;

import rx.Notification;
import rx.Observable;
import rx.Subscription;
import rx.functions.Func1;
import rx.observables.ConnectableObservable;
import rx.observables.GroupedObservable;

public class Router {

    private RemoteCallResponder responder;
    private ConnectableObservable<RemoteCall> remoteCalls;

    private Subscription subscription;

    public Router(RemoteCallListener remoteCalls, RemoteCallResponder responder) {
        this.remoteCalls = remoteCalls.getRemoteCalls().publish();
        this.responder = responder;
    }

    public void listen() {
        subscription = remoteCalls.connect();
    }

    public void shutdown() {
        subscription.unsubscribe();
    }

    public Subscription use(final Func1<RemoteCall, Observable<Object>> handler) {
        return remoteCalls
                .groupBy(new Func1<RemoteCall, String>() {
                    @Override
                    public String call(RemoteCall remoteCall) {
                        return remoteCall.correlationId;
                    }
                })
                .flatMap(new Func1<GroupedObservable<String, RemoteCall>, Observable<RemoteCallResult>>() {
                    @Override
                    public Observable<RemoteCallResult> call(final GroupedObservable<String, RemoteCall> remoteCallsById) {
                        Observable<RemoteCall> share = remoteCallsById.share();

                        Observable<RemoteCall> cancellations = share.filter(new Func1<RemoteCall, Boolean>() {
                            @Override
                            public Boolean call(RemoteCall remoteCall) {
                                return remoteCall.isCancelled;
                            }
                        });

                        return share
                                .filter(new Func1<RemoteCall, Boolean>() {
                                    @Override
                                    public Boolean call(RemoteCall remoteCall) {
                                        return !remoteCall.isCancelled;
                                    }
                                })
                                .first()
                                .flatMap(handler)
                                .materialize()
                                .map(new Func1<Notification<Object>, RemoteCallResult>() {
                                    @Override
                                    public RemoteCallResult call(Notification<Object> o) {
                                        RemoteCallResult result = new RemoteCallResult();
                                        result.correlationId = remoteCallsById.getKey();
                                        result.notification = o;
                                        return result;
                                    }
                                })
                                .takeUntil(cancellations);
                    }
                })
                .flatMap(new Func1<RemoteCallResult, Observable<?>>() {
                    @Override
                    public Observable<?> call(RemoteCallResult remoteCallResult) {
                        return responder.respond(remoteCallResult).onErrorComplete().toObservable();
                    }
                })
                .subscribe();
    }
}
