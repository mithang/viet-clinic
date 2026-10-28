<template>
  <el-card class="page-container">
    <el-alert
      title="Chức năng chưa triển khai"
      type="warning"
      :closable="false"
      show-icon
    >
      <div>
        Chức năng<span v-if="$route.meta && $route.meta.name"> “{{ $route.meta.name }}”</span>
        chưa được triển khai trong hệ thống. Nội dung sẽ được bổ sung sau;
        vui lòng liên hệ quản trị viên nếu cần sử dụng chức năng này.
      </div>
    </el-alert>
  </el-card>
</template>

<script>
export default {
  name: 'NotImplementedPlaceholder'
}
</script>
