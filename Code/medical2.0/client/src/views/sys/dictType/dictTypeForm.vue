<template>
  <el-dialog :title='dialogProps.title' :visible.sync='dialogProps.visible' :close-on-click-modal='false' width='50%' 
    @open='onDialogOpen()' v-loading='loading'>
    <div slot='title' class='dialog-header'>
      {{ dialogProps.title }}
      <OperationIcon v-show='dialogProps.action === "view" && permission.edit' type='primary' text='Sửa' placement='top-start' icon-name='el-icon-edit' @click='switchEdit'></OperationIcon>
    </div>

    <el-form :model='bizFormModel' :rules='formRules' 
      ref='dictTypeForm' label-width='120px' label-position='right' class='edit-form'>
        <!--tabsChuyển đổi  Bắt đầu-->
        <el-row>
          <el-radio-group v-model="tabIndex" style="margin-bottom: 10px;">
            <el-radio-button label="1">Thông tin cơ bản</el-radio-button>
                <el-radio-button label="2">Mục từ điển</el-radio-button>
          </el-radio-group>
        </el-row>
        <!--tabsChuyển đổi  Kết thúc-->
      <div class="tab-item" v-show='tabIndex==="1"'>
        <!-- Biểu mẫu chính  Bắt đầu-->

              <el-row>
        <el-col :span='24/1'>
          <el-form-item label='Mã loại từ điển' prop='code' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.code' :maxlength='64' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập mã loại từ điển"' autofocus></el-input>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/1'>
          <el-form-item label='Tên loại từ điển' prop='name' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.name' :maxlength='128' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập tên loại từ điển"' ></el-input>
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
        <el-col :span='24/1' v-if="currentUser.id === 1000">
          <el-form-item label='Cấp hệ thống' prop='isSystem' >
            <el-switch :disabled='dialogProps.action === "view"' v-model='bizFormModel.isSystem' active-color='#13ce66' inactive-color='#dbdfe6' active-value='1' inactive-value='0' @change='onSystemChange'></el-switch>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/1' v-if="currentUser.id === 1000 && bizFormModel.isSystem === '0'">
          <el-form-item label='Đơn vị thuê trực thuộc' prop='company.id' :rules="[{ required: true, message: 'Vui lòng chọn đơn vị thuê trực thuộc', trigger: 'change' }]">
            <el-select :disabled='dialogProps.action === "view"' v-model='bizFormModel.company.id' filterable placeholder='Vui lòng chọn đơn vị thuê trực thuộc' style='width: 100%;'>
              <el-option v-for='item in companyList' :key='item.id' :label='item.name' :value='item.id'></el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
        <!-- Biểu mẫu chính  Kết thúc-->

      </div>

    <!--Bảng con: Mục từ điển Bắt đầu-->
    <div class="tab-item" v-show='tabIndex==="2"'>
      <el-row>
        <el-col>
            <div class="tab-option" :style="{top: tabOptionBtnTop}">
              <el-button v-if='dialogProps.action !== "view"' type='primary' :plain='true' icon='el-icon-plus'
                         @click='onAddDictItemRow(bizFormModel.dictItemList)'></el-button>
            </div>
          <el-table ref='dictType' :data='bizFormModel.dictItemList' border @current-change='(currentRow, oldCurrentRow) => {dictItemCurrentRow = currentRow}'>
            <el-table-column prop='name' label='Tên' min-width='120px' header-align='center'>
              <template slot='header'><span style="color:#F56C6C;margin-right: 4px">*</span>Tên</template>
              <template slot-scope='{row,$index}'>
                <el-form-item data-num='2' :ref="`dictItemList.${$index}.name`" v-if='dialogProps.action !== "view" && row === dictItemCurrentRow' :prop="`dictItemList.${$index}.name`" :rules='formRules.dictItem_name' label-width=0>
                  <el-input v-model='row.name'  :maxlength='64' clearable placeholder='Vui lòng nhập tên'></el-input>
                </el-form-item>
                <span v-else>{{row.name}}</span>
                  </template>
                </el-table-column>
            <el-table-column prop='value' label='giá trị' min-width='160px' header-align='center'>
              <template slot='header'><span style="color:#F56C6C;margin-right: 4px">*</span>giá trị</template>
              <template slot-scope='{row,$index}'>
                <el-form-item data-num='2' :ref="`dictItemList.${$index}.value`" v-if='dialogProps.action !== "view" && row === dictItemCurrentRow' :prop="`dictItemList.${$index}.value`" :rules='formRules.dictItem_value' label-width=0>
                  <el-input v-model='row.value'  :maxlength='128' clearable placeholder='Vui lòng nhập giá trị'></el-input>
                </el-form-item>
                <span v-else>{{row.value}}</span>
                  </template>
                </el-table-column>
            <el-table-column prop='remarks' label='Thông tin ghi chú' min-width='200px' header-align='center'>
              <template slot='header'>Thông tin ghi chú</template>
              <template slot-scope='{row,$index}'>
                <el-form-item data-num='2' :ref="`dictItemList.${$index}.remarks`" v-if='dialogProps.action !== "view" && row === dictItemCurrentRow' :prop="`dictItemList.${$index}.remarks`" :rules='formRules.dictItem_remarks' label-width=0>
                  <el-input v-model='row.remarks'  :maxlength='255' clearable placeholder='Vui lòng nhập thông tin ghi chú'></el-input>
                </el-form-item>
                <span v-else>{{row.remarks}}</span>
                  </template>
                </el-table-column>
            <el-table-column v-if='dialogProps.action !== "view"' label='Thao tác' header-align='center' align='center' width='60px'>
              <template slot-scope='scope'>
                <el-tooltip class='item' effect='light' content='Xóa' placement='top-start'>
                  <i class='el-icon-delete' style='color:#F56C6C;cursor:pointer;'
                     @click='onDeleteRow(scope.$index, bizFormModel.dictItemList)'></i>
                </el-tooltip>
              </template>
            </el-table-column>
          </el-table>
        </el-col>
      </el-row>
    </div>
    <!--Bảng con: Mục từ điển Kết thúc-->
    </el-form>

    <!-- Nút  Bắt đầu-->
    <span slot='footer' class='dialog-footer'>
      <el-button v-if='dialogProps.action !== "view"' :disabled="flag" type='primary' :plain='true' @click='onSubmit("dictTypeForm")'>Lưu</el-button>
      <el-button v-if='dialogProps.action !== "view"' :plain='true' @click='onDialogClose()'>Hủy</el-button>
      <el-button v-if='dialogProps.action === "view"' :plain='true' @click='onDialogClose()'>Đóng</el-button>
    </span>
    <!-- Nút Kết thúc-->
  </el-dialog>
