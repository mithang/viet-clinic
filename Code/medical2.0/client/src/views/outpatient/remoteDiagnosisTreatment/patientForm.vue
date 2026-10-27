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
                :disabled="true"
                v-model="bizFormModel.name"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24 / 2">
            <el-form-item label="Giới tính" prop="gender">
              <el-input
                :disabled="true"
                v-model="bizFormModel.gender"
              ></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24 / 2">
            <el-form-item label="Số giấy tờ của bệnh nhân" prop="card">
              <el-input
                :disabled="true"
                v-model="bizFormModel.card"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24 / 2">
            <el-form-item label="Bệnh viện đề nghị" prop="hospital">
              <el-input
                :disabled="true"
                v-model="bizFormModel.hospital"
              ></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24 / 2">
            <el-form-item label="Khoa/phòng đề nghị" prop="department">
              <el-input
                :disabled="true"
                v-model="bizFormModel.department"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24 / 2">
            <el-form-item label="Bác sĩ đề nghị" prop="medic">
              <el-input
                :disabled="true"
                v-model="bizFormModel.medic"
              ></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24 / 2">
            <el-form-item label="Trạng thái" prop="status" v-if="dialogProps.action === 'view'">
              <el-input :disabled="true" v-if="bizFormModel.status === '2'" value="Đang khám"></el-input>
              <el-input :disabled="true" v-if="bizFormModel.status === '3'" value="Đã kết thúc"></el-input>
              <el-input :disabled="true" v-if="bizFormModel.status === '4'" value="Đã phát thuốc"></el-input>
              <el-input :disabled="true" v-if="bizFormModel.status === '5'" value="Đã trả lại"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24 / 2">
            <el-form-item label="Kết quả đề nghị" prop="applicationResults" v-if="dialogProps.action === 'view'">
              <el-input :disabled="true" v-if="bizFormModel.applicationResults === '0'" value="Qua"></el-input>
              <el-input :disabled="true" v-if="bizFormModel.applicationResults === '1'" value="Trả lại"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <!--<el-row>
          <el-col :span="24 / 2">
            <el-form-item label="Số đơn hàng" prop="orderNo" v-if="dialogProps.action === 'view'">
              <el-input
                :disabled="true"
                v-model="bizFormModel.orderNo"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-row>
            <el-col :span="24 / 2">
              <el-form-item label="费用金额" prop="payFee" v-if="dialogProps.action === 'view'">
                <el-input
                  :disabled="true"
                  v-model="bizFormModel.payFee"
                ></el-input>
              </el-form-item>
            </el-col>
          </el-row>
        </el-row>-->
        <el-row>
          <el-col :span="24 / 2">
            <el-form-item label="Thời gian hẹn" prop="diagnosisTime">
              <el-input
                :disabled="true"
                v-model="bizFormModel.diagnosisTime"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-row>
            <el-col :span="24 / 2">
              <el-form-item label="Thời gian tạo" prop="createDate" v-if="dialogProps.action === 'view'">
                <el-input
                  :disabled="true"
                  v-model="bizFormModel.createDate"
                ></el-input>
              </el-form-item>
            </el-col>

          </el-row>
        </el-row>
      </div>
    </el-form>
    <span slot='footer' class='dialog-footer'>
      <el-button v-if='dialogProps.action !== "view"' :disabled="flag" type='primary' :plain='true'
                 @click='onSubmit("patientForm")'>Gửi
      </el-button>
      <el-button v-if='dialogProps.action !== "view"' :plain='true' @click='onDialogClose()'>Hủy</el-button>
      <el-button v-if='dialogProps.action === "view"' :plain='true' @click='onDialogClose()'>Đóng</el-button>
    </span>
  </el-dialog>
