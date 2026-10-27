<template>
  <el-row v-loading='loading'>
    <el-container>
      <!-- Lịch sử  -->
      <History :bussObject='curentRow' ></History>
      <!-- Cửa sổ chỉnh sửa  -->
      <role-form ref='roleForm' :permission='permission' @save-finished='loadData'></role-form>
      <role-permission ref="rolePermission"></role-permission>
      <!-- Người không phải quản trị hệ thống không thấy được  -->
      <div class="page-left-container" v-show='currentUser.id === 1001'>
        <el-aside>
          <el-table ref='treeTable' :data='companys' row-key='id' :tree-props="{children: 'children', hasChildren: 'hasChildren'}" highlight-current-row :cell-style="function() {return {borderBottom: 'none'} }" @current-change='onLeftCurrentChange'>
            <el-table-column label='Công ty' prop='name'></el-table-column>
          </el-table>
        </el-aside>
      </div>
      <el-card class="page-container">
      <!--  Thanh tìm kiếm  Bắt đầu -->
      <div class='query-form-container'>
          <el-row v-if='!moreCodition' class='search-row'>
            <el-form :model='queryModel' @submit.native.prevent label-position="left" label-width='70px' ref='queryForm' :inline-message='true'>
              <el-col :span="6">
                <el-form-item label='Tên' prop='name'>
                  <el-input v-model='queryModel.name' :clearable='true' placeholder='Vui lòng nhập tên'></el-input>
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
            <el-col :span="15" style="text-align:right;padding-right:5px">
              <el-button-group>
                  <el-button
                    v-show="permission.add"
                    type="primary"
                    icon="el-icon-plus"                   
                    @click="onCreateRole()"
                    >Thêm</el-button
                  >
              </el-button-group>
            </el-col>
            </el-form>
          </el-row>
          <QueryForm v-else v-model='moreParm' :tableId='tableId' :schemeId='schemeId' :curNode='currentCompany' exclude='company_id' :routerId='$route.meta.routerId' @search='onSearch()' @moreCodition='onMoreCodition()'></QueryForm>
      </div>
      <!--  Thanh tìm kiếm  Kết thúc -->
      
      <!-- Thanh công cụ Bắt đầu -->
      
      <!-- Thanh công cụ Kết thúc -->
      
      <!-- Thanh bảng  Bắt đầu -->
      <el-row>
        <el-col :span='24'>
          <div @mouseleave='moveTableOutside'>
            <el-table ref="mutipleTable" height="calc(100vh - 254px)" class='drag_table' :data='roleList' border @sort-change='onSortChange' @header-dragend='onChangeWidth' :cell-class-name='cellClassName' :header-cell-class-name='headerCellClassName' highlight-current-row>
              <el-table-column
              label="Số thứ tự"
              type="index"
              :index="indexMethod"
              align="center">
            </el-table-column>
              <el-table-column v-for="(cv, index) in columnViews" v-if='cv.display' :prop='cv.prop' :key="`columnViews_${index}`" :label='cv.label' sortable='custom' :align='cv.align' :min-width='cv.miniWidth+"px"' :width='cv.width+"px"' header-align='center' :column-key='index.toString()' :render-header="renderHeader">
                <template slot-scope='{row,$index}'>
                  <span v-if='columnViews[index].showType === "Switch" || columnViews[index].showType === "Checkbox" || columnViews[index].showType === "Radio"'>
                    <li v-if='getAttrValue(row, columnViews[index].prop) === "0"' class='el-icon-check' style='color:#F56C6C;'></li>
                  </span>
                  <span v-else>{{ getAttrValue(row, columnViews[index].prop, columnViews[index].javaType)}}</span>
                </template>
              </el-table-column>
              <!--Nút thao tác theo dòng-->
              <el-table-column label='Thao tác' header-align='center' :width='155 + "px"'  :key="'operate'">
                <template slot='header' slot-scope="scope">
                  <span>Thao tác</span>
                  <view-columns-select v-model='columnViews' v-on:save-column-view='saveColumn' v-on:show-all-column='showAllColumn' v-on:show-default-column='showDefaultColumn'></view-columns-select>
                  <export-excel-button v-show='permission.export' :data='roleList' :tHeader='getHeads()' :filterVal='getFilterVal()' :plain='true'></export-excel-button>
                </template>
                <template slot-scope='scope'>
                  <OperationIcon v-show='permission.view' type='info' content='Xem' placement='top-start' icon-name='el-icon-view'
                    @click='onViewEntity(scope.$index, scope.row, "roleForm")'></OperationIcon>
                  <OperationIcon v-show='permission.edit' type='primary' content='Sửa' placement='top-start' icon-name='el-icon-edit'
                    @click='onEditEntity(scope.$index, scope.row, "roleForm")'></OperationIcon>
                  <OperationIcon v-show='permission.add' type='primary' content='Sao chép' placement='top-start' icon-name='el-icon-document'
                    @click='onCopyEntity(scope.$index, scope.row, "roleForm")'></OperationIcon>
                    <OperationIcon v-show='permission.edit' type='primary' content='Cài đặt quyền' placement='top-start' icon-name='el-icon-menu'
                      @click='handleSetPermission(scope.$index, scope.row)'></OperationIcon>
                  <OperationIcon v-show='permission.remove' type='danger' content='Xóa' placement='top-start' icon-name='el-icon-delete'
                    @click='onDeleteEntity(scope.$index, scope.row, deleteApi)'></OperationIcon>
                  <OperationIcon v-show='permission.remove' type='info' content='Lịch sử' placement='top-start' icon-name='el-icon-info'
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
            :page-sizes='[20, 50, 100, roleTotal]'
            :page-size='20'
            layout='total, sizes, prev, pager, next, jumper'
            :total='roleTotal'>
          </el-pagination>
        </el-col>
      </el-row>
      <!-- Thanh phân trang     Kết thúc --> 
      </el-card>
    </el-container>
  </el-row>
