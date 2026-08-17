package com.biliww.zhuangxiu.common;

import java.util.List;

/**
 * 业务常量：标准 8 阶段工序、附件关联类型。
 */
public final class Constants {

    private Constants() {
    }

    /** 标准装修工序（固定 8 阶段，P0）。 */
    public static final List<String> STANDARD_PHASES = List.of(
            "拆改", "水电", "泥瓦", "木工", "油漆", "安装", "软装", "验收");

    public static final String REF_PROJECT = "PROJECT";
    public static final String REF_PHASE = "PHASE";
    public static final String REF_EXPENSE = "EXPENSE";
}
