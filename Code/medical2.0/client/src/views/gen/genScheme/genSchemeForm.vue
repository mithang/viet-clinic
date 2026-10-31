<template>
  <el-dialog :title='dialogProps.title' :visible.sync='dialogProps.visible' :close-on-click-modal='false' width='850px'
    @open='onDialogOpen()' v-loading='loading'>
    <div slot='title' class='dialog-header'>
      {{ dialogProps.title }}
      <OperationIcon v-show='dialogProps.action === "view" && permission.edit' type='primary' text='Sửa' placement='top-start' icon-name='el-icon-edit' @click='switchEdit'></OperationIcon>
    </div>
    <el-form :model='bizFormModel' :rules='formRules'
      ref='genSchemeForm' label-width='120px' label-position='right' class='edit-form'>
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Tên' prop='name'>
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.name' :maxlength='200' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập tên"' autofocus></el-input>
          </el-form-item>
        </el-col>
                <el-col :span='24/2'>
          <el-form-item label='Phân loại mẫu' prop='category.value'>
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.category.name'></el-input>
            <el-select v-else v-model='bizFormModel.category' value-key='value' filterable clearable placeholder='Vui lòng chọn phân loại mẫu'
              @clear='bizFormModel.category={
                "value": null,
                                "name": null,
                }'>
             <el-option v-for='category in category_List' :key='category.value' :label='category.name' :value='category'></el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Đường dẫn gói' prop='packageName'>
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.packageName' :maxlength='500' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập đường dẫn gói"' ></el-input>
          </el-form-item>
        </el-col>
                <el-col :span='24/2'>
          <el-form-item label='Tên mô-đun' prop='moduleName'>
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.moduleName' :maxlength='30' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập tên mô-đun"' ></el-input>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Tên mô-đun con' prop='subModuleName'>
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.subModuleName' :maxlength='30' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập tên mô-đun con"' ></el-input>
          </el-form-item>
        </el-col>
                <el-col :span='24/2'>
          <el-form-item label='Tên chức năng' prop='functionName'>
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.functionName' :maxlength='500' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập tên chức năng"' ></el-input>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Tên viết tắt chức năng' prop='functionNameSimple'>
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.functionNameSimple' :maxlength='100' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập tên viết tắt chức năng"' ></el-input>
          </el-form-item>
        </el-col>
                <el-col :span='24/2'>
          <el-form-item label='Tác giả' prop='functionAuthor'>
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.functionAuthor' :maxlength='100' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập tác giả"' ></el-input>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Độ rộng chỉnh sửa' prop='dialogWidth'>
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.dialogWidth' :maxlength='20' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập độ rộng chỉnh sửa"' ></el-input>
          </el-form-item>
        </el-col>
                <el-col :span='24/2'>
          <el-form-item label='Số cột chỉnh sửa' prop='colCounts'>
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.colCounts'></el-input>
            <el-input-number v-else v-model='bizFormModel.colCounts' controls-position='right'></el-input-number>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Bảng nghiệp vụ' prop='genTable.id'>
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.genTable.name'></el-input>
            <el-select v-else v-model='bizFormModel.genTable' value-key='id' filterable clearable placeholder='Vui lòng chọn bảng nghiệp vụ'
              @clear='bizFormModel.genTable={
                "id": null,
                                "name": null,
                }'>
             <el-option v-for='genTable in genTable_List' :key='genTable.id' :label='genTable.name + " :" + genTable.comments' :value='genTable'></el-option>    
            </el-select>
          </el-form-item>
        </el-col>
              </el-row>
      <el-row>
        <el-col>
          <el-form-item label='Thông tin ghi chú' prop='remarks'>
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.remarks' type='textarea' 
             :maxlength='255' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập thông tin ghi chú"' ></el-input>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <span slot='footer' class='dialog-footer'>
      <el-dropdown v-if='dialogProps.action !== "view"' split-button type='warning' @click='generate("genSchemeForm", false)'>
        Tạo mã
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item><el-button  type='text' :plain='true' @click='generate("genSchemeForm", true)'>Ghi đè tệp</el-button></el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
      <el-button v-if='dialogProps.action !== "view"' :disabled="flag" type='primary' :plain='true' @click='onSubmit("genSchemeForm")'>Lưu</el-button>
      <el-button v-if='dialogProps.action !== "view"' :plain='true' @click='onDialogClose()'>Hủy</el-button>
      <el-button v-if='dialogProps.action === "view"' :plain='true' @click='onDialogClose()'>Đóng</el-button>
    </span>
  </el-dialog>
