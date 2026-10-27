<template>
  <el-dialog :title='dialogProps.title' :visible.sync='dialogProps.visible' :close-on-click-modal='false' width='50%'
    @open='onDialogOpen()' v-loading='loading'>
    <div slot='title' class='dialog-header'>
      {{ dialogProps.title }}
      <OperationIcon v-show='dialogProps.action === "view" && permission.edit' type='primary' text='Sửa' placement='top-start' icon-name='el-icon-edit' @click='switchEdit'></OperationIcon>
    </div>

    <el-form :model='bizFormModel' :rules='formRules'
      ref='tollInfoForm' label-width='120px' label-position='right' class='edit-form'>
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
          <el-form-item label='Bệnh án' prop='medical.id' >
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.medical.patient_tell'></el-input>
            <el-select v-else v-model='bizFormModel.medical' value-key='id' filterable clearable placeholder='Vui lòng chọn bệnh án'
              @clear='bizFormModel.medical={
                "id": null,
                                "patient_tell": null,
                }'>
             <el-option v-for='medical in medical_List' :key='medical.id' :label='medical.patient_tell' :value='medical'></el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Số tiền phải thu (hoàn lại)' prop='amountReceivable' >
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.amountReceivable'></el-input>
            <number-input v-else v-model="bizFormModel.amountReceivable" currency='CNY' :precision="2"></number-input>
          </el-form-item>
        </el-col>
                <el-col :span='24/2'>
          <el-form-item label='Số tiền thực thu' prop='amountReceived' >
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.amountReceived'></el-input>
            <number-input v-else v-model="bizFormModel.amountReceived" currency='CNY' :precision="2"></number-input>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Số tiền ưu đãi' prop='amountDiscounted' >
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.amountDiscounted'></el-input>
            <number-input v-else v-model="bizFormModel.amountDiscounted" currency='CNY' :precision="2"></number-input>
          </el-form-item>
        </el-col>
                <el-col :span='24/2'>
          <el-form-item label='Mức chiết khấu ' prop='discount' >
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.discount'></el-input>
            <number-input v-else v-model="bizFormModel.discount"  :precision="2"></number-input>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Loại thanh toán' prop='paymentType.value' >
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.paymentType.name'></el-input>
            <el-select v-else v-model='bizFormModel.paymentType' value-key='value' filterable clearable placeholder='Vui lòng chọn loại thanh toán'
              @clear='bizFormModel.paymentType={
                "value": null,
                                "name": null,
                }'>
             <el-option v-for='paymentType in paymentType_List' :key='paymentType.value' :label='paymentType.name' :value='paymentType'></el-option>
            </el-select>
          </el-form-item>
        </el-col>
                <el-col :span='24/2'>
          <el-form-item label='Trạng thái chứng từ，Enum tham khảo' prop='amountStatus.value' >
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.amountStatus.name'></el-input>
            <el-select v-else v-model='bizFormModel.amountStatus' value-key='value' filterable clearable placeholder='Vui lòng chọn trạng thái chứng từ，Enum tham khảo'
              @clear='bizFormModel.amountStatus={
                "value": null,
                                "name": null,
                }'>
             <el-option v-for='amountStatus in amountStatus_List' :key='amountStatus.value' :label='amountStatus.name' :value='amountStatus'></el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Số phiếu thu phí' prop='tollNumber' >
            <el-input disabled v-model='bizFormModel.tollNumber' :maxlength='64' placeholder='Tự động tạo sau khi lưu' ></el-input>
          </el-form-item>
        </el-col>
                <el-col :span='24/2'>
          <el-form-item label='Loại thu phí，Enum tham khảo' prop='tollType.value' >
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.tollType.name'></el-input>
            <el-select v-else v-model='bizFormModel.tollType' value-key='value' filterable clearable placeholder='Vui lòng chọn loại thu phí，Enum tham khảo'
              @clear='bizFormModel.tollType={
                "value": null,
                                "name": null,
                }'>
             <el-option v-for='tollType in tollType_List' :key='tollType.value' :label='tollType.name' :value='tollType'></el-option>
            </el-select>
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
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Bệnh nhân' prop='patient.id' >
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.patient.name'></el-input>
            <el-select v-else v-model='bizFormModel.patient' value-key='id' filterable clearable placeholder='Vui lòng chọn bệnh nhân'
              @clear='bizFormModel.patient={
                "id": null,
                                "name": null,
                }'>
             <el-option v-for='patient in patient_List' :key='patient.id' :label='patient.name' :value='patient'></el-option>
            </el-select>
          </el-form-item>
        </el-col>
    </el-row>
      </div>
    </el-form>
    <span slot='footer' class='dialog-footer'>
      <el-button v-if='dialogProps.action !== "view"' type='primary' :plain='true' @click='onSubmit("tollInfoForm")'>Lưu</el-button>
      <el-button v-if='dialogProps.action !== "view"' :plain='true' @click='onDialogClose()'>Hủy</el-button>
      <el-button v-if='dialogProps.action === "view"' :plain='true' @click='onDialogClose()'>Đóng</el-button>
    </span>
  </el-dialog>
