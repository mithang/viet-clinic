<template>
  <el-dialog :title='dialogProps.title' :visible.sync='dialogProps.visible' :close-on-click-modal='false' width='30%'
    @open='onDialogOpen()' v-loading='loading'>
    <div slot='title' class='dialog-header'>
      {{ dialogProps.title }}
      <OperationIcon v-show='dialogProps.action === "view" && permission.edit' type='primary' text='Sửa' placement='top-start' icon-name='el-icon-edit' @click='switchEdit'></OperationIcon>
    </div>
    <el-form :model='bizFormModel' :rules='formRules'
      ref='roleForm' label-width='120px' label-position='right' class='edit-form'
      style="marginTop: 10px">

              <el-row>
        <el-col :span='24/1'>
          <el-form-item label='Mã nguồn' prop='code'>
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.code' :maxlength='32' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập mã"' autofocus></el-input>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/1'>
          <el-form-item label='Tên' prop='name'>
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.name' :maxlength='32' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập tên"' ></el-input>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/1'>
          <el-form-item label='Có kích hoạt không' prop='isLocked'>
            <el-switch :disabled='dialogProps.action === "view"' v-model='bizFormModel.isLocked' active-color='#13ce66' inactive-color='#dbdfe6' :active-value='0' :inactive-value='1'></el-switch>
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
      <el-button v-if='dialogProps.action !== "view"'  :disabled="flag" type='primary' :plain='true' @click='onSubmit("roleForm")'>Lưu</el-button>
      <el-button v-if='dialogProps.action !== "view"' :plain='true' @click='onDialogClose()'>Hủy</el-button>
      <el-button v-if='dialogProps.action === "view"' :plain='true' @click='onDialogClose()'>Đóng</el-button>
    </span>
  </el-dialog>
</template>
<script>
import { validatenull } from '@/utils/validate'
import { treeCompany } from '@/api/org/company'
import { saveRole } from '@/api/admin/role'
import BaseUI from '@/views/components/baseUI'
import OperationIcon from '@/components/OperationIcon'
export default {
  extends: BaseUI,
  name: 'role-form',
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
        'company.id': [
            { required: true, message: 'Vui lòng chọn công ty', trigger: 'change' }
        ],
        'code': [
            { required: true, message: 'Vui lòng nhập mã', trigger: 'blur' }
        ],
        'name': [
            { required: true, message: 'Vui lòng nhập tên', trigger: 'blur' }
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
      saveRole(this.bizFormModel).then(responseData => {
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
      this.dialogProps.title = 'Sửa vai trò'
      this.initOptions(this.bizFormModel)
    },
    onDialogClose() {
      this.dialogProps.visible = false
    },
    onDialogOpen() {
      this.$nextTick(() => {
        this.$refs['roleForm'].clearValidate()
      })
    },
    initFormModel(This) {
      return {
        'company': {     // Bảng cha Công ty
          'id': validatenull(This) || validatenull(This.company) ? null : This.company.id,
          'name': validatenull(This) || validatenull(This.company) ? null : This.company.name,
        },
        'code': '',   // Mã nguồn
        'name': '',   // Tên
        'isLocked': 0,    // Vô hiệu hóa
        'remarks': '',   // Thông tin ghi chú

      }
    },
    initOptions(This) {

    },
    openViewRoleDialog(role) {
      this.dialogProps.action = 'view'
      this.dialogProps.title = '查看角色'
      this.bizFormModel = role
      this.dialogProps.visible = true
    },
    openEditRoleDialog(role) {
      this.dialogProps.action = 'edit'
      this.dialogProps.title = 'Sửa vai trò'
      this.bizFormModel = role
      this.initOptions(this.bizFormModel)
      this.dialogProps.visible = true
    },
    openAddRoleDialog(parent) {
      this.dialogProps.action = 'add'
      this.dialogProps.title = 'Thêm vai trò'
      this.bizFormModel = this.initFormModel(parent)
      this.initOptions(this.bizFormModel)
      this.dialogProps.visible = true
    },
    openCopyRoleDialog(role) {
      this.dialogProps.action = 'add'
      this.dialogProps.title = 'Thêm vai trò'
      this.bizFormModel = role
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
