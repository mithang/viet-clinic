<template>
  <el-dialog :title='dialogProps.title' :visible.sync='dialogProps.visible' :close-on-click-modal='false' width='50%' 
    @open='onDialogOpen()' v-loading='loading'>
    <div slot='title' class='dialog-header'>
      {{ dialogProps.title }}
      <!-- <OperationIcon v-show='dialogProps.action == "view" && permission.edit' type='primary' text='Sửa' placement='top-start' icon-name='el-icon-edit' @click='switchEdit'></OperationIcon> -->
    </div>

    <el-form :model='bizFormModel' :rules='formRules' 
      ref='memberItemForm' label-width='120px' label-position='right' class='edit-form'>  
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
          <el-form-item label='Thẻ hội viênid' prop='member.id' >
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.member.name'></el-input>
            <el-select v-else v-model='bizFormModel.member' value-key='id' filterable clearable placeholder='Vui lòng chọn thẻ thành viênid' 
              @clear='bizFormModel.member={
                "id": null,
                                "name": null,
                }'>
             <el-option v-for='member in member_List' :key='member.id' :label='member.name' :value='member'></el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Hạng mục tương ứngid' prop='costItem.id' >
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.costItem.name'></el-input>
            <el-select v-else v-model='bizFormModel.costItem' value-key='id' filterable clearable placeholder='Vui lòng chọn hạng mục tương ứngid' 
              @clear='bizFormModel.costItem={
                "id": null,
                                "name": null,
                }'>
             <el-option v-for='costItem in costItem_List' :key='costItem.id' :label='costItem.name' :value='costItem'></el-option>
            </el-select>
          </el-form-item>
        </el-col>
                <el-col :span='24/2'>
          <el-form-item label='Số lượng hạng mục tặng' prop='number' >
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.number'></el-input>
            <number-input v-else v-model="bizFormModel.number"  :precision="0"></number-input>
          </el-form-item>
        </el-col>
      </el-row>
      </div>
    </el-form>
    <span slot='footer' class='dialog-footer'>
      <el-button v-if='dialogProps.action !== "view"' type='primary' :plain='true' @click='onSubmit("memberItemForm")'>Lưu</el-button>
      <el-button v-if='dialogProps.action !== "view"' :plain='true' @click='onDialogClose()'>Hủy</el-button>
      <el-button v-if='dialogProps.action === "view"' :plain='true' @click='onDialogClose()'>Đóng</el-button>
    </span>    
  </el-dialog>
</template>
<script>
import { validatenull } from '@/utils/validate'
import { listCompanyAll } from '@/api/org/company'
import { listMemberSetAll } from '@/api/member/memberSet'
import { listCostItemAll } from '@/api/treatment/costItem'
import { saveMemberItem } from '@/api/member/memberItem'
import BaseUI from '@/views/components/baseUI'
import OperationIcon from '@/components/OperationIcon'
import { getCurrentCompanyId } from "@/utils/userCache";
export default {
  extends: BaseUI,
  name: 'memberItem-form',
  components: {
    OperationIcon
  },  
  data() {
    return {
      bizFormModel: this.initFormModel(),
      tabIndex: '1',
          company_List: [],  // Phòng khámid
          member_List: [],  // Thẻ hội viênid
          costItem_List: [],  // Hạng mục tương ứngid
       dialogProps: {
        visible: false,
        action: '',
        title: ''
      },
      formRules: {
        'company.id': [
            { required: true, message: 'Vui lòng chọn phòng khámid', trigger: 'change' }
        ],
        'member.id': [
            { required: true, message: 'Vui lòng chọn thẻ thành viênid', trigger: 'change' }
        ],
        'costItem.id': [
            { required: true, message: 'Vui lòng chọn hạng mục tương ứngid', trigger: 'change' }
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
      saveMemberItem(this.bizFormModel).then(responseData => {
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
      this.dialogProps.title = 'Sửa chi tiết thẻ thành viên'
      this.initOptions(this.bizFormModel)
    },
    onDialogClose() {
      this.dialogProps.visible = false  
    },
    onDialogOpen() {
      this.$nextTick(() => {
        this.$refs['memberItemForm'].clearValidate()
      })
    },
    initFormModel(This) {
      return {
        'company': {     // Phòng khámid
          'id': currentUser.company.id,
          'name': null,
        },
        'member': {     // Thẻ hội viênid
          'id': null,
          'name': null,
        },
        'costItem': {     // Hạng mục tương ứngid
          'id': null,
          'name': null,
        },
        'number': 0,    // Số lượng hạng mục tặng

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
      let member_search = {
        params: []
      }
        // Trên bảng tương ứng của trườngfilterĐiều kiện
        member_search.params.push.apply(member_search.params, [{columnName: 'company_id', queryType: '=', value: currentUser.company.id}])
      // Quyền dữ liệu: Cài đặt thẻ thành viênmember_set
      this.pushDataPermissions(member_search.params, this.$route.meta.routerId, '1222698883343517159')
      this.member_List.splice(0, this.member_List.length)
      listMemberSetAll(member_search).then(responseData => {
        this.member_List = responseData.data
      })
      let costItem_search = {
        params: []
      }
        // Trên bảng tương ứng của trườngfilterĐiều kiện
        costItem_search.params.push.apply(costItem_search.params, [{columnName: 'company_id', queryType: '=', value: getCurrentCompanyId()}])
      // Quyền dữ liệu: Hạng mục chi phícost_item
      this.pushDataPermissions(costItem_search.params, this.$route.meta.routerId, '998465736089977637')
      this.costItem_List.splice(0, this.costItem_List.length)
      listCostItemAll(costItem_search).then(responseData => {
        this.costItem_List = responseData.data
      })
    },
    openViewMemberItemDialog(memberItem) {
      this.dialogProps.action = 'view'
      this.dialogProps.title = 'Xem chi tiết thẻ hội viên'
      this.bizFormModel = {...this.initFormModel(), ...memberItem}
      this.initOptions(this.bizFormModel)
      this.tabIndex = '1'
      this.dialogProps.visible = true
    },
    openEditMemberItemDialog(memberItem) {
      this.dialogProps.action = 'edit'
      this.dialogProps.title = 'Sửa chi tiết thẻ thành viên'
      this.bizFormModel = {...this.initFormModel(), ...memberItem}
      this.initOptions(this.bizFormModel)
      this.tabIndex = '1'
      this.dialogProps.visible = true
    },
    openAddMemberItemDialog() {
      this.dialogProps.action = 'add'
      this.dialogProps.title = 'Thêm chi tiết thẻ thành viên'
      this.bizFormModel = this.initFormModel()
      this.initOptions(this.bizFormModel)
      this.tabIndex = '1'
      this.dialogProps.visible = true
    },
    openCopyMemberItemDialog(memberItem) {
      this.dialogProps.action = 'add'
      this.dialogProps.title = 'Thêm chi tiết thẻ thành viên'
      this.bizFormModel = {...this.initFormModel(), ...memberItem}
      this.initOptions(this.bizFormModel)
      this.tabIndex = '1'
      this.bizFormModel.id = null
      this.dialogProps.visible = true
    },
  },
}
</script>