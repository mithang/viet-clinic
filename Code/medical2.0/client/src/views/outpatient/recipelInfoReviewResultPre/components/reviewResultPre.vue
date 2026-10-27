<template>
  <div class="review-container">
    <!-- Cửa sổ chỉnh sửa  -->
    <review-form-pre ref='reviewPreForm' :permission='permission' @save-finished='getReviewList()'></review-form-pre>
    <!--  Thanh tìm kiếm  Bắt đầu -->
    <div class='query-form-container'>
      <el-form :model='queryModel' @submit.native.prevent label-position="left" label-width='70px' ref='queryForm' :inline-message='true'>
        <el-row v-if='!moreCodition' class='search-row'>
          <el-col :span="12">
            <el-form-item label='Thời gian lập phiếu' prop='createDate'>
              <data-range-picker v-model='queryModel.createDate' ></data-range-picker>
            </el-form-item>
          </el-col>
          <!--<el-col :span="6">
            <el-form-item label='Phòng khám kê đơn' prop='company'>
              <el-select v-model='queryModel.company' value-key='value' filterable clearable placeholder='Vui lòng chọn thao tác'>
                <el-option v-for='company in company_List' :key='company.value' :label='company.name' :value='company'></el-option>
              </el-select>
            </el-form-item>
          </el-col>-->
          <el-col :span="6">
            <el-form-item label='Từ khóa' prop='patientName'>
              <el-input
                v-model="queryModel.patientName"
                placeholder="Họ tên/Số khám ngoại trú"
                clearable
              />
            </el-form-item>
          </el-col>
        </el-row >
        <el-row class='search-row'>
          <!--<el-col :span="6">
            <el-form-item label='Trạng thái đơn thuốc' prop='recipelStatus'>
              <el-select v-model='queryModel.recipelStatus' value-key='value' filterable clearable placeholder='Vui lòng chọn thao tác'>
                  <el-option v-for='recipelStatus in recipelStatus_List' :key='recipelStatus.value' :label='recipelStatus.name' :value='recipelStatus'></el-option>
              </el-select>
            </el-form-item>
          </el-col>-->

          <!--<el-col :span="6">
            <el-form-item label='Trạng thái thẩm tra' prop='reviewStatus'>
              <el-select v-model='queryModel.reviewStatus' value-key='value' filterable clearable placeholder='Vui lòng chọn thao tác'>
                <el-option v-for='reviewStatus in reviewStatus_List' :key='reviewStatus.value' :label='reviewStatus.name' :value='reviewStatus'></el-option>
              </el-select>
            </el-form-item>
          </el-col>-->

          <el-col :span="6">
            <el-button-group>
              <el-tooltip  effect="light" content="Tìm kiếm" placement="top-start">
                <el-button type="primary" icon="el-icon-search" @click='onSearch()' :plain='true'></el-button>
              </el-tooltip>
              <el-tooltip  effect="light" content="Đặt lại" placement="top-start">
                <el-button type="primary" icon="el-icon-refresh-left" @click='$refs.queryForm.resetFields()' :plain='true'></el-button>
              </el-tooltip>
            </el-button-group>
          </el-col>
        </el-row>
      </el-form>
    </div>
    <!-- Thanh công cụ Bắt đầu -->
    <!--      <div class="page-container-header-end">
            <div>
              <el-button v-show='permission.add' type='primary' icon='el-icon-plus'  @click='onCreateTheme()'>Thêm</el-button>
            </div>
          </div>-->
    <!-- Thanh công cụ Kết thúc -->

    <!--  Thanh tìm kiếm  Kết thúc -->
    <!-- Thanh bảng  Bắt đầu -->
    <el-row>
      <el-col :span='24'>
        <div @mouseleave='moveTableOutside'>
          <el-table ref="reviewTable"
                    class='drag_table' :data='reviewList' border @sort-change='onSortChange'
                    @header-dragend='onChangeWidth' :cell-class-name='cellClassName'
                    :header-cell-class-name='headerCellClassName' highlight-current-row
                    @current-change="handleCurrentChange"
          >
            <el-table-column
              label="Số thứ tự"
              type="index"
              :index="indexMethod"
              align="center">
            </el-table-column>
            <!--<el-table-column
              label='Phòng khám kê đơn'
              prop='recipelInfo.company.name'
              align="center">
            </el-table-column>-->
            <el-table-column
              label='Khoa lập phiếu'
              prop='recipelInfo.registration.clinicOffice.name'
              align="center">
            </el-table-column>
            <el-table-column
              label='Bác sĩ kê đơn'
              prop='recipelInfo.registration.doctor.name'
              align="center">
            </el-table-column>
            <el-table-column
              label='Ngày kê đơn'
              prop='recipelInfo.createDate'
              width="180"
              align="center">
            </el-table-column>
            <el-table-column
              label='Số đơn thuốc'
              prop='recipelInfo.code'
              width="180"
              align="center">
            </el-table-column>

            <el-table-column
              label='Tên bệnh nhân'
              prop='recipelInfo.registration.patientId.name'
              align="center">
            </el-table-column>

