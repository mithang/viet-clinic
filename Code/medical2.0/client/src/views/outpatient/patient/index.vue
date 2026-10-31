<template>
  <el-row v-loading='loading'>
    <!-- Lịch sử  -->
    <History :bussObject='curentRow' ></History>
    <!-- Cửa sổ chỉnh sửa  -->
    <patient-form ref='patientForm' :permission='permission' @save-finished='loadData()'></patient-form>
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
                    v-show="permission.add"
                    type="primary"
                    icon="el-icon-plus"
                    @click='onCreateEntity("patientForm")'
                    >Thêm</el-button
                  >
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
            <el-table ref="patientTableRef"  :data='patientList' border @sort-change='onSortChange' @header-dragend='onChangeWidth' :cell-class-name='cellClassName' :header-cell-class-name='headerCellClassName' highlight-current-row>
              <el-table-column
              label="Số thứ tự"
              width="70px"
              type="index"
              :index="indexMethod"
              align="center">
            </el-table-column>
              <el-table-column v-for="(cv, index) in columnViews" v-if="cv.display"  :prop='cv.prop' :key="`columnViews_${index}`" :label='cv.label' sortable='custom' :align='cv.align'   header-align='center' :column-key='index.toString()' :render-header="renderHeader">
                <template slot-scope='{row}'>
<!--                  <span v-if='columnViews[index].showType == "Switch" || columnViews[index].showType == "Checkbox" || columnViews[index].showType == "Radio"'>
                    <li v-if='getAttrValue(row, columnViews[index].prop) === "1"' class='el-icon-check' style='color:#F56C6C;'></li>
                  </span>
                  <span v-else>{{ getAttrValue(row, columnViews[index].prop, columnViews[index].javaType )}}</span>-->
                  <span>{{ getAttrValue(row, columnViews[index].prop, columnViews[index].javaType )}}</span>
                </template>
              </el-table-column>
              <!--Nút thao tác theo dòng-->
              <el-table-column  label='Thao tác'  header-align='center' :key="'operate'" :width='140' >
<!--                <template slot='header' slot-scope="scope">
                  <span>Thao tác</span>
                  <view-columns-select v-model='columnViews' v-on:save-column-view='saveColumn' v-on:show-all-column='showAllColumn' v-on:show-default-column='showDefaultColumn'></view-columns-select>
                  <export-excel-button v-show='permission.export' :data='patientList' :tHeader='getHeads()' :filterVal='getFilterVal()' :plain='true'></export-excel-button>
                </template>-->
                <template slot-scope='scope'>
                  <OperationIcon v-show='permission.view' type='info' content='Xem' placement='top-start' icon-name='el-icon-view'
                    @click='onViewEntity(scope.$index, scope.row, "patientForm")'></OperationIcon>
                  <OperationIcon v-show='permission.edit' type='primary' content='Sửa' placement='top-start' icon-name='el-icon-edit'
                    @click='onEditEntity(scope.$index, scope.row, "patientForm")'></OperationIcon>
                  <OperationIcon v-show='permission.add' type='primary' content='Sao chép' placement='top-start' icon-name='el-icon-document'
                    @click='onCopyEntity(scope.$index, scope.row, "patientForm")'></OperationIcon>
                  <OperationIcon v-show='permission.remove' type='danger' content='Xóa' placement='top-start' icon-name='el-icon-delete'
                    @click='onDeleteEntity(scope.$index, scope.row, deleteApi)'></OperationIcon>
                  <OperationIcon v-show='permission.view' type='info' content='Lịch sử' placement='top-start' icon-name='el-icon-info'
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
      <!-- Thanh phân trang     Kết thúc -->     </el-card>
  </el-row>
</template>

<script>
import { listPatientPage, getPatientById, deletePatient } from '@/api/outpatient/patient'
import listViewMixin from '@/mixins/listViewMixin'
import PatientForm from './patientForm'
import ExportExcelButton from '@/components/ExportExcelButton'
import ViewColumnsSelect from '@/views/components/ViewColumnsSelect'
import QueryForm from '@/views/components/queryForm'
import MainUI from '@/views/components/mainUI'
import OperationIcon from '@/components/OperationIcon'
import History from '@/views/components/history'
export default {
  extends: MainUI,
  mixins: [listViewMixin],
  components: {
    PatientForm,
    ExportExcelButton,
    ViewColumnsSelect,
    QueryForm,
    OperationIcon,
    History
  },
  data() {
    return {
      listApi: listPatientPage,
      getApi: getPatientById,
      deleteApi: deletePatient,
      entityName: 'Patient',
      permissionPrefix: 'patient',
      queryTypes: {
        'name': 'like',
        'phone': 'like',
        'card': '=',
      },
      queryModel: {
        'name': '',   // Tên bệnh nhân
        'phone': '',   // Thông tin liên hệ
        'card': '',   // Số CMND/CCCD
      },
      patientTotal: 0,
      patientList: [],

      oprColumnWidth: 140,  // Độ rộng cột thao tác
      tableId: '1008489176147648530',
      schemeId: '1008489176147648553'
    }
  },
  methods: {
    reset(){
      this.$refs.queryForm.resetFields()
      this.onSearch()
    },
    appendSearchParams() {
      this.search.params.push({columnName: 'company_id', queryType: '=', value: currentUser.company.id})
      if (this.moreCodition) {
        this.search.params = this.search.params.concat(this.compositeCondition())
      } else {
        // Tham số truy vấn: Tên bệnh nhân
        this.search.params.push({
          columnName: 'name',
          queryType: 'like',
          value: this.queryModel.name
        })
        // Tham số truy vấn: Thông tin liên hệ
        this.search.params.push({
          columnName: 'phone',
          queryType: 'like',
          value: this.queryModel.phone
        })
        // Tham số truy vấn: Số CMND/CCCD
        this.search.params.push({
          columnName: 'card',
          queryType: '=',
          value: this.queryModel.card
        })
      }
      // Quyền dữ liệu: Bảng bệnh nhânpatient
      this.pushDataPermissions(this.search.params, this.$route.meta.routerId, this.tableId)
    },
    handleListResponse(responseData) {
      this.patientTotal = responseData.data.total
      this.patientList = responseData.data.rows
    },
    initOptions(This) {
    }
  },
  watch: {
     // tableDataCóel-tableDữ liệu đã liên kết
      tableData: {
          // Khắc phục lỗi hiển thị lệch bảng
          handler () {
              this.$nextTick(() => {
                  // tableRefCóel-tableĐã liên kếtrefGiá trị thuộc tính
                  this.$refs.patientTableRef.doLayout()// Đối với Table Thực hiện bố cục lại
              })
          },
          deep: true
      }
  },
  updated(){
  //  if(this.$refs.patientTableRef){
      this.$nextTick(() => {
            // tableRefCóel-tableĐã liên kếtrefGiá trị thuộc tính
            this.$refs.patientTableRef.doLayout()// Đối với Table Thực hiện bố cục lại
        })
    //}
  },
  mounted() {
    this.pageInit()
  }
}
</script>
<style lang="scss" scoped>
  .page-container{
    padding: 0;
  };

  ::v-deep.el-table{
    .el-table__fixed-body-wrapper{
      top: 47px !important;
    }
  };

  ::v-deep .el-table__fixed-right-patch{
    width:5px !important
  };

  ::v-deep .el-table colgroup col[name='gutter']{
    width:5px !important
  };

  ::v-deep .el-table__body{
    width:100% !important
  };

  .drag_table{
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
<style scoped>
 .el-table__body-wrapper{
    height: calc(100% - 44px) !important;
  }
</style>
