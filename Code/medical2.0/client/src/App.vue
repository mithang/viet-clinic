<template>
  <div id="app" style="height: 100%;">
    <keep-alive :max="20" v-if="isRouterAlice">
      <router-view></router-view>
    </keep-alive>
  </div>
</template>
<script>
  // Địa chỉ service backend truyền cho component báo cáo
  window.REPORT_SERVER_URL = process.env.REPORT_SERVER_URL
  export default {
    name: "app",
    provide() {
      return {
        reload: this.reload
      }
    },
    data() {
      return {
        isRouterAlice: true
      }
    },
    mounted() {

    },
    methods: {
     
      reload() {
        this.isRouterAlice = false;
        this.$nextTick(() => {
          this.isRouterAlice = true
        })
      }
    }

  };
</script>
