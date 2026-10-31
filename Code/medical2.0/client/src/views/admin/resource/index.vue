<template>
  <el-row v-loading='loading'>
    <el-container>
      <!-- Lịch sử  -->
      <History :bussObject='curentRow' ></History>
      <!-- Cửa sổ chỉnh sửa  -->
      <resource-form ref='resourceForm' :permission='permission' @save-finished='loadData'></resource-form>
      <div class="page-left-container">
        <el-aside>
          <el-table ref='treeTable' :data='routers' row-key='id' :tree-props="{children: 'children', hasChildren: 'hasChildren'}" highlight-current-row :cell-style="function() {return {borderBottom: 'none'} }" @current-change='onLeftCurrentChange'>
            <el-table-column label='Định tuyến' prop='name'></el-table-column>
          </el-table>
        </el-aside>
      </div>
      <div class="page-container">
      <!--  Thanh tìm kiếm  Bắt đầu -->
      <div class='query-form-container'>
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
          <QueryForm v-else v-model='moreParm' :tableId='tableId' :schemeId='schemeId' :curNode='currentRouter' exclude='router_id' :routerId='$route.meta.routerId' @search='onSearch()' @moreCodition='onMoreCodition()'></QueryForm>
      </div>
      <!--  Thanh tìm kiếm  Kết thúc -->
      <!-- Thanh công cụ Bắt đầu -->
      <div class="page-container-header-end">
        <div>
          <el-button v-show='permission.add' :plain='true' @click='onFastCreateResourc()'>Thêm nhanh</el-button>
          <el-button v-show='permission.add' type='primary' icon='el-icon-plus'  @click='onCreateResource()'>Thêm</el-button>
        </div>
      </div>
      <!-- Thanh công cụ Kết thúc -->
      <!-- Thanh bảng  Bắt đầu -->
      <el-row>
        <el-col :span='24'>
          <div @mouseleave='moveTableOutside'>        
            <el-table class='drag_table' :data='resourceList' border @sort-change='onSortChange' @header-dragend='onChangeWidth' :cell-class-name='cellClassName' :header-cell-class-name='headerCellClassName' highlight-current-row>                
              <el-table-column v-for="(cv, index) in columnViews" v-if='cv.display' :prop='cv.prop' :key="`columnViews_${index}`" :label='cv.label' sortable='custom' :align='cv.align' :min-width='cv.miniWidth+"px"' :width='cv.width+"px"' header-align='center' :column-key='index.toString()' :render-header="renderHeader">
                <template slot-scope='{row}'>
                  <span v-if='columnViews[index].showType === "Switch" || columnViews[index].showType === "Checkbox" || columnViews[index].showType === "Radio"'>
                    <li v-if='getAttrValue(row, columnViews[index].prop) === "1"' class='el-icon-check' style='color:#F56C6C;'></li>
                  </span>
                  <span v-else>{{ getAttrValue(row, columnViews[index].prop, columnViews[index].javaType )}}</span>
                </template>
              </el-table-column>
              <!--Nút thao tác theo dòng-->
              <el-table-column label='Thao tác' header-align='center' :width='oprColumnWidth + "px"'>        
                <template slot='header'>
                  <span>Thao tác</span>
                  <view-columns-select v-model='columnViews' v-on:save-column-view='saveColumn' v-on:show-all-column='showAllColumn' v-on:show-default-column='showDefaultColumn'></view-columns-select>
                  <export-excel-button v-show='permission.export' :data='resourceList' :tHeader='getHeads()' :filterVal='getFilterVal()' :plain='true'></export-excel-button>
                </template>
                <template slot-scope='scope'>
                  <OperationIcon v-show='permission.view' type='info' content='Xem' placement='top-start' icon-name='el-icon-view' 
                    @click='onViewEntity(scope.$index, scope.row, "resourceForm")'></OperationIcon>
                  <OperationIcon v-show='permission.edit' type='primary' content='Sửa' placement='top-start' icon-name='el-icon-edit' 
                    @click='onEditEntity(scope.$index, scope.row, "resourceForm")'></OperationIcon>
                  <OperationIcon v-show='permission.add' type='primary' content='Sao chép' placement='top-start' icon-name='el-icon-document' 
                    @click='onCopyEntity(scope.$index, scope.row, "resourceForm")'></OperationIcon>
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
            :page-sizes='[10, 20, 50, 100, resourceTotal]'
            :page-size='10'
            layout='total, sizes, prev, pager, next, jumper'
            :total='resourceTotal'>
          </el-pagination>
        </el-col>
      </el-row>
      <!-- Thanh phân trang     Kết thúc --> 
      </div>
    </el-container>
  </el-row>
</template>

