<template>
  <el-dialog :title='dialogProps.title' :visible.sync='dialogProps.visible' :close-on-click-modal='false' width='30%'
    @open='onDialogOpen()' v-loading='loading'>
    <div slot='title' class='dialog-header'>
      {{ dialogProps.title }}
      <OperationIcon v-show='dialogProps.action === "view" && permission.edit' type='primary' text='Sửa' placement='top-start' icon-name='el-icon-edit' @click='switchEdit'></OperationIcon>
    </div>
    <el-form :model='bizFormModel' :rules='formRules'
      ref='departmentForm' label-width='120px' label-position='right' class='edit-form'>

              <el-row>
        <el-col :span='24/1'>
          <el-form-item label='Bộ phận cấp trên' prop='parent.id'>
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.parent.name'></el-input>
            <el-cascader v-else v-model='bizFormModel.parent.id' :options='parent_List' :key='key_parent' ref='parentCascader' @change='onParentChange' :props='{value: "id", label: "name", checkStrictly: true, emitPath: false}' filterable clearable placeholder='Vui lòng chọn phòng ban cấp trên' />
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/1'>
          <el-form-item label='Mã' prop='code'>
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.code' :maxlength='10' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập mã"' ></el-input>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/1'>
          <el-form-item label='Tên' prop='name'>
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.name' :maxlength='100' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập tên"' ></el-input>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/1'>
          <el-form-item label='Sắp xếp' prop='sort'>
            <number-input v-if='dialogProps.action === "view"' disabled v-model="bizFormModel.sort" :precision="0"></number-input>
            <number-input v-else v-model="bizFormModel.sort" :precision="0"></number-input>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/1'>
          <el-form-item label='Giám đốc' prop='director.id'>
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.director.name'></el-input>
            <el-select v-else v-model='bizFormModel.director' value-key='id' filterable clearable placeholder='Vui lòng chọn giám đốc' @clear='bizFormModel.director={"id": null,"name": null,}'>
             <el-option v-for='director in director_List' :key='director.id' :label='director.name' :value='director'></el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/1'>
          <el-form-item label='Quản lý' prop='manager.id'>
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.manager.name'></el-input>
            <el-select v-else v-model='bizFormModel.manager' value-key='id' filterable clearable placeholder='Vui lòng chọn quản lý' @clear='bizFormModel.manager={"id": null,"name": null,}'>
             <el-option v-for='manager in manager_List' :key='manager.id' :label='manager.name' :value='manager'></el-option>
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
      <el-button v-if='dialogProps.action !== "view"' :disabled="flag" type='primary' :plain='true' @click='onSubmit("departmentForm")'>Lưu</el-button>
      <el-button v-if='dialogProps.action !== "view"' :plain='true' @click='onDialogClose()'>Hủy</el-button>
      <el-button v-if='dialogProps.action === "view"' :plain='true' @click='onDialogClose()'>Đóng</el-button>
    </span>
  </el-dialog>
