<template>
  <div style="height: 100%; background-color: rgb(247, 248, 250)">
    <Preview
      v-if="reportType === 1"
      :key="embeddedKey"
      :is-click-component="isClickComponent"
      :component-data="componentData"
      :canvas-style-data="canvasStyleData"
      :now-panel-track-info="nowPanelTrackInfo"
      :now-panel-jump-info="nowPanelJumpInfo"
      @EmbendChartClick='onEmbendChartClick'></Preview>

    <PanelViewShow
      v-if="reportType === 2"
      :is-click-component="isClickComponent"
      :component-data="componentData"
      :canvas-style-data="canvasStyleData"
      :panelInfos="panelInfo"
      :now-panel-track-info="nowPanelTrackInfo"
      :now-panel-jump-info="nowPanelJumpInfo"
      :user="user"></PanelViewShow>

    <PreviewEject
      v-if="reportType === 3"
      :is-click-component="isClickComponent"
      :component-data="componentData"
      :canvas-style-data="canvasStyleData"
      :now-panel-track-info="nowPanelTrackInfo"
      :now-panel-jump-info="nowPanelJumpInfo"></PreviewEject>
  </div>
</template>

<script>
  import { get } from '@/api/report/panel/panel'
  import { getPanelAllLinkageInfo } from '@/api/report/panel/linkage'
  import { queryPanelJumpInfo } from '@/api/report/panel/linkJump'
  import { uuid } from 'vue-uuid'

  import bus from '@/views/report/utils/bus'
  // 注入报表组件
  import store from '@/store'
  import { UxGrid } from 'umy-ui'
  Vue.use(window['vue-dataviewer']).use(UxGrid)

  const reportMixin = window['vue-dataviewer'].reportMixin

  export default {
    name: "Report",
    mixins: [reportMixin],
    props: {
      // 报表显示模式 1 嵌入式模式； 2 浏览报表模式； 3 浏览器弹出页面模式
      reportType: {
        type: String | Number,
        required: true
      },
      // Chỉ có hiệu lực trong báo cáo nhúng
      dashboardId: {
        type: String,
        default: () => {
          return null
        }
      }
    },
    data() {
      return {
        isComponentDataDone: false,
        embeddedKey: 0
      }
    },
    watch: {

    },
    mounted() {
      this.$nextTick(() => {
        // 获取看板 Chỉ có hiệu lực trong báo cáo nhúng
        if(this.dashboardId) {
          this.getDashboard(this.dashboardId)
        }

        // của đối tượng trong thành phần mixin addViewFilter lấy phương thức ra và thực hiện
        this.$on('addEmbeddedViewFilter', function(param) {
          // this.addViewFilter(param)
          this.addViewFilterFun(param)
        })
        // của đối tượng trong thành phần mixin removeViewFilter lấy phương thức ra và thực hiện
        this.$on('removeEmbeddedViewFilter', function(componentId) {
          // this.removeViewFilter(componentId)
          this.removeViewFilterFun(componentId)
        })

        // tại PanelViewShow Sử dụng
        bus.$on('setComponentData', (data) => {
          this.setComponentData(data)
        })
        bus.$on('setCanvasStyle', (data) => {
          this.setCanvasStyle(data)
        })
        bus.$on('setPanelInfo', (data) => {
          this.setPanelInfo(data)
        })
        bus.$on('setNowPanelTrackInfo', (data) => {
          this.setNowPanelTrackInfo(data)
        })
        bus.$on('setNowPanelJumpInfo', (data) => {
          this.setNowPanelJumpInfo(data)
        })
      })
    },
    methods: {
      onEmbendChartClick(param) {
        this.$emit('EmbendChartClick', param)
      },
      addViewFilterFun(param) {
        if (this.isComponentDataDone) {
          this.addViewFilter(param)
        } else {
          setTimeout(() => {
            // 为解决默认初始值有时不准确
            this.embeddedKey++
            this.addViewFilterFun(param)
          }, 50)
        }
      },
      removeViewFilterFun(id) {
        if (this.isComponentDataDone) {
          this.removeViewFilter(id)
        } else {
          setTimeout(() => {
            this.removeViewFilterFun(id)
          }, 50)
        }
      },
      getDashboard(dashboardId) {
        get('panel/group/findOne/' + dashboardId).then(response => {
          if (response.data) {
            this.setComponentData(this.resetID(JSON.parse(response.data.panelData)))
            this.setCanvasStyle(JSON.parse(response.data.panelStyle))
          } else {
            this.setComponentData([])
            this.setCanvasStyle([])
          }
          this.isComponentDataDone = true
          const data = {
            id: dashboardId,
          }
          // this.$store.dispatch('panel/setPanelInfo', data)
          this.setPanelInfo(data)
          // Làm mới thông tin liên kết
          getPanelAllLinkageInfo(dashboardId).then(rsp => {
            // this.$store.commit('report/setNowPanelTrackInfo', rsp.data)
            this.setNowPanelTrackInfo(rsp.data)
          })

          // Làm mới thông tin chuyển hướng
          queryPanelJumpInfo(dashboardId).then(rsp => {
            // this.$store.commit('report/setNowPanelJumpInfo', rsp.data)
            this.setNowPanelJumpInfo(rsp.data)
          })

        })
      },
      resetID(data) {
        if (data) {
          data.forEach(item => {
            item.type !== 'custom' && (item.id = uuid.v1())
          })
        }
        return data
      },
    }
  }
</script>
