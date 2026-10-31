<template>
  <el-row v-loading='loading'>
    <!-- Lịch sử  -->
    <History :bussObject='curentRow' ></History>
    <!-- Cửa sổ chỉnh sửa  -->
    <inventoryVerification-form ref='inventoryVerificationForm' :permission='permission' @save-finished='loadData'></inventoryVerification-form>
    <div class="page-container">
        <!--  Thanh tìm kiếm  Bắt đầu -->
        <div class='query-form-container'>
          <el-row v-if='!moreCodition' class='search-row'>
            <el-form :model='queryModel' @submit.native.prevent label-position="left" label-width='70px' ref='queryForm' :inline-message='true'>
                <el-col :span="24/3">
                  <el-form-item   label="Thời gian tạo" prop="createDate">
                     <el-date-picker
                        v-model="queryModel.createDate"
                        type="datetimerange"
                        align="right"
                        start-placeholder="Ngày bắt đầu"
                        end-placeholder="Ngày kết thúc"
                        format="dd/MM/yyyy HH:mm:ss"
                        :default-time="['00:00:00', '23:59:59']">
                      </el-date-picker>
              </el-form-item>
               </el-col>

               <el-col :span="24/3">
                  <el-form-item   label="Số đơn kiểm kê" prop="code">
                <el-input
                  v-model="queryModel.code"
                  :clearable="true"
                  placeholder="Vui lòng nhập số đơn kiểm kê"
                 style="width:200px"
                ></el-input>
              </el-form-item>
               </el-col>
                      <!-- <el-col :span="6">
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
              </el-col> -->
              
               <el-col :span="3" style="text-align:right;padding-right:5px;float:right;">
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
             
            </el-form>
          </el-row>
          <QueryForm v-else v-model='moreParm' :tableId='tableId' :schemeId='schemeId'  :routerId='$route.meta.routerId' @search='onSearch()' @moreCodition='onMoreCodition()'></QueryForm>
        </div>
        <!--  Thanh tìm kiếm  Kết thúc -->

        <!-- Thanh công cụ Bắt đầu -->
        <!-- <div class="page-container-header-end">
          <div>
            <el-button v-show='permission.add' type='primary' icon='el-icon-plus' :plain='true' @click='onCreateInventoryVerification()'>Thêm kiểm kê</el-button>
          </div>
        </div> -->
        <div>
          <el-row>
               
              <el-col :span="3" style="text-align:right;padding-right:5px;float:right;">
                    <el-button-group>
                        <el-button
                          v-show="permission.add"
                          type="primary"
                          icon="el-icon-plus"                   
                          @click="onCreateInventoryVerification('0')"
                          >Thêm kiểm kê thuốc mới</el-button
                        >
                       
                    </el-button-group>
                  
                  </el-col>
                   <el-col :span="3" style="text-align:right;padding-right:5px;float:right;">
                    <el-button-group>
                        <el-button
                          v-show="permission.add"
                          type="primary"
                          icon="el-icon-plus"                   
                          @click="onCreateInventoryVerification('1')"
                          >Thêm kiểm kê vật tư mới</el-button
                        >
                       
                    </el-button-group>
                  
                  </el-col>
                  <el-col :span="6" style="text-align:right;padding-right:5px;float:right;">
                  <el-checkbox v-model="check">Có bao gồm tồn kho bằng 0 không</el-checkbox>
               </el-col>
          </el-row>
        </div>
        <!-- Thanh công cụ Kết thúc -->
        <br>
      <!-- Thanh bảng  Bắt đầu -->
      <el-row>
        <el-col :span='24'>
          <div @mouseleave='moveTableOutside'>        
            <el-table  style="margin: auto" class='drag_table' ref="tableRef" height="calc(100vh - 260px)" :data='inventoryVerificationList' border @sort-change='onSortChange' @header-dragend='onChangeWidth' :cell-class-name='cellClassName' :header-cell-class-name='headerCellClassName' highlight-current-row>                
              <el-table-column
              label="Số thứ tự"
              type="index"
              :index="indexMethod"
              align="center">
            </el-table-column>
              <el-table-column align='center' v-for="(cv, index) in columnViews" v-if='cv.display' :prop='cv.prop' :key="`columnViews_${index}`" :label='cv.label' sortable='custom' :align='cv.align' :min-width='cv.miniWidth+"px"' :width='cv.width+"px"' header-align='center' :column-key='index.toString()' :render-header="renderHeader">
                <template slot-scope='{row}'>
                  <span v-if='columnViews[index].showType === "Checkbox" || columnViews[index].showType === "Radio"'>
                    <li v-if='getAttrValue(row, columnViews[index].prop) === "1"' class='el-icon-check' style='color:#F56C6C;'></li>
                  </span>
                  <span v-if="columnViews[index].prop==='type'">
                    <span v-if="getAttrValue(row, columnViews[index].prop)==='0'">
                      Thuốc
                    </span>
                    <span v-else>
                       Vật tư
                    </span>
                  </span>
                  <span v-else-if="columnViews[index].prop==='status'">
                    <span v-if="getAttrValue(row, columnViews[index].prop)==='0'" style="color:#FF9966">
                        Đang kiểm kê
                    </span>
                    <span v-else style="color:#99CC66">
                      Đã kiểm kê xong
                    </span>
                  </span>
                  <span v-else>{{ getAttrValue(row, columnViews[index].prop, columnViews[index].javaType )}}</span>
                </template>
              </el-table-column>
              <!--Nút thao tác theo dòng-->
              <el-table-column label='Thao tác' header-align='center' :width='120 + "px"' :key="'operate'">        
                <template slot='header'>
                  <span>Thao tác</span>
                  <view-columns-select v-model='columnViews' v-on:save-column-view='saveColumn' v-on:show-all-column='showAllColumn' v-on:show-default-column='showDefaultColumn'></view-columns-select>
                  <export-excel-button v-show='permission.export' :data='inventoryVerificationList' :tHeader='getHeads()' :filterVal='getFilterVal()' :plain='true'></export-excel-button>
                </template>
                 <template slot-scope='scope'>
                 <center>
                    <el-button type="text"    @click="onViewInventoryVerification(scope.row,scope.$index)">Xem</el-button>
                  <el-button type="text" v-if="scope.row.status==='0'"  @click="onDeleteInventoryVerification(scope.row,scope.$index)">Xóa</el-button>
                 </center>
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
            :page-sizes='[20, 50, 100, inventoryVerificationTotal]'
            :page-size='10'
            layout='total, sizes, prev, pager, next, jumper'
            :total='inventoryVerificationTotal'>
          </el-pagination>
        </el-col>
      </el-row>
      <!-- Thanh phân trang     Kết thúc -->     </div>
  </el-row>
