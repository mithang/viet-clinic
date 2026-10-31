<template>
    <el-card class="index-module-card">
      <add-today-work ref='addTodayWorkForm' ></add-today-work>
      <!-- Tiêu đề -->
      <div slot="header" class="card-title-box">
        <span class="el-card-title">Lịch trình của tôi</span>
        <i v-show="!isPanelSetIcon" class="el-icon-close" style="float: right; padding: 3px 0" @click="deletePanelItem"></i>
        <span class="choice-month-btn">
          <span>{{headOptions.date }}{{'(' + headOptions.ncWeek + ')'}}</span>
        </span>
      </div>

      <div class="common">
        <el-radio-group v-model="dateType" @change="dateTypeChange">
          <el-radio-button label="1">tháng</el-radio-button>
          <el-radio-button label="2">tuần</el-radio-button>
        </el-radio-group>

        <!-- tháng -->
        <span  v-if="dateType === 1">
          <el-date-picker v-model="value" type="month" :clearable="false"></el-date-picker>
          <el-button-group>
            <el-button plain @click="handleMonth('on')" icon="el-icon-arrow-left">Tháng trước</el-button>
            <el-button plain @click="getToday">Hôm nay</el-button>
            <el-button plain @click="handleMonth('next')">Tháng sau<i class="el-icon-arrow-right el-icon--right"></i></el-button>
          </el-button-group>
        </span>
      
        <!-- tuần -->
        <span  v-else>
          <el-date-picker v-model="value" type="week" format="[Tuần] WW, yyyy" @change="weekChange" :clearable="false"></el-date-picker>
          <el-button-group>
            <el-button plain icon="el-icon-arrow-left" @click="handleOnWeek('on')">Tuần trước</el-button>
            <el-button plain @click="getToday">Hôm nay</el-button>
            <el-button plain @click="handleOnWeek('next')">Tuần sau<i class="el-icon-arrow-right el-icon--right"></i></el-button>
          </el-button-group>
        </span>
        <i class="el-icon-s-fold" @click="fadeShow"></i>
      </div>

      <el-container class='query-form-container'>
        <el-main>
          <!-- Lịch -->
          <el-calendar v-model="value" :range="range" :class="{'weeks': dateType === '1' ? false : true}">
            <template slot="dateCell" slot-scope="{ date, data }">
              <div @click="calendarOnClick(data)" :class="['date-content', data.isSelected ? 'is-selected' : '']">
                <div>
                  <span v-if="systemTime === data.day">Hôm nay</span>
                  <span v-else :class="[isWeek(date) ? 'restRed' : '' , 'isrest']">{{ getDay(date, data) }}</span>
                  <!-- <span v-if="isWeek(date)" class="rest">Nghỉ</span> -->
                  <span>{{ data.isSelected ? '✔️' : ''}}</span>
                  <div>{{solarDate2lunar(data.day)}}</div>
                  <div v-for="memoItem in dataList" :key="memoItem.data">
                    <div v-if="memoItem.date === data.day && memoItem.list && memoItem.list.length > 0" class="round">
                      <svg-icon iconClass="round"/>
                    </div>
                  </div>
                </div>
              </div>
            </template>
          </el-calendar>
        </el-main>
        <el-aside align="center" width="300px" :class="scheduleAnimation" v-show="show3">
          <div style="background-color: #f2f6fc; height: 94%; max-height: 94%;">
            <div style="padding: 10px 0px;">{{headOptions.date }}{{'(' + headOptions.ncWeek + ')'}}</div>
            <div class="block"><el-avatar shape="square" :size="100" >{{ headOptions.cDay }}</el-avatar></div>
            <div>{{headOptions.IMonthCn }}{{headOptions.IDayCn}}</div>
            <div class="schedule">
              <div class="schedule_Header common">
                <div>Lịch trình của tôi</div>
                <el-button circle icon="el-icon-plus" @click="schedule"></el-button>
              </div>
              <el-empty description="Chưa có lịch trình" v-if="popoverList.length <= 0"></el-empty>

              <div class="schedule_List" v-else>
                <div v-for="(item,indexs) in popoverList" :key="indexs">
                  <el-alert 
                    :title="item.name" 
                    :description="'Nội dung mô tả chi tiết'"
                    :type="item.type" 
                    :closable="item.closable" 
                    style="margin-bottom: 4px;width: 98%;">
                  </el-alert>
                </div>
              </div>

            </div>
          </div>
        </el-aside>
      </el-container>
    </el-card>
</template>

<script>
import { calendar } from '@/utils/calendar' // Chuyển đổi âm lịch - tiết khí
import addTodayWork from './addTodayWork'

