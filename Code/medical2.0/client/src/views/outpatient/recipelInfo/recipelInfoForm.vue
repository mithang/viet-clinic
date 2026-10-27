<template>
  <el-dialog :title='dialogProps.title' :visible.sync='dialogProps.visible' :close-on-click-modal='false' width='50%' 
    @open='onDialogOpen()' v-loading='loading'>
    <div slot='title' class='dialog-header'>
      {{ dialogProps.title }}
      <OperationIcon v-show='dialogProps.action === "view" && permission.edit' type='primary' text='Sửa' placement='top-start' icon-name='el-icon-edit' @click='switchEdit'></OperationIcon>
    </div>

    <el-form :model='bizFormModel' :rules='formRules' 
      ref='recipelInfoForm' label-width='120px' label-position='right' class='edit-form'>  
      <div class="tab-item" v-show='tabIndex==="1"'>  
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Bệnh án' prop='medicalRecord.id' >
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.medicalRecord.diagnose'></el-input>
            <el-select v-else v-model='bizFormModel.medicalRecord' value-key='id' filterable clearable placeholder='Vui lòng chọn bệnh án' 
              @clear='bizFormModel.medicalRecord={
                "id": null,
                                "diagnose": null,
                }'>
             <el-option v-for='medicalRecord in medicalRecord_List' :key='medicalRecord.id' :label='medicalRecord.diagnose' :value='medicalRecord'></el-option>
            </el-select>
          </el-form-item>
        </el-col>
                <el-col :span='24/2'>
          <el-form-item label='Loại đơn thuốc' prop='recipelType.value' >
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.recipelType.name'></el-input>
            <el-select v-else v-model='bizFormModel.recipelType' value-key='value' filterable clearable placeholder='Vui lòng chọn nhóm đơn thuốc' 
              @clear='bizFormModel.recipelType={
                "value": null,
                                "name": null,
                }'>
             <el-option v-for='recipelType in recipelType_List' :key='recipelType.value' :label='recipelType.name' :value='recipelType'></el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Liều lượng' prop='dosage' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.dosage' :maxlength='45' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập liều lượng"' ></el-input>
          </el-form-item>
        </el-col>
                <el-col :span='24/2'>
          <el-form-item label='Cách dùng' prop='recipelUse.value' >
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.recipelUse.name'></el-input>
            <el-select v-else v-model='bizFormModel.recipelUse' value-key='value' filterable clearable placeholder='Vui lòng chọn cách dùng' 
              @clear='bizFormModel.recipelUse={
                "value": null,
                                "name": null,
                }'>
             <el-option v-for='recipelUse in recipelUse_List' :key='recipelUse.value' :label='recipelUse.name' :value='recipelUse'></el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Liều lượng theo tần suất' prop='frequency.value' >
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.frequency.name'></el-input>
            <el-select v-else v-model='bizFormModel.frequency' value-key='value' filterable clearable placeholder='Vui lòng chọn liều lượng theo tần suất' 
              @clear='bizFormModel.frequency={
                "value": null,
                                "name": null,
                }'>
             <el-option v-for='frequency in frequency_List' :key='frequency.value' :label='frequency.name' :value='frequency'></el-option>
            </el-select>
          </el-form-item>
        </el-col>
                <el-col :span='24/2'>
          <el-form-item label='Tần suất uống' prop='takeFrequency.value' >
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.takeFrequency.name'></el-input>
            <el-select v-else v-model='bizFormModel.takeFrequency' value-key='value' filterable clearable placeholder='Vui lòng chọn tần suất dùng thuốc' 
              @clear='bizFormModel.takeFrequency={
                "value": null,
                                "name": null,
                }'>
             <el-option v-for='takeFrequency in takeFrequency_List' :key='takeFrequency.value' :label='takeFrequency.name' :value='takeFrequency'></el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Liều dùng mỗi lần' prop='singleDosage' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.singleDosage' :maxlength='11' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập liều dùng một lần"' ></el-input>
          </el-form-item>
        </el-col>
                <el-col :span='24/2'>
          <el-form-item label='Tổng giá đơn thuốc' prop='fee' >
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.fee'></el-input>
            <number-input v-else v-model="bizFormModel.fee" currency='CNY' :precision="2"></number-input>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Đã thanh toán chưa' prop='isPay' >
            <el-switch :disabled='dialogProps.action === "view"' v-model='bizFormModel.isPay' active-color='#13ce66' inactive-color='#dbdfe6' active-value='1' inactive-value='0'></el-switch>
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
          <el-form-item label='Thời gian thanh toán' prop='payDate' >
            <el-date-picker :disabled='dialogProps.action === "view"' v-model='bizFormModel.payDate' type='datetime' value-format='yyyy-MM-dd HH:mm:ss' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập thời gian thanh toán"' ></el-date-picker>             
          </el-form-item>
        </el-col>
                <el-col :span='24/2'>
          <el-form-item label='Đã trả thuốc chưa' prop='isReturnDrug' >
            <el-switch :disabled='dialogProps.action === "view"' v-model='bizFormModel.isReturnDrug' active-color='#13ce66' inactive-color='#dbdfe6' active-value='1' inactive-value='0'></el-switch>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Thời gian trả thuốc' prop='returnDrugDate' >
            <el-date-picker :disabled='dialogProps.action === "view"' v-model='bizFormModel.returnDrugDate' type='datetime' value-format='yyyy-MM-dd HH:mm:ss' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập thời gian trả thuốc"' ></el-date-picker>             
          </el-form-item>
        </el-col>
                <el-col :span='24/2'>
          <el-form-item label='Đã hoàn phí chưa' prop='isReturnFee' >
            <el-switch :disabled='dialogProps.action === "view"' v-model='bizFormModel.isReturnFee' active-color='#13ce66' inactive-color='#dbdfe6' active-value='1' inactive-value='0'></el-switch>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Thời gian hoàn phí' prop='returnFeeDate' >
            <el-date-picker :disabled='dialogProps.action === "view"' v-model='bizFormModel.returnFeeDate' type='datetime' value-format='yyyy-MM-dd HH:mm:ss' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập thời gian hoàn phí"' ></el-date-picker>             
          </el-form-item>
        </el-col>
    </el-row>
      </div>
    </el-form>
    <span slot='footer' class='dialog-footer'>
      <el-button v-if='dialogProps.action !== "view"' :disabled="flag" type='primary' :plain='true' @click='onSubmit("recipelInfoForm")'>Lưu</el-button>
      <el-button v-if='dialogProps.action !== "view"' :plain='true' @click='onDialogClose()'>Hủy</el-button>
      <el-button v-if='dialogProps.action === "view"' :plain='true' @click='onDialogClose()'>Đóng</el-button>
    </span>    
  </el-dialog>