</template>
<script>
import { validatenull } from '@/utils/validate'
import { treeCompany } from '@/api/org/company'
import { treeDepartment } from '@/api/org/department'
import { listUserAll } from '@/api/admin/user'
import { saveDepartment } from '@/api/org/department'
import BaseUI from '@/views/components/baseUI'
import OperationIcon from '@/components/OperationIcon'
export default {
  extends: BaseUI,
  name: 'department-form',
  components: {
    OperationIcon
  },
  data() {
    return {
      bizFormModel: this.initFormModel(),
      flag:false,
                  key_parent: 1,    // el-cascader key
          parent_List: [],  // Bộ phận cấp trên
                                          director_List: [],  // Giám đốc
                  manager_List: [],  // Quản lý
               dialogProps: {
        visible: false,
        action: '',
        title: '',
        
      },
      formRules: {
        'company.id': [
            { required: true, message: 'Vui lòng chọn công ty', trigger: 'change' }
        ],
        'code': [
            { required: true, message: 'Vui lòng nhập mã', trigger: 'blur' }
        ],
        'name': [
            { required: true, message: 'Vui lòng nhập tên', trigger: 'blur' }
        ],
        'sort': [
            { required: true, message: 'Vui lòng chọn sắp xếp', trigger: 'blur' }
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
      saveDepartment(this.bizFormModel).then(responseData => {
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
    onDialogClose() {
      this.dialogProps.visible = false
    },
    onDialogOpen() {
      this.$nextTick(() => {
        this.$refs['departmentForm'].clearValidate()
      })
    },
    switchEdit() {
      this.dialogProps.action = 'edit'
      this.dialogProps.title = 'Sửa bộ phận'
      this.initOptions(this.bizFormModel)
    },
    onParentChange() {
      let nodes = this.$refs['parentCascader'].getCheckedNodes()
      if(nodes.length > 0) {
        if(nodes[0] && nodes[0].data && nodes[0].data.children) {
          this.bizFormModel.sort = (nodes[0].data.children.length + 1) * 10000
        } else if(nodes[0]){
            this.bizFormModel.sort = 10000
        } else {
          this.bizFormModel.sort = (this.parent_List.length + 1) * 10000
        }
      }else{
        this.bizFormModel.sort = (this.parent_List.length + 1) * 10000
      }
    },
    initFormModel(This) {
      return {
        'company': {     // Bảng cha Công ty
          'id': validatenull(This) || validatenull(This.company) ? null : This.company.id,
          'name': validatenull(This) || validatenull(This.company) ? null : This.company.name,
        },
        'parent': {     // Nút cấp trên Bộ phận cấp trên
          'id': validatenull(This) || validatenull(This.id) ? null : This.id,
          'name': validatenull(This) || validatenull(This.name) ? null : This.name,
        },
        'code': '',   // Mã
        'name': '',   // Tên
          'sort': This && This.children ? (This.children.length + 1) * 10000 : (this.parent_List ? (this.parent_List + 1) * 10000 : 10000),
        'director': {     // Giám đốc
          'id': null,
          'name': null,
        },
        'manager': {     // Quản lý
          'id': null,
          'name': null,
        },
        'remarks': '',   // Thông tin ghi chú

      }
    },
    initOptions(This) {

                this.key_parent++
      let parent_search = {
        params: [{'columnName':'company_id', 'queryType': '=', 'value': This.company.id}]
      }
        // Trên bảng tương ứng của trườngfilterĐiều kiện
        parent_search.params.push.apply(parent_search.params, [])
      // Quyền dữ liệu: Phòng banorg_department
      this.pushDataPermissions(parent_search.params, this.$route.meta.routerId, '52676365136650250')
      this.parent_List.splice(0, this.parent_List.length)
      treeDepartment(parent_search).then(responseData => {
        this.parent_List = responseData.data
      })
                                                        let director_search = {
        params: []
      }
        // Trên bảng tương ứng của trườngfilterĐiều kiện
        director_search.params.push.apply(director_search.params, [])
        // Bảng có trường bị vô hiệu hóa
        director_search.params.push.apply(director_search.params, [{columnName: 'is_locked', queryType: '=', value: '0'}])
      // Quyền dữ liệu:  Người dùngsys_user
      this.pushDataPermissions(director_search.params, this.$route.meta.routerId, '4004')
      this.director_List = []
      listUserAll(director_search).then(responseData => {
        this.director_List = responseData.data
      })
                let manager_search = {
        params: []
      }
        // Trên bảng tương ứng của trườngfilterĐiều kiện
        manager_search.params.push.apply(manager_search.params, [])
        // Bảng có trường bị vô hiệu hóa
        manager_search.params.push.apply(manager_search.params, [{columnName: 'is_locked', queryType: '=', value: '0'}])
      // Quyền dữ liệu:  Người dùngsys_user
      this.pushDataPermissions(manager_search.params, this.$route.meta.routerId, '4004')
      this.manager_List = []
      listUserAll(manager_search).then(responseData => {
        this.manager_List = responseData.data
      })

    },
    openViewDepartmentDialog(department) {
      this.dialogProps.action = 'view'
      this.dialogProps.title = 'Xem phòng ban'
      this.bizFormModel = department
      this.dialogProps.visible = true
    },
    openEditDepartmentDialog(department) {
      this.dialogProps.action = 'edit'
      this.dialogProps.title = 'Sửa bộ phận'
      this.bizFormModel = department
      this.initOptions(this.bizFormModel)
      this.dialogProps.visible = true
    },
    openAddDepartmentDialog(parent) {
      this.dialogProps.action = 'add'
      this.dialogProps.title = 'Thêm bộ phận'
      this.bizFormModel = this.initFormModel(parent)
      this.initOptions(this.bizFormModel)
      this.dialogProps.visible = true
    },
    openCopyDepartmentDialog(department) {
      this.dialogProps.action = 'add'
      this.dialogProps.title = 'Thêm bộ phận'
      this.bizFormModel = department
      this.initOptions(this.bizFormModel)
      this.bizFormModel.id = null
      this.dialogProps.visible = true
    },
  },
  watch: {
    parent_List(newVal, oldVal) {
      if(newVal !== oldVal && this.dialogProps.action === 'add' ) {
        this.$nextTick(() => {
          this.onParentChange()
        })
      }
    }

  },
  mounted: function() {
  },
}
</script>
