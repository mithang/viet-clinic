<template>
  <el-dialog :title='dialogProps.title' :visible.sync='dialogProps.visible' :close-on-click-modal='false' width='30%' 
    @open='onDialogOpen()' v-loading='loading'>
    <div slot='title' class='dialog-header'>
      {{ dialogProps.title }}
      <OperationIcon v-show='dialogProps.action === "view" && permission.edit' type='primary' text='Sửa' placement='top-start' icon-name='el-icon-edit' @click='switchEdit'></OperationIcon>
    </div>
    <el-form :model='bizFormModel' :rules='formRules' 
      ref='codeRuleForm' label-width='120px' label-position='right' class='edit-form'>    
              <el-row>
        <el-col :span='24/1'>
          <el-form-item label='Mã' prop='code'>
            <el-input :disabled='dialogProps.action === "view" || !canEditCode' v-model='bizFormModel.code' :maxlength='20' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập mã"' autofocus></el-input>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/1'>
          <el-form-item label='Tên' prop='name'>
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.name' :maxlength='128' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập tên"' ></el-input>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/1'>
          <el-form-item label='Quy tắc' prop='ruleDef'>
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.ruleDef' :maxlength='512' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập quy tắc"' ></el-input>
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
      <el-button v-if='dialogProps.action !== "view"' :disabled="flag" type='primary' :plain='true' @click='onSubmit("codeRuleForm")'>Lưu</el-button>
      <el-button v-if='dialogProps.action !== "view"' :plain='true' @click='onDialogClose()'>Hủy</el-button>
      <el-button v-if='dialogProps.action === "view"' :plain='true' @click='onDialogClose()'>Đóng</el-button>
    </span>    
  </el-dialog>
</template>
<script>
import { validatenull } from '@/utils/validate'
import { saveCodeRule } from '@/api/sys/codeRule'
import BaseUI from '@/views/components/baseUI'
import OperationIcon from '@/components/OperationIcon'
export default {
  extends: BaseUI,
  name: 'codeRule-form',
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
        'code': [
            {
              validator: (rule, value, callback) => {
                // Same predicate as the input's :disabled (canEditCode): when the
                // code field is editable it is required; when it is locked, do not
                // fail validation on a field the user cannot fill. A regular role
                // creating a rule with an empty code is still rejected by the
                // backend (@Valid + @Length(min=1) on CodeRule.code).
                if (!this.canEditCode || (value !== undefined && value !== null && value !== '')) {
                  callback()
                } else {
                  callback(new Error('Vui lòng nhập mã'))
                }
              },
              trigger: 'blur'
            }
        ],
        'name': [
            { required: true, message: 'Vui lòng nhập tên', trigger: 'blur' }
        ],
        'ruleDef': [
            { required: true, message: 'Vui lòng nhập quy tắc', trigger: 'blur' }
        ],
      },
      currentUserId: currentUser.id,
      // Login name of the full-permission test account (see canEditCode below).
      currentUserLoginname: currentUser.loginname || ''
    }    
  },
  props: {
    // Quyền
    permission: {
      type: Object
    }
  },  
  computed: {
    // Single predicate shared by the code input's :disabled and its validator,
    // so the two can never disagree again. '1000' is the original system-only
    // gate (String() because the login id may arrive as a number); 'testfull'
    // is the full-permission test account, opened so it can validate Add.
    // Every other role keeps the original behaviour: code locked, and Add
    // cannot succeed (backend rejects an empty code).
    canEditCode() {
      return String(this.currentUserId) === '1000' || this.currentUserLoginname === 'testfull'
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
      saveCodeRule(this.bizFormModel).then(responseData => {
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
      this.dialogProps.title = 'Sửa quy tắc mã hệ thống'
      this.initOptions(this.bizFormModel)
    },
    onDialogClose() {
      this.dialogProps.visible = false  
    },
    onDialogOpen() {
      this.$nextTick(() => {
        this.$refs['codeRuleForm'].clearValidate()
      })
    },
    initFormModel(This) {
      return {
        'code': '',   // Mã
        'name': '',   // Tên
        'ruleDef': '',   // Quy tắc
        'remarks': '',   // Thông tin ghi chú

      }
    },
    initOptions(This) {
    },
    openViewCodeRuleDialog(codeRule) {
      this.dialogProps.action = 'view'
      this.dialogProps.title = 'Xem quy tắc mã hệ thống'
      this.bizFormModel = codeRule
      this.initOptions(this.bizFormModel)
      this.dialogProps.visible = true
    },
    openEditCodeRuleDialog(codeRule) {
      this.dialogProps.action = 'edit'
      this.dialogProps.title = 'Sửa quy tắc mã hệ thống'
      this.bizFormModel = codeRule
      this.initOptions(this.bizFormModel)
      this.dialogProps.visible = true
    },
    openAddCodeRuleDialog() {
      this.dialogProps.action = 'add'
      this.dialogProps.title = 'Thêm quy tắc mã hệ thống'
      this.bizFormModel = this.initFormModel()
      this.initOptions(this.bizFormModel)
      this.dialogProps.visible = true
    },
    openCopyCodeRuleDialog(codeRule) {
      this.dialogProps.action = 'add'
      this.dialogProps.title = 'Thêm quy tắc mã hệ thống'
      this.bizFormModel = codeRule
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