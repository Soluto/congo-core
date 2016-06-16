import rx.Completable;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.subjects.PublishSubject;
import soluto.congo.core.RemoteCallResponder;
import soluto.congo.core.RemoteCallResult;

public class TestRemoteCallResponder implements RemoteCallResponder {
    private PublishSubject<RemoteCallResult> responseStream;

    public TestRemoteCallResponder(PublishSubject<RemoteCallResult> responseStream) {
        this.responseStream = responseStream;
    }

    @Override
    public Completable respond(final RemoteCallResult remoteCallResult) {
        return Completable.fromAction(new Action0() {
            @Override
            public void call() {
                responseStream.onNext(remoteCallResult);
            }
        });
    }
}