<!--            <el-table-column-->
<!--              label='Giới tính'-->
<!--              prop='recipelInfo.registration.patientId.gender.name'-->
<!--              align="center">-->
<!--            </el-table-column>-->
<!--            <el-table-column-->
<!--              label='Tuổi'-->
<!--              prop='recipelInfo.registration.patientId.age'-->
<!--              align="center">-->
<!--            </el-table-column>-->

<!--            <el-table-column-->
<!--              label='Phân loại đơn thuốc'-->
<!--              prop='recipelInfo.recipelType.name'-->
<!--              align="center">-->
<!--            </el-table-column>-->
<!--            <el-table-column-->
<!--              label='Thông tin chẩn đoán'-->
<!--              prop='recipelInfo.recipelType.name'-->
<!--              width="220"-->
<!--              align="center">-->
<!--              <template slot-scope="scope">-->
<!--                Chẩn đoán Đông y：{{scope.row.medicalRecord.chinaDiagnose}};-->
<!--                Chẩn đoán Tây y：{{scope.row.medicalRecord.westernDiagnose}};-->
<!--              </template>-->
<!--            </el-table-column>-->
            <el-table-column
              label='Trạng thái xét duyệt'
              prop='recipelInfo.recipelType.name'
              align="center">
              <template slot-scope="scope">
                <el-tag :type="getTagType(scope.row)" disable-transitions>
                  {{ recipellnfoStatus(scope.row,'1') }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column
              label='有效状态'
              prop='recipelInfo.recipelType.name'
              align="center">
              <template slot-scope="scope">
                <el-tag :type="getTagType(scope.row)" disable-transitions>
                  {{ recipellnfoStatus(scope.row,'2') }}
                </el-tag>
              </template>
            </el-table-column>

            <el-table-column
              label='Kết quả thẩm tra'
              prop='reviewResult'
              align="center">
              <template slot-scope="scope">
                {{ recipellnfoResult(scope.row) }}
              </template>
            </el-table-column>
            <el-table-column
              label='药师审查内容'
              prop='reviewContent'
              align="center">
            </el-table-column>
            <el-table-column
              label='Người thẩm tra'
              prop='reviewerName'
              align="center">
            </el-table-column>
            <el-table-column
              label='Thời gian thẩm tra'
              prop='createDate'
              width="180"
              align="center">
            </el-table-column>
            <!--Nút thao tác theo dòng-->
            <el-table-column label='Thao tác' header-align='center' :width='60 + "px"' :key="'operate'">
              <!--<template slot='header' slot-scope="scope">
                <span>Thao tác</span>
              </template>-->
              <template slot-scope='scope'>
                <!--<OperationIcon v-show='permission.edit' type='primary' content='Sửa' placement='top-start' icon-name='el-icon-edit'
                               @click='onEditReview(scope.$index, scope.row)'></OperationIcon>-->
                <OperationIcon v-show='permission.view' type='info' content='Xem' placement='top-start' icon-name='el-icon-view'
                               @click='onViewReviewResult(scope.$index, scope.row)'></OperationIcon>
              </template>
            </el-table-column>
          </el-table>
          <div  style="margin-top: 20px;">
            <el-button @click="onQueryMedicine(selectedRow)" type="primary">Truy vấn lấy thuốc</el-button>
            <el-button @click="onAuditQuery(selectedRow)" type="primary">Truy vấn xét duyệt</el-button>
<!--            <el-button @click="onPrescriptionQuery(selectedRow)" type="primary">Truy vấn đơn thuốc</el-button>-->
            <el-button  @click="onPrescriptionCancel(selectedRow)"  type="danger">Thu hồi đơn thuốc</el-button>
          </div>

          <!-- 取药查询模态框 -->
          <medicine-dialog
            :dialogVisible="medicineDialogVisible"
            :formData="medicineFormData"
            @update:dialogVisible="medicineDialogVisible = $event"
          ></medicine-dialog>
          <!-- 审核查询模态框 -->
          <audit-dialog
            :dialogVisible="auditDialogVisible"
            :formData="auditFormData"
            @update:dialogVisible="auditDialogVisible = $event"
          ></audit-dialog>
          <!-- 处方查询模态框 -->
          <prescription-dialog
            :dialogVisible="prescriptionDialogVisible"
            :formData="prescriptionFormData"
            @update:dialogVisible="prescriptionDialogVisible = $event"
          ></prescription-dialog>
        </div>
      </el-col>
    </el-row>
<!--    Thu hồi-->
    <el-dialog title="Lý do thu hồi" :visible.sync="dialogVisible" width="400px">
      <el-form :model="cancelForm" ref="cancelForm" label-width="120px">
        <el-form-item prop="reason" label="Lý do thu hồi" :rules="[{ required: true, message: 'Vui lòng nhập lý do thu hồi', trigger: 'submit' }]">
          <el-input v-model="cancelForm.reason" type="textarea" placeholder="Vui lòng nhập lý do thu hồi" maxlength="300"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
      <el-button @click="dialogVisible = false">Hủy</el-button>
      <el-button type="primary" @click="submitCancel">确认撤销</el-button>
    </span>
    </el-dialog>
    <!-- Thanh bảng  Kết thúc -->
    <!-- Thanh phân trang     Bắt đầu -->
    <el-row>
      <el-col :span='24'>
        <el-pagination
          background
          @size-change='onSizeChange'
          @current-change='onCurrentChange'
          :current-page.sync='currentPage'
          :page-sizes='[10, 20, 50, 100, reviewTotal]'
          :page-size='10'
          layout='total, sizes, prev, pager, next, jumper'
          :total='reviewTotal'>
        </el-pagination>
      </el-col>
    </el-row>
    <!-- Thanh phân trang     Kết thúc -->
  </div>
</template>

<script>

import { listRecipelInfoReviewPage, getRecipelInfoReviewById,
  getRecipelInfoReviewByRecipelInfoId,
  getReviewFormByRecipelInfoId,
  cancelElectronicPrescription ,
  getMdMedicineInfo,
   getMdExamineInfo }
  from '@/api/outpatient/recipelInfoReview'
import { listResourcePermission } from '@/api/admin/common/permission'
import { listDictItemAll } from '@/api/sys/dictItem'
import { getDictItemsByCode, DICT_CODE } from '@/utils/dictCache'
import { getCompanyById } from '@/api/org/company'
import { listUserAll } from "@/api/admin/user";
import { listCompanyAll } from "@/api/org/company";
import MainUI from '@/views/components/mainUI'
import OperationIcon from '@/components/OperationIcon'
import DataRangePicker from '@/components/DataRangePicker'
import MedicineDialog from "./MedicineDialog.vue";
import AuditDialog from "./AuditDialog.vue";
import PrescriptionDialog from "./PrescriptionDialog.vue";
import ReviewFormPre from "../../recipelInfoReviewPre/components/reviewFormPre.vue";
import { getCurrentUser } from "@/utils/userCache";
export default {
  name: "review-result-pre",
  extends: MainUI,
  components: {
    PrescriptionDialog,
    AuditDialog,
    MedicineDialog,
    ReviewFormPre,
    OperationIcon,
    DataRangePicker,
  },
  computed: {
    Company() {
      let company = getCurrentUser().company;
      return {
        id: company.id,
        label: company.label,
        name: company.name,
      };
    },
    Department() {
      let department = getCurrentUser().department;
      return {
        id: department.id,
        label: department.label,
        name: department.name,
      };
    },
  },
  data() {
    return {
      selectedRow: null,  // 当前选中的行
      medicineDialogVisible: false,
      auditDialogVisible: false,
      prescriptionDialogVisible: false,
      dialogVisible:false,
      medicineFormData: {},
      auditFormData: {},
      prescriptionFormData: {},
      cancelForm: {
        reason: '' // Lý do thu hồi
      },
      permission: {
        view: false,
        add: false,
        edit: false,
        remove: false,
        export: false
      },
      queryTypes: {
        'name': 'like',
      },
      queryModel: {
        // Thời gian lập phiếu
        'createDate': null,
        // Phòng khám kê đơn
        'company': '',
        // Bác sĩ kê đơn
        'doctor': '',
        // Số khám ngoại trú/Tên bệnh nhân
        'patientName': '',
        // Phân loại đơn thuốc
        'recipelType': '',
        // Trạng thái đơn thuốc
        'recipelStatus': '',
        // Trạng thái thẩm tra
        'reviewStatus': '',
      },
      search: {
        params: [],
        offset: 0,
        limit: 10,
        columnName: '',       // Tên trường sắp xếp
        order: ''             // Sắp xếp
      },
      currentPage: 1,
      reviewTotal: 0,
      reviewList: [],

      company_List:[],
      doctor_List:[],
      recipelType_List:[
      ],
      recipelStatus_List:[
        {name: "Tất cả", value: ""},
        {name: "Chờ thu phí", value: "recipelInfo.charge_status&0&recipelInfo.status&1"},
        {name: "Đã thu phí", value: "recipelInfo.charge_status&1&recipelInfo.dispension_status&0"},
        {name: "Đã hoàn phí", value: "recipelInfo.charge_status&-1"},
        // {name: "Chờ phát thuốc", value: "recipelInfo.dispension_status&0&recipelInfo.charge_status&1"},
        {name: "Đã phát thuốc", value: "recipelInfo.dispension_status&1"},
        {name: "Đã trả thuốc, chưa hoàn phí", value: "recipelInfo.dispension_status&-1&recipelInfo.charge_status&1"},
        {name: "Đã hủy bỏ", value: "recipelInfo.status&-1"},
      ],
      reviewStatus_List:[
        {name: "Tất cả", value: ""},
        {name: "Chưa xét duyệt", value: "0"},
        {name: "Đã xét duyệt", value: "1"},
        {name: "Đã thu hồi", value: "2"},
      ],
      loginCompany:{},

    }
  },
  props: {
  },
  methods: {
    handleCurrentChange(val) {
      this.selectedRow = val;
    },
    // Truy vấn lấy thuốc
    onQueryMedicine(row) {
      if (!row) {
        this.$message.warning('Vui lòng chọn trước một bản ghi đơn thuốc');
        return;
      }
      this.medicineDialogVisible = true;
      // 在这里编写取药查询的逻辑
      getMdMedicineInfo(row).then(response => {
       this.medicineFormData = response.data
        }
      );

    },
    // Truy vấn xét duyệt
    onAuditQuery(row) {
      if (!row) {
        this.$message.warning('Vui lòng chọn trước một bản ghi đơn thuốc');
        return;
      }
      this.auditDialogVisible = true;
      // 在这里编写审核查询的逻辑
      getMdExamineInfo(row).then(response => {
        this.auditFormData = response.data
      })
    },

    // Truy vấn đơn thuốc
    onPrescriptionQuery(row) {
      if (!this.selectedRow) {
        this.$message.warning('Vui lòng chọn trước một bản ghi đơn thuốc');
        return;
      }
      this.prescriptionFormData = true;

      // 在这里编写处方查询的逻辑
    },

    // Thu hồi đơn thuốc
    onPrescriptionCancel() {
      if (!this.selectedRow) {
        this.$message.warning('Vui lòng chọn trước một bản ghi đơn thuốc');
        return;
      }else if(this.selectedRow.reviewStatus === 2){
        this.$message.warning('Đã hủy đơn thuốc!');
        return;
      }
      // 重置撤销原因
      this.cancelForm.reason = '';
      // 显示模态框
      this.dialogVisible = true;

    },
    submitCancel() {
      // 校验撤销原因
      this.$refs.cancelForm.validate(valid => {
        if (valid) {
          this.cancelPrescription();
        } else {
          return false;
        }
      });
    },
    cancelPrescription() {
      // 处理撤销
      const recipelInfoReview =  this.selectedRow;  // 假设选中行有 id
      const undoRea = this.cancelForm.reason;
      //cancelElectronicPrescription( recipelInfoReview, undoRea);
      this.dialogVisible = false;
      this.pageInit()
    },
    getReviewList() {
      this.setLoad()
      /* Tham số truy vấn và quyền dữ liệu */
      this.search.params = []
      this.search.params.push({
        columnName: 'create_date',
        queryType: 'between',
        value: this.queryModel.createDate
      })
      this.search.params.push({
          columnName: 'recipelInfo.company_id',
          queryType: '=',
          value: this.queryModel.company.id
        })
      // this.search.params.push({
      //     columnName: 'registration.doctor_id',
      //     queryType: '=',
      //     value: this.queryModel.doctor.id
      //   })
      // this.search.params.push({
      //     columnName: 'recipelInfo.recipel_type',
      //     queryType: '=',
      //     value: this.queryModel.recipelType.value
      //   })
      // /*if (this.queryModel.recipelStatus && this.queryModel.recipelStatus !== "") {
      //   this.search.params.push(...this.createQueryObjects(this.queryModel.recipelStatus.value))
      // }*/
      // /*this.search.params.push({
      //     columnName: 'review_status',
      //     queryType: this.queryModel.reviewStatus.value === '1' ? '=' : 'is null',
      //     value: this.queryModel.reviewStatus.value === '1' ? '1' : null
      //   })*/
      // this.search.params.push({
      //   columnName: 'review_result',
      //   queryType: '=',
      //   value: 0
      // })
      this.search.params.push({
        columnName: 'company.id',
        queryType: '=',
        value: currentUser.company.id
      })
      //Lọc đơn thuốc điện tử
      this.search.params.push({
        columnName: 'recipelInfo.is_pre',
        queryType:  '=',
        value:'1'
      })
      //Lọc đơn thuốc điện tử
      if (this.queryModel.patientName) {
        this.search.params.push({
          logic: "AND",
          queryType: "(",
        });
        this.search.params.push({
          logic: "",
          columnName: "",
          queryType: "like",
          value: this.queryModel.patientName,
        });
        this.search.params.push({
          logic: "OR",
          columnName: "patientId.phone",
          queryType: "like",
          value: this.SearchPatientInfo,
        });
        this.search.params.push({
          logic: "",
          queryType: ")",
        });
      }
      // Quyền dữ liệu
      this.pushDataPermissions(this.search.params, this.$route.meta.routerId, this.tableId)
      listRecipelInfoReviewPage(this.search).then(responseData => {
        if(responseData.code === 100) {
          this.reviewTotal = responseData.data.total
          this.reviewList = responseData.data.rows
        } else {
          this.showMessage(responseData)
        }
        this.resetLoad()
      }).catch(error => {
        this.outputError(error)
      })
    },
    //SẽrecipelStatusGhép điều kiện truy vấn trạng thái đơn thuốc thành cấu trúc dữ liệu backend cần
    createQueryObjects(inputString){
      let parts = inputString.split('&');
      let queryObjects = [];

      for (let i = 0; i < parts.length; i += 2) {
        const columnName = parts[i];
        const value = parts[i + 1];

        const queryObject = {
          columnName: columnName,
          queryType: '=',
          value: value,
        };
        queryObjects.push(queryObject);
      }
      return queryObjects;
    },
    onSearch() {
      this.$refs['queryForm'].validate(valid => {
        if (valid) {
          this.search.offset = 0
          this.currentPage = 1
          this.getReviewList()
        } else {
          return false
        }
      })

    },
    onSizeChange(val) {
      this.currentPage = 1
      this.search.limit = val;
      this.search.offset = (this.currentPage - 1) * val
      this.getReviewList()
    },
    onCurrentChange(val) {
      this.search.offset = (val - 1) * this.search.limit
      this.currentPage = val
      this.getReviewList()
    },
    async pageInit() {
      this.setLoad()
      try {
        await this.initOptions(this.queryModel)
        this.search.params = []

        this.search.params.push({
          columnName: 'company.id',
          queryType: '=',
          value: currentUser.company.id
        })
        this.search.params.push({
          columnName: 'recipelInfo.is_pre',
          queryType:  '=',
          value:'1'
        })
        // Quyền dữ liệu: Thẩm tra đơn thuốc
        this.pushDataPermissions(this.search.params, this.$route.meta.routerId, this.tableId)
        let [listReviewRespData, listPermissionRespData] = await Promise.all([
          listRecipelInfoReviewPage(this.search),
          listResourcePermission(this.$route.meta.routerId)
        ])
        if(listReviewRespData.code === 100 && listPermissionRespData.code === 100) {
          this.reviewTotal = listReviewRespData.data.total
          this.reviewList = listReviewRespData.data.rows
          this.permission.view = listPermissionRespData.data.find(item => {
            return item.permission === 'recipelInfoReviewResultPre:read'
          })
          this.permission.export = listPermissionRespData.data.find(item => {
            return item.permission === 'recipelInfoReviewResult:export'
          })
          this.permission.add = listPermissionRespData.data.find(item => {
            return item.permission === 'recipelInfoReviewResult:create'
          })
          this.permission.edit = listPermissionRespData.data.find(item => {
            return item.permission === 'recipelInfoReviewResult:update'
          })
          this.permission.remove = listPermissionRespData.data.find(item => {
            return item.permission === 'recipelInfoReviewResult:delete'
          })
        } else {
          this.showMessage(listPermissionRespData.code !== 100 ? listPermissionRespData : listReviewRespData)
        }
        this.resetLoad()
      } catch(error) {
        this.outputError(error)
      }
    },
    onViewTheme(index, row) {
      this.setLoad()
      getRecipelInfoReviewById(row.id).then(responseData => {
        if(responseData.code === 100) {
          this.$refs.reviewPreForm.openViewThemeDialog(responseData.data)
        } else {
          this.showMessage(responseData)
        }
        this.resetLoad()
      }).catch(error => {
        this.outputError(error)
      })
    },
    onViewReviewResult(index, row) {
      this.setLoad()
      getReviewFormByRecipelInfoId(row.recipelInfo.id).then(responseData => {
        if(responseData.code === 100) {
          this.$refs.reviewPreForm.openViewReviewDialog(responseData.data)
        }else{
          this.showMessage(responseData)
        }
        this.resetLoad()
      }).catch(error => {
        this.outputError(error)
      })
    },
    async initOptions(This) {
      getDictItemsByCode(DICT_CODE.RECIPEL_TYPE).then((data) => {
        this.recipelType_List = data
        this.recipelType_List.unshift({name: "Tất cả", value: ""})
      })
      //Khởi tạo bác sĩ kê đơn
      let doctor_search = {
        params: [
          {
            columnName: "department_id",
            queryType: "=",
            value: this.Department.id,
          },
          {
            columnName: "is_locked",
            queryType: "=",
            value: 0,
          },
          {
            columnName: "company_id",
            queryType: "=",
            value: this.Company.id,
          },
        ]}
      // Trên bảng tương ứng của trườngfilterĐiều kiện
      doctor_search.params.push.apply(doctor_search.params, [])
      // Quyền dữ liệu: Mục từ điểnsys_dict_item
      this.pushDataPermissions(doctor_search.params, this.$route.meta.routerId, '1014474470772899974')
      this.doctor_List = []
      listUserAll(doctor_search).then(responseData => {
        this.doctor_List = responseData.data
        this.doctor_List.unshift({name: "Tất cả", id: ""})
      })
      //Khởi tạo phòng khám kê đơn
      let company_search = {
        params: [
          {
            columnName: "status",
            queryType: "=",
            value: 1,
          },
          {
            columnName: "is_institution",
            queryType: "=",
            value: 0,
          },
        ]}
      // Trên bảng tương ứng của trườngfilterĐiều kiện
      company_search.params.push.apply(company_search.params, [])
      // Quyền dữ liệu: Mục từ điểnsys_dict_item
      this.pushDataPermissions(company_search.params, this.$route.meta.routerId, '1014474470772899974')
      this.company_List = []
      listCompanyAll(company_search).then(responseData => {
        this.company_List = responseData.data
        this.company_List.unshift({name: "Tất cả", id: ""})
      })
      //Lấy công ty/tổ chức
      let [companyRespData] = await Promise.all([getCompanyById(this.Company.id)])
      this.loginCompany = companyRespData.data
    },
    recipellnfoStatus(row,type){
      if(type === '1'){
        if (row.reviewStatus === 1) {
          return "Đã xét duyệt";
        }else if (row.reviewStatus === 0) {
          return "Chưa xét duyệt";
        }else {
          return "Đã thu hồi";
        }
      }else{
        if (row.reviewStatus === 1) {
          return "Hiệu lực";
        }else if (row.reviewStatus === 0) {
          return "Chưa xét duyệt";
        }else {
          return "Đã thu hồi";
        }
      }
    },
    recipellnfoResult(row){
      if (row.reviewResult === 1) {
        return "Qua";
      }else if (row.reviewResult === 0) {
        return "Không qua";
      }else {
        return "";
      }
    },
    getTagType(row) {
      const status = row.reviewStatus;
      // 根据不同的状态返回不同的标签类型
      if (status === 1) {
        return 'success';  // green color
      } else if (status === 0) {
        return 'danger';  // yellow color
      } else {
        return 'warning ';
      }
    }

  },
  watch: {

  },
  mounted() {
    this.pageInit()
  }
}
</script>
<style>
  .review-container{
    margin-top: 8px;
  }
</style>
