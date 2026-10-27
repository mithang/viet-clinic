<template>
  <el-upload
    ref="imageUpload"
    style="width: 90px"
    class="upload-item"
    action="#"
    :headers="uploadHeaders"
    accept=".jpg,.jpeg,.png,.ico"
    :auto-upload="false"
    :on-success="onSuccess"
    :on-change="onChange"
    :on-preview="onPreview"
    :on-remove="onRemove"
    :before-remove="()=>false"
    :multiple="false"
    :before-upload="beforeUpload"
    :on-exceed="onExceed"
    :http-request="httpRequest"
    :file-list="fileList"
    list-type="picture">
    <el-button size="mini">Chọn hình ảnh</el-button>
  </el-upload>
</template>

<script>
  import { getLocalToken } from '@/utils/auth'
	export default {
		name: "imageUpload",
    props: {
      fileList: {
        type: Array
      },
      onChange: {
        type: Function
      },
      beforeRemove: {
        type: Function
      },
      beforeUpload: {
        type: Function
      },
      onRemove: {
        type: Function
      },
      onExceed: {
        type: Function
      },
      onSuccess: {
        type: Function
      },
      onPreview: {
        type: Function
      },
      httpRequest: {
        type: Function
      }
    },
    data() {
		  return {
        uploadHeaders: {'X-Token': getLocalToken()},
      }
    },
    methods: {
      handleExceed(files, fileList) {
        this.$message.warning(`Giới hạn chỉ được chọn 1 tệp`)
      },
      submit() {
        this.$refs.imageUpload.submit()
      }
    }
	}
</script>

<style scoped>
  .upload-item {
    text-align: right;
  }
</style>