</template>

<script>
import { validatenull } from '@/utils/validate'

import { saveDictType } from '@/api/sys/dictType'
import { listCompanyAll } from '@/api/org/company'
import BaseUI from '@/views/components/baseUI'
import OperationIcon from '@/components/OperationIcon'
export default {
  extends: BaseUI,
  name: 'dictType-form',
  components: {
    OperationIcon
  },
  data() {
    return {
      bizFormModel: this.initFormModel(),
      flag:false,
      tabIndex: '1',
      companyList: [],  // Danh sách đơn vị thuê
       dialogProps: {
        visible: false,
        action: '',
        title: '',

      },
      formRules: {
        // Xác thực bảng chính
        'code': [
            { required: true, message: 'Vui lòng nhập mã loại từ điển', trigger: 'blur' }
        ],
        'name': [
            { required: true, message: 'Vui lòng nhập tên loại từ điển', trigger: 'blur' }
        ],
        // Xác thực bảng con Mục từ điển
        'dictItem_name': [
            { required: true, message: 'Vui lòng nhập tên', trigger: 'blur' }
        ],
        'dictItem_value': [
            { required: true, message: 'Vui lòng nhập giá trị', trigger: 'blur' }
        ],
      },
    dictItemCurrentRow: {},     // Bảng con Mục từ điển Dòng đang chọn
    }
  },
  props: {
    // Quyền
    permission: {
      type: Object
    }
  },
  computed: {
    ...Vuex.mapGetters(["settings"]),
    tabOptionBtnTop() {
      let top
      switch (this.settings.size) {
        case 'medium':
          top = -46 + 'px'
              break;
        case 'small':
          top = -42 + 'px'
              break;
        case 'mini':
          top = -38 + 'px'
              break;
        default:
          top = -50 + 'px'
      }
      return top
    }
  },
  methods: {
    onSubmit(formName) {
      this.flag=true
      this.$refs[formName].validate((valid, object) => {
        if (valid) {
          this.doSave()
        } else {
          this.flag=false
          // Xử lý định vị kiểm tra
          let arr = []
          let numArr = []
          for (let i in object) {
            let dom = this.$refs[i]
            if (!dom) {
              this.tabIndex = 1
              return
            } else {
              arr.push(dom)
              if (Object.prototype.toString.call(arr) !== '[object Object]') {
                arr.forEach(item => {
                  numArr.push(item.$el.dataset.num)
                })
                this.tabIndex = Math.min(...numArr)
              }
            }
          }
          
          return false
        }
      });
    },
    doSave() {
      this.setLoad()
      saveDictType(this.bizFormModel).then(responseData => {
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
    onAddDictItemRow(tableData) {
        tableData.push({
            'dictType': {     // Bảng cha - ID bảng loại từ điển
              'id': validatenull(parent) || validatenull(parent.dictType) ? null : parent.dictType.id,
              'name': validatenull(parent) || validatenull(parent.dictType) ? null : parent.dictType.name,
            },
            'name': '',   // Tên
            'value': '',   // giá trị
            'remarks': '',   // Thông tin ghi chú
        })
    },
    onDeleteRow(index, tableData) {
        tableData.splice(index, 1)
    },
    switchEdit() {
      this.dialogProps.action = 'edit'
      this.dialogProps.title = 'Sửa quản lý loại từ điển'
      this.initOptions(this.bizFormModel)
    },
    onDialogClose() {
      this.dialogProps.visible = false  
    },
    onDialogOpen() {
      this.$nextTick(() => {
        this.$refs['dictTypeForm'].clearValidate()
      })
    },
    initFormModel() {
      return {
        'code': '',   // Mã loại từ điển
        'name': '',   // Tên loại từ điển
        'remarks': '',   // Thông tin ghi chú
        'isSystem': '0',   // Cấp hệ thống
        'company': {     // Đơn vị thuê trực thuộc
          'id': '',
          'name': ''
        },
        dictItemList: [],       // Danh sách bảng con
      }
    },
    initOptions() {
      // Bảng chính - Tải danh sách đơn vị thuê
      this.loadCompanyList()
      // Bảng con  Mục từ điển
    },
    loadCompanyList() {
      // Chỉ quản trị viên cấp cao mới cần tải danh sách đơn vị thuê
      if (currentUser.id === 1000) {
        listCompanyAll({
          params: [{ columnName: 'parent_id', queryType: '!=', value: '0' }]
        }).then(responseData => {
          if (responseData.code === 100) {
            this.companyList = responseData.data || []
          }
        }).catch(error => {
          console.error('Tải danh sách đơn vị thất bại', error)
        })
      }
    },
    onSystemChange(val) {
      // Khi chuyển sang cấp hệ thống, xoá lựa chọn đơn vị thuê
      if (val === '1') {
        this.bizFormModel.company = null
      } else {
        // Khi chuyển sang cấp nghiệp vụ, khởi tạo đối tượng công ty
        this.bizFormModel.company = { id: '', name: '' }
      }
    },
    openViewDictTypeDialog(dictType) {
      this.dialogProps.action = 'view'
      this.dialogProps.title = 'Xem quản lý loại từ điển'
      this.bizFormModel = dictType
      this.initOptions(this.bizFormModel)
      this.tabIndex = '1'
      this.dialogProps.visible = true
    },
    openEditDictTypeDialog(dictType) {
      this.dialogProps.action = 'edit'
      this.dialogProps.title = 'Sửa quản lý loại từ điển'
      // sys_dict_type has no company column, so a loaded record carries no company
      // while backend update validation requires it; fall back to the login company.
      if (!dictType.company || !dictType.company.id) dictType.company = { id: currentUser.company.id, name: null }
      this.bizFormModel = dictType
      this.initOptions(this.bizFormModel)
      this.tabIndex = '1'
      this.dialogProps.visible = true
    },
    openAddDictTypeDialog() {
      this.dialogProps.action = 'add'
      this.dialogProps.title = 'Thêm quản lý loại từ điển'
      this.bizFormModel = this.initFormModel()
      this.initOptions(this.bizFormModel)
      this.tabIndex = '1'
      this.dialogProps.visible = true
    },
    openCopyDictTypeDialog(dictType) {
      if (!dictType.company || !dictType.company.id) dictType.company = { id: currentUser.company.id, name: null }
      this.dialogProps.action = 'add'
      this.dialogProps.title = 'Thêm quản lý loại từ điển'
      this.bizFormModel = dictType
      this.initOptions(this.bizFormModel)
      this.tabIndex = '1'
      this.bizFormModel.id = null
      for (let i = 0; i <= this.bizFormModel.dictItemList.length - 1; i++) {
          this.bizFormModel.dictItemList[i].id = null
      }
      this.dialogProps.visible = true
    }
  },
  watch: {
  },
  mounted: function() {
  },
}
</script>
<style lang="scss" scoped>
  .tab-item {
    position: relative;
    .tab-option {
      position: absolute;
      top: -46px;
      right: 0;
    }
  }
</style>