</template>

<script>
import { listInventoryVerificationPage, getInventoryVerificationById, deleteInventoryVerification,saveInventoryVerification } from '@/api/stock/inventoryVerification'
import {getInventoryVerificationDetailByInventoryId,listInventoryVerificationDetailPage} from '@/api/stock/inventoryVerificationDetail'
import listViewMixin from '@/mixins/listViewMixin'
import InventoryVerificationForm from './inventoryVerificationForm'
import ExportExcelButton from '@/components/ExportExcelButton'
import ViewColumnsSelect from '@/views/components/ViewColumnsSelect'
import QueryForm from '@/views/components/queryForm'
import MainUI from '@/views/components/mainUI'
import History from '@/views/components/history'
export default {
  extends: MainUI,
  mixins: [listViewMixin],
  components: {
    InventoryVerificationForm,
    ExportExcelButton,
    ViewColumnsSelect,
    QueryForm,
    History
  },
  data() {
    return {
      // listViewMixin Cấu hình
      listApi: listInventoryVerificationPage,
      getApi: getInventoryVerificationById,
      deleteApi: deleteInventoryVerification,
      entityName: 'InventoryVerification',
      permissionPrefix: 'inventoryVerification',

      queryTypes: {
      },
      queryModel: {
        code:'',  //Số đơn kiểm kê
        createDate:'', // Thời gian tạo
      },
      inventoryVerificationTotal: 0,
      inventoryVerificationList: [],

      oprColumnWidth: 140,  // Độ rộng cột thao tác
      tableId: '1235640942656921602',
      schemeId: '1235640942656921612',
      check:false,   //Điều kiện thêm mới
    }
  },
  methods: {
    appendSearchParams() {
      this.search.params = [{columnName: 'company_id', queryType: '=', value: currentUser.company.id}]
      if(this.moreCodition) {
        this.search.params = this.search.params.concat(this.compositeCondition())
      } else {
        if(this.queryModel.code!==''&&this.queryModel!==undefined){
          this.search.params.push(
            {columnName: 'code', queryType: '=', value: this.queryModel.code}
          )
        }
        if (this.queryModel.createDate && this.queryModel.createDate.length) {
          this.search.params.push({
            logic: "AND",
            queryType: "("
          },{
            columnName: "create_date",
            logic: "",
            queryType: 'between',
            value: this.queryModel.createDate,
          },{
            logic: "",
            queryType: ")"
          })
        }
      }
      this.pushDataPermissions(this.search.params, this.$route.meta.routerId, this.tableId)
    },
    handleListResponse(responseData) {
      this.inventoryVerificationTotal = responseData.data.total
      this.inventoryVerificationList = responseData.data.rows
    },
    //Đặt lại tìm kiếm
    reset(){
      this.queryModel={
        code:"",
        createDate:""
      }
      this.loadData()
    },
    onViewInventoryVerification(row, index) {
      this.$refs.inventoryVerificationForm.openViewInventoryVerificationDialog(row)
    },
    onCreateInventoryVerification(sign) {
      //Kiểm tra tất cả hạng mục đã hoàn thành kiểm kê chưa
      let flag=0;
      if(this.inventoryVerificationList){
        this.inventoryVerificationList.forEach(item=>{
        if(item.status==='0'){
          flag=1
        }
      })
      }
      if(flag===1){
        this.$message.warning("Hiện đang có nhiệm vụ kiểm kê, vui lòng hoàn thành lần kiểm kê trước!")
        return
      }
      let type='0'
      if(this.check){
        type='1'
      }else{
        type='0'
      }
      let variety=sign
      //Thực hiện thêm mới kiểm kê
      saveInventoryVerification(type,variety).then(res=>{
        if(res.code === 100){
          this.$message.success("Thêm kiểm kê mới thành công")
          this.check=false
          this.loadData()
        }else{
          this.$message.warning(res.msg)
        }
      }).catch()
    },
    onDeleteInventoryVerification(row, index) {
      this.$confirm('Bạn có chắc muốn xóa không？', 'Xác nhận', {
        confirmButtonText: 'Xác nhận',
        cancelButtonText: 'Hủy',
        type: 'warning'
      }).then(() => {
        this.setLoad()
        deleteInventoryVerification(row).then(responseData => {
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
  updated(){
    if(this.$refs.tableRef){
      this.$nextTick(() => {
            // tableRefCóel-tableĐã liên kếtrefGiá trị thuộc tính
            this.$refs.tableRef.doLayout()// Đối với Table Thực hiện bố cục lại
        })
    }
  },
  mounted() {
    this.pageInit()
  }
}
</script>
<style lang="scss" >
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
<style scoped>
::v-deep .el-table__body-wrapper{
    height: calc(100% - 44px) !important;
  }
</style>