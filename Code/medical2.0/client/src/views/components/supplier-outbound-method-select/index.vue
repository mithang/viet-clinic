<template>
  <el-select v-model="selectedMethod" filterable placeholder="Vui lòng chọn phương thức xuất kho" clearable>
    <el-option v-for="t in typeList" :label="t.name" :value="t.id" :key="t.id">
    </el-option>
  </el-select>
</template>

<script>
  export default {
    props: {
      value: {
        type: String,
        default: ''
      }
    },

    data() {
      return {
        typeList: [{name:'Trả lại nhà cung cấp',id:'1'},{name:'Báo hỏng thuốc',id:'2'},{name:'Xuất điều chuyển',id:'3'},{name:'Khác',id:'4'}],
        selectedMethod: null
      };
    },
    computed:{
    },
    watch: {
      value() {
        this.selectedMethod = this.value;
      },
      selectedMethod() {
        this.$emit('input', this.selectedMethod);
      }
    },
    methods: {
    },
    mounted() {
    },
  };
</script>

<style>
</style>
