<template>
    <div>
       <div style="padding-top:10px">
             <el-radio-group v-model="tabPosition" @change="changStatus" style="margin-bottom: 10px;">
                <el-radio-button label="0">Cảnh báo hạn sử dụng</el-radio-button>
                <el-radio-button label="1">Cảnh báo tồn kho</el-radio-button>
            </el-radio-group>
             <div style="float:right">
              <el-button type="primary" icon='el-icon-upload2' @click="exportTable">Xuất</el-button>
            </div>
        </div>

        <!-- Cảnh báo hạn sử dụng -->
        <div v-if="tabPosition==='0'">

           <div class='query-form-container'>
          <el-row class='search-row'>
            <el-form :model='queryModel' @submit.native.prevent label-position="left" label-width='70px' ref='queryForm' :inline-message='true'>
              <el-col :span="6">
                <el-form-item label='Tên vật tư' prop='name'>
                  <el-input
                    style="width:320px;"
                    v-model="queryModel.name"
                    placeholder="Vui lòng nhập tên vật tư hoặc mã pinyin"
                  ></el-input>
                </el-form-item>
              </el-col>
                <el-col :span="6">
              <el-form-item label='Nhà cung cấp' prop='type'>
                <el-select
                  v-model="queryModel.supplier"
                  value-key="value"
                  filterable
                  clearable
                  placeholder="Vui lòng chọn nhà cung cấp"
                  class="typeClass"
                >
                  <el-option
                    v-for="type in supplierList"
                    :key="type.id"
                    :label="type.name"
                    :value="type.id"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-col>
              <el-col :span="6">
              <el-form-item label='Loại vật tư' prop='type'>
                <el-select
                  v-model="queryModel.type"
                  value-key="value"
                  filterable
                  clearable
                  placeholder="Vui lòng chọn loại vật tư"
                  class="typeClass"
                >
                  <el-option
                    v-for="type in type_List"
                    :key="type.value"
                    :label="type.name"
                    :value="type"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label='Có kích hoạt không' prop='status'>
                <el-select
                  v-model="queryModel.status"
                  value-key="value"
                  filterable
                  clearable
                  placeholder="Vui lòng chọn có kích hoạt không"
                  class="typeClass"
                >
                  <el-option
                    v-for="type in isEnable"
                    :key="type.value"
                    :label="type.name"
                    :value="type.value"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="6">
                <el-form-item label='Số lượng còn lại' prop='inventory'>
                <el-select
                  v-model="queryModel.inventory"
                  value-key="value"
                  filterable
                  clearable
                  placeholder="Vui lòng chọn số lượng còn lại"
                  class="typeClass"
                >
                  <el-option
                    v-for="type in inventorys"
                    :key="type.value"
                    :label="type.name"
                    :value="type.value"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-col>
            </el-form>
          </el-row>
          <el-row id="conditionOperation">
            <el-col :span="24" style="text-align:right;padding-right:5px">

                <el-button type="primary" icon="el-icon-search" @click='stuffBatchNumber' :plain='true'>Tìm kiếm</el-button>
                <el-button type="info" icon="el-icon-refresh-left" @click='resetCondition' :plain='true'>Đặt lại</el-button>
            </el-col>
          </el-row>
        </div>
      <!--  Thanh tìm kiếm  Kết thúc -->

          <el-row>
        <el-col :span="24">
          <div @mouseleave="moveTableOutside">
            <el-table
              class="dispensing_table"
              :data="indateList"
              height="calc(100vh - 330px)"
              border
              ref="mutipleTable1"
              @sort-change="onSortChange"
              @header-dragend="onChangeWidth"
              :cell-class-name="cellClassName"
              :header-cell-class-name="headerCellClassName"
              highlight-current-row
            >
            <!-- show-summary
             :summary-method='getTotal' -->
              <el-table-column
              label="Số thứ tự"
              type="index"
              :index="indexMethod"
              align="center">
            </el-table-column>
             <el-table-column prop="stuff.name" label="Tên vật tư" > </el-table-column>
              <el-table-column prop="stuff.type.name" label="Phân loại vật tư">
              </el-table-column>

              <el-table-column prop="norms" label="Quy cách">
                 </el-table-column>
                <el-table-column prop="factory.name" label="Nhà sản xuất" > </el-table-column>
                <el-table-column prop="supplierId.name" label="Nhà cung cấp" > </el-table-column>
                <el-table-column prop="batchNo" label="Số lô" > </el-table-column>
              <el-table-column prop="number" label="Số lượng còn lại" key="slot">
                  <template #default="scope">
                  {{Math.floor(scope.row.stock.surplusStock/scope.row.stuff.packNumber) >= 0 ? Math.floor(scope.row.stock.surplusStock/scope.row.stuff.packNumber)+(scope.row.stuff.packUnit.name)+((scope.row.stock.surplusStock%scope.row.stuff.packNumber > 0) ? (scope.row.stock.surplusStock%scope.row.stuff.packNumber) + scope.row.stuff.minUnit.name:""):scope.row.stock.surplusStock+scope.row.drug.stuff.packNumber}}
                </template>
                </el-table-column>
                <el-table-column prop="endDate" label="Hạn sử dụng" > </el-table-column>
                <el-table-column prop="cancellation" label="Trạng thái" >
                    <template slot-scope="scope">
                      <span v-if="scope.row.cancellation==='0'" style="color:orange">
                          Sắp hết hạn
                      </span>
                      <span v-else style="color:red">
                         Đã hết hạn
                      </span>
                    </template>
                </el-table-column>
            </el-table>
          </div>
        </el-col>
      </el-row>
        </div>

        <!-- Cảnh báo tồn kho -->
        <div v-else><el-row>

          <div class='query-form-container'>
          <el-row class='search-row'>
            <el-form :model='inventoryQueryModel' @submit.native.prevent label-position="left" label-width='70px' ref='queryForm' :inline-message='true'>
              <el-col :span="6">
                <el-form-item label='Tên vật tư' prop='name'>
                  <el-input
                    style="width:320px;"
                    v-model="inventoryQueryModel.name"
                    placeholder="Vui lòng nhập tên vật tư hoặc mã pinyin"
                  ></el-input>
                </el-form-item>
              </el-col>

              <el-col :span="6">
              <el-form-item label='Loại vật tư' prop='type'>
                <el-select
                  v-model="inventoryQueryModel.type"
                  value-key="value"
                  filterable
                  clearable
                  placeholder="Vui lòng chọn loại vật tư"
                  class="typeClass"
                >
                  <el-option
                    v-for="type in type_List"
                    :key="type.value"
                    :label="type.name"
                    :value="type"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label='Có kích hoạt không' prop='status'>
                <el-select
                  v-model="inventoryQueryModel.status"
                  value-key="value"
                  filterable
                  clearable
                  placeholder="Vui lòng chọn có kích hoạt không"
                  class="typeClass"
                >
                  <el-option
                    v-for="type in isEnable"
                    :key="type.value"
                    :label="type.name"
                    :value="type.value"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="6">
                <el-form-item label='Số lượng còn lại' prop='inventory'>
                <el-select
                  v-model="inventoryQueryModel.inventory"
                  value-key="value"
                  filterable
                  clearable
                  placeholder="Vui lòng chọn số lượng còn lại"
                  class="typeClass"
                >
                  <el-option
                    v-for="type in inventorys"
                    :key="type.value"
                    :label="type.name"
                    :value="type.value"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-col>
            </el-form>
          </el-row>
          <el-row id="conditionOperation">
            <el-col :span="24" style="text-align:right;padding-right:5px">

                <el-button type="primary" icon="el-icon-search" @click='stuffInventory' :plain='true'>Tìm kiếm</el-button>
                <el-button type="info" icon="el-icon-refresh-left" @click='resetCondition' :plain='true'>Đặt lại</el-button>
            </el-col>
          </el-row>
        </div>

        <el-col :span="24">
          <div @mouseleave="moveTableOutside">
            <el-table
              class="dispensing_table"
              :data="inventoryList"
              height="calc(100vh - 330px)"
              border
              ref="mutipleTable1"
              @sort-change="onSortChange"
              @header-dragend="onChangeWidth"
              :cell-class-name="cellClassName"
              :header-cell-class-name="headerCellClassName"
              highlight-current-row
            >
            <!-- show-summary
             :summary-method='getTotal' -->
              <el-table-column
              label="Số thứ tự"
              type="index"
              :index="indexMethod"
              align="center">
            </el-table-column>
             <el-table-column prop="name" label="Tên vật tư" > </el-table-column>
              <el-table-column prop="type.name" label="Phân loại vật tư">
              </el-table-column>
                 <el-table-column prop="packNumber" label="Quy cách" key="slot">
                   <template #default="scope">
                    {{scope.row.packNumber}}{{scope.row.minUnit.name}}/{{scope.row.packUnit.name}}
                   </template>
                 </el-table-column>
                <el-table-column prop="factory.name" label="Nhà sản xuất" > </el-table-column>
              <el-table-column prop="inventory" label="Số lượng còn lại">
                <template slot-scope="scope">
                  <span style="color:orange">
                  {{Math.floor(scope.row.stock.surplusStock/scope.row.packNumber) >= 0 ? Math.floor(scope.row.stock.surplusStock/scope.row.packNumber)+(scope.row.packUnit.name)+((scope.row.stock.surplusStock%scope.row.packNumber > 0) ? (scope.row.stock.surplusStock%scope.row.packNumber) + scope.row.minUnit.name:""):scope.row.stock.surplusStock+scope.row.minUnit.name}}
                  </span>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-col>
      </el-row>
        </div>
    </div>
