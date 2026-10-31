<template>
<el-dialog
    :title="dialogProps.title"
    :visible.sync="dialogProps.visible"
    v-if="dialogProps.visible"
    :close-on-click-modal="false"
    width="60%"
    @open="onDialogOpen()"
    v-loading="loading"
  >
<div>
  <el-form ref="addtaskForm" :model="ruleForm" :rules="rules"  label-width="20%" class="demo-ruleForm">
      <el-form-item label="Loại nhiệm vụ" prop="tasktype">
    <el-radio-group v-model="ruleForm.tasktype">
      <el-radio :label="'0'">Nhiệm vụ thường xuyên</el-radio>
      <el-radio :label="'1'">Hoạt động tuyên truyền</el-radio>
    </el-radio-group>
  </el-form-item>
  <el-form-item label="Tên nhiệm vụ" prop="taskname">
    <el-input v-model="ruleForm.taskname"></el-input>
  </el-form-item>
  <el-form-item label="Mô tả nhiệm vụ" prop="taskdescribe">
    <el-input
  type="textarea"
  :rows="3"
  placeholder="Vui lòng điền mô tả nhiệm vụ"
  v-model="ruleForm.taskdescribe">
</el-input>
<!-- <el-upload
  class="upload-demo"
  action="https://jsonplaceholder.typicode.com/posts/"
  :on-preview="handlePreview"
  :on-remove="handleRemove"
  :before-remove="beforeRemove"
  multiple
  :limit="10"
  :on-exceed="handleExceed"
  :file-list="fileList">
  <el-button size="small" type="primary">Nhấn để tải lên</el-button>
  <div slot="tip" class="el-upload__tip">Chỉ được tải lên tệp JPG/PNG, dung lượng không quá 500KB</div>
</el-upload> -->
              <div style="margin-left:-50px">
              <upload-file  :objectId="uploadFiles" :value="uploads" @getFileList="getFileList" :action="flags" @deleteFile="deleteFile"></upload-file>
              </div>
  </el-form-item>
  <el-form-item label="Người khởi tạo nhiệm vụ">
      {{this.ruleForm.taskinitiator}}
    <!-- <el-descriptions>
    <el-descriptions-item label="">{{this.form.initiator}}</el-descriptions-item>
</el-descriptions> -->
  </el-form-item>
  <el-form-item label="Thời hạn nhiệm vụ"  prop="taskdeadline">
    <el-col :span="11">
      <!-- <el-date-picker type="datetime" placeholder="Chọn ngày giờ" v-model="ruleForm.taskdeadline" style="width: 100%;"></el-date-picker> -->
      <el-date-picker
      v-model="ruleForm.taskdeadline"
      type="datetime"
      value-format="yyyy-MM-dd HH:mm:ss"
      placeholder="Chọn ngày giờ">
    </el-date-picker>
    </el-col>
    <!-- <el-col class="line" :span="2">-</el-col>
    <el-col :span="11">
      <el-time-picker placeholder="Chọn thời gian" v-model="ruleForm.taskdeadline2" style="width: 100%;"></el-time-picker>
    </el-col> -->
  </el-form-item>

  <el-form-item label="Người thực hiện nhiệm vụ" width="80%">
    <!-- <el-select v-model="ruleForm.taskregion" placeholder="Vui lòng chọn người thực hiện nhiệm vụ">
      <el-option label="Quản trị viên phòng khám" value="1247793999298339926"></el-option>
      <el-option label="Người thực hiện 2" value="beijing"></el-option>
    </el-select> -->
    <!-- <el-cascader
    :options="ruleForm.taskregion"
    :props="{ multiple: true, checkStrictly: true }"
    clearable></el-cascader> -->
    <el-select v-model="jgiddatalist" multiple placeholder="Vui lòng chọn" @change="hideOther()">
      <el-option
      key="-1"
          label="Tất cả"
          value="qb"
          ></el-option>
    <el-option
          v-for="(item, i) in jglist"
          :key="i"
          :label="item.jgmc"
          :value="item.jgid"
        />
  </el-select>
  </el-form-item>
  
</el-form>
  
