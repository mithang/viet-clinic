<template>
  <el-dialog
    class="registrationForm"
    :title="dialogProps.title"
    :visible.sync="dialogProps.visible"
    :close-on-click-modal="false"
    width="50%"
    @open="onDialogOpen()"
    v-loading="loading"
  >
    <!-- <div slot="title" class="dialog-header">
      {{ dialogProps.title }}
      <OperationIcon
        v-show="dialogProps.action === 'view' && permission.edit"
        type="primary"
        text="Sửa"
        placement="top-start"
        icon-name="el-icon-edit"
        @click="switchEdit"
      ></OperationIcon>
    </div> -->

    <el-form
      :model="bizFormModel"
      :rules="formRules"
      ref="registrationForm"
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
                >
                </el-input>
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
                    :disabled="dialogProps.action !== 'add'"
                    v-model="bizFormModel.age"
                    :maxlength="20"
                    :placeholder="
                      dialogProps.action === 'view' ? '' : 'Tuổi'
                    "
                  >
                    <template slot="append">
                      tuổi
                    </template>
                  </el-input>
                </el-col>
                <el-col :span="24 / 2">
                  <el-input
                    :disabled="dialogProps.action !== 'add'"
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
              <el-form-item label="Số giấy tờ" prop="card">
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
              <el-form-item label="Loại giấy tờ khám" prop="cardType.name">
                <el-input
                  v-if="dialogProps.action === 'view'"
                  :disabled="true"
                  v-model="bizFormModel.cardType.name"
                ></el-input>
                <el-select
                  v-else
                  v-model="bizFormModel.cardType"
                  value-key="value"
                  filterable
                  clearable
                  placeholder="Vui lòng chọn loại giấy tờ"
                  @clear="
                    bizFormModel.medType = {
                      value: null,
                      name: null,
                    }
                  "
                >
                  <el-option
                    v-for="cardType in cardTypeList"
                    :key="cardType.value"
                    :label="cardType.name"
                    :value="cardType"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="24 / 2" v-if="bizFormModel.cardType && bizFormModel.cardType.value === '03'">
              <el-form-item
                label="Số thẻ bảo hiểm xã hội" prop="mdtrtCertNo">
                <el-input
                  :disabled="dialogProps.action === 'view'"
                  v-model="bizFormModel.mdtrtCertNo"
                  :maxlength="45"
                  :placeholder="dialogProps.action === 'view' ? '' : 'Nhập Số thẻ bảo hiểm xã hội'"
                ></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="24 / 2">
              <el-form-item label="Dân tộc" prop="nation">
                <el-input
                  :disabled="dialogProps.action === 'view'"
                  v-model="bizFormModel.nation"
                  :maxlength="45"
                  :placeholder="dialogProps.action === 'view' ? '' : 'Vui lòng nhập dân tộc'"
                ></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="24 / 2">
              <el-form-item label="Nghề nghiệp" prop="occupation">
                <el-input
                  :disabled="dialogProps.action === 'view'"
                  v-model="bizFormModel.occupation"
                  :maxlength="45"
                  :placeholder="dialogProps.action === 'view' ? '' : 'Vui lòng nhập nghề nghiệp'"
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
            <el-col :span="24 / 2">
              <el-form-item label="Đơn vị công tác" prop="workplace">
                <el-input
                  :disabled="dialogProps.action === 'view'"
                  v-model="bizFormModel.workplace"
                  :maxlength="45"
                  :placeholder="dialogProps.action === 'view' ? '' : 'Vui lòng nhập đơn vị công tác'"
                ></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <!-- <el-col :span="24 / 2">
              <el-form-item label="Quan hệ với bệnh nhân" prop="withPatientNexus">
                <el-input
                  v-if="dialogProps.action !== 'add'"
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
            </el-col> -->
          </el-row>
        </div>
        <div class="registrationItemBox">
          <h3>Thông tin đăng ký</h3>
          <el-row>
            <el-col :span="24 / 2">
              <el-form-item label="Khoa" prop="clinicOffice.id">
                <el-input
                  v-if="dialogProps.action === 'view'"
                  :disabled="true"
                  v-model="bizFormModel.clinicOffice.name"
                ></el-input>
                <el-cascader
                  v-else
                  v-model="bizFormModel.clinicOffice.id"
                  @change="sortDoc"
                  :options="department_List"
                  :key="key_department"
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
                  v-if="dialogProps.action === 'view'"
                  :disabled="true"
                  v-model="bizFormModel.doctor.name"
                ></el-input>
                <el-select
                  v-else
                  v-model="bizFormModel.doctor"
                  value-key="id"
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
            <!-- <el-col :span="24 / 2">
              <el-form-item label="Nguồn" prop="source">
                <el-input
                  v-if="dialogProps.action === 'view'"
                  :disabled="true"
                  v-model="bizFormModel.source.name"
                ></el-input>
                <el-select
                  v-else
                  v-model="bizFormModel.source"
                  value-key="value"
                  filterable
                  clearable
                  placeholder="Vui lòng chọn nguồn"
                  @clear="
                    bizFormModel.source = {
                      value: null,
                      name: null,
                    }
                  "
                >
                  <el-option
                    v-for="source in source_List"
                    :key="source.value"
                    :label="source.name"
                    :value="source"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-col> -->
          </el-row>
          <el-row>

            <!-- <el-col :span="24 / 2">
              <el-form-item label="Loại điều trị" prop="treatType.name">
                <el-input
                  v-if="dialogProps.action === 'view'"
                  :disabled="true"
                  v-model="bizFormModel.treatType.name"
                ></el-input>
                <el-select
                  v-else
                  v-model="bizFormModel.treatType.name"
                  value-key="value"
                  filterable
                  clearable
                  placeholder="Vui lòng chọn loại điều trị"
                  @clear="
                    bizFormModel.treatType = {
                      value: null,
                      name: null,
                    }
                  "
                >
                  <el-option
                    v-for="treatType in treatType_List"
                    :key="treatType.value"
                    :label="treatType.name"
                    :value="treatType.name"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-col> -->
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
                  :disabled="bizFormModel.freeRegistrationFee === '1' || dialogProps.action !== 'add'"
                ></number-input>
              </el-form-item>
            </el-col>
            <el-col :span="24 / 2">
              <el-form-item label="Loại điều trị" prop="treatType.name">
                <el-input
                  v-if="dialogProps.action === 'view'"
                  :disabled="true"
                  v-model="bizFormModel.treatType.name"
                ></el-input>
                <el-select
                  v-else
                  v-model="bizFormModel.treatType.name"
                  value-key="value"
                  filterable
                  clearable
                  placeholder="Vui lòng chọn loại điều trị"
                  @clear="
                    bizFormModel.treatType = {
                      value: null,
                      name: null,
                    }
                  "
                >
                  <el-option
                    v-for="treatType in treatType_List"
                    :key="treatType.value"
                    :label="treatType.name"
                    :value="treatType.name"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-col>
