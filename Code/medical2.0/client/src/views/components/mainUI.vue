<template>
  <div  style="text-align:left" >
    Lớp thành phần cơ sở của cửa sổ list, xử lý kéo thả sắp xếp tiêu đề cột, dữ liệu tiêu đề cột của dữ liệu xuất, và cấu hình hiển thị cá nhân hóa danh sách.
  </div>
</template>
<script>
import BaseUI from './baseUI'
import { listColumnView, saveColumnView, showDefaultColumnView, showAllColumnView } from '@/api/common/columnView'
export default {
  extends: BaseUI,
  name: 'MainUI',
  data() {
    return {
      columnViews: [],
      oprColumnWidth: 140, // Độ rộng cột thao tác, mặc định 140px, ghi đè trong lớp con
      oprColumnDefaultWidth: null,    // Lưu độ rộng cột thao tác mặc định
      tableId: '0',       // Ghi đè trong lớp con
      dragState: {
        startIndex: -1, // index phần tử bắt đầu kéo
        endIndex: -1, // index phần tử kết thúc kéo
        afterMoveIndex: -1, // index phần tử sau khi kéo
        dragging: false, // Có đang kéo hay không
        direction: null, // Hướng kéo
        moveTableOutsideBack: false, // Kéo ra ngoài table rồi kéo trở lại
        startClassName: '',          // Lưu class của cột bắt đầu kéo
        startCursor: '',            // Lưu hình dạng con trỏ của cột kéo
        startShowIndex: -1          // Ghi lại index phần tử bắt đầu kéo, số cột hiển thị trong table
      },
      curentRow: {},
      moreCodition: false,     // Hộp tìm kiếm có thêm điều kiện truy vấn không
      moreParm: {   // Giá trị điều kiện thêm
          groupOne: [
            {logic: 'AND', column: null, queryType: '', value: null, operations: []},
            {logic: 'AND', column: null, queryType: '', value: null, operations: []},
            {logic: 'AND', column: null, queryType: '', value: null, operations: []}
          ],
          groupTwo: [
            {logic: 'AND', column: null, queryType: '', value: null, operations: []},
            {logic: 'AND', column: null, queryType: '', value: null, operations: []},
            {logic: 'AND', column: null, queryType: '', value: null, operations: []}
          ],
          groupLogic: 'OR'
      }
    }
  },
  methods: {
    async mainUIInit() {
      this.oprColumnDefaultWidth = this.oprColumnWidth  // Lưu độ rộng cột thao tác mặc định
      if (this.tableId && this.tableId !== '0') {
        // Route không có routerId (vd mở bằng hash tới route chưa đăng ký):
        // không gọi backend với tham số thiếu — trước đây trả 400 + alert
        // "thiếu bắt buộc Tham số: routerId". Giữ cột mặc định, trang vẫn
        // hiển thị; quyền thao tác fail-closed ở pageInit.
        const routerId = this.resolveRouterId()
        if (!routerId) {
          return
        }
        this.setLoad()
        listColumnView(routerId, this.tableId).then(responseData => {
          if(responseData.code === 100) {
            this.initViewColumn(responseData.data)
          } else {
            this.showMessage(responseData)
          }
          this.resetLoad()
        }).catch(error => {
          this.outputError(error)
        })
      }
    },
    // Tương ứng hiển thị nút lịch sử
    onShowHistory(index, row) {
      this.curentRow = {id: row.id, name: row.name}
    },
    // Lưu cột hiển thị cá nhân hóa
    saveColumn(column) {
      column['tableId'] = this.tableId
      column['routerId'] = this.$route.meta.routerId
      this.setLoad()
      saveColumnView(column).then(responseData => {
        if(responseData.code !== 100) {
          this.showMessage(responseData)
        }
        this.resetLoad()
      }).catch(error => {
        this.outputError(error)
      })
    },
    // Hiển thị tất cả cột
    showAllColumn() {
      this.setLoad()
      showAllColumnView(this.$route.meta.routerId, this.tableId).then(responseData => {
        if(responseData.code === 100) {
          this.initViewColumn(responseData.data)
        } else {
          this.showMessage(responseData)
        }
        this.resetLoad()
      }).catch(error => {
        this.outputError(error)
      })
    },
    // Hiển thị cột mặc định
    showDefaultColumn() {
      this.setLoad()
      showDefaultColumnView(this.$route.meta.routerId, this.tableId).then(responseData => {
        if(responseData.code === 100) {
          this.oprColumnWidth = this.oprColumnDefaultWidth  // Khôi phục độ rộng cột thao tác mặc định
          this.initViewColumn(responseData.data)
        } else {
          this.showMessage(responseData)
        }
        this.resetLoad()
      }).catch(error => {
        this.outputError(error)
      })
    },
    // drag_table được gọi khi render tiêu đề bảng
    renderHeader (h, { column, $index }) {
      // Có thể sửa đổi theo nhu cầu dựa trên giá trị $index,
      return h('span', {
          'class': ['thead-cell'],
          on: {
            mousedown: ($event) => { this.handleMouseDown($event, column) },
            mouseup: ($event) => { this.handleMouseUp($event, column) },
            mousemove: ($event) => { this.handleMouseMove($event, column) }
          }
        },
        [
          // Thêm <a> để hiển thị label tiêu đề bảng
          h('a', column.label),
          // Thêm một thẻ rỗng để hiển thị hiệu ứng kéo
          h('span', {
            'class': ['virtual']
          })
        ]
      )
    },


    // Nhấn chuột bắt đầu kéo
    handleMouseDown (e, column) {
      // Kiểm tra có phải nút trái chuột không
      if (e.button === 0) {
        // Kéo thay đổi độ rộng cột
        if(document.body.style.cursor === "col-resize") {
          return
        }

        this.dragState.dragging = true
        this.dragState.startIndex = parseInt(column.columnKey)
        this.dragState.startShowIndex = this.getShowIndex(this.dragState.startIndex)
        // cho cột hiện cần kéo thiết lập class và cursor
        let thElement = document.querySelectorAll('.drag_table table thead tr th')[this.dragState.startShowIndex]
        this.dragState.startClassName = thElement.className
        thElement.className = 'dragging_column'
        this.dragState.startCursor = thElement.style.cursor
        thElement.style.cursor = 'move'
        // Thêm chiều rộng và chiều cao cho container ảo khi kéo
        let table = document.getElementsByClassName('drag_table')[0]
        let virtual = document.getElementsByClassName('virtual')
        // Thiết lập vị trí, độ rộng, chiều cao cho mỗi cột
        for (let item of virtual) {
          // Giống vị trí bắt đầu của <th>
          item.style.top = item.parentElement.parentElement.parentElement.getBoundingClientRect().y + 'px'
          item.style.left = item.parentElement.parentElement.parentElement.getBoundingClientRect().x + 'px'
          item.style.height = table.clientHeight + 'px'
          item.style.width = item.parentElement.parentElement.parentElement.clientWidth + 'px'
        }
        this.dragState.moveTableOutsideBack = false
      }
    },

    // Đang kéo
    handleMouseMove (e, column) {
      // Kiểm tra có phải nút trái chuột không và đã kích hoạtMouseDownsự kiện
      if (e.button === 0 && this.dragState.startIndex > -1) {
        if (this.dragState.dragging) {
          let currentIndex = parseInt(column.columnKey) // index cột kéo hiện tại
          if (currentIndex !== this.dragState.startIndex) {
            this.dragState.direction = currentIndex - this.dragState.startIndex < 0 ? 'left' : 'right' // Xác định hướng kéo
            this.dragState.afterMoveIndex = currentIndex
          } else {
            this.dragState.direction = null
          }
        } else {
          return false
        }
      }
    },

    // Nhả chuột kết thúc kéo
    handleMouseUp (e, column) {
      // Kiểm tra có phải nút trái chuột không và đã kích hoạtMouseDownsự kiện
      if (e.button === 0 && this.dragState.startIndex > -1) {
        // Kéo ra ngoài table rồi kéo trở lại, không thực hiện đổi chỗ (đã xử lý khi kéo ra ngoài)
        if (this.dragState.moveTableOutsideBack) {
          return false
        } else {
          this.dragState.endIndex = parseInt(column.columnKey) // Ghi lại index cột kết thúc
          if (this.dragState.startIndex !== this.dragState.endIndex) {
            this.dragColumn(this.dragState)
          }
          this.finishDragInit()
        }
      }
    },

    // Xử lý khi kéo ra ngoài table hiện tại
    moveTableOutside () {
      if (this.dragState.dragging) {
        this.dragState.endIndex = this.dragState.startIndex
        if (this.dragState.startIndex !== this.dragState.endIndex) {
          this.dragColumn(this.dragState)
        }
        this.finishDragInit()
        this.dragState.moveTableOutsideBack = true
      }
    },

    // Kéo đổi chỗ
    dragColumn ({ startIndex, endIndex, direction }) {
      // Xác định di chuyển trái hay phải
      let column = this.columnViews[startIndex]
      if (direction === 'left') {
        if(endIndex === 0) {
          column.sort = this.columnViews[endIndex].sort/2
        }else{
          column.sort = (this.columnViews[endIndex - 1].sort + this.columnViews[endIndex].sort)/2
        }
        this.columnViews.splice(endIndex, 0, this.columnViews[startIndex])
        this.columnViews.splice(startIndex + 1, 1)
      } else {
        if(endIndex === this.columnViews.length - 1) {
          column.sort = (this.columnViews[endIndex].sort + (this.columnViews.length + 1)*10000)/2
        }else{
          column.sort = (this.columnViews[endIndex].sort + this.columnViews[endIndex + 1].sort)/2
        }
        this.columnViews.splice(endIndex + 1, 0, this.columnViews[startIndex])
        this.columnViews.splice(startIndex, 1)
      }
      this.saveColumn(column)
    },

    // Khởi tạo sau khi kéo hoàn thành
    finishDragInit () {
      // cho cột hiện cần kéo Khôi phục class và cursor
      let thElement = document.querySelectorAll('.drag_table table thead tr th')[this.dragState.startShowIndex]
      thElement.className = this.dragState.startClassName
      thElement.style.cursor = this.dragState.startCursor
      // Khởi tạo lại trạng thái kéo
      this.dragState = {
        startIndex: -1,
        endIndex: -1,
        afterMoveIndex: -1,
        dragging: false,
        direction: null,
        moveTableOutsideBack: false,
        startClassName: '',
        startCursor: '',
        startShowIndex: -1
      }
    },

    // Thêm class động cho ô tiêu đề bảng, tạo hiệu ứng đường nét đứt khi đang kéo
    headerCellClassName ({ column, columnIndex }) {
      return columnIndex === this.getShowIndex(this.dragState.afterMoveIndex) ? `drag_active_${this.dragState.direction}` : ''
    },

    // Thêm class động cho th ô tiêu đề bảng, tạo nền khi đang kéo
    cellClassName ({ column, columnIndex }) {
       return (columnIndex === this.dragState.startShowIndex ? `dragging_column` : '')
    },

    initViewColumn(columns) {
      this.columnViews.splice(0, this.columnViews.length)
      for (let i = 0; i <= columns.length - 1; i++) {
        if(columns[i].prop === 'operate_column') {   // Cột thao tác
	        this.oprColumnWidth = columns[i].width
        } else {
          this.columnViews.push(columns[i])
        }
      }
    },

    /* Sự kiện đáp ứng khoảng cách biên cột kéo */
    onChangeWidth(newWidth, oldWidth, column, event) {
      if(column.columnKey) {
        this.columnViews[column.columnKey].width = newWidth
        this.saveColumn(this.columnViews[column.columnKey])
      } else {  // Cột thao tác
        this.oprColumnWidth = newWidth
        this.saveColumn({
          width: this.oprColumnWidth,
          prop: 'operate_column',
          display: false,
          sort: (this.columnViews.length + 1) * 10000
        })
      }
    },

    /* Lấy số cột hiển thị thực tế */
    getShowIndex(index) {
      let showIndex = -1
      for (let i = 0; i <= index; i++) {
        if(this.columnViews[i].display) {
          showIndex = showIndex + 1
        }
      }
      return showIndex
    },

    /* Mảng tiêu đề cột hiển thị danh sách khi xuất excel */
    getHeads(){
      const heads = []
      for (let i = 0; i <= this.columnViews.length - 1; i++) {
        if(this.columnViews[i].display) {
          heads.push(this.columnViews[i].label)
        }
      }
      return heads
    },
    /* Mảng trường dữ liệu cột hiển thị danh sách khi xuất excel */
    getFilterVal(){
      const filterVal = []
      for (let i = 0; i <= this.columnViews.length - 1; i++) {
        if(this.columnViews[i].display) {
          filterVal.push(this.columnViews[i].prop)
        }
      }
      return filterVal
    },

    onMoreCodition() {
      this.moreCodition = !this.moreCodition
    }
  },
  mounted() {
    this.mainUIInit()
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style lang="scss">
.drag_table th {
  height: 44px;
}
.drag_table th.gutter:last-of-type {
  height: 0 !important;
}
.drag_table .el-table__body-wrapper {
  overflow-y: auto;
  height: calc(100% - 44px) !important;
}
.drag_table .el-table__fixed-right {
  height: 100% !important;
}
.virtual {
  position: fixed;
  display: block;
}
.drag_active_left .virtual {
  cursor: move;
  border-left: 1px dotted #666;
  z-index: 99;
}
.drag_active_right .virtual {
  cursor: move;
  border-right: 1px dotted #666;
  z-index: 99;
}
.thead-cell {
  display: inline-flex;
  flex-direction: column;
}
.thead-cell:before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  bottom: 0;
  right: 0;
}
.dragging_column {
  background-color: #f3f3f3 !important;
}
</style>
