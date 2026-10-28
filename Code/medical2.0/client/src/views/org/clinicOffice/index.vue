<template>
  <el-row v-loading='loading'>
    <!-- Lịch sử  -->
    <History :bussObject='curentRow' ></History>
    <!-- Cửa sổ chỉnh sửa  -->
    <clinicOffice-form ref='clinicOfficeForm' :permission='permission' @save-finished='loadData'></clinicOffice-form>
    <el-card class="page-container">
        <!--  Thanh tìm kiếm  Bắt đầu -->
        <div class='query-form-container'>
          <el-row v-if='!moreCodition' class='search-row'>
            <el-form :model='queryModel' @submit.native.prevent label-position="left" label-width='70px' ref='queryForm' :inline-message='true'>

            <el-col :span="6">
              <el-form-item label='Tên khoa/phòng' prop='name'>
                <el-input v-model='queryModel.name' :clearable='true' placeholder='Vui lòng nhập tên khoa/phòng'></el-input>
              </el-form-item>
            </el-col>
            <!-- <el-col :span="6">
              <el-form-item label='Có bị vô hiệu hóa không' prop='isLocked'>
                <el-switch v-model='queryModel.isLocked' active-color='#13ce66' inactive-color='#dbdfe6' active-value='1' inactive-value='0'></el-switch>
              </el-form-item>
            </el-col> -->
            <el-col :span="5">
              <el-form-item label='Có kích hoạt không' prop='isLocked'>
                  <el-select v-model="queryModel.isLocked" placeholder="Vui lòng chọn có kích hoạt không">
                    <el-option
                      v-for="item in isLocked"
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
            <el-col :span="10" style="text-align:right;padding-right:5px">
              <el-button-group>
                  <el-button
                    v-show="permission.add"
                    type="primary"
                    icon="el-icon-plus"
                    @click="onCreateEntity('clinicOfficeForm')"
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
            <el-table ref="mutipleTable"  min-height="calc(100vh - 250px)" class='drag_table' :data='clinicOfficeList' border @sort-change='onSortChange' @header-dragend='onChangeWidth' :cell-class-name='cellClassName' :header-cell-class-name='headerCellClassName' highlight-current-row>
              <el-table-column
              label="Số thứ tự"
              type="index"
              :index="indexMethod"
              align="center">
            </el-table-column>
              <el-table-column v-for="(cv, index) in columnViews" v-if='cv.display' :prop='cv.prop' :key="`columnViews_${index}`" :label='cv.label' sortable='custom' :align='cv.align' :min-width='cv.miniWidth+"px"' :width='cv.width+"px"' header-align='center' :column-key='index.toString()' :render-header="renderHeader">
                <template slot-scope='{row,$index}'>
                  <span v-if='columnViews[index].showType === "Checkbox" || columnViews[index].showType === "Radio"'>
                    <li v-if='getAttrValue(row, columnViews[index].prop) === "1"' class='el-icon-check' style='color:#F56C6C;'></li>
                  </span>

                  <span v-else-if="columnViews[index].prop==='isLocked'">
                    <span v-if='getAttrValue(row, columnViews[index].prop) === "0"' class='el-icon-check' style='color:#F56C6C;'></span>
                    <span v-else></span>
                  </span>
                  <span v-else-if="columnViews[index].prop==='isDefault'">
                    <span v-if='getAttrValue(row, columnViews[index].prop) === "1"' class='el-icon-check' style='color:#F56C6C;'></span>
                    <span v-else></span>
                  </span>
                   <span v-else-if="columnViews[index].prop==='isRegister'">
                    <span v-if='getAttrValue(row, columnViews[index].prop) === "1"' class='el-icon-check' style='color:#F56C6C;'></span>
                    <span v-else></span>
                  </span>
                   <span v-else>{{ getAttrValue(row, columnViews[index].prop, columnViews[index].javaType )}}</span>
                </template>
              </el-table-column>
              <!--Nút thao tác theo dòng-->
              <el-table-column label='Thao tác' header-align='center' :width='140 + "px"' :key="'operate'">
                <template slot='header' slot-scope="scope">
                  <span>Thao tác</span>
                  <view-columns-select v-model='columnViews' v-on:save-column-view='saveColumn' v-on:show-all-column='showAllColumn' v-on:show-default-column='showDefaultColumn'></view-columns-select>
                  <export-excel-button v-show='permission.export' :data='clinicOfficeList' :tHeader='getHeads()' :filterVal='getFilterVal()' :plain='true'></export-excel-button>
                </template>
                <template slot-scope='scope'>
                  <OperationIcon v-show='permission.view' type='info' content='Xem' placement='top-start' icon-name='el-icon-view'
                    @click='onViewEntity(scope.$index, scope.row, "clinicOfficeForm")'></OperationIcon>
                  <OperationIcon v-show='permission.edit' type='primary' content='Sửa' placement='top-start' icon-name='el-icon-edit'
                    @click='onEditEntity(scope.$index, scope.row, "clinicOfficeForm")'></OperationIcon>
                  <OperationIcon v-show='permission.add' type='primary' content='Sao chép' placement='top-start' icon-name='el-icon-document'
                    @click='onCopyEntity(scope.$index, scope.row, "clinicOfficeForm")'></OperationIcon>
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
            :page-sizes='[20, 50, 100, clinicOfficeTotal]'
            :page-size='20'
            layout='total, sizes, prev, pager, next, jumper'
            :total='clinicOfficeTotal'>
          </el-pagination>
        </el-col>
      </el-row>
      <!-- Thanh phân trang     Kết thúc -->     </el-card>
  </el-row>
