<template>
  <el-dialog :title='dialogProps.title' :visible.sync='dialogProps.visible' :close-on-click-modal='false' width='50%' 
    @open='onDialogOpen()' v-loading='loading'>
    <div slot='title' class='dialog-header'>
      {{ dialogProps.title }}
      <OperationIcon v-show='dialogProps.action === "view" && permission.edit' type='primary' text='Sửa' placement='top-start' icon-name='el-icon-edit' @click='switchEdit'></OperationIcon>
    </div>

    <el-form :model='bizFormModel' :rules='formRules' 
      ref='companyForm' label-width='120px' label-position='right' class='edit-form'>  
      <div class="tab-item" v-show='tabIndex==="1"'>  
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Đơn vị thuê' prop='parent.id' >
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.parent.name'></el-input>
            <el-cascader v-else v-model='bizFormModel.parent.id' :options='parent_List' :key='key_parent' ref='parentCascader' @change='onParentChange' :props='{value: "id", label: "name", checkStrictly: true, emitPath: false}' filterable clearable placeholder='Vui lòng chọn đơn vị thuê' />
          </el-form-item>
        </el-col>
                <el-col :span='24/2'>
          <el-form-item label='Tên đầy đủ' prop='fullName' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.fullName' :maxlength='512' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập tên đầy đủ"' ></el-input>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Tên' prop='name' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.name' :maxlength='128' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập tên"' ></el-input>
          </el-form-item>
        </el-col>
                <el-col :span='24/2'>
          <el-form-item label='Mã' prop='code' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.code' :maxlength='64' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập mã"' ></el-input>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Sắp xếp' prop='sort' >
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.sort'></el-input>
            <number-input v-else v-model="bizFormModel.sort"  :precision="2"></number-input>
          </el-form-item>
        </el-col>
                <el-col :span='24/2'>
          <el-form-item label='Địa chỉ (Tỉnh/Thành phố)' prop='addressProvince' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.addressProvince' :maxlength='10' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập địa chỉ (Tỉnh/Thành phố)"' ></el-input>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Địa chỉ (Thành phố)' prop='addressCity' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.addressCity' :maxlength='10' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập địa chỉ (Thành phố)"' ></el-input>
          </el-form-item>
        </el-col>
                <el-col :span='24/2'>
          <el-form-item label='Địa chỉ (Quận/Huyện)' prop='addressRegion' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.addressRegion' :maxlength='10' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập địa chỉ (Quận/Huyện)"' ></el-input>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Địa chỉ' prop='address' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.address' :maxlength='100' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập địa chỉ"' ></el-input>
          </el-form-item>
        </el-col>
                <el-col :span='24/2'>
          <el-form-item label='Điện thoại' prop='phoneNumber' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.phoneNumber' :maxlength='21' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập số điện thoại"' ></el-input>
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
          <el-form-item label='Môn được cấp phép hành nghề' prop='licenseSubject' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.licenseSubject' :maxlength='100' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập môn được cấp phép hành nghề"' ></el-input>
          </el-form-item>
        </el-col>
                        </el-row>
      <el-row>
         <!-- Tải lên nhiều tệp: Ảnh giấy phép phòng khám -->
        <upload-file v-if='dialogProps.visible' v-model='bizFormModel.fileIdFile' titile='Ảnh giấy phép phòng khám' :action='dialogProps.action' :objectId='bizFormModel.fileId'></upload-file>
      </el-row>
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Phiên bản phòng khám' prop='version.id' >
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.version.name'></el-input>
            <el-select v-else v-model='bizFormModel.version' value-key='id' filterable clearable placeholder='Vui lòng chọn phiên bản phòng khám' 
              @clear='bizFormModel.version={
                "id": null,
                                "name": null,
                }'>
             <el-option v-for='version in version_List' :key='version.id' :label='version.name' :value='version'></el-option>
            </el-select>
          </el-form-item>
        </el-col>
                <el-col :span='24/2'>
          <el-form-item label='Trạng thái' prop='status' >
            <el-switch :disabled='dialogProps.action === "view"' v-model='bizFormModel.status' active-color='#13ce66' inactive-color='#dbdfe6' :active-value='1' :inactive-value='0'></el-switch>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Thời gian kích hoạt phòng khám' prop='startUseDate' >
            <el-date-picker :disabled='dialogProps.action === "view"' v-model='bizFormModel.startUseDate' type='datetime' value-format='yyyy-MM-dd HH:mm:ss' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập thời gian đưa phòng khám vào hoạt động"' ></el-date-picker>             
          </el-form-item>
        </el-col>
                <el-col :span='24/2'>
          <el-form-item label='Thời gian hết hạn phòng khám' prop='expireDate' >
            <el-date-picker :disabled='dialogProps.action === "view"' v-model='bizFormModel.expireDate' type='datetime' value-format='yyyy-MM-dd HH:mm:ss' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập thời gian hết hạn phòng khám"' ></el-date-picker>             
          </el-form-item>
        </el-col>
      </el-row>
      </div>
    </el-form>
    <span slot='footer' class='dialog-footer'>
      <el-button v-if='dialogProps.action !== "view"' :disabled="flag" type='primary' :plain='true' @click='onSubmit("companyForm")'>Lưu</el-button>
      <el-button v-if='dialogProps.action !== "view"' :plain='true' @click='onDialogClose()'>Hủy</el-button>
      <el-button v-if='dialogProps.action === "view"' :plain='true' @click='onDialogClose()'>Đóng</el-button>
    </span>    
  </el-dialog>
