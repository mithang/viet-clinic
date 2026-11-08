<template>
  <el-dialog
    :title="dialogProps.title"
    :visible.sync="dialogProps.visible"
    :close-on-click-modal="false"
    width="50%"
    @open="onDialogOpen()"
    v-loading="loading"
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
      ref="patientForm"
      label-width="120px"
      label-position="right"
      class="edit-form"
      style="marginTop: 10px"
    >
      <div class="tab-item" v-show="tabIndex === '1'">
        <el-row>
          <el-col :span="24 / 2">
            <el-form-item label="Tên bệnh nhân" prop="name">
              <el-input
                :disabled="dialogProps.action === 'view'"
                v-model="bizFormModel.name"
                :maxlength="45"
                :placeholder="
                  dialogProps.action === 'view' ? '' : 'Vui lòng nhập tên bệnh nhân'
                "
                autofocus
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24 / 2">
            <el-form-item label="Giới tính" prop="gender.value">
              <el-input
                v-if="dialogProps.action === 'view'"
                :disabled="true"
                v-model="bizFormModel.gender.name"
              ></el-input>
              <el-select
                v-else
                v-model="bizFormModel.gender"
                value-key="value"
                filterable
                clearable
                placeholder="Vui lòng chọn giới tính"
                @clear="
                  bizFormModel.gender = {
                    value: null,
                    name: null,
                  }
                "
              >
                <el-option
                  v-for="gender in gender_List"
                  :key="gender.value"
                  :label="gender.name"
                  :value="gender"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="24 / 2">
            <el-form-item label="Ngày sinh" prop="birthday">
              <el-date-picker
                :disabled="dialogProps.action === 'view'"
                v-model="bizFormModel.birthday"
                type="date"
                value-format="yyyy-MM-dd"
                format="dd/MM/yyyy"
                :placeholder="
                  dialogProps.action === 'view' ? '' : 'Vui lòng nhập ngày sinh'
                "
                @change="birthdayChanges"
              ></el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="24 / 2">
            <el-form-item label="Tuổi" prop="age">
              <el-col :span="24 / 2">
                <el-input
                  :disabled="false"
                  v-model="bizFormModel.age"
                  :maxlength="20"
                  :placeholder="dialogProps.action === 'view' ? '' : 'Tuổi'"
                  @input="exchageAge"
                >
                  <template slot="append">
                    tuổi
                  </template>
                </el-input>
              </el-col>
              <el-col :span="24 / 2">
                <el-input
                  :disabled="false"
                  v-model="bizFormModel.month"
                  :maxlength="20"
                  :placeholder="dialogProps.action === 'view' ? '' : 'tháng'"
                >
                  <template slot="append">
                    tháng
                  </template>
                </el-input>
              </el-col>
            </el-form-item>
          </el-col>

        </el-row>
        <el-row>
          <el-col :span="24 / 2">
            <el-form-item label="Số CMND/CCCD" prop="card">
              <el-input
                :disabled="dialogProps.action === 'view'"
                v-model="bizFormModel.card"
                :maxlength="18"
                :placeholder="
                  dialogProps.action === 'view' ? '' : 'Vui lòng nhập số CMND/CCCD'
                "
                @input="getBirthday"
                @blur="checkDuplicates"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24 / 2">
            <el-form-item label="Thông tin liên hệ" prop="phone">
              <el-input
                :disabled="dialogProps.action === 'view'"
                v-model="bizFormModel.phone"
                :maxlength="45"
                :placeholder="
                  dialogProps.action === 'view' ? '' : 'Vui lòng nhập thông tin liên hệ'
                "
                @blur="checkDuplicates"
              ></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24 / 2">
            <el-form-item label="Họ tên người giám hộ" prop="guardianName">
              <el-input
                :disabled="dialogProps.action === 'view'"
                v-model="bizFormModel.guardianName"
                :maxlength="45"
                :placeholder="dialogProps.action === 'view' ? '' : 'Vui lòng nhập tên người giám hộ'"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24 / 2">
            <el-form-item label="Số điện thoại người giám hộ" prop="guardianPhone">
              <el-input
                :disabled="dialogProps.action === 'view'"
                v-model="bizFormModel.guardianPhone"
                :maxlength="45"
                :placeholder="dialogProps.action === 'view' ? '' : 'Vui lòng nhập số điện thoại người giám hộ'"
              ></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24 / 2">
            <el-form-item label="Quan hệ với bệnh nhân" prop="withPatientNexus.value">
              <el-input
                v-if="dialogProps.action === 'view'"
                :disabled="true"
                v-model="bizFormModel.withPatientNexus.name"
              ></el-input>
              <el-select
                v-else
                v-model="bizFormModel.withPatientNexus"
                value-key="value"
                filterable
                clearable
                placeholder="Vui lòng chọn quan hệ với bệnh nhân"
                @clear="
                  bizFormModel.withPatientNexus = {
                    value: null,
                    name: null,
                  }
                "
              >
                <el-option
                  v-for="withPatientNexus in withPatientNexus_List"
                  :key="withPatientNexus.value"
                  :label="withPatientNexus.name"
                  :value="withPatientNexus"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24 / 2">
            <el-form-item label="Số thẻ sức khỏe" prop="healthCardNo">
              <el-input
                :disabled="dialogProps.action === 'view'"
                v-model="bizFormModel.healthCardNo"
                :maxlength="45"
                :placeholder="
                  dialogProps.action === 'view' ? '' : 'Vui lòng nhập số thẻ sức khỏe'
                "
              ></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>

          <!-- <el-col :span="24 / 2">
            <el-form-item label="Tỉnh/Thành phố" prop="province">
               <el-select
                :disabled="dialogProps.action === 'view'"
                v-model="bizFormModel.province"
                value-key="value"
                filterable
                clearable
                :placeholder="dialogProps.action === 'view' ? '' : 'Chọn tỉnh/thành phố'"
                @clear="
                  bizFormModel.province = '';
                  bizFormModel.city= '';
                  bizFormModel.area= '';

                "
                @change="changeProvince"
              >
                <el-option
                  v-for="prov in province_List"
                  :key="prov.id"
                  :label="prov.name"
                  :value="prov.id"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col> -->
        </el-row>
        <el-row>

          <!-- <el-form-item label="Thành phố" prop="city">
            <el-select
              :disabled="dialogProps.action === 'view'"
              v-model="bizFormModel.city"
              :maxlength="45"
              value-key="value"
              filterable
              clearable
              :placeholder="dialogProps.action === 'view' ? '' : 'Chọn quận/huyện'"
              @clear="
                bizFormModel.city= '';
                bizFormModel.area= '';
              "
              @change="changeCity"
            >
              <el-option
                v-for="city in city_List"
                :key="city.id"
                :label="city.name"
                :value="city.id"
              ></el-option>
            </el-select>
          </el-form-item> -->
          <el-form-item label='Khu vực' prop=''>
            <v-distpicker :disabled='dialogProps.action === "view"' :province="province" :city="city" :area="area"
                          @province="onChangeProvince" @city="onChangeCity" @area="onChangeArea"></v-distpicker>
          </el-form-item>

          <!-- <el-col :span="24 / 2">
            <el-form-item label="Quận/Huyện" prop="area">
              <el-select
                :disabled="dialogProps.action === 'view'"
                :maxlength="45"
                v-model="bizFormModel.area"
                value-key="value"
                filterable
                clearable
                :placeholder="dialogProps.action === 'view' ? '' : 'Chọn xã/phường'"
                @clear="
                  bizFormModel.area= '';
                "
              >
                <el-option
                  v-for="area in area_List"
                  :key="area.id"
                  :label="area.name"
                  :value="area.id"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col> -->
        </el-row>
        <el-row>
          <el-col :span="24 / 2">
            <el-form-item label="Địa chỉ chi tiết" prop="address">
              <el-input
                :disabled="dialogProps.action === 'view'"
                v-model="bizFormModel.address"
                :maxlength="45"
                :placeholder="
                  dialogProps.action === 'view' ? '' : 'Vui lòng nhập địa chỉ chi tiết'
                "
              ></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col>
            <el-form-item label="Thông tin ghi chú" prop="remarks">
              <el-input
                :disabled="dialogProps.action === 'view'"
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
        <!-- N1: prompt bệnh nhân trùng khớp (chỉ ở chế độ thêm mới, như G3 ở
          form đăng ký). Không tự động merge: người dùng chọn một bệnh nhân
          để nạp lại hồ sơ có sẵn, hoặc bấm "Tạo bệnh nhân mới" để tắt
          prompt với SĐT/card này và lưu như trước. -->
        <el-row v-if="dialogProps.action === 'add' && duplicateMatches.length > 0">
          <el-col :span="24">
            <el-alert
              title="Đã có bệnh nhân trùng khớp — chọn một bệnh nhân để dùng lại thay vì tạo trùng"
              type="warning"
              :closable="false"
              show-icon
              style="margin-bottom: 10px"
            ></el-alert>
            <el-table
              :data="duplicateMatches"
              size="mini"
              style="width: 100%; margin-bottom: 10px"
            >
              <el-table-column prop="name" label="Họ tên"></el-table-column>
              <el-table-column prop="phone" label="SĐT"></el-table-column>
              <el-table-column prop="card" label="CMND/CCCD"></el-table-column>
              <el-table-column label="Ngày sinh">
                <template slot-scope="scope">
                  {{ formatDuplicateBirthday(scope.row.birthday) }}
                </template>
              </el-table-column>
              <el-table-column label="Khớp theo">
                <template slot-scope="scope">
                  {{ scope.row.matchType === 'both' ? 'SĐT + CMND/CCCD' : (scope.row.matchType === 'phone' ? 'SĐT' : 'CMND/CCCD') }}
                </template>
              </el-table-column>
              <el-table-column label="" width="150">
                <template slot-scope="scope">
                  <el-button
                    size="mini"
                    type="primary"
                    @click="applyDuplicatePatient(scope.row)"
                  >Dùng bệnh nhân này</el-button>
                </template>
              </el-table-column>
            </el-table>
            <div style="text-align: right; margin-bottom: 10px">
              <el-button size="mini" @click="dismissDuplicates">Tạo bệnh nhân mới</el-button>
            </div>
          </el-col>
        </el-row>
        <!-- O2: prompt cảnh báo-only ở chế độ SỬA hồ sơ (phần còn lại của
          N1). KHÔNG có nút "Dùng bệnh nhân này": apply ở chế độ sửa sẽ
          thay toàn bộ bizFormModel bằng hồ sơ khác rồi savePatient cập
          nhật nhầm hồ sơ đó. Chỉ cảnh báo + giữ lại hoặc hoàn lại
          SĐT/CCCD cũ; không chặn lưu. -->
        <el-row v-if="dialogProps.action === 'edit' && duplicateMatches.length > 0">
          <el-col :span="24">
            <el-alert
              title="SĐT/CMND-CCCD này đã thuộc về bệnh nhân khác — lưu tiếp sẽ tạo 2 hồ sơ cùng liên hệ"
              type="warning"
              :closable="false"
              show-icon
              style="margin-bottom: 10px"
            ></el-alert>
            <el-table
              :data="duplicateMatches"
              size="mini"
              style="width: 100%; margin-bottom: 10px"
            >
              <el-table-column prop="name" label="Họ tên"></el-table-column>
              <el-table-column prop="phone" label="SĐT"></el-table-column>
              <el-table-column prop="card" label="CMND/CCCD"></el-table-column>
              <el-table-column label="Ngày sinh">
                <template slot-scope="scope">
                  {{ formatDuplicateBirthday(scope.row.birthday) }}
                </template>
              </el-table-column>
              <el-table-column label="Khớp theo">
                <template slot-scope="scope">
                  {{ scope.row.matchType === 'both' ? 'SĐT + CMND/CCCD' : (scope.row.matchType === 'phone' ? 'SĐT' : 'CMND/CCCD') }}
                </template>
              </el-table-column>
            </el-table>
            <div style="text-align: right; margin-bottom: 10px">
              <el-button size="mini" @click="restoreDuplicateOriginals">Hoàn lại SĐT/CCCD cũ</el-button>
              <el-button size="mini" type="primary" @click="dismissDuplicates">Đã hiểu, giữ SĐT/CCCD này</el-button>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-form>
    <span slot='footer' class='dialog-footer'>
      <el-button v-if='dialogProps.action !== "view"' :disabled="flag" type='primary' :plain='true'
                 @click='onSubmit("patientForm")'>Lưu
      </el-button>
      <el-button v-if='dialogProps.action !== "view"' :plain='true' @click='onDialogClose()'>Hủy</el-button>
      <el-button v-if='dialogProps.action === "view"' :plain='true' @click='onDialogClose()'>Đóng</el-button>
    </span>
  </el-dialog>
