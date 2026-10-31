<template>
  <el-row v-loading='loading'>
    <!-- Lịch sử  -->
    <History :bussObject='curentRow' ></History>
    <!-- Cửa sổ chỉnh sửa  -->
    <supplier-form ref='supplierForm' :permission='permission' @save-finished='loadData'></supplier-form>
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
              <el-col :span="6">
              <el-form-item label='Người liên hệ' prop='linkman'>
                <el-input v-model='queryModel.linkman' :clearable='true' placeholder='Vui lòng nhập người liên hệ'></el-input>
              </el-form-item>
            </el-col>
              <el-col :span="6">
              <el-form-item label='Điện thoại' prop='phone'>
                <el-input v-model='queryModel.phone' :clearable='true' placeholder='Vui lòng nhập số điện thoại'></el-input>
              </el-form-item>
            </el-col>
              <el-col :span="6">
              <el-form-item label='Địa chỉ' prop='address'>
                <el-input v-model='queryModel.address' placeholder='Vui lòng nhập địa chỉ'></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label='Loại hãng' prop='type'>
                  <el-select v-model="queryModel.type" placeholder="Vui lòng chọn loại nhà cung cấp">
                    <el-option
                      v-for="item in types"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value">
                    </el-option>
                  </el-select>
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
                    @click="onCreateEntity('supplierForm')"
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
            <el-table ref="tableRef" height="calc(100vh - 300px)" class='drag_table' :data='supplierList' border @sort-change='onSortChange' @header-dragend='onChangeWidth' :cell-class-name='cellClassName' :header-cell-class-name='headerCellClassName' highlight-current-row>                
              <el-table-column
              label="Số thứ tự"
              type="index"
              :index="indexMethod"
              align="center">
            </el-table-column>
              <el-table-column v-for="(cv, index) in columnViews" v-if='cv.display' :prop='cv.prop' :key="`columnViews_${index}`" :label='cv.label' sortable='custom' :align='cv.align' :min-width='cv.miniWidth+"px"' :width='cv.width+"px"' header-align='center' :column-key='index.toString()' :render-header="renderHeader">
                <template slot-scope='{row,$index}'>
                  <span v-if='columnViews[index].showType === "Switch" || columnViews[index].showType === "Checkbox" || columnViews[index].showType === "Radio"'>
                    <li v-if='getAttrValue(row, columnViews[index].prop) === "1"' class='el-icon-check' style='color:#F56C6C;'></li>
                  </span>
                  <span v-else-if="columnViews[index].showType === 'Radios'">
                    <span v-if='getAttrValue(row, columnViews[index].prop) === "1"'>
                      Nhà cung cấp thuốc
                    </span>
                    <span v-else-if='getAttrValue(row, columnViews[index].prop) === "2"'>
                      Nhà cung cấp vật tư
                    </span>
                  </span>
                  <span v-else>{{ getAttrValue(row, columnViews[index].prop, columnViews[index].javaType )}}</span>
                </template>
              </el-table-column>
              <!--Nút thao tác theo dòng-->
              <el-table-column label='Thao tác' header-align='center' :width='140 + "px"' :key="'operate'">        
                <template slot='header' slot-scope="scope">
                  <span>Thao tác</span>
                  <view-columns-select v-model='columnViews' v-on:save-column-view='saveColumn' v-on:show-all-column='showAllColumn' v-on:show-default-column='showDefaultColumn'></view-columns-select>
                  <export-excel-button v-show='permission.export' :data='supplierList' :tHeader='getHeads()' :filterVal='getFilterVal()' :plain='true'></export-excel-button>
                </template>
                <template slot-scope='scope'>
                  <OperationIcon v-show='permission.view' type='info' content='Xem' placement='top-start' icon-name='el-icon-view' 
                    @click='onViewEntity(scope.$index, scope.row, "supplierForm")'></OperationIcon>
                  <OperationIcon v-show='permission.edit' type='primary' content='Sửa' placement='top-start' icon-name='el-icon-edit' 
                    @click='onEditEntity(scope.$index, scope.row, "supplierForm")'></OperationIcon>
                  <OperationIcon v-show='permission.add' type='primary' content='Sao chép' placement='top-start' icon-name='el-icon-document' 
                    @click='onCopyEntity(scope.$index, scope.row, "supplierForm")'></OperationIcon>
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
            :page-sizes='[20, 50, 100, supplierTotal]'
            :page-size='20'
            layout='total, sizes, prev, pager, next, jumper'
            :total='supplierTotal'>
          </el-pagination>
        </el-col>
      </el-row>
      <!-- Thanh phân trang     Kết thúc -->     </el-card>
  </el-row>
</template>

<script>
import { listSupplierPage, getSupplierById, deleteSupplier } from '@/api/stock/supplier'
import listViewMixin from '@/mixins/listViewMixin'
import SupplierForm from './supplierForm'
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
    SupplierForm,
    ExportExcelButton,
    ViewColumnsSelect,
    QueryForm,
    OperationIcon,
    History
  },
  data() {
    return {
      // listViewMixin Cấu hình
      listApi: listSupplierPage,
      getApi: getSupplierById,
      deleteApi: deleteSupplier,
      entityName: 'Supplier',
      permissionPrefix: 'supplier',

      queryTypes: {
        'name': 'like',
        'linkman': 'like',
        'phone': 'like',
        'address': 'like',
      },
      types:[
        {
          value: '1',
          label: 'Nhà cung cấp thuốc'
        },
        {
          value: '2',
          label: 'Nhà cung cấp vật tư'
        },
      ],//Loại nhà cung cấp
      queryModel: {
        'name': '',   // Tên
        'linkman': '',   // Người liên hệ
        'phone': '',   // Điện thoại
        'address': '',   // Địa chỉ
      },
      supplierTotal: 0,
      supplierList: [],

      oprColumnWidth: 140,  // Độ rộng cột thao tác
      tableId: '1005526731044757538',
      schemeId: '1005526731044757555'
    }
  },
  methods: {
    appendSearchParams() {
      this.search.params = [{columnName: 'company_id', queryType: '=', value: currentUser.company.id}]
      if(this.moreCodition) {
        this.search.params = this.search.params.concat(this.compositeCondition())
      } else {
        this.search.params.push({columnName: 'name', queryType: 'like', value: this.queryModel.name})
        this.search.params.push({columnName: 'linkman', queryType: 'like', value: this.queryModel.linkman})
        this.search.params.push({columnName: 'phone', queryType: 'like', value: this.queryModel.phone})
        this.search.params.push({columnName: 'address', queryType: 'like', value: this.queryModel.address})
        this.search.params.push({columnName: 'type', queryType: '=', value: this.queryModel.type})
      }
      this.pushDataPermissions(this.search.params, this.$route.meta.routerId, this.tableId)
    },
    handleListResponse(responseData) {
      this.supplierTotal = responseData.data.total
      this.supplierList = responseData.data.rows
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
                  this.$refs.tableRef.doLayout()// Đối với Table Thực hiện bố cục lại
              })
          },
          deep: true
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

</style>