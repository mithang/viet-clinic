<template>
  <div @click="tableTwoPopoverClick">
    <el-header
      class="main-header"
      height="56px"
      :style="{ backgroundColor: settings.headerColor }"
    >
      <div
        class="header-left"
        v-if="settings.style === 'up-down' && settings.showLogo"
      >
        <div
          class="aside-header"
          :style="{
            color: isLight('headerColor')
              ? 'rgba(0, 0, 0, 0.65)'
              : 'rgb(255, 255, 255)',
          }"
        >
          <img class="sysLogo" :src="sysLogo" alt="" />
        </div>
      </div>
      <div class="header-content">
        <!-- Có thể chọn: Ẩn nút hamburger (vì không còn cần sidebar)-->
        <div class="hamburger-container" v-if="false">
          <svg
            t="1492500959545"
            @click="toggleClick"
            class="wscn-icon hamburger"
            :class="{ 'is-active': !isCollapse }"
            style=""
            viewBox="0 0 1024 1024"
            width="64"
            height="64"
            :fill="
              isLight('headerColor')
                ? 'rgba(0, 0, 0, 0.65)'
                : 'rgb(255, 255, 255)'
            "
          >
            <path
              d="M966.8023 568.849776 57.196677 568.849776c-31.397081 0-56.850799-25.452695-56.850799-56.850799l0 0c0-31.397081 25.452695-56.849776 56.850799-56.849776l909.605623 0c31.397081 0 56.849776 25.452695 56.849776 56.849776l0 0C1023.653099 543.397081 998.200404 568.849776 966.8023 568.849776z"
              p-id="1692"
            ></path>
            <path
              d="M966.8023 881.527125 57.196677 881.527125c-31.397081 0-56.850799-25.452695-56.850799-56.849776l0 0c0-31.397081 25.452695-56.849776 56.850799-56.849776l909.605623 0c31.397081 0 56.849776 25.452695 56.849776 56.849776l0 0C1023.653099 856.07443 998.200404 881.527125 966.8023 881.527125z"
              p-id="1693"
            ></path>
            <path
              d="M966.8023 256.17345 57.196677 256.17345c-31.397081 0-56.850799-25.452695-56.850799-56.849776l0 0c0-31.397081 25.452695-56.850799 56.850799-56.850799l909.605623 0c31.397081 0 56.849776 25.452695 56.849776 56.850799l0 0C1023.653099 230.720755 998.200404 256.17345 966.8023 256.17345z"
              p-id="1694"
            ></path>
          </svg>
        </div>
        <div class="header-menu" ref="headerMenu">
          <el-menu
            ref="menuUl"
            :default-active="defaultActive"
            :text-color="
              'rgba(0, 0, 0, 0.65)'
            "
            :active-text-color="
              isLight('headerColor')
                ? 'rgba(0, 0, 0, 0.65)'
                : 'rgba(255, 255, 255, 1)'
            "
            class="el-menu-demo"
            mode="horizontal"
            :style="styleObject"
            @select="handleMenuSelect"
            @mouseenter="handleMenuMouseEnter"
          >
            <!-- Cải tạo: menu cấp một đổi thành submenu, hỗ trợ menu con -->
            <el-submenu
              ref="menuLi"
              v-for="(module, index) in handleRouters"
              v-show="
                index < limitNumber &&
                (module.parentId === '' || module.parentId === '0')
              "
              :key="module.id"
              :index="module.id"
              popper-class="top-submenu"
              :popper-append-to-body="false"
            >
              <template slot="title">
                <svg-icon
                  :iconClass="module.cssClass"
                  :style="{
                    color: isLight('headerColor')
                      ? 'rgba(0, 0, 0, 0.65)'
                      : 'rgba(255, 255, 255, .8)',
                    fill: 'currentColor',
                  }"
                />
                {{ module.name }}
              </template>
              <!-- Menu router con -->
              <el-menu-item
                v-for="subModule in getChildRouters(module.id)"
                :key="subModule.id"
                :index="subModule.id"
                @click="handleSubMenuClick(subModule)"
              >
                <svg-icon
                  v-if="subModule.cssClass"
                  :iconClass="subModule.cssClass"
                />
                {{ subModule.name }}
              </el-menu-item>
            </el-submenu>

            <!-- Vượt quá giới hạn gập lại: Thêm menu con -->
            <el-submenu index="more" v-if="handleRouters.length > limitNumber">
              <template slot="title">Thêm</template>
              <!-- Menu cấp một trong phần Thêm -->
              <el-submenu
                class="header-sub-menu"
                v-for="(module, index) in handleRouters"
                v-show="index >= limitNumber"
                :key="module.id"
                :index="module.id"
              >
                <template slot="title">
                  <svg-icon :iconClass="module.cssClass" />
                  {{ module.name }}
                </template>
                <!-- Router con trong phần Thêm -->
                <el-menu-item
                  v-for="subModule in getChildRouters(module.id)"
                  :key="subModule.id"
                  :index="subModule.id"
                  @click="handleSubMenuClick(subModule)"
                >
                  <svg-icon
                    v-if="subModule.cssClass"
                    :iconClass="subModule.cssClass"
                  />
                  {{ subModule.name }}
                </el-menu-item>
              </el-submenu>
            </el-submenu>
          </el-menu>
        </div>

        <div class="header-function" style="max-width: 400px" ref="headerRight">
          <div
            class="company-name"
            v-show="currentUser.id !== 1000 && currentUser.id !== 1001"
          >
            <el-dropdown
              @command="changeCompany"
              trigger="click"
              placement="bottom-start"
            >
              <span class="el-dropdown-link" style="padding-right:12px">
                {{ company.name
                }}<i class="el-icon-arrow-down el-icon--right"></i>
              </span>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item
                  v-for="item in companys"
                  :key="item.id"
                  :command="item.id"
                >{{ item.name }}</el-dropdown-item
                >
              </el-dropdown-menu>
            </el-dropdown>
          </div>
          <div class="messages">
            <div class="messages" v-if="drugIndateWarning" style="padding-left:10px">
              <el-popover
                placement="right"
                width="500"
                ref="popoverRefStuff"
                trigger="click"
              >
                <stuff-indate></stuff-indate>
                <el-button
                  class="notice-tip"
                  :style="{
                    color:
      'rgba(0, 0, 0, 0.65)'

                  }"
                  type="text"
                  size="large"
                  icon="el-icon-bell"
                  ref="bntClick"
                  slot="reference"
                  @click="unreadStuffNotice"
                >
                  <el-badge
                    :hidden="stuffIndateWarningTotal <= 0"
                    style="position: absolute; top: 0; right: -10px"
                    class="mark"
                    :max="10"
                    :value="stuffIndateWarningTotal"
                  ></el-badge>
                  <span style="position:absolute;margin-top:20px;font-size:10px;text-align:center;color:#171616;margin-left:-30px">
                    Cảnh báo vật tư
                  </span>
                </el-button>
              </el-popover>
            </div>

            <div class="messages" v-if="drugIndateWarning" style="padding-left:30px">
              <el-popover
                placement="right"
                width="500"
                ref="popoverRefDrug"
                trigger="click"
              >
                <drug-indate></drug-indate>
                <el-button
                  class="notice-tip"
                  :style="{
                    color:
                     'rgba(0, 0, 0, 0.65)'

                  }"
                  type="text"
                  size="large"
                  icon="el-icon-bell"
                  ref="bntClick"
                  slot="reference"
                  @click="unreadDrugNotice"
                >
                  <el-badge
                    :hidden="drugIndateWarningTotal <= 0"
                    style="position: absolute; top: 0; right: -10px"
                    class="mark"
                    :max="10"
                    :value="drugIndateWarningTotal"
                  ></el-badge>
                  <span style="position:absolute;margin-top:20px;font-size:10px;text-align:center;color:#171616;margin-left:-30px">
                    Cảnh báo thuốc
                  </span>
                </el-button>
              </el-popover>
            </div>

            <div class="messages" style="padding-left:20px">
              <el-popover
                placement="right"
                width="500"
                ref="popoverRef"
                trigger="manual"
              >
                <tabSite ref="tabSite" />
              </el-popover>
            </div>
          </div>

          <el-dropdown
            class="personal"
            :key="sizeKey"
            :size="settings.size"
            @command="handleCommand"
          >
            <a class="personal-link">
              <img v-if="userHeader" :src="userHeader" alt="" />
              <img v-else src="@/assets/images/personal_avatar.jpeg" alt="" />
              <span
                class="username"
                :style="{
                  color:
