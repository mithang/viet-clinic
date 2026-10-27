<template>
  <el-row v-loading='loading'>
    <!-- Lịch sử  -->
    <History :bussObject='curentRow' ></History>
    <!-- Cửa sổ chỉnh sửa  -->
    <memberManagement-form ref='memberManagementForm' :permission='permission' @save-finished='loadData'></memberManagement-form>
    <div class="page-container">
        <!--  Thanh tìm kiếm  Bắt đầu -->
        <div class='query-form-container'>
          <el-row v-if='!moreCodition' class='search-row'>
            <el-form :model='queryModel' @submit.native.prevent label-position="left" label-width='70px' ref='queryForm' :inline-message='true'>
                      <el-col :span="6">
              <el-form-item label='Họ tên' prop='name'>
                <el-input v-model='queryModel.name' :clearable='true' placeholder='Vui lòng nhập họ tên'></el-input>
              </el-form-item>
            </el-col>
              <el-col :span="6">
              <el-form-item label='会员卡号' prop='card'>
                <el-input v-model='queryModel.card' :clearable='true' placeholder='请输入卡号'></el-input>
              </el-form-item>
            </el-col>
             <el-col :span="6">
             <el-form-item label="Trạng thái" prop="status">
                <el-select
                  v-model="queryModel.status"
                  value-key="value"
                  filterable
                  clearable
                  placeholder="Vui lòng chọn trạng thái"
                >
                  <el-option
                    v-for="item in memberStatus"
                    :key="item.value"
                    :label="item.name"
                    :value="item.value"
                  ></el-option>
                </el-select>
              </el-form-item>
              </el-col>
               <el-col :span="6">
                      <el-form-item label='开卡日期' prop='updateDate'>
                       <el-date-picker
                        v-model="queryModel.updateDate"
                        type="datetimerange"
                        align="right"
                        start-placeholder="Ngày bắt đầu"
                        end-placeholder="Ngày kết thúc"
                        format="yyyy-MM-dd HH:mm:ss"
                        :default-time="['00:00:00', '23:59:59']">
                      </el-date-picker>
                      </el-form-item>
                </el-col>
                 <el-col :span='6'>
                  <el-form-item label='Loại thẻ thành viên' prop='type' label-width='100px'>
                    <el-select 
                    v-model="queryModel.type"
                    style="margin-left:-5px"
                    value-key="value"
                    filterable
                    clearable
                    placeholder="Vui lòng chọn loại thẻ thành viên"
                    @change="getMember">
                    <el-option v-for='item in typeList' :key='item.value' :label='item.name' :value='item.value'></el-option>
                    </el-select>
                  </el-form-item>
                </el-col>
                
                <el-col :span='6'>
                  <el-form-item label='Tên thẻ thành viên' prop='memberName' label-width='100px'>
                
                    <el-select  
                    v-model="queryModel.memberName"
                    style="margin-left:-5px"
                    value-key="value"
                    filterable
                    clearable
                    placeholder="Vui lòng chọn tên thẻ thành viên"
                   >
                    <el-option  v-for='item in memberSet_List' :key='item.id' :label='item.name' :value='item.name'></el-option>
                    </el-select>
                  </el-form-item>
                </el-col>      
               <el-col :span="3" style="text-align:right;padding-right:5px;float:right;">
                    <el-button-group>
                        <el-button
                          v-show="permission.add"
                          type="primary"
                          icon="el-icon-plus"                   
                          @click="onCreateMemberManagement()"
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
            <el-button v-show='permission.add' type='primary' icon='el-icon-plus' :plain='true' @click='onCreateMemberManagement()'>Thêm</el-button>
          </div>
        </div> -->
        <!-- Thanh công cụ Kết thúc -->

      <!-- Thanh bảng  Bắt đầu -->
      <el-row>
        <el-col :span='24'>
          <div @mouseleave='moveTableOutside'>        
            <el-table class='drag_table' ref="tableRef" height="calc(100vh - 240px)" :data='memberManagementList' border @sort-change='onSortChange' @header-dragend='onChangeWidth' :cell-class-name='cellClassName' :header-cell-class-name='headerCellClassName' highlight-current-row>                
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
                  <span v-else-if="columnViews[index].prop==='gender'">
                    <span v-if="memberManagementList[$index].gender==='gender_0'">男</span>
                    <span v-else>女</span>
                  </span>
                  <span v-else-if="columnViews[index].prop==='status'">
                    <span v-if="memberManagementList[$index].status==='0'">Chưa sử dụng</span>
                    <span v-else-if="memberManagementList[$index].status==='1'">Đã sử dụng</span>
                    <span v-else-if="memberManagementList[$index].status==='2'">Đã bị vô hiệu hóa</span>
                    <span v-else>Đã hết hiệu lực</span>
                  </span>
                  <span v-else-if="columnViews[index].prop==='age'">
                    {{memberManagementList[$index].age}}tuổi
                  </span>
                   <span v-else-if="columnViews[index].prop==='type'">
                    <!-- <span v-if="memberManagementList[$index].type=='gender_0'">男</span> -->
                    {{memberManagementList[$index].type.name}}
                  </span>
                  <span v-else>{{ getAttrValue(row, columnViews[index].prop, columnViews[index].javaType )}}</span>
                </template>
              </el-table-column>
              <!--Nút thao tác theo dòng-->
              <el-table-column label='Thao tác' header-align='center' :width='oprColumnWidth + "px"'>        
                <template slot='header' slot-scope="scope">
                  <span>Thao tác</span>
                  <view-columns-select v-model='columnViews' v-on:save-column-view='saveColumn' v-on:show-all-column='showAllColumn' v-on:show-default-column='showDefaultColumn'></view-columns-select>
                  <export-excel-button v-show='permission.export' :data='memberManagementList' :tHeader='getHeads()' :filterVal='getFilterVal()' :plain='true'></export-excel-button>
                </template>
                <template slot-scope='scope'>
                    <center> 
                      <el-button type="text" v-show='permission.view' @click='onViewEntity(scope.$index, scope.row, "memberManagementForm")'>Chi tiết</el-button>
                      <el-button v-if="scope.row.status==='0'" type="text" v-show='permission.view' @click='disableMember(scope.$index, scope.row)' style="color:red;">Vô hiệu hóa</el-button>
                    </center>
                  <!-- <OperationIcon v-show='permission.view' type='info' content='Xem' placement='top-start' icon-name='el-icon-view' 
                    @click='onViewMemberManagement(scope.$index, scope.row)'></OperationIcon>
                  <OperationIcon v-show='permission.edit' type='primary' content='Sửa' placement='top-start' icon-name='el-icon-edit' 
                    @click='onEditMemberManagement(scope.$index, scope.row)'></OperationIcon>
                  <OperationIcon v-show='permission.add' type='primary' content='Sao chép' placement='top-start' icon-name='el-icon-document' 
                    @click='onCopyMemberManagement(scope.$index, scope.row)'></OperationIcon>
                  <OperationIcon v-show='permission.remove' type='danger' content='Xóa' placement='top-start' icon-name='el-icon-delete' 
                    @click='onDeleteMemberManagement(scope.$index, scope.row)'></OperationIcon>
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
            :page-sizes='[20, 50, 100, memberManagementTotal]'
            :page-size='20'
            layout='total, sizes, prev, pager, next, jumper'
            :total='memberManagementTotal'>
          </el-pagination>
        </el-col>
      </el-row>
      <!-- Thanh phân trang     Kết thúc -->     </div>
  </el-row>