</template>

<script>
import { listClinicOfficePage, getClinicOfficeById, deleteClinicOffice } from '@/api/org/clinicOffice'
import listViewMixin from '@/mixins/listViewMixin'
import ClinicOfficeForm from './clinicOfficeForm'
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
    ClinicOfficeForm,
    ExportExcelButton,
    ViewColumnsSelect,
    QueryForm,
    OperationIcon,
    History
  },
  data() {
    return {
      listApi: listClinicOfficePage,
      getApi: getClinicOfficeById,
      deleteApi: deleteClinicOffice,
      entityName: 'ClinicOffice',
      permissionPrefix: 'clinicOffice',
      queryTypes: {
        'is_locked': '=',
        'name': 'like',
      },
      queryModel: {
        'isLocked': '',   // Vô hiệu hóa（0：Chưa bị vô hiệu hóa；1：Vô hiệu hóa）
        'name': '',   // Tên khoa/phòng
      },
      isLocked: [
        {
          value: '0',
          label: 'Không'
        },
        {
          value: '1',
          label: 'Có'
        },
      ], //Có kích hoạt không
      clinicOfficeTotal: 0,
      clinicOfficeList: [],
      oprColumnWidth: 140,  // Độ rộng cột thao tác
      tableId: '1026680647570219053',
      schemeId: '1026680647570219069'
    }
  },
  methods: {
    appendSearchParams() {
      this.search.params.push({
        columnName: 'company_id',
        queryType: '=',
        value: currentUser.company.id
      })
      if (this.moreCodition) {
        this.search.params = this.search.params.concat(this.compositeCondition())
      } else {
        // Tham số truy vấn: Vô hiệu hóa（0：Chưa bị vô hiệu hóa；1：Vô hiệu hóa）
        this.search.params.push({
          columnName: 'is_locked',
          queryType: '=',
          value: this.queryModel.isLocked
        })
        // Tham số truy vấn: Tên khoa/phòng
        this.search.params.push({
          columnName: 'name',
          queryType: 'like',
          value: this.queryModel.name
        })
      }
    },
    handleListResponse(responseData) {
      this.clinicOfficeTotal = responseData.data.total
      this.clinicOfficeList = responseData.data.rows
    },
    initOptions(This) {
    }
  },
  watch: {
    clinicOfficeList(val) {
      if (val) {
        this.$nextTick(() => {
          this.$refs.mutipleTable.doLayout();
        });
      }
    }
  },
  updated() {
    if (this.$refs.mutipleTable) {
      this.$nextTick(() => {
        this.$refs.mutipleTable.doLayout()
      })
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

 .drag_table {
   overflow: auto;
}
</style>
