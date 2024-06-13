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
    private static volatile ExcelConfig excelConfig;

    public static void load() throws IOException {
        Tables tables = new Tables(file -> JsonParser.parseString(
                new String(Files.readAllBytes(Paths.get(dirPath, file + ".json")), "UTF-8")));
        ExcelConfig config = new ExcelConfig(tables);
        config.afterLoad();

        excelConfig = config;
        LOGGER.info("load succeed");
    }

    // 返回值：true 成功，false 失败
    public static boolean reload() {
        Tables tables;
        try {
            tables = new Tables(file -> JsonParser.parseString(
                    new String(Files.readAllBytes(Paths.get(dirPath, file + ".json")), "UTF-8")));
        }
        catch (IOException e) {
            LOGGER.error("reload error", e);
            return false;
        }

        ExcelConfig config = new ExcelConfig(tables);
        try {
            config.afterLoad();
        }
        catch (Exception e) {
            LOGGER.error("reload error", e);
            return false;
        }

        excelConfig = config;
        LOGGER.info("reload succeed");
        return true;
    }

    public static Tables getTables() {
        return excelConfig.getTables();
    }

    public static ExcelConfig getExcelConfig() {
        return excelConfig;
    }
}
