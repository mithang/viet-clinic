<template>
  <el-dialog :title='dialogProps.title' :visible.sync='dialogProps.visible' :close-on-click-modal='false' width='50%' 
    @open='onDialogOpen()' v-loading='loading'>
    <div slot='title' class='dialog-header'>
      {{ dialogProps.title }}
      <OperationIcon v-show='dialogProps.action === "view" && permission.edit' type='primary' text='Sửa' placement='top-start' icon-name='el-icon-edit' @click='switchEdit'></OperationIcon>
    </div>

    <el-form :model='bizFormModel' :rules='formRules' 
      ref='inspectionCheckForm' label-width='120px' label-position='right' class='edit-form'>  
      <div class="tab-item" v-show='tabIndex==="1"'>  
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Phòng khámid' prop='company.id' >
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.company.name'></el-input>
            <el-select v-else v-model='bizFormModel.company' value-key='id' filterable clearable placeholder='Vui lòng chọn phòng khámid' 
              @clear='bizFormModel.company={
                "id": null,
                                "name": null,
                }'>
             <el-option v-for='company in company_List' :key='company.id' :label='company.name' :value='company'></el-option>
            </el-select>
          </el-form-item>
        </el-col>
                <el-col :span='24/2'>
          <el-form-item label='Bệnh nhânid' prop='patientId' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.patientId' :maxlength='20' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập bệnh nhânid"' ></el-input>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Thông tin đăng kýid' prop='registrationId' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.registrationId' :maxlength='20' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập thông tin đăng kýid"' ></el-input>
          </el-form-item>
        </el-col>
                <el-col :span='24/2'>
          <el-form-item label='Hạng mục khám chữa bệnhid' prop='costItemId' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.costItemId' :maxlength='20' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập hạng mục khám chữa bệnhid"' ></el-input>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Tên hạng mục' prop='name' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.name' :maxlength='100' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập tên hạng mục"' ></el-input>
          </el-form-item>
        </el-col>
                <el-col :span='24/2'>
          <el-form-item label='Loại' prop='type' >
            <el-switch :disabled='dialogProps.action === "view"' v-model='bizFormModel.type' active-color='#13ce66' inactive-color='#dbdfe6' active-value='1' inactive-value='0'></el-switch>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Trạng thái điền' prop='status' >
            <el-switch :disabled='dialogProps.action === "view"' v-model='bizFormModel.status' active-color='#13ce66' inactive-color='#dbdfe6' active-value='1' inactive-value='0'></el-switch>
          </el-form-item>
        </el-col>
                <el-col :span='24/2'>
          <el-form-item label='Tên bệnh nhân' prop='patientName' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.patientName' :maxlength='100' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập tên bệnh nhân"' ></el-input>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Giới tính' prop='sex' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.sex' :maxlength='100' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập giới tính"' ></el-input>
          </el-form-item>
        </el-col>
                <el-col :span='24/2'>
          <el-form-item label='Số điện thoại di động' prop='phone' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.phone' :maxlength='100' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập số điện thoại di động"' ></el-input>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Chẩn đoán' prop='diagnose' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.diagnose' :maxlength='100' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập chẩn đoán"' ></el-input>
          </el-form-item>
        </el-col>
                <el-col :span='24/2'>
          <el-form-item label='Bác sĩ kê đơn' prop='completeBy' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.completeBy' :maxlength='100' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập bác sĩ kê đơn"' ></el-input>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Thời gian lập phiếu' prop='completeDate' >
            <el-date-picker :disabled='dialogProps.action === "view"' v-model='bizFormModel.completeDate' type='datetime' value-format='yyyy-MM-dd HH:mm:ss' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập thời gian lập phiếu"' ></el-date-picker>             
          </el-form-item>
        </el-col>
    </el-row>
      </div>
    </el-form>
    <span slot='footer' class='dialog-footer'>
      <el-button v-if='dialogProps.action !== "view"' type='primary' :plain='true' @click='onSubmit("inspectionCheckForm")'>Lưu</el-button>
      <el-button v-if='dialogProps.action !== "view"' :plain='true' @click='onDialogClose()'>Hủy</el-button>
      <el-button v-if='dialogProps.action === "view"' :plain='true' @click='onDialogClose()'>Đóng</el-button>
    </span>    
  </el-dialog>
