<template>
  <el-row v-loading="loading">
    <!-- Lịch sử  -->
    <History :bussObject="curentRow"></History>
    <!-- Cửa sổ chỉnh sửa  -->
    <registration-form
      ref="registrationForm"
      :permission="permission"
      @save-finished="loadData"
    ></registration-form>
    <ys-form ref="ysForm" :permission="permission" @typeclick="rettonys"></ys-form>
    <beforehand-form ref="beforehandForm" :permission="permission" @typeclick="rettonys"></beforehand-form>
    <el-card class="page-container">
      <el-tabs v-model="activeName" @tab-click="handleClick"
               style="width: 100%; height: 100%">
        <el-tab-pane label="Thông tin đăng ký" name="djxx">
          <!--  Thanh tìm kiếm  Bắt đầu -->
          <div class="query-form-container" style="margin-top:20px;">
            <el-row v-if="!moreCodition" class="search-row">
              <el-form
                :model="queryModel"
                @submit.native.prevent
                label-position="left"
                label-width="70px"
                ref="queryForm"
                :inline-message="true"
              >
                <el-col :span="6">
                  <el-form-item label="Thời gian đăng ký" prop="createDate">
                    <el-date-picker
                      style="width: 200px"
                      :picker-options="filerData.pickerOptions"
                      v-model="queryModel.createDate"
                      type="datetimerange"
                      range-separator="đến"
                      start-placeholder="Ngày bắt đầu"
                      end-placeholder="Ngày kết thúc"
                      :default-time="['00:00:00', '23:59:59']"
                    >
                    </el-date-picker>
                  </el-form-item>
                </el-col>
                <el-col :span="4" style="text-align:right">
                  <el-form-item label="Khoa" prop="department.id" style="padding-right:20px">
                    <el-cascader
                      style="width:180px"
                      v-model="queryModel.department.id"
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
                <el-col :span="4" style="text-align:right">
                  <el-form-item label="Bác sĩ" prop="doctor">
                    <el-select
                      style="width: 140px"
                      v-model="queryModel.doctor"
                      value-key="id"
                      filterable
                      clearable
                      placeholder="Vui lòng chọn bác sĩ"
                    >
                      <el-option
                        v-for="doctor in doctor_List"
                        :key="doctor.id"
                        :label="doctor.name"
                        :value="doctor"
                      ></el-option>
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="4" style="text-align:right">
                  <el-form-item label="Nguồn" prop="source">
                    <el-select
                      style="width: 140px"
                      v-model="queryModel.source"
                      value-key="value"
                      filterable
                      clearable
                      placeholder="Vui lòng chọn nguồn"
                    >
                      <el-option
                        v-for="source in source_List"
                        :key="source.value"
                        :label="source.name"
                        :value="source.value"
                      ></el-option>
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="4" style="text-align:right">
                  <el-form-item label="Bệnh nhân" prop="patientId">
                    <el-select
                      style="width: 140px"
                      v-model="queryModel.patientId"
                      value-key="id"
                      filterable
                      clearable
                      placeholder="Vui lòng chọn bệnh nhân"
                    >
                      <el-option
                        v-for="patientId in patientId_List"
                        :key="patientId.id"
                        :label="patientId.name"
                        :value="patientId"
                      ></el-option>
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="4">
                  <el-form-item label="Trạng thái" prop="status">
                    <el-select
                      style="width: 140px"
                      v-model="queryModel.status"
                      value-key="id"
                      filterable
                      clearable
                      placeholder="Vui lòng chọn trạng thái đăng ký"
                    >
                      <el-option
                        v-for="status in statusList"
                        :key="status.id"
                        :label="status.name"
                        :value="status"
                      ></el-option>
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="3" style="display:flex;justivy-content:space-around">
                  <el-button
                    type="primary"
                    icon="el-icon-search"
                    @click="onSearch()"
                    :plain="true"
                  >Tìm kiếm
                  </el-button>
                  <el-button
                    type="info"
                    icon="el-icon-refresh-left"
                    @click="reset"
                    :plain="true"
                  >Đặt lại
                  </el-button>
                </el-col>
                <el-col :span="17" style="text-align:right;padding-right:5px">
                  <el-button-group>
                    <el-button
                      v-show="permission.add"
                      type="primary"
                      icon="el-icon-plus"
                      @click="onCreateEntity('registrationForm')"
                    >新增登记
                    </el-button
                    >
                  </el-button-group>
                </el-col>
              </el-form>
            </el-row>
            <QueryForm
              v-else
              v-model="moreParm"
              :tableId="tableId"
              :schemeId="schemeId"
              :routerId="$route.meta.routerId"
              @search="onSearch()"
              @moreCodition="onMoreCodition()"
            ></QueryForm>
          </div>
          <!--  Thanh tìm kiếm  Kết thúc -->

          <!-- Thanh công cụ Bắt đầu -->

          <!-- Thanh công cụ Kết thúc -->

          <!-- Thanh bảng  Bắt đầu -->
          <el-row>
            <el-col :span="24">
              <div @mouseleave="moveTableOutside">
                <el-table
                  class="registration-table"
                  :data="registrationList"
                  border
                  height="calc(100vh - 350px)"
                  @sort-change="onSortChange"
                  @header-dragend="onChangeWidth"
                  :cell-class-name="cellClassName"
                  :header-cell-class-name="headerCellClassName"
                  highlight-current-row
                  ref="mutipleTables"
                >
                  <el-table-column
                    label="Số thứ tự"
                    type="index"
                    :index="indexMethod"
                    align="center">
                  </el-table-column>
                  <el-table-column
                    v-for="(cv, index) in columnViews"
                    v-if="cv.display"
                    :prop="cv.prop"
                    :key="`columnViews_${index}`"
                    :label="cv.label"
                    sortable="custom"
                    :align="cv.align"
                    :min-width="cv.miniWidth + 'px'"
                    :width="cv.width + 'px'"
                    header-align="center"
                    :column-key="index.toString()"
                  >
                    <template slot-scope="{ row, $index }">
                  <span
                    v-if="
                      columnViews[index].showType === 'Switch' ||
                      columnViews[index].showType === 'Checkbox' ||
                      columnViews[index].showType === 'Radio'
                    "
                  >
                    <li
                      v-if="getAttrValue(row, columnViews[index].prop) === '1'"
                      class="el-icon-check"
                      style="color: #f56c6c"
                    ></li>
                  </span>
                      <div v-else-if="columnViews[index].label === 'Trạng thái'">
                    <span v-if="getAttrValue(row, columnViews[index].prop) === 'Chờ khám'"
                          style="color: #f56c6c">Chờ khám</span>
                        <span v-else>{{
                            getAttrValue(
                              row,
                              columnViews[index].prop,
                              columnViews[index].javaType
                            )
                          }}</span>
                      </div>
                      <span v-else>{{
                          getAttrValue(
                            row,
                            columnViews[index].prop,
                            columnViews[index].javaType
                          )
                        }}</span>
                    </template>
                  </el-table-column>
                  <!--Nút thao tác theo dòng-->
                  <el-table-column
                    label="Thao tác"
                    header-align="center"
                    :width="ColumnWidth"
                  
                    key="operationColumn"
                  >
                    <template slot="header" slot-scope="scope">
                      <span>Thao tác</span>
                      <view-columns-select
                        v-model="columnViews"
                        v-on:save-column-view="saveColumn"
                        v-on:show-all-column="showAllColumn"
                        v-on:show-default-column="showDefaultColumn"
                      ></view-columns-select>
                      <export-excel-button
                        v-show="permission.export"
                        :data="registrationList"
                        :tHeader="getHeads()"
                        :filterVal="getFilterVal()"
                        :plain="true"
                      ></export-excel-button>
                    </template>
                    <template slot-scope="scope">
                      <!-- Đã khám -->
                      <div style="display:flex;justify-content:space-around">
                        <p class="btn" v-if="scope.row.status.value === 'registrationStatus_3'|| scope.row.status.value === null">
                      <span @click="onEditEntity(scope.$index, scope.row, 'registrationForm')"
                      >查看或修改</span>
                          <span
                            @click="updateStatusId(scope.row.id, scope.row.status.value,scope.row.doctor,scope.row.clinicOffice,scope.row.subscribeDate)"
                          >签到</span>
                        </p>
                        <p
                          class="btn1"
                          v-if="scope.row.status.value === 'registrationStatus_0' || scope.row.status.value === 'registrationStatus_5' || scope.row.status.name === ''"
                        >
                          <span v-show="permission.edit" @click="applyForStudentExcel(scope.$index, scope.row)">Khám chữa bệnh từ xa&nbsp;</span>
                          <span @click="onEditEntity(scope.$index, scope.row, 'registrationForm')">Sửa</span>
                          <el-popconfirm title="确定退号吗？" @confirm="chargeClick(scope.row)">
                            <span slot="reference">退号</span>
                          </el-popconfirm>
                          <span class="btn1" @click="openRegistration(scope.row.id)">In</span>

                        </p>
                        <p
                          class="btn2"
                          v-if="
                      scope.row.status.value === 'registrationStatus_1' ||
                      scope.row.status.value === 'registrationStatus_2' ||
                      scope.row.status.value === 'registrationStatus_4'
                    "
                          @click="onViewEntity(scope.$index, scope.row, 'registrationForm')"
                        >
                          Xem
                        </p>
                        <p
                          class="btn1"
                          v-if="
                      scope.row.registrationFee > 0 &&(scope.row.status.value === 'registrationStatus_1' ||
                      scope.row.status.value === 'registrationStatus_2')
                    "
                          @click="openRegistration(scope.row.id)"
                        >
                          In
                        </p>
                      </div>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </el-col>
          </el-row>
          <!-- Thanh bảng  Kết thúc -->
          <!-- Thanh phân trang     Bắt đầu -->
          <el-row>
            <el-col :span="24">
              <el-pagination
                background
                @size-change="onSizeChange"
                @current-change="onCurrentChange"
                :current-page.sync="currentPage"
                :page-sizes="[20, 50, 100, registrationTotal]"
                :page-size="20"
                layout="total, sizes, prev, pager, next, jumper"
                :total="registrationTotal"
              >
              </el-pagination>
            </el-col>
          </el-row>
          <!-- Thanh phân trang     Kết thúc -->
          <!--Hộp thoại hoàn phí Bắt đầu-->
          <el-dialog
            title="退费信息"
            :visible.sync="refundRegistraionVisible"
            width="50%"
            :close-on-click-modal="false"
            :before-close="dialogBeforeClose"
          >
            <div>
              <el-divider></el-divider>
              <div id="refundTotalFee_Container">
                <div id="refundTotalFee_Title">应退金额（VNĐ）：</div>
                <div id="refundTotalFee_Fee">{{ refundTotalFee }}</div>
              </div>
              <div class="refund-registration-flex-start">
                <p class="refund-registration-p">Phương thức thanh toán</p>
                <el-radio-group v-model="refundReistrationPayType" class="refund-registration-content">
                  <el-radio
                    v-for="payType in payType_List"
                    :key="payType.value"
                    :label="payType.value"
                  >{{ payType.name }}
                  </el-radio
                  >
                </el-radio-group>
              </div>
              <div class="refund-registration-flex-start">
                <p class="refund-registration-p">Ghi chú</p>
                <el-input v-model="refundRegistrationRemarks" class="refund-registration-content"></el-input>
              </div>
              <el-divider></el-divider>
            </div>
            <div slot="footer">
              <el-button @click="refundRegistraionVisible = false">Hủy</el-button>
              <el-button type="primary" @click="refundRegistrationPayClick"
              >确认退费
              </el-button
              >
            </div>
          </el-dialog>
          <!--Hộp thoại hoàn phí Kết thúc-->

          <!-- 签到弹出框 -->
          <el-dialog
            title="添加科室和医生"
            :visible.sync="signDialogVisible"
            width="30%"
            :before-close="handleClose">
            <el-form
              :model="bizFormModel"
              :rules="formRules"
              ref="registrationForm"
              label-width="120px"
              label-position="right"
              style="marginTop: 10px"
              class="edit-form"
            >
              <el-row>
                <el-col :span="24 / 2">
                  <el-form-item label="Khoa" prop="clinicOffice.id">
                    <el-cascader
                      v-model="bizFormModel.clinicOffice.id"
                      @change="sortDoc"
                      :options="departments_List"
                      :key="keys_department"
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
                    <el-select
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
                        v-for="doctor in this.selects_doctor"
                        :key="doctor.id"
                        :label="doctor.name"
                        :value="doctor"
                      ></el-option>
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
            <span slot="footer" class="dialog-footer">
          <el-button @click="signDialogVisible = false">Hủy</el-button>
          <el-button type="primary" @click="signIn">Xác nhận</el-button>
        </span>
          </el-dialog>
          <!-- Đề nghị khám chữa bệnh từ xa -->
          <el-dialog
            title="Đề nghị khám chữa bệnh từ xa"
            :visible.sync="applyForDialogVisible"
            width="30%"
            :before-close="handleClose">
            <el-form
              :model="bizFormModel"
              :rules="formRules"
              ref="registrationForm"
              label-width="120px"
              label-position="right"
              style="marginTop: 10px"
              class="edit-form"
            >
              <el-row>
                <el-form-item label="医院名称" prop="hospital.OrganizeId">
                  <el-select
                    v-model="bizFormModel.hospital"
                    @change="getks()"
                    value-key="Name"
                    filterable
                    clearable
                    placeholder="Vui lòng chọn bệnh viện"
                    @clear="
                    bizFormModel.hospital = {
                      OrganizeId: null,
                      Name: null,
                    }
                  "
                  >
                    <el-option
                      v-for="hospital in this.selects_mechanism"
                      :key="hospital.OrganizeId"
                      :label="hospital.Name"
                      :value="hospital"
                    ></el-option>
                  </el-select>
                </el-form-item>
                <el-form-item label="Tên bệnh nhân" prop="name">
                  <el-input
                    :disabled="true"
                    v-model="bizFormModel.patientId.name"
                  ></el-input>
                </el-form-item>
                <el-form-item label="Giới tính" prop="gender.name">
                  <el-input
                    :disabled="true"
                    v-model="bizFormModel.patientId.gender.name"
                  ></el-input>
                </el-form-item>
                <el-form-item label="Số CMND/CCCD" prop="card">
                  <el-input
                    :disabled="true"
                    v-model="bizFormModel.card"
                  ></el-input>
                </el-form-item>
                <el-form-item label="诊疗科室" prop="clinicOffice">
                  <el-select
                    v-model="bizFormModel.clinicOffice"
                    @change="getys()"
                    value-key="ks"
                    filterable
                    clearable
                    placeholder="Vui lòng chọn khoa/phòng"
                    @clear="
                    bizFormModel.clinicOffice = {
                      ks: null,
                      ksmc: null,
                    }
                  "
                  >
                    <el-option
                      v-for="clinicOffice in this.departments_List"
                      :key="clinicOffice.ks"
                      :label="clinicOffice.ksmc"
                      :value="clinicOffice"
                    ></el-option>
                  </el-select>
                </el-form-item>
                <el-form-item label="诊疗医生" prop="doctor">
                  <el-select
                    v-model="bizFormModel.doctor"
                    value-key="ysgh"
                    filterable
                    clearable
                    placeholder="Vui lòng chọn bác sĩ"
                    @clear="
                    bizFormModel.doctor = {
                      ysgh: null,
                      ysxm: null,
                    }
                  "
                  >
                    <el-option
                      v-for="doctor in this.doctors_List"
                      :key="doctor.ysgh"
                      :label="doctor.ysxm"
                      :value="doctor"
                    ></el-option>
                  </el-select>
                </el-form-item>
                <el-form-item label="诊疗日期:" style="margin-right: 3px;">
                  <el-date-picker
                    v-model="bizFormModel.diagnosisTime"
                    type="datetime"
                    placeholder="Chọn ngày giờ"
                    align="right"
                    :picker-options="pickerOptions">
                  </el-date-picker>
                </el-form-item>
              </el-row>
            </el-form>
            <span slot="footer" class="dialog-footer">
          <el-button @click="applyForDialogVisible = false">Hủy</el-button>
          <el-button type="primary" @click="savedRequest">Xác nhận</el-button>
        </span>
          </el-dialog>
        </el-tab-pane>
