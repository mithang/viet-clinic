<template>
  <el-dialog :title='dialogProps.title' :visible.sync='dialogProps.visible' :close-on-click-modal='false' width='80%'
    @open='onDialogOpen()' v-loading='loading'>
    <div slot='title' class='dialog-header'>
      {{ dialogProps.title }}
    </div>
    <el-form :model='bizFormModel' :rules='formRules' v-if="dialogProps.dataLoaded"
      ref='reviewForm' label-width='120px' label-position='right' class='edit-form'>
      <el-row>
        <el-col :span='5'>
          <el-form-item label='Tên bệnh nhân' >
            <el-input :disabled='true' v-model='bizFormModel.recipelInfoReview.recipelInfo.registration.patientId.name' :maxlength='128' :placeholder='""' ></el-input>
          </el-form-item>
        </el-col>
        <el-col :span='5'>
          <el-form-item label='Giới tính'>
            <el-input :disabled='true' v-model='bizFormModel.recipelInfoReview.recipelInfo.registration.patientId.gender.name' :maxlength='128' :placeholder='""' ></el-input>
          </el-form-item>
        </el-col>
        <el-col :span='5'>
          <el-form-item label='Tuổi' >
            <el-input :disabled='true' v-model='bizFormModel.recipelInfoReview.recipelInfo.registration.patientId.age' :maxlength='128' :placeholder='""' ></el-input>
          </el-form-item>
        </el-col>
        <el-col :span='5'>
          <el-form-item label='CMND/CCCD' >
            <el-input :disabled='true' v-model='bizFormModel.recipelInfoReview.recipelInfo.registration.patientId.card' :maxlength='128' :placeholder='""' ></el-input>
          </el-form-item>
        </el-col>
        <el-col :span='4'>
          <el-form-item label='Thông tin liên hệ' >
            <el-input :disabled='true' v-model='bizFormModel.recipelInfoReview.recipelInfo.registration.patientId.phone' :maxlength='128' :placeholder='""' ></el-input>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Chẩn đoán tây y' >
            <el-input :disabled='true' v-model='bizFormModel.westernDiagnose' type='textarea'
                      :maxlength='2000' :placeholder='""'></el-input>
          </el-form-item>
        </el-col>
        <el-col :span='24/2'>
          <el-form-item label='Chẩn đoán đông y' >
            <el-input :disabled='true' v-model='bizFormModel.chinaDiagnose' type='textarea'
                      :maxlength='2000' :placeholder='""'></el-input>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col>
          <span class="price">Chi phí của đơn thuốc này{{bizFormModel.recipelInfoReview.recipelInfo.fee}}VNĐ</span>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="24/1">
          <el-divider content-position="left">Thông tin đơn thuốc</el-divider>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="3">
          <el-input
            disabled
            v-model="bizFormModel.recipelInfoReview.recipelInfo.recipelType.name"></el-input>
        </el-col>
        <el-col :span="4">
          <!--<el-checkbox v-if="bizFormModel.recipelInfoReview.recipelInfo.recipelType.value == 'recipelType_0'"
                       style="margin-left: 20px;"
                       disabled
                       v-model="bizFormModel.recipelInfoReview.recipelInfo.chronicDisease"  >Có phải bệnh mạn tính không</el-checkbox>-->
          <el-switch
            v-if="bizFormModel.recipelInfoReview.recipelInfo.recipelType.value === 'recipelType_0'"
            style="margin-left: 20px;"
            v-model="bizFormModel.recipelInfoReview.recipelInfo.chronicDisease"
            active-text="Có phải bệnh mạn tính không"
            disabled>
          </el-switch>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="24/1">
          <el-table :data="bizFormModel.recipelDetails" >
            <el-table-column label="Số thứ tự" type="index" align="center">
            </el-table-column>
            <el-table-column
              prop="drugStuffId.name"
              label="Tên thuốc"
              width="width">
              <template slot-scope="scope">
                {{ scope.row.drugStuffId.name }}
                <div v-if="bizFormModel.recipelInfoReview.recipelInfo.recipelType.value==='recipelType_0'">
                        <span class="nameStyle"
                        >{{ scope.row.drugStuffId.drug.dosis
                          }}{{ scope.row.drugStuffId.drug.dosisUnit.name }} *
                          {{ scope.row.drugStuffId.drug.preparation
                          }}{{ scope.row.drugStuffId.drug.preparationUnit.name }}/{{
                            scope.row.drugStuffId.drug.pack.name
                          }}</span
                        >
                </div>
              </template>
            </el-table-column>
            <el-table-column
              prop="drugStuffId.drug.factory.name"
              label="Nhà sản xuất"
              width="width"
            >
            </el-table-column>

            <el-table-column
              prop="westernMedicineUse.name"
              label="Cách dùng"
              width="width"
              v-if="bizFormModel.recipelInfoReview.recipelInfo.recipelType.value === 'recipelType_0'"
            >
            </el-table-column>
            <el-table-column
              prop="singleDosage"
              label="Liều dùng mỗi lần"
              width="width"
              v-if="bizFormModel.recipelInfoReview.recipelInfo.recipelType.value === 'recipelType_0'"
            >
              <template slot-scope="scope">
                <template>{{scope.row.singleDosage?scope.row.singleDosage+''+scope.row.drugStuffId.dosisUnit.name:"" }}</template>
              </template>
            </el-table-column>
            <el-table-column
              prop="frequency"
              label="Tần suất"
              align="center"
              v-if="bizFormModel.recipelInfoReview.recipelInfo.recipelType.value === 'recipelType_0'"
              width="width"
            >
              <template slot-scope="scope"
              >{{ scope.row.infuseUse.name
                }}{{ scope.row.frequency.name }}</template
              >
            </el-table-column>
            <el-table-column prop="days.name" label="Số ngày" width="width" align="center"
                             v-if="bizFormModel.recipelInfoReview.recipelInfo.recipelType.value === 'recipelType_0'">
              <template slot-scope="scope">
                {{scope.row.days.name}}ngày
              </template>
            </el-table-column>
            <el-table-column prop="total" label="Số lượng" width="width">
              <template slot-scope="scope">
                        <span v-if="scope.row.drugStuffId.costItem != null">
                          {{ scope.row.total
                          }}{{ scope.row.drugStuffId.dosisUnit.name }}
                        </span>
                <span v-else-if="scope.row.drugStuffId.stuff != null">
                          {{
                            Math.floor(
                              scope.row.total / scope.row.drugStuffId.stuff.packNumber
                            ) > 0
                              ? Math.floor(
                                  scope.row.total /
                                    scope.row.drugStuffId.stuff.packNumber
                                ) +
                                scope.row.drugStuffId.stuff.packUnit.name +
                                (scope.row.total %
                                  scope.row.drugStuffId.stuff.packNumber >
                                0
                                  ? (scope.row.total %
                                      scope.row.drugStuffId.stuff.packNumber) +
                                    scope.row.drugStuffId.stuff.minUnit.name
                                  : "")
                              : scope.row.total +
                                scope.row.drugStuffId.stuff.minUnit.name
                          }}
                        </span>
                <span v-else-if="scope.row.drugStuffId.drug != null">
                          {{
                            Math.floor(
                              scope.row.total /
                                (scope.row.drugStuffId.drug.preparation - 0)
                            ) > 0
                              ? Math.floor(
                                  scope.row.total /
                                    (scope.row.drugStuffId.drug.preparation - 0)
                                ) +
                                scope.row.drugStuffId.drug.pack.name +
                                (scope.row.total %
                                  (scope.row.drugStuffId.drug.preparation - 0) >
                                0
                                  ? (scope.row.total %
                                      (scope.row.drugStuffId.drug.preparation - 0)) +
                                    scope.row.drugStuffId.drug.preparationUnit.name
                                  : "")
                              : scope.row.total +
                                scope.row.drugStuffId.drug.preparationUnit.name
                          }}
                        </span>
              </template>

            </el-table-column>
            <el-table-column
              prop="unitPrice"
              label="Đơn giá"
              width="width"
            >
              <template slot-scope="scope">
                      <span
                      >{{ scope.row.unitPrice }}VNĐ/{{
                          scope.row.isUnpackSell === 1
                            ? scope.row.drugStuffId.preparationUnit.name
                            : scope.row.drugStuffId.pack.name
                        }}</span
                      >
              </template>
            </el-table-column>
            <el-table-column prop="allFee" width="width" label="Tổng tiền">
              <template slot-scope="scope">
                {{scope.row.allFee}}VNĐ
              </template>
            </el-table-column>
          </el-table>
        </el-col>
      </el-row>
      <el-row v-if="bizFormModel.recipelInfoReview.recipelInfo.recipelType.value  ==='recipelType_1'">
        <el-input disabled v-model="bizFormModel.recipelInfoReview.recipelInfo.dosage" oninput="value=value.replace(/[^\d.]/g,'')" style="width: 60px"></el-input>
        &nbsp;thang &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Cách dùng：
        <el-select disabled v-model="bizFormModel.recipelInfoReview.recipelInfo.recipelUse" placeholder="Vui lòng chọn" style="width: 110px">
          <el-option v-for="pitem in ChineseUseOption" :key="pitem.value"
                     :label="pitem.name"
                     :value="{ name: pitem.name, value: pitem.value }">
          </el-option>
        </el-select>
        <el-select disabled v-model="bizFormModel.recipelInfoReview.recipelInfo.frequency" placeholder="Vui lòng chọn" style="width: 110px">
          <el-option v-for="pitem in ChineseTimeOption" :key="pitem.value"
                     :label="pitem.name"
                     :value="{ name: pitem.name, value: pitem.value }">
          </el-option>
        </el-select>
        <el-select disabled v-model="bizFormModel.recipelInfoReview.recipelInfo.takeFrequency" placeholder="Vui lòng chọn" style="width: 110px">
          <el-option v-for="pitem in JSON.parse(
                            JSON.stringify(FrequencyOption)
                          )" :key="pitem.value" :label="pitem.name" :value="{
                            name: pitem.name,
                            value: pitem.value,
                          }">
          </el-option>
        </el-select>
        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; một lần&nbsp;<el-input disabled v-model="bizFormModel.recipelInfoReview.recipelInfo.singleDosage" style="width: 60px"></el-input>&nbsp;
        <span>ml</span>
      </el-row>
      <el-row>
        <el-col :span="24/1">
          <el-divider content-position="left">Phí phụ thêm</el-divider>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="24/1">
          <el-table :data="bizFormModel.additionalCharges">
            <el-table-column label="Số thứ tự" type="index" align="center" width="width">
            </el-table-column>
            <el-table-column
              prop="drugStuffId.name"
              label="Tên"
              width="width"
            >
            </el-table-column>
            <el-table-column
              prop="drugStuffId.drug.factory.name"
              label="Nhà sản xuất"
              width="width"
            >
            </el-table-column>
            <el-table-column
              prop="unitPrice"
              label="Đơn giá"
              width="width"
            >
              <template slot-scope="scope">
                      <span
                      >{{ scope.row.unitPrice }}VNĐ/{{
                          scope.row.isUnpackSell === 1
                            ? scope.row.drugStuffId.preparationUnit.name
                            : scope.row.drugStuffId.pack.name
                        }}</span
                      >
              </template>
            </el-table-column>
            <el-table-column prop="total" label="Số lượng" width="width">
              <template slot-scope="scope">
                        <span v-if="scope.row.drugStuffId.costItem != null">
                          {{ scope.row.total
                          }}{{ scope.row.drugStuffId.dosisUnit.name }}
                        </span>
                <span v-else-if="scope.row.drugStuffId.stuff != null">
                          {{
                            Math.floor(
                              scope.row.total / scope.row.drugStuffId.stuff.packNumber
                            ) > 0
                              ? Math.floor(
                                  scope.row.total /
                                    scope.row.drugStuffId.stuff.packNumber
                                ) +
                                scope.row.drugStuffId.stuff.packUnit.name +
                                (scope.row.total %
                                  scope.row.drugStuffId.stuff.packNumber >
                                0
                                  ? (scope.row.total %
                                      scope.row.drugStuffId.stuff.packNumber) +
                                    scope.row.drugStuffId.stuff.minUnit.name
                                  : "")
                              : scope.row.total +
                                scope.row.drugStuffId.stuff.minUnit.name
                          }}
                        </span>
                <span v-else-if="scope.row.drugStuffId.drug != null">
                          {{
                            Math.floor(
                              scope.row.total /
                                (scope.row.drugStuffId.drug.preparation - 0)
                            ) > 0
                              ? Math.floor(
                                  scope.row.total /
                                    (scope.row.drugStuffId.drug.preparation - 0)
                                ) +
                                scope.row.drugStuffId.drug.pack.name +
                                (scope.row.total %
                                  (scope.row.drugStuffId.drug.preparation - 0) >
                                0
                                  ? (scope.row.total %
                                      (scope.row.drugStuffId.drug.preparation - 0)) +
                                    scope.row.drugStuffId.drug.preparationUnit.name
                                  : "")
                              : scope.row.total +
                                scope.row.drugStuffId.drug.preparationUnit.name
                          }}
                        </span>
              </template>
            </el-table-column>
            <el-table-column prop="allFee" label="Tổng tiền" width="width">
              <template slot-scope="scope">
                {{scope.row.allFee}}VNĐ
              </template>
            </el-table-column>
          </el-table>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="24/1">
          <el-divider content-position="left">Hạng mục y lệnh</el-divider>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="24/1">
          <el-input
            type="textarea"
            :rows="2"
            v-model="bizFormModel.recipelInfoReview.recipelInfo.entrust"
            disabled>
          </el-input>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="24/1">
          <el-divider content-position="left">Nội dung thẩm tra đơn thuốc</el-divider>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="24/3">
          <el-form-item label='Có hợp lý không' >
            <el-radio-group :disabled="dialogProps.action === 'view'" v-model="bizFormModel.recipelInfoReview.reviewResult">
              <el-radio :label="1">Hợp lý</el-radio>
              <el-radio :label="0">Không hợp lý</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="24/3">
          <el-form-item label='Người thẩm tra'  v-if='bizFormModel.recipelInfoReview.reviewStatus === "1"'>
            <el-input :disabled='true'  v-model='bizFormModel.recipelInfoReview.reviewerName' :placeholder='""'></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="24/3">
          <el-form-item label='Thời gian thẩm tra'  v-if='bizFormModel.recipelInfoReview.reviewStatus === "1"'>
            <el-input :disabled='true'  v-model='bizFormModel.recipelInfoReview.createDate' :placeholder='""'></el-input>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="24/1">
          <el-input
            type="textarea"
            :rows="2"
            :placeholder='"Vui lòng nhập nội dung"'
            :disabled="dialogProps.action === 'view'"
            v-model="bizFormModel.recipelInfoReview.reviewContent">
          </el-input>
        </el-col>
      </el-row>
    </el-form>
    <span slot='footer' class='dialog-footer'>
      <el-button v-if='dialogProps.action !== "view"' :disabled="flag" type='primary' :plain='true' @click='onSubmit("reviewForm")'>Lưu</el-button>
      <el-button v-if='dialogProps.action !== "view"' :disabled="flag" type='primary' :plain='true' @click='onSubmitAndContinue("reviewForm")'>Lưu và tiếp tục</el-button>
      <el-button v-if='dialogProps.action !== "view"' :plain='true' @click='onDialogClose()'>Hủy</el-button>
      <el-button v-if='dialogProps.action === "view"' :plain='true' @click='onDialogClose()'>Đóng</el-button>
    </span>
  </el-dialog>
