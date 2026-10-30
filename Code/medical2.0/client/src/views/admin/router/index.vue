<template>
  <el-row v-loading='loading'>
    <!-- Lịch sử  -->
    <History :bussObject='curentRow' ></History>
    <!-- Cửa sổ chỉnh sửa  -->
    <router-form ref='routerForm' :permission='permission' @save-finished='loadData'></router-form>
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
          <QueryForm v-else v-model='moreParm' :tableId='tableId' :schemeId='schemeId'  :routerId='$route.meta.routerId' @search='onSearch()' @moreCodition='onMoreCodition()'></QueryForm>
      </div>
      <!-- Thanh công cụ Bắt đầu -->
      <div class="page-container-header-end">
        <div>
          <el-button v-show='permission.add' type='primary' icon='el-icon-plus'  @click='onCreateRouter()'>Thêm</el-button>
        </div>
      </div>
      <!-- Thanh công cụ Kết thúc -->

      <!--  Thanh tìm kiếm  Kết thúc -->
      <!-- Thanh bảng  Bắt đầu -->
      <el-row>
        <el-col :span='24'>
          <div @mouseleave='moveTableOutside'>        
            <el-table class='drag_table' :data='routerList' row-key='id' :tree-props="{children: 'children', hasChildren: 'hasChildren'}" border @sort-change='onSortChange' @header-dragend='onChangeWidth' :cell-class-name='cellClassName' :header-cell-class-name='headerCellClassName' highlight-current-row>                
              <el-table-column v-for="(cv, index) in columnViews" v-if='cv.display' :prop='cv.prop' :key="`columnViews_${index}`" :label='cv.label' sortable='custom' :align='cv.align' :min-width='cv.miniWidth+"px"' :width='cv.width+"px"' header-align='center' :column-key='index.toString()' :render-header="renderHeader">
                <template slot-scope='{row,$index}'>
                  <span v-if='columnViews[index].showType === "Switch" || columnViews[index].showType === "Checkbox" || columnViews[index].showType === "Radio"'>
                    <li v-if='getAttrValue(row, columnViews[index].prop) === "1"' class='el-icon-check' style='color:#F56C6C;'></li>
                  </span>
                  <span v-else>{{ getAttrValue(row, columnViews[index].prop, columnViews[index].javaType )}}</span>
                </template>
              </el-table-column>
              <!--Nút thao tác theo dòng-->
              <el-table-column label='Thao tác' header-align='center' :width='oprColumnWidth + "px"'>        
                <template slot='header' slot-scope="scope">
                  <span>Thao tác</span>
                  <view-columns-select v-model='columnViews' v-on:save-column-view='saveColumn' v-on:show-all-column='showAllColumn' v-on:show-default-column='showDefaultColumn'></view-columns-select>
                  <export-excel-button v-show='permission.export' :data='routerList' :tHeader='getHeads()' :filterVal='getFilterVal()' :plain='true'></export-excel-button>
                </template>
                <template slot-scope='scope'>
                  <OperationIcon v-show='permission.view' type='info' content='Xem' placement='top-start' icon-name='el-icon-view' 
                    @click='onViewRouter(scope.$index, scope.row)'></OperationIcon>
                  <OperationIcon v-show='permission.add' type='primary' content='Thêm cấp dưới' placement='top-start' icon-name='el-icon-circle-plus-outline' 
                    @click='onCreateRouter(scope.$index, scope.row)'></OperationIcon>
                  <OperationIcon v-show='permission.edit' type='primary' content='Sửa' placement='top-start' icon-name='el-icon-edit' 
                    @click='onEditRouter(scope.$index, scope.row)'></OperationIcon>
                  <OperationIcon v-show='permission.add' type='primary' content='Sao chép' placement='top-start' icon-name='el-icon-document' 
                    @click='onCopyRouter(scope.$index, scope.row)'></OperationIcon>
                  <OperationIcon v-show='permission.remove && (!(scope.row.children) || scope.row.children.length <=0)' type='danger' content='Xóa' placement='top-start' icon-name='el-icon-delete'
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
    </div>
  </el-row>
