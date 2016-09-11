package com.metarhia.lundibundi.console.utils;

/**
 * Created by lundibundi on 7/23/16.
 */
public class Constants {
    public static final String APPLICATION_NAME = "applicationName";

    public static final String GLOBAL_BROADCAST = "globalBroadcast";

    public static final String NODE_GLOBAL = "nodeGlobal";

    public static final String ACTIVITY_CONTEXT = "activityContext";

    public static final String APPLICATION_CONTEXT = "applicationContext";

    public static final String DEFAULT_PREFS = "defaultSharedPrefs";

    public static final String MAIN_HANDLER = "mainHandler";

    // Intent categories
    public static final String CATEGORY_GLOBAL = "com.metarhia.lundibundi.console.categoryGlobal";
    public static final String CATEGORY_LOCAL_SCREEN = "com.metarhia.lundibundi.console.categoryLocalScreen";

    // Intent actions
    public static final String ACTION_CONFIGURATION_CHANGE = "com.metarhia.lundibundi.console.actionConfigurationChange";
    public static final String ACTION_CHANGE_SCREEN = "com.metarhia.lundibundi.console.actionChangeScreen";
    public static final String ACTION_NOTIFY = "com.metarhia.lundibundi.console.ACTION_NOTIFY";
    public static final String ACTION_INVALIDATE = "com.metarhia.lundibundi.console.ACTION_INVALIDATE";

    // Intent extras - data
    public static final String DATA_GLOBAL_MESSAGE = "com.metarhia.lundibundi.console.dataGlobalMessage";

    public static final String HIERARCHY_METARHIA_CONTROL = "MetarhiaControl";
    public static final String HIERARCHY_METARHIA_SCREEN = "MetarhiaScreen";

//    public static final String configTemp = "['Nyaa', \n  {\n    login: {\n      control: 'screen',\n      controls: {\n        login: {\n          control: 'edit',\n          filter: 'login',\n          top: 50, left: 50, right: 50,\n          width: 60, height: 50,\n          label: 'login'\n        },\n        password: {\n          control: 'edit',\n          mode: 'password',\n          top: 80, left: 50, right: 50,\n          height: 20,\n          label: 'password'\n        },\n        cancel: {\n          control: 'button',\n          top: 110, left: 30,\n          width: 60, height: 50,\n          text: 'Cancel'\n        },\n        signin: {\n          control: 'button',\n          top: 110, left: 100,\n          width: 60, height: 50,\n          text: 'Sign in'\n        },\n        social: {\n          control: 'panel',\n          top: 55, bottom: 10, left: 10, right: 10,\n          controls: {\n            googlePlus: {\n              control: 'button',\n              top: 0, left: 0,\n              height: 10, width: 10,\n              image: 'googlePlus'\n            },\n            facebook: {\n              control: 'button',\n              top: 0, left: 10,\n              height: 10, width: 10,\n              image: 'facebook'\n            },\n            vk: {\n              control: 'button',\n              top: 0, left: 10,\n              height: 10, width: 10,\n              image: 'vk'\n            },\n            twitter: {\n              control: 'button',\n              top: 0, left: 20,\n              height: 10, width: 10,\n              image: 'twitter'\n            }\n          }\n        }\n      }\n    },\n    main: {\n      control: 'screen',\n      controls: {\n        message: {\n          control: 'label',\n          top: 100, left: 10, right: 10,\n          height: 20,\n          text: 'You are logged in'\n        }\n      }\n    }\n}, 'whatever']";
}
