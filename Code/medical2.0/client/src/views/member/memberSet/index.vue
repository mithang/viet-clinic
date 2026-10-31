<template>
  <el-row v-loading='loading'>
    <!-- Lịch sử  -->
    <History :bussObject='curentRow' ></History>
    <!-- Cửa sổ chỉnh sửa  -->
    <memberSet-form ref='memberSetForm' :permission='permission' @save-finished='loadData'></memberSet-form>
    <div class="page-container">
        <!--  Thanh tìm kiếm  Bắt đầu -->
        <div class='query-form-container'>
          <el-row v-if='!moreCodition' class='search-row'>
            <el-form :model='queryModel' @submit.native.prevent label-position="left" label-width='100px' ref='queryForm' :inline-message='true'>
                      <el-col :span="5">
              <el-form-item label='Tên thẻ thành viên' prop='name'>
                <el-input v-model='queryModel.name' :clearable='true' placeholder='Vui lòng nhập tên thẻ thành viên'></el-input>
              </el-form-item>
            </el-col>
              <el-col :span="4">
             <el-form-item label="Loại" prop="memberType">
                <el-select
                  v-model="queryModel.memberType"
                  value-key="value"
                  filterable
                  clearable
                  placeholder="Vui lòng chọn loại"
                >
                  <el-option
                    v-for="item in memberType"
                    :key="item.value"
                    :label="item.name"
                    :value="item"
                  ></el-option>
                </el-select>
              </el-form-item>
              </el-col>
              <el-col :span="5">
             <el-form-item label="Có kích hoạt không" prop="status">
                <el-select
                  v-model="queryModel.status"
                  value-key="value"
                  filterable
                  clearable
                  placeholder="Vui lòng chọn có kích hoạt không"
                >
                  <el-option
                    v-for="item in status"
                    :key="item.value"
                    :label="item.name"
                    :value="item.value"
                  ></el-option>
                </el-select>
              </el-form-item>
              </el-col>
              <el-col :span="5">
             <el-form-item label="Có hiệu lực không" prop="valid">
                <el-select
                  v-model="queryModel.valid"
                  value-key="value"
                  filterable
                  clearable
                  placeholder="Vui lòng chọn có hiệu lực hay không"
                >
                  <el-option
                    v-for="item in valid"
                    :key="item.value"
                    :label="item.name"
                    :value="item.value"
                  ></el-option>
                </el-select>
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
               <el-col :span="2" style="text-align:right;padding-right:5px;float:right;">
                    <el-button-group>
                        <el-button
                          v-show="permission.add"
                          type="primary"
                          icon="el-icon-plus"                   
                          @click="onCreateMemberSet()"
                          >Thêm</el-button
                        >
                    </el-button-group>
                  </el-col>
                    <el-col :span="3" style="display:flex;justivy-content:space-around;float:right;">
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
            <el-button v-show='permission.add' type='primary' icon='el-icon-plus' :plain='true' @click='onCreateMemberSet()'>Thêm</el-button>
          </div>
        </div> -->
        <!-- Thanh công cụ Kết thúc -->

      <!-- Thanh bảng  Bắt đầu -->
      <el-row>
        <el-col :span='24'>
          <div @mouseleave='moveTableOutside'>
            
            <el-table class='drag_table' ref="tableRef" height="calc(100vh - 240px)" :data='memberSetList' border @sort-change='onSortChange' @header-dragend='onChangeWidth' :cell-class-name='cellClassName' :header-cell-class-name='headerCellClassName' highlight-current-row>                
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
                  <span v-else-if="columnViews[index].prop==='type'">
                    {{memberSetList[$index].type.name}}
                  </span>
                  <span v-else>{{ getAttrValue(row, columnViews[index].prop, columnViews[index].javaType )}}</span>
                </template>
              </el-table-column>
              <!--Nút thao tác theo dòng-->
              <el-table-column label='Thao tác' header-align='center' :width='oprColumnWidth + "px"' :key="'operate'">        
                <template slot='header'>
                  <span>Thao tác</span>
                  <view-columns-select v-model='columnViews' v-on:save-column-view='saveColumn' v-on:show-all-column='showAllColumn' v-on:show-default-column='showDefaultColumn'></view-columns-select>
                  <export-excel-button v-show='permission.export' :data='memberSetList' :tHeader='getHeads()' :filterVal='getFilterVal()' :plain='true'></export-excel-button>
                </template>
                <template slot-scope='scope'>
                  <!-- <OperationIcon v-show='permission.view' type='info' content='Xem' placement='top-start' icon-name='el-icon-view' 
                    @click='onViewMemberSet(scope.$index, scope.row)'></OperationIcon> -->
                   <center> 
                    <el-button type="text" v-show='permission.view' @click='onViewEntity(scope.$index, scope.row, "memberSetForm")'>Chi tiết</el-button>
                    <el-button v-if="scope.row.amount===scope.row.number" style="color:red" type="text" v-show='permission.view' @click='onDeleteEntity(scope.$index, scope.row, deleteApi)'>Xóa</el-button>
                  </center>
                    
                  <!-- <OperationIcon v-show='permission.edit' type='primary' content='Sửa' placement='top-start' icon-name='el-icon-edit' 
                    @click='onEditMemberSet(scope.$index, scope.row)'></OperationIcon>
                  <OperationIcon v-show='permission.add' type='primary' content='Sao chép' placement='top-start' icon-name='el-icon-document' 
                    @click='onCopyMemberSet(scope.$index, scope.row)'></OperationIcon>
                  <OperationIcon v-show='permission.remove' type='danger' content='Xóa' placement='top-start' icon-name='el-icon-delete' 
                    @click='onDeleteEntity(scope.$index, scope.row, deleteApi)'></OperationIcon>
                  <OperationIcon v-show='permission.view' type='info' content='Lịch sử' placement='top-start' icon-name='el-icon-info' 
                    @click='onShowHistory(scope.$index, scope.row)'></OperationIcon> -->
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
            :page-sizes='[20, 50, 100, memberSetTotal]'
            :page-size='20'
            layout='total, sizes, prev, pager, next, jumper'
            :total='memberSetTotal'>
          </el-pagination>
        </el-col>
      </el-row>
      <!-- Thanh phân trang     Kết thúc -->     </div>
  </el-row>
