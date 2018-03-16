package com.why.platform.framework.engine.thread;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhouxl on 2016/11/17 0017.
 */
public final class ThreadContext {
    private static ThreadLocal<Map<String, Object>> context = new ThreadLocal<Map<String, Object>>() {

        @Override
        protected Map<String, Object> initialValue() {
            return new HashMap<String, Object>();
        }
    };

    @SuppressWarnings("unchecked")
    public static <T> T get(String name) {
        Map<String, Object> t_context = context.get();
        Object v = t_context.get(name);
        return (T) v;
    }

    public static void put(String name, Object value) {
        context.get().put(name, value);
    }

    public static void remove(String name) {
        context.get().remove(name);
    }

    public static void clear() {
        context.remove();
    }

    public static Map<String, Object> getContext() {
        return context.get();
    }

    public static void setContext(Map<String, Object> _context) {
		/* 父线程创建的contex跟当前线程的context可能是同一个引用 */
		/* 所以创建新的map,作为新的context */
        Map<String, Object> t_context = new HashMap<String, Object>(_context);
        context.set(t_context);
    }
}
