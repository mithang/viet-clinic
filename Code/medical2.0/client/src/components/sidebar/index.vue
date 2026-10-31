<template>
  <!-- Container ngoài: bố cục flex dọc, lấp đầy container cha, đảm bảo bản quyền ở đáy -->
  <div class="sidebar-wrapper" :style="{ height: '100%' }">
    <el-menu
      class="nav-menu"
      :default-active="defaultActive"
      :background-color="settings.sidebarColor"
      :text-color="isLight('sidebarColor')? 'rgba(0, 0, 0, 0.65)' : 'rgb(191, 203, 217)'"
      :active-text-color="settings.theme"
      :default-openeds="defaultOpeneds"
      :collapse="isCollapse"
      :style="styleObject">
      <sidebar-item v-for="(router, index) in routers" :key="index" :item="router"></sidebar-item>
    </el-menu>

    <!-- Khu vực thông tin bản quyền: Căn trái + thỏa thuận một dòng riêng + ở đáy + siêu liên kết -->
    <div
      class="sidebar-copyright"
      :style="{
        backgroundColor: settings.sidebarColor,
        color: isLight('sidebarColor') ? 'rgba(0, 0, 0, 0.5)' : 'rgba(255, 255, 255, 0.5)',
        fontSize: '9.5px',
        textAlign: 'left', // Căn trái
        padding: isCollapse ? '12px 12px' : '16px 20px',
        lineHeight: '1.6',
        borderTop: isLight('sidebarColor') ? '1px solid rgba(0,0,0,0.05)' : '1px solid rgba(255,255,255,0.05)',
        marginTop: 'auto'
      }">
      <!-- Dòng đầu: thông tin bản quyền -->
      <div v-if="!isCollapse">Bản quyền © 2025 Công ty TNHH Công nghệ Chuỗi số Thiên Thiên Hồ Bắc</div>
      <!-- Dòng thứ hai: tiền tố thỏa thuận -->
      <div v-if="!isCollapse" style="margin-top: 6px;">Mã nguồn của hệ thống này được cấp phép theo</div>
      <!-- Dòng thứ ba: Tên thỏa thuận (một dòng riêng) -->
      <div v-if="!isCollapse" style="margin-top: 2px;">
        <a
          href="https://open.tntlinking.com/communityTreaty"
          target="_blank"
          rel="noopener noreferrer"
          :style="{
            color: settings.theme, // Dùng lại màu chủ đề làm màu liên kết
            textDecoration: 'none',
            display: 'inline-block' // Đảm bảo ổn định bố cục một dòng riêng
          }">
          Thỏa thuận Giấy phép Phần mềm Mã nguồn mở Thiên Thiên (Bản Cộng đồng)
        </a>
      </div>

      <!-- Trạng thái thu gọn hiển thị ngắn gọn (căn giữa, tránh tràn văn bản) -->
      <div v-if="isCollapse" style="font-size: 10px; text-align: center;">©2025 Thiên Thiên Số Liên</div>
    </div>
  </div>
</template>

