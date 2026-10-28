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
      ref="userForm"
      label-width="120px"
      style="marginTop: 10px"
      label-position="right"
      class="edit-form"
    >
      <div class="tab-item" v-show="tabIndex === '1'">
        <!-- Biểu mẫu chính  Bắt đầu-->

        <el-row>
          <el-col :span="24 / 2">
            <el-form-item label="Họ tên" prop="name">
              <el-input
                :disabled="dialogProps.action === 'view'"
                v-model="bizFormModel.name"
                :maxlength="128"
                :placeholder="
                  dialogProps.action === 'view' ? '' : 'Vui lòng nhập tên đăng nhập'
                "
              ></el-input>
            </el-form-item>
          </el-col>
          <!-- <el-col :span="24 / 2">
            <el-form-item label="Giới tính" prop="userExt.sex">
              <el-radio v-model="bizFormModel.userExt.sex" label="1"
                >Nam</el-radio
              >
              <el-radio v-model="bizFormModel.userExt.sex" label="2"
                >Nữ</el-radio
              >
            </el-form-item>
          </el-col> -->
        </el-row>
        <el-row>
          <!-- <el-col :span="24 / 2">
            <el-form-item label="Dân tộc" prop="userExt.nation">
              <el-input
                v-model="bizFormModel.userExt.nation"
                :maxlength="128"
                :placeholder="'Vui lòng nhập dân tộc'"
              ></el-input>
            </el-form-item>
          </el-col> -->
          <el-col :span="24 / 2">
            <el-form-item label="Số điện thoại di động" prop="phone">
              <el-input
                :disabled="dialogProps.action === 'view'||  dialogProps.action === 'edit'"
                v-model="bizFormModel.phone"
                :maxlength="11"
                :placeholder="
                  dialogProps.action === 'view' ? '' : 'Vui lòng nhập số điện thoại di động'
                "
              ></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <!-- <el-col :span="24 / 2">
            <el-form-item label="Dân tộc" prop="userExt.nation">
              <el-input
                v-model="bizFormModel.userExt.nation"
                :maxlength="128"
                :placeholder="'Vui lòng nhập dân tộc'"
              ></el-input>
            </el-form-item>
          </el-col> -->
          <el-col :span="24 / 2">
            <el-form-item label="Mã hành nghề" prop="pracPsnCode">
              <el-input
                :disabled="dialogProps.action === 'view'"
                v-model="bizFormModel.userExt.pracPsnCode"
                :maxlength="11"
                :placeholder="
                  dialogProps.action === 'view' ? '' : 'Vui lòng nhập mã hành nghề'
                "
              ></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <!-- <el-col :span="24 / 2">
            <el-form-item label="Ngày sinh" prop="userExt.birthday">
              <el-date-picker
                v-model="bizFormModel.userExt.birthday"
                type="date"
                placeholder="Chọn ngày"
              >
              </el-date-picker>
            </el-form-item>
          </el-col> -->
          <el-col :span="24 / 2">
            <el-form-item label="Tải ảnh đại diện lên" prop="userHeader">
              <el-upload
                class="avatar-uploader"
                :show-file-list="false"
                :on-change="handleLicensePreview"
                :before-upload="beforeLicenseUpload"
                :auto-upload="false"
              >
                <img
                  v-if="bizFormModel.userHeader"
                  :src="bizFormModel.userHeader"
                  class="avatar"
                />
                <i v-else class="el-icon-plus avatar-uploader-icon"></i>
              </el-upload>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24 / 2">
            <el-form-item label="Loại giấy tờ" prop="userExt.creditType">
              <el-select
                v-model="bizFormModel.userExt.creditType"
                placeholder="Vui lòng chọn loại giấy tờ"
              >
                <el-option label="CMND/CCCD" value="CMND/CCCD"> </el-option>
                <el-option label="Giấy phép lái xe" value="Giấy phép lái xe"> </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24 / 2">
            <el-form-item label="Số giấy tờ" prop="userExt.creditNum">
              <el-input
                v-model="bizFormModel.userExt.creditNum"
                :maxlength="18"
                :placeholder="'Vui lòng nhập số giấy tờ'"
              ></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24 / 2">
            <el-form-item label="Khoa" prop="userExt.office">
              <!-- <el-select
                v-model="bizFormModel.userExt.office"
                placeholder="Vui lòng chọn khoa/phòng"
              >
                <el-option label="Khoa1" value="Khoa1"> </el-option>
                <el-option label="Khoa2" value="Khoa2"> </el-option>
              </el-select> -->

              <el-select
                v-if="!aa"
                v-model="bizFormModel.userExt.office"
                value-key="id"
                filterable
                clearable
                placeholder="Vui lòng chọn phòng khám trực thuộc"
              >
                <el-option
                  v-for="company in department_List"
                  :key="company.id"
                  :label="company.name"
                  :value="company.name"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24 / 2">
            <el-form-item label="Chức danh bảo hiểm y tế" prop="userExt.post">
              <el-select
                v-model="bizFormModel.userExt.post"
                placeholder="Chức danh bảo hiểm y tế"
              >
                <!-- 医生相关的选项 -->
                <el-option v-if="bizFormModel.userExt.jobType === 'Bác sĩ'" label="Bác sĩ trưởng khoa" value="231"></el-option>
                <el-option v-if="bizFormModel.userExt.jobType === 'Bác sĩ'" label="Phó bác sĩ trưởng khoa" value="232"></el-option>
                <el-option v-if="bizFormModel.userExt.jobType === 'Bác sĩ'" label="Bác sĩ điều trị chính" value="233"></el-option>
                <el-option v-if="bizFormModel.userExt.jobType === 'Bác sĩ'" label="Bác sĩ" value="234"></el-option>
                <el-option v-if="bizFormModel.userExt.jobType === 'Bác sĩ'" label="Y sĩ" value="235"></el-option>

                <!-- 药师相关的选项 -->
                <el-option v-if="bizFormModel.userExt.jobType === '药师'" label="Dược sĩ hành nghề" value="1"></el-option>
                <el-option v-if="bizFormModel.userExt.jobType === '药师'" label="Dược sĩ phụ trách" value="2.3"></el-option>
                <el-option v-if="bizFormModel.userExt.jobType === '药师'" label="Dược sĩ trưởng" value="2.1"></el-option>
                <el-option v-if="bizFormModel.userExt.jobType === '药师'" label="Phó dược sĩ trưởng" value="2.2"></el-option>
                <el-option v-if="bizFormModel.userExt.jobType === '药师'" label="Dược sĩ" value="2.4"></el-option>
                <el-option v-if="bizFormModel.userExt.jobType === '药师'" label="Dược tá" value="2.5"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col>
            <el-form-item label="Loại" prop="userExt.jobType">
              <el-radio v-model="bizFormModel.userExt.jobType" label="Bác sĩ">Bác sĩ</el-radio>
              <el-radio v-model="bizFormModel.userExt.jobType" label="药师">Dược sĩ</el-radio>
              <el-radio v-model="bizFormModel.userExt.jobType" label="Điều dưỡng">Điều dưỡng</el-radio>
              <el-radio v-model="bizFormModel.userExt.jobType" label="Khác">Khác</el-radio>
            </el-form-item>
          </el-col>
        </el-row>

  <!--Bảng con：   Vai trò người dùng Bắt đầu-->
        <el-row>
          <el-col>
            <el-form-item label="Vai trò người dùng" prop="userRoleList">
              <el-select
                :disabled="dialogProps.action === 'view'"
                v-model="bizFormModel.userRoleList"
                value-key="role.id"
                multiple
                clearable
                filterable
                placeholder="Vui lòng chọn mã vai trò"
              >
                <el-option
                  v-for="role in role_UserRole_List"
                  :key="role.id"
                  :label="role.name"
                  :value="{ id: '', role: role }"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <!--Bảng con：   Vai trò người dùng Kết thúc-->

        <el-row>
          <el-col :span="24 / 2">
            <el-form-item
              label="Thời gian bắt đầu hành nghề tại phòng khám này"
              prop="userExt.startWorkTime"
            >
              <el-date-picker
                v-model="bizFormModel.userExt.startWorkTime"
                type="date"
                format="yyyy-MM-dd"
                placeholder="Thời gian bắt đầu"
                :picker-options="starttime"
              >
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="24 / 2">
            <el-form-item label="Thời gian kết thúc hành nghề tại phòng khám này" prop="userExt.endWorkTime">
              <el-date-picker
                v-model="bizFormModel.userExt.endWorkTime"
                type="date"
                format="yyyy-MM-dd"
                placeholder="Thời gian kết thúc"
                :picker-options="endTime"
              >
              </el-date-picker>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24 / 2">
            <el-form-item label="Tên đăng nhập" prop="phone">
              <el-input
                disabled='false'
                v-model="bizFormModel.phone"
                :maxlength="10"
                :placeholder="dialogProps.action === 'view' ? '' : ''"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24 / 2">
            <el-form-item
              label="Đổi mật khẩu"
              prop="loginPasswordUpdate"
              v-if="dialogProps.action === 'edit'"
            >
              <el-checkbox v-model="bizFormModel.loginPasswordUpdate"
                >Đổi mật khẩu</el-checkbox
              >
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24 / 2">
            <el-form-item
              label="Mật khẩu"
              prop="loginPassword"
              v-if="
                dialogProps.action !== 'view' &&
                dialogProps.action !== 'add' &&
                bizFormModel.loginPasswordUpdate === true
              "
            >
              <el-input
                :disabled="dialogProps.action === 'view'"
                v-model="bizFormModel.loginPassword"
                type="password"
                auto-complete="new-password"
                :maxlength="64"
                :placeholder="dialogProps.action === 'view' ? '' : 'Vui lòng nhập mật khẩu'"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24 / 2">
            <el-form-item
              label="Xác nhận mật khẩu"
              prop="loginPasswordConfirm"
              v-if="
                dialogProps.action !== 'view' &&
                dialogProps.action !== 'add' &&
                bizFormModel.loginPasswordUpdate === true
              "
            >
              <el-input
                :disabled="dialogProps.action === 'view'"
                v-model="bizFormModel.loginPasswordConfirm"
                type="password"
                :maxlength="64"
                :placeholder="
                  dialogProps.action === 'view' ? '' : 'Vui lòng nhập mật khẩu xác nhận'
                "
              ></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24 / 2">
            <el-form-item label="Có kích hoạt không" prop="isLocked">
              <el-switch
                :disabled="dialogProps.action === 'view'"
                v-model="bizFormModel.isLocked"
                active-color="#13ce66"
                inactive-color="#dbdfe6"
                :active-value="0"
                :inactive-value="1"
              ></el-switch>
            </el-form-item>
          </el-col>
        </el-row>
        <!-- <el-row>
          <el-col :span="24 / 2">
            <el-form-item label="Email" prop="email">
              <el-input
                :disabled="dialogProps.action === 'view'"
                v-model="bizFormModel.email"
                :maxlength="100"
                :placeholder="dialogProps.action === 'view' ? '' : 'Vui lòng nhập email'"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24 / 2">
            <el-form-item label="Có đang công tác không" prop="userExt.isDuty">
              <el-radio v-model="bizFormModel.userExt.isDuty" label="0"
                >Không</el-radio
              >
              <el-radio v-model="bizFormModel.userExt.isDuty" label="1"
                >Có</el-radio
              >
            </el-form-item>
          </el-col>
        </el-row> -->
        <el-row>
          <el-col>
            <el-form-item label="Giới thiệu" prop="userExt.description">
              <el-input
                :disabled="dialogProps.action === 'view'"
                v-model="bizFormModel.userExt.description"
                type="textarea"
                :maxlength="255"
                :placeholder="
                  dialogProps.action === 'view' ? '' : 'Vui lòng nhập thông tin giới thiệu'
                "
                clearable
              ></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <!-- Biểu mẫu chính  Kết thúc-->

      </div>
    </el-form>

    <!-- Nút  Bắt đầu-->
    <span slot="footer" class="dialog-footer">
      <el-button
        v-if="dialogProps.action !== 'view'"
        type="primary"
        :disabled="flag"
        :plain="true"
        @click="onSubmit('userForm')"
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
    <!-- Nút Kết thúc-->
  </el-dialog>