</template>

<script>
import { validatenull } from '@/utils/validate'
import { listMemberManagementPage, getMemberManagementById, deleteMemberManagement} from '@/api/member/memberManagement'
import listViewMixin from '@/mixins/listViewMixin'
import MemberManagementForm from './memberManagementForm'
import ExportExcelButton from '@/components/ExportExcelButton'
import ViewColumnsSelect from '@/views/components/ViewColumnsSelect'
import QueryForm from '@/views/components/queryForm'
import MainUI from '@/views/components/mainUI'
import OperationIcon from '@/components/OperationIcon'
import History from '@/views/components/history'
import { getDictItemsByCode, DICT_CODE } from '@/utils/dictCache'
import { listMemberSetAll } from '@/api/member/memberSet'
export default {
  extends: MainUI,
  mixins: [listViewMixin],
  components: {
    MemberManagementForm,
    ExportExcelButton,
    ViewColumnsSelect,
    QueryForm,
    OperationIcon,
    History
  },
  data() {
    return {
      listApi: listMemberManagementPage,
      getApi: getMemberManagementById,
      deleteApi: deleteMemberManagement,
      entityName: 'MemberManagement',
      permissionPrefix: 'memberManagement',
      queryTypes: {
        'name': 'like',
      },
      queryModel: {
         name: '',   // Họ tên
         card:'',   //Số thẻ
         updateDate:[],  //Thời gian
         status:"",  //Trạng thái
         type:"",   //Loại
         memberName:'', //Tên
      },
      memberManagementTotal: 0,
      memberManagementList: [],
        
      
      oprColumnWidth: 140,  // Độ rộng cột thao tác
      tableId: '1225301049409470622',
      schemeId: '1225301049409470640',
      memberStatus:[
        {
          name:"Chưa sử dụng",
          value:'0'
        },
        {
          name:"Đã sử dụng",
          value:'1'
        },
        {
          name:"Đã bị vô hiệu hóa",
          value:'2'
        },
        {
          name:"Đã hết hiệu lực",
          value:'4'
        },
      ],
      typeList:[],  //Loại thẻ thành viên
      memberSet_List:[], // Tên thẻ thành viên
    }
  },
  methods: {
    //Đặt lại tìm kiếm
    reset(){
      this.queryModel={
         name: '',   // Họ tên
         card:'',   //Số thẻ
         updateDate:[],  //Thời gian
         status:"",  //Trạng thái
         type:"",   //Loại
         memberName:'', //Tên
      }
      this.memberSet_List=[]
      this.onSearch()
    },
    //Lấy thẻ thành viên tương ứng với loại thẻ thành viên
    getMember(row){
      
      let memberSet_search = {
        params: []
      }
        // Trên bảng tương ứng của trườngfilterĐiều kiện
        memberSet_search.params.push.apply(memberSet_search.params, [{columnName: 'company_id', queryType: '=', value: currentUser.company.id},{columnName: 'type', queryType: '=', value: row.value},{columnName: 'status', queryType: '=', value: 0},{columnName: 'failure', queryType: '=', value: 0},{columnName: 'number', queryType: '>', value: 0}])
      // Quyền dữ liệu: Cài đặt thẻ thành viênmember_set
      this.pushDataPermissions(memberSet_search.params, this.$route.meta.routerId, '1222698883343517159')
      this.memberSet_List.splice(0, this.memberSet_List.length)
      listMemberSetAll(memberSet_search).then(responseData => {
        this.memberSet_List = responseData.data
      })
    },
    //禁用会员
    disableMember(index,row){
      deleteMemberManagement(row).then((res)=>{
        if(res.code === 100){
            this.loadData()
            this.showMessage({type: 'success', msg: '禁用成功'})
        }else{
           this.showMessage(res)
        }
      }).catch((error)=>{
         this.outputError(error)  
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
          columnName: 'name',
          queryType: 'like',
          value: this.queryModel.name
        })
        if(this.queryModel.card!==''){
          this.search.params.push({
            columnName: 'card',
            queryType: '=',
            value: this.queryModel.card
          })
        }
        if(this.queryModel.status!==''){
          this.search.params.push({
            columnName: 'status',
            queryType: '=',
            value: this.queryModel.status
          })
        }
        if(this.queryModel.type!==''){
          this.search.params.push({
            columnName: 'type',
            queryType: '=',
            value: this.queryModel.type
          })
        }
        if(this.queryModel.memberName!==''){
          this.search.params.push({
            columnName: 'member_name',
            queryType: 'like',
            value: this.queryModel.memberName
          })
        }
        if (this.queryModel.updateDate && this.queryModel.updateDate.length) {
          this.search.params.push({
            logic: "AND",
            queryType: "("
          },{
            columnName: "update_date",
            logic: "",
            queryType: 'between',
            value: this.queryModel.updateDate,
          },{
            logic: "",
            queryType: ")"
          })
        }
      }
      this.pushDataPermissions(this.search.params, this.$route.meta.routerId, this.tableId)
    },
    handleListResponse(responseData) {
      this.memberManagementTotal = responseData.data.total
      this.memberManagementList = responseData.data.rows
    },
    initOptions() {
      getDictItemsByCode(DICT_CODE.MEMBER_TYPE).then((data) => {
        this.typeList = data;
        this.$forceUpdate()
      })
    }
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
