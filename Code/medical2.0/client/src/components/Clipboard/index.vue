<template>
  <el-tooltip :content="tipText" :placement="placement" v-bind="$attrs">
    <el-button
      :icon="icon"
      :type="type"
      v-bind="$attrs"
      @click="handleCopy(content)">Sao chép</el-button>
  </el-tooltip>
</template>

<script>
	export default {
		name: "Clipboard",
    props: {
      content: {
        type: String
      },
      icon: {
        type: String,
        default: () => 'el-icon-document-copy'
      },
      tipText: {
        type: String,
        default: () => 'Sao chép'
      },
      placement: {
        type: String,
        default: () => 'top'
      },
      type: {
        type: String,
        default: () => 'text'
      }
    },
    data() {
		  return {

      }
    },
    methods: {
      handleCopy(content) {
        if (content) {
          // Sử dụng tiến triển, tạm thời không xét tương thích
          let input = document.createElement('input')
          input.value = content
          document.body.appendChild(input)
          input.select()
          document.execCommand("Copy")
          input.remove()
          this.$message({
            type: 'success',
            message: 'Sao chép thành công!'
          })
        }
      }
    }
	}
</script>

<style scoped>

</style>
