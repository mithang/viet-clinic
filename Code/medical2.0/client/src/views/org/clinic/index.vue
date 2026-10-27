<template>
  <el-row v-loading='loading'>
    <!-- Lịch sử  -->
    <History :bussObject='curentRow' ></History>
    <!-- Cửa sổ chỉnh sửa  -->
    <clinic-form ref='clinicForm' :permission='permission' @save-finished='loadData'></clinic-form>
    <div class="page-container">
        <!--  Thanh tìm kiếm  Bắt đầu -->
        <div class='query-form-container'>
          <el-row v-if='!moreCodition' class='search-row'>
            <el-form :model='queryModel' @submit.native.prevent label-position="left" label-width='70px' ref='queryForm' :inline-message='true'>
                      <el-col :span="6">
              <el-form-item label='Tên phòng khám' prop='name'>
                <el-input v-model='queryModel.name' :clearable='true' placeholder='Vui lòng nhập tên phòng khám'></el-input>
              </el-form-item>
            </el-col>
              <el-col :span="6">
              <el-form-item label='Điện thoại' prop='phoneNumber'>
                <el-input v-model='queryModel.phoneNumber' :clearable='true' placeholder='Vui lòng nhập số điện thoại'></el-input>
              </el-form-item>
            </el-col>
              <el-col :span="6">
              <el-form-item label='Phiên bản sản phẩm' prop='version'>
                <el-select v-model='queryModel.version' value-key='id'  filterable clearable placeholder='Vui lòng chọn phiên bản sản phẩm'>
                   <el-option v-for='version in version_List' :key='version.id' :label='version.name' :value='version'></el-option>
                </el-select>
              </el-form-item>
            </el-col>
              <el-col :span="6">
              <el-form-item label='Trạng thái' prop='status'>
                <el-switch v-model='queryModel.status' active-color='#13ce66' inactive-color='#dbdfe6' :active-value='1' :inactive-value='0'></el-switch>
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
            <el-button v-show='permission.add' type='primary' icon='el-icon-plus'  @click='onCreateEntity("clinicForm")'>Thêm</el-button>
          </div>
        </div>
        <!-- Thanh công cụ Kết thúc -->

      <!-- Thanh bảng  Bắt đầu -->
      <el-row>
        <el-col :span='24'>
          <div @mouseleave='moveTableOutside'>        
            <el-table class='drag_table' :data='clinicList' border @sort-change='onSortChange' @header-dragend='onChangeWidth' :cell-class-name='cellClassName' :header-cell-class-name='headerCellClassName' highlight-current-row>                
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
                  <export-excel-button v-show='permission.export' :data='clinicList' :tHeader='getHeads()' :filterVal='getFilterVal()' :plain='true'></export-excel-button>
                </template>
                <template slot-scope='scope'>
                  <OperationIcon v-show='permission.view' type='info' content='Xem' placement='top-start' icon-name='el-icon-view'
                    @click='onViewEntity(scope.$index, scope.row, "clinicForm")'></OperationIcon>
                  <OperationIcon v-show='permission.edit' type='primary' content='Sửa' placement='top-start' icon-name='el-icon-edit'
                    @click='onEditEntity(scope.$index, scope.row, "clinicForm")'></OperationIcon>
                  <OperationIcon v-show='permission.add' type='primary' content='Sao chép' placement='top-start' icon-name='el-icon-document'
                    @click='onCopyEntity(scope.$index, scope.row, "clinicForm")'></OperationIcon>
                  <OperationIcon v-show='permission.remove' type='danger' content='Xóa' placement='top-start' icon-name='el-icon-delete'
                    @click='onDeleteEntity(scope.$index, scope.row, deleteClinic)'></OperationIcon>
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
            :page-sizes='[10, 20, 50, 100, clinicTotal]'
            :page-size='10'
            layout='total, sizes, prev, pager, next, jumper'
            :total='clinicTotal'>
          </el-pagination>
        </el-col>
      </el-row>
      <!-- Thanh phân trang     Kết thúc -->     </div>
  </el-row>
