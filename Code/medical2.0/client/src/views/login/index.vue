<template>
  <div
    v-if="isLoaded"
    class="login-container"
    :class="sysData.loginLayout"
    :style="{ backgroundImage: 'url(' + (sysData.loginBg || backImg) + ')' }"
  >
    <div class="login-header">
      <div class="login-logo" v-if="sysData.projectLogo">
        <img :src="sysData.projectLogo" alt="Logo phòng khám" />
      </div>
      <div class="login-title">
        {{ sysData.sysName || 'Phòng khám đám mây' }}
      </div>
    </div>
    <div class="login-form">
      <el-form
        class="form"
        :model="loginForm"
        :rules="rules"
        ref="loginForm"
        autoComplete="on"
        label-position="left"
        @submit.native.prevent
      >
        <div class="login-body">
          <h1 class="form-title">Đăng nhập hệ thống</h1>
          <p class="form-subtitle">
            Vui lòng đăng nhập bằng tài khoản được cấp bởi quản trị viên.
          </p>

          <el-alert
            class="login-error"
            v-show="isError"
            :title="loginError"
            type="error"
            show-icon
            :closable="false"
          ></el-alert>

          <el-form-item prop="username">
            <span class="field-label">Tên đăng nhập</span>
            <el-input
              name="username"
              v-model="loginForm.username"
              autoComplete="username"
              autofocus
              placeholder="Nhập tên đăng nhập"
              prefix-icon="el-icon-user"
              @input="isError = false"
              size="large"
            ></el-input>
          </el-form-item>

          <el-form-item prop="password">
            <span class="field-label">Mật khẩu</span>
            <el-input
              name="password"
              type="password"
              v-model.trim="loginForm.password"
              autoComplete="current-password"
              placeholder="Nhập mật khẩu"
              prefix-icon="el-icon-lock"
              show-password
              @input="isError = false"
              @keyup.enter.native="doLogin"
              size="large"
            ></el-input>
          </el-form-item>

          <el-button
            class="login-submit"
            type="primary"
            :loading="showLoading"
            @click.native.prevent="doLogin"
            native-type="submit"
            size="large"
            >Đăng nhập</el-button
          >
        </div>
      </el-form>
      <div
        class="graph"
        :style="{ backgroundImage: 'url(' + sysData.loginGraph + ')' }"
      ></div>
    </div>



    <div class="footer">
      <div class="footer-copyright">
        Bản quyền © 2025 Công ty TNHH Công nghệ Chuỗi số Thiên Thiên Hồ Bắc
        <span class="footer-legal">(湖北天天数链技术有限公司)</span>
      </div>
      <!-- Dòng riêng: tên thỏa thuận cấp phép -->
      <div class="footer-license">
        <a
          href="https://open.tntlinking.com/communityTreaty"
          target="_blank"
          rel="noopener noreferrer"
        >
          Mã nguồn hệ thống được phát hành theo Thỏa thuận Giấy phép Phần mềm
          Mã nguồn mở Thiên Thiên (Bản Cộng đồng)
        </a>
      </div>
    </div>


    <el-dialog
      title="Chọn phòng khám"
      :visible.sync="dialogVisible"
      width="440px"
      append-to-body
      custom-class="clinic-picker-dialog"
      :close-on-click-modal="false"
    >
      <p class="til">
        Tài khoản của bạn được liên kết với nhiều phòng khám. Vui lòng chọn
        phòng khám cần đăng nhập.
      </p>
      <ul class="clinic-list">
        <li
          v-for="(i, ind) in firmList"
          :key="i.id"
          :class="{ 'is-selected': firmIndex === ind }"
          @click="firmIndex = ind"
        >
          <span class="clinic-radio">
            <i v-show="firmIndex === ind" class="el-icon-check"></i>
          </span>
          <span class="clinic-name">{{ i.name }}</span>
        </li>
      </ul>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">Hủy</el-button>
        <el-button type="primary" @click="confirmLogin()">Xác nhận</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getToken } from "@/api/auth";
