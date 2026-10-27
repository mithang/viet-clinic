<template>
  <el-dialog :visible.sync="dialogVisible" title="Tra cứu kiểm duyệt đơn thuốc điện tử" width="60%"
             :close-on-click-modal="false"  @close="closeDialog"
  ><el-form :model="formData" ref="auditForm" label-width="120px">
    <el-row gutter={20}>
      <el-col :span="8">
        <el-form-item label="Mã đơn thuốc bảo hiểm y tế">
          <el-input v-model="formData.hiRxno" readonly></el-input>
        </el-form-item>
      </el-col>
      <el-col :span="8">
        <el-form-item label="Tên bác sĩ bảo hiểm y tế">
          <el-input v-model="formData.pharName" readonly></el-input>
        </el-form-item>
      </el-col>
      <el-col :span="8">
        <el-form-item label="Mã bác sĩ bảo hiểm y tế">
          <el-input v-model="formData.pharCode" readonly></el-input>
        </el-form-item>
      </el-col>
    </el-row>

    <el-row gutter={20}>
      <el-col :span="8">
        <el-form-item label="Trạng thái kiểm duyệt đơn thuốc">
          <el-input v-model="formData.rxChkStasCodg" readonly></el-input>
        </el-form-item>
      </el-col>
      <el-col :span="8">
        <el-form-item label="Ý kiến kiểm duyệt đơn thuốc">
          <el-input v-model="formData.rxChkOpnn" readonly></el-input>
        </el-form-item>
      </el-col>
      <el-col :span="8">
        <el-form-item label="Thời gian kiểm duyệt đơn thuốc">
          <el-input v-model="formData.rxChkTime" readonly></el-input>
        </el-form-item>
      </el-col>
    </el-row>
  </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button @click="closeDialog">Đóng</el-button>
    </span>
  </el-dialog>
</template>

<script>
export default {
  name: "AuditDialog",
  props: {
    dialogVisible: {
      type: Boolean,
      default: false
    },
    formData: {
      type: Object,
      default: () => ({
        reviewerName: '',
        reviewResult: '',
        reviewContent: ''
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
