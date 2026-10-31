<template>
  <div></div>
</template>

<script>
export default {
  name: 'Redirect',
  beforeCreate() {
    const { params, query } = this.$route
    const { to } = query

    // Nếu có tham số to, chuyển đến đường dẫn chỉ định
    if (to) {
      this.$router.replace({
        path: to,
        query: query
      })
    } else {
      // Ngược lại chuyển về trang chủ
      this.$router.replace('/homepage')
    }
  },
  render(h) {
    return h() // Tránh render bất kỳ nội dung nào
  }
}
</script>
