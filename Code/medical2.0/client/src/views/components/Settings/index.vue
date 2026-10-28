<template>
  <div class="drawer-container" v-loading="drawerLoading" element-loading-text="Đang cập nhật chủ đề">
    <div>
      <h3 class="drawer-title">Phong cách chủ đề</h3>
      <div class="drawer-item">
        <el-row class="drawer-style" :gutter="10">
          <el-col :span="6" v-for="(item,index) in styles" :key="index">
            <span
              ref="drawerStyle"
              :style="styleObject"
              :class="currentStyle === item.code? 'is-active' : ''"
              @click="handleSetting('style', item.code)">
              <i class="el-icon-check"></i>
            <img :src="item.icon" alt="">
          </span>
          </el-col>
        </el-row>
      </div>
      <div class="drawer-item">
        <span>Chủ đề hệ thống</span>
        <el-select style="width: 170px" v-model="currentTheme" size="mini" placeholder="Chọn chủ đề" @change="(val) => {handleThemeChange(val)}">
          <el-option
            v-for="(item, index) in sysThemeList"
            :key="index"
            :label="item.name"
            :value="item.id">
          </el-option>
        </el-select>
      </div>
      <h3 class="drawer-title">Tùy chỉnh</h3>
      <div class="drawer-item">
        <span>Thanh điều hướng</span>
        <el-color-picker color-format="rgb" v-model="currentHeaderColor" :predefine="predefine" class="theme-picker color-picker" popper-class="theme-picker-dropdown"/>
      </div>
      <div class="drawer-item">
        <span>Thanh bên</span>
        <el-color-picker color-format="rgb" v-model="currentSidebarColor" :predefine="predefine" class="theme-picker color-picker" popper-class="theme-picker-dropdown"/>
      </div>
      <div class="drawer-item">
        <span>Màu nền（Nên dùng tông màu nhạt）</span>
        <el-color-picker color-format="rgb" v-model="currentBackgroundColor" :predefine="predefine" class="theme-picker color-picker" popper-class="theme-picker-dropdown"/>
      </div>
      <div class="drawer-item">
        <span>Màu chủ đề</span>
        <theme-picker ref="themePicker" class="color-picker" />
      </div>
      <div class="drawer-item">
        <span>Kích thước phần tử</span>
        <div class="drawer-radio">
          <el-radio-group v-model="currentSize" size="mini" @change="((size) => {handleSetSize('size', size)})">
            <el-radio-button v-for="(item, index) in sizes" :label="item.size" :key="index">{{item.name}}</el-radio-button>
          </el-radio-group>
        </div>
      </div>
      <div class="drawer-item">
        <span>Có cố định đầu trang không</span>
        <el-switch v-model="isFixedHeader" @change="handleSetting('fixedHeader', isFixedHeader)" class="drawer-switch" />
      </div>
      <div class="drawer-item">
        <span>Có hiển thị khônglogo</span>
        <el-switch v-model="isShowLogo" class="drawer-switch" @change="handleSetting('showLogo', isShowLogo)"/>
      </div>

      <div class="drawer-footer" style="text-align: center; padding: 10px 0">
        <el-button size="mini" type="primary" plain icon="el-icon-document-checked" @click="handleLayoutSave">Lưu cài đặt</el-button>
        <el-button v-show='isCustomerTheme()' size="mini" plain icon="el-icon-refresh-right" @click="handleLayoutReset">Đặt lại cài đặt</el-button>
      </div>

      <div v-if="loginId === '1001' || loginId==='1000'" style="text-align: center; margin-top: 15px" >
        <el-button  size="medium"  type="primary" plain @click="saveTheTheme">Lưu thành chủ đề</el-button>
      </div>
    </div>

  </div>
</template>

