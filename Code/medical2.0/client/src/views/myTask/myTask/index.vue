<template>
  <el-row v-loading='loading'>
    <!-- Lịch sử  -->
    <History :bussObject='curentRow' ></History>
    <!-- Cửa sổ chỉnh sửa  -->
    <my-task ref='myTask' :permission='permission' v-on:save-finished='getMyTaskList'></my-task>
    <el-card class="page-container">
        <!--  Thanh tìm kiếm  Bắt đầu -->
        <div class='query-form-container'>
          <el-row v-if='!moreCodition' class='search-row'>
            <el-form :model='queryModel' @submit.native.prevent label-position="left" label-width='70px' ref='queryForm' :inline-message='true'>

            <el-col :span="6">
              <el-form-item label='Tên nhiệm vụ' prop='name'>
                <el-input v-model='queryModel.name' :clearable='true' placeholder='Vui lòng nhập tên nhiệm vụ'></el-input>
              </el-form-item>
            </el-col>
            <!-- <el-col :span="6">
              <el-form-item label='Có bị vô hiệu hóa không' prop='isLocked'>
                <el-switch v-model='queryModel.isLocked' active-color='#13ce66' inactive-color='#dbdfe6' active-value='1' inactive-value='0'></el-switch>
              </el-form-item>
            </el-col> -->
            <el-col :span="5">
              <el-form-item label='Trạng thái nhiệm vụ' prop='isLocked'>
                  <el-select v-model="queryModel.isLocked" placeholder="请选择任务状态">
                    <el-option
                      v-for="item in isLocked"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value">
                    </el-option>
                  </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="3" style="display:flex;justivy-content:space-around">
               <el-button
                    type="primary"
                    icon="el-icon-search"
                    @click="onSearch()"
                    :plain="true"
                  >Tìm kiếm</el-button>
                <el-button
                    type="info"
                    icon="el-icon-refresh-left"
                    @click="reset"
                    :plain="true"
                  >Đặt lại</el-button>
            </el-col>
            <el-col :span="10" style="text-align:right;padding-right:5px">
              <!-- <el-button-group>
                  <el-button
                    v-show="permission.add"
                    type="primary"
                    icon="el-icon-plus"
                    @click="onCreateClinicOffice()"
                    >Thêm</el-button
                  >
              </el-button-group> -->
            </el-col>
            </el-form>
          </el-row>
          <QueryForm v-else v-model='moreParm' :tableId='tableId' :schemeId='schemeId'  :routerId='$route.meta.routerId' @search='onSearch()' @moreCodition='onMoreCodition()'></QueryForm>
        </div>
        <!--  Thanh tìm kiếm  Kết thúc -->

        <!-- Thanh công cụ Bắt đầu -->

        <!-- Thanh công cụ Kết thúc -->

      <!-- Thanh bảng  Bắt đầu -->
      <el-row>
        <el-col :span='24'>
          <div @mouseleave='moveTableOutside'>
            <el-table ref="mutipleTable" height="calc(100vh - 250px)" class='drag_table' :data='clinicOfficeList' border @sort-change='onSortChange' @header-dragend='onChangeWidth' :cell-class-name='cellClassName' :header-cell-class-name='headerCellClassName' highlight-current-row>
              <el-table-column
              label="Số thứ tự"
              type="index"
              :index="indexMethod"
              align="center">
            </el-table-column>
            <el-table-column
              label='Loại nhiệm vụ'
              prop='taskManagement.tasktype'
              align="center"
            >
            <template slot-scope="scope">
              <span v-if="scope.row.taskManagement.tasktype==='0'">
                Nhiệm vụ thường xuyên
              </span>
               <span v-else>
                Hoạt động tuyên truyền
              </span>
            </template>
            </el-table-column>
             <el-table-column
              label='Tên nhiệm vụ'
              prop='taskManagement.taskname'
              align="center"
            >

            </el-table-column>
            <el-table-column
              label='Mô tả nhiệm vụ'
              prop='taskManagement.taskdescribe'
              align="center"
              :show-overflow-tooltip="true"
            >

            </el-table-column>
              <el-table-column
              label='Thời gian đăng'
              prop='taskManagement.updateDate'
              align="center"
            >

            </el-table-column>
             <el-table-column
              label='Thời hạn nhiệm vụ'
              prop='taskManagement.taskdeadline'
              align="center"
            >

            </el-table-column>
            <el-table-column
              label='Trạng thái nhiệm vụ'
              prop='executeschedule'
              align="center"
            >
            <template slot-scope="scope">
              <span v-if="scope.row.executeschedule==='0'" style="color:red">
                Chờ thực hiện
              </span>
               <span v-else-if="scope.row.executeschedule==='1' & scope.row.auditType==='0'" style="color:orange">
                Chờ xét duyệt
              </span>
               <span v-else-if="scope.row.executeschedule==='1' & scope.row.auditType==='1'" style="color:lightgreen">
                 Đã qua
              </span>
              <span v-else-if="scope.row.executeschedule==='1' & scope.row.auditType==='2'" style="color:red">
                 Chưa qua
              </span>
            </template>
            </el-table-column>

              <!--Nút thao tác theo dòng-->
              <el-table-column label='Thao tác' header-align='center' :width='140 + "px"' :key="'operate'">
                <template slot='header' slot-scope="scope">
                  <span>Thao tác</span>
                  <view-columns-select v-model='columnViews' v-on:save-column-view='saveColumn' v-on:show-all-column='showAllColumn' v-on:show-default-column='showDefaultColumn'></view-columns-select>
                  <export-excel-button v-show='permission.export' :data='clinicOfficeList' :tHeader='getHeads()' :filterVal='getFilterVal()' :plain='true'></export-excel-button>
                </template>
                <template slot-scope='scope'>
                  <!-- <OperationIcon v-show='permission.view' type='info' content='Xem' placement='top-start' icon-name='el-icon-view'
                    @click='onViewClinicOffice(scope.$index, scope.row)'></OperationIcon>
                  <OperationIcon v-show='permission.edit' type='primary' content='Sửa' placement='top-start' icon-name='el-icon-edit'
                    @click='onEditClinicOffice(scope.$index, scope.row)'></OperationIcon>
                  <OperationIcon v-show='permission.add' type='primary' content='Sao chép' placement='top-start' icon-name='el-icon-document'
                    @click='onCopyClinicOffice(scope.$index, scope.row)'></OperationIcon>

                  <OperationIcon v-show='permission.view' type='info' content='Lịch sử' placement='top-start' icon-name='el-icon-info'
                    @click='onShowHistory(scope.$index, scope.row)'></OperationIcon> -->
                  <center>
                    <el-button type="text" v-if="scope.row.executeschedule==='0'"  @click="execute(scope.row,scope.$index)">立即执行</el-button>
                  <el-button type="text" v-if="scope.row.executeschedule==='1'||scope.row.executeschedule==='0'"  @click="viewDetails(scope.row,scope.$index)">Xem chi tiết</el-button>
                  </center>
                </template>
              </el-table-column>
            </el-table>
    	  </div>
        </el-col>
      </el-row>
      <!-- Thanh bảng  Kết thúc -->
      <!-- Thanh phân trang     Bắt đầu -->
      <el-row>
        <el-col :span='24'>
          <el-pagination
            background
            @size-change='onSizeChange'
            @current-change='onCurrentChange'
            :current-page.sync='currentPage'
            :page-sizes='[20, 50, 100, clinicOfficeTotal]'
            :page-size='20'
            layout='total, sizes, prev, pager, next, jumper'
            :total='clinicOfficeTotal'>
          </el-pagination>
        </el-col>
      </el-row>
      <!-- Thanh phân trang     Kết thúc -->     </el-card>

      <!-- 立即执行弹出框 -->
      <el-dialog
        title="任务执行"
        :visible.sync="executeDialogVisible"
        v-if="executeDialogVisible"
        width="50%"
        :before-close="handleClose">
        <div class="specifications">Chi tiết nhiệm vụ</div>
        <div style="width:80%;margin-left:100px">
           <div>
             Loại nhiệm vụ：{{task.taskManagement.tasktype==='0'?"Nhiệm vụ thường xuyên":"Hoạt động tuyên truyền"}}
           </div>
           <div>
             Tên nhiệm vụ：{{task.taskManagement.taskname}}
           </div>
           <div>
             Mô tả nhiệm vụ：{{task.taskManagement.taskdescribe}}
             <upload-file  :objectId="uploadFiles" @getFileList="getFileList" :action="flags" @deleteFile="deleteFile"></upload-file>
           </div>
            <div>
             Người khởi tạo nhiệm vụ：{{task.taskManagement.user.name}}
           </div>
            <div>
             Thời hạn nhiệm vụ：{{task.taskManagement.taskdeadline}}
           </div>
            <div v-if="task.auditType==='1' || task.auditType==='2'">
             Ghi chú：{{task.taskManagement.taskremark}}
           </div>
        </div>
        <br>
        <br>
        <div class="specifications">Phản hồi tình hình thực hiện</div>
         <div style="width:80%;margin-left:100px">
           <div>
             Tiến độ thực hiện：<el-radio :disabled="flagsTask" v-model="executeschedule" label="0">Chờ thực hiện</el-radio>
                        <el-radio :disabled="flagsTask" v-model="executeschedule" label="1">Đã thực hiện</el-radio>
           </div>

           <div>
             <upload-file-1  :objectId="uploadFilesTask" @getFileList="getFileListTask" :action="flagsTask" @deleteFile="deleteFileTask"></upload-file-1>
           </div>

            <div>
             Ghi chú：<el-input :disabled="flagsTask" v-model="remark"></el-input>
           </div>
        </div>
        <span slot="footer" class="dialog-footer">
          <el-button @click="executeDialogVisible = false">Hủy</el-button>
          <el-button v-show="!flagsTask" type="primary" @click="save">Xác nhận</el-button>
        </span>
      </el-dialog>
  </el-row>
