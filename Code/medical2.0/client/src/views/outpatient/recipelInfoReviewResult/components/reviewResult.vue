<template>
  <div class="review-container">
    <!-- Cửa sổ chỉnh sửa  -->
    <review-form ref='reviewForm' :permission='permission' @save-finished='getReviewList'></review-form>
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
            <el-form-item label='Bác sĩ kê đơn' prop='doctor'>
              <el-select v-model='queryModel.doctor' value-key='value' filterable clearable placeholder='Vui lòng chọn thao tác'>
                <el-option v-for='doctor in doctor_List' :key='doctor.value' :label='doctor.name' :value='doctor'></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label='Loại đơn thuốc' prop='recipelType'>
              <el-select v-model='queryModel.recipelType' value-key='value' filterable clearable placeholder='Vui lòng chọn thao tác'>
                <el-option v-for='recipelType in recipelType_List' :key='recipelType.value' :label='recipelType.name' :value='recipelType'></el-option>
              </el-select>
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
          <el-table ref="reviewTable" class='drag_table' :data='reviewList' border @sort-change='onSortChange' @header-dragend='onChangeWidth' :cell-class-name='cellClassName' :header-cell-class-name='headerCellClassName' highlight-current-row>
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

            <el-table-column
              label='Giới tính'
              prop='recipelInfo.registration.patientId.gender.name'
              align="center">
            </el-table-column>
            <el-table-column
              label='Tuổi'
              prop='recipelInfo.registration.patientId.age'
              align="center">
            </el-table-column>

            <el-table-column
              label='Phân loại đơn thuốc'
              prop='recipelInfo.recipelType.name'
              align="center">
            </el-table-column>
            <el-table-column
              label='Thông tin chẩn đoán'
              prop='recipelInfo.recipelType.name'
              width="220"
              align="center">
              <template slot-scope="scope">
                Chẩn đoán Đông y：{{scope.row.medicalRecord.chinaDiagnose}};
                Chẩn đoán Tây y：{{scope.row.medicalRecord.westernDiagnose}};
              </template>
            </el-table-column>
            <el-table-column
              label='Trạng thái đơn thuốc'
              prop='recipelInfo.recipelType.name'
              align="center">
              <template slot-scope="scope">
                {{recipellnfoStatus(scope.row)}}
              </template>
            </el-table-column>
            <el-table-column
              label='Kết quả thẩm tra'
              prop='reviewResult'
              align="center">
              <template slot-scope="scope">
                {{scope.row.reviewResult === 1?"Hợp lý":"Không hợp lý"}}
              </template>
            </el-table-column>
            <el-table-column
              label='Nội dung thẩm tra'
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
        </div>
      </el-col>
    </el-row>
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
import { validatenull } from '@/utils/validate'
import { listRecipelInfoReviewPage, getRecipelInfoReviewById,getRecipelInfoReviewByRecipelInfoId,getReviewFormByRecipelInfoId } from '@/api/outpatient/recipelInfoReview'
import { listResourcePermission } from '@/api/admin/common/permission'
import { listDictItemAll } from '@/api/sys/dictItem'
import { getDictItemsByCode, DICT_CODE } from '@/utils/dictCache'
import { getCompanyById } from '@/api/org/company'
import { listUserAll } from "@/api/admin/user";
import { listCompanyAll } from "@/api/org/company";
import ReviewForm from './../../recipelInfoReview/components/reviewForm'
import MainUI from '@/views/components/mainUI'
import OperationIcon from '@/components/OperationIcon'
import DataRangePicker from '@/components/DataRangePicker'
import { getCurrentUser } from "@/utils/userCache";
export default {
  name: "review-result",
  extends: MainUI,
  components: {
    ReviewForm,
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
        // Loại đơn thuốc
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
      ],
      loginCompany:{},

    }
  },
  props: {
  },
  methods: {
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
      this.search.params.push({
          columnName: 'registration.doctor_id',
          queryType: '=',
          value: this.queryModel.doctor.id
        })
      this.search.params.push({
          columnName: 'recipelInfo.recipel_type',
          queryType: '=',
          value: this.queryModel.recipelType.value
        })
      /*if (this.queryModel.recipelStatus && this.queryModel.recipelStatus !== "") {
        this.search.params.push(...this.createQueryObjects(this.queryModel.recipelStatus.value))
      }*/
      /*this.search.params.push({
          columnName: 'review_status',
          queryType: this.queryModel.reviewStatus.value === '1' ? '=' : 'is null',
          value: this.queryModel.reviewStatus.value === '1' ? '1' : null
        })*/
      this.search.params.push({
        columnName: 'review_result',
        queryType: '=',
        value: 0
      })
      this.search.params.push({
        columnName: 'company.id',
        queryType: '=',
        value: currentUser.company.id
      })
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
          columnName: 'review_result',
          queryType: '=',
          value: 0
        })
        this.search.params.push({
          columnName: 'company.id',
          queryType: '=',
          value: currentUser.company.id
        })
        this.search.params.push({
          columnName: 'recipelInfo.is_pre',
          queryType:  '=',
          value:'0'
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
            return item.permission === 'recipelInfoReviewResult:read'
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
          this.$refs.reviewForm.openViewThemeDialog(responseData.data)
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
          this.$refs.reviewForm.openViewReviewDialog(responseData.data)
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
    recipellnfoStatus(row){
      if (row.recipelInfo.status === -1) {
        return "Hủy đơn thuốc";
      }
      if (row.recipelInfo.chargeStatus === -1) {
        return "Đã hoàn phí";
      }
      if (row.recipelInfo.dispensionStatus === 1) {
        return "Đã phát thuốc";
      }else if (row.recipelInfo.dispensionStatus === -1) {
        if (row.recipelInfo.chargeStatus === 1) {
          return "Đã trả thuốc, chưa hoàn phí";
        }
      }else if (row.recipelInfo.dispensionStatus === 0) {
        if (row.recipelInfo.chargeStatus === 0) {
          return "Chờ thu phí";
        }else if (row.recipelInfo.chargeStatus === 1) {
          return "Đã thu phí";
        }
      }
    },
  },
  watch: {
  },
  mounted() {
    this.pageInit()
  }
}
</script>
<style lang="scss" scoped>
  .drag_table {
    // Cài đặt bảngheaderChiều cao của
    ::v-deep th {
      height: 44px;
    }
    ::v-deep th.gutter:last-of-type {
      height: 0 !important;
    }
    // Cài đặt bảngbodyChiều cao của
    ::v-deep.el-table__body-wrapper {
      //Khắc phục hiển thị dữ liệu vượt quábodychiều cao không cuộn đượcbug
      overflow-y: auto;
      // Phần trừ đi là bảngheaderChiều cao của
      height: calc(100% - 44px) !important;
    }

    .el-table__fixed-right {
      height: 100% !important;
    }
  }
</style>
<style>
  .review-container{
    margin-top: 8px;
  }
</style>