export default {
  props: ['id', 'panelSetIcon'],
  data () {
    return {
      value: new Date(),
      range: null,
      dateType: '1', // Loại ngày
      headOptions: {},
      panelId: this.id,
      systemTime: '',
      show3: true,
      isPanelSetIcon: this.panelSetIcon,
      showAll: false,
      popoverList: [], // Nội dung hiển thị trong popover
      scheduleAnimation:'',
      dataList: [
        { date:'2022-03-09',
          list: [
            { closable: false, type: 'warning' ,name: 'Phòng 221 toà nhà 2 - Cảnh báo hợp đồng sắp hết hạn'},
            { closable: true, type: 'success' ,name: 'Phòng 221 toà nhà 2 - Cảnh báo hợp đồng sắp hết hạn'},
            { closable: false, type: 'error' ,name: 'Phòng 221 toà nhà 2 - Cảnh báo hợp đồng sắp hết hạn'},
            { closable: true, type: 'success' ,name: 'Cảnh báo hợp đồng sắp hết hạn-xxx'},
            { closable: false, type: 'info' ,name: 'Phòng 221 toà nhà 2 - Cảnh báo hợp đồng sắp hết hạn'},
            { closable: true, type: 'success' ,name: 'Phòng 221 toà nhà 2 - Cảnh báo hợp đồng sắp hết hạn'},
          ],
        },
        { date:'2022-03-14',
          list: [
            {closable: false, type: 'error', name: 'Việc cần làm'}
          ],
        }
      ],
    }
  },
  components: {
    addTodayWork
  },
  watch:{
    panelSetIcon:{
      deep: true,
      handler(){
        this.isPanelSetIcon = this.panelSetIcon;
      }
    },
    value: {
      handler(newVal,oldVal) {
        if (newVal !== oldVal) {
          this.headOptions = this.solarlunarDay(newVal)
        }
      }
    }
  },
  mounted () {
    this.systemTime = this.fun() // Ngày hiện tại định dạng yyyy-mm-dd
    this.headOptions = this.solarlunarDay(new Date())
    this.calendarOnClick(this.systemTime)
  },
  methods: {
    schedule() {
      this.$refs.addTodayWorkForm.openAddGroupDeviceDialog(this.systemTime)
    },
    fadeShow() {
      this.show3 = !this.show3
      if (this.show3) {
        this.scheduleAnimation = 'move_up'
      }
    },
    // Hiển thị âm lịch
    solarDate2lunar(solarDate) {
      const solar = solarDate.split('-')
      const lunar = calendar.solar2lunar(solar[0], solar[1], solar[2])
      return lunar.lunarFestival || lunar.festival || lunar.Term || lunar.IDayCn
    },
    // Lấy thông tin chi tiết của ngày hiện tại
    solarlunarDay(solarDate) {
      const timestamp = new Date(solarDate);
      const res = timestamp.toISOString().slice(0,10)
      const solar = res.split('-')
      const lunar = calendar.solar2lunar(solar[0], solar[1], solar[2])
      return lunar
    },
    // Xem thêm, lấy toàn bộ lịch trình của ngày hiện tại
    getAll() {
      this.popoverList = [
        { closable: true, type: 'success', name: 'Cảnh báo hợp đồng sắp hết hạn-xxx', },
        { closable: false, type: 'warning', name: 'LS/12/1210Chờ ghi số'},
        { closable: false, type: 'info', name: 'GM/14/1410Chờ ghi số'},
        { closable: true, type: 'success', name: 'Phòng 221 toà nhà 2 - Cảnh báo hợp đồng sắp hết hạn'},
        { closable: true, type: 'success', name: 'LS/12/1210Chờ ghi số'},
        { closable: false, type: 'error', name: 'LS/12/1210Chờ ghi số'},
      ]
    },
    // Sự kiện gửi khi xóa mục bảng
    deletePanelItem () {
      this.$emit('deletePanelItemEvent', this.panelId)
    },
    // Chọn một việc cần làm cụ thể
    getMemorandum () {
    },
    // Nhấn vào ô lịch
    calendarOnClick(data) {
      this.popoverList = []
      let calendardata;
      this.dataList.forEach(item => {
        calendardata = typeof (data) === 'object' ? data.day : data
        if (item.date === calendardata) {
          this.popoverList = item.list
          return
        } 
      })
    },
    // Chuyển đếntoday
    getToday() {
      if (this.dateType === '2') { // tuần
        let weeks = this.GetMonday(new Date())
        weeks = new Date(Date.parse(weeks))
        this.weekChange(weeks) // Chuyển đến tuần chứa ngày hiện tại
      } else {
        this.range = null
      }
      this.calendarOnClick(this.systemTime)
      this.value = new Date()
    },
    // Chuyển tháng trước / tháng sau
    handleMonth(type) {
      const month = this.getLastMonth(this.value, type)
      this.value = new Date(Date.parse(month))
    },
    getLastMonth(Time,type) {
      let year = Time.getFullYear(); // Năm hiện tại: bốn chữ số
      let month = Time.getMonth() + 1; // Tháng hiện tại: 0-11
      if (type === 'on' ) {// Nếu là 1, tháng trước chính là tháng 12 của năm trước
        if (month === 1) {
          year -= 1;
          month = 12;
        } else {
          month -= 1
        }
      } 
      if (type === 'next' ) {// Nếu là 12, tháng sau chính là tháng 1 của năm sau
        if (month === 12) {
          year += 1;
          month = 1;
        } else {
          month += 1;
        }
      }
      month = month < 10 ? ('0' + month) : month;   // Tháng nhỏ hơn 10 thì thêm số 0 phía trước
      let lastYearMonth = year + '-' + month;
      return lastYearMonth;
    },
    // Chuyển đổi tháng/tuần Hiển thị
    dateTypeChange(val) {
      if (val === '1') { // tháng
        this.range = null
      } else { // tuần
        const weeks = this.GetMonday(this.value)
        this.value = new Date(Date.parse(weeks))
        this.weekChange(this.value)
      }
    },
    // Chọn tuần
    weekChange(val) {
      let timeStamp = val.getTime(); // Chuyển thời gian chuẩn sang timestamp, đơn vị mili giây
      this.range = []
      this.range[0] = this.fun(timeStamp ); // Thời gian bắt đầu
      this.range[1] = this.fun(timeStamp + (24 * 60 * 60 * 1000) * 6) // Thời gian kết thúc
    },
    // Chuyển tuần trước / tuần sau
    handleOnWeek(val) {
      let weeks = this.GetMonday(this.value)
      weeks = new Date(weeks).getTime()
      if (val === 'on') { // Tuần trước
        weeks = this.fun(weeks - (24 * 60 * 60 * 1000) * 7)
      } else if (val === 'next'){ // Tuần sau
        weeks = this.fun(weeks + (24 * 60 * 60 * 1000) * 7)
      }
      this.value = new Date(Date.parse(weeks))
      this.weekChange(this.value)
    },
    // Lấy thứ Hai của tuần chứa ngày: phạm vi thời gian của điều khiển lịch, thời gian bắt đầu phải là thứ Hai, thời gian kết thúc phải là Chủ nhật
    GetMonday(dd) {
      const week = dd.getDay(); // Lấy thứ trong tuần của thời gian
      const minus = week ? week - 1 : 6;
      dd.setDate(dd.getDate() - minus); // Lấy ngày cách đây minus ngày
      const res = dd.toISOString().slice(0,10)
      return res
    },
    // Chuyển timestamp sang yyyy-mm-dd
    fun(unixtimestamp) {
      const timestamp = unixtimestamp ? new Date(unixtimestamp): new Date();
      const res = timestamp.toISOString().slice(0,10)
      return res
    },
    // Xử lý ngày
    getDay(date) {
      return date.getDate()
    },
    // Kiểm tra có phải thứ Bảy hoặc Chủ nhật
    isWeek(date) {
      return date.getDay() === 6 || date.getDay() === 0
    },
  }
}
</script>