</template>

<script>
import { validatenull } from '@/utils/validate'
import { treeRouter, getRouterById, deleteRouter } from '@/api/admin/router'
import listViewMixin from '@/mixins/listViewMixin'
import { listResourcePermission } from '@/api/resourcePermission'
import { mapPermissions } from '@/utils/searchParamsBuilder'
import RouterForm from './routerForm'
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
    RouterForm,
    ExportExcelButton,
    ViewColumnsSelect,
    QueryForm,
    OperationIcon,
    History
  },
  data() {
    return {
      listApi: treeRouter,
      getApi: getRouterById,
      deleteApi: deleteRouter,
      entityName: 'Router',
      permissionPrefix: 'router',
      queryTypes: {
        'name': 'like',
      },
      queryModel: {
        'name': '',   // Tên
      },
      search: {
        params: [],
        offset: 0,
        limit: 10,
        columnName: '',
        order: ''
      },
      routerList: [],

      oprColumnWidth: 165,  // Độ rộng cột thao tác
      tableId: '4003',
      schemeId: '6009'
    }
  },
  methods: {
    appendSearchParams() {
      if(this.moreCodition) {
        this.search.params = this.search.params.concat(this.compositeCondition())
      } else {
        this.search.params.push({
          columnName: 'name',
          queryType: 'like',
          value: this.queryModel.name
        })
      }
      this.pushDataPermissions(this.search.params, this.$route.meta.routerId, this.tableId)
    },
    handleListResponse(responseData) {
      this.routerList = responseData.data
    },
    async pageInit() {
      this.setLoad()
      try {
        this.initOptions(this.queryModel)
        this.search.params = []
        this.pushDataPermissions(this.search.params, this.$route.meta.routerId, this.tableId)
        let [listRouterRespData, listPermissionRespData] = await Promise.all([
          treeRouter(this.search),
          listResourcePermission(this.$route.meta.routerId)
        ])
        if(listRouterRespData.code === 100 && listPermissionRespData.code === 100) {
          this.handleListResponse(listRouterRespData)
          this.permission = mapPermissions(listPermissionRespData.data, 'router')
        } else {
          this.showMessage(listPermissionRespData.code !== 100 ? listPermissionRespData : listRouterRespData)
        }
        this.resetLoad()
      } catch(error) {
        this.outputError(error)
      }
    },
    onViewRouter(index, row) {
      this.setLoad()
      getRouterById(row.id).then(responseData => {
        if(responseData.code === 100) {
          let router = responseData.data
          if(validatenull(router.parent)) {
            router.parent = {id: null}
          }
          this.$refs.routerForm.openViewRouterDialog(router)
        } else {
          this.showMessage(responseData)
        }
        this.resetLoad()
      }).catch(error => {
        this.outputError(error)
      })
    },
    onCreateRouter(index, row) {
      this.$refs.routerForm.openAddRouterDialog(row)
    },
    onEditRouter(index, row) {
      this.setLoad()
      getRouterById(row.id).then(responseData => {
        if(responseData.code === 100) {
          let router = responseData.data
          if(validatenull(router.parent)) {
            router.parent = {id: null}
          }
          this.$refs.routerForm.openEditRouterDialog(router)
        }else{
          this.showMessage(responseData)
        }
        this.resetLoad()
      }).catch(error => {
        this.outputError(error)
      })
    },
    onCopyRouter(index, row) {
      this.setLoad()
      getRouterById(row.id).then(responseData => {
        if(responseData.code === 100) {
          let router = responseData.data
          if(validatenull(router.parent)) {
            router.parent = {id: null}
          }
          this.$refs.routerForm.openCopyRouterDialog(router)
        } else {
          this.showMessage(responseData)
        }
        this.resetLoad()
      }).catch(error => {
        this.outputError(error)
      })
    },
    initOptions(This) {
    }
  },
  watch: {
  },
  mounted() {
    this.pageInit()
  }
}
</script>