</template>
<script>
import { getDictItemsByCode, DICT_CODE } from '@/utils/dictCache'
import {listAdministrativeDivisionAll} from "@/api/org/administrativeDivision";
import {savePatient, lookupDuplicates, getPatientById} from "@/api/outpatient/patient";
import BaseUI from "@/views/components/baseUI";
import OperationIcon from "@/components/OperationIcon";
import VDistpicker from 'v-distpicker'

export default {
  extends: BaseUI,
  name: "patient-form",
  components: {
    OperationIcon,
    VDistpicker
  },
  data() {
    return {
      bizFormModel: this.initFormModel(),
      tabIndex: "1",
      gender_List: [], // Giới tính
      //Liên kết ba cấp tỉnh/thành phố/quận/huyện
      province: '',
      city: '',
      area: '',
      flag: false,//Ngăn gửi trùng lặp
      duplicateMatches: [],
      duplicateChecking: false,
      duplicateDismissedKey: "",
      duplicateAppliedKey: "",
      // O2: SĐT/CCCD gốc của hồ sơ đang sửa (để nút "Hoàn lại" khôi phục).
      duplicateOriginalPhone: "",
      duplicateOriginalCard: "",
      withPatientNexus_List: [], // Quan hệ với bệnh nhân
      dialogProps: {
        visible: false,
        action: '',
        title: '',
      },
      formRules: {
        name: [{required: true, message: "Vui lòng nhập tên bệnh nhân", trigger: "blur"}],
        "gender.value": [
          {required: true, message: "Vui lòng chọn giới tính", trigger: "change"},
        ],
        age: [{required: true, message: "Vui lòng nhập tuổi", trigger: "blur"}],
        month: [{required: true, message: "Vui lòng nhập tháng", trigger: "blur"}],
        // phone: [{ required: true, message: "Vui lòng nhập thông tin liên hệ", trigger: "blur" }],
        // "birthday": [
        //   {required: true, message: "Vui lòng chọn ngày sinh", trigger: "change"},
        // ],
        // "card": [
        //   { required: true, message: 'Vui lòng nhập số CMND/CCCD', trigger: 'blur' },
        //   { min:18,max:18, message: "Vui lòng nhập18chữ số của số CMND/CCCD", trigger: "blur" },
        //   {	//Gọi phương thức đã định nghĩa ở trên để kiểm tra định dạng có đúng không
        //            validator: isCnNewID,trigger: "blur"
        //   }
        // ],
        guardianName: [{required: false, message: "Vui lòng nhập tên người giám hộ", trigger: "change"},],
        guardianPhone: [{required: false, message: "Vui lòng nhập số điện thoại người giám hộ", trigger: "change"},]
      },
      province_List: [], // Tỉnh/Thành phố
      city_List: [], //Thành phố
      area_List: [] //Quận/Huyện
    };
  },
  props: {
    // Quyền
    permission: {
      type: Object,
    },
  },
  methods: {
    //Lấy ngày tháng năm sinh qua số CMND/CCCD
    // DS137: CCCD Việt Nam 12 số không mã hóa ngày sinh (chữ số thứ 4 chỉ
    // mã giới tính: chẵn Nam, lẻ Nữ); CMND 9 số cũng không mã hóa ngày sinh.
    // Không suy ngày sinh từ số giấy tờ nữa — nhánh 18 số cũ (layout CMND
    // Trung Quốc) ghi đè ngày sinh đã chọn, và xóa ô giấy tờ thì xóa luôn
    // ngày sinh + giới tính đã nhập, khiến bệnh nhân lưu với birthday NULL.
    getBirthday() {
      if (this.bizFormModel.card.length === 12) {
        // CCCD 12 số: chỉ suy giới tính từ chữ số thứ 4, không chạm ngày sinh.
        let sexDigit = parseInt(this.bizFormModel.card.substring(3, 4), 10)
        let sexName = sexDigit % 2 === 0 ? "Nam" : "Nữ";
        if (this.gender_List && this.gender_List.length > 0) {
          this.bizFormModel.gender = this.gender_List.find(item => {
            return item.name === sexName
          })
        }
      }
    },
    //Sự kiện thay đổi tuổi
    exchageAge() {
      if (this.bizFormModel.age < 12) {
        this.formRules.guardianName[0].required = true
        this.formRules.guardianPhone[0].required = true
        if (this.formRules.phone && this.formRules.phone[0]) {
          this.formRules.phone[0].required = false
        }

      } else {
        this.formRules.guardianName[0].required = false
        this.formRules.guardianPhone[0].required = false
        this.$refs["patientForm"].clearValidate();
        if (this.formRules.phone && this.formRules.phone[0]) {
          this.formRules.phone[0].required = true
        }
      }
    },

    //Sự kiện tự tính tuổi
    birthdayChanges() {

      if (!this.bizFormModel.birthday) {
        this.bizFormModel.age = "";
        this.bizFormModel.month = ""
        return
      }
      
      const duration = this.$moment.duration(this.$moment().diff(this.bizFormModel.birthday));
      this.bizFormModel.age = duration.years();
      this.bizFormModel.month = duration.months();
      if (this.bizFormModel.age < 12) {
        this.formRules.guardianName[0].required = true
        this.formRules.guardianPhone[0].required = true
        //this.formRules.phone[0].required=false

      } else {
        this.formRules.guardianName[0].required = false
        this.formRules.guardianPhone[0].required = false
        this.$refs["patientForm"].clearValidate();
        //this.formRules.phone[0].required=true
      }
    },

    changeProvince() {
      if (!this.bizFormModel.province) return;
      this.bizFormModel.city = '';
      this.bizFormModel.area = '';
      this.area_List = [];
      let division_search = {
        params: [
          {
            columnName: "parten_id",
            queryType: "=",
            value: this.bizFormModel.province,
          },
          {
            columnName: "level",
            queryType: "=",
            value: "2",
          }
        ],
      };

      listAdministrativeDivisionAll(division_search).then((responseData) => {
        this.city_List = responseData.data;
      });
    },
    changeCity() {
      if (!this.bizFormModel.city) return;

      this.bizFormModel.area = '';
      let division_search = {
        params: [
          {
            columnName: "parten_id",
            queryType: "=",
            value: this.bizFormModel.city,
          },
          {
            columnName: "level",
            queryType: "=",
            value: "3",
          }
        ],
      };

      listAdministrativeDivisionAll(division_search).then((responseData) => {
        this.area_List = responseData.data;
      });
    },
    birthdayChange() {

      if (!this.bizFormModel.birthday) return;
      const duration = this.$moment.duration(this.$moment().diff(this.bizFormModel.birthday));
      this.bizFormModel.age = duration.years();
      this.bizFormModel.month = duration.months();
      if (this.bizFormModel.age < 12) {
        this.formRules.guardianName[0].required = true
        this.formRules.guardianPhone[0].required = true
        if (this.formRules.phone && this.formRules.phone[0]) {
          this.formRules.phone[0].required = false
        }

      } else {
        this.formRules.guardianName[0].required = false
        this.formRules.guardianPhone[0].required = false
        this.$refs["patientForm"].clearValidate();
        if (this.formRules.phone && this.formRules.phone[0]) {
          this.formRules.phone[0].required = true
        }
      }
    },
    // N1: tra trùng khi rời khỏi ô SĐT hoặc CMND/CCCD (chỉ ở chế độ thêm
    // mới, chưa có id). Tái dùng lookupDuplicates của G3; lỗi tra cứu
    // (kể cả 403) không chặn lưu: prompt chỉ ẩn đi, luồng cũ chạy như trước.
    // O2: mở thêm cho chế độ sửa — nhưng phải lọc hồ sơ đang sửa ra khỏi
    // kết quả (findDuplicates trả mọi hồ sơ khớp, kể cả chính nó), nếu
    // không hồ sơ sẽ tự báo trùng với chính mình khi SĐT/CCCD không đổi.
    duplicateLookupKey() {
      const phone = (this.bizFormModel.phone || "").replace(/[^0-9]/g, "");
      const card = (this.bizFormModel.card || "").replace(/[^0-9]/g, "");
      return phone + "|" + card;
    },
    checkDuplicates() {
      const action = this.dialogProps.action;
      if ((action !== "add" && action !== "edit") || this.duplicateChecking) {
        return;
      }
      if (action === "add" && this.bizFormModel.id) {
        return;
      }
      const key = this.duplicateLookupKey();
      const phone = (this.bizFormModel.phone || "").trim();
      const card = (this.bizFormModel.card || "").trim();
      if (!phone && !card) {
        this.duplicateMatches = [];
        return;
      }
      if (key === this.duplicateDismissedKey || key === this.duplicateAppliedKey) {
        return;
      }
      this.duplicateChecking = true;
      lookupDuplicates({ phone: phone, card: card })
        .then((responseData) => {
          this.duplicateChecking = false;
          if (this.duplicateLookupKey() !== key) {
            return;
          }
          if (responseData.code === 100 && Array.isArray(responseData.data)) {
            if (key !== this.duplicateDismissedKey && key !== this.duplicateAppliedKey) {
              // O2: ở chế độ sửa, loại hồ sơ đang sửa khỏi kết quả
              // (so sánh dạng chuỗi vì id có thể là số hoặc chuỗi).
              const selfId = this.bizFormModel.id;
              this.duplicateMatches = responseData.data.filter((row) => {
                return selfId === null || selfId === undefined || String(row.id) !== String(selfId);
              });
            }
          }
        })
        .catch(() => {
          this.duplicateChecking = false;
          this.duplicateMatches = [];
        });
    },
    dismissDuplicates() {
      this.duplicateDismissedKey = this.duplicateLookupKey();
      this.duplicateMatches = [];
    },
    // O2: hoàn lại SĐT/CCCD gốc của hồ sơ đang sửa, rồi xóa prompt.
    // Lần blur sau sẽ tra lại nhưng chỉ còn chính hồ sơ này nên bị lọc,
    // không hiện prompt nữa.
    restoreDuplicateOriginals() {
      this.bizFormModel.phone = this.duplicateOriginalPhone;
      this.bizFormModel.card = this.duplicateOriginalCard;
      this.duplicateMatches = [];
    },
    formatDuplicateBirthday(birthday) {
      if (!birthday) {
        return "";
      }
      const parts = birthday.split("-");
      if (parts.length === 3) {
        return parts[2] + "/" + parts[1] + "/" + parts[0];
      }
      return birthday;
    },
    // N1: nạp lại hồ sơ đầy đủ của bệnh nhân đã chọn (getPatientById) thay
    // vì chỉ chép các trường của DTO trùng khớp — tránh ghi đè một phần
    // lên hồ sơ có sẵn khi lưu (savePatient đi nhánh cập nhật nhờ id).
    applyDuplicatePatient(patient) {
      getPatientById(patient.id).then((responseData) => {
        if (responseData.code === 100 && responseData.data) {
          this.duplicateAppliedKey = this.duplicateLookupKey();
          this.duplicateMatches = [];
          this.bizFormModel = {...this.initFormModel(), ...responseData.data};
          this.province = this.bizFormModel.province;
          this.city = this.bizFormModel.city;
          this.area = this.bizFormModel.area;
        }
      }).catch(() => {
        this.outputError({ message: "Không nạp được hồ sơ bệnh nhân đã chọn" });
      });
    },
    resetDuplicates() {
      this.duplicateMatches = [];
      this.duplicateDismissedKey = "";
      this.duplicateAppliedKey = "";
      this.duplicateOriginalPhone = "";
      this.duplicateOriginalCard = "";
    },
    onSubmit(formName) {
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
    doSave() {
      let flagCard = false
      //Kiểm tra số CMND/CCCD: CMND 9 số, CCCD 12 số của Việt Nam luôn hợp lệ;
      //CMND 18 số kiểu Trung Quốc vẫn kiểm tra checksum có trọng số như trước.
      if (this.bizFormModel.card) {
        if (/^\d{9}$/.test(this.bizFormModel.card) || /^\d{12}$/.test(this.bizFormModel.card)) {
          flagCard = true
        } else {
          const arrExp = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2];//Hệ số trọng số
          const arrValid = [1, 0, "X", 9, 8, 7, 6, 5, 4, 3, 2];//Mã kiểm tra
          if (/^\d{17}[\dX]$/i.test(this.bizFormModel.card)) {
            let sum = 0, idx;
            for (let i = 0; i < this.bizFormModel.card.length - 1; i++) {
              // Đối với17chữ số đầu nhân với trọng số rồi cộng lại
              sum += parseInt(this.bizFormModel.card.substr(i, 1), 10) * arrExp[i];
            }
            // Tính modulo (Thuật toán cố định)
            idx = sum % 11;
            // Kiểm tra chữ số thứ18có bằng mã kiểm tra không
            if (String(arrValid[idx]) === this.bizFormModel.card.substr(17, 1).toUpperCase()) {
              flagCard = true
            } else {
              flagCard = false
            }
          } else {
            flagCard = false
          }
        }
      }

      if (!flagCard && this.bizFormModel.card) {
        this.$message.error("Số CMND/CCCD không đúng, vui lòng nhập CMND 9 số, CCCD 12 số hoặc CMND 18 số")
        this.flag = false
        return;
      }

      this.setLoad()
      this.bizFormModel.province = this.province
      this.bizFormModel.city = this.city
      this.bizFormModel.area = this.area
      // this.bizFormModel.id=""
      savePatient(this.bizFormModel).then(responseData => {
        this.flag = false
        if (responseData.code === 100) {
          this.dialogProps.visible = false
          this.$emit('save-finished')
        } else {
          this.flag = false
          this.showMessage(responseData)
        }
        this.resetLoad()
      }).catch(error => {
        this.flag = false
        this.outputError(error)
      })
    },
    switchEdit() {
      // O2: chuyển từ xem sang sửa cũng phải chụp SĐT/CCCD gốc để nút
      // "Hoàn lại" có giá trị khôi phục.
      this.duplicateOriginalPhone = this.bizFormModel.phone || "";
      this.duplicateOriginalCard = this.bizFormModel.card || "";
      this.dialogProps.action = "edit";
      this.dialogProps.title = "Sửa thông tin bệnh nhân";
      this.initOptions(this.bizFormModel);
    },
    onDialogClose() {
      this.dialogProps.visible = false;
    },
    onDialogOpen() {
      this.$nextTick(() => {
        this.$refs["patientForm"].clearValidate();
      });
    },
    initFormModel() {
      return {
        company: {
          // Phòng khámid
          id: currentUser.company.id,
          name: null,
        },
        name: "", // Tên bệnh nhân
        gender: {
          // Giới tính
          value: null,
          name: null,
        },
        age: "", // Tuổi
        month: "", // tháng
        birthday: "", // Ngày sinh
        phone: "", // Thông tin liên hệ
        card: "", // Số CMND/CCCD
        withPatientNexus: {
          // Quan hệ với bệnh nhân
          value: null,
          name: null,
        },
        healthCardNo: "", // Số thẻ sức khỏe
        province: "", // Tỉnh/Thành phố
        city: "", // Thành phố
        area: "", // Quận/Huyện
        address: "", // Địa chỉ chi tiết
        remarks: "", // Thông tin ghi chú
      };
    },
    initOptions() {
      let division_search = {
        params: [
          {
            columnName: "parten_id",
            queryType: "=",
            value: "0",
          },
          {
            columnName: "level",
            queryType: "=",
            value: "1",
          }
        ],
      }

      listAdministrativeDivisionAll(division_search).then((responseData) => {
        this.province_List = responseData.data;
      });

      getDictItemsByCode(DICT_CODE.GENDER).then((data) => {
        this.gender_List = data;
      });
      getDictItemsByCode(DICT_CODE.WITH_PATIENT_NEXUS).then((data) => {
        this.withPatientNexus_List = data;
      })
    },
    // Lấy giá trị liên kết ba cấp tỉnh/thành phố/quận/huyện
    onChangeProvince(data) {
      this.province = data.value

    },
    onChangeCity(data) {

      this.city = data.value

    },
    onChangeArea(data) {

      this.area = data.value

    },
    openViewPatientDialog(patient) {
      this.resetDuplicates()
      this.dialogProps.action = 'view'
      this.dialogProps.title = 'Xem thông tin bệnh nhân'
      this.bizFormModel = {...this.initFormModel(), ...patient}
      this.initOptions(this.bizFormModel)
      this.tabIndex = '1'
      this.dialogProps.visible = true
      this.province = this.bizFormModel.province
      this.city = this.bizFormModel.city
      this.area = this.bizFormModel.area
    },
    openEditPatientDialog(patient) {
      this.resetDuplicates()
      this.dialogProps.action = 'edit'
      this.dialogProps.title = 'Sửa thông tin bệnh nhân'
      this.bizFormModel = {...this.initFormModel(), ...patient}
      // O2: chụp SĐT/CCCD gốc trước khi người dùng sửa.
      this.duplicateOriginalPhone = this.bizFormModel.phone || "";
      this.duplicateOriginalCard = this.bizFormModel.card || "";
      this.initOptions(this.bizFormModel)
      this.tabIndex = '1'
      this.dialogProps.visible = true
      this.province = this.bizFormModel.province
      this.city = this.bizFormModel.city
      this.area = this.bizFormModel.area
    },
    openAddPatientDialog() {
      this.resetDuplicates()
      this.dialogProps.action = 'add'
      this.dialogProps.title = 'Thêm thông tin bệnh nhân'
      this.bizFormModel = this.initFormModel(this.user)
      this.initOptions(this.bizFormModel)
      this.tabIndex = '1'
      this.dialogProps.visible = true
      this.province = ''
      this.city = ''
      this.area = ''
    },
    openCopyPatientDialog(patient) {
      this.resetDuplicates()
      this.dialogProps.action = 'add'
      this.dialogProps.title = 'Thêm thông tin bệnh nhân'
      this.bizFormModel = {...this.initFormModel(this.user), ...patient}
      this.initOptions(this.bizFormModel)
      this.tabIndex = '1'
      this.bizFormModel.id = null
      this.dialogProps.visible = true
    },
  },
  watch: {
    'bizFormModel.age': function () {
      if (this.bizFormModel.age < 12) {
        this.formRules.guardianName[0].required = true
        this.formRules.guardianPhone[0].required = true
      } else {
        this.formRules.guardianName[0].required = false
        this.formRules.guardianPhone[0].required = false
      }
    }
  },
  mounted: function () {
  },
}
</script>
<style lang="scss">
.distpicker-address-wrapper {
  select {
    padding: 0px 10px !important;
    height: 30px !important;
    font-size: 15px !important;
    line-height: 30px !important;
  }
}
</style>
