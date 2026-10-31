<template>
  <el-card class="page-container">
    <div id="storage">
    <el-row id="conditionRow">
      <el-col :span="4">
        <el-input  style="width: 90%;" v-model="condition.pinyinCode" placeholder="Nhập tên thuốc/Mã pinyin"></el-input>
      </el-col>
      <el-col :span="4">
        <el-select style="width: 90%;" v-model="condition.status" placeholder="Trạng thái đơn nhập kho">
          <el-option label="Tất cả" value="">Tất cả</el-option>
          <el-option
            v-for="statusxz in statusList"
            :label="statusxz.name"
            :value="statusxz.value"
            :key="statusxz.value"
          ></el-option>
        </el-select>
      </el-col>
      <el-col :span="7" >
        <el-date-picker
          style="width: 90%;"
          :picker-options="filerData.pickerOptions"
          v-model="condition.dateRange"
          type="datetimerange"
          range-separator="đến"
          start-placeholder="Ngày bắt đầu"
          end-placeholder="Ngày kết thúc"
          :default-time="['00:00:00', '23:59:59']"
        >
        </el-date-picker>
      </el-col>
      <el-col :span="3" style="display:flex;justivy-content:space-around">
               <el-button
                    type="primary"
                    icon="el-icon-search"
                    @click="querySupplierStorageList()"
                    :plain="true"
                  >Tìm kiếm</el-button>
                <el-button
                    type="info"
                    icon="el-icon-refresh-left"
                    @click="resetCondition()"
                    :plain="true"
                  >Đặt lại</el-button>
            </el-col>
    </el-row>
    <el-row style="margin-top:10px;margin-bottom:10px">
      <el-col :span="24" style="text-align:right">
        <div class="bg_btn">
          <el-button type="primary" @click="editAndAddStorage(1)"
            >Thêm mới thuốc</el-button
          >
          <el-button type="primary" @click="editAndAddStorage(2)"
            >Thêm mới vật tư</el-button
          >
          <el-button>Quét mã vạch</el-button>
        </div>
      </el-col>
    </el-row>
    <!-- <el-row id="conditionOperation">
      <el-col :span="2" :offset="22">
        <div class="bg_btn">
          <el-button type="primary" @click="querySupplierStorageList()"
            >Truy vấn</el-button
          >
          <el-button @click="resetCondition()">Đặt lại</el-button>
        </div>
      </el-col>
    </el-row> -->
    <el-row style="height: 250px">
      <el-col :span="24">
        <el-table
          border
          height="230"
          :data="storageTable"
          style="width: 100%"
          highlight-current-row
          @selection-change="selectionChange">
          <!-- @row-click="textRowClick"-->
        <el-table-column
          type="selection"
          width="55">
        </el-table-column>
          <el-table-column type="index" :index="indexMethod" width="50px" label="Số thứ tự" align="center">
          </el-table-column>
          <el-table-column prop="code" label="Số đơn nhập kho">
            <template slot-scope="scope">
              <div class="examine-box">
                <span>{{ scope.row.code }}</span>
                <span
                  class="examine"
                  v-if="scope.row.examine.name === 'Đã hủy bỏ'"
                  >{{ scope.row.examine.name }}</span
                >
              </div>
            </template>
          </el-table-column>
          <el-table-column
            prop="supplier.name"
            label="Nhà cung cấp"
          ></el-table-column>
          <el-table-column prop="breed" label="Số lượng chủng loại" width="80px"></el-table-column>
          <!-- <el-table-column prop="number" label="Số lượng chi tiết"></el-table-column> -->
          <el-table-column prop="type" label="Loại vật phẩm" width="80px">
            <template slot-scope="scope">
              <span v-if="scope.row.type==='1'">
                 Thuốc
              </span>
              <span v-else>
                Vật tư
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="createBy" label="Người nhập kho"></el-table-column>
          <el-table-column prop="createDate" label="Ngày nhập kho"></el-table-column>
          <el-table-column label="Ngày hủy bỏ"></el-table-column>
          <el-table-column prop="examine.name" label="Trạng thái" width="80px"></el-table-column>
          <el-table-column
            prop="examineDate"
            label="Ngày phê duyệt"
          ></el-table-column>
        </el-table>
        <el-pagination
          @size-change="storageTableSizeChange"
          @current-change="storageTableCurrentChange"
          :page-sizes="[100,200,storageTableTotal]"
          :page-size="storageTablePagesize"
          :current-page="storageTableCurrentPage"
          layout="total,sizes, prev, pager, next,jumper"
          :total="storageTableTotal"
        >
        </el-pagination>
      </el-col>
    </el-row>
    <el-row style="height: 250px; margin-top: 10px">
      <el-col :span="24">
        <el-table
          id="exportTableStorage"
          border
          height="230"
          :data="storageDetailTable"
          show-summary
          sum-text=""
          :summary-method="getDetailSummaries"
          style="width: 100%"
        >
        <el-table-column type="index" :index="indexMethod" width="50px" label="Số thứ tự" align="center">
          </el-table-column>
          <el-table-column prop="norms" label="Số đơn nhập kho">
            <template slot-scope="scope">
              <span>{{ scope.row.code }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="drug.goodsName" label="Thuốc/Tên vật tư">
            <template slot-scope="scope">
              <span v-if="scope.row.drug.goodsName===''">{{ scope.row.stuff.name }}</span>
              <span v-else>{{ scope.row.drug.goodsName }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="norms" label="Quy cách" width="80px"></el-table-column>
          <el-table-column
            prop="factory.name"
            label="Tên nhà sản xuất"
          ></el-table-column>
          <el-table-column prop="batchNo" label="Số lô sản xuất"></el-table-column>
          <el-table-column
            prop="produceDate"
            label="Ngày sản xuất"
          ></el-table-column>
          <el-table-column prop="endDate" label="Hạn hiệu lực"></el-table-column>
          <el-table-column prop="number" label="Số lượng" width="80px"></el-table-column>
          <el-table-column prop="bid" label="Giá nhập" width="60px"></el-table-column>
          <el-table-column prop="leastBid" label="Giá nhập theo đơn vị nhỏ nhất" width="100px"></el-table-column>
          <el-table-column prop="allBid" label="Tổng giá nhập" width="100px">
            <template slot-scope="scope" v-if="systemParamConfig===2">
             <!-- {{scope.row.leastBid*scope.row.inventory}}-->
             {{scope.row.allBid}}
            </template>
            <template slot-scope="scope" v-if="systemParamConfig===1">
              <!-- {{scope.row.leastBid*scope.row.inventory}}-->
              {{scope.row.allRetailPrice}}
            </template>
          </el-table-column>
          <el-table-column prop="retailPrice" label="Giá bán lẻ" width="80px"></el-table-column>
          <el-table-column
            prop="allRetailPrice"
            label="Tổng giá bán lẻ"
             width="100px"
          >
          <template slot-scope="scope">
            {{scope.row.allRetailPrice}}
          </template>
          </el-table-column>
        </el-table>
        <!-- <el-pagination
          @size-change="storageDetailTableSizeChange"
          @current-change="storageDetailTableCurrentChange"
          :page-sizes="[5, 10, 15, 20]"
          :page-size="storageDetailTablePagesize"
          :current-page="storageDetailTableCurrentPage"
          layout="total,sizes, prev, pager, next,jumper"
          :total="storageDetailTableTotal"
        >
        </el-pagination> -->
      </el-col>
    </el-row>
    <el-row style="margin-top: 10px;">
      <!-- <el-col :span="4" :offset="1">
        <div class="spaninput">
          <span>Số đơn kèm hàng: </span>
          <el-input
            id="withGoodsNo"
            v-model="otherInfo.withGoodsNo"
            :readonly="true"
          >
          </el-input>
        </div>
      </el-col>
      <el-col :span="4" :offset="1">
        <div class="spaninput">
          <span>Số hóa đơn: </span>
          <el-input v-model="otherInfo.invoiceNumber" :readonly="true">
          </el-input>
        </div>
      </el-col> -->
      <el-col :span="2" :offset="16" style="text-align:right">
        <el-button @click="inStorage()"
        :style="{ 'background': btnType?'white':'gray','color':btnType?'#606266':'white' }">
          Nhập kho</el-button
        >
      </el-col>
      <el-col :span="2"  style="text-align:right">
        <el-button @click="destoryStorage()"
        :style="{ 'background': btnType?'white':'gray','color':btnType?'#606266':'white' }">
          Hủy bỏ</el-button
        >
      </el-col>
      <el-col :span="2" style="text-align:right">
        <el-button @click="exportExcel" :style="{ 'background': btnType?'white':'gray','color':btnType?'#606266':'white' }">Xuất chi tiết</el-button>
      </el-col>
      <el-col :span="2" style="text-align:right">
        <el-button @click="searchDetail" :style="{ 'background':'white','color':'#606266' }">Xem chi tiết</el-button>
      </el-col>
    </el-row>

    <el-dialog
      custom-class="customWidth"
      width="90%"
      modal="true"
      :close-on-click-modal="false"
      :title="dialogTitle"
      :visible.sync="dialogVisible"
    >
      <AddStorage
        @fatherMethod="closeDialog"
        :supplierList="supplierList"
        :addType="addType"
      ></AddStorage>
    </el-dialog>
  </div>
  </el-card>
</template>

<script>
import AddStorage from "./components/addstorage";
import { listSupplierPage, getSupplierById,listSupplierAll } from "@/api/stock/supplier";
import {
  bulkUpdateSupplierStorage,
  bulkUpdateSupplierStorage1,
  bulkInSupplierStorage,
  listSupplierStorageAll,
  listSupplierStoragePage,
  listByCode
} from "@/api/stock/supplierStorage";
import {
  listSupplierStockAll,
  listSupplierStockPage,
  getSupplierStockBySid,
} from "@/api/stock/supplierStock";
import { listDictItemAll } from "@/api/sys/dictItem";
import { getDictItemsByCode, DICT_CODE } from '@/utils/dictCache'

 import FileSaver from 'file-saver'
 import * as XLSX from 'xlsx'
//import XLSX from 'xlsx'
import XLSXD from 'xlsx-style'
import {listSysParamConfigAll} from "../../../api/sys/sysParamConfig";
export default {
  components: { AddStorage },
  data() {
    return {
      btnType: false,
      filerData: {
        pickerOptions: {
          // disabledDate(time) {
          //   return time.getTime() > Date.now() - 8.64e6;
          // },
        },
      },
      company_id: "", /*Phòng khámID - bởicurrentUser.company.idLấy động*/
      examine_destroy_id: "1005787933775863932",
      examine_destroy_value: "supplierStorageExamineStatus_2" /*Trạng thái hủy bỏvalue*/,
      storageTableCurrentPage: 1,
      storageTablePagesize: 100 /*Số dòng hiển thị tối đa của bảng chính*/,
      storageTableTotal: 0 /*Tổng số bản ghi của bảng chính*/,
      storageDetailTableCurrentPage: 1,
      storageDetailTablePagesize: 5 /*Số dòng hiển thị tối đa của bảng chính*/,
      storageDetailTableTotal: 0 /*Tổng số bản ghi của bảng chính*/,
      supplierList: null,
      statusList: null,
      storageCode:'',//Số đơn nhập kho
      supplierName:'',//Tên nhà cung cấp
      initialNum:[],//Gán giá trị cho bảng chi tiết
      invalid:[],//Chi tiết hủy bỏ
      condition: {
        pinyinCode: '',
        status: '',
        supplier: {
          id: null,
        },
        status: null,
        dateRange: [] /*Khoảng thời gian*/,
        batchNo: "",
      },
      storageTable: null,
      otherInfo: {
        withGoodsNo: "" /*Số đơn cung ứng*/,
        invoiceNumber: "" /*Số hóa đơn*/,
      },
      storageDetailTable: null,
      dialogVisible: false,
      dialogTitle: "",
      isAdd: 0 /*0Sửa 1Thêm mới*/,
      addtype: 1, //Thêm mới loại đơn nhập kho 1là thuốc 2là vật tư
      selectStorage: [],
      supplierSearch: {
        params: [{ columnName: "company_id", queryType: "=", value: "" }],
        offset: 0,
        limit: 100,
        columnName: "", // Tên trường sắp xếp
        order: "", // Sắp xếp
      },
      statusSearch: {
        params: [{ columnName: "company_id", queryType: "=", value: "" }],
        offset: 0,
        limit: 100,
        columnName: "", // Tên trường sắp xếp
        order: "", // Sắp xếp
      },
      supplierStorageSearch: {
        params: [{ columnName: "company_id", queryType: "=", value: "" }],
        offset: 0,
        limit: 10000,
        columnName: "code", // Tên trường sắp xếp
        order: "desc", // Sắp xếp
      },
      systemParamConfig:"",
      systemParamConfigSearch: {
        params: [{
          columnName: "company_id",
          queryType: "=",
          value: currentUser.company.id
        }
        ]
      },
      supplierStockSearch: {
        params: [{ columnName: "company_id", queryType: "=", value: "" }],
        offset: 0,
        limit: 10000,
        columnName: "", // Tên trường sắp xếp
        order: "", // Sắp xếp
      },
    };
  },
  methods: {
    selectionChange(rows){
      if(rows.length===0){
        this.btnType = false;
      }
      this.selectStorage = rows;
      this.searchDetail();
    },
    // Giá nhập có phải giá bán lẻ không
    GetSysParamConfig(){
      listSysParamConfigAll(this.systemParamConfigSearch).then(responseData => {
        if(responseData.code === 100) {
          if (Array.isArray(responseData.data) && responseData.data.length >= 1){
            responseData.data.forEach(data=>{
              if (data.configName === "retailPrice") {
                /*if (data.configValue){
                  this.systemParamConfig = 1
                }else{
                  this.systemParamConfig = 2
                }*/
                this.systemParamConfig = ("true" === data.configValue) ? 1 : 2
              }
            })
          }
        } else {
          this.showMessage(responseData)
        }
      }).catch(error => {
        this.outputError(error)
      })
    },
    //Xem chi tiết
    searchDetail(){
      this.storageDetailTable = []
      this.storageDetailTableTotal = null
      this.invalid = []
      this.selectStorage.forEach(item=>{
        this.supplierStockSearch.params = [
          { columnName: "company_id", queryType: "=", value: currentUser.company.id },
          {
            columnName: "supplier_storage_id",
            queryType: "=",
            value: item.id,
          },
        ];
        listSupplierStockPage(this.supplierStockSearch)
        .then((responseData) => {
           if (responseData.code === 100) {
            this.storageDetailTableTotal += responseData.data.total;
            this.invalid.push(...responseData.data.rows)
            let initalArr=[]
            initalArr=responseData.data.rows;
            this.initialNum=[]
            for (let i = 0; i < initalArr.length; i++) {
              initalArr[i].code = item.code
              initalArr[i].supplier = item.supplier.name
              this.initialNum.push(initalArr[i].number)
              if(initalArr[i].drug.goodsName!==''){
                  initalArr[i].inventory = initalArr[i].number
                  initalArr[i].number=Math.floor(initalArr[i].number/initalArr[i].drug.preparation) >= 0 ? Math.floor(initalArr[i].number/initalArr[i].drug.preparation)+(initalArr[i].drug.pack.name)+((initalArr[i].number%initalArr[i].drug.preparation > 0) ? (initalArr[i].number%initalArr[i].drug.preparation) + initalArr[i].drug.preparationUnit.name:""):initalArr[i].number+initalArr[i].drug.preparationUnit.name
                  initalArr[i].allRetailPrice=initalArr[i].allRetailPrice//initalArr[i].retailPrice/initalArr[i].drug.preparation
               }else if(initalArr[i].stuff.name!==''){
                  initalArr[i].inventory = initalArr[i].number
                  initalArr[i].number=Math.floor(initalArr[i].number/initalArr[i].stuff.packNumber) >= 0 ? Math.floor(initalArr[i].number/initalArr[i].stuff.packNumber)+initalArr[i].stuff.packUnit.name+((initalArr[i].number%initalArr[i].stuff.packNumber > 0) ? (initalArr[i].number%initalArr[i].stuff.packNumber)+initalArr[i].stuff.minUnit.name:""):initalArr[i].number+initalArr[i].stuff.minUnit.name
                  initalArr[i].allRetailPrice=initalArr[i].allRetailPrice//initalArr[i].retailPrice/initalArr[i].stuff.packNumber
              }
            }
            this.storageDetailTable.push(...initalArr);
           } else {
             this.$message.error(responseData);
           }
        })
        .catch((error) => {
          this.$message.error(error);
        });
      })
      if(this.selectStorage.length>0) this.btnType = true;
    },
    /*textRowClick(rows) {
      if(rows.length==0){
        this.btnType = false;
      }
      this.selectStorage = rows;
      this.searchDetail();
    },*/
    pageInit() {
      const day = new Date();
      // this.condition.dateRange = [
      //   new Date(day.getFullYear(), day.getMonth(), day.getDate(), 0, 0),
      //   new Date(day.getFullYear(), day.getMonth(), day.getDate(), 23, 59, 59),
      // ];

      this.supplierSearch.params = [
        {
          columnName: "company_id",
          queryType: "=",
          value: currentUser.company.id,
        },
      ];
      listSupplierAll(this.supplierSearch)
        .then((responseData) => {
          if (responseData.code === 100) {
            this.supplierList = responseData.data;
          } else {
            this.$message.error(responseData);
          }
        })
        .catch((error) => {
          this.$message.error(error);
        });

      getDictItemsByCode(DICT_CODE.SUPPLIER_STORAGE_EXAMINE_STATUS).then((data) => {
        this.statusList = data;
      });
      this.querySupplierStorageList();
    },
    storageTableSizeChange(val) {
      this.storageTablePagesize = val;
      this.querySupplierStorageList();
    },
    storageTableCurrentChange(val) {
      this.storageTableCurrentPage = val;
      this.querySupplierStorageList();
    },
    storageDetailTableSizeChange(val) {
      this.storageDetailTablePagesize = val;
      this.storageTableRowClick(this.selectStorage);
    },
    storageDetailTableCurrentChange(val) {
      this.storageDetailTableCurrentPage = val;
      this.storageTableRowClick(this.selectStorage);
    },
    querySupplierStorageList() {
      let params = {
        pinyinCode: this.condition.pinyinCode,
        status: this.condition.status,
        startTime: this.condition.dateRange[0]
          ? this.$moment(this.condition.dateRange[0]).format(
              "YYYY-MM-DD HH:mm:ss"
            )
          : "",
        endTime: this.condition.dateRange[1]
          ? this.$moment(this.condition.dateRange[1]).format(
              "YYYY-MM-DD HH:mm:ss"
            )
          : "",
        offset: 0,
        limit: 10000,
      }
      params.offset =
        (this.storageTableCurrentPage - 1) * this.storageTablePagesize;
      params.limit = this.storageTablePagesize;
      listByCode(params)
        .then((responseData) => {
          if (responseData.code === 100) {
           if(responseData.data.rows!=null){
              let arr=[]
            arr = responseData.data.rows;
            clearTimeout(this.timer);
            this.timer=setTimeout(()=>{
              this.storageTable=arr
               this.storageTableTotal = responseData.data.total;
            },400)

            this.clearOtherAndDetailData();
           }else{
              this.storageTable=responseData.data.rows
               this.storageTableTotal = responseData.data.total;
           }

          } else {
            this.$message.error(responseData);
          }
        })
        .catch((error) => {
          this.$message.error(error);
        });
    },
    storageTableRowClick(row) {
      this.storageCode=row.code
      this.supplierName=row.supplier.name
      this.btnType = true;
      this.selectStorage = row;
      this.supplierStockSearch.offset =
        (this.storageDetailTableCurrentPage - 1) *
        this.storageDetailTablePagesize;
      this.supplierStockSearch.limit = this.storageDetailTablePagesize;
      this.supplierStockSearch.params = [
        { columnName: "company_id", queryType: "=", value: currentUser.company.id },
        {
          columnName: "supplier_storage_id",
          queryType: "=",
          value: row.id,
        },
      ];
      listSupplierStockPage(this.supplierStockSearch)
        .then((responseData) => {
           if (responseData.code === 100) {
             this.storageDetailTableTotal = responseData.data.total;
              this.invalid=responseData.data.rows
            let initalArr=[]
            initalArr=responseData.data.rows;

            this.initialNum=[]
            for (let i = 0; i < initalArr.length; i++) {
              initalArr[i].code = this.selectStorage.code
              this.initialNum.push(initalArr[i].number)
              if(initalArr[i].drug.goodsName!==''){
                  initalArr[i].number=Math.floor(initalArr[i].number/initalArr[i].drug.preparation) >= 0 ? Math.floor(initalArr[i].number/initalArr[i].drug.preparation)+(initalArr[i].norms.split("/")[1])+((initalArr[i].number%initalArr[i].drug.preparation > 0) ? (initalArr[i].number%initalArr[i].drug.preparation) + initalArr[i].norms.split("*")[1].split("/")[0].split("")[initalArr[i].norms.split("*")[1].split("/")[0].split("").length-1]:""):initalArr[i].number+initalArr[i].norms.split("*")[1].split("/")[0].split("")[initalArr[i].norms.split("*")[1].split("/")[0].split("").length-1]
                }else if(initalArr[i].stuff.name!==''){
                  initalArr[i].number=Math.floor(initalArr[i].number/initalArr[i].stuff.packNumber) >= 0 ? Math.floor(initalArr[i].number/initalArr[i].stuff.packNumber)+(initalArr[i].norms.split("*")[1])+((initalArr[i].number%initalArr[i].stuff.packNumber > 0) ? (initalArr[i].number%initalArr[i].stuff.packNumber)+initalArr[i].norms.split("*")[0].split("")[initalArr[i].norms.split("*")[0].split("").length-1]:""):initalArr[i].number+initalArr[i].norms.split("*")[0].split("")[initalArr[i].norms.split("*")[0].split("").length-1]
              }
            }
            this.storageDetailTable = initalArr;
           } else {
             this.$message.error(responseData);
           }
        })
        .catch((error) => {
          this.$message.error(error);
        });
      this.otherInfo.withGoodsNo = row.withGoodsNo;
      this.otherInfo.invoiceNumber = row.invoiceNumber;
    },
    getDetailSummaries(param) {
      const { columns, data } = param;
      const sums = [];
      columns.forEach((column, index) => {
        if (index === 0) {
          if (data == null) sums[index] = "Tổng cộng:0";
          else sums[index] = "Tổng cộng:" + data.length;
          return;
        } else if (index === 8) {
          if (data == null) sums[index] = 0;
          else {
            const values = data.map((item) => Number(item[column.property]));
            if (!values.every((value) => isNaN(value))) {
              sums[index] = values.reduce((prev, curr) => {
                const value = Number(curr);
                if (!isNaN(value)) {
                  return prev + curr;
                } else {
                  return prev;
                }
              }, 0);
            }
          }
        } else {
          sums[index] = "";
        }
      });

      return sums;
    },
    editAndAddStorage(type) {
        this.addType = type
        this.isAdd = 1;
        this.dialogVisible = true;
        if(this.addType === 1){
          this.dialogTitle = "Thêm mới đơn nhập kho thuốc";
        }else{
          this.dialogTitle = "Thêm mới đơn nhập kho vật tư";
        }

    },
    closeDialog() {
      this.dialogVisible = false;
      this.querySupplierStorageList();
    },
    resetCondition() {
      const day = new Date();
      this.condition = {
        supplier: {
          id: null,
        },
        status: null,
        dateRange: [
          // new Date(day.getFullYear(), day.getMonth(), day.getDate(), 0, 0),
          // new Date(
          //   day.getFullYear(),
          //   day.getMonth(),
          //   day.getDate(),
          //   23,
          //   59,
          //   59
          // ),
        ] /*Khoảng thời gian*/,
        drugName: "",
      };
      this.clearData();
      this.pageInit();
    },
    clearData() {
      // this.storageTable = null;
      this.clearOtherAndDetailData();
    },
    clearOtherAndDetailData() {
      this.storageDetailTable = null;
      this.otherInfo.withGoodsNo = null;
      this.otherInfo.invoiceNumber = null;
      this.selectStorage = [];
    },
    inStorage() {
      this.$confirm("Bạn có chắc muốn nhập kho phiếu nhập kho này không", "Nhắc nhở", {
          confirmButtonText: "Xác nhận",
          cancelButtonText: "Hủy",
          type: "warning",
        })
          .then(() => {
            let params = {
              "supplierStorage": {
                "examine": {
                  "id": "1005787933775863930",
                  "value": "supplierStorageExamineStatus_0"
                }
              },
            }
            let supplierStockList = this.selectStorage.map(item => ({ id: item.id }))
            params.supplierStockList = supplierStockList
            bulkInSupplierStorage(params)
              .then((responseData) => {
                if (responseData.code === 100) {
                  this.$message({ message: "Xét duyệt qua", type: "success" });
                  this.querySupplierStorageList();
                } else {
                  this.$message.error(responseData);
                }
              })
              .catch((error) => {
                this.$message.error(error);
              });
          })
          .catch(() => {
            return;
          });
    },
    destoryStorage() {
       //  this.selectStorage.number=this.initialNum
      if (this.selectStorage.length===1&&this.invalid.length>0) {
        if (this.selectStorage[0].examine.value === this.examine_destroy_value) {
          this.$message({
            message: "Đơn nhập kho đã hủy không cần hủy lại",
            type: "warning",
          });
          return;
        }
        for (let i = 0; i < this.invalid.length; i++) {
          if(this.invalid[i].initial>this.initialNum[i]){

                this.$message({
                message: "Tồn kho này đã bị sử dụng",
                type: "warning",
              });
              return;
          }
        }
        // if(this.selectStorage.initial > this.selectStorage.number){
        //     this.$message({
        //     message: "Tồn kho này đã bị sử dụng",
        //     type: "warning",
        //   });
        //   return;
        // }
        this.$confirm("Bạn có chắc muốn hủy đơn nhập kho đang chọn không", "Nhắc nhở", {
          confirmButtonText: "Xác nhận",
          cancelButtonText: "Hủy",
          type: "warning",
        })
          .then(() => {
            this.selectStorage[0].examine.id = this.examine_destroy_id;
            this.selectStorage[0].examine.value = this.examine_destroy_value;

            const destoryList = [];
            destoryList.push(this.selectStorage[0]);
            bulkUpdateSupplierStorage1(this.selectStorage[0])
              .then((responseData) => {
                if (responseData.code === 100) {
                  this.$message({ message: "Hủy thành công", type: "success" });
                  this.querySupplierStorageList();
                } else {
                  this.$message.error(responseData);
                }
              })
              .catch((error) => {
                this.$message.error(error);
              });
          })
          .catch(() => {
            return;
          });
      } else if(this.selectStorage.length>1) {
        this.$message({ message: "Mỗi lần chỉ chọn một đơn nhập kho để hủy", type: "error" });
      }else if(this.invalid.length===0){
        this.$message({ message: "Vui lòng truy vấn chi tiết trước", type: "error" });
      }
     },
      //Xuất bảng
      exportExcel () {
        let biaoge=[]
        biaoge[0]={
             code:"Số chứng từ",
             name:"Thuốc/Tên vật tư",
             norms:"Quy cách",
             number:"Số lượng",
             bid:"Giá nhập",
             allBid:"Tổng giá nhập",
             retailPrice:"Giá bán lẻ",
             allRetailPrice:"Tổng giá bán lẻ",
             batchNo:"Số lô sản xuất",
             produceDate:"Ngày sản xuất",
             endDate:"Hạn sử dụng",
             createDate:"Thời gian nhập kho",
             supplier:"Đơn vị cung ứng",
             factory:"Tên nhà sản xuất",
             beizhu:"Ghi chú"
        }

        let numbers=0;
        let allRetailPrices=0
        for (let i = 0; i < this.storageDetailTable.length; i++) {
          let arr={
          code:'',
          name:'',
          norms:'',
          number:'',
          bid:'',
          allBid:'',
          retailPrice:'',
          allRetailPrice:'',
          batchNo:'',
          produceDate:'',
          endDate:'',
          createDate:'',
          supplier:'',
          factory:'',
        }

          if(this.storageDetailTable[i].drug.goodsName===''){
            arr.name=this.storageDetailTable[i].stuff.name
          }else{
            arr.name=this.storageDetailTable[i].drug.goodsName
          }
          arr.norms=this.storageDetailTable[i].norms
          arr.factory=this.storageDetailTable[i].factory.name
          arr.batchNo=this.storageDetailTable[i].batchNo
          arr.code=this.storageDetailTable[i].code
          arr.supplier=this.storageDetailTable[i].supplier
          arr.createDate=this.storageDetailTable[i].createDate.split(" ")[0]
          arr.produceDate=this.storageDetailTable[i].produceDate.split(" ")[0]
          arr.endDate=this.storageDetailTable[i].endDate.split(" ")[0]
          arr.number=this.storageDetailTable[i].number
          arr.bid=this.storageDetailTable[i].bid+''
          // arr.leastBid=this.storageDetailTable[i].leastBid+''
          arr.allBid=this.storageDetailTable[i].allBid+''
          arr.retailPrice=this.storageDetailTable[i].retailPrice+''
          arr.allRetailPrice=this.storageDetailTable[i].allRetailPrice+''
          arr.beizhu=''
          biaoge.push(arr)
          numbers+=this.storageDetailTable[0].number.substring(0,this.storageDetailTable[0].number.length-1)-0
          allRetailPrices+=this.storageDetailTable[i].allRetailPrice

        }
        let num=biaoge.length
         biaoge[num]={
             code:'Tổng cộng',
             name:"",
             norms:"",
             number:"",
             bid:"",
            //  leastBid:"",
             allBid:"",
             retailPrice:"",
             allRetailPrice:""+allRetailPrices,
             batchNo:"",
             produceDate:"",
             endDate:"",
             createDate:'',
            //  number:""+numbers,
             supplier:'',
             factory:"",
             beizhu:""
        }
        // biaoge[num+1]={
        // code:'Người lập đơn: ',
        //      name:"",
        //      norms:"",
        //      number:"",
        //      bid:"",
        //     //  leastBid:"",
        //      allBid:"",
        //      retailPrice:"",
        // allRetailPrice:"Thủ kho: ",
        //      batchNo:"",
        //      produceDate:"",
        //      endDate:"",
        // createDate:'Người nhận hàng: ',
        //      supplier:'',
        //      factory:"",
        // beizhu:"Ngày nhận hàng: "
        // }

           let web = XLSX.utils.book_new();
           let headers={
             name:"Thuốc/Tên vật tư",
             norms:"Quy cách",
             factory:"Tên nhà sản xuất",
             batchNo:"Số lô sản xuất",
             produceDate:"Ngày sản xuất",
             endDate:"Hạn sử dụng",
             number:"Số lượng",
             bid:"Giá nhập",
             leastBid:"Giá nhập theo đơn vị nhỏ nhất",
             allBid:"Tổng giá nhập",
             retailPrice:"Giá bán lẻ",
             allRetailPrice:"Tổng giá bán lẻ"
           }
            //biaoge.unshift(headers)
            // let contentWs = XLSX.utils.json_to_sheet(biaoge,{
            //   headers:['name','norms','factory','batchNo','produceDate','endDate','number','bid','leastBid','allBid','retailPrice','allRetailPrice'],
            //   skipHeader:false,
            //   orgin: "A4",
            // });
             let contentWs = XLSX.utils.json_to_sheet(biaoge);

            contentWs["A1"]={
              t:"s",
              v:this.storageDetailTable[0].company.name + ' Bảng chi tiết nhập kho',
              s:{
                alignment:{
                  horizontal:"center",
                  vertical:"center",
                  wrapText:true,
                },
              }
            }

            for (let i = 0; i < biaoge.length; i++) {
             if(i===biaoge.length-1){
                contentWs["A"+(i+2)]={
                    t:"s",
                    v:biaoge[i].code,
                    s:{
                      border: {
                      top: { style: 'thin' },
                      left: { style: 'thin' },
                      bottom: { style: 'thin' },
                      right: { style: 'thin' },
                    }
                  }
                }
              contentWs["B"+(i+2)]={
                    t:"s",
                    v:biaoge[i].name,
                    s:{

                      border: {
                      top: { style: 'thin' },
                      left: { style: 'thin' },
                      bottom: { style: 'thin' },
                      right: { style: 'thin' },
                    }
                  }
                }
                contentWs["C"+(i+2)]={
                    t:"s",
                    v:biaoge[i].norms,
                    s:{

                      border: {
                      top: { style: 'thin' },
                      left: { style: 'thin' },
                      bottom: { style: 'thin' },
                      right: { style: 'thin' },
                    }
                  }
                }
                contentWs["D"+(i+2)]={
                    t:"s",
                    v:biaoge[i].number,
                    s:{

                      border: {
                      top: { style: 'thin' },
                      left: { style: 'thin' },
                      bottom: { style: 'thin' },
                      right: { style: 'thin' },
                    }
                  }
                }
                contentWs["E"+(i+2)]={
                    t:"s",
                    v:biaoge[i].bid,
                    s:{

                      border: {
                      top: { style: 'thin' },
                      left: { style: 'thin' },
                      bottom: { style: 'thin' },
                      right: { style: 'thin' },
                    }
                  }
                }

                contentWs["F"+(i+2)]={
                    t:"s",
                    v:biaoge[i].allBid,
                    s:{

                      border: {
                      top: { style: 'thin' },
                      left: { style: 'thin' },
                      bottom: { style: 'thin' },
                      right: { style: 'thin' },
                    }
                  }
                }
                contentWs["G"+(i+2)]={
                    t:"s",
                    v:biaoge[i].retailPrice,
                    s:{

                      border: {
                      top: { style: 'thin' },
                      left: { style: 'thin' },
                      bottom: { style: 'thin' },
                      right: { style: 'thin' },
                    }
                  }
                }
                contentWs["H"+(i+2)]={
                    t:"s",
                    v:biaoge[i].allRetailPrice,
                    s:{

                      border: {
                      top: { style: 'thin' },
                      left: { style: 'thin' },
                      bottom: { style: 'thin' },
                      right: { style: 'thin' },
                    }
                  }
                }
                contentWs["I"+(i+2)]={
                    t:"s",
                    v:biaoge[i].batchNo,
                    s:{

                      border: {
                      top: { style: 'thin' },
                      left: { style: 'thin' },
                      bottom: { style: 'thin' },
                      right: { style: 'thin' },
                    }
                  }
                }
                contentWs["J"+(i+2)]={
                    t:"s",
                    v:biaoge[i].produceDate,
                    s:{

                      border: {
                      top: { style: 'thin' },
                      left: { style: 'thin' },
                      bottom: { style: 'thin' },
                      right: { style: 'thin' },
                    }
                  }
                }
                contentWs["K"+(i+2)]={
                    t:"s",
                    v:biaoge[i].endDate,
                    s:{

                      border: {
                      top: { style: 'thin' },
                      left: { style: 'thin' },
                      bottom: { style: 'thin' },
                      right: { style: 'thin' },
                    }
                  }
                }

                contentWs["L"+(i+2)]={
                    t:"s",
                    v:biaoge[i].createDate,
                    s:{

                      border: {
                      top: { style: 'thin' },
                      left: { style: 'thin' },
                      bottom: { style: 'thin' },
                      right: { style: 'thin' },
                    }
                  }
                }
                contentWs["M"+(i+2)]={
                    t:"s",
                    v:biaoge[i].supplier,
                    s:{

                      border: {
                      top: { style: 'thin' },
                      left: { style: 'thin' },
                      bottom: { style: 'thin' },
                      right: { style: 'thin' },
                    }
                  }
                }
                contentWs["N"+(i+2)]={
                    t:"s",
                    v:biaoge[i].factory,
                    s:{

                      border: {
                      top: { style: 'thin' },
                      left: { style: 'thin' },
                      bottom: { style: 'thin' },
                      right: { style: 'thin' },
                    }
                  }
                }
                if(biaoge[i].beizhu===''){
                   contentWs["O"+(i+2)]={
                    t:"s",
                    v:"",
                    s:{

                      border: {
                      top: { style: 'thin' },
                      left: { style: 'thin' },
                      bottom: { style: 'thin' },
                      right: { style: 'thin' },
                    }
                  }
                }
                }else{
                  contentWs["O"+(i+2)]={
                    t:"s",
                    v:biaoge[i].beizhu,
                    s:{

                      border: {
                      top: { style: 'thin' },
                      left: { style: 'thin' },
                      bottom: { style: 'thin' },
                      right: { style: 'thin' },
                    }
                  }
                }
                }
             }else{
                contentWs["A"+(i+2)]={
                    t:"s",
                    v:biaoge[i].code,
                    s:{
                      alignment:{
                        horizontal:"center",
                        vertical:"center",
                        wrapText:true,
                      },
                      border: {
                      top: { style: 'thin' },
                      left: { style: 'thin' },
                      bottom: { style: 'thin' },
                      right: { style: 'thin' }
                    }
                  }
                }
              contentWs["B"+(i+2)]={
                    t:"s",
                    v:biaoge[i].name,
                    s:{
                      alignment:{
                        horizontal:"center",
                        vertical:"center",
                        wrapText:true,
                      },
                      border: {
                      top: { style: 'thin' },
                      left: { style: 'thin' },
                      bottom: { style: 'thin' },
                      right: { style: 'thin' }
                    }
                  }
                }
                contentWs["C"+(i+2)]={
                    t:"s",
                    v:biaoge[i].norms,
                    s:{
                      alignment:{
                        horizontal:"center",
                        vertical:"center",
                        wrapText:true,
                      },
                      border: {
                      top: { style: 'thin' },
                      left: { style: 'thin' },
                      bottom: { style: 'thin' },
                      right: { style: 'thin' }
                    }
                  }
                }
                contentWs["D"+(i+2)]={
                    t:"s",
                    v:biaoge[i].number,
                    s:{
                      alignment:{
                        horizontal:"center",
                        vertical:"center",
                        wrapText:true,
                      },
                      border: {
                      top: { style: 'thin' },
                      left: { style: 'thin' },
                      bottom: { style: 'thin' },
                      right: { style: 'thin' }
                    }
                  }
                }
                contentWs["E"+(i+2)]={
                    t:"s",
                    v:biaoge[i].bid,
                    s:{
                      alignment:{
                        horizontal:"center",
                        vertical:"center",
                        wrapText:true,
                      },
                      border: {
                      top: { style: 'thin' },
                      left: { style: 'thin' },
                      bottom: { style: 'thin' },
                      right: { style: 'thin' }
                    }
                  }
                }

                contentWs["F"+(i+2)]={
                    t:"s",
                    v:biaoge[i].allBid,
                    s:{
                      alignment:{
                        horizontal:"center",
                        vertical:"center",
                        wrapText:true,
                      },
                      border: {
                      top: { style: 'thin' },
                      left: { style: 'thin' },
                      bottom: { style: 'thin' },
                      right: { style: 'thin' }
                    }
                  }
                }
                contentWs["G"+(i+2)]={
                    t:"s",
                    v:biaoge[i].retailPrice,
                    s:{
                      alignment:{
                        horizontal:"center",
                        vertical:"center",
                        wrapText:true,
                      },
                      border: {
                      top: { style: 'thin' },
                      left: { style: 'thin' },
                      bottom: { style: 'thin' },
                      right: { style: 'thin' }
                    }
                  }
                }
                contentWs["H"+(i+2)]={
                    t:"s",
                    v:biaoge[i].allRetailPrice,
                    s:{
                      alignment:{
                        horizontal:"center",
                        vertical:"center",
                        wrapText:true,
                      },
                      border: {
                      top: { style: 'thin' },
                      left: { style: 'thin' },
                      bottom: { style: 'thin' },
                      right: { style: 'thin' }
                    }
                  }
                }
                contentWs["I"+(i+2)]={
                    t:"s",
                    v:biaoge[i].batchNo,
                    s:{
                      alignment:{
                        horizontal:"center",
                        vertical:"center",
                        wrapText:true,
                      },
                      border: {
                      top: { style: 'thin' },
                      left: { style: 'thin' },
                      bottom: { style: 'thin' },
                      right: { style: 'thin' }
                    }
                  }
                }
                contentWs["J"+(i+2)]={
                    t:"s",
                    v:biaoge[i].produceDate,
                    s:{
                      alignment:{
                        horizontal:"center",
                        vertical:"center",
                        wrapText:true,
                      },
                      border: {
                      top: { style: 'thin' },
                      left: { style: 'thin' },
                      bottom: { style: 'thin' },
                      right: { style: 'thin' }
                    }
                  }
                }
                contentWs["K"+(i+2)]={
                    t:"s",
                    v:biaoge[i].endDate,
                    s:{
                      alignment:{
                        horizontal:"center",
                        vertical:"center",
                        wrapText:true,
                      },
                      border: {
                      top: { style: 'thin' },
                      left: { style: 'thin' },
                      bottom: { style: 'thin' },
                      right: { style: 'thin' }
                    }
                  }
                }

                contentWs["L"+(i+2)]={
                    t:"s",
                    v:biaoge[i].createDate,
                    s:{
                      alignment:{
                        horizontal:"center",
                        vertical:"center",
                        wrapText:true,
                      },
                      border: {
                      top: { style: 'thin' },
                      left: { style: 'thin' },
                      bottom: { style: 'thin' },
                      right: { style: 'thin' }
                    }
                  }
                }
                contentWs["M"+(i+2)]={
                    t:"s",
                    v:biaoge[i].supplier,
                    s:{
                      alignment:{
                        horizontal:"center",
                        vertical:"center",
                        wrapText:true,
                      },
                      border: {
                      top: { style: 'thin' },
                      left: { style: 'thin' },
                      bottom: { style: 'thin' },
                      right: { style: 'thin' }
                    }
                  }
                }
                contentWs["N"+(i+2)]={
                    t:"s",
                    v:biaoge[i].factory,
                    s:{
                      alignment:{
                        horizontal:"center",
                        vertical:"center",
                        wrapText:true,
                      },
                      border: {
                      top: { style: 'thin' },
                      left: { style: 'thin' },
                      bottom: { style: 'thin' },
                      right: { style: 'thin' }
                    }
                  }
                }
                if(biaoge[i].beizhu===''){
                   contentWs["O"+(i+2)]={
                    t:"s",
                    v:"",
                    s:{
                      alignment:{
                        horizontal:"center",
                        vertical:"center",
                        wrapText:true,
                      },
                      border: {
                      top: { style: 'thin' },
                      left: { style: 'thin' },
                      bottom: { style: 'thin' },
                      right: { style: 'thin' }
                    }
                  }
                }
                }else{
                  contentWs["O"+(i+2)]={
                    t:"s",
                    v:biaoge[i].beizhu,
                    s:{
                      alignment:{
                        horizontal:"center",
                        vertical:"center",
                        wrapText:true,
                      },
                      border: {
                      top: { style: 'thin' },
                      left: { style: 'thin' },
                      bottom: { style: 'thin' },
                      right: { style: 'thin' }
                    }
                  }
                }
                }
             }

            }


             const mergeTitle = [

              {
                s: {r: 0, c: 0},
                e: {r: 0, c: 14}
              },

              ]

              //Cố định dòng đầu tiên và cột đầu tiên: 
              contentWs['!freeze'] = {
                xSplit: "11",  //Cố định cột
                ySplit: "1",  //Cố định dòng
                topLeftCell: "A2", //Ô hiển thị ở góc trên bên trái của vùng chưa cố định, Mặc định là ô chưa cố định đầu tiên
                state: "frozen"
              }

            contentWs["!merges"]=mergeTitle

            contentWs["!cols"]=[{wch:20},{wch:20},{wch:12},{wch:10},{wch:10},{wch:15},{wch:15},{wch:15},{wch:15},{wch:10},{wch:10},{wch:10},{wch:20},{wch:20},{wch:15}]
            contentWs["!rows"]={}

            contentWs["!margins"]={left:1.0,right:1.0,top:1.0,bottom:1.0,header:0.5,footer:0.5}





            XLSX.utils.book_append_sheet(web,contentWs,"Bảng chi tiết nhập kho");

            // //Sử dụngxlsx-styleCách ghi tệp, để kiểu tùy chỉnh có hiệu lực
            const tmpDown=new Blob([
              this.s2ab(
                XLSXD.write(web,{
                  bookType:"xlsx",
                  BookSST:true,
                  type:"binary",
                  cellStyles:true,
                })
              )
            ])
          //  XLSX.writeFile(web,'Bảng chi tiết nhập kho.xlsx');
           this.downExcel(tmpDown,'Bảng chi tiết nhập kho.xlsx')
      },
      downExcel(obj,fileName){
        const a_node=document.createElement("a");
        a_node.download=fileName;
        if("msSaveOrOpenBlob" in navigator){
          window.navigator.msSaveOrOpenBlob(obj,fileName)
        }else{
          a_node.href=URL.createObjectURL(obj);
        }
        a_node.click()

        setTimeout(()=>{
          URL.revokeObjectURL(obj)
        },2000)
      },
      s2ab(s){
        if(typeof ArrayBuffer !=="undefined"){
          const buf=new ArrayBuffer(s.length);
          const view=new Uint8Array(buf);
          for(let i=0;i!==s.length;++i){
            view[i]=s.charCodeAt(i)&0xff
          }
          return buf;
        }else{
          const buf=new Array(s.length);
          for(let i=0;i!==s.length;++i){
            buf[i]=s.charCodeAt(i)&0xff;
          }
          return buf;
        }
      },
      indexMethod(index){
       return (this.storageTableCurrentPage-1)*this.storageTablePagesize+index +1;
    },
      indexMethod2(index){
       return (this.storageDetailTableCurrentPage-1)*this.storageDetailTablePagesize+index +1;
    }
  },
  mounted() {
    this.GetSysParamConfig();
    this.pageInit();
  },
};
</script>


<style rel="stylesheet/scss" lang="scss" scoped>
.page-container{
  padding: 0;
}
.examine-box {
  padding: 15px 20px;
  position: relative;
}
.examine {
  color: red;
  position: absolute;
  top: 10px;
  right: 0;
  transform: rotate(45deg);
}

.bg_btn button {
  width: 100px;
}

#conditionRow {
  margin-top: 1%;
}
#conditionOperation {
  margin-top: 1%;
}

.spaninput {
  display: flex;
  text-overflow: ellipsis;
  overflow: hidden;
  white-space: nowrap;
  font-size: 12px;
}
::v-deep .el-table__fixed-right-patch{
    width:5px !important
  }
  ::v-deep .el-table colgroup col[name='gutter']{
    width:5px !important
  }
  ::v-deep .el-table__body{
    width:100% !important
  }
</style>
