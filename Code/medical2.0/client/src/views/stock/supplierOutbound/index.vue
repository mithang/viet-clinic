<template>
  <el-card class="page-container">
    <div id="storage">
      <el-row id="conditionRow">
        <el-col :span="8" style="text-align:center">
          <el-date-picker :picker-options="filerData.pickerOptions" v-model="condition.dateRange" type="datetimerange"
            range-separator="đến" start-placeholder="Ngày bắt đầu" end-placeholder="Ngày kết thúc"
            :default-time="['00:00:00', '23:59:59']">
          </el-date-picker>
        </el-col>
        <el-col :span="4">
          <SearchItem label="Loại">
            <DrugStuffTypeSelect v-model="condition.type"></DrugStuffTypeSelect>
          </SearchItem>
          <!-- <el-select v-model="condition.type" placeholder="Loại">
            <el-option label="Tất cả" value="">Tất cả</el-option>
            <el-option
              v-for="typexz in typeList"
              :label="typexz.name"
              :value="typexz.value"
              :key="typexz.value"
            ></el-option>
          </el-select>-->
        </el-col>
        <el-col :span="4">
          <SearchItem label="Trạng thái">
            <SupplierOutboundStatusSelect v-model="condition.status"></SupplierOutboundStatusSelect>
          </SearchItem>
          <!--   <el-select v-model="condition.status" placeholder="出库单状态">
            <el-option label="Tất cả" value="">Tất cả</el-option>
            <el-option
              v-for="statusxz in statusList"
              :label="statusxz.name"
              :value="statusxz.value"
              :key="statusxz.value"
            ></el-option>
          </el-select>-->
        </el-col>
        <el-col :span="4">

          <SearchItem label="Phương thức xuất kho">
            <!--   <SupplierOutboundMethodSelect v-model="condition.method"></SupplierOutboundMethodSelect> -->
            <DictItemSelect type="outboundMethod" v-model="condition.method" :showHeader="true"></DictItemSelect>
          </SearchItem>
          <!--
          <el-select v-model="condition.method" placeholder="Phương thức xuất kho">
            <el-option label="Tất cả" value="">Tất cả</el-option>
            <el-option
              v-for="methodxz in methodList"
              :key="methodxz.name"
              :label="methodxz.name"
              :value="methodxz.name"
            >
            </el-option>
          </el-select>-->
        </el-col>
        <el-col :span="3" style="display:flex;justivy-content:space-around">
          <el-button type="primary" icon="el-icon-search" @click="querySupplierStorageList()" :plain="true">Tìm kiếm
          </el-button>
          <el-button type="info" icon="el-icon-refresh-left" @click="resetCondition()" :plain="true">Đặt lại</el-button>
        </el-col>
      </el-row>
      <el-row style="margin-top:10px;margin-bottom:10px">
        <el-col :span="24" style="text-align:right">
          <div class="bg_btn">
            <el-button type="primary" @click="editAndAddOutbound(1)">Thêm mới thuốc</el-button>
            <el-button type="primary" @click="editAndAddOutbound(2)">Thêm mới vật tư</el-button>
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
          <el-table border height="480" :data="storageTable" style="width: 100%" highlight-current-row
            @selection-change="selectionChange">
            <el-table-column type="selection" width="55">
            </el-table-column>
            <el-table-column type="index" :index="indexMethod" width="50px" label="Số thứ tự" align="center">
            </el-table-column>
            <el-table-column prop="code" label="Số đơn xuất kho">
            </el-table-column>
            <el-table-column prop="method" label="Phương thức xuất kho" width="80px">
              <template slot-scope="scope">
                <DictItemView type="outboundMethod" :dictValue="scope.row.method"></DictItemView>
              </template>
            </el-table-column>
            <el-table-column prop="type" label="Loại vật phẩm" width="80px">
              <template slot-scope="scope">
                <span v-if="scope.row.type==='1'">
                  Thuốc
                </span>
                <span v-if="scope.row.type==='2'">
                  Vật tư
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="createBy" label="出库人"></el-table-column>
            <el-table-column prop="createDate" label="出库日期"></el-table-column>
            <el-table-column prop="examineBy" label="审批人"></el-table-column>
            <el-table-column prop="examineDate" label="Ngày phê duyệt"></el-table-column>
            <!--  <el-table-column prop="purchasing" label="进价总额"></el-table-column>
         <el-table-column prop="retail" label="零售总额"></el-table-column>-->
            <el-table-column prop="examine.name" label="Trạng thái" width="80px">
              <template slot-scope="scope">
                <SupplierOutboundStatusView v-model="scope.row.status"></SupplierOutboundStatusView>
              </template>
            </el-table-column>
            <el-table-column label="Thao tác" align="center" width="120">
              <template slot-scope="scope">
               <OperationIcon  content='Xem'  type="info"  icon-name="el-icon-view"  @click="onView(scope.$index,scope.row)"></OperationIcon>
               <OperationIcon v-if="scope.row.status===1"  content='Duyệt'  type="primary"  icon-name="el-icon-document"  @click="onAudit(scope.$index,scope.row)"></OperationIcon>
                <OperationIcon v-if="scope.row.status===1"  content='Sửa'  type="primary"  icon-name="el-icon-edit"  @click="onUpdate(scope.$index,scope.row)"></OperationIcon>
                <OperationIcon v-if="scope.row.status===1"  content='撤消'  type="primary"  icon-name="el-icon-delete"  @click="onCancel(scope.$index,scope.row)"></OperationIcon>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination @size-change="storageTableSizeChange" @current-change="storageTableCurrentChange"
            :page-sizes="[100,200,storageTableTotal]" :page-size="storageTablePagesize"
            :current-page="storageTableCurrentPage" layout="total,sizes, prev, pager, next,jumper"
            :total="storageTableTotal">
          </el-pagination>
        </el-col>
      </el-row>
      <AddStorageTo @fatherMethod="closeDialog" @onSupplierOutboundUpdated="onSupplierOutboundUpdated"
        :title="dialogTitle" :addType="addType" ref="uAddStorage"></AddStorageTo>
      <SupplierOutboundView @fatherMethod="closeDialog"  title="出库单详情" ref="uOutboundView">
      </SupplierOutboundView>
    </div>
  </el-card>