<script>
import { validatenull } from '@/utils/validate'
import { listResourcePage, getResourceById, deleteResource,saveResource } from '@/api/admin/resource'
import { listResourcePermission } from '@/api/admin/common/permission'
import listViewMixin from '@/mixins/listViewMixin'
import ResourceForm from './resourceForm'
import { treeRouter } from '@/api/admin/router'

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
    ResourceForm,
    ExportExcelButton,
    ViewColumnsSelect,
    QueryForm,
    OperationIcon,
    History
  },
  data() {
    return {
      listApi: listResourcePage,
      getApi: getResourceById,
      deleteApi: deleteResource,
      entityName: 'Resource',
      permissionPrefix: 'resource',
      queryTypes: {
        'name': 'like',
    	'router_id': '=',
      },
      queryModel:  {
        'name': '',   // Tên
        'router': {     // Bảng cha Định tuyến
          'id': null,
          'name': '',
        },
      },
      search: {
        limit: 10
      },
      currentRouter: {},     //Route được chọn trong cấu trúc cây
      resourceTotal: 0,
      resourceList: [],     // Dữ liệu bảng số
      routers: [],           // Bảng cây route
    
      oprColumnWidth: 140,  // Độ rộng cột thao tác
      resourceDefs: [{name: 'Truy vấn', permission: 'list'}, {name: 'Xem', permission: 'read'}, {name: 'Xuất', permission: 'export'}, {name: 'Thêm', permission: 'create'}, {name: 'Xóa', permission: 'delete'}, {name: 'Cập nhật', permission: 'update'}],
      tableId: '4006',
      schemeId: '6008'
    }
  },
  methods: {
    async pageInit() {
      this.setLoad()
      try {
        let params = []
        params.push.apply(params, [{columnName: 'is_locked', queryType: '=', value: '0'}])
        this.pushDataPermissions(params, this.$route.meta.routerId, '4003')
        let [treeRouterRespData, listPermissionRespData] = await Promise.all([
          treeRouter({params: params, columnName: '', order: ''}),
          listResourcePermission(this.$route.meta.routerId)
        ])
        if(treeRouterRespData.code === 100 && listPermissionRespData.code === 100) {
          this.routers = treeRouterRespData.data
          this.$nextTick(() => {
            if(this.routers && this.routers.length > 0 && this.$refs.treeTable){
              this.$refs.treeTable.setCurrentRow(this.routers[0])
            }
          })
          this.permission.view = listPermissionRespData.data.find(item => item.permission === 'resource:read')
          this.permission.export = listPermissionRespData.data.find(item => item.permission === 'resource:export')
          this.permission.add = listPermissionRespData.data.find(item => item.permission === 'resource:create')
          this.permission.edit = listPermissionRespData.data.find(item => item.permission === 'resource:update')
          this.permission.remove = listPermissionRespData.data.find(item => item.permission === 'resource:delete')
        } else {
          this.showMessage(listPermissionRespData.code !== 100 ? listPermissionRespData : treeRouterRespData)
        }
        this.resetLoad()
      } catch(error) {
        this.outputError(error)
      }
    },
    appendSearchParams() {
      if(validatenull(this.currentRouter)) {
        this.$alert('Vui lòng chọn tuyến đường', 'Nhắc nhở', {
          confirmButtonText: 'Xác nhận',
          type: 'info'
        })
        return
      }
      this.search.params.push({
        columnName: 'router_id',
        queryType: '=',
        value: this.currentRouter.id
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
      this.resourceTotal = responseData.data.total
      this.resourceList = responseData.data.rows
    },

    onFastCreateResourc() {
      if(validatenull(this.currentRouter)) {
        this.$alert('Vui lòng chọn tuyến đường', 'Nhắc nhở', {
          confirmButtonText: 'Xác nhận',
          type: 'info'
        })
        return
      }
      for(let item in this.resourceDefs) {
        let resource = {
          'name': this.resourceDefs[item].name + this.currentRouter.name,   // Tên
          'code': this.currentRouter.code,   // Mã nguồn
          'url': this.currentRouter.url,   // Url
          'permission': this.currentRouter.code.substr(this.currentRouter.code.lastIndexOf('/') + 1) + ':' + this.resourceDefs[item].permission,   // Quyền
          'canPermission': '1', // Có thể cấp quyền
          'isDefault': 0,  // Có phải tài nguyên mặc định không
          'isLocked': 0,    // Vô hiệu hóa
          'router': {     // Bảng cha Định tuyến
            'id': this.currentRouter.id,
            'name': this.currentRouter.name,
          },
          'remarks': '',   // Thông tin ghi chú
        }

        this.setLoad()
        saveResource(resource).then(responseData => {
          this.resetLoad()
          if(this.loadcount === 0) {
            this.getResourceList()
          }
        }).catch(error => {
          this.outputError(error)
          if(this.loadcount === 0) {
            this.getResourceList()
          }
        })
      }
    },
    onCreateResource() {
      if(validatenull(this.currentRouter)) {
        this.$alert('Vui lòng chọn tuyến đường', 'Nhắc nhở', {
          confirmButtonText: 'Xác nhận',
          type: 'info'
        })
        return
      } 
      let row={
          'router': this.currentRouter
        }
      this.$refs.resourceForm.openAddResourceDialog(row)
    },
    onLeftCurrentChange(currentRow, oldCurrentRow) {
      if(currentRow !== oldCurrentRow) {
        this.currentRouter = currentRow
        this.queryModel['router'] = currentRow
        this.initOptions(this.queryModel)
        this.onSearch()
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
