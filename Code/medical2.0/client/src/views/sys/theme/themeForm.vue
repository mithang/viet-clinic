<template>
  <el-dialog :title='dialogProps.title' :visible.sync='dialogProps.visible' :close-on-click-modal='false' width='30%' 
    @open='onDialogOpen()' v-loading='loading'>
    <div slot='title' class='dialog-header'>
      {{ dialogProps.title }}
      <OperationIcon v-show='dialogProps.action === "view" && permission.edit' type='primary' text='Sửa' placement='top-start' icon-name='el-icon-edit' @click='switchEdit'></OperationIcon>
    </div>
    <el-form :model='bizFormModel' :rules='formRules' 
      ref='themeForm' label-width='120px' label-position='right' class='edit-form'>    

              <el-row>
        <el-col :span='24/1'>
          <el-form-item label='Tên' prop='name'>
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.name' :maxlength='128' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập tên"' autofocus></el-input>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col>
          <el-form-item label='Chủ đề' prop='theme'>        
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.theme' type='textarea'  
             :maxlength='2000' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập chủ đề"' ></el-input>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/1'>
          <el-form-item label='Mặc định' prop='isDefault'>
            <el-switch :disabled='dialogProps.action === "view"' v-model='bizFormModel.isDefault' active-color='#13ce66' inactive-color='#dbdfe6' active-value='1' inactive-value='0'></el-switch>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <span slot='footer' class='dialog-footer'>
      <el-button v-if='dialogProps.action !== "view"' :disabled="flag" type='primary' :plain='true' @click='onSubmit("themeForm")'>Lưu</el-button>
      <el-button v-if='dialogProps.action !== "view"' :plain='true' @click='onDialogClose()'>Hủy</el-button>
      <el-button v-if='dialogProps.action === "view"' :plain='true' @click='onDialogClose()'>Đóng</el-button>
    </span>    
  </el-dialog>
</template>
<script>
import { validatenull } from '@/utils/validate'
import { saveTheme } from '@/api/sys/theme'
import BaseUI from '@/views/components/baseUI'
import OperationIcon from '@/components/OperationIcon'
export default {
  extends: BaseUI,
  name: 'theme-form',
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
        'theme': [
            { required: true, message: 'Vui lòng nhập chủ đề', trigger: 'blur' }
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
      saveTheme(this.bizFormModel).then(responseData => {
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
      this.dialogProps.title = 'Sửa chủ đề hệ thống'
      this.initOptions(this.bizFormModel)
    },
    onDialogClose() {
      this.dialogProps.visible = false  
    },
    onDialogOpen() {
      this.$nextTick(() => {
        this.$refs['themeForm'].clearValidate()
      })
    },
    initFormModel(This) {
      return {
        'name': '',   // Tên
        'theme': '',   // Chủ đề
        'isDefault': 1,   // Mặc định
        'remarks': '',   // Thông tin ghi chú

      }
    },
    initOptions(This) {
    },
    openViewThemeDialog(theme) {
      this.dialogProps.action = 'view'
      this.dialogProps.title = '查看系统主题'
      this.bizFormModel = theme
      this.initOptions(this.bizFormModel)
      this.dialogProps.visible = true
    },
    openEditThemeDialog(theme) {
      this.dialogProps.action = 'edit'
      this.dialogProps.title = 'Sửa chủ đề hệ thống'
      this.bizFormModel = theme
      this.initOptions(this.bizFormModel)
      this.dialogProps.visible = true
    },
    openAddThemeDialog() {
      this.dialogProps.action = 'add'
      this.dialogProps.title = 'Thêm chủ đề hệ thống'
      this.bizFormModel = this.initFormModel()
      this.initOptions(this.bizFormModel)
      this.dialogProps.visible = true
    },
    openCopyThemeDialog(theme) {
      this.dialogProps.action = 'add'
      this.dialogProps.title = 'Thêm chủ đề hệ thống'
      this.bizFormModel = theme
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