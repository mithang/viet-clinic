<template>
  <el-dialog :title='dialogProps.title' :visible.sync='dialogProps.visible' :close-on-click-modal='false' width='50%' 
    @open='onDialogOpen()' v-loading='loading'>
    <div slot='title' class='dialog-header'>
      {{ dialogProps.title }}
      <OperationIcon v-show='dialogProps.action == "view" && permission.edit' type='primary' text='Sửa' placement='top-start' icon-name='el-icon-edit' @click='switchEdit'></OperationIcon>
    </div>

    <el-form :model='bizFormModel' :rules='formRules' 
      ref='memberSetForm' label-width='120px' label-position='right' class='edit-form'>  
      <div class="tab-item" v-show='tabIndex==="1"'>  
              <el-row>
                <el-col :span='24/2'>
          <el-form-item label='Tên thẻ thành viên' prop='name' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.name' :maxlength='255' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập tên thẻ thành viên"' ></el-input>
          </el-form-item>
        </el-col>
         <el-col :span='24/2'>
          <el-form-item label='Số thẻ hội viên' prop='amount' >
            <el-input :disabled='dialogProps.action === "view"' oninput="value=value.replace(/[^\d.]/g,'')"  v-model='bizFormModel.amount'></el-input>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Loại thẻ thành viên' prop='type' >
            <!-- <el-input :disabled='dialogProps.action == "view"' v-model='bizFormModel.type' :maxlength='100' :placeholder='dialogProps.action == "view"? "" : "Vui lòng nhập loại thẻ thành viên"' ></el-input> -->
                            <el-select
                              v-model='bizFormModel.type'
                             :disabled='dialogProps.action === "view"'
                             :placeholder='dialogProps.action === "view"? "" : "Vui lòng chọn loại thẻ thành viên"'
                            >
                              <el-option
                                v-for="item in memberType"
                                :key="item.value"
                                :label="item.name"
                                :value="item"
                              >
                              </el-option>
                            </el-select>
          </el-form-item>
        </el-col>
         <el-col :span='24/2'>
           
          <el-form-item label='Thời gian hiệu lực' prop='startDate' >
            <!-- <el-date-picker :disabled='dialogProps.action == "view"' v-model='bizFormModel.startDate' type='date' value-format='yyyy-MM-dd HH:mm:ss' :placeholder='dialogProps.action == "view"? "" : "Vui lòng nhập thời gian bắt đầu"' ></el-date-picker>              -->
            <el-date-picker
            v-model="bizFormModel.startDate"
            type="daterange"
            style="width:290px"
            :disabled='dialogProps.action === "view"' 
            value-format='yyyy-MM-dd HH:mm:ss' 
            :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập thời gian hiệu lực"'
            start-placeholder="Ngày bắt đầu"
            end-placeholder="Ngày kết thúc"
            :default-time="['00:00:00', '23:59:59']">
          </el-date-picker>
          </el-form-item>
          
        <!-- </el-col>
                <el-col :span='24/3'>
          <el-form-item label='Thời gian kết thúc' prop='endDate' >
            <el-date-picker :disabled='dialogProps.action === "view"' v-model='bizFormModel.endDate' type='date' value-format='yyyy-MM-dd HH:mm:ss' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập thời gian kết thúc"' ></el-date-picker>             
          </el-form-item>  -->
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Có kích hoạt không' prop='status' >
            <el-switch :disabled='dialogProps.action === "view"' v-model='bizFormModel.status' active-color='#13ce66' inactive-color='#dbdfe6' active-value='0' inactive-value='1'></el-switch>
          </el-form-item>
        </el-col>
             
      </el-row>
          <!-- Thêm hạng mục -->
        <div>
            <el-row>
        <el-col :span='24'>
          <div>
            <span style="margin-left:55px;"><b>Hạng mục trải nghiệm</b></span>
             <el-button v-if="dialogProps.action !== 'view'" style="float:right;margin-top:-10px" type="primary" @click="addItem">Thêm hạng mục trải nghiệm</el-button>
          </div>
           </el-col>
      </el-row>
           <el-row>
             <el-col :span="24">
                 <el-table
              :data="memberData"
              style="width: 90%;margin-left:65px">
                <el-table-column
              label="Số thứ tự"
              type="index"
              align="center">
            </el-table-column>     
              <el-table-column
                prop="name"
                label="Tên hạng mục"
                width="180">
                 <template slot-scope="scope">
                  <el-select
                    v-model='memberData[scope.$index].name'
                  :disabled='dialogProps.action === "view"'
                  :placeholder='dialogProps.action === "view"? "" : "Vui lòng chọn hạng mục"'
                  @change="itemChange(scope.$index)"
                  >
                    <el-option
                      v-for="item in TreatmentTable"
                      :key="item.id"
                      :label="item.itemName"
                      :value="item.id"
                    >
                    </el-option>
                  </el-select>
              </template>
              </el-table-column>
             
              <el-table-column
                prop="itemType"
                label="Loại hạng mục"
                width="100">
              </el-table-column>
              <el-table-column
               align="center"
                prop="salePrice"
                label="Số tiền hạng mục">
                <template slot-scope="scope">
                  <span v-if='scope.row.salePrice>0'>
                    {{scope.row.salePrice}}VNĐ
                  </span>
                </template>
              </el-table-column>
               <el-table-column
                prop="number"
                label="Số lượng hạng mục"
                align="center">
                 <template slot-scope="scope">
                  <el-input-number :disabled="dialogProps.action === 'view'" size="mini" v-model="memberData[scope.$index].number" @change="handleChange"    ></el-input-number>
                  </template>
              </el-table-column>
              <el-table-column
                prop="unit"
                label="Đơn vị">
              </el-table-column>
             
                <el-table-column
                v-if="dialogProps.action !== 'view'"
                  label="Thao tác"
                  width="100">
                  <template slot-scope="scope">
                    <el-button v-if="dialogProps.action !== 'view'" @click="removeClick(scope.row)" type="text" size="small">Xóa</el-button>
                  </template>
                </el-table-column>
            </el-table>
             </el-col>
           </el-row>
        </div>
      </div>
    </el-form>
    <span slot='footer' class='dialog-footer'>
      <el-button v-if='dialogProps.action !== "view"' type='primary' :plain='true' @click='onSubmit("memberSetForm")'>Lưu</el-button>
      <el-button v-if='dialogProps.action !== "view"' :plain='true' @click='onDialogClose()'>Hủy</el-button>
      <el-button v-if='dialogProps.action === "view"' :plain='true' @click='onDialogClose()'>Đóng</el-button>
    </span>    
  </el-dialog>
</template>
<script>
import { validatenull } from '@/utils/validate'
import { getDictItemsByCode, DICT_CODE } from "@/utils/dictCache";
import { listCompanyAll } from '@/api/org/company'
import { saveMemberSet } from '@/api/member/memberSet'
import BaseUI from '@/views/components/baseUI'
import { listCostItemAll } from "@/api/treatment/costItem";
import OperationIcon from '@/components/OperationIcon'
import { BigNumber } from "bignumber.js";
export default {
  extends: BaseUI,
  name: 'memberSet-form',
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
      TreatmentTable:[],   //Tập hợp hạng mục điều trị
      formRules: {
        'name': [
            { required: true, message: 'Vui lòng nhập tên thẻ thành viên', trigger: 'blur' }
        ],
        'type': [
            { required: true, message: 'Vui lòng nhập loại thẻ thành viên', trigger: 'blur' }
        ],
        amount:[
          {required: true, message: 'Vui lòng nhập số lượng thẻ', trigger: 'blur'}
        ]
      }
    }    
  },
  memberType:[],   //Loại thẻ thành viên
  memberData:[
    {
      name:'',//Tên hạng mục
      itemType:'',  //Loại hạng mục
      salePrice:10,   //Số tiền hạng mục
      unit:"" ,  //Đơn vị
      number:0  //Số lượng tặng
      
    }
  ],
  props: {
    // Quyền
    permission: {
      type: Object
    }
  },  
  methods: {

    //Thực hiện xoá hạng mục
    removeClick(row,index){
      this.memberData.splice(index,1)
    },
    //Thêm hạng mục
    addItem(){
      let objectItem={
        name:'',//Tên hạng mục
        itemType:'',  //Loại hạng mục
        salePrice:0,   //Số tiền hạng mục
        unit:"" ,  //Đơn vị
        number:""  //Số lượng tặng
      }
      this.memberData.push(objectItem)
    },

    //Chọn hạng mục điều trị
    itemChange(index){
      this.TreatmentTable.forEach(item=>{
        if(item.id===this.memberData[index].name){
          this.memberData[index].salePrice=item.salePrice
          this.memberData[index].itemType=item.itemType.name
          this.memberData[index].unit=item.unit.name
          this.memberData[index].number=1
        }
      })
    },
    //Lấy hạng mục khám chữa bệnh
    getCostItem(){
      let SearchModel = {
        columnName: "",
        limit: 10,
        offset: 0,
        order: "",
        params: [
          {
            columnName: "company_id",
            queryType: "=",
            value: currentUser.company.id,
          },
          {
            columnName: "item_name",
            queryType: "like",
            value: "",
          },
          {
            columnName: "item_type",
            queryType: "<>",
            value: "treatmentItemType_4",
          },
        ],
      };
      listCostItemAll(SearchModel).then((responseData) => {
        if (responseData.code === 100) {
          this.TreatmentTable = responseData.data;
        }
      });
    },
    //Lấy loại thẻ thành viên
    getType(){
      getDictItemsByCode(DICT_CODE.MEMBER_TYPE).then((data) => {
        this.memberType = data;
        this.$forceUpdate()
      })
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
     
      let dateTime=this.bizFormModel.startDate
      this.bizFormModel.startDate=dateTime[0]
      this.bizFormModel.endDate=dateTime[1]
      this.bizFormModel.amount=this.bizFormModel.amount-0
      this.bizFormModel.number=this.bizFormModel.amount
      let amount=0
      this.memberData.forEach(item=>{
          amount+=  Math.ceil(BigNumber(item.number).multipliedBy(item.salePrice).toNumber())
          item.company=this.bizFormModel.company
      })
      this.bizFormModel.money=amount
   
      
      this.bizFormModel.memberItem=this.memberData
      this.setLoad()
      saveMemberSet(this.bizFormModel).then(responseData => {
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
      this.dialogProps.title = 'Sửa thẻ thành viên'
      this.initOptions(this.bizFormModel)
       this.memberData=this.bizFormModel.memberItem
        this.memberData.forEach(item=>{
          item.itemType=item.costItem.itemType.name
          item.unit=item.costItem.unit.name
          item.salePrice=item.costItem.salePrice
        })
    },
    onDialogClose() {
      this.dialogProps.visible = false  
    },
    onDialogOpen() {
      this.$nextTick(() => {
        this.$refs['memberSetForm'].clearValidate()
      })
    },
    initFormModel(This) {
      return {
        'company': {     // Phòng khámid
          'id': currentUser.company.id,
          'name': null,
        },
        'name': '',   // Tên thẻ thành viên
        'type': {name:'Thẻ trải nghiệm',
                value:'memberType_0'},   // Loại thẻ thành viên
        'money': 0,    // Số tiền thẻ hội viên
        'amount': 0,    // Tổng số thẻ hội viên
        'number': 0,    // Số lượng còn lại
        'status': '0', // Có kích hoạt không (0: Kích hoạt, 1:Vô hiệu hóa)
        'failure': '0', // Có hết hiệu lực không (0: Không, 1: Có)
        'startDate': '',   // Thời gian bắt đầu
        'endDate': '',   // Thời gian kết thúc

      }
    },
    initOptions(This) {
      this.getType()
      this.getCostItem()
      this.memberData=[
    {
      name:'',//Tên hạng mục
      itemType:'',  //Loại hạng mục
      salePrice:0,   //Số tiền hạng mục
      unit:"" ,  //Đơn vị
      number:""  //Số lượng tặng
      
    }]
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

      //Lấy loại thẻ thành viên
    },

    openViewMemberSetDialog(memberSet) {
      this.dialogProps.action = 'view'
      this.dialogProps.title = 'Xem thẻ hội viên'
      this.bizFormModel = {...this.initFormModel(), ...memberSet}
      this.initOptions(this.bizFormModel)
      this.bizFormModel.startDate = [this.bizFormModel.startDate, this.bizFormModel.endDate]
      this.memberData = this.bizFormModel.memberItem
      this.memberData.forEach(item => {
        item.itemType = item.costItem.itemType.name
        item.unit = item.costItem.unit.name
        item.salePrice = item.costItem.salePrice
      })
      this.tabIndex = '1'
      this.dialogProps.visible = true
    },
    openEditMemberSetDialog(memberSet) {
      this.dialogProps.action = 'edit'
      this.dialogProps.title = 'Sửa thẻ thành viên'
      this.bizFormModel = {...this.initFormModel(), ...memberSet}
      this.initOptions(this.bizFormModel)
      this.bizFormModel.startDate = [this.bizFormModel.startDate, this.bizFormModel.endDate]
      this.memberData = this.bizFormModel.memberItem
      this.memberData.forEach(item => {
        item.itemType = item.costItem.itemType.name
        item.unit = item.costItem.unit.name
        item.salePrice = item.costItem.salePrice
      })
      this.tabIndex = '1'
      this.dialogProps.visible = true
    },
    openAddMemberSetDialog() {
      this.dialogProps.action = 'add'
      this.dialogProps.title = 'Thêm thẻ thành viên'
      this.bizFormModel = this.initFormModel()
      this.initOptions(this.bizFormModel)
      this.tabIndex = '1'
      this.dialogProps.visible = true
    },
    openCopyMemberSetDialog(memberSet) {
      this.dialogProps.action = 'add'
      this.dialogProps.title = 'Thêm thẻ thành viên'
      this.bizFormModel = {...this.initFormModel(), ...memberSet}
      this.initOptions(this.bizFormModel)
      this.tabIndex = '1'
      this.bizFormModel.id = null
      this.dialogProps.visible = true
    },
  },
}
</script>