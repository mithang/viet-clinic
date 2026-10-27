<template>
  <span>{{statusName}}</span>
</template>

<script>
  export default {
    props: {
      value: {
        type: Number,
        default: null
      }
    },

    data() { 
      return {};
    },
    computed:{
      statusName()
      {
        if(!this.value)return '';
        switch(this.value)
        {
          case 1:return 'Chờ xét duyệt';
          case 2:return 'Đã xét duyệt';
          case 3:return 'Đã thu hồi';
        }
        return '';
      }
    },
    watch: { 
    },
    methods: {
    },
    mounted() {
    },
  };
</script>

<style>
</style>
