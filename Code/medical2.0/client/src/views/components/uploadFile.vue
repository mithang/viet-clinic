<template>
  <div>
    <el-collapse v-model="activeNames" v-loading="loading">
      <el-collapse-item name="3">
        <template slot="title">
          <span class="collapse-title">{{title}}<span style="color: #ccc"> (Mở rộng để xem thêm)</span></span>
          <div class="collapse-btn">
<!--            <el-button style="width: 90px;" type="primary" @click.stop="onUploadFile" v-if='!action'>Tải lên</el-button>-->
            <el-button style="width: 90px;" type="primary" @click.stop="onUploadFile" v-if='authority === "add" || authority === "edit"'>Tải lên</el-button>
          </div>
        </template>
        <el-upload
          :disabled='authority === "view"'
          class="upload-demo"
          multiple
          action=""
          :auto-upload="false"
          accept=".jpg,.jpeg,.png,.doc,.docx,.xls,.xlsx,.pdf,.txt"
          :file-list="fileList"
          :headers="uploadHeaders"
          :limit="7"
          :on-preview="onPreviewFile"
          :on-remove="onRemoveFileList"
          :before-remove="beforeRemoveFile"
          :on-success="fileSuccess"
          :on-exceed="onExceedFile"
          :on-change="onChangeFileList">
          <el-button :id="uploadBtnId" size="small" type="primary">Tải tệp đính kèm lên</el-button>
        </el-upload>
        <el-table :data='viewFileList' style='width: 100%' v-show="authority !== 'add'">
<!--        <el-table :data='viewFileList' style='width: 100%' v-show="action">-->
          <el-table-column prop='name' label='Tên tệp đính kèm' :width="width"></el-table-column>
          <el-table-column prop="createBy" label='Người tải lên' width='190px'></el-table-column>
          <el-table-column prop='createDate' label='Ngày tải lên' width='160px'></el-table-column>
          <el-table-column label='Thao tác' header-align='center' width='120px'>
            <template slot-scope='scope'>
              <OperationIcon type='info' content='Xem trước' placement='top-start' icon-name='el-icon-view'
                             @click='onViewFileList(scope.$index, scope.row)'></OperationIcon>
              <OperationIcon type='info' content='Tải xuống' placement='top-start' icon-name='el-icon-download'
                             @click='onUploadViewFile(scope.$index, scope.row)'></OperationIcon>
              <OperationIcon v-if="authority === 'edit'" type='info' content='Xóa' placement='top-start' icon-name='el-icon-close'
                             @click='onRemoveViewFile(scope.$index, scope.row)'></OperationIcon>
            </template>
          </el-table-column>
        </el-table>
      </el-collapse-item>
    </el-collapse>
  </div>

</template>

