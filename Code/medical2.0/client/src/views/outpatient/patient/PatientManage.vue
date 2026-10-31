<template>
  <el-row v-loading='loading'>
    <!-- Lịch sử  -->
    <History :bussObject='curentRow' ></History>
    <!-- Cửa sổ chỉnh sửa  -->
    <patient-form ref='patientForm' @save-finished='getPatientList()'></patient-form>
    <el-card class="page-container">
      <!--  Thanh tìm kiếm  Bắt đầu -->
      <div class='query-form-container'>
        <el-row v-if='!moreCodition' class='search-row'>
          <el-form :model='queryModel' @submit.native.prevent label-position="left" label-width='70px' ref='queryForm' :inline-message='true'>
            <el-col :span="6">
              <el-form-item label='Tên bệnh nhân' prop='name'>
                <el-input v-model='queryModel.name' :clearable='true' placeholder='Vui lòng nhập tên bệnh nhân'></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label='Thông tin liên hệ' prop='phone'>
                <el-input v-model='queryModel.phone' :clearable='true' placeholder='Vui lòng nhập thông tin liên hệ'></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label='Số CMND/CCCD' prop='card'>
                <el-input v-model='queryModel.card' :clearable='true' placeholder='Vui lòng nhập số CMND/CCCD'></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="3" style="display:flex;justivy-content:space-around">
              <el-button
                type="primary"
                icon="el-icon-search"
                @click="onSearch()"
                :plain="true"
              >Tìm kiếm</el-button>
              <el-button
                type="info"
                icon="el-icon-refresh-left"
                @click="reset"
                :plain="true"
              >Đặt lại</el-button>
            </el-col>
            <el-col :span="3" style="text-align:right;padding-right:5px">
              <el-button-group>
                <el-button
                  type="primary"
                  icon="el-icon-plus"
                  @click="onCreatePatient()"
                >Thêm</el-button>
              </el-button-group>
            </el-col>
          </el-form>
        </el-row>
        <QueryForm v-else v-model='moreParm' :tableId='tableId' :schemeId='schemeId'  :routerId='$route.meta.routerId' @search='onSearch()' @moreCodition='onMoreCodition()'></QueryForm>
      </div>
      <!--  Thanh tìm kiếm  Kết thúc -->

      <!-- Thanh công cụ Bắt đầu -->
      <!-- Thanh công cụ Kết thúc -->

      <!-- Thanh bảng  Bắt đầu -->
      <el-row>
        <el-col :span='24'>
          <div @mouseleave='moveTableOutside'>
            <el-table ref="patientTableRef" class="patient-table" :data='patientList' border height="calc(100vh - 350px)" @sort-change='onSortChange' @header-dragend='onChangeWidth' :cell-class-name='cellClassName' :header-cell-class-name='headerCellClassName' highlight-current-row>
              <el-table-column
                label="Số thứ tự"
                width="70px"
                type="index"
                :index="indexMethod"
                align="center">
              </el-table-column>
              <el-table-column v-for="(cv, index) in columnViews" v-if="cv.display"  :prop='cv.prop' :key="`columnViews_${index}`" :label='cv.label' sortable='custom' :align='cv.align'   header-align='center' :column-key='index.toString()'>
                <template slot-scope='{row,$index}'>
                  <span>{{ getAttrValue(row, cv.prop, cv.javaType )}}</span>
                </template>
              </el-table-column>
              <el-table-column  label='Thao tác'  header-align='center' key="operationColumn" width='160' >
                <template slot-scope='scope'>
                  <OperationIcon type='info' content='Xem' placement='top-start' icon-name='el-icon-view'
                                 @click='onViewPatient(scope.$index, scope.row)'></OperationIcon>
                  <OperationIcon type='primary' content='Sửa' placement='top-start' icon-name='el-icon-edit'
                                 @click='onEditPatient(scope.$index, scope.row)'></OperationIcon>
                  <OperationIcon type='primary' content='Sao chép' placement='top-start' icon-name='el-icon-document'
                                 @click='onCopyPatient(scope.$index, scope.row)'></OperationIcon>
                  <OperationIcon type='info' content='Lịch sử' placement='top-start' icon-name='el-icon-info'
                                 @click='onShowHistory(scope.$index, scope.row)'></OperationIcon>
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
            :page-sizes='[ 20, 50, 100, patientTotal]'
            :page-size='20'
            layout='total, sizes, prev, pager, next, jumper'
            :total='patientTotal'>
          </el-pagination>
        </el-col>
      </el-row>
      <!-- Thanh phân trang     Kết thúc -->
    </el-card>
  </el-row>