</template>

<script>
import '@/utils/validate';
import { listMemberSetPage, getMemberSetById, deleteMemberSet } from '@/api/member/memberSet'
import listViewMixin from '@/mixins/listViewMixin'
import MemberSetForm from './memberSetForm'
import ExportExcelButton from '@/components/ExportExcelButton'
import ViewColumnsSelect from '@/views/components/ViewColumnsSelect'
import QueryForm from '@/views/components/queryForm'
import MainUI from '@/views/components/mainUI'
import OperationIcon from '@/components/OperationIcon'
import History from '@/views/components/history'
import { getDictItemsByCode, DICT_CODE } from '@/utils/dictCache'
export default {
  extends: MainUI,
  mixins: [listViewMixin],
  components: {
    MemberSetForm,
    ExportExcelButton,
    ViewColumnsSelect,
    QueryForm,
    OperationIcon,
    History
  },
  data() {
    return {
      listApi: listMemberSetPage,
      getApi: getMemberSetById,
      deleteApi: deleteMemberSet,
      entityName: 'MemberSet',
      permissionPrefix: 'memberSet',
      queryTypes: {
        'name': 'like',
      },
      queryModel: {
        'name': '',   // Tên thẻ thành viên
        status:'',
        valid:'',
        memberType:{}
      },
      memberSetTotal: 0,
      memberSetList: [],
        
      
      oprColumnWidth: 140,  // Độ rộng cột thao tác
      tableId: '1222698883343517159',
      schemeId: '1222698883343517177',
      status:[{
        name:'Có',
        value:"0",
      },{
        name:'Không',
        value:'1',
      }],
      valid:[
        {
          name:'Có',
          value:'0'
        },
        {
          name:'Không',
          value:'1'
        }
      ],
      memberType:[],//Loại thẻ thành viên
    }
  },
  methods: {
    // CRUD button in the template calls this name; previously it did not
    // exist, so clicking Thêm did nothing. Delegate to the form component.
    onCreateMemberSet() {
      this.$refs.memberSetForm.openAddMemberSetDialog()
    },
    //Đặt lại
     reset(){
      this.queryModel={
         'name': '',   // Tên thẻ thành viên
        status:'',
        valid:'',
        memberType:{}
      }
      this.onSearch()
    },

     //Lấy loại thẻ thành viên
    initOptions(){
      getDictItemsByCode(DICT_CODE.MEMBER_TYPE).then((data) => {
        this.memberType = data;
        this.$forceUpdate()
      })
    },
    appendSearchParams() {
      this.search.params.push({
        columnName: 'company_id',
        queryType: '=',
        value: currentUser.company.id
      })
      if(this.moreCodition) {
        this.search.params = this.search.params.concat(this.compositeCondition())
      }else{
        this.search.params.push({
          columnName: 'type',
          queryType: '=',
          value: this.queryModel.memberType.value
        })
        this.search.params.push({
          columnName: 'name',
          queryType: 'like',
          value: this.queryModel.name
        })
        this.search.params.push({
          columnName: 'status',
          queryType: '=',
          value: this.queryModel.status
        })
        this.search.params.push({
          columnName: 'failure',
          queryType: '=',
          value: this.queryModel.valid
        })
      }
      this.pushDataPermissions(this.search.params, this.$route.meta.routerId, this.tableId)
    },
    handleListResponse(responseData) {
      this.memberSetTotal = responseData.data.total
      this.memberSetList = responseData.data.rows
    },
  },
  watch: {
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
