<template>
  <el-date-picker
    v-if='type === "datetimerange"'
    v-model='vModel'
    type='datetimerange'
    range-separator='đến'
    start-placeholder='Ngày giờ bắt đầu'
    end-placeholder='Ngày giờ kết thúc'
    align='right'
    value-format='yyyy-MM-dd HH:mm:ss'
    format='dd/MM/yyyy HH:mm:ss'
    :picker-options='pickerOptions'
    unlink-panels>
  </el-date-picker>
  <el-date-picker
    v-else
    v-model='vModel'
    type='daterange'
    range-separator='đến'
    start-placeholder='Ngày bắt đầu'
    end-placeholder='Ngày kết thúc'
    align='right'
    value-format='yyyy-MM-dd'
    format='dd/MM/yyyy'
    :picker-options='pickerOptions'
    unlink-panels>
  </el-date-picker>
</template>

<script>
export default {
  name: 'DataRange',
  data() {
    return {
      vModel: null,
      pickerOptions: {
        shortcuts: [{
          text: 'Tuần gần nhất',
          onClick(picker) {
            const end = new Date();
            const start = new Date();
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 7);
            picker.$emit('pick', [start, end]);
          }
        }, {
          text: 'Tháng gần nhất',
          onClick(picker) {
            const end = new Date();
            const start = new Date();
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 30);
            picker.$emit('pick', [start, end]);
          }
        }, {
          text: '3 tháng gần nhất',
          onClick(picker) {
            const end = new Date();
            const start = new Date();
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 90);
            picker.$emit('pick', [start, end]);
          }
        }]
      }
    }
  },
  props: {
    //Nhận từ bên ngoàiv-modelgiá trị truyền vào
    value: {
      type: Array
    },
    type: {
      type: String,
      default: () => {return 'daterange'}
    }
  },
  watch:{
    //Kiểm tra giá trị hộp thả xuống có thay đổi không
    vModel(val, oldVal) {
      if(val!==oldVal){
          this.$emit('input', this.vModel);
      }
    },
    value(val, oldVal){   //Thêm theo dõi value giá trị
      if(val!==oldVal){
        this.vModel=this.value;
      }
    },
  },
  mounted(){
    //Khởi tạo giá trị ô ngày tháng
    this.vModel=this.value;
  }
}
</script>

<style scoped>
.el-date-editor--daterange.el-input, .el-date-editor--daterange.el-input__inner, .el-date-editor--timerange.el-input, .el-date-editor--timerange.el-input__inner {
    width: 100%;
}
.el-date-editor--datetimerange.el-input, .el-date-editor--datetimerange.el-input__inner {
    width: 100%;
}
</style>
