package soluto.congo.core;

import com.google.gson.Gson;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.functions.Func1;

public class Bridge {

    private Object controller;
    private RpcListener rpcListener;
    private List<Subscription> bridgeSubscription = new ArrayList<Subscription>();

    public Bridge(Object controller, RpcListener rpcListener) {
        this.controller = controller;
        this.rpcListener = rpcListener;
    }

    public void startListening() {
        forwardTo(controller);
    }

    public void stopListening() {
        for (Subscription subscription : bridgeSubscription) {
            subscription.unsubscribe();
        }
        bridgeSubscription.clear();
    }

    private void forwardTo(Object instance) {
        Class type = instance.getClass();

        for (Method method : instance.getClass().getMethods()) {
            if (method.getDeclaringClass() != type) continue;
            if (method.isSynthetic()) continue;
            Observable<IncomingCall<IncomingCallResult>> incomingCallsStream = rpcListener.on(method.getName());

            bridgeSubscription.add(incomingCallsStream.flatMap(handleIncomingCall(instance, method)).subscribe());
        }
    }

    private Func1<IncomingCall<IncomingCallResult>, Observable<Void>> handleIncomingCall(final Object instance, final Method method) {
        return new Func1<IncomingCall<IncomingCallResult>,Observable<Void>>() {
            @Override
            public Observable<Void> call(final IncomingCall<IncomingCallResult> incomingCall) {
                IncomingCallResult result;
                try {
                    method.setAccessible(true);
                    Object methodResult = method.invoke(instance, fixTyping(incomingCall.getArgs(), method.getParameterTypes()));
                    result = new IncomingCallResult(methodResult);

                } catch (ArrayIndexOutOfBoundsException ex) {
                    Exception exception = new Exception("Bridge: method ["+method.getName()+"] of ["+instance.getClass().getSimpleName()+"] controller was invoked with wrong number of arguments");
                    result = new IncomingCallResult(exception);

                } catch (Exception ex) {
                    Exception exception = new Exception("Bridge: method ["+method.getName()+"] of ["+instance.getClass().getSimpleName()+"] controller invocation caused an exception - " + ex.getCause().getMessage());
                    result = new IncomingCallResult(exception);
                }
                return incomingCall.respondWith(result);
            }
        };
    }

    private static Object[] fixTyping(Object[] realArgs,  Class[] argTypes ) {
        Object[] fixedArgs = new Object[argTypes.length];
        Gson gson = new Gson();
        for (int i =0; i<argTypes.length;i++)
        {
            Class argType = argTypes[i];
            fixedArgs[i] = gson.fromJson(gson.toJsonTree(realArgs[i]), argType);
        }
        return fixedArgs;
    }
}
