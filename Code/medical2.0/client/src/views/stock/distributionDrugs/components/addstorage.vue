<template>
  <div>

    <el-row>
      <el-col :span="6">
        <span><b>Phòng khám：</b></span>
        <el-select
          v-model="otherInfo.companyId"
          filterable
          @change="getDrugOrStuff"
          placeholder="Vui lòng chọn phòng khám"
        >
          <el-option
            v-for="clinic in clinicList"
            :label="clinic.name"
            :value="clinic.id"
            :key="clinic.id"
          ></el-option>
        </el-select>
      </el-col>
      <el-col :span="6">
        <span><b>Nhà cung cấp：</b></span>
        <el-select
          v-model="otherInfo.supplierID"
          filterable
          placeholder="Vui lòng chọn nhà cung cấp"
        >
          <el-option
            v-for="supplierxz in supplierLists"
            :label="supplierxz.name"
            :value="supplierxz.id"
            :key="supplierxz.id"
          ></el-option>
        </el-select>
      </el-col>

      <el-col :span="6">
        <span v-if="addType === 1"><b>Tên thuốc：</b></span>
        <span v-else><b>Tên vật tư：</b></span>
        <el-popover
          placement="right"
          v-if="!IsOnlyRead"
          width="600"
          trigger="click"
          popper-class="popover"
        >

          <el-table
            :data="newList"
            border
            highlight-current-row
            @row-click="RowClickSurchargeTable"
          >
            <el-table-column
              prop="goodsName"
              :label="addType === 1 ? 'Tên thuốc' : 'Tên vật tư'"
              v-if="addType === 1"
            >
            </el-table-column>
            <el-table-column
              prop="name"
              :label="addType === 1 ? 'Tên thuốc' : 'Tên vật tư'"
              v-if="addType === 2"
            >
            </el-table-column>
            <el-table-column
              prop="type.name"
              :label="addType === 1 ? 'Loại thuốc' : 'Loại vật tư'"
            >
            </el-table-column>
            <el-table-column
              prop="specification"
              label="Quy cách"
              width="100"
              v-if="addType === 1"
            >
            </el-table-column>
            <el-table-column
              prop="price1"
              label="Đơn giá"
              width="100"
              v-if="addType === 2"
            >
            </el-table-column>
            <el-table-column
              prop="factory.name"
              label="Nhà sản xuất"
              width="100"
              v-if="addType === 1"
            >
            </el-table-column>
            <el-table-column
              prop="pack.name"
              label="Đơn vị"
              width="80"
              v-if="addType === 1"
            >
            </el-table-column>
            <el-table-column
              prop="price1"
              label="Bán lẻ"
              width="80"
              v-if="addType === 1"
            >
            </el-table-column>
          </el-table>

          <!-- <span v-if="addType === 1">Tên thuốc：</span>
        Tên vật tư： -->
          <el-input
            style="width: 80%"
            slot="reference"
            :disabled="!otherInfo.companyId"
            v-model="searchName"
            :placeholder="drugStuffPlaceholder"
          ></el-input>

        </el-popover>
        <!-- @input="GetSurchargeTable" -->

        <!-- <el-autocomplete
          popper-class="my-autocomplete"
          v-model="goodsNameTemp"
          :fetch-suggestions="queryGoods"
          placeholder="Vui lòng nhập tên thuốc"
          @select="selectGoods"
        >
          <template slot-scope="{ item }">
            <div>{{ item.goodsName }}</div>
          </template>
        </el-autocomplete> -->
      </el-col>


      <el-col :span="1" style="margin-left:100px">
        <el-button type="primary" @click="importStudentExcel(addType)">Nhập hàng loạt</el-button>
      </el-col>
      <!-- <el-col :span="6">
        <div>
          <el-radio v-model="radio" label="1" border>Thuốc</el-radio>
          <el-radio v-model="radio" label="2" border>Vật tư</el-radio>
        </div>
      </el-col> -->
      <!-- <el-col :span="6">
        <el-link type="primary" @click="AddRow()">Thêm</el-link>
      </el-col> -->
    </el-row>
    <br>
    <el-row>
      <el-col :span="24">
        <el-table :data="storageDetailTable" style="width: 100%" height="350">
          <el-table-column prop="goodsName" :label="drugStuffName" width="200">
          </el-table-column>
          <!-- <el-table-column
            prop="goodsName"
            label="Thuốc/Tên vật tư"
            width="200"
            v-if="radio === 2"
          >
          </el-table-column> -->
          <el-table-column prop="specification" label="Quy cách"></el-table-column>
          <el-table-column prop="factoryName" label="Nhà sản xuất"></el-table-column>
          <el-table-column label="Số lô sản xuất">
            <template slot-scope="scope">
              <el-input
                v-model="scope.row.batchNumber"
                placeholder="Số lô sản xuất"
              ></el-input>
            </template>
          </el-table-column>
          <el-table-column label="Ngày sản xuất" :required="true" type="index" width="110">
            <template slot-scope="scope">
              <el-date-picker
                :id="'a' + scope.$index"
                v-model="scope.row.producedDate"
                placeholder="Ngày sản xuất"
                type="date"
                :picker-options="expireTimeOption"
              ></el-date-picker>
            </template>
          </el-table-column>
          <el-table-column label="Ngày hiệu lực" :required="true" width="110">
            <template slot-scope="scope">
              <el-date-picker
                v-model="scope.row.expireDate"
                placeholder="Ngày hiệu lực"
                type="date"
              ></el-date-picker>
            </template>
          </el-table-column>
          <el-table-column label="Số lượng" :required="true" width="150" prop="specification">
            <template slot-scope="scope">
              <div class="quantity-box">
                <el-input
                  v-model="scope.row.quantity"
                  type="number"
                  required="true"
                  placeholder="Số lượng"
                  oninput="value=((value.replace(/[^0-9.]/g,'')).replace(/([0-9]+\.[0-9]{2})[0-9]*/,'$1'))"
                ></el-input
                >
                <span>{{ scope.row.pack.name }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column :required="true" label="Giá nhập">
            <template slot-scope="scope">
              <el-input
                v-if="systemParamConfig===1"
                disabled
                v-model="scope.row.outPrice"
                type="number"
                :change="checkPrice()"
                placeholder="Giá nhập"
              ></el-input>
              <el-input
                v-if="systemParamConfig===2"
                v-model="scope.row.inPrice"
                type="number"
                :change="checkPrice()"
                placeholder="Giá nhập"
              ></el-input>
            </template>
          </el-table-column>
          <el-table-column prop="outPrice" label="Giá bán lẻ"></el-table-column>
          <el-table-column label="Tổng giá nhập" v-if="systemParamConfig===1">
            <template slot-scope="scope"
            >{{ calcTotalOutPrice(scope.row) }}
            </template>
          </el-table-column>
          <el-table-column label="Tổng giá nhập" v-if="systemParamConfig===2">
            <template slot-scope="scope" v-if="systemParamConfig===2"
            >{{ calcTotalInPrice(scope.row) }}
            </template>
          </el-table-column>
          <el-table-column label="Tổng giá bán lẻ">
            <template slot-scope="scope"
            >{{ calcTotalOutPrice(scope.row) }}
            </template>
          </el-table-column>
          <el-table-column label="" width="100">
            <template slot-scope="scope">
              <el-button
                type="danger"
                size="small"
                @click.native.prevent="
                  deleteRow(scope.$index, storageDetailTable)
                "
              >Xóa
              </el-button
              >
            </template>
          </el-table-column>
        </el-table>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="3" :offset="12">Chủng loại：{{ CalcBreed() }}</el-col>
      <el-col :span="3">Số lượng：{{ calcTotalNumber() }}</el-col>
      <el-col :span="3" v-if="systemParamConfig===1">Tổng giá nhập：{{ calcAllRetailPrice() }}</el-col>
      <el-col :span="3" v-if="systemParamConfig===2">Tổng giá nhập：{{ calcAllBid() }}</el-col>
      <el-col :span="3">Tổng giá bán lẻ：{{ calcAllRetailPrice() }}</el-col>
    </el-row>
    <el-row>
      <el-col :span="24 / 4">
        <div class="footer-box">
          <el-input
            v-model="otherInfo.withGoodsNo"
            placeholder="Số đơn kèm hàng"
          ></el-input>
        </div>
      </el-col>
      <el-col :span="24 / 4">
        <div class="footer-box">
          <el-input
            v-model="otherInfo.invoiceNumber"
            placeholder="Số hóa đơn"
          ></el-input>
        </div>
      </el-col>
      <el-col :span="24 / 4">
        <div class="footer-box">
          <el-input v-model="otherInfo.remarks" placeholder="Ghi chú"></el-input>
        </div>
      </el-col>
      <el-col :span="24 / 4">
        <div class="footer-box footer-box1">
          <el-button type="primary" @click="saveSupplierStock">Xác nhận</el-button>
          <el-button type="" @click="closeDialog()">Hủy</el-button>
        </div>
      </el-col>
    </el-row>
    <!-- Hộp thoại hiện sau khi nhấn nhập -->
    <el-dialog
    :title="addType===1 ?'Nhập thuốc':'Nhập vật tư'"
    :append-to-body="true"
    :visible.sync="importDialogVisible"
    width="45%"
    :before-close="cancellation">
      <!--limit:Số lượng tải lên tối đa，on-exceed：Thao tác khi vượt quá số lượng tải lên tối đa  -->
      <el-upload
        class="upload-demo"
        action=""
        :on-change="handleChange"
        :on-remove="handleRemove"
        :on-exceed="handleExceed"
        accept="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet, application/vnd.ms-excel"
        :auto-upload="false">
        <el-button icon="el-icon-upload" type="primary">Nhấn để tải lên</el-button><br>
      </el-upload>
      <el-button type="primary" icon="el-icon-download" @click="downloadExcel">Tải mẫu xuống</el-button>
      <el-button @click="cancellation()">Hủy</el-button>
    </el-dialog>

    <el-dialog :title="addType===1 ?'Lỗi nhập thông tin thuốc':'Lỗi nhập thông tin vật tư'" :visible.sync="dialogVisible" width="36%"
    :append-to-body="true"
               :before-close="mistakeClose">
      <span> Nhập dữ liệu thành công<span style="white-space:pre;color:#08ff00;font-size: 16px">{{ chengGong }}</span>dòng&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </span>
      <span> Nhập dữ liệu thất bại<span style="white-space:pre;color:red;font-size: 16px">{{ shiBai }}</span>dòng </span><br>
      <span style="white-space:pre;color:red;font-size: 15px">{{ mistake }}</span>
      <span slot="footer" class="dialog-footer">
          <el-button @click="dialogVisible = false">Hủy</el-button>
          <el-button type="primary" @click="dialogVisible = false">Xác nhận</el-button>
        </span>
    </el-dialog>
  </div>
</template>

<script>
  import {listDrugAll} from "@/api/stock/drug";
  import { getSubordinateClinic } from "@/api/stock/clinic";
  import {listSysParamConfigAll} from '@/api/sys/sysParamConfig';
  import {listStuffAll, listStuffPage, listDrug} from "@/api/stock/stuff";
  import {saveSupplierStockList,saveSupplierStockListV1} from "@/api/stock/supplierStock";
  import {getSysParamConfigById} from '@/api/sys/sysParamConfig'
  import {listPage} from "@/api/stock/medicinalStorageControl";
  import {listSupplierPage, getSupplierById, listSupplierAll} from "@/api/stock/supplier";
  import {BigNumber} from "bignumber.js";
  import BaseUI from "@/views/components/baseUI";
  import {getLocalCurrentCompany} from "@/utils/auth";
  import XLSX from 'xlsx'
  import Vue from "vue";
  import fileurl from "@/assets/file/inventory.xlsx";

  Vue.use(XLSX)

  export default {
    extends: BaseUI,
    props: {
      supplierList: null,
      addType: null,
    },

    data() {
      return {
        importDialogVisible: false, // Biến hộp thoại nhập hàng loạt
        dialogVisible: false, // Biến hộp thoại báo lỗi nhập
        chengGong: "",
        shiBai: "",
        mistake: "",
        newList: [],
        flag: true,
        //Đặt ngày trước hôm nay của ngày hết hiệu lực thành không thể chọn
        expireTimeOption: {
          // disabledDate(date) {
          //   //disabledDate Trên tài liệu：Đặt trạng thái vô hiệu hóa，Tham số là ngày hiện tại，Yêu cầu trả về Boolean
          //   return date.getTime() < Date.now() - 24 * 60 * 60 * 1000;
          // },
        },
        searchName: "", // Tên thuốc hoặc vật tư
        // radio: "1", // 1 Thuốc，2Vật tư
        company_id: JSON.parse(window.sessionStorage.getItem("currentUser"))
          .company.id /*Phòng khámID*/,
        // examine_id: "1005787933775863930" /*Trạng thái đã quaID*/,
        // examine_value: "supplierStorageExamineStatus_0" /*Trạng thái đã quaValuegiá trị*/,
        examine_id: "2087542517503803429" /*Trạng thái chờ xét duyệtID*/,
        examine_value: "supplierStorageExamineStatus_3" /*Trạng thái chờ xét duyệtValuegiá trị*/,
        title: "Thêm mới đơn nhập kho",
        goodsNameTemp: "",
        drugPreparation: 0,
        stuffPackNum: 0,
        drugStuffName: "",//Tên thuốc hoặc vật tư
        drugStuffPlaceholder: "",//tương ứngplaceholder
        goodsInfo: null,
        queryList: [], // Tất cả thuốc hoặc vật tư
        selectGoodsTemp: null,
        storageDetailTable: [],
        supplierLists: [],//Nhà cung cấp khác nhau
        clinicList: [], // 诊所列表
        otherInfo: {
          companyId: null,
          supplierID: null,
          breed: 0 /*Số lượng chủng loại*/,
          totalNumber: 0 /*Tổng số lượng*/,
          allBid: 0 /*Tổng giá nhập*/,
          allRetailPrice: 0 /*Tổng giá bán lẻ*/,
          withGoodsNo: "" /*Số đơn cung ứng*/,
          invoiceNumber: "" /*Số hóa đơn*/,
          remarks: "" /*Ghi chú*/,
        },
        drugSearch: {
          params: [{columnName: "company_id", queryType: "=", value: ""}],
          offset: 0,
          limit: 100,
          columnName: "", // Tên trường sắp xếp
          order: "", // Sắp xếp
        },
        stuffSearch: {
          params: [{columnName: "company_id", queryType: "=", value: ""}],
          offset: 0,
          limit: 100,
          columnName: "", // Tên trường sắp xếp
          order: "", // Sắp xếp
        },
        supplierSearch: {
          params: [{columnName: "company_id", queryType: "=", value: ""}],
          offset: 0,
          limit: 100,
          columnName: "", // Tên trường sắp xếp
          order: "", // Sắp xếp

        },
        permission: {
          view: false,
          add: false,
          edit: false,
          remove: false,
          export: false
        },
        //Cấu hình tham số hệ thống
        systemParamConfig: "",
        systemParamConfigSearch: {
          params: []
        },
      };
    },
    watch: {
      addType: function (newVal, oldVal) {
        this.otherInfo.supplierID = null
        this.otherInfo.companyId = null
        this.getClinicList()
        if (newVal === 1) this.getDrugList();
        if (newVal === 2) this.getStuffList();
      },

      searchName: function (newVal, oldVal) {
        const pattern = new RegExp("[A-Za-z]+");
        if (this.flag) {
          if (newVal === "") {
            this.newList = this.queryList;
          } else if (pattern.test(newVal)) {

            window.setTimeout(() => {
              this.newList = this.queryList.filter((i) => {
                if (i.pinyinCode) {
                  let newSearchName = this.searchName.toUpperCase();
                  return i.pinyinCode.indexOf(newSearchName) > -1;
                }
                if (i.pinyinCode) {
                  return i.pinyinCode.indexOf(newSearchName) > -1;
                }
              });
              this.flag = true;
            }, 1000);
          } else {
            window.setTimeout(() => {
              this.newList = this.queryList.filter((i) => {
                if (i.name) {
                  return i.name.indexOf(this.searchName) > -1;
                }
                if (i.goodsName) {
                  return i.goodsName.indexOf(this.searchName) > -1;
                }
              });
              this.flag = true;
            }, 1000);
          }
        }
      },
      storageDetailTable: {
        handler(newVal, oldVal) {
          if (newVal.length !== 0) {
            newVal.forEach((i, ind) => {

              if (
                this.$moment(i.expireDate).format("YYYY-MM-DD HH:mm:ss") <
                this.$moment(i.producedDate).format("YYYY-MM-DD HH:mm:ss")
              ) {
                newVal[ind].expireDate = "";
                this.$message({
                  message: "Hạn sử dụng không được nhỏ hơn ngày sản xuất",
                  type: "warning",
                });

                // this.$nextTick(() => {
                //   document.getElementById('a'+ind).focus();
                // });
              }
              this.storageDetailTable = newVal;
              // }
            });
          }
        },
        immediate: true,
        deep: true, // Cần thêmdeeplàtruelà có thể đối vớiobjtiến hành theo dõi sâu
      },
    },
  methods: {
      getDrugOrStuff(val) {
        if (!val) return
          if (this.addType === 1) this.getDrugList(false);
          if (this.addType === 2) this.getStuffList(false);
      },
      // 获取诊所列表
      getClinicList() {
        getSubordinateClinic()
          .then(res => {
            if (res.code === 100) {
              this.clinicList = res.data
            }
          })
          .catch(err => {
            console.error(err);
          })
      },
      //  Thuốc
      RowClickSurchargeTable(row) {

        this.searchName = row.goodsName ? row.goodsName : row.name;
        const newStorage = {
          goodsID: "",
          goodsName: "",
          specification: "",
          factoryName: "",
          factoryID: "",
          batchNumber: "",
          producedDate: null,
          expireDate: null,
          quantity: "",
          inPrice: "",
          outPrice: null,
          totalInPrice: "",
          totalOutPrice: "",
          pack: {},
          preparationUnit: {},
          preparation: 0,
          packNumber: 0,
        };
        newStorage.goodsID = row.id;
        newStorage.goodsName = row.goodsName ? row.goodsName : row.name;
        newStorage.goodsType = row.goodsType;
        newStorage.specification = row.specification;
        newStorage.factoryName = row.factory.name;
        newStorage.factoryID = row.factory.id;
        newStorage.outPrice = row.price;
        newStorage.pack = row.pack;
        newStorage.preparationUnit = row.preparationUnit
        newStorage.preparation = row.preparation - 0;
        newStorage.packNumber = row.packNumber;
        this.drugPreparation = row.preparation - 0;
        if (row.packNumber !== undefined) {
          this.stuffPackNum = row.packNumber;
        } else {
          this.stuffPackNum = 0
        }

        this.selectGoodsTemp = newStorage;
        this.AddRow();
        // this.storageDetailTable.splice(
        //   this.storageDetailTable.length - 1,
        //   1,
        //   newStorage
        // );
      },
      //Nhấn nút thêm
      AddRow() {
        if (this.selectGoodsTemp) {
          const newStorage = {
            goodsID: "",
            goodsName: "",
            specification: "",
            factoryName: "",
            factoryID: "",
            batchNumber: "",
            producedDate: null,
            expireDate: null,
            quantity: "",
            inPrice: "",
            outPrice: null,
            totalInPrice: "",
            totalOutPrice: "",
            pack: {},
            preparationUnit: {},
            preparation: 0,
            packNumber: 0,
          };
          newStorage.goodsID = this.selectGoodsTemp.goodsID;
          newStorage.goodsName = this.selectGoodsTemp.goodsName;
          newStorage.goodsType = this.selectGoodsTemp.goodsType;
          newStorage.specification = this.selectGoodsTemp.specification;
          newStorage.factoryName = this.selectGoodsTemp.factoryName;
          newStorage.factoryID = this.selectGoodsTemp.factoryID;
          newStorage.outPrice = this.selectGoodsTemp.outPrice;
          newStorage.pack = this.selectGoodsTemp.pack;
          newStorage.preparationUnit = this.selectGoodsTemp.preparationUnit
          newStorage.preparation = this.selectGoodsTemp.preparation
          newStorage.packNumber = this.selectGoodsTemp.packNumber
          this.storageDetailTable.push(newStorage);
        } else {
          this.$message({
            message: "Vui lòng chọn thuốc hoặc vật tư cần thêm mới nhập kho",
            type: "warning",
          });
        }
        this.searchName = ""
      },
      // Tất cả vật tư
      getStuffList(hasSupplier = true) {
        this.storageDetailTable = []
        this.drugStuffPlaceholder = 'Nhập tên vật tư/Mã pinyin'
        this.drugStuffName = 'Tên vật tư'
        let stuff_search = {
          params: [],
        };
        // Trên bảng tương ứng của trườngfilterĐiều kiện
        stuff_search.params.push.apply(stuff_search.params, [
          {
            columnName: "company_id",
            queryType: "=",
            value: this.otherInfo.companyId,
          },
          {
            columnName: "is_out_sell",
            queryType: "=",
            value: "1",
          }
        ]);
        // Quyền dữ liệu: Vật tưstuff
        this.pushDataPermissions(
          stuff_search.params,
          this.$route.meta.routerId,
          "1004462867645374480"
        );
        this.queryList.splice(0, this.queryList.length);
        if (this.otherInfo.companyId) {
          listStuffAll(stuff_search).then((responseData) => {
            if (Array.isArray(responseData.data)) {
              responseData.data.forEach((i) => {
                if (!i.price1) i.price1 = i.priceOutSell + "/" + i.packUnit.name;
                if (!i.price) i.price = i.priceOutSell;
                if (!i.pack) i.pack = i.packUnit;
                i.value = i.name;
                i.goodsType = 2;
                if (!i.specification)
                  i.specification =
                    i.packNumber + i.minUnit.name + "/" + i.packUnit.name;
              });
            }
            this.queryList = responseData.data;
            this.newList = this.queryList;
          });
        }
        if(!hasSupplier) return
        this.supplierSearch.params = [
          {
            columnName: "company_id",
            queryType: "=",
            value: currentUser.company.id,
          },
          {
            columnName: "type",
            queryType: "=",
            value: "2",
          }
        ];
        this.supplierLists = null
        listSupplierAll(this.supplierSearch).then((responseData) => {
          if (responseData.code === 100) {
            this.supplierLists = responseData.data
          }
        }).catch(() => {
        })
      },
      // Tất cả thuốc
      getDrugList(hasSupplier=true) {
        this.storageDetailTable = []
        this.drugStuffPlaceholder = 'Nhập tên thuốc/Mã pinyin'
        this.drugStuffName = 'Tên thuốc'
        let drug_search = {
          params: [],
        };
        // Trên bảng tương ứng của trườngfilterĐiều kiện
        drug_search.params.push.apply(drug_search.params, [
          {
            columnName: "company_id",
            queryType: "=",
            value: this.otherInfo.companyId,
          },
        ]);
        // Quyền dữ liệu: Thông tin thuốcdrug
        this.pushDataPermissions(
          drug_search.params,
          this.$route.meta.routerId,
          "1004078055755374623"
        );
        this.queryList.splice(0, this.queryList.length);
        if (this.otherInfo.companyId) {
          listDrugAll(drug_search).then((responseData) => {
            if (Array.isArray(responseData.data)) {
              responseData.data.forEach((i) => {
                i.value = i.goodsName;
                i.goodsType = 1;
                i.price1 = i.price + "/" + i.pack.name;
                i.specification =
                  i.dosis + i.dosisUnit.name + "*" + i.preparation + i.preparationUnit.name + "/" + i.pack.name;
              });
            }
            this.queryList = responseData.data;
            this.newList = this.queryList;

          });
        }
        if(!hasSupplier) return
        this.supplierSearch.params = [
          {
            columnName: "company_id",
            queryType: "=",
            value: currentUser.company.id,
          },
          {
            columnName: "type",
            queryType: "=",
            value: "1",
          }
        ];
        this.supplierLists = null
        listSupplierAll(this.supplierSearch).then((responseData) => {
          if (responseData.code === 100) {
            this.supplierLists = responseData.data
          }
        }).catch(() => {
        })

      },
      // Giá nhập không được là số âm
      checkPrice() {
        this.storageDetailTable.forEach((i) => {
          let checkPlan = "" + i.inPrice;
          checkPlan = checkPlan
            .replace(/[^\d.]/g, "") // Xóa“chữ số”và“.”ký tự khác
            .replace(/\.{2,}/g, ".") // Chỉ giữ lại ký tự đầu tiên. Xóa các ký tự thừa
            .replace(/^\./g, "") // Đảm bảo ký tự đầu tiên là chữ số chứ không phải.
            .replace(".", "$#$")
            .replace(/\./g, "")
            .replace("$#$", ".");
          if (checkPlan.indexOf(".") < 0 && checkPlan !== "") {
            // Các ký tự trên đã được lọc，Tại đây kiểm soát trường hợp không có dấu thập phân，Ký tự đầu tiên không được giống 01、02số tiền
            checkPlan = parseFloat(checkPlan) + "";
          } else if (checkPlan.indexOf(".") >= 0) {
            // checkPlan = checkPlan.replace(/^()*(\d+)\.(\d\d).*$/, "$1$2.$3"); // Chỉ được nhập tối đa hai chữ số thập phân
            checkPlan = checkPlan.replace(/^(\-)*(\d+)\.(\d{4}).*$/, '$1$2.$3'); // Chỉ được nhập tối đa hai chữ số thập phân
          }
          i.inPrice = checkPlan;
        });
        // let checkPlan = '' + this.numberValidateForm.price
        // checkPlan = checkPlan
        //   .replace(/[^\d.]/g, '') // Xóa“chữ số”và“.”ký tự khác
        //   .replace(/\.{2,}/g, '.') // Chỉ giữ lại ký tự đầu tiên. Xóa các ký tự thừa
        //   .replace(/^\./g, '') // Đảm bảo ký tự đầu tiên là chữ số chứ không phải.
        //   .replace('.', '$#$')
        //   .replace(/\./g, '')
        //   .replace('$#$', '.')
        // if (checkPlan.indexOf('.') < 0 && checkPlan !== '') {
        //   // Các ký tự trên đã được lọc，Tại đây kiểm soát trường hợp không có dấu thập phân，Ký tự đầu tiên không được giống 01、02số tiền
        //   checkPlan = parseFloat(checkPlan) + ''
        // } else if (checkPlan.indexOf('.') >= 0) {
        //   checkPlan = checkPlan
        //     .replace(/^()*(\d+)\.(\d\d).*$/, '$1$2.$3') // Chỉ được nhập tối đa hai chữ số thập phân
        // }
        // this.numberValidateForm.price = checkPlan
      },

      async queryItem(queryString, callback) {
        await this.queryGoods(queryString, callback);
      },
      // Giá nhập có phải giá bán lẻ không
      GetSysParamConfig() {
        listSysParamConfigAll(this.systemParamConfigSearch).then(responseData => {
          if (responseData.code === 100) {
            if (Array.isArray(responseData.data) && responseData.data.length >= 1) {
              responseData.data.forEach(data => {
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
      /*Kích hoạt truy vấn thuốc theo tham số đầu vào*/
      queryGoods(queryString, callback) {
        this.goodsInfo = [];
        if (queryString !== "") {
          setTimeout(() => {
            queryString = "%" + queryString + "%";
            this.drugSearch.params = [
              {
                columnName: "company_id",
                queryType: "=",
                value: this.company_id,
              },
            ];
            this.drugSearch.params.push({
              columnName: "goods_name",
              queryType: "like",
              value: queryString,
            });

            if (this.addType === "1") {
              listDrug(this.drugSearch)
                .then((responseData) => {
                  if (responseData.code === 100) {
                    if (responseData.data.rows) {
                      responseData.data.rows.forEach((element) => {
                        this.goodsInfo.push({
                          id: element.id,
                          goodsName: element.goodsName,
                          value: element.goodsName,
                          goodsType: 1,
                          specification:
                            element.dosis +
                            element.dosisUnit.name +
                            "*" +
                            element.preparationUnit.name,
                          factoryName: element.factory.name,
                          factoryID: element.factory.id,
                          outPrice: element.price,
                          pack: element.pack,
                        });
                      });
                    } else {
                      return;
                    }
                  } else {
                    // this.$message.error(responseData);
                  }
                })
                .catch((error) => {
                  // this.$message.error(error);
                });

            }

            this.stuffSearch.params = [
              {
                columnName: "company_id",
                queryType: "=",
                value: this.company_id,
              },
            ];
            this.stuffSearch.params.push({
              columnName: "name",
              queryType: "like",
              value: queryString,
            });

            if (this.addType === "2") {
              listStuffPage(this.stuffSearch).then((responseData) => {
                if (responseData.code === 100) {
                  if (responseData.data.rows) {
                    responseData.data.rows.forEach((element) => {
                      this.goodsInfo.push({
                        id: element.id,
                        goodsName: element.name,
                        value: element.name,
                        goodsType: 2,
                        specification: element.specifications,
                        factoryName: element.factory.name,
                        factoryID: element.factory.id,
                        outPrice: element.priceOutSell,
                        pack: element.pack,
                      });
                    });
                  } else {
                    return;
                  }
                } else {
                  this.$message.error(responseData);
                }
              });
            }

            callback(this.goodsInfo);
          }, 1000);
        } else {
          callback(this.goodsInfo);
        }
      },
      //Thuốc hoặc vật tư đang chọn
      selectGoods(item) {
        this.selectGoodsTemp = item;
      },
      // 每条里的模糊查询后的选中
      selectGoodsItem(item) {
        this.selectGoodsTemp = item;
        const newStorage = {
          goodsID: "",
          goodsName: "",
          specification: "",
          factoryName: "",
          factoryID: "",
          batchNumber: "",
          producedDate: null,
          expireDate: null,
          quantity: "",
          inPrice: "",
          outPrice: null,
          totalInPrice: "",
          totalOutPrice: "",
          pack: {},
        };
        newStorage.goodsID = this.selectGoodsTemp.id;
        newStorage.goodsName = this.selectGoodsTemp.goodsName;
        newStorage.goodsType = this.selectGoodsTemp.goodsType;
        newStorage.specification = this.selectGoodsTemp.specification;
        newStorage.factoryName = this.selectGoodsTemp.factoryName;
        newStorage.factoryID = this.selectGoodsTemp.factoryID;
        newStorage.outPrice = this.selectGoodsTemp.outPrice;
        newStorage.pack = this.selectGoodsTemp.pack;
        this.storageDetailTable.splice(
          this.storageDetailTable.length - 1,
          1,
          newStorage
        );
      },
      //Lưu thuốc hoặc vật tư
      saveSupplierStock() {
        this.flag = true
        if (!this.otherInfo.companyId) {
          this.$message({message: "Vui lòng chọn phòng khám", type: "warning"});
          return;
        }
        if (!this.otherInfo.supplierID) {
          this.$message({message: "Vui lòng chọn nhà cung cấp", type: "warning"});
          return;
        }

        if (this.storageDetailTable.length === 0) {
          this.$message({message: "Vui lòng nhập thông tin chi tiết nhập kho", type: "warning"});
          return;
        }

        const day = new Date();
        const saveInfo = {
          supplierStorage: {
            company: {
              id: this.otherInfo.companyId,
            },
            supplier: {
              id: this.otherInfo.supplierID,
            },
            code: "",
            breed: this.otherInfo.breed,
            number: this.stuffPackNum === 0 ? this.otherInfo.totalNumber * this.drugPreparation : this.otherInfo.totalNumber * this.stuffPackNum,
            allBid: this.otherInfo.allBid,
            allRetailPrice: this.otherInfo.allRetailPrice,
            cancelDate: null,
            examine: {
              id: this.examine_id,
              value: this.examine_value,
            },
            examineDate: this.$moment(day).format("YYYY-MM-DD HH:mm:ss"),
            withGoodsNo: this.otherInfo.withGoodsNo,
            invoiceNumber: this.otherInfo.invoiceNumber,
            remarks: this.otherInfo.remarks,
            type: this.addType + '',
          },
          supplierStockList: [],
        };
        this.storageDetailTable.forEach((element) => {
          if (this.systemParamConfig === 1) {
            element.inPrice = element.outPrice
          }
          try {
            if (element.quantity === "" || element.quantity === 0) {
              throw "Số lượng";
            }
            if (element.inPrice === "") {
              throw "Giá nhập";
            }
            if (
              this.$moment(element.expireDate).format("YYYY-MM-DD HH:mm:ss") <
              this.$moment(element.producedDate).format("YYYY-MM-DD HH:mm:ss")
            ) {
              throw "Thời gian";
            }
            if (
              !element.expireDate
            ) {
              throw "Hạn sử dụng";
            }
            const detail = {
              company: {
                // Phòng khámid
                id: this.otherInfo.companyId,
              },
              supplierId: {
                // Nhà cung cấp
                id: this.otherInfo.supplierID,
              },
              drug: {
                // Tên thuốc
                id: element.goodsType === 1 ? element.goodsID : null,
                goodsName: element.goodsType === 1 ? element.goodsName : null,
              },
              supplierStorage: null,
              stuff: {
                // Tên vật tư
                id: element.goodsType === 2 ? element.goodsID : null,
                name: element.goodsType === 2 ? element.goodsName : null,
              },
              norms: element.specification, // Quy cách
              factory: {
                id: element.factoryID,
                name: element.factoryName,
              }, // Nhà sản xuất
              batchNo: element.batchNumber, // Số lô
              produceDate:
                element.producedDate !== ""
                  ? this.$moment(element.producedDate).format(
                  "YYYY-MM-DD HH:mm:ss"
                  )
                  : null, // Ngày sản xuất
              endDate:
                element.expireDate !== ""
                  ? this.$moment(element.expireDate).format("YYYY-MM-DD HH:mm:ss")
                  : null, // Hạn sử dụng
              number: element.goodsType === 1 ? element.quantity * element.preparation : element.quantity * element.packNumber, // Số lượng
              bid: element.inPrice, // Giá nhập
              retailPrice: element.outPrice, // Giá bán lẻ
              allBid: element.totalInPrice, // Tổng giá nhập
              allRetailPrice: element.totalOutPrice, // Tổng giá bán lẻ
              remarks: "", // Thông tin ghi chú
              leastBid: element.goodsType === 1 ? Number((element.inPrice / element.preparation).toFixed(4)) : Number((element.inPrice / element.packNumber).toFixed(4))
            };
            saveInfo.supplierStockList.push(detail);
          } catch (e) {
            console.error('格式化入库单明细出错', e);
            this.flag = e;
          }
        });
        if (this.systemParamConfig === 1) {
          this.inPrice = this.outPrice
          this.totalInPrice = this.totalOutPrice
          saveInfo.supplierStockList.allBid = saveInfo.supplierStockList.allRetailPrice
        }
        if (this.flag === "Số lượng") {
          this.$message({message: "Số lượng nhập kho không được để trống", type: "warning"});
          this.flag = "";
          return;
        } else if (this.flag === "Giá nhập") {
          this.$message({message: "Giá nhập không được để trống", type: "warning"});
          this.flag = "";
          return;
        } else if (this.flag === "Thời gian") {
          this.flag = "";
          this.$message({
            message: "Hạn sử dụng không được nhỏ hơn ngày sản xuất",
            type: "warning",
          });
          return;
        } else if (this.flag === "Hạn sử dụng") {
          this.$message({
            message: "Vui lòng nhập hạn sử dụng",
            type: "warning",
          });
          return;
        } else {
          saveSupplierStockListV1(saveInfo).then((responseData) => {
            if (responseData.code === 100) {
              this.$message({message: "Lưu thành công", type: "success"});
              this.closeDialog();
            } else {
              this.$message({message: responseData.msg, type: "error"});
            }
          });
        }
      },
      //Đóng hộp thoại
      closeDialog() {
        this.storageDetailTable = [];

        this.otherInfo = {
          supplierID: null,
          breed: 0 /*Số lượng chủng loại*/,
          totalNumber: 0 /*Tổng số lượng*/,
          allBid: 0 /*Tổng giá nhập*/,
          allRetailPrice: 0 /*Tổng giá bán lẻ*/,
          withGoodsNo: "" /*Số đơn cung ứng*/,
          invoiceNumber: "" /*Số hóa đơn*/,
          remarks: "" /*Ghi chú*/,
        };
        this.selectGoodsTemp = null;

        this.searchName = "";
        this.$emit("fatherMethod");
      },

      // Đóng thông báo lỗi nhập
      mistakeClose(done) {
        this.$confirm('Xác nhận đóng？')
          .then(_ => {
            done();
          })
          .catch(_ => {
          });
      },

      deleteRow(index, tb) {
        this.storageDetailTable.splice(index, 1);
      },
      calcTotalInPrice(row) {
        const number = Number(row.quantity);
        if (isNaN(number)) return;
        const inPrice = Number(row.inPrice);
        if (isNaN(inPrice)) return;
        const result = number * inPrice;
        row.totalInPrice = result;
        return result;
      },
      calcTotalOutPrice(row) {
        const number = Number(row.quantity);
        if (isNaN(number)) return;
        const outPrice = Number(row.outPrice);
        if (isNaN(outPrice)) return;
        let result = number * outPrice;
        result = result.toFixed(2)
        row.totalOutPrice = result;
        return result;
      },
      CalcBreed() {
        if (this.storageDetailTable) {
          this.otherInfo.breed = this.storageDetailTable.length;
          return this.otherInfo.breed;
        }
        return 0;
      },
      calcTotalNumber() {
        let totalNumber = 0;
        if (this.storageDetailTable) {
          this.storageDetailTable.forEach((element) => {
            const number = Number(element.quantity);
            if (isNaN(number)) totalNumber += 0;
            else totalNumber += number;
          });
        }
        this.otherInfo.totalNumber = totalNumber;
        return totalNumber;
      },
      calcAllBid() {
        let allBid = 0;
        if (this.storageDetailTable) {
          this.storageDetailTable.forEach((element) => {
            const number = Number(element.totalInPrice);
            if (isNaN(number)) allBid += 0;
            else allBid += number;
          });
        }
        this.otherInfo.allBid = allBid;
        return allBid;
      },
      calcAllRetailPrice() {
        let allRetailPrice = 0;
        if (this.storageDetailTable) {
          this.storageDetailTable.forEach((element) => {
            const number = Number(element.totalOutPrice);
            if (isNaN(number)) allRetailPrice += 0;
            else allRetailPrice += number;
          });
        }
        this.otherInfo.allRetailPrice = allRetailPrice;
        return allRetailPrice;
      },
      testSelectGoods(data) {
      },
      // Sự kiện nhấn nút nhập hàng loạt
      importStudentExcel() {
        this.importDialogVisible = true;
      },

      // Đóng cửa sổ nhập
      cancellation() {
        this.importDialogVisible = false;
      },


      //Phương pháp xử lý khi tải tệp lên
      handleChange(file, fileList) {
        this.fileTemp = file.raw;
        if (this.fileTemp) {
          if ((this.fileTemp.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet')
            || (this.fileTemp.type === 'application/vnd.ms-excel')) {
            this.importfxx(this.fileTemp);
          } else {
            this.$message({
              type: 'warning',
              message: 'Định dạng tệp đính kèm sai，Vui lòng xóa rồi tải lên lại！'
            })
          }
        } else {
          this.$message({
            type: 'warning',
            message: 'Vui lòng tải tệp đính kèm lên！'
          })
        }
      },
      //Phương pháp xử lý khi vượt quá số lượng tệp tải lên tối đa
      handleExceed() {
        this.$message({
          type: 'warning',
          message: 'Giới hạn vượt quá số lượng tệp tải lên tối đa！'
        })
        return;
      },
      //Thao tác xóa tệp
      handleRemove(file, fileList) {
        this.fileTemp = null
      },

      async importfxx(obj) {
        let _this = this;
        let inputDOM = this.$refs.inputer;
        // QuaDOMLấy dữ liệu tệp

        this.file = event.currentTarget.files[0];

        const rABS = false; //Có đọc tệp dưới dạng chuỗi nhị phân không
        const f = this.file;
        const newList = this.newList;
        const storageDetailTable = this.storageDetailTable;
        let dr = 0;
        let wdr = 0;
        let a = 0;
        let cw = "";


        const reader = new FileReader();
        //if (!FileReader.prototype.readAsBinaryString) {
        FileReader.prototype.readAsBinaryString = function (f) {
          let binary = "";
          const rABS = false; //Có đọc tệp dưới dạng chuỗi nhị phân không
          const pt = this;
          let wb; //Dữ liệu đã đọc xong
          let outdata;
          const reader = new FileReader();
          reader.onload = function (e) {
            const bytes = new Uint8Array(reader.result);
            const length = bytes.byteLength;
            for (let i = 0; i < length; i++) {
              binary += String.fromCharCode(bytes[i]);
            }
            //Tại đây nhập vào，Dùng để phân tíchexcel
            const XLSX = require("xlsx");
            if (rABS) {
              wb = XLSX.read(btoa(fixdata(binary)), {
                //Chuyển đổi thủ công
                type: "base64"
              });
            } else {
              wb = XLSX.read(binary, {
                type: "binary"
              });
            }
            outdata = XLSX.utils.sheet_to_json(wb.Sheets[wb.SheetNames[0]]);
            //outdatachính là dữ liệu đã đọc（không bao gồm dòng tiêu đề tức là hàng đầu bảng，Hàng đầu bảng sẽ làm chỉ số của đối tượng）
            //Tại đây có thể xử lý dữ liệu
            // let arr = [];
            // outdata.map(v => {
            //     let obj = {}
            //     obj.code = v['Code']
            //     obj.name = v['Name']
            //     obj.pro = v['province']
            //     obj.cit = v['city']
            //     obj.dis = v['district']
            //     arr.push(obj)
            // });
            // _this.da=arr;
            // _this.dalen=arr.length;
            for (let i = 0; i < outdata.length; i++) {
              for (let j = 0; j < newList.length; j++) {
                if (outdata[i].药品材料名称 === newList[j].goodsName && outdata[i].规格 === newList[j].specification && outdata[i].售价 === newList[j].price) {
                  newList[j].batchNumber = outdata[i].生产批号;
                  newList[j].producedDate = outdata[i].生产日期;
                  newList[j].expireDate = outdata[i].过期日期;
                  newList[j].quantity = outdata[i].入库数量;
                  newList[j].inPrice = outdata[i].进价;
                  newList[j].outPrice = newList[j].price;
                  storageDetailTable.push(newList[j]);
                  dr += 1;
                  a = 1;
                }
              }
              if (a !== 1){
                wdr += 1;
                cw += "Dòng thứ"+(i+1)+"dòng["+outdata[i].药品材料名称+"]Thông tin bất thường，Vui lòng kiểm tra rồi nhập lại\n";
              }
              a = 0;
            }
            return this.storageDetailTable = storageDetailTable;
          };
          reader.readAsArrayBuffer(f);
        }
        if (rABS) {
          reader.readAsArrayBuffer(f);
        } else {
          reader.readAsBinaryString(f);
        }
        this.cancellation();

        setTimeout(() => {
          if (wdr!==0){
            this.chengGong = dr
            this.shiBai = wdr
            this.mistake = cw
            this.dialogVisible = true
          }
        }, 3000);
      },

      // Tải mẫu xuống
      downloadExcel() {
        let a = document.createElement('a');
        let evt = document.createEvent('MouseEvents');
        a.download = this.addType === 1 ? 'Thông tin nhập thuốc' : 'Thông tin nhập vật tư';
        a.href = fileurl;
        evt.initEvent('click', true, true);
        a.dispatchEvent(evt);
        window.URL.revokeObjectURL(a.href);
      },
    },
    mounted() {
      this.GetSysParamConfig();
      this.getClinicList()
      if (this.addType === 1) this.getDrugList();
      if (this.addType === 2) this.getStuffList();


    },
  };
</script>
<style scoped>
  .quantity-box {
    display: flex;

  }

  .footer-box {
    padding: 30px 20px 0 20px;
    margin-left: -20px;
  }

  .footer-box1 {
    display: flex;
    justify-content: right;
    align-items: center;
  }

  .popover {
    max-height: 300px !important;
  }
</style>
<style>
  .popover {
    max-height: 300px !important;
  }
</style>
