package com.metarhia.lundibundi.console.contracts;

import android.content.Context;
import android.os.Handler;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;
import com.eclipsesource.v8.V8Value;
import com.eclipsesource.v8.utils.V8ObjectUtils;
import com.metarhia.lundibundi.console.MetarhiaObject;
import com.metarhia.lundibundi.console.utils.Utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by lundibundi on 7/25/16.
 */
public abstract class MetarhiaContractUtils {

    public static final int methodDefaultPrefixLength = 3;

//    public static <T extends V8Object, F> boolean postUpdate(final Context context, final T params, final Utils.RunnableWithParams<F> updater) {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                final F adaptedParams;
//                if (params instanceof V8Array) {
//                    adaptedParams = (F) V8ObjectUtils.toList((V8Array) params);
//                } else if (params instanceof V8Object) {
//                    adaptedParams = (F) V8ObjectUtils.toMap(params);
//                } else {
//                    adaptedParams = (F) params;
//                }
//                updater.setParameters(adaptedParams);
//
//                Handler mainHandler = new Handler(context.getMainLooper());
//                mainHandler.post(updater);
//            }
//        }).start();
//        return true;
//    }
//
//    public static <T, F extends V8Value> boolean postUpdateValue(final Context context, final Map<String, ParameterConf> dslToConfiguration, final T object,
//                                                                 final String name, final F params) {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                final Object adaptedParams;
//                if (params instanceof V8Array) {
//                    adaptedParams = V8ObjectUtils.toList((V8Array) params);
//                } else if (params instanceof V8Object) {
//                    adaptedParams = V8ObjectUtils.toMap((V8Object) params);
//                } else {
//                    adaptedParams = params;
//                }
//
//                Handler mainHandler = new Handler(context.getMainLooper());
//                mainHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        updateValue(dslToConfiguration, object, name, adaptedParams);
//                    }
//                });
//            }
//        }).start();
//        return true;
//    }
//
//    public static <T extends MetarhiaObject, F extends V8Object> boolean postUpdate(final Context context, final T object,
//                                                                                        final F params) {
//        return postUpdate(context, params, new Utils.RunnableWithParams<Map<String, ?>>() {
//            @Override
//            public void run() {
//                object.updateConfiguration(getParameters());
//            }
//        });
//    }

    // todo make update value accept dsl's with overrrides to avoid method duplication
    public static <T, F> boolean updateValue(Map<String, ParameterConf> dslToConfiguration, T object,
                                             String name, F params) {
        ParameterConf conf = dslToConfiguration.get(name);
        if (conf != null && conf.setter != null) {
            try {
                if (conf.includeFieldName) {
                    conf.setter.invoke(null, object, name, params);
                } else {
                    conf.setter.invoke(null, object, params);
                }
                return true;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static void registerMethodsOnDSL(Map<String, ParameterConf> parameterConfMap, Class<?> methodsOrigin) {
        registerMethodsOnDSL(parameterConfMap, new HashSet<>(Collections.singletonList("updateValue")), methodsOrigin);
    }

    public static void registerMethodsOnDSL(Map<String, ParameterConf> parameterConfMap, Set<String> excludedMethods, Class<?> methodsOrigin) {
        for (Method setter : methodsOrigin.getDeclaredMethods()) {
            if (excludedMethods.contains(setter.getName())) continue;

            String controlName = setter.getName().substring(methodDefaultPrefixLength).toLowerCase();
            ParameterConf param = parameterConfMap.get(controlName);
            if (param == null) {
                parameterConfMap.put(controlName, new ParameterConf(setter));
            } else if (param.setter == null) {
                param.setter = setter;
            }
        }
    }

    public static void registerCustomMethodsOnDSL(Map<String, ParameterConf> parameterConfMap, Class<?> methodsOrigin) {
        Map<String, Method> methods = new HashMap<>();
        for (Method setter : methodsOrigin.getDeclaredMethods()) {
            methods.put(setter.getName(), setter);
        }

        for (Map.Entry<String, ParameterConf> me : parameterConfMap.entrySet()) {
            if (me.getValue().setter == null) {
                me.getValue().setter = methods.get(me.getValue().methodName);
            }
        }
    }

    public static List<Class<?>> getSortedAppliableContracts(Class<? extends Object> clazz) {
        try {
            return (List<Class<?>>) clazz.getDeclaredMethod("getSortedAppliableContracts").invoke(null);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    // TODO this should be done at compile time for every <MetarhiaContract...> class + generate V8Object for prototype
    public static Map<String, String> generateDefaultConfiguration(List<Class<?>>... appliableContracts) {
        Map<String, String> configuration = new HashMap<>();
        for (List<Class<?>> classContracts : appliableContracts) {
            for (Class<?> contract : classContracts) {
                try {
                    Map<String, ParameterConf> contractDSL = (Map<String, ParameterConf>) contract.getDeclaredField("dslToConfiguration").get(null);

                    for (Map.Entry<String, ParameterConf> parameter : contractDSL.entrySet()) {
                        final ParameterConf value = parameter.getValue();
                        if (value.optional) {
                            configuration.put(parameter.getKey(), value.defaultValue);
                        }
                    }
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return configuration;
    }

    /**
     * Created by lundibundi on 8/26/16.
     */
    public static class ParameterConf {
        public String methodName;
        public String defaultValue;
        public Method setter;
        public boolean includeFieldName;
        public boolean optional;

        public ParameterConf() {
            this("null");
        }

        public ParameterConf(String defaultValue) {
            this(null, defaultValue);
        }

        public ParameterConf(String defaultValue, boolean optional) {
            this(null, defaultValue, null, false, optional);
        }

        public ParameterConf(String methodName, String defaultValue) {
            this(methodName, defaultValue, null, false, false);
        }

        public ParameterConf(Method setter, boolean includeFieldName, boolean optional) {
            this(null, "null", setter, includeFieldName, optional);
        }

        public ParameterConf(Method setter) {
            this(null, "null", setter, false, false);
        }

        public ParameterConf(String methodName, String defaultValue, Method setter,
                             boolean includeFieldName, boolean optional) {
            this.methodName = methodName;
            this.defaultValue = defaultValue;
            this.setter = setter;
            this.includeFieldName = includeFieldName;
            this.optional = optional;
        }
    }

}
