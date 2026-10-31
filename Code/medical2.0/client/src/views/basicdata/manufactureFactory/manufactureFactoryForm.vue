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
      ref="manufactureFactoryForm"
      label-width="120px"
      label-position="right"
      class="edit-form"
      style="margintop: 10px"
    >
      <div class="tab-item" v-show="tabIndex === '1'">
        <el-row>
          <el-col :span="24 / 2">
            <el-form-item label="Tên nhà sản xuất" prop="name">
              <el-input
                :disabled="dialogProps.action === 'view'"
                v-model="bizFormModel.name"
                :maxlength="128"
                :placeholder="
                  dialogProps.action === 'view' ? '' : 'Vui lòng nhập tên nhà sản xuất'
                "
                autofocus
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24 / 2">
            <el-form-item label="Người liên hệ" prop="people">
              <el-input
                :disabled="dialogProps.action === 'view'"
                v-model="bizFormModel.people"
                :maxlength="45"
                :placeholder="
                  dialogProps.action === 'view' ? '' : 'Vui lòng nhập người liên hệ'
                "
              ></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24 / 2">
            <el-form-item label="Điện thoại" prop="phone">
              <el-input
                :disabled="dialogProps.action === 'view'"
                v-model="bizFormModel.phone"
                :maxlength="45"
                :placeholder="dialogProps.action === 'view' ? '' : 'Vui lòng nhập số điện thoại'"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24 / 2">
            <el-form-item label="Có kích hoạt không" prop="status">
              <el-switch
                :disabled="dialogProps.action === 'view'"
                v-model="value"
                active-color="#13ce66"
                inactive-color="#dbdfe6"
                active-value="1"
                inactive-value="0"
              ></el-switch>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24 / 2">
            <el-form-item label="Khu vực" prop="">
              <v-distpicker
                :disabled="dialogProps.action === 'view'"
                :province="province"
                :city="city"
                :area="area"
                @province="onChangeProvince"
                @city="onChangeCity"
                @area="onChangeArea"
              ></v-distpicker>
            </el-form-item>
          </el-col>
          <el-col :span="24 / 2">
            <el-form-item label="Phân loại nhà sản xuất" prop="type">
              <div>
                <el-radio
                  :disabled="dialogProps.action === 'view'"
                  v-model="bizFormModel.type"
                  label="1"
                  border
                  >Hãng thuốc</el-radio
                >
                <el-radio
                  :disabled="dialogProps.action === 'view'"
                  v-model="bizFormModel.type"
                  label="2"
                  border
                  >Hãng vật tư</el-radio
                >
              </div>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col>
            <el-form-item label="Địa chỉ" prop="address">
              <el-input
                :disabled="dialogProps.action === 'view'"
                v-model="bizFormModel.address"
                type="textarea"
                :maxlength="128"
                :placeholder="dialogProps.action === 'view' ? '' : 'Vui lòng nhập địa chỉ'"
                clearable
              ></el-input>
            </el-form-item>
          </el-col>
        </el-row>
      </div>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button
        v-if="dialogProps.action !== 'view'"
        :disabled="flag"
        type="primary"
        :plain="true"
        @click="onSubmit('manufactureFactoryForm')"
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
import "@/utils/validate";
import { saveManufactureFactory } from "@/api/basicdata/manufactureFactory";
import BaseUI from "@/views/components/baseUI";
import OperationIcon from "@/components/OperationIcon";
import VDistpicker from "v-distpicker";
export default {
  extends: BaseUI,
  name: "manufactureFactory-form",
  components: {
    OperationIcon,
    VDistpicker,
  },
  data() {
    return {
      bizFormModel: this.initFormModel(),
      tabIndex: "1",
      //Liên kết ba cấp tỉnh/thành phố/quận/huyện
      province: "",
      city: "",
      area: "",
      value: "1",
      flag: false, //Ngăn gửi trùng lặp
      dialogProps: {
        visible: false,
        action: "",
        title: "",
      },
      formRules: {
        name: [{ required: true, message: "Vui lòng nhập tên nhà sản xuất", trigger: "blur" }],
        type: [
          { required: true, message: "Vui lòng chọn loại nhà cung cấp", trigger: "change" },
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
    onSubmit(formName) {
      this.flag = true;
      this.$refs[formName].validate((valid) => {
        if (valid) {
          this.doSave();
        } else {
          this.flag = false;
          return false;
        }
      });
    },
    doSave() {
      this.setLoad();
      this.bizFormModel.province = this.province;
      this.bizFormModel.city = this.city;
      this.bizFormModel.area = this.area;
      this.bizFormModel.status = this.value;
      saveManufactureFactory(this.bizFormModel)
        .then((responseData) => {
          this.flag = false;
          if (responseData.code === 100) {
            this.dialogProps.visible = false;
            this.$emit("save-finished", "1");
          } else {
            this.showMessage(responseData);
          }
          this.resetLoad();
        })
        .catch((error) => {
          this.flag = false;
          this.outputError(error);
        });
    },
    switchEdit() {
      this.dialogProps.action = "edit";
      this.dialogProps.title = "Sửa nhà sản xuất";
      this.initOptions(this.bizFormModel);
    },
    onDialogClose() {
      this.dialogProps.visible = false;
    },
    onDialogOpen() {
      this.$nextTick(() => {
        this.$refs["manufactureFactoryForm"].clearValidate();
      });
    },
    initFormModel() {
      return {
        company: {
          // Phòng khámid
          id: currentUser.company.id,
          name: null,
        },
        name: "", // Tên nhà sản xuất
        people: "", // Người liên hệ
        phone: "", // Điện thoại
        province: "", // Tỉnh/Thành phố
        city: "", // Thành phố
        area: "", // Quận/Huyện
        address: "", // Địa chỉ
        remarks: "", // Thông tin ghi chú
      };
    },
    initOptions() {},
    // Lấy giá trị liên kết ba cấp tỉnh/thành phố/quận/huyện
    onChangeProvince(data) {
      this.province = data.value;
    },
    onChangeCity(data) {
      this.city = data.value;
    },
    onChangeArea(data) {
      this.area = data.value;
    },
      openViewManufactureFactoryDialog(manufactureFactory) {
          this.dialogProps.action = "view";
          this.dialogProps.title = "Xem nhà sản xuất";
          this.bizFormModel = {
            ...this.initFormModel(),
            ...manufactureFactory,
          };
          this.initOptions(this.bizFormModel);
          this.tabIndex = "1";
          this.value = this.bizFormModel.status;
          this.dialogProps.visible = true;
          this.province = this.bizFormModel.province;
          this.city = this.bizFormModel.city;
          this.area = this.bizFormModel.area;
        },
    openEditManufactureFactoryDialog(manufactureFactory) {
          this.dialogProps.action = "edit";
          this.dialogProps.title = "Sửa nhà sản xuất";
          this.bizFormModel = {
            ...this.initFormModel(),
            ...manufactureFactory,
          };
          this.initOptions(this.bizFormModel);
          this.value = this.bizFormModel.status;
          this.tabIndex = "1";
          this.dialogProps.visible = true;
          this.province = this.bizFormModel.province;
          this.city = this.bizFormModel.city;
          this.area = this.bizFormModel.area;
        },
    openAddManufactureFactoryDialog() {
        this.dialogProps.action = "add";
        this.dialogProps.title = "Thêm nhà sản xuất";
        this.bizFormModel = this.initFormModel();
        this.initOptions(this.bizFormModel);
        this.tabIndex = "1";
        this.dialogProps.visible = true;
        this.province = "";
        this.city = "";
        this.area = "";
      },
    openCopyManufactureFactoryDialog(manufactureFactory) {
          this.dialogProps.action = "add";
          this.dialogProps.title = "Thêm nhà sản xuất";
          this.bizFormModel = {
            ...this.initFormModel(),
            ...manufactureFactory,
          };
          this.initOptions(this.bizFormModel);
          this.tabIndex = "1";
          this.bizFormModel.id = null; //ĐặtidĐặt thành trống, Thêm mới một
          this.dialogProps.visible = true;
        },
},
  watch: {},
  
};
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