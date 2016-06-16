import rx.Notification;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import soluto.congo.core.RemoteCall;
import soluto.congo.core.RemoteCallInvoker;
import soluto.congo.core.RemoteCallResult;

public class TestRemoteCallInvoker implements RemoteCallInvoker {
    private final PublishSubject<RemoteCall> requestStream;
    private final Observable<RemoteCallResult> responseStream;

    public TestRemoteCallInvoker(PublishSubject<RemoteCall> requestStream, Observable<RemoteCallResult> responseStream) {
        this.requestStream = requestStream;
        this.responseStream = responseStream;
    }

    @Override
    public Observable<Object> invoke(final RemoteCall remoteCall) {
        return Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(final Subscriber<? super Object> subscriber) {
                responseStream
                        .filter(new Func1<RemoteCallResult, Boolean>() {
                            @Override
                            public Boolean call(RemoteCallResult remoteCallResult) {
                                return remoteCallResult.correlationId.equals(remoteCall.correlationId);
                            }
                        })
                        .map(new Func1<RemoteCallResult, Notification<Object>>() {
                            @Override
                            public Notification<Object> call(RemoteCallResult remoteCallResult) {
                                return remoteCallResult.notification;
                            }
                        })
                        .dematerialize()
                        .subscribe(subscriber);

                requestStream.onNext(remoteCall);
                subscriber.add(new Subscription() {
                    @Override
                    public void unsubscribe() {
                        remoteCall.isCancelled = true;
                        requestStream.onNext(remoteCall);
                    }

                    @Override
                    public boolean isUnsubscribed() {
                        return subscriber.isUnsubscribed();
                    }
                });
            }
        });
    }
}