#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Áp dụng bản dịch Trung -> Việt cho các chuỗi HIỂN THỊ cho người dùng.

Nguyên tắc:
  - Chỉ thay chuỗi hiển thị / message, KHÔNG đụng giá trị dùng để so sánh
    với dữ liệu DB (dict value, mã trạng thái, tên cột Excel).
  - Mỗi thay thế khai báo số lần xuất hiện mong đợi; lệch -> báo lỗi.
"""
import os
import sys
import io

ROOT = 'Code/medical2.0/client/src'

# (file, old, new, expected_count)
R = [
    # ---------- sidebar / footer ----------
    ('components/sidebar/index.vue',
     'Copyright © 2025 湖北天天数链技术有限公司',
     'Bản quyền © 2025 Công ty TNHH Công nghệ Chuỗi số Thiên Thiên Hồ Bắc', 1),
    ('components/sidebar/index.vue',
     '《phần mềm mã nguồn mở 天天（bản cộng đồng）thỏa thuận cấp phép》',
     'Thỏa thuận Giấy phép Phần mềm Mã nguồn mở Thiên Thiên (Bản Cộng đồng)', 1),
    ('components/sidebar/index.vue', '©2025 天天数链', '©2025 Thiên Thiên Số Liên', 1),

    # ---------- myTask ----------
    ('views/myTask/myTask/index.vue', '立即执行', 'Thực hiện ngay', 1),

    # ---------- admin/user ----------
    ('views/admin/user/userForm.vue', '请选择类别', 'Vui lòng chọn loại', 1),
    ('views/admin/user/userForm.vue',
     '{ min: 11, max: 11, message: "Vui lòng nhập11位手机号码", trigger: "blur" },',
     '{ min: 10, max: 10, message: "Vui lòng nhập số điện thoại 10 chữ số", trigger: "blur" },', 1),
    ('views/admin/user/userForm.vue', '请输入正确的手机号码', 'Vui lòng nhập đúng số điện thoại', 1),
    ('views/admin/user/userForm.vue', '此号码已注册', 'Số điện thoại này đã được đăng ký', 1),

    # ---------- admin/common/personalInfo ----------
    ('views/admin/common/personalInfo.vue', '请再次输入密码', 'Vui lòng nhập lại mật khẩu', 1),
    ('views/admin/common/personalInfo.vue', '请输入用户名称', 'Vui lòng nhập tên người dùng', 1),
    ('views/admin/common/personalInfo.vue', '请输入手机号码', 'Vui lòng nhập số điện thoại', 1),
    ('views/admin/common/personalInfo.vue',
     '邮箱格式如:xxx@163.com', 'Email không đúng định dạng, ví dụ: ten@domain.com', 1),

    # ---------- metadata module names ----------
    ('views/admin/resource/metadata.js', "name: '资源'", "name: 'Tài nguyên'", 1),
    ('views/cure/inspectionCheck/metadata.js', "name: '检验检查'", "name: 'Xét nghiệm - Kiểm tra'", 1),
    ('views/member/memberItem/metadata.js', "name: '会员卡项目'", "name: 'Hạng mục thẻ hội viên'", 1),
    ('views/member/memberManagement/metadata.js', "name: '会员卡管理'", "name: 'Quản lý thẻ hội viên'", 1),
    ('views/schedule/scheduleJob/metadata.js', "name: '定时任务'", "name: 'Tác vụ định kỳ'", 1),
    ('views/sys/action/metadata.js', "name: '操作日志'", "name: 'Nhật ký thao tác'", 1),
    ('views/toll/tollDetail/metadata.js', "name: '收费明细表'", "name: 'Bảng kê chi tiết thu phí'", 1),

    # ---------- chargecombinereport (dữ liệu demo) ----------
    ('views/chargecombinereport/index.vue', 'name: "张三"', 'name: "Nguyễn Văn An"', 2),
    ('views/chargecombinereport/index.vue', 'name: "李四"', 'name: "Trần Thị Bình"', 2),
    ('views/chargecombinereport/index.vue', 'name: "内科"', 'name: "Nội khoa"', 1),
    ('views/chargecombinereport/index.vue', 'name: "外科"', 'name: "Ngoại khoa"', 1),

    # ---------- clinic version ----------
    ('views/clinic/clinicVersion/clinicVersionForm.vue', '请选择版本价格', 'Vui lòng chọn giá phiên bản', 1),
    ('views/clinic/clinicVersion/clinicVersionForm.vue', '请输入版本编号', 'Vui lòng nhập mã phiên bản', 1),

    # ---------- Settings ----------
    ('views/components/Settings/index.vue', "'（系统默认）'", "' (mặc định hệ thống)'", 1),
    ('views/components/Settings/index.vue', "message: '已重置'", "message: 'Đã đặt lại'", 1),
    ('views/components/Settings/index.vue', "text: '处理中...'", "text: 'Đang xử lý...'", 1),

    # ---------- queryForm ----------
    ('views/components/queryForm.vue', '请输入查询标题', 'Vui lòng nhập tiêu đề tra cứu', 1),

    # ---------- supplier-outbound-method-select ----------
    ('views/components/supplier-outbound-method-select/index.vue',
     "typeList: [{name:'退还供方',id:'1'},{name:'药品报损',id:'2'},{name:'调拔出库',id:'3'},{name:'Khác',id:'4'}],",
     "typeList: [{name:'Trả lại nhà cung cấp',id:'1'},{name:'Báo hỏng thuốc',id:'2'},{name:'Xuất điều chuyển',id:'3'},{name:'Khác',id:'4'}],", 1),

    # ---------- cure: gender + payment display ----------
    ('views/cure/cureManagement/index.vue', 'gender.name = "男";', 'gender.name = "Nam";', 1),
    ('views/cure/cureManagement/index.vue', 'gender.name = "女";', 'gender.name = "Nữ";', 1),
    ('views/cure/cureManagement/index.vue', 'paymentType.name = "支付宝";', 'paymentType.name = "Alipay";', 3),
    ('views/cure/cureManagement/index.vue', 'paymentType.name = "银行卡";', 'paymentType.name = "Thẻ ngân hàng";', 3),
    ('views/cure/infusion/index.vue', 'gender.name = "男";', 'gender.name = "Nam";', 1),
    ('views/cure/infusion/index.vue', 'gender.name = "女";', 'gender.name = "Nữ";', 1),
    ('views/cure/infusion/index.vue', 'paymentType.name = "支付宝";', 'paymentType.name = "Alipay";', 3),
    ('views/cure/infusion/index.vue', 'paymentType.name = "银行卡";', 'paymentType.name = "Thẻ ngân hàng";', 3),
    ('views/toll/tollInfo/index.vue', 'gender.name = "男";', 'gender.name = "Nam";', 1),
    ('views/toll/tollInfo/index.vue', 'gender.name = "女";', 'gender.name = "Nữ";', 1),
    ('views/toll/tollInfo/index.vue', 'paymentType.name = "支付宝";', 'paymentType.name = "Alipay";', 3),
    ('views/toll/tollInfo/index.vue', 'paymentType.name = "银行卡";', 'paymentType.name = "Thẻ ngân hàng";', 3),

    # ---------- hậu tố "折" (chiết khấu) -> "%" ----------
    ('views/toll/tollInfo/index.vue', '<template slot="append">折</template>',
     '<template slot="append">%</template>', 2),
    ('views/toll/retailCharge/index.vue', '<template slot="append">折</template>',
     '<template slot="append">%</template>', 1),

    # ---------- cure/inspectionCheck ----------
    ('views/cure/inspectionCheck/index.vue', "name:'高',", "name:'Cao',", 1),
    ('views/cure/inspectionCheck/index.vue', "name:'低',", "name:'Thấp',", 1),
    ('views/cure/inspectionCheck/index.vue', 'this.typeName="填写检验报告"',
     'this.typeName="Nhập kết quả xét nghiệm"', 1),
    ('views/cure/inspectionCheck/index.vue', 'this.typeName="填写检查报告"',
     'this.typeName="Nhập kết quả kiểm tra"', 1),
    ('views/cure/inspectionCheck/index.vue', '?"男":"女"', '?"Nam":"Nữ"', 2),
    ('views/cure/inspectionCheck/index.vue', '>填写报告</el-button>', '>Nhập kết quả</el-button>', 1),

    # ---------- cure/stmanagement (kết quả xét nghiệm) ----------
    ('views/cure/stmanagement/index.vue', '<el-option label="阴性" value="0"></el-option>',
     '<el-option label="Âm tính" value="0"></el-option>', 1),
    ('views/cure/stmanagement/index.vue', '<el-option label="阳性" value="1"></el-option>',
     '<el-option label="Dương tính" value="1"></el-option>', 1),

    # ---------- gen ----------
    ('views/gen/genScheme/genSchemeForm.vue', '请选择编辑列数', 'Vui lòng chọn số cột hiển thị', 1),
    ('views/gen/genTable/genTableForm.vue', "placeholder='请输入描述'", "placeholder='Vui lòng nhập mô tả'", 1),
    ('views/gen/genTable/genTableForm.vue', "placeholder='请选择约束字段'", "placeholder='Vui lòng chọn trường ràng buộc'", 1),
    ('views/gen/genTable/genTableForm.vue', "placeholder='请选择自定义类型'", "placeholder='Vui lòng chọn kiểu tuỳ chỉnh'", 1),
    ('views/gen/genTable/genTableForm.vue', "placeholder='请输入默认值'", "placeholder='Vui lòng nhập giá trị mặc định'", 1),
    ('views/gen/genTable/genTableForm.vue', "placeholder='请选择查询方式'", "placeholder='Vui lòng chọn cách truy vấn'", 1),
    ('views/gen/genTable/genTableForm.vue', "placeholder='请输入查询条件'", "placeholder='Vui lòng nhập điều kiện truy vấn'", 1),
    ('views/gen/genTable/genTableForm.vue', "placeholder='请选择控件'", "placeholder='Vui lòng chọn loại điều khiển'", 1),
    ('views/gen/genTable/genTableForm.vue', "placeholder='请选择联动变量'", "placeholder='Vui lòng chọn biến liên động'", 1),
    ('views/gen/genTable/genTableForm.vue', "placeholder='请输入联动值'", "placeholder='Vui lòng nhập giá trị liên động'", 1),
    ('views/gen/genTable/genTableForm.vue', "placeholder='请输入联动参数'", "placeholder='Vui lòng nhập tham số liên động'", 1),
    ('views/gen/genTable/genTableForm.vue', "placeholder='请输入排序'", "placeholder='Vui lòng nhập thứ tự'", 1),
    ('views/gen/genTable/genTableForm.vue', '请输入物理类型', 'Vui lòng nhập kiểu dữ liệu vật lý', 1),
    ('views/gen/genTable/genTableForm.vue', "name: '单行文本框'", "name: 'Ô nhập một dòng'", 1),
    ('views/gen/genTable/genTableForm.vue', "col.comments + ' 升序'", "col.comments + ' tăng dần'", 1),
    ('views/gen/genTable/genTableForm.vue', "col.comments + ' 降序'", "col.comments + ' giảm dần'", 1),

    # ---------- homepage/todayWork (dữ liệu demo) ----------
    ('views/homepage/components/todayWork.vue',
     '陆家嘴2号楼221Cảnh báo hợp đồng sắp hết hạn',
     'Phòng 221 toà nhà 2 - Cảnh báo hợp đồng sắp hết hạn', 6),
    ('views/homepage/components/todayWork.vue', "name: '待办事项'", "name: 'Việc cần làm'", 1),

    # ---------- member ----------
    ('views/member/memberSet/memberSetForm.vue', '请输入会员卡张数', 'Vui lòng nhập số lượng thẻ', 1),
    ('views/member/memberSet/memberSetForm.vue', "name:'体验卡'", "name:'Thẻ trải nghiệm'", 1),

    # ---------- org ----------
    ('views/org/clinic/clinicForm.vue', '请输入医疗机构代码', 'Vui lòng nhập mã cơ sở y tế', 1),

    # ---------- outpatient/medicalRecord ----------
    ('views/outpatient/medicalRecord/index.vue', '<el-radio label="初诊">初诊</el-radio>',
     '<el-radio label="初诊">Khám lần đầu</el-radio>', 1),

    # ---------- outpatient/medicalOutpatientRecord ----------
    ('views/outpatient/medicalOutpatientRecord/index.vue', 'name: "初诊",', 'name: "Khám lần đầu",', 2),

    # ---------- outpatient/recipeTemplate (dữ liệu demo thuốc) ----------
    ('views/outpatient/recipeTemplate/recipetemplateForm.vue', 'brandName: "阿司匹林",', 'brandName: "Aspirin",', 1),
    ('views/outpatient/recipeTemplate/recipetemplateForm.vue', "name: '安徽药材'", "name: 'Dược liệu An Huy'", 1),
    ('views/outpatient/recipeTemplate/recipetemplateForm.vue', 'goodsName: "阿司匹林胶囊",', 'goodsName: "Aspirin 81mg",', 1),
    ('views/outpatient/recipeTemplate/recipetemplateForm.vue', "name: '进口'", "name: 'Nhập khẩu'", 1),
    ('views/outpatient/recipeTemplate/recipetemplateForm.vue', "name: '盒'", "name: 'Hộp'", 1),
    ('views/outpatient/recipeTemplate/recipetemplateForm.vue', "name: '粒'", "name: 'Viên'", 1),
    ('views/outpatient/recipeTemplate/recipetemplateForm.vue', 'source: "德国",', 'source: "Đức",', 1),

    # ---------- outpatient/review ----------
    ('views/outpatient/recipelInfoReviewPre/components/reviewPre.vue', 'return "无效";', 'return "Không hợp lệ";', 1),
    ('views/outpatient/recipelInfoReviewStatement/index.vue', 'name: "按诊所汇总"', 'name: "Tổng hợp theo phòng khám"', 1),
    ('views/outpatient/recipelInfoReviewStatement/index.vue', 'name: "按科室汇总"', 'name: "Tổng hợp theo khoa"', 1),

    # ---------- outpatient/registration ----------
    ('views/outpatient/registration/registrationForm.vue', 'let sexName = "男";', 'let sexName = "Nam";', 1),
    ('views/outpatient/registration/registrationForm.vue', 'sexName = "女";', 'sexName = "Nữ";', 1),
    ('views/outpatient/registration/registrationForm.vue', 'treatType.name = "初诊"', 'treatType.name = "Khám lần đầu"', 1),

    # ---------- outpatient/remoteDiagnosisTreatment ----------
    ('views/outpatient/remoteDiagnosisTreatment/patientForm.vue', 'label="费用金额"', 'label="Số tiền phí"', 1),
    ('views/outpatient/remoteDiagnosisTreatment/patientForm.vue', '=== "男" ? "1" : "2"', '=== "Nam" ? "1" : "2"', 1),
    ('views/outpatient/remoteDiagnosisTreatment/index.vue', '发起会议', 'Bắt đầu cuộc họp', 1),

    # ---------- outpatient/scheduling (dữ liệu demo) ----------
    ('views/outpatient/scheduling/index.vue', 'name: "张三",', 'name: "Nguyễn Văn An",', 4),
    ('views/outpatient/scheduling/index.vue', 'address: "上海市普陀区金沙江路 1518 弄",',
     'address: "Số 1518 đường Kim Sa Giang, Q. Phổ Đà, TP. Thượng Hải",', 1),
    ('views/outpatient/scheduling/index.vue', 'address: "上海市普陀区金沙江路 1517 弄",',
     'address: "Số 1517 đường Kim Sa Giang, Q. Phổ Đà, TP. Thượng Hải",', 1),
    ('views/outpatient/scheduling/index.vue', 'address: "上海市普陀区金沙江路 1519 弄",',
     'address: "Số 1519 đường Kim Sa Giang, Q. Phổ Đà, TP. Thượng Hải",', 1),
    ('views/outpatient/scheduling/index.vue', 'address: "上海市普陀区金沙江路 1516 弄",',
     'address: "Số 1516 đường Kim Sa Giang, Q. Phổ Đà, TP. Thượng Hải",', 1),
    ('views/outpatient/scheduling/schedulingForm.vue', 'treatType.name = "初诊"', 'treatType.name = "Khám lần đầu"', 1),
    ('views/outpatient/visitprogress/index.vue', 'item.jzlx="初诊"', 'item.jzlx="Khám lần đầu"', 1),

    # ---------- stock ----------
    ('views/stock/drugNew/metadata.js', '"label": "启用标志"', '"label": "Trạng thái hoạt động"', 1),
    ('views/stock/drugNew/metadata.js', '"label": "商品别名"', '"label": "Tên khác"', 1),
    ('views/stock/drugNew/metadata.js', '"label": "生产单位"', '"label": "Đơn vị sản xuất"', 1),
    ('views/stock/drugNew/metadata.js', '"label": "价格"', '"label": "Giá"', 1),
    ('views/stock/stuffNew/synchronous.vue', '请选择要同步的数据', 'Vui lòng chọn dữ liệu cần đồng bộ', 1),
    ('views/stock/supplierOutbound/components/addstorageTo.vue', "name: '销售出库'", "name: 'Xuất bán'", 1),
    ('views/stock/supplierOutbound/components/addstorageTo.vue',
     '"可用库存不足数量"', '"Số lượng tồn khả dụng không đủ"', 1),
    ('views/stock/supplierOutbound/components/addstorageTo.vue',
     '"请选择出库人员"', '"Vui lòng chọn người xuất kho"', 1),
    ('views/stock/supplierOutbound/index.vue', "content='撤消'", "content='Hoàn tác'", 1),
    ('views/stock/supplierOutbound/index.vue', "message: '撤消成功'", "message: 'Hoàn tác thành công'", 1),
    ('views/stock/supplierOutbound/index.vue', "'撤消失败'", "'Hoàn tác thất bại'", 1),
    ('views/stock/supplierOutbound/index.vue', 'this.dialogTitle = "修改出库单";',
     'this.dialogTitle = "Sửa phiếu xuất kho";', 1),
    ('views/stock/supplierOutbound/index.vue', 'this.dialogTitle = "新增药品出库单";',
     'this.dialogTitle = "Thêm phiếu xuất thuốc";', 1),
    ('views/stock/supplierOutbound/index.vue', 'this.dialogTitle = "新增材料出库单";',
     'this.dialogTitle = "Thêm phiếu xuất vật tư";', 1),
    ('views/stock/supplierOutbound/index.vue', 'createDate: "出库时间",', 'createDate: "Thời gian xuất",', 1),

    # ---------- sys ----------
    ('views/sys/action/index.vue', "content='隐藏'", "content='Ẩn'", 1),
    ('views/sys/dictType/dictTypeForm.vue', '加载租户列表失败', 'Tải danh sách đơn vị thất bại', 1),
    ('views/sys/message/index.vue', '>标为已读</el-button>', '>Đánh dấu đã đọc</el-button>', 1),
    ('views/sys/message/index.vue',
     '通知公告指的是系统或官方发出的消息，一般是不可回复的',
     'Thông báo là tin do hệ thống hoặc đơn vị phát hành, thường không thể trả lời', 1),
    ('views/sys/message/index.vue',
     '消息涵盖面比较广，会包含私信、评论、关注、点赞、收藏、系统通知等通知',
     'Tin nhắn bao gồm tin riêng, bình luận, theo dõi, lượt thích, mục đã lưu và thông báo hệ thống', 1),
    ('views/sys/message/index.vue', '今晚12点整发大红包，先到先得',
     'Ưu đãi gửi lúc 0 giờ đêm nay, số lượng có hạn', 1),
    ('views/sys/propertySet/components/propertyCustom.vue', "'默认分组'", "'Nhóm mặc định'", 1),

    # ---------- taskmanagement ----------
    ('views/taskmanagement/article/articleForm.vue', '请输入正文"', 'Vui lòng nhập nội dung"', 1),
    ('views/taskmanagement/article/articleForm.vue', '请输入摘要', 'Vui lòng nhập tóm tắt', 1),
    ('views/taskmanagement/article/articleForm.vue', '请输入正文内容', 'Vui lòng nhập nội dung chính', 1),
    ('views/taskmanagement/articlereview/index.vue', '>审 核</el-button>', '>Duyệt</el-button>', 1),
    ('views/taskmanagement/articlereview/index.vue', '>查 看</el-button>', '>Xem</el-button>', 1),
    ('views/taskmanagement/releaseatask/addtaskForm.vue',
     '只能上传jpg/pngTệp,且不超过500kb',
     'Chỉ được tải lên tệp JPG/PNG, dung lượng không quá 500KB', 1),
    ('views/taskmanagement/releaseatask/addtaskForm.vue', 'label="执行人二"', 'label="Người thực hiện 2"', 1),
    ('views/taskmanagement/releaseatask/addtaskForm.vue', '请选择任务类型', 'Vui lòng chọn loại công việc', 1),
    ('views/taskmanagement/releaseatask/addtaskForm.vue', '请填写任务名称', 'Vui lòng nhập tên công việc', 1),
    ('views/taskmanagement/releaseatask/addtaskForm.vue', '请选择日期时间', 'Vui lòng chọn ngày giờ', 1),
    ('views/taskmanagement/releaseatask/index.vue', 'label="市区"', 'label="Nội thành"', 1),
    ('views/taskmanagement/releaseatask/taskdetailsForm.vue', '请选择审核结果', 'Vui lòng chọn kết quả duyệt', 1),

    # ---------- toll ----------
    ('views/toll/outpatientLog/index.vue', "'门诊日志.xlsx'", "'Nhat_ky_kham_benh.xlsx'", 1),
    ('views/toll/tollInfo/tollInfoForm.vue', '请选择收费单号', 'Vui lòng chọn số phiếu thu', 1),
    ('views/treatment/costItemNew/syncCostItemForm.vue', '请选择要同步的项目',
     'Vui lòng chọn mục cần đồng bộ', 1),

    # ---------- Crontab ----------
    ('components/Crontab/index.vue', '("准备还原")', '("Sẵn sàng khôi phục")', 1),
    ('components/Crontab/result.vue', "['没有达到条件的结果！']",
     "['Không có kết quả nào thoả mãn điều kiện!']", 1),
    ('components/Crontab/result.vue',
     "'最近100年内只有上面' + resultArr.length + '条结果！'",
     "'Trong 100 năm gần nhất chỉ có ' + resultArr.length + ' kết quả như trên!'", 1),

    # ---------- utils: log ----------
    ('components/DictSelect/index.vue', '加载字典项失败', 'Tải mục từ điển thất bại', 1),
    ('utils/dictCache.js', '加载字典失败', 'Tải từ điển thất bại', 1),
    ('utils/userCache.js', '解析用户信息失败', 'Phân tích thông tin người dùng thất bại', 1),
    ('utils/userCache.js', '解析公司信息失败', 'Phân tích thông tin đơn vị thất bại', 1),

    # ---------- stock addstorage: thông báo lỗi nhập Excel ----------
    ('views/stock/auditWarehousing/components/addstorage.vue',
     '"Dòng thứ"+(i+1)+"dòng["+outdata[i].药品材料名称+"]Thông tin bất thường，Vui lòng kiểm tra rồi nhập lại\\n"',
     '"Dòng "+(i+1)+" ["+outdata[i].药品材料名称+"] có thông tin bất thường, vui lòng kiểm tra rồi nhập lại\\n"', 1),
    ('views/stock/distributionDrugs/components/addstorage.vue',
     '"Dòng thứ"+(i+1)+"dòng["+outdata[i].药品材料名称+"]Thông tin bất thường，Vui lòng kiểm tra rồi nhập lại\\n"',
     '"Dòng "+(i+1)+" ["+outdata[i].药品材料名称+"] có thông tin bất thường, vui lòng kiểm tra rồi nhập lại\\n"', 1),
    ('views/stock/supplierStorage/components/addstorage.vue',
     '"Dòng thứ"+(i+1)+"dòng["+outdata[i].药品材料名称+"]Thông tin bất thường，Vui lòng kiểm tra rồi nhập lại\\n"',
     '"Dòng "+(i+1)+" ["+outdata[i].药品材料名称+"] có thông tin bất thường, vui lòng kiểm tra rồi nhập lại\\n"', 1),
    ('views/stock/distributionDrugs/components/addstorage.vue',
     "'格式化入库单明细出错'", "'Lỗi định dạng chi tiết phiếu nhập kho'", 1),
    ('views/stock/supplierOutbound/components/addstorageTo.vue',
     "'格式化出库单明细出错'", "'Lỗi định dạng chi tiết phiếu xuất kho'", 1),
]


def main():
    errors = []
    changed = 0
    for rel, old, new, expect in R:
        path = os.path.join(ROOT, rel)
        if not os.path.exists(path):
            errors.append('KHONG TIM THAY FILE: %s' % rel)
            continue
        with io.open(path, encoding='utf-8') as f:
            src = f.read()
        n = src.count(old)
        if n != expect:
            errors.append('LECH SO LAN  %s\n    old=%r\n    mong doi=%d, thuc te=%d'
                          % (rel, old[:60], expect, n))
            continue
        with io.open(path, 'w', encoding='utf-8') as f:
            f.write(src.replace(old, new))
        changed += 1

    print('Da ap dung: %d/%d thay the' % (changed, len(R)))
    if errors:
        print('\n=== LOI (%d) ===' % len(errors))
        for e in errors:
            print(' - ' + e)
        return 1
    print('Tat ca khop, khong co loi.')
    return 0


if __name__ == '__main__':
    sys.exit(main())