</template>

<script>
import { validatenull } from '@/utils/validate'
import { listClinicPage, getClinicById, deleteClinic } from '@/api/org/clinic'
import listViewMixin from '@/mixins/listViewMixin'
import { listResourcePermission } from '@/api/resourcePermission'
import { mapPermissions } from '@/utils/searchParamsBuilder'
import ClinicForm from './clinicForm'
import { listClinicVersionAll } from '@/api/clinic/clinicVersion'
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
    ClinicForm,
    ExportExcelButton,
    ViewColumnsSelect,
    QueryForm,
    OperationIcon,
    History
  },
  data() {
    return {
      listApi: listClinicPage,
      getApi: getClinicById,
      deleteApi: deleteClinic,
      entityName: 'Clinic',
      permissionPrefix: 'clinic',
      queryTypes: {
        'name': 'like',
        'lessee_id': '=',
        'phone_number': 'like',
        'version_id': '=',
        'status': '=',
      },
      queryModel: {
        'name': '',   // Tên phòng khám
        'lesseeId': {     // Bảng cha Đơn vị thuê
          'id': null,
          'userName': '',
        },
        'phoneNumber': '',   // Điện thoại
        'version': {     // Phiên bản sản phẩm
          'id': null,
          'name': '',
        },
        'status': 1
      },
      search: {
        params: [],
        offset: 0,
        limit: 10,
        columnName: '',
        order: ''
      },
      clinicTotal: 0,
      clinicList: [],

      version_List: [],    // Phiên bản sản phẩm

      oprColumnWidth: 140,  // Độ rộng cột thao tác
      tableId: '41040096140492800',
      schemeId: '986498806151577787'
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
        this.search.params.push({
          columnName: 'phone_number',
          queryType: 'like',
          value: this.queryModel.phoneNumber
        })
        this.search.params.push({
          columnName: 'version_id',
          queryType: '=',
          value: validatenull(this.queryModel.version.id) ? '' : this.queryModel.version.id
        })
        this.search.params.push({
          columnName: 'status',
          queryType: '=',
          value: this.queryModel.status
        })
      }
      this.pushDataPermissions(this.search.params, this.$route.meta.routerId, this.tableId)
    },
    handleListResponse(responseData) {
      this.clinicTotal = responseData.data.total
      this.clinicList = responseData.data.rows
    },
    async pageInit() {
      this.setLoad()
      try {
        this.initOptions(this.queryModel)
        this.search.params = []
        this.pushDataPermissions(this.search.params, this.$route.meta.routerId, this.tableId)
        let [listClinicRespData, listPermissionRespData] = await Promise.all([
          listClinicPage(this.search),
          listResourcePermission(this.$route.meta.routerId)
        ])
        if(listClinicRespData.code === 100 && listPermissionRespData.code === 100) {
          this.handleListResponse(listClinicRespData)
          this.permission = mapPermissions(listPermissionRespData.data, 'clinic')
        } else {
          this.showMessage(listPermissionRespData.code !== 100 ? listPermissionRespData : listClinicRespData)
        }
        this.resetLoad()
      } catch(error) {
        this.outputError(error)
      }
    },
    initOptions(This) {
      let version_search = {
        params: []
      }
      version_search.params.forEach(item => {
        if(this.queryTypes[item.columnName]) {
          item.queryType = this.queryTypes[item.columnName]
        }
      })
      this.pushDataPermissions(version_search.params, this.$route.meta.routerId, '987744398207139863')
      this.version_List.splice(0, this.version_List.length)
      listClinicVersionAll(version_search).then(responseData => {
        this.version_List = responseData.data
      })
    }
  },
  watch: {
  },
  mounted() {
    this.pageInit()
  }
}
</script>