</template>
<script>
import '@/utils/validate';
import { generateCode } from '@/api/gen/genCode'
import '@/api/sys/dictItem';
import { getDictItemsByCode } from '@/utils/dictCache';
import { listGenTableAll } from '@/api/gen/genTable'
import { saveGenScheme } from '@/api/gen/genScheme'
import BaseUI from '@/views/components/baseUI'
import OperationIcon from '@/components/OperationIcon'
export default {
  extends: BaseUI,
  name: 'genScheme-form',
  components: {
    OperationIcon
  },
  data() {
    return {
      bizFormModel: this.initFormModel(),
          category_List: [],  // Phân loại mẫu
          genTable_List: [],  // Bảng nghiệp vụ
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
        'category.value': [
            { required: true, message: 'Vui lòng chọn phân loại mẫu', trigger: 'change' }
        ],
        'packageName': [
            { required: true, message: 'Vui lòng nhập đường dẫn gói', trigger: 'blur' }
        ],
        'moduleName': [
            { required: true, message: 'Vui lòng nhập tên mô-đun', trigger: 'blur' }
        ],
        'functionName': [
            { required: true, message: 'Vui lòng nhập tên chức năng', trigger: 'blur' }
        ],
        'functionNameSimple': [
            { required: true, message: 'Vui lòng nhập tên viết tắt chức năng', trigger: 'blur' }
        ],
        'dialogWidth': [
            { required: true, message: 'Vui lòng nhập độ rộng chỉnh sửa', trigger: 'blur' }
        ],
        'colCounts': [
            { required: true, message: 'Vui lòng chọn số cột hiển thị', trigger: 'blur' }
        ],
        'genTable.id': [
            { required: true, message: 'Vui lòng chọn bảng nghiệp vụ', trigger: 'change' }
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
      saveGenScheme(this.bizFormModel).then(responseData => {
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
      this.dialogProps.title = 'Sửa phương án mã'
      this.initOptions(this.bizFormModel)
    },
    onDialogClose() {
      this.dialogProps.visible = false
    },
    onDialogOpen() {
      this.$nextTick(() => {
        this.$refs['genSchemeForm'].clearValidate()
      })
    },
    initFormModel() {
      return {
        'name': '',   // Tên
        'category': {     // Phân loại mẫu
          'value': null,
          'name': null,
        },
        'packageName': '',   // Đường dẫn gói
        'moduleName': '',   // Tên mô-đun
        'subModuleName': '',   // Tên mô-đun con
        'functionName': '',   // Tên chức năng
        'functionNameSimple': '',   // Tên viết tắt chức năng
        'functionAuthor': '',   // Tác giả
        'dialogWidth': '',   // Độ rộng chỉnh sửa
        'colCounts': 0,    // Số cột chỉnh sửa
        'genTable': {     // Bảng nghiệp vụ
          'id': null,
          'name': null,
        },
        'remarks': '',   // Thông tin ghi chú

      }
    },
    initOptions() {
      getDictItemsByCode('template').then((data) => {
        this.category_List = data
      })
      let genTable_search = {
        params: []
      }
        // Trên bảng tương ứng của trườngfilterĐiều kiện
        genTable_search.params.push.apply(genTable_search.params, [])
      // Quyền dữ liệu: Bảng nghiệp vụgen_table
      this.pushDataPermissions(genTable_search.params, this.$route.meta.routerId, '4001')
      this.genTable_List.splice(0, this.genTable_List.length)
      listGenTableAll(genTable_search).then(responseData => {
        this.genTable_List = responseData.data
      })
    },
    doGenerateCode() {
      this.setLoad()
      generateCode(this.bizFormModel).then(responseData => {
        if(responseData.code === 100) {
          this.loading = false
          this.dialogProps.visible = false
          this.$emit('save-finished')
        }
        this.showMessage(responseData)
        this.resetLoad()
      }).catch(error => {
        this.outputError(error)
      })
    },
    generate(formName, replace) {
      this.$refs[formName].validate(valid => {
        if (valid) {
          /* Có thay thế tệp không */
          this.bizFormModel.replace = replace
          this.doGenerateCode();
        } else {
          return false
        }
      })
    },
    openViewGenSchemeDialog(genScheme) {
      this.dialogProps.action = 'view'
      this.dialogProps.title = 'Xem phương án mã'
      this.bizFormModel = genScheme
      this.initOptions(this.bizFormModel)
      this.dialogProps.visible = true
    },
    openEditGenSchemeDialog(genScheme) {
      this.dialogProps.action = 'edit'
      this.dialogProps.title = 'Sửa phương án mã'
      this.bizFormModel = genScheme
      this.initOptions(this.bizFormModel)
      this.dialogProps.visible = true
    },
    openAddGenSchemeDialog() {
      this.dialogProps.action = 'add'
      this.dialogProps.title = 'Thêm phương án mã'
      this.bizFormModel = this.initFormModel()
      this.initOptions(this.bizFormModel)
      this.dialogProps.visible = true
    },
    openCopyGenSchemeDialog(genScheme) {
      this.dialogProps.action = 'add'
      this.dialogProps.title = 'Thêm phương án mã'
      this.bizFormModel = genScheme
      this.initOptions(this.bizFormModel)
      this.bizFormModel.id = null
      this.dialogProps.visible = true
    },
  }
}
</script>
