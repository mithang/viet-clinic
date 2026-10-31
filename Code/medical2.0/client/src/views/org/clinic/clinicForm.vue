<template>
  <el-dialog :title='dialogProps.title' :visible.sync='dialogProps.visible' :close-on-click-modal='false' width='30%'
             @open='onDialogOpen()' v-loading='loading'>
    <div slot='title' class='dialog-header'>
      {{ dialogProps.title }}
      <OperationIcon v-show='dialogProps.action === "view" && permission.edit' type='primary' text='Sửa'
                     placement='top-start' icon-name='el-icon-edit' @click='switchEdit'></OperationIcon>
    </div>

    <el-form :model='bizFormModel' :rules='formRules'
             ref='clinicForm' label-width='120px' label-position='right' class='edit-form'>
      <div class="tab-item" v-show='tabIndex==="1"'>
        <el-row>
          <el-col :span='24/1'>
            <el-form-item label='Tên phòng khám' prop='name'>
              <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.name' :maxlength='100'
                        :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập tên phòng khám"' autofocus></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span='24/1'>
            <el-form-item label='Đơn vị thuê' prop='lesseeId.id'>
              <el-input v-if='dialogProps.action === "view"' :disabled='true'
                        v-model='bizFormModel.lesseeId.userName'></el-input>
              <el-select v-else v-model='bizFormModel.lesseeId' value-key='id' filterable clearable placeholder='Vui lòng chọn đơn vị thuê'
                         @clear='bizFormModel.lesseeId={
                "id": null,
                                "name": null,
                }'>
                <el-option v-for='lesseeId in lesseeId_List' :key='lesseeId.id' :label='lesseeId.name'
                           :value='lesseeId'></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span='24/1'>
            <el-form-item label='Địa chỉ(Tỉnh/Thành phố)' prop='addressProvince'>
              <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.addressProvince' :maxlength='10'
                        :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập địa chỉ(Tỉnh/Thành phố)"'></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span='24/1'>
            <el-form-item label='Địa chỉ(Thành phố)' prop='addressCity'>
              <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.addressCity' :maxlength='10'
                        :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập địa chỉ(Thành phố)"'></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span='24/1'>
            <el-form-item label='Địa chỉ(Quận/Huyện)' prop='addressRegion'>
              <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.addressRegion' :maxlength='10'
                        :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập địa chỉ(Quận/Huyện)"'></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span='24/1'>
            <el-form-item label='Địa chỉ' prop='address'>
              <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.address' :maxlength='100'
                        :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập địa chỉ"'></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span='24/1'>
            <el-form-item label='Số điện thoại di động' prop='phoneNumber'>
              <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.phoneNumber' :maxlength='20'
                        :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập số điện thoại di động"'></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span='24/1'>
            <el-form-item label='Môn được cấp phép hành nghề' prop='licenseSubject'>
              <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.licenseSubject' :maxlength='100'
                        :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập môn được cấp phép hành nghề"'></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          Thông số cấu hình bảo hiểm y tế
        </el-row>
        <el-row>
          <el-col :span='24/1'>
            <el-form-item label='Mã cơ sở y tế' prop='fixmedinsCode'>
              <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.fixmedinsCode' :maxlength='100'
                        autofocus></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <!-- Tên cơ sở y tế được chỉ định -->
          <el-col :span='24/1'>
            <el-form-item label='Tên cơ sở y tế' prop='fixmedinsName'>
              <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.fixmedinsName' :maxlength='100'></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <!-- Mã khu vực khám bệnh -->
          <el-col :span='24/1'>
            <el-form-item label='Mã khu vực khám bệnh' prop='mdtrtareaAdmvs'>
              <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.mdtrtareaAdmvs' :maxlength='100'></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <!-- appId -->
          <el-col :span='24/1'>
            <el-form-item label='Khóa bí mậtId' prop='appId'>
              <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.appId' :maxlength='100'></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <!-- appSecret -->
          <el-col :span='24/1'>
            <el-form-item label='Khóa bí mậtKey' prop='appSecret'>
              <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.appSecret' :maxlength='100'></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <!-- privateKey -->
          <el-col :span='24/1'>
            <el-form-item label='Khóa riêng của cơ sở' prop='privateKey'>
              <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.privateKey' :maxlength='100'></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <!-- publicKey -->
          <el-col :span='24/1'>
            <el-form-item label='Khóa công khai của trung tâm bảo hiểm y tế' prop='publicKey'>
              <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.publicKey' :maxlength='100'></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <!-- Tải lên nhiều tệp: Ảnh giấy phép phòng khám -->
          <upload-file v-if='dialogProps.visible' v-model='bizFormModel.fileIdFile' titile='Ảnh giấy phép phòng khám'
                       :action='dialogProps.action' :objectId='bizFormModel.fileId'></upload-file>
        </el-row>
        <el-row>
          <el-col :span='24/1'>
            <el-form-item label='Phiên bản sản phẩm' prop='version.id'>
              <el-input v-if='dialogProps.action === "view"' :disabled='true'
                        v-model='bizFormModel.version.name'></el-input>
              <el-select v-else v-model='bizFormModel.version' value-key='id' filterable clearable placeholder='Vui lòng chọn phiên bản sản phẩm'
                         @clear='bizFormModel.version={
                "id": null,
                                "name": null,
                                                "roleId":null
                }'>
                <el-option v-for='version in version_List' :key='version.id' :label='version.name'
                           :value='version'></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span='24/1'>
            <el-form-item label='Trạng thái' prop='status'>
              <el-switch :disabled='dialogProps.action === "view"' v-model='bizFormModel.status' active-color='#13ce66'
                         inactive-color='#dbdfe6' :active-value='1' :inactive-value='0'></el-switch>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span='24/1'>
            <el-form-item label='Có phải cơ sở không' prop='isInstitution'>
              <el-switch :disabled='dialogProps.action === "view"' v-model='bizFormModel.isInstitution' active-color='#13ce66'
                         inactive-color='#dbdfe6' :active-value='1' :inactive-value='0'></el-switch>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span='24/1'>
            <el-form-item label='Thời gian bắt đầu phòng khám' prop='startUseDate'>
              <el-date-picker :disabled='dialogProps.action === "view"' v-model='bizFormModel.startUseDate'
                              type='datetime' value-format='yyyy-MM-dd HH:mm:ss'
                              :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập thời gian bắt đầu của phòng khám"'></el-date-picker>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span='24/1'>
            <el-form-item label='Thời gian hết hạn phòng khám' prop='expireDate'>
              <el-date-picker :disabled='dialogProps.action === "view"' v-model='bizFormModel.expireDate' type='datetime'
                              value-format='yyyy-MM-dd HH:mm:ss'
                              :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập thời gian hết hạn phòng khám"'></el-date-picker>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col>
            <el-form-item label='Thông tin ghi chú' prop='remarks'>
              <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.remarks' type='textarea'
                        :maxlength='255' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập thông tin ghi chú"'
                        clearable></el-input>
            </el-form-item>
          </el-col>
        </el-row>
      </div>
    </el-form>
    <span slot='footer' class='dialog-footer'>
      <el-button v-if='dialogProps.action !== "view"' :disabled="flag" type='primary' :plain='true'
                 @click='onSubmit("clinicForm")'>Lưu</el-button>
      <el-button v-if='dialogProps.action !== "view"' :plain='true' @click='onDialogClose()'>Hủy</el-button>
      <el-button v-if='dialogProps.action === "view"' :plain='true' @click='onDialogClose()'>Đóng</el-button>
    </span>
  </el-dialog>