</template>

<script>
  import SupplierOutboundView from "./components/supplierOutboundView";
  import AddStorageTo from "./components/addstorageTo";
  import {
    listSupplierPage,
    getSupplierById,
    listSupplierAll
  } from "@/api/stock/supplier";
  import {
    bulkUpdateSupplierStorage,
    bulkUpdateSupplierStorage1,
    listSupplierStorageAll,
    listSupplierStoragePage,
  } from "@/api/stock/supplierStorage";
  import {
    listSupplierStockAll,
    listSupplierStockPage,
    getSupplierStockBySid,
  } from "@/api/stock/supplierStock";
  import {
    listSupplierOutbound,
    cancelSupplierOutbound,
    examineSupplierOutbound
  } from "@/api/stock/supplierOutbound";
  import {
    listDictItemAll
  } from "@/api/sys/dictItem";

  import DrugStuffTypeSelect from "@/views/components/drug-stuff-type-select/index.vue";
  import SupplierOutboundMethodSelect from "@/views/components/supplier-outbound-method-select/index.vue";
  import SupplierOutboundStatusSelect from "@/views/components/supplier-outbound-status-select/index.vue";
  import SupplierOutboundStatusView from "@/views/components/supplier-outbound-status-view/index.vue";
  import SearchItem from "@/views/components/search-item/index.vue";
  import DictItemView from "@/views/components/dict-item-view/index.vue";
  import DictItemSelect from "@/views/components/dict-item-select/index.vue";
