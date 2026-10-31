<template>
  <el-dialog title='Thông tin cá nhân' :visible.sync='dialogVisible' width='560px'
    @open='handleDialogOpen()' :lock-scroll='true' v-loading='loading'>
    <div slot='title' class='dialog-header'>Thông tin cá nhân</div>
    <el-form :model='userModel' :rules='rules'
      ref='personalInfoForm' label-width='80px' label-position='right'>
      <el-form-item label='Họ tên' prop='name'>
        <el-col :span='16'><el-input ref='username' :maxlength='32' v-model='userModel.name' autofocus></el-input></el-col>
      </el-form-item>
      <el-form-item label='Tên đăng nhập' prop='loginName'>
        <el-col :span='16'><el-input disabled :maxlength='32' v-model='userModel.loginName'></el-input></el-col>
      </el-form-item>
      <el-form-item label='Số điện thoại di động' prop='phone'>
        <el-col :span='16'><el-input :maxlength='11' v-model='userModel.phone'></el-input></el-col>
      </el-form-item>
      <!-- <el-form-item label='Email' prop='email'>
        <el-col :span='16'><el-input :maxlength='32' v-model='userModel.email'></el-input></el-col>
      </el-form-item> -->
      <el-form-item><el-checkbox v-model='userModel.loginPasswordUpdate' @change='handleChangePasswordChanged'>Đổi mật khẩu</el-checkbox></el-form-item>
      <el-form-item label='Mật khẩu' prop='loginPassword'>
        <el-col :span='16'>
          <el-input type='password' ref='password' :disabled='!userModel.loginPasswordUpdate' v-model='userModel.loginPassword' auto-complete='off'></el-input>
        </el-col>
      </el-form-item>
      <el-form-item label='Xác nhận mật khẩu' prop='loginPasswordConfirm'>
        <el-col :span='16'>
          <el-input type='password' v-model='userModel.loginPasswordConfirm' :disabled='!userModel.loginPasswordUpdate' auto-complete='off'></el-input>
        </el-col>
      </el-form-item>
    </el-form>
    <span slot='footer' class='dialog-footer'>
      <el-button type='primary' :plain='true' @click="submitForm('personalInfoForm')">Xác nhận</el-button>
      <el-button :plain='true' @click="handleDialogClose()">Hủy</el-button>
    </span>
  </el-dialog>
</template>

