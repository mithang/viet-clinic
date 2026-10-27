<template>
  <el-dialog custom-class="customWidth" width="90%" modal="true" :close-on-click-modal="true" :title="title"
    :visible.sync="dialogVisibleTo">
    <div>
      <el-form :model="otherInfo" :rules="formRules" ref="stuffForm" label-width="120px" style="marginTop: 10px"
        label-position="right" class="edit-form">
        <el-row>
          <el-col :span="3">
            <SearchItem label="Trạng thái">
              <SupplierOutboundStatusView v-model="outbound.status"></SupplierOutboundStatusView>
            </SearchItem>
          </el-col>
          <el-col :span="4">
            <SearchItem  label="Người tạo">
              {{outbound.createBy}}
            </SearchItem>
          </el-col>
          <el-col :span="4">
            <SearchItem  label="Thời gian tạo">
              {{outbound.createDate}}
            </SearchItem>
          </el-col>
          <el-col :span="3">
            <SearchItem  label="Người duyệt">
              {{outbound.examineBy}}
            </SearchItem>
          </el-col>

          <el-col :span="4">
            <SearchItem label="Phương thức xuất kho">
              <DictItemView type="outboundMethod" :dictValue="outbound.method"></DictItemView>
            </SearchItem>
          </el-col>
          <el-col :span="6">
            <SearchItem label="Ghi chú">
              <el-input   :disabled="true" v-model="outbound.remarks" ></el-input>
            </SearchItem>
          </el-col>
        </el-row>

        <br>
        <el-row>
          <el-col :span="24">
            <el-table :data="storageDetailTable" style="width: 100%" height="350">
              <el-table-column prop="drugStuffName" label="Tên" width="150">
              </el-table-column>
              <el-table-column prop="norms" label="Quy cách"></el-table-column>
              <el-table-column prop="factoryName" label="Nhà sản xuất"></el-table-column>
              <el-table-column prop="batchNo" label="Số lô sản xuất"></el-table-column>
              <el-table-column label="Ngày sản xuất">
                <template slot-scope="scope">{{scope.row.produceDate|dateFormat}}</template>
              </el-table-column>
              <el-table-column label="Ngày hiệu lực">
                <template slot-scope="scope">{{scope.row.endDate|dateFormat}}</template>
              </el-table-column>
              <!-- <el-table-column prop="occupyStock" label="Chiếm dụng tồn kho"></el-table-column>-->

              <el-table-column prop="surplusStock" label="Tồn kho khả dụng" v-if="thisType===1">
                <template slot-scope="scope">
                  {{parseInt(scope.row.surplusStock/scope.row.drug.preparation)}}
                  <DictItemView type="medicalPackUnit" :dictValue="scope.row.packValue" style="width: 80px;">
                  </DictItemView>
                  {{parseInt(scope.row.surplusStock%scope.row.drug.preparation)}}
                  <DictItemView type="medicalPreparationUnit" :dictValue="scope.row.preparationUnitValue"
                    style="width: 80px;"></DictItemView>
                </template>
              </el-table-column>
              <el-table-column label="Số lượng" width="250" v-if="thisType===1">
                <template slot-scope="scope">
                  <div class="quantity-box">
                    <el-input v-model="scope.row.packAmount" :disabled="true" type="number" min="0" required="true" placeholder="Số lượng">
                    </el-input>
                    <DictItemView type="medicalPackUnit" :dictValue="scope.row.packValue" style="width: 80px;">
                    </DictItemView>
                    <el-input v-model="scope.row.preparationAmount" :disabled="true" type="number" min="0" required="true"
                      placeholder="Số lượng">
                    </el-input>
                    <DictItemView type="medicalPreparationUnit" :dictValue="scope.row.preparationUnitValue"
                      style="width: 80px;"></DictItemView>
                  </div>
                </template>
              </el-table-column>

              <el-table-column prop="surplusStock" label="Tồn kho khả dụng" v-if="thisType===2">
                <template slot-scope="scope">
                  {{parseInt(scope.row.surplusStock/scope.row.stuff.packNumber)}}
                  <DictItemView type="medicalPackUnit" :dictValue="scope.row.packUnitValue" style="width: 80px;">
                  </DictItemView>
                  {{parseInt(scope.row.surplusStock%scope.row.stuff.packNumber)}}
                  <DictItemView type="medicalPreparationUnit" :dictValue="scope.row.minUnitValue" style="width: 80px;">
                  </DictItemView>
                </template>
              </el-table-column>
              <el-table-column label="Số lượng" width="250" prop="specification" v-if="thisType===2">
                <template slot-scope="scope">
                  <div class="quantity-box">
                    <el-input v-model="scope.row.packAmount" :disabled="true" type="number" min="0" required="true" placeholder="Số lượng">
                    </el-input>
                    <DictItemView type="medicalPackUnit" :dictValue="scope.row.packUnitValue" style="width: 80px;">
                    </DictItemView>
                    <el-input v-model="scope.row.preparationAmount" :disabled="true" type="number" min="0" required="true"
                      placeholder="Số lượng">
                    </el-input>
                    <DictItemView type="medicalPreparationUnit" :dictValue="scope.row.minUnitValue"
                      style="width: 80px;"></DictItemView>
                  </div>
                </template>
              </el-table-column>
              <!-- <el-table-column prop="reason" label="Lý do xuất kho">
                <el-select v-model="otherInfo.reason" filterable placeholder="Vui lòng chọn lý do xuất kho">
                  <el-option v-for="reasonxz in reasonLists" :label="reasonxz.name" :value="reasonxz.id"
                    :key="reasonxz.id"></el-option>
                </el-select>
              </el-table-column>-->
              <el-table-column prop="bid" label="Giá nhập"></el-table-column>
              <el-table-column prop="retailPrice" label="Giá bán lẻ"></el-table-column>
              <el-table-column label="Tổng giá nhập">
                <template slot-scope="scope">{{ calcTotalInPrice(scope.row) |numberFormatter(2)}}
                </template>
              </el-table-column>
              <el-table-column label="Tổng giá bán lẻ">
                <template slot-scope="scope">{{ calcTotalOutPrice(scope.row)|numberFormatter(2) }}
                </template>
              </el-table-column>
            </el-table>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="3" :offset="12">Chủng loại：{{ CalcBreed() }}</el-col>
          <el-col :span="3">Số lượng：{{ calcTotalNumber() }}</el-col>
          <el-col :span="3">Tổng giá nhập：{{ calcAllBid() |numberFormatter(2)}}</el-col>
          <el-col :span="3">Tổng giá bán lẻ：{{ calcAllRetailPrice() |numberFormatter(2) }}</el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <div class="footer-box footer-box1">
              <el-button type="" @click="closeDialog()">Hủy</el-button>
            </div>
          </el-col>
        </el-row>
      </el-form>
    </div>
  </el-dialog>
