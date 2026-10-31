<template>
  <el-row v-loading='loading'>
    <!-- Lịch sử  -->
    <History :bussObject='curentRow' ></History>
    <!-- Cửa sổ chỉnh sửa  -->
    <genScheme-form ref='genSchemeForm' :permission='permission' @save-finished='loadData'></genScheme-form>
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
      <!--  Thanh tìm kiếm  Kết thúc -->
      <!-- Thanh công cụ Bắt đầu -->
      <div class="page-container-header-end">
        <div>
          <el-button v-show='permission.add' type='primary' icon='el-icon-plus'  @click='onCreateEntity("genSchemeForm")'>Thêm</el-button>
        </div>
      </div>
      <!-- Thanh công cụ Kết thúc -->
      <!-- Thanh bảng  Bắt đầu -->
      <el-row>
        <el-col :span='24'>
          <div @mouseleave='moveTableOutside'>
            <el-table class='drag_table' :data='genSchemeList' border @sort-change='onSortChange' @header-dragend='onChangeWidth' :cell-class-name='cellClassName' :header-cell-class-name='headerCellClassName' highlight-current-row>
              <el-table-column v-for="(cv, index) in columnViews" v-if='cv.display' :prop='cv.prop' :key="`columnViews_${index}`" :label='cv.label' sortable='custom' :align='cv.align' :min-width='cv.miniWidth+"px"' :width='cv.width+"px"' header-align='center' :column-key='index.toString()' :render-header="renderHeader">
                <template slot-scope='{row}'>
                  <span v-if='columnViews[index].showType === "Switch" || columnViews[index].showType === "Checkbox" || columnViews[index].showType === "Radio"'>
                    <li v-if='getAttrValue(row, columnViews[index].prop) === "1"' class='el-icon-check' style='color:#F56C6C;'></li>
                  </span>
                  <span v-else>{{ getAttrValue(row, columnViews[index].prop, columnViews[index].javaType)}}</span>
                </template>
              </el-table-column>
              <!--Nút thao tác theo dòng-->
              <el-table-column label='Thao tác' header-align='center' :width='oprColumnWidth + "px"'>
                <template slot='header'>
                  <span>Thao tác</span>
                  <view-columns-select v-model='columnViews' v-on:save-column-view='saveColumn' v-on:show-all-column='showAllColumn' v-on:show-default-column='showDefaultColumn'></view-columns-select>
                  <export-excel-button v-show='permission.export' :data='genSchemeList' :tHeader='getHeads()' :filterVal='getFilterVal()' :plain='true'></export-excel-button>
                </template>
                <template slot-scope='scope'>
                  <OperationIcon v-show='permission.view' type='info' content='Xem' placement='top-start' icon-name='el-icon-view'
                    @click='onViewEntity(scope.$index, scope.row, "genSchemeForm")'></OperationIcon>
                  <OperationIcon v-show='permission.edit' type='primary' content='Sửa' placement='top-start' icon-name='el-icon-edit'
                    @click='onEditEntity(scope.$index, scope.row, "genSchemeForm")'></OperationIcon>
                  <OperationIcon v-show='permission.add' type='primary' content='Sao chép' placement='top-start' icon-name='el-icon-document'
                    @click='onCopyEntity(scope.$index, scope.row, "genSchemeForm")'></OperationIcon>
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
            :page-sizes='[10, 20, 50, 100, genSchemeTotal]'
            :page-size='10'
            layout='total, sizes, prev, pager, next, jumper'
            :total='genSchemeTotal'>
          </el-pagination>
        </el-col>
      </el-row>
      <!-- Thanh phân trang     Kết thúc --> 
    </div>
  </el-row>
</template>

<script>
import { listGenSchemePage, getGenSchemeById, deleteGenScheme } from '@/api/gen/genScheme'
import listViewMixin from '@/mixins/listViewMixin'
import GenSchemeForm from './genSchemeForm'
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
    GenSchemeForm,
    ExportExcelButton,
    ViewColumnsSelect,
    QueryForm,
    OperationIcon,
    History
  },
  data() {
    return {
      listApi: listGenSchemePage,
      getApi: getGenSchemeById,
      deleteApi: deleteGenScheme,
      entityName: 'GenScheme',
      permissionPrefix: 'genScheme',
      queryTypes: {
        'name': 'like',
      },
      queryModel: {
        'name': '',   // Tên
      },
      genSchemeTotal: 0,
      genSchemeList: [],

      oprColumnWidth: 140,  // Độ rộng cột thao tác
      tableId: '4008',
      schemeId: '6002'
    }
  },
  methods: {
    appendSearchParams() {
      if(this.moreCodition) {
        this.search.params = this.search.params.concat(this.compositeCondition())
      } else {
        // Tham số truy vấn: Tên
        this.search.params.push({
          columnName: 'name',
          queryType: 'like',
          value: this.queryModel.name
        })
      }
      // Quyền dữ liệu: Bảng phương án mãgen_scheme
      this.pushDataPermissions(this.search.params, this.$route.meta.routerId, this.tableId)
    },
    handleListResponse(responseData) {
      this.genSchemeTotal = responseData.data.total
      this.genSchemeList = responseData.data.rows
    },
    initOptions() {
    }
  },
  watch: {
  },
  mounted() {
    this.pageInit()
  }
}
</script>
