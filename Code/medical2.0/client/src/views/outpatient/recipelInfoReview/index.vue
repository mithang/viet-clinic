<template>
  <el-row v-loading='loading'>
    <div class="page-container">
      <!--Tab   Bắt đầu-->
      <el-tabs v-model="activeName" @tab-click="onHandleClickTabs">
        <el-tab-pane label="Rà soát thường quy" name="first">
          <review></review>
        </el-tab-pane>
        <el-tab-pane label="Lấy ngẫu nhiên" name="second">
          <random-review></random-review>
        </el-tab-pane>
      </el-tabs>
    </div>
  </el-row>
</template>

<script>
import Review from "./components/review"
import RandomReview from "./components/randomReview"
import listViewMixin from '@/mixins/listViewMixin'
import MainUI from '@/views/components/mainUI'
export default {
  extends: MainUI,
  mixins: [listViewMixin],
  components: {
    Review,
    RandomReview
  },
  data() {
    return {
      activeName: 'first'
    }
  }
}
</script>
