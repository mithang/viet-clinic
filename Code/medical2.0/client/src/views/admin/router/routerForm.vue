<template>
  <el-dialog :title='dialogProps.title' :visible.sync='dialogProps.visible' :close-on-click-modal='false' width='50%' 
    @open='onDialogOpen()' v-loading='loading'>
    <div slot='title' class='dialog-header'>
      {{ dialogProps.title }}
      <OperationIcon v-show='dialogProps.action === "view" && permission.edit' type='primary' text='Sửa' placement='top-start' icon-name='el-icon-edit' @click='switchEdit'></OperationIcon>
    </div>
    <el-form :model='bizFormModel' :rules='formRules' 
      ref='routerForm' label-width='120px' label-position='right' class='edit-form'>    

              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Cấp cha' prop='parent.id' >
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.parent.name'></el-input>
            <el-cascader v-else v-model='bizFormModel.parent.id' :options='parent_List' :key='key_parent' ref='parentCascader' @change='onParentChange' :props='{value: "id", label: "name", checkStrictly: true, emitPath: false}' filterable clearable placeholder='Vui lòng chọn cấp cha' />
          </el-form-item>
        </el-col>
                <el-col :span='24/2'>
          <el-form-item label='Tên' prop='name' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.name' :maxlength='128' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập tên"' ></el-input>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Mã nguồn' prop='code' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.code' :maxlength='64' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập mã"' ></el-input>
          </el-form-item>
        </el-col>
                <el-col :span='24/2'>
          <el-form-item label='URL' prop='url' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.url' :maxlength='128' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhậpURL"' ></el-input>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Có thể cấp quyền' prop='canPermission' >
            <el-switch :disabled='dialogProps.action === "view"' v-model='bizFormModel.canPermission' active-color='#13ce66' inactive-color='#dbdfe6' active-value='1' inactive-value='0'></el-switch>
          </el-form-item>
        </el-col>
                <el-col :span='24/2'>
          <el-form-item label='Vô hiệu hóa' prop='isLocked' >
            <el-switch :disabled='dialogProps.action === "view"' v-model='bizFormModel.isLocked' active-color='#13ce66' inactive-color='#dbdfe6' :active-value='1' :inactive-value='0'></el-switch>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Số thứ tự hiển thị' prop='sort' >
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.sort'></el-input>
            <el-input-number v-else v-model='bizFormModel.sort' controls-position='right'></el-input-number>
          </el-form-item>
        </el-col>
                        </el-row>
            <el-row>
                <el-col>
                    <el-form-item label='Thuộc tính mở rộng' prop='properties' >
                        <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.properties' type='textarea'
                                  :maxlength='1024' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập thuộc tính mở rộng"'  clearable></el-input>
                    </el-form-item>
                </el-col>
            </el-row>
    </el-form>
    <span slot='footer' class='dialog-footer'>
      <el-button v-if='dialogProps.action !== "view"' :disabled="flag" type='primary' :plain='true' @click='onSubmit("routerForm")'>Lưu</el-button>
      <el-button v-if='dialogProps.action !== "view"' :plain='true' @click='onDialogClose()'>Hủy</el-button>
      <el-button v-if='dialogProps.action === "view"' :plain='true' @click='onDialogClose()'>Đóng</el-button>
    </span>    
  </el-dialog>
</template>
<script>
import { validatenull } from '@/utils/validate'
import { treeRouter } from '@/api/admin/router'
import { saveRouter } from '@/api/admin/router'
import BaseUI from '@/views/components/baseUI'
import OperationIcon from '@/components/OperationIcon'
export default {
  extends: BaseUI,
  name: 'router-form',
  components: {
    OperationIcon
  },  
  data() {
    return {
      bizFormModel: this.initFormModel(),
          key_parent: 1,    // el-cascader key
          parent_List: [],  // Cấp cha
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
        'properties': [
            { required: true, message: 'Vui lòng nhập thuộc tính mở rộng', trigger: 'blur' }
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
      saveRouter(this.bizFormModel).then(responseData => {
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
      this.dialogProps.title = 'Sửa tuyến đường'
      this.initOptions(this.bizFormModel)
    },
    onDialogClose() {
      this.dialogProps.visible = false  
    },
    onDialogOpen() {
      this.$nextTick(() => {
        this.$refs['routerForm'].clearValidate()
      })
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
        'parent': {     // Nút cấp trên Cấp cha
          'id': validatenull(This) || validatenull(This.id) ? null : This.id,
          'name': validatenull(This) || validatenull(This.name) ? null : This.name,
        },
        'name': '',   // Tên
        'code': '',   // Mã nguồn
        'url': '',   // URL
        'canPermission': '1',   // Có thể cấp quyền
        'isLocked': 0,    // Vô hiệu hóa
          'sort': This && This.children ? (This.children.length + 1) * 10000 : (this.parent_List ? (this.parent_List.length + 1) * 10000 : 10000),
        'properties': '',   // Thuộc tính mở rộng

      }
    },
    initOptions(This) {
      this.key_parent++
      let parent_search = {
        params: []
      }
        // Trên bảng tương ứng của trườngfilterĐiều kiện
        parent_search.params.push.apply(parent_search.params, [])
        // Bảng có trường bị vô hiệu hóa
        parent_search.params.push.apply(parent_search.params, [{columnName: 'is_locked', queryType: '=', value: '0'}])
      // Quyền dữ liệu: Định tuyếnsys_router
      this.pushDataPermissions(parent_search.params, this.$route.meta.routerId, '4003')
      this.parent_List.splice(0, this.parent_List.length)
      treeRouter(parent_search).then(responseData => {
        this.parent_List = responseData.data
      }).catch(error => {
        this.outputError(error)
      })
    },
    openViewRouterDialog(router) {
      this.dialogProps.action = 'view'
      this.dialogProps.title = 'Xem tuyến đường'
      this.bizFormModel = router
      this.initOptions(this.bizFormModel)
      this.dialogProps.visible = true
    },
    openEditRouterDialog(router) {
      this.dialogProps.action = 'edit'
      this.dialogProps.title = 'Sửa tuyến đường'
      this.bizFormModel = router
      this.initOptions(this.bizFormModel)
      this.dialogProps.visible = true
    },
    openAddRouterDialog(parent) {
      this.dialogProps.action = 'add'
      this.dialogProps.title = 'Thêm tuyến đường'
      this.bizFormModel = this.initFormModel(parent)
      this.initOptions(this.bizFormModel)
      this.dialogProps.visible = true
    },
    openCopyRouterDialog(router) {
      this.dialogProps.action = 'add'
      this.dialogProps.title = 'Thêm tuyến đường'
      this.bizFormModel = router
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
    },
  },
  mounted: function() {
  },
}
</script>