<!--            <el-col :span="24 / 2">-->
<!--              <el-form-item label="Loại hình bảo hiểm y tế" prop="medType.name">-->
<!--                <el-input-->
<!--                  v-if="dialogProps.action === 'view'"-->
<!--                  :disabled="true"-->
<!--                  v-model="bizFormModel.medType.name"-->
<!--                ></el-input>-->
<!--                <el-select-->
<!--                  v-else-->
<!--                  v-model="bizFormModel.medType"-->
<!--                  value-key="value"-->
<!--                  filterable-->
<!--                  clearable-->
<!--                  placeholder="Vui lòng chọn loại y tế"-->
<!--                  @clear="-->
<!--                    bizFormModel.medType = {-->
<!--                      value: null,-->
<!--                      name: null,-->
<!--                    }-->
<!--                  "-->
<!--                >-->
<!--                  <el-option-->
<!--                    v-for="medType in medTypeList"-->
<!--                    :key="medType.value"-->
<!--                    :label="medType.name"-->
<!--                    :value="medType"-->
<!--                  ></el-option>-->
<!--                </el-select>-->
<!--              </el-form-item>-->
<!--            </el-col>-->
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
            <!-- <el-col :span="24 / 2">
              <el-form-item
                label="Phương thức thanh toán"
                prop="payType"
                v-if="true"
              >
                <el-input
                  :disabled="true"
                  v-model="bizFormModel.payType.name"
                ></el-input>
              </el-form-item>
            </el-col> -->
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
                 @click='onSubmit("registrationForm")'>Lưu
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
          <div id="registrationTotalFee_Title">Số tiền phải thu (VNĐ): </div>
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
import {validatenull} from "@/utils/validate";
import {listSysParamConfigAll, saveSysParamConfigList} from '@/api/sys/sysParamConfig'
import {listClinicOfficeAll} from "@/api/org/clinicOffice";
import {getPatientById, listPatientAll, savePatient,} from "@/api/outpatient/patient";
import {listDictItemAll} from "@/api/sys/dictItem";
import { getDictItemsByCode, DICT_CODE } from '@/utils/dictCache'
import {listDoctorsAllnew, listDoctorsAll, saveRegistration} from "@/api/outpatient/registration";
import BaseUI from "@/views/components/baseUI";
import OperationIcon from "@/components/OperationIcon";
import {saveTollInfo} from "@/api/toll/tollInfo";

