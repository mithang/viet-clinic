<template>
  <el-dialog
    :title="dialogProps.title"
    :visible.sync="dialogProps.visible"
    :close-on-click-modal="false"
    width="30%"
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
      ref="clinicOfficeForm"
      label-width="120px"
      label-position="right"
      class="edit-form"
      style="marginTop: 10px"
    >
      <div class="tab-item" v-show="tabIndex === '1'">
        <el-row>
          <el-col :span="24 / 1">
            <el-form-item label="Phòng khám" prop="company.id">
             <el-input
                v-if="isCompanyShow"
                :disabled="true"
                v-model="aa.name"
              ></el-input>
              <el-input
                v-if="dialogProps.action === 'view'"
                :disabled="true"
                v-model="bizFormModel.company.name"
              ></el-input>
              <el-select
                v-if="!aa"
                v-model="bizFormModel.company"
                value-key="id"
                filterable
                clearable
                placeholder="Vui lòng chọn phòng khám trực thuộc"

              >
                <el-option
                  v-for="company in company_List"
                  :key="company.id"
                  :label="company.name"
                  :value="company"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24 / 1">
            <el-form-item label="Mã khoa/phòng" prop="code">
              <el-input
                :disabled="dialogProps.action === 'view'"
                v-model="bizFormModel.code"
                :maxlength="64"
                :placeholder="
                  dialogProps.action === 'view' ? '' : 'Vui lòng nhập mã khoa/phòng'
                "
              ></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24 / 1">
            <el-form-item label="Tên khoa/phòng" prop="name">
              <el-input
                :disabled="dialogProps.action === 'view'"
                v-model="bizFormModel.name"
                :maxlength="128"
                :placeholder="
                  dialogProps.action === 'view' ? '' : 'Vui lòng nhập tên khoa/phòng'
                "
              ></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24 / 1">
            <el-form-item label="Nhóm lớn khoa/phòng" prop="category">
              <el-input
                :disabled="dialogProps.action === 'view'"
                v-model="bizFormModel.category"
                :maxlength="128"
                :placeholder="
                  dialogProps.action === 'view' ? '' : '请输入科室大类'
                "
              ></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24 / 1">
            <el-form-item label="Địa chỉ" prop="address">
              <el-input
                :disabled="dialogProps.action === 'view'"
                v-model="bizFormModel.address"
                :maxlength="255"
                :placeholder="dialogProps.action === 'view' ? '' : 'Vui lòng nhập địa chỉ'"
              ></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24 / 1">
            <el-form-item label="Có phải khoa/phòng đăng ký không" prop="isRegister">
              <el-switch
                :disabled="dialogProps.action === 'view'"
                v-model="bizFormModel.isRegister"
                active-color="#13ce66"
                inactive-color="#dbdfe6"
                active-value="1"
                inactive-value="0"
              ></el-switch>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24 / 1">
            <el-form-item label="Khoa/phòng đăng ký mặc định" prop="isDefault">
              <el-switch
                :disabled="dialogProps.action === 'view'"
                v-model="bizFormModel.isDefault"
                active-color="#13ce66"
                inactive-color="#dbdfe6"
                active-value="1"
                inactive-value="0"
              ></el-switch>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24 / 1">
            <el-form-item label="Có kích hoạt không" prop="isLocked">
              <el-switch
                :disabled="dialogProps.action === 'view'"
                v-model="bizFormModel.isLocked"
                active-color="#13ce66"
                inactive-color="#dbdfe6"
                active-value="0"
                inactive-value="1"
              ></el-switch>
            </el-form-item>
          </el-col>
        </el-row>
      </div>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button
        v-if="dialogProps.action !== 'view'"
        type="primary"
        :disabled="flag"
        :plain="true"
        @click="onSubmit('clinicOfficeForm')"
        >Lưu</el-button
      >
      <el-button
        v-if="dialogProps.action !== 'view'"
        :plain="true"
        @click="onDialogClose()"
        >Hủy</el-button
      >
      <el-button
        v-if="dialogProps.action === 'view'"
        :plain="true"
        @click="onDialogClose()"
        >Đóng</el-button
      >
    </span>
  </el-dialog>
