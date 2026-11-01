<template>
  <el-card class="box-card" shadow="always">
    <div class="card-header">
      <span class="cartitle">Lịch tái khám đến hạn</span>
      <div>
        <el-button style="padding: 3px 0" type="text" @click="openCreateDialog">Tạo nhắc lịch</el-button>
        <el-button style="padding: 3px 0; margin-left: 10px" type="text" @click="runJob">Chạy nhắc hôm nay</el-button>
        <el-badge :value="dueTotal < 10 ? dueTotal : '10+'" class="badgeitem" v-show="dueTotal > 0" />
      </div>
    </div>
    <div v-if="dueList.length === 0" class="text" style="color: #909399;">
      Không có lịch tái khám đến hạn
    </div>
    <div v-for="(item, index) in dueList" :key="item.id" class="text">
      <span class="cara1">{{ index + 1 }}. {{ item.patientName }} / {{ item.followupType }} / Hạn {{ formatDate(item.nextDate) }}</span>
      <span class="cara2">
        <el-tag v-if="item.daysOverdue > 0" type="danger" size="mini">Quá hạn {{ item.daysOverdue }} ngày</el-tag>
        <el-tag v-else type="warning" size="mini">Đến hạn hôm nay</el-tag>
        <a style="margin-left: 5px;" @click="acknowledge(item)">Xác nhận</a>
      </span>
    </div>
    <div v-if="dueTotal > dueList.length" class="text" style="color: #909399;">
      và {{ dueTotal - dueList.length }} mục khác
    </div>
    <el-dialog title="Tạo nhắc lịch tái khám" :visible.sync="dialogVisible" width="480px" append-to-body>
      <el-form :model="form" label-width="110px">
        <el-form-item label="Bệnh nhân">
          <el-select v-model="form.patientId" filterable placeholder="Chọn bệnh nhân" style="width: 100%;" @change="onPatientChange">
            <el-option v-for="p in patientOptions" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="Loại tái khám">
          <el-select v-model="form.followupType" placeholder="Chọn loại" style="width: 100%;">
            <el-option v-for="t in typeOptions" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="Ngày hẹn">
          <el-date-picker v-model="form.nextDate" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" placeholder="Chọn ngày hẹn" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="Ghi chú">
          <el-input v-model="form.remarks" placeholder="Ghi chú (không bắt buộc)" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">Đóng</el-button>
        <el-button type="primary" @click="submitForm">Lưu</el-button>
      </span>
    </el-dialog>
  </el-card>
</template>

<script>
import moment from 'moment'
import { listDueReminders, acknowledgeReminder, runDailyReminderJob, saveReminder } from '@/api/followup/reminder'
import { listPatientAll } from '@/api/outpatient/patient'

export default {
  name: 'FollowupReminderCard',
  data() {
    return {
      dueList: [],
      dueTotal: 0,
      dialogVisible: false,
      patientOptions: [],
      typeOptions: ['Tái khám định kỳ', 'Tiêm chủng nhắc lại', 'Xét nghiệm lại', 'Khám chuyên khoa', 'Khác'],
      form: {
        patientId: '',
        patientName: '',
        followupType: '',
        nextDate: '',
        remarks: ''
      }
    }
  },
  created() {
    this.loadDue()
    this.loadPatients()
  },
  methods: {
    formatDate(value) {
      if (!value) {
        return ''
      }
      return moment(value).format('DD/MM/YYYY')
    },
    loadDue() {
      listDueReminders({ offset: 0, limit: 10 }).then((responseData) => {
        if (responseData.code === 100) {
          this.dueList = responseData.data.rows || []
          this.dueTotal = responseData.data.total || 0
        }
      }).catch(() => {
        this.dueList = []
        this.dueTotal = 0
      })
    },
    loadPatients() {
      listPatientAll({ params: [] }).then((responseData) => {
        if (responseData.code === 100) {
          this.patientOptions = responseData.data || []
        }
      }).catch(() => {
        this.patientOptions = []
      })
    },
    onPatientChange(patientId) {
      const found = this.patientOptions.find((p) => String(p.id) === String(patientId))
      this.form.patientName = found ? found.name : ''
    },
    openCreateDialog() {
      this.form = {
        patientId: '',
        patientName: '',
        followupType: '',
        nextDate: '',
        remarks: ''
      }
      this.dialogVisible = true
    },
    submitForm() {
      if (!this.form.patientId || !this.form.patientName || !this.form.followupType || !this.form.nextDate) {
        this.$message.warning('Vui lòng chọn bệnh nhân, loại tái khám và ngày hẹn')
        return
      }
      saveReminder(this.form).then((responseData) => {
        if (responseData.code === 100) {
          this.$message.success('Đã tạo nhắc lịch tái khám')
          this.dialogVisible = false
          this.loadDue()
        }
      })
    },
    acknowledge(item) {
      acknowledgeReminder(item.id).then((responseData) => {
        if (responseData.code === 100) {
          if (responseData.data === 0) {
            this.$message.info('Mục này đã được xử lý trước đó')
          } else {
            this.$message.success('Đã xác nhận nhắc lịch')
          }
          this.loadDue()
        }
      })
    },
    runJob() {
      runDailyReminderJob().then((responseData) => {
        if (responseData.code === 100) {
          this.$message.success('Đã tạo ' + responseData.data + ' thông báo nội bộ')
          this.loadDue()
        }
      })
    }
  }
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.cartitle {
  font-weight: bold;
}
.text {
  margin-bottom: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.cara1 {
  flex: 1;
  margin-right: 8px;
}
.cara2 {
  white-space: nowrap;
}
.badgeitem {
  margin-left: 8px;
}
</style>
