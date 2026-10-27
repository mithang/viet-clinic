<template>
  <el-dialog :title='dialogProps.title' :visible.sync='dialogProps.visible' v-if="dialogProps.visible" :close-on-click-modal='false' width='50%' 
    @open='onDialogOpen()' v-loading='loading'>
    <!-- <div slot='title' class='dialog-header'>
      {{ dialogProps.title }}
      <OperationIcon v-show='dialogProps.action === "view" && permission.edit' type='primary' text='Sửa' placement='top-start' icon-name='el-icon-edit' @click='switchEdit'></OperationIcon>
    </div> -->

    <el-form :model='bizFormModel' :rules='formRules' 
      ref='memberManagementForm' label-width='120px' label-position='right' class='edit-form'>  
      <div class="tab-item" v-show='tabIndex==="1"'>  
              <el-row>
                <el-col :span='24/2'>
          <el-form-item label='Họ tên' prop='patient.id' >
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.patient.name'></el-input>
            <el-select v-else @change="getPatient" v-model='bizFormModel.patient' value-key='id' filterable clearable placeholder='Vui lòng chọn thông tin bệnh nhân' 
              @clear='bizFormModel.patient={
                "id": null,
                                "name": null,
                }'>
             <el-option v-for='patient in patient_List' :key='patient.id' :label='patient.name' :value='patient'></el-option>
            </el-select>
          </el-form-item>
        </el-col>
          <el-col :span='24/2'>
          <el-form-item label='Giới tính' prop='gender' >
            <el-input :disabled='true' v-model='bizFormModel.gender.name' :maxlength='100' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập giới tính"' >
            
            </el-input>
          </el-form-item>
        </el-col>
      </el-row>
         <el-row>
        <el-col :span='24/2'>
          <el-form-item label='CMND/CCCD' prop='identityCard' >
            <el-input :disabled='true' v-model='bizFormModel.identityCard' :maxlength='100' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập số CMND/CCCD"' ></el-input>
          </el-form-item>
        </el-col>
               <el-col :span='24/2'>
          <el-form-item label='Điện thoại' prop='phone' >
            <el-input :disabled='true' v-model='bizFormModel.phone' :maxlength='100' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập số điện thoại"' ></el-input>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
                  <el-col :span='24/2'>
          <el-form-item label='Loại thẻ thành viên' prop='type' >
             <el-select :disabled='dialogProps.action === "view"'  v-model='bizFormModel.type'  filterable clearable placeholder='Vui lòng chọn loại thẻ thành viên' 
             @change="getMember">
             <el-option v-for='item in typeList' :key='item.value' :label='item.name' :value='item'></el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span='24/2'>
          <el-form-item label='Tên thẻ thành viên' prop='memberSet' >
         
            <el-select :disabled='dialogProps.action === "view"' v-model='bizFormModel.memberSet'  filterable clearable placeholder='Vui lòng chọn thẻ thành viên' 
             @change="getCostItem">
             <el-option  v-for='item in memberSet_List' :key='item.id' :label='item.name' :value='item'></el-option>
            </el-select>
          </el-form-item>
        </el-col>      
      </el-row> 
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Số thẻ' prop='card' >
            <el-input :disabled='true' v-model='bizFormModel.card' :maxlength='100' :placeholder='dialogProps.action === "view"? "" : "Số thẻ sẽ tự động tạo sau khi lưu"' ></el-input>
          </el-form-item>
        </el-col> 
      </el-row>
      
      <div v-if='memberItem.length>=1' style="margin-left:70px"> 
            <el-table
              :data="memberItem"
              style="width: 90%">
                <el-table-column
              label="Số thứ tự"
              type="index"
              align="center">
            </el-table-column>     
              <el-table-column
                prop="costItem.itemName"
                label="Tên hạng mục"
                width="width">
              </el-table-column>
              <el-table-column
                prop="costItem.itemType.name"
                label="Loại hạng mục"
                width="width">
              </el-table-column>
              <el-table-column
                width="width"
                prop="costItem.salePrice"
                label="Số tiền hạng mục">
              </el-table-column>
                <el-table-column
                width="width"
                prop="number"
                label="Số lượng">
                <!-- <template slot-scope="scope">
                  <span v-if="dialogProps.action === 'view'">
                    {{scope.row.useNumber}}/{{scope.row.number}}
                  </span>
                </template> -->
              </el-table-column>
              <el-table-column
                width="width"
                prop="costItem.unit.name"
                label="Đơn vị">
              </el-table-column>
            
            </el-table>
      </div>
      <div v-else-if="memberManagementDetail.length>0" style="margin-left:70px">
           <el-table
              :data="memberManagementDetail"
              style="width: 90%">
                <el-table-column
              label="Số thứ tự"
              type="index"
              align="center">
            </el-table-column>     
              <el-table-column
                prop="memberItem.costItem.itemName"
                label="Tên hạng mục"
                width="width">
              </el-table-column>
              <el-table-column
                prop="memberItem.costItem.itemType.name"
                label="Loại hạng mục"
                width="width">
              </el-table-column>
              <el-table-column
                width="width"
                prop="memberItem.costItem.salePrice"
                label="Số tiền hạng mục">
              </el-table-column>
               <el-table-column
                width="width"
                prop="number"
                label="Số lượng">
                <template slot-scope="scope">         
                    {{scope.row.useNumber}}/{{scope.row.number}}
                </template>
              </el-table-column>
              <el-table-column
                width="width"
                prop="memberItem.costItem.unit.name"
                label="Đơn vị">
              </el-table-column>
             
            </el-table>
      </div>
      </div>
    </el-form>
    <span slot='footer' class='dialog-footer'>
      <el-button v-if='dialogProps.action !== "view"' type='primary' :plain='true' @click='onSubmit("memberManagementForm")'>Lưu</el-button>
      <el-button v-if='dialogProps.action !== "view"' :plain='true' @click='onDialogClose()'>Hủy</el-button>
      <el-button v-if='dialogProps.action === "view"' :plain='true' @click='onDialogClose()'>Đóng</el-button>
    </span>    
  </el-dialog>
