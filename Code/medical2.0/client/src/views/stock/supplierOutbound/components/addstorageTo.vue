<template>
  <el-dialog custom-class="customWidth" width="90%" modal="true" :close-on-click-modal="true" :title="title"
    :visible.sync="dialogVisibleTo">
    <div>
      <el-form :model="otherInfo" :rules="formRules" ref="stuffForm" label-width="120px" style="marginTop: 10px"
        label-position="right" class="edit-form">
        <el-row>
          <el-col :span="6">
            <SearchItem label="Nhà cung cấp">
              <SupplierSelect v-model="supplierId" :type="addType"></SupplierSelect>
            </SearchItem>
          </el-col>
          <el-col :span="6">
            <SearchItem :label="addType === '1'?'Tên thuốc':'Tên vật tư'">
              <DrugStuffStorageSelect :type="addType" :supplierId="supplierId"
                @onMedicinalStorageSelect="onMedicinalStorageSelect"></DrugStuffStorageSelect>
            </SearchItem>
          </el-col>

          <el-col :span="6">
            <SearchItem label="Phương thức xuất kho">
              <DictItemSelect type="outboundMethod" v-model="otherInfo.method"></DictItemSelect>
            </SearchItem>
          </el-col>
          <el-col :span="6">
            <SearchItem label="Ghi chú">
              <el-input v-model="otherInfo.remarks" placeholder="Ghi chú phiếu xuất kho"></el-input>
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
              <el-table-column
                v-if="addType === 1"
                prop="drugTracCodg"
                label="Mã truy xuất thuốc"
              > <template slot-scope="scope">
                <el-button :type="scope.row.completed ? 'success' : 'warning'" @click="openDrugDialog(scope)">Nhập mã truy xuất</el-button>
              </template></el-table-column>
              <el-table-column prop="specification" label="Có tách hộp không">
                <template slot-scope="scope"><el-switch
                  v-model="scope.row.isSplit"
                  active-color="#13ce66"
                  inactive-color="#ff4949"
                  @change="updateMaxInputCount(scope.row)">
                </el-switch>
                </template>
              </el-table-column>

              <el-table-column label="Số lượng" width="250" v-if="thisType===1">
                <template slot-scope="scope">
                  <div class="quantity-box  num-color" v-if="parseInt(scope.row.surplusStock/scope.row.drug.preparation) < scope.row.packAmount
                  || (parseInt(scope.row.surplusStock/scope.row.drug.preparation) === scope.row.packAmount && parseInt(scope.row.surplusStock%scope.row.drug.preparation) < scope.row.preparationAmount)">
                    <el-input v-model="scope.row.packAmount" type="number" @input="onNumInput" min="0" required="true" placeholder="Số lượng">

                    </el-input>
                    <DictItemView type="medicalPackUnit" :dictValue="scope.row.packValue" style="width: 80px;">
                    </DictItemView>
                    <el-input v-model="scope.row.preparationAmount" type="number" min="0" required="true"
                              placeholder="Số lượng">
                    </el-input>
                    <DictItemView type="medicalPreparationUnit" :dictValue="scope.row.preparationUnitValue"
                                  style="width: 80px;"></DictItemView>
                  </div>
                  <div v-else class="quantity-box">
                    <el-input v-model="scope.row.packAmount" type="number" min="0" required="true" placeholder="Số lượng">

                    </el-input>
                    <DictItemView type="medicalPackUnit" :dictValue="scope.row.packValue" style="width: 80px;">
                    </DictItemView>
                    <el-input  v-if="scope.row.isSplit"  v-model="scope.row.preparationAmount" type="number" min="0" required="true"
                      placeholder="Số lượng">
                    </el-input>
                    <DictItemView   v-if="scope.row.isSplit"  type="medicalPreparationUnit" :dictValue="scope.row.preparationUnitValue"
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
                    <el-input v-model="scope.row.packAmount" type="number" min="0" required="true" placeholder="Số lượng">
                    </el-input>
                    <DictItemView type="medicalPackUnit" :dictValue="scope.row.packUnitValue" style="width: 80px;">
                    </DictItemView>
                    <el-input v-if="scope.row.isSplit" v-model="scope.row.preparationAmount" type="number" min="0" required="true"
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
                <template slot-scope="scope">{{ calcTotalInPrice(scope.row)|numberFormatter(2) }}
                </template>
              </el-table-column>
              <el-table-column label="Tổng giá bán lẻ">
                <template slot-scope="scope">{{ calcTotalOutPrice(scope.row) |numberFormatter(2)}}
                </template>
              </el-table-column>
              <el-table-column label="" width="80">
                <template slot-scope="scope">
                  <el-button type="danger" size="small"
                    @click.native.prevent="deleteRow(scope.$index, storageDetailTable)">Xóa
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="3" :offset="12">Chủng loại: {{ CalcBreed() }}</el-col>
          <el-col :span="3">Số lượng: {{ calcTotalNumber() }}</el-col>
          <el-col :span="3">Tổng giá nhập: {{ otherInfo.allBid}}</el-col>
          <el-col :span="3">Tổng giá bán lẻ: {{ otherInfo.allRetailPrice }}</el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <div class="footer-box footer-box1">
              <el-button type="primary" @click="saveSupplierOutbound">Xác nhận</el-button>
              <el-button type="" @click="closeDialog()">Hủy</el-button>
            </div>
          </el-col>
        </el-row>
      </el-form>
    </div>
    <!-- Hộp thoại hiện sau khi nhấn nhập mã truy xuất -->
    <el-dialog @keydown.native="handleKeydown" :modal="false"  :visible.sync="drugDialogVisible" title="Nhập mã truy xuất thuốc" @open="focusInput" @close="resetInput">
      <el-input
        v-model="currentInput"
        placeholder="Vui lòng nhập mã truy xuất thuốc"
        ref="inputFocus"
      ></el-input>
      <span slot="footer" class="dialog-footer">
        <el-button @click="submitInput" type="primary">Xác nhận</el-button>
        <el-button @click="drugDialogVisible = false">Hủy</el-button>
      </span>
    </el-dialog>
  </el-dialog>
