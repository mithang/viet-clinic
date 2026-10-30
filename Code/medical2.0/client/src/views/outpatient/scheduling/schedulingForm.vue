<template>
  <el-dialog
    class="testForm"
    :title="dialogProps.title"
    :visible.sync="dialogProps.visible"
    :close-on-click-modal="false"
    width="50%"
    @open="onDialogOpen()"
    v-loading="loading"
  >

    <el-form
      :model="bizFormModel"
      :rules="formRules"
      ref="testForm"
      label-width="120px"
      label-position="right"
      style="marginTop: 10px"
      class="edit-form"
    >
      <div class="tab-item" v-show="tabIndex === '1'">
        <div class="registrationItemBox">
          <h3>Thông tin cơ bản</h3>
          <el-row>
            <el-col :span="24 / 2">
              <el-form-item label="Bệnh nhân" prop="name">
                <el-input
                  v-if="dialogProps.action !== 'add'"
                  :disabled="true"
                  v-model="bizFormModel.name"
                ></el-input>
                <el-select
                  v-else
                  v-model="bizFormModel.name"
                  value-key="id"
                  filterable
                  allow-create
                  @change="GetPatientInfo()"
                  placeholder="Vui lòng chọn bệnh nhân"
                >
                  <el-option
                    v-for="patientId in patientId_List"
                    :key="patientId.id"
                    :label="patientId.name"
                    :value="patientId"
                  >
                    <span style="float: left">{{ patientId.name }}</span>
                    <span
                      style="float: right; color: #8492a6; font-size: 13px"
                    >{{ patientId.phone }}</span
                    >
                  </el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="24 / 2">
              <el-form-item label="Giới tính" prop="gender.name">
                <el-input
                  v-if="dialogProps.action !== 'add'"
                  :disabled="true"
                  v-model="bizFormModel.gender.name"
                ></el-input>
                <el-select
                  v-else
                  v-model="bizFormModel.gender"
                  value-key="value"
                  filterable
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
                  :disabled="dialogProps.action !== 'add'"
                  v-model="bizFormModel.birthday"
                  type="date"
                  @change="birthdayChanges"
                  :picker-options="setDisabled"
                  value-format="yyyy-MM-dd"
                  format="dd/MM/yyyy"
                  :placeholder="
                    dialogProps.action === 'view' ? '' : 'Vui lòng nhập ngày sinh'
                  "

                ></el-date-picker>

              </el-form-item>
            </el-col>
            <el-col :span="24 / 2">
              <el-form-item label="Tuổi" prop="age">
                <el-col :span="24 / 2">
                  <el-input

                    v-model="bizFormModel.age"
                    :maxlength="45"
                    :placeholder="
                      dialogProps.action === 'view' ? '' : 'Tuổi'
                    "
                  >
                    <template slot="append">tuổi</template>
                  </el-input>
                </el-col>
                <el-col :span="24 / 2">
                  <el-input
                    v-model="bizFormModel.month"
                    :maxlength="45"
                    :placeholder="dialogProps.action === 'view' ? '' : 'tháng'"
                  >
                    <template slot="append">tháng</template>
                  </el-input>
                </el-col>
              </el-form-item>
            </el-col>

          </el-row>
          <el-row>
            <el-col :span="24 / 2">
              <el-form-item label="Số CMND/CCCD" prop="card">
                <el-input
                  :disabled="dialogProps.action !== 'add'"
                  v-model="bizFormModel.card"
                  :maxlength="18"
                  :placeholder="
                    dialogProps.action === 'view' ? '' : 'Vui lòng nhập số CMND/CCCD'
                  "
                  @input="getBirthday"
                ></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="24 / 2">
              <el-form-item label="Thông tin liên hệ" prop="phone">
                <el-input
                  :disabled="dialogProps.action !== 'add'"
                  v-model="bizFormModel.phone"
                  :maxlength="45"
                  :placeholder="
                    dialogProps.action === 'view' ? '' : 'Vui lòng nhập thông tin liên hệ'
                  "
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

          </el-row>
        </div>
        <div class="registrationItemBox">
          <h3>Thông tin đăng ký</h3>
          <el-row>
            <el-col :span="24 / 2">
              <el-form-item label="Khoa" prop="clinicOffice.id">
                <el-input
                  v-if="dialogProps.action === 'view'||dialogProps.action === 'add'"
                  :disabled="true"
                  v-model="bizFormModel.clinicOffice.name"
                ></el-input>
                <el-cascader
                  v-else
                  v-model="bizFormModel.clinicOffice.id"
                  @change="sortDoc"
                  :options="department_List"
                  :key="key_department"
                  :disabled="bizFormModel.clinicOffice.id!=null?true:false"
                  :props="{
                    value: 'id',
                    label: 'name',
                    checkStrictly: true,
                    emitPath: false,
                  }"
                  filterable
                  clearable
                  placeholder="Vui lòng chọn khoa/phòng"
                />
              </el-form-item>
            </el-col>
            <el-col :span="24 / 2">
              <el-form-item label="Bác sĩ" prop="doctor.name">
                <el-input
                  v-if="dialogProps.action === 'view'||dialogProps.action === 'add'"
                  :disabled="true"
                  v-model="bizFormModel.doctor.name"
                ></el-input>
                <el-select
                  v-else
                  v-model="bizFormModel.doctor"
                  value-key="id"
                  :disabled="bizFormModel.doctor.id!=null?true:false"
                  filterable
                  clearable
                  placeholder="Vui lòng chọn bác sĩ"
                  @clear="
                    bizFormModel.doctor = {
                      id: null,
                      name: null,
                    }
                  "
                >
                  <el-option
                    v-for="doctor in this.select_doctor"
                    :key="doctor.id"
                    :label="doctor.name"
                    :value="doctor"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-col>

          </el-row>
          <el-row>


            <el-col :span="24 / 2">
              <el-form-item label="Miễn phí đăng ký khám" prop="freeRegistrationFee">
                <el-switch
                  :disabled="dialogProps.action === 'view'"
                  v-model="bizFormModel.freeRegistrationFee"
                  active-color="#13ce66"
                  inactive-color="#dbdfe6"
                  active-value="1"
                  inactive-value="0"
                ></el-switch>
              </el-form-item>
            </el-col>
            <el-col :span="24 / 2">
              <el-form-item
                label="Phí đăng ký khám"
                prop="registrationFee"
                :rules="
                  bizFormModel.freeRegistrationFee === '0'
                    ? formRules.registrationFee
                    : [
                        {
                          required: false,
                        },
                      ]
                "
              >
                <el-input
                  v-if="dialogProps.action === 'view'"
                  :disabled="true"
                  v-model="bizFormModel.registrationFee"
                ></el-input>
                <number-input
                  v-else
                  v-model="bizFormModel.registrationFee"
                  currency="VND"
                  :precision="2"
                  :disabled="bizFormModel.freeRegistrationFee === '1'"
                ></number-input>
              </el-form-item>
            </el-col>
          </el-row>
        </div>
        <div class="registrationItemBox">
          <h3>Thông tin khác</h3>
          <el-row>
            <el-col :span="24 / 2">
              <el-form-item label="Nhiệt độ cơ thể" prop="temperature">
                <div class="ipt-box">
                  <el-input
                    :disabled="dialogProps.action === 'view'"
                    v-model="bizFormModel.temperature"
                    :maxlength="45"
                    :placeholder="
                      dialogProps.action === 'view' ? '' : 'Vui lòng nhập nhiệt độ cơ thể'
                    "
                  ></el-input>
                  <span>&nbsp;℃</span>
                </div>
              </el-form-item>
            </el-col>
            <el-col :span="24 / 2">
              <el-form-item label="Mạch" prop="pulse">
                <div class="ipt-box">
                  <el-input
                    :disabled="dialogProps.action === 'view'"
                    v-model="bizFormModel.pulse"
                    :maxlength="45"
                    :placeholder="
                      dialogProps.action === 'view' ? '' : 'Vui lòng nhập mạch'
                    "
                  ></el-input>
                  <span>&nbsp;lần/min</span>
                </div>
              </el-form-item>
            </el-col>
            <el-col :span="24 / 2">
              <el-form-item label="Nhịp thở" prop="breathe">
                <div class="ipt-box">
                  <el-input
                    :disabled="dialogProps.action === 'view'"
                    v-model="bizFormModel.breathe"
                    :maxlength="45"
                    :placeholder="
                      dialogProps.action === 'view' ? '' : 'Vui lòng nhập nhịp thở'
                    "
                  ></el-input>
                  <span>&nbsp;lần/min</span>
                </div>
              </el-form-item>
            </el-col>
            <el-col :span="24 / 2">
              <el-form-item label="Huyết áp" prop="bloodPressure">
                <div class="ipt-box">
                  <el-input
                    :disabled="dialogProps.action === 'view'"
                    v-model="bizFormModel.bloodPressure"
                    :maxlength="45"
                    :placeholder="
                      dialogProps.action === 'view' ? '' : 'Vui lòng nhập huyết áp'
                    "
                  ></el-input>
                  <span>&nbsp;mmHg</span>
                </div>
              </el-form-item>
            </el-col>

          </el-row>
          <el-row>
            <el-col :span="24 / 2">
              <el-form-item label="Có từng đến vùng dịch không" prop="isGoHigharea">
                <el-switch
                  :disabled="dialogProps.action === 'view'"
                  v-model="bizFormModel.isGoHigharea"
                  active-color="#13ce66"
                  inactive-color="#dbdfe6"
                  active-value="1"
                  inactive-value="0"
                ></el-switch>
              </el-form-item>
            </el-col>
            <el-col :span="24 / 2">
              <el-form-item
                label="Địa điểm vùng dịch"
                prop="highArea"
                :rules="
                  bizFormModel.isGoHigharea === '1'
                    ? formRules.highArea
                    : [
                        {
                          required: false,
                        },
                      ]
                "
              >
                <el-input
                  :disabled="
                    dialogProps.action === 'view' ||
                    bizFormModel.isGoHigharea === '0'
                  "
                  v-model="bizFormModel.highArea"
                  :maxlength="128"
                  :placeholder="
                    dialogProps.action === 'view' ? '' : 'Vui lòng nhập địa điểm vùng dịch'
                  "
                ></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24 / 2">
              <el-form-item
                label="Ngày hẹn" prop="subscribedaterq"
              >
                <el-input
                  :disabled="true"
                  v-model="bizFormModel.subscribedaterq"
                  :maxlength="45"
                  :placeholder="dialogProps.action === 'view' ? '' : 'Nhập ngày hẹn'"
                ></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="24 / 2">
              <el-form-item label="Thời gian hẹn" prop="subscribedatetime">
                <el-time-select
                  v-model="bizFormModel.subscribedatetime"
                  :picker-options="{
                    start: listarry.streatime,
                    step: '00:30',
                    end: listarry.endtime
                  }"
                  placeholder="Chọn thời gian">
                </el-time-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24">
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
        </div>
      </div>
    </el-form>
    <span slot='footer' class='dialog-footer'>
      <el-button v-if='dialogProps.action !== "view"' type='primary' :disabled="flag" :plain='true'
                 @click='onSubmit("testForm")'>Lưu
      </el-button>
      <el-button v-if='dialogProps.action !== "view"' :plain='true' @click='onDialogClose()'>Hủy</el-button>
      <el-button v-if='dialogProps.action === "view"' :plain='true' @click='onDialogClose()'>Đóng</el-button>
    </span>

    <!--Hộp thoại thanh toán thu phí Bắt đầu-->
    <el-dialog
      title="Thu phí"
      :visible.sync="payRegistrationFeeVisible"
      width="40%"
      :close-on-click-modal="false"
      :before-close="dialogBeforeClose"
      append-to-body
    >
      <div>
        <el-divider></el-divider>
        <div id="registrationTotalFee_Container">
          <div id="registrationTotalFee_Title">Số tiền phải thu（VNĐ）：</div>
          <div id="registrationTotalFee_Fee">
            {{ this.bizFormModel.registrationFee }}
          </div>
        </div>
        <div class="pay-registration-flex-start">
          <p class="pay-registration-p">Số tiền thực thu</p>
          <el-input
            v-model="bizFormModel.practicleRegistrationFee"
            placeholder="Vui lòng nhập số tiền thực thu"
            type="number"
            class="pay-registration-content"
          ></el-input>
        </div>
        <div class="pay-registration-flex-start">
          <p class="pay-registration-p">Phương thức thanh toán</p>
          <el-radio-group
            v-model="bizFormModel.payType.value"
            class="pay-registration-content"
          >
            <el-radio
              v-for="ptype in payType_List"
              :key="ptype.value"
              :label="ptype.value"
            >{{ ptype.name }}
            </el-radio
            >
          </el-radio-group>
        </div>
        <div class="pay-registration-flex-start">
          <p class="pay-registration-p">Ghi chú</p>
          <el-input
            type="textarea"
            :rows="3"
            v-model="bizFormModel.practicalRegistrationPayRemarks"
            class="pay-registration-content"
          ></el-input>
        </div>
        <el-divider></el-divider>
      </div>
      <div slot="footer">
        <el-button @click="payRegistrationFeeVisible = false">Hủy</el-button>
        <el-button type="primary" @click="payRegistrationFeeClick"
        >Xác nhận thu phí
        </el-button>
      </div>
    </el-dialog>
    <!--Hộp thoại thanh toán thu phí Kết thúc-->
  </el-dialog>
