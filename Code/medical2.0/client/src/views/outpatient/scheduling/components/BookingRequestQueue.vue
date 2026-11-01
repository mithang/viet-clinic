<template>
  <div>
    <el-row style="margin-bottom: 12px;">
      <el-button type="primary" @click="openCreateDialog">Ghi nhận yêu cầu mới</el-button>
      <el-button @click="loadPending">Tải lại hàng chờ</el-button>
      <span style="margin-left: 12px; color: #909399;">
        Yêu cầu pending không ghi dữ liệu khám chữa; xác nhận sẽ tạo lịch hẹn thật
        (phiếu đăng ký với ngày hẹn/dịch vụ), từ chối giữ lại lý do.
      </span>
    </el-row>
    <el-table :data="pendingList" border style="width: 100%;" :empty-text="emptyText">
      <el-table-column prop="patientName" label="Người đặt lịch" width="180" />
      <el-table-column prop="phone" label="Số điện thoại" width="150" />
      <el-table-column label="Ngày muốn khám" width="150">
        <template slot-scope="scope">
          {{ formatDate(scope.row.wantedDate) }}
        </template>
      </el-table-column>
      <el-table-column prop="service" label="Dịch vụ muốn khám" />
      <el-table-column label="Thao tác" width="300">
        <template slot-scope="scope">
          <el-button size="mini" type="primary" @click="confirmItem(scope.row)">Xác nhận thành lịch hẹn</el-button>
          <el-button size="mini" @click="openPriorDialog(scope.row)">Yêu cầu trước đó</el-button>
          <el-button size="mini" type="danger" @click="openRejectDialog(scope.row)">Từ chối</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div v-if="pendingTotal > pendingList.length" style="margin-top: 8px; color: #909399;">
      và {{ pendingTotal - pendingList.length }} yêu cầu khác
    </div>

    <el-dialog title="Ghi nhận yêu cầu đặt lịch" :visible.sync="createDialogVisible" width="480px" append-to-body>
      <el-form :model="form" label-width="130px">
        <el-form-item label="Tên người đặt lịch">
          <el-input v-model="form.patientName" placeholder="Họ tên (không cần tài khoản)" />
        </el-form-item>
        <el-form-item label="Số điện thoại">
          <el-input v-model="form.phone" placeholder="VD: 0901234567" />
        </el-form-item>
        <el-form-item label="Ngày muốn khám">
          <el-date-picker v-model="form.wantedDate" type="date" value-format="yyyy-MM-dd" placeholder="Chọn ngày" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="Dịch vụ muốn khám">
          <el-input v-model="form.service" placeholder="VD: Khám tổng quát (không ghi chẩn đoán)" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="createDialogVisible = false">Đóng</el-button>
        <el-button type="primary" @click="submitForm">Gửi yêu cầu</el-button>
      </span>
    </el-dialog>

    <el-dialog title="Từ chối yêu cầu" :visible.sync="rejectDialogVisible" width="480px" append-to-body>
      <el-form label-width="130px">
        <el-form-item label="Lý do từ chối">
          <el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="VD: Ngày này đã kín lịch trực" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="rejectDialogVisible = false">Đóng</el-button>
        <el-button type="danger" @click="submitReject">Xác nhận từ chối</el-button>
      </span>
    </el-dialog>

    <el-dialog title="Các yêu cầu trước đó của số điện thoại này" :visible.sync="priorDialogVisible" width="720px" append-to-body>
      <el-table :data="priorList" border style="width: 100%;" empty-text="Không có yêu cầu trước đó">
        <el-table-column label="Ngày muốn khám" width="150">
          <template slot-scope="scope">
            {{ formatDate(scope.row.wantedDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="patientName" label="Người đặt lịch" width="160" />
        <el-table-column prop="service" label="Dịch vụ" />
        <el-table-column label="Trạng thái" width="140">
          <template slot-scope="scope">
            {{ statusLabel(scope.row.status) }}
          </template>
        </el-table-column>
        <el-table-column prop="rejectReason" label="Lý do từ chối" width="200" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script>
import moment from 'moment'
import {
  submitBookingRequest,
  listPendingBookingRequests,
  listPriorBookingRequests,
  confirmBookingRequest,
  rejectBookingRequest
} from '@/api/booking/request'

export default {
  name: 'BookingRequestQueue',
  data() {
    return {
      pendingList: [],
      pendingTotal: 0,
      emptyText: 'Không có yêu cầu đặt lịch đang chờ',
      createDialogVisible: false,
      rejectDialogVisible: false,
      priorDialogVisible: false,
      form: {
        patientName: '',
        phone: '',
        wantedDate: '',
        service: ''
      },
      selected: null,
      rejectReason: '',
      priorList: []
    }
  },
  created() {
    this.loadPending()
  },
  methods: {
    formatDate(value) {
      if (!value) {
        return ''
      }
      return moment(value).format('DD/MM/YYYY')
    },
    statusLabel(status) {
      if (status === '1') {
        return 'Đã xác nhận'
      }
      if (status === '2') {
        return 'Đã từ chối'
      }
      return 'Chờ xử lý'
    },
    loadPending() {
      listPendingBookingRequests({ offset: 0, limit: 10 }).then((responseData) => {
        if (responseData.code === 100) {
          this.pendingList = responseData.data.rows || []
          this.pendingTotal = responseData.data.total || 0
        }
      }).catch(() => {
        this.pendingList = []
        this.pendingTotal = 0
      })
    },
    openCreateDialog() {
      this.form = {
        patientName: '',
        phone: '',
        wantedDate: '',
        service: ''
      }
      this.createDialogVisible = true
    },
    submitForm() {
      if (!this.form.patientName || !this.form.phone || !this.form.wantedDate) {
        this.$message.warning('Vui lòng nhập tên, số điện thoại và ngày muốn khám')
        return
      }
      submitBookingRequest(this.form).then((responseData) => {
        if (responseData.code === 100) {
          if (responseData.data && responseData.data.duplicate) {
            this.$message.info('Số điện thoại này đã có yêu cầu chờ cho ngày đó')
          } else {
            this.$message.success('Đã ghi nhận yêu cầu đặt lịch')
          }
          this.createDialogVisible = false
          this.loadPending()
        }
      })
    },
    confirmItem(item) {
      confirmBookingRequest(item.id).then((responseData) => {
        if (responseData.code === 100) {
          this.$message.success('Đã tạo lịch hẹn, mã phiếu đăng ký: ' + responseData.data)
          this.loadPending()
        }
      }).catch(() => {
        this.loadPending()
      })
    },
    openRejectDialog(item) {
      this.selected = item
      this.rejectReason = ''
      this.rejectDialogVisible = true
    },
    submitReject() {
      if (!this.rejectReason) {
        this.$message.warning('Vui lòng nhập lý do từ chối')
        return
      }
      rejectBookingRequest(this.selected.id, this.rejectReason).then((responseData) => {
        if (responseData.code === 100) {
          this.$message.success('Đã từ chối yêu cầu, lý do đã được lưu')
          this.rejectDialogVisible = false
          this.loadPending()
        }
      })
    },
    openPriorDialog(item) {
      this.selected = item
      listPriorBookingRequests({ phone: item.phone, offset: 0, limit: 50 }).then((responseData) => {
        if (responseData.code === 100) {
          this.priorList = responseData.data.rows || []
          this.priorDialogVisible = true
        }
      })
    }
  }
}
</script>
