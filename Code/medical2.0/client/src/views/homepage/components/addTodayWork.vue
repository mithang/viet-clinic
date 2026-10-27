<template>
  <el-dialog :title='dialogProps.title' :visible.sync='dialogProps.visible' :append-to-body="true" :close-on-click-modal='false' width='40%'
      @open='onDialogOpen()' v-loading='loading'>
    <div slot='title' class='dialog-header'>
      {{ dialogProps.title }}
      <OperationIcon v-show='dialogProps.action === "view"' type='primary' text='Sửa' placement='top-start' icon-name='el-icon-edit' @click='switchEdit'></OperationIcon>
    </div>
    <el-form :model='bizFormModel' :rules='formRules' ref='addTodayWorkForm' label-width='120px' label-position='right' class='edit-form'>
      <div class="tab-item">
        <el-row>
          <el-col :span='24/1'>
            <el-form-item label='Tiêu đề' prop='name' >
              <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.name' :maxlength='100' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập tên thiết bị"' ></el-input>
            </el-form-item>
          </el-col>
          <el-col :span='24/1'>
            <el-form-item label='Cả ngày' prop='allDay' >
              <el-switch :disabled='dialogProps.action === "view"' v-model='bizFormModel.allDay' active-color='#13ce66' inactive-color='#dbdfe6' active-value='0' inactive-value='1'></el-switch>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span='24/1'>
            <el-form-item label='Bắt đầu' prop='fromDate'>
               <el-date-picker :disabled='dialogProps.action === "view"' v-model="bizFormModel.fromDate" type="datetime" placeholder="选择开始日期" align="right" default-time="10:00:00"></el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span='24/1'>
            <el-form-item label='Kết thúc' prop='toDate' >
               <el-date-picker :disabled='dialogProps.action === "view"' v-model="bizFormModel.toDate" type="datetime" placeholder="选择结束日期" align="right" default-time="10:00:00"></el-date-picker>
            </el-form-item>
          </el-col>
        </el-row>
       
        <el-row>
          <el-col>
            <el-form-item label='Thông tin ghi chú' prop='remarks' >
              <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.remarks' type='textarea' autosize
                  :rows="3" :maxlength='255' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập thông tin ghi chú"'  clearable></el-input>
            </el-form-item>
          </el-col>
        </el-row>
      </div>
    </el-form>
    <span slot='footer' class='dialog-footer'>
      <el-button v-if='dialogProps.action !== "view"' :disabled="flag" type='primary' :plain='true' @click='onSubmit("addTodayWorkForm")'>Lưu</el-button>
      <el-button v-if='dialogProps.action !== "view"' :plain='true' @click='onDialogClose()'>Hủy</el-button>
      <el-button v-if='dialogProps.action === "view"' :plain='true' @click='onDialogClose()'>Đóng</el-button>
    </span>
  </el-dialog>
</template>
<script>
  import { validatenull } from '@/utils/validate'
  import BaseUI from '@/views/components/baseUI'
  import OperationIcon from '@/components/OperationIcon'
  import propertyCustom from  '@/views/sys/propertySet/components/propertyCustom'
  export default {
    extends: BaseUI,
    name: 'addTodayWork-form',
    components: {
      OperationIcon,
      propertyCustom
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
        formRules: {
          'name': [
            { required: true, message: 'Vui lòng nhập tên thiết bị', trigger: 'blur' }
          ],
        }
      }
    },
    props: {
    },
    created(){
    },
    methods: {
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
        this.dialogProps.visible = false
        this.flag=false
      },
      switchEdit() {
        this.dialogProps.action = 'edit'
        this.dialogProps.title = '修改日程'
        this.initOptions(this.bizFormModel)
      },
      onDialogClose() {
        this.dialogProps.visible = false
      },
      onDialogOpen() {
        this.$nextTick(() => {
          this.$refs['addTodayWorkForm'].clearValidate()
        })
      },
      initFormModel(This) {
        return {
          'name': '',   // 设备名称
          'remarks': '',   // Thông tin ghi chú
          'allDay': '0', // 是否全天  0Có 1Không
          'fromDate': null,
          'toDade': null
        }
      },
      initOptions(This) {
      },
      openAddGroupDeviceDialog(parent, type) {
        this.dialogProps.action = 'add'
        this.dialogProps.title = '添加日程'
        this.bizFormModel = this.initFormModel(parent)
        this.initOptions(this.bizFormModel)
        this.dialogProps.visible = true
      },
    },
  }
</script>
