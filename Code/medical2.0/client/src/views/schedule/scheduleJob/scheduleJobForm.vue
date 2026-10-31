<template>
  <el-dialog :title='dialogProps.title' :visible.sync='dialogProps.visible' :close-on-click-modal='false' width='30%' 
    @open='onDialogOpen()' v-loading='loading'>
    <div slot='title' class='dialog-header'>
      {{ dialogProps.title }}
      <OperationIcon v-show='dialogProps.action === "view" && permission.edit' type='primary' text='Sửa' placement='top-start' icon-name='el-icon-edit' @click='switchEdit'></OperationIcon>
    </div>
    <el-form :model='bizFormModel' :rules='formRules' 
      ref='scheduleJobForm' label-width='120px' label-position='right' class='edit-form'>    

              <el-row>
        <el-col :span='24/1'>
          <el-form-item label='Tên nhiệm vụ' prop='name' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.name' :maxlength='100' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập tên nhiệm vụ"' autofocus></el-input>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/1'>
          <el-form-item label='Mã nhiệm vụ' prop='code' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.code' :maxlength='100' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập mã nhiệm vụ"' ></el-input>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/1'>
          <el-form-item label='Biểu thức nhiệm vụ' prop='cron' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.cron' :maxlength='30' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập biểu thức nhiệm vụ"' >
              <template slot="append">
                <el-button type="primary" @click="handleShowCron">
                  Tạo biểu thức
                  <i class="el-icon-time el-icon--right"></i>
                </el-button>
              </template>
            </el-input>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/1'>
          <el-form-item label='Trạng thái nhiệm vụ' prop='status' >
            <el-input disabled :value='statusLabel' :maxlength='100' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập trạng thái nhiệm vụ"' ></el-input>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/1'>
          <el-form-item label='Có bị vô hiệu hóa không' prop='isLock' >
            <el-switch :disabled='dialogProps.action === "view"' v-model='bizFormModel.isLock' active-color='#13ce66' inactive-color='#dbdfe6' active-value='1' inactive-value='0'></el-switch>
          </el-form-item>
        </el-col>
      </el-row>
                    <el-row>
                <el-col>
                    <el-form-item label='Ghi chú' prop='remarks' >
                        <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.remarks' type='textarea'
                                  :maxlength='100' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập ghi chú"'  clearable></el-input>
                    </el-form-item>
                </el-col>
            </el-row>
    </el-form>
    <span slot='footer' class='dialog-footer'>
      <el-dropdown v-if='dialogProps.action === "edit"' split-button type='warning' @click='startTask("scheduleJobForm")'>
        Khởi động nhiệm vụ
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item><el-button  type='text' :plain='true' @click='restartTask("scheduleJobForm")'>Lưu và khởi động</el-button></el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
       <el-button v-if='dialogProps.action === "edit"' type='primary' :plain='true' @click='endTask("scheduleJobForm")'>Tạm dừng</el-button>
      <el-button v-if='dialogProps.action !== "view"' :disabled="flag" type='primary' :plain='true' @click='onSubmit("scheduleJobForm")'>Lưu</el-button>
      <el-button v-if='dialogProps.action !== "view"' :plain='true' @click='onDialogClose()'>Hủy</el-button>
      <el-button v-if='dialogProps.action === "view"' :plain='true' @click='onDialogClose()'>Đóng</el-button>
    </span>
    <el-dialog title="Trình tạo biểu thức Cron" width="80%" :visible.sync="openCron" append-to-body destroy-on-close class="scrollbar">
      <crontab @hide="openCron=false" @fill="crontabFill" :expression="expression"></crontab>
    </el-dialog>
  </el-dialog>
</template>
<script>
import '@/utils/validate';
import { saveScheduleJob,startTask,endTask,restartTask } from '@/api/schedule/scheduleJob'
import BaseUI from '@/views/components/baseUI'
import OperationIcon from '@/components/OperationIcon'
import Crontab from '@/components/Crontab'

