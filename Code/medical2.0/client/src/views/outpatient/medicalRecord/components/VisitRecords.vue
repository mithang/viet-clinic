<template>
  <div class="visitRecords">
    <ul class="patientInfo">
      <li>
        Tên bệnh nhân：{{ NowVisitInfo.info.name }}（{{ NowVisitInfo.info.age }}tuổi）
      </li>
      <li>Giới tính：{{ NowVisitInfo.info.gender.name }}</li>
      <li>Thời gian khám：{{ NowVisitInfo.medicalRecord.diagnoseDate }}</li>
      <li>Bác sĩ khoa：{{ NowVisitInfo.medicalRecord.createBy }}</li>
    </ul>
    <div class="titleBox">
      <h4><el-checkbox v-model="checkedForm.record">Bệnh án</el-checkbox></h4>
      <el-button class="titleBtn">{{ NowVisitInfo.info.treatType }}</el-button>
    </div>
    <ul class="infoItem">
      <li
        v-show="item.value"
        v-for="(item, index) in AllMedicineRecordLabelList"
        :key="index"
      >
        <p class="infoName">【{{ item.label }}】</p>
        <p class="itemContent">{{ item.value }}</p>
      </li>
    </ul>
    <div class="titleBox" v-if="NowVisitInfo.recipelInfoEvtList.length">
      <h4>
        <el-checkbox v-model="checkedForm.Prescription">Đơn thuốc</el-checkbox>
      </h4>
    </div>
    <ul class="translateBox" v-if="NowVisitInfo.recipelInfoEvtList.length">
      <li v-if="WesternMedicineModel.recipelDetailEvtList.length">
        <p>【Đơn thuốc tây】</p>
        <el-table
          :data="WesternMedicineModel.recipelDetailEvtList"
          border
          style="width: 100%"
        >
          <el-table-column type="index" label="Số thứ tự" width="50" align="center">
          </el-table-column>
          <el-table-column prop="drugStuffId.name" label="Tên" align="center">
          </el-table-column>
          <el-table-column prop="singleDosage" label="Liều dùng mỗi lần" width="100">
          </el-table-column>
          <el-table-column
            prop="westernMedicineUse.name"
            label="Cách dùng"
            width="100"
          >
          </el-table-column>
          <el-table-column prop="frequency.name" label="Tần suất" width="120">
          </el-table-column>
          <el-table-column prop="days.name" label="Số ngày" width="100">
          </el-table-column>
          <el-table-column prop="total" label="Tổng số lượng" width="100">
          </el-table-column>
          <el-table-column
            prop="drugStuffId.price"
            label="Đơn giá"
            width="100"
            align="center"
          >
          </el-table-column>
          <el-table-column
            prop="allFee"
            label="Tổng cộng"
            width="100"
            align="center"
          >
          </el-table-column>
        </el-table>
        <p class="userTypeBox">
          {{ WesternMedicineModel.recipelInfo.remarks }}
        </p>
      </li>
      <li v-if="ChineseMedicineModel.recipelDetailEvtList.length">
        <p>【Đơn thuốc đông y】</p>
        <ul class="medicineBox">
          <li
            v-for="(item, index) in ChineseMedicineModel.recipelDetailEvtList"
            :key="index"
          >
            {{ item.drugStuffId.name }}&nbsp;&nbsp;{{ item.singleDosage
            }}{{ item.drugStuffId.dosisUnit.name }}
          </li>
        </ul>
        <p class="userTypeBox">
          {{
            ChineseMedicineModel.recipelInfo.dosage
          }}thang&nbsp;&nbsp;&nbsp;&nbsp;{{
            ChineseMedicineModel.recipelInfo.recipelUse.name
          }}&nbsp;&nbsp;&nbsp;&nbsp;{{
            ChineseMedicineModel.recipelInfo.frequency.name
          }}&nbsp;&nbsp;&nbsp;&nbsp;{{
            ChineseMedicineModel.recipelInfo.takeFrequency.name
          }}
        </p>
      </li>
      <li v-if="InfusionItemList.length">
        <p>【Đơn truyền dịch】</p>
        <div v-for="(item, index) in InfusionItemList" :key="index">
          <InfusionPrescription
            :InfusionUseOption="InfusionUseOption"
            :InfusionOption="InfusionOption"
            :DayNumOption="DayNumOption"
            :FrequencyOption="FrequencyOption"
            :InfoModel="item"
            :IsOnlyRead="IsOnlyRead"
            :GroupNumber="index + 1"
            :key="index"
          />
        </div>
        <p class="userTypeBox">{{ InfusionModel.remarks }}</p>
      </li>
      <li v-if="CostItemList.length">
        <p>【Hạng mục khám chữa bệnh】</p>
        <el-table
          :data="CostItemList"
          border
          style="width: 100%"
          class="tableStyle"
        >
          <el-table-column type="index" label="Số thứ tự" width="50" align="center">
          </el-table-column>
          <el-table-column prop="drugStuffId.name" label="Tên hạng mục">
          </el-table-column>
          <el-table-column prop="total" label="Số lượng" width="100">
          </el-table-column>
          <el-table-column prop="drugStuffId.price" label="Đơn giá" width="80">
          </el-table-column>
          <el-table-column
            prop="allFee"
            label="Tổng cộng"
            width="100"
            align="center"
          >
          </el-table-column>
        </el-table>
      </li>
      <li v-if="SurchargeList.length">
        <p>【Phí phụ thêm】</p>
        <el-table
          :data="SurchargeList"
          border
          style="width: 100%"
          class="tableStyle"
        >
          <el-table-column type="index" label="Số thứ tự" width="50" align="center">
          </el-table-column>
          <el-table-column prop="drugStuffId.name" label="Tên chi phí">
          </el-table-column>
          <el-table-column prop="total" label="Số lượng" width="100">
          </el-table-column>
          <el-table-column prop="drugStuffId.price" label="Đơn giá" width="80">
          </el-table-column>
          <el-table-column
            prop="allFee"
            label="Tổng cộng"
            width="100"
            align="center"
          >
          </el-table-column>
        </el-table>
      </li>
    </ul>
  </div>
