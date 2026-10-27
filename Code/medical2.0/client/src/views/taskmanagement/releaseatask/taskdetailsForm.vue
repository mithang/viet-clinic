<template>
    <el-dialog
    :title="dialogProps.title"
    :visible.sync="dialogProps.visible"
    v-if="dialogProps.visible"
    :close-on-click-modal="false"
    width="60%"
    ref="taskdetailsForm"

    v-loading="loading"
  >
  <!-- 选择任务执行审核 -->
    <div v-if="mark==='0'">
          <el-table
            :data="tableData"
            style="width: 100%"
            max-height="250">
            <el-table-column
               type="index"
               label='Số thứ tự'
               width="width"
               align="center"
            >
            </el-table-column>
            <el-table-column
              prop="company.name"
              label="Phòng khám thực hiện"
              align="center"
              width="width">
            </el-table-column>
            <el-table-column
              prop="executeschedule"
              label="Trạng thái thực hiện"
              align="center"
              width="width">
              <template slot-scope="scope">
                <span v-if="scope.row.executeschedule==='0'" style="color:orange;">
                  Chờ thực hiện
                </span>
                <span v-if="scope.row.executeschedule==='1'" style="color:lightgreen;">
                  Đã thực hiện
                </span>
              </template>
            </el-table-column>
            <el-table-column
              prop="updateBy"
              label="Người thực hiện"
              align="center"
              width="width">
            </el-table-column>
            <el-table-column
              prop="updateDate"
              label="Thời gian thực hiện"
              align="center"
              width="width">
            </el-table-column>
            <el-table-column
              prop="auditType"
              label="Trạng thái xét duyệt"
              align="center"
              width="width">
              <template slot-scope="scope">
                <span v-if="scope.row.auditType==='0'" style="color:orange">
                    Chờ xét duyệt
                </span>
                 <span v-if="scope.row.auditType==='1'" style="color:lightgreen">
                    Đã xét duyệt
                </span>
                <span v-if="scope.row.auditType==='2'" style="color:red">
                    Chưa qua
                </span>
              </template>
            </el-table-column>
            <el-table-column
              label="Thao tác"
              width="120">
              <template slot-scope="scope">
                <el-button
                  v-if="scope.row.auditType==='0'"
                  @click.native.prevent="auditStart(scope.$index, scope.row)"
                  type="text"
                  size="small">
                  Duyệt
                </el-button>
                 <el-button
                  v-if="scope.row.auditType==='1'||scope.row.auditType==='2'"
                  @click.native.prevent="check(scope.$index, scope.row)"
                  type="text"
                  size="small">
                  Xem
                </el-button>
              </template>
            </el-table-column>
          </el-table>
    </div>
  <div v-else>
  <div>
     <div class="specifications">Chi tiết nhiệm vụ</div>
        <div style="width:80%;margin-left:100px">
           <div>
             Loại nhiệm vụ：{{this.indexdata.tasktype==='0'?"Nhiệm vụ thường xuyên":"Hoạt động tuyên truyền"}}
           </div>
           <div>
             Tên nhiệm vụ：{{this.indexdata.taskname}}
           </div>
           <div>
             Mô tả nhiệm vụ：{{this.indexdata.taskdescribe}}
              <upload-file  :objectId="uploadFiles" @getFileList="getFileList" :action="flags" @deleteFile="deleteFile"></upload-file>
           </div>
            <div>
             Người khởi tạo nhiệm vụ：{{this.indexdata.user.name}}
           </div>
            <div>
             Thời hạn nhiệm vụ：{{this.indexdata.taskdeadline}}
           </div>
        </div>
         <br>
        <br>
        <div class="specifications">Phản hồi tình hình thực hiện</div>
         <div style="width:80%;margin-left:100px">
           <div>
             Tiến độ thực hiện：{{this.feedbacklist[0]===undefined?"":this.feedbacklist[0].executeschedule}}
           </div>

           <div>
              <upload-file-1  :objectId="uploadFilesfee" @getFileList="getFileListfee" :action="flagsfee" @deleteFile="deleteFile"></upload-file-1>
           </div>

            <div>
             Ghi chú：{{this.feedbacklist[0]===undefined?"---":this.feedbacklist[0].remark}}
           </div>
        </div>
