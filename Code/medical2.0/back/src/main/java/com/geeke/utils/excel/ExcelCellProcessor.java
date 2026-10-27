package com.geeke.utils.excel;
import com.geeke.common.constants.BizConstants;

import java.util.HashMap;
import java.util.Map;

public class ExcelCellProcessor {
    private Map<String, ValueConverter> converterMap = new HashMap<>();

    public ExcelCellProcessor() {
        // đăng kýGiá trịchuyển đổithiết bị
        /** Giới tính */
        converterMap.put("OutpatientLog-gender", value -> {
            String strValue = (String) value;
            if (BizConstants.GENDER_MALE.equals(strValue)) {
                return "Nam";
            } else if (BizConstants.GENDER_FEMALE.equals(strValue)) {
                return "Nữ";
            }
            return "Chưabiết";
        });
        /** ban đầu/Tái khám */
        converterMap.put("OutpatientLog-initialVisit", value -> {
            String strValue = (String) value;
            if (BizConstants.REG_SOURCE_NORMAL.equals(strValue)) {
                return "Khám lần đầu";
            } else {
                return "Tái khám";
            }
        });
        /** Bệnh truyền nhiễm */
        converterMap.put("OutpatientLog-infect", value -> {
            String strValue = (String) value;
            if (BizConstants.INFECT_TYPE_NONE.equals(strValue)) {
                return "是";
            } else {
                return " không ";
            }
        });
    }

    public String convert(String propertyName, Object propertyValue) {
        // LấyGiá trịchuyển đổithiết bị
        ValueConverter converter = converterMap.get(propertyName);
        if (converter != null) {
            return converter.convert(propertyValue);
        }
        // Mặc định trả về dạng chuỗi của giá trị thuộc tính
        return String.valueOf(propertyValue);
    }

    // Giá trịchuyển đổithiết bịgiao diện
    interface ValueConverter {
        String convert(Object obj);
    }
}