import BaseUI from "@/views/components/baseUI";
import { listSysSetingAll, getFirmList } from "@/api/sys/sysSeting.js";
import {
  setLocalToken,
  setLocalCurrentUser,
  setLocalCompanys,
  getLocalRouters,
  setLocalRouters,
  setLocalDataPermisions,
  setLocalSysSetting,
  setLocalCurrentCompany,
  setLocalPersonalTheme,
} from "@/utils/auth";
import { routerTree, handleFamily } from "@/utils/routerTree";

import backImg from "@/assets/images/Logbk.png";

const _import = require("@/router/_import_" + process.env.NODE_ENV);

export default {
  extends: BaseUI,
  data() {
    return {
      backImg:backImg,
      dialogVisible: false,
      firmList: [],
      firmIndex: 0,
      isLoaded: false,
      baseApi: process.env.BASE_API,
      displayLoading: false,
      loginForm: {
        // username: process.env.NODE_ENV == "development" ? "15827280002" : "", //system  1824422426
        // password: process.env.NODE_ENV == "development" ? "280002" : "",
        username: process.env.NODE_ENV === "development" ? "xzadmin" : "", //system  1824422426
        password: process.env.NODE_ENV === "development" ? "xzadmin" : "",
      },
      rules: {
        username: [
          { required: true, message: "Vui lòng nhập tên đăng nhập", trigger: "blur" },
        ],
        password: [{ required: true, message: "Vui lòng nhập mật khẩu", trigger: "blur" }],
      },
      loginBg: require("../../assets/images/Logbk.png"),
      loginSubBg: require("../../assets/images/logsubbg.jpg"),
      sysData: {
        sysName: "", // Tên hệ thống
        sysAbbrname: "", // Tên viết tắt hệ thống
        loginBgcolor: "", // Màu nền trang đăng nhập
        loginLayout: "simple-center",
        projectLogo: "", // Hạng mụclogo
        loginLogo: "", // Hộp đăng nhậplogo
        sysLogo: "", // Trang chủ hệ thốnglogo
        favicon: "", // Đầu trang web favicon Biểu tượng
        loginBg: "", // Ảnh nền trang đăng nhập
        loginGraph: "", // Ảnh minh họa trang đăng nhập
      },
      isError: false,
      loginError: "",
    };
  },
  components: {},
  computed: {
    ...Vuex.mapGetters(["showLoading", "settings"]),
    styleObject() {
      return {
        "--active-color": this.settings.theme,
      };
    },
  },
  methods: {
    ...Vuex.mapActions({
      changeSetting: "settings/changeSetting",
    }),

    getSysSetting() {
      let parmas = {};
      listSysSetingAll(parmas)
        .then((response) => {
          if (response.code === 100) {
            let result = response.data[0];
            this.sysData.sysName = result.sysName;
            this.sysData.sysAbbrname = result.sysAbbrname;
            this.sysData.loginBgcolor = result.loginBgcolor;
            this.sysData.loginLayout = result.loginLayout;
            this.sysData.projectLogo = `${this.baseApi}/sys/sysSeting/getFile/1`;
            this.sysData.loginLogo = `${this.baseApi}/sys/sysSeting/getFile/2`;
            this.sysData.sysLogo = `${this.baseApi}/sys/sysSeting/getFile/3`;
            // this.sysData.favicon = `${this.baseApi}/sys/sysSeting/getFile/4`;
            //this.sysData.loginBg = `${this.baseApi}/sys/sysSeting/getFile/5`;
            this.sysData.loginGraph = `${this.baseApi}/sys/sysSeting/getFile/6`;



            setLocalSysSetting(this.sysData);

            // this.changeFavicon(this.sysData.favicon);
            this.changeWebTitle(this.sysData.sysAbbrname);
          }
          this.isLoaded = true;
        })
        .catch(() => {
          this.isLoaded = true;
        });
    },
    changeFavicon(icon) {
      let link =
        document.querySelector("link[rel*='Icon']") ||
        document.createElement("link");
      link.type = "image/x-icon";
      link.rel = "Shortcut Icon";
      link.href = icon;
      document.getElementsByTagName("head")[0].appendChild(link);
    },
    changeWebTitle(title) {
      document.title = title;
    },
    login(id) {
      this.$store.dispatch("setLoading", true);
      getToken(this.loginForm.username, this.loginForm.password, id)
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
    confirmLogin() {
      this.dialogVisible = false;
      this.login(this.firmList[this.firmIndex].id);
    },
    async doLogin() {
      this.$refs["loginForm"].validate(async (valid) => {
        if (valid) {
          try {
            const res = await getFirmList(this.loginForm.username, this.loginForm.password);
            // Kiểm tra xem có trả về phản hồi lỗi không
            if (res && res.code && res.code !== 100) {
              this.isError = true;
              this.loginError = res.msg || "Đăng nhập thất bại";
              return;
            }
            if (!res || !Array.isArray(res) || res.length === 0) {
              // Khi danh sách phòng khám rỗng (ví dụ siêu quản trị viên), thử đăng nhập trực tiếp
              try {
                const tokenRes = await getToken(this.loginForm.username, this.loginForm.password, "")
                if (tokenRes && tokenRes.code === 100) {
                  this.handleLoginInfo(tokenRes.data)
                  return
                }
              } catch (e) {
                // fall through to error
              }
              this.isError = true
              this.loginError = "Không tìm thấy phòng khám liên kết"
              return
            }
            setLocalCompanys(res);
            this.firmList = res;
            if (res.length > 1) {
              this.dialogVisible = true;
            } else {
              this.login(res[0] ? res[0].id : "");
            }
          } catch (error) {
            this.isError = true;
            this.loginError = error.msg || "Đăng nhập thất bại, vui lòng kiểm tra kết nối mạng";
          }
        }
      });
    },
    initIndexRouter() {
      if (sessionStorage.getItem('indexRouterGenerated') === 'true') {
        return
      }
      let childRouters = this.generateChildRouters();
      let indexRouter = {
        path: "/",
        name: "/",
        component: _import("home/index"),
        children: [...childRouters],
      };
      // this.$router.addRoutes([indexRouter])
      this.$router.addRoute("/", indexRouter);
      sessionStorage.setItem('indexRouterGenerated', 'true')
    },
    generateChildRouters() {
      let routers = getLocalRouters();
      let routerTreeArr = routerTree(routers);
      if (!routers) {
        return [];
      }

      // Đụng độ path không phân biệt hoa/thường (vd 'myTask' vs 'mytask'):
      // vue-router mặc định match không phân biệt hoa/thường nên route đăng
      // ký trước thắng và trang thật không bao giờ render. Chỉ các route có
      // path đụng độ mới bật caseSensitive (cần cả 2 phía).
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
      try {
        window.sessionStorage.setItem("User_P", this.loginForm.password);
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

        const personalTheme = userData.personalTheme || {};
        try {
          personalTheme.theme = typeof personalTheme.theme === 'string' ? JSON.parse(personalTheme.theme) : (personalTheme.theme || {});
        } catch (e) {
          personalTheme.theme = {};
          }
        setLocalPersonalTheme(personalTheme);
        this.changeTheme(personalTheme.theme);

        // Khởi tạo router trang chủ
        this.initIndexRouter();

        const routers = userData.routers; // Router của người dùng hiện tại
        if (this.$route.query.redirect) {
          // Kiểm tra có chuyển hướng không
          const url = this.removeBlock(
            JSON.stringify({ url: this.$route.query.redirect.substring(1) })
          );
          if (routers.includes(url)) {
            // Kiểm tra router có chứa đường dẫn chuyển hướng, kiểm tra có quyền không
            this.$router.push(this.$route.query.redirect);
          }
        }
      } catch (e) {
        console.error("Lỗi xử lý thông tin đăng nhập:", e);
      }
      // Chuyển đến trang chủ
      this.$router.push("/").catch(err => {
        console.error("Chuyển hướng router thất bại:", err);
      });
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
  },
  created: function () {
    this.getSysSetting();
  },
};
</script>

<style scoped>
/* Hộp thoại chọn phòng khám khi tài khoản liên kết nhiều cơ sở */
.til {
  margin: 0 0 14px;
  font-size: 13px;
  line-height: 1.55;
  color: #5a6b7d;
}

.clinic-list {
  margin: 0;
  padding: 0;
  list-style: none;
  max-height: 280px;
  overflow-y: auto;

  li {
    display: flex;
    align-items: center;
    padding: 11px 14px;
    margin-bottom: 8px;
    border: 1px solid #dfe6ee;
    border-radius: 8px;
    cursor: pointer;
    transition: border-color 0.18s, background-color 0.18s;

    &:last-child {
      margin-bottom: 0;
    }

    &:hover {
      border-color: #a8cfe0;
      background-color: #f7fbfd;
    }

    &.is-selected {
      border-color: #018cb7;
      background-color: #eef8fc;
    }
  }

  .clinic-radio {
    display: flex;
    align-items: center;
    justify-content: center;
    flex: 0 0 auto;
    width: 18px;
    height: 18px;
    margin-right: 12px;
    border: 1px solid #c3cfdb;
    border-radius: 50%;
    font-size: 11px;
    color: #fff;
    background-color: #fff;
    transition: all 0.18s;
  }

  li.is-selected .clinic-radio {
    border-color: #018cb7;
    background-color: #018cb7;
    }

  .clinic-name {
    font-size: 14px;
    line-height: 1.4;
    color: #1f2d3d;
    word-break: break-word;
  }
}
</style>
<style rel="stylesheet/scss" lang="scss" scoped>
// ===========================================================================
// Trang đăng nhập — bố cục responsive theo chuẩn hiện đại.
// Thay thế layout cũ dùng min-width: 1000px + margin-top âm (vỡ trên laptop
// 1366x768 và không dùng được trên tablet).
// ===========================================================================
$brand: #018cb7;
$brand-dark: #013c5a;

.login-container {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  min-height: 100vh;
  padding: 32px 16px 80px;
  box-sizing: border-box;
  background-color: #eef4f8;
  background-repeat: no-repeat;
  background-position: center;
  background-size: cover;
  overflow-x: hidden;
  z-index: 1001;

  // Lớp phủ giúp nội dung luôn đủ tương phản trên ảnh nền tuỳ chỉnh
  &::before {
    content: "";
    position: absolute;
    top: 0;
    right: 0;
    bottom: 0;
    left: 0;
    background: linear-gradient(
      135deg,
      rgba(1, 140, 183, 0.62) 0%,
      rgba(1, 60, 90, 0.74) 100%
    );
    z-index: 0;
  }

  // Header: logo + tên hệ thống, nằm trên thẻ đăng nhập
  .login-header {
    position: relative;
    z-index: 1;
    display: flex;
    align-items: center;
    width: 100%;
    max-width: 420px;
    height: auto;
    margin: 0 0 18px;
    padding: 0;
    box-sizing: border-box;

    .login-logo {
      display: flex;
      align-items: center;
      height: auto;
      margin-right: 12px;
      float: none;

      img {
        display: block;
        height: 34px;
        width: auto;
        max-width: 140px;
        object-fit: contain;
      }
    }

    .login-title {
      position: static;
      float: none;
      padding: 0;
      font-size: 20px;
      font-weight: 600;
      line-height: 1.3;
      color: #fff;
      text-shadow: 0 1px 2px rgba(0, 0, 0, 0.18);
    }
  }

  // Thẻ đăng nhập
  .login-form {
    position: relative;
    z-index: 1;
    width: 100%;
    max-width: 420px;
    margin: 0;
    background-color: #fff;
    border-radius: 14px;
    box-shadow: 0 18px 48px rgba(1, 60, 90, 0.2);
    overflow: hidden;
  }

  .form {
    width: 100%;
  }

  .login-body {
    padding: 32px 28px 30px;
    text-align: left;

    .form-title {
      margin: 0 0 6px;
      font-size: 22px;
      font-weight: 600;
      line-height: 1.35;
      color: #1f2d3d;
      text-align: left;
    }

    .form-subtitle {
      margin: 0 0 22px;
      font-size: 13px;
      line-height: 1.5;
      color: #7a8699;
    }

    .login-error {
      margin-bottom: 16px;
      border-radius: 6px;
    }

    .field-label {
      display: block;
      margin-bottom: 6px;
      font-size: 13px;
      font-weight: 500;
      color: #48576a;
    }

    .el-form-item {
      margin-bottom: 18px;
    }

    .login-username,
    .login-password {
      width: 20px;
      height: 20px;
      background-repeat: no-repeat;
      background-position: center;
      background-size: 16px;
        }

    .login-username {
      background-image: url("../../assets/images/user.png");
    }

    .login-password {
      background-image: url("../../assets/images/lock.png");
        }

    .login-submit {
      width: 100%;
      height: 44px;
      margin-top: 4px;
      font-size: 15px;
      font-weight: 500;
      letter-spacing: 0.2px;
      border-radius: 8px;
    }
  }

  .el-input {
    width: 100%;

    ::v-deep .el-input__inner {
      height: 44px;
      line-height: 44px;
      font-size: 14px;
      border-radius: 8px;
    }

    ::v-deep .el-input__prefix {
      left: 12px;
      display: flex;
      align-items: center;
      font-size: 16px;
      color: #9aa8b8;
    }

    ::v-deep .el-input__inner:not([type="password"]) {
      padding-left: 36px;
    }
  }

  // Ảnh minh hoạ: chỉ hiển thị ở biến thể 2 cột trên màn hình lớn
    .graph {
    display: none;
    background-repeat: no-repeat;
    background-position: center;
    background-size: cover;
  }

  // Footer bản quyền / giấy phép
  .footer {
    position: absolute;
    right: 0;
    bottom: 16px;
    left: 0;
    z-index: 1;
    padding: 0 16px;
    font-size: 12px;
    line-height: 1.6;
    text-align: center;
    color: rgba(255, 255, 255, 0.86);

    .footer-copyright {
      color: rgba(255, 255, 255, 0.9);
    }

    // Tên pháp lý gốc: hiển thị mờ hơn để không phá bố cục tiếng Việt
    .footer-legal {
      color: rgba(255, 255, 255, 0.55);
      font-size: 11px;
        }

    .footer-license {
      margin-top: 2px;
    }

    a {
      color: rgba(255, 255, 255, 0.94);
      text-decoration: underline;
      transition: opacity 0.2s;

      &:hover {
        opacity: 0.75;
      }
        }
      }
    }

// ---------------------------------------------------------------------------
// Biến thể bố cục 2 cột (left-graph / right-graph) trên màn hình >= 1024px
// ---------------------------------------------------------------------------
@mixin two-column($form-order: 1, $graph-order: 2, $graph-radius: 0 16px 16px 0) {
  @media (min-width: 1024px) {
    flex-direction: row;
    align-items: stretch;
    justify-content: center;
    padding: 0;

    .login-header {
      position: absolute;
      top: 32px;
      left: 48px;
      max-width: none;
      width: auto;
      margin: 0;
    }

    .login-form {
      display: flex;
      max-width: 960px;
      min-height: 520px;
      margin: 0;
      border-radius: 16px;
      align-items: stretch;
    }

    .form {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 46%;
      flex: 0 0 46%;
      order: $form-order;
    }

    .login-body {
      width: 100%;
      padding: 40px 36px;
    }

    .graph {
      display: block;
      flex: 1 1 auto;
      min-height: 100%;
      order: $graph-order;
      border-radius: $graph-radius;
    }
  }
}

.left-graph {
  @include two-column(1, 2, 16px 0 0 16px);
}

.right-graph {
  @include two-column(2, 1, 0 16px 16px 0);
}

// ---------------------------------------------------------------------------
// Màn hình nhỏ: thu gọn khoảng cách, footer chuyển về luồng bình thường
// ---------------------------------------------------------------------------
@media (max-width: 480px) {
  .login-container {
    padding: 20px 12px 24px;
    justify-content: flex-start;

    .login-header {
      margin-top: 8px;
      margin-bottom: 14px;

      .login-title {
        font-size: 17px;
      }
    }

    .login-body {
      padding: 24px 20px;
    }

    .footer {
      position: static;
      margin-top: 20px;
      font-size: 11px;
    }
  }
}

// Tablet nằm ngang thấp: tránh cắt nội dung
@media (max-height: 640px) and (min-width: 481px) {
  .login-container {
    justify-content: flex-start;
    padding-top: 24px;
    padding-bottom: 64px;
  }
}
</style>
