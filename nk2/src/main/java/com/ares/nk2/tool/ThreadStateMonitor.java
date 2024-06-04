package com.ares.nk2.tool;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class ThreadStateMonitor {
    private static final Logger logger = LoggerFactory.getLogger(ThreadStateMonitor.class);

    public static String getStackTraceSelfClass() {
        StringBuffer sb = new StringBuffer();
        sb.append("\n");
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        if (stackTraceElements.length <= 2) {
            return "invalid stack trace";
        }

        for (int i = 2; i < stackTraceElements.length; i++) {
            StackTraceElement e = stackTraceElements[i];
            if (e.getClassName().startsWith("com.kuro")) {
                String fullClassName = e.getClassName();
                sb.append("\t");
                sb.append(fullClassName.substring(fullClassName.lastIndexOf('.') + 1)).append(".");
                sb.append(e.getMethodName()).append("(");
                sb.append(e.getFileName()).append(":");
                sb.append(e.getLineNumber()).append(")\n");
            }
        }
        return sb.toString();
    }
}