<!-- <el-descriptions title="Chi tiết nhiệm vụ" :column="1" >
    <el-descriptions-item label="Loại nhiệm vụ">{{this.indexdata.tasktype=this.indexdata.tasktype==="0"?"Nhiệm vụ thường xuyên":"Hoạt động tuyên truyền"}}</el-descriptions-item>
    <el-descriptions-item label="Tên nhiệm vụ">{{this.indexdata.taskname}}</el-descriptions-item>
    <el-descriptions-item label="Mô tả nhiệm vụ">{{this.indexdata.taskdescribe}}</el-descriptions-item>
    <el-descriptions-item label="Người khởi tạo nhiệm vụ">{{this.indexdata.taskinitiatorname}}</el-descriptions-item>
    <el-descriptions-item label="Thời hạn nhiệm vụ">{{this.indexdata.taskdeadline}}</el-descriptions-item>
    <el-descriptions-item >
    <upload-file  :objectId="uploadFiles" @getFileList="getFileList" :action="flags" @deleteFile="deleteFile"></upload-file>
    </el-descriptions-item>
</el-descriptions> -->


<!-- <el-descriptions title="Phản hồi tình hình thực hiện" :column="1" >
    <el-descriptions-item label="Tiến độ thực hiện">{{this.zxjd}}</el-descriptions-item>

    <el-descriptions-item>
      <upload-file-1  :objectId="uploadFilesfee" @getFileList="getFileListfee" :action="flagsfee" @deleteFile="deleteFile"></upload-file-1>
    </el-descriptions-item>


    <el-descriptions-item label="Ghi chú">{{this.feedbacklist[0]===undefined?"---":this.feedbacklist[0].remark}}</el-descriptions-item>
</el-descriptions> -->

<el-descriptions title="Kết quả xét duyệt" ></el-descriptions>
    <el-form  @open="onDialogOpen()" :model="ruleForm" :rules="rules" ref="ruleForm" label-width="100px" class="demo-ruleForm">
      <el-form-item label="Kết quả xét duyệt" prop="auditresult">
    <el-radio-group :disabled="checkFlage" v-model="ruleForm.auditresult">
      <el-radio :label="'0'">Qua</el-radio>
      <el-radio :label="'1'">Không qua</el-radio>
    </el-radio-group>
  </el-form-item>
  <el-form-item label="Ghi chú" >
    <el-input :disabled="checkFlage" type="textarea" v-model="ruleForm.taskremark"></el-input>
  </el-form-item>
    </el-form>



      </div>
    <span slot='footer' class='dialog-footer'>
      <el-button :disabled="flag" type='primary' v-if="!checkFlage" :plain='true' @click="submitForm('ruleForm')">Lưu</el-button>
      <el-button :plain='true' type="primary" @click='onDialogClose1()'>Hủy</el-button>
    </span>
  </div>
    </el-dialog>
</template>


