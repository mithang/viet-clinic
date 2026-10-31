<template>
  <el-dialog :visible="dialogVisible" @update:visible="$emit('update:dialogVisible', $event)" title="Truy vấn đơn thuốc" width="50%">
    <el-form :model="formData" ref="prescriptionForm" label-width="120px">
      <el-form-item label="Số đơn thuốc">
        <el-input :value="formData.prescriptionCode" readonly></el-input>
      </el-form-item>
      <el-form-item label="Bác sĩ kê đơn">
        <el-input :value="formData.doctorName" readonly></el-input>
      </el-form-item>
      <el-form-item label="Ngày kê đơn">
        <el-input :value="formData.prescriptionDate" readonly></el-input>
      </el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button @click="closeDialog">Đóng</el-button>
    </span>
  </el-dialog>
</template>

<script>
export default {
  name: "PrescriptionDialog",
  props: {
    dialogVisible: {
      type: Boolean,
      default: false
    },
    formData: {
      type: Object,
      default: () => ({
      })
    }
  },
  methods: {
    closeDialog() {
      this.$emit('update:dialogVisible', false); // Đóng hộp thoại
    }
  }
};
</script>