'rgb(23,22,22)'
                }"
              >{{ currentUser.name }}</span
              >
            </a>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="personalInfo"
              ><svg-icon iconClass="profileInfo" /> Thông tin cá nhân</el-dropdown-item
              >
              <el-dropdown-item command="layoutSet"
              ><svg-icon iconClass="layout" /> Cài đặt bố cục</el-dropdown-item
              >
              <el-dropdown-item command="sysSet" v-if="currentUser.id === 1000"
              ><svg-icon iconClass="sysSet" /> Cài đặt hệ thống</el-dropdown-item
              >
              <el-dropdown-item command="logout" divided
              ><svg-icon iconClass="loginout" /> Đăng xuất</el-dropdown-item
              >
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </div>
    </el-header>
  </div>
</template>

<script>
import { getUserById } from '@/api/admin/user'
import { getPhotoById } from "@/api/sys/sysSeting";
import { logOut, getToken } from "@/api/auth";
import { isLightOrDark } from "@/utils/common";
import tabSite from "../../notice/noticeRecordSite/tabSite";
import drugIndate from "../../components/drugIndate";
import stuffIndate from "../../components/stuffIndate";
import {
  clearLocalData,
  getLocalCompanys,
  setLocalToken,
  setLocalCurrentUser,
  getLocalRouters,
  setLocalRouters,
  setLocalDataPermisions,
  setLocalCurrentCompany,
  setLocalPersonalTheme,
} from "@/utils/auth";
import { routerTree, handleFamily } from "@/utils/routerTree";
import {getUserIndateWarning} from "@/api/admin/router";
import "@/assets/images/KCI8nCoKFf.jpg";

