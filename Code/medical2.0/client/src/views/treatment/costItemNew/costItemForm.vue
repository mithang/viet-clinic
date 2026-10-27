<template>
  <el-dialog
    :title="dialogProps.title"
    :visible.sync="dialogProps.visible"
    :close-on-click-modal="false"
    width="60%"
    @open="onDialogOpen()"
    v-loading="loading"
    @close="close"
    :append-to-body="true"
  >
    <div slot="title" class="dialog-header">
      {{ dialogProps.title }}
      <OperationIcon
        v-show="dialogProps.action === 'view' && permission.edit"
        type="primary"
        text="Sửa"
        placement="top-start"
        icon-name="el-icon-edit"
        @click="switchEdit"
      ></OperationIcon>
    </div>
    
    <el-form
      :model="bizFormModel"
      :rules="formRules"
      ref="costItemForm"
      style="marginTop: 10px"
      label-width="120px"
      label-position="right"
      class="edit-form"
    >
    <el-row>
    <el-col :span="11">
      <div class="tab-item" v-show="tabIndex === '1'">
        <el-row>
          <el-col :span="24 / 1">
            <el-form-item label="Mã hạng mục" prop="itemCode">
              <el-input
                disabled
                v-model="bizFormModel.itemCode"
                :maxlength="64"
                placeholder="Tự động tạo sau khi lưu"
                autofocus
              ></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24 / 1">
            <el-form-item label="Mã quốc gia" prop="itemCountryCode">
              <el-input
                :disabled="dialogProps.action === 'view' || (bizFormModel.syncId !== '' && dialogProps.action !== 'add')"
                v-model="bizFormModel.itemCountryCode"
                :maxlength="64"
                :placeholder="dialogProps.action === 'view' ? '' : 'Vui lòng nhập mã quốc gia'"
                autofocus
              ></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24 / 1">
            <el-form-item label="Tên hạng mục" prop="itemName">
              <el-input
                :disabled="dialogProps.action === 'view' || (bizFormModel.syncId !== '' && dialogProps.action !== 'add')"
                v-model="bizFormModel.itemName"
                :maxlength="64"
                @input="pinyinInput"
                :placeholder="
                  dialogProps.action === 'view' ? '' : 'Vui lòng nhập tên hạng mục'
                "
              ></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24 / 1">
            <el-form-item label="Mã pinyin" prop="pinyinCode">
              <el-input
                disabled=true
                v-model="bizFormModel.pinyinCode"
                :maxlength="64"
                @input="pinyinInput"
                :placeholder="
                  dialogProps.action === 'view' ? '' : 'Vui lòng nhập mã pinyin'
                "
              ></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24 / 1">
            <el-form-item label="Loại hạng mục" prop="isPackage">
                 <el-radio :disabled="dialogProps.action === 'view' || (bizFormModel.syncId !== '' && dialogProps.action !== 'add')" v-model="bizFormModel.isPackage" label="0">Hạng mục đơn</el-radio>
                 <el-radio :disabled="dialogProps.action === 'view' || (bizFormModel.syncId !== '' && dialogProps.action !== 'add')" v-model="bizFormModel.isPackage" label="1">Hạng mục kết hợp</el-radio>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24 / 1">
            <el-form-item label="Loại hạng mục" prop="itemType.value">
              <el-input
                v-if="dialogProps.action === 'view' || (bizFormModel.syncId !== '' && dialogProps.action !== 'add')"
                :disabled="true"
                v-model="bizFormModel.itemType.name"
              ></el-input>
              <el-select
                v-else
                v-model="bizFormModel.itemType"
                value-key="value"
                filterable
                clearable
                placeholder="Vui lòng chọn loại hạng mục"
                :disabled="bizFormModel.syncId !== '' && dialogProps.action !== 'add'"
                @clear="
                  bizFormModel.itemType = {
                    value: null,
                    name: null,
                  }
                "
              >
                <el-option
                  v-for="itemType in itemType_List"
                  :key="itemType.value"
                  :label="itemType.name"
                  :value="itemType"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24 / 1">
            <el-form-item label="Đơn vị" prop="unit.value">
              <el-input
                v-if="dialogProps.action === 'view' || (bizFormModel.syncId !== '' && dialogProps.action !== 'add')"
                :disabled="true"
                v-model="bizFormModel.unit.name"
              ></el-input>
              <el-select
                v-else
                v-model="bizFormModel.unit"
                value-key="value"
                filterable
                clearable
                placeholder="Vui lòng chọn đơn vị"
                @clear="
                  bizFormModel.unit = {
                    value: null,
                    name: null,
                  }
                "
              >
                <el-option
                  v-for="unit in unit_List"
                  :key="unit.value"
                  :label="unit.name"
                  :value="unit"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24 / 1">
            <el-form-item label="Giá vốn" prop="costPrice">
              <el-input
                v-if="dialogProps.action === 'view'"
                :disabled="true"
                v-model="bizFormModel.costPrice"
              ></el-input>
              <number-input
                v-else
                :disabled="salesFlage"
                v-model="bizFormModel.costPrice"
                currency="CNY"
                :precision="2"
              ></number-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24 / 1">
            <el-form-item label="Giá bán" prop="salePrice">
              <el-input
                v-if="dialogProps.action === 'view'"
                :disabled="true"
                v-model="bizFormModel.salePrice"
              ></el-input>
              <number-input
                v-else
                :disabled="salesFlage"
                v-model="bizFormModel.salePrice"
                currency="CNY"
                :precision="2"
              ></number-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24 / 1">
            <el-form-item label="Có kích hoạt không" prop="isUse">
              <el-switch
                :disabled="dialogProps.action === 'view' || (bizFormModel.syncId !== '' && dialogProps.action !== 'add')"
                v-model="bizFormModel.isUse"
                active-color="#13ce66"
                inactive-color="#dbdfe6"
                active-value="1"
                inactive-value="0"
              ></el-switch>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col>
            <el-form-item label="Thông tin ghi chú" prop="remarks">
              <el-input
                :disabled="dialogProps.action === 'view' || (bizFormModel.syncId !== '' && dialogProps.action !== 'add')"
                v-model="bizFormModel.remarks"
                type="textarea"
                :maxlength="255"
                :placeholder="
                  dialogProps.action === 'view' ? '' : 'Vui lòng nhập thông tin ghi chú'
                "
                clearable
              ></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <!-- <el-row>
          <el-col :span="24 / 1">
            <el-form-item label="Có thực hiện khấu trừ không" prop="isDeduct">
              <el-switch
                :disabled="dialogProps.action === 'view'"
                v-model="bizFormModel.isDeduct"
                active-color="#13ce66"
                inactive-color="#dbdfe6"
                active-value="1"
                inactive-value="0"
              ></el-switch>
            </el-form-item>
          </el-col>
        </el-row> -->
      </div>
    </el-col>
    <el-col :span="2">
    <div class="verticalBar"></div>
    </el-col>
    <div v-if="objectType==='1'">
       <el-col :span="11">
      <el-row>
        <el-col :span="24/1">
            <el-form-item label="Tên hạng mục con khám chữa bệnh" prop="itemNames">
                <el-select
                :disabled="dialogProps.action === 'view' || (bizFormModel.syncId !== '' && dialogProps.action !== 'add')"
                   multiple
                  v-model="subjectName"
                  value-key="value"
                  filterable
                  clearable
                  placeholder="Vui lòng chọn hạng mục con"
                  @change="changeMessage"
                >
                  <el-option
                    v-for="itemNames in itemName_List"
                    :key="itemNames.id"
                    :label="itemNames.itemName"
                    :value="itemNames.itemName"
                  ></el-option>
                </el-select>
              </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col>
             <el-table
                :data="subproject"
                border
                style="width: 100%"
                max-height="250">
                <el-table-column
                  fixed
                  prop="itemCode"
                  label="Mã hạng mục"
                  width="100">
                </el-table-column>
                <el-table-column
                  prop="itemName"
                  label="Tên hạng mục con"
                  width="120">
                </el-table-column>
                <el-table-column
                  prop="quantity"
                  label="Số lượng"
                  width="120">
                  <template slot-scope="scope">
                  <el-input-number :disabled="dialogProps.action === 'view' || (bizFormModel.syncId !== '' && dialogProps.action !== 'add')" size="mini" v-model="subproject[scope.$index].quantity" @change="handleChange" :min="0"   ></el-input-number>
                  </template>
                </el-table-column>
                <el-table-column
                  prop="salePrice"
                  label="Giá bán"
                 
                  width="160">
                   <template slot-scope="scope">
                      {{ fun(scope.row.salePrice) }}
                    </template>
                </el-table-column>
               
                <el-table-column
                  label="Thao tác"
                  width="50">
                  <template slot-scope="scope">
                    <el-button
                      :disabled="dialogProps.action === 'view' || (bizFormModel.syncId !== '' && dialogProps.action !== 'add')"
                      @click.native.prevent="deleteRow(scope.$index, tableData)"
                      type="text"
                      size="small">
                      Xóa
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
        </el-col>
      </el-row>
    </el-col>
    </div>
    <div v-else>
      <el-col :span="11">
      <el-form ref="form" :model="examine" label-width="80px">
        <el-form-item label="Đơn vị tham khảo">
          <el-input  label="Vui lòng nhập đơn vị tham khảo" :disabled="dialogProps.action === 'view' || (bizFormModel.syncId !== '' && dialogProps.action !== 'add')" v-model="examine.referenceUnit"></el-input>
        </el-form-item>
        <el-form-item label="Giá trị tham khảo">
          <el-input label="Vui lòng nhập giá trị tham khảo，Xuống dòng thêm nhiều nhóm" :disabled="dialogProps.action === 'view' || (bizFormModel.syncId !== '' && dialogProps.action !== 'add')" type="textarea" v-model="examine.referenceValue"></el-input>
        </el-form-item>
      </el-form>
      </el-col>
    </div>
    </el-row>
    </el-form>
   
    <span slot='footer' class='dialog-footer'>
      <el-button v-if='dialogProps.action !== "view"' type='primary' :disabled="flag" :plain='true' @click='onSubmit("costItemForm")'>Lưu</el-button>
      <el-button v-if='dialogProps.action !== "view"' :plain='true' @click='onDialogClose()'>Hủy</el-button>
      <el-button v-if='dialogProps.action === "view"' :plain='true' @click='onDialogClose()'>Đóng</el-button>
    </span>    
  </el-dialog>
