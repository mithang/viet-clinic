<template>
  <el-dialog :title='dialogProps.title' :visible.sync='dialogProps.visible' :close-on-click-modal='false' width='30%' 
    @open='onDialogOpen()' v-loading='loading'>
    <div slot='title' class='dialog-header'>
      {{ dialogProps.title }}
      <OperationIcon v-show='dialogProps.action === "view" && permission.edit' type='primary' text='Sửa' placement='top-start' icon-name='el-icon-edit' @click='switchEdit'></OperationIcon>
    </div>

    <el-form :model='bizFormModel' :rules='formRules' 
      ref='lesseeForm' label-width='120px' label-position='right' class='edit-form'>  
      <div class="tab-item" v-show='tabIndex==="1"'>  
              <el-row>
        <el-col :span='24/1'>
          <el-form-item label='Tên đơn vị thuê' prop='name' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.name' :maxlength='100' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập tên đơn vị thuê"' autofocus></el-input>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/1'>
          <el-form-item label='Số điện thoại' prop='phoneNumber' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.phoneNumber' :maxlength='20' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập số điện thoại"' ></el-input>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/1'>
          <el-form-item label='Mã thuê bao' prop='code' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.code' :maxlength='100' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập mã thuê bao"' ></el-input>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/1'>
          <el-form-item label='Họ tên người liên hệ' prop='contactName' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.contactName' :maxlength='50' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập họ tên người liên hệ"' ></el-input>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/1'>
          <el-form-item label='Địa chỉ (Tỉnh/Thành phố)' prop='addressProvince' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.addressProvince' :maxlength='10' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập địa chỉ (Tỉnh/Thành phố)"' ></el-input>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/1'>
          <el-form-item label='Địa chỉ (Thành phố)' prop='addressCity' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.addressCity' :maxlength='10' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập địa chỉ (Thành phố)"' ></el-input>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/1'>
          <el-form-item label='Địa chỉ (Quận/Huyện)' prop='addressRegion' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.addressRegion' :maxlength='10' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập địa chỉ (Quận/Huyện)"' ></el-input>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/1'>
          <el-form-item label='Địa chỉ liên hệ' prop='address' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.address' :maxlength='100' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập địa chỉ liên hệ"' ></el-input>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/1'>
          <el-form-item label='Trạng thái' prop='status' >
            <el-switch :disabled='dialogProps.action === "view"' v-model='bizFormModel.status' active-color='#13ce66' inactive-color='#dbdfe6' active-value='1' inactive-value='0'></el-switch>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col>
          <el-form-item label='Ghi chú' prop='remarks' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.remarks' type='textarea'
                                  :maxlength='255' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập ghi chú"'  clearable></el-input>
          </el-form-item>
        </el-col>
      </el-row>
      </div>
    </el-form>
    <span slot='footer' class='dialog-footer'>
      <el-button v-if='dialogProps.action !== "view"' :disabled="flag" type='primary' :plain='true' @click='onSubmit("lesseeForm")'>Lưu</el-button>
      <el-button v-if='dialogProps.action !== "view"' :plain='true' @click='onDialogClose()'>Hủy</el-button>
      <el-button v-if='dialogProps.action === "view"' :plain='true' @click='onDialogClose()'>Đóng</el-button>
    </span>    
  </el-dialog>
</template>
<script>
import { validatenull } from '@/utils/validate'
import { saveTenant } from '@/api/tenant/tenant'
import BaseUI from '@/views/components/baseUI'
import OperationIcon from '@/components/OperationIcon'
export default {
  extends: BaseUI,
  name: 'lessee-form',
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
        title: ''
      },
      formRules: {
        'name': [
            { required: true, message: 'Vui lòng nhập tên đơn vị thuê', trigger: 'blur' }
        ],
        'phoneNumber': [
            { required: true, message: 'Vui lòng nhập số điện thoại', trigger: 'blur' }
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
      saveTenant(this.bizFormModel).then(responseData => {
        if(responseData.code === 100) {
          this.flag=false
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
      this.dialogProps.title = 'Sửa đơn vị thuê'
      this.initOptions(this.bizFormModel)
    },
    onDialogClose() {
      this.dialogProps.visible = false  
    },
    onDialogOpen() {
      this.$nextTick(() => {
        this.$refs['lesseeForm'].clearValidate()
      })
    },
    initFormModel(This) {
      return {
        'name': '',   // Tên đơn vị thuê
        'phoneNumber': '',   // Số điện thoại
        'code': '',   // 
        'contactName': '',   // Họ tên người liên hệ
        'addressProvince': '', // Địa chỉ (Tỉnh/Thành phố)
        'addressCity': '', // Địa chỉ (Thành phố)
        'addressRegion': '', // Địa chỉ (Quận/Huyện)
        'address': '',   // Địa chỉ liên hệ
        'status': '1',   // Trạng thái
        'remarks': '',   // Ghi chú

      }
    },
    initOptions(This) {
    },
    openViewLesseeDialog(lessee) {
      this.dialogProps.action = 'view'
      this.dialogProps.title = 'Xem đơn vị thuê'
      this.bizFormModel = {...this.initFormModel(), ...lessee}
      this.initOptions(this.bizFormModel)
      //debugger
      this.tabIndex = '1'
      this.dialogProps.visible = true
    },
    openEditLesseeDialog(lessee) {
      this.dialogProps.action = 'edit'
      this.dialogProps.title = 'Sửa đơn vị thuê'
      this.bizFormModel = {...this.initFormModel(), ...lessee}
      this.initOptions(this.bizFormModel)
      this.tabIndex = '1'
      this.dialogProps.visible = true
    },
    openAddLesseeDialog() {
      this.dialogProps.action = 'add'
      this.dialogProps.title = 'Thêm đơn vị thuê'
      this.bizFormModel = this.initFormModel()
      this.initOptions(this.bizFormModel)
      this.tabIndex = '1'
      this.dialogProps.visible = true
    },
    openCopyLesseeDialog(lessee) {
      this.dialogProps.action = 'add'
      this.dialogProps.title = 'Thêm đơn vị thuê'
      this.bizFormModel = {...this.initFormModel(), ...lessee}
      this.initOptions(this.bizFormModel)
      this.tabIndex = '1'
      this.bizFormModel.id = null //ĐặtidĐặt thành trống, Thêm mới một
      this.dialogProps.visible = true
    }
  },
  watch: {
  }
}
</script>