// Hàm khởi tạo cấp module — tránh gọi this.Phương pháp trong data() của Vue (lúc này methods chưa được khởi tạo)
function _defaultFormModel() {
  const cu = window.currentUser || {};
  const co = cu.company || {};
  return {
    company: { id: co.id || '', name: null },
    clinicOffice: { id: null, name: null },
    gender: { name: "", value: "" },
    mdtrtCertNo: "",
    patientId: { id: cu.id || '', name: cu.name || '' },
    doctor: { id: cu.id || '', name: cu.name || '' },
    treatType: { value: null, name: null },
    medType: { value: null, name: null },
    cardType: { value: null, name: null },
    receptionStartDate: "",
    receptionEndDate: "",
    source: { value: 'registrationSource_0', name: 'Tại chỗ' },
    pulse: "", breathe: "", temperature: "",
    isGoHigharea: "0", highArea: "",
    freeRegistrationFee: '1',
    registrationFee: 0,
    practicleRegistrationFee: 0,
    practicalRegistrationPayRemarks: "",
    refundRegistrationPayType: { value: null, name: null },
    infectType: { value: "infectType_0", name: "Không" },
    payType: { value: "payType_0", name: "Tiền mặt" },
    status: { value: "registrationStatus_0", name: "Chờ khám" },
    dispensingStatus: "0", dispensingDate: "",
    chargeStatus: "0", chargeDate: "",
    name: "", age: "", month: "", birthday: "", phone: "",
    nation: "", occupation: "", card: "", workplace: "",
    healthCardNo: "", province: "", city: "", area: "", address: "",
    remarks: "",
    registrationDate: "",
    department: { id: "", name: "" },
    visitDate: "", diagnosis: "",
    westernDiagnose: "", chinaDiagnose: "", chinaSyndrome: "",
    diagnoseId: "", syndromeId: "",
  };
}
function _defaultPatientModel() {
  const cu = window.currentUser || {};
  const co = cu.company || {};
  return {
    company: { id: co.id || '', name: null },
    name: "",
    gender: { value: null, name: null },
    age: "", month: "", birthday: "", phone: "",
    nation: "", occupation: "", card: "", cardType: "", workplace: "",
    withPatientNexus: { value: null, name: null },
    healthCardNo: "", province: "", city: "", area: "", address: "", remarks: "",
  };
}