</template>
<script>
import { validatenull } from '@/utils/validate'
import { listCompanyAll } from '@/api/org/company'
import { listMedicalRecordAll } from '@/api/outpatient/medicalRecord'
import { getDictItemsByCode, DICT_CODE } from '@/utils/dictCache'
import { listPatientAll } from '@/api/outpatient/patient'
import { saveTollInfo } from '@/api/toll/tollInfo'
import BaseUI from '@/views/components/baseUI'
import OperationIcon from '@/components/OperationIcon'
export default {
  extends: BaseUI,
  name: 'tollInfo-form',
  components: {
    OperationIcon
  },
  data() {
    return {
      bizFormModel: this.initFormModel(),
      tabIndex: '1',
          company_List: [],  // Phòng khámid
          medical_List: [],  // Bệnh án
          paymentType_List: [],  // Loại thanh toán
          amountStatus_List: [],  // Trạng thái chứng từ，Enum tham khảo
          tollType_List: [],  // Loại thu phí，Enum tham khảo
          patient_List: [],  // Bệnh nhân
       dialogProps: {
        visible: false,
        action: '',
        title: ''
      },
      formRules: {
        'paymentType.value': [
            { required: true, message: 'Vui lòng chọn loại thanh toán', trigger: 'change' }
        ],
        'amountStatus.value': [
            { required: true, message: 'Vui lòng chọn trạng thái chứng từ，Enum tham khảo', trigger: 'change' }
        ],
        'tollNumber': [
            { required: true, message: '请选择收费单号', trigger: 'blur' }
        ],
        'tollType.value': [
            { required: true, message: 'Vui lòng chọn loại thu phí，Enum tham khảo', trigger: 'change' }
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
      saveTollInfo(this.bizFormModel).then(responseData => {
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
      this.dialogProps.title = 'Sửa quản lý thu phí'
      this.initOptions(this.bizFormModel)
    },
    onDialogClose() {
      this.dialogProps.visible = false
    },
    onDialogOpen() {
      this.$nextTick(() => {
        this.$refs['tollInfoForm'].clearValidate()
      })
    },
    initFormModel(This) {
      return {
        'company': {     // Phòng khámid
          'id': currentUser.company.id,
          'name': null,
        },
        'medical': {     // Bệnh án
          'id': null,
          'patient_tell': null,
        },
        'amountReceivable': 0,    // Số tiền phải thu (hoàn lại)
        'amountReceived': 0,    // Số tiền thực thu
        'amountDiscounted': 0,    // Số tiền ưu đãi
        'discount': 0,    // Mức chiết khấu
        'paymentType': {     // Loại thanh toán
          'value': null,
          'name': null,
        },
        'amountStatus': {     // Trạng thái chứng từ，Enum tham khảo
          'value': null,
          'name': null,
        },
        'tollNumber': '',    // Số phiếu thu phí
        'tollType': {     // Loại thu phí，Enum tham khảo
          'value': null,
          'name': null,
        },
        'remarks': '',   // Thông tin ghi chú
        'patient': {     // Bệnh nhân
          'id': null,
          'name': null,
        },

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
      let medical_search = {
        params: []
      }
        // Trên bảng tương ứng của trườngfilterĐiều kiện
        medical_search.params.push.apply(medical_search.params, [{columnName: 'company_id', queryType: '=', value: currentUser.company.id}])
      // Quyền dữ liệu: Bảng bệnh ánmedical_record
      this.pushDataPermissions(medical_search.params, this.$route.meta.routerId, '1014786989773332526')
      this.medical_List.splice(0, this.medical_List.length)
      listMedicalRecordAll(medical_search).then(responseData => {
        this.medical_List = responseData.data
      })
      getDictItemsByCode(DICT_CODE.PAY_TYPE).then((data) => {
        this.paymentType_List = data;
      });
      getDictItemsByCode(DICT_CODE.AMOUNT_STATUS).then((data) => {
        this.amountStatus_List = data;
      });
      getDictItemsByCode(DICT_CODE.TOLL_TYPE).then((data) => {
        this.tollType_List = data;
      });
      let patient_search = {
        params: []
      }
        // Trên bảng tương ứng của trườngfilterĐiều kiện
        patient_search.params.push.apply(patient_search.params, [{columnName: 'company_id', queryType: '=', value: currentUser.company.id}])
      // Quyền dữ liệu: Bảng bệnh nhânpatient
      this.pushDataPermissions(patient_search.params, this.$route.meta.routerId, '1008489176147648530')
      this.patient_List.splice(0, this.patient_List.length)
      listPatientAll(patient_search).then(responseData => {
        this.patient_List = responseData.data
      })
    },
    openViewTollInfoDialog(tollInfo) {
      this.dialogProps.action = 'view'
      this.dialogProps.title = 'Xem quản lý thu phí'
      this.bizFormModel = {...this.initFormModel(), ...tollInfo}
      this.initOptions(this.bizFormModel)
      this.tabIndex = '1'
      this.dialogProps.visible = true
    },
    openEditTollInfoDialog(tollInfo) {
      this.dialogProps.action = 'edit'
      this.dialogProps.title = 'Sửa quản lý thu phí'
      this.bizFormModel = {...this.initFormModel(), ...tollInfo}
      this.initOptions(this.bizFormModel)
      this.tabIndex = '1'
      this.dialogProps.visible = true
    },
    openAddTollInfoDialog() {
      this.dialogProps.action = 'add'
      this.dialogProps.title = 'Thêm quản lý thu phí'
      this.bizFormModel = this.initFormModel()
      this.initOptions(this.bizFormModel)
      this.tabIndex = '1'
      this.dialogProps.visible = true
    },
    openCopyTollInfoDialog(tollInfo) {
      this.dialogProps.action = 'add'
      this.dialogProps.title = 'Thêm quản lý thu phí'
      this.bizFormModel = {...this.initFormModel(), ...tollInfo}
      this.initOptions(this.bizFormModel)
      this.tabIndex = '1'
      this.bizFormModel.id = null   //ĐặtidĐặt thành trống，Thêm mới một
      this.dialogProps.visible = true
    }
  },
  watch: {
  }
}
</script>
