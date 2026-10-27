<template>
  <el-dialog :title='dialogProps.title' :visible.sync='dialogProps.visible' :close-on-click-modal='false' width='50%' 
    @open='onDialogOpen()' v-loading='loading'>
    <div slot='title' class='dialog-header'>
      {{ dialogProps.title }}
      <OperationIcon v-show='dialogProps.action === "view" && permission.edit' type='primary' text='Sửa' placement='top-start' icon-name='el-icon-edit' @click='switchEdit'></OperationIcon>
    </div>
    <el-form :model='bizFormModel' :rules='formRules' 
      ref='propertySetForm' label-width='120px' label-position='right' class='edit-form'>    

              <el-row>
        <el-col :span='24/1'>
          <el-form-item label='Tên' prop='name' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.name' :maxlength='128' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập tên"' autofocus></el-input>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/1'>
          <el-form-item label='Định nghĩa thuộc tính' prop='propertiesDef' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.propertiesDef'  :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập định nghĩa thuộc tính"' ></el-input>
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
      <el-button v-if='dialogProps.action !== "view"' :disabled="flag" type='primary' :plain='true' @click='onSubmit("propertySetForm")'>Lưu</el-button>
      <el-button v-if='dialogProps.action !== "view"' :plain='true' @click='onDialogClose()'>Hủy</el-button>
      <el-button v-if='dialogProps.action === "view"' :plain='true' @click='onDialogClose()'>Đóng</el-button>
    </span>    
  </el-dialog>
</template>
<script>
import { validatenull } from '@/utils/validate'
import { savePropertySet } from '@/api/sys/propertySet'
import BaseUI from '@/views/components/baseUI'
import OperationIcon from '@/components/OperationIcon'
export default {
  extends: BaseUI,
  name: 'propertySet-form',
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
        'propertiesDef': [
            { required: true, message: 'Vui lòng nhập định nghĩa thuộc tính', trigger: 'blur' }
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
      savePropertySet(this.bizFormModel).then(responseData => {
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
      this.dialogProps.title = 'Sửa tập thuộc tính'
      this.initOptions(this.bizFormModel)
    },
    onDialogClose() {
      this.dialogProps.visible = false  
    },
    onDialogOpen() {
      this.$nextTick(() => {
        this.$refs['propertySetForm'].clearValidate()
      })
    },
    initFormModel(This) {
      return {
        'name': '',   // Tên
        'propertiesDef': '',   // Định nghĩa thuộc tính
        'remarks': '',   // Thông tin ghi chú

      }
    },
    initOptions(This) {
    },
    openViewPropertySetDialog(propertySet) {
      this.dialogProps.action = 'view'
      this.dialogProps.title = '查看属性集'
      this.bizFormModel = propertySet
      this.initOptions(this.bizFormModel)
      this.dialogProps.visible = true
    },
    openEditPropertySetDialog(propertySet) {
      this.dialogProps.action = 'edit'
      this.dialogProps.title = 'Sửa tập thuộc tính'
      this.bizFormModel = propertySet
      this.initOptions(this.bizFormModel)
      this.dialogProps.visible = true
    },
    openAddPropertySetDialog() {
      this.dialogProps.action = 'add'
      this.dialogProps.title = 'Thêm tập thuộc tính'
      this.bizFormModel = this.initFormModel()
      this.initOptions(this.bizFormModel)
      this.dialogProps.visible = true
    },
    openCopyPropertySetDialog(propertySet) {
      this.dialogProps.action = 'add'
      this.dialogProps.title = 'Thêm tập thuộc tính'
      this.bizFormModel = propertySet
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