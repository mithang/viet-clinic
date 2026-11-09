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
        <div style="margin: 8px 0">
          <el-button
            type="info"
            icon="el-icon-document"
            @click="openAuditDialog"
          >Nhật ký truy cập hồ sơ</el-button>
        </div>
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
              <template v-for="(cv, index) in columnViews">
              <el-table-column v-if="cv.display"  :prop='cv.prop' :key="`columnViews_${index}`" :label='cv.label' sortable='custom' :align='cv.align'   header-align='center' :column-key='index.toString()' :render-header="renderHeader">
                <template slot-scope='{row}'>
<!--                  <span v-if='columnViews[index].showType == "Switch" || columnViews[index].showType == "Checkbox" || columnViews[index].showType == "Radio"'>
                    <li v-if='getAttrValue(row, columnViews[index].prop) === "1"' class='el-icon-check' style='color:#F56C6C;'></li>
                  </span>
                  <span v-else>{{ getAttrValue(row, columnViews[index].prop, columnViews[index].javaType )}}</span>-->
                  <span>{{ formatCellValue(row, columnViews[index]) }}</span>
                </template>
              </el-table-column>
              </template>
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
    <!-- G8: nhật ký truy cập hồ sơ — chỉ đọc, không thêm router/menu. -->
    <el-dialog
      title="Nhật ký truy cập hồ sơ"
      :visible.sync="auditDialogVisible"
      width="900px"
    >
      <div style="display:flex;gap:8px;margin-bottom:12px">
        <el-input
          v-model="auditPatientId"
          clearable
          placeholder="Lọc theo mã bệnh nhân (để trống = tất cả)"
          style="width:300px"
        ></el-input>
        <el-select v-model="auditAction" clearable placeholder="Tất cả hành động" style="width:200px">
          <el-option label="Xem hồ sơ" value="VIEW"></el-option>
          <el-option label="Xem danh sách" value="LIST"></el-option>
          <el-option label="Sửa hồ sơ" value="EDIT"></el-option>
        </el-select>
        <el-button type="primary" :loading="auditLoading" @click="loadAudits">Tải lại</el-button>
      </div>
      <p style="color:#909399;font-size:13px;margin:0 0 8px">
        Nhật ký chỉ ghi lại ai đã xem hoặc sửa hồ sơ, và khi nào — không
        ghi nội dung hồ sơ. Lượt xem danh sách được ghi đúng một dòng
        cho cả lần mở, không ghi theo từng bệnh nhân.
      </p>
      <el-table v-loading="auditLoading" :data="auditList" border style="width:100%" empty-text="Chưa có nhật ký truy cập nào.">
        <el-table-column label="Thời gian" width="170" align="center">
          <template slot-scope="scope">{{ formatAuditDate(scope.row.createDate) }}</template>
        </el-table-column>
        <el-table-column label="Người truy cập" align="center">
          <template slot-scope="scope">{{ scope.row.userName || scope.row.userLoginName || scope.row.createBy || '—' }}</template>
        </el-table-column>
        <el-table-column label="Hành động" width="140" align="center">
          <template slot-scope="scope">{{ formatAuditAction(scope.row.action) }}</template>
        </el-table-column>
        <el-table-column label="Bệnh nhân" align="center">
          <template slot-scope="scope">{{ scope.row.patientName || (scope.row.patientId ? scope.row.patientId : '— (danh sách)') }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </el-row>
</template>

<script>
import { listPatientPage, getPatientById, deletePatient } from '@/api/outpatient/patient'
import { listAdministrativeDivisionAll } from '@/api/org/administrativeDivision'
import { listRecordAudits } from '@/api/recordAudit'
import listViewMixin from '@/mixins/listViewMixin'
import PatientForm from './patientForm'
import QueryForm from '@/views/components/queryForm'
import MainUI from '@/views/components/mainUI'
import OperationIcon from '@/components/OperationIcon'
import History from '@/views/components/history'
export default {
  name: 'OutpatientPatientIndex',
  extends: MainUI,
  mixins: [listViewMixin],
  components: {
    PatientForm,
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
      // Tra cứu tên tỉnh/xã từ administrative_division: danh sách lưu mã
      // (province/area) nên tra tên để hiển thị thay mã.
      divisionNameMap: {},
      divisionAreaLoaded: {},

      // G8: nhật ký truy cập hồ sơ (dialog chỉ đọc trên trang sẵn có).
      auditDialogVisible: false,
      auditLoading: false,
      auditList: [],
      auditPatientId: '',
      auditAction: '',

      oprColumnWidth: 140,  // Độ rộng cột thao tác
      tableId: '1008489176147648530',
      schemeId: '1008489176147648553'
    }
  },
  methods: {
    // G8: mở dialog nhật ký và tải 100 dòng mới nhất của tenant.
    openAuditDialog() {
      this.auditDialogVisible = true
      this.loadAudits()
    },
    loadAudits() {
      this.auditLoading = true
      listRecordAudits({
        patientId: this.auditPatientId || undefined,
        action: this.auditAction || undefined,
        limit: 100
      }).then((response) => {
        this.auditList = response.data || []
        this.auditLoading = false
      }).catch(() => {
        this.auditList = []
        this.auditLoading = false
      })
    },
    formatAuditAction(action) {
      if (action === 'VIEW') return 'Xem hồ sơ'
      if (action === 'LIST') return 'Xem danh sách'
      if (action === 'EDIT') return 'Sửa hồ sơ'
      return action || '—'
    },
    formatAuditDate(value) {
      if (!value) return '—'
      const d = new Date(String(value).replace(' ', 'T'))
      if (isNaN(d.getTime())) return String(value)
      const pad = (n) => (n < 10 ? '0' + n : '' + n)
      return pad(d.getDate()) + '/' + pad(d.getMonth() + 1) + '/' + d.getFullYear()
        + ' ' + pad(d.getHours()) + ':' + pad(d.getMinutes())
    },
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
      this.loadDivisionAreasForRows()
    },
    // Tra cứu tên tỉnh/xã: tỉnh (level=1) tải một lần khi mở trang; xã
    // (level=2) tải theo từng tỉnh xuất hiện trong trang rồi cache lại.
    formatCellValue(row, cv) {
      const raw = this.getAttrValue(row, cv.prop, cv.javaType)
      if ((cv.prop === 'province' || cv.prop === 'city' || cv.prop === 'area') && raw !== undefined && raw !== null && raw !== '') {
        return this.divisionNameMap[String(raw)] || raw
      }
      return raw
    },
    loadDivisionProvinces() {
      const search = { params: [
        { columnName: 'parten_id', queryType: '=', value: '0' },
        { columnName: 'level', queryType: '=', value: '1' }
      ] }
      return listAdministrativeDivisionAll(search).then((responseData) => {
        const entries = {}
        ;(responseData.data || []).forEach((item) => { entries[String(item.id)] = item.name })
        this.divisionNameMap = Object.assign({}, this.divisionNameMap, entries)
      }).catch(() => {})
    },
    loadDivisionAreasForRows() {
      const provinces = []
      ;(this.patientList || []).forEach((row) => {
        const p = row.province
        if (p !== undefined && p !== null && p !== '' && !this.divisionAreaLoaded[String(p)] && provinces.indexOf(String(p)) === -1) {
          provinces.push(String(p))
        }
      })
      provinces.forEach((provinceId) => {
        this.divisionAreaLoaded[provinceId] = true
        const search = { params: [
          { columnName: 'parten_id', queryType: '=', value: provinceId },
          { columnName: 'level', queryType: '=', value: '2' }
        ] }
        listAdministrativeDivisionAll(search).then((responseData) => {
          const entries = {}
          ;(responseData.data || []).forEach((item) => { entries[String(item.id)] = item.name })
          this.divisionNameMap = Object.assign({}, this.divisionNameMap, entries)
        }).catch(() => {})
      })
    },
    initOptions() {
      return this.loadDivisionProvinces()
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