</template>

<script>

import BaseUI from "@/views/components/baseUI";
import {saveTollInfo} from "@/api/toll/tollInfo";
import {validatenull} from "@/utils/validate";
import {getPatientById, listPatientAll, savePatient,} from "@/api/outpatient/patient";
import {listDictItemAll} from "@/api/sys/dictItem";
import { getDictItemsByCode, DICT_CODE } from '@/utils/dictCache'
import {listDoctorsAll, saveRegistration} from "@/api/outpatient/registration";

export default {
  extends: BaseUI,
  name: "test-form",
  components: {},
  data() {
    //Kiểm tra CMND/CCCD: chấp nhận CMND 9 số và CCCD 12 số của Việt Nam;
    //CMND 18 số kiểu Trung Quốc vẫn được chấp nhận để tương thích dữ liệu cũ
    //(kiểm tra checksum có trọng số như trước).
    const isCnNewID = (rule, value, callback) => {
      if (!value) {
        callback()
        return
      }
      if (/^\d{9}$/.test(value) || /^\d{12}$/.test(value)) {
        callback()
        return
      }
      const arrExp = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2];//Hệ số trọng số
      const arrValid = [1, 0, "X", 9, 8, 7, 6, 5, 4, 3, 2];//Mã kiểm tra
      if (/^\d{17}[\dX]$/i.test(value)) {
        let sum = 0, idx;
        for (let i = 0; i < value.length - 1; i++) {
          // Đối với17chữ số đầu nhân với trọng số rồi cộng lại
          sum += parseInt(value.substr(i, 1), 10) * arrExp[i];
        }
        // Tính modulo（Thuật toán cố định）
        idx = sum % 11;
        // Kiểm tra chữ số thứ18có bằng mã kiểm tra không
        if (String(arrValid[idx]) === value.substr(17, 1).toUpperCase()) {
          callback()
        } else {
          callback("Định dạng CMND/CCCD không đúng, vui lòng nhập CMND 9 số, CCCD 12 số hoặc CMND 18 số")
        }
      } else {
        callback("Định dạng CMND/CCCD không đúng, vui lòng nhập CMND 9 số, CCCD 12 số hoặc CMND 18 số")
      }
    }
    return {
      setDisabled: {
        disabledDate(time) {
          return time.getTime() > Date.now(); // Có thể chọn ngày trước、Có thể chọn ngày hiện tại、Không thể chọn ngày tương lai
          // return time.getTime() > Date.now() - 8.64e7;  // Có thể chọn ngày trước、Không thể chọn ngày hiện tại、Không thể chọn ngày tương lai
          // return time.getTime() < Date.now() - 8.64e7;  // Không thể chọn ngày trước、Có thể chọn ngày hiện tại、Có thể chọn ngày tương lai
          // return time.getTime() < Date.now(); // Không thể chọn ngày trước、Không thể chọn ngày hiện tại、Có thể chọn ngày tương lai

          /*Chỉ chọn được 7 ngày gần đây trước hôm nay*/
          // let now = Date.now();
          // let seven = 7 * 8.64e7;
          // let sevenDays = now - seven;
          // return time.getTime() > now || time.getTime() < sevenDays;  //Vô hiệu hóa ngày lớn hơn hôm nay và nhỏ hơn 7 ngày trước
        },
      },

      payRegistrationFeeVisible: false, //Hiển thị cửa sổ thu phí
      bizFormModel: {...this.initFormModel(), ...this.initPatientModel()},
      PatientFormModel: this.initPatientModel(),
      tabIndex: "1",
      listarry: {streatime: '', endtime: ''},//父级传值接收使用
      flag: false,//Ngăn gửi trùng lặp
      gender_List: [], // Giới tính
      withPatientNexus_List: [], // Quan hệ với bệnh nhân
      key_department: 1, // el-cascader key
      department_List: [], // Khoa
      patientId_List: [], // Bệnh nhân
      doctor_List: [], // Bác sĩ
      select_doctor: [],
      treatType_List: [], // Loại điều trị
      source_List: [], // Nguồn
      payType_List: [], // Phương thức thanh toán
      status_List: [], // Trạng thái
      dialogProps: {
        visible: false,
        action: '',
        title: '',
        source: {
          name: 'Tại chỗ',
          value: 'registrationSource_0'
        }
      },
      formRules: {
        registrationFee: [
          {required: true, message: "Vui lòng nhập phí đăng ký khám", trigger: "blur"},
        ],
        highArea: [
          {required: true, message: "Vui lòng nhập địa điểm vùng dịch", trigger: "blur"},
        ],
        "doctor.name": [
          {required: true, message: "Vui lòng chọn bác sĩ", trigger: "change"},
        ],
        "birthday": [
          {required: true, message: "Vui lòng chọn ngày sinh", trigger: "change"},
        ],
        //phone: [{ required: false, message: "Vui lòng nhập thông tin liên hệ", trigger: "blur" }],
        age: [{required: true, message: "Vui lòng nhập tuổi", trigger: "blur"}],
        name: [{required: true, message: "Vui lòng nhập họ tên", trigger: "change"}],
        "gender.name": [
          {required: true, message: "Vui lòng nhập giới tính", trigger: "change"},
        ],
        guardianName: [{required: false, message: "Vui lòng nhập tên người giám hộ", trigger: "change"},],
        guardianPhone: [{required: false, message: "Vui lòng nhập số điện thoại người giám hộ", trigger: "change"},],
        "subscribedatetime": [
          {required: true, message: "Vui lòng chọn thời gian hẹn", trigger: "change"},
        ],
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
    getBirthday() {

      if (this.bizFormModel.card.length === 18) {

        let birthDay = this.bizFormModel.card.substring(6, 14)
        let year = birthDay.substring(0, 4)
        let month = birthDay.substring(4, 6)
        let day = birthDay.substring(6)
        let newBirthday = year + "-" + month + "-" + day
        this.bizFormModel.birthday = newBirthday
        this.birthdayChanges()
      } else if (this.bizFormModel.card.length === 0) {
        this.bizFormModel.birthday = ""
        this.birthdayChanges()
      }
    },
    sortDoc(clinicOffice, officename) {
      if (this.doctor_List.length !== 0) {
        //Lọc bác sĩ
        this.select_doctor = this.doctor_List.filter(doctor => doctor.office === clinicOffice)
      }
    },
    GetPatientInfo() {
      //Đăng ký khámidKhông thể bị ghi đè
      const id = this.bizFormModel.id;
      if (this.bizFormModel.name.id) {
        this.PatientFormModel = this.bizFormModel.name;
        this.bizFormModel = {...this.bizFormModel, ...this.PatientFormModel};
      } else {
        this.PatientFormModel = {
          ...this.initPatientModel(),
          name: this.bizFormModel.name,
        };
        this.bizFormModel = {...this.bizFormModel, ...this.PatientFormModel};
      }
      this.bizFormModel.id = id;
    },
    savePatient() {
      this.flag = false
      const registerId = this.bizFormModel.id;
      this.bizFormModel.id = this.PatientFormModel.id;
      if (this.bizFormModel.id !== undefined) {
        this.bizFormModel.patientId = {
          id: this.bizFormModel.id,
          name: this.PatientFormModel.name,
        };
        this.bizFormModel.id = registerId;

        this.doSave();
        return;
      }

      savePatient(this.bizFormModel)
        .then((responseData) => {
          this.flag = false
          if (responseData.code === 100) {
            this.bizFormModel.patientId = {
              id: responseData.data,
              name: this.PatientFormModel.name,
            };
            this.bizFormModel.id = registerId;
            this.doSave();
          } else {
            this.flag = false
            this.showMessage(responseData);
          }
          this.resetLoad();
        })
        .catch((error) => {
          this.flag = false
          this.outputError(error);
        });
    },
    payRegistrationFeeClick() {
      this.savePatient();
    },
    onSubmit(formName) {
      this.flag = true
      for (let i = 0; i < this.treatType_List.length; i++) {
        if (this.bizFormModel.treatType.name === this.treatType_List[i].name) {
          this.bizFormModel.treatType.value = this.treatType_List[i].value
        }

      }
      this.bizFormModel.subscribeDate = this.bizFormModel.subscribedaterq + " " + this.bizFormModel.subscribedatetime;
      this.$refs[formName].validate(valid => {
        if (valid) {
          if (
            this.bizFormModel.freeRegistrationFee === '1'
          ) {
            this.savePatient();
          } else {
            this.flag = false
            if (this.bizFormModel.registrationFee === 0) {
              this.$message({
                showClose: true,
                message: 'Vui lòng điền phí đăng ký khám',
                type: 'error'
              });
              return
            }

            if (this.payType_List.length > 0) {
              this.bizFormModel.payType.value = this.payType_List[0].value;
            }
            this.bizFormModel.practicleRegistrationFee =
              this.bizFormModel.registrationFee;
            this.bizFormModel.practicalRegistrationPayRemarks = "";
            this.payRegistrationFeeVisible = true;
          }
        } else {
          this.flag = false
          return false

        }
      });
    },
    doSave() {
      this.flag = false
      this.setLoad();
      if (this.bizFormModel.source.value === "registrationSourc_1") {
        this.bizFormModel.status.value = "registrationStatus_3";
        this.bizFormModel.status.name = "Chờ check-in";
      }
      this.payType_List.forEach(item => {
        if (item.value === this.bizFormModel.payType.value) {
          this.bizFormModel.payType.name = item.name
        }
      })
      let toll = {
        company: this.bizFormModel.company,
        registrationFeeId: '',
        amountReceivable: this.bizFormModel.registrationFee, //Phải thu/phải trả
        amountReceived: this.bizFormModel.registrationFee,  //Thực thu
        paymentType: this.bizFormModel.payType,//Loại thanh toán
        patient: this.bizFormModel.patientId,
        amountStatus: {
          name: 'Đã thu phí',
          value: 'amountStatus_1'
        },    //Trạng thái chứng từ
        tollType: {
          name: 'Đăng ký khám',
          value: 'tollType_4'
        },    //Loại thu phí
        remarks: this.bizFormModel.remarks   //Thông tin ghi chú
      }

      saveRegistration(this.bizFormModel)
        .then((responseData) => {
          this.flag = false
          if (responseData.code === 100) {
            this.dialogProps.visible = false;
            this.payRegistrationFeeVisible = false;
            //Sau khi lưu thành công，Nếu có phí đăng ký khám，Chèn dữ liệu vào bảng thu phí
            if (this.bizFormModel.registrationFee > 0) {
              toll.registrationFeeId = responseData.data
              //Chèn dữ liệu vào bảng thu phí
              saveTollInfo(toll).then((res) => {
                if (res.code === 100) {

                }
              }).catch(() => {
              })
            }
            setTimeout(() => {
              this.$emit("save-finished");
            }, 1000);
          } else {

            this.showMessage(responseData);
          }
          this.resetLoad();
        })
        .catch((error) => {
          this.flag = false
          this.outputError(error);
        });
    },
    switchEdit() {
      this.dialogProps.action = "edit";
      this.dialogProps.title = "Sửa thông tin đăng ký";
      this.initOptions(this.bizFormModel);
    },
    onDialogClose() {
      this.dialogProps.visible = false;
    },
    onDialogOpen() {
      this.$nextTick(() => {
        this.$refs["testForm"].clearValidate();
      });
    },
    initPatientModel() {
      return {
        company: {
          // Phòng khámid
          id: currentUser.company.id,
          name: null,
        },
        name: "", // Tên bệnh nhân
        gender: {
          // Giới tínhnull
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
    initFormModel(This) {
      return {
        company: {
          // Phòng khámid
          id: currentUser.company.id,
          name: null,
        },
        clinicOffice: {
          // Khoa
          id: null,
          name: null,
        },
        patientId: {
          // Bảng cha Bệnh nhân
          id:
            validatenull(This) || validatenull(This.patientId)
              ? null
              : This.patientId.id,
          name:
            validatenull(This) || validatenull(This.patientId)
              ? null
              : This.patientId.name,
        },
        doctor: {
          // Bác sĩ
          id: null,
          name: null,
        },
        treatType: {
          // Loại điều trị
          value: null,
          name: null,
        },
        receptionStartDate: "", // Thời gian bắt đầu tiếp nhận khám
        source: {
          // Nguồn
          value: 'registrationSource_0',
          name: 'Tại chỗ',
        },
        receptionEndDate: "", // Thời gian kết thúc tiếp nhận khám
        pulse: "", // Mạch
        breathe: "", // Nhịp thở
        temperature: "", // Nhiệt độ cơ thể
        isGoHigharea: "0", // Có từng đến vùng dịch không
        highArea: "", // Địa điểm vùng dịch
        freeRegistrationFee: "0", // Miễn phí đăng ký khám
        registrationFee: 0, // Phí đăng ký khám
        practicleRegistrationFee: 0, //Phí đăng ký khám thực tế đã trả
        practicalRegistrationPayRemarks: "", //Ghi chú thanh toán thực tế
        refundRegistrationPayType: {
          value: null,
          name: null,
        },
        payType: {
          // Phương thức thanh toán
          value: "payType_0",
          name: "Tiền mặt",
        },
        remarks: "", // Thông tin ghi chú
        status: {
          // Trạng thái
          value: "registrationStatus_3",
          name: "Chờ check-in",
        },
        dispensingStatus: "0", // Trạng thái phát thuốc
        dispensingDate: "", // 发/Thời gian trả thuốc
        chargeStatus: "0", // Trạng thái thu phí
        chargeDate: "", // Thời gian thu phí
        subscribeDate: "",//Ngày hẹn
        subscribedaterq: "",//显示用 不传入后台 拼接预约日期
        subscribedatetime: "",//Thời gian hẹn 传入后台前拼接到预约日期字段里 这里只做显示和绑定使用
      };
    },
    initOptions(This) {
      //Truy vấn giới tính bệnh nhân
      getDictItemsByCode(DICT_CODE.GENDER).then((data) => {
        this.gender_List = data;
      });

      //Truy vấn quan hệ với bệnh nhân
      getDictItemsByCode(DICT_CODE.WITH_PATIENT_NEXUS).then((data) => {
        this.withPatientNexus_List = data;
      });

      //Truy vấn khoa/phòng có thể đăng ký khám
      this.key_department++;
      let department_search = {
        params: [
          {
            columnName: "company_id",
            queryType: "=",
            value: currentUser.company.id,
          },
          {columnName: "is_locked", queryType: "=", value: "0"},
          {columnName: "is_register", queryType: "=", value: "1"},
        ],
      };


      // Trên bảng tương ứng của trườngfilterĐiều kiện
      department_search.params.push.apply(department_search.params, []);
      // Quyền dữ liệu: Phòng banorg_department
      this.pushDataPermissions(
        department_search.params,
        this.$route.meta.routerId,
        "52676365136650250"
      );
      this.department_List.splice(0, this.department_List.length);//Yêu cầu truy vấn khoa/phòng đã đưa vào yêu cầu truy vấn bác sĩ
      //Truy vấn danh sách bệnh nhân
      let patientId_search = {
        params: [],
      };
      // Trên bảng tương ứng của trườngfilterĐiều kiện
      patientId_search.params.push.apply(patientId_search.params, [
        {
          columnName: "company_id",
          queryType: "=",
          value: currentUser.company.id,
        },
      ]);
      // Quyền dữ liệu: Bảng bệnh nhânpatient
      this.pushDataPermissions(
        patientId_search.params,
        this.$route.meta.routerId,
        "1008489176147648530"
      );
      this.patientId_List.splice(0, this.patientId_List.length);
      listPatientAll(patientId_search).then((responseData) => {
        this.patientId_List = responseData.data;
      });

      //Truy vấn bác sĩ ngoại trú
      let doctor_search = {
        params: [
          {
            columnName: "department_id",
            queryType: "=",
            value: currentUser.department.id,
          },
        ],
      };
      // Trên bảng tương ứng của trườngfilterĐiều kiện
      doctor_search.params.push.apply(doctor_search.params, []);
      // Bảng có trường bị vô hiệu hóa
      doctor_search.params.push.apply(doctor_search.params, [
        {columnName: "is_locked", queryType: "=", value: "0"},
      ]);
      // Quyền dữ liệu:  Người dùngsys_user
      this.pushDataPermissions(
        doctor_search.params,
        this.$route.meta.routerId,
        "4004"
      );
      // this.doctor_List.splice(0, this.doctor_List.length);
      // listDoctorsAll().then((responseData) => {
      //   this.doctor_List = responseData.data;
      //    listClinicOfficeAll(department_search).then((responseData) => {
      //   this.department_List = responseData.data;
      //   for (let i = 0; i < this.department_List.length; i++) {
      //     if(this.department_List[i].isDefault==='1'){
      //       this.bizFormModel.clinicOffice.id=this.department_List[i].id
      //        this.sortDoc(this.department_List[i].id)
      //     }
      //   }
      // });

      // });

      //Loại điều trị
      getDictItemsByCode(DICT_CODE.TREAT_TYPE).then((data) => {
        this.treatType_List = data;
      });

      //Nguồn bệnh nhân
      getDictItemsByCode(DICT_CODE.REGISTRATION_SOURCE).then((data) => {
        this.source_List = data;
      });

      //Truy vấn loại thanh toán
      getDictItemsByCode(DICT_CODE.PAY_TYPE).then((data) => {
        this.payType_List = data;
      });

      //Trạng thái đăng ký khám
      getDictItemsByCode(DICT_CODE.REGISTRATION_STATUS).then((data) => {
        this.status_List = data;
      });
    },
    //Sự kiện tự tính tuổi
    birthdayChanges() {

      if (!this.bizFormModel.birthday) {
        this.bizFormModel.age = "";
        this.bizFormModel.month = ""
        return
      }
      ;
      const duration = this.$moment.duration(this.$moment().diff(this.bizFormModel.birthday));
      this.bizFormModel.age = duration.years();
      this.bizFormModel.month = duration.months();
      if (this.bizFormModel.age < 12) {
        this.formRules.guardianName[0].required = true
        this.formRules.guardianPhone[0].required = true
        //this.formRules.phone[0].required=false

      } else {
        this.formRules.guardianName[0].required = true
        this.formRules.guardianPhone[0].required = true
        this.$refs["testForm"].clearValidate();
        //this.formRules.phone[0].required=true
      }
    },

    async openViewRegistrationDialog(registration) {
      this.dialogProps.action = "view";
      this.dialogProps.title = "Xem thông tin đăng ký";
      this.bizFormModel = {...this.initFormModel(), ...registration};
      this.initOptions(this.bizFormModel);
      getPatientById(registration.patientId.id).then((responseData) => {
        this.PatientFormModel = responseData.data;
        this.bizFormModel = { ...this.bizFormModel, ...this.PatientFormModel };
      });
      this.tabIndex = "1";
      this.dialogProps.visible = true;
    },
    async openEditRegistrationDialog(registration) {
      this.dialogProps.action = "edit";
      this.dialogProps.title = "Sửa thông tin đăng ký";
      this.bizFormModel = {...this.initFormModel(), ...registration};
      this.initOptions(this.bizFormModel);
      const responseData = await getPatientById(registration.patientId.id);
      this.PatientFormModel = responseData.data;
      const registerId = this.bizFormModel.id;
      this.bizFormModel = { ...this.bizFormModel, ...this.PatientFormModel };
      this.bizFormModel.id = registerId;
      this.tabIndex = "1";
      this.dialogProps.visible = true;
    },
    openAddRegistrationDialog(rowdata) {
      this.listarry = [];
      this.dialogProps.action = "add";
      this.dialogProps.title = "Thêm thông tin đăng ký";
      this.bizFormModel = this.initFormModel();
      this.PatientFormModel = this.initPatientModel();
      this.bizFormModel = { ...this.bizFormModel, ...this.PatientFormModel };
      this.initOptions(this.bizFormModel);
      this.bizFormModel.treatType.name = "初诊"
      this.bizFormModel.subscribedaterq = rowdata.fn;
      this.bizFormModel.doctor.id = rowdata.userid;
      this.bizFormModel.doctor.name = rowdata.username;
      this.bizFormModel.clinicOffice.id = rowdata.ksid;
      this.bizFormModel.clinicOffice.name = rowdata.ksname;
      this.tabIndex = "1";
      this.dialogProps.visible = true;
      let myDate = new Date();
      let nowDate = new Date(Date.parse(new Date()));
      let hou = String(myDate.getHours() < 10 ? '0' + myDate.getHours() : myDate.getHours())
      let min = String(myDate.getMinutes() < 10 ? '0' + myDate.getMinutes() : myDate.getMinutes())
      let hhmm = hou + ':' + min;
      let hh = myDate.getHours() >= rowdata.timestreaint ? hou : rowdata.timestreaint;
      let hhflgs = myDate.getMinutes() >= 30 ? true : false;
      if (Date.parse(rowdata.fn) === Date.parse(rowdata.thecurrent)) {
        if (hhflgs) {
          hhmm = String(myDate.getHours() + 1 < 10 ? '0' + myDate.getHours() + 1 : myDate.getHours() + 1);
          hhmm += ':00';
        } else {
          hhmm = hh + ':30';
        }
      } else {
        hhmm = rowdata.timestrea
      }
      this.listarry.streatime = hhmm;
      this.listarry.endtime = rowdata.timeend;
    },
    openCopyRegistrationDialog(registration) {
      this.dialogProps.action = "add";
      this.dialogProps.title = "Thêm thông tin đăng ký";
      this.bizFormModel = {...this.initFormModel(), ...registration};
      this.initOptions(this.bizFormModel);
      getPatientById(registration.patientId.id).then((responseData) => {
        this.PatientFormModel = responseData.data;
        this.bizFormModel = { ...this.bizFormModel, ...this.PatientFormModel };
      });
      this.tabIndex = "1";
      this.bizFormModel.id = null;
      this.dialogProps.visible = true;
    },
  },
  watch: {
    //Trình theo dõi phát hiện khoa/phòng thay đổi，Lấy liên kết nhân viên trong khoa/phòng
    "bizFormModel.department.id": function (val, oldVal) {
      if (val !== oldVal) {
        if (
          this.dialogProps.action === "add" ||
          this.dialogProps.action === "edit"
        ) {
          this.bizFormModel.doctor = {
            // Bác sĩ
            id: null,
            name: null,
          };
        }
        let doctor_search = {
          params: [
            {
              columnName: "department_id",
              queryType: "=",
              value: currentUser.department.id,
            },
          ],
        };
        // Trên bảng tương ứng của trườngfilterĐiều kiện
        doctor_search.params.push.apply(doctor_search.params, []);
        // Bảng có trường bị vô hiệu hóa
        doctor_search.params.push.apply(doctor_search.params, [
          {columnName: "is_locked", queryType: "=", value: "0"},
        ]);
        // Quyền dữ liệu:  Người dùngsys_user
        this.pushDataPermissions(
          doctor_search.params,
          this.$route.meta.routerId,
          "4004"
        );
        this.doctor_List.splice(0, this.doctor_List.length);
        listDoctorsAll().then((responseData) => {
          this.doctor_List = responseData.data;
        });
      }
    },
    "bizFormModel.age": function (newValue, oldValue) {
      if (newValue < 12) {
        this.formRules.guardianName[0].required = true
        this.formRules.guardianPhone[0].required = true
        //this.formRules.phone[0].required=false

      } else {
        this.formRules.guardianName[0].required = false
        this.formRules.guardianPhone[0].required = false
        this.$refs["testForm"].clearValidate();
        //this.formRules.phone[0].required=true
      }
    }

  },
  mounted: async function () {
  },
}
</script>
<style lang="scss" scoped>
.ipt-box .el-input--mini {
  width: 70%;
}

.registrationForm {
  .el-dialog__body {
    padding: 0 10px;
  }
}
</style>
<style lang="scss" scoped>
.registrationItemBox {
  width: 100%;

  h3 {
    margin: 0 10px;
  }
}

#registrationTotalFee_Container {
  display: flex;
  justify-content: flex-start;
  padding: 20px;
  margin: 10px;
  background-color: rgba(207, 227, 236, 0.803921568627451);
  border-radius: 7px;
}

#registrationTotalFee_Title {
  font-family: "Arial Normal", "Arial", sans-serif;
  font-weight: 400;
  font-style: normal;
  font-size: 21px;
  margin-right: 8px;
}

#registrationTotalFee_Fee {
  font-weight: 800;
  font-style: black;
  font-size: 25px;
  margin-top: -4px;
}

.pay-registration-flex-start {
  display: flex;
  justify-content: flex-start;
  margin: 10px;
}

.pay-registration-p {
  width: 80px;
  text-align: left;
  margin-right: 10px;
}

.pay-registration-content {
  widows: 200px;
  margin-top: 12px;
}

.ipt-box {
  display: flex;
  align-items: center;
}

::v-deep .el-dialog__body {
  padding: 0 7px;
  margin: 0
}
</style>