</template>
<script>
import { validatenull } from '@/utils/validate'
import { listCompanyAll } from '@/api/org/company'
import { listPatientAll } from '@/api/outpatient/patient'
import { listMemberSetAll } from '@/api/member/memberSet'
import { saveMemberManagement } from '@/api/member/memberManagement'
import BaseUI from '@/views/components/baseUI'
import OperationIcon from '@/components/OperationIcon'
import { getDictItemsByCode, DICT_CODE } from '@/utils/dictCache'
export default {
  extends: BaseUI,
  name: 'memberManagement-form',
  components: {
    OperationIcon
  },  
  data() {
    return {
      bizFormModel: this.initFormModel(),
      tabIndex: '1',
          company_List: [],  // Phòng khámid
          patient_List: [],  // Thông tin bệnh nhân
          memberSet_List: [],  // Thẻ hội viênid
          typeList:[],  //Loại thẻ thành viên
          memberItem:[],  //项目内容
          memberManagementDetail:[],  //会员详情
       dialogProps: {
        visible: false,
        action: '',
        title: ''
      },
      formRules: {
        
        'patient.id': [
            { required: true, message: 'Vui lòng chọn thông tin bệnh nhân', trigger: 'change' }
        ],
        'memberSet': [
            { required: true, message: 'Vui lòng chọn tên thẻ thành viên', trigger: 'change' }
        ],
        'type': [
            { required: true, message: 'Vui lòng nhập loại thẻ thành viên', trigger: 'change' }
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
    //获取项目
    getCostItem(row){
      this.memberItem=row.memberItem
    },

    //Lấy thẻ thành viên tương ứng với loại thẻ thành viên
    getMember(row){
      
      let memberSet_search = {
        params: []
      }
        // Trên bảng tương ứng của trườngfilterĐiều kiện
        memberSet_search.params.push.apply(memberSet_search.params, [{columnName: 'company_id', queryType: '=', value: currentUser.company.id},{columnName: 'type', queryType: '=', value: row.value},{columnName: 'status', queryType: '=', value: 0},{columnName: 'failure', queryType: '=', value: 0},{columnName: 'number', queryType: '>', value: 0}])
      // Quyền dữ liệu: Cài đặt thẻ thành viênmember_set
      this.pushDataPermissions(memberSet_search.params, this.$route.meta.routerId, '1222698883343517159')
      this.memberSet_List.splice(0, this.memberSet_List.length)
      listMemberSetAll(memberSet_search).then(responseData => {
        this.memberSet_List = responseData.data
      })
    },

    //Lấy thông tin bệnh nhân
    getPatient(row){
        this.bizFormModel.gender=row.gender
        this.bizFormModel.phone=row.phone
        this.bizFormModel.identityCard =row.card
    },
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
      
      this.bizFormModel.age=this.bizFormModel.patient.age
      this.bizFormModel.memberName=this.bizFormModel.memberSet.name
      this.bizFormModel.name=this.bizFormModel.patient.name
      //return
      this.setLoad()
      saveMemberManagement(this.bizFormModel).then(responseData => {
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
      this.dialogProps.title = 'Sửa quản lý thẻ hội viên'
      this.initOptions(this.bizFormModel)
    },
    onDialogClose() {
      this.dialogProps.visible = false  
    },
    onDialogOpen() {
      this.$nextTick(() => {
        this.$refs['memberManagementForm'].clearValidate()
      })
    },
    initFormModel(This) {
      return {
        'company': {     // Phòng khámid
          'id': currentUser.company.id,
          'name': null,
        },
        'patient': {     // Thông tin bệnh nhân
          'id': null,
          'name': null,
        },
        'memberSet': {     // Thẻ hội viênid
          'id': null,
          'name': null,
        },
        'name': '',   // Họ tên
        'age': '',   // Tuổi
        'gender': '',   // Giới tính
        'card': '',   // Số thẻ
        'type': '',   // Loại thẻ thành viên
        'memberName': '',   // Tên thẻ thành viên
        'status': '0',   // Trạng thái
        'identityCard': '',   // Số CMND/CCCD

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
      let patient_search = {
        params: []
      }
        // Trên bảng tương ứng của trườngfilterĐiều kiện
        patient_search.params.push.apply(patient_search.params, [{columnName: 'company_id', queryType: '=', value: currentUser.company.id}])
      // Quyền dữ liệu: Bảng bệnh nhânpatient
      this.pushDataPermissions(patient_search.params, this.$route.meta.routerId, '1008489176147648530')
      this.patient_List.splice(0, this.patient_List.length)
      listPatientAll(patient_search).then(responseData => {
        this.patient_List = responseData.data
      })
      
      getDictItemsByCode(DICT_CODE.MEMBER_TYPE).then((data) => {
        this.typeList = data;
        this.$forceUpdate()
      })
    },
    openViewMemberManagementDialog(memberManagement) {
      this.dialogProps.action = 'view'
      this.dialogProps.title = 'Xem hội viên'
      this.bizFormModel = {...this.initFormModel(), ...memberManagement}
      this.initOptions(this.bizFormModel)
      this.getMember(this.bizFormModel.type)
      this.memberItem = []
      this.memberManagementDetail = this.bizFormModel.memberManagementDetails
      this.tabIndex = '1'
      this.dialogProps.visible = true
    },
    openEditMemberManagementDialog(memberManagement) {
      this.dialogProps.action = 'edit'
      this.dialogProps.title = 'Sửa hội viên'
      this.bizFormModel = {...this.initFormModel(), ...memberManagement}
      this.initOptions(this.bizFormModel)
      this.getMember(this.bizFormModel.memberSet.type.value)
      this.memberItem = []
      this.memberManagementDetail = this.bizFormModel.memberManagementDetails
      this.tabIndex = '1'
      this.dialogProps.visible = true
    },
    openAddMemberManagementDialog() {
      this.dialogProps.action = 'add'
      this.dialogProps.title = 'Thêm thành viên'
      this.memberManagementDetail = []
      this.memberItem = []
      this.bizFormModel = this.initFormModel()
      this.initOptions(this.bizFormModel)
      this.bizFormModel.memberSet = null
      this.tabIndex = '1'
      this.dialogProps.visible = true
    },
    openCopyMemberManagementDialog(memberManagement) {
      this.dialogProps.action = 'add'
      this.dialogProps.title = 'Thêm thành viên'
      this.bizFormModel = {...this.initFormModel(), ...memberManagement}
      this.initOptions(this.bizFormModel)
      this.tabIndex = '1'
      this.bizFormModel.id = null
      this.dialogProps.visible = true
    },
  },
}
</script>