</template>

<script>
import { validatenull } from '@/utils/validate'
import { listRolePage, getRoleById, deleteRole } from '@/api/admin/role'
import { listResourcePermission } from '@/api/admin/common/permission'
import listViewMixin from '@/mixins/listViewMixin'
import RoleForm from './roleForm'
import { treeCompany } from '@/api/org/company'

import ExportExcelButton from '@/components/ExportExcelButton'
import ViewColumnsSelect from '@/views/components/ViewColumnsSelect'
import QueryForm from '@/views/components/queryForm'
import MainUI from '@/views/components/mainUI'
import OperationIcon from '@/components/OperationIcon'
import RolePermission from '@/views/admin/common/rolePermission'
import History from '@/views/components/history'
export default {
  extends: MainUI,
  mixins: [listViewMixin],
  components: {
    RoleForm,
    ExportExcelButton,
    ViewColumnsSelect,
    QueryForm,
    OperationIcon,
    RolePermission,
    History
  },
  data() {
    return {
      listApi: listRolePage,
      getApi: getRoleById,
      deleteApi: deleteRole,
      entityName: 'Role',
      permissionPrefix: 'role',
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
      roleTotal: 0,
      roleList: [],     // Dữ liệu bảng số
      companys: [],           // Bảng cây công ty

      oprColumnWidth: 165,  // Độ rộng cột thao tác
      tableId: '4012',
      schemeId: '6011'
    }
  },
  methods: {
    async pageInit() {
      this.setLoad()
      try {
        let params = []
        if(currentUser.id !== 1001) {
          params.push({'columnName':'id', 'queryType': '=', 'value': currentUser.company.id})
        }
        this.pushDataPermissions(params, this.$route.meta.routerId, '41040096140492800')
        let [treeCompanyRespData, listPermissionRespData] = await Promise.all([
          treeCompany({params: params, columnName: '', order: ''}),
          listResourcePermission(this.$route.meta.routerId)
        ])
        if(treeCompanyRespData.code === 100 && listPermissionRespData.code === 100) {
          this.companys = treeCompanyRespData.data
          this.$nextTick(() => {
            if(this.companys && this.companys.length > 0 && this.$refs.treeTable){
              this.$refs.treeTable.setCurrentRow(this.companys[0])
            }
          })
          this.permission.view = listPermissionRespData.data.find(item => item.permission === 'role:read')
          this.permission.export = listPermissionRespData.data.find(item => item.permission === 'role:export')
          this.permission.add = listPermissionRespData.data.find(item => item.permission === 'role:create')
          this.permission.edit = listPermissionRespData.data.find(item => item.permission === 'role:update')
          this.permission.remove = listPermissionRespData.data.find(item => item.permission === 'role:delete')
        } else {
          this.showMessage(listPermissionRespData.code !== 100 ? listPermissionRespData : treeCompanyRespData)
        }
        this.resetLoad()
      } catch(error) {
        this.outputError(error)
      }
    },
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
      if(this.moreCodition) {
        this.search.params = this.search.params.concat(this.compositeCondition())
      }else{
        this.search.params.push({
          columnName: 'name',
          queryType: 'like',
          value: this.queryModel.name
        })
      }
      this.pushDataPermissions(this.search.params, this.$route.meta.routerId, this.tableId)
    },
    handleListResponse(responseData) {
      this.roleTotal = responseData.data.total
      this.roleList = responseData.data.rows
    },
    onCreateRole() {
      if(validatenull(this.currentCompany)) {
        this.$alert('Vui lòng chọn công ty', 'Nhắc nhở', {
          confirmButtonText: 'Xác nhận',
          type: 'info'
        })
        return
      }
      let row={
          'company': this.currentCompany
        }
      this.$refs.roleForm.openAddRoleDialog(row)
    },
    onLeftCurrentChange(currentRow, oldCurrentRow) {
      if(currentRow !== oldCurrentRow) {
        this.currentCompany = currentRow
        this.queryModel['company'] = currentRow
        this.onSearch()
      }
    },

    handleSetPermission(index, row) {
      this.$refs.rolePermission.openSetPermissionDialog(row)
    }
  },
  watch: {
    roleList(val){
      if(val){
        this.$nextTick(() => {
            if (this.$refs.mutipleTable) {
              this.$refs.mutipleTable.doLayout();
            }
        });
      }
    }
  },
  updated(){
    if(this.$refs.mutipleTable){
      this.$nextTick(() => {
            this.$refs.mutipleTable.doLayout();
        });
    }
  },
  mounted() {
    this.pageInit()
  }
}
</script>
<style lang="scss" scoped>
.page-container{
  padding: 0;
}
  ::v-deep.el-table{
    .el-table__fixed-body-wrapper{
      top: 47px !important;
    }
  }
  ::v-deep .el-table__fixed-right-patch{
    width:5px !important
  }
  ::v-deep .el-table colgroup col[name='gutter']{
    width:5px !important
  }
  ::v-deep .el-table__body{
    width:100% !important
  }
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
