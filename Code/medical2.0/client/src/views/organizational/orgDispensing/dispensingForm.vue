<template>
  <el-dialog :title='dialogProps.title' :visible.sync='dialogProps.visible' :close-on-click-modal='false' width='50%' 
    @open='onDialogOpen()' v-loading='loading'>
    <div slot='title' class='dialog-header'>
      {{ dialogProps.title }}
      <OperationIcon v-show='dialogProps.action === "view" && permission.edit' type='primary' text='Sửa' placement='top-start' icon-name='el-icon-edit' @click='switchEdit'></OperationIcon>
    </div>

    <el-form :model='bizFormModel' :rules='formRules' 
      ref='dispensingForm' label-width='120px' label-position='right' class='edit-form'>  
      <div class="tab-item" v-show='tabIndex==="1"'>  
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Phòng khámid' prop='company.id' >
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.company.name'></el-input>
            <el-select v-else v-model='bizFormModel.company' value-key='id' filterable clearable placeholder='Vui lòng chọn phòng khámid' 
              @clear='bizFormModel.company={
                "id": null,
                                "name": null,
                }'>
             <el-option v-for='company in company_List' :key='company.id' :label='company.name' :value='company'></el-option>
            </el-select>
          </el-form-item>
        </el-col>
                <el-col :span='24/2'>
          <el-form-item label='Đăng ký khámid' prop='registration.id' >
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.registration.id'></el-input>
            <el-select v-else v-model='bizFormModel.registration' value-key='id' filterable clearable placeholder='Vui lòng chọn đăng ký khámid' 
              @clear='bizFormModel.registration={
                "id": null,
                }'>
             <el-option v-for='registration in registration_List' :key='registration.id' :label='registration.id' :value='registration'></el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Tồn khoid' prop='supplierStock.id' >
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.supplierStock.id'></el-input>
            <el-select v-else v-model='bizFormModel.supplierStock' value-key='id' filterable clearable placeholder='Vui lòng chọn tồn khoid' 
              @clear='bizFormModel.supplierStock={
                "id": null,
                }'>
             <el-option v-for='supplierStock in supplierStock_List' :key='supplierStock.id' :label='supplierStock.id' :value='supplierStock'></el-option>
            </el-select>
          </el-form-item>
        </el-col>
                <el-col :span='24/2'>
          <el-form-item label='Thông tin đơn thuốcid' prop='recipelInfo.id' >
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.recipelInfo.name'></el-input>
            <el-select v-else v-model='bizFormModel.recipelInfo' value-key='id' filterable clearable placeholder='Vui lòng chọn thông tin đơn thuốcid' 
              @clear='bizFormModel.recipelInfo={
                "id": null,
                                "name": null,
                }'>
             <el-option v-for='recipelInfo in recipelInfo_List' :key='recipelInfo.id' :label='recipelInfo.name' :value='recipelInfo'></el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Số lượng' prop='number' >
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.number'></el-input>
            <number-input v-else v-model="bizFormModel.number"  :precision="0"></number-input>
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
      <el-button v-if='dialogProps.action !== "view"' type='primary' :plain='true' @click='onSubmit("dispensingForm")'>Lưu</el-button>
      <el-button v-if='dialogProps.action !== "view"' :plain='true' @click='onDialogClose()'>Hủy</el-button>
      <el-button v-if='dialogProps.action === "view"' :plain='true' @click='onDialogClose()'>Đóng</el-button>
    </span>    
  </el-dialog>