</template>
<script>
import { validatenull } from "@/utils/validate";
import { listClinicOfficeAll } from "@/api/org/clinicOffice";
import { saveClinicOffice } from "@/api/org/clinicOffice";
import BaseUI from "@/views/components/baseUI";
import OperationIcon from "@/components/OperationIcon";
import { getLocalCurrentCompany } from "@/utils/auth";
export default {
  extends: BaseUI,
  name: "my-task",
  components: {
    OperationIcon,
  },
  data() {
    return {
      aa:{},
      isCompanyShow: false,
      bizFormModel: this.initFormModel(),
      tabIndex: "1",
      company_List: [], // Phòng khám trực thuộc
       flag:false,
      dialogProps: {
        visible: false,
        action: "",
        title: "",
       
      },
      formRules: {
        "company.id": [
          // { required: true, message: "Vui lòng chọn phòng khám trực thuộc", trigger: "change" },
        ],
        code: [{ required: true, message: "Vui lòng nhập mã khoa/phòng", trigger: "blur" }],
        name: [{ required: true, message: "Vui lòng nhập tên khoa/phòng", trigger: "blur" }],
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
    isCompany() {
      const company = getLocalCurrentCompany();
      if (company) {
        this.aa=company;
        this.bizFormModel.company={id:company.id,name:company.name};
      //   this.$set(this.bizFormModel.company,'name',company.name);
        // this.bizFormModel.company.name = company.name;
        this.isCompanyShow = true;
      }
    },
    onSubmit(formName) {
      this.flag=true
      this.$refs[formName].validate((valid) => {
        if (valid) {
          this.doSave();
        } else {
          this.flag=false
          return false;
        }
      });
    },
    doSave() {
      this.setLoad();
      saveClinicOffice({...this.bizFormModel,company:this.aa})
        .then((responseData) => {
          this.flag=false
          if (responseData.code === 100) {
            this.dialogProps.visible = false;
            this.$emit("save-finished","1");
          } else {
            this.showMessage(responseData);
          }
          this.resetLoad();
        })
        .catch((error) => {
          this.flag=false
          this.outputError(error);
        });
    },
    switchEdit() {
      this.dialogProps.action = "edit";
      this.dialogProps.title = "Sửa khoa/phòng";
      this.initOptions(this.bizFormModel);
    },
    onDialogClose() {
      this.dialogProps.visible = false;
    },
    onDialogOpen() {
      this.$nextTick(() => {
        this.$refs["clinicOfficeForm"].clearValidate();
      });
    },
    initFormModel(This) {
      return {
        company: {
          // Nút cấp trên Phòng khám trực thuộc
          id: validatenull(This) || validatenull(This.id) ? null : This.id,
          name:
            validatenull(This) || validatenull(This.name) ? null : This.name,
        },
        code: "", // Mã khoa/phòng
        isLocked: "0", // Vô hiệu hóa（0：Chưa bị vô hiệu hóa；1：Vô hiệu hóa）
        name: "", // Tên khoa/phòng
        sort: "0", // Sắp xếp
        category: "", // Nhóm lớn khoa/phòng
        address: "", // Địa chỉ
      };
    },
    initOptions(This) {
      let company_search = {
        params: [],
      };
      // Bảng có trường bị vô hiệu hóa
      company_search.params.push.apply(company_search.params, [
        { columnName: "is_locked", queryType: "=", value: "0" },
      ]);
      // Quyền dữ liệu: Khoaclinic_office
      this.pushDataPermissions(
        company_search.params,
        this.$route.meta.routerId,
        "1026680647570219053"
      );
      this.company_List.splice(0, this.company_List.length);
      listClinicOfficeAll(company_search).then((responseData) => {
        this.company_List = responseData.data;
      });
    },
    openViewClinicOfficeDialog(clinicOffice) {
      this.dialogProps.action = "view";
      this.dialogProps.title = "Xem khoa/phòng";
      this.bizFormModel = { ...this.initFormModel(), ...clinicOffice };
      this.initOptions(this.bizFormModel);
      this.tabIndex = "1";
      this.dialogProps.visible = true;
    },
    openEditClinicOfficeDialog(clinicOffice) {
      this.dialogProps.action = "edit";
      this.dialogProps.title = "Sửa khoa/phòng";
      this.bizFormModel = { ...this.initFormModel(), ...clinicOffice };
      this.initOptions(this.bizFormModel);
      this.tabIndex = "1";
      this.dialogProps.visible = true;
    },
    openAddClinicOfficeDialog() {
      this.dialogProps.action = "add";
      this.dialogProps.title = "Thêm khoa/phòng";
      this.bizFormModel = this.initFormModel();
      this.initOptions(this.bizFormModel);
      this.tabIndex = "1";
      this.dialogProps.visible = true;
    },
    openCopyClinicOfficeDialog(clinicOffice) {
      this.dialogProps.action = "add";
      this.dialogProps.title = "Thêm khoa/phòng";
      this.bizFormModel = { ...this.initFormModel(), ...clinicOffice };
      this.initOptions(this.bizFormModel);
      this.tabIndex = "1";
      this.bizFormModel.id = null;
      this.dialogProps.visible = true;
    },
  },
  watch: {},
  mounted: function () {
    this.isCompany();
  },
};
</script>
