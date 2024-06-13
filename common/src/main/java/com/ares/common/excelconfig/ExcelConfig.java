package com.ares.common.excelconfig;

import cfg.Tables;

public class ExcelConfig {
    private final Tables tables;

    public ExcelConfig(Tables tables) {
        this.tables = tables;
    }

    public Tables getTables() {
        return tables;
    }

    // 做后处理
    public void afterLoad() {

    }
}