<!--        <el-tab-pane label="预约分诊" name="yyfz">-->
<!--          <el-row style="margin-top:40px;">-->
<!--            <el-col :span="7" v-for="(item,index) in registrationList" :key="index" :offset="1"-->
<!--                    style="padding-top:20px;">-->
<!--              <div>-->
<!--                <el-card class="box-card" :body-style="{ padding:'0px'}" shadow="hover">-->
<!--                  <div style="width:400px; height:160px; padding-left:100px; padding-top:20px;">-->
<!--                    <span style="font-size: 16px; font-weight:540;">{{ item.patientId.name }}  </span>-->

<!--                    <span-->
<!--                      style="font-size: 14px; color: darkgray;">   {{-->
<!--                        item.patientId.gender.name-->
<!--                      }}   {{ item.patientId.age }}{{ "tuổi" }}</span>-->
<!--                    <br/>-->
<!--                    <br/>-->
<!--                    <span-->
<!--                      style="font-size: 14px; color: darkgray;">登记科室：{{-->
<!--                        item.clinicOffice.name-->
<!--                      }} {{ "(" + item.treatType.name + ")" }}</span>-->
<!--                    <br/>-->
<!--                    <span-->
<!--                      style="font-size: 14px; color: darkgray;">Thời gian đăng ký：{{-->
<!--                        $moment(item.createDate).format("HH:mm")-->
<!--                      }}</span>-->

