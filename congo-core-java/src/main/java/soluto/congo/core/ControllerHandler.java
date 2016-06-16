package soluto.congo.core;

import com.google.gson.Gson;
import rx.Observable;
import rx.functions.Func1;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ControllerHandler implements Func1<RemoteCall, Observable<Object>> {

    private final String serviceName;
    private final Object controller;
    private List<Method> methods = new ArrayList<>();

    public ControllerHandler(String serviceName, Object controller){
        this.serviceName = serviceName;
        this.controller = controller;
        methods = Arrays.asList(controller.getClass().getMethods());
    }

    @Override
    public Observable<Object> call(RemoteCall remoteCall) {
        if (!remoteCall.service.equals(serviceName)) return Observable.empty();
        return handleRequest(remoteCall);
    }

    public Observable<Object> handleRequest(RemoteCall remoteCall){
        try {
            Method method = getMethodByName(methods, remoteCall);
            Object[] args = getArgs(method.getParameterTypes(), remoteCall);
            method.setAccessible(true);
            Object methodResult = method.invoke(controller, args);
            if (!(methodResult instanceof Observable)) {
                return Observable.just(methodResult);
            }
            else {
                return ((Observable<Object>)methodResult);
            }
        }
        catch (Throwable ex) {
            return Observable.error(ex);
        }
    }

    private Object[] getArgs(Class<?>[] argTypes, RemoteCall remoteCall) {
        if (argTypes.length != remoteCall.args.length) {
            throw new IllegalArgumentException("wrong number of arguments for " + remoteCall.service + "/" + remoteCall.method);
        }
        Object[] fixedArgs = new Object[argTypes.length];
        Gson gson = new Gson();
        for (int i =0; i<argTypes.length;i++)
        {
            Class argType = argTypes[i];
            fixedArgs[i] = gson.fromJson(gson.toJsonTree(remoteCall.args[i]), argType);
        }
        return fixedArgs;
    }

    private Method getMethodByName(List<Method> methods, RemoteCall remoteCall) {
        for (Method method :methods) {
            if (method.getName().equals(remoteCall.method))
                return method;
        }
        throw new MethodNotExists("method not exists for " + remoteCall.service + "/" + remoteCall.method);
    }

}
