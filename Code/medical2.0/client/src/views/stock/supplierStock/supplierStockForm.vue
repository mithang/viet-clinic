<template>
  <el-dialog :title='dialogProps.title' :visible.sync='dialogProps.visible' :close-on-click-modal='false' width='50%' 
    @open='onDialogOpen()' v-loading='loading'>
    <div slot='title' class='dialog-header'>
      {{ dialogProps.title }}
      <OperationIcon v-show='dialogProps.action === "view" && permission.edit' type='primary' text='Sửa' placement='top-start' icon-name='el-icon-edit' @click='switchEdit'></OperationIcon>
    </div>

    <el-form :model='bizFormModel' :rules='formRules' 
      ref='supplierStockForm' label-width='120px' label-position='right' class='edit-form'>  
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
          <el-form-item label='Nhà cung cấp' prop='supplierId.id' >
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.supplierId.name'></el-input>
            <el-select v-else v-model='bizFormModel.supplierId' value-key='id' filterable clearable placeholder='Vui lòng chọn nhà cung cấp' 
              @clear='bizFormModel.supplierId={
                "id": null,
                                "name": null,
                }'>
             <el-option v-for='supplierId in supplierId_List' :key='supplierId.id' :label='supplierId.name' :value='supplierId'></el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Tên thuốc' prop='drug.id' >
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.drug.goodsName'></el-input>
            <el-select v-else v-model='bizFormModel.drug' value-key='id' filterable clearable placeholder='Vui lòng chọn tên thuốc' 
              @clear='bizFormModel.drug={
                "id": null,
                                "goodsName": null,
                }'>
             <el-option v-for='drug in drug_List' :key='drug.id' :label='drug.goodsName' :value='drug'></el-option>
            </el-select>
          </el-form-item>
        </el-col>
                <el-col :span='24/2'>
          <el-form-item label='Phiếu nhập khoid' prop='supplierStorage.id' >
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.supplierStorage.code'></el-input>
            <el-select v-else v-model='bizFormModel.supplierStorage' value-key='id' filterable clearable placeholder='Vui lòng chọn phiếu nhập khoid' 
              @clear='bizFormModel.supplierStorage={
                "id": null,
                                "code": null,
                }'>
             <el-option v-for='supplierStorage in supplierStorage_List' :key='supplierStorage.id' :label='supplierStorage.code' :value='supplierStorage'></el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Tên vật tư' prop='stuff.id' >
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.stuff.name'></el-input>
            <el-select v-else v-model='bizFormModel.stuff' value-key='id' filterable clearable placeholder='Vui lòng chọn tên vật tư' 
              @clear='bizFormModel.stuff={
                "id": null,
                                "name": null,
                }'>
             <el-option v-for='stuff in stuff_List' :key='stuff.id' :label='stuff.name' :value='stuff'></el-option>
            </el-select>
          </el-form-item>
        </el-col>
                <el-col :span='24/2'>
          <el-form-item label='Quy cách' prop='norms' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.norms' :maxlength='64' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập quy cách"' ></el-input>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Nhà sản xuất' prop='factory.id' >
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.factory.name'></el-input>
            <el-select v-else v-model='bizFormModel.factory' value-key='id' filterable clearable placeholder='Vui lòng chọn nhà sản xuất' 
              @clear='bizFormModel.factory={
                "id": null,
                                "name": null,
                }'>
             <el-option v-for='factory in factory_List' :key='factory.id' :label='factory.name' :value='factory'></el-option>
            </el-select>
          </el-form-item>
        </el-col>
                <el-col :span='24/2'>
          <el-form-item label='Số lô' prop='batchNo' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.batchNo' :maxlength='45' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập số lô"' ></el-input>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Ngày sản xuất' prop='produceDate' >
            <el-date-picker :disabled='dialogProps.action === "view"' v-model='bizFormModel.produceDate' type='datetime' value-format='yyyy-MM-dd HH:mm:ss' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập ngày sản xuất"' ></el-date-picker>             
          </el-form-item>
        </el-col>
                <el-col :span='24/2'>
          <el-form-item label='Hạn sử dụng' prop='endDate' >
            <el-date-picker :disabled='dialogProps.action === "view"' v-model='bizFormModel.endDate' type='datetime' value-format='yyyy-MM-dd HH:mm:ss' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập hạn sử dụng"' ></el-date-picker>             
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Số lượng' prop='number' >
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.number'></el-input>
            <number-input v-else v-model="bizFormModel.number"  :precision="0"></number-input>
          </el-form-item>
        </el-col>
                <el-col :span='24/2'>
          <el-form-item label='Giá nhập' prop='bid' >
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.bid'></el-input>
            <number-input v-else v-model="bizFormModel.bid" currency='VND' :precision="2"></number-input>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Giá bán lẻ' prop='retailPrice' >
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.retailPrice'></el-input>
            <number-input v-else v-model="bizFormModel.retailPrice" currency='VND' :precision="2"></number-input>
          </el-form-item>
        </el-col>
                <el-col :span='24/2'>
          <el-form-item label='Tổng giá nhập' prop='allBid' >
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.allBid'></el-input>
            <number-input v-else v-model="bizFormModel.allBid" currency='VND' :precision="2"></number-input>
          </el-form-item>
        </el-col>
      </el-row>
              <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Tổng giá bán lẻ' prop='allRetailPrice' >
            <el-input v-if='dialogProps.action === "view"' :disabled='true' v-model='bizFormModel.allRetailPrice'></el-input>
            <number-input v-else v-model="bizFormModel.allRetailPrice" currency='VND' :precision="2"></number-input>
          </el-form-item>
        </el-col>
    </el-row>
      </div>
    </el-form>
    <span slot='footer' class='dialog-footer'>
      <el-button v-if='dialogProps.action !== "view"' :disabled="flag" type='primary' :plain='true' @click='onSubmit("supplierStockForm")'>Lưu</el-button>
      <el-button v-if='dialogProps.action !== "view"' :plain='true' @click='onDialogClose()'>Hủy</el-button>
      <el-button v-if='dialogProps.action === "view"' :plain='true' @click='onDialogClose()'>Đóng</el-button>
    </span>    
  </el-dialog>
</template>
<script>
import { validatenull } from '@/utils/validate'
import { listCompanyAll } from '@/api/org/company'
import { listSupplierAll } from '@/api/stock/supplier'
import { listDrugAll } from '@/api/stock/drug'
import { listSupplierStorageAll } from '@/api/stock/supplierStorage'
import { listStuffAll } from '@/api/stock/stuff'
import { listManufactureFactoryAll } from '@/api/basicdata/manufactureFactory'
import { saveSupplierStock } from '@/api/stock/supplierStock'
import BaseUI from '@/views/components/baseUI'
import OperationIcon from '@/components/OperationIcon'
import { getCurrentUser, getCurrentCompanyId } from "@/utils/userCache";
export default {
  extends: BaseUI,
  name: 'supplierStock-form',
  components: {
    OperationIcon
  },
  data() {
    return {
      bizFormModel: this.initFormModel(),
      tabIndex: '1',
          company_List: [],  // Phòng khámid
          supplierId_List: [],  // Nhà cung cấp
          drug_List: [],  // Tên thuốc
          supplierStorage_List: [],  // Phiếu nhập khoid
          stuff_List: [],  // Tên vật tư
          factory_List: [],  // Nhà sản xuất
          flag:false,
       dialogProps: {
        visible: false,
        action: '',
        title: '',
        
      },
      formRules: {
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
      saveSupplierStock(this.bizFormModel).then(responseData => {
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
      this.dialogProps.title = 'Sửa tồn kho nhà cung cấp'
      this.initOptions(this.bizFormModel)
    },
    onDialogClose() {
      this.dialogProps.visible = false  
    },
    onDialogOpen() {
      this.$nextTick(() => {
        this.$refs['supplierStockForm'].clearValidate()
      })
    },
    initFormModel(This) {
      return {
        'company': {     // Phòng khámid
          'id': null,
          'name': null,
        },
        'supplierId': {     // Nhà cung cấp
          'id': null,
          'name': null,
        },
        'drug': {     // Tên thuốc
          'id': null,
          'goodsName': null,
        },
        'supplierStorage': {     // Bảng cha Phiếu nhập khoid
          'id': validatenull(This) || validatenull(This.supplierStorage) ? null : This.supplierStorage.id,
          'code': validatenull(This) || validatenull(This.supplierStorage) ? null : This.supplierStorage.code,
        },
        'stuff': {     // Tên vật tư
          'id': null,
          'name': null,
        },
        'norms': '',   // Quy cách
        'factory': {     // Nhà sản xuất
          'id': null,
          'name': null,
        },
        'batchNo': '',   // Số lô
        'produceDate': '',   // Ngày sản xuất
        'endDate': '',   // Hạn sử dụng
        'number': 0,    // Số lượng
        'bid': 0,    // Giá nhập
        'retailPrice': 0,    // Giá bán lẻ
        'allBid': 0,    // Tổng giá nhập
        'allRetailPrice': 0,    // Tổng giá bán lẻ
        'remarks': '',   // Thông tin ghi chú

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
      })
      let supplierId_search = {
        params: []
      }
        // Trên bảng tương ứng của trườngfilterĐiều kiện
        supplierId_search.params.push.apply(supplierId_search.params, [{columnName: 'company_id', queryType: '=', value: currentUser.company.id}])
      // Quyền dữ liệu: Nhà cung cấpsupplier
      this.pushDataPermissions(supplierId_search.params, this.$route.meta.routerId, '1005526731044757538')
      this.supplierId_List.splice(0, this.supplierId_List.length)
      listSupplierAll(supplierId_search).then(responseData => {
        this.supplierId_List = responseData.data
      })
      let drug_search = {
        params: []
      }
        // Trên bảng tương ứng của trườngfilterĐiều kiện
        drug_search.params.push.apply(drug_search.params, [{columnName: 'company_id', queryType: '=', value: currentUser.company.id}])
      // Quyền dữ liệu: Thông tin thuốcdrug
      this.pushDataPermissions(drug_search.params, this.$route.meta.routerId, '1004078055755374623')
      this.drug_List.splice(0, this.drug_List.length)
      listDrugAll(drug_search).then(responseData => {
        this.drug_List = responseData.data
      })
      let supplierStorage_search = {
        params: []
      }
        // Trên bảng tương ứng của trườngfilterĐiều kiện
        supplierStorage_search.params.push.apply(supplierStorage_search.params, [{columnName: 'company_id', queryType: '=', value: getCurrentCompanyId()}])
      // Quyền dữ liệu: Phiếu nhập khosupplier_storage
      this.pushDataPermissions(supplierStorage_search.params, this.$route.meta.routerId, '1007238052174135324')
      this.supplierStorage_List.splice(0, this.supplierStorage_List.length)
      listSupplierStorageAll(supplierStorage_search).then(responseData => {
        this.supplierStorage_List = responseData.data
      })
      let stuff_search = {
        params: []
      }
        // Trên bảng tương ứng của trườngfilterĐiều kiện
        stuff_search.params.push.apply(stuff_search.params, [{columnName: 'company_id', queryType: '=', value: currentUser.company.id}])
      // Quyền dữ liệu: Vật tưstuff
      this.pushDataPermissions(stuff_search.params, this.$route.meta.routerId, '1004462867645374480')
      this.stuff_List.splice(0, this.stuff_List.length)
      listStuffAll(stuff_search).then(responseData => {
        this.stuff_List = responseData.data
      })
      let factory_search = {
        params: []
      }
        // Trên bảng tương ứng của trườngfilterĐiều kiện
        factory_search.params.push.apply(factory_search.params, [{columnName: 'company_id', queryType: '=', value: currentUser.company.id}])
      // Quyền dữ liệu: Nhà sản xuấtmanufacture_factory
      this.pushDataPermissions(factory_search.params, this.$route.meta.routerId, '1016206064147988493')
      this.factory_List.splice(0, this.factory_List.length)
      listManufactureFactoryAll(factory_search).then(responseData => {
        this.factory_List = responseData.data
      })
    },
    openViewSupplierStockDialog(supplierStock) {
      this.dialogProps.action = 'view'
      this.dialogProps.title = 'Xem tồn kho nhà cung cấp'
      this.bizFormModel = {...this.initFormModel(), ...supplierStock}
      this.initOptions(this.bizFormModel)
      this.tabIndex = '1'
      this.dialogProps.visible = true
    },
    openEditSupplierStockDialog(supplierStock) {
      this.dialogProps.action = 'edit'
      this.dialogProps.title = 'Sửa tồn kho nhà cung cấp'
      this.bizFormModel = {...this.initFormModel(), ...supplierStock}
      this.initOptions(this.bizFormModel)
      this.tabIndex = '1'
      this.dialogProps.visible = true
    },
    openAddSupplierStockDialog() {
      this.dialogProps.action = 'add'
      this.dialogProps.title = 'Thêm tồn kho nhà cung cấp'
      this.bizFormModel = this.initFormModel()
      this.initOptions(this.bizFormModel)
      this.tabIndex = '1'
      this.dialogProps.visible = true
    },
    openCopySupplierStockDialog(supplierStock) {
      this.dialogProps.action = 'add'
      this.dialogProps.title = 'Thêm tồn kho nhà cung cấp'
      this.bizFormModel = {...this.initFormModel(), ...supplierStock}
      this.initOptions(this.bizFormModel)
      this.tabIndex = '1'
      this.bizFormModel.id = null   //ĐặtidĐặt thành trống，Thêm mới một
      this.dialogProps.visible = true
    }
  },
  watch: {
  }
}
</script>