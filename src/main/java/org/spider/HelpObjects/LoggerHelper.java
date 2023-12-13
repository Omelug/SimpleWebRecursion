package org.spider.HelpObjects;

import lombok.Data;

@Data
public class LoggerHelper {
    static boolean DEBUG = false;

    static boolean info = false;
    static boolean error = false;
    static boolean warning = false;

    public LoggerHelper(boolean debug) {
        DEBUG = debug;
    }

    public void enableDebug() {
        DEBUG = true;
    };
    public void disableDebug() {
        DEBUG = false;
    };

    public void info(String string){
        if (DEBUG && info) System.out.println(string);
    }
    public void error(String string){
        if (DEBUG && error) System.out.println(string);
    }
    public void warning(String string){
        if (DEBUG && warning) System.out.println(string);
    }

}
