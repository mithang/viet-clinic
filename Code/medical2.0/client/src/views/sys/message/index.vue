<template>
    <div class="app-container">
        <div class="crumbs">
            <el-breadcrumb separator="/">
                <el-breadcrumb-item><i class="el-icon-message"></i> tabThẻ tab</el-breadcrumb-item>
            </el-breadcrumb>
        </div>
        <div class="container">
            <el-tabs v-model="message">
                <el-tab-pane :label="`Tin nhắn chưa đọc(${unread.length})`" name="first">
                    <el-table :data="unread" :show-header="false" style="width: 100%">
                        <el-table-column>
                            <template slot-scope="scope">
                                <span class="message-title">{{scope.row.title}}</span>
                            </template>
                        </el-table-column>
                        <el-table-column prop="date" width="180"></el-table-column>
                        <el-table-column width="120">
                            <template slot-scope="scope">
                                <el-button @click="handleRead(scope.$index)">Đánh dấu đã đọc</el-button>
                            </template>
                        </el-table-column>
                    </el-table>
                    <div class="handle-row">
                        <el-button type="primary">Đánh dấu tất cả đã đọc</el-button>
                    </div>
                </el-tab-pane>
                <el-tab-pane :label="`Tin đã đọc (${read.length})`" name="second">
                    <template v-if="message === 'second'">
                        <el-table :data="read" :show-header="false" style="width: 100%">
                            <el-table-column>
                                <template slot-scope="scope">
                                    <span class="message-title">{{scope.row.title}}</span>
                                </template>
                            </el-table-column>
                            <el-table-column prop="date" width="150"></el-table-column>
                            <el-table-column width="120">
                                <template slot-scope="scope">
                                    <el-button type="danger" @click="handleDel(scope.$index)">Xóa</el-button>
                                </template>
                            </el-table-column>
                        </el-table>
                        <div class="handle-row">
                            <el-button type="danger">Xóa tất cả</el-button>
                        </div>
                    </template>
                </el-tab-pane>
                <el-tab-pane :label="`Thùng rác(${recycle.length})`" name="third">
                    <template v-if="message === 'third'">
                        <el-table :data="recycle" :show-header="false" style="width: 100%">
                            <el-table-column>
                                <template slot-scope="scope">
                                    <span class="message-title">{{scope.row.title}}</span>
                                </template>
                            </el-table-column>
                            <el-table-column prop="date" width="150"></el-table-column>
                            <el-table-column width="120">
                                <template slot-scope="scope">
                                    <el-button @click="handleRestore(scope.$index)">Khôi phục</el-button>
                                </template>
                            </el-table-column>
                        </el-table>
                        <div class="handle-row">
                            <el-button type="danger">Dọn sạch thùng rác</el-button>
                        </div>
                    </template>
                </el-tab-pane>
            </el-tabs>
        </div>
    </div>
</template>

<script>
    export default {
      name: 'tabs',
      data() {
        return {
          message: 'first',
          showHeader: false,
          unread: [
          {
            date: '2019-11-7 8:00:00',
            title: 'Thông báo là tin do hệ thống hoặc đơn vị phát hành, thường không thể trả lời'
          },
          {
            date: '2019-11-7 9:00:00',
            title: 'Tin nhắn bao gồm tin riêng, bình luận, theo dõi, lượt thích, mục đã lưu và thông báo hệ thống'
          },          
          {
            date: '2018-04-19 20:00:00',
            title: '【Thông báo hệ thống】Hệ thống này sẽ tiến hành nâng cấp bảo trì vào rạng sáng nay lúc2giờ đến5giờ để nâng cấp bảo trì'
          }, {
            date: '2018-04-19 21:00:00',
            title: 'Ưu đãi gửi lúc 0 giờ đêm nay, số lượng có hạn'
          }],
          read: [{
            date: '2018-04-19 20:00:00',
            title: '【Thông báo hệ thống】Hệ thống này sẽ tiến hành nâng cấp bảo trì vào rạng sáng nay lúc2giờ đến5giờ để nâng cấp bảo trì'
          }],
          recycle: [{
            date: '2018-04-19 20:00:00',
            title: '【Thông báo hệ thống】Hệ thống này sẽ tiến hành nâng cấp bảo trì vào rạng sáng nay lúc2giờ đến5giờ để nâng cấp bảo trì'
          }]
        }
      },
      methods: {
        handleRead(index) {
          const item = this.unread.splice(index, 1)
          this.read = item.concat(this.read)
        },
        handleDel(index) {
          const item = this.read.splice(index, 1)
          this.recycle = item.concat(this.recycle)
        },
        handleRestore(index) {
          const item = this.recycle.splice(index, 1)
          this.read = item.concat(this.read)
        }
      },
      computed: {
        unreadNum() {
          return this.unread.length
        }
      }
    }

</script>

<style>
.message-title{
    cursor: pointer;
}
.handle-row{
    margin-top: 30px;
}
</style>

