package soluto.congo.core;

import rx.Observable;

@Deprecated
public interface RpcClient{
    <TResult> Observable<TResult> invoke(String methodName, Class<TResult> resultClass, Object... args);
}
