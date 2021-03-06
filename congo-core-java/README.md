# Congo Core - Java
Java implementation of [Congo](https://github.com/Soluto/congo-core)

## Installation
In your ```build.gradle``` file add:
```
repositories {
   ...
    maven {
        url "https://dl.bintray.com/soluto/soluto-jars"
    }
    ...
   
}
...
dependencies {
    ...
    compile 'soluto:congo-core-java:0.0.4'
    ...
}
```

### Usage
This example usage is based on the library tests. For real app example see: [Congo Examples](https://github.com/Soluto/congo-examples)

Setup the communication layer:
```java
PublishSubject<RemoteCall> requestStream = PublishSubject.create();
PublishSubject<RemoteCallResult> responseStream = PublishSubject.create();
```

Create a ```RemoteCallListener``` that will receive incoming ```RemoteCall``` from the communication layer:
```java
RemoteCallListener listener = new TestRemoteCallListener(requestStream);
```
Create a ```RemoteCallResponder``` that will send ```RemoteCallResult``` through the communication layer:
```java
RemoteCallResponder responder = new TestRemoteCallResponder(responseStream);
```
Creata a ```Router``` that maps incoming ```RemoteCall``` from the listener to the implementing service, and forwards the result as a ```RemoteCallResult``` to the responder:
```java
Router router = new Router(listener, responder);

router.use(new ControllerHandler("someService", new Object() {
   public Observable<String> someMethod(final String text) {
       return Observable.interval(1, TimeUnit.SECONDS).map(new Func1<Long, String>() {
           @Override
           public String call(Long aLong) {
               return text;
           }
       });
   }
}));

router.listen();
```
In order to execute remote call, the client need to invoke a ```RemoteCall``` using the  ```RemoteCallInvoker```:
```java
RemoteCallInvoker invoker = new TestRemoteCallInvoker(requestStream, responseStream);

RemoteCall remoteCall = new RemoteCall();
remoteCall.service = "someService";
remoteCall.method = "someMethod";
remoteCall.args = ["Hello World!"];
remoteCall.correlationId = UUID.randomUUID().toString();

Observable<String> resultStream = remoteCallInvoker.invoke(remoteCall).cast(String.class)
```
Subscribe to ```resultStream ``` to get items from the service