</template>

<script>
  import DictItemView from "@/views/components/dict-item-view/index.vue";
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
      addType: null,
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
      DictItemView
    },
    data() {
      return {
        test: 1,
        thisType: 1, //Loại hiện tại, Thuốc hoặc vật tư
        flag: true,
        outboundId: 0, //Mã đơn xuất kho hiện cần sửa
        supplierId: null,
        dialogVisibleTo: false, //Có hiển thị không
        //Đặt ngày trước hôm nay của ngày hết hiệu lực thành không thể chọn
        expireTimeOption: {
          // disabledDate(date) {
          // //disabledDate Trên tài liệu: Đặt trạng thái vô hiệu hóa, Tham số là ngày hiện tại, Yêu cầu trả về Boolean
          //   return date.getTime() < Date.now() - 24 * 60 * 60 * 1000;
          // },
        },
        // radio: "1", // 1 Thuốc, 2Vật tư
        company_id: currentUser.company.id /*Phòng khámID*/ ,
        examine_id: "1005787933775863930" /*Trạng thái đã quaID*/ ,
        examine_value: "supplierStorageExamineStatus_0" /*Trạng thái đã quaValuegiá trị*/ ,
        title: "Thêm mới đơn nhập kho",
        goodsNameTemp: "",
        drugPreparation: 0,
        stuffPackNum: 0,
        drugStuffName: "", //Tên thuốc hoặc vật tư
        drugStuffPlaceholder: "", //tương ứngplaceholder
        goodsInfo: null,
        storageDetailTable: [],
        reasonLists: null, //Các lý do xuất kho khác nhau
        methodLists: [{
          name: 'Xuất bán',
          id: 1
        }], //Các hình thức xuất kho khác nhau
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
        },
        reason: {
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
        //Điều khiển hộp thoại mã truy xuất
        drugDialogVisible: false,
        currentInput: '',
        inputCount: 0, // Số lần nhập hiện tại
        maxInputCount: 0, // Số lần nhập tối đa
        currentRow: null, // Dòng đang thao tác
        allInputs: [], // Tất cả mã truy xuất đã nhập
      };
    },
    watch: {
      addType() {
        this.thisType = this.addType;
      },
      storageDetailTable: {
        handler(newVal, oldVal) {
          if (newVal.length !== 0) {
            newVal.forEach((i, ind) => {
              if (
                parseInt(i.surplusStock/i.drug.preparation) < i.packAmount
                || (parseInt(i.surplusStock/i.drug.preparation) === i.packAmount && parseInt(i.surplusStock%i.drug.preparation) < i.preparationAmount)
              ) {
                i.packAmount = 0
                i.preparationAmount = 0
                newVal[ind].expireDate = "";
                this.$message({
                  message: i.drugStuffName+"Số lượng tồn khả dụng không đủ",
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
    inject: ["fatherMethod"],

    methods: {
      handleKeydown(event) {
        if (event.key === 'Enter') {
          // Gọi thao tác xác nhận
          this.submitInput();
        }
      },
      focusInput() {
        // Sử dụng ref để truy cập ô nhập và gọi focus() Phương pháp
        this.$nextTick(() => {
          this.$refs.inputFocus.focus(); // Tự động focus vào ô nhập
        });
      },
      openDrugDialog(scope) {
        if ( scope.row.packAmount <= 0 || ! scope.row.packAmount) {
          this.$message.error('Vui lòng nhập trước số lượng hợp lệ và có tách hộp không!');
          return; // Nếu số lượng không hợp lệ, Dừng thực hiện
        }
        this.currentRow = scope.row; // Ghi lại dòng hiện tại
        this.inputCount = 0; // Đặt lại bộ đếm nhập liệu
        this.allInputs = []; // Xóa dữ liệu nhập trước đó
        this.drugDialogVisible = true;
        // Đặt số lần nhập cơ bản và tối đa theo số lượng của dòng hiện tại
        const quantity = this.currentRow.packAmount;
        this.maxInputCount = this.currentRow.isSplit ? this.currentRow.preparation * quantity: quantity;
      },
      submitInput() {
        if (this.currentInput) {
          this.allInputs.push(this.currentInput); // Thêm dữ liệu nhập hiện tại
          this.currentRow.drugTracCodg = this.allInputs.join(','); // Cập nhật mã truy xuất thuốc của dòng hiện tại
          this.currentInput = ''; // Xóa ô nhập
          this.inputCount++; // Tăng bộ đếm nhập

          // Kiểm tra đã đạt số lần nhập tối đa chưa
          if (this.inputCount >= this.maxInputCount) {
            this.$set(this.currentRow, 'completed', true); // Đặt thành trạng thái hoàn thành
            this.drugDialogVisible = false; // Đóng hộp thoại
          }
          this.$nextTick(() => {
            this.$refs.inputFocus.focus(); // Tự động focus vào ô nhập
          });
        }
      },
      resetInput() {
        this.currentInput = ''; // Xóa ô nhập
        this.inputCount = 0; // Đặt lại bộ đếm nhập liệu
        this.currentRow = null; // Xóa bản ghi dòng hiện tại
      },
      updateMaxInputCount(row) {
        this.$set(this.currentRow, 'isSplit', true); // Đặt thành trạng thái hoàn thành
        // Mỗi khi trạng thái công tắc thay đổi thì cập nhật số lần nhập tối đa
        const quantity = row.packAmount;
        this.maxInputCount = row.isSplit ? quantity * row.preparationAmount: quantity;
      },


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
            that.otherInfo = {
              id: data.id,
              code: data.code,
              number: data.number,
              breed: data.breed /*Số lượng chủng loại*/ ,
              totalNumber: 0 /*Tổng số lượng*/ ,
              allBid: data.allBid /*Tổng giá nhập*/ ,
              allRetailPrice: data.allRetailPrice /*Tổng giá bán lẻ*/ ,
              remarks: data.remarks /*Ghi chú*/ ,
              method: data.method,
              reason: data.reason
            };
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
      //Kích hoạt khi chọn một tồn kho
      onMedicinalStorageSelect(row) {
        let obj = {
          drug: row.drug,
          stuff: row.stuff,
          occupyStock: row.occupyStock,
          surplusStock : row.surplusStock,
          drugStuffName:row.drugStuffName,
        };
        //Chuyển toàn bộ giá trị từ đối tượng con ra gốc để dễ xử lý
        obj.factoryName = null;
        if (row.drug && row.drug.id) {
          obj.factoryName = row.drug.factory.name;
          obj.factoryName = row.drug.factory.name;
          obj.packValue = row.drug.pack.value;
          obj.preparationUnitValue = row.drug.preparationUnit.value;
        } else if (row.stuff && row.stuff.id) {
          obj.factoryName = row.stuff.factory.name;
          obj.packUnitValue = row.stuff.packUnit.value;
          obj.minUnitValue = row.stuff.minUnit.value;
        }
        //obj.drug = row.drug;
        //obj.stuff = row.stuff;
        obj.medicinalStorageId = row.id;
        obj.norms = row.supplierStock.norms;
        obj.batchNo = row.supplierStock.batchNo;
        obj.produceDate = row.supplierStock.produceDate;
        obj.endDate = row.supplierStock.endDate;
        obj.bid = row.supplierStock.bid;
        obj.retailPrice = row.supplierStock.retailPrice;
        obj.packName = this.getPackName(row);
        obj.packAmount = 0;
        obj.preparationAmount = 0;
        this.storageDetailTable.push(obj);
      },

      //Lưu phiếu xuất kho
      saveSupplierOutbound() {
        /* if (!this.otherInfo.supplierID) {
          this.$message({
            message: "Vui lòng chọn người xuất kho",
            type: "warning"
          });
          return;
        } */
        if (!this.otherInfo.method) {
          this.$message({
            message: "Vui lòng chọn phương thức xuất kho",
            type: "warning"
          });
          return;
        }

        if (this.storageDetailTable.length === 0) {
          this.$message({
            message: "Vui lòng nhập thông tin chi tiết nhập kho",
            type: "warning"
          });
          return;
        }

        const day = new Date();
        const saveInfo = {
          supplierOutbound: {
            id: this.otherInfo.id,
            method: this.otherInfo.method,
            remarks: this.otherInfo.remarks,
            company: {
              id: this.company_id,
            },
            supplier: {
              id: this.otherInfo.supplierID ? this.otherInfo.supplierID : 0,
            },
            code: "",
            breed: this.otherInfo.breed,
            number: this.stuffPackNum === 0 ? this.otherInfo.totalNumber * this.drugPreparation : this.otherInfo
              .totalNumber * this.stuffPackNum,
            allBid: this.otherInfo.allBid,
            allRetailPrice: this.otherInfo.allRetailPrice,
            reason: null
          },
          supplierOutboundOrderList: [],
        };
        this.storageDetailTable.forEach((element) => {
          try {
            if (element.quantity === "" || element.quantity === 0) {
              throw "Số lượng";
            }
            const detail = {
              company: {
                // Phòng khámid
                id: this.company_id,
              },
              number: this.getNumber(element), //element.quantity,
              cnt:   element.packAmount,        //Số lượng bảo hiểm y tế
              drugTracCodg:element.drugTracCodg,//Mã truy xuất bảo hiểm y tế
              supplierId: {
                // Nhà cung cấp
                id: this.otherInfo.supplierID,
              },
              drug: {
                // Tên thuốc
                id: element.drug.id
              },
              stuff: {
                // Tên vật tư
                id: element.stuff.id
              },
              currentStock: element.surplusStock,
              medicinalStorage: {
                id: element.medicinalStorageId
              },
              remark: '',
              initial: element.surplusStock
            };
            saveInfo.supplierOutboundOrderList.push(detail);
          } catch (e) {
            console.error('Lỗi định dạng chi tiết phiếu xuất kho', e);
          }
        });
        if (this.flag === "Số lượng") {
          this.$message({
            message: "Số lượng nhập kho không được để trống",
            type: "warning"
          });
          this.flag = "";
          return;
        } else {
          saveSupplierOutbound(saveInfo).then((responseData) => {
            if (responseData.code === 100) {
              this.$message({
                message: "Lưu thành công",
                type: "success"
              });
              this.$emit('onSupplierOutboundUpdated');
              this.closeDialog();
            } else {
              this.$message({
                message: responseData.msg,
                type: "error"
              });
            }
          });
        }
      },
      //Đóng hộp thoại
      closeDialog() {
        this.dialogVisibleTo = false;
        //this.fatherMethod();
        this.reset();
      },
      reset() {
        this.storageDetailTable = [];

        this.otherInfo = {
          id: null,
          code: null,
          number: 0,
          breed: 0 /*Số lượng chủng loại*/ ,
          totalNumber: 0 /*Tổng số lượng*/ ,
          allBid: 0 /*Tổng giá nhập*/ ,
          allRetailPrice: 0 /*Tổng giá bán lẻ*/ ,
          remarks: "" /*Ghi chú*/ ,
          reason: null
        };
      },
      deleteRow(index, tb) {
        this.storageDetailTable.splice(index, 1);
      },
      calcTotalInPrice(row) {
        const number = this.getNumber(row);
        if (isNaN(number)) return;
        const inPrice = Number(row.bid);
        if (isNaN(inPrice)) return;
        const result = number * inPrice/row.drug.preparation;
        row.totalInPrice = result;
        this.calcAllBid()
        return result;
      },
      calcTotalOutPrice(row) {
        const number = this.getNumber(row);
        if (isNaN(number)) return;
        const outPrice = Number(row.retailPrice);
        if (isNaN(outPrice)) return;
        let result = number * outPrice/row.drug.preparation;
        result = result.toFixed(2)
        row.totalOutPrice = result;
        this.calcAllRetailPrice()
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
            packNumber=parseInt(row.drug.preparation);
        } else if (row.stuff && row.stuff.id) {
          packNumber = parseInt(row.stuff.packNumber);
        }

        let n= parseInt(row.packAmount) * packNumber + parseInt( row.preparationAmount);
        return n;
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
            if (isNaN(number)){
              allRetailPrice += 0;
            } else {
              allRetailPrice += number;
            }
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

  .num-color {
    color: red;
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