</template>

<script>
  import DictItemView from "@/views/components/dict-item-view/index.vue";
  import SupplierOutboundStatusView from "@/views/components/supplier-outbound-status-view/index.vue";
  import {
    listDrugAll
  } from "@/api/stock/drug";
  import {
    listStuffAll,
    listStuffPage,
    listDrug
  } from "@/api/stock/stuff";
  import {
    saveSupplierStockList
  } from "@/api/stock/supplierStock";
  import {
    listSupplierPage,
    getSupplierById,
    listSupplierAll
  } from "@/api/stock/supplier";
  import {
    saveSupplierOutbound,
    getSupplierOutbound
  } from "@/api/stock/supplierOutbound";
  import {
    listByBoutbound
  } from "@/api/stock/supplierOutboundDetail";
  import {
    BigNumber
  } from "bignumber.js";
  import BaseUI from "@/views/components/baseUI";
  import SearchItem from "@/views/components/search-item/index.vue";
  import SupplierSelect from "@/views/components/supplier-select/index.vue";
  import DrugStuffStorageSelect from "@/views/components/drug-stuff-storage-select/index.vue";
  import SupplierOutboundMethodSelect from "@/views/components/supplier-outbound-method-select/index.vue";
  import DictItemSelect from "@/views/components/dict-item-select/index.vue";
  import {
    listDictItemAll
  } from "@/api/sys/dictItem";

  export default {
    props: {
      title: {
        type: String,
        default: null
      }
    },
    components: {
      SearchItem,
      SupplierSelect,
      DrugStuffStorageSelect,
      SupplierOutboundMethodSelect,
      DictItemSelect,
      DictItemView,
      SupplierOutboundStatusView
    },
    data() {
      return {
        thisType: 1, //Loại hiện tại，Thuốc hoặc vật tư
        outboundId: 0, //Mã đơn xuất kho hiện cần sửa
        dialogVisibleTo: false, //Có hiển thị không
        // radio: "1", // 1 Thuốc，2Vật tư
        company_id: currentUser.company.id /*Phòng khámID*/ ,
        outbound: {},
        storageDetailTable: [],
        otherInfo: {
          supplierID: null,
          /*Nhà cung cấp*/
          reason: null,
          /*Lý do xuất kho*/
          method: null,
          /*Phương thức xuất kho*/
          breed: 0 /*Số lượng chủng loại*/ ,
          totalNumber: 0 /*Tổng số lượng*/ ,
          allBid: 0 /*Tổng giá nhập*/ ,
          allRetailPrice: 0 /*Tổng giá bán lẻ*/ ,
          withGoodsNo: "" /*Số đơn cung ứng*/ ,
          invoiceNumber: "" /*Số hóa đơn*/ ,
          remarks: "" /*Ghi chú*/ ,
        }
      };
    },
    watch: {},
    methods: {
      openDialog(outboundId) {
        this.reset();
        this.outboundId = outboundId;
        this.dialogVisibleTo = true;
        if (this.outboundId) {
          this.initOutbound();
        }
      },
      //TừoutboundIdKhởi tạo đơn xuất kho để sửa
      initOutbound() {
        this.loadSupplierOutbound();
        this.loadSupplierOutboundDetail();
      },
      loadSupplierOutbound() {
        let that = this;
        getSupplierOutbound(this.outboundId).then((res) => {
          if (res.code === 100) {
            let data = res.data;
            this.outbound = data;
            /* that.otherInfo = {
              id: data.id,
              code: data.code,
              number: data.number,
              breed: data.breed  ,//Số lượng chủng loại
              totalNumber: 0   ,//Tổng số lượng
              allBid: data.allBid  ,//Tổng giá nhập
              allRetailPrice: data.allRetailPrice   ,//Tổng giá bán lẻ
              remarks: data.remarks ,//Ghi chú
              method: data.method,
              reason: data.reason
            }; */
          }
        });
      },
      loadSupplierOutboundDetail() {
        let that = this;
        listByBoutbound(this.outboundId).then((res) => {
          if (res.code === 100) {
            let arr = [];
            let data = res.data;
            for (let i in data) {
              let row = data[i];
              let obj = {
                id: row.id,
                quantity: row.quantity,
                drug: row.drug,
                stuff: row.stuff
              };
              obj.drugStuffName = row.medicinalStorage.drugStuffName;
              obj.factoryName = null;
              obj.packAmount = 0;
              obj.preparationAmount = 0;
              if (row.drug && row.drug.id) {
                obj.factoryName = row.drug.factory.name;
                obj.packValue = row.drug.pack.value;
                obj.preparationUnitValue = row.drug.preparationUnit.value;
                this.thisType = 1;
                let packNumber = parseInt(row.drug.preparation);
                obj.packAmount = parseInt(row.number / packNumber);
                obj.preparationAmount = row.number % packNumber;
              } else if (row.stuff && row.stuff.id) {
                obj.factoryName = row.stuff.factory.name;
                obj.packUnitValue = row.stuff.packUnit.value;
                obj.minUnitValue = row.stuff.minUnit.value;
                this.thisType = 2;
                obj.packAmount = parseInt(row.number / row.stuff.packNumber);
                obj.preparationAmount = row.number % row.stuff.packNumber;
              }
              //obj.drug = row.drug;
              obj.stuff = row.stuff;
              obj.norms = row.supplierStock.norms;
              obj.batchNo = row.supplierStock.batchNo;
              obj.produceDate = row.supplierStock.produceDate;
              obj.endDate = row.supplierStock.endDate;
              obj.bid = row.supplierStock.bid;
              obj.retailPrice = row.supplierStock.retailPrice;
              obj.medicinalStorageId = row.medicinalStorage.id;
              obj.occupyStock = row.medicinalStorage.occupyStock;
              obj.surplusStock = row.medicinalStorage.surplusStock;
              arr.push(obj);
            }
            that.storageDetailTable = arr;
          }
        });
      },
      //Đóng hộp thoại
      closeDialog() {
        this.dialogVisibleTo = false;
        this.reset();
      },
      reset() {
        this.storageDetailTable = [];
        this.outbound = {};
      },
      calcTotalInPrice(row) {
        const number = this.getNumber(row);
        if (isNaN(number)) return;
        const inPrice = Number(row.bid);
        if (isNaN(inPrice)) return;
        const result = number * inPrice;
        row.totalInPrice = result;
        return result;
      },
      calcTotalOutPrice(row) {
        const number = this.getNumber(row);
        if (isNaN(number)) return;
        const outPrice = Number(row.retailPrice);
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
            const number = this.getNumber(element); // Number(element.packAmount);
            if (isNaN(number)) totalNumber += 0;
            else totalNumber += number;
          });
        }
        this.otherInfo.totalNumber = totalNumber;
        return totalNumber;
      },
      //Lấy số lượng sau khi tách hộp
      getNumber(row) {
        let packNumber = 0;
        if (row.drug && row.drug.id) {
          packNumber = parseInt(row.drug.preparation);
        } else if (row.stuff && row.stuff.id) {
          packNumber = parseInt(row.stuff.packNumber);
        }

        let n = parseInt(row.packAmount) * packNumber + parseInt(row.preparationAmount);
        return n;
      },
      calcAllBid() {
        let allBid = 0;
        if (this.storageDetailTable) {
          this.storageDetailTable.forEach((element) => {
            const number = Number(element.bid);
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
            const number = Number(element.retailPrice);
            if (isNaN(number)) allRetailPrice += 0;
            else allRetailPrice += number;
          });
        }
        this.otherInfo.allRetailPrice = allRetailPrice;
        return allRetailPrice;
      },
      getPackName(item) {
        if (item.drug && item.drug.pack) return item.drug.pack.name;
        if (item.stuff && item.stuff.pack) return item.stuff.pack.name;
        return '';
      }
    },
    mounted() {},
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
