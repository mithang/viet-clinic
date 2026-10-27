<template>
  <el-card class="index-module-card">
    <!-- Tiêu đề -->
    <div slot="header">
      <span class="el-card-title">Quy trình thường dùng</span>
      <i
        v-show="!isPanelSetIcon"
        class="el-icon-close"
        style="float: right; padding: 3px 0"
        @click="deletePanelItem"
      ></i>
      <i
        v-show="isPanelSetIcon"
        class="iconfont icon-setting"
        style="float: right; padding: 3px 0"
      ></i>
    </div>
    <!-- 流程区域 -->
    <div class="often-apply-box">
      <div class="apply-item">
        <el-avatar class="avatar-icon" :size="50"> user </el-avatar>
        <span class="apply-title">Quy trình xin nghỉ phép</span>
      </div>
      <div class="apply-item">
        <el-avatar class="avatar-icon" :size="50"> user </el-avatar>
        <span class="apply-title">Quy trình triển khai kiểm thử</span>
      </div>
      <div class="apply-item">
        <el-avatar class="avatar-icon" :size="50"> user </el-avatar>
        <span class="apply-title">Quy trình triển khai kiểm thử</span>
      </div>
      <div class="apply-item">
        <el-avatar class="avatar-icon" :size="50"> user </el-avatar>
        <span class="apply-title">Quy trình triển khai kiểm thử</span>
      </div>
      <div class="apply-item">
        <el-avatar class="avatar-icon" :size="50"> user </el-avatar>
        <span class="apply-title">Quy trình triển khai kiểm thử</span>
      </div>
    </div>
  </el-card>
</template>

<script>
export default {
  props: ['id', 'panelSetIcon'],
  data () {
    return {
      panelId: this.id,
      isPanelSetIcon: this.panelSetIcon
    }
  },
  watch:{
    panelSetIcon:{
      deep:true,
      handler(){
        this.isPanelSetIcon = this.panelSetIcon;
      }
    }
  },
  methods: {
    // Sự kiện gửi khi xóa mục bảng
    deletePanelItem () {
      this.$emit('deletePanelItemEvent', this.panelId)
    }
  }
}
</script>

<style lang="scss" scoped>
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
.el-icon-close {
  font-size: 1.2em;
  cursor: pointer;
}
.el-card-title {
  font-weight: bold;
}
.often-apply-box {
  display: grid;
  grid-template-columns: repeat(1, 100%);
}
.often-apply-box .apply-item {
  display: flex;
  align-items: center;
  cursor: pointer;
  margin-bottom: 20px;
}
.apply-item .apply-title {
  margin-left: 10px;
}
.apply-item .avatar-icon {
  background-color: #2f7dcd !important;
}
</style>
