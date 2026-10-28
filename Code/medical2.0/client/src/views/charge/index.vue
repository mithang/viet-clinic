<template>
  <div class="charge_container">
    <el-alert
      title="Dữ liệu minh họa"
      type="warning"
      :closable="false"
      show-icon
      style="margin-bottom: 10px"
    >
      <div>
        Màn hình thu phí này hiện dùng dữ liệu minh họa cố định, chưa kết nối
        dữ liệu thu phí thật của phòng khám — vì vậy số liệu ở đây không khớp
        với trang Thu phí (tollInfo). Xem dữ liệu thu phí thật của phòng khám
        đang đăng nhập tại trang Thu phí.
      </div>
    </el-alert>
    <div class="charge_left">
      <div
        style="
          display: flex;
          justify-content: flex-end;
          margin-top: 10px;
        "
      >
        <el-button type="primary" @click="changeToRetail">Thu phí bán lẻ</el-button>
      </div>
      <el-divider></el-divider>
      <div
        style="display: flex; justify-content: flex-start;"
      >
        <el-input
          v-model="selectCondition"
          placeholder="Vui lòng nhập tên bệnh nhân/Số thẻ"
          prefix-icon="el-icon-search"
          style="margin-right: 8px"
          @keydown.enter.native="selectPatientList"
        ></el-input>
        <el-button type="text" @click="selectPatientList">Tìm kiếm</el-button>
      </div>
      <div>
        <el-tabs
          stretch
          v-model="chargeStatusActiveName"
          @tab-click="selectPatientList"
        >
          <el-tab-pane label="Chờ thu phí" name="1"></el-tab-pane>
          <el-tab-pane label="Đã thu phí" name="2"></el-tab-pane>
          <el-tab-pane label="Đã hoàn phí" name="3"></el-tab-pane>
        </el-tabs>
        <el-table
          :data="patientList"
          :show-header="false"
          highlight-current-row
          @row-click="selectPatientChange"
          style="width: 100%;height:80%;margin-top:-15px"
        >
          <el-table-column prop="name" label="Họ tên"> </el-table-column>
          <el-table-column
            prop="clinicDate"
            label="Thời gian khám"
            :formatter="clinicDateFormatter"
          >
          </el-table-column>
          <el-table-column prop="chargeStatus" label="Trạng thái thu phí">
          </el-table-column>
        </el-table>
        <div style="display: flex; justify-content: flex-end">
          <el-pagination
            layout="prev, next"
            :page-size="patientPageSize"
            prev-text="Trang trước"
            next-text="Trang sau"
            :current-page="patientCurrentPage"
            @prev-click="selectPatientList"
            @next-click="selectPatientList"
            :total="patientTotalCount"
          >
          </el-pagination>
        </div>
      </div>
    </div>
    <div class="charge_right">
      
      <div class="flex-space-between" style="margin-top: 10px">
        <div style="font-weight: bold">Thu phí</div>
        <div class="flex-space-between">
          <div class="goods-back">Đã trả thuốc</div>
          <div style="margin-top: 5px; margin-right: 10px">23.00</div>
          <el-button
            :type="chargeStatusActiveName === '1' ? 'warning' : ''"
            :disabled="chargeStatusActiveName === '3'"
            @click="chargeClick"
            >{{ chargeStatusActiveName === "2" ? "Hoàn phí" : "Thu phí" }}</el-button
          >
          <el-button v-if="chargeStatusActiveName === '2'">Xuất hóa đơn</el-button>
        </div>
      </div>
      <div class="patientinfo_form">
        <el-form
          ref="patientInfoForm"
          :model="patientInfoForm"
          :rules="isRetail ? patientInfoRules : patientInfoNoRules"
          label-width="80px"
        >
          <el-row>
            <el-col :span="8">
              <el-form-item label="Tên bệnh nhân" prop="name">
                <el-input v-model="patientInfoForm.name"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="5">
              <el-form-item label="Giới tính" prop="gender">
                <el-select v-model="patientInfoForm.gender">
                  <el-option
                    v-for="gender in genderList"
                    :key="gender.id"
                    :label="gender.name"
                    :value="gender.id"
                  >
                  </el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="11">
              <div
                style="
                  display: flex;
                  flex-direction: row;
                  justify-content: flex-start;
                "
              >
                <el-form-item label="Tuổi" prop="age_year">
                  <el-input v-model="patientInfoForm.age_year" type="number">
                    <template slot="append">tuổi</template>
                  </el-input>
                </el-form-item>
                <div style="margin-left: -80px">
                  <el-form-item prop="age_month">
                    <el-input v-model="patientInfoForm.age_month" type="number">
                      <template slot="append">tháng</template>
                    </el-input>
                  </el-form-item>
                </div>
              </div>
            </el-col>
          </el-row>
          <el-row :gutter="10">
            <el-col :span="8">
              <el-form-item label="Số điện thoại liên hệ" prop="telephone">
                <el-input v-model="patientInfoForm.telephone"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="11">
              <el-form-item label="Số CMND/CCCD" prop="idCardNo">
                <el-input v-model="patientInfoForm.idCardNo"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="5"></el-col>
          </el-row>
        </el-form>
      </div>
      <div v-if="isRetail">
        <div>
          <el-autocomplete
            popper-class="my-autocomplete"
            v-model="goodsNameTemp"
            :fetch-suggestions="queryGoods"
            placeholder="Vui lòng nhập tên thuốc"
            @select="selectGoods"
          >
            <template slot-scope="{ item }">
              <div>{{ item.goodsName }}</div>
            </template>
          </el-autocomplete>
          <el-link type="primary" @click="AddRetailDrug()">Thêm</el-link>
        </div>
        <el-table :data="data" style="width: 100%">
          <el-table-column prop="prop" label="Thông tin thuốc" width="width">
          </el-table-column>
          <el-table-column prop="prop" label="Nhà sản xuất" width="width">
          </el-table-column>
          <el-table-column prop="prop" label="Đơn giá" width="width">
          </el-table-column>
          <el-table-column prop="prop" label="Số lượng" width="width">
          </el-table-column>
          <el-table-column prop="prop" label="Đơn vị" width="width">
          </el-table-column>
          <el-table-column prop="prop" label="Tổng tiền" width="width">
          </el-table-column>
        </el-table>
      </div>
      <div v-else>
        <el-card
          class="prescriptionCard"
          v-for="(prescriptionMain, index) in prescriptionMainList"
          :key="index"
        >
          <div v-if="prescriptionMain.prescriptionType === 1">
            
            <el-table :data="prescriptionMain.prescriptionDetail" style="width: 100%">
              <el-table-column prop="goodsName" label="Đơn thuốc tây" width="width">
              </el-table-column>
              <el-table-column prop="factoryName" label="Nhà sản xuất" width="width">
              </el-table-column>
              <el-table-column prop="price" label="Đơn giá" width="width">
              </el-table-column>
              <el-table-column prop="quantity" label="Số lượng" width="width">
              </el-table-column>
              <el-table-column prop="unit" label="Đơn vị" width="width">
              </el-table-column>
              <el-table-column prop="totalCost" label="Tổng tiền" width="width">
              </el-table-column>
            </el-table>
            <div class="flex-end">
              <div>Tổng cộng{{prescriptionMain.prescriptionDetail.length }},Tổng cộng{{ getPrescriptionTotalCost(prescriptionMain.prescriptionDetail) }}VNĐ</div>
            </div>
          </div>
          <!--Thuốc đông y-->
          <div v-else-if="prescriptionMain.prescriptionType === 2">
            <el-table :data="prescriptionMain.prescriptionDetail" style="width: 100%">
              <el-table-column prop="goodsName" label="Đơn thuốc đông y" width="width">
              </el-table-column>
              <el-table-column prop="factoryName" label="Nhà sản xuất" width="width">
              </el-table-column>
              <el-table-column prop="tisanesMethod" label="Cách sắc thuốc" width="width">
              </el-table-column>
              <el-table-column prop="price" label="Đơn giá" width="width">
              </el-table-column>
              <el-table-column prop="quantity" label="Số lượng" width="width">
              </el-table-column>
              <el-table-column prop="totalQuantity" label="Tổng số lượng" width="width">
              </el-table-column>
              <el-table-column prop="totalCost" label="Tổng tiền" width="width">
              </el-table-column>
            </el-table>
            <div class="flex-space-between">
              <div class="flex-start">
                <div style="">{{ prescriptionMain.quantity }}thang</div>
                <div>Cách dùng：{{ prescriptionMain.useMethod }}</div>
                <div>{{ yypc }}</div>
                <!--煎药频次：1ngày1thang=-->
                <div>{{ fypc }}</div>
                <!--服药频次：1ngày2lần=-->
                <div>{{ mcyl }}</div>
                <!--Liều dùng mỗi lần：một lần500ml-->
              </div>
              <div>Tổng cộng{{ pz }},Tổng cộng{{ zj }}VNĐ</div>
            </div>
          </div>
          <!--Phí phụ thêm-->
          <div v-if="prescriptionMain.prescriptionType === 3">
            <el-table :data="prescriptionMain.prescriptionDetail" style="width: 100%">
              <el-table-column prop="goodsName" label="Phí phụ thêm" width="width">
              </el-table-column>
              <el-table-column prop="quantity" label="Số lượng" width="width">
              </el-table-column>
              <el-table-column prop="price" label="Đơn giá" width="width">
              </el-table-column>
              <el-table-column prop="totalCost" label="Tổng cộng" width="width">
              </el-table-column>
            </el-table>
            <div class="flex-end">
              <div>Tổng cộng{{ prescriptionMain.prescriptionDetail.length }},Tổng cộng{{ getPrescriptionTotalCost(prescriptionMain.prescriptionDetail) }}VNĐ</div>
            </div>
          </div>
        </el-card>
      </div>
    </div>
    <el-dialog
      title="Thu phí"
      :visible.sync="chargeDialogVisible"
      width="50%"
      :close-on-click-modal="false"
      :before-close="dialogBeforeClose"
    >
      <div>
        <el-divider></el-divider>
        <div id="chargeTotalFee_Container">
          <div id="chargeTotalFee_Title">Số tiền phải thu（VNĐ）：</div>
          <div id="chargeTotalFee_Fee">{{ chargeTotalFee }}</div>
        </div>
        <el-form ref="chargeForm" :model="chargeForm" label-width="80px">
          <el-row>
            <el-col :span="5">
              <el-form-item label="Số tiền ưu đãi">
                <el-input
                  v-model="chargeForm.discountAmount"
                  placeholder="Số tiền ưu đãi"
                >
                  <template slot="append">VNĐ</template>
                </el-input>
              </el-form-item>
            </el-col>
            <el-col :span="5">
              <el-form-item label="Chiết khấu">
                <el-input v-model="chargeForm.discountRate" placeholder="Chiết khấu">
                  <template slot="append">折</template>
                </el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="10">
              <el-form-item label="Số tiền thực thu">
                <el-input v-model="model">
                  <template slot="append">VNĐ</template>
                </el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="12">
              <el-form-item label="Phương thức thanh toán">
                <el-radio-group v-model="model">
                  <el-radio label="1">Tiền mặt</el-radio>
                  <el-radio label="2">支付宝</el-radio>
                  <el-radio label="3">WeChat</el-radio>
                  <el-radio label="3">银行卡</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="15">
              <el-form-item label="Ghi chú">
                <el-input v-model="model"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
        <el-divider></el-divider>
      </div>
      <div slot="footer">
        <el-button @click="chargeDialogVisible = false">Hủy</el-button>
        <el-button type="primary" @click="chargeDialogVisible = false"
          >Xác nhận thu phí</el-button
        >
      </div>
    </el-dialog>
    <el-dialog
      title="退费明细"
      :visible.sync="refundDialogVisible"
      :close-on-click-modal="false"
      width="50%"
    >
      <div>
        <el-divider></el-divider>
          <el-card
          class="prescriptionCard"
          v-for="(refundPrescriptionMain, index) in refundPrescriptionMainList"
          :key="index"
        >
          <div v-if="refundPrescriptionMain.prescriptionType === 1">
            <el-table :data="refundPrescriptionMain.prescriptionDetail" style="width: 100%">
              <el-table-column align="left" width="55px">
                <template slot="header"  slot-scope="scope">
                  <el-checkbox v-model="refundPrescriptionMain.isSelect"></el-checkbox>
                </template>
              </el-table-column>
              <el-table-column prop="goodsName" label="Đơn thuốc tây" width="width">
              </el-table-column>
              <el-table-column prop="factoryName" label="Nhà sản xuất" width="width">
              </el-table-column>
              <el-table-column prop="price" label="Đơn giá" width="width">
              </el-table-column>
              <el-table-column prop="quantity" label="Số lượng" width="width">
              </el-table-column>
              <el-table-column prop="unit" label="Đơn vị" width="width">
              </el-table-column>
              <el-table-column prop="totalCost" label="Tổng tiền" width="width">
              </el-table-column>
            </el-table>
          </div>
          <!--Thuốc đông y-->
          <div v-else-if="refundPrescriptionMain.prescriptionType === 2">
            <el-table  :data="refundPrescriptionMain.prescriptionDetail" style="width: 100%">
              <el-table-column align="left"  width="55px">
                <template slot="header" slot-scope="scope">
                  <el-checkbox v-model="refundPrescriptionMain.isSelect"></el-checkbox>
                </template>
              </el-table-column>
              <el-table-column prop="goodsName" label="Đơn thuốc đông y" width="width">
              </el-table-column>
              <el-table-column prop="factoryName" label="Nhà sản xuất" width="width">
              </el-table-column>
              <el-table-column prop="tisanesMethod" label="Cách sắc thuốc" width="width">
              </el-table-column>
              <el-table-column prop="price" label="Đơn giá" width="width">
              </el-table-column>
              <el-table-column prop="quantity" label="Số lượng" width="width">
              </el-table-column>
              <el-table-column prop="totalQuantity" label="Tổng số lượng" width="width">
              </el-table-column>
              <el-table-column prop="totalCost" label="Tổng tiền" width="width">
              </el-table-column>
            </el-table>
          </div>
          <!--Phí phụ thêm-->
          <div v-if="refundPrescriptionMain.prescriptionType === 3">
            <el-table :data="refundPrescriptionMain.prescriptionDetail" style="width: 100%">
              <el-table-column align="left"  width="55px">
                <template slot="header"  slot-scope="scope">
                  <el-checkbox v-model="refundPrescriptionMain.isSelect"></el-checkbox>
                </template>
              </el-table-column>
              <el-table-column prop="goodsName" label="Phí phụ thêm" width="width">
              </el-table-column>
              <el-table-column prop="quantity" label="Số lượng" width="width">
              </el-table-column>
              <el-table-column prop="price" label="Đơn giá" width="width">
              </el-table-column>
              <el-table-column prop="totalCost" label="Tổng cộng" width="width">
              </el-table-column>
            </el-table>
          </div>
        </el-card>
        <el-divider></el-divider>
      </div>
      <div slot="footer" class="flex-space-between">
        <div>实退金额：{{getRefundTotalCost()}}</div>
        <div>
          <el-button @click="refundDialogVisible = false">Hủy</el-button>
          <el-button type="primary" @click="refundDialogVisible = false"
            >Xác nhận</el-button
          >
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  data() {
    let cardIdNo = (rule, value, callback) => {
      if (!value) {
        return callback(new Error("Số CMND/CCCD không được để trống"));
      } else {
        let reg = /((^\d{9}$)|(^\d{12}$)|(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$))/;
        if (!reg.test(value)) {
          callback(new Error("Vui lòng nhập đúng số CMND/CCCD"));
        } else {
          callback();
        }
      }
    };
    return {
      isRetail: true, //Có bán lẻ không
      selectCondition: "", //Điều kiện truy vấn bệnh nhân
      chargeStatusActiveName: "1", //Tab trạng thái thu phí
      chargeDialogVisible: false,
      chargeTotalFee: 100,
      chargeForm: {
        discountAmount: 0,
        discountRate: 0,
      },
      refundDialogVisible: false,
      patientList: [
        {
          name: "邓诗诗",
          clinicDate: "2022-06-13 12:23:34",
          chargeStatus: "Đã thu phí",
        },
      ], //病人列表
      patientTotalCount: 0, //病人总数
      patientPageSize: 15, //Số bệnh nhân hiển thị mỗi trang
      patientCurrentPage: 1, //病人当前页
      patientInfoForm: {
        name: "",
        gender: "",
        age_year: "",
        age_month: "",
        telephone: "",
        idCardNo: "",
      },
      patientInfoNoRules: null,
      patientInfoRules: {
        name: [
          { required: true, message: "Vui lòng nhập họ tên", trigger: "blur" },
          { min: 1, max: 50, message: "Độ dài trong khoảng1đến50ký tự", trigger: "blur" },
        ],
        gender: [{ required: true, message: "Vui lòng chọn giới tính", trigger: "blur" }],
        age_year: [
          { required: true, message: "Vui lòng nhập tuổi", trigger: "blur" },
        ],
        age_month: [
          { required: true, message: "Vui lòng nhập tuổi (tháng)", trigger: "blur" },
        ],
        telephone: [
          { required: true, message: "Vui lòng nhập số điện thoại liên hệ", trigger: "blur" },
          { min: 1, max: 20, message: "Độ dài trong khoảng1-20ký tự", trigger: "blur" },
        ],
        idCardNo: [
          {
            required: true,
            trigger: "blur",
            validator: cardIdNo,
          },
        ],
      },
      genderList: [
        {
          id: 1,
          name: "Nam",
        },
        {
          id: 1,
          name: "Nữ",
        },
      ],
      /*Danh sách đơn thuốc chờ thu phí*/
      prescriptionMainList:[{
        prescriptionType:1,
        isSelect:true,
        totalCost:50,
        prescriptionDetail:[{
          goodsName:"test",
          factoryName:"",
          price:10,
          quantity:5,
          unit:"",
          totalCost:50
        }]
      },{
        prescriptionType:2,
        isSelect:true,
        totalCost:150,
        quantity:3,
        useMethod:"test",
        prescriptionDetail:[{
          goodsName:"test2",
          factoryName:"",
          tisanesMethod:"",
          price:10,
          quantity:5,
          totalQuantity:15,
          totalCost:150
        }]
      },{
        prescriptionType:3,
        isSelect:true,
        totalCost:50,
        prescriptionDetail:[{
          goodsName:"test3",
          price:10,
          quantity:5,
          totalCost:50
        }]
      }],
      refundPrescriptionMainList:[{
        prescriptionType:1,
        isSelect:true,
        totalCost:50,
        prescriptionDetail:[{
          goodsName:"test",
          factoryName:"",
          price:10,
          quantity:5,
          unit:"",
          totalCost:50
        }]
      },{
        prescriptionType:2,
        isSelect:true,
        totalCost:150,
        prescriptionDetail:[{
          goodsName:"test2",
          factoryName:"",
          tisanesMethod:"",
          price:10,
          quantity:5,
          totalQuantity:15,
          totalCost:150
        }]
      },{
        prescriptionType:3,
        isSelect:true,
        totalCost:50,
        prescriptionDetail:[{
          goodsName:"test3",
          price:10,
          quantity:5,
          totalCost:50
        }]
      }]
    };
  },
  methods: {
    /*Nhấn nút thu phí/hoàn phí*/
    chargeClick() {
      if (this.chargeStatusActiveName === "1") {
        this.chargeDialogVisible = true;
      } else {
        this.refundDialogVisible = true;
      }
    },
    getPrescriptionTotalCost(data){
       let totalCost = 0;
       data.forEach(element => {
        if(element.totalCost){
          totalCost+=element.totalCost
        }
      });
      return totalCost;
    },
    /*Tính tổng đơn thuốc hoàn phí hiện tại*/
    getRefundTotalCost(){
      let totalCost = 0;
      this.refundPrescriptionMainList.forEach(element => {
        if(element.totalCost){
          totalCost+=element.totalCost
        }
      });
      return totalCost;
    },
    /*Chuyển sang bán lẻ*/
    changeToRetail() {
      this.isRetail = true;
      //Xóa bệnh nhân
      patientInfoForm = {
        name: "",
        gender: "",
        age_year: "",
        age_month: "",
        telephone: "",
        idCardNo: "",
      };
    },
    /*Nhấn vào danh sách bệnh nhân*/
    selectPatientChange(row) {
      this.isRetail = false;
      this.$nextTick(() => {
        this.$refs["patientInfoForm"].clearValidate();
      });
    },
    /*Truy vấn bệnh nhân*/
    selectPatientList() {},
    clinicDateFormatter(row) {
      const date = new Date(row.clinicDate);
      return (
        date.getMonth() +
        1 +
        "/" +
        date.getDate() +
        " " +
        date.getHours() +
        ":" +
        date.getMinutes()
      );
    },

  },
};
</script>


