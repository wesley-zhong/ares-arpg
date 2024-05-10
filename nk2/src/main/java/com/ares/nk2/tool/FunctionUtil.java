package com.ares.nk2.tool;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class FunctionUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(FunctionUtil.class);

    public static StackTraceElement getCallerElement(int index) {
        StackTraceElement[] stackTraceElementArray = Thread.currentThread().getStackTrace();
        int i = 3 + index;
        if (stackTraceElementArray.length <= i) {
            return null;
        } else {
            return stackTraceElementArray[i];
        }
    }

    public static StackTraceElement getCallerElement(int index, StackTraceElement[] stackTraceElementArray) {
        int i = 3 + index;
        if (stackTraceElementArray.length <= i) {
            return null;
        } else {
            return stackTraceElementArray[i];
        }
    }


    public static String getCallerInfo(int index) {
        StackTraceElement element = getCallerElement(index + 1);
        if (element == null) {
            return "";
        }

        return stackStraceElementFormat(element);
    }

    public static String getStackTrace() {
        StackTraceElement[] stackTraceElementArray = Thread.currentThread().getStackTrace();
        return stackStraceToString(stackTraceElementArray);
    }

    public static String stackStraceToString(StackTraceElement[] stackTraceElementArray) {
        if (stackTraceElementArray.length <= 2) {
            return "invalid stack trace";
        }
        StringBuilder builder = new StringBuilder();

        int minLength = stackTraceElementArray.length <= 50 ? stackTraceElementArray.length : 50;
        for (int i = 2; i < minLength; i++) {
            StackTraceElement element = stackTraceElementArray[i];
            builder.append(NKStringFormater.format("\t{}.{}({}:{})", element.getClassName(), element.getMethodName(), element.getFileName(), element.getLineNumber()));
        }

        return builder.toString();
    }

    public static String debugGetStackTrace() {
        if (LOGGER.isDebugEnabled()) {
            return getStackTrace();
        } else {
            return "log not debug level, do not show stack trace";
        }
    }

    public static String stackStraceElementFormat(StackTraceElement element) {
        return NKStringFormater.format("({}:{})({}.{})",
                element.getFileName(),
                element.getLineNumber(),
                element.getClassName().substring(element.getClassName().lastIndexOf('.') + 1),
                element.getMethodName());
    }
}
