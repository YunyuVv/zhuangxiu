package com.biliww.zhuangxiu.common;

import lombok.Data;

import java.util.List;

/**
 * 分页返回体（默认 size=20，按研发评审结论）。
 */
@Data
public class PageVO<T> {

    private long total;
    private int page;
    private int size;
    private List<T> list;

    public static <T> PageVO<T> of(long total, int page, int size, List<T> list) {
        PageVO<T> vo = new PageVO<>();
        vo.setTotal(total);
        vo.setPage(page);
        vo.setSize(size);
        vo.setList(list);
        return vo;
    }
}
