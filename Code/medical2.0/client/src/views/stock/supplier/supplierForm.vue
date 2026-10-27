<template>
  <el-dialog :title='dialogProps.title' :visible.sync='dialogProps.visible' :close-on-click-modal='false' width='50%'
    @open='onDialogOpen()' v-loading='loading'>
    <div slot='title' class='dialog-header'>
      {{ dialogProps.title }}
      <OperationIcon v-show='dialogProps.action === "view" && permission.edit' type='primary' text='Sửa' placement='top-start' icon-name='el-icon-edit' @click='switchEdit'></OperationIcon>
    </div>

    <el-form :model='bizFormModel' :rules='formRules'
      ref='supplierForm' label-width='120px' label-position='right' class='edit-form' style="marginTop: 10px">
      <div class="tab-item" v-show='tabIndex==="1"'>
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Tên' prop='name' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.name' :maxlength='64' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập tên"' autofocus @input="pinyinInput"></el-input>
          </el-form-item>
        </el-col>
                <el-col :span='24/2'>
          <el-form-item label='Người liên hệ' prop='linkman' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.linkman' :maxlength='45' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập người liên hệ"' ></el-input>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
           <el-col :span='24/2'>
          <el-form-item label='Mã pinyin' prop='pinyinCode' >
            <el-input disabled=true v-model='bizFormModel.pinyinCode' :maxlength='128' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập mã pinyin"' ></el-input>
          </el-form-item>
        </el-col>
        <el-col :span='24/2'>
          <el-form-item label='Điện thoại' prop='phone' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.phone' :maxlength='45' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập số điện thoại"' ></el-input>
          </el-form-item>
        </el-col>

      </el-row>
              <el-row>
        <el-col :span='24/2'>
           <el-form-item label='Mã tín dụng' prop='creditCode' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.creditCode' :maxlength='128' :placeholder='dialogProps.action === "view"? "" : "请输入信用代码"' ></el-input>
          </el-form-item>
        </el-col>
         <el-col :span='24/2'>
          <el-form-item label='Email' prop='mail' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.mail' :maxlength='128' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập email"' ></el-input>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
         <el-col :span='24/1'>
              <el-form-item label='Có kích hoạt không' prop='status' >
            <el-switch :disabled='dialogProps.action === "view"' v-model='value' active-color='#13ce66' inactive-color='#dbdfe6' active-value='1' inactive-value='0'></el-switch>
          </el-form-item>
         </el-col>
      </el-row>
              <el-row>

          <el-form-item label='Khu vực' prop='' >
             <v-distpicker :disabled='dialogProps.action === "view"' :province="province" :city="city" :area="area"  @province="onChangeProvince" @city="onChangeCity" @area="onChangeArea"></v-distpicker>
          </el-form-item>
      </el-row>
      <el-row>
        <el-col :span="24/1">
        <el-form-item label='供应商分类' prop='type'>
          <div>
            <el-radio :disabled='dialogProps.action === "view"' v-model="bizFormModel.type" label="1" border>Nhà cung cấp thuốc</el-radio>
          <el-radio :disabled='dialogProps.action === "view"' v-model="bizFormModel.type" label="2" border>Nhà cung cấp vật tư</el-radio>
          </div>
        </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col>
          <el-form-item label='Địa chỉ' prop='address' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.address' type='textarea'
                                  :maxlength='128' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập địa chỉ"'  clearable></el-input>
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
      <el-button v-if='dialogProps.action !== "view"' :disabled="flag" type='primary' :plain='true' @click='onSubmit("supplierForm")'>Lưu</el-button>
      <el-button v-if='dialogProps.action !== "view"' :plain='true' @click='onDialogClose()'>Hủy</el-button>
      <el-button v-if='dialogProps.action === "view"' :plain='true' @click='onDialogClose()'>Đóng</el-button>
    </span>
  </el-dialog>
</template>
<script>
import { validatenull } from '@/utils/validate'
import { saveSupplier } from '@/api/stock/supplier'
import BaseUI from '@/views/components/baseUI'
import OperationIcon from '@/components/OperationIcon'
import VDistpicker from 'v-distpicker'

