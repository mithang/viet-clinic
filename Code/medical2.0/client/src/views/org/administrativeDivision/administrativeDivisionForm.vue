<template>
  <el-dialog :title='dialogProps.title' :visible.sync='dialogProps.visible' :close-on-click-modal='false' width='30%' 
    @open='onDialogOpen()' v-loading='loading'>
    <div slot='title' class='dialog-header'>
      {{ dialogProps.title }}
      <OperationIcon v-show='dialogProps.action === "view" && permission.edit' type='primary' text='Sửa' placement='top-start' icon-name='el-icon-edit' @click='switchEdit'></OperationIcon>
    </div>

    <el-form :model='bizFormModel' :rules='formRules' 
      ref='administrativeDivisionForm' label-width='120px' label-position='right' class='edit-form'>  
      <div class="tab-item" v-show='tabIndex==="1"'>  
              <el-row>
        <el-col :span='24/1'>
          <el-form-item label='Mã khu vực cấp trên' prop='parten.id' >
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.parten.name'></el-input>
            <el-select v-else v-model='bizFormModel.parten' value-key='id' filterable clearable placeholder='Vui lòng chọn mã phân vùng cấp trên' 
              @clear='bizFormModel.parten={
                "id": null,
                                "name": null,
                }'>
             <el-option v-for='parten in parten_List' :key='parten.id' :label='parten.name' :value='parten'></el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/1'>
          <el-form-item label='Tên khu vực' prop='name' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.name' :maxlength='50' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập tên khu vực"' ></el-input>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/1'>
          <el-form-item label='Cấp khu vực' prop='level' >
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.level'></el-input>
            <number-input v-else v-model="bizFormModel.level"  :precision="0"></number-input>
          </el-form-item>
        </el-col>
      </el-row>
      </div>
    </el-form>
    <span slot='footer' class='dialog-footer'>
      <el-button v-if='dialogProps.action !== "view"' :disabled="flag" type='primary' :plain='true' @click='onSubmit("administrativeDivisionForm")'>Lưu</el-button>
      <el-button v-if='dialogProps.action !== "view"' :plain='true' @click='onDialogClose()'>Hủy</el-button>
      <el-button v-if='dialogProps.action === "view"' :plain='true' @click='onDialogClose()'>Đóng</el-button>
    </span>    
  </el-dialog>
</template>
<script>
import { validatenull } from '@/utils/validate'
import { listAdministrativeDivisionAll } from '@/api/org/administrativeDivision'
import { saveAdministrativeDivision } from '@/api/org/administrativeDivision'
import BaseUI from '@/views/components/baseUI'
import OperationIcon from '@/components/OperationIcon'
export default {
  extends: BaseUI,
  name: 'administrativeDivision-form',
  components: {
    OperationIcon
  },  
  data() {
    return {
      bizFormModel: this.initFormModel(),
      tabIndex: '1',
       flag:false,
          parten_List: [],  // Mã khu vực cấp trên
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
      saveAdministrativeDivision(this.bizFormModel).then(responseData => {
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
      this.dialogProps.title = 'Sửa phân chia khu vực hành chính'
      this.initOptions(this.bizFormModel)
    },
    onDialogClose() {
      this.dialogProps.visible = false  
    },
    onDialogOpen() {
      this.$nextTick(() => {
        this.$refs['administrativeDivisionForm'].clearValidate()
      })
    },
    initFormModel(This) {
      return {
        'parten': {     // Nút cấp trên Mã khu vực cấp trên
          'id': validatenull(This) || validatenull(This.id) ? null : This.id,
          'name': validatenull(This) || validatenull(This.name) ? null : This.name,
        },
        'name': '',   // Tên khu vực
        'level': 0,    // Cấp khu vực

      }
    },
    initOptions() {
      let parten_search = {
        params: []
      }
      // Quyền dữ liệu: Phân chia hành chínhadministrative_division
      this.pushDataPermissions(parten_search.params, this.$route.meta.routerId, '986666876811870222')
      this.parten_List.splice(0, this.parten_List.length)
      listAdministrativeDivisionAll(parten_search).then(responseData => {
        this.parten_List = responseData.data
      })
    },
    openViewAdministrativeDivisionDialog(administrativeDivision) {
      this.dialogProps.action = 'view'
      this.dialogProps.title = 'Xem phân vùng hành chính'
      this.bizFormModel = {...this.initFormModel(), ...administrativeDivision}
      this.initOptions(this.bizFormModel)
      this.tabIndex = '1'
      this.dialogProps.visible = true
    },
    openEditAdministrativeDivisionDialog(administrativeDivision) {
      this.dialogProps.action = 'edit'
      this.dialogProps.title = 'Sửa phân chia khu vực hành chính'
      this.bizFormModel = {...this.initFormModel(), ...administrativeDivision}
      this.initOptions(this.bizFormModel)
      this.tabIndex = '1'
      this.dialogProps.visible = true
    },
    openAddAdministrativeDivisionDialog() {
      this.dialogProps.action = 'add'
      this.dialogProps.title = 'Thêm phân chia khu vực hành chính'
      this.bizFormModel = this.initFormModel()
      this.initOptions(this.bizFormModel)
      this.tabIndex = '1'
      this.dialogProps.visible = true
    },
    openCopyAdministrativeDivisionDialog(administrativeDivision) {
      this.dialogProps.action = 'add'
      this.dialogProps.title = 'Thêm phân chia khu vực hành chính'
      this.bizFormModel = {...this.initFormModel(), ...administrativeDivision}
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