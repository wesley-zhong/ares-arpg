package com.ares.nk2.tool;

import org.slf4j.helpers.MessageFormatter;

public class NKStringFormater {
    static public String format(String format, Object... arguments) {
        return MessageFormatter.arrayFormat(format, arguments).getMessage();
    }
}