</template>

<script>
import { validatenull } from '@/utils/validate'
import { listPatientPage, getPatientById, deletePatient } from '@/api/outpatient/patient'
import PatientForm from '@/views/outpatient/patient/patientForm'
import QueryForm from '@/views/components/queryForm'
import MainUI from '@/views/components/mainUI'
import OperationIcon from '@/components/OperationIcon'
import History from '@/views/components/history'

export default {
  name: 'PatientManage',
  extends: MainUI,
  components: {
    PatientForm,
    QueryForm,
    OperationIcon,
    History
  },
  data() {
    return {
      queryTypes: {
        'name': 'like',
        'phone': 'like',
        'card': '=',
      },
      queryModel: {
        'name': '',
        'phone': '',
        'card': '',
      },
      search: {
        params: [{columnName: 'company_id', queryType: '=', value: currentUser.company.id}],
        offset: 0,
        limit: 20,
        columnName: '',
        order: ''
      },
      currentPage: 1,
      patientTotal: 0,
      patientList: [],
      loading: false,
      moreCodition: false,
      moreParm: {},
      oprColumnWidth: 140,
      tableId: '1008489176147648530',
      schemeId: '1008489176147648553',
      columnViews: [],
      curentRow: {}
    }
  },
  methods: {
    reset(){
      this.$refs.queryForm.resetFields()
      this.onSearch()
    },
    getPatientList() {
      this.setLoad()
      this.search.params = [{columnName: 'company_id', queryType: '=', value: currentUser.company.id}]
      if(this.moreCodition) {
        this.search.params = this.search.params.concat(this.compositeCondition())
      }else{
        this.search.params.push({
          columnName: 'name',
          queryType: 'like',
          value: this.queryModel.name
        })
        this.search.params.push({
          columnName: 'phone',
          queryType: 'like',
          value: this.queryModel.phone
        })
        this.search.params.push({
          columnName: 'card',
          queryType: '=',
          value: this.queryModel.card
        })
      }
      this.pushDataPermissions(this.search.params, this.$route.meta.routerId, this.tableId)
      listPatientPage(this.search).then(responseData => {
        if(responseData.code === 100) {
          this.patientTotal = responseData.data.total
          this.patientList = responseData.data.rows
        } else {
          this.showMessage(responseData)
        }
        this.resetLoad()
      }).catch(error => {
        this.outputError(error)
      })
    },
    indexMethod(index){
      return (this.currentPage-1)*this.search.limit+index +1;
    },
    onSearch() {
      if(this.moreCodition) {
        this.search.offset = 0
        this.currentPage = 1
        this.getPatientList()
      } else {
        this.$refs['queryForm'].validate(valid => {
          if (valid) {
            this.search.offset = 0
            this.currentPage = 1
            this.getPatientList()
          } else {
            return false
          }
        })
      }
    },
    onSizeChange(val) {
      this.currentPage = 1
      this.search.limit = val;
      this.search.offset = (this.currentPage - 1) * val
      this.getPatientList()
    },
    onCurrentChange(val) {
      this.search.offset = (val - 1) * this.search.limit
      this.currentPage = val
      this.getPatientList()
    },
    async pageInit() {
      this.setLoad()
      try {
        this.initOptions(this.queryModel)
        this.search.params = [{columnName: 'company_id', queryType: '=', value: currentUser.company.id}]
        this.pushDataPermissions(this.search.params, this.$route.meta.routerId, this.tableId)

        let listPatientRespData = await listPatientPage(this.search)

        if(listPatientRespData.code === 100) {
          this.patientTotal = listPatientRespData.data.total
          this.patientList = listPatientRespData.data.rows
        }
        this.resetLoad()
      } catch(error) {
        this.outputError(error)
        this.resetLoad()
      }
    },
    onViewPatient(index, row) {
      this.setLoad()
      getPatientById(row.id).then(responseData => {
        if(responseData.code === 100) {
          this.$refs.patientForm.openViewPatientDialog(responseData.data)
        } else {
          this.showMessage(responseData)
        }
        this.resetLoad()
      }).catch(error => {
        this.outputError(error)
      })
    },
    onCreatePatient() {
      this.$refs.patientForm.openAddPatientDialog()
    },
    onEditPatient(index, row) {
      this.setLoad()
      getPatientById(row.id).then(responseData => {
        if(responseData.code === 100) {
          this.$refs.patientForm.openEditPatientDialog(responseData.data)
        }else{
          this.showMessage(responseData)
        }
        this.resetLoad()
      }).catch(error => {
        this.outputError(error)
      })
    },
    onCopyPatient(index, row) {
      this.setLoad()
      getPatientById(row.id).then(responseData => {
        if(responseData.code === 100) {
          this.$refs.patientForm.openCopyPatientDialog(responseData.data)
        } else {
          this.showMessage(responseData)
        }
        this.resetLoad()
      }).catch(error => {
        this.outputError(error)
      })
    },
    onDeletePatient(index, row) {
      this.$confirm('Bạn có chắc muốn xóa không？', 'Xác nhận', {
        confirmButtonText: 'Xác nhận',
        cancelButtonText: 'Hủy',
        type: 'warning'
      }).then(() => {
        this.setLoad()
        deletePatient(row).then(responseData => {
          if(responseData.code === 100) {
            this.getPatientList()
            this.showMessage({type: 'success', msg: 'Xóa thành công'})
          } else {
            this.showMessage(responseData)
          }
          this.resetLoad()
        }).catch(error => {
          this.outputError(error)
        })
      }).catch(() => {})
    },
    onSortChange(orderby) {
      if(validatenull(orderby.prop)) {
        this.search.columnName = ''
        this.search.order = ''
      } else {
        this.search.columnName = orderby.prop
        this.search.order = orderby.order === 'descending' ? 'desc' : 'asc'
      }
      this.getPatientList()
    },
    initOptions(This) {},
    setLoad() {
      this.loading = true
    },
    resetLoad() {
      this.loading = false
    },
    moveTableOutside() {},
    onChangeWidth() {},
    cellClassName() {},
    headerCellClassName() {},

    onMoreCodition() {
      this.moreCodition = !this.moreCodition
    },
    onShowHistory(index, row) {
      this.curentRow = row
    }
  },
  watch: {
    patientList: {
      handler () {
        this.$nextTick(() => {
          if(this.$refs.patientTableRef){
            this.$refs.patientTableRef.doLayout()
          }
        })
      },
      deep: true
    }
  },
  updated(){
    this.$nextTick(() => {
      if(this.$refs.patientTableRef){
        this.$refs.patientTableRef.doLayout()
      }
    })
  },
  mounted() {
    this.pageInit()
  }
}
</script>

<style scoped>
/* Xuyên qua scope, áp dụng cho cột cố định */
::v-deep(.el-table__fixed-right .cell) {
  display: flex !important;
  align-items: center !important;
  justify-content: flex-start !important; /* Đổi thành center theo yêu cầu */
  gap: 5px; /* Khoảng cách giữa các nút */
  padding: 0 5px; /* Padding bên trong */
  height: 100% !important;
}

/* Khắc phục căn chỉnh tiêu đề danh sách cố định */
::v-deep(.el-table__fixed-right .el-table__header-wrapper th) {
  height: 48px !important;
  line-height: 48px !important;
  vertical-align: middle !important;
}

/* Sửa chiều cao dòng bảng */
::v-deep(.el-table__fixed-right .el-table__body-wrapper .el-table__row) {
  height: 48px !important;
}

/* Căn giữa nội dung bảng */
::v-deep(.el-table__cell) {
  vertical-align: middle !important;
}
</style>
