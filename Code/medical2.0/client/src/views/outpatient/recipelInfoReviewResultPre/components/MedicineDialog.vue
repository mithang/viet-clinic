<template>
  <el-dialog :visible.sync="dialogVisible" title="Tra cứu kết quả lấy thuốc" width="70%" :close-on-click-modal="false"  @close="closeDialog" >
    <el-form :model="formData" ref="medicineForm" label-width="120px">
      <el-row>
        <!-- 让两个表单项并排显示 -->
        <el-col :span="8" label-align="left" >
          <el-form-item label="Mã đơn thuốc bảo hiểm y tế">
            <el-input v-model="formData.hiRxno" readonly style="width: 100%;"></el-input>
          </el-form-item>
        </el-col>

        <el-col :span="8" label-align="left">
          <el-form-item label="Thời gian quyết toán bảo hiểm y tế">
            <el-input v-model="formData.setlTime" readonly style="width: 100%;"></el-input>
          </el-form-item>
        </el-col>
      </el-row>
      <!-- Thông tin lấy thuốc của đơn thuốc -->
      <el-col :span="8" style="margin-top: 5px;font-size: 12px">Thông tin lấy thuốc của đơn thuốc</el-col>
      <!-- 渲染表格 -->
      <el-table :data="formData.eltdelts" style="width: 100%">
        <el-table-column prop="medinsListCodg" label="Mã thuốc của cơ sở y tế"></el-table-column>
        <el-table-column prop="drugGenname" label="Tên chung"></el-table-column>
        <el-table-column prop="drugProdname" label="Tên thương mại của thuốc"></el-table-column>
        <el-table-column prop="drugDosform" label="Dạng bào chế"></el-table-column>
        <el-table-column prop="drugSpec" label="Quy cách thuốc"></el-table-column>
        <el-table-column prop="ent" label="Số lượng"></el-table-column>
        <el-table-column prop="aprvno" label="Số phê duyệt"></el-table-column>
        <el-table-column prop="bchno" label="Số lô"></el-table-column>
        <el-table-column prop="manuLotnum" label="Số lô sản xuất"></el-table-column>
        <el-table-column prop="prdrName" label="Nhà sản xuất"></el-table-column>
      </el-table>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button @click="closeDialog">Đóng</el-button>
    </span>
  </el-dialog>
</template>

<script>
export default {
  name: "MedicineDialog",
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
