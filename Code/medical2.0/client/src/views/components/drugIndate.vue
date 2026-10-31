<template>
    <div>
              <i style="float: right; padding: 3px 0; position: relative; z-index: 1" class="el-icon-close" @click="closeTab"></i>

      <el-tabs v-model="activeName" type="card" @tab-click="handleClick">
        <el-tab-pane label="Hạn sử dụng của thuốc" name="drugIndate">
          <el-table :data="drugIndateList" :row-key="getRowKeys" @expand-change="expandSelect" :expand-row-keys="expands"  border v-loading="loading" highlight-current-row>
          <!-- <el-table-column type="expand">
            <template slot-scope="props">
              <span id="unread">
                {{onOpen(props.row,props.$index)}}
              </span>
            </template>
          </el-table-column> -->
          <el-table-column align="center" prop="drug.goodsName" label="Tên thuốc">
          </el-table-column>
            <el-table-column align="center" prop="endDate" label="Hạn sử dụng">
                <template slot-scope="scope">
                    {{getTime(scope.row.endDate)}}
                </template>
            </el-table-column>
            <el-table-column
                label="Thao tác"
                align="center"
                width="100">
                <template slot-scope="scope">
                    <el-button @click="checkDrug(scope.row)" type="text" size="small">Xem</el-button>
                    <!-- <el-button type="text" size="small">Sửa</el-button> -->
                </template>
            </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="Tồn kho thuốc" name="drugInventory">
        <el-table :data="inventoryList"	:row-key="getRowKeys" @expand-change="expandSelect" :expand-row-keys="expands" border v-loading="loading" highlight-current-row>

        <el-table-column align="center" prop="goodsName" label="Tên thuốc">
          </el-table-column>
            <el-table-column align="center" prop="inventory" label="Tồn kho">
              <template #default="scope">
                  <span style="color:orange">
                  {{Math.floor(scope.row.stock.surplusStock/scope.row.preparation) >= 0 ? Math.floor(scope.row.stock.surplusStock/scope.row.preparation)+(scope.row.pack.name)+((scope.row.stock.surplusStock%scope.row.preparation > 0) ? (scope.row.stock.surplusStock%scope.row.preparation) + scope.row.preparationUnit.name:""):scope.row.stock.surplusStock+scope.row.preparationUnit.name}}
                  </span>
              </template>
            </el-table-column>
            <el-table-column
                label="Thao tác"
                align="center"
                width="100">
                <template slot-scope="scope">
                    <el-button @click="checkDrugInventory(scope.row)" type="text" size="small">Xem</el-button>
                    <!-- <el-button type="text" size="small">Sửa</el-button> -->
                </template>
            </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>


      <el-col :span='24'>
        <el-pagination
          @size-change='onSizeChange'
          @current-change='onCurrentChange'
          :current-page.sync='currentPage'
          :page-sizes='[5, 10, 20, 40, total]'
          :page-size='5'
          :pager-count="3"
          background
          layout="total,sizes,prev, pager, next"
          :total='total'>
        </el-pagination>
      </el-col>
    </div>
</template>
<script>
import {getDrugIndateWarning, getDrugInventoryWarning} from "@/api/stock/warning";

import MainUI from '@/views/components/mainUI'

export default {
    extends: MainUI,
    props:['fatherDrugIndateList'],
    data(){
        return{
           expands: [], // Đặt qua thuộc tính nàyTableDòng đang mở rộng hiện tại, Cần đặtrow-keythuộc tính mới sử dụng được, Thuộc tính này là của dòng mở rộngkeysMảng
           drugIndateList:[],
           inventoryList:[],
           activeName: 'drugIndate',
           total:0,
           currentPage:1,
           search: {
                params: [{'columnName':'receive_by', 'queryType': '=', 'value': currentUser.id}],
                offset: 0,
                limit: 5,
                orderby: ''
            },
            inventorySearch:{
                offset: 0,
                limit: 5,
                order: "",
                columnName: "",
                params: [
                {
                    columnName: "company_id",
                    queryType: "=",
                    value: currentUser.company.id,
                },
                ],
            }
        }
    },
    mounted(){

        this.activeName = 'drugIndate';
         //Kiểm tra cảnh báo hạn sử dụng thuốc có tồn tại hay không
        this.search={
            offset: 0,
            limit: 5,
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
            this.init();
            this.inventoryInit();
    },
    methods:{
        getTime(time){
           let date = new Date(time);
           let year = date.getFullYear();
           let month = date.getMonth() + 1;
           month = month < 10 ? '0' + month : month;
           let day = date.getDate()
           day = day < 10 ? '0' + day : day;
          return year + '-' + month + '-' + day;
        },
        checkDrug(){
            this.$router.push({
                path:'warning'
            })
            this.$parent.doToggle();
        },
        checkDrugInventory(){
             this.$router.push({
                path:'warning',
                query:{
                    type:1,
                }
            })
            this.$parent.doToggle();
        },
         closeTab(){       //Đóng hộp hiển thị
            this.$parent.doToggle();
        },

        inventoryInit(){
                this.setLoad()
                getDrugInventoryWarning(this.inventorySearch).then((res)=>{
                    if(res.code===100){
                    if(res.data.rows){
                        this.inventoryList = res.data.rows
                    }else{
                        this.inventoryList = []
                    }

                    if(this.activeName === 'drugInventory'){
                        this.total = res.data.total
                    }
                     this.$store.dispatch('notices/SetDrugInventoryWarningTotal',res.data.total)
                    this.resetLoad();
                    }
                }).catch((error)=>{
                    this.outputError(error)
                })
        },

        init(){
           this.setLoad()
            getDrugIndateWarning(this.search).then((res)=>{
                if(res.code===100){
                this.total = res.data.total;

                if(res.data.rows){
                    this.drugIndateList = res.data.rows
                }else{
                    this.drugIndateList = []
                }
                this.$store.dispatch('notices/SetDrugIndateWarningTotal',res.data.total)
                this.resetLoad();
                }
            }).catch((error)=>{
                this.outputError(error)
            })
            },
        getRowKeys(row) { // của dữ liệu dòngKey
            return row.id
        },
        // tableMỗi lần chỉ mở rộng được một dòng
        expandSelect(row, expandedRows) {
            this.expands = [];
            if (expandedRows.length > 0) {
            row ? this.expands.push(row.id) : ''
            }
        },
        handleClick(tab, event){  //tabChuyển trang
            if(tab.name!=='all'){
                if(tab.name === 'drugIndate'){
                    this.currentPage =1
                    this.search={
                    offset: 0,
                    limit: 5,
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
                this.init();
                }else{
                   this.currentPage=1
                   this.inventorySearch={
                        offset: 0,
                        limit: 5,
                        order: "",
                        columnName: "",
                        params: [
                        {
                            columnName: "company_id",
                            queryType: "=",
                            value: currentUser.company.id,
                        },
                        ],
                    }
                  this.inventoryInit();
                }
            }
        },
         onSizeChange(val) {
        this.currentPage = 1
        if(this.activeName === 'drugIndate'){
            this.search.limit = val;
            this.search.offset = (this.currentPage - 1) * val;
            this.init()
        }else{
            this.inventorySearch.limit = val;
            this.inventorySearch.offset = (this.currentPage - 1) * val;
            this.inventoryInit();
        }
      },
      onCurrentChange(val) {
        this.currentPage = val;
        if(this.activeName ==='drugIndate'){
            this.search.offset = (val - 1) * this.search.limit;
            this.init()
        }else{
            this.inventorySearch.offset = (val-1) * this.inventorySearch.limit;
            this.inventoryInit();
        }
      },
    }
}
</script>