const _import = require("@/router/_import_" + process.env.NODE_ENV);
import 'axios';

export default {
  name: "NavHeader",
  components: { tabSite,drugIndate,stuffIndate},
  props: {
    isCollapse: Boolean,
    routers: Array,
    sysLogo: "",
  },
  data() {
    return {
      longRange:{Data:"API.Manage",AppId:"Oh_Newtouch_Clinic"},
      dialogVisible: false,
      limitNumber: 10,
      noticeTotal: 0,
      drugIndateWarningTotal:0,
      stuffIndateWarningTotal:0,
      drugIndateWarning:false,
      activeIndex: "1",
      isActive: true,
      currentUser: window.currentUser,
      sizeKey: 0,
      currentActive: "",
      // ❌ Xóa menuKey, không còn buộc render lại menu
      companys: getLocalCompanys(),
      company: window.currentUser.company,
      userHeader:'',
      // ✅ Thêm: Cache router đã xử lý, tránh tính toán lặp lại
      cachedHandleRouters: [],
    };
  },
  mounted() {
    if (this.routers.length <= 0) {
      this.$alert("Tài khoản này chưa được phân quyền, vui lòng liên hệ quản trị viên!", "", {
        confirmButtonText: "Đã biết",
        type: "warning",
        callback: () => {},
      });
    }
    getUserById(this.currentUser.id).then(res => {
      if (res.code === 100) {
        if (res.data.userExt && res.data.userExt.photoId) {
          getPhotoById(res.data.userExt.photoId).then((responseData) => {
            const src = `data:image/png;base64,${responseData}`;
            this.userHeader = src;
          })
        }
      }
    })
    this.setLimitNumber();
    this.getUserIsIndate();
  },
  watch: {
    "$store.state.notices.noticeTotal"(val) {
      this.noticeTotal = val;
    },
    "$store.state.notices.drugIndateWarningTotal"(val) {
      this.drugIndateWarningTotal = val+this.$store.state.notices.drugInventoryWarningTotal;
      this.$forceUpdate()
    },
    "$store.state.notices.drugInventoryWarningTotal"(val){
      this.drugIndateWarningTotal = val+this.$store.state.notices.drugIndateWarningTotal
    },
    "$store.state.notices.stuffIndateWarningTotal"(val){
      this.stuffIndateWarningTotal = val+this.$store.state.notices.stuffInventoryWarningTotal
    },
    "$store.state.notices.stuffInventoryWarningTotal"(val){
      this.stuffIndateWarningTotal = val+this.$store.state.notices.stuffIndateWarningTotal
    },
    "settings.size"(val) {
      if (val) {
        this.sizeKey += 1;
      }
    },
    // ❌ Xóa: $route listener không còn buộc làm mới menuKey
    // $route(val, old) {
    //   this.menuKey += 1;
    // },
    // ✅ Thêm: Lắng nghe thay đổi routers, cập nhật cache
    routers: {
      immediate: true,
      handler(newVal) {
        if (newVal && newVal.length > 0) {
          this.updateCachedRouters();
        }
      }
    }
  },
  computed: {
    ...Vuex.mapGetters(["settings", "menus"]),
    handleRouters() {
      // ✅ Sử dụng cache, tránh tính toán lại mỗi lần gây render lại
      if (this.cachedHandleRouters.length > 0) {
        return this.cachedHandleRouters;
      }
      return this.updateCachedRouters();
    },
    isLight() {
      return (colorType) => {
        return isLightOrDark(this.settings[colorType]);
      };
    },
    styleObject() {
      return {
        "--active-color": this.isLight("headerColor")
          ? "rgba(0, 0, 0, 0.65)"
          : "rgb(255, 255, 255)",
      };
    },
    defaultActive() {
      const route = this.$route;
      let routers = [];
      if (this.routers.length <= 0) {
        return "";
      }
      // ✅ Sử dụng dữ liệu router đã cache
      const parentRouters = this.cachedHandleRouters.length > 0
        ? this.cachedHandleRouters
        : this.handleRouters;

      if (route.meta.family && route.meta.parentId !== 0) {
        this.getMenus(route.meta.family[0]);
        return route.meta.family[0];
      } else {
        if (route.path === "/") {
          this.$router.push({ path: "/homepage" });
        }
        if (!this.menus.topMenuId) {
          if (parentRouters.length > 0) {
            this.getMenus(parentRouters[0].id);
            return parentRouters[0].id;
          }
          return "";
        } else {
          this.getMenus(this.menus.topMenuId);
          return this.menus.topMenuId;
        }
      }
    },
  },
  methods: {
    // ✅ Thêm mới: Cập nhật dữ liệu router đã cache
    updateCachedRouters() {
      let routers = [];
      this.routers.forEach((item) => {
        if (!item.code && (item.parentId === '0' || item.parentId === '')) {
          routers.push(item);
        }
      });
      this.cachedHandleRouters = routers;
      return routers;
    },
    tableTwoPopoverClick () {},
    // Thêm mới: Lấy router con của router cha được chỉ định
    // Đi xuyên nhóm trung gian KHÔNG có code (ví dụ nhóm "Quản lý nhiệm vụ"
    // ...9085 dưới nhóm cấp cao ...9083): các nhóm này không phải menu lá,
    // không có url, nên không hiển thị mà chỉ dùng để gom nhóm. Chỉ các item
    // CÓ code mới là menu lá và được trả về; không đi xuyên qua item có code
    // để thứ tự/nội dung các menu khác không đổi. seen chống vòng lặp/lặp item.
    getChildRouters(parentId) {
      let childRouters = [];
      const seen = new Set();
      const walk = (pid) => {
        this.routers.forEach((item) => {
          if (item.parentId === pid && !seen.has(item.id)) {
            seen.add(item.id);
            if (item.code) {
              childRouters.push(item);
            } else {
              walk(item.id);
            }
          }
        });
      };
      walk(parentId);
      return childRouters;
    },
    // Thêm mới: Sự kiện click menu con
    handleSubMenuClick(subModule) {
      if (subModule.url) {
        this.$router.push({ path: subModule.url });
      }
    },
    // Thêm mới: Sự kiện hover menu (tối ưu trải nghiệm)
    handleMenuMouseEnter() {
      this.$refs.menuUl && this.$refs.menuUl.updatePopper();
    },
    getUserIsIndate(){
      let router = JSON.parse(sessionStorage.getItem("routers"));
      let flags = false;
      router.forEach((item)=>{
        if(item.code==='stock/warning'){
          flags = true;
        }
      });
      if(flags){
        getUserIndateWarning(currentUser.id).then((res)=>{
          if(res.code===100){
            if(res.data!=null&&res.data!==undefined){
              this.drugIndateWarning=true;
            }else{
              this.drugIndateWarning = false;
            }
          }
        }).catch((error)=>{
          this.outputError(error)
        })
      }else{
        this.drugIndateWarning = false;
      }
    },
    unreadDrugNotice(){},
    unreadStuffNotice(){},
    ...Vuex.mapActions({
      changeSetting: "settings/changeSetting",
    }),
    changeCompany(command) {
      if (this.companys.length !== 0)
        this.company = this.companys.find((i) => command === i.id);
      this.login(command);
      window.location.reload();
    },
    login(id) {
      getToken(
        this.currentUser.loginname,
        window.sessionStorage.getItem("User_P"),
        id
      )
        .then((responseData) => {
          if (responseData.code === 100) {
            this.handleLoginInfo(responseData.data);
          } else {
            this.isError = true;
            this.loginError = responseData.msg;
          }
          this.$store.dispatch("setLoading", false);
        })
        .catch((error) => {
          this.$store.dispatch("setLoading", false);
          this.outputError(error);
        });
    },
    initIndexRouter() {
      if (sessionStorage.getItem('indexRouterGenerated') === 'true') {
        return
      }
      let indexRouter = {
        path: "/",
        name: "/",
        component: _import("home/index"),
        children: [...this.generateChildRouters()],
      };
      this.$router.addRoute("/", indexRouter);
      sessionStorage.setItem('indexRouterGenerated', 'true')
    },
    generateChildRouters() {
      let routers = getLocalRouters();
      let routerTreeArr = routerTree(routers);
      if (!routers) {
        return [];
      }
      // Đụng độ path không phân biệt hoa/thường (vd 'myTask' của myTask/myTask
      // vs 'mytask' của workbench/mytask): vue-router mặc định match không
      // phân biệt hoa/thường nên route đăng ký trước thắng và trang thật
      // không bao giờ render. Chỉ các route có path đụng độ mới bật
      // caseSensitive (cần cả 2 phía), các route khác giữ nguyên mặc định.
      const lowerPathCounts = {};
      routers.forEach((r) => {
        if (r.code && r.url) {
          const k = String(r.url).toLowerCase();
          lowerPathCounts[k] = (lowerPathCounts[k] || 0) + 1;
        }
      });
      let childRouters = [];
      for (let router of routers) {
        if (router.code) {
          let routerProps = JSON.parse(router.properties);
          let childRouter = {
            path: router.url,
            name: router.code,
            component: _import(router.code + "/index"),
            caseSensitive: !!router.url && lowerPathCounts[String(router.url).toLowerCase()] > 1,
            meta: {
              name: router.name,
              cssClass: routerProps.cssClass,
              routerId: router.id,
              parentId: router.parent.id,
              family: handleFamily(
                routerTreeArr,
                (item) => item.id === router.id
              ),
              linkUrl: routerProps.linkUrl,
              requiresAuth: routerProps.meta.requiresAuth,
              nameFullPath: routerProps.nameFullPath,
            },
          };
          childRouters.push(childRouter);
        }
      }
      let redirectRouter = {
        path: "redirect",
        component: _import("redirect/index"),
      };
      childRouters.push(redirectRouter);
      return childRouters;
    },
    handleLoginInfo(userData) {
      setLocalCurrentUser({
        id: userData.userId,
        name: userData.username,
        loginname: userData.loginname,
        company: userData.company,
        department: userData.department,
      });
      setLocalCurrentCompany({
        id: userData.company.id,
        code: userData.company.code,
        name: userData.company.name,
      });
      setLocalToken(userData.token);
      setLocalRouters(userData.routers);
      setLocalDataPermisions(userData.dataPermisions);

      const personalTheme = userData.personalTheme;
      personalTheme.theme = JSON.parse(personalTheme.theme);
      setLocalPersonalTheme(personalTheme);
      this.changeTheme(personalTheme.theme);

      this.initIndexRouter();

      const routers = userData.routers;
      if (this.$route.query.redirect) {
        const url = this.removeBlock(
          JSON.stringify({ url: this.$route.query.redirect.substring(1) })
        );
        if (routers.includes(url)) {
          this.$router.push(this.$route.query.redirect);
        }
      }
      if (this.$route.path !== "/") {
        this.$router.push("/");
      } else {
        location.reload();
      }
    },
    removeBlock(str) {
      if (str) {
        const reg = /^\{/gi;
        const reg2 = /\}$/gi;
        str = str.replace(reg, "");
        str = str.replace(reg2, "");
        return str;
      } else {
        return str;
      }
    },
    changeTheme(theme) {
      const settings = [
        "theme",
        "headerColor",
        "sidebarColor",
        "backgroundColor",
        "size",
        "style",
        "showTagsView",
        "fixedHeader",
        "showLogo",
      ];
      this.$ELEMENT.size = theme.size;
      settings.forEach((item) => {
        this.handleSetting(item, theme[item]);
      });
    },
    handleSetting(key, value) {
      this.changeSetting({ key, value });
    },
    setLimitNumber() {
      this.$nextTick(() => {
        const menus = [...this.$refs.menuUl.$el.children || []];
        let menuUlWidth = 0;
        menus.forEach((item) => {
          menuUlWidth += item.offsetWidth;
        });
        const width = document.body.getBoundingClientRect().width;
        const usefullWidth = width - 660;

        if (usefullWidth < menuUlWidth) {
          this.limitNumber = 10;
        } else {
          this.limitNumber = 10;
        }
      });
    },
    unreadNotice() {
      this.$refs.popoverRef.doToggle();
      this.$refs.tabSite.onSearch();
    },
    ...Vuex.mapActions({
      getMenus: "menus/getMenus",
    }),
    async handleMenuSelect(menuId) {
      await this.getMenus(menuId);
      const firstChild = this.getChildRouters(menuId);
      if (firstChild && firstChild.length > 0) {
        this.$router.push(firstChild[0].url);
      }
    },
    toggleClick() {
      this.isActive = this.isCollapse;
      this.$emit("hamburger", !this.isActive);
    },
    handleCommand(command) {
      switch (command) {
        case "logout":
          this.doLogOut();
          break;
        case "personalInfo":
          this.$emit("openPersonalInfoDialog");
          break;
        case "record":
          this.dialogVisible = true;
          break;
        case "layoutSet":
          this.$store.dispatch("settings/showPanel", {
            isShow: true,
            type: "layout",
          });
          this.$emit("openSetting", "layout", "openLayout");
          break;
        case "sysSet":
          this.$store.dispatch("sys/showPanel", { isShow: true, type: "sys" });
          this.$emit("openSetting", "sys", "openSys");
      }
    },
    doLogOut() {
      this.$confirm("Bạn có chắc muốn thoát khỏi hệ thống không?", "Nhắc nhở", {
        confirmButtonText: "Xác nhận",
        cancelButtonText: "Hủy",
        type: "warning",
      })
        .then(() => {
          // Gọi API logout của backend trước (/auth/logout, đã có sẵn)
          // để hủy session và ghi log đăng xuất. logOut() phải gọi TRƯỚC
          // khi dọn local, vì interceptor đọc token từ local storage lúc
          // tạo request. Kể cả khi token đã hết hạn (API reject 20011)
          // vẫn dọn local và về trang login như cũ.
          const finish = () => {
            clearLocalData();
            this.$router.replace("/login");
            // Sử dụng setTimeout để đảm bảo chuyển hướng router hoàn tất trước khi làm mới
            setTimeout(() => {
              window.location.reload();
            }, 100);
          };
          logOut().then(finish).catch(finish);
        })
        .catch(() => {});
    },
  },
};
</script>

