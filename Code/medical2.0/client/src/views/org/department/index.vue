<template>
  <el-row v-loading='loading'>
    <el-container>
      <!-- Lịch sử  -->
      <History :bussObject='curentRow' ></History>
      <!-- Cửa sổ chỉnh sửa  -->
      <department-form ref='departmentForm' :permission='permission' @save-finished='loadData'></department-form>
      <!-- Người không phải quản trị hệ thống không thấy được  -->
      <div class="page-left-container" v-show='Number(currentUser.id) === 1001'>
        <el-aside>
          <el-table ref='treeTable' :data='companys' row-key='id' :tree-props="{children: 'children', hasChildren: 'hasChildren'}" highlight-current-row :cell-style="function() {return {borderBottom: 'none'} }" @current-change='onLeftCurrentChange'>
            <el-table-column label='Công ty' prop='name'></el-table-column>
          </el-table>
        </el-aside>
      </div>
      <div class="page-container">
      <!--  Thanh tìm kiếm  Bắt đầu -->
      <div class='query-form-container'>
        <el-main>
          <el-row v-if='!moreCodition' class='search-row'>
            <el-form :model='queryModel' @submit.native.prevent label-position="left" label-width='70px' ref='queryForm' :inline-message='true'>
              <el-col :span="6">
                <el-form-item label='Tên' prop='name'>
                  <el-input v-model='queryModel.name' :clearable='true' placeholder='Vui lòng nhập tên'></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-button-group>
                  <el-tooltip  effect="light" content="Tìm kiếm" placement="top-start">
                    <el-button type="primary" icon="el-icon-search" @click='onSearch()' :plain='true'></el-button>
                  </el-tooltip>
                    <el-tooltip  effect="light" content="Đặt lại" placement="top-start">
                    <el-button type="primary" icon="el-icon-refresh-left" @click='$refs.queryForm.resetFields()' :plain='true'></el-button>
                  </el-tooltip>
                  <el-tooltip  effect="light" content="Thêm" placement="top-start">
                    <el-button type="primary" icon="el-icon-d-arrow-right" @click='onMoreCodition()' :plain='true'></el-button>
                  </el-tooltip>
                </el-button-group>
              </el-col>
            </el-form>
          </el-row>
          <QueryForm v-else v-model='moreParm' :tableId='tableId' :schemeId='schemeId' :curNode='currentCompany' exclude='company_id' :routerId='resolvedRouterId' @search='onSearch()' @moreCodition='onMoreCodition()'></QueryForm>
        </el-main>
      </div>
      <!--  Thanh tìm kiếm  Kết thúc -->

      <!-- Thanh công cụ Bắt đầu -->
      <div class="page-container-header-end">
        <div>
          <el-button v-show='permission.add' type='primary' icon='el-icon-plus'  @click='onCreateDepartment()'>Thêm</el-button>
        </div>
      </div>
      <!-- Thanh công cụ Kết thúc -->

      <!-- Thanh bảng  Bắt đầu -->
      <el-row>
        <el-col :span='24'>
          <div @mouseleave='moveTableOutside'>
            <el-table class='drag_table' :data='departmentList' row-key='id' :tree-props="{children: 'children', hasChildren: 'hasChildren'}" border @sort-change='onSortChange' @header-dragend='onChangeWidth' :cell-class-name='cellClassName' :header-cell-class-name='headerCellClassName' highlight-current-row>
              <el-table-column v-for="(cv, index) in columnViews" v-if='cv.display' :prop='cv.prop' :key="`columnViews_${index}`" :label='cv.label' sortable='custom' :align='cv.align' :min-width='cv.miniWidth+"px"' :width='cv.width+"px"' header-align='center' :column-key='index.toString()' :render-header="renderHeader">
                <template slot-scope='{row,$index}'>
                  <span v-if='columnViews[index].showType === "Switch" || columnViews[index].showType === "Checkbox" || columnViews[index].showType === "Radio"'>
                    <li v-if='getAttrValue(row, columnViews[index].prop) === "1"' class='el-icon-check' style='color:#F56C6C;'></li>
                  </span>
                  <span v-else>{{ getAttrValue(row, columnViews[index].prop, columnViews[index].javaType)}}</span>
                </template>
              </el-table-column>
              <!--Nút thao tác theo dòng-->
              <el-table-column label='Thao tác' header-align='center' :width='oprColumnWidth + "px"'>
                <template slot='header' slot-scope="scope">
                  <span>Thao tác</span>
                  <view-columns-select v-model='columnViews' v-on:save-column-view='saveColumn' v-on:show-all-column='showAllColumn' v-on:show-default-column='showDefaultColumn'></view-columns-select>
                  <export-excel-button v-show='permission.export' :data='departmentList' :tHeader='getHeads()' :filterVal='getFilterVal()' :plain='true'></export-excel-button>
                </template>
                <template slot-scope='scope'>
                  <OperationIcon v-show='permission.view' type='info' content='Xem' placement='top-start' icon-name='el-icon-view'
                    @click='onViewDepartment(scope.$index, scope.row)'></OperationIcon>
                  <OperationIcon v-show='permission.add' type='primary' content='Thêm cấp dưới' placement='top-start' icon-name='el-icon-circle-plus-outline'
                    @click='onCreateDepartment(scope.$index, scope.row)'></OperationIcon>
                  <OperationIcon v-show='permission.edit' type='primary' content='Sửa' placement='top-start' icon-name='el-icon-edit'
                    @click='onEditDepartment(scope.$index, scope.row)'></OperationIcon>
                  <OperationIcon v-show='permission.add' type='primary' content='Sao chép' placement='top-start' icon-name='el-icon-document'
                    @click='onCopyDepartment(scope.$index, scope.row)'></OperationIcon>
                  <OperationIcon v-show='permission.remove && (!(scope.row.children) || scope.row.children.length <=0)' type='danger' content='Xóa' placement='top-start' icon-name='el-icon-delete'
                    @click='onDeleteDepartment(scope.$index, scope.row)'></OperationIcon>
                  <OperationIcon v-show='permission.view' type='info' content='Lịch sử' placement='top-start' icon-name='el-icon-info'
                    @click='onShowHistory(scope.$index, scope.row)'></OperationIcon>
                </template>
              </el-table-column>
            </el-table>
    	  </div>
        </el-col>
      </el-row>
      <!-- Thanh bảng  Kết thúc -->
      </div>
    </el-container>
  </el-row>
