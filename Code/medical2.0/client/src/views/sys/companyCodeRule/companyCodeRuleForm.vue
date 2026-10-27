<template>
  <el-dialog :title='dialogProps.title' :visible.sync='dialogProps.visible' :close-on-click-modal='false' width='30%' 
    @open='onDialogOpen()' v-loading='loading'>
    <div slot='title' class='dialog-header'>
      {{ dialogProps.title }}
      <OperationIcon v-show='dialogProps.action === "view" && permission.edit' type='primary' text='Sửa' placement='top-start' icon-name='el-icon-edit' @click='switchEdit'></OperationIcon>
    </div>
    <el-form :model='bizFormModel' :rules='formRules' 
      ref='companyCodeRuleForm' label-width='120px' label-position='right' class='edit-form'>    
              <el-row>
        <el-col :span='24/1'>
          <el-form-item label='Quy tắc' prop='ruleDef'>
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.ruleDef' :maxlength='512' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập quy tắc"' autofocus></el-input>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col>
          <el-form-item label='Thông tin ghi chú' prop='remarks'>        
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.remarks' type='textarea'  
             :maxlength='255' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập thông tin ghi chú"' ></el-input>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <span slot='footer' class='dialog-footer'>
      <el-button v-if='dialogProps.action !== "view"' :disabled="flag" type='primary' :plain='true' @click='onSubmit("companyCodeRuleForm")'>Lưu</el-button>    
      <el-button v-if='dialogProps.action !== "view"' :plain='true' @click='onDialogClose()'>Hủy</el-button>
      <el-button v-if='dialogProps.action === "view"' :plain='true' @click='onDialogClose()'>Đóng</el-button>
    </span>    
  </el-dialog>
</template>
<script>
import { validatenull } from '@/utils/validate'
import { saveCompanyCodeRule } from '@/api/sys/companyCodeRule'
import BaseUI from '@/views/components/baseUI'
import OperationIcon from '@/components/OperationIcon'
export default {
  extends: BaseUI,
  name: 'companyCodeRule-form',
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
        'ruleDef': [
            { required: true, message: 'Vui lòng nhập quy tắc', trigger: 'blur' }
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
      saveCompanyCodeRule(this.bizFormModel).then(responseData => {
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
    onDialogClose() {
      this.dialogProps.visible = false  
    },
    onDialogOpen() {
      this.$nextTick(() => {
        this.$refs['companyCodeRuleForm'].clearValidate()
      })
    },
    switchEdit() {
      this.dialogProps.action = 'view'
      this.dialogProps.title = 'Xem quy tắc mã công ty'
      this.initOptions(this.bizFormModel)
    },
    initFormModel(This) {
      return {
        'company': {     // Công ty
          'id': null,
          'name': null,
        },
        'sysRule': {     // 系统编号规则
          'id': null,
          'name': null,
        },
        'code': '',   // Mã
        'name': '',   // Tên
        'ruleDef': '',   // Quy tắc
        'remarks': '',   // Thông tin ghi chú

      }
    },
    initOptions(This) {
    },
    openViewCompanyCodeRuleDialog(companyCodeRule) {
      this.dialogProps.action = 'view'
      this.dialogProps.title = 'Xem quy tắc mã công ty'
      this.bizFormModel = companyCodeRule
      this.dialogProps.visible = true
    },
    openEditCompanyCodeRuleDialog(companyCodeRule) {
      this.dialogProps.action = 'edit'
      this.dialogProps.title = '修改公司编码规则'
      this.bizFormModel = companyCodeRule
      this.initOptions(this.bizFormModel)
      this.dialogProps.visible = true
    },
    openAddCompanyCodeRuleDialog() {
      this.dialogProps.action = 'add'
      this.dialogProps.title = 'Thêm quy tắc mã công ty'
      this.bizFormModel = this.initFormModel()
      this.initOptions(this.bizFormModel)
      this.dialogProps.visible = true
    },
    openCopyCompanyCodeRuleDialog(companyCodeRule) {
      this.dialogProps.action = 'add'
      this.dialogProps.title = 'Thêm quy tắc mã công ty'
      this.bizFormModel = companyCodeRule
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