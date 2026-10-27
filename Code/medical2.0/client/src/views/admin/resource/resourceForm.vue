<template>
  <el-dialog :title='dialogProps.title' :visible.sync='dialogProps.visible' :close-on-click-modal='false' width='50%' 
    @open='onDialogOpen()' v-loading='loading'>
    <div slot='title' class='dialog-header'>
      {{ dialogProps.title }}
      <OperationIcon v-show='dialogProps.action === "view" && permission.edit' type='primary' text='Sửa' placement='top-start' icon-name='el-icon-edit' @click='switchEdit'></OperationIcon>
    </div>
    <el-form :model='bizFormModel' :rules='formRules' 
      ref='resourceForm' label-width='120px' label-position='right' class='edit-form'>    

              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Tên' prop='name' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.name' :maxlength='128' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập tên"' autofocus></el-input>
          </el-form-item>
        </el-col>
                <el-col :span='24/2'>
          <el-form-item label='Mã nguồn' prop='code' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.code' :maxlength='64' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập mã"' ></el-input>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Url' prop='url' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.url' :maxlength='128' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhậpUrl"' ></el-input>
          </el-form-item>
        </el-col>
                <el-col :span='24/2'>
          <el-form-item label='Quyền' prop='permission' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.permission' :maxlength='32' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập quyền"' ></el-input>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Có thể cấp quyền' prop='canPermission' >
            <el-switch :disabled='dialogProps.action === "view"' v-model='bizFormModel.canPermission' active-color='#13ce66' inactive-color='#dbdfe6' active-value='1' inactive-value='0'></el-switch>
          </el-form-item>
        </el-col>
                <el-col :span='24/2'>
          <el-form-item label='Quyền mặc định' prop='isDefault' >
            <el-switch :disabled='dialogProps.action === "view"' v-model='bizFormModel.isDefault' active-color='#13ce66' inactive-color='#dbdfe6' active-value='1' inactive-value='0'></el-switch>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Vô hiệu hóa' prop='isLocked' >
            <el-switch :disabled='dialogProps.action === "view"' v-model='bizFormModel.isLocked' active-color='#13ce66' inactive-color='#dbdfe6' :active-value='1' :inactive-value='0'></el-switch>
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
    </el-form>
    <span slot='footer' class='dialog-footer'>
      <el-button v-if='dialogProps.action !== "view"' :disabled="flag" type='primary' :plain='true' @click='onSubmit("resourceForm")'>Lưu</el-button>
      <el-button v-if='dialogProps.action !== "view"' :plain='true' @click='onDialogClose()'>Hủy</el-button>
      <el-button v-if='dialogProps.action === "view"' :plain='true' @click='onDialogClose()'>Đóng</el-button>
    </span>    
  </el-dialog>
</template>
<script>
import { validatenull } from '@/utils/validate'
import { saveResource } from '@/api/admin/resource'
import BaseUI from '@/views/components/baseUI'
import OperationIcon from '@/components/OperationIcon'
export default {
  extends: BaseUI,
  name: 'resource-form',
  components: {
    OperationIcon
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
            { required: true, message: 'Vui lòng nhập tên', trigger: 'blur' }
        ],
        'code': [
            { required: true, message: 'Vui lòng nhập mã', trigger: 'blur' }
        ],
        'url': [
            { required: true, message: 'Vui lòng nhậpUrl', trigger: 'blur' }
        ],
        'permission': [
            { required: true, message: 'Vui lòng nhập quyền', trigger: 'blur' }
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
      saveResource(this.bizFormModel).then(responseData => {
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
      this.dialogProps.title = 'Sửa tài nguyên'
      this.initOptions(this.bizFormModel)
    },
    onDialogClose() {
      this.dialogProps.visible = false  
    },
    onDialogOpen() {
      this.$nextTick(() => {
        this.$refs['resourceForm'].clearValidate()
      })
    },
    initFormModel(This) {
      return {
        'name': '',   // Tên
        'code': This ? 'This.router.code' : null,   // Mã nguồn
        'url': This ? 'This.router.url' : null,   // Url
        'permission': '',   // Quyền
        'canPermission': '1',   // Có thể cấp quyền
        'isDefault': '0',   // Quyền mặc định
        'isLocked': 0,    // Vô hiệu hóa
        'router': {     // Bảng cha Định tuyến
          'id': validatenull(This) || validatenull(This.router) ? null : This.router.id,
          'name': validatenull(This) || validatenull(This.router) ? null : This.router.name,
        },
        'remarks': '',   // Thông tin ghi chú

      }
    },
    initOptions(This) {

    },
    openViewResourceDialog(resource) {
      this.dialogProps.action = 'view'
      this.dialogProps.title = 'Xem tài nguyên'
      this.bizFormModel = resource
      this.dialogProps.visible = true
    },
    openEditResourceDialog(resource) {
      this.dialogProps.action = 'edit'
      this.dialogProps.title = 'Sửa tài nguyên'
      this.bizFormModel = resource
      this.initOptions(this.bizFormModel)
      this.dialogProps.visible = true
    },
    openAddResourceDialog(parent) {
      this.dialogProps.action = 'add'
      this.dialogProps.title = 'Thêm tài nguyên'
      this.bizFormModel = this.initFormModel(parent)
      this.initOptions(this.bizFormModel)
      this.dialogProps.visible = true
    },
    openCopyResourceDialog(resource) {
      this.dialogProps.action = 'add'
      this.dialogProps.title = 'Thêm tài nguyên'
      this.bizFormModel = resource
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