<style scoped>
.charge_container {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: space-around;
}

.charge_left {
  width: 30%;
  height: 100%;
}

.charge_right {
  height: 100%;
  width: 67%;
}

.patientinfo_form {
  background: #f2f2f2;
  border-radius: 10px;
  margin: 10px 0px 10px 0px;
  padding: 10px 10px 0px 10px;
}

.first_column {
  font: bold black;
}

#chargeTotalFee_Container {
  display: flex;
  justify-content: flex-start;
  padding: 20px;
  margin: 10px;
  background-color: rgba(207, 227, 236, 0.803921568627451);
  border-radius: 7px;
}

#chargeTotalFee_Title {
  font-family: "Arial Normal", "Arial", sans-serif;
  font-weight: 400;
  font-style: normal;
  font-size: 21px;
  margin-right: 8px;
}

#chargeTotalFee_Fee {
  font-weight: 800;
  font-style: black;
  font-size: 25px;
  margin-top: -4px;
}

.flex-space-between {
  display: flex;
  justify-content: space-between;
}

.flex-start{
  display:flex;
  justify-content: flex-start;
}

.flex-end{
  display:flex;
  justify-content: flex-end;
}

.el-divider--horizontal{
  margin:5px 0;
}

.prescriptionCard{
  margin:5px 0;
}

.goods-back{
  position: absolute;
  top:20px;
  right: 200px;
  width: 60px;
  height: 25px;
  text-align: center;
  vertical-align: center;
  color:red;
  border: 1px solid red;
  border-radius: 45%;
  cursor: none;
  -webkit-transform: rotate(-45deg);
  float: right;
  z-index: 1;
}
</style>