</template>
<script>
import { validatenull } from '@/utils/validate'
import { listCompanyAll } from '@/api/org/company'
import { saveInspectionCheck } from '@/api/cure/inspectionCheck'
import BaseUI from '@/views/components/baseUI'
import OperationIcon from '@/components/OperationIcon'
export default {
  extends: BaseUI,
  name: 'inspectionCheck-form',
  components: {
    OperationIcon
  },  
  data() {
    return {
      bizFormModel: this.initFormModel(),
      tabIndex: '1',
          company_List: [],  // Phòng khámid
       dialogProps: {
        visible: false,
        action: '',
        title: ''
      },
      formRules: {
        'company.id': [
            { required: true, message: 'Vui lòng chọn phòng khámid', trigger: 'change' }
        ],
        'patientId': [
            { required: true, message: 'Vui lòng nhập bệnh nhânid', trigger: 'blur' }
        ],
        'registrationId': [
            { required: true, message: 'Vui lòng nhập thông tin đăng kýid', trigger: 'blur' }
        ],
        'costItemId': [
            { required: true, message: 'Vui lòng nhập hạng mục khám chữa bệnhid', trigger: 'blur' }
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
      saveInspectionCheck(this.bizFormModel).then(responseData => {
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
      this.dialogProps.title = 'Sửa xét nghiệm/kiểm tra'
      this.initOptions(this.bizFormModel)
    },
    onDialogClose() {
      this.dialogProps.visible = false  
    },
    onDialogOpen() {
      this.$nextTick(() => {
        this.$refs['inspectionCheckForm'].clearValidate()
      })
    },
    initFormModel(This) {
      return {
        'company': {     // Phòng khámid
          'id': currentUser.company.id,
          'name': null,
        },
        'patientId': '',   // Bệnh nhânid
        'registrationId': '',   // Thông tin đăng kýid
        'costItemId': '',   // Hạng mục khám chữa bệnhid
        'name': '',   // Tên hạng mục
        'type': '',   // Loại（0：Xét nghiệm；1：Kiểm tra）
        'status': '0',   // Trạng thái điền（0：Chưa điền；1：Đã điền）
        'patientName': '',   // Tên bệnh nhân
        'sex': '',   // Giới tính
        'phone': '',   // Số điện thoại di động
        'diagnose': '',   // Chẩn đoán
        'completeBy': '',   // Bác sĩ kê đơn
        'completeDate': '',   // Thời gian lập phiếu

      }
    },
    initOptions(This) {
      let company_search = {
        params: []
      }
        // Trên bảng tương ứng của trườngfilterĐiều kiện
        company_search.params.push.apply(company_search.params, [])
      // Quyền dữ liệu: Phòng khámorg_company
      this.pushDataPermissions(company_search.params, this.$route.meta.routerId, '41040096140492800')
      this.company_List.splice(0, this.company_List.length)
      listCompanyAll(company_search).then(responseData => {
        this.company_List = responseData.data
        // Lấy giá trị ban đầu
        this.company_List.forEach( item => {
          if(item.id === this.bizFormModel.company.id) {
            this.bizFormModel.company = item
            return
          }
        })
      })
    },
      openViewInspectionCheckDialog(inspectionCheck) {
        this.dialogProps.action = 'view'
        this.dialogProps.title = 'Xem xét nghiệm/kiểm tra'
        this.bizFormModel = {...this.initFormModel(), ...inspectionCheck}
        this.initOptions(this.bizFormModel)
        this.tabIndex = '1'
        this.dialogProps.visible = true
      },
    openEditInspectionCheckDialog(inspectionCheck) {
        this.dialogProps.action = 'edit'
        this.dialogProps.title = 'Sửa xét nghiệm/kiểm tra'
        this.bizFormModel = {...this.initFormModel(), ...inspectionCheck}
        this.initOptions(this.bizFormModel)
        this.tabIndex = '1'
        this.dialogProps.visible = true
      },
    openAddInspectionCheckDialog() {
        this.dialogProps.action = 'add'
        this.dialogProps.title = 'Thêm xét nghiệm/kiểm tra'
        this.bizFormModel = this.initFormModel()
        this.initOptions(this.bizFormModel)
        this.tabIndex = '1'
        this.dialogProps.visible = true
      },
    openCopyInspectionCheckDialog(inspectionCheck) {
        this.dialogProps.action = 'add'
        this.dialogProps.title = 'Thêm xét nghiệm/kiểm tra'
        this.bizFormModel = {...this.initFormModel(), ...inspectionCheck}
        this.initOptions(this.bizFormModel)
        this.tabIndex = '1'
        this.bizFormModel.id = null   //ĐặtidĐặt thành trống，Thêm mới một
        this.dialogProps.visible = true
      },
},
  watch: {
  },
    
}
</script>