</template>
<script>
  import {validatenull} from '@/utils/validate'
  import {listTenantAll} from '@/api/tenant/tenant'
  import {listClinicVersionAll} from '@/api/clinic/clinicVersion'
  import uploadFile from '@/views/components/uploadFile'
  import {saveClinic} from '@/api/org/clinic'
  import BaseUI from '@/views/components/baseUI'
  import OperationIcon from '@/components/OperationIcon'

  export default {
    extends: BaseUI,
    name: 'clinic-form',
    components: {
      uploadFile,
      OperationIcon
    },
    data() {
      return {
        bizFormModel: this.initFormModel(),
        tabIndex: '1',
        lesseeId_List: [],  // Đơn vị thuê
        version_List: [],  // Phiên bản sản phẩm
        flag: false,
        dialogProps: {
          visible: false,
          action: '',
          title: '',

        },
        formRules: {
          'name': [
            {required: true, message: 'Vui lòng nhập tên phòng khám', trigger: 'blur'}
          ],
          'fixmedinsCode': [
            {required: true, message: 'Vui lòng nhập mã cơ sở y tế', trigger: 'blur'}
          ],
          'lesseeId.id': [
            {required: true, message: 'Vui lòng chọn đơn vị thuê', trigger: 'change'}
          ],
          'phoneNumber': [
            {required: true, message: 'Vui lòng nhập số điện thoại di động', trigger: 'blur'}
          ],
          'version.id': [
            {required: true, message: 'Vui lòng chọn phiên bản sản phẩm', trigger: 'change'}
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
        this.flag = true
        this.$refs[formName].validate(valid => {
          if (valid) {
            this.doSave()
          } else {
            this.flag = false
            return false
          }
        });
      },
      doSave() {
        this.setLoad()
        let formData = this.createFormData(this.bizFormModel)
        saveClinic(formData).then(responseData => {
          this.flag = false
          if (responseData.code === 100) {
            this.dialogProps.visible = false
            this.$emit('save-finished')
          } else {
            this.showMessage(responseData)
          }
          this.resetLoad()
        }).catch(error => {
          this.flag = false
          this.outputError(error)
        })
      },
      createFormData(bizFormModel) {
        let formData = new FormData()
        let deletes = []
        for (let idx in bizFormModel.fileIdFile.uploads) {
          formData.append('fileIdUploads', bizFormModel.fileIdFile.uploads[idx].raw)
        }
        deletes = deletes.concat(bizFormModel.fileIdFile.deletes)
        formData.append('entity', JSON.stringify(bizFormModel))
        formData.append('deleteIds', JSON.stringify(deletes))

        return formData
      },
      switchEdit() {
        this.dialogProps.action = 'edit'
        this.dialogProps.title = 'Sửa phòng khám'
        this.initOptions(this.bizFormModel)
      },
      onDialogClose() {
        this.dialogProps.visible = false
      },
      onDialogOpen() {
        this.$nextTick(() => {
          this.$refs['clinicForm'].clearValidate()
        })
      },
      initFormModel(This) {
        return {
          'name': '',   // Tên phòng khám
          'lesseeId': {     // Bảng cha Đơn vị thuê
            'id': validatenull(This) || validatenull(This.lesseeId) ? null : This.lesseeId.id,
            'name': validatenull(This) || validatenull(This.lesseeId) ? null : This.lesseeId.name,
          },
          'addressProvince': '',   // Địa chỉ(Tỉnh/Thành phố)
          'addressCity': '',   // Địa chỉ(Thành phố)
          'addressRegion': '',   // Địa chỉ(Quận/Huyện)
          'address': '',   // Địa chỉ
          'phoneNumber': '',   // Số điện thoại di động
          'licenseSubject': '',   // Môn được cấp phép hành nghề
          'parentId': '', //zuhu
          'fileId': '',
          'fileIdFile': {
            deletes: [], // Chờ xóa (Đã tải lên)danh sách tệp
            uploads: []   // Danh sách tệp chờ tải lên
          },
          'version': {     // Phiên bản sản phẩm
            'id': null,
            'name': null,
            'roleId': null
          },
          'status': 1,    // Trạng thái
          'isInstitution': 0,    // Có phải cơ sở không
          'startUseDate': '',   // Thời gian bắt đầu phòng khám
          'expireDate': '',   // Thời gian hết hạn phòng khám
          'remarks': '',   // Thông tin ghi chú
          //Thông tin cấu hình bảo hiểm y tế
          'fixmedinsCode':'' ,//Mã cơ sở y tế
          'fixmedinsName': '',   // Tên cơ sở y tế
          'mdtrtareaAdmvs': '',  // Mã khu vực khám bệnh
          'appId': '',           // Đơn thuốc điện tử AppId
          'appSecret': '',       // Đơn thuốc điện tử AppSecret
          'privateKey': '',      // Đơn thuốc điện tử - khoá riêng
          'publicKey': '',       // Đơn thuốc điện tử - khoá công khai
        }
      },
      initOptions(This) {
        let lesseeId_search = {
          params: []
        }
        // Quyền dữ liệu: Đơn vị thuêlessee
        this.pushDataPermissions(lesseeId_search.params, this.$route.meta.routerId, '985180319911059468')
        this.lesseeId_List.splice(0, this.lesseeId_List.length)
        listTenantAll(lesseeId_search).then(responseData => {
          this.lesseeId_List = responseData.data
        })
        let version_search = {
          params: []
        }
        // Quyền dữ liệu: Phiên bản phòng khámclinic_version
        this.pushDataPermissions(version_search.params, this.$route.meta.routerId, '987744398207139863')
        this.version_List.splice(0, this.version_List.length)
        listClinicVersionAll(version_search).then(responseData => {
          this.version_List = responseData.data
        })
      },
      openViewClinicDialog(clinic) {
        this.dialogProps.action = 'view'
        this.dialogProps.title = 'Xem phòng khám'
        this.bizFormModel = {...this.initFormModel(), ...clinic}
        this.initOptions(this.bizFormModel)
        this.tabIndex = '1'
        this.dialogProps.visible = true
      },
      openEditClinicDialog(clinic) {
        this.dialogProps.action = 'edit'
        this.dialogProps.title = 'Sửa phòng khám'
        this.bizFormModel = {...this.initFormModel(), ...clinic}
        this.initOptions(this.bizFormModel)
        this.tabIndex = '1'
        this.dialogProps.visible = true
      },
      openAddClinicDialog() {
        this.dialogProps.action = 'add'
        this.dialogProps.title = 'Thêm phòng khám'
        this.bizFormModel = this.initFormModel()
        this.initOptions(this.bizFormModel)
        this.tabIndex = '1'
        this.dialogProps.visible = true
      },
      openCopyClinicDialog(clinic) {
        this.dialogProps.action = 'add'
        this.dialogProps.title = 'Thêm phòng khám'
        this.bizFormModel = {...this.initFormModel(), ...clinic}
        this.initOptions(this.bizFormModel)
        this.tabIndex = '1'
        this.bizFormModel.id = null
        this.dialogProps.visible = true
      },
    },
    watch: {},
    mounted: function () {
    },
  }
</script>
