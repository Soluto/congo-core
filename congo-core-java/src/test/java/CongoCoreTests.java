import helpers.ControllerForTests;
import helpers.SomeObject;
import org.junit.Before;
import org.junit.Test;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.schedulers.TestScheduler;
import rx.subjects.PublishSubject;
import soluto.congo.core.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CongoCoreTests {
    Router router;
    RemoteCallResponder remoteCallResponder;
    RemoteCallListener remoteCallListener;
    RemoteCallInvoker remoteCallInvoker;

    PublishSubject<RemoteCall> requestStream = PublishSubject.create();
    PublishSubject<RemoteCallResult> responseStream = PublishSubject.create();

    @Before
    public void setup() {
        remoteCallInvoker = new TestRemoteCallInvoker(requestStream, responseStream);
        remoteCallListener = new TestRemoteCallListener(requestStream);
        remoteCallResponder = new TestRemoteCallResponder(responseStream);
        router = new Router(remoteCallListener, remoteCallResponder);
    }

    @Test
    public void invokeRemoteCall_controllerReturnsObject_shouldReturnResult() throws InterruptedException {
        router.use(new ControllerHandler("someService", new ControllerForTests()));
        router.listen();

        RemoteCall remoteCall = createCall(UUID.randomUUID(), "someService", "methodThatReturnsComplexResult");

        SomeObject someResult = remoteCallInvoker.invoke(remoteCall).cast(SomeObject.class).toBlocking().first();
        assertEquals(someResult.someBool, true);
        assertEquals(someResult.someInt, 7);
    }

    @Test
    public void invokeRemoteCall_methodOnAnonymousObject_shouldReturnResult() {
        router.use(new ControllerHandler("someService", new Object() {
            public SomeObject someMethod() {
                SomeObject someResult = new SomeObject();
                someResult.someInt = 7;
                someResult.someBool = true;
                return someResult;
            }
        }));
        router.listen();

        RemoteCall remoteCall = createCall(UUID.randomUUID(), "someService", "someMethod");

        SomeObject someResult = remoteCallInvoker.invoke(remoteCall).cast(SomeObject.class).toBlocking().first();
        assertEquals(someResult.someBool, true);
        assertEquals(someResult.someInt, 7);
    }

    @Test
    public void invokeRemoteCall_controllerReturnsObservable_shouldReturnResult() {
        router.use(new ControllerHandler("someService", new ControllerForTests()));
        router.listen();

        RemoteCall remoteCall = createCall(UUID.randomUUID(), "someService", "methodThatReturnsObservableWithComplextResult");

        SomeObject someResult = remoteCallInvoker.invoke(remoteCall).cast(SomeObject.class).toBlocking().first();
        assertEquals(someResult.someBool, true);
        assertEquals(someResult.someInt, 7);
    }

    @Test
    public void invokeRemoteCall_controllerShouldReturnObjectButThrowsException_shouldThrowException() {
        router.use(new ControllerHandler("someService", new ControllerForTests()));
        router.listen();

        RemoteCall remoteCall = createCall(UUID.randomUUID(), "someService", "methodThatThrowsException");

        try {
            remoteCallInvoker.invoke(remoteCall).cast(SomeObject.class).toBlocking().first();
            fail();
        } catch (Exception e) {
            assertTrue(true);
        }
    }

    @Test
    public void invokeRemoteCall_controllerReturnsErrorObservable_shouldThrowException() {
        router.use(new ControllerHandler("someService", new ControllerForTests()));
        router.listen();

        RemoteCall remoteCall = createCall(UUID.randomUUID(), "someService", "methodThatReturnsObservableError");

        try {
            remoteCallInvoker.invoke(remoteCall).cast(SomeObject.class).toBlocking().first();
            fail();
        } catch (Exception e) {
            assertTrue(true);
        }
    }

    @Test
    public void invokeRemoteCall_controllerShouldReturnObservableButThrowsException_shouldThrowException() {
        router.use(new ControllerHandler("someService", new ControllerForTests()));
        router.listen();

        RemoteCall remoteCall = createCall(UUID.randomUUID(), "someService", "methodThatReturnsObservableError");

        try {
            remoteCallInvoker.invoke(remoteCall).cast(SomeObject.class).toBlocking().first();
            fail();
        } catch (Exception e) {
            assertTrue(true);
        }
    }

    @Test
    public void sendRemoteCall_methodDoesNotExistsOnController_shouldSendErrorResponseExactlyOnce() {
        router.use(new ControllerHandler("someService", new ControllerForTests()));
        router.listen();

        RemoteCall remoteCall = createCall(UUID.randomUUID(), "someService", "methodThatDoesNotExist");

        try {
            remoteCallInvoker.invoke(remoteCall).cast(SomeObject.class).toBlocking().first();
        } catch (MethodNotExists e) {
            assertTrue(true);
        }
    }

    @Test
    public void sendRemoteCall_controllerFirstMethodThrowsException_sendAnotherRemoteCall_shouldSendSuccessResponse() {
        router.use(new ControllerHandler("someService", new ControllerForTests()));
        router.listen();

        RemoteCall remoteCall1 = createCall(UUID.randomUUID(), "someService", "methodThatThrowsException");
        try {
            remoteCallInvoker.invoke(remoteCall1).cast(SomeObject.class).toBlocking().first();
        } catch (Exception e) {

        }

        RemoteCall remoteCall12 = createCall(UUID.randomUUID(), "someService", "methodThatRunSuccessfully");
        remoteCallInvoker.invoke(remoteCall12).toBlocking().first();
    }

    @Test
    public void sendRemoteCall_controllerFirstMethodReturnsObservableError_sendAnotherRemoteCall_shouldSendSuccessResponse() {
        router.use(new ControllerHandler("someService", new ControllerForTests()));
        router.listen();

        RemoteCall remoteCall1 = createCall(UUID.randomUUID(), "someService", "methodThatReturnsObservableError");
        try {
            remoteCallInvoker.invoke(remoteCall1).cast(SomeObject.class).toBlocking().first();
        } catch (Exception e) {

        }

        RemoteCall remoteCall12 = createCall(UUID.randomUUID(), "someService", "methodThatRunSuccessfully");
        remoteCallInvoker.invoke(remoteCall12).cast(Void.class).toBlocking().first();
    }

    @Test
    public void sendRemoteCall_methodThatDoesNotExist_sendAnotherRemoteCall_shouldSendSuccessResponse() {
        router.use(new ControllerHandler("someService", new ControllerForTests()));
        router.listen();

        RemoteCall remoteCall1 = createCall(UUID.randomUUID(), "someService", "methodThatDoesNotExist");
        try {
            remoteCallInvoker.invoke(remoteCall1).cast(SomeObject.class).toBlocking().first();
        } catch (MethodNotExists e) {
        }

        RemoteCall remoteCall2 = createCall(UUID.randomUUID(), "someService", "methodThatRunSuccessfully");
        remoteCallInvoker.invoke(remoteCall2).cast(Void.class).toBlocking().first();
    }

    @Test
    public void sendRemoteCallWithArguments_controllerHasMethodThatAcceptArguments_shouldSendSuccessResponse() {
        router.use(new ControllerHandler("someService", new ControllerForTests()));
        router.listen();

        RemoteCall remoteCall = createCall(UUID.randomUUID(), "someService", "methodWithArguments", "first", "second");

        String someResult = remoteCallInvoker.invoke(remoteCall).cast(String.class).toBlocking().first();
        assertEquals(someResult, "first_second");
    }

    @Test
    public void sendRemoteCallWithArguments_controllerReturnsPrimitiveResult_shouldSendSuccessResponse() {
        router.use(new ControllerHandler("someService", new ControllerForTests()));
        router.listen();

        RemoteCall remoteCall = createCall(UUID.randomUUID(), "someService", "methodThatReturnsPrimitive", 5);

        int someResult = remoteCallInvoker.invoke(remoteCall).cast(Integer.class).toBlocking().first();
        assertEquals(someResult, 5);
    }

    @Test
    public void sendRemoteCallWithArguments_controllerMethodAcceptsComplexObject_shouldSendSuccessResponse() {
        router.use(new ControllerHandler("someService", new ControllerForTests()));
        router.listen();

        RemoteCall remoteCall = createCall(UUID.randomUUID(), "someService", "methodWithComplexArgument", new SomeObject(5, true));

        remoteCallInvoker.invoke(remoteCall).cast(Void.class).toBlocking().first();
    }

    @Test
    public void sendRemoteCallWithArguments_numberOfArgumentsInTheRemoteCallIsDifferentThanTheArgumentsInTheController_shouldSendErrorResponse() {
        router.use(new ControllerHandler("someService", new ControllerForTests()));
        router.listen();

        RemoteCall remoteCall = createCall(UUID.randomUUID(), "someService", "methodWithArguments");

        try {
            remoteCallInvoker.invoke(remoteCall).cast(Void.class).toBlocking().first();
        } catch (IllegalArgumentException ex) {
            assertTrue(true);
        }
    }

    @Test
    public void invokeRemoteCallTwice_differentCorrelationId_shouldReturnResultTwice() {
        router.use(new ControllerHandler("someService", new ControllerForTests()));
        router.listen();

        RemoteCall remoteCall1 = createCall(UUID.randomUUID(), "someService", "methodThatRunSuccessfully");
        remoteCallInvoker.invoke(remoteCall1).cast(Void.class).toBlocking().first();

        RemoteCall remoteCall2 = createCall(UUID.randomUUID(), "someService", "methodThatRunSuccessfully");
        remoteCallInvoker.invoke(remoteCall2).cast(Void.class).toBlocking().first();
    }

    @Test
    public void shutdown_invokeRemoteCall_doesNothing() {
        router.use(new ControllerHandler("someService", new ControllerForTests()));
        router.listen();

        RemoteCall remoteCall1 = createCall(UUID.randomUUID(), "someService", "methodThatRunSuccessfully");
        remoteCallInvoker.invoke(remoteCall1).cast(Void.class).toBlocking().first();

        router.shutdown();

        RemoteCall remoteCall2 = createCall(UUID.randomUUID(), "someService", "methodThatRunSuccessfully");

        try {
            remoteCallInvoker.invoke(remoteCall2).timeout(100, TimeUnit.MILLISECONDS).toBlocking().first();
        } catch (Exception e) {
            if (e.getCause().getClass() == TimeoutException.class) {
                assertTrue(true);
                return;
            }
        }
        fail();
    }

    @Test
    public void doubleInvocation_observables() {
        final TestScheduler scheduler = new TestScheduler();

        router.use(new ControllerHandler("someService", new Object() {
            public Observable<Long> take(int number) {
                return Observable.interval(1, TimeUnit.SECONDS, scheduler).take(number);
            }
        }));
        router.listen();

        RemoteCall remoteCall1 = createCall(UUID.randomUUID(), "someService", "take", 10);
        final List<Object> invocation1Results = new ArrayList<>();
        remoteCallInvoker.invoke(remoteCall1)
                .doOnNext(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        invocation1Results.add(o);
                    }
                })
                .subscribe();

        RemoteCall remoteCall2 = createCall(UUID.randomUUID(), "someService", "take", 10);
        final List<Object> invocation2Results = new ArrayList<>();
        remoteCallInvoker.invoke(remoteCall2)
                .doOnNext(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        invocation2Results.add(o);
                    }
                })
                .subscribe();

        scheduler.advanceTimeBy(2, TimeUnit.SECONDS);
        assertEquals(2, invocation1Results.size());
        assertEquals(2, invocation2Results.size());
    }

    @Test
    public void doubleInvocation_twoObservablesAreRunning_oneIsStopped_otherContinues() {
        final TestScheduler scheduler = new TestScheduler();

        router.use(new ControllerHandler("someService", new Object() {
            public Observable<Long> take(int number) {
                return Observable.interval(1, TimeUnit.SECONDS, scheduler).take(number);
            }
        }));
        router.listen();

        RemoteCall remoteCall1 = createCall(UUID.randomUUID(), "someService", "take", 10);
        final List<Object> invocation1Results = new ArrayList<>();
        Subscription invocation1Subscription = remoteCallInvoker.invoke(remoteCall1)
                .doOnNext(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        invocation1Results.add(o);
                    }
                })
                .subscribe();

        RemoteCall remoteCall2 = createCall(UUID.randomUUID(), "someService", "take", 10);
        final List<Object> invocation2Results = new ArrayList<>();
        remoteCallInvoker.invoke(remoteCall2)
                .doOnNext(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        invocation2Results.add(o);
                    }
                })
                .subscribe();

        scheduler.advanceTimeBy(2, TimeUnit.SECONDS);
        invocation1Subscription.unsubscribe();
        scheduler.advanceTimeBy(8, TimeUnit.SECONDS);

        assertEquals(2, invocation1Results.size());
        assertEquals(10, invocation2Results.size());
    }

    @Test
    public void doubleInvocation_methods() {
        final TestScheduler scheduler = new TestScheduler();

        router.use(new ControllerHandler("someService", new Object() {
            public String echo(String value) {
                return value;
            }
        }));
        router.listen();

        RemoteCall remoteCall1 = createCall(UUID.randomUUID(), "someService", "echo", "hello");
        final List<Object> invocation1Results = new ArrayList<>();
        remoteCallInvoker.invoke(remoteCall1).observeOn(scheduler)
                .doOnNext(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        invocation1Results.add(o);
                    }
                })
                .subscribe();

        RemoteCall remoteCall2 = createCall(UUID.randomUUID(), "someService", "echo", "world");
        final List<Object> invocation2Results = new ArrayList<>();
        remoteCallInvoker.invoke(remoteCall2).observeOn(scheduler)
                .doOnNext(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        invocation2Results.add(o);
                    }
                })
                .subscribe();

        scheduler.advanceTimeBy(10, TimeUnit.SECONDS);
        assertEquals("hello", invocation1Results.get(0));
        assertEquals("world", invocation2Results.get(0));
    }

    @Test
    public void invokeRemoteCall_actionHandler() {


    }

    private RemoteCall createCall(UUID correlationId, String service, String method, Object... args) {
        RemoteCall remoteCall = new RemoteCall();
        remoteCall.service = service;
        remoteCall.method = method;
        remoteCall.args = args;
        remoteCall.correlationId = correlationId.toString();
        remoteCall.isCancelled = false;
        return remoteCall;
    }
}