let pinyin = require('js-pinyin');
export default {
  extends: BaseUI,
  name: 'supplier-form',
  components: {
    OperationIcon,
    VDistpicker
  },
  data() {
    return {
      bizFormModel: this.initFormModel(),
      tabIndex: '1',
      //Liên kết ba cấp tỉnh/thành phố/quận/huyện
      eprovinc : '',
      city : '',
      area : '',
      value:'1',
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
        type: [
            { required: true, message: 'Vui lòng chọn loại nhà cung cấp', trigger: 'change' }
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
      this.bizFormModel.province=this.province
      this.bizFormModel.city=this.city
      this.bizFormModel.area=this.area
      this.bizFormModel.status=this.value
      saveSupplier(this.bizFormModel).then(responseData => {
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
      this.dialogProps.title = 'Sửa quản lý nhà cung cấp'
      this.initOptions(this.bizFormModel)
    },
    onDialogClose() {
      this.dialogProps.visible = false
    },
    onDialogOpen() {
      this.$nextTick(() => {
        this.$refs['supplierForm'].clearValidate()
      })
    },
    initFormModel(This) {
      return {
        'company': {     // Phòng khámid
          'id': currentUser.company.id,
          'name': null,
        },
        'name': '',   // Tên
        'linkman': '',   // Người liên hệ
        'phone': '',   // Điện thoại
        'mail': '',   // Email
        'province': '',   // Tỉnh/Thành phố
        'city': '',   // Thành phố
        'creditCode': '',   // Mã tín dụng
        'area': '',   // Quận/Huyện
        'address': '',   // Địa chỉ
        'remarks': '',   // Thông tin ghi chú

      }
    },
    initOptions(This) {
    },
    // Lấy giá trị liên kết ba cấp tỉnh/thành phố/quận/huyện
    onChangeProvince(data) {
        this.province=data.value

    },
    onChangeCity(data){
      this.city=data.value

    },
    onChangeArea(data){
      this.area=data.value

    },
     //Mã pinyin của ô nhập
     pinyinInput(value){
       this.bizFormModel.pinyinCode=pinyin.getCamelChars(value)

     },
    openViewSupplierDialog(supplier) {
      this.dialogProps.action = 'view'
      this.dialogProps.title = '查看供应商管理'
      this.bizFormModel = {...this.initFormModel(), ...supplier}
      this.initOptions(this.bizFormModel)
      this.value = this.bizFormModel.status
      this.tabIndex = '1'
      this.dialogProps.visible = true
      this.province = this.bizFormModel.province
      this.city = this.bizFormModel.city
      this.area = this.bizFormModel.area
    },
    openEditSupplierDialog(supplier) {
      this.dialogProps.action = 'edit'
      this.dialogProps.title = 'Sửa quản lý nhà cung cấp'
      this.bizFormModel = {...this.initFormModel(), ...supplier}
      this.initOptions(this.bizFormModel)
      this.value = this.bizFormModel.status
      this.tabIndex = '1'
      this.dialogProps.visible = true
      this.province = this.bizFormModel.province
      this.city = this.bizFormModel.city
      this.area = this.bizFormModel.area
    },
    openAddSupplierDialog() {
      this.dialogProps.action = 'add'
      this.dialogProps.title = 'Thêm quản lý nhà cung cấp'
      this.bizFormModel = this.initFormModel()
      this.initOptions(this.bizFormModel)
      this.tabIndex = '1'
      this.dialogProps.visible = true
      this.province = ''
      this.city = ''
      this.area = ''
    },
    openCopySupplierDialog(supplier) {
      this.dialogProps.action = 'add'
      this.dialogProps.title = 'Thêm quản lý nhà cung cấp'
      this.bizFormModel = {...this.initFormModel(), ...supplier}
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
<style lang="scss">
  .distpicker-address-wrapper {
  select {
    padding: 0px 10px !important;
    height: 30px !important;
    font-size: 15px !important;
    line-height: 30px !important;
  }
}
</style>