<script>
  import { isLightOrDark } from '@/utils/common'
  import { getLocalToken } from '@/utils/auth'
  import SidebarItem from './SidebarItem'
  export default {
    name: 'sidebar',
    components: { SidebarItem },
    props: ['isCollapse', 'routers'],
    data() {
      return {
        defaultOpeneds: []
      }
    },
    watch: {
      routers() {
        // if (this.routers.length > 0) {
        //   this.defaultActive = this.routers[0].id
        //   this.$router.push(this.routers[0].url)
        // }
      }
    },
    computed: {
      ...Vuex.mapGetters(['settings']),
      isLight() {
        return function(colorType) {
          return isLightOrDark(this.settings[colorType])
        }
      },
      styleObject() {
        return {
          '--active-color': this.settings.theme
        }
      },
      defaultActive() {
        const route = this.$route
        // if (route.meta.parentId && route.meta.parentId != 0) {
        //   this.getMenus(route.meta.parentId)
        // }
        if (route.meta.family && route.meta.parentId !== 0) {
          this.getMenus(route.meta.family[0])
        }
        return route.meta.routerId
      }
    },
    mounted() {

    },
    methods: {
      ...Vuex.mapActions({
        getMenus: 'menus/getMenus'
      }),
      handleOpenWindow(url) {
        if (url) {
          switch (url) {
            case 'screenView':
              this.handleScreenView()
              break
            case 'reportDesign':
              this.openReportDesign()
              break
            default:
              break
          }
        }
      },
      handleScreenView() {
        return false
        // Tạm thời vô hiệu hóa
        // let screenWindow = null;
        // this.openWindow(screenWindow)
      },
      openReportDesign() {
        const token = getLocalToken()
        window.open(process.env.REPORT_CLIENT_URL + "?accss_tocken=" + token, "dataease")
      },
      openWindow(view) {
        let routeData = this.$router.resolve({
          path: "/screenView"
        });
        if (!view) {
          let width = screen.availWidth;
          let height = screen.availHeight;
          window.name = 'index';
          view = window.open(
            routeData.href,
            "screenView",
            "menubar=0,scrollbar=0,resizable=1,location=0,status=0, top=10, left=10, width=" + width + ", height=" + height
          );
          view.focus();
        }
        //   let width = screen.availWidth;
        //   let height = screen.availHeight;
        // window.open(
        //   'https://www.hightopo.com/demo/Seari_AirPort',
        //   "screenView",
        //   "menubar=0,scrollbar=0,resizable=1,location=no,status=0, top=10, left=10, width=" + width + ", height=" + height
        // );
      }
    }
  };
</script>

<style lang="scss" scoped>
  a {
    text-decoration: none;
  }
  .el-menu .svg-icon {
    color: rgb(191, 203, 217);
    fill: currentColor;
  }
  .el-menu-item.is-active .svg-icon, .el-submenu__title.is-active .svg-icon {
    color: var(--active-color) !important;
    fill: currentColor!important;
  }
  .el-menu-item, .el-submenu__title {
    color: rgb(191, 203, 217);
    fill: currentColor;
  }
</style>
<style lang="scss">
  .nav-menu {
    .el-submenu__title:hover,
    .el-menu-item:hover {
      background-color: rgba(0,0,0,.08)!important;
    }
    .el-menu-item:focus, .el-menu-item:active {
      background-color: rgba(0,0,0,0)!important;
    }

    &.el-menu--collapse {
      .el-submenu > .el-submenu__title {
        > span {
          height: 0!important;
          width: 0!important;
          overflow: hidden;
          visibility: hidden!important;
          display: inline-block;
        }
        .el-submenu__icon-arrow {
          display: none;
        }
      }
    }
  }
  .el-menu--vertical .el-submenu .el-submenu__title,
  .el-menu--vertical .el-submenu.is-opened .el-submenu__title {
    &:hover {
      background-color: rgba(0,0,0,0)!important;
    }
    background-color: rgba(0,0,0,0)!important;
  }
  .el-menu-item:hover {
    background-color: rgba(0,0,0,.08)!important;
  }
  .sidebar-wrapper {
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }

  /* Tùy chọn: Tối ưu hóa kiểu dáng thanh cuộn */
  .nav-menu::-webkit-scrollbar {
    width: 4px;
  }
  .nav-menu::-webkit-scrollbar-thumb {
    background-color: rgba(0,0,0,0.1);
  }

  /* Bố cục cốt lõi: Đảm bảo container lấp đầy chiều cao, bản quyền ở đáy */
  .sidebar-wrapper {
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }

  /* Menu chiếm đầy chiều cao còn lại, nội dung vượt có thể cuộn */
  .nav-menu {
    flex: 1;
    overflow-y: auto;
    overflow-x: hidden;
  }

  /* Tối ưu hóa hiệu ứng hover liên kết (tương thích kiểu scoped) */
  .sidebar-copyright a {
    color: var(--el-color-primary) !important; /* Dùng lại màu chủ đề element-plus */
    text-decoration: none;
  }

  .sidebar-copyright a:hover {
    text-decoration: underline !important;
  }

  /* Tùy chọn: Tối ưu hóa kiểu dáng thanh cuộn */
  .nav-menu::-webkit-scrollbar {
    width: 4px;
  }
  .nav-menu::-webkit-scrollbar-thumb {
    background-color: rgba(0,0,0,0.1);
  }

  /* Đảm bảo bố cục bình thường trong trạng thái thu gọn */
  :deep(.el-menu--collapse) {
    width: 64px !important;
  }
</style>
