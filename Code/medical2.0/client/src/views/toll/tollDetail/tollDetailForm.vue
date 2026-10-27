<template>
  <el-dialog :title='dialogProps.title' :visible.sync='dialogProps.visible' :close-on-click-modal='false' width='50%' 
    @open='onDialogOpen()' v-loading='loading'>
    <div slot='title' class='dialog-header'>
      {{ dialogProps.title }}
      <OperationIcon v-show='dialogProps.action === "view" && permission.edit' type='primary' text='Sửa' placement='top-start' icon-name='el-icon-edit' @click='switchEdit'></OperationIcon>
    </div>

    <el-form :model='bizFormModel' :rules='formRules' 
      ref='tollDetailForm' label-width='120px' label-position='right' class='edit-form'>  
      <div class="tab-item" v-show='tabIndex==="1"'>  
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Đơn thuốc' prop='recipel.id' >
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.recipel.name'></el-input>
            <el-select v-else v-model='bizFormModel.recipel' value-key='id' filterable clearable placeholder='Vui lòng chọn đơn thuốc' 
              @clear='bizFormModel.recipel={
                "id": null,
                                "name": null,
                }'>
             <el-option v-for='recipel in recipel_List' :key='recipel.id' :label='recipel.name' :value='recipel'></el-option>
            </el-select>
          </el-form-item>
        </el-col>
                <el-col :span='24/2'>
          <el-form-item label='Bảng thu phí chính' prop='tollId.id' >
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.tollId.name'></el-input>
            <el-select v-else v-model='bizFormModel.tollId' value-key='id' filterable clearable placeholder='Vui lòng chọn bảng thu phí chính' 
              @clear='bizFormModel.tollId={
                "id": null,
                                "name": null,
                }'>
             <el-option v-for='tollId in tollId_List' :key='tollId.id' :label='tollId.name' :value='tollId'></el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      </div>
    </el-form>
    <span slot='footer' class='dialog-footer'>
      <el-button v-if='dialogProps.action !== "view"' type='primary' :plain='true' @click='onSubmit("tollDetailForm")'>Lưu</el-button>
      <el-button v-if='dialogProps.action !== "view"' :plain='true' @click='onDialogClose()'>Hủy</el-button>
      <el-button v-if='dialogProps.action === "view"' :plain='true' @click='onDialogClose()'>Đóng</el-button>
    </span>    
  </el-dialog>
</template>
<script>
import { validatenull } from '@/utils/validate'
import { listRecipelInfoAll } from '@/api/outpatient/recipelInfo'
import { listTollInfoAll } from '@/api/toll/tollInfo'
import { saveTollDetail } from '@/api/toll/tollDetail'
import BaseUI from '@/views/components/baseUI'
import OperationIcon from '@/components/OperationIcon'
export default {
  extends: BaseUI,
  name: 'tollDetail-form',
  components: {
    OperationIcon
  },  
  data() {
    return {
      bizFormModel: this.initFormModel(),
      tabIndex: '1',
          recipel_List: [],  // Đơn thuốc
          tollId_List: [],  // Bảng thu phí chính
       dialogProps: {
        visible: false,
        action: '',
        title: ''
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
      saveTollDetail(this.bizFormModel).then(responseData => {
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
      this.dialogProps.title = 'Sửa quản lý chi tiết thu phí'
      this.initOptions(this.bizFormModel)
    },
    onDialogClose() {
      this.dialogProps.visible = false  
    },
    onDialogOpen() {
      this.$nextTick(() => {
        this.$refs['tollDetailForm'].clearValidate()
      })
    },
    initFormModel(This) {
      return {
        'recipel': {     // Đơn thuốc
          'id': null,
          'name': null,
        },
        'tollId': {     // Bảng cha Bảng thu phí chính
          'id': validatenull(This) || validatenull(This.tollId) ? null : This.tollId.id,
          'name': validatenull(This) || validatenull(This.tollId) ? null : This.tollId.name,
        },

      }
    },
    initOptions(This) {
      let recipel_search = {
        params: []
      }
        // Trên bảng tương ứng của trườngfilterĐiều kiện
        recipel_search.params.push.apply(recipel_search.params, [{columnName: 'company_id', queryType: '=', value: currentUser.company.id}])
      // Quyền dữ liệu: Thông tin đơn thuốcrecipel_info
      this.pushDataPermissions(recipel_search.params, this.$route.meta.routerId, '1014474470772900008')
      this.recipel_List.splice(0, this.recipel_List.length)
      listRecipelInfoAll(recipel_search).then(responseData => {
        this.recipel_List = responseData.data
      })
      let tollId_search = {
        params: []
      }
        // Trên bảng tương ứng của trườngfilterĐiều kiện
        tollId_search.params.push.apply(tollId_search.params, [{columnName: 'company_id', queryType: '=', value: currentUser.company.id}])
      // Quyền dữ liệu: Bảng thu phí chínhtoll_info
      this.pushDataPermissions(tollId_search.params, this.$route.meta.routerId, '1024846610635145502')
      this.tollId_List.splice(0, this.tollId_List.length)
      listTollInfoAll(tollId_search).then(responseData => {
        this.tollId_List = responseData.data
      })
    },
    openViewTollDetailDialog(tollDetail) {
      this.dialogProps.action = 'view'
      this.dialogProps.title = 'Xem quản lý chi tiết thu phí'
      this.bizFormModel = {...this.initFormModel(), ...tollDetail}
      this.initOptions(this.bizFormModel)
      this.tabIndex = '1'
      this.dialogProps.visible = true
    },
    openEditTollDetailDialog(tollDetail) {
      this.dialogProps.action = 'edit'
      this.dialogProps.title = 'Sửa quản lý chi tiết thu phí'
      this.bizFormModel = {...this.initFormModel(), ...tollDetail}
      this.initOptions(this.bizFormModel)
      this.tabIndex = '1'
      this.dialogProps.visible = true
    },
    openAddTollDetailDialog() {
      this.dialogProps.action = 'add'
      this.dialogProps.title = 'Thêm quản lý chi tiết thu phí'
      this.bizFormModel = this.initFormModel()
      this.initOptions(this.bizFormModel)
      this.tabIndex = '1'
      this.dialogProps.visible = true
    },
    openCopyTollDetailDialog(tollDetail) {
      this.dialogProps.action = 'add'
      this.dialogProps.title = 'Thêm quản lý chi tiết thu phí'
      this.bizFormModel = {...this.initFormModel(), ...tollDetail}
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