<script>
import { getCurrentUser, updateCurrentUser } from '@/api/admin/common/personalInfo'
import BaseUI from '@/views/components/baseUI'
import { getLocalCurrentUser } from '@/utils/auth'
export default {
  extends: BaseUI,
  name: 'personal-info',
  data() {
    const validatePassword = (rule, value, callback) => {
      if (this.userModel.loginPasswordUpdate && (value === '' || value === undefined)) {
        callback(new Error('Vui lòng nhập mật khẩu'));
      } else {
        if (this.userModel.loginPasswordConfirm !== '') {
          this.$refs.personalInfoForm.validateField('loginPasswordConfirm');
        }
        callback()
      }
    }
    const validateCheckPassword = (rule, value, callback) => {
      if (this.userModel.loginPasswordUpdate && (value === '' || value === undefined)) {
        callback(new Error('Vui lòng nhập lại mật khẩu'))
      } else if (this.userModel.loginPasswordUpdate && (value !== this.userModel.loginPassword)) {
        callback(new Error('Hai lần nhập mật khẩu không khớp!'))
      } else {
        callback()
      }
    }
    return {
      loading: false,
      userModel: {
        id: '',
        name: '',
        loginName: '',
        phone:'',
        email:'',
        loginPassword: '',
        loginPasswordUpdate: false
      },
      dialogVisible: false,
      rules: {
        name: [
          { required: true, message: 'Vui lòng nhập tên người dùng', trigger: 'blur' },
          { min: 3, max: 32, message: 'Độ dài trong khoảng 3 đến 32 ký tự', trigger: 'blur' }
        ],
        loginName: [
          { required: true, message: 'Vui lòng nhập tên đăng nhập', trigger: 'blur' },
          { min: 3, max: 32, message: 'Độ dài trong khoảng 3 đến 32 ký tự', trigger: 'blur' }
        ],
        phone: [
          { required: true, message: 'Vui lòng nhập số điện thoại di động', trigger: 'blur' },
          { validator: this.isMobileNumber, trigger: "blur" }
        ],
        email: [
          { required: true, message: 'Vui lòng nhập email', trigger: 'blur' },
          { validator: this.isEmail, trigger: "blur" }
        ],
        loginPassword: [
          { validator: validatePassword, trigger: 'blur' }
        ],
        loginPasswordConfirm: [
          { validator: validateCheckPassword, trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    isMobileNumber(rule, value, callback) {
      if (!value) {
        return new Error("Vui lòng nhập số điện thoại");
      } else {
        const reg = /^1[3|4|5|6|7|8|9][0-9]\d{8}$/;
        const isPhone = reg.test(value);
        value = Number(value); //Chuyển thành số
        if (typeof value === "number" && !isNaN(value)) {//Kiểm tra có phải là số không
          value = value.toString(); //Chuyển thành chuỗi
          if (value.length !== 11 || !isPhone) { //Kiểm tra có phải là11chữ số của số điện thoại di động
            callback(new Error("Định dạng số điện thoại ví dụ như:138xxxx8888"));
          } else {
            callback();
          }
        } else {
          callback(new Error("Vui lòng nhập số điện thoại"));
        }
      }
    },
    //Kiểm tra email
     isEmail(rule, value, callback) {
      if (!value) {
        callback();
      } else {
        const reg = /^[a-z0-9]+([._\\-]*[a-z0-9])*@([a-z0-9]+[-a-z0-9]*[a-z0-9]+.){1,63}[a-z0-9]+$/;
        const email = reg.test(value);
        if (!email) {
          callback(new Error("Email không đúng định dạng, ví dụ: ten@domain.com"));
        } else {
          callback();
        }
      }
    },
    submitForm(formName) {
      this.$refs[formName].validate(valid => {
        if (valid) {
          this.doUpdatePersonalInfo()
        } else {
          return false;
        }
      })
    },
    doUpdatePersonalInfo() {
      this.loading = true
      if(!this.userModel.loginPasswordUpdate) {
          this.userModel.password = ''
      }
      updateCurrentUser(this.userModel).then(responseData => {
        if(responseData.code === 100) {
          let currentUser = window.currentUser
          currentUser.name = this.userModel.name
          currentUser.phone = this.userModel.phone
          currentUser.email = this.userModel.email
          getLocalCurrentUser(currentUser)
          this.$emit('username-changed')
          this.userModel.loginPasswordUpdate = false
          this.loading = false
          this.dialogVisible = false
        } else {
          this.showMessage(responseData)
        }
      })
      .catch(error => {
        this.outputError(error)
      })
    },
    handleDialogClose() {
      this.dialogVisible = false
    },
    handleDialogOpen() {
      this.$nextTick(() => {
        this.$refs['personalInfoForm'].clearValidate()
        this.$refs['username'].focus()
      })
    },
    handleChangePasswordChanged(val) {
      this.$refs['personalInfoForm'].clearValidate()
      if(val) {
        this.$nextTick(() => {
            this.$refs['password'].focus()
        })
      }
    },
    openPersonalInfoDialog() {
      this.loading = true
      this.changePassword = false
      getCurrentUser()
        .then(responseData => {
          if (responseData.code === 100) {
            this.userModel = responseData.data
            this.userModel.password = ''
            this.dialogVisible = true
          } else {
            this.showMessage(responseData)
          }
        })
        .catch(error => {
          this.outputError(error)
        })
    },
  },
  mounted: function() {
  },
}
</script>