<script>
  import { validatenull } from '@/utils/validate'
  import { fileUploadById, deleteSysFile, listSysFileAll } from '@/api/sys/sysFile'
  import { getPhotoById,getFiled } from "@/api/sys/sysSeting"
  import OperationIcon from '@/components/OperationIcon'
  import BaseUI from '@/views/components/baseUI'
  import { getLocalToken } from '@/utils/auth'

  let Base64 = require('js-base64').Base64
  export default {
    extends: BaseUI,
    name: "uploadFile",
    props: {
      value: {
        type: Array,
         default(){
            return {uploads:[]}
         }
      },
      action: {
        type: Boolean | String
      },
      objectId: {
        type: String | Number
      },
      title: {
        type: String,
        default: () => {
          return 'Tải tệp đính kèm lên'
        }
      }
    },
    data() {
      return {
        uploadBtnId: 'uploadBtn' + Math.random(), // Id động của tệp tải lên, giải quyết vấn đề một trang có nhiều component tải tệp

        activeNames: [], // Bảng thu gọn
        uploadHeaders: {'X-Token': getLocalToken()},
        baseApi: process.env.BASE_API,
        fileList:  [],
        loading: false,
        viewFileList: [], // Danh sách tệp đính kèm hiển thị lại
        fileName:"",
        uploadFiles:"",
      }
    },
    components: {
      OperationIcon
    },
    methods: {
      onUploadFile() { // Tải tệp đính kèm lên
        if (this.activeNames.indexOf('3') === -1) {
          this.activeNames.push("3")
        }
        document.getElementById(this.uploadBtnId).click()
      },
      onRemoveFileList(file, fileList) { // Xoá tệp
        this.value.uploads = fileList
      },
      onUploadViewFile(index, row) { // Tải xuống
        // fileUploadById(row.id).then(responseData => {
        //   const content = responseData;
        //   this.download(content, row.name);
        // }).catch(error => {
        //   this.outputError(error)
        // });
         getFiled(row.id).then((res) => {
         const src = `data:text/plain;base64,${res.base64Str}`;
        let name = res.name
      //  this.fileName=name
        // this.uploadFiles = src
        this.download(src,name)
        }).catch((error)=>{
            this.outputError(error)
        })
      },
      // Phương thức tải xuống
      download(uploadFiles, fileName) {
        // const blob = new Blob([content]);
        // const url = window.URL.createObjectURL(blob);
        // let dom = document.createElement("a");
        // dom.style.display = "none";
        // dom.href = url;
        // dom.setAttribute("download", fileName);
        // document.body.appendChild(dom);
        // dom.click();

        const blob = this.dataURLtoBlob(uploadFiles)
        const elink = document.createElement('a')
        // Đặt tên tệp tải xuống
        const timedate = Date.parse(new Date())
        elink.download = fileName
        elink.style.display = 'none'
        elink.href = URL.createObjectURL(blob)
        document.body.appendChild(elink)
        elink.click()
        document.body.removeChild(elink)
      },
      // Sẽ base64 Chuyển đổi thành blob
    dataURLtoBlob(dataurl) {
      const arr = dataurl.split(',')
      const mime = arr[0].match(/:(.*?);/)[1]
      const bstr = atob(arr[1])
      let n = bstr.length
      const u8arr = new Uint8Array(n)
      while (n--) {
        u8arr[n] = bstr.charCodeAt(n)
      }
      return new Blob([u8arr], { type: mime })
    },
      onPreviewFile(file) {
      },
      onExceedFile(file, fileList) {
        this.$message.warning(`Hiện giới hạn tải lên 7 tệp`);
      },
      beforeRemoveFile(file, fileList) {
        let index = this.fileList.indexOf(file);
        if (index > -1) {
          this.fileList.splice(index, 1);
          this.$emit("beforeRemove",file,fileList)
        }
      },
      fileSuccess(res, file){                 // Tải tệp lên thành công
      },
      onChangeFileList(file, fileList) {
        this.fileList = fileList
       this.value.uploads = fileList
       this.$emit("getFileList",this.fileList)
      },
      onViewFileList(index, row) { // Xem trước
        let suffix = row.name.substring(row.name.lastIndexOf("."))
        let fileId = row.id + "" + suffix // Ghép tham số, ID tệp + phần mở rộng
        let token = getLocalToken()
        // Đường dẫn truy cập luồng tệp
        let url = process.env.FILE_PREVIEW_URL + `sys/sysSeting/getFile/` + fileId + `?token=` + token
        window.open(process.env.KK_FILE_URL + encodeURIComponent(Base64.encode(url)));
      },
      onRemoveViewFile(index, row) {
        this.$confirm(`Xác nhận xóa ${ row.name }?`, 'Xác nhận', {
          confirmButtonText: 'Xác nhận',
          cancelButtonText: 'Hủy'
        }).then(() => {
          this.viewFileList.splice(index, 1)
          //this.value.deletes.push(row.id)
          this.$emit("deleteFile",row.id)
        })
      },
      getFileList(){
        return this.fileList;
      },
      getViewFileList(){
        return this.viewFileList;
      },
      // Theo objectId, lấy thông tin tệp đính kèm
      getSysFileAll() {
        this.setLoad()
        let file_search = {
          params: [{'columnName':'object_id', 'queryType': '=', 'value': this.objectId}]
        }
        listSysFileAll(file_search).then(responseData => {
          if(responseData.code === 100) {
            this.viewFileList = responseData.data
            if (!validatenull(this.viewFileList)) {
              this.activeNames.push("3")
            }
          } else {
            this.showMessage(responseData)
          }
          this.resetLoad()
        }).catch(error => {
          this.outputError(error)
        })
      },
      initOption() { // Khởi tạo dữ liệu
        this.fileList = []  // Danh sách tệp đính kèm đã tải lên
        this.activeNames = [] // Khởi tạo bảng gập
        this.viewFileList = []
        if (!validatenull(this.objectId)) {
          this.getSysFileAll()
        }
      }
    },
    computed: {
      authority() {
        if (validatenull(this.action)) {
          return "add";
        }
        if (typeof this.action === 'boolean') {
          if (this.action) {
            this.fileList = []  // Danh sách tệp đính kèm đã tải lên
            return "view";
          } else {
            return "add";
          }
        } else {
          if (this.action !== "add") {
            this.fileList = []  // Danh sách tệp đính kèm đã tải lên
          }
          return this.action;
        }
      }
    },
    watch: {
      'objectId'(newVal, oldVal) {
        if (!validatenull(this.objectId)) {
          this.getSysFileAll()
        }else{
          this.fileList = []  // Danh sách tệp đính kèm đã tải lên
          this.activeNames = [] // Khởi tạo bảng gập
          this.viewFileList = []
        }
      },
      'action'(newVal,oldVal){
        this.action=newVal
        if (newVal !== oldVal) {
          this.getSysFileAll()
        }
      }
    },
    mounted() {
      this.initOption()

    }
  }
</script>

<style lang="scss" scoped>
  ::v-deep .upload-demo .el-upload {
    display: none!important;
  }
  .collapse-title {
    font-size: 14px;
    color: #606266;
  }
  .el-collapse-item__header {
    position: relative;
    border-bottom: 1px solid #EBEEF5!important;
    margin-bottom: 10px;
    .collapse-btn {
      // position: absolute;
      right: 30px;
    }
  }
</style>
