<template>
  <div>
   <el-select v-model='arrSelected' multiple :placeholder=placeholder >
    <el-option v-for='item in opts' :key='item[valKey]' :label='item[label]' :value='item[valKey]'></el-option>
  </el-select>
</div>
</template>

<script>
import { validatenull } from '@/utils/validate'

export default {
  name: 'StringMultiSelect',
  data() {
    return {
      arrSelected: []
    }
  },
  props: {
    // Nhận từ bên ngoàiv-modelgiá trị truyền vào
    value: {
      type: String
    },
    placeholder: {
      type: String,
      default: () => {return 'Vui lòng chọn'}
    },
    // 选择器选择项
    opts: {
      type: Array,
      default: () => {return []}
    },

    valKey: {
      type: String,
      required: true
    },
    label: {
      type: String,
      required: true
    }
  },
  watch: {
    value(val, oldVal) {
      if(val !== oldVal) {
        this.init()
      }
    },
    // Kiểm tra giá trị hộp thả xuống có thay đổi không
    arrSelected(val, oldVal) {
      if(val!==oldVal){
        if(this.arrSelected.length > 0) {
          this.$emit('input', this.arrSelected.join(',')); 
        } else {
          this.$emit('input', null); 
        }
      }
    }
  },  
  mounted(){
    // Giá trị ban đầu
    this.init()
  },
  methods: {
    init() {
      if(validatenull(this.value)) {
        this.arrSelected = []
      } else {
        this.arrSelected = this.value.split(',')
      }
    }
  }
}

</script>