</template>
<script>
import { validatenull } from '@/utils/validate'
import { treeCompany } from '@/api/org/company'
import { listClinicVersionAll } from '@/api/clinic/clinicVersion'
import uploadFile from '@/views/components/uploadFile'
import { saveCompany } from '@/api/org/company'
import BaseUI from '@/views/components/baseUI'
import OperationIcon from '@/components/OperationIcon'
import { getCurrentCompanyId } from "@/utils/userCache";
export default {
  extends: BaseUI,
  name: 'company-form',
  components: {
    uploadFile,
    OperationIcon
  },  
  data() {
    return {
      bizFormModel: this.initFormModel(),
      tabIndex: '1',
          key_parent: 1,    // el-cascader key
          parent_List: [],  // Đơn vị thuê
          version_List: [],  // Phiên bản phòng khám
           flag:false,
       dialogProps: {
        visible: false,
        action: '',
        title: '',
       
      },
      formRules: {
        'fullName': [
            { required: true, message: 'Vui lòng nhập tên đầy đủ', trigger: 'blur' }
        ],
        'name': [
            { required: true, message: 'Vui lòng nhập tên', trigger: 'blur' }
        ],
        'code': [
            { required: true, message: 'Vui lòng nhập mã', trigger: 'blur' }
        ],
        'sort': [
            { required: true, message: 'Vui lòng chọn sắp xếp', trigger: 'blur' }
        ],
        'addressProvince': [
            { required: true, message: 'Vui lòng nhập địa chỉ (Tỉnh/Thành phố)', trigger: 'blur' }
        ],
        'addressCity': [
            { required: true, message: 'Vui lòng nhập địa chỉ (Thành phố)', trigger: 'blur' }
        ],
        'addressRegion': [
            { required: true, message: 'Vui lòng nhập địa chỉ (Quận/Huyện)', trigger: 'blur' }
        ],
        'address': [
            { required: true, message: 'Vui lòng nhập địa chỉ', trigger: 'blur' }
        ],
        'phoneNumber': [
            { required: true, message: 'Vui lòng nhập số điện thoại', trigger: 'blur' }
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
      let formData = this.createFormData(this.bizFormModel)
      saveCompany(formData).then(responseData => {
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
      createFormData(bizFormModel) {
        let formData = new FormData()
        let deletes = []
        for(let idx in bizFormModel.fileIdFile.uploads) {
          formData.append('fileIdUploads', bizFormModel.fileIdFile.uploads[idx].raw)
        }
        deletes = deletes.concat(bizFormModel.fileIdFile.deletes)
        formData.append('entity', JSON.stringify(bizFormModel))
        formData.append('deleteIds', JSON.stringify(deletes))

        return formData
      },
    switchEdit() {
      this.dialogProps.action = 'edit'
      this.dialogProps.title = 'Sửa công ty'
      this.initOptions(this.bizFormModel)
    },
    onDialogClose() {
      this.dialogProps.visible = false  
    },
    onDialogOpen() {
      this.$nextTick(() => {
        this.$refs['companyForm'].clearValidate()
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
        'parent': {     // Nút cấp trên Đơn vị thuê
          'id': validatenull(This) || validatenull(This.id) ? null : This.id,
          'name': validatenull(This) || validatenull(This.name) ? null : This.name,
        },
        'fullName': '',   // Tên đầy đủ
        'name': '',   // Tên
        'code': '',   // Mã
        'sort': This && This.children ? (This.children.length + 1) * 10000 : (this.parent_List ? (this.parent_List.length + 1) * 10000 : 10000),
        'addressProvince': '', // Địa chỉ (Tỉnh/Thành phố)
        'addressCity': '', // Địa chỉ (Thành phố)
        'addressRegion': '', // Địa chỉ (Quận/Huyện)
        'address': '',   // Địa chỉ
        'phoneNumber': '',   // Điện thoại
        'remarks': '',   // Thông tin ghi chú
        'licenseSubject': '',   // Môn được cấp phép hành nghề
        'fileId': '',
        'fileIdFile': {
          deletes: [], // Chờ xóa (Đã tải lên)danh sách tệp
          uploads: []   // Danh sách tệp chờ tải lên
         },
        'version': {     // Phiên bản phòng khám
          'id': null,
          'name': null,
        },
        'status': 1,    // Trạng thái
        'startUseDate': '',   // Thời gian kích hoạt phòng khám
        'expireDate': '',   // Thời gian hết hạn phòng khám

      }
    },
    initOptions(This) {
      this.key_parent++
      let parent_search = {
        params: []
      }
        // Trên bảng tương ứng của trườngfilterĐiều kiện
        parent_search.params.push.apply(parent_search.params, [])
      // Quyền dữ liệu: Công tyorg_company
      this.pushDataPermissions(parent_search.params, this.$route.meta.routerId, '41040096140492800')
      this.parent_List.splice(0, this.parent_List.length)
      treeCompany(parent_search).then(responseData => {
        this.parent_List = responseData.data
      })
      let version_search = {
        params: []
      }
        // Trên bảng tương ứng của trườngfilterĐiều kiện
        version_search.params.push.apply(version_search.params, [{columnName: 'company_id', queryType: '=', value: getCurrentCompanyId()}])
      // Quyền dữ liệu: Phiên bản phòng khámclinic_version
      this.pushDataPermissions(version_search.params, this.$route.meta.routerId, '987744398207139863')
      this.version_List.splice(0, this.version_List.length)
      listClinicVersionAll(version_search).then(responseData => {
        this.version_List = responseData.data
      })
    },
    openViewCompanyDialog(company) {
      this.dialogProps.action = 'view'
      this.dialogProps.title = 'Xem công ty'
      this.bizFormModel = {...this.initFormModel(), ...company}
      this.initOptions(this.bizFormModel)
      this.tabIndex = '1'
      this.dialogProps.visible = true
    },
    openEditCompanyDialog(company) {
      this.dialogProps.action = 'edit'
      this.dialogProps.title = 'Sửa công ty'
      this.bizFormModel = {...this.initFormModel(), ...company}
      this.initOptions(this.bizFormModel)
      this.tabIndex = '1'
      this.dialogProps.visible = true
    },
    openAddCompanyDialog(parent) {
      this.dialogProps.action = 'add'
      this.dialogProps.title = 'Thêm công ty'
      this.bizFormModel = this.initFormModel(parent)
      this.initOptions(this.bizFormModel)
      this.tabIndex = '1'
      this.dialogProps.visible = true
    },
    openCopyCompanyDialog(company) {
      this.dialogProps.action = 'add'
      this.dialogProps.title = 'Thêm công ty'
      this.bizFormModel = {...this.initFormModel(), ...company}
      this.initOptions(this.bizFormModel)
      this.tabIndex = '1'
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