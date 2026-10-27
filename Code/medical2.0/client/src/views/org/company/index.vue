<template>
  <el-row v-loading='loading'>
    <!-- Lịch sử  -->
    <History :bussObject='curentRow' ></History>
    <!-- Cửa sổ chỉnh sửa  -->
    <company-form ref='companyForm' :permission='permission' @save-finished='loadData'></company-form>
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
              <el-form-item label='Điện thoại' prop='phoneNumber'>
                <el-input v-model='queryModel.phoneNumber' :clearable='true' placeholder='Vui lòng nhập số điện thoại'></el-input>
              </el-form-item>
            </el-col>
              <el-col :span="6">
              <el-form-item label='Phiên bản phòng khám' prop='version'>
                <el-select v-model='queryModel.version' value-key='id'  filterable clearable placeholder='Vui lòng chọn phiên bản phòng khám'>
                   <el-option v-for='version in version_List' :key='version.id' :label='version.name' :value='version'></el-option>
                </el-select>
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
            <el-button v-show='permission.add' type='primary' icon='el-icon-plus'  @click='onCreateCompany()'>Thêm</el-button>
          </div>
        </div>
        <!-- Thanh công cụ Kết thúc -->

      <!-- Thanh bảng  Bắt đầu -->
      <el-row>
        <el-col :span='24'>
          <div @mouseleave='moveTableOutside'>        
            <el-table class='drag_table' :data='companyList' row-key='id' :tree-props="{children: 'children', hasChildren: 'hasChildren'}"border @sort-change='onSortChange' @header-dragend='onChangeWidth' :cell-class-name='cellClassName' :header-cell-class-name='headerCellClassName' highlight-current-row>                
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
                  <export-excel-button v-show='permission.export' :data='companyList' :tHeader='getHeads()' :filterVal='getFilterVal()' :plain='true'></export-excel-button>
                </template>
                <template slot-scope='scope'>
                  <OperationIcon v-show='permission.view' type='info' content='Xem' placement='top-start' icon-name='el-icon-view' 
                    @click='onViewCompany(scope.$index, scope.row)'></OperationIcon>
                  <OperationIcon v-show='permission.add' type='primary' content='Thêm cấp dưới' placement='top-start' icon-name='el-icon-circle-plus-outline' 
                    @click='onCreateCompany(scope.$index, scope.row)'></OperationIcon>
                  <OperationIcon v-show='permission.edit' type='primary' content='Sửa' placement='top-start' icon-name='el-icon-edit' 
                    @click='onEditCompany(scope.$index, scope.row)'></OperationIcon>
                  <OperationIcon v-show='permission.add' type='primary' content='Sao chép' placement='top-start' icon-name='el-icon-document' 
                    @click='onCopyCompany(scope.$index, scope.row)'></OperationIcon>
                  <OperationIcon v-show='permission.remove && (!(scope.row.children) || scope.row.children.length <=0)' type='danger' content='Xóa' placement='top-start' icon-name='el-icon-delete'
                    @click='onDeleteEntity(scope.$index, scope.row, deleteCompany)'></OperationIcon>
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
import { treeCompany, getCompanyById, deleteCompany } from '@/api/org/company'
import listViewMixin from '@/mixins/listViewMixin'
import { listResourcePermission } from '@/api/resourcePermission'
import { mapPermissions } from '@/utils/searchParamsBuilder'
import CompanyForm from './companyForm'
import { listClinicVersionAll } from '@/api/clinic/clinicVersion'
import ExportExcelButton from '@/components/ExportExcelButton'
import ViewColumnsSelect from '@/views/components/ViewColumnsSelect'
import QueryForm from '@/views/components/queryForm'
import MainUI from '@/views/components/mainUI'
import OperationIcon from '@/components/OperationIcon'
import History from '@/views/components/history'
import { getCurrentCompanyId } from "@/utils/userCache";
export default {
  extends: MainUI,
  mixins: [listViewMixin],
  components: {
    CompanyForm,
    ExportExcelButton,
    ViewColumnsSelect,
    QueryForm,
    OperationIcon,
    History
  },
  data() {
    return {
      listApi: treeCompany,
      getApi: getCompanyById,
      deleteApi: deleteCompany,
      entityName: 'Company',
      permissionPrefix: 'company',
      queryTypes: {
        'name': 'like',
        'phone_number': '=',
        'version_id': '=',
      },
      queryModel: {
        'name': '',   // Tên
        'phoneNumber': '',   // Điện thoại
        'version': {     // Phiên bản phòng khám
          'id': null,
          'name': '',
        },
      },
      search: {
        params: [],
        offset: 0,
        limit: 10,
        columnName: '',
        order: ''
      },
      companyList: [],

      version_List: [],    // Phiên bản phòng khám

      oprColumnWidth: 160,  // Độ rộng cột thao tác
      tableId: '41040096140492800',
      schemeId: '41040096140492817'
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
          queryType: '=',
          value: this.queryModel.phoneNumber
        })
        this.search.params.push({
          columnName: 'version_id',
          queryType: '=',
          value: validatenull(this.queryModel.version.id) ? '' : this.queryModel.version.id
        })
      }
      this.pushDataPermissions(this.search.params, this.$route.meta.routerId, this.tableId)
    },
    handleListResponse(responseData) {
      this.companyList = responseData.data
    },
    async pageInit() {
      this.setLoad()
      try {
        this.initOptions(this.queryModel)
        this.search.params = []
        this.pushDataPermissions(this.search.params, this.$route.meta.routerId, this.tableId)
        let [listCompanyRespData, listPermissionRespData] = await Promise.all([
          treeCompany(this.search),
          listResourcePermission(this.$route.meta.routerId)
        ])
        if(listCompanyRespData.code === 100 && listPermissionRespData.code === 100) {
          this.handleListResponse(listCompanyRespData)
          this.permission = mapPermissions(listPermissionRespData.data, 'company')
        } else {
          this.showMessage(listPermissionRespData.code !== 100 ? listPermissionRespData : listCompanyRespData)
        }
        this.resetLoad()
      } catch(error) {
        this.outputError(error)
      }
    },
    onViewCompany(index, row) {
      this.setLoad()
      getCompanyById(row.id).then(responseData => {
        if(responseData.code === 100) {
          let company = responseData.data
          if(validatenull(company.parent)) {
            company.parent = {id: null}
          }
          this.$refs.companyForm.openViewCompanyDialog(company)
        } else {
          this.showMessage(responseData)
        }
        this.resetLoad()
      }).catch(error => {
        this.outputError(error)
      })
    },
    onCreateCompany(index, row) {
      this.$refs.companyForm.openAddCompanyDialog(row)
    },
    onEditCompany(index, row) {
      this.setLoad()
      getCompanyById(row.id).then(responseData => {
        if(responseData.code === 100) {
          let company = responseData.data
          if(validatenull(company.parent)) {
            company.parent = {id: null}
          }
          this.$refs.companyForm.openEditCompanyDialog(company)
        }else{
          this.showMessage(responseData)
        }
        this.resetLoad()
      }).catch(error => {
        this.outputError(error)
      })
    },
    onCopyCompany(index, row) {
      this.setLoad()
      getCompanyById(row.id).then(responseData => {
        if(responseData.code === 100) {
          let company = responseData.data
          if(validatenull(company.parent)) {
            company.parent = {id: null}
          }
          this.$refs.companyForm.openCopyCompanyDialog(company)
        } else {
          this.showMessage(responseData)
        }
        this.resetLoad()
      }).catch(error => {
        this.outputError(error)
      })
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
      version_search.params.push.apply(version_search.params, [{columnName: 'company_id', queryType: '=', value: getCurrentCompanyId()}])
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