</template>

<script>
import { validatenull } from '@/utils/validate'
import { listMyTaskPage, saveMyTask, deleteMyTask } from '@/api/myTask/myTask'
import { listResourcePermission } from '@/api/admin/common/permission'
import MyTask from "./myTask.vue"
import ExportExcelButton from '@/components/ExportExcelButton'
import ViewColumnsSelect from '@/views/components/ViewColumnsSelect'
import QueryForm from '@/views/components/queryForm'
import MainUI from '@/views/components/mainUI'
import OperationIcon from '@/components/OperationIcon'
import History from '@/views/components/history'
import UploadFile from '../../components/uploadFile.vue'
import UploadFile1 from '../../components/uploadFile.vue'
import { getPhotoById,getFiled } from "@/api/sys/sysSeting"
export default {
  extends: MainUI,
  components: {
    ExportExcelButton,
    ViewColumnsSelect,
    QueryForm,
    OperationIcon,
    History,
    MyTask,
    UploadFile,
    UploadFile1
  },
  data() {
    return {
      permission: {
        view: false,
        add: true,
        edit: false,
        remove: false,
        export: false
      },
      executeDialogVisible:false,
      queryTypes: {
        'is_locked': '=',
        'name': 'like',
      },
      queryModel: {
        'isLocked': '',   // Vô hiệu hóa（0：Chưa bị vô hiệu hóa；1：Vô hiệu hóa）
        'name': '',   // Tên khoa/phòng
      },

       isLocked:[
            {
              value: '0',
              label: 'Chờ thực hiện'
            },
            {
              value: '1',
              label: 'Đã thực hiện'
            },
          ],//Có kích hoạt không


      search: {
        params: [],
        offset: 0,
        limit: 20,
		columnName: '',      // Tên trường sắp xếp
        order: ''            // Sắp xếp
      },
      currentPage: 1,
      clinicOfficeTotal: 0,
      clinicOfficeList: [],


      oprColumnWidth: 140,  // Độ rộng cột thao tác
      tableId: '1026680647570219053',
      schemeId: '1026680647570219069',
      task:{
        taskManagement:{
          tasktype:"",
          taskname:"",
          taskdescribe:"",
          user:{
            name:""
          },
          taskdeadline:"",
        },
      },
       uploadFiles:"",
       uploadFilesTask:"",
        bizFormModel:{
          fileIdFile:[],   //Tệp
          uploadFile:[],  //Tệp
          inspectionCheckInfo:{}
        },     //Điền đóng gói báo cáo
         bizFormModelTask:{
          fileIdFile:[],   //Tệp
          uploadFile:[],  //Tệp
          inspectionCheckInfo:{}
        },     //Điền đóng gói báo cáo
        flags:false,
        fileIds:[],
        fileIdsTask:[],
        executeschedule:"0",
        remark:"",
        fileName:'',  //Tên tệp
        fileNameTask:"",
        flagsTask:false,
    }
  },
  methods: {
    //Lưu
    save(){
       if(this.flags){
       return
     }
     this.task.executeschedule =this.executeschedule
     this.task.remark = this.remark


     this.flagsTask=true
    //  let userHeaderFile = new FormData();
      //  userHeaderFile.append("entity", JSON.stringify(this.bizFormModel.inspectionCheckInfo));
      // userHeaderFile.append("deleteIds",JSON.stringify([this.photoId]));
     // return
       this.setLoad()
     if(this.bizFormModelTask.fileIdFile.length>0){
        this.bizFormModelTask.fileIdFile.forEach(item=>{
      //  let userHeaderFile = {};
      let userHeaderFile = new FormData();
         userHeaderFile.append("entity", JSON.stringify(this.task));
        userHeaderFile.append("deleteIds",JSON.stringify(this.fileIdsTask));
        userHeaderFile.append("fileIdUploads",item);
        //  userHeaderFile.entity=JSON.stringify(this.bizFormModel.inspectionCheckInfo);
        // userHeaderFile.deleteIds=JSON.stringify([this.photoId]);
        // userHeaderFile.fileIdUploads=item;

      saveMyTask(userHeaderFile).then((res)=>{
          if(res.code===100){
            this.$message.success("Thao tác thành công!")
            this.uploadFilesTask = ""
            this.getMyTaskList("1")
          }else{
            this.$message.error("Lưu thất bại!")
          }
          this.executeDialogVisible=false
          this.resetLoad()
          this.flagsTask=false
      }).catch((error)=>{
        this.$message.error("Thực hiện thất bại，请联系管理员处理!")
        this.flagsTask=false
        this.resetLoad()
      })
      })
     }else{
        let userHeaderFile = new FormData();
         userHeaderFile.append("entity", JSON.stringify(this.task));
        userHeaderFile.append("deleteIds",JSON.stringify(this.fileIdsTask));
        userHeaderFile.append("fileIdUploads",this.bizFormModelTask.fileIdFile);

      saveMyTask(userHeaderFile).then((res)=>{
          if(res.code===100){
            this.$message.success("Thao tác thành công!")
            this.executeDialogVisible=false
            this.flagsTask=false
            this.getMyTaskList("1")
          }
          this.resetLoad()
      }).catch((error)=>{
        this.flagsTask=false
        this.$message.error("Thực hiện thất bại，Vui lòng liên hệ quản trị viên xử lý!")
        this.resetLoad()
      })
     }
    },

    getPhoto(id) {
      if(!id){
        this.bizFormModel.uploadFile = ''
      }else{
        getFiled(id).then((res) => {
         const src = `data:text/plain;base64,${res.base64Str}`;
        // let name = res.name.substring(0,res.name.lastIndexOf("."))
        let name = res.name
        this.fileName=name
        this.bizFormModel.uploadFile = src;
        return src;
      });
      }
    },
    getPhotoTask(id) {
      if(!id){
        this.bizFormModelTask.uploadFile = ''
      }else{
        getFiled(id).then((res) => {
         const src = `data:text/plain;base64,${res.base64Str}`;
        // let name = res.name.substring(0,res.name.lastIndexOf("."))
        let name = res.name
        this.fileNameTask=name
        this.bizFormModelTask.uploadFile = src;
        return src;
      });
      }
    },
     deleteFile(fileIds){
      this.fileIds.push(fileIds)
    },
    deleteFileTask(fileIds){
      this.fileIdsTask.push(fileIds)
    },
     getFileList(fileList){
      this.bizFormModel.fileIdFile=[]
      this.bizFormModel.uploadFile=[]
      this.bizFormModel.uploadFile=fileList
      for (let i = 0; i < fileList.length; i++) {
       this.bizFormModel.fileIdFile.push(fileList[i].raw)
      }
    },
    getFileListTask(fileList){
      this.bizFormModelTask.fileIdFile=[]
      this.bizFormModelTask.uploadFile=[]
      this.bizFormModelTask.uploadFile=fileList
      for (let i = 0; i < fileList.length; i++) {
       this.bizFormModelTask.fileIdFile.push(fileList[i].raw)
      }
    },
    //执行任务
    execute(row,index){
      this.fileIds=[]
      this.fileIdsTask=[]
      this.bizFormModel={
        fileIdFile:[],   //Tệp
        uploadFile:[],  //Tệp
        inspectionCheckInfo:{}
      }
      this.bizFormModelTask = {
        fileIdFile:[],   //Tệp
        uploadFile:[],  //Tệp
        inspectionCheckInfo:{}
      }
      this.task = row
      this.flags = true
      this.flagsTask = false
      this.uploadFiles=this.task.taskManagement.id
      this.uploadFilesTask = this.task.id
      this.photoId=this.task.taskManagement.taskaccessory
      this.photoIdTask = this.task.fileId
      this.executeschedule = this.task.executeschedule
      this.remark = this.task.remark
      this.getPhoto(this.task.taskManagement.taskaccessory);
      this.getPhotoTask(this.task.fileId)
      this.executeDialogVisible = true
    },
    //Xem chi tiết
    viewDetails(row,index){
       this.fileIds=[]
      this.fileIdsTask=[]
      this.bizFormModel={
        fileIdFile:[],   //Tệp
        uploadFile:[],  //Tệp
        inspectionCheckInfo:{}
      }
      this.bizFormModelTask = {
        fileIdFile:[],   //Tệp
        uploadFile:[],  //Tệp
        inspectionCheckInfo:{}
      }
      this.task = row
      this.flags = true
      this.flagsTask = true
      this.uploadFiles=this.task.taskManagement.id
      this.uploadFilesTask = this.task.id
      this.photoId=this.task.taskManagement.taskaccessory
      this.photoIdTask = this.task.fileId
      this.executeschedule = this.task.executeschedule
      this.remark = this.task.remark
      this.getPhoto(this.task.taskManagement.taskaccessory);
      this.getPhotoTask(this.task.fileId)
      this.executeDialogVisible = true
    },
    indexMethod(index){
       return (this.currentPage-1)*this.search.limit+index +1;
    },
    reset(){
      this.$refs.queryForm.resetFields()
      this.onSearch()
    },
    getMyTaskList(val) {
      this.setLoad()
      /* Tham số truy vấn và quyền dữ liệu */
      this.search.params = []
       this.search.params.push({
      	  columnName: 'company_id',
      	  queryType: '=',
          value: currentUser.company.id
        })
      if(val==="1") {
        this.search.params = this.search.params.concat(this.compositeCondition())
      }else{
        // Tham số truy vấn: Vô hiệu hóa（0：Chưa bị vô hiệu hóa；1：Vô hiệu hóa）
        this.search.params.push({
      	  columnName: 'executeschedule',
      	  queryType: '=',
          value: this.queryModel.isLocked
        })
        // Tham số truy vấn: Tên khoa/phòng
        this.search.params.push({
      	  columnName: 'tm.taskname',
      	  queryType: 'like',
          value: this.queryModel.name
        })
      }
      // Quyền dữ liệu: Khoaclinic_office
      this.pushDataPermissions(this.search.params, this.$route.meta.routerId, this.tableId)
      listMyTaskPage(this.search).then(responseData => {
        if(responseData.code === 100) {
          this.clinicOfficeTotal = responseData.data.total
          this.clinicOfficeList = responseData.data.rows

        } else {
          this.showMessage(responseData)

        }
        this.resetLoad()
      }).catch(error => {
        this.outputError(error)
        this.resetLoad()
      })
    },
    onSearch() {
      if(this.moreCodition) {
        this.search.offset = 0
        this.currentPage = 1
        this.getMyTaskList()
      } else {
        this.$refs['queryForm'].validate(valid => {
          if (valid) {
            this.search.offset = 0
            this.currentPage = 1
            this.getMyTaskList()
          } else {
            return false
          }
        })
      }
    },
    onSizeChange(val) {
      this.currentPage = 1
      this.search.limit = val;
      this.search.offset = (this.currentPage - 1) * val
      this.getMyTaskList()
    },
    onCurrentChange(val) {
      this.search.offset = (val - 1) * this.search.limit
      this.currentPage = val
      this.getMyTaskList()
    },
    async pageInit() {
      this.setLoad()
      try {
        this.initOptions(this.queryModel)
        this.search.params = []
        // Quyền dữ liệu: Khoaclinic_office
         this.search.params.push({
      	  columnName: 'company_id',
      	  queryType: '=',
          value: currentUser.company.id
        },

        )
        this.pushDataPermissions(this.search.params, this.$route.meta.routerId, this.tableId)
        let [listClinicOfficeRespData, listPermissionRespData] = await Promise.all([
          listMyTaskPage(this.search),
          listResourcePermission(this.$route.meta.routerId)
        ])
        if(listClinicOfficeRespData.code === 100 && listPermissionRespData.code === 100) {
          this.clinicOfficeTotal = listClinicOfficeRespData.data.total
          this.clinicOfficeList = listClinicOfficeRespData.data.rows
          this.permission.view = listPermissionRespData.data.find(item => {
            return item.permission === 'myTask:read'
          })
          this.permission.export = listPermissionRespData.data.find(item => {
            return item.permission === 'myTask:export'
          })
          this.permission.add = listPermissionRespData.data.find(item => {
            return item.permission === 'myTask:create'
          })
          this.permission.edit = listPermissionRespData.data.find(item => {
            return item.permission === 'myTask:update'
          })
          this.permission.remove = listPermissionRespData.data.find(item => {
            return item.permission === 'myTask:delete'
          })
        } else {
          this.showMessage(listPermissionRespData.code !== 100 ? listPermissionRespData : listClinicOfficeRespData)
        }
        this.resetLoad()
      } catch(error) {
        this.outputError(error)
      }
    },
    onViewClinicOffice(index, row) {
      this.setLoad()
      getClinicOfficeById(row.id).then(responseData => {
        if(responseData.code === 100) {
          this.$refs.myTask.openViewClinicOfficeDialog(responseData.data)
        } else {
          this.showMessage(responseData)
        }
        this.resetLoad()
      }).catch(error => {
        this.outputError(error)
      })
    },
    onCreateClinicOffice() {
      this.$refs.myTask.openAddClinicOfficeDialog()
    },
    onEditClinicOffice(index, row) {
      this.setLoad()
      getClinicOfficeById(row.id).then(responseData => {
        if(responseData.code === 100) {
          this.$refs.myTask.openEditClinicOfficeDialog(responseData.data)
        }else{
          this.showMessage(responseData)
        }
        this.resetLoad()
      }).catch(error => {
        this.outputError(error)
      })
    },
    onCopyClinicOffice(index, row) {
      this.setLoad()
      getClinicOfficeById(row.id).then(responseData => {
        if(responseData.code === 100) {
          this.$refs.myTask.openCopyClinicOfficeDialog(responseData.data)
        } else {
          this.showMessage(responseData)
        }
        this.resetLoad()
      }).catch(error => {
        this.outputError(error)
      })
    },
    onDeleteClinicOffice(index, row) {
      this.$confirm('Bạn có chắc muốn xóa không？', 'Xác nhận', {
        confirmButtonText: 'Xác nhận',
        cancelButtonText: 'Hủy',
        type: 'warning'
      }).then(() => {
        this.setLoad()
        deleteClinicOffice(row).then(responseData => {
          if(responseData.code === 100) {
            this.getMyTaskList()
            this.showMessage({type: 'success', msg: 'Xóa thành công'})
          } else {
            this.showMessage(responseData)
          }
          this.resetLoad()
        }).catch(error => {
          this.outputError(error)
        })
      }).catch(() => {})
    },
    onSortChange( orderby ) {
      if(validatenull(orderby.prop)) {
        this.search.columnName = ''
        this.search.order = ''
      } else  {
        this.search.columnName = orderby.prop
        this.search.order = orderby.order === 'descending' ? 'desc' : 'asc'
      }

      this.getMyTaskList()
    },
    initOptions(This) {
    }
  },
  watch: {
    clinicOfficeList(val){
      if(val){
        this.$nextTick(() => {
            if (this.$refs.mutipleTable) {
              this.$refs.mutipleTable.doLayout();
            }
        });
      }
    }
  },
  updated(){
    if(this.$refs.mutipleTable){
       this.$nextTick(() => {
            // tableRefCóel-tableĐã liên kếtrefGiá trị thuộc tính
            this.$refs.mutipleTable.doLayout()// Đối với Table Thực hiện bố cục lại
        })
    }
  },
  mounted() {
    this.pageInit()
  }
}
</script>
<style lang="scss" scoped>
.page-container{
  padding: 0;
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
