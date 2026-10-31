<template>
  <el-dialog :title='dialogProps.title' :visible.sync='dialogProps.visible' :close-on-click-modal='false' width='50%' 
    @open='onDialogOpen()' v-loading='loading'>
    <div slot='title' class='dialog-header'>
      {{ dialogProps.title }}
      <OperationIcon v-show='dialogProps.action === "view" && permission.edit' type='primary' text='Sửa' placement='top-start' icon-name='el-icon-edit' @click='switchEdit'></OperationIcon>
    </div>

    <el-form :model='bizFormModel' :rules='formRules' 
      ref='recipelDetailForm' label-width='120px' label-position='right' class='edit-form'>  
      <div class="tab-item" v-show='tabIndex==="1"'>  
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Đơn thuốc' prop='recipelInfo.id' >
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.recipelInfo.remarks'></el-input>
            <el-select v-else v-model='bizFormModel.recipelInfo' value-key='id' filterable clearable placeholder='Vui lòng chọn đơn thuốc' 
              @clear='bizFormModel.recipelInfo={
                "id": null,
                                "remarks": null,
                }'>
             <el-option v-for='recipelInfo in recipelInfo_List' :key='recipelInfo.id' :label='recipelInfo.remarks' :value='recipelInfo'></el-option>
            </el-select>
          </el-form-item>
        </el-col>
                <el-col :span='24/2'>
          <el-form-item label='Thuốc/vật tưid' prop='drugStuffId' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.drugStuffId' :maxlength='21' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập thuốc/vật tưid"' ></el-input>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Liều dùng mỗi lần' prop='singleDosage' >
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.singleDosage'></el-input>
            <number-input v-else v-model="bizFormModel.singleDosage"  :precision="0"></number-input>
          </el-form-item>
        </el-col>
                <el-col :span='24/2'>
          <el-form-item label='Tổng số lượng' prop='total' >
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.total'></el-input>
            <number-input v-else v-model="bizFormModel.total"  :precision="0"></number-input>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Tổng giá' prop='allFee' >
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.allFee'></el-input>
            <number-input v-else v-model="bizFormModel.allFee" currency='VND' :precision="2"></number-input>
          </el-form-item>
        </el-col>
                <el-col :span='24/2'>
          <el-form-item label='Cách dùng thuốc Tây y' prop='westernMedicineUse.value' >
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.westernMedicineUse.name'></el-input>
            <el-select v-else v-model='bizFormModel.westernMedicineUse' value-key='value' filterable clearable placeholder='Vui lòng chọn cách dùng thuốc Tây' 
              @clear='bizFormModel.westernMedicineUse={
                "value": null,
                                "name": null,
                }'>
             <el-option v-for='westernMedicineUse in westernMedicineUse_List' :key='westernMedicineUse.value' :label='westernMedicineUse.name' :value='westernMedicineUse'></el-option>
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
          <el-form-item label='Số ngày' prop='days.value' >
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.days.name'></el-input>
            <el-select v-else v-model='bizFormModel.days' value-key='value' filterable clearable placeholder='Vui lòng chọn số ngày' 
              @clear='bizFormModel.days={
                "value": null,
                                "name": null,
                }'>
             <el-option v-for='days in days_List' :key='days.value' :label='days.name' :value='days'></el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Cách dùng thuốc Đông y' prop='chineseMedicineUse.value' >
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.chineseMedicineUse.name'></el-input>
            <el-select v-else v-model='bizFormModel.chineseMedicineUse' value-key='value' filterable clearable placeholder='Vui lòng chọn cách dùng thuốc Đông y' 
              @clear='bizFormModel.chineseMedicineUse={
                "value": null,
                                "name": null,
                }'>
             <el-option v-for='chineseMedicineUse in chineseMedicineUse_List' :key='chineseMedicineUse.value' :label='chineseMedicineUse.name' :value='chineseMedicineUse'></el-option>
            </el-select>
          </el-form-item>
        </el-col>
                <el-col :span='24/2'>
          <el-form-item label='Cách dùng truyền dịch' prop='infuseUse.value' >
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.infuseUse.name'></el-input>
            <el-select v-else v-model='bizFormModel.infuseUse' value-key='value' filterable clearable placeholder='Vui lòng chọn cách dùng truyền dịch' 
              @clear='bizFormModel.infuseUse={
                "value": null,
                                "name": null,
                }'>
             <el-option v-for='infuseUse in infuseUse_List' :key='infuseUse.value' :label='infuseUse.name' :value='infuseUse'></el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Tốc độ truyền' prop='drippingSpeed' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.drippingSpeed' :maxlength='45' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập tốc độ truyền"' ></el-input>
          </el-form-item>
        </el-col>
                <el-col :span='24/2'>
          <el-form-item label='Test da' prop='skinTest.value' >
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.skinTest.name'></el-input>
            <el-select v-else v-model='bizFormModel.skinTest' value-key='value' filterable clearable placeholder='Vui lòng chọn thử phản ứng da' 
              @clear='bizFormModel.skinTest={
                "value": null,
                                "name": null,
                }'>
             <el-option v-for='skinTest in skinTest_List' :key='skinTest.value' :label='skinTest.name' :value='skinTest'></el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Số nhóm' prop='infuseGroup' >
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.infuseGroup'></el-input>
            <number-input v-else v-model="bizFormModel.infuseGroup"  :precision="0"></number-input>
          </el-form-item>
        </el-col>
    </el-row>
      </div>
    </el-form>
    <span slot='footer' class='dialog-footer'>
      <el-button v-if='dialogProps.action !== "view"' :disabled="flag" type='primary' :plain='true' @click='onSubmit("recipelDetailForm")'>Lưu</el-button>
      <el-button v-if='dialogProps.action !== "view"' :plain='true' @click='onDialogClose()'>Hủy</el-button>
      <el-button v-if='dialogProps.action === "view"' :plain='true' @click='onDialogClose()'>Đóng</el-button>
    </span>    
  </el-dialog>
