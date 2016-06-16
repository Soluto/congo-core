import rx.Observable;
import rx.subjects.PublishSubject;
import soluto.congo.core.RemoteCall;
import soluto.congo.core.RemoteCallListener;

public class TestRemoteCallListener implements RemoteCallListener {
    private PublishSubject<RemoteCall> requestStream;

    public TestRemoteCallListener(PublishSubject<RemoteCall> requestStream) {
        this.requestStream = requestStream;
    }

    @Override
    public Observable<RemoteCall> getRemoteCalls() {
        return requestStream;
    }
}
