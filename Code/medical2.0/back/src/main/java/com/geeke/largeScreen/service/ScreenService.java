package com.geeke.largeScreen.service;


import com.geeke.largeScreen.dao.ScreenDao;
import com.geeke.largeScreen.entity.Basics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Màn hình dữ liệu lớnService
 * @author zhanghan
 * @version 2021-10-12
 */
 
@Service("ScreenService")
@Transactional(readOnly = true)
public class ScreenService{
    @Autowired
    private ScreenDao screenDao;

    /** Lấylớn屏cơ bảnDữ liệu */
    public Basics getBasics(String parentId) {
        Basics basics = new Basics();
        basics.setQuest(screenDao.getQuest(parentId));
        basics.setRegister(screenDao.getRegister(parentId));
        basics.setReceivePatients(screenDao.getReceivePatients(parentId));
        basics.setIncome(screenDao.getIncome(parentId));
        // basics.setSigning(screenDao.getSigning(parentId));

        return basics;
    }


}