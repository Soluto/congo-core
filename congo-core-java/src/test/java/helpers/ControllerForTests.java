package helpers;

import rx.Observable;

public class ControllerForTests {

    public void methodThatRunSuccessfully() { }

    public SomeObject methodThatReturnsComplexResult() {
        SomeObject someResult = new SomeObject();
        someResult.someInt = 7;
        someResult.someBool = true;
        return someResult ;
    }

    public String methodWithArguments(String param1, String param2) {
        return param1 + "_" + param2;
    }

    public int methodThatReturnsPrimitive(int expectedResult) {
        return expectedResult;
    }

    public void methodWithComplexArgument(SomeObject someObject){  }

    public Observable<SomeObject> methodThatReturnsObservableWithComplextResult() {
        SomeObject someResult = new SomeObject();
        someResult.someInt = 7;
        someResult.someBool = true;
        return Observable.just(someResult);
    }

    public void methodThatThrowsException() throws Exception {
        throw new Exception("There was an error!");
    }

    public Observable<SomeObject> methodThatReturnsObservableError() {
        return Observable.error(new Exception("There was an error!"));

    }

    public Observable<SomeObject> methodThatShouldReturnObservableButThrowsException() throws Exception {
        throw new Exception("There was an error!");

    }

}
