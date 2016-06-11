package soluto.congo.core;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;

public class RpcListener {

    private Observable<RemoteCall> serviceCalls;
    private Observable<RemoteCall> cancellations;
    private List<String> registeredMethods = new ArrayList<String>();
    private RemoteCallResponder mResponder;

    public RpcListener(String serviceName, Observable<RemoteCall> remoteCalls, RemoteCallResponder responder) {
        serviceCalls = remoteCalls
                .onBackpressureBuffer(2000)
                .filter(byServiceName(serviceName))
                .flatMap(verifyMethodRegistration(registeredMethods, serviceName))
                .share();

        cancellations = serviceCalls.filter(new Func1<RemoteCall, Boolean>() {
            @Override
            public Boolean call(RemoteCall remoteCall) {
                return remoteCall.isCancelled;
            }
        });

        mResponder = responder;
    }

    public Observable<IncomingCall<IncomingCallResult>> on(final String methodName) {
        registeredMethods.add(methodName);
        return serviceCalls.filter(byMethodName(methodName))
                .filter(new Func1<RemoteCall, Boolean>() {
                    @Override
                    public Boolean call(RemoteCall remoteCall) {
                        return !remoteCall.isCancelled;
                    }
                })
                .map(toIncomingCall());
    }

    Func1<RemoteCall, IncomingCall<IncomingCallResult>> toIncomingCall() {
        return new Func1<RemoteCall, IncomingCall<IncomingCallResult>>() {
            @Override
            public IncomingCall<IncomingCallResult> call(final RemoteCall remoteCall) {
                return new IncomingCall<IncomingCallResult>() {
                    @Override
                    public Object[] getArgs() {
                        return remoteCall.args;
                    }

                    @Override
                    public Observable<Void> respondWith(final IncomingCallResult callResult) {
                        Observable methodCancellations = cancellations.filter(new Func1<RemoteCall, Boolean>() {
                            @Override
                            public Boolean call(RemoteCall cancellationCall) {
                                return remoteCall.correlationId.equals(cancellationCall.correlationId);
                            }
                        });

                        return callResult.value.takeUntil(methodCancellations)
                                .doOnNext(new Action1() {
                                    @Override
                                    public void call(Object o) {
                                        mResponder.onNext(remoteCall, o);
                                    }
                                })
                                .doOnError(new Action1<Throwable>() {
                                    @Override
                                    public void call(Throwable throwable) {
                                        mResponder.onError(remoteCall, throwable);
                                    }
                                })
                                .doOnCompleted(new Action0() {
                                    @Override
                                    public void call() {
                                        mResponder.onCompleted(remoteCall);
                                    }
                                })
                                .onErrorResumeNext(Observable.empty())
                                .ignoreElements().cast(Void.class);
                    }
                };
            }
        };
    }

    Func1<RemoteCall, Boolean> byServiceName(final String serviceName) {
        return new Func1<RemoteCall, Boolean>() {
            @Override
            public Boolean call(RemoteCall remoteCall) {
                return serviceName.equals(remoteCall.service);
            }
        };
    }

    Func1<RemoteCall, Observable<RemoteCall>> verifyMethodRegistration(final List<String> registeredMethods, final String serviceName) {
        return new Func1<RemoteCall, Observable<RemoteCall>>() {
            @Override
            public Observable<RemoteCall> call(final RemoteCall remoteCall) {
                if (!registeredMethods.contains(remoteCall.method)) {
                    mResponder.onError(remoteCall, new Exception("method [" + remoteCall.method + "] not found on [" + serviceName + "]"));
                    return Observable.empty();
                }
                else
                    return Observable.just(remoteCall);
            }
        };
    }

    Func1<RemoteCall, Boolean> byMethodName(final String methodName) {
        return new Func1<RemoteCall, Boolean>() {
            @Override
            public Boolean call(RemoteCall remoteCall) {
                return methodName.equals(remoteCall.method);
            }
        };
    }
}