<style lang="scss" scoped>
.company-name .el-dropdown-link {
  cursor: pointer;
  color: #171616;
}
.company-name .el-icon-arrow-down {
  font-size: 12px;
}
.red-point {
  position: relative;
}
.red-point::before {
  content: " ";
  border: 3px solid red;
  border-radius: 3px;
  position: absolute;
  z-index: 1000;
  right: 0%;
}
.nav-menu:not(.el-menu--collapse) {
  width: 200px;
  height: 100%;
  overflow: auto;
}
.main-header {
  display: flex;
  padding: 0;
  .header-function {
    float: right;
    line-height: 56px;
    height: 100%;
    .personal-link {
      padding: 0 10px;
      text-decoration: none;
      white-space: nowrap;
      text-overflow: ellipsis;
      overflow: hidden;
      word-break: break-all;
    }
    .personal-link:hover {
    }
    img {
      -moz-border-radius: 50%;
      -webkit-border-radius: 50%;
      border-radius: 50%;
      vertical-align: middle;
      width: 32px;
      height: 32px;
    }
    .username {
      margin-left: 3px;
      font-size: 10pt;
      color: #fff;
    }
    .company-name {
      float: left;
      display: flex;
      align-items: center;
      height: 100%;
      position: relative;
      &:after {
        position: absolute;
        content: "";
        width: 1px;
        height: 14px;
        right: 0;
        top: 50%;
        margin-top: -7px;
        background-color: rgba(255, 255, 255, 0.5);
      }
      .el-button {
        font-size: 14px;
        background: none;
        border: none;
        outline: none;
        cursor: default;
        white-space: nowrap;
        text-overflow: ellipsis;
        overflow: hidden;
        word-break: break-all;
      }
    }
    .messages {
      float: left;
      height: 100%;
      > span {
        display: flex;
        height: 100%;
      }
      ::v-deep .el-popover__reference-wrapper {
        display: flex;
        align-items: center;
      }
      .el-button {
        span {
          width: 100%;
          display: inline-block;
          white-space: nowrap;
          text-overflow: ellipsis;
          word-break: break-all;
          text-align: center;
        }
      }
      .notice-tip {
        max-width: 89px;
        width: 30px;
        margin-left: 10px;
        position: relative;
        ::v-deep .el-icon-bell {
          font-size: 18px !important;
        }
      }
    }
    .personal {
      display: flex;
      align-items: center;
    }
  }
  .header-left {
    width: 220px;
    .aside-header {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      overflow: hidden;
      padding-left: 50px;    // Đẩy toàn bộ sang phải
      .sysLogo {
        // Quan trọng: Giới hạn trong container, giữ tỷ lệ
        height: 30px;
        display: block;
        object-fit: contain;

      }
    }
  }
  .header-content {
    position: relative;
    flex: 1;
    .hamburger-container {
      float: left;
      height: 100%;
      width: 50px;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: rgba(255, 255, 255, 0.05);
      transition: background-color 0.3s;
      &:hover {
        background-color: rgba(255, 255, 255, 0.1);
      }
    }
  }
  .header-menu {
    position: absolute;
    top: 5px;
    left: 60px;
    display: inline-block;
    width: calc(100% - 700px); // Điều chỉnh độ rộng menu con
    .el-menu {
      background: none;
      border-bottom: 0;
      width: 100%;
    }
    ::v-deep .el-menu--horizontal {
      // Kiểu dáng tiêu đề submenu cấp một
      .el-submenu {
        height: 45px !important;
        line-height: 45px !important;
        position: relative;

        &:hover {
          background-color: rgba(255, 255, 255, 0.08) !important;
        }

        .el-submenu__title {
          height: 45px !important;
          line-height: 45px !important;
          border-bottom: 0;
          padding: 0 20px !important;

          &:hover {
            background-color: transparent !important;
            color: #171616 !important;
          }

          &:after {
            position: absolute;
            content: "";
            width: 0;
            left: 50%;
            height: 2px;
            border-radius: 6px;
            transition: all 0.3s;
            background-color: var(--active-color) !important;
            bottom: 0;
          }
        }

        &.is-active {
          .el-submenu__title {
            font-weight: bold;
            color: rgb(23, 22, 22);

            &:after {
              width: 30px;
              left: 50%;
              margin-left: -15px;
            }
          }
        }

        // Lớp pop-up của menu con
        .el-menu {
          background-color: #fff !important;  // Nền trắng
          border-radius: 4px;
          box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);

          // Mục menu con - chỉ đổi màu chữ
          .el-menu-item {
            color: #606266 !important;  // Màu chữ mặc định
            height: 40px !important;
            line-height: 40px !important;
            transition: all 0.2s ease !important;

            &:hover,
            &:focus {
              background-color: transparent !important;  // Nền trong suốt
              font-weight: 500 !important;
            }

            &.is-active {
              color: #409EFF !important;
              background-color: transparent !important;
              font-weight: 600 !important;
            }
            }

          // Menu con lồng nhau
          .el-submenu {
            .el-submenu__title {
              color: #606266 !important;
              height: 40px !important;
              line-height: 40px !important;

              &:hover,
              &:focus {
                background-color: transparent !important;  // Nền trong suốt
                color: #409EFF !important;                // Chỉ đổi màu xanh
              }
              }

            // Menu cấp ba
            .el-menu-item {
              &:hover {
                background-color: transparent !important;
                color: #409EFF !important;
              }
              }
            }
          }
        }
      }

    // popper-class Kiểu dáng
    ::v-deep .top-submenu {
      background-color: #fff !important;

      .el-menu-item {
        &:hover {
          background-color: transparent !important;
          color: #409EFF !important;
        }
      }

      .el-submenu__title {
        &:hover {
          background-color: transparent !important;
          color: #409EFF !important;
        }
      }
    }

    // Quan trọng: đảm bảo style popper-class có hiệu lực
    ::v-deep .top-submenu {


      .el-menu-item {
        &:hover {
          background-color: #171616 !important;
          color: #1890ff !important;
        }
        }

      .el-submenu__title {
        &:hover {
          background-color: #171616 !important;
          color: #1890ff !important;
        }
        }
      }
    }
  }
