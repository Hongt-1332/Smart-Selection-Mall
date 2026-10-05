package edu.fafu.tool.page;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class PageResult<T> {

    private int pageNum;
    private int pageSize;
    private long total;
    private int pages;
    private boolean hasPreviousPage;
    private boolean hasNextPage;
    private List<T> list;
    private Map<String, Object> extra;

    public PageResult(IPage<T> iPage) {
        this.pageNum = (int) iPage.getCurrent();
        this.pageSize = (int) iPage.getSize();
        this.total = iPage.getTotal();
        this.pages = (int) iPage.getPages();
        this.hasPreviousPage = iPage.getCurrent() > 1;
        this.hasNextPage = iPage.getCurrent() < iPage.getPages();
        this.list = iPage.getRecords();
    }

    public PageResult<T> addExtra(String key, Object value) {
        if (this.extra == null) this.extra = new HashMap<>();
        this.extra.put(key, value);
        return this;
    }
}