</div>
<span slot='footer' class='dialog-footer'>
      <el-button :disabled="flag" type='primary' :plain='true' @click="save('addtaskForm')">Lưu</el-button>
  
      <el-button :disabled="flag" type='primary' :plain='true' @click="submitForm('addtaskForm')">Phát hành</el-button>
      
      <el-button :plain='true' type="primary" @click='onDialogClose()'>Hủy</el-button>
    </span> 
</el-dialog>
</template>

<style>
  .el-table .warning-row {
    background: oldlace;
  }

  .el-table .success-row {
    background: #f0f9eb;
  }
</style>

<script>
import BaseUI from "@/views/components/baseUI";
import {inserttask,getusertree,storageTask} from "@/api/taskmanagement/taskmanagement"
import UploadFile from '../../components/uploadFile.vue'
import { getjglist} from "@/api/toll/tollInfo";
  export default {
    extends: BaseUI,
  name: "addtask-form",
  components: {
    UploadFile
  },  
   props:['closeValue'],
    data() {
      return {
        tableData: [],
        dialogProps: {
        visible: false,
        action: '',
        title: '',
      },
      ruleForm: {
          tasktype:0,
          taskname:'',
          taskdescribe:'',
          taskaccessory:'',
          taskinitiator:currentUser.name,
          taskdeadline:'',
          taskregion:[],
          
        //   name: '',
        //   describe:'',
        //   initiator:currentUser.name,
        //   region: '',
        //   date1: '',
        //   date2: '',
        //   delivery: false,
        //   type: [],
        //   resource: '',
        //   desc: '',
        //   radio:1,
        },
        fileList: [{name: 'food.jpeg', url: 'https://fuss10.elemecdn.com/3/63/4e7f3a15429bfda99bce42a18cdd1jpeg.jpeg?imageMogr2/thumbnail/360x360/format/webp/quality/100'}, {name: 'food2.jpeg', url: 'https://fuss10.elemecdn.com/3/63/4e7f3a15429bfda99bce42a18cdd1jpeg.jpeg?imageMogr2/thumbnail/360x360/format/webp/quality/100'}],

        rules: {
            tasktype: [
            { required: true, message: 'Vui lòng chọn loại công việc', trigger: 'change' }
          ],
          taskname: [
            { required: true, message: 'Vui lòng nhập tên công việc', trigger: 'blur' },
            // { min: 3, max: 5, message: 'Độ dài trong khoảng 3 đến 5 ký tự', trigger: 'blur' }
          ],
          taskdescribe: [
            { required: true, message: 'Vui lòng điền mô tả nhiệm vụ', trigger: 'change' }
          ],
          
          taskdeadline: [
            {  required: true, message: 'Vui lòng chọn ngày giờ', trigger: 'change' }
          ],
          taskregion: [
            { type: 'array', required: true, message: 'Vui lòng chọn người thực hiện nhiệm vụ', trigger: 'change' }
          ],
        //   resource: [
        //     { required: true, message: 'Vui lòng chọn tài nguyên hoạt động', trigger: 'change' }
        //   ],
        //   desc: [
        //     { required: true, message: 'Vui lòng nhập hình thức hoạt động', trigger: 'blur' }
        //   ]
        },
        bizFormModel:{
        fileIdFile:[],   //Tệp
        uploadFile:[],  //Tệp
        inspectionCheckInfo:{}
      },     //Điền đóng gói báo cáo
      flags:false,
      flags:false,  //Ngăn gửi trùng lặp
      gettaskrcvo: {
          limit: 20,
        offset: 0,
        jgid: '',
          kssj:'',
          jssj:'',
          tasktype:'',
          taskstatus:'',
          taskname:'',
          companyId:currentUser.company.id,
        },
        jglist:[],
        YpjxcRc:{
        limit: 20,
        offset: 0,
        companyId: currentUser.company.id,
        orderby:"batch_no",
        jgid:null,
      },
      jgiddatalist:[],
      fileIds:[],
      uploadFiles:"",
      taskRow:null, //Đối tượng công việc
      uploads:null,
      }
    },
    methods: {

      //Lưu công việc
      save(formName){
           if(this.flags){
       return
     }
     this.flags=true
       this.$refs[formName].validate(async (valid) => {
         if(valid){
            let countId ="0"
       if(this.bizFormModel.fileIdFile.length>0){
         for(let item of this.bizFormModel.fileIdFile){
         //this.bizFormModel.fileIdFile.forEach( item=>{
      //  let userHeaderFile = {};
    
      let userHeaderFile = new FormData();
        if(this.taskRow==null){
            this.ruleForm.taskinitiator=currentUser.id;
           userHeaderFile.append("entity", JSON.stringify(this.ruleForm));
        }else{
        this.taskRow.taskname = this.ruleForm.taskname
        this.taskRow.taskdescribe = this.ruleForm.taskdescribe
        this.taskRow.taskaccessory = this.ruleForm.taskaccessory
        this.taskRow.taskinitiator = currentUser.id
        this.taskRow.taskdeadline = this.ruleForm.taskdeadline
        this.taskRow.taskregion = this.ruleForm.taskregion
         userHeaderFile.append("entity", JSON.stringify(this.taskRow));
          
        }
     
        userHeaderFile.append("deleteIds",JSON.stringify(this.fileIds));
        userHeaderFile.append("fileIdUploads",item);
        userHeaderFile.append("countId",countId);
      // this.$refs[formName].validate((valid) => {
          //  if (valid) {
           await  storageTask(userHeaderFile).then((responseData)=>{
               if(responseData.code === 100){
                 countId = responseData.data.id
               }else {
                 this.$message.error("Thực hiện thất bại!")
                  
                 this.flags=false
                 return;
               }
             })
          //  }else{
          //    console.log("Thất bại");
          //    this.flags=false
          //  }
         //})
         }
            this.ruleForm= {
                tasktype:'0',
                taskname:'',
                taskdescribe:'',
                taskaccessory:'',
                taskinitiator:currentUser.name,
                taskdeadline:'',
                taskregion:[],
             };
            this.$message.success("Thực hiện thành công!")
            this.flags=false
            this.onDialogClose();
        //})
       }else{

          let userHeaderFile = new FormData();
        
          if(this.taskRow==null){
            this.ruleForm.taskinitiator=currentUser.id;
           userHeaderFile.append("entity", JSON.stringify(this.ruleForm));
        }else{
          this.taskRow.taskname = this.ruleForm.taskname
          this.taskRow.taskdescribe = this.ruleForm.taskdescribe
          this.taskRow.taskaccessory = this.ruleForm.taskaccessory
          this.taskRow.taskinitiator = currentUser.id
          this.taskRow.taskdeadline = this.ruleForm.taskdeadline
          this.taskRow.taskregion = this.ruleForm.taskregion
          userHeaderFile.append("entity", JSON.stringify(this.taskRow));
        }
 
        userHeaderFile.append("deleteIds",JSON.stringify(this.fileIds));
        userHeaderFile.append("fileIdUploads",null);
        userHeaderFile.append("countId",countId);
        // this.$refs[formName].validate((valid) => {
            //  if (valid) {
            //  this.ruleForm.taskinitiator=currentUser.id;
             storageTask(userHeaderFile).then((responseData)=>{
               if(responseData.code === 100){
                 this.ruleForm= {
                    tasktype:0,
                    taskname:'',
                    taskdescribe:'',
                    taskaccessory:'',
                    taskinitiator:currentUser.name,
                    taskdeadline:'',
                    taskregion:[],
                          };
                 this.flags=false
                 this.onDialogClose();
               }
             })
          //  }else{
          //    console.log("Thất bại");
          //    this.flags=false
          //  }
        // })
       }
         }
       })

     
     
      },

      tableRowClassName({rowIndex}) {
        if (rowIndex === 1) {
          return 'warning-row';
        } else if (rowIndex === 3) {
          return 'success-row';
        }
        return '';
      },
      onDialogClose() {
     this.$emit('typeclick',"");
      this.dialogProps.visible = false;
      
    },
    onDialogOpen() {
      this.$nextTick(() => {
        this.$refs["addtaskForm"].clearValidate();
      });
    },
    //Tải tệp đính kèm lên
    handleRemove() {
      },
      handlePreview() {
      },
      handleExceed(files, fileList) {
        this.$message.warning(`Giới hạn chọn hiện tại 3 tệp, Lần này đã chọn ${files.length} tệp, Đã chọn tổng cộng ${files.length + fileList.length} tệp`);
      },
      beforeRemove(file) {
        return this.$confirm(`Xác nhận xóa ${ file.name }？`);
      },
      submitForm(formName){
       
        if(this.flags){
       return
     }
     this.flags=true
     this.$refs[formName].validate( async (valid) => {
       if(valid){
         let countId = "0"
       if(this.bizFormModel.fileIdFile.length>0){
         for(let item of this.bizFormModel.fileIdFile){
       // this.bizFormModel.fileIdFile.forEach(item=>{
      //  let userHeaderFile = {};
      
     
      let userHeaderFile = new FormData();
       if(this.taskRow==null){
            this.ruleForm.taskinitiator=currentUser.id;
           userHeaderFile.append("entity", JSON.stringify(this.ruleForm));
        }else{
        this.taskRow.taskname = this.ruleForm.taskname
        this.taskRow.taskdescribe = this.ruleForm.taskdescribe
        this.taskRow.taskaccessory = this.ruleForm.taskaccessory
        this.taskRow.taskinitiator = currentUser.id
        this.taskRow.taskdeadline = this.ruleForm.taskdeadline
        this.taskRow.taskregion = this.ruleForm.taskregion
         userHeaderFile.append("entity", JSON.stringify(this.taskRow));    
        }
      //   userHeaderFile.append("entity", JSON.stringify(this.ruleForm));
        userHeaderFile.append("deleteIds",JSON.stringify(this.fileIds));
        userHeaderFile.append("fileIdUploads",item);
        userHeaderFile.append("countId",countId);
        // this.$refs[formName].validate((valid) => {
            //  if (valid) {
            //  this.ruleForm.taskinitiator=currentUser.id;
            await inserttask(userHeaderFile).then((responseData)=>{
               if(responseData.code === 100){
                 countId = responseData.data.id
               }else{
                this.flags=false
                this.$message.error("Thực hiện thất bại!")
                return;
               }
             })

          //  }else{
          //    console.log("Thất bại");
          //    this.flags=false
          //  }
       //  })
        // })
         }
          this.ruleForm= {
                tasktype:0,
                taskname:'',
                taskdescribe:'',
                taskaccessory:'',
                taskinitiator:currentUser.name,
                taskdeadline:'',
                taskregion:[],
            };
            this.$message.success("Thực hiện thành công!")
            this.flags=false
            this.onDialogClose();

       }else{
         
           
      let userHeaderFile = new FormData();
       if(this.taskRow==null){
            this.ruleForm.taskinitiator=currentUser.id;
           userHeaderFile.append("entity", JSON.stringify(this.ruleForm));
        }else{
        this.taskRow.taskname = this.ruleForm.taskname
        this.taskRow.taskdescribe = this.ruleForm.taskdescribe
        this.taskRow.taskaccessory = this.ruleForm.taskaccessory
        this.taskRow.taskinitiator = currentUser.id
        this.taskRow.taskdeadline = this.ruleForm.taskdeadline
        this.taskRow.taskregion = this.ruleForm.taskregion
         userHeaderFile.append("entity", JSON.stringify(this.taskRow));
          
        }
        userHeaderFile.append("deleteIds",JSON.stringify(this.fileIds));
        userHeaderFile.append("fileIdUploads",null);
        userHeaderFile.append("countId",countId);
          // this.$refs[formName].validate((valid) => {
          // if (valid) {
            //  this.ruleForm.taskinitiator=currentUser.id;
             inserttask(userHeaderFile).then((responseData)=>{
               if(responseData.code === 100){
                 this.ruleForm= {
          tasktype:'0',
          taskname:'',
          taskdescribe:'',
          taskaccessory:'',
          taskinitiator:currentUser.name,
          taskdeadline:'',
          taskregion:[],
                 };
                 this.flags=false
                 this.onDialogClose();
               }
             })
          //  }else{
          //    console.log("Thất bại");
          //    this.flags=false
          //  }
         //})
       }
       }
     })
      
        
      },

       getFileList(fileList){
      this.bizFormModel.fileIdFile=[]
      this.bizFormModel.uploadFile=[]
      this.bizFormModel.uploadFile=fileList
      for (let i = 0; i < fileList.length; i++) {
       this.bizFormModel.fileIdFile.push(fileList[i].raw)
      }
     
    },
    deleteFile(fileIds){
      this.fileIds.push(fileIds)
    },
    Getusertreedata(){
      getusertree(this.gettaskrcvo).then((responseData)=>{
        if(responseData.code === 100 ){
          this.ruleForm.taskregion=responseData.data;
        }
      })
    },
    Getcliniclist(type){
          this.jglist=[];
          getjglist(this.YpjxcRc).then((responseData)=>{
              if (responseData.code === 100){
                  if(responseData.data.length>0){
                     this.zsids='';
                      responseData.data.forEach((item)=>{
                          if(item.jgid!==currentUser.company.id){
                          this.jglist.push({
                              jgid:item.jgid,
                              jgmc:item.jgmc,
                           })
                          
                         }
                      })
                     
                  }
                  if(type===2){
                       this.jgiddatalist = []
                        if(this.jglist.length===this.taskRow.taskexecutors.length){
                          this.jgiddatalist.push("qb")
                        }else{
                          this.taskRow.taskexecutors.forEach((item)=>{
                            this.jgiddatalist.push(item.company.id)
                        })
                        }
                      this.uploadFiles= this.taskRow.id 
                  }
              }
          })
      },
      hideOther() {
      // Phương thức indexOf của mảng, trả về chỉ mục của giá trị hiện tại trong mảng, không có thì trả về -1
      if (this.jgiddatalist.indexOf('qb') === -1 ) {
        this.jglist.map((item) => {
          item.disabled = true
          
        })
        
      } else {
        this.jglist.map((item) => {
          item.disabled = false
        })
        this.jgiddatalist = ['qb']
       }
      
      if(this.jgiddatalist==="qb"){
this.ruleForm.taskregion=[];
        this.jglist.forEach((item2)=>{
         this.ruleForm.taskregion.push(item2.jgid)
                      })
      }
      else{
        this.ruleForm.taskregion=[];
        this.ruleForm.taskregion=this.jgiddatalist;
      }
    },
      init(){
        this.ruleForm = {
          tasktype:'0',
          taskname:'',
          taskdescribe:'',
          taskaccessory:'',
          taskinitiator:currentUser.name,
          taskdeadline:'',
          taskregion:[],
        }
        this.jgiddatalist=[]
        this.taskRow=null
      },
      openAddworkbenchDialog(types) {
        this.uploadFiles = ""
        this.uploads = []
        this.bizFormModel = {
          fileIdFile: [],
          uploadFile: [],
          inspectionCheckInfo: {}
        }
        this.init()
        this.flags = false
        this.flags = false

        if (types !== "" && types != null) {
          this.Getselectmbbm(types);
        } else {
          this.mblx = "0",
          this.bllx = "0",
          this.formLabelAlign = {};
        }
        this.Getcliniclist(1)
        this.dialogProps.action = 'add'
        this.dialogProps.title = 'Thêm thông tin nhiệm vụ mới'
        this.tabIndex = '1'
        this.dialogProps.visible = true
        this.province = ''
        this.city = ''
        this.area = ''
      },
      openUpdateworkbenchDialog(row) {
        this.uploadFiles = ""
        this.taskRow = row
        this.fileIds = []
        this.bizFormModel = {
          fileIdFile: [],
          uploadFile: [],
          inspectionCheckInfo: {}
        }
        this.uploads = []
        this.flags = false
        this.flags = false

        if (row !== "" && row != null) {
        } else {
          this.mblx = "0",
          this.bllx = "0",
          this.formLabelAlign = {};
        }
        this.Getcliniclist(2)
        let arr = []
        row.taskexecutors.forEach((item) => {
          arr.push(item.id)
        })
        this.ruleForm = {
          tasktype: row.tasktype,
          taskname: row.taskname,
          taskdescribe: row.taskdescribe,
          taskaccessory: row.taskaccessory,
          taskinitiator: row.taskinitiatorname,
          taskdeadline: row.taskdeadline,
          taskregion: arr,
        }
        this.dialogProps.action = 'update'
        this.dialogProps.title = 'Sửa thông tin nhiệm vụ'
        this.tabIndex = '1'
        this.dialogProps.visible = true
      },
    },
  }
</script>
