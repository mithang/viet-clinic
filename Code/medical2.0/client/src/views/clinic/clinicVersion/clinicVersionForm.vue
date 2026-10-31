<template>
  <el-dialog :title='dialogProps.title' :visible.sync='dialogProps.visible' :close-on-click-modal='false' width='30%' 
    @open='onDialogOpen()' v-loading='loading'>
    <div slot='title' class='dialog-header'>
      {{ dialogProps.title }}
      <OperationIcon v-show='dialogProps.action === "view" && permission.edit' type='primary' text='Sửa' placement='top-start' icon-name='el-icon-edit' @click='switchEdit'></OperationIcon>
    </div>

    <el-form :model='bizFormModel' :rules='formRules' 
      ref='clinicVersionForm' label-width='120px' label-position='right' class='edit-form'>  
      <div class="tab-item" v-show='tabIndex==="1"'>  
              <el-row>
        <el-col :span='24/1'>
          <el-form-item label='Tên phiên bản' prop='name' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.name' :maxlength='50' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập tên phiên bản"' autofocus></el-input>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/1'>
          <el-form-item label='Mã phiên bản' prop='code' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.code' :maxlength='50' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập mã phiên bản"' autofocus></el-input>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/1'>
          <el-form-item label='Giá phiên bản' prop='price' >
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.price'></el-input>
            <number-input v-else v-model="bizFormModel.price" currency='VND' :precision="2"></number-input>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/1'>
          <el-form-item label='Mô tả phiên bản' prop='description' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.description' :maxlength='200' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập mô tả phiên bản"' ></el-input>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col>
          <el-form-item label='Thông tin ghi chú' prop='remarks' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.remarks' type='textarea'
                                  :maxlength='255' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập thông tin ghi chú"'  clearable></el-input>
          </el-form-item>
        </el-col>
      </el-row>
      </div>
    </el-form>
    <span slot='footer' class='dialog-footer'>
      <el-button v-if='dialogProps.action !== "view"' :disabled="flag" type='primary' :plain='true' @click='onSubmit("clinicVersionForm")'>Lưu</el-button>
      <el-button v-if='dialogProps.action !== "view"' :plain='true' @click='onDialogClose()'>Hủy</el-button>
      <el-button v-if='dialogProps.action === "view"' :plain='true' @click='onDialogClose()'>Đóng</el-button>
    </span>    
  </el-dialog>
</template>
<script>
import '@/utils/validate';
import { saveClinicVersion } from '@/api/clinic/clinicVersion'
import BaseUI from '@/views/components/baseUI'
import OperationIcon from '@/components/OperationIcon'
export default {
  extends: BaseUI,
  name: 'clinicVersion-form',
  components: {
    OperationIcon
  },  
  data() {
    return {
      bizFormModel: this.initFormModel(),
      tabIndex: '1',
      flag:false,
       dialogProps: {
        visible: false,
        action: '',
        title: '',
        
      },
      formRules: {
        'name': [
            { required: true, message: 'Vui lòng nhập tên phiên bản', trigger: 'blur' }
        ],
        'price': [
            { required: true, message: 'Vui lòng chọn giá phiên bản', trigger: 'blur' }
        ],
        'code': [
            { required: true, message: 'Vui lòng nhập mã phiên bản', trigger: 'blur' }
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
  methods: {
    onSubmit(formName) {
      this.flag=true
      this.$refs[formName].validate(valid => {
        //debugger
        if (valid) {
          //debugger
          this.doSave()
        } else {
          this.flag=false
          return false
        }
      });
    },
    doSave() {
      this.setLoad()
      // debugger
      saveClinicVersion(this.bizFormModel).then(responseData => {
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
      this.dialogProps.title = 'Sửa phiên bản phòng khám'
      this.initOptions(this.bizFormModel)
    },
    onDialogClose() {
      this.dialogProps.visible = false  
    },
    onDialogOpen() {
      this.$nextTick(() => {
        this.$refs['clinicVersionForm'].clearValidate()
      })
    },
    initFormModel() {
      return {
        'name': '',   // Tên phiên bản
        'code': '',   // Mã phiên bản
        'price': 0,    // Giá phiên bản
        'description': '',   // Mô tả phiên bản
        'remarks': '',   // Thông tin ghi chú

      }
    },
    initOptions() {
    },
    openViewClinicVersionDialog(clinicVersion) {
      this.dialogProps.action = 'view'
      this.dialogProps.title = 'Xem phiên bản phòng khám'
      this.bizFormModel = {...this.initFormModel(), ...clinicVersion}
      this.initOptions(this.bizFormModel)
      this.tabIndex = '1'
      this.dialogProps.visible = true
    },
    openEditClinicVersionDialog(clinicVersion) {
      this.dialogProps.action = 'edit'
      this.dialogProps.title = 'Sửa phiên bản phòng khám'
      this.bizFormModel = {...this.initFormModel(), ...clinicVersion}
      this.initOptions(this.bizFormModel)
      this.tabIndex = '1'
      this.dialogProps.visible = true
    },
    openAddClinicVersionDialog() {
      this.dialogProps.action = 'add'
      this.dialogProps.title = 'Thêm phiên bản phòng khám'
      this.bizFormModel = this.initFormModel()
      this.initOptions(this.bizFormModel)
      this.tabIndex = '1'
      this.dialogProps.visible = true
    },
    openCopyClinicVersionDialog(clinicVersion) {
      this.dialogProps.action = 'add'
      this.dialogProps.title = 'Thêm phiên bản phòng khám'
      this.bizFormModel = {...this.initFormModel(), ...clinicVersion}
      this.initOptions(this.bizFormModel)
      this.tabIndex = '1'
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