<!--                  </div>-->

<!--                  <div class="bottom clearfix">-->
<!--                    <el-row style="padding-left:40px;">-->
<!--                      <el-col :span="10">-->
<!--                        <el-button type="text" style="font-size: 15px; color:darkgray;" @click="buttonyz(item)">预诊-->
<!--                        </el-button>-->
<!--                      </el-col>-->
<!--                      <el-col :span="10">-->
<!--                        <el-button type="text" style="font-size: 15px; color:darkgray;" @click="buttonys(item)">选择医生-->
<!--                        </el-button>-->
<!--                      </el-col>-->
<!--                    </el-row>-->
<!--                  </div>-->
<!--                </el-card>-->
<!--              </div>-->
<!--            </el-col>-->
<!--          </el-row>-->
<!--        </el-tab-pane>-->
        <el-tab-pane label="患者档案" name="hzdangan">
          <!-- 直接使用封装好的患者管理组件 -->
          <PatientManage />
        </el-tab-pane>

      </el-tabs>
    </el-card>
  </el-row>
</template>

<script>
  import {validatenull} from "@/utils/validate";
  import {saveDiagnosis, getRegistrationId} from '@/api/outpatient/remoteDiagnosisTreatment';
  import {
    listRegistrationPage,
    getRegistrationById,
    deleteRegistration,
    updateStatus,
    refundRegistrationPay,
  } from "@/api/outpatient/registration";
  import listViewMixin from '@/mixins/listViewMixin'
  import { listResourcePermission } from '@/api/admin/common/permission'
  import { mapPermissions } from '@/utils/searchParamsBuilder'
  import RegistrationForm from "./registrationForm";
  import YsForm from "./ysForm";
  import BeforehandForm from "./beforehandForm";
  import {listClinicOfficeAll} from "@/api/org/clinicOffice";
  import {listPatientAll} from "@/api/outpatient/patient";
  import {listDoctorsAll} from "@/api/outpatient/registration";
  import ExportExcelButton from "@/components/ExportExcelButton";
  import ViewColumnsSelect from "@/views/components/ViewColumnsSelect";
  import QueryForm from "@/views/components/queryForm";
  import MainUI from "@/views/components/mainUI";
  import History from "@/views/components/history";
  import {listDictItemAll} from "@/api/sys/dictItem";
  import { getDictItemsByCode, DICT_CODE } from '@/utils/dictCache'
  import {getTollInfoByRegistrationId, saveTollInfo} from "@/api/toll/tollInfo";
  import axios from 'axios'
  import Vue from "vue";
  Vue.prototype.$axios = axios;
  import { getLocalToken } from '@/utils/auth';
  import request from '@/utils/request'
  import PatientManage from "../patient/PatientManage.vue";

  const loginForm = {Data:"API.Manage",AppId:"Oh_Newtouch_Clinic"}
  const config = {
    headers: {
      Token: getLocalToken()
    }
  }
  const institution = {
    Data: {TopOrganizeId: null},
    AppId: "Oh_Newtouch_Clinic",
    Timestamp: new Date()
  }



  const doctors = {
    appId: "Oh_Newtouch_Clinic",
    organizeId: "6d5752a7-234a-403e-aa1c-df8b45d3469f",
    timestamp: new Date(),
    data: {
      ks:""
  }
  }

  export default {

    extends: MainUI,
    mixins: [listViewMixin],
    components: {
      PatientManage,
      RegistrationForm,
      ExportExcelButton,
      ViewColumnsSelect,
      QueryForm,
      History,
      YsForm,
      BeforehandForm,
    },
    data() {
      return {
        longRange:{Data:"API.Manage",AppId:"Oh_Newtouch_Clinic"},

        tokenData:null,
        ColumnWidth: 200,
        filerData: {
          pickerOptions: {
            // disabledDate(time) {
            //   return time.getTime() > Date.now() - 8.64e6;
            // },
          },
        },
        // exitNumberDate:"",      //退号时间
        activeName: "djxx",
        listApi: listRegistrationPage,
        getApi: getRegistrationById,
        deleteApi: deleteRegistration,
        entityName: 'Registration',
        permissionPrefix: 'registration',
        queryTypes: {
          department_id: "=",
          patient_id: "=",
          doctor_id: "=",
        },
        queryModel: {
          department: {
            // Khoa
            id: null,
            name: "",
          },
          patientId: {
            // Bảng cha Bệnh nhân
            id: null,
            name: "",
          },
          doctor: {
            // Bác sĩ
            id: null,
            name: "",
          },
          hospital: {
            // 医院
            OrganizeId: null,
            Name: "",
          },
          createDate: [this.addCreateDate(), this.$moment(new Date()).endOf("day")],//Khoảng thời gian
          cource: ''
        },
        source_List: [
          {
            name: 'Tại chỗ',
            value: 'registrationSource_0'
          },
          {
            name: 'Trực tuyến',
            value: 'registrationSource_1'
          }
        ],
        returnToll: {},
        statusSearch: {
          params: [{columnName: "company_id", queryType: "=", value: ""}],
          offset: 0,
          limit: 100,
          columnName: "", // Tên trường sắp xếp
          order: "", // Sắp xếp
        },
        statusList: null,
        registrationTotal: 0,
        registrationList: [],

        key_department: 1, // el-cascader key
        department_List: [], // Khoa
        patientId_List: [], // Bệnh nhân
        doctor_List: [], // Bác sĩ

        oprColumnWidth: 140, // Độ rộng cột thao tác
        tableId: "1008534118685450402",
        schemeId: "1008534118685450425",
        BASE_API: process.env.BASE_API,
        payType_List: [], //支付类型集合
        refundRegistrationId: 0,
        refundRegistraionVisible: false, //是否显示退挂号费弹框
        refundTotalFee: 0, //退费费用金额
        refundReistrationPayType: "", //退费支付方式
        refundRegistrationRemarks: "", //退费备注信息

        refundStatus: "registrationStatus_2", //退费状态默认值

        bizFormModel: {
          id: '',
          status: '',
          diagnosisTime:null,
          patientId: {
            gender: {}
          },
          doctor: {},
          clinicOffice: {},
          hospital: {}
        },//网上签到封装对象
        departments_List: [], // Khoa
        doctors_List: [], // Bác sĩ
        selects_doctor: [],
        selects_mechanism: [],
        signDialogVisible: false,
        keys_department: 1, // el-cascader key
        applyForDialogVisible: false, // 远程诊疗申请弹框变量
        formRules: {

          "hospital.Name": [
            {required: true, message: "Vui lòng chọn bệnh viện", trigger: "change"},
          ],
          "doctor.name": [
            {required: true, message: "Vui lòng chọn bác sĩ", trigger: "change"},
          ],
          "clinicOffice.id": [
            {required: true, message: "Vui lòng chọn khoa/phòng", trigger: "change"},
          ],
        },
        types: "",
      };
    },
    methods: {
      handleClick(tab, event) {
        if (tab.name === "djxx") {
          this.pageInit();
        } else if (tab.name === "yyfz") {
          this.pageInitnew();
        }

      },
      buttonyz(data) {
        this.$refs.beforehandForm.openViewbeforehandDialog(data);
      },
      buttonys(data) {
        debugger
        if (this.types !== "") {
          this.$refs.ysForm.openViewysDialog(data, this.types);
        } else {
          this.$message.warning("请先进行预诊！")
        }

      },

      // 打开远程诊疗申请窗口
      async applyForStudentExcel(index, row) {
        await getRegistrationId(row.id).then((res) => {
          if (res.data !== null) {
            this.applyForDialogVisible = false
            this.$message.warning("该用户已申请远程诊疗，请前往远程诊疗管理界面查看!")
            this.loadData();
          } else {
            this.applyForDialogVisible = true
          }
        }).catch((error) => {
          this.$message.error(error)
        })
        // Lấy phòng khám từ xatoken
        await this.$axios.post('/token/Auth/GetAppFrienAuthToken',loginForm,config)
          .then((response) => {
            this.tokenData = response.data.BusData.data.Token
          })
          .catch((error) => {
          });

        // 获取远程诊所医院
        await this.$axios.post('apis/SysOrg/GetOrgList',institution,{
          headers: {
            Authorization: "Bearer "+this.tokenData
          }
        })
          .then((response) => {
            this.bizFormModel.hospital = response.data.BusData.data
            this.selects_mechanism = this.bizFormModel.hospital
          })
          .catch((error) => {
          });

        // 获取远程诊所科室
        await this.$axios.post('apis/Booking/GetOutpDepartment',{
          Data: null,
          OrganizeId: this.bizFormModel.hospital.OrganizeId,
          AppId: "Oh_Newtouch_Clinic",
          Timestamp: new Date()
        },{
          headers: {
            Authorization: "Bearer "+this.tokenData
          }
        })
          .then((response) => {
            this.departments_List = response.data.BusData.data
          })
          .catch((error) => {
          });

        // Lấy bác sĩ phòng khám từ xa
        await this.$axios.post('apis/Booking/GetOutpDoctor',{
            appId: "Oh_Newtouch_Clinic",
            organizeId: this.bizFormModel.hospital.OrganizeId,
            timestamp: new Date(),
            data: {
              ks:this.bizFormModel.clinicOffice.ks
            }
          },{
          headers: {
            Authorization: "Bearer "+this.tokenData
          }
        })
          .then((response) => {
            this.doctors_List = response.data.BusData.data
          })
          .catch((error) => {
          });

        await getRegistrationById(row.id)
          .then((responseData) => {
            if (responseData.code === 100) {
              this.bizFormModel = responseData.data
            } else {
              this.showMessage(responseData);
            }
            this.resetLoad();
          })
          .catch((error) => {
            this.outputError(error);
          });
      },
      // 远程诊疗申请保存
      savedRequest() {
        this.bizFormModel.hospital = this.bizFormModel.hospital.Name
        this.bizFormModel.hospitalId = this.bizFormModel.doctor.organizeId
        this.bizFormModel.name = this.bizFormModel.patientId.name
        this.bizFormModel.nameId = this.bizFormModel.patientId.id
        this.bizFormModel.registrationId = this.bizFormModel.id
        this.bizFormModel.gender = this.bizFormModel.patientId.gender.name
        this.bizFormModel.department = this.bizFormModel.clinicOffice.ksmc
        this.bizFormModel.departmentId = this.bizFormModel.clinicOffice.ks
        this.bizFormModel.medic = this.bizFormModel.doctor.ysxm
        this.bizFormModel.medicId = this.bizFormModel.doctor.ysgh
        this.bizFormModel.status = '0'
        this.bizFormModel.id = null
        saveDiagnosis(this.bizFormModel).then((res) => {
          if (res.code === 100) {
            this.applyForDialogVisible = false
            this.$message.success("Lưu thành công！")
            this.loadData();
          } else {
            this.$message.error("Lưu thất bại，Vui lòng liên hệ backend!")
          }
        }).catch((error) => {
          this.$message.error(error)
        })
      },

      getks(){
        this.$axios.post('apis/Booking/GetOutpDepartment',{
          Data: null,
          OrganizeId: this.bizFormModel.hospital.OrganizeId,
          AppId: "Oh_Newtouch_Clinic",
          Timestamp: new Date()
        },{
          headers: {
            Authorization: "Bearer "+this.tokenData
          }
        })
          .then((response) => {
            this.departments_List = response.data.BusData.data
          })
          .catch((error) => {
          });
      },

      getys(){
        // Lấy bác sĩ phòng khám từ xa
        this.$axios.post('apis/Booking/GetOutpDoctor',{
          appId: "Oh_Newtouch_Clinic",
          organizeId: this.bizFormModel.hospital.OrganizeId,
          timestamp: new Date(),
          data: {
            ks:this.bizFormModel.clinicOffice.ks
          }
        },{
          headers: {
            Authorization: "Bearer "+this.tokenData
          }
        })
          .then((response) => {
            this.doctors_List = response.data.BusData.data
          })
          .catch((error) => {
          });
      },

      rettonys(value) {
        this.types = value;
        this.pageInitnew();
      },
      chargeClick(refundData) {
        this.refundRegistrationId = refundData.id;
        this.refundTotalFee = refundData.registrationFee;
        this.refundReistrationPayType = refundData.payType.value;
        this.refundRegistrationRemarks = "";
        this.returnToll = refundData
        this.refundRegistrationPayClick()
      },
      sortDoc(clinicOffice, officename) {
        if (this.doctors_List.length !== 0) {
          //Lọc bác sĩ
          this.selects_doctor = this.doctors_List.filter(doctor => doctor.office === clinicOffice)
        }
      },
      reset() {
        this.$refs.queryForm.resetFields()
        this.onSearch()
      },
      openRegistration(id) {
        let page = window.open(this.BASE_API + '/ureport/preview?_u=Newtouch:registration.ureport.xml&_t=0&registrationId=' + id)
        page.onload = () => {
          page.document.title = "新致云诊所"
        }
      },
      async refundRegistrationPayClick() {
        let toll = {
          company: this.returnToll.company,
          registrationFeeId: this.returnToll.id,
          amountReceivable: this.returnToll.registrationFee, //Phải thu/phải trả
          amountReceived: this.returnToll.registrationFee,  //Thực thu
          paymentType: this.returnToll.payType,//Loại thanh toán
          patient: this.returnToll.patientId,
          amountStatus: {
            name: 'Đã hoàn phí',
            value: 'amountStatus_2'
          },    //Trạng thái chứng từ
          tollType: {
            name: 'Đăng ký khám',
            value: 'tollType_4'
          },    //Loại thu phí
          remarks: ""   //Thông tin ghi chú
        }

        const res = await refundRegistrationPay(
          this.refundRegistrationId,
          this.refundStatus,
          this.refundReistrationPayType,
          this.refundRegistrationRemarks,
        );
        if (res.code === 100) {
          //退费成功后修改费用表的状态
          // getTollInfoByRegistrationId(this.refundRegistrationId).then((res)=>{
          //   if(res.code=="100"){
          //     let tollMesssage=res.data
          //     tollMesssage.amountStatus.name="Đã hoàn phí"
          //     tollMesssage.amountStatus.value="amountStatus_2"
          //     saveTollInfo(tollMesssage).then((res)=>{
          //           if(res.code=="100"){
          //           }
          //         }).catch(()=>{})
          //   }
          // }).catch(()=>{})
          //Chèn dữ liệu vào bảng thu phí
          if (res.data === 1) {
            if (this.returnToll.freeRegistrationFee !== "1") {
              saveTollInfo(toll).then((res) => {
                if (res.code === 100) {

                }
              }).catch(() => {
              })
            }

            this.loadData();
            this.refundRegistraionVisible = false;
          } else {
            this.$message.error(res.data)
          }
        }
      },
      //显示退费弹框
      showRefundRegistrationDialog(refundData) {
        this.returnToll = refundData
        this.refundRegistrationId = refundData.id;
        this.refundTotalFee = refundData.registrationFee;
        this.refundReistrationPayType = refundData.payType.value;
        this.refundRegistrationRemarks = "";
        this.refundRegistraionVisible = true;
      },
      async updateStatusId(id, status, doctor, clinicOffice, subscribeDate) {
        let flag = false
        //判断预约时间是否在当天
        if (subscribeDate) {
          let orderDate = new Date(subscribeDate);
          let orderYear = orderDate.getFullYear()
          let orderMonth = orderDate.getMonth() + 1
          let orderDay = orderDate.getDate()
          let newOrderTime = orderYear + "-" + orderMonth + "-" + orderDay

          //获取当前时间
          let now = new Date();
          let nowYear = now.getFullYear();
          let nowMonth = now.getMonth() + 1;
          let nowDay = now.getDate();
          let newTime = nowYear + "-" + nowMonth + "-" + nowDay
          if (newOrderTime !== newTime) {
            flag = true
          }
        }

        if (flag) {
          this.$message.warning("还未到预约时间,不能进行签到！")
          return
        }

        this.bizFormModel.id = id
        if (status === "registrationStatus_0") this.bizFormModel.status = "registrationStatus_2";
        if (status === "registrationStatus_3") this.bizFormModel.status = "registrationStatus_0";

        if (doctor && clinicOffice) {
          updateStatus(this.bizFormModel.id, this.bizFormModel.status, clinicOffice.id, doctor.id).then((res) => {
            if (res.code === 100) {

              this.signDialogVisible = false
              this.loadData();


            } else {
              this.$message.error("Lưu thất bại，Vui lòng liên hệ backend")
            }
          }).catch((error) => {
            this.$message.error(error)
          })
          return;
        }

        this.signDialogVisible = true;

      },
      appendSearchParams() {
        this.search.params = [
          { columnName: 'company_id', queryType: '=', value: currentUser.company.id }
        ]
        if (this.moreCodition) {
          this.search.params = this.search.params.concat(this.compositeCondition())
        } else {
          this.search.params.push({ columnName: 'department_id', queryType: '=', value: validatenull(this.queryModel.department.id) ? '' : this.queryModel.department.id })
          this.search.params.push({ columnName: 'patient_id', queryType: '=', value: validatenull(this.queryModel.patientId.id) ? '' : this.queryModel.patientId.id })
          this.search.params.push({ columnName: 'doctor_id', queryType: '=', value: validatenull(this.queryModel.doctor.id) ? '' : this.queryModel.doctor.id })
          this.search.params.push({ columnName: 'status', queryType: '=', value: this.queryModel.status !== undefined ? this.queryModel.status.value : '' })
          this.search.params.push({ columnName: 'source.value', queryType: '=', value: this.queryModel.source !== undefined ? this.queryModel.source : '' })
          this.search.params.push({ columnName: 'create_date', queryType: '>=', value: this.queryModel.createDate ? this.$moment(this.queryModel.createDate[0]).format('YYYY-MM-DD HH:mm:ss') : '' })
          this.search.params.push({ columnName: 'create_date', queryType: '<=', value: this.queryModel.createDate ? this.$moment(this.queryModel.createDate[1]).format('YYYY-MM-DD HH:mm:ss') : '' })
        }
        this.pushDataPermissions(this.search.params, this.$route.meta.routerId, this.tableId)
      },
      handleListResponse(responseData) {
        this.registrationTotal = responseData.data.total
        this.registrationList = responseData.data.rows
      },
      async pageInitnew() {
        this.setLoad();
        try {
          // debugger;
          this.initOptions(this.queryModel);
          this.search.order = " create_date ";
          this.search.params = [
            {
              columnName: "company_id",
              queryType: "=",
              value: currentUser.company.id,
            },
          ];
          this.search.params.push({
            columnName: "status",
            queryType: "=",
            value: "registrationStatus_5",
          });
          this.search.params.push({
            columnName: "doctor_id",
            queryType: "is null",
            value: null,
          });
          this.search.params.push({
            columnName: "create_date",
            queryType: ">=",
            value: this.$moment(new Date()).format(
              "YYYY-MM-DD 00:00:00"
            ),
          });
          this.search.params.push({
            columnName: "create_date",
            queryType: "<=",
            value: this.$moment(new Date()).format(
              "YYYY-MM-DD 23:59:59"
            ),
          });

          // Quyền dữ liệu: Bảng đăng ký khámregistration
          this.pushDataPermissions(
            this.search.params,
            this.$route.meta.routerId,
            this.tableId
          );
          let [listRegistrationRespData, listPermissionRespData] =
            await Promise.all([
              listRegistrationPage(this.search),
              listResourcePermission(this.$route.meta.routerId),
            ]);
          if (
            listRegistrationRespData.code === 100 &&
            listPermissionRespData.code === 100
          ) {
            this.registrationTotal = listRegistrationRespData.data.total;
            this.registrationList = listRegistrationRespData.data.rows;
            this.permission = {
              ...mapPermissions(listPermissionRespData.data, 'registration'),
              skip: !!listPermissionRespData.data.find(item => item.permission === 'registration:jump')
            }
          } else {
            this.showMessage(
              listPermissionRespData.code !== 100
                ? listPermissionRespData
                : listRegistrationRespData
            );
          }
          this.resetLoad();
        } catch (error) {
          this.outputError(error);
        }
      },
      async pageInit() {
        this.setLoad()
        try {
          if (this.initOptions) {
            this.initOptions(this.queryModel)
          }
          this.search.params = []
          if (this.appendSearchParams) {
            this.appendSearchParams()
          }
          const prefix = this.permissionPrefix || this.entityName
          const [listRespData, permissionRespData] = await Promise.all([
            this.listApi(this.search),
            listResourcePermission(this.$route.meta.routerId)
          ])
          if (listRespData.code === 100 && permissionRespData.code === 100) {
            this.handleListResponse(listRespData)
            this.permission = {
              ...mapPermissions(permissionRespData.data, prefix),
              skip: !!permissionRespData.data.find(item => item.permission === `${prefix}:jump`)
            }
            this.ColumnWidth = 200
            // Load patient permissions for PatientManage component
            const patientPermissionRes = await listResourcePermission('patient-router-id')
            if (patientPermissionRes.code === 100) {
              this.patientPermission = {
                view: !!patientPermissionRes.data.find(item => item.permission === 'patient:read'),
                add: !!patientPermissionRes.data.find(item => item.permission === 'patient:create'),
                edit: !!patientPermissionRes.data.find(item => item.permission === 'patient:update'),
                remove: !!patientPermissionRes.data.find(item => item.permission === 'patient:delete'),
                export: !!patientPermissionRes.data.find(item => item.permission === 'patient:export'),
                skip: false
              }
            }
          } else {
            this.showMessage(permissionRespData.code !== 100 ? permissionRespData : listRespData)
          }
          this.resetLoad()
        } catch (error) {
          this.outputError(error)
        }
      },
      onCreateRegistration() {
        this.$refs.registrationForm.openAddRegistrationDialog(this.permission.remove, this.permission.view)
      },
      onCopyRegistration(index, row) {
        this.setLoad()
        getRegistrationById(row.id).then(responseData => {
          if (responseData.code === 100) {
            this.$refs.registrationForm.openCopyRegistrationDialog(responseData.data, this.permission.remove, this.permission.view)
          } else {
            this.showMessage(responseData)
          }
          this.resetLoad()
        }).catch(error => {
          this.outputError(error)
        })
      },
      initOptions(This) {
        this.key_department++;
        let department_search = {
          params: [
            {
              columnName: "company_id",
              queryType: "=",
              value: currentUser.company.id,
            },
          ],
        };
        // Toán tử điều kiện của trường phản hồi，Thay bằng toán tử của trường kích hoạt
        department_search.params.forEach((item) => {
          if (this.queryTypes[item.columnName]) {
            item.queryType = this.queryTypes[item.columnName];
          }
        });
        // Trên bảng tương ứng của trườngfilterĐiều kiện
        department_search.params.push.apply(department_search.params, []);
        // Quyền dữ liệu: Phòng banorg_department
        this.pushDataPermissions(
          department_search.params,
          this.$route.meta.routerId,
          "52676365136650250"
        );
        this.department_List.splice(0, this.department_List.length);
        listClinicOfficeAll(department_search).then((responseData) => {
          this.department_List = responseData.data;
        });
        let patientId_search = {
          params: [],
        };
        // Toán tử điều kiện của trường phản hồi，Thay bằng toán tử của trường kích hoạt
        patientId_search.params.forEach((item) => {
          if (this.queryTypes[item.columnName]) {
            item.queryType = this.queryTypes[item.columnName];
          }
        });
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
        let doctor_search = {
          params: [
            {
              columnName: "department_id",
              queryType: "=",
              value: currentUser.department.id,
            },
          ],
        };
        // Toán tử điều kiện của trường phản hồi，Thay bằng toán tử của trường kích hoạt
        doctor_search.params.forEach((item) => {
          if (this.queryTypes[item.columnName]) {
            item.queryType = this.queryTypes[item.columnName];
          }
        });
        // Trên bảng tương ứng của trườngfilterĐiều kiện
        doctor_search.params.push.apply(doctor_search.params, []);
        // Quyền dữ liệu:  Người dùngsys_user
        this.pushDataPermissions(
          doctor_search.params,
          this.$route.meta.routerId,
          "4004"
        );
        this.doctor_List.splice(0, this.doctor_List.length);
        listDoctorsAll(doctor_search).then((responseData) => {
          this.doctor_List = responseData.data;
        });

        //Trạng thái đăng ký khám
        getDictItemsByCode(DICT_CODE.REGISTRATION_STATUS).then((data) => {
          this.statusList = data;
        });


        //Truy vấn loại thanh toán
        getDictItemsByCode(DICT_CODE.PAY_TYPE).then((data) => {
          this.payType_List = data;
        });
        //Truy vấn khoa/phòng có thể đăng ký khám
        this.keys_department++;
        let departments_search = {
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
        departments_search.params.push.apply(departments_search.params, []);
        // Quyền dữ liệu: Phòng banorg_department
        this.pushDataPermissions(
          departments_search.params,
          this.$route.meta.routerId,
          "52676365136650250"
        );
        this.departments_List.splice(0, this.departments_List.length);//Yêu cầu truy vấn khoa/phòng đã đưa vào yêu cầu truy vấn bác sĩ

        //Truy vấn bác sĩ ngoại trú
        let doctors_search = {
          params: [
            {
              columnName: "department_id",
              queryType: "=",
              value: currentUser.department.id,
            },
          ],
        };
        // Trên bảng tương ứng của trườngfilterĐiều kiện
        doctors_search.params.push.apply(doctors_search.params, []);
        // Bảng có trường bị vô hiệu hóa
        doctors_search.params.push.apply(doctors_search.params, [
          {columnName: "is_locked", queryType: "=", value: "0"},
        ]);
        // Quyền dữ liệu:  Người dùngsys_user
        this.pushDataPermissions(
          doctors_search.params,
          this.$route.meta.routerId,
          "4004"
        );
        this.doctors_List.splice(0, this.doctors_List.length);
        listDoctorsAll().then((responseData) => {
          this.doctors_List = responseData.data;
          listClinicOfficeAll(departments_search).then((responseData) => {
            this.departments_List = responseData.data;
            for (let i = 0; i < this.departments_List.length; i++) {
              if (this.departments_List[i].isDefault === '1') {
                this.bizFormModel.clinicOffice.id = this.departments_List[i].id
                this.sortDoc(this.departments_List[i].id)
              }
            }
          });

        });
      },
      addCreateDate() {

        let myDate = new Date();
        let lw = new Date(myDate.getTime() - 1000 * 60 * 60 * 24 * 30); //Chữ số cuối cùng30có thể sửa，30nghĩa là ngày
        let lastY = lw.getFullYear();
        let lastM = lw.getMonth() + 1;
        let lastD = lw.getDate();
        let housrs = lw.getHours();
        let minutes = lw.getMinutes()
        let seconds = lw.getSeconds()
        let startData =
          lastY +
          "-" +
          (lastM < 10 ? "0" + lastM : lastM) +
          "-" +
          (lastD < 10 ? "0" + lastD : lastD) +
          " " + housrs + ":" + minutes + ":" + seconds //Ngày của 30 ngày trước
        return startData;

      },
      signIn() {
        updateStatus(this.bizFormModel.id, this.bizFormModel.status, this.bizFormModel.clinicOffice.id, this.bizFormModel.doctor.id).then((res) => {
          if (res.code === 100) {

            this.signDialogVisible = false
            this.loadData();


          } else {
            this.$message.error("Lưu thất bại，Vui lòng liên hệ backend")
          }
        }).catch((error) => {
          this.$message.error(error)
        })
      }
    },
    watch: {
      "queryModel.department.id"(val, oldVal) {
        if (val !== oldVal) {
          let This = this.queryModel;
          this.queryModel.doctor = {
            // Bác sĩ
            id: null,
            name: null,
          };
          let doctor_search = {
            params: [
              {
                columnName: "department_id",
                queryType: "=",
                value: currentUser.department.id,
              },
            ],
          };
          // Toán tử điều kiện của trường phản hồi，Thay bằng toán tử của trường kích hoạt
          doctor_search.params.forEach((item) => {
            if (this.queryTypes[item.columnName]) {
              item.queryType = this.queryTypes[item.columnName];
            }
          });
          // Trên bảng tương ứng của trườngfilterĐiều kiện
          doctor_search.params.push.apply(doctor_search.params, []);
          // Quyền dữ liệu:  Người dùngsys_user
          this.pushDataPermissions(
            doctor_search.params,
            this.$route.meta.routerId,
            "4004"
          );
          this.doctor_List.splice(0, this.doctor_List.length);
          listDoctorsAll(doctor_search).then((responseData) => {
            this.doctor_List = responseData.data;
          });
        }
      },
      registrationList(val) {
        // if(val){
        //   this.$nextTick(() => {
        //       this.$refs.mutipleTable.doLayout();
        //   });
        // }
      },
    },
    updated() {
      if (this.$refs.mutipleTables) {
        this.$nextTick(() => {
          this.$refs.mutipleTables.doLayout();
        });
      }
    },
    created() {
      this.pageInit();
    },
    mounted() {
      this.pageInit();
    },
  };
</script>
<style lang="scss" scoped>

  .page-container {
    padding: 0;
  }

  .btn,
  .btn1,
  .btn2 {
    display: flex;
    justify-content: center;
    align-items: center;
    color: #01a5e4;
    margin: 0;
  }

  .btn span:nth-child(1) {
    color: #01a5e4;
    padding: 0px 10px;
  }

  .btn span:nth-child(1) {
    color: #01a5e4;
    padding: 0px 10px;
  }

  .btn span:nth-child(2) {
    color: #14ca04;
    padding: 0px 10px;
  }

  .btn1 span:nth-child(3) {
    color: #e32929;
    padding: 0px 10px;
  }

  .btn2 {
    color: #01a5e4;
  }

  #refundTotalFee_Container {
    display: flex;
    justify-content: flex-start;
    padding: 20px;
    margin: 10px;
    background-color: rgba(207, 227, 236, 0.803921568627451);
    border-radius: 7px;
  }

  #refundTotalFee_Title {
    font-family: "Arial Normal", "Arial", sans-serif;
    font-weight: 400;
    font-style: normal;
    font-size: 21px;
    margin-right: 8px;
  }

  #refundTotalFee_Fee {
    font-weight: 800;
    font-style: black;
    font-size: 25px;
    margin-top: -4px;
  }

  .refund-registration-flex-start {
    display: flex;
    justify-content: flex-start;
    padding: 0 20px;
    margin: 10px;
  }

  .refund-registration-p {
    width: 80px;
    text-align: right;
    margin-right: 10px;
  }

  .refund-registration-content {
    widows: 250px;
    margin-top: 15px;
  }


  ::v-deep .el-table__fixed-right-patch {
    width: 5px !important;
  }

  ::v-deep .el-table colgroup col[name='gutter'] {
    width: 5px !important;
  }

  ::v-deep .el-table__body {
    width: 100% !important;
  }

  ::v-deep .el-table__fixed-body-wrapper {
    top: 47px !important;
  }

  .registration-table {
    // Cài đặt bảngheaderChiều cao của
    ::v-deep th {
      height: 44px;
      vertical-align: middle !important;
    }

    ::v-deep th.gutter:last-of-type {
      height: 0 !important;
    }

    // Cài đặt bảngbodyChiều cao của
    ::v-deep .el-table__body-wrapper {
      //Khắc phục hiển thị dữ liệu vượt quábodychiều cao không cuộn đượcbug
      overflow-y: auto;
      // Phần trừ đi là bảngheaderChiều cao của
      height: calc(100vh - 350px - 44px) !important;
    }

    ::v-deep .el-table__fixed-right {
      height: 100% !important;
    }
    
    // 修复操作列按钮对齐
    ::v-deep .el-table__column--fixed-right .cell {
      display: flex !important;
      align-items: center !important;
      justify-content: center !important;
      flex-wrap: wrap !important;
      gap: 4px !important;
      height: 100% !important;
    }
    
    // 修复表格行高度和对齐
    ::v-deep .el-table__row {
      height: 48px !important;
    }
    
    // 修复单元格内容垂直居中
    ::v-deep .el-table__cell {
      vertical-align: middle !important;
    }
    
    // 修复表头对齐
    ::v-deep .el-table__header th {
      vertical-align: middle !important;
    }

  }


  .text {
    font-size: 14px;

  }

  .item {
    padding: 18px 0;
  }

  .bottom {
    background-color: rgba(213, 218, 223, 0.87);
    border: 1px solid beige;
    margin-top: 13px;
    height: 40px;
    line-height: 35px;
  }


  .clearfix:before,
  .clearfix:after {
    display: table;
    content: "";
  }

  .clearfix:after {
    clear: both;
  }

  .box-card {
    margin-right: 20px;
    // width: 400px;
    // height: 200px;
  }
</style>