</template>
<script>
import { validatenull } from "@/utils/validate";
import MainUI from "@/views/components/mainUI";
import {listSupplierAll} from "@/api/stock/supplier"
import { getDictItemsByCode, DICT_CODE } from '@/utils/dictCache'
import {getStuffIndateWarning,getStuffInventoryWarning,exportTable} from "@/api/stock/warning";
export default {
  extends: MainUI,
  props:['stuffActiveName','getCurrentPage','getSizeChange'],
  data() {
    return {
       type:0,    //Chuyển đổi cảnh báo
       tabPosition:"0",   //Chuyển đổi cảnh báo
       supplierList:[],   //Nhà cung cấp vật tư
       type_List:[],    //Loại vật tư
       queryTypes: {
        goods_name: "like",
        type: "=",
        bar_code: "like",
      },
      indateSearch:{
        offset: 0,
        limit: 20,
        order: "",
        columnName: "",
        params: [
          {
            columnName: "company_id",
            queryType: "=",
            value: currentUser.company.id,
          },
          {
            columnName:"cancellation",
            queryType:"<>",
            value:"1"
          },
        ],
      },
      inventorySearch:{
        offset: 0,
        limit: 20,
        order: "",
        columnName: "",
        params: [
          {
            columnName: "company_id",
            queryType: "=",
            value: currentUser.company.id,
          },
        ],
      },
      indateList:[],   //Tập dữ liệu cảnh báo hạn hiệu lực
      sizeChange:20,   //Số lượng hiển thị mỗi trang
      currentPage:1,  //Số dòng hiển thị mỗi trang
      queryModel: {
        name:'',
        type:'',
        status:'',
        inventory:"",
        supplier:"",
      },
      inventoryQueryModel:{
        name:'',
        type:'',
        status:'',
        inventory:"",
        supplier:"",
      },
      isEnable:[
          {
              value:"0",
              name:"Không"
          },
          {
              value:"1",
              name:"Có"
          }
      ],
      inventorys:[
          {
              value:"0",
              name:"Bằng0"
          },
          {
              value:"1",
              name:"Lớn hơn0"
          }
      ],

      inventoryList:[],   //Tập cảnh báo tồn kho

    };
  },
  methods: {

    //Xuất
    exportTable(){
      if(this.tabPosition==='0'){
        this.indateSearch.columnName = '2'
        //Xuất theo hạn sử dụng
        exportTable(this.indateSearch).then((res)=>{
               const filename = decodeURI(res.headers.split(';')[1].split('=')[1]) || '.xls'
                const blob = new Blob([res.data], {

                type: 'application/octet-stream'

              })

                let url = window.URL.createObjectURL(blob);

                let link = document.createElement('a');

                link.style.display = 'none';

                link.href = url;

                link.setAttribute('download', filename);

                document.body.appendChild(link);

                link.click()
        }).catch((Error)=>{
          this.outputError(Error)
        })
      }else{
        this.inventorySearch.columnName = "3"
        //Xuất theo tồn kho
        exportTable(this.inventorySearch).then((res)=>{
                const filename = decodeURI(res.headers.split(';')[1].split('=')[1]) || '.xls'
                const blob = new Blob([res.data], {

                type: 'application/octet-stream'

              })

                let url = window.URL.createObjectURL(blob);

                let link = document.createElement('a');

                link.style.display = 'none';

                link.href = url;

                link.setAttribute('download', filename);

                document.body.appendChild(link);

                link.click()
        }).catch((Error)=>{
          this.outputError(Error)
        })
      }
    },

    resetCondition(){
      if(this.tabPosition==='0'){
        this.queryModel={
         name:'',
        type:'',
        status:'',
        inventory:"",
        supplier:"",
      }
      this.stuffBatchNumber()
      }else{
        this.inventoryQueryModel ={
          name:'',
          type:'',
          status:'',
          inventory:"",
          supplier:"",
        }
        this.stuffInventory()
      }
    },

    //Tìm tồn kho
    stuffInventory(){
      this.currentPage = 1
      this.inventorySearch = {
          columnName:"",
          limit: this.sizeChange,
          offset: this.currentPage - 1,
          order: "",
          params:[
            {
              columnName: "company_id",
              queryType: "=",
              value: currentUser.company.id,
            },
            {
              columnName:"type",
              queryType:"=",
              value:validatenull(this.inventoryQueryModel.type.value)
                    ? ""
                    : this.inventoryQueryModel.type.value
            },
            {
              columnName:"status",
              queryType:"=",
              value:validatenull(this.inventoryQueryModel.status)
              ? ""
              :this.inventoryQueryModel.status
            }
          ]
      }
      if(this.inventoryQueryModel.name!==undefined && this.inventoryQueryModel.name!==''){

             const pattern2 = new RegExp("[A-Za-z]+");
                if (pattern2.test(this.inventoryQueryModel.name)) {
                  this.inventorySearch.params.push(
                    {
                      columnName: "pinyin_code",
                      queryType: "like",
                      value: this.inventoryQueryModel.name.toUpperCase(),
                    },

                  )
                } else {
                  this.inventorySearch.params.push(
                    {
                      columnName: "name",
                      queryType: "like",
                      value: this.inventoryQueryModel.name,
                    },

                  )
                }
          }
          if(this.inventoryQueryModel.inventory && this.inventoryQueryModel.inventory!==''){
              if(this.inventoryQueryModel.inventory==='0'){
                   this.inventorySearch.params.push(
                    {
                      columnName: "inventory",
                      queryType: "=",
                      value: 0,
                    })
              }else{
                   this.inventorySearch.params.push(
                    {
                      columnName: "inventory",
                      queryType: ">",
                      value: 0,
                    })
              }
          }
          this.setLoad()
          getStuffInventoryWarning(this.inventorySearch).then((res)=>{
            if(res.code===100){
              if(this.currentPage===1){
                this.$emit("changeCurrentPage",1)
            }
              this.$emit("changeTotal",res.data.total)
              this.inventoryList = res.data.rows
              this.resetLoad()
            }
          }).catch((error)=>{
            this.outputError(error)
          })
    },

    //Tìm hạn sử dụng
    stuffBatchNumber(){
      this.currentPage = 1
       this.indateSearch = {
          columnName:"",
          limit: this.sizeChange,
          offset: this.currentPage - 1,
          order: "",
          params:[
            {
              columnName: "company_id",
              queryType: "=",
              value: currentUser.company.id,
            },
            {
              columnName: "stuff.type",
              queryType: "=",
              value: validatenull(this.queryModel.type.value)
                    ? ""
                    : this.queryModel.type.value,
            },
            {
              columnName: "stuff.status",
              queryType: "=",
              value: validatenull(this.queryModel.status)
                    ? ""
                    : this.queryModel.status,
            },
            {
                columnName:"cancellation",
                queryType:"<>",
                value:"1"
            },
            {
                columnName:"stuff_id",
                queryType:"<>",
                value:"''"
            }
          ]
        }

        if(this.queryModel.name!==undefined && this.queryModel.name!==''){

             const pattern2 = new RegExp("[A-Za-z]+");
                if (pattern2.test(this.queryModel.name)) {
                  this.indateSearch.params.push(
                    {
                      columnName: "stuff.pinyin_code",
                      queryType: "like",
                      value: this.queryModel.name.toUpperCase(),
                    },

                  )
                } else {
                  this.indateSearch.params.push(
                    {
                      columnName: "stuff.goods_name",
                      queryType: "like",
                      value: this.queryModel.name,
                    },

                  )
                }
          }
          if(this.queryModel.inventory && this.queryModel.inventory!==''){
              if(this.queryModel.inventory==='0'){
                   this.indateSearch.params.push(
                    {
                      columnName: "number",
                      queryType: "=",
                      value: 0,
                    })
              }else{
                   this.indateSearch.params.push(
                    {
                      columnName: "number",
                      queryType: ">",
                      value: 0,
                    })
              }
            }
        if(this.queryModel.supplier && this.queryModel.supplier!==''){
            this.indateSearch.params.push(
                {
                    columnName: "supplier_id",
                    queryType: "=",
                    value: this.queryModel.supplier,
                }
            )
        }


        this.setLoad();
        getStuffIndateWarning(this.indateSearch).then(res=>{
        if(res.code===100){
         if(this.currentPage===1){
            this.$emit("changeCurrentPage",1)
         }
          this.$emit("changeTotal",res.data.total)
          this.indateList = res.data.rows
          this.resetLoad();
        }
      }).catch((error)=>{
        this.outputError(error)
      })
    },

    //Số thứ tự bảng
    indexMethod(index){
       return (this.currentPage-1)*this.indateSearch.limit+index +1;
    },

    //Lấy tổng cộng
    getTotal(param){
      let { columns } = param;
      let arr = []
      columns.forEach((column, index) => {
          if (index === 0) {
            arr[index] = 'Tổng cộng';
          }else{
            arr[index] = ''
          }
      })
    //  if(this.tabPosition=='0'){
    //      arr[8] = new BigNumber(Number(this.allTotal.totalSellingPrice)).toFormat(2)
    //      arr[10] = new BigNumber(Number(this.allTotal.totalPrice)).toFormat(2)
    //  }else{
    //       //Tổng giá bán
    //   arr[6] = new BigNumber(Number(this.allTotal.totalSellingPrice)).toFormat(2)

    //   //Tổng giá nhập
    //   arr[7] = new BigNumber(Number(this.allTotal.totalPrice)).toFormat(2)
    //  }
      return arr
    },

    //Khởi tạo lấy cảnh báo tồn kho
    inventoryInit(){
      this.setLoad()
      getStuffInventoryWarning(this.inventorySearch).then((res)=>{
        if(res.code===100){
          if(this.currentPage===1){
            this.$emit("changeCurrentPage",1)
         }
          this.$emit("changeTotal",res.data.total)
          this.inventoryList = res.data.rows
        }
      }).catch((error)=>{
        this.outputError(error)
      })
    },

    //Khởi tạo lấy cảnh báo hạn hiệu lực
    init(){

      this.setLoad();
      //Khởi tạo số trang
      getStuffIndateWarning(this.indateSearch).then(res=>{
        if(res.code===100){
         if(this.currentPage===1){
            this.$emit("changeCurrentPage",1)
         }
          this.$emit("changeTotal",res.data.total)
          this.indateList = res.data.rows
          this.resetLoad();
        }
      }).catch((error)=>{
        this.outputError(error)
      })
    },

    //Khởi tạo dữ liệu
    initOptions(){
       this.tabPosition = '0'
      getDictItemsByCode(DICT_CODE.STUFF_TYPE).then((data) => {
        this.type_List = data;
      });

      //Lấy nhà cung cấp
      let supplierSearch = {
        order:"",
        params: [
          {
            columnName: "company_id",
            queryType: "=",
            value: currentUser.company.id,
          },
          {
            columnName: "type",
            queryType: "=",
            value: '2',
          }
        ],
      }
        listSupplierAll(supplierSearch).then((res)=>{
            if(res.code===100){
                this.supplierList = res.data
            }
        }).catch((error)=>{
            this.$message.error(error)
        })
    },

    handleClick(tab){
        this.type = tab.name
    },
    changStatus(){
      if(this.tabPosition==='0'){

      }else{

      }
    },
  },
  watch: {

    type:function(newVal){
      this.tabPosition = newVal
    },
    getSizeChange:function(newVal){
      this.sizeChange = newVal
      this.currentPage = 1
      if(this.tabPosition === '0'){
        this.indateSearch.limit = newVal;
        this.indateSearch.offset = (this.currentPage - 1) * newVal;
        this.init();
      }else{
        this.inventorySearch.limit = newVal
        this.inventorySearch.offset = (this.currentPage - 1) * newVal
        this.inventoryInit()
      }
    },
    getCurrentPage:function(newVal){

      this.currentPage = newVal
      if(this.tabPosition==='0'){
        this.indateSearch.offset = (newVal - 1) * this.indateSearch.limit;
        this.init()
      }else{
        this.inventorySearch.offset = (newVal - 1) * this.inventorySearch.limit
        this.inventoryInit()
      }
    },
    tabPosition:{
      handler(newVal){
        this.tabPosition = newVal
        if(this.tabPosition==='0'){
          this.currentPage = 1
          this.queryModel={
            name:'',
            type:'',
            status:'',
            inventory:"",
            supplier:"",
          }
          this.indateSearch={
              offset: 0,
              limit: 20,
              order: "",
              columnName: "",
              params: [
                {
                  columnName: "company_id",
                  queryType: "=",
                  value: currentUser.company.id,
                },
                {
                  columnName:"cancellation",
                  queryType:"<>",
                  value:"1"
                },
              ],
            }
          this.init()
        }else{
          this.currentPage = 1
          this.inventoryQueryModel = {
              name:'',
              type:'',
              status:'',
              inventory:"",
              supplier:"",
          }
          this.inventorySearch = {
             offset: 0,
              limit: 20,
              order: "",
              columnName: "",
              params: [
                {
                  columnName: "company_id",
                  queryType: "=",
                  value: currentUser.company.id,
                }
              ],
          }
          this.inventoryInit()
        }
      }
    },
    // tableDataCóel-tableDữ liệu đã liên kết
      tableData: {
          // Khắc phục lỗi hiển thị lệch bảng
          handler () {
              this.$nextTick(() => {
                  // tableRefCóel-tableĐã liên kếtrefGiá trị thuộc tính
                  this.$refs.mutipleTable1.doLayout()// Đối với Table Thực hiện bố cục lại
              })
          },
          deep: true
      },
  },
  updated(){
    if(this.$refs.mutipleTable1){
      this.$nextTick(() => {
            // tableRefCóel-tableĐã liên kếtrefGiá trị thuộc tính
            this.$refs.mutipleTable1.doLayout()// Đối với Table Thực hiện bố cục lại
        })
    }
  },
  mounted() {
    if(this.stuffActiveName===1){
      this.type='1'
    }
    this.initOptions();
    this.init();
  },
};
</script>
<style lang="scss" scoped>
.page-container{
  padding: 0;
}
.query-form-container{
  padding: 0px 0px 10px 0px;
}
.typeClass{
  ::v-deep .el-input{
    width: 90% !important;
    input{
      width: 90% !important;
      padding-right: 0px;
    }
  }
}
.el-col{
  ::v-deep .el-range-separator{
    width: 10%;
  }
}
.cell{
  span{
    display: inline-block;
    width: 100%;
    text-align: right;
  }
}
::v-deep .el-table__footer-wrapper{
  td:not(:nth-of-type(1)){
    .cell{
      display: inline-block;
      width: 100%;
      text-align: left;
    }
  }

}
.el-table::before{
  height: 0;
}
::v-deep .el-table colgroup col[name='gutter']{
  width:5px !important
}
::v-deep .el-table__body{
  width:100% !important
}
// Cài đặt bảngbodyChiều cao của
 ::v-deep.el-table__body-wrapper {
  //Khắc phục hiển thị dữ liệu vượt quábodychiều cao không cuộn đượcbug
  overflow-y: auto;
   // Phần trừ đi là bảngheaderChiều cao của
   height: calc(100% - 44px) !important;
 }
</style>
