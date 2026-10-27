<template>
    <OperationIcon type='primary' content='XuấtexcelTệp' placement='top-start' icon-name='el-icon-download' @click='onExport'></OperationIcon>
</template>

<script>
import OperationIcon from '@/components/OperationIcon'
export default {
  name: 'ExportExcelButton',
  components: { 
    OperationIcon
  },
  data() {
    return {
      loading: false,
      vModel: null
    }
  },
  props: {
    //Nhận từ bên ngoàiv-modelgiá trị truyền vào
    value: {
      type: Array
    },
    data: {
      type: Array
    },
    tHeader: {
      type: Array,
      required: true
    },
    filterVal: {
      type: Array,
      required: true
    }
  },
  methods: {
    onExport() { 
      // 没有数据时，Nhắc nhở
      if(!this.data) {
        this.$alert('没有导出数据！', 'Nhắc nhở', {
          type: 'info'
        })
        return
      }
      this.loading = true
      import('@/vendor/Export2Excel').then(excel => {
        const data = this.formatJson(this.filterVal, this.data)
        excel.export_json_to_excel({
          header: this.tHeader,
          data,
          filename: 'export',
          autoWidth: true
        })
        this.loading = false
      }).catch(error => {
        console.error('export excel error:', error)
        this.loading = false
      })
    },
    formatJson(filterVal, jsonData) {
      return jsonData.map(v => filterVal.map(j => {
        if (j === 'timestamp') {
          return parseTime(v[j])
        } else {
          return this.parsValue(v, j)
        }
      }))
    },
    parsValue(val, keys) {
      let entity = val
      let arr = keys.split('.')
      for (let i = 0; i < arr.length; i++) {
        entity = entity[arr[i]]
      }
      return entity;
    }
  }
}
</script>