</template>
<script>
import { validatenull } from '@/utils/validate'
import { listMedicalRecordAll } from '@/api/outpatient/medicalRecord'
import { getDictItemsByCode, DICT_CODE } from '@/utils/dictCache'
import { saveRecipelInfo } from '@/api/outpatient/recipelInfo'
import BaseUI from '@/views/components/baseUI'
import OperationIcon from '@/components/OperationIcon'
export default {
  extends: BaseUI,
  name: 'recipelInfo-form',
  components: {
    OperationIcon
  },  
  data() {
    return {
      bizFormModel: this.initFormModel(),
      tabIndex: '1',
          medicalRecord_List: [],  // Bệnh án
          recipelType_List: [],  // Loại đơn thuốc
          recipelUse_List: [],  // Cách dùng
          frequency_List: [],  // Liều lượng theo tần suất
          takeFrequency_List: [],  // Tần suất uống
          flag:false,
       dialogProps: {
        visible: false,
        action: '',
        title: '',
        
      },
      formRules: {
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
      saveRecipelInfo(this.bizFormModel).then(responseData => {
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
      this.dialogProps.title = 'Sửa thông tin đơn thuốc'
      this.initOptions(this.bizFormModel)
    },
    onDialogClose() {
      this.dialogProps.visible = false  
    },
    onDialogOpen() {
      this.$nextTick(() => {
        this.$refs['recipelInfoForm'].clearValidate()
      })
    },
    initFormModel(This) {
      return {
        'company': {     // Phòng khámid
          'id': currentUser.company.id,
          'name': null,
        },
        'medicalRecord': {     // Bệnh án
          'id': null,
          'diagnose': null,
        },
        'recipelType': {     // Loại đơn thuốc
          'value': null,
          'name': null,
        },
        'dosage': '',   // Liều lượng
        'recipelUse': {     // Cách dùng
          'value': null,
          'name': null,
        },
        'frequency': {     // Liều lượng theo tần suất
          'value': null,
          'name': null,
        },
        'takeFrequency': {     // Tần suất uống
          'value': null,
          'name': null,
        },
        'singleDosage': '',   // Liều dùng mỗi lần
        'fee': 0,    // Tổng giá đơn thuốc
        'isPay': '0',   // Đã thanh toán chưa
        'remarks': '',   // Thông tin ghi chú
        'payDate': '',   // Thời gian thanh toán
        'isReturnDrug': '0',   // Đã trả thuốc chưa
        'returnDrugDate': '',   // Thời gian trả thuốc
        'isReturnFee': '0',   // Đã hoàn phí chưa
        'returnFeeDate': '',   // Thời gian hoàn phí

      }
    },
    initOptions(This) {
      let medicalRecord_search = {
        params: []
      }
        // Trên bảng tương ứng của trườngfilterĐiều kiện
        medicalRecord_search.params.push.apply(medicalRecord_search.params, [{columnName: 'company_id', queryType: '=', value: currentUser.company.id}])
      // Quyền dữ liệu: Bảng bệnh ánmedical_record
      this.pushDataPermissions(medicalRecord_search.params, this.$route.meta.routerId, '1014786989773332526')
      this.medicalRecord_List.splice(0, this.medicalRecord_List.length)
      listMedicalRecordAll(medicalRecord_search).then(responseData => {
        this.medicalRecord_List = responseData.data
      })
      getDictItemsByCode(DICT_CODE.RECIPEL_TYPE).then((data) => {
        this.recipelType_List = data;
      });
      getDictItemsByCode(DICT_CODE.CHINESE_MEDICINE_RECIPEL_USE).then((data) => {
        this.recipelUse_List = data;
      });
      getDictItemsByCode(DICT_CODE.CHINESE_MEDICINE_RECIPEL_FREQUENCY).then((data) => {
        this.frequency_List = data;
      });
      getDictItemsByCode(DICT_CODE.CHINESE_MEDICINE_RECIPEL_TAKE_FREQUENCY).then((data) => {
        this.takeFrequency_List = data;
      });
    },
    openViewRecipelInfoDialog(recipelInfo) {
      this.dialogProps.action = 'view'
      this.dialogProps.title = 'Xem thông tin đơn thuốc'
      this.bizFormModel = {...this.initFormModel(), ...recipelInfo}
      this.initOptions(this.bizFormModel)
      this.tabIndex = '1'
      this.dialogProps.visible = true
    },
    openEditRecipelInfoDialog(recipelInfo) {
      this.dialogProps.action = 'edit'
      this.dialogProps.title = 'Sửa thông tin đơn thuốc'
      this.bizFormModel = {...this.initFormModel(), ...recipelInfo}
      this.initOptions(this.bizFormModel)
      this.tabIndex = '1'
      this.dialogProps.visible = true
    },
    openAddRecipelInfoDialog() {
      this.dialogProps.action = 'add'
      this.dialogProps.title = 'Thêm thông tin đơn thuốc'
      this.bizFormModel = this.initFormModel()
      this.initOptions(this.bizFormModel)
      this.tabIndex = '1'
      this.dialogProps.visible = true
    },
    openCopyRecipelInfoDialog(recipelInfo) {
      this.dialogProps.action = 'add'
      this.dialogProps.title = 'Thêm thông tin đơn thuốc'
      this.bizFormModel = {...this.initFormModel(), ...recipelInfo}
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