</template>

<script>
import { validatenull } from '@/utils/validate'
import { treeDepartment, getDepartmentById, deleteDepartment } from '@/api/org/department'
import listViewMixin from '@/mixins/listViewMixin'
import { listResourcePermission } from '@/api/resourcePermission'
import { mapPermissions } from '@/utils/searchParamsBuilder'
import DepartmentForm from './departmentForm'
import { treeCompany } from '@/api/org/company'

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
    DepartmentForm,
    ExportExcelButton,
    ViewColumnsSelect,
    QueryForm,
    OperationIcon,
    History
  },
  data() {
    return {
      listApi: treeDepartment,
      getApi: getDepartmentById,
      deleteApi: deleteDepartment,
      entityName: 'Department',
      permissionPrefix: 'department',
      queryTypes: {
    	'company_id': '=',
        'name': 'like',
      },
      queryModel:  {
        'company': {     // Bảng cha Công ty
          'id': null,
          'name': '',
        },
        'name': '',   // Tên
      },
      currentCompany: {},     //Công ty được chọn trong cấu trúc cây
      departmentList: [],     // Dữ liệu bảng số
      companys: [],           // Bảng cây công ty

      oprColumnWidth: 165,  // Độ rộng cột thao tác
      tableId: '52676365136650250',
      schemeId: '52676365136650309'
    }
  },
  methods: {
    appendSearchParams() {
      if(validatenull(this.currentCompany)) {
        this.$alert('Vui lòng chọn công ty', 'Nhắc nhở', {
          confirmButtonText: 'Xác nhận',
          type: 'info'
        })
        return
      }
      this.search.params.push({
      	columnName: 'company_id',
      	queryType: '=',
      	value: this.currentCompany.id
      })
      if(!this.moreCodition) {
        this.search.params.push({
          columnName: 'name',
          queryType: 'like',
          value: this.queryModel.name
        })
      }
      this.pushDataPermissions(this.search.params, this.resolvedRouterId, this.tableId)
    },
    handleListResponse(responseData) {
      this.departmentList = responseData.data
    },
    async pageInit() {
      this.setLoad()
      try {
        let params = []
        /* Người không phải quản trị hệ thống，Chỉ truy vấn được dữ liệu của công ty mình */
        if(Number(currentUser.id) !== 1001) {
          params.push({'columnName':'id', 'queryType': '=', 'value': currentUser.company.id})
        }
        // Quyền dữ liệu: Công tyorg_company
        this.pushDataPermissions(params, this.resolvedRouterId, '41040096140492800')
        // RouterId thiếu (route không có meta): bỏ qua lệnh gọi quyền
        // (trước đây gọi với undefined → 400 + alert), quyền rỗng fail-closed,
        // danh sách vẫn tải.
        let [treeCompanyRespData, listPermissionRespData] = await Promise.all([
          treeCompany({params: params, columnName: '', 'order': ''}),
          this.resolvedRouterId ? listResourcePermission(this.resolvedRouterId) : Promise.resolve({ code: 100, data: [] })
        ])
        if(treeCompanyRespData.code === 100 && listPermissionRespData.code === 100) {
          this.companys = treeCompanyRespData.data
          // Fallback: auto-select company so Create/Search are not blocked when the tree
          // is hidden (non-system users) or setCurrentRow does not fire current-change.
          // System (1001) uses the first tree node; others use their own company.
          if (typeof this.currentCompany !== 'undefined' && (this.currentCompany === null || Object.keys(this.currentCompany).length === 0)) {
            if (Number(currentUser.id) !== 1001 && currentUser.company && currentUser.company.id) {
              this.currentCompany = currentUser.company
            } else if (this.companys && this.companys.length > 0) {
              this.currentCompany = this.companys[0]
            }
          }
          this.$nextTick(() => {
            if(this.companys && this.companys.length > 0 && this.$refs.treeTable){
              this.$refs.treeTable.setCurrentRow(this.companys[0])
            }
          })
          this.permission = mapPermissions(listPermissionRespData.data, 'department')
        } else {
          this.showMessage(listPermissionRespData.code !== 100 ? listPermissionRespData : treeCompanyRespData)
        }
        this.resetLoad()
      } catch(error) {
        this.outputError(error)
      }
    },
    onViewDepartment(index, row) {
      this.setLoad()
      getDepartmentById(row.id).then(responseData => {
        if(responseData.code === 100) {
          let department = responseData.data
          if(validatenull(department.parent)) {
            department.parent = {id: null}
          }
          this.$refs.departmentForm.openViewDepartmentDialog(department)
        } else {
          this.showMessage(responseData)
        }
        this.resetLoad()
      }).catch(error => {
        this.outputError(error)
      })
    },
    onCreateDepartment(index, row) {
      if(validatenull(this.currentCompany)) {
        this.$alert('Vui lòng chọn công ty', 'Nhắc nhở', {
          confirmButtonText: 'Xác nhận',
          type: 'info'
        })
        return
      }
      if(validatenull(row)) {
        row={
          id: null,
          name: '',
          'company': this.currentCompany
        }
      }
      this.$refs.departmentForm.openAddDepartmentDialog(row)
    },
    onEditDepartment(index, row) {
      this.setLoad()
      getDepartmentById(row.id).then(responseData => {
        if(responseData.code === 100) {
          let department = responseData.data
          if(validatenull(department.parent)) {
            department.parent = {id: null}
          }
          this.$refs.departmentForm.openEditDepartmentDialog(department)
        }else{
          this.showMessage(responseData)
        }
        this.resetLoad()
      }).catch(error => {
        this.outputError(error)
      })
    },
    onCopyDepartment(index, row) {
      this.setLoad()
      getDepartmentById(row.id).then(responseData => {
        if(responseData.code === 100) {
          let department = responseData.data
          if(validatenull(department.parent)) {
            department.parent = {id: null}
          }
          this.$refs.departmentForm.openCopyDepartmentDialog(department)
        } else {
          this.showMessage(responseData)
        }
        this.resetLoad()
      }).catch(error => {
        this.outputError(error)
      })
    },
    onDeleteDepartment(index, row) {
      this.$confirm('Bạn có chắc muốn xóa không？', 'Xác nhận', {
        confirmButtonText: 'Xác nhận',
        cancelButtonText: 'Hủy',
        type: 'warning'
      }).then(() => {
        this.setLoad()
        deleteDepartment(row).then(responseData => {
          if(responseData.code === 100) {
            this.loadData()
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
    onLeftCurrentChange(currentRow, oldCurrentRow) {
      if(currentRow !== oldCurrentRow) {
        this.currentCompany = currentRow
        this.queryModel['company'] = currentRow
        this.initOptions(this.queryModel)
        this.onSearch()
      }
    },

    initOptions(This) {
    }
  },
  computed: {
    // RouterId đã resolve (meta, fallback session, hoặc null): dùng cho
    // QueryForm và các lệnh gọi quyền thay vì $route.meta.routerId thô.
    resolvedRouterId() {
      return this.resolveRouterId()
    }
  },
  watch: {
  },
  mounted() {
    this.pageInit()
  }
}
</script>