<script>
  import ThemePicker from '@/components/ThemePicker'

  import { listThemeAll } from '@/api/sys/theme'
  import { saveTheme } from '@/api/sys/theme'
  import { getLocalCurrentUser, getLocalPersonalTheme, setLocalPersonalTheme } from '@/utils/auth'
  export default {
		name: "Setting",
    components: {
      ThemePicker,
    },
    data() {
      return {
        isFirstOpen: true,  // Mã tệp lần đầu
        drawerLoading: false,
        search: {
          params: [],
          offset: 0,
          limit: 10,
          columnName: '',       // Tên trường sắp xếp
          order: ''             // Sắp xếp
        },
        sysThemeList: [],
        isThemeDefault: false,
        currentTheme: '',
        predefine: ['#018cb7','#409EFF', '#1890ff', '#2f4050','#212121','#11a983', '#13c2c2', '#6959CD'],
        styles: [
          {code: 'up-down', icon: require('../../../assets/images/style_up_down.png')},
          //{code: 'left-right', icon: require('../../../assets/images/style_left_right.png')},
        ],
        sizes: [
          {size: 'default', name: 'Lớn'},
          {size: 'medium', name: 'Vừa'},
          {size: 'small', name: 'Nhỏ'},
          {size: 'mini', name: 'Mini'}
        ],
        loginId:''
      }
    },
    watch: {

    },
    computed: {
      ...Vuex.mapGetters(["settings"]),
      styleObject() {
        return {
          '--border-active-color': this.settings.theme
        }
      },

      currentStyle: {
        get() {
          return this.settings.style
        },
        set(val) {}
      },

      currentHeaderColor: {
        get() {
          return this.settings.headerColor
        },
        set(val) {
          this.changeSetting({ key: 'headerColor', value: val });
        }
      },

      currentSidebarColor: {
        get() {
          return this.settings.sidebarColor
        },
        set(val) {
          this.changeSetting({ key: 'sidebarColor', value: val });
        }
      },

      currentBackgroundColor: {
        get() {
          return this.settings.backgroundColor
        },
        set(val) {
          this.changeSetting({ key: 'backgroundColor', value: val });
        }
      },

      currentSize: {
        get() {
          return this.settings.size
        },
        set(val) {
          this.changeSetting({ key: 'size', value: val });
        }
      },

      isFixedHeader: {
        get() {
          return this.settings.fixedHeader
        },
        set(val) {
          this.changeSetting({ key: 'fixedHeader', value: val });
        }
      },

      isShowLogo: {
        get() {
          return this.settings.showLogo
        },
        set(val) {
          this.changeSetting({ key: 'showLogo', value: val });
        }
      }
    },
    methods: {
		  ...Vuex.mapActions({
        changeSetting: 'settings/changeSetting',
        saveSetting: 'settings/saveSetting',
        resetSetting: 'settings/resetSetting',
        showPanel: 'settings/showPanel',
      }),
      //Theoid来判断保存主题得按钮是否显示
      getLoginId(){
          this.loginId = getLocalCurrentUser().id;
      },
      isCustomerTheme() {
        let id = getLocalPersonalTheme().id
        return id ? true : false
      },
      getThemeList() {
		    this.drawerLoading = true;
        listThemeAll(this.search).then(responseData => {
          if(responseData.code === 100) {
            this.drawerLoading = false;
            const result = responseData.data;
            // 引用类型 存放在堆内存中
            const obj = result.find(item => {
              return item.isDefault === '1'
            })
            obj.name = obj.name + '（系统默认）'
            this.sysThemeList = result
            const personalTheme = getLocalPersonalTheme()
            personalTheme.theme = JSON.stringify(personalTheme.theme);
            if (personalTheme.id) {
              personalTheme.name = 'Tùy chỉnh'
              this.sysThemeList.unshift(personalTheme)
              this.currentTheme = personalTheme.id
            } else {
              this.currentTheme = obj.id
            }
            this.handleThemeChange(this.currentTheme)
          }
        }).catch(error => {
          this.drawerLoading = false
        })
      },

      handleThemeChange(val) {
        let obj = {};
        obj = this.sysThemeList.find((item) => {
          return item.id === val
        });
        let theme = JSON.parse(obj.theme)
        this.changeTheme(theme)
        this.$refs.themePicker.themeVal = theme.theme
      },

      changeTheme(theme) {
        const settings = [
          'theme',
          'headerColor',
          'sidebarColor',
          'backgroundColor',
          'size',
          'style',
          'showTagsView',
          'fixedHeader',
          'showLogo'
        ];
        this.$ELEMENT.size = theme.size;
        settings.forEach(item => {
          this.handleSetting(item, theme[item])
        })
      },

      handleSetSize(key, value) {
        this.$ELEMENT.size = value
        this.handleSetting(key, value)
        this.refreshView()
      },

      handleSetting(key, value) {
        this.changeSetting({ key, value })
      },

      refreshView() {
        this.$router.replace({
          path: '/redirect',
          query: {
            t: Date.now()
          }
        })
      },

      async handleLayoutSave() {
        this.drawerLoading = true;
        const personalTheme = getLocalPersonalTheme()
        personalTheme.theme = JSON.stringify(this.settings)
        const result = await this.saveSetting(personalTheme)
        if (result && result.code === 100) {
          personalTheme.id = result.data
          personalTheme.theme = this.settings
          setLocalPersonalTheme(personalTheme)
          this.$message({
            type: 'success',
            message: 'Lưu thành công'
          })

          this.getThemeList()
          this.drawerLoading = false
        } else {
          this.$message({
            type: 'error',
            message: 'Xảy ra lỗi'
          })
          this.drawerLoading = false
        }
      },
      async handleLayoutReset() {
        this.layoutLoading();
        const result = await this.resetSetting();
        if (result && result.code === 100) {
          const obj = this.sysThemeList.find(item => {
              return item.isDefault === '1'
          })
          setLocalPersonalTheme({
            userId: getLocalCurrentUser().id,
            theme: JSON.parse(obj.theme)
          })

          this.$message({
            type: 'success',
            message: '已重置'
          })
          this.layoutLoading().close()
          this.getThemeList()
        } else {
          this.$message({
            type: 'error',
            message: 'Xảy ra lỗi'
          })
          this.layoutLoading().close()
        }
      },
      //保存主题
      saveTheTheme() {
        const data={
            "theme":this.settings.theme,
            "headerColor":this.settings.headerColor,
            "sidebarColor":this.settings.sidebarColor,
            "backgroundColor":this.settings.backgroundColor,
            "style":this.settings.style,
            "size":this.settings.size,
            "fixedHeader":this.settings.fixedHeader,
            "showLogo":this.settings.showLogo,
            "showTagsView":this.settings.showTagsView,
            "datetime":new Date().getTime()
        }
          this.$prompt('Vui lòng nhập tên chủ đề', 'Nhắc nhở', {
              confirmButtonText: 'Xác nhận',
              cancelButtonText: 'Hủy',
              inputPattern: /^(a-z|A-Z|0-9)*[^$%^&*;:,<>?()\""\']{1,128}$/,
              inputErrorMessage: 'Vui lòng nhập tên chủ đề'
          }).then(({ value }) => {
              const dateTheme={
                  name:value,
                  theme:JSON.stringify(data),
              }
              saveTheme(dateTheme).then(responseData => {
                  if(responseData.code === 100){
                      this.$message({
                          type: 'success',
                          message: 'Lưu thành công'
                      });
                      this.showPanel(false);
                  }else {
                      this.$message({
                          type: 'error',
                          message: `${responseData.msg}`
                      });
                  }
              })

          }).catch(() => { });
      },
      layoutLoading() {
        return this.$loading({
          lock: true,
          text: '处理中...',
          spinner: 'el-icon-loading',
          background: 'rgba(0, 0, 0, 0.7)'
        });
      },
      openLayout() {
        if(this.isFirstOpen) {
          this.isFirstOpen = false
          this.getLoginId()
          this.getThemeList()
        }
      }
    }
	}
</script>

<style lang="scss" scoped>
  .drawer-container {
    padding: 24px;
    font-size: 14px;
    line-height: 1.5;
    word-wrap: break-word;

    .drawer-title {
      margin: 0;
      padding: 12px 0;
      color: rgba(0, 0, 0, .85);
      font-size: 14px;
      line-height: 22px;
      &:first-child {
        border-top: none;
      }
      border-top: 1px solid #dbdbdb;
    }

    .drawer-item {
      width: 100%;
      color: rgba(0, 0, 0, .65);
      font-size: 14px;
      padding: 12px 0;
      display: flex;
      align-content: center;
      justify-content: space-between;
      align-items: center;
      .drawer-theme {
        width: 100%;
        > div {
          width: 30%;
          float: left;
          .sys-theme {
            text-align: center;
            p {
              margin: 0;
            }
            border: 1px solid #ffffff;
          }
        }
        .sys-theme {
          span {
            display: inline-block;
            width: 30px;
            height: 30px;
            background-color: #018cb7;
          }
          p {
            font-size: 12px;
            text-align: center;
          }
        }
      }
      .drawer-radio {
        float: right;
      }
      .drawer-style {
        width: 100%;
        span {
          display: inline-block;
          width: 50px;
          height: 50px;
          border: 1px solid #ffffff;
          cursor: pointer;
          transition: all .3s;
          border-radius: 6px;
          position: relative;
          -webkit-box-shadow: inset 0 1px 1px rgba(0, 0, 0, .055), 0 0 2px rgba(0, 0, 0, .3);
          -moz-box-shadow: inset 0 1px 1px rgba(0, 0, 0, .055), 0 0 2px rgba(0, 0, 0, .3);
          box-shadow: inset 0 1px 1px rgba(0, 0, 0, .055), 0 0 2px rgba(0, 0, 0, .3);
          img {
            display: inline-block;
            width: 100%;
          }
          i {
            position: absolute;
            font-size: 20px;
            left: 50%;
            margin-left: -10px;
            top: 50%;
            margin-top: -10px;
            display: none;
            font-weight: bold;
          }
          &.is-active {
            box-shadow: inset 0 1px 1px rgba(0, 0, 0, .055), 0 0 2px var(--border-active-color);
            border: 1px solid var(--border-active-color);
            i {
              display: block;
              color: var(--border-active-color);
            }
          }
        }
      }
      .drawer-switch {
        float: right
      }

      .color-picker {
        float: right;
        height: 26px;
        margin: -3px 8px 0 0;
      }
    }
  }
</style>d
<style lang="scss">
  .drawer-radio {
    .el-radio-button__inner {
      padding: 7px 8px!important;
    }
  }
</style>