export default {
  extends: BaseUI,
  name: "registration-form",
  components: {
    OperationIcon,
  },
  data() {
    return {
      setDisabled: {
        disabledDate(time) {
          return time.getTime() > Date.now(); // Có thể chọn ngày trước, Có thể chọn ngày hiện tại, Không thể chọn ngày tương lai
          // return time.getTime() > Date.now() - 8.64e7; // Có thể chọn ngày trước, Không thể chọn ngày hiện tại, Không thể chọn ngày tương lai
          // return time.getTime() < Date.now() - 8.64e7; // Không thể chọn ngày trước, Có thể chọn ngày hiện tại, Có thể chọn ngày tương lai
          // return time.getTime() < Date.now(); // Không thể chọn ngày trước, Không thể chọn ngày hiện tại, Có thể chọn ngày tương lai

          /*Chỉ chọn được 7 ngày gần đây trước hôm nay*/
          // let now = Date.now();
          // let seven = 7 * 8.64e7;
          // let sevenDays = now - seven;
          // return time.getTime() > now || time.getTime() < sevenDays;  //Vô hiệu hóa ngày lớn hơn hôm nay và nhỏ hơn 7 ngày trước
        },
      },

      remove:false,
      view:false,
      payRegistrationFeeVisible: false, //Hiển thị cửa sổ thu phí
      bizFormModel: {..._defaultFormModel(), ..._defaultPatientModel()},
      PatientFormModel: _defaultPatientModel(),
      tabIndex: "1",
      flag: false,//Ngăn gửi trùng lặp
      gender_List: [], // Giới tính
      withPatientNexus_List: [], // Quan hệ với bệnh nhân
      key_department: 1, // el-cascader key
      department_List: [], // Khoa
      patientId_List: [], // Bệnh nhân
      doctor_List: [], // Bác sĩ
      select_doctor: [],
      treatType_List: [], // Loại điều trị
      medTypeList: [],//Loại y tế
      cardTypeList:[],//Loại giấy tờ
      source_List: [], // Nguồn
      payType_List: [], // Phương thức thanh toán
      status_List: [], // Trạng thái
      sysParamConfigForm: {
        chronicDays: null,//Số ngày đơn thuốc bệnh mạn tính
        normalDays: null,//Số ngày đơn thuốc thông thường
        registrationFee: null,//Số tiền đăng ký khám mặc định
        retailPrice: null,//Có mặc định giá bán lẻ không
      },
      saveMap:new Map(),
      search: {
        columnName: "company_id",
        queryType: "=",
        value: currentUser.company.id
      },
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
        "treatType.name": [
          { required: true, message: "Vui lòng chọn loại điều trị", trigger: "change" },
         ],
        // "medType.name": [
        //   { required: true, message: "Vui lòng chọn loại y tế", trigger: "change" },
        // ],
        "doctor.name": [
          { required: true, message: "Vui lòng chọn bác sĩ", trigger: "change" },
        ],
        "clinicOffice.id": [
          { required: true, message: "Vui lòng chọn khoa/phòng", trigger: "change" },
        ],
        // "birthday": [
        //   {required: true, message: "Vui lòng chọn ngày sinh", trigger: "change"},
        // ],
        //  phone: [{ required: true, message: "Vui lòng nhập thông tin liên hệ", trigger: "blur" }],
        age: [{required: true, message: "Vui lòng nhập tuổi", trigger: "blur"}],
        name: [{required: true, message: "Vui lòng nhập họ tên", trigger: "change"}],
        "gender.name": [
          {required: true, message: "Vui lòng nhập giới tính", trigger: "change"},
        ],
        // "card": [
        //   { required: true, message: 'Vui lòng nhập số CMND/CCCD', trigger: 'blur' },
        //   { min:18,max:18, message: "Vui lòng nhập18chữ số của số CMND/CCCD", trigger: "blur" },
        //    {	//Gọi phương thức đã định nghĩa ở trên để kiểm tra định dạng có đúng không
        //            validator: isCnNewID,trigger: "blur"
        //   }
        // ],
        guardianName: [{required: false, message: "Vui lòng nhập tên người giám hộ", trigger: "change"},],
        guardianPhone: [{required: false, message: "Vui lòng nhập số điện thoại người giám hộ", trigger: "change"},],
        workplace: [{required: false, message: "Vui lòng nhập đơn vị công tác", trigger: "change"},],
        nation: [{required: false, message: "Vui lòng nhập dân tộc", trigger: "change"},],
        occupation: [{required: false, message: "Vui lòng nhập nghề nghiệp", trigger: "change"},]
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

        // Giới tính
        let sexStr = this.bizFormModel.card.substring(16, 17)
        // 0 nữ 1 nam
        let sexName = "Nam";
        if (parseInt(sexStr) % 2 === 0) {
          sexName = "Nữ";
        }
        if (this.gender_List && this.gender_List.length > 0) {
          this.bizFormModel.gender = this.gender_List.find(item => {
            return item.name === sexName
          })
        }
      } else if (this.bizFormModel.card.length === 0) {
        this.bizFormModel.birthday = ""
        this.bizFormModel.gender = {
          value: null,
          name: null,
        }
        this.birthdayChanges()
      }
    },
    sortDoc(clinicOffice, officename) {
      if (officename !== 'no') {
        this.bizFormModel.doctor = {
          id: "",
          name: "",
        };
      }
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
      if (this.bizFormModel.doctor.id == null && this.bizFormModel.doctor.name == null) {
        this.bizFormModel.status.name = "Chờ phân loại khám";
        this.bizFormModel.status.value = "registrationStatus_5";
      }
      if (this.bizFormModel.doctor.id != null && this.bizFormModel.doctor.name != null) {
        this.bizFormModel.status.name = "Chờ khám";
        this.bizFormModel.status.value = "registrationStatus_0";
      }
      if (this.bizFormModel.id !== undefined) {
        this.bizFormModel.patientId = {
          id: this.bizFormModel.id,
          name: this.PatientFormModel.name,
        };
        this.bizFormModel.id = registerId;

        this.doSave();
        return;
      }
      //Bảo hiểm y tế mặc định là khám ngoại trú thường
      this.bizFormModel.medType.value='11'

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
      // if(!flagCard){
      // this.$message.error("Số CMND/CCCD không đúng, Vui lòng nhập lại")
      //   return;
      // }
      this.flag = true
      for (let i = 0; i < this.treatType_List.length; i++) {
        if (this.bizFormModel.treatType.name === this.treatType_List[i].name) {
          this.bizFormModel.treatType.value = this.treatType_List[i].value
        }

      }
      this.$refs[formName].validate(valid => {
        if (valid) {
          if (
            this.bizFormModel.freeRegistrationFee === '1' || this.dialogProps.action !== "add"
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
          if (responseData.code === 100 && this.dialogProps.action === "add") {
            this.dialogProps.visible = false;
            this.payRegistrationFeeVisible = false;
            if (this.dialogProps.action === "add" && this.remove){
              this.$router.push('/medicalOutpatientRecord')
            }
            //Sau khi lưu thành công, Nếu có phí đăng ký khám, Chèn dữ liệu vào bảng thu phí
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
            this.flag = false
            this.showMessage(responseData);
          }
          this.resetLoad();
          this.dialogProps.visible = false;
        })
        .catch((error) => {
          this.dialogProps.visible = false;
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
        // this.$refs["registrationForm"].clearValidate();
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
        nation: "", // Dân tộc
        occupation: "", // Nghề nghiệp
        card: "", // Số giấy tờ
        cardType:"",//Loại giấy tờ
        workplace: "", // Đơn vị công tác
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
        gender: {
          name: "",
          value: ""
        },
        mdtrtCertNo:"",
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
          id:  currentUser.id ,
          name:  currentUser.name
        },
        treatType: {
          // Loại điều trị
          value: null,
          name: null,
        },
        medType: {
          // Loại điều trị
          value: null,
          name: null,
        },
        cardType: {
          // Loại giấy tờ
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
        freeRegistrationFee: this.view ? '0': '1', // Miễn phí đăng ký khám
        registrationFee:  0, // Phí đăng ký khám
        practicleRegistrationFee: 0, //Phí đăng ký khám thực tế đã trả
        practicalRegistrationPayRemarks: "", //Ghi chú thanh toán thực tế
        refundRegistrationPayType: {
          value: null,
          name: null,
        },
        infectType:{
          //Bệnh truyền nhiễm
          value: "infectType_0",
          name: "Không",
        },
        payType: {
          // Phương thức thanh toán
          value: "payType_0",
          name: "Tiền mặt",
        },
        remarks: "", // Thông tin ghi chú
        status: {
          // Trạng thái
          value: "registrationStatus_0",
          name: "Chờ khám",
        },
        dispensingStatus: "0", // Trạng thái phát thuốc
        dispensingDate: "", // Thời gian phát/trả thuốc
        chargeStatus: "0", // Trạng thái thu phí
        chargeDate: "", // Thời gian thu phí
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
      this.doctor_List.splice(0, this.doctor_List.length);
      listDoctorsAll().then((responseData) => {
        this.doctor_List = responseData.data;
        listClinicOfficeAll(department_search).then((responseData) => {
          this.department_List = responseData.data;
          for (let i = 0; i < this.department_List.length; i++) {
            if (this.department_List[i].isDefault === '1' && this.dialogProps.action === "add") {
              this.bizFormModel.clinicOffice.id = this.department_List[i].id
              this.sortDoc(this.department_List[i].id, 'no')
            }
          }
        });

      });

      //Loại điều trị
      getDictItemsByCode(DICT_CODE.TREAT_TYPE).then((data) => {
        this.treatType_List = data;
      });

      //Loại y tế
      getDictItemsByCode(DICT_CODE.MED_TYPE).then((data) => {
        this.medTypeList = data;
      });
      //Loại giấy tờ
      getDictItemsByCode(DICT_CODE.CARD_TYPE).then((data) => {
        this.cardTypeList = data;
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
        //this.$refs["registrationForm"].clearValidate();
        // this.formRules.phone[0].required=true
      }
    },
    // Lấy phí đăng ký khám mặc định
    async pageInit() {
      listSysParamConfigAll({params:[this.search]}).then(responseData => {
        if(responseData.code === 100) {
          if (Array.isArray(responseData.data) && responseData.data.length >= 1){
            responseData.data.forEach(data=>{
              this.saveMap.set(data.configName, data);
              if (this.sysParamConfigForm.hasOwnProperty(data.configName)) {
                this.sysParamConfigForm[data.configName] = data.configValue;
              }
            })
            this.bizFormModel.registrationFee = this.sysParamConfigForm.registrationFee
            if(this.bizFormModel.freeRegistrationFee === '1'){
              this.bizFormModel.registrationFee = 0
            }
          }else {
            this.initSaveDatas();
          }
        } else {
          this.showMessage(responseData)
        }
      }).catch(error => {
        this.outputError(error)
      })
    },
    /**
     * Khởi tạosaveMap
     */
    initSaveDatas(){
      for (let key in this.sysParamConfigForm) {
        if (this.sysParamConfigForm.hasOwnProperty(key)) {
          let data = {
            id: null,
            configName: key,
            configValue: this.sysParamConfigForm[key],
            companyId: currentUser.company.id
          };
          this.saveMap.set(key, data);
        }
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
      this.bizFormModel.gender.name = registration.patientId.gender.name
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
      this.bizFormModel.gender.name = registration.patientId.gender.name
      this.sortDoc(registration.clinicOffice.id, 'no');
      this.tabIndex = "1";
      this.dialogProps.visible = true;
    },
    openAddRegistrationDialog(remove, view) {
      this.remove = remove;
      this.view = view;
      this.dialogProps.action = "add";
      this.dialogProps.title = "Thêm thông tin đăng ký";
      this.bizFormModel = this.initFormModel();
      this.PatientFormModel = this.initPatientModel();
      this.bizFormModel = { ...this.bizFormModel, ...this.PatientFormModel };
      this.initOptions(this.bizFormModel);
      this.bizFormModel.treatType.name = "Khám lần đầu"
      if (this.cardTypeList && this.cardTypeList.length > 0) {
        this.bizFormModel.cardType = this.cardTypeList[0];
      }
      this.tabIndex = "1";
      this.dialogProps.visible = true;
      this.pageInit()
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
      if (this.cardTypeList && this.cardTypeList.length > 0) {
        this.bizFormModel.cardType = this.cardTypeList[0];
      }
      this.bizFormModel.id = null;
      this.dialogProps.visible = true;
    },
  },
  watch: {
    //Trình theo dõi phát hiện khoa/phòng thay đổi, Lấy liên kết nhân viên trong khoa/phòng
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
    "bizFormModel.freeRegistrationFee": function (newValue, oldValue) {
      if (newValue === '1') {
        this.bizFormModel.registrationFee = 0
      }
    },
    "bizFormModel.age": function (newValue, oldValue) {
      if (newValue < 12) {
        this.formRules.guardianName[0].required = true
        this.formRules.guardianPhone[0].required = true
        // this.formRules.phone[0].required=false

      } else {
        this.formRules.guardianName[0].required = false
        this.formRules.guardianPhone[0].required = false
        // this.$refs["registrationForm"].clearValidate();
        // this.formRules.phone[0].required=true
      }
    },
  },
};
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
