package com.biliww.zhuangxiu.common;

import java.util.List;

/**
 * 业务常量：标准 8 阶段工序、附件关联类型。
 */
public final class Constants {

    private Constants() {
    }

    /** 默认阶段模板（新建项目时初始化；用户可增/删/改/排序，非强制固定）。 */
    public static final List<String> STANDARD_PHASES = List.of(
            "拆改", "水电", "泥瓦", "木工", "油漆", "安装", "软装", "验收");

    public static final String REF_PROJECT = "PROJECT";
    public static final String REF_PHASE = "PHASE";
    public static final String REF_EXPENSE = "EXPENSE";
}
