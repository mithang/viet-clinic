<template>
  <el-row v-loading='loading'>
    <!-- Lịch sử  -->
    <History :bussObject='curentRow' ></History>
    <!-- Cửa sổ chỉnh sửa  -->
    <noticeSend-form ref='noticeSendForm' :permission='permission' @save-finished='loadData'></noticeSend-form>
    <el-col :span='24'>
      <!--  Thanh tìm kiếm  Bắt đầu -->
      <el-container class='query-form-container'>
        <el-main>
          <el-row v-if='!moreCodition' class='search-row'>
            <el-form :model='queryModel' @submit.native.prevent label-position="right" label-width='100px' ref='queryForm' :inline-message='true'>
                  <el-col :span="6">
              <el-form-item label='Tiêu đề' prop='title'>
                <el-input v-model='queryModel.title' :clearable='true' placeholder='Vui lòng nhập tiêu đề'></el-input>
              </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label-width="20px">
              <el-button type='primary' icon='el-icon-search' @click='onSearch()' :plain='true'>Tìm kiếm</el-button>
              <el-button type='info' icon='el-icon-refresh-left' @click='$refs.queryForm.resetFields()' :plain='true'>Đặt lại</el-button>
              <el-button type='info' icon='el-icon-d-arrow-right' @click='onMoreCodition()' :plain='true'>Thêm</el-button>
                </el-form-item>
              </el-col>
            </el-form>
          </el-row>
          <QueryForm v-else v-model='moreParm' :tableId='tableId' :schemeId='schemeId'  :routerId='$route.meta.routerId' @search='onSearch()' @moreCodition='onMoreCodition()'></QueryForm>
        </el-main>
        <el-aside align="right" width="90px">
          <el-button v-show='permission.add' type='primary' icon='el-icon-plus'  @click='onCreateEntity("noticeSendForm")'>Thêm</el-button>
        </el-aside>
      </el-container>
      <!--  Thanh tìm kiếm  Kết thúc -->
      <el-container class="data-container">
        <el-main>
      <!-- Thanh bảng  Bắt đầu -->
      <el-row>
        <el-col :span='24'>
          <div @mouseleave='moveTableOutside'>        
            <el-table class='drag_table' :data='noticeSendList' border @sort-change='onSortChange' @header-dragend='onChangeWidth' :cell-class-name='cellClassName' :header-cell-class-name='headerCellClassName' highlight-current-row>                
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
                  <export-excel-button v-show='permission.export' :data='noticeSendList' :tHeader='getHeads()' :filterVal='getFilterVal()' :plain='true'></export-excel-button>
                </template>
                <template slot-scope='scope'>
                  <OperationIcon v-show='permission.view' type='info' content='Xem' placement='top-start' icon-name='el-icon-view'
                    @click='onViewEntity(scope.$index, scope.row, "noticeSendForm")'></OperationIcon>
                  <OperationIcon v-show='permission.edit' type='primary' content='Sửa' placement='top-start' icon-name='el-icon-edit'
                    @click='onEditEntity(scope.$index, scope.row, "noticeSendForm")'></OperationIcon>
                  <OperationIcon v-show='permission.add' type='primary' content='Sao chép' placement='top-start' icon-name='el-icon-document'
                    @click='onCopyEntity(scope.$index, scope.row, "noticeSendForm")'></OperationIcon>
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
            :page-sizes='[10, 20, 50, 100, noticeSendTotal]'
            :page-size='10'
            layout='total, sizes, prev, pager, next, jumper'
            :total='noticeSendTotal'>
          </el-pagination>
        </el-col>
      </el-row>
      <!-- Thanh phân trang     Kết thúc -->       	</el-main>
      </el-container>
    </el-col>
  </el-row>
</template>

<script>
import { listNoticeSendPage, getNoticeSendById, deleteNoticeSend } from '@/api/noticesend/noticeSend'
import listViewMixin from '@/mixins/listViewMixin'
import NoticeSendForm from './noticeSendForm'
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
    NoticeSendForm,
    ExportExcelButton,
    ViewColumnsSelect,
    QueryForm,
    OperationIcon,
    History
  },
  data() {
    return {
      listApi: listNoticeSendPage,
      getApi: getNoticeSendById,
      deleteApi: deleteNoticeSend,
      entityName: 'NoticeSend',
      permissionPrefix: 'noticeSend',
      queryTypes: {
        'title': 'like',
      },
      queryModel: {
        'title': '',   // Tiêu đề
      },
      noticeSendTotal: 0,
      noticeSendList: [],

      oprColumnWidth: 140,  // Độ rộng cột thao tác
      tableId: '723817798886907905',
      schemeId: '723817798886907925'
    }
  },
  methods: {
    appendSearchParams() {
      if(this.moreCodition) {
        this.search.params = this.search.params.concat(this.compositeCondition())
      } else {
        // Tham số truy vấn: Tiêu đề
        this.search.params.push({
          columnName: 'title',
          queryType: 'like',
          value: this.queryModel.title
        })
      }
      // Quyền dữ liệu: Bản ghi gửi thông báo notice_send
      this.pushDataPermissions(this.search.params, this.$route.meta.routerId, this.tableId)
    },
    handleListResponse(responseData) {
      this.noticeSendTotal = responseData.data.total
      this.noticeSendList = responseData.data.rows
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
