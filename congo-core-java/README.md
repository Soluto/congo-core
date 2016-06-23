# Congo Core - Java
Java implementation of [Congo](https://github.com/Soluto/congo-core)

### Usage
This is an example usage based on the library tests. For real app example see: https://github.com/Soluto/congo-examples

Setup the listener and the responder:
```java
PublishSubject<RemoteCall> requestStream = PublishSubject.create();
PublishSubject<RemoteCallResult> responseStream = PublishSubject.create();

RemoteCallListener listener = new TestRemoteCallListener(requestStream);
RemoteCallResponder responder = new TestRemoteCallResponder(responseStream);

router = new Router(listener, responder);
router.use(new ControllerHandler("someService", new SomeService()));
router.listen();
```

Invoke remote call with the invoker:
```java
RemoteCallInvoker invoker = new TestRemoteCallInvoker(requestStream, responseStream);
RemoteCall remoteCall = createCall(UUID.randomUUID(), "someService", "someMethod");

Observable<String> resultStream = remoteCallInvoker.invoke(remoteCall).cast(String.class)
```
Subscribe to ```resultStream ``` to get items from the service