export default {
  extends: BaseUI,
  name: 'scheduleJob-form',
  components: {
    OperationIcon,Crontab
  },  
  data() {
    return {
      bizFormModel: this.initFormModel(),
      flag:false,
       dialogProps: {
        visible: false,
        action: '',
        title: '',
        
      },
      openCron: false,
      // Biểu thức được truyền vào
      expression: "",
      formRules: {
        'name': [
            { required: true, message: 'Vui lòng nhập tên nhiệm vụ', trigger: 'blur' }
        ],
        'code': [
            { required: true, message: 'Vui lòng nhập mã nhiệm vụ', trigger: 'blur' }
        ],
        'cron': [
            { required: true, message: 'Vui lòng nhập biểu thức nhiệm vụ', trigger: 'blur' }
        ],
      }
    }    
  },
  props: {
    // Quyền
    permission: {
      type: Object
    }
  },  
  computed: {
    /**
     * Nhãn trạng thái hiển thị cho người dùng.
     * Backend (Quartz) trả về chuỗi tiếng Trung nên chỉ dịch phần hiển thị,
     * KHÔNG đổi giá trị gán cho bizFormModel.status để giữ nguyên logic so sánh.
     */
    statusLabel() {
      const map = {
        '执行中': 'Đang chạy',
        '已暂停': 'Đã tạm dừng',
        '待执行': 'Chờ chạy',
        '已完成': 'Đã hoàn thành',
        '已停止': 'Đã dừng'
      }
      const status = this.bizFormModel.status
      if (!status) {
        return ''
      }
      return map[status] || status
    }
  },
  methods: {
    /** Thao tác nút biểu thức cron */
    handleShowCron() {
      this.expression = this.bizFormModel.cron;
      this.openCron = true;
    },
    /** Trả giá trị về sau khi xác nhận */
    crontabFill(value) {
      this.bizFormModel.cron = value;
    },

    //Huỷ nhiệm vụ
    endTask(formName){
      this.$refs[formName].validate(valid => {
        if (valid) {
          if (this.bizFormModel.status==='已暂停') {
            this.$message.warning("Nhiệm vụ đang tạm dừng, không thể tạm dừng lại");
            return  false;
          }
          this.setLoad()
          endTask(this.bizFormModel).then(responseData => {
            if(responseData.code === 100) {
              this.dialogProps.visible = false
              this.$emit('save-finished')
              this.$message.success(responseData.data)
            } else {
              this.showMessage(responseData)
            }
            this.resetLoad()
          }).catch(error => {
            this.outputError(error)
          })
        } else {
          return false
        }
      });
    },
    //Khởi động tác vụ định kỳ
    startTask(formName){
      this.$refs[formName].validate(valid => {
        if (valid) {
          if (this.bizFormModel.status==='执行中') {
              this.$message.warning("Nhiệm vụ đang chạy, không thể khởi động lại");
              return  false;
          }
          this.setLoad()
             startTask(this.bizFormModel).then(responseData => {
              if(responseData.code === 100) {
                this.dialogProps.visible = false
                this.$emit('save-finished')
                this.$message.success(responseData.data)
              } else {
                this.showMessage(responseData)
              }
              this.resetLoad()
            }).catch(error => {
              this.outputError(error)
            })
        } else {
          return false
        }
      });
    },

    //Lưu và khởi động lại nhiệm vụ
    restartTask(formName){
      this.$refs[formName].validate(valid => {
        if (valid) {
          this.setLoad()
          saveScheduleJob(this.bizFormModel).then(responseData => {
              if(responseData.code === 100) {
                //Khởi động lại nhiệm vụ
                  restartTask(this.bizFormModel).then(responseData => {
                    if(responseData.code === 100) {
                      this.dialogProps.visible = false
                      this.$emit('save-finished')
                      this.$message.success(responseData.data)
                    } else {
                      this.showMessage(responseData)
                    }
                    this.resetLoad()
                  }).catch(error => {
                    this.outputError(error)
                  })
              } else {
                this.showMessage(responseData)
              }
              this.resetLoad()
            }).catch(error => {
               this.outputError(error)
          })
        } else {return false}
      });
    },




    onSubmit(formName) {
      this.flag=true
      this.$refs[formName].validate(valid => {
        if (valid) {
          this.doSave()
        } else {
          this.flag=false
          return false
        }
      });
    },
    doSave() {
      this.setLoad()
      saveScheduleJob(this.bizFormModel).then(responseData => {
        this.flag=false
        if(responseData.code === 100) {
          this.dialogProps.visible = false
          this.$emit('save-finished')
        } else {
          this.showMessage(responseData)
        }
        this.resetLoad()
      }).catch(error => {
        this.flag=false
        this.outputError(error)
      })
    },
    switchEdit() {
      this.dialogProps.action = 'edit'
      this.dialogProps.title = 'Sửa quản lý nhiệm vụ định kỳ'
      this.initOptions(this.bizFormModel)
    },
    onDialogClose() {
      this.dialogProps.visible = false  
    },
    onDialogOpen() {
      this.$nextTick(() => {
        this.$refs['scheduleJobForm'].clearValidate()
      })
    },
    initFormModel() {
      return {
        'company': {     // Phòng khámid
          'id': currentUser.company.id,
          'name': null,
        },
        'name': '',   // Tên nhiệm vụ
        'code': '',   // Mã nhiệm vụ
        'cron': '',   // Biểu thức nhiệm vụ
        'status': '',   // Trạng thái nhiệm vụ
        'isLock': '',   // Có bị vô hiệu hóa không
        'remarks': '',   // Ghi chú

      }
    },
    initOptions() {
    },
    openViewScheduleJobDialog(scheduleJob) {
      this.dialogProps.action = 'view'
      this.dialogProps.title = 'Xem quản lý nhiệm vụ định kỳ'
      this.bizFormModel = scheduleJob
      this.initOptions(this.bizFormModel)
      this.dialogProps.visible = true
    },
    openEditScheduleJobDialog(scheduleJob) {
      this.dialogProps.action = 'edit'
      this.dialogProps.title = 'Sửa quản lý nhiệm vụ định kỳ'
      // schedule_job has no company column, so a loaded record carries no company
      // while backend update validation requires it; fall back to the login company.
      if (!scheduleJob.company || !scheduleJob.company.id) scheduleJob.company = { id: currentUser.company.id, name: null }
      this.bizFormModel = scheduleJob
      this.initOptions(this.bizFormModel)
      this.dialogProps.visible = true
    },
    openAddScheduleJobDialog() {
      this.dialogProps.action = 'add'
      this.dialogProps.title = 'Thêm quản lý nhiệm vụ định kỳ'
      this.bizFormModel = this.initFormModel()
      this.initOptions(this.bizFormModel)
      this.dialogProps.visible = true
    },
    openCopyScheduleJobDialog(scheduleJob) {
      this.dialogProps.action = 'add'
      this.dialogProps.title = 'Thêm quản lý nhiệm vụ định kỳ'
      if (!scheduleJob.company || !scheduleJob.company.id) scheduleJob.company = { id: currentUser.company.id, name: null }
      this.bizFormModel = scheduleJob
      this.initOptions(this.bizFormModel)
      this.bizFormModel.id = null
      this.dialogProps.visible = true
    },
  },
  watch: {
  },
  mounted: function() {
  },
}
</script>
