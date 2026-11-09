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
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.creditCode' :maxlength='128' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập mã tín dụng"' ></el-input>
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
             <el-select v-model='province' placeholder='Chọn tỉnh/thành phố' filterable clearable :disabled='dialogProps.action === "view"' @change='onChangeProvince' style='width:48%;margin-right:4%'>
               <el-option v-for='item in province_List' :key='item.id' :label='item.name' :value='item.id'></el-option>
             </el-select>
             <el-select v-model='area' placeholder='Chọn xã/phường' filterable clearable :disabled='dialogProps.action === "view"' @change='onChangeArea' style='width:48%'>
               <el-option v-for='item in area_List' :key='item.id' :label='item.name' :value='item.id'></el-option>
             </el-select>
          </el-form-item>
      </el-row>
      <el-row>
        <el-col :span="24/1">
        <el-form-item label='Phân loại nhà cung cấp' prop='type'>
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
import { saveSupplier } from '@/api/stock/supplier'
import { listAdministrativeDivisionAll } from '@/api/org/administrativeDivision'
import BaseUI from '@/views/components/baseUI'
import OperationIcon from '@/components/OperationIcon'

let pinyin = require('js-pinyin');
export default {
  extends: BaseUI,
  name: 'supplier-form',
  components: {
    OperationIcon
  },
  data() {
    return {
      bizFormModel: this.initFormModel(),
      tabIndex: '1',
      //Liên kết hai cấp tỉnh/xã lấy từ bảng administrative_division
      province : '',
      city : '',
      area : '',
      province_List : [],
      area_List : [],
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
      this.bizFormModel.city='' // Không còn cấp huyện/quận từ khi dùng bảng 34 tỉnh
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
    initFormModel() {
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
    initOptions() {
      this.loadProvinceList()
    },
    // Tỉnh/xã lấy từ bảng administrative_division (34 tỉnh, không còn cấp huyện).
    loadProvinceList() {
      const search = { params: [
        { columnName: 'parten_id', queryType: '=', value: '0' },
        { columnName: 'level', queryType: '=', value: '1' }
      ] }
      listAdministrativeDivisionAll(search).then((responseData) => {
        this.province_List = responseData.data || []
      }).catch(() => {})
    },
    loadAreaList() {
      if (!this.province) {
        this.area_List = []
        return
      }
      const search = { params: [
        { columnName: 'parten_id', queryType: '=', value: this.province },
        { columnName: 'level', queryType: '=', value: '2' }
      ] }
      listAdministrativeDivisionAll(search).then((responseData) => {
        this.area_List = responseData.data || []
      }).catch(() => {})
    },
    onChangeProvince(value) {
      this.province = value
      this.area = ''
      this.loadAreaList()
    },
    onChangeArea(value) {
      this.area = value
    },
     //Mã pinyin của ô nhập
     pinyinInput(value){
       this.bizFormModel.pinyinCode=pinyin.getCamelChars(value)

     },
    openViewSupplierDialog(supplier) {
      this.dialogProps.action = 'view'
      this.dialogProps.title = 'Xem quản lý nhà cung cấp'
      this.bizFormModel = {...this.initFormModel(), ...supplier}
      this.initOptions(this.bizFormModel)
      this.value = this.bizFormModel.status
      this.tabIndex = '1'
      this.dialogProps.visible = true
      this.province = this.bizFormModel.province
      this.city = ''
      this.area = this.bizFormModel.area
      this.loadAreaList()
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
      this.city = ''
      this.area = this.bizFormModel.area
      this.loadAreaList()
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
      this.loadAreaList()
    },
    openCopySupplierDialog(supplier) {
      this.dialogProps.action = 'add'
      this.dialogProps.title = 'Thêm quản lý nhà cung cấp'
      this.bizFormModel = {...this.initFormModel(), ...supplier}
      this.initOptions(this.bizFormModel)
      this.tabIndex = '1'
      this.bizFormModel.id = null
      this.province = this.bizFormModel.province
      this.city = ''
      this.area = this.bizFormModel.area
      this.loadAreaList()
      this.dialogProps.visible = true
    },
  },
  watch: {
  },
  mounted: function() {
  },
}
</script>