</template>
<script>
import { validatenull } from '@/utils/validate'
import { listCompanyAll } from '@/api/org/company'
import { listRegistrationAll } from '@/api/outpatient/registration'
import { listSupplierStockAll } from '@/api/stock/supplierStock'
import { listRecipelInfoAll } from '@/api/outpatient/recipelInfo'
import { saveDispensing } from '@/api/stock/dispensing'
import BaseUI from '@/views/components/baseUI'
import OperationIcon from '@/components/OperationIcon'
export default {
  extends: BaseUI,
  name: 'dispensing-form',
  components: {
    OperationIcon
  },  
  data() {
    return {
      bizFormModel: this.initFormModel(),
      tabIndex: '1',
          company_List: [],  // Phòng khámid
          registration_List: [],  // Đăng ký khámid
          supplierStock_List: [],  // Tồn khoid
          recipelInfo_List: [],  // Thông tin đơn thuốcid
       dialogProps: {
        visible: false,
        action: '',
        title: ''
      },
      formRules: {
        'registration.id': [
            { required: true, message: 'Vui lòng chọn đăng ký khámid', trigger: 'change' }
        ],
        'supplierStock.id': [
            { required: true, message: 'Vui lòng chọn tồn khoid', trigger: 'change' }
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
      this.$refs[formName].validate(valid => {
        if (valid) {
          this.doSave()
        } else {
          return false
        }
      });
    },
    doSave() {
      this.setLoad()
      saveDispensing(this.bizFormModel).then(responseData => {
        if(responseData.code === 100) {
          this.dialogProps.visible = false
          this.$emit('save-finished')
        } else {
          this.showMessage(responseData)
        }
        this.resetLoad()
      }).catch(error => {
        this.outputError(error)
      })
    },
    switchEdit() {
      this.dialogProps.action = 'edit'
      this.dialogProps.title = 'Sửa chi tiết phát thuốc'
      this.initOptions(this.bizFormModel)
    },
    onDialogClose() {
      this.dialogProps.visible = false  
    },
    onDialogOpen() {
      this.$nextTick(() => {
        this.$refs['dispensingForm'].clearValidate()
      })
    },
    initFormModel(This) {
      return {
        'company': {     // Phòng khámid
          'id': currentUser.company.id,
          'name': null,
        },
        'registration': {     // Đăng ký khámid
          'id': null,
        },
        'supplierStock': {     // Tồn khoid
          'id': null,
        },
        'recipelInfo': {     // Thông tin đơn thuốcid
          'id': null,
          'name': null,
        },
        'number': 0,    // Số lượng
        'remarks': '',   // Thông tin ghi chú

      }
    },
    initOptions(This) {
      let company_search = {
        params: []
      }
        // Trên bảng tương ứng của trườngfilterĐiều kiện
        company_search.params.push.apply(company_search.params, [])
      // Quyền dữ liệu: Phòng khámorg_company
      this.pushDataPermissions(company_search.params, this.$route.meta.routerId, '41040096140492800')
      this.company_List.splice(0, this.company_List.length)
      listCompanyAll(company_search).then(responseData => {
        this.company_List = responseData.data
        // Lấy giá trị ban đầu
        this.company_List.forEach( item => {
          if(item.id === this.bizFormModel.company.id) {
            this.bizFormModel.company = item
            return
          }
        })
      })
      let registration_search = {
        params: []
      }
        // Trên bảng tương ứng của trườngfilterĐiều kiện
        registration_search.params.push.apply(registration_search.params, [{columnName: 'company_id', queryType: '=', value: currentUser.company.id}])
      // Quyền dữ liệu: Bảng đăng ký khámregistration
      this.pushDataPermissions(registration_search.params, this.$route.meta.routerId, '1008534118685450402')
      this.registration_List.splice(0, this.registration_List.length)
      listRegistrationAll(registration_search).then(responseData => {
        this.registration_List = responseData.data
      })
      let supplierStock_search = {
        params: []
      }
        // Trên bảng tương ứng của trườngfilterĐiều kiện
        supplierStock_search.params.push.apply(supplierStock_search.params, [{columnName: 'company_id', queryType: '=', value: currentUser.company.id}])
      // Quyền dữ liệu: Tồn kho nhà cung cấpsupplier_stock
      this.pushDataPermissions(supplierStock_search.params, this.$route.meta.routerId, '1005591224273698934')
      this.supplierStock_List.splice(0, this.supplierStock_List.length)
      listSupplierStockAll(supplierStock_search).then(responseData => {
        this.supplierStock_List = responseData.data
      })
      let recipelInfo_search = {
        params: []
      }
        // Trên bảng tương ứng của trườngfilterĐiều kiện
        recipelInfo_search.params.push.apply(recipelInfo_search.params, [{columnName: 'company_id', queryType: '=', value: currentUser.company.id}])
      // Quyền dữ liệu: Thông tin đơn thuốcrecipel_info
      this.pushDataPermissions(recipelInfo_search.params, this.$route.meta.routerId, '1014474470772900008')
      this.recipelInfo_List.splice(0, this.recipelInfo_List.length)
      listRecipelInfoAll(recipelInfo_search).then(responseData => {
        this.recipelInfo_List = responseData.data
      })
    },
    openViewDispensingDialog(dispensing) {
      this.dialogProps.action = 'view'
      this.dialogProps.title = 'Xem chi tiết phát thuốc'
      this.bizFormModel = {...this.initFormModel(), ...dispensing}
      this.initOptions(this.bizFormModel)
      this.tabIndex = '1'
      this.dialogProps.visible = true
    },
    openEditDispensingDialog(dispensing) {
      this.dialogProps.action = 'edit'
      this.dialogProps.title = 'Sửa chi tiết phát thuốc'
      this.bizFormModel = {...this.initFormModel(), ...dispensing}
      this.initOptions(this.bizFormModel)
      this.tabIndex = '1'
      this.dialogProps.visible = true
    },
    openAddDispensingDialog() {
      this.dialogProps.action = 'add'
      this.dialogProps.title = 'Thêm chi tiết phát thuốc'
      this.bizFormModel = this.initFormModel()
      this.initOptions(this.bizFormModel)
      this.tabIndex = '1'
      this.dialogProps.visible = true
    },
    openCopyDispensingDialog(dispensing) {
      this.dialogProps.action = 'add'
      this.dialogProps.title = 'Thêm chi tiết phát thuốc'
      this.bizFormModel = {...this.initFormModel(), ...dispensing}
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