<script>
import BaseUI from "@/views/components/baseUI";
import {inserttask,getusertree,getfeedbacktable,updateaudit} from "@/api/taskmanagement/taskmanagement"
import UploadFile from '../../components/uploadFile.vue'
import UploadFile1 from '../../components/uploadFile.vue'
import { getPhotoById,getFiled } from "@/api/sys/sysSeting"
import { getjglist} from "@/api/toll/tollInfo";
import { listMyTaskPage, saveMyTask, updateMyTask } from '@/api/myTask/myTask'
  export default {
    extends: BaseUI,
  name: "taskdetails-form",
  components: {
    UploadFile,
    UploadFile1
  },
   props:['closeValue'],
   data(){
     return{
       indexdata:[],
       dialogProps: {
        visible: false,
        action: '',
        title: '',
      },
      gettaskrcvo: {
        taskmanagementid:'',
      },
      feedbacklist:[],
      ruleForm:{
        id:'',
        auditresult:"0",
        taskremark:'',
      },
       rules: {
          auditresult: [
            { required: true, message: '请选择审核结果', trigger: 'change' }
          ],
       },
       fileName:'',  //Tên tệp
       bizFormModel:{
        fileIdFile:[],   //Tệp
        uploadFile:[],  //Tệp
        inspectionCheckInfo:{}
      },
      bizFormModel2:{
        fileIdFile:[],   //Tệp
        uploadFile:[],  //Tệp
        inspectionCheckInfo:{}
      },
      flags:true,
      flagsfee:true,
      fileIds:[],
      flags:false,  //Ngăn gửi trùng lặp
      uploadFiles:'',
      taskaccessoryid:'',//Tệp đính kèmid
      uploadFilesfee:'',
      zxjd:'',
      bz:'',
      mark:"0",  //div切换标志
      tableData:[],  //执行任务数据
      checkFlage:false,
      taskManagementId:"",
      myTaskId:"",
     }
   },
   methods:{
     //取消按钮
     onDialogClose() {
     this.$emit('typeclick',"");
      this.dialogProps.visible = false;

    },
    onDialogClose1() {
      this.mark="0";
    },
    onDialogOpen() {
      this.$nextTick(() => {
        this.$refs["ruleForm"].clearValidate();
      });
    },

    submitForm(){
      updateaudit(this.ruleForm).then((responseData)=>{
        if(responseData.code === 100){
          if(this.ruleForm.auditresult==='0'){
            this.myTaskId.auditType = "1"
          }else{
            this.myTaskId.auditType = "2"
          }
          updateMyTask(this.myTaskId).then(res=>{
              if(res.code===100){
                this.ruleForm={
                  id:'',
                  auditresult:0,
                  taskremark:'',
                };
                this.getFeedbacktableList(this.taskManagementId)
                this.onDialogClose1();
                this.$message.success("Xét duyệt thành công")
              }
          })
        }
      })
    },
    //Xem trước
    look(row){

      //  var blob = this.dataURLtoBlob(row)
      //   const elink = document.createElement('a')
      //   // Đặt tên tệp tải xuống
      //   const timedate = Date.parse(new Date())
      //   elink.download = this.fileName
      //   elink.style.display = 'none'
      //   elink.href = URL.createObjectURL(blob)
      //   document.body.appendChild(elink)
      //   elink.click()
      //   document.body.removeChild(elink)
     window.open("http://192.168.0.31:8099/ffview/onlinePreview?url=" + encodeURIComponent(Base64.encode(row)));
    },
    // Sẽbase64Chuyển đổi thànhblob
    dataURLtoBlob: function (dataurl) {
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
    getfileddata(){
      getFiled(this.taskaccessory).then((res) => {
         const src = `data:text/plain;base64,${res.base64Str}`;
        // let name = res.name.substring(0,res.name.lastIndexOf("."))
        let name = res.name
        this.fileName=name
        this.bizFormModel.uploadFile = src;
        return src;
      });
    },
    getfileddata2(){
      getFiled(this.feedbacklist.fileid).then((res) => {
         const src = `data:text/plain;base64,${res.base64Str}`;
        // let name = res.name.substring(0,res.name.lastIndexOf("."))
        let name = res.name
        this.fileName=name
        this.bizFormModel2.uploadFile = src;
        return src;
      });
    },
    getFileList(fileList){
      this.bizFormModel.fileIdFile=[]
      this.bizFormModel.uploadFile=[]
      this.bizFormModel.uploadFile=fileList
      if(fileList!=null&&fileList!==undefined){
        for (let i = 0; i < fileList.length; i++) {
       this.bizFormModel.fileIdFile.push(fileList[i].raw)
      }
      }

    },
    getFileListfee(fileList){
      this.bizFormModel2.fileIdFile=[]
      this.bizFormModel2.uploadFile=[]
      this.bizFormModel2.uploadFile=fileList

      if(fileList!=null&&fileList!==undefined){
        for (let i = 0; i < fileList.length; i++) {
       this.bizFormModel.fileIdFile.push(fileList[i].raw)
      }
      }
    },
    deleteFile(fileIds){
      this.fileIds.push(fileIds)
    },
    getFeedbacktableList(row){
      let search = {
        params: [
          {
            columnName: "tm.id",
            queryType: "=",
            value: row.id,
          },
          {
            columnName: "taskmanagement_id",
            queryType: "=",
            value: row.id,
          }
        ],
        offset: 0,
        limit: 2000,
        columnName: "", // Tên trường sắp xếp
        order: "", // Sắp xếp
      }
      listMyTaskPage(search).then((res)=>{
        if(res.code===100){

          this.tableData = res.data.rows
        }
      }).catch((error)=>{

      })
    },
    check(index,row){
       this.feedbacklist=[]
       this.uploadFiles=row.taskManagement.id

        this.taskaccessory=row.taskManagement.taskaccessory
        this.indexdata=row.taskManagement;
        this.gettaskrcvo.taskmanagementid=row.taskManagement.id
         this.ruleForm.id=row.taskManagement.id
        this.getfileddata();
       // this.Getfeedbackdata();
         this.feedbacklist.push({
                companyId:row.companyId,
                createdBy:row.createdBy,
                createdDate:row.createdDate,
                executeschedule:row.executeschedule === 1 ? "Đã hoàn thành":"Đang thực hiện",
                fileid:row.fileid,
                id:row.id,
                remark:row.remark?row.remark:"",
                taskmanagementid:row.taskManagement.id,
                updatedBy:row.updatedBy,
                updatedDate:row.updatedDate,
                auditType:row.auditType
              })
        this.ruleForm.auditresult = row.taskManagement.auditresult
        this.ruleForm.taskremark = row.taskManagement.taskremark
        this.uploadFilesfee=this.feedbacklist[0].id
        this.getfileddata2();
        this.checkFlage=true
        this.mark = "1"
    },
    auditStart(index,row){
      this.checkFlage = false
      this.feedbacklist=[]
      this.myTaskId = row
       this.uploadFiles=row.taskManagement.id

        this.taskaccessory=row.taskManagement.taskaccessory
        this.indexdata=row.taskManagement;
        this.gettaskrcvo.taskmanagementid=row.taskManagement.id
         this.ruleForm.id=row.taskManagement.id
        this.getfileddata();
       // this.Getfeedbackdata();
         this.feedbacklist.push({
                companyId:row.companyId,
                createdBy:row.createdBy,
                createdTime:row.createdDate,
                executeschedule:row.executeschedule === 1 ? "Đã hoàn thành":"Đang thực hiện",
                fileid:row.fileid,
                id:row.id,
                remark:row.remark?row.remark:"",
                taskmanagementid:row.taskManagement.id,
                updatedBy:row.updatedBy,
                updatedTime:row.updatedDate,
              })
        this.uploadFilesfee=this.feedbacklist[0].id
        this.getfileddata2();
        this.mark = "1"
    },
    openAddworkbenchDialog(types) {
      this.mark = "0"
      this.getFeedbacktableList(types)
      this.taskManagementId = types
      this.dialogProps.action = 'add'
      this.dialogProps.title = ''
      this.tabIndex = '1'
      this.dialogProps.visible = true
    },
   },
  }
  </script>

  <<style lang="scss" scoped>
  .el-descriptions--mini:not(.is-bordered) .el-descriptions-item__cell{
    padding-bottom: 13px;
    padding-left: 16%;
  }
  .specifications {
  width: 100%;
  font-weight: bold;
  font-size: 15px;
  padding-bottom: 8px;
  margin-top: -20px;
  //padding: 20px;
  color: #333;
}
  </style>
