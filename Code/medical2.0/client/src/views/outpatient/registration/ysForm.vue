<template>
  <el-dialog
    class="ysForm"
    :title="dialogProps.title"
    :visible.sync="dialogProps.visible"
    :close-on-click-modal="false"
    width="50%"
    @open="onDialogOpen()"
    v-loading="loading"
  >
    <el-form
      :model="queryModel"
      :rules="formRules"
      ref="ysForm"
      label-width="120px"
      label-position="right"
      style="margintop: 10px"
      class="edit-form"
    >
      <el-row>
        <el-col :span="24 / 2">
          <el-form-item label="Bác sĩ" prop="doctor">
            <el-select
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
      </el-row>
    </el-form>
    <div slot="footer">
      <el-button @click="onDialogclose">Đóng</el-button>
      <el-button type="primary" @click="buttoninser"
        >Xác nhận</el-button
      >
    </div>
  </el-dialog>
</template>

<script>
import BaseUI from "@/views/components/baseUI";
import { listDictItemAll } from "@/api/sys/dictItem";
import { getDictItemsByCode, DICT_CODE } from '@/utils/dictCache'
import { saveRegistration,listDoctorsAll,listDoctorsAllnew,registationupdatenew } from "@/api/outpatient/registration";
export default {
  extends: BaseUI,
  name: "ys-form",
  data() {
    return {
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
      queryModel: {
        doctor: {
          // Bác sĩ
          id: null,
          name: "",
        }
      },
      doctor_List: [], // Bác sĩ
      status_List: [], // Trạng thái

      insetModel:{
         registration:{
        id:"",
        company:{
          id:currentUser.company.id,
          name:null,
        },
        doctor:{id:null,name:null},
        patientId:{id:null,name:null},
        status:{name:"Chờ khám",value:"registrationStatus_0"},
         },
      },
    };
  },
  methods: {
    onDialogOpen() {
      this.$nextTick(() => {
        this.$refs["ysForm"].clearValidate();
      });
    },
    arrlistcreat() {
      this.queryModel = {
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
        cource: "",
      };
    },
    onDialogclose() {
      this.$emit('typeclick');
      this.dialogProps.visible = false;
      this.arrlistcreat();
    },
    initys() {
      //Trạng thái đăng ký khám
      getDictItemsByCode(DICT_CODE.REGISTRATION_STATUS).then((data) => {
        this.status_List = data;
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

      this.doctor_List.splice(0, this.doctor_List.length);
      // listDoctorsAll(doctor_search).then((responseData) => {
      //   this.doctor_List = responseData.data;
      // });
      listDoctorsAllnew().then((responseData) => {
        this.doctor_List = responseData.data;
      })
    },
    buttoninser(){
      if(this.queryModel.doctor.id==null&&this.queryModel.doctor.name===""){
         this.$message({
                showClose: true,
                message:'Vui lòng chọn bác sĩ',
                type: 'error'
             });
             return;
      }
      this.insetModel.registration.doctor.id=this.queryModel.doctor.id;
      this.insetModel.registration.doctor.name=this.queryModel.doctor.name;
      registationupdatenew(this.insetModel)
        .then((responseData) => {
          if (responseData.code === 100) {
            this.$emit('typeclick',"");
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

    },
    openViewysDialog(item, types) {
      this.arrlistcreat();
      this.dialogProps.action = "view";
      this.dialogProps.title = "Phân khoa";

      this.insetModel.registration.id = item.id;
      this.insetModel.registration.patientId.id = item.patientId.id;
      this.insetModel.registration.patientId.name = item.patientId.name;
      this.initys();
      this.dialogProps.visible = true;
    },
  },
};
</script>
  