<template>
  <el-dialog
    class="beforehandForm"
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
      ref="beforehandForm"
      label-width="120px"
      label-position="right"
      style="margintop: 10px"
      class="edit-form"
    >
      <el-tabs
        v-model="activeName"
        type="border-card"
        @tab-click="handleClick"
        style="width: 100%; height: 100%"
      >
        <el-tab-pane label="Thông tin dấu hiệu sinh tồn" name="tzxx">
          <span slot="label">Thông tin dấu hiệu sinh tồn</span>
          <div class="registrationItemBox">
            <h3></h3>
            <el-row>
              <el-col :span="24 / 2">
                <el-form-item label="Nhiệt độ cơ thể" prop="temperature">
                  <div class="ipt-box">
                    <el-input
                      v-model="bizFormModel.registration.temperature"
                      :maxlength="45"
                      :placeholder="'Vui lòng nhập nhiệt độ cơ thể'"
                    ></el-input>
                    <span>&nbsp;℃</span>
                  </div>
                </el-form-item>
              </el-col>
              <el-col :span="24 / 2">
                <el-form-item label="Mạch" prop="pulse">
                  <div class="ipt-box">
                    <el-input
                      v-model="bizFormModel.registration.pulse"
                      :maxlength="45"
                      :placeholder="'Vui lòng nhập mạch'"
                    ></el-input>
                    <span>&nbsp;lần/min</span>
                  </div>
                </el-form-item>
              </el-col>
              <el-col :span="24 / 2">
                <el-form-item label="Nhịp thở" prop="breathe">
                  <div class="ipt-box">
                    <el-input
                      v-model="bizFormModel.registration.breathe"
                      :maxlength="45"
                      :placeholder="'Vui lòng nhập nhịp thở'"
                    ></el-input>
                    <span>&nbsp;lần/min</span>
                  </div>
                </el-form-item>
              </el-col>
              <el-col :span="24 / 2">
                <el-form-item label="Huyết áp" prop="bloodPressure">
                  <div class="ipt-box">
                    <el-input
                      v-model="bizFormModel.registration.bloodPressure"
                      :maxlength="45"
                      :placeholder="'Vui lòng nhập huyết áp'"
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
                    v-model="bizFormModel.registration.isGoHigharea"
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
                    bizFormModel.registration.isGoHigharea === '1'
                      ? formRules.highArea
                      : [
                          {
                            required: false,
                          },
                        ]
                  "
                >
                  <el-input
                    :disabled="bizFormModel.registration.isGoHigharea === '0'"
                    v-model="bizFormModel.registration.highArea"
                    :maxlength="128"
                    :placeholder="'Vui lòng nhập địa điểm vùng dịch'"
                  ></el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row>
              <el-col :span="24">
                <el-form-item label="Thông tin ghi chú" prop="remarks">
                  <el-input
                    v-model="bizFormModel.registration.remarks"
                    type="textarea"
                    :maxlength="255"
                    :placeholder="'Vui lòng nhập thông tin ghi chú'"
                    clearable
                  ></el-input>
                </el-form-item>
              </el-col>
            </el-row>
          </div>
        </el-tab-pane>
        <el-tab-pane label="Thông tin bệnh án" name="blxx">
            <span slot="label">Thông tin bệnh án</span>
            <el-form :label-position="labelPosition" label-width="80px" :model="bizFormModel.medicalRecord">
                <el-form-item label="Bác sĩ" :required="true" prop="doctor">
            <el-select
              v-model="bizFormModel.medicalRecord.doctor"
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
  <el-form-item label="Lý do khám chính" :required="true">
    <el-input type="textarea" v-model="bizFormModel.medicalRecord.patientTell"></el-input>
  </el-form-item>
  <el-form-item label="Bệnh sử hiện tại">
    <el-input type="textarea" v-model="bizFormModel.medicalRecord.nowHistory"></el-input>
  </el-form-item>
  <el-form-item label="Tiền sử bệnh">
    <el-input type="textarea" v-model="bizFormModel.medicalRecord.beforeHistory"></el-input>
  </el-form-item>
  <el-form-item label="Tiền sử cá nhân">
    <el-input type="textarea" v-model="bizFormModel.medicalRecord.personalHistory"></el-input>
  </el-form-item>
  <el-form-item label="Tiền sử dị ứng">
    <el-input type="textarea" v-model="bizFormModel.medicalRecord.allergyHistory"></el-input>
  </el-form-item>
  <el-form-item label="Tiền sử bệnh">
    <el-input type="textarea" v-model="bizFormModel.medicalRecord.diseaseHistory"></el-input>
  </el-form-item>
  <el-form-item label="Tiền sử hôn nhân/mang thai">
    <el-input type="textarea" v-model="bizFormModel.medicalRecord.pregnancyHistory"></el-input>
  </el-form-item>
  <el-form-item label="Tiền sử bệnh truyền nhiễm">
    <el-input type="textarea" v-model="bizFormModel.medicalRecord.infectiousHistory"></el-input>
  </el-form-item>
  <el-form-item label="Tiền sử kinh nguyệt">
    <el-input type="textarea" v-model="bizFormModel.medicalRecord.lunariaHistory"></el-input>
  </el-form-item>
  <el-form-item label="Tiền sử gia đình">
    <el-input type="textarea" v-model="bizFormModel.medicalRecord.familyHistory"></el-input>
  </el-form-item>
  <el-form-item label="Tiền sử phẫu thuật">
    <el-input type="textarea" v-model="bizFormModel.medicalRecord.surgeryHistory"></el-input>
  </el-form-item>
  <el-form-item label="Tiền sử truyền máu">
    <el-input type="textarea" v-model="bizFormModel.medicalRecord.transfusionHistory"></el-input>
  </el-form-item>
  <el-form-item label="Khám thực thể">
    <el-input type="textarea" v-model="bizFormModel.medicalRecord.physiqueCheck"></el-input>
  </el-form-item>
  <el-form-item label="Xét nghiệm bổ sung">
    <el-input type="textarea" v-model="bizFormModel.medicalRecord.assistCheck"></el-input>
  </el-form-item>
  <el-form-item label="Chẩn đoán cấp cứu">
    <el-input type="textarea" v-model="bizFormModel.medicalRecord.emergencyDiagnose"></el-input>
  </el-form-item>
  <el-form-item label="Hiệu quả cấp cứu">
    <el-input type="textarea" v-model="bizFormModel.medicalRecord.emergencyEffect"></el-input>
  </el-form-item>
  <el-form-item label="Xét nghiệm khác">
    <el-input type="textarea" v-model="bizFormModel.medicalRecord.otherCheck"></el-input>
  </el-form-item>
