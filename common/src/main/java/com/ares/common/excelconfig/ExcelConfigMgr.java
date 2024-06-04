package com.ares.common.excelconfig;

import cfg.Tables;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ExcelConfigMgr {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExcelConfigMgr.class);

    private static final String dirPath = "./excel-json";
    private static volatile Tables tables;

    public static void load() throws IOException {
        Tables tables1 = new Tables(file -> JsonParser.parseString(
                new String(Files.readAllBytes(Paths.get(dirPath, file + ".json")), "UTF-8")));
        tables = tables1;

        LOGGER.info("load succeed");
    }

    // 返回值：true 成功，false 失败
    public static boolean reload() {
        Tables tables1;
        try {
            tables1 = new Tables(file -> JsonParser.parseString(
                    new String(Files.readAllBytes(Paths.get(dirPath, file + ".json")), "UTF-8")));
        }
        catch (IOException e) {
            LOGGER.error("reload error", e);
            return false;
        }

        tables = tables1;
        LOGGER.info("reload succeed");
        return true;
    }

    public static Tables getTables() {
        return tables;
    }
}