</template>

<script>
import InfusionPrescription from "./InfusionPrescription.vue";
export default {
  components: { InfusionPrescription },
  props: {
    NowVisitInfo: {
      type: Object,
    },
    InfusionUseOption: {
      type: Object,
    },
    InfusionOption: {
      type: Object,
    },
    DayNumOption: {
      type: Object,
    },
    FrequencyOption: {
      type: Object,
    },
    checkedForm: {
      type: Object,
    },
  },
  data() {
    return {
      IsOnlyRead: true,
      //全部病例配置
      AllMedicineRecordLabelList: [
        {
          label: "Lý do khám chính",
          name: "patientTell",
          value: "",
        },
        {
          label: "Bệnh sử hiện tại",
          name: "nowHistory",
          value: "",
        },
        {
          label: "Tiền sử bệnh",
          name: "beforeHistory",
          value: "",
        },
        {
          label: "Xét nghiệm khác",
          name: "otherCheck",
          value: "",
        },
        {
          label: "Chẩn đoán",
          name: "diagnose",
          value: "",
        },
        {
          label: "Tiền sử cá nhân",
          name: "personalHistory",
          value: "",
        },
        {
          label: "Tiền sử dị ứng",
          name: "allergyHistory",
          value: "",
        },
        {
          label: "Tiền sử bệnh",
          name: "diseaseHistory",
          value: "",
        },
        {
          label: "Tiền sử bệnh truyền nhiễm",
          name: "infectiousHistory",
          value: "",
        },
        {
          label: "Tiền sử phẫu thuật",
          name: "surgeryHistory",
          value: "",
        },
        {
          label: "Tiền sử truyền máu",
          name: "transfusionHistory",
          value: "",
        },
        {
          label: "Khám thực thể",
          name: "physiqueCheck",
          value: "",
        },
        {
          label: "Chẩn đoán cấp cứu",
          name: "emergencyDiagnose",
          value: "",
        },
        {
          label: "Hiệu quả cấp cứu",
          name: "emergencyEffect",
          value: "",
        },
        {
          label: "Tiền sử gia đình",
          name: "familyHistory",
          value: "",
        },
        {
          label: "Tiền sử kinh nguyệt",
          name: "lunariaHistory",
          value: "",
        },
        {
          label: "Tiền sử hôn nhân/sinh đẻ",
          name: "pregnancyHistory",
          value: "",
        },
        {
          label: "Xét nghiệm bổ sung",
          name: "assistCheck",
          value: "",
        },
      ],
      WesternMedicineModel: {
        recipelInfo: {},
        recipelDetailEvtList: [],
      },
      ChineseMedicineModel: {
        recipelInfo: {
          recipelUse: {},
          frequency: {},
          takeFrequency: {},
        },
        recipelDetailEvtList: [],
      },
      InfusionItemList: [],
      InfusionModel: {},
      CostItemList: [],
      SurchargeList: [],
    };
  },
  mounted() {
    this.InitData();
  },
  methods: {
    InitData() {
      this.WesternMedicineModel = {
        recipelInfo: {},
        recipelDetailEvtList: [],
      };
      this.ChineseMedicineModel = {
        recipelInfo: {
          recipelUse: {},
          frequency: {},
          takeFrequency: {},
        },
        recipelDetailEvtList: [],
      };
      this.InfusionItemList = [];
      this.InfusionModel = {};
      this.CostItemList = [];
      this.SurchargeList = [];
      this.AllMedicineRecordLabelList.map((item) => {
        item.value = this.NowVisitInfo.medicalRecord[`${item.name}`];
      });
      this.NowVisitInfo.recipelInfoEvtList.map((prescriptionItem) => {
        switch (prescriptionItem.recipelInfo.recipelType.name) {
          case "Đơn thuốc tây":
            this.WesternMedicineModel = prescriptionItem;
            break;
          case "Đơn thuốc đông y":
            this.ChineseMedicineModel = prescriptionItem;
            break;
          case "Đơn truyền dịch":
            this.InfusionModel = prescriptionItem.recipelInfo;
            this.InfusionItemList = [];
            let GroupArr = [];
            prescriptionItem.recipelDetailEvtList.map((item) => {
              GroupArr.push(item.infuseGroup);
            });
            GroupArr = [...new Set(GroupArr)].sort();
            GroupArr.map((group) => {
              let InfoModel = {
                infuseUse: {},
                frequency: {},
                days: {},
                drippingSpeed: "",
                infuseGroup: group,
                infusionList: [],
              };
              this.InfusionItemList.push(JSON.parse(JSON.stringify(InfoModel)));
              prescriptionItem.recipelDetailEvtList.map((item) => {
                if (group === item.infuseGroup) {
                  this.InfusionItemList[group - 1].infuseUse = item.infuseUse;
                  this.InfusionItemList[group - 1].frequency = item.frequency;
                  this.InfusionItemList[group - 1].days = item.days;
                  this.InfusionItemList[group - 1].drippingSpeed =
                    item.drippingSpeed;
                  this.InfusionItemList[group - 1].infuseGroup = item.group;
                  this.InfusionItemList[group - 1].infusionList.push(item);
                }
              });
            });
            break;
          case "Hạng mục khám chữa bệnh":
            this.CostItemList = prescriptionItem.recipelDetailEvtList;
            break;
          case "Phí phụ thêm":
            this.SurchargeList = prescriptionItem.recipelDetailEvtList;
            break;
          default:
            break;
        }
      });
    },
  },
};
</script>

<style lang='scss' scoped>
$borderColor: rgb(219, 219, 219); //Gạch chân viền xám
$fontAndBgColor: #169bd5; //Nút và chữ màu xanh dương
.visitRecords {
  padding: 0 10px;
  max-height: 60vh;
  .patientInfo {
    width: 100%;
    height: 40px;
    border: 1px solid $borderColor;
    line-height: 40px;
    padding: 0 4px;
    border-radius: 2px;
    list-style: none;
    display: flex;
    justify-content: space-between;
  }
  .titleBox {
    width: 100%;
    height: 30px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    h4 {
      margin: 0;
      color: $fontAndBgColor;
    }
    .titleBtn {
      background-color: $fontAndBgColor;
      color: white;
    }
  }
  .infoItem {
    width: 100%;
    padding: 4px;
    list-style: none;
    border: 1px solid $borderColor;
    li {
      display: flex;
      justify-content: space-between;
      p {
        margin: 0;
      }
      .infoName {
        width: 90px;
        font-weight: bold;
      }
      .itemContent {
        flex: 1;
      }
    }
  }
  .translateBox {
    width: 100%;
    padding: 4px;
    list-style: none;
    border: 1px solid $borderColor;
    li {
      margin-bottom: 20px;
      p {
        margin: 0;
        font-weight: bold;
      }
      .medicineBox {
        width: 100%;
        padding: 4px;
        list-style: none;
        display: flex;
        margin-bottom: 10px;
        li {
          margin-right: 30px;
        }
      }
      .userTypeBox {
        padding-left: 6px;
        color: $fontAndBgColor;
      }
    }
  }
}
</style>