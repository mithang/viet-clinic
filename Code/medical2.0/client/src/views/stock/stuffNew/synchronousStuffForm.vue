<template>
  <el-dialog :title="dialogProps.title" :visible.sync="dialogProps.visible" :close-on-click-modal="true" width="50%"
    @open="onDialogOpen()" v-loading="loading" :modal="false">
    <el-form :model="bizFormModel" :rules="formRules" ref="stuffForm" label-width="120px" style="marginTop: 10px"
      label-position="right" class="edit-form">
      <div class="tab-item" v-show="tabIndex === '1'">
        <div class="specifications">Thông tin vật tư</div>
        <el-row>
          <el-col :span="24 / 2">
            <el-form-item label="Tên vật tư" prop="name">
              <el-input :disabled="dialogProps.action === 'view' || dialogProps.action === 'edit'" v-model="bizFormModel.name" :maxlength="64"
                @input="pinyinInput" :placeholder="dialogProps.action === 'view' ? '' : 'Vui lòng nhập tên vật tư'
    " autofocus></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24 / 2">
            <el-form-item label="Tên thường dùng" prop="commonName">
              <el-input :disabled="dialogProps.action === 'view' || dialogProps.action === 'edit'" v-model="bizFormModel.commonName" :maxlength="45"
                :placeholder="dialogProps.action === 'view' ? '' : 'Vui lòng nhập tên thường dùng'
    "></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24 / 2">
            <el-form-item label="Mã pinyin" prop="pinyinCode">
              <el-input disabled v-model="bizFormModel.pinyinCode" :maxlength="45" placeholder="Vui lòng nhập mã pinyin"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24 / 2">
            <el-form-item label="Loại vật tư" prop="type.value">
              <el-input v-if="dialogProps.action === 'view'" :disabled="true"
                v-model="bizFormModel.type.name"></el-input>
              <el-select v-else v-model="bizFormModel.type" :disabled="dialogProps.action === 'edit'" value-key="value" filterable clearable placeholder="Vui lòng chọn loại vật tư"
                @clear="
    bizFormModel.type = {
      value: null,
      name: null,
    }
    ">
                <el-option v-for="item in type_List" :key="item.value" :label="item.name" :value="item"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24 / 2">
            <el-form-item label="Nhà sản xuất" prop="factory.id">
              <el-input v-if="dialogProps.action === 'view'" :disabled="true"
                v-model="bizFormModel.factory.name"></el-input>
              <el-select v-else v-model="bizFormModel.factory" :disabled="dialogProps.action === 'edit'" value-key="id" filterable clearable placeholder="Vui lòng chọn nhà sản xuất"
                @clear="
    bizFormModel.factory = {
      id: null,
      name: null,
    }
    ">
                <el-option v-for="factory in factory_List" :key="factory.id" :label="factory.name"
                  :value="factory"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24 / 2">
            <el-form-item label="Tính chất" prop="nature.value">
              <el-input v-if="dialogProps.action === 'view'" :disabled="true"
                v-model="bizFormModel.nature.name"></el-input>
              <el-select v-else v-model="bizFormModel.nature" :disabled="dialogProps.action === 'edit'" value-key="value" filterable clearable placeholder="Vui lòng chọn tính chất"
                @clear="
    bizFormModel.nature = {
      value: null,
      name: null,
    }
    ">
                <el-option v-for="nature in nature_List" :key="nature.value" :label="nature.name"
                  :value="nature"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24 / 2">
            <el-form-item label="Mã vạch" prop="barCode">
              <el-input :disabled="dialogProps.action === 'view' || dialogProps.action === 'edit'" v-model="bizFormModel.barCode" :maxlength="64"
                :placeholder="dialogProps.action === 'view' ? '' : 'Vui lòng nhập mã vạch'
    "></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24 / 2">
            <el-form-item label="Tên giấy chứng nhận đăng ký" prop="registrationName">
              <el-input :disabled="dialogProps.action === 'view' || dialogProps.action === 'edit'" v-model="bizFormModel.registrationName" :maxlength="64"
                :placeholder="dialogProps.action === 'view' ? '' : 'Vui lòng nhập tên giấy chứng nhận đăng ký'
    "></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24 / 2">
            <el-form-item label="Số giấy chứng nhận đăng ký" prop="registrationCode">
              <el-input :disabled="dialogProps.action === 'view' || dialogProps.action === 'edit'" v-model="bizFormModel.registrationCode" :maxlength="64"
                :placeholder="dialogProps.action === 'view' ? '' : 'Vui lòng nhập số giấy chứng nhận đăng ký'
    "></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24 / 2">
            <el-form-item label="Quy cách" prop="specifications">
              <el-input :disabled="dialogProps.action === 'view' || dialogProps.action === 'edit'" v-model="bizFormModel.specifications" :maxlength="45"
                :placeholder="dialogProps.action === 'view' ? '' : 'Vui lòng nhập quy cách'"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24 / 2">
            <el-form-item label="Số lượng đóng gói" prop="packNumber">
              <el-input v-if="dialogProps.action === 'view'  || dialogProps.action === 'edit'" :disabled="true"
                v-model="bizFormModel.packNumber"></el-input>
              <number-input v-else v-model="bizFormModel.packNumber" :precision="0"></number-input>
            </el-form-item>
          </el-col>
          <el-col :span="24 / 2">
            <el-form-item label="Đơn vị nhỏ nhất" prop="minUnit.value">
              <el-input v-if="dialogProps.action === 'view'" :disabled="true"
                v-model="bizFormModel.minUnit.name"></el-input>
              <el-select v-else v-model="bizFormModel.minUnit" value-key="value" filterable clearable :disabled="dialogProps.action === 'edit'"
                placeholder="Vui lòng chọn đơn vị nhỏ nhất" @clear="
    bizFormModel.minUnit = {
      value: null,
      name: null,
    }
    ">
                <el-option v-for="minUnit in minUnit_List" :key="minUnit.value" :label="minUnit.name"
                  :value="minUnit"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24 / 2">
            <el-form-item label="Đơn vị đóng gói" prop="packUnit.value">
              <el-input v-if="dialogProps.action === 'view'" :disabled="true"
                v-model="bizFormModel.packUnit.name"></el-input>
              <el-select v-else v-model="bizFormModel.packUnit" value-key="value" filterable clearable :disabled="dialogProps.action === 'edit'"
                placeholder="Vui lòng chọn đơn vị đóng gói" @clear="
    bizFormModel.packUnit = {
      value: null,
      name: null,
    }
    ">
                <el-option v-for="packUnit in packUnit_List" :key="packUnit.value" :label="packUnit.name"
                  :value="packUnit"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24 / 2">
            <el-form-item label="Cho phép bán ra ngoài" prop="isOutSell">
              <el-switch :disabled="dialogProps.action === 'view' || dialogProps.action === 'edit'" v-model="bizFormModel.isOutSell"
                active-color="#13ce66" inactive-color="#dbdfe6" active-value="1" inactive-value="0"></el-switch>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24 / 2">
            <el-form-item label="Giá bán ra ngoài" prop="priceOutSell">
              <el-input v-if="dialogProps.action === 'view'" :disabled="true"
                v-model="bizFormModel.priceOutSell"></el-input>
              <number-input v-else v-model="bizFormModel.priceOutSell" :disabled="bizFormModel.isOutSell === '0'"
                currency="VND" :precision="2"></number-input>
            </el-form-item>
          </el-col>
          <el-col :span="24 / 2">
            <el-form-item label="Cho phép bán lẻ tách hộp" prop="isUnpackSell">
              <el-switch :disabled="dialogProps.action === 'view' || dialogProps.action === 'edit'" v-model="bizFormModel.isUnpackSell"
                active-color="#13ce66" inactive-color="#dbdfe6" active-value="1" inactive-value="0"></el-switch>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24 / 2">
            <el-form-item label="Giá bán lẻ sau khi tách hộp" prop="retailPrice">
              <el-input v-if="dialogProps.action === 'view'" :disabled="true"
                v-model="bizFormModel.retailPrice"></el-input>
              <number-input v-else v-model="bizFormModel.retailPrice" :disabled="bizFormModel.isUnpackSell === '0'"
                currency="VND" :precision="2"></number-input>
            </el-form-item>
          </el-col>
          <el-col :span="24 / 2">
            <el-form-item label='Có kích hoạt không' prop='status'>
              <el-switch :disabled='dialogProps.action === "view" || dialogProps.action === "edit"' v-model='value' active-color='#13ce66'
                inactive-color='#dbdfe6' active-value='1' inactive-value='0'></el-switch>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24 / 2">
            <el-form-item label="Mã vật tư" prop="code">
              <el-input disabled v-model="bizFormModel.code" :maxlength="45" placeholder="Tự động tạo sau khi lưu"></el-input>
            </el-form-item>
          </el-col>

        </el-row>
        <el-divider class="line"></el-divider>
        <div class="specifications">Thông tin khác</div>
        <div>
          <el-row>
            <el-col :span="24 / 3">
              <el-form-item label="Hạn sử dụng" prop="indate">
                <el-input :disabled="(dialogProps.action === 'view'|| dialogProps.action === 'edit') && updateType === '0'" v-model="bizFormModel.indate"
                  :maxlength="64" placeholder="Vui lòng nhập hạn sử dụng">
                  <template slot="append">ngày</template>
                </el-input>
              </el-form-item>
            </el-col>
            <el-col :span="24 / 3">
              <el-form-item label="Ngưỡng tồn kho tối thiểu" prop="inventoryFloor">
                <el-input :disabled="(dialogProps.action === 'view'|| dialogProps.action === 'edit') && updateType === '0'"
                  v-model="bizFormModel.inventoryFloor" :maxlength="64" placeholder="Vui lòng nhập ngưỡng tồn kho tối thiểu">
                  <template slot="append">
                    {{ bizFormModel.packUnit.name }}
                  </template>
                </el-input>
              </el-form-item>
            </el-col>
            <el-col :span="24 / 3">
              <el-form-item label="Ngưỡng tồn kho tối đa" prop="inventoryLimit">
                <el-input :disabled="(dialogProps.action === 'view'|| dialogProps.action === 'edit') && updateType === '0'"
                  v-model="bizFormModel.inventoryLimit" :maxlength="64" placeholder="Vui lòng nhập ngưỡng tồn kho tối đa">
                  <template slot="append">
                    {{ bizFormModel.packUnit.name }}
                  </template>
                </el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24 / 3">
              <el-form-item label="Số vị trí kho" prop="locationNumber">
                <el-input :disabled="(dialogProps.action === 'view'|| dialogProps.action === 'edit') && updateType === '0'"
                  v-model="bizFormModel.locationNumber" :maxlength="64" placeholder="Vui lòng nhập số vị trí kho"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
        </div>
      </div>
    </el-form>
    <span slot='footer' class='dialog-footer'>
      <el-button v-if='updateType === "1" || dialogProps.action === "add"' :disabled="flag" type='primary' :plain='true'
        @click='onSubmit("stuffForm")'>Lưu</el-button>
      <el-button v-if='dialogProps.action === "add"' :disabled="flag" type='primary' :plain='true'
        @click='onGoSubmit("stuffForm")'>Lưu và tiếp tục</el-button>
      <el-button v-if='dialogProps.action !== "view"' :plain='true' @click='onDialogClose()'>Hủy</el-button>
      <el-button v-if='dialogProps.action === "view"' :plain='true' @click='onDialogClose()'>Đóng</el-button>
    </span>
  </el-dialog>
