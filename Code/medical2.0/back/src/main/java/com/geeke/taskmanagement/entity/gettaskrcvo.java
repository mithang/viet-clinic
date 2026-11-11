package com.geeke.taskmanagement.entity;

import lombok.Data;

@Data
public class gettaskrcvo {
    private int limit;
    private int offset;
    public String kssj;
    public String jssj;
    public String jgid;
    public String tasktype;
    public String taskstatus;
    //public String auditresult;
    public String taskname;
    public String companyId;

    public String taskmanagementid;

    /**
     * DS204: limit chi co tran tren, offset khong am (mirror
     * Visitprogresspara) — tran 10000 = PageRequest.MAX_PAGE_SIZE.
     * Truoc day limit client di thang vao LIMIT #{limit} o
     * selecttasklist khong co guard nao.
     */
    public boolean isValidLimit() {
        return limit >= 0 && limit <= com.geeke.common.data.PageRequest.MAX_PAGE_SIZE;
    }

    public boolean isValidOffset() {
        return offset >= 0;
    }
}
