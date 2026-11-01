<template>
  <div class="day-dashboard">
    <div class="day-dashboard-header">
      <span class="day-dashboard-title">Tổng quan ngày</span>
      <el-date-picker
        v-model="selectedDate"
        type="date"
        value-format="yyyy-MM-dd"
        placeholder="Chọn ngày"
        size="small"
      ></el-date-picker>
      <el-button type="primary" size="small" :loading="loading" @click="loadDashboard">Xem</el-button>
    </div>
    <div v-if="error" class="day-dashboard-error">{{ error }}</div>
    <div v-if="data" class="day-dashboard-body">
      <div class="day-dashboard-window">
        Ngày phòng khám: {{ data.date }} ({{ data.timezone }}) — cửa sổ giờ lưu trữ:
        {{ data.windowStart }} đến {{ data.windowEnd }}
      </div>
      <div class="day-dashboard-grid">
        <div class="day-dashboard-stat">
          <div class="stat-label">Lượt khám</div>
          <div class="stat-value">{{ data.visits.visitCount }}</div>
        </div>
        <div class="day-dashboard-stat">
          <div class="stat-label">Chờ thu phí</div>
          <div class="stat-value">{{ data.visits.pendingChargeCount }}</div>
        </div>
        <div class="day-dashboard-stat">
          <div class="stat-label">Số phiếu thu</div>
          <div class="stat-value">{{ data.charges.chargeCount }}</div>
        </div>
        <div class="day-dashboard-stat">
          <div class="stat-label">Phải thu</div>
          <div class="stat-value">{{ formatMoney(data.charges.totalReceivable) }}</div>
        </div>
        <div class="day-dashboard-stat">
          <div class="stat-label">Thực thu</div>
          <div class="stat-value">{{ formatMoney(data.charges.totalReceived) }}</div>
        </div>
        <div class="day-dashboard-stat">
          <div class="stat-label">Ưu đãi</div>
          <div class="stat-value">{{ formatMoney(data.charges.totalDiscounted) }}</div>
        </div>
        <div class="day-dashboard-stat">
          <div class="stat-label">Hoàn phí (số phiếu)</div>
          <div class="stat-value">{{ data.charges.refundCount }}</div>
        </div>
        <div class="day-dashboard-stat">
          <div class="stat-label">Hoàn phí (số tiền)</div>
          <div class="stat-value">{{ formatMoney(data.charges.refundTotal) }}</div>
        </div>
        <div class="day-dashboard-stat">
          <div class="stat-label">Tồn kho thấp (thuốc / vật tư)</div>
          <div class="stat-value">{{ showCount(data.stock.lowStockDrugCount) }} / {{ showCount(data.stock.lowStockStuffCount) }}</div>
        </div>
        <div class="day-dashboard-stat">
          <div class="stat-label">Sắp hết hạn (thuốc / vật tư)</div>
          <div class="stat-value">{{ showCount(data.stock.nearExpiryDrugCount) }} / {{ showCount(data.stock.nearExpiryStuffCount) }}</div>
        </div>
      </div>
      <div v-if="data.charges.byPaymentMethod && data.charges.byPaymentMethod.length" class="day-dashboard-methods">
        <div class="methods-title">Doanh thu theo phương thức</div>
        <el-table :data="data.charges.byPaymentMethod" size="small" border>
          <el-table-column prop="paymentMethod" label="Phương thức"></el-table-column>
          <el-table-column prop="count" label="Số phiếu"></el-table-column>
          <el-table-column prop="totalReceivable" label="Phải thu"></el-table-column>
          <el-table-column prop="totalReceived" label="Thực thu"></el-table-column>
          <el-table-column prop="totalDiscounted" label="Ưu đãi"></el-table-column>
        </el-table>
      </div>
      <div v-else class="day-dashboard-empty">Chưa có phiếu thu trong ngày này.</div>
      <div class="day-dashboard-note">
        Tồn kho thấp và sắp hết hạn là trạng thái hiện tại lúc xem, không phải số phát sinh trong ngày chọn.
        Hoàn phí trình bày riêng, không trừ ngầm vào tổng thu.
      </div>
    </div>
  </div>
</template>

<script>
import { getDayDashboard } from "@/api/dayDashboard";

// Ngày mặc định là ngày phòng khám Asia/Ho_Chi_Minh, không phải ngày
// theo múi giờ của trình duyệt (VM/browser có thể chạy ở UTC, khi đó
// new Date() sẽ lệch sang hôm trước vào buổi sáng giờ phòng khám).
function todayLocal() {
  try {
    return new Intl.DateTimeFormat("en-CA", {
      timeZone: "Asia/Ho_Chi_Minh",
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
    }).format(new Date());
  } catch (e) {
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, "0");
    const day = String(now.getDate()).padStart(2, "0");
    return `${year}-${month}-${day}`;
  }
}

export default {
  name: "DayDashboard",
  data() {
    return {
      selectedDate: todayLocal(),
      loading: false,
      error: "",
      data: null,
    };
  },
  created() {
    this.loadDashboard();
  },
  methods: {
    loadDashboard() {
      this.loading = true;
      this.error = "";
      getDayDashboard(this.selectedDate)
        .then((response) => {
          this.data = response.data || response;
        })
        .catch((err) => {
          this.data = null;
          this.error =
            (err && err.response && err.response.data && err.response.data.message) ||
            "Không tải được tổng quan ngày.";
        })
        .finally(() => {
          this.loading = false;
        });
    },
    formatMoney(value) {
      if (value === null || value === undefined) {
        return "—";
      }
      return Number(value).toLocaleString("vi-VN");
    },
    showCount(value) {
      if (value === null || value === undefined) {
        return "—";
      }
      return value;
    },
  },
};
</script>

<style scoped>
.day-dashboard {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(64, 158, 255, 0.12);
  padding: 20px 24px;
  margin: 20px auto 0;
  max-width: 900px;
  text-align: left;
}
.day-dashboard-header {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.day-dashboard-title {
  font-size: 20px;
  font-weight: bold;
  margin-right: 8px;
}
.day-dashboard-error {
  color: #f56c6c;
  margin-top: 12px;
}
.day-dashboard-window {
  margin-top: 12px;
  color: #606266;
  font-size: 13px;
}
.day-dashboard-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 12px;
  margin-top: 12px;
}
.day-dashboard-stat {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 12px;
}
.stat-label {
  color: #909399;
  font-size: 13px;
  margin-bottom: 6px;
}
.stat-value {
  font-size: 20px;
  font-weight: bold;
}
.day-dashboard-methods {
  margin-top: 16px;
}
.methods-title {
  font-weight: bold;
  margin-bottom: 8px;
}
.day-dashboard-empty {
  margin-top: 16px;
  color: #909399;
}
.day-dashboard-note {
  margin-top: 12px;
  color: #909399;
  font-size: 13px;
}
</style>