import OperationIcon from '@/components/OperationIcon'
  import FileSaver from 'file-saver'
  import * as XLSX from 'xlsx'
  //import XLSX from 'xlsx'
  import XLSXD from 'xlsx-style'
  export default {
    components: {
      SupplierOutboundView,
      AddStorageTo,
      DrugStuffTypeSelect,
      SupplierOutboundMethodSelect,
      SupplierOutboundStatusSelect,
      SupplierOutboundStatusView,
      SearchItem,
      DictItemView,
      DictItemSelect,
      OperationIcon
    },
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
        company_id: "" /*Phòng khámID - bởicurrentUser.company.idLấy động*/ ,
        examine_destroy_id: "1005787933775863932",
        examine_destroy_value: "supplierStorageExamineStatus_2" /*Trạng thái hủy bỏvalue*/ ,
        storageTableCurrentPage: 1,
        storageTablePagesize: 100 /*Số dòng hiển thị tối đa của bảng chính*/ ,
        storageTableTotal: 0 /*Tổng số bản ghi của bảng chính*/ ,
        storageDetailTableCurrentPage: 1,
        storageDetailTablePagesize: 5 /*Số dòng hiển thị tối đa của bảng chính*/ ,
        storageDetailTableTotal: 0 /*Tổng số bản ghi của bảng chính*/ ,
        supplierList: null,
        breedList: null,
        typeList: null,
        statusList: null,
        methodList: null,
        storageCode: '', //Số đơn xuất kho
        supplierName: '', //Tên nhà cung cấp
        initialNum: [], //Gán giá trị cho bảng chi tiết
        invalid: [], //Chi tiết hủy bỏ
        condition: {
          supplier: {
            id: null,
          },
          method: null,
          type: null,
          status: null,
          dateRange: [] /*Khoảng thời gian*/ ,
          batchNo: "",
        },
        storageTable: null,
        otherInfo: {
          withGoodsNo: "" /*Số đơn cung ứng*/ ,
          invoiceNumber: "" /*Số hóa đơn*/ ,
        },
        storageDetailTable: null,
        dialogVisible: false,
        dialogTitle: "",
        isAdd: 0 /*0Sửa 1Thêm mới*/ ,
        addtype: 1, //新增出库单类型 1là thuốc 2là vật tư
        selectStorage: [],
        supplierSearch: {
          params: [{
            columnName: "company_id",
            queryType: "=",
            value: ""
          }],
          offset: 0,
          limit: 100,
          columnName: "", // Tên trường sắp xếp
          order: "", // Sắp xếp
        },
        statusSearch: {
          params: [{
            columnName: "company_id",
            queryType: "=",
            value: ""
          }],
          offset: 0,
          limit: 100,
          columnName: "", // Tên trường sắp xếp
          order: "", // Sắp xếp
        },
        methodSearch: {
          params: [{
            columnName: "company_id",
            queryType: "=",
            value: ""
          }],
          offset: 0,
          limit: 100,
          columnName: "", // Tên trường sắp xếp
          order: "", // Sắp xếp
        },
        typeSearch: {
          params: [{
            columnName: "company_id",
            queryType: "=",
            value: ""
          }],
          offset: 0,
          limit: 100,
          columnName: "", // Tên trường sắp xếp
          order: "", // Sắp xếp
        },
        supplierStorageSearch: {
          params: [{
            columnName: "company_id",
            queryType: "=",
            value: ""
          }],
          offset: 0,
          limit: 10000,
          columnName: "code", // Tên trường sắp xếp
          order: "desc", // Sắp xếp
        },
        supplierStockSearch: {
          params: [{
            columnName: "company_id",
            queryType: "=",
            value: ""
          }],
          offset: 0,
          limit: 10000,
          columnName: "", // Tên trường sắp xếp
          order: "", // Sắp xếp
        }
      };
    },
    provide() {
      return {
        fatherMethod: this.reload,
      };
    },
    methods: {
      onView(type, row) {
        let outboundId = row.id;
        this.$refs.uOutboundView.openDialog(outboundId);
      },
      onAudit(type, row) {
        this.$confirm('Bạn có chắc duyệt đạt không?', 'Xác nhận', {
          confirmButtonText: 'Xác nhận',
          cancelButtonText: 'Hủy',
          type: 'warning'
        }).then(() => {
          examineSupplierOutbound(row.id).then(res => {
            if (res.code === 100) {
              this.$message({
                type: 'success',
                message: 'Xét duyệt thành công'
              })
            } else {
              this.$message(res.msg)
            }
            this.querySupplierStorageList()
          }).catch(error => {
            console.error('Thao tác thất bại',error);
          })
        }).catch(() => {})
      },
      onCancel(index, row) {
        let that=this;
        this.$confirm('Bạn có chắc hủy phiếu xuất kho không?', 'Xác nhận', {
          confirmButtonText: 'Xác nhận',
          cancelButtonText: 'Hủy',
          type: 'warning'
        }).then(() => {
          cancelSupplierOutbound(row.id).then(res => {
            if (res.code === 100) {
              that.$message({
                type: 'success',
                message: '撤消成功'
              })
            } else {
              that.$message(res.msg)
            }
            that.querySupplierStorageList()
          }).catch(error => {
            //this.outputError(error)
            console.error('撤消失败',error);
          })
        }).catch(() => {})
      },
      onUpdate(index, row) {
        let outboundId = row.id;
        this.dialogTitle = "修改出库单";
        this.$refs.uAddStorage.openDialog(outboundId);
      },
      selectionChange(rows) {
        if (rows.length === 0) {
          this.btnType = false;
        }
        this.selectStorage = rows;
      },
      pageInit() {
        const day = new Date();
        // this.condition.dateRange = [
        //   new Date(day.getFullYear(), day.getMonth(), day.getDate(), 0, 0),
        //   new Date(day.getFullYear(), day.getMonth(), day.getDate(), 23, 59, 59),
        // ];

        /* this.supplierSearch.params = [{
          columnName: "company_id",
          queryType: "=",
          value: currentUser.company.id,
        }, ];
        listSupplierAll(this.supplierSearch)
          .then((responseData) => {
            if (responseData.code == 100) {
              this.supplierList = responseData.data;
            } else {
              this.$message.error(responseData);
            }
          })
          .catch((error) => {
            this.$message.error(error);
          });

        this.statusSearch.params = [{
          columnName: "dict_type_id",
          queryType: "=",
          value: "1005787933775863928",
        }, ];
        listDictItemAll(this.statusSearch).then((responseData) => {
          this.statusList = responseData.data;
        });

        this.typeSearch.params = [{
          columnName: "dict_type_id",
          queryType: "=",
          value: "1607184304135529357",
        }, ];
        listDictItemAll(this.typeSearch).then((responseData) => {
          this.typeList = responseData.data;
        });

        this.methodSearch.params = [{
          columnName: "dict_type_id",
          queryType: "=",
          value: "1607184304135529252",
        }, ];
        listDictItemAll(this.methodSearch).then((responseData) => {
          this.methodList = responseData.data;
        }); */

        this.querySupplierStorageList();
      },
      reload() {
          this.$router.go(0)
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
        this.supplierStorageSearch.offset =
          (this.storageTableCurrentPage - 1) * this.storageTablePagesize;
        this.supplierStorageSearch.limit = this.storageTablePagesize;
        this.supplierStorageSearch.params = [{
          columnName: "company_id",
          queryType: "=",
          value: currentUser.company.id
        }, ];
        /* if (this.condition.supplier.id) {
          this.supplierStorageSearch.params.push({
            columnName: "supplier_id",
            queryType: "=",
            value: this.condition.supplier.id,
          });
        } */
        if (this.condition.method) {
          this.supplierStorageSearch.params.push({
            columnName: "method",
            queryType: "=",
            value: this.condition.method,
          });
        }
        if (this.condition.status) {
          this.supplierStorageSearch.params.push({
            columnName: "status",
            queryType: "=",
            value: this.condition.status,
          });
        }

        if (this.condition.type) {
          this.supplierStorageSearch.params.push({
            columnName: "type",
            queryType: "=",
            value: this.condition.type, // 1 2 type-1
          });
        }

        /* if (this.condition.batchNo) {
          this.supplierStorageSearch.params.push({
            columnName: "ss.batch_no",
            queryType: "=",
            value: this.condition.batchNo,
          });
        } */

        this.supplierStorageSearch.params.push({
          columnName: "create_date",
          queryType: ">=",
          value: this.condition.dateRange[0] ?
            this.$moment(this.condition.dateRange[0]).format(
              "YYYY-MM-DD HH:mm:ss"
            ) : "",
        });
        this.supplierStorageSearch.params.push({
          columnName: "create_date",
          queryType: "<=",
          value: this.condition.dateRange[1] ?
            this.$moment(this.condition.dateRange[1]).format(
              "YYYY-MM-DD HH:mm:ss"
            ) : "",
        });
        listSupplierOutbound(this.supplierStorageSearch)
          .then((responseData) => {
            if (responseData.code === 100) {
              if (responseData.data.rows != null) {
                let arr = []
                arr = responseData.data.rows;
                // for (let i = 0; i < arr.length; i++) {

                //   getSupplierStockBySid(arr[i].id).then((res)=>{
                //        if(res.data[0].stuff.name==''){
                //        arr[i].types='Thuốc'

                //        //arr[i].number=Math.floor(res.data.number/res.data.drug.preparation) > 0 ? Math.floor(res.data.number/res.data.drug.preparation)+(res.data.norms.split("/")[1])+((res.data.number%res.data.drug.preparation > 0) ? (res.data.number%res.data.drug.preparation) + res.data.norms.split("*")[1].split("/")[0].split("")[res.data.norms.split("*")[1].split("/")[0].split("").length-1]:""):res.data.number+res.data.norms.split("*")[1].split("/")[0].split("")[res.data.norms.split("*")[1].split("/")[0].split("").length-1]

                //      }else if(res.data[0].stuff.name!=''){
                //        arr[i].types='Vật tư'

                //       // arr[i].number=Math.floor(res.data.number/res.data.stuff.packNumber) > 0 ? Math.floor(res.data.number/res.data.stuff.packNumber)+(res.data.norms.split("*")[1])+((res.data.number%res.data.stuff.packNumber > 0) ? (res.data.number%res.data.stuff.packNumber)+res.data.norms.split("*")[0].split("")[res.data.norms.split("*")[0].split("").length-1]:""):res.data.number+res.data.norms.split("*")[0].split("")[res.data.norms.split("*")[0].split("").length-1]

                //     }

                //   }).catch(()=>{})

                // }

                clearTimeout(this.timer);
                this.timer = setTimeout(() => {
                  this.storageTable = arr
                  this.storageTableTotal = responseData.data.total;
                }, 400)

                this.clearOtherAndDetailData();
              } else {
                this.storageTable = responseData.data.rows
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
        this.storageCode = row.code
        this.supplierName = row.supplier.name
        this.btnType = true;
        this.selectStorage = row;
        this.supplierStockSearch.offset =
          (this.storageDetailTableCurrentPage - 1) *
          this.storageDetailTablePagesize;
        this.supplierStockSearch.limit = this.storageDetailTablePagesize;
        this.supplierStockSearch.params = [{
            columnName: "company_id",
            queryType: "=",
            value: currentUser.company.id
          },
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
              this.invalid = responseData.data.rows
              let initalArr = []
              initalArr = responseData.data.rows;

              this.initialNum = []
              for (let i = 0; i < initalArr.length; i++) {
                initalArr[i].code = this.selectStorage.code
                this.initialNum.push(initalArr[i].number)
                if (initalArr[i].drug.goodsName !== '') {
                  initalArr[i].number = Math.floor(initalArr[i].number / initalArr[i].drug.preparation) >= 0 ? Math
                    .floor(initalArr[i].number / initalArr[i].drug.preparation) + (initalArr[i].norms.split("/")[1]) +
                    ((initalArr[i].number % initalArr[i].drug.preparation > 0) ? (initalArr[i].number % initalArr[i]
                      .drug.preparation) + initalArr[i].norms.split("*")[1].split("/")[0].split("")[initalArr[i]
                      .norms.split("*")[1].split("/")[0].split("").length - 1] : "") : initalArr[i].number +
                    initalArr[i].norms.split("*")[1].split("/")[0].split("")[initalArr[i].norms.split("*")[1].split(
                      "/")[0].split("").length - 1]
                } else if (initalArr[i].stuff.name !== '') {
                  initalArr[i].number = Math.floor(initalArr[i].number / initalArr[i].stuff.packNumber) >= 0 ? Math
                    .floor(initalArr[i].number / initalArr[i].stuff.packNumber) + (initalArr[i].norms.split("*")[1]) +
                    ((initalArr[i].number % initalArr[i].stuff.packNumber > 0) ? (initalArr[i].number % initalArr[i]
                      .stuff.packNumber) + initalArr[i].norms.split("*")[0].split("")[initalArr[i].norms.split("*")[
                      0].split("").length - 1] : "") : initalArr[i].number + initalArr[i].norms.split("*")[0].split(
                      "")[initalArr[i].norms.split("*")[0].split("").length - 1]
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
      editAndAddOutbound(type) {
        this.addType = type
        this.isAdd = 1;
        if (this.addType === 1) {
          this.dialogTitle = "新增药品出库单";
        } else {
          this.dialogTitle = "新增材料出库单";
        }
        this.$refs.uAddStorage.openDialog();
      },
      onSupplierOutboundUpdated() {
        this.querySupplierStorageList();
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
          type: null,
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
          ] /*Khoảng thời gian*/ ,
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
      //Xuất bảng
      exportExcel() {
        let biaoge = []
        biaoge[0] = {
          code: "Số chứng từ",
          name: "Thuốc/Tên vật tư",
          norms: "Quy cách",
          number: "Số lượng",
          bid: "Giá nhập",
          allBid: "Tổng giá nhập",
          retailPrice: "Giá bán lẻ",
          allRetailPrice: "Tổng giá bán lẻ",
          batchNo: "Số lô sản xuất",
          produceDate: "Ngày sản xuất",
          endDate: "Hạn sử dụng",
          createDate: "出库时间",
          supplier: "Đơn vị cung ứng",
          factory: "Tên nhà sản xuất",
          beizhu: "Ghi chú"
        }

        let numbers = 0;
        let allRetailPrices = 0
        for (let i = 0; i < this.storageDetailTable.length; i++) {
          let arr = {
            code: '',
            name: '',
            norms: '',
            number: '',
            bid: '',
            allBid: '',
            retailPrice: '',
            allRetailPrice: '',
            batchNo: '',
            produceDate: '',
            endDate: '',
            createDate: '',
            supplier: '',
            factory: '',
          }

          if (this.storageDetailTable[i].drug.goodsName === '') {
            arr.name = this.storageDetailTable[i].stuff.name
          } else {
            arr.name = this.storageDetailTable[i].drug.goodsName
          }
          arr.norms = this.storageDetailTable[i].norms
          arr.factory = this.storageDetailTable[i].factory.name
          arr.batchNo = this.storageDetailTable[i].batchNo
          arr.code = this.storageDetailTable[i].code
          arr.supplier = this.storageDetailTable[i].supplier
          arr.createDate = this.storageDetailTable[i].createDate.split(" ")[0]
          arr.produceDate = this.storageDetailTable[i].produceDate.split(" ")[0]
          arr.endDate = this.storageDetailTable[i].endDate.split(" ")[0]
          arr.number = this.storageDetailTable[i].number
          arr.bid = this.storageDetailTable[i].bid + ''
          // arr.leastBid=this.storageDetailTable[i].leastBid+''
          arr.allBid = this.storageDetailTable[i].allBid + ''
          arr.retailPrice = this.storageDetailTable[i].retailPrice + ''
          arr.allRetailPrice = this.storageDetailTable[i].allRetailPrice + ''
          arr.beizhu = ''
          biaoge.push(arr)
          numbers += this.storageDetailTable[0].number.substring(0, this.storageDetailTable[0].number.length - 1) - 0
          allRetailPrices += this.storageDetailTable[i].allRetailPrice

        }
        let num = biaoge.length
        biaoge[num] = {
          code: 'Tổng cộng',
          name: "",
          norms: "",
          number: "",
          bid: "",
          //  leastBid:"",
          allBid: "",
          retailPrice: "",
          allRetailPrice: "" + allRetailPrices,
          batchNo: "",
          produceDate: "",
          endDate: "",
          createDate: '',
          //  number:""+numbers,
          supplier: '',
          factory: "",
          beizhu: ""
        }
        // biaoge[num+1]={
        //      code:'Người lập đơn：',
        //      name:"",
        //      norms:"",
        //      number:"",
        //      bid:"",
        //     //  leastBid:"",
        //      allBid:"",
        //      retailPrice:"",
        //      allRetailPrice:"Thủ kho：",
        //      batchNo:"",
        //      produceDate:"",
        //      endDate:"",
        //      createDate:'Người nhận hàng：',
        //      supplier:'',
        //      factory:"",
        //      beizhu:"Ngày nhận hàng："
        // }

        let web = XLSX.utils.book_new();
        let headers = {
          name: "Thuốc/Tên vật tư",
          norms: "Quy cách",
          factory: "Tên nhà sản xuất",
          batchNo: "Số lô sản xuất",
          produceDate: "Ngày sản xuất",
          endDate: "Hạn sử dụng",
          number: "Số lượng",
          bid: "Giá nhập",
          leastBid: "Giá nhập theo đơn vị nhỏ nhất",
          allBid: "Tổng giá nhập",
          retailPrice: "Giá bán lẻ",
          allRetailPrice: "Tổng giá bán lẻ"
        }
        //biaoge.unshift(headers)
        // let contentWs = XLSX.utils.json_to_sheet(biaoge,{
        //   headers:['name','norms','factory','batchNo','produceDate','endDate','number','bid','leastBid','allBid','retailPrice','allRetailPrice'],
        //   skipHeader:false,
        //   orgin: "A4",
        // });
        let contentWs = XLSX.utils.json_to_sheet(biaoge);

        contentWs["A1"] = {
          t: "s",
          v: this.storageDetailTable[0].company.name + ' Bảng chi tiết xuất kho',
          s: {
            alignment: {
              horizontal: "center",
              vertical: "center",
              wrapText: true,
            },
          }
        }

        for (let i = 0; i < biaoge.length; i++) {
          if (i === biaoge.length - 1) {
            contentWs["A" + (i + 2)] = {
              t: "s",
              v: biaoge[i].code,
              s: {
                border: {
                  top: {
                    style: 'thin'
                  },
                  left: {
                    style: 'thin'
                  },
                  bottom: {
                    style: 'thin'
                  },
                  right: {
                    style: 'thin'
                  },
                }
              }
            }
            contentWs["B" + (i + 2)] = {
              t: "s",
              v: biaoge[i].name,
              s: {

                border: {
                  top: {
                    style: 'thin'
                  },
                  left: {
                    style: 'thin'
                  },
                  bottom: {
                    style: 'thin'
                  },
                  right: {
                    style: 'thin'
                  },
                }
              }
            }
            contentWs["C" + (i + 2)] = {
              t: "s",
              v: biaoge[i].norms,
              s: {

                border: {
                  top: {
                    style: 'thin'
                  },
                  left: {
                    style: 'thin'
                  },
                  bottom: {
                    style: 'thin'
                  },
                  right: {
                    style: 'thin'
                  },
                }
              }
            }
            contentWs["D" + (i + 2)] = {
              t: "s",
              v: biaoge[i].number,
              s: {

                border: {
                  top: {
                    style: 'thin'
                  },
                  left: {
                    style: 'thin'
                  },
                  bottom: {
                    style: 'thin'
                  },
                  right: {
                    style: 'thin'
                  },
                }
              }
            }
            contentWs["E" + (i + 2)] = {
              t: "s",
              v: biaoge[i].bid,
              s: {

                border: {
                  top: {
                    style: 'thin'
                  },
                  left: {
                    style: 'thin'
                  },
                  bottom: {
                    style: 'thin'
                  },
                  right: {
                    style: 'thin'
                  },
                }
              }
            }

            contentWs["F" + (i + 2)] = {
              t: "s",
              v: biaoge[i].allBid,
              s: {

                border: {
                  top: {
                    style: 'thin'
                  },
                  left: {
                    style: 'thin'
                  },
                  bottom: {
                    style: 'thin'
                  },
                  right: {
                    style: 'thin'
                  },
                }
              }
            }
            contentWs["G" + (i + 2)] = {
              t: "s",
              v: biaoge[i].retailPrice,
              s: {

                border: {
                  top: {
                    style: 'thin'
                  },
                  left: {
                    style: 'thin'
                  },
                  bottom: {
                    style: 'thin'
                  },
                  right: {
                    style: 'thin'
                  },
                }
              }
            }
            contentWs["H" + (i + 2)] = {
              t: "s",
              v: biaoge[i].allRetailPrice,
              s: {

                border: {
                  top: {
                    style: 'thin'
                  },
                  left: {
                    style: 'thin'
                  },
                  bottom: {
                    style: 'thin'
                  },
                  right: {
                    style: 'thin'
                  },
                }
              }
            }
            contentWs["I" + (i + 2)] = {
              t: "s",
              v: biaoge[i].batchNo,
              s: {

                border: {
                  top: {
                    style: 'thin'
                  },
                  left: {
                    style: 'thin'
                  },
                  bottom: {
                    style: 'thin'
                  },
                  right: {
                    style: 'thin'
                  },
                }
              }
            }
            contentWs["J" + (i + 2)] = {
              t: "s",
              v: biaoge[i].produceDate,
              s: {

                border: {
                  top: {
                    style: 'thin'
                  },
                  left: {
                    style: 'thin'
                  },
                  bottom: {
                    style: 'thin'
                  },
                  right: {
                    style: 'thin'
                  },
                }
              }
            }
            contentWs["K" + (i + 2)] = {
              t: "s",
              v: biaoge[i].endDate,
              s: {

                border: {
                  top: {
                    style: 'thin'
                  },
                  left: {
                    style: 'thin'
                  },
                  bottom: {
                    style: 'thin'
                  },
                  right: {
                    style: 'thin'
                  },
                }
              }
            }

            contentWs["L" + (i + 2)] = {
              t: "s",
              v: biaoge[i].createDate,
              s: {

                border: {
                  top: {
                    style: 'thin'
                  },
                  left: {
                    style: 'thin'
                  },
                  bottom: {
                    style: 'thin'
                  },
                  right: {
                    style: 'thin'
                  },
                }
              }
            }
            contentWs["M" + (i + 2)] = {
              t: "s",
              v: biaoge[i].supplier,
              s: {

                border: {
                  top: {
                    style: 'thin'
                  },
                  left: {
                    style: 'thin'
                  },
                  bottom: {
                    style: 'thin'
                  },
                  right: {
                    style: 'thin'
                  },
                }
              }
            }
            contentWs["N" + (i + 2)] = {
              t: "s",
              v: biaoge[i].factory,
              s: {

                border: {
                  top: {
                    style: 'thin'
                  },
                  left: {
                    style: 'thin'
                  },
                  bottom: {
                    style: 'thin'
                  },
                  right: {
                    style: 'thin'
                  },
                }
              }
            }
            if (biaoge[i].beizhu === '') {
              contentWs["O" + (i + 2)] = {
                t: "s",
                v: "",
                s: {

                  border: {
                    top: {
                      style: 'thin'
                    },
                    left: {
                      style: 'thin'
                    },
                    bottom: {
                      style: 'thin'
                    },
                    right: {
                      style: 'thin'
                    },
                  }
                }
              }
            } else {
              contentWs["O" + (i + 2)] = {
                t: "s",
                v: biaoge[i].beizhu,
                s: {

                  border: {
                    top: {
                      style: 'thin'
                    },
                    left: {
                      style: 'thin'
                    },
                    bottom: {
                      style: 'thin'
                    },
                    right: {
                      style: 'thin'
                    },
                  }
                }
              }
            }
          } else {
            contentWs["A" + (i + 2)] = {
              t: "s",
              v: biaoge[i].code,
              s: {
                alignment: {
                  horizontal: "center",
                  vertical: "center",
                  wrapText: true,
                },
                border: {
                  top: {
                    style: 'thin'
                  },
                  left: {
                    style: 'thin'
                  },
                  bottom: {
                    style: 'thin'
                  },
                  right: {
                    style: 'thin'
                  }
                }
              }
            }
            contentWs["B" + (i + 2)] = {
              t: "s",
              v: biaoge[i].name,
              s: {
                alignment: {
                  horizontal: "center",
                  vertical: "center",
                  wrapText: true,
                },
                border: {
                  top: {
                    style: 'thin'
                  },
                  left: {
                    style: 'thin'
                  },
                  bottom: {
                    style: 'thin'
                  },
                  right: {
                    style: 'thin'
                  }
                }
              }
            }
            contentWs["C" + (i + 2)] = {
              t: "s",
              v: biaoge[i].norms,
              s: {
                alignment: {
                  horizontal: "center",
                  vertical: "center",
                  wrapText: true,
                },
                border: {
                  top: {
                    style: 'thin'
                  },
                  left: {
                    style: 'thin'
                  },
                  bottom: {
                    style: 'thin'
                  },
                  right: {
                    style: 'thin'
                  }
                }
              }
            }
            contentWs["D" + (i + 2)] = {
              t: "s",
              v: biaoge[i].number,
              s: {
                alignment: {
                  horizontal: "center",
                  vertical: "center",
                  wrapText: true,
                },
                border: {
                  top: {
                    style: 'thin'
                  },
                  left: {
                    style: 'thin'
                  },
                  bottom: {
                    style: 'thin'
                  },
                  right: {
                    style: 'thin'
                  }
                }
              }
            }
            contentWs["E" + (i + 2)] = {
              t: "s",
              v: biaoge[i].bid,
              s: {
                alignment: {
                  horizontal: "center",
                  vertical: "center",
                  wrapText: true,
                },
                border: {
                  top: {
                    style: 'thin'
                  },
                  left: {
                    style: 'thin'
                  },
                  bottom: {
                    style: 'thin'
                  },
                  right: {
                    style: 'thin'
                  }
                }
              }
            }

            contentWs["F" + (i + 2)] = {
              t: "s",
              v: biaoge[i].allBid,
              s: {
                alignment: {
                  horizontal: "center",
                  vertical: "center",
                  wrapText: true,
                },
                border: {
                  top: {
                    style: 'thin'
                  },
                  left: {
                    style: 'thin'
                  },
                  bottom: {
                    style: 'thin'
                  },
                  right: {
                    style: 'thin'
                  }
                }
              }
            }
            contentWs["G" + (i + 2)] = {
              t: "s",
              v: biaoge[i].retailPrice,
              s: {
                alignment: {
                  horizontal: "center",
                  vertical: "center",
                  wrapText: true,
                },
                border: {
                  top: {
                    style: 'thin'
                  },
                  left: {
                    style: 'thin'
                  },
                  bottom: {
                    style: 'thin'
                  },
                  right: {
                    style: 'thin'
                  }
                }
              }
            }
            contentWs["H" + (i + 2)] = {
              t: "s",
              v: biaoge[i].allRetailPrice,
              s: {
                alignment: {
                  horizontal: "center",
                  vertical: "center",
                  wrapText: true,
                },
                border: {
                  top: {
                    style: 'thin'
                  },
                  left: {
                    style: 'thin'
                  },
                  bottom: {
                    style: 'thin'
                  },
                  right: {
                    style: 'thin'
                  }
                }
              }
            }
            contentWs["I" + (i + 2)] = {
              t: "s",
              v: biaoge[i].batchNo,
              s: {
                alignment: {
                  horizontal: "center",
                  vertical: "center",
                  wrapText: true,
                },
                border: {
                  top: {
                    style: 'thin'
                  },
                  left: {
                    style: 'thin'
                  },
                  bottom: {
                    style: 'thin'
                  },
                  right: {
                    style: 'thin'
                  }
                }
              }
            }
            contentWs["J" + (i + 2)] = {
              t: "s",
              v: biaoge[i].produceDate,
              s: {
                alignment: {
                  horizontal: "center",
                  vertical: "center",
                  wrapText: true,
                },
                border: {
                  top: {
                    style: 'thin'
                  },
                  left: {
                    style: 'thin'
                  },
                  bottom: {
                    style: 'thin'
                  },
                  right: {
                    style: 'thin'
                  }
                }
              }
            }
            contentWs["K" + (i + 2)] = {
              t: "s",
              v: biaoge[i].endDate,
              s: {
                alignment: {
                  horizontal: "center",
                  vertical: "center",
                  wrapText: true,
                },
                border: {
                  top: {
                    style: 'thin'
                  },
                  left: {
                    style: 'thin'
                  },
                  bottom: {
                    style: 'thin'
                  },
                  right: {
                    style: 'thin'
                  }
                }
              }
            }

            contentWs["L" + (i + 2)] = {
              t: "s",
              v: biaoge[i].createDate,
              s: {
                alignment: {
                  horizontal: "center",
                  vertical: "center",
                  wrapText: true,
                },
                border: {
                  top: {
                    style: 'thin'
                  },
                  left: {
                    style: 'thin'
                  },
                  bottom: {
                    style: 'thin'
                  },
                  right: {
                    style: 'thin'
                  }
                }
              }
            }
            contentWs["M" + (i + 2)] = {
              t: "s",
              v: biaoge[i].supplier,
              s: {
                alignment: {
                  horizontal: "center",
                  vertical: "center",
                  wrapText: true,
                },
                border: {
                  top: {
                    style: 'thin'
                  },
                  left: {
                    style: 'thin'
                  },
                  bottom: {
                    style: 'thin'
                  },
                  right: {
                    style: 'thin'
                  }
                }
              }
            }
            contentWs["N" + (i + 2)] = {
              t: "s",
              v: biaoge[i].factory,
              s: {
                alignment: {
                  horizontal: "center",
                  vertical: "center",
                  wrapText: true,
                },
                border: {
                  top: {
                    style: 'thin'
                  },
                  left: {
                    style: 'thin'
                  },
                  bottom: {
                    style: 'thin'
                  },
                  right: {
                    style: 'thin'
                  }
                }
              }
            }
            if (biaoge[i].beizhu === '') {
              contentWs["O" + (i + 2)] = {
                t: "s",
                v: "",
                s: {
                  alignment: {
                    horizontal: "center",
                    vertical: "center",
                    wrapText: true,
                  },
                  border: {
                    top: {
                      style: 'thin'
                    },
                    left: {
                      style: 'thin'
                    },
                    bottom: {
                      style: 'thin'
                    },
                    right: {
                      style: 'thin'
                    }
                  }
                }
              }
            } else {
              contentWs["O" + (i + 2)] = {
                t: "s",
                v: biaoge[i].beizhu,
                s: {
                  alignment: {
                    horizontal: "center",
                    vertical: "center",
                    wrapText: true,
                  },
                  border: {
                    top: {
                      style: 'thin'
                    },
                    left: {
                      style: 'thin'
                    },
                    bottom: {
                      style: 'thin'
                    },
                    right: {
                      style: 'thin'
                    }
                  }
                }
              }
            }
          }

        }


        const mergeTitle = [

          {
            s: {
              r: 0,
              c: 0
            },
            e: {
              r: 0,
              c: 14
            }
          },

        ]

        //Cố định dòng đầu tiên và cột đầu tiên：
        contentWs['!freeze'] = {
          xSplit: "11", //Cố định cột
          ySplit: "1", //Cố định dòng
          topLeftCell: "A2", //Ô hiển thị ở góc trên bên trái của vùng chưa cố định，Mặc định là ô chưa cố định đầu tiên
          state: "frozen"
        }

        contentWs["!merges"] = mergeTitle

        contentWs["!cols"] = [{
          wch: 20
        }, {
          wch: 20
        }, {
          wch: 12
        }, {
          wch: 10
        }, {
          wch: 10
        }, {
          wch: 15
        }, {
          wch: 15
        }, {
          wch: 15
        }, {
          wch: 15
        }, {
          wch: 10
        }, {
          wch: 10
        }, {
          wch: 10
        }, {
          wch: 20
        }, {
          wch: 20
        }, {
          wch: 15
        }]
        contentWs["!rows"] = {}

        contentWs["!margins"] = {
          left: 1.0,
          right: 1.0,
          top: 1.0,
          bottom: 1.0,
          header: 0.5,
          footer: 0.5
        }

        XLSX.utils.book_append_sheet(web, contentWs, "Bảng chi tiết xuất kho");

        // //Sử dụngxlsx-styleCách ghi tệp，để kiểu tùy chỉnh có hiệu lực
        const tmpDown = new Blob([
          this.s2ab(
            XLSXD.write(web, {
              bookType: "xlsx",
              BookSST: true,
              type: "binary",
              cellStyles: true,
            })
          )
        ])
        //  XLSX.writeFile(web,'Bảng chi tiết xuất kho.xlsx');
        this.downExcel(tmpDown, 'Bảng chi tiết xuất kho.xlsx')
      },
      downExcel(obj, fileName) {
        const a_node = document.createElement("a");
        a_node.download = fileName;
        if ("msSaveOrOpenBlob" in navigator) {
          window.navigator.msSaveOrOpenBlob(obj, fileName)
        } else {
          a_node.href = URL.createObjectURL(obj);
        }
        a_node.click()

        setTimeout(() => {
          URL.revokeObjectURL(obj)
        }, 2000)
      },
      s2ab(s) {
        if (typeof ArrayBuffer !== "undefined") {
          const buf = new ArrayBuffer(s.length);
          const view = new Uint8Array(buf);
          for (let i = 0; i !== s.length; ++i) {
            view[i] = s.charCodeAt(i) & 0xff
          }
          return buf;
        } else {
          const buf = new Array(s.length);
          for (let i = 0; i !== s.length; ++i) {
            buf[i] = s.charCodeAt(i) & 0xff;
          }
          return buf;
        }
      },
      indexMethod(index) {
        return (this.storageTableCurrentPage - 1) * this.storageTablePagesize + index + 1;
      },
      indexMethod2(index) {
        return (this.storageDetailTableCurrentPage - 1) * this.storageDetailTablePagesize + index + 1;
      }
    },
    mounted() {
      this.pageInit();
    },
  };
</script>


<style rel="stylesheet/scss" lang="scss" scoped>
  .page-container {
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

  ::v-deep .el-table__fixed-right-patch {
    width: 5px !important
  }

  ::v-deep .el-table colgroup col[name='gutter'] {
    width: 5px !important
  }

  ::v-deep .el-table__body {
    width: 100% !important
  }
</style>
