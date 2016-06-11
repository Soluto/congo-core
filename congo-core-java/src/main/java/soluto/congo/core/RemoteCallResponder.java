package soluto.congo.core;

public interface RemoteCallResponder {
    void onNext(RemoteCall javaScriptCall, Object result);
    void onCompleted(RemoteCall javaScriptCall);
    void onError(RemoteCall javaScriptCall, Throwable throwable);
}
