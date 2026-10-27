<template>
  <el-select v-model="selectedStatus" filterable placeholder="请选状态" clearable>
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
        typeList: [{name:'Chờ xét duyệt',id:'1'},{name:'Đã xét duyệt',id:'2'},{name:'Đã thu hồi',id:'3'}],
        selectedStatus: null
      };
    },
    computed:{
    },
    watch: {
      value() {
        this.selectedStatus = this.value;
      },
      selectedStatus() {
        this.$emit('input', this.selectedStatus);
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