// Ẩn nút hamburger (có thể chọn)
.hamburger {
  display: none !important;
}
.hamburger.is-active {
  transform: rotate(0deg);
}
// Style submenu "Thêm" - tăng phản hồi khi hover
.header-sub-menu {
  .el-submenu__title {
    color: #555 !important;
    // Hover tiêu đề submenu cấp một trong "Thêm"
    &:hover {
      color: #1890ff !important;
    }
  }
  .el-menu-item {
    color: #333 !important;
    // Hover các mục submenu trong "Thêm"
    &:hover {
      color: #1890ff !important;
    }
  }
}
// Style lớp popup submenu tuỳ chỉnh - tăng độ ưu tiên
::v-deep .top-submenu {
  background-color: #fff !important;

  .el-menu-item {
    &:hover {
      background-color: transparent !important;
      color: #409EFF !important;
    }
    }

  .el-submenu__title {
    &:hover {
      background-color: transparent !important;
      color: #409EFF !important;
    }
    }
  }
// Style hover của mục menu thả xuống
::v-deep .el-dropdown-menu__item:not(.is-disabled) {
  &:hover {
    color: #1890ff !important;
    .svg-icon {
      color: var(--active-color) !important;
      fill: currentColor;
    }
    }
  }
// Buộc ghi đè style submenu mặc định của Element UI (sửa lỗi quan trọng)
::v-deep .el-menu--popup {
  .el-menu-item {
    &:hover {
      color: #1890ff !important;
    }
    }
  .el-submenu__title:hover {
    color: #1890ff !important;
  }
  }
</style>
