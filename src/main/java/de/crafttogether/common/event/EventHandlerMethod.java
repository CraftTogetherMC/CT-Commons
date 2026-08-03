package de.crafttogether.common.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class EventHandlerMethod {
    private final Object listener;
    private final Method method;

    public EventHandlerMethod(Object listener, Method method) {
        this.listener = listener;
        this.method = method;
    }

    public void invoke(Object event) throws IllegalAccessException,
            IllegalArgumentException,
            InvocationTargetException {
        method.invoke(listener, event);
    }

    public Method getMethod() {
        return method;
    }

    public Object getListener() {
        return listener;
    }
}