</template>

<script>
import { validatenull } from "@/utils/validate";
import { listClinicOfficeAll } from "@/api/org/clinicOffice";
import { getPhotoById } from "@/api/sys/sysSeting";
import { treeCompany } from "@/api/org/company";
import { treeDepartment } from "@/api/org/department";
import { listRoleAll } from "@/api/admin/role";
import { saveUser, updateUser, changemylist } from "@/api/admin/user";
import BaseUI from "@/views/components/baseUI";
import OperationIcon from "@/components/OperationIcon";
export default {
  extends: BaseUI,
  name: "user-form",
  components: {
    OperationIcon,
  },
  data() {
    return {
      photoId: null, // 头像id 查看或修改会给它赋值
      bizFormModel: this.initFormModel(),
      tabIndex: "1",
      flag:false,
      key_department: 1, // el-cascader key
      department_List: [], // Phòng ban

      role_UserRole_List: [], // Mã vai trò

      dialogProps: {
        visible: false,
        action: "",
        title: "",
      },
      formRules: {
        // Xác thực bảng chính
        "company.id": [
          { required: true, message: "Vui lòng chọn công ty", trigger: "change" },
        ],
        "department.id": [
          { required: false, message: "Vui lòng chọn bộ phận", trigger: "change" },
        ],
        // "userExt.creditNum": [
        //   { required: true, message: "Vui lòng nhập số giấy tờ", trigger: "blur" },

        //   {
        //     min: 18,
        //     max: 18,
        //     message: "Vui lòng nhập18chữ số của số CMND/CCCD",
        //     trigger: "blur",
        //   },
        // ],
        // "userExt.nation": [
        //   { required: true, message: "Vui lòng nhập dân tộc", trigger: "blur" },
        // ],
        // "userExt.birthday": [
        //   { required: true, message: "Vui lòng chọn ngày sinh", trigger: "change" },
        // ],
        // "userExt.startWorkTime": [
        //   {
        //     required: true,
        //     message: "请选择开始日期",
        //     trigger: "blur",
        //   },
        // ],
        // "userExt.endWorkTime": [
        //   {
        //     required: true,
        //     message: "请选择结束日期",
        //     trigger: "blur",
        //   },
        // ],
        // "userExt.creditType": [
        //   { required: true, message: "Vui lòng chọn loại giấy tờ", trigger: "change" },
        // ],
        // "userExt.post": [
        //   { required: true, message: "请选择职位", trigger: "blur" },
        // ],
        "userExt.office": [
          { required: true, message: "Vui lòng chọn khoa/phòng", trigger: "change" },
        ],
        // "userExt.sex": [
        //   { required: true, message: "Vui lòng chọn giới tính", trigger: "change" },
        // ],
        // "userExt.isDuty": [
        //   { required: true, message: "请选择是否在职", trigger: "change" },
        // ],
        "userExt.jobType": [
          { required: true, message: "请选择类别", trigger: "change" },
        ],
        name: [{ required: true, message: "Vui lòng nhập tên đăng nhập", trigger: "blur" }],
        // In Add mode these three values are derived by createForme() (loginName = phone,
        // password = last 6 digits of phone) and their inputs are not rendered (password
        // inputs only render for edit + password change), so requiring them here made
        // every Add fail validation silently (no rendered field to show the error on).
        // They stay required in edit mode, where server-loaded values exist.
        loginName: [
          {
            validator: (rule, value, callback) => {
              if (this.dialogProps.action === "add" || (value !== undefined && value !== null && value !== "")) {
                callback();
              } else {
                callback(new Error("Vui lòng nhập tên đăng nhập"));
              }
            },
            trigger: "blur",
          },
        ],
        loginPassword: [
          {
            validator: (rule, value, callback) => {
              if (this.dialogProps.action === "add" || (value !== undefined && value !== null && value !== "")) {
                callback();
              } else {
                callback(new Error("Vui lòng nhập mật khẩu"));
              }
            },
            trigger: "blur",
          },
        ],
        // userHeader: [{ required: true, message: "Tải ảnh đại diện lên", trigger: "blur" }],
        loginPasswordConfirm: [
          {
            validator: (rule, value, callback) => {
              if (this.dialogProps.action === "add") {
                callback();
              } else if (value === undefined || value === null || value === "") {
                callback(new Error("Vui lòng nhập mật khẩu xác nhận"));
              } else if (value !== this.bizFormModel.loginPassword) {
                callback(new Error("Hai lần nhập mật khẩu không khớp!"));
              } else {
                callback();
              }
            },
            trigger: "blur",
          },
        ],
        phone: [
          { required: true, message: "Vui lòng nhập số điện thoại di động", trigger: "blur" },
          { min: 11, max: 11, message: "Vui lòng nhập11位手机号码", trigger: "blur" },
          {
            pattern:
          // /^1(3[0-9]|4[01456879]|5[0-3,5-9]|6[2567]|7[0-8]|8[0-9]|9[0-3,5-9])d{8}$/,
          // /^(13[0-9]|14[579]|15[0-3,5-9]|16[6]|17[0135678]|18[0-9]|19[89])\d{8}$/,
            /^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\d{8}$/,
          message: "请输入正确的手机号码",
          },
        ],
        // email: [{ required: true, message: "Vui lòng nhập email", trigger: "blur" }],
        // Xác thực bảng con Vai trò người dùng
        userRoleList: [
          { required: true, message: "Vui lòng chọn mã vai trò", trigger: "change" },
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
  computed: {
    ...Vuex.mapGetters(["settings"]),
    tabOptionBtnTop() {
      let top;
      switch (this.settings.size) {
        case "medium":
          top = -46 + "px";
          break;
        case "small":
          top = -42 + "px";
          break;
        case "mini":
          top = -38 + "px";
          break;
        default:
          top = -50 + "px";
      }
      return top;
    },
  },
  methods: {
    // 上传图片
    handleLicensePreview(file) {
      const isLt2M = file.size / 1024 / 1024 < 2;
      if (!isLt2M) {
        this.$message.error(this.inter.sctpdx + " 2MB!");
      } else {
        // 存储图片，Tải lên thống nhất khi nhấn nút xác nhận

        this.bizFormModel.userHeader = URL.createObjectURL(file.raw);
        this.bizFormModel.fileIdFile = file.raw;
      }
    },

    //图片上传限制
    beforeLicenseUpload(file) {
      const fileSuffix = file.name.substring(file.name.lastIndexOf(".") + 1);
      const whiteList = ["jpg", "jpeg", "png", "gif", "JPG", "JPEG"];

      if (whiteList.indexOf(fileSuffix) === -1) {
        this.$message.error(this.inter.scyyzz);
        return false;
      }
      const isLt2M = file.size / 1024 / 1024 < 2;
      if (!isLt2M) {
        this.$message.error(this.inter.sctpdx + " 2MB!");
        return false;
      }
    },

    onSubmit(formName) {
      this.flag=true
      this.$refs[formName].validate((valid, object) => {
        if (valid) {
          if (this.dialogProps.action === "edit") {
            this.doUpdate();
          } else {
            this.doSave();
          }
        } else {
          this.flag=false
          // Xử lý định vị kiểm tra
          let arr = [];
          let numArr = [];
          for (let i in object) {
            let dom = this.$refs[i];
            if (!dom) {
              this.tabIndex = 1;
              return;
            } else {
              arr.push(dom);
              if (Object.prototype.toString.call(arr) !== "[object Object]") {
                arr.forEach((item) => {
                  numArr.push(item.$el.dataset.num);
                });
                this.tabIndex = Math.min(...numArr);
              }
            }
          }
          return false;
        }
      });
    },

    createForme() {
      this.bizFormModel.loginName = this.bizFormModel.phone;
      //如果是新增，密码默认手机号最后6vị
      if(this.dialogProps.action === "add"){
        this.bizFormModel.loginPassword = this.bizFormModel.phone.substring((this.bizFormModel.phone.length-6),this.bizFormModel.phone.length)
      }
      let userHeaderFile = new FormData();
      userHeaderFile.append("fileIdUploads", this.bizFormModel.fileIdFile);
      userHeaderFile.append("entity", JSON.stringify(this.bizFormModel));
      userHeaderFile.append("deleteIds",JSON.stringify([this.photoId]));
      return userHeaderFile;
    },
    doSave() {
      this.setLoad();

      //
      // saveUser({
      //   ...this.bizFormModel,
      //   loginName: this.bizFormModel.phone,

      // })
      saveUser(this.createForme())
        .then((responseData) => {
          this.flag=false
          if (responseData.code === 100) {
            this.dialogProps.visible = false;
            this.$emit("save-finished");
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
    doUpdate() {
      // console.log(this.createForme(),'aghaskgjal');
      // return
      this.setLoad();
      updateUser(this.createForme())
        .then((responseData) => {
          this.flag=false
          if (responseData.code === 100) {
            this.dialogProps.visible = false;
            this.$emit("save-finished");
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
    onAddUserRoleRow(tableData) {
      tableData.push({
        user: {
          // Bảng cha 用户标识
          id:
            validatenull(parent) || validatenull(parent.user)
              ? null
              : parent.user.id,
          name:
            validatenull(parent) || validatenull(parent.user)
              ? null
              : parent.user.name,
        },
        role: {
          // Mã vai trò
          id: null,
          name: "",
        },
        remarks: "", // Ghi chú
      });
    },
    onDeleteRow(index, tableData) {
      tableData.splice(index, 1);
    },
    switchEdit() {
      this.dialogProps.action = "edit";
      this.dialogProps.title = "Sửa người dùng";
      this.initOptions(this.bizFormModel);
    },
    onDialogClose() {
      this.dialogProps.visible = false;
    },
    onDialogOpen() {
      this.$nextTick(() => {
        this.$refs["userForm"].clearValidate();
      });
    },
    initFormModel(This) {
      return {
        company: {
          // Bảng cha Công ty
          id:
            validatenull(This) || validatenull(This.company)
              ? null
              : This.company.id,
          name:
            validatenull(This) || validatenull(This.company)
              ? null
              : This.company.name,
          ids:
            validatenull(This) || validatenull(This.company)
              ? null
              : This.company.ids,
          code:
            validatenull(This) || validatenull(This.company)
              ? null
              : This.company.code,
          fullName:
            validatenull(This) || validatenull(This.company)
              ? null
              : This.company.fullName,
        },
        department: {
          // Phòng ban
          id: null,
          name: null,
          ids: null,
          code: null,
        },
        fileIdFile: "", //Hình ảnh
        userExt: {
          sex: "", //Giới tính
          nation: "", // Dân tộc
          birthday: "", // Ngày sinh
          description: "", // Giới thiệu
          isDuty: "", // Có đang công tác không
          creditType: "", // Loại giấy tờ
          creditNum: "", // Số giấy tờ
          post: "", // 职位
          postCode:"" ,//职位代码
          office: "", // Tên khoa/phòng
          jobType: "", // 职业类型
          startWorkTime: "", // Thời gian bắt đầu
          endWorkTime: "", //Thời gian kết thúc
          pracPsnCode:"" ,//Mã hành nghề
        },
        userHeader: "", // 用户头像回显
        name: "", // Tên đăng nhập
        loginName: "", // Tên đăng nhập
        loginPassword: "", // Mật khẩu
        loginPasswordUpdate:
          this.dialogProps &&
          (this.dialogProps.action === "add" ||
            this.dialogProps.action === "copy")
            ? true
            : false, // 是否修改密码
        isLocked: 0, // Kích hoạt
        phone: "", // Số điện thoại di động
        email: "", // Email
        qyOpenId: "", // 企业openid
        remarks: "", // Thông tin ghi chú
        // userRoleList: [], // Danh sách bảng con
      };
    },

    initOptions(This) {
      let company_search = {
        params: [],
      };
      // Use the company selected for this form (parent row / record), not the
      // logged-in user's company — system (id 1001) has company_id 0, which
      // would return no clinic offices. Fall back to currentUser.company.
      const formCompanyId = (This && This.company && This.company.id) ? This.company.id : currentUser.company.id;
      company_search.params.push.apply(company_search.params, [
        {
            columnName: "company_id",
            queryType: "=",
            value: formCompanyId,
          },
        { columnName: "is_locked", queryType: "=", value: "0" },
      ]);
      // Quyền dữ liệu: Khoaclinic_office
      this.pushDataPermissions(
        company_search.params,
        this.$route.meta.routerId,
        "1026680647570219053"
      );
      this.department_List.splice(0, this.department_List.length);
      listClinicOfficeAll(company_search).then((responseData) => {
        this.department_List = responseData.data;
      }).catch((error) => {
        this.outputError(error);
      });

      const company_id = This.company.id;
      this.key_department++;
      let department_search = {
        params: [
          { columnName: "company_id", queryType: "=", value: This.company.id },
        ],
      };
      // // Trên bảng tương ứng của trườngfilterĐiều kiện
      // department_search.params.push.apply(department_search.params, []);
      // // Quyền dữ liệu: Phòng banorg_department
      // this.pushDataPermissions(
      //   department_search.params,
      //   this.$route.meta.routerId,
      //   "52676365136650250"
      // );
      // this.department_List.splice(0, this.department_List.length);
      // treeDepartment(department_search).then((responseData) => {
      //   // this.department_List = responseData.data;
      // });

      // Bảng con  Vai trò người dùng
      This = this.userRoleCurrentRow;
      let role_UserRole_search = {
        params: [],
      };
      // Trên bảng tương ứng của trườngfilterĐiều kiện
      role_UserRole_search.params.push.apply(role_UserRole_search.params, []);
      // Bảng có trường bị vô hiệu hóa
      role_UserRole_search.params.push.apply(role_UserRole_search.params, [
        { columnName: "is_locked", queryType: "=", value: "0" },
        { columnName: "company_id", queryType: "=", value: company_id },
      ]);
      // Quyền dữ liệu: Vai tròsys_role
      this.pushDataPermissions(
        role_UserRole_search.params,
        this.$route.meta.routerId,
        "4012"
      );
      this.role_UserRole_List.splice(0, this.role_UserRole_List.length);
      listRoleAll(role_UserRole_search).then((responseData) => {
        this.role_UserRole_List = responseData.data;
      }).catch((error) => {
        this.outputError(error);
      });
    },
    //根据手机号获取表单信息
    async byPhoneGetList(phone) {
      const res = await changemylist(phone);
      if (res.code === 100 && res.data) {
        this.bizFormModel = { ...res.data, userHeader: "" };
        await this.getPhoto(res.data.userExt.photoId);
        this.bizFormModel.userExt.jobType = "";
        this.bizFormModel.userExt.startWorkTime = "";
        this.bizFormModel.userExt.endWorkTime = "";
        this.bizFormModel.userExt.isDuty = "";
        this.bizFormModel.userExt.description = "";
        this.bizFormModel.userExt.post = "";
        this.bizFormModel.userExt.office = "";
      } else if (res.code === 100 && !res.data) {
        return;
      } else {
        this.$message({
          type: "error",
          message: "此号码已注册",
        });
      }
    },
    getPhoto(id) {
      if(!id){
        this.bizFormModel.userHeader = ''
      }else{
        getPhotoById(id).then((res) => {
        const src = `data:image/png;base64,${res}`;
        this.bizFormModel.userHeader = src;
        return src;
      }).catch((error) => {
        this.outputError(error);
      });
      }
    },
    async openViewUserDialog(user) {
      if(user.userExt!==undefined){
        this.photoId = user.userExt.photoId;
        await this.getPhoto(user.userExt.photoId);
      }else{
        this.photoId = null
        this.bizFormModel = this.initFormModel()
      }
      this.dialogProps.action = "view";
      this.dialogProps.title = "Xem người dùng";
      this.bizFormModel = { ...this.bizFormModel, ...user };
      this.tabIndex = "1";
      this.initOptions(this.bizFormModel);
      this.dialogProps.visible = true;
    },
    async openEditUserDialog(user) {
      if(user.userExt!==undefined){
        this.photoId = user.userExt.photoId;
        await this.getPhoto(user.userExt.photoId);
      }else{
        this.photoId = null
        this.bizFormModel = this.initFormModel()
      }
      this.dialogProps.action = "edit";
      this.dialogProps.title = "Sửa người dùng";
      this.bizFormModel = { ...this.bizFormModel, ...user };
      this.tabIndex = "1";
      this.initOptions(this.bizFormModel);
      this.dialogProps.visible = true;
    },
    openAddUserDialog(parent) {
      this.dialogProps.action = "add";
      this.dialogProps.title = "Thêm người dùng";
      this.bizFormModel = this.initFormModel(parent);
      this.initOptions(this.bizFormModel);
      this.tabIndex = "1";
      this.dialogProps.visible = true;
    },
    openCopyUserDialog(user) {
      this.dialogProps.action = "copy";
      this.dialogProps.title = "Thêm người dùng";
      this.bizFormModel = user;
      this.initOptions(this.bizFormModel);
      this.tabIndex = "1";
      this.bizFormModel.id = null;
      for (let i = 0; i <= this.bizFormModel.userRoleList.length - 1; i++) {
        this.bizFormModel.userRoleList[i].id = null;
      }
      this.bizFormModel.loginPasswordUpdate = true;
      this.dialogProps.visible = true;
    },
  },
  watch: {
    //监听输入框手机号位数
    "bizFormModel.phone"(newValue, oldValue) {
      //  if( this.dialogProps.action == 'edit' ){
      if (newValue.length === 11 && this.dialogProps.action === "add") {
        this.byPhoneGetList(newValue);
      }
    },
  },
};
</script>
<style lang="scss" scoped>
.tab-item {
  .avatar-uploader {
    width: 100px;
    height: 100px;
    border: 1px dashed #d9d9d9;
  }
  .avatar-uploader .el-upload {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    width: 100%;
    height: 100%;
  }
  .avatar-uploader .el-upload:hover {
    border-color: #409eff;
  }
  .avatar-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 100px;
    height: 100px;
    line-height: 100px;
    text-align: center;
  }
  .avatar {
    width: 100px;
    height: 100px;
    display: block;
  }
  position: relative;
  .tab-option {
    position: absolute;
    top: -46px;
    right: 0;
  }
}
</style>