<style lang="scss" scoped>
::v-deep .el-calendar__header { // Ẩn phần đầu của component
  display: none;
}
.index-module-card.el-card ::v-deep .el-card__header {
  padding: 10px 16px!important;
  .card-close, .card-setting {
    float: right;
    padding: 3px 0
  }
}
.el-card {
  height: 100%;
}
.choice-month-btn {
  display: inline;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}
.card-title-box {
  position: relative;
}
.el-card-title {
  font-weight: bold;
}
.weeks ::v-deep .el-calendar-table .el-calendar-day {
  height: 515px;
  padding: 0px !important;
}
::v-deep .el-calendar-table .el-calendar-day {
  padding: 0px !important;
}
::v-deep .el-calendar__header {
  padding: 0px 20px 8px 20px;
}
::v-deep .el-calendar-table thead th {
  padding: 5px 0px;
}
::v-deep .el-card__body {
  padding: 8px;
}
.date-content {
  height: 100%;
  text-align: center;
  font-size: 14px;
}
.restRed {
  color: rgb(250, 124, 77);
}
.isrest {
  font-size: 18px;
}
// .date-content .rest {
//   color: #fff;
//   border-radius: 50%;
//   background: rgb(250, 124, 77);
//   width: 20px;
//   height: 20px;
//   line-height: 20px;
//   display: inline-block;
//   font-size: 12px;
//   margin-left: 5px;
// }
.is-selected {
  color: #07519b;
}
.query-form-container {
  padding: 0px;
  margin: 0px;
  overflow: hidden;
  margin-top: 5px;
}
::v-deep .el-avatar {
  background: #d1d2d5;
  font-size: 36px ;
}
.round { 
  fill: #408ad3;
  font-size: 12px;
}
.el-icon-s-fold {
  font-size: 22px;
  cursor: pointer;
}
.schedule {
  .schedule_List {
    text-align:left;
    padding-left: 8px; 
    overflow: auto; 
    height: 300px;
    min-height: 320px;
  }
  .schedule_Header {
    margin: 5px 10px;
    padding: 10px 8px;
    font-size: 18px;
    background-color: #e4e7ed;
  }
}
.common {
  display: flex;
  justify-content: space-between;
  align-content: center;
}    

.move_up {
  animation: move_up .3s linear; 
  -webkit-animation: move_up .3s linear; 
}

@keyframes move_up {
  from {
    transform: translate(300px,0);
  }
  to {
    transform: translate(-100px -10px);
  }
}
</style>
