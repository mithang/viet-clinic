package com.geeke.outpatient.controller;

import com.bstek.ureport.definition.datasource.BuildinDatasource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
class TestBuildinDatasource implements BuildinDatasource {
    @Autowired
    private DataSource dataSource;
    /**
     * @return Trả vềDữ liệunguồnTên
     */
    @Override
    public String name() {
        return "trongđặtDữ liệunguồnDEMO";
    }
    /**
     * @return Trả vềHiện tạilấydùngDữ liệunguồn của mộtCáiliêntiếp
     */
    @Override
    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