</el-form>
            </el-tab-pane>
      </el-tabs>
    </el-form>
    <div slot="footer">
      <el-button @click="onDialogclose">Đóng</el-button>
      <el-button type="primary" @click="buttoninser">Xác nhận</el-button>
    </div>
  </el-dialog>
</template>

<script>
import BaseUI from "@/views/components/baseUI";
import { listDictItemAll } from "@/api/sys/dictItem";
import {
  saveRegistration,
  listDoctorsAll,
  listDoctorsAllnew,
  registationupdatenew,
} from "@/api/outpatient/registration";
export default {
  extends: BaseUI,
  name: "beforehand-form",
  data() {
    return {
      activeName: "tzxx",
      dialogProps: {
        visible: false,
        action: "",
        title: "",
        source: {
          name: "Tại chỗ",
          value: "registrationSource_0",
        },
      },
      queryTypes: {
        department_id: "=",
        patient_id: "=",
        doctor_id: "=",
      },
      bizFormModel: {
          registration:{
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
          id: null,
          name: null,
        },
        id: null,
        pulse: null, // Mạch
        breathe: null, // Nhịp thở
        temperature: null, // Nhiệt độ cơ thể
        isGoHigharea: "0", // Có từng đến vùng dịch không
        highArea: null, // Địa điểm vùng dịch
        remarks: null, // Thông tin ghi chú
        doctor:null,
        status:null,
          },
          medicalRecord:{
              doctor:'',
              patientTell:'',
          },
      },
      formRules: {
        highArea: [
          { required: true, message: "Vui lòng nhập địa điểm vùng dịch", trigger: "blur" },
        ],
      },
      labelPosition: 'right',
        blxxformLabelAlign: {
            doctor:'',
        },
        doctor_List: [], // Bác sĩ
        status_List: [], // Trạng thái
    };
  },
  methods: {
    handleClick(tab, event) {
      //this.$message.success(this.tabPosition);

      if (tab.name === "tzxx") {

      } else if (tab.name === "schedulingmx") {


      } else if (tab.name === "schedulingzl") {

      }

    },
    buttoninser(){
        if(this.activeName==="tzxx" || this.activeName==="blxx"){
            if(this.bizFormModel.medicalRecord.patientTell!=='' && this.bizFormModel.medicalRecord.patientTell!=null){
                if(this.bizFormModel.medicalRecord.doctor!=='' && this.bizFormModel.medicalRecord.doctor!=null){
                    this.bizFormModel.registration.doctor=this.bizFormModel.medicalRecord.doctor;
                    this.bizFormModel.registration.status={name:"Chờ khám",value:"registrationStatus_0"};
                }
            }
            if(this.bizFormModel.medicalRecord.doctor==null && this.bizFormModel.medicalRecord.patientTell!=null){
                this.$message.warning("Vui lòng chọn bác sĩ!")
                return
            }
            if(this.bizFormModel.medicalRecord.doctor!=null && this.bizFormModel.medicalRecord.patientTell==null){
                this.$message.warning("Vui lòng điền lý do khám chính!")
                return
            }

            registationupdatenew(this.bizFormModel)
        .then((responseData) => {
          if (responseData.code === 100) {
            this.$emit('typeclick',"1");
            this.arrlistcreat();
            this.dialogProps.visible = false;

          } else {

            this.showMessage(responseData);
          }
          this.resetLoad();
        })
        .catch((error) => {
          this.outputError(error);
        });
        }


    },
    initys() {
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
      // listDoctorsAllnew().then((responseData) => {
      //    // debugger
      //   this.doctor_List = responseData.data;
      // })
    },
    arrlistcreat() {
      this.bizFormModel = {
          registration:{
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
          id: null,
          name: null,
        },

        pulse: "", // Mạch
        breathe: "", // Nhịp thở
        temperature: "", // Nhiệt độ cơ thể
        isGoHigharea: "0", // Có từng đến vùng dịch không
        highArea: "", // Địa điểm vùng dịch
        remarks: "", // Thông tin ghi chú
          },
          medicalRecord:{},
      };
    },
    onDialogOpen() {
      this.$nextTick(() => {
        this.$refs["beforehandForm"].clearValidate();
      });
    },
    onDialogclose() {
      //this.$emit("typeclick");
      this.dialogProps.visible = false;
      this.arrlistcreat();
    },
    openViewbeforehandDialog(item) {
      this.arrlistcreat();
      this.dialogProps.action = "view";
      this.dialogProps.title = "预诊";

      this.bizFormModel.registration.id = item.id;
      this.bizFormModel.registration.patientId.id = item.patientId.id;
      this.bizFormModel.registration.patientId.name = item.patientId.name;
      this.initys();
      this.dialogProps.visible = true;
    },
  },
};
</script>
<style lang="scss" scoped>
.ipt-box .el-input--mini {
  width: 70%;
}
.beforehandForm {
  .el-dialog__body {
    padding: 0 10px;
  }
}
</style>