</template>
<script>
  import {listDictItemAll} from "@/api/sys/dictItem";
  import { getDictItemsByCode, DICT_CODE } from '@/utils/dictCache'
  import {listAdministrativeDivisionAll} from "@/api/org/administrativeDivision";
  import {saveDiagnosis, modifiedState} from "@/api/outpatient/remoteDiagnosisTreatment";
  import {getPatient as getRemotePatient, patientSelfReg, remoteTreatedApplyZfOnly} from "@/api/remoteApis";
  import BaseUI from "@/views/components/baseUI";
  import OperationIcon from "@/components/OperationIcon";
  import VDistpicker from 'v-distpicker'
  import {getLocalToken} from '@/utils/auth';
  import axios from 'axios'
  import Vue from "vue";

  Vue.prototype.$axios = axios;

  const loginForm = {Data: "API.Manage", AppId: "Oh_Newtouch_Clinic"}
  const config = {
    headers: {
      Token: getLocalToken()
    }
  }
  export default {
    extends: BaseUI,
    name: "patient-form",
    components: {
      OperationIcon,
      VDistpicker
    },
    data() {
      //Kiểm tra CMND/CCCD
      const isCnNewID = (rule, value, callback) => {
        const arrExp = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2];//Hệ số trọng số
        const arrValid = [1, 0, "X", 9, 8, 7, 6, 5, 4, 3, 2];//Mã kiểm tra
        if (/^\d{17}\d|x$/i.test(value)) {
          let sum = 0, idx;
          for (let i = 0; i < value.length - 1; i++) {
            // Đối với17chữ số đầu nhân với trọng số rồi cộng lại
            sum += parseInt(value.substr(i, 1), 10) * arrExp[i];
          }
          // Tính modulo（Thuật toán cố định）
          idx = sum % 11;
          // Kiểm tra chữ số thứ18có bằng mã kiểm tra không
          if (arrValid[idx] === value.substr(17, 1).toUpperCase()) {
            callback()
          } else {
            callback("Định dạng CMND/CCCD không đúng")
          }
        } else {
          callback("Định dạng CMND/CCCD không đúng")
        }
      }
      return {
        applyId: null,
        tokenData: null,
        bizFormModel: this.initFormModel(),
        tabIndex: "1",
        gender_List: [], // Giới tính
        //Liên kết ba cấp tỉnh/thành phố/quận/huyện
        province: '',
        city: '',
        area: '',
        flag: false,//Ngăn gửi trùng lặp
        withPatientNexus_List: [], // Quan hệ với bệnh nhân
        dialogProps: {
          visible: false,
          action: '',
          title: '',
        },
        formRules: {
          /*name: [{required: true, message: "Vui lòng nhập tên bệnh nhân", trigger: "blur"}],
          "gender.value": [
            {required: true, message: "Vui lòng chọn giới tính", trigger: "change"},
          ],
          age: [{required: true, message: "Vui lòng nhập tuổi", trigger: "blur"}],
          month: [{required: true, message: "Vui lòng nhập tháng", trigger: "blur"}],
          // phone: [{ required: true, message: "Vui lòng nhập thông tin liên hệ", trigger: "blur" }],
          "birthday": [
            {required: true, message: "Vui lòng chọn ngày sinh", trigger: "change"},
          ],
          // "card": [
          //   { required: true, message: 'Vui lòng nhập số CMND/CCCD', trigger: 'blur' },
          //   { min:18,max:18, message: "Vui lòng nhập18chữ số của số CMND/CCCD", trigger: "blur" },
          //   {	//Gọi phương thức đã định nghĩa ở trên để kiểm tra định dạng có đúng không
          //            validator: isCnNewID,trigger: "blur"
          //   }
          // ],
          guardianName: [{required: false, message: "Vui lòng nhập tên người giám hộ", trigger: "change"},],
          guardianPhone: [{required: false, message: "Vui lòng nhập số điện thoại người giám hộ", trigger: "change"},]*/
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
          // 0女1男
          let sexName = "男";
          if (parseInt(sexStr) % 2 === 0) {
            sexName = "女";
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
      //Sự kiện thay đổi tuổi
      exchageAge() {
        if (this.bizFormModel.age < 12) {
          this.formRules.guardianName[0].required = true
          this.formRules.guardianPhone[0].required = true
          this.formRules.phone[0].required = false

        } else {
          this.formRules.guardianName[0].required = false
          this.formRules.guardianPhone[0].required = false
          this.$refs["patientForm"].clearValidate();
          this.formRules.phone[0].required = true
        }
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
          this.formRules.phone[0].required = false

        } else {
          this.formRules.guardianName[0].required = true
          this.formRules.guardianPhone[0].required = true
          this.$refs["patientForm"].clearValidate();
          this.formRules.phone[0].required = true
        }
      },

      getToken() {
        // Lấy phòng khám từ xatoken
        this.$axios.post('/token/Auth/GetAppFrienAuthToken', loginForm, config)
          .then((response) => {
            this.tokenData = response.data.BusData.data.Token
          })
          .catch(function (error) {
            console.error(error);
          });
      },
      // Sửa trạng thái
      statusEdit(index) {
        this.bizFormModel.status = index
        saveDiagnosis(this.bizFormModel).then(responseData => {
          if (responseData.code === 100) {
          } else {
            this.showMessage(responseData)
          }
        }).catch(error => {
          this.outputError(error)
        })
      },
      // 获取远程患者信息
      async getPatient() {
        this.bizFormModel.patientId = null
        this.bizFormModel.patientKh = null
        await getRemotePatient(
          { zjh: this.bizFormModel.card, xm: this.bizFormModel.name },
          this.bizFormModel.hospitalId, this.tokenData
        ).then((response) => {
          if (response.data.BusData.code === 40004) {
            patientSelfReg(
              { zjh: this.bizFormModel.card, xm: this.bizFormModel.name, xb: this.bizFormModel.gender === "男" ? "1" : "2" },
              this.bizFormModel.hospitalId, this.tokenData
            ).then((response) => {
              this.bizFormModel.patientId = response.data.BusData.data.patid
              this.bizFormModel.patientKh = response.data.BusData.data.kh
            }).catch((error) => { console.error(error); });
          } else if (response.data.BusData.code !== 40004) {
            this.bizFormModel.patientId = response.data.BusData.data[0].patid
            this.bizFormModel.patientKh = response.data.BusData.data[0].kh
          }
        }).catch((error) => { console.error(error); });
      },
      async onSubmit(formName) {
        // Lấy phòng khám từ xatoken
        await this.$axios.post('/token/Auth/GetAppFrienAuthToken', loginForm, config)
          .then((response) => {
            this.tokenData = response.data.BusData.data.Token
          })
          .catch(function (error) {
            console.error(error);
          });
        this.flag = true
        // 提交远程会诊申请
        await this.getPatient()
        await this.getPatient()
        await remoteTreatedApplyZfOnly(
          { sqsj: this.bizFormModel.diagnosisTime, ks: this.bizFormModel.departmentId,
            ysgh: this.bizFormModel.medicId, patid: this.bizFormModel.patientId,
            kh: this.bizFormModel.patientKh, xm: this.bizFormModel.name,
            sqr: this.bizFormModel.createBy, sqlsh: this.bizFormModel.id,
            sqrgh: currentUser.id, ApplyOrg: currentUser.company.id,
            ApplyOrgName: this.bizFormModel.company.name },
          this.bizFormModel.hospitalId, this.tokenData
        )
          .then((response) => {
            if (response.data.BusData.code === 10000) {
              this.setLoad()
              this.bizFormModel.applyId = response.data.BusData.data.ApplyId
              this.dialogProps.visible = false;
              this.statusEdit("1")
              this.$message.success("Lưu thành công！")
              modifiedState(this.bizFormModel).then(responseData => {
                if (responseData.code === 100) {
                } else {
                  this.showMessage(responseData)
                }
                this.resetLoad()
              }).catch(error => {
                this.outputError(error)
              })
            } else {
              this.$message.warning("Thông tin đề nghị khám chữa bệnh bất thường，Vui lòng liên hệ quản trị viên rồi gửi lại đề nghị!")
            }

          })
          .catch(function (error) {
            console.error(error);
          });
        this.resetLoad()
        this.$refs.index.reset();
      },
      doSave() {
        let flagCard = false
        //Kiểm tra số CMND/CCCD
        if (this.bizFormModel.card) {
          const arrExp = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2];//Hệ số trọng số
          const arrValid = [1, 0, "X", 9, 8, 7, 6, 5, 4, 3, 2];//Mã kiểm tra
          if (/^\d{17}\d|x$/i.test(this.bizFormModel.card)) {
            let sum = 0, idx;
            for (let i = 0; i < this.bizFormModel.card.length - 1; i++) {
              // Đối với17chữ số đầu nhân với trọng số rồi cộng lại
              sum += parseInt(this.bizFormModel.card.substr(i, 1), 10) * arrExp[i];
            }
            // Tính modulo（Thuật toán cố định）
            idx = sum % 11;
            // Kiểm tra chữ số thứ18có bằng mã kiểm tra không
            if (arrValid[idx] === this.bizFormModel.card.substr(17, 1).toUpperCase()) {
              flagCard = true
            } else {
              flagCard = false
            }
          } else {
            flagCard = false
          }
        }

        if (!flagCard && this.bizFormModel.card) {
          this.$message.error("Số CMND/CCCD không đúng，Vui lòng nhập lại")
          this.flag = false
          return;
        }

        this.setLoad()
        this.bizFormModel.province = this.province
        this.bizFormModel.city = this.city
        this.bizFormModel.area = this.area
        // this.bizFormModel.id=""
        saveDiagnosis(this.bizFormModel).then(responseData => {
          this.flag = false
          if (responseData.code === 100) {
            this.dialogProps.visible = false
            this.$emit('save-finished')
          } else {
            this.flag = false
            this.showMessage(responseData)
          }
        }).catch(error => {
          this.flag = false
          this.outputError(error)
        })
      },
      switchEdit() {
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
      initFormModel(This) {
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
      initOptions(This) {
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
          this.withPatientNexus_List = data
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
        this.dialogProps.action = 'edit'
        this.dialogProps.title = 'Gửi yêu cầu'
        this.bizFormModel = {...this.initFormModel(), ...patient}
        this.initOptions(this.bizFormModel)
        this.tabIndex = '1'
        this.dialogProps.visible = true
        this.province = this.bizFormModel.province
        this.city = this.bizFormModel.city
        this.area = this.bizFormModel.area
      },
      openAddPatientDialog() {
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
        this.dialogProps.action = 'view'
        this.dialogProps.title = 'Xem thông tin chi tiết'
        this.bizFormModel = {...this.initFormModel(this.user), ...patient}
        this.initOptions(this.bizFormModel)
        this.tabIndex = '1'
        this.bizFormModel.id = null   //ĐặtidĐặt thành trống，Thêm mới một
        this.dialogProps.visible = true
      },
    },
    watch: {
      'this.bizFormModel.age': function () {
        if (this.bizFormModel.age < 12) {
          formRules.guardianName[0].required = true
          formRules.guardianPhone[0].required = true
        } else {
          formRules.guardianName[0].required = true
          formRules.guardianPhone[0].required = true
        }
      }
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