</template>
<script>
import { validatenull } from '@/utils/validate'
import { listRecipelInfoAll } from '@/api/outpatient/recipelInfo'
import { getDictItemsByCode, DICT_CODE } from '@/utils/dictCache'
import { saveRecipelDetail } from '@/api/outpatient/recipelDetail'
import BaseUI from '@/views/components/baseUI'
import OperationIcon from '@/components/OperationIcon'
export default {
  extends: BaseUI,
  name: 'recipelDetail-form',
  components: {
    OperationIcon
  },  
  data() {
    return {
      bizFormModel: this.initFormModel(),
      tabIndex: '1',
          recipelInfo_List: [],  // Đơn thuốc
          westernMedicineUse_List: [],  // Cách dùng thuốc Tây y
          frequency_List: [],  // Liều lượng theo tần suất
          days_List: [],  // Số ngày
          chineseMedicineUse_List: [],  // Cách dùng thuốc Đông y
          infuseUse_List: [],  // Cách dùng truyền dịch
          skinTest_List: [],  // Test da
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
      saveRecipelDetail(this.bizFormModel).then(responseData => {
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
      this.dialogProps.title = 'Sửa chi tiết đơn thuốc'
      this.initOptions(this.bizFormModel)
    },
    onDialogClose() {
      this.dialogProps.visible = false  
    },
    onDialogOpen() {
      this.$nextTick(() => {
        this.$refs['recipelDetailForm'].clearValidate()
      })
    },
    initFormModel(This) {
      return {
        'company': {     // Phòng khámid
          'id': currentUser.company.id,
          'name': null,
        },
        'recipelInfo': {     // Bảng cha Đơn thuốc
          'id': validatenull(This) || validatenull(This.recipelInfo) ? null : This.recipelInfo.id,
          'remarks': validatenull(This) || validatenull(This.recipelInfo) ? null : This.recipelInfo.remarks,
        },
        'drugStuffId': '',   // Thuốc/vật tưid
        'singleDosage': 0,    // Liều dùng mỗi lần
        'total': 0,    // Tổng số lượng
        'allFee': 0,    // Tổng giá
        'westernMedicineUse': {     // Cách dùng thuốc Tây y
          'value': null,
          'name': null,
        },
        'frequency': {     // Liều lượng theo tần suất
          'value': null,
          'name': null,
        },
        'days': {     // Số ngày
          'value': null,
          'name': null,
        },
        'chineseMedicineUse': {     // Cách dùng thuốc Đông y
          'value': null,
          'name': null,
        },
        'infuseUse': {     // Cách dùng truyền dịch
          'value': null,
          'name': null,
        },
        'drippingSpeed': '',   // Tốc độ truyền
        'skinTest': {     // Test da
          'value': null,
          'name': null,
        },
        'infuseGroup': 0,    // Số nhóm
        'remarks': '',   // Thông tin ghi chú

      }
    },
    initOptions() {
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
      getDictItemsByCode(DICT_CODE.WESTERN_MEDICINE_USE).then((data) => {
        this.westernMedicineUse_List = data;
      });
      getDictItemsByCode(DICT_CODE.RECIPEL_DETAIL_FREQUENCY).then((data) => {
        this.frequency_List = data;
      });
      getDictItemsByCode(DICT_CODE.RECIPEL_DETAIL_DAYS).then((data) => {
        this.days_List = data;
      });
      getDictItemsByCode(DICT_CODE.CHINESE_MEDICINE_USE).then((data) => {
        this.chineseMedicineUse_List = data;
      });
      getDictItemsByCode(DICT_CODE.INFUSE_USE).then((data) => {
        this.infuseUse_List = data;
      });
      getDictItemsByCode(DICT_CODE.SKIN_TEST).then((data) => {
        this.skinTest_List = data;
      });
    },
    openViewRecipelDetailDialog(recipelDetail) {
      this.dialogProps.action = 'view'
      this.dialogProps.title = 'Xem chi tiết đơn thuốc'
      this.bizFormModel = {...this.initFormModel(), ...recipelDetail}
      this.initOptions(this.bizFormModel)
      this.tabIndex = '1'
      this.dialogProps.visible = true
    },
    openEditRecipelDetailDialog(recipelDetail) {
      this.dialogProps.action = 'edit'
      this.dialogProps.title = 'Sửa chi tiết đơn thuốc'
      this.bizFormModel = {...this.initFormModel(), ...recipelDetail}
      this.initOptions(this.bizFormModel)
      this.tabIndex = '1'
      this.dialogProps.visible = true
    },
    openAddRecipelDetailDialog() {
      this.dialogProps.action = 'add'
      this.dialogProps.title = 'Thêm chi tiết đơn thuốc'
      this.bizFormModel = this.initFormModel()
      this.initOptions(this.bizFormModel)
      this.tabIndex = '1'
      this.dialogProps.visible = true
    },
    openCopyRecipelDetailDialog(recipelDetail) {
      this.dialogProps.action = 'add'
      this.dialogProps.title = 'Thêm chi tiết đơn thuốc'
      this.bizFormModel = {...this.initFormModel(), ...recipelDetail}
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