</template>
<script>

import { validatenull } from "@/utils/validate";
import { listDictItemAll } from "@/api/sys/dictItem";
import { getDictItemsByCode, DICT_CODE } from '@/utils/dictCache'
import { saveCostItem } from "@/api/treatment/costItem";
import BaseUI from "@/views/components/baseUI";
import OperationIcon from "@/components/OperationIcon";

let pinyin = require('js-pinyin');
export default {
  
  extends: BaseUI,
  name: "costItem-form",
  components: {
    OperationIcon,
  },
  data() {
    const salePrice = (rule, val, callback) => {
      if (val <= 0 || val === "") {
        callback("Vui lòng nhập giá bán");
      } else {
        callback("Vui lòng nhập giá bán");
      }
    };
    return {
      user: JSON.parse(sessionStorage.getItem("currentUser")),
      bizFormModel: this.initFormModel(this.user),
      tabIndex: "1",
      subjectName:[],//Đóng gói tên hạng mục con
      itemType_List: [], // Loại hạng mục
      itemName_List:[],//Tên hạng mục con
      unit_List: [], // Đơn vị
      flag:false,
      subproject:[],//子项目表格集合
      examine:{
        referenceUnit:"",
        referenceValue:""
      },
      arr:[],
      salesFlage:false,//Giá bán có thể sửa được không
      sale:0,//Lưu tạm giá bán hạng mục con
      cost:0,//Lưu tạm giá thị trường hạng mục con
      dto:{
        costItem:{},
        costItemPackage:[],
      },//Đối tượng đóng gói
      dialogProps: {
        visible: false,
        action: '',
        title: '',
        
      },
       radio:"0",   //Có phải hạng mục đóng gói không
       objectType:"1",
      formRules: {
        itemName: [
          { required: true, message: "Vui lòng nhập tên hạng mục", trigger: "blur" },
        ],
        salePrice: [
          { required: salePrice, message: "Vui lòng nhập giá bán", trigger: "blur" },
        ],
        "itemType.value":[
          { required: salePrice, message: "Vui lòng chọn loại hạng mục", trigger: "change" }
        ],
        "unit.value":[
          { required: salePrice, message: "Vui lòng chọn đơn vị hạng mục", trigger: "change" }
        ],
        isPackage:[
          { required: true, message: 'Vui lòng chọn loại hạng mục', trigger: 'change' }
        ]
      },
    };
  },
  props: {
    // Quyền
    permission: {
      type: Object,
    },
   
  },
  methods: {
    onSubmit(formName) {
      this.flag=true
      this.$refs[formName].validate(valid => {
        if (valid) {
          if(this.bizFormModel.isPackage==="0"&&this.bizFormModel.itemType.value==="treatmentItemType_0"){
            this.dto.costItem=this.bizFormModel
            this.dto.costItemPackage=[]
            this.dto.costItemPackage.push(this.examine)
          }else{
            this.dto.costItem=this.bizFormModel
            this.dto.costItemPackage=this.subproject
          }
          
          this.doSave();
        } else {
          this.flag=false
          this.salesFlage=false
          return false
        }
      });
     
    },
    doSave() {
      this.setLoad()
     // return
      saveCostItem(this.dto).then(responseData => {
        this.flag=false
        this.salesFlage=false
         if(responseData.code === 100) {
           this.dialogProps.visible = false
           this.$emit('save-finished',"1")
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
      this.dialogProps.action = "edit";
      this.dialogProps.title = "Sửa hạng mục khám chữa bệnh";
      this.initOptions(this.bizFormModel);
    },
    onDialogClose() {
      this.dialogProps.visible = false;
      this.salesFlage=false
    },
    onDialogOpen() {
      this.$nextTick(() => {
        this.$refs["costItemForm"].clearValidate();
      });
    },
    initFormModel(This) {
      return {
        company: {
          // Phòng khámid
          id: This ? This.company.id : null,
          name: null,
        },
        itemCode: "", // Mã hạng mục
        itemName: "", // Tên hạng mục
        itemType: {
          // Loại hạng mục
          value: null,
          name: null,
        },
        unit: {
          // Đơn vị
          value: null,
          name: null,
        },
        costPrice: 0, // Giá vốn
        salePrice: 0, // Giá bán
        isUse: "1", // Trạng thái
        remarks: "", // Thông tin ghi chú
        isDeduct: "0", // Có thực hiện khấu trừ không
      };
    },
    initOptions(This) {
      getDictItemsByCode(DICT_CODE.TREATMENT_ITEM_TYPE).then((data) => {
        this.itemType_List = data;
      });
      getDictItemsByCode(DICT_CODE.TREATMENT_ITEM_UNIT).then((data) => {
        this.unit_List = data;
      });
    },

    //Xóa hạng mục con
    deleteRow(index, rows) {
       //Tính giá bán
        this.bizFormModel.salePrice-=(this.subproject[index].quantity*this.subproject[index].salePrice)
        this.bizFormModel.costPrice-=(this.subproject[index].quantity*this.subproject[index].costPrice)
        this.sale-=(this.subproject[index].quantity*this.subproject[index].salePrice)
        this.cost-=(this.subproject[index].quantity*this.subproject[index].costPrice)

        this.subproject[index].quantity=0;
        this.subproject.splice(index, 1);
        this.subjectName.splice(index,1);
        if(this.subproject.length!==0){
          this.salesFlage=true
        }else{
          this.salesFlage=false
        }
      
       
      },
    assign(row){
      let a='0'
      for (let i = 0; i < row.length; i++) {
        if(row[i].isPackage===a){
          this.itemName_List.push(row[i])
        }
        
      }      
      
    },
    //Hạng mục conselectSự kiện hộp thả xuống
    changeMessage(value){
      
      if(value.length===0){
         this.arr=[]
         for (let i = 0; i < this.subproject.length; i++) {
          this.subproject[i].quantity=0         
         }
        
         this.bizFormModel.salePrice=this.bizFormModel.salePrice-this.sale
         this.bizFormModel.costPrice=this.bizFormModel.costPrice-this.cost
         this.sale=0
         this.cost=0
       }else{
          if(this.arr.length===0){
            this.arr=value
         }else{
            for (let i = 0; i < this.arr.length; i++) {
              for (let j = 0; j < value.length; j++) {
                if(this.arr[i]===value[j]){
                  this.arr.splice(i,1);
                }           
              }       
            }
            if(this.arr.length!==0){
              for (let i = 0; i < this.subproject.length; i++) {
                for (let j = 0; j < this.arr.length; j++) {
                  if(this.subproject[i].itemName===this.arr[j]){
                     //Tính giá bán
                    this.bizFormModel.salePrice-=(this.subproject[i].quantity*this.subproject[i].salePrice)
                    this.sale-=(this.subproject[i].quantity*this.subproject[i].salePrice)
                    this.bizFormModel.costPrice-=(this.subproject[i].quantity*this.subproject[i].costPrice)
                    this.cost-=(this.subproject[i].quantity*this.subproject[i].costPrice)

                    this.subproject[i].quantity=0
                  }
                  
                }           
              }
            }
             this.arr=[]
             this.arr=value
          }
        
       }
     // this.subproject=[]
      //Thêm
      
      if(this.subjectName.length>this.subproject.length){
      let selectChange1=[];         
        for (let i = 0; i < this.subjectName.length; i++) {
          let flags=false;
          for (let j = 0; j < this.subproject.length; j++) {
            if(this.subjectName[i]===this.subproject[j].itemName){
              flags=true;
              break;
            }
          } 
          if(!flags){
            selectChange1.push(this.subjectName[i]);
          }
        }
        for (let x = 0; x < this.itemName_List.length; x++) {
        for (let j = 0; j < selectChange1.length; j++) {
            if(selectChange1[j]===this.itemName_List[x].itemName){
              this.subproject.push(this.itemName_List[x])
            }
        }
      }
      }else{//Giảm
        let selectChange2=[];
        for (let i = 0; i < this.subproject.length; i++) {
          for (let j = 0; j < this.subjectName.length; j++) {
           if(this.subjectName[j]===this.subproject[i].itemName){
            selectChange2.push(this.subproject[i]);
           }
          }     
        }
       this.subproject=[]
       this.subproject=selectChange2
      }
    //  if(this.subjectName.length!=null){
    //     for (let x = 0; x < this.subjectName.length; x++) {
    //     for (let j = 0; j < this.itemName_List.length; j++) {
    //         if(this.subjectName[x]==this.itemName_List[j].itemName){
    //           this.subproject.push(this.itemName_List[j])
    //         }
    //     }
    //   }
    //  }
      if(this.subproject.length!==0){
          this.salesFlage=true
        }else{
          this.salesFlage=false
        }
      
    },
    fun(val){
　　　　return Number(val).toFixed(2);
　　},
    handleChange(currentValue, oldValue){ 
       this.bizFormModel.salePrice=this.bizFormModel.salePrice-this.sale
       this.sale=0
       this.bizFormModel.costPrice=this.bizFormModel.costPrice-this.cost
       this.cost=0
      for (let i = 0; i < this.subproject.length; i++) {              
        if(this.subproject[i].quantity!==undefined){
          this.sale+=(this.subproject[i].quantity*this.subproject[i].salePrice)
          this.cost+=(this.subproject[i].quantity*this.subproject[i].costPrice)
        }
      }
       this.bizFormModel.salePrice=this.bizFormModel.salePrice+this.sale
       this.bizFormModel.costPrice=this.bizFormModel.costPrice+this.cost
     },
     //添加弹出框的回调事件
     close(){
       this.salesFlage=false
     },
     //Mã pinyin của ô nhập
     pinyinInput(value){
       this.bizFormModel.pinyinCode=pinyin.getCamelChars(value)
       
     },
  },
  watch: {
    "bizFormModel.isPackage":function(newVal, oldVal){
          if(newVal==="0"&&this.bizFormModel.itemType.value==="treatmentItemType_0"){
            this.objectType="0"
          }else{
            this.objectType="1"
          }
    },
    "bizFormModel.itemType":function(newVal, oldVal){
       if(this.bizFormModel.isPackage==="0"&&newVal.value==="treatmentItemType_0"){
            this.objectType="0"
          }else{
            this.objectType="1"
          }
    }
  },
  mounted: function () {
    this.$nextTick(() => {
      this.$on("openViewCostItemDialog", function (costItemDTO) {
        this.dialogProps.action = "view";
        this.subjectName=[]
        this.subproject=[]
        this.dialogProps.title = "Xem hạng mục khám chữa bệnh";
        this.bizFormModel = { ...this.initFormModel(), ...costItemDTO.costItem };
        this.initOptions(this.bizFormModel);
       if(costItemDTO.costItemPackage[0]!=null&&costItemDTO.costItems[0]!=null){
          for (let i = 0; i < costItemDTO.costItemPackage.length; i++) {
          this.subproject.push(costItemDTO.costItemPackage[i])     
        }
        for (let i = 0; i < costItemDTO.costItems.length; i++) {
          
            this.subjectName.push(costItemDTO.costItems[i].itemName)
                         
        }
        for (let i = 0; i < this.subproject.length; i++) {
          for (let j = 0; j < costItemDTO.costItems.length; j++) {
            if(this.subproject[i].costItemId===costItemDTO.costItems[j].id){
              this.subproject[i].itemCode=costItemDTO.costItems[j].itemCode
              this.subproject[i].itemName=costItemDTO.costItems[j].itemName
              this.subproject[i].salePrice=costItemDTO.costItems[j].salePrice
            }
          }
          
        }
       }else if(costItemDTO.costItemPackage[0]!=null&&costItemDTO.costItems[0]==null){
          this.examine=costItemDTO.costItemPackage[0]
       }
        this.tabIndex = "1";
        this.dialogProps.visible = true;
      });
      this.$on("openEditCostItemDialog", function (costItemDTO) {
        this.subjectName=[]
        this.subproject=[]
        this.itemName_List=[]
        this.arr=[]
        this.examine={
           referenceUnit:"",
           referenceValue:""
        }
       
        this.dialogProps.action = "edit";
        this.dialogProps.title = "Sửa hạng mục khám chữa bệnh";
       
        
        this.bizFormModel = { ...this.initFormModel(), ...costItemDTO.response.costItem};
        this.initOptions(this.bizFormModel);
        
        if(costItemDTO.response.costItemPackage[0]!=null&&costItemDTO.response.costItems[0]!=null){
          for (let i = 0; i < costItemDTO.response.costItemPackage.length; i++) {
          this.subproject.push(costItemDTO.response.costItemPackage[i])     
        }
        for (let i = 0; i < costItemDTO.response.costItems.length; i++) {
         
            this.subjectName.push(costItemDTO.response.costItems[i].itemName)
                 
        }
        for (let i = 0; i < this.subproject.length; i++) {
          for (let j = 0; j < costItemDTO.response.costItems.length; j++) {
            if(this.subproject[i].costItemId===costItemDTO.response.costItems[j].id){
              this.subproject[i].itemCode=costItemDTO.response.costItems[j].itemCode
              this.subproject[i].itemName=costItemDTO.response.costItems[j].itemName
              this.subproject[i].salePrice=costItemDTO.response.costItems[j].salePrice
              this.subproject[i].costPrice=costItemDTO.response.costItems[j].costPrice
            }
          }          
        }
       }else if(costItemDTO.response.costItemPackage[0]!=null&&costItemDTO.response.costItems[0]==null){
          this.examine=costItemDTO.response.costItemPackage[0]
       }

        
        if(this.subproject.length>0){
          this.salesFlage=true
        }else{
          this.salesFlage=false
        }
        this.sale=0
        this.cost=0
        //SẽsubprojectGán giá tiền của hạng mục con chosale
        for (let i = 0; i < this.subproject.length; i++) {
          this.sale+=(this.subproject[i].quantity*this.subproject[i].salePrice)
          this.cost+=(this.subproject[i].quantity*this.subproject[i].costPrice)
        }
        this.assign(costItemDTO.res)
        this.arr=this.subjectName
        this.tabIndex = "1";
        this.dialogProps.visible = true;
        
      });
      this.$on("openAddCostItemDialog", function (subprojects) {
        this.itemName_List=[]
        this.arr=[]
        this.dialogProps.action = "add";
        this.dialogProps.title = "Thêm hạng mục khám chữa bệnh";
        this.bizFormModel = this.initFormModel(this.user);
        this.initOptions(this.bizFormModel);
        this.tabIndex = "1";
        this.dialogProps.visible = true;
        
        this.assign(subprojects);
        this.subproject=[]
        this.subjectName=[]
        this.sale=0
        this.cost=0
      });
      this.$on("openCopyCostItemDialog", function (costItemDTO) {
        // this.dialogProps.action = "add";
        // this.dialogProps.title = "Thêm hạng mục khám chữa bệnh";
        // this.bizFormModel = { ...this.initFormModel(this.user), ...costItem };
        // this.initOptions(this.bizFormModel);
        // this.tabIndex = "1";
        // this.bizFormModel.id = null; //ĐặtidĐặt thành trống，Thêm mới một
        // this.dialogProps.visible = true;
        // this.subjectName=[]
        // this.subproject=[]
        // this.itemName_List=[]
        // this.arr=[]
        // this.examine={
        //    referenceUnit:"",
        //    referenceValue:""
        // }
       
        this.dialogProps.action = "add";
        this.dialogProps.title = "Thêm hạng mục khám chữa bệnh";
       
        
        this.bizFormModel = { ...this.initFormModel(), ...costItemDTO.response.costItem};
        this.initOptions(this.bizFormModel);
        
        if(costItemDTO.response.costItemPackage[0]!=null&&costItemDTO.response.costItems[0]!=null){
          for (let i = 0; i < costItemDTO.response.costItemPackage.length; i++) {
          this.subproject.push(costItemDTO.response.costItemPackage[i])     
        }
        for (let i = 0; i < costItemDTO.response.costItems.length; i++) {
         
            this.subjectName.push(costItemDTO.response.costItems[i].itemName)
                 
        }
        for (let i = 0; i < this.subproject.length; i++) {
          for (let j = 0; j < costItemDTO.response.costItems.length; j++) {
            if(this.subproject[i].costItemId===costItemDTO.response.costItems[j].id){
              this.subproject[i].itemCode=costItemDTO.response.costItems[j].itemCode
              this.subproject[i].itemName=costItemDTO.response.costItems[j].itemName
              this.subproject[i].salePrice=costItemDTO.response.costItems[j].salePrice
              this.subproject[i].costPrice=costItemDTO.response.costItems[j].costPrice
            }
          }          
        }
       }else if(costItemDTO.response.costItemPackage[0]!=null&&costItemDTO.response.costItems[0]==null){
          this.examine=costItemDTO.response.costItemPackage[0]
       }

        
        if(this.subproject.length>0){
          this.salesFlage=true
        }else{
          this.salesFlage=false
        }
        this.sale=0
        this.cost=0
        //SẽsubprojectGán giá tiền của hạng mục con chosale
        for (let i = 0; i < this.subproject.length; i++) {
          this.sale+=(this.subproject[i].quantity*this.subproject[i].salePrice)
          this.cost+=(this.subproject[i].quantity*this.subproject[i].costPrice)
        }
        this.bizFormModel.id = null;
        this.bizFormModel.itemCode = "";
        this.assign(costItemDTO.res)
        this.arr=this.subjectName
        this.tabIndex = "1";
        this.dialogProps.visible = true;
      });
    });
  },
};
</script>
<style >
 .verticalBar {
        width: 2px;
        height: 500px;
        background: rgba(235, 231, 231, 0.897);
        display: inline-block;
        margin-top: 10px;
        vertical-align: top;
        margin-right: 29px;
        margin-left: 30px;
    }
</style>