</template>
<script>
import { validatenull } from "@/utils/validate";
import { listDictItemAll } from "@/api/sys/dictItem";
import { getDictItemsByCode, DICT_CODE } from '@/utils/dictCache'
import { listManufactureFactoryAll } from "@/api/basicdata/manufactureFactory";
import { saveStuff } from "@/api/stock/stuff";
import BaseUI from "@/views/components/baseUI";
let pinyin = require('js-pinyin');
export default {
  extends: BaseUI,
  name: "stuff-form",
  components: {
  },
  data() {
    return {
      bizFormModel: this.initFormModel(),
      tabIndex: "1",
      type_List: [], // Loại vật tư
      factory_List: [], // Nhà sản xuất
      nature_List: [], // Tính chất
      minUnit_List: [], // Đơn vị nhỏ nhất
      packUnit_List: [], // Đơn vị đóng gói
      value: '1',
      flag: false,
      dialogProps: {
        visible: false,
        action: '',
        title: '',

      },
      formRules: {
        name: [{ required: true, message: "Vui lòng nhập tên vật tư", trigger: "blur" }],
        packNumber: [
          { required: true, message: "Vui lòng chọn số lượng đóng gói", trigger: "blur" },
        ],
        "minUnit.value": [
          { required: true, message: "Vui lòng chọn đơn vị nhỏ nhất", trigger: "change" },
        ],
        "packUnit.value": [
          { required: true, message: "Vui lòng chọn đơn vị đóng gói", trigger: "change" },
        ],
      },
      updateType: "0"  //Sửa dấu
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
      /*if (this.bizFormModel.company.id !== this.currentUser.company.id) {
        this.$message.error('Không thể sửa thông tin thuốc của cơ sở，Vui lòng liên hệ cơ sở để sửa thông tin thuốc cơ bản')
        return
      }*/
      this.flag = true
      this.$refs[formName].validate(valid => {
        if (valid) {
          this.doSave();
        } else {
          this.flag = false
          return false
        }
      });
    },
    //Lưu và tiếp tục
    onGoSubmit(formName) {
      this.flag = true
      this.$refs[formName].validate(valid => {
        if (valid) {
          this.setLoad()
          this.bizFormModel.status = this.value
          saveStuff(this.bizFormModel).then(responseData => {
            this.flag = false
            if (responseData.code === 100) {
              // this.dialogProps.visible = false
              this.bizFormModel.name = ""
              this.bizFormModel.commonName = ""
              this.bizFormModel.code = ""
              this.bizFormModel.type = {}
              this.bizFormModel.factory = {}
              this.bizFormModel.nature = {}
              this.bizFormModel.barCode = ""
              this.bizFormModel.registrationName = ""
              this.bizFormModel.registrationCode = ""
              this.bizFormModel.specifications = ""
              this.bizFormModel.packNumber = 0
              // this.bizFormModel.minUnit={}
              //  this.bizFormModel.packUnit=""
              this.bizFormModel.isOutSell = "0"
              this.bizFormModel.priceOutSell = 0
              this.bizFormModel.isUnpackSell = "0"
              this.bizFormModel.retailPrice = 0
              this.bizFormModel.remarks = ""
              this.bizFormModel.pinyinCode = ""
              this.bizFormModel.status = ""
              this.value = '1'
              this.$emit('save-finished', "1")
            } else {
              this.showMessage(responseData)
            }
            this.resetLoad()
          }).catch(error => {
            this.flag = false
            this.outputError(error)
          })
        } else {
          this.flag = false
          return false
        }
      });
    },
    doSave() {
      this.setLoad()
      this.bizFormModel.status = this.value
      saveStuff(this.bizFormModel).then(responseData => {
        this.flag = false
        if (responseData.code === 100) {
          this.dialogProps.visible = false
          this.$emit('save-finished', "1")
        } else {
          this.showMessage(responseData)
        }
        this.resetLoad()
      }).catch(error => {
        this.flag = false
        this.outputError(error)
      })
    },
    switchEdit() {
      this.dialogProps.action = "edit";
      this.dialogProps.title = "Sửa thông tin vật tư";
      this.initOptions(this.bizFormModel);
    },
    onDialogClose() {
      this.dialogProps.visible = false;
    },
    onDialogOpen() {
      this.$nextTick(() => {
        this.$refs["stuffForm"].clearValidate();
      });
    },
    initFormModel(This) {
      return {
        company: {
          // Phòng khámid
          id: currentUser.company.id,
          name: null,
        },
        name: "", // Tên vật tư
        commonName: "", // Tên thường dùng
        code: "", // Mã vật tư
        type: {
          // Loại vật tư
          value: null,
          name: null,
        },
        factory: {
          // Nhà sản xuất
          id: null,
          name: null,
        },
        nature: {
          // Tính chất
          value: null,
          name: null,
        },
        barCode: "", // Mã vạch
        registrationName: "", // Tên giấy chứng nhận đăng ký
        registrationCode: "", // Số giấy chứng nhận đăng ký
        specifications: "", // Quy cách
        packNumber: 0, // Số lượng đóng gói
        minUnit: {
          // Đơn vị nhỏ nhất
          value: null,
          name: null,
        },
        packUnit: {
          // Đơn vị đóng gói
          value: null,
          name: null,
        },
        isOutSell: "0", // Cho phép bán ra ngoài
        priceOutSell: 0, // Giá bán ra ngoài
        isUnpackSell: "0", // Cho phép bán lẻ tách hộp
        retailPrice: 0, // Giá bán lẻ sau khi tách hộp
        remarks: "", // Thông tin ghi chú
      };
    },
    initOptions(This) {
      getDictItemsByCode(DICT_CODE.STUFF_TYPE).then((data) => {
        this.type_List = data;
      });
      let factory_search = {
        params: [],
      };
      // Trên bảng tương ứng của trườngfilterĐiều kiện
      factory_search.params.push.apply(factory_search.params, [
        {
          columnName: "company_id",
          queryType: "=",
          value: currentUser.company.id,
        },
        {
          columnName: "type",
          queryType: "=",
          value: "2",
        },
      ]);
      // Quyền dữ liệu: Nhà sản xuấtmanufacture_factory
      this.pushDataPermissions(
        factory_search.params,
        this.$route.meta.routerId,
        "1016206064147988493"
      );
      this.factory_List.splice(0, this.factory_List.length);
      listManufactureFactoryAll(factory_search).then((responseData) => {
        this.factory_List = responseData.data;
      });
      getDictItemsByCode(DICT_CODE.MEDICAL_NATURE).then((data) => {
        this.nature_List = data;
      });
      getDictItemsByCode(DICT_CODE.MEDICAL_PREPARATION_UNIT).then((data) => {
        this.minUnit_List = data;
      });
      getDictItemsByCode(DICT_CODE.MEDICAL_PACK_UNIT).then((data) => {
        this.packUnit_List = data;
      });
    },
    //Mã pinyin của ô nhập
    pinyinInput(value) {
      this.bizFormModel.pinyinCode = pinyin.getCamelChars(value)

    },
    openViewStuffDialog(stuff) {
      this.dialogProps.action = "view";
      this.updateType = "0"
      this.dialogProps.title = "Xem thông tin vật tư";
      this.bizFormModel = { ...this.initFormModel(), ...stuff };
      this.initOptions(this.bizFormModel);
      this.value = this.bizFormModel.status
      this.tabIndex = "1";
      this.dialogProps.visible = true;
    },
    openEditStuffDialog(stuff) {
      if ((stuff.stock.storageStock - stuff.stock.usedStock - stuff.stock.reimburseStock)) {
        this.dialogProps.action = "view";
      } else {
        this.dialogProps.action = "edit";
      }
      this.updateType = "0"
      this.dialogProps.title = "Sửa thông tin vật tư";
      this.bizFormModel = { ...this.initFormModel(), ...stuff };
      this.initOptions(this.bizFormModel);
      this.value = this.bizFormModel.status
      this.tabIndex = "1";
      this.dialogProps.visible = true;
    },
    openAddStuffDialog() {
      this.dialogProps.action = "add";
      this.dialogProps.title = "Thêm thông tin vật tư";
      this.bizFormModel = this.initFormModel();
      this.initOptions(this.bizFormModel);
      this.tabIndex = "1";
      this.dialogProps.visible = true;
    },
    openCopyStuffDialog(stuff) {
      this.dialogProps.action = "add";
      this.dialogProps.title = "Thêm thông tin vật tư";
      this.bizFormModel = { ...this.initFormModel(), ...stuff };
      this.initOptions(this.bizFormModel);
      this.tabIndex = "1";
      this.bizFormModel.id = null;
      this.dialogProps.visible = true;
    },
  },
  watch: {},
  mounted: function () {
  },
};
</script>
<style scoped>
.line {
  margin-top: -10px;
}

.specifications {
  margin-top: -30px;
  width: 100%;
  font-weight: bold;
  padding-bottom: 8px;
  padding: 20px;
  color: #333;
}

/* .specifications {
  width: 100%;
  font-weight: bold;
  padding-bottom: 8px;
  padding: 20px;
  color: #333;
} */
</style>
