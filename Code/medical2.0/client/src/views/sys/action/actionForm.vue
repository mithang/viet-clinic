<template>
  <el-dialog :title='dialogProps.title' :visible.sync='dialogProps.visible' :close-on-click-modal='false' width='30%' 
    @open='onDialogOpen()' v-loading='loading'>
    <div slot='title' class='dialog-header'>
      {{ dialogProps.title }}
      <OperationIcon v-show='dialogProps.action === "view" && permission.edit' type='primary' text='Sửa' placement='top-start' icon-name='el-icon-edit' @click='switchEdit'></OperationIcon>
    </div>    
    
    <el-form :model='actionModel' :rules='formRules' 
      ref='actionForm' label-width='120px' label-position='right' class='edit-form'>    
    <!-- Biểu mẫu chính  Bắt đầu-->
      <el-row>
        <el-col :span='24/1'>
          <el-form-item label='Thao tác' prop='actionType.value'>
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='actionModel.actionType.name'></el-input>
            <el-select v-else v-model='actionModel.actionType' value-key='value' filterable clearable placeholder='Vui lòng chọn thao tác'>
             <el-option v-for='actionType in actionType_List' :key='actionType.value' :label='actionType.name' :value='actionType'></el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span='24/1'>
          <el-form-item label='Loại đối tượng' prop='objectType.id'>
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='actionModel.objectType.comments'></el-input>
            <el-select v-else v-model='actionModel.objectType' value-key='id' filterable clearable placeholder='Vui lòng chọn loại đối tượng'>
             <el-option v-for='objectType in objectType_List' :key='objectType.id' :label='objectType.comments' :value='objectType'></el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span='24/1'>
          <el-form-item label='Đối tượng nghiệp vụ' prop='objectId'>
            <el-input :disabled='dialogProps.action === "view"' v-model='actionModel.objectId'  :maxlength='40' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập đối tượng nghiệp vụ"' ></el-input>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span='24/1'>
          <el-form-item label='Tên đối tượng' prop='objectName'>
            <el-input :disabled='dialogProps.action === "view"' v-model='actionModel.objectName'  :maxlength='512' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập tên đối tượng"' ></el-input>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col>
          <el-form-item label='Thông tin ghi chú' prop='remarks'>        
            <el-input :disabled='dialogProps.action === "view"' v-model='actionModel.remarks' type='textarea'  
              :maxlength='255' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập thông tin ghi chú"' ></el-input>
          </el-form-item>
        </el-col>
      </el-row>
    <!-- Biểu mẫu chính  Kết thúc-->
    
    <!--Bảng con: Thùng rác Bắt đầu-->
      <el-divider content-position="left"> Thùng rác</el-divider>
      <el-row>
        <el-col>
    <el-table ref='action' :data='actionModel.actionRecycleList' border @current-change='(currentRow, oldCurrentRow) => {actionRecycleCurrentRow = currentRow}'>
      <el-table-column prop='tableName' label='Tên bảng' min-width='180px' header-align='center'>
        <template slot='header'><span style="color:#F56C6C;margin-right: 4px">*</span>Tên bảng</template>
        <template slot-scope='{row,$index}'>
          <el-form-item v-if='dialogProps.action !== "view" && row === actionRecycleCurrentRow' :prop="`actionRecycleList.${$index}.tableName`" :rules='formRules.actionRecycle_tableName' label-width=0>
            <el-input v-model='row.tableName'  :maxlength='200' clearable placeholder='Vui lòng nhập tên bảng'></el-input>
          </el-form-item>
          <span v-else>{{row.tableName}}</span>
        </template>
      </el-table-column>
      <el-table-column prop='objectId' label='Đối tượng nghiệp vụ' min-width='180px' header-align='center'>
        <template slot='header'><span style="color:#F56C6C;margin-right: 4px">*</span>Đối tượng nghiệp vụ</template>
        <template slot-scope='{row,$index}'>
          <el-form-item v-if='dialogProps.action !== "view" && row === actionRecycleCurrentRow' :prop="`actionRecycleList.${$index}.objectId`" :rules='formRules.actionRecycle_objectId' label-width=0>
            <el-input v-model='row.objectId'  :maxlength='40' clearable placeholder='Vui lòng nhập đối tượng nghiệp vụ'></el-input>
          </el-form-item>
          <span v-else>{{row.objectId}}</span>
        </template>
      </el-table-column>
      <el-table-column prop='objectName' label='Tên đối tượng' min-width='180px' header-align='center'>
        <template slot='header'>Tên đối tượng</template>
        <template slot-scope='{row,$index}'>
          <el-form-item v-if='dialogProps.action !== "view" && row === actionRecycleCurrentRow' :prop="`actionRecycleList.${$index}.objectName`" :rules='formRules.actionRecycle_objectName' label-width=0>
            <el-input v-model='row.objectName'  :maxlength='200' clearable placeholder='Vui lòng nhập tên đối tượng'></el-input>
          </el-form-item>
          <span v-else>{{row.objectName}}</span>
        </template>
      </el-table-column>
      <el-table-column v-if='dialogProps.action !== "view"' label='Thao tác' header-align='center' align='center' width='60px'>
        <template slot-scope='scope'>
          <el-tooltip class='item' effect='light' content='Xóa' placement='top-start'>
            <i class='el-icon-delete' style='color:#F56C6C;cursor:pointer;'
              @click='onDeleteRow(scope.$index, actionModel.actionRecycleList)'></i>
          </el-tooltip>         
        </template>
      </el-table-column>          
    </el-table>
    <el-button v-if='dialogProps.action !== "view"' type='primary' 
      @click='onAddActionRecycleRow(actionModel.actionRecycleList)'>Thêm</el-button>
        </el-col>
      </el-row>
      <!--Bảng con: Thùng rác Kết thúc-->
    </el-form>
        
    <!-- Nút  Bắt đầu-->
    <span slot='footer' class='dialog-footer'>
      <el-button v-if='dialogProps.action !== "view"' :disabled="flag" type='primary' :plain='true' @click='onSubmit("actionForm")'>Lưu</el-button>    
      <el-button v-if='dialogProps.action !== "view"' :plain='true' @click='onDialogClose()'>Hủy</el-button>
      <el-button v-if='dialogProps.action === "view"' :plain='true' @click='onDialogClose()'>Đóng</el-button>
    </span> 
    <!-- Nút Kết thúc-->
  </el-dialog>
</template>

<script>
import { validatenull } from '@/utils/validate'

import '@/api/sys/dictItem';
import { getDictItemsByCode, DICT_CODE } from '@/utils/dictCache'
import { listGenTableAll } from '@/api/gen/genTable'

import { saveAction } from '@/api/sys/action'
import BaseUI from '@/views/components/baseUI'
import OperationIcon from '@/components/OperationIcon'
export default {
  extends: BaseUI,
  name: 'action-form',
  components: {
    OperationIcon
  },  
  data() {
    return {
      actionModel: this.initFormModel(),
          actionType_List: [],  // Thao tác
          objectType_List: [],  // Loại đối tượng
          flag:false,
       dialogProps: {
        visible: false,
        action: '',
        title: '',
        
      },
      formRules: {
        // Xác thực bảng chính
        // Xác thực bảng con Thùng rác
        'actionRecycle_tableName': [
            { required: true, message: 'Vui lòng nhập tên bảng', trigger: 'blur' }
        ],
        'actionRecycle_objectId': [
            { required: true, message: 'Vui lòng nhập đối tượng nghiệp vụ', trigger: 'blur' }
        ],
      },
    actionRecycleCurrentRow: {},     // Bảng con Thùng rác Dòng đang chọn
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
      saveAction(this.actionModel).then(responseData => {
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
    onAddActionRecycleRow(tableData) {
        tableData.push({
            'action': {     // Bảng cha Nhật ký
              'id': validatenull(parent) || validatenull(parent.action) ? null : parent.action.id,
            },
            'tableName': '',   // Tên bảng
            'objectId': '',   // Đối tượng nghiệp vụ
            'objectName': '',   // Tên đối tượng
            'remarks': '',   // Thông tin ghi chú
        })
    },
    onDeleteRow(index, tableData) {
        tableData.splice(index, 1)
    },
    onDialogClose() {
      this.dialogProps.visible = false  
    },
    onDialogOpen() {
      this.$nextTick(() => {
        this.$refs['actionForm'].clearValidate()
      })
    },
    switchEdit() {
      this.dialogProps.action = 'edit'
      this.dialogProps.title = 'Sửa nhật ký thao tác'
      this.initOptions(this.actionModel)
    },
    initFormModel() {
      return {
        'actionType': {     // Thao tác
          'value': '',
          'name': '',
        },
        'objectType': {     // Loại đối tượng
          'id': 0,
          'comments': '',
        },
        'objectId': '',   // Đối tượng nghiệp vụ
        'objectName': '',   // Tên đối tượng
        'status': 0,
        'remarks': '',   // Thông tin ghi chú

        actionRecycleList: [],       // Danh sách bảng con
      }
    },
    initOptions() {
      // Bảng chính
      let This = this.actionModel
      getDictItemsByCode(DICT_CODE.ACTION_TYPE).then((data) => {
        this.actionType_List = data
      })
      let objectType_search = {
        params: []
      }
      // Trên bảng tương ứng của trườngfilterĐiều kiện
        objectType_search.params.push.apply(objectType_search.params, [])
      // Quyền dữ liệu: Bảng nghiệp vụgen_table
      this.pushDataPermissions(objectType_search.params, this.$route.meta.routerId, '4001')
      this.objectType_List = []
      listGenTableAll(objectType_search).then(responseData => {
        this.objectType_List = responseData.data
      })

      // Bảng con  Thùng rác
      This = this.actionRecycleCurrentRow
    },
    openViewActionDialog(action) {
      this.dialogProps.action = 'view'
      this.dialogProps.title = 'Xem nhật ký thao tác'
      this.actionModel = action
      this.dialogProps.visible = true
    },
    openEditActionDialog(action) {
      this.dialogProps.action = 'edit'
      this.dialogProps.title = 'Sửa nhật ký thao tác'
      this.actionModel = action
      this.initOptions()
      this.dialogProps.visible = true
    },
    openAddActionDialog() {
      this.dialogProps.action = 'add'
      this.dialogProps.title = 'Thêm nhật ký thao tác'
      this.actionModel = this.initFormModel()
      this.initOptions()
      this.dialogProps.visible = true
    },
    openCopyActionDialog(action) {
      this.dialogProps.action = 'edit'
      this.dialogProps.title = 'Thêm nhật ký thao tác'
      this.actionModel = action
      this.initOptions()
      this.actionModel.id = null
      for (let i = 0; i <= this.actionModel.actionRecycleList.length - 1; i++) {
          this.actionModel.actionRecycleList[i].id = null
      }
      this.dialogProps.visible = true
    },
  },
  watch: {
  },
  mounted: function() {
  },
}
</script>