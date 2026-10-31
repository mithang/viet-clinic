<template>
  <div width='40%'>
    <el-tabs type="border-card">
      <el-tab-pane label="giây" v-if="shouldHide('second')">
        <CrontabSecond @update="updateCrontabValue" :check="checkNumber" ref="cronsecond" />
      </el-tab-pane>

      <el-tab-pane label="Phút" v-if="shouldHide('min')">
        <CrontabMin
          @update="updateCrontabValue"
          :check="checkNumber"
          :cron="crontabValueObj"
          ref="cronmin"
        />
      </el-tab-pane>

      <el-tab-pane label="giờ" v-if="shouldHide('hour')">
        <CrontabHour
          @update="updateCrontabValue"
          :check="checkNumber"
          :cron="crontabValueObj"
          ref="cronhour"
        />
      </el-tab-pane>

      <el-tab-pane label="ngày" v-if="shouldHide('day')">
        <CrontabDay
          @update="updateCrontabValue"
          :check="checkNumber"
          :cron="crontabValueObj"
          ref="cronday"
        />
      </el-tab-pane>

      <el-tab-pane label="tháng" v-if="shouldHide('month')">
        <CrontabMonth
          @update="updateCrontabValue"
          :check="checkNumber"
          :cron="crontabValueObj"
          ref="cronmonth"
        />
      </el-tab-pane>

      <el-tab-pane label="tuần" v-if="shouldHide('week')">
        <CrontabWeek
          @update="updateCrontabValue"
          :check="checkNumber"
          :cron="crontabValueObj"
          ref="cronweek"
        />
      </el-tab-pane>

      <el-tab-pane label="năm" v-if="shouldHide('year')">
        <CrontabYear
          @update="updateCrontabValue"
          :check="checkNumber"
          :cron="crontabValueObj"
          ref="cronyear"
        />
      </el-tab-pane>
    </el-tabs>

    <div class="popup-main">
      <div class="popup-result">
        <p class="title">Biểu thức thời gian</p>
        <table>
          <thead>
            <th v-for="item of tabTitles" width="40" :key="item">{{item}}</th>
            <th>Cron Biểu thức</th>
          </thead>
          <tbody>
            <td>
              <span>{{crontabValueObj.second}}</span>
            </td>
            <td>
              <span>{{crontabValueObj.min}}</span>
            </td>
            <td>
              <span>{{crontabValueObj.hour}}</span>
            </td>
            <td>
              <span>{{crontabValueObj.day}}</span>
            </td>
            <td>
              <span>{{crontabValueObj.month}}</span>
            </td>
            <td>
              <span>{{crontabValueObj.week}}</span>
            </td>
            <td>
              <span>{{crontabValueObj.year}}</span>
            </td>
            <td>
              <span>{{crontabValueString}}</span>
            </td>
          </tbody>
        </table>
      </div>
      <CrontabResult :ex="crontabValueString"></CrontabResult>

      <div class="pop_btn">
        <el-button size="small" type="primary" @click="submitFill">Xác nhận</el-button>
        <el-button size="small" type="warning" @click="clearCron">Đặt lại</el-button>
        <el-button size="small" @click="hidePopup">Hủy</el-button>
      </div>
    </div>
  </div>
</template>

<script>
import CrontabSecond from "./second.vue";
import CrontabMin from "./min.vue";
import CrontabHour from "./hour.vue";
import CrontabDay from "./day.vue";
import CrontabMonth from "./month.vue";
import CrontabWeek from "./week.vue";
import CrontabYear from "./year.vue";
import CrontabResult from "./result.vue";

export default {
  data() {
    return {
      tabTitles: ["giây", "Phút", "giờ", "ngày", "tháng", "tuần", "năm"],
      tabActive: 0,
      myindex: 0,
      crontabValueObj: {
        second: "*",
        min: "*",
        hour: "*",
        day: "*",
        month: "*",
        week: "?",
        year: "",
      },
    };
  },
  name: "vcrontab",
  props: ["expression", "hideComponent"],
  methods: {
    shouldHide(key) {
      if (this.hideComponent && this.hideComponent.includes(key)) return false;
      return true;
    },
    resolveExp() {
      // Phân tích ngược Biểu thức
      if (this.expression) {
        let arr = this.expression.split(" ");
        if (arr.length >= 6) {
          //6 vị trở lên là biểu thức hợp lệ
          let obj = {
            second: arr[0],
            min: arr[1],
            hour: arr[2],
            day: arr[3],
            month: arr[4],
            week: arr[5],
            year: arr[6] ? arr[6] : "",
          };
          this.crontabValueObj = {
            ...obj,
          };
          for (let i in obj) {
            if (obj[i]) this.changeRadio(i, obj[i]);
          }
        }
      } else {
        // Không có biểu thức truyền vào thì khôi phục
        this.clearCron();
      }
    },
    // Giá trị chuyển đổi tab
    tabCheck(index) {
      this.tabActive = index;
    },
    // Do thành phần con kích hoạt, thay đổi giá trị trường cấu thành biểu thức
    updateCrontabValue(name, value, from) {
      "updateCrontabValue", name, value, from;
      this.crontabValueObj[name] = value;
      if (from && from !== name) {
        this.changeRadio(name, value);
      }
    },
    // Gán giá trị vào thành phần
    changeRadio(name, value) {
      let arr = ["second", "min", "hour", "month"],
        refName = "cron" + name,
        insValue;

      if (!this.$refs[refName]) return;

      if (arr.includes(name)) {
        if (value === "*") {
          insValue = 1;
        } else if (value.indexOf("-") > -1) {
          let indexArr = value.split("-");
          isNaN(indexArr[0])
            ? (this.$refs[refName].cycle01 = 0)
            : (this.$refs[refName].cycle01 = indexArr[0]);
          this.$refs[refName].cycle02 = indexArr[1];
          insValue = 2;
        } else if (value.indexOf("/") > -1) {
          let indexArr = value.split("/");
          isNaN(indexArr[0])
            ? (this.$refs[refName].average01 = 0)
            : (this.$refs[refName].average01 = indexArr[0]);
          this.$refs[refName].average02 = indexArr[1];
          insValue = 3;
        } else {
          insValue = 4;
          this.$refs[refName].checkboxList = value.split(",");
        }
      } else if (name === "day") {
        if (value === "*") {
          insValue = 1;
        } else if (value === "?") {
          insValue = 2;
        } else if (value.indexOf("-") > -1) {
          let indexArr = value.split("-");
          isNaN(indexArr[0])
            ? (this.$refs[refName].cycle01 = 0)
            : (this.$refs[refName].cycle01 = indexArr[0]);
          this.$refs[refName].cycle02 = indexArr[1];
          insValue = 3;
        } else if (value.indexOf("/") > -1) {
          let indexArr = value.split("/");
          isNaN(indexArr[0])
            ? (this.$refs[refName].average01 = 0)
            : (this.$refs[refName].average01 = indexArr[0]);
          this.$refs[refName].average02 = indexArr[1];
          insValue = 4;
        } else if (value.indexOf("W") > -1) {
          let indexArr = value.split("W");
          isNaN(indexArr[0])
            ? (this.$refs[refName].workday = 0)
            : (this.$refs[refName].workday = indexArr[0]);
          insValue = 5;
        } else if (value === "L") {
          insValue = 6;
        } else {
          this.$refs[refName].checkboxList = value.split(",");
          insValue = 7;
        }
      } else if (name === "week") {
        if (value === "*") {
          insValue = 1;
        } else if (value === "?") {
          insValue = 2;
        } else if (value.indexOf("-") > -1) {
          let indexArr = value.split("-");
          isNaN(indexArr[0])
            ? (this.$refs[refName].cycle01 = 0)
            : (this.$refs[refName].cycle01 = indexArr[0]);
          this.$refs[refName].cycle02 = indexArr[1];
          insValue = 3;
        } else if (value.indexOf("#") > -1) {
          let indexArr = value.split("#");
          isNaN(indexArr[0])
            ? (this.$refs[refName].average01 = 1)
            : (this.$refs[refName].average01 = indexArr[0]);
          this.$refs[refName].average02 = indexArr[1];
          insValue = 4;
        } else if (value.indexOf("L") > -1) {
          let indexArr = value.split("L");
          isNaN(indexArr[0])
            ? (this.$refs[refName].weekday = 1)
            : (this.$refs[refName].weekday = indexArr[0]);
          insValue = 5;
        } else {
          this.$refs[refName].checkboxList = value.split(",");
          insValue = 7;
        }
      } else if (name === "year") {
        if (value === "") {
          insValue = 1;
        } else if (value === "*") {
          insValue = 2;
        } else if (value.indexOf("-") > -1) {
          insValue = 3;
        } else if (value.indexOf("/") > -1) {
          insValue = 4;
        } else {
          this.$refs[refName].checkboxList = value.split(",");
          insValue = 5;
        }
      }
      this.$refs[refName].radioValue = insValue;
    },
    // Thành phần con của tùy chọn form xác thực định dạng số (Truyền qua props)
    checkNumber(value, minLimit, maxLimit) {
      // Kiểm tra phải là số nguyên
      value = Math.floor(value);
      if (value < minLimit) {
        value = minLimit;
      } else if (value > maxLimit) {
        value = maxLimit;
      }
      return value;
    },
    // Ẩn popup
    hidePopup() {
      this.$emit("hide");
    },
    // Điền biểu thức
    submitFill() {
      this.$emit("fill", this.crontabValueString);
      this.hidePopup();
    },
    clearCron() {
      // Khôi phục tùy chọn đã chọn
      ("Sẵn sàng khôi phục");
      this.crontabValueObj = {
        second: "*",
        min: "*",
        hour: "*",
        day: "*",
        month: "*",
        week: "?",
        year: "",
      };
      for (let j in this.crontabValueObj) {
        this.changeRadio(j, this.crontabValueObj[j]);
      }
    },
  },
  computed: {
    crontabValueString: function() {
      let obj = this.crontabValueObj;
      let str =
        obj.second +
        " " +
        obj.min +
        " " +
        obj.hour +
        " " +
        obj.day +
        " " +
        obj.month +
        " " +
        obj.week +
        (obj.year === "" ? "" : " " + obj.year);
      return str;
    },
  },
  components: {
    CrontabSecond,
    CrontabMin,
    CrontabHour,
    CrontabDay,
    CrontabMonth,
    CrontabWeek,
    CrontabYear,
    CrontabResult,
  },
  watch: {
    expression: "resolveExp",
    hideComponent(value) {
      // Ẩn một số thành phần
    },
  },
  mounted: function() {
    this.resolveExp();
  },
};
</script>
<style scoped>
.pop_btn {
  text-align: center;
  margin-top: 20px;
}
.popup-main {
  position: relative;
  margin: 10px auto;
  background: #fff;
  border-radius: 5px;
  font-size: 12px;
  overflow: hidden;
}
.popup-title {
  overflow: hidden;
  line-height: 34px;
  padding-top: 6px;
  background: #f2f2f2;
}
.popup-result {
  box-sizing: border-box;
  line-height: 24px;
  margin: 25px auto;
  padding: 15px 10px 10px;
  border: 1px solid #ccc;
  position: relative;
}
.popup-result .title {
  position: absolute;
  top: -28px;
  left: 50%;
  width: 140px;
  font-size: 14px;
  margin-left: -70px;
  text-align: center;
  line-height: 30px;
  background: #fff;
}
.popup-result table {
  text-align: center;
  width: 100%;
  margin: 0 auto;
}
.popup-result table span {
  display: block;
  width: 100%;
  font-family: arial;
  line-height: 30px;
  height: 30px;
  white-space: nowrap;
  overflow: hidden;
  border: 1px solid #e8e8e8;
}
.popup-result-scroll {
  font-size: 12px;
  line-height: 24px;
  height: 10em;
  overflow-y: auto;
}
</style>