</template>
<script>
  import { saveRecipelInfoReview } from '@/api/outpatient/recipelInfoReview'
  import {listDictItemAll} from "@/api/sys/dictItem"
  import BaseUI from '@/views/components/baseUI'
  export default {
  extends: BaseUI,
  name: 'review-form',
  components: {
  },
    computed: {
    },
  data() {
    return {
      bizFormModel: this.initFormModel(),
      flag: false,
      dialogProps: {
        visible: false,
        action: '',
        title: '',
        dataLoaded: false
      },
      formRules: {
        'name': [
          {required: true, message: 'Vui lòng nhập tên', trigger: 'blur'}
        ],
        'theme': [
          {required: true, message: 'Vui lòng nhập chủ đề', trigger: 'blur'}
        ],
      },
      //Danh sách thả xuống cách dùng thuốc Đông y
      ChineseUseOption: [],
      ChineseTimeOption: [],
      ChineseUseTimeOption: [],
      //Phòng khám đặc biệt
      isSpecial: false,
      //Danh sách thả xuống tần suất thuốc Tây y
      FrequencyOption: [],
    };
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
          this.doSave(false)
        } else {
          this.flag=false
          return false
        }
      });
    },
    onSubmitAndContinue(formName){
      this.flag=true
      this.$refs[formName].validate(valid => {
        if (valid) {
          this.doSave(true)
        } else {
          this.flag=false
          return false
        }
      });
    },
    doSave(visible) {
      this.setLoad()
      this.bizFormModel.recipelInfoReview.reviewerName = this.currentUser.name
      this.bizFormModel.recipelInfoReview.reviewStatus = 1
      saveRecipelInfoReview(this.bizFormModel.recipelInfoReview).then(responseData => {
        this.flag=false
        if(responseData.code === 100) {
          if (visible) {
            this.$emit('save-and-continue');
          }else {
            this.dialogProps.visible = visible
            this.$emit('save-finished');
          }
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
      this.dialogProps.title = 'Sửa thẩm tra đơn thuốc'
      this.initOptions(this.bizFormModel)
    },
    onDialogClose() {
      this.dialogProps.visible = false
      this.$emit('save-finished');
    },
    onDialogOpen() {
      this.$nextTick(() => {
        this.$refs['reviewForm'].clearValidate()
      })
    },
    updateData(review){
      this.bizFormModel = review
    },
    initFormModel(This) {
      return {

      }
    },
    initOptions(This) {
      if (this.ChineseUseOption.length === 0) {
        this.getOption("1014474470772899981")
      }
      if (this.ChineseTimeOption.length === 0) {
        this.getOption("1014474470772899985")
      }
      if (this.ChineseUseTimeOption.length === 0) {
        this.getOption("1014474470772900058")
      }
      if (this.FrequencyOption.length === 0) {
        this.getOption("1014474470772899990")
      }
    },
    getOption(optionId){
      let model = {
        params: [{
          columnName: "dict_type_id",
          queryType: "=",
          value: optionId,
        },],
      };
      listDictItemAll(model).then((responseData) => {
        if (optionId === "1014474470772899981") {
          this.ChineseUseOption = responseData.data;
          if (this.isSpecial) {
            this.ChineseUseOption = this.ChineseUseOption.filter(
              (item) => item.name === "水冲"
            );
          }
        }else if (optionId === "1014474470772899985")
          this.ChineseTimeOption = responseData.data;
        else if (optionId === "1014474470772899990")
          this.FrequencyOption = responseData.data;
        else if (optionId === "1014474470772900058")
          this.ChineseUseTimeOption = responseData.data;
      });
    },
    openViewReviewDialog(review) {
      this.dialogProps.action = 'view'
      this.dialogProps.title = '查看处方审查'
      this.bizFormModel = review
      this.initOptions(this.bizFormModel)
      this.dialogProps.visible = true
      this.dialogProps.dataLoaded = true
    },
    openEditReviewDialog(review) {
      this.dialogProps.action = 'edit'
      this.dialogProps.title = 'Sửa thẩm tra đơn thuốc'
      this.bizFormModel = review
      this.initOptions(this.bizFormModel)
      this.dialogProps.visible = true
      this.dialogProps.dataLoaded = true
    },
    openAddReviewDialog(review) {
      this.dialogProps.action = 'add'
      this.dialogProps.title = 'Thêm thẩm tra đơn thuốc'
      this.bizFormModel = this.initFormModel()
      this.initOptions(this.bizFormModel)
      this.dialogProps.visible = true
      this.dialogProps.dataLoaded = true
    },
    openCopyReviewDialog(review) {
      this.dialogProps.action = 'add'
      this.dialogProps.title = 'Thêm thẩm tra đơn thuốc'
      this.bizFormModel = review
      this.initOptions(this.bizFormModel)
      this.bizFormModel.id = null   //ĐặtidĐặt thành trống，Thêm mới một
      this.dialogProps.visible = true
      this.dialogProps.dataLoaded = true
    },
  },
  watch: {
  },
}
</script>
<style scoped>
  .price{
    padding: 8px 0 0;
    font-size: 14px;
    font-weight: 700;
    color: midnightblue;
    line-height: 28px;
  }
</style>
