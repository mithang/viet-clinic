<template>
  <el-dialog
    :title="dialogProps.title"
    :visible.sync="dialogProps.visible"
    :close-on-click-modal="false"
    width="60%"
    @open="onDialogOpen()"
    v-loading="loading"
  >
    <div>
      <el-row>
        <el-form :label-position="labelPosition" :model="formLabelAlign"
                 :rules="rules"
                 ref="formLabelAlign">
          <el-col :span="24">
            <el-form-item>
              <span class="bt">Thông tin cơ bản của mẫu</span>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item
              prop="mbmc"
              label="Tên mẫu"
            >
              <el-input v-model="formLabelAlign.mbmc" style="width:70%"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item
              label="Loại mẫu"
            >
              <el-radio v-model="mblx" label="0">Dùng chung</el-radio>
              <el-radio v-model="mblx" label="1">Cá nhân</el-radio>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item
              label="Loại bệnh án"
            >
              <el-radio v-model="bllx" label="0">Bệnh án Tây y</el-radio>
              <el-radio v-model="bllx" label="1">Bệnh án Đông y</el-radio>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item>
              <span class="bt">Nội dung mẫu</span>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="Lý do khám chính">
              <el-input
                type="textarea"
                autosize
                placeholder="Vui lòng nhập nội dung"
                style="width:50%"
                v-model="formLabelAlign.zs">
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="Tiền sử cá nhân">
              <el-input
                type="textarea"
                autosize
                placeholder="Vui lòng nhập nội dung"
                style="width:50%"
                v-model="formLabelAlign.grs">
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="Tiền sử dị ứng">
              <el-input
                type="textarea"
                autosize
                placeholder="Vui lòng nhập nội dung"
                style="width:50%"
                v-model="formLabelAlign.gms">
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="Tiền sử bệnh">
              <el-input
                type="textarea"
                autosize
                placeholder="Vui lòng nhập nội dung"
                style="width:50%;"
                v-model="formLabelAlign.jbs">
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="Tiền sử bệnh truyền nhiễm">
              <el-input
                type="textarea"
                autosize
                placeholder="Vui lòng nhập nội dung"
                style="width:50%;"
                v-model="formLabelAlign.crbs">
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="Tiền sử phẫu thuật">
              <el-input
                type="textarea"
                autosize
                placeholder="Vui lòng nhập nội dung"
                style="width:50%;"
                v-model="formLabelAlign.sss">
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="Tiền sử truyền máu">
              <el-input
                type="textarea"
                autosize
                placeholder="Vui lòng nhập nội dung"
                style="width:50%;"
                v-model="formLabelAlign.sxs">
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="Khám thực thể">
              <el-input
                type="textarea"
                autosize
                placeholder="Vui lòng nhập nội dung"
                style="width:50%;"
                v-model="formLabelAlign.tgjc">
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="Chẩn đoán cấp cứu">
              <el-input
                type="textarea"
                autosize
                placeholder="Vui lòng nhập nội dung"
                style="width:50%;"
                v-model="formLabelAlign.jzzd">
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="Hiệu quả cấp cứu">
              <el-input
                type="textarea"
                autosize
                placeholder="Vui lòng nhập nội dung"
                style="width:50%;"
                v-model="formLabelAlign.jzxg">
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="Tiền sử gia đình">
              <el-input
                type="textarea"
                autosize
                placeholder="Vui lòng nhập nội dung"
                style="width:50%;"
                v-model="formLabelAlign.jzs">
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="Tiền sử kinh nguyệt">
              <el-input
                type="textarea"
                autosize
                placeholder="Vui lòng nhập nội dung"
                style="width:50%;"
                v-model="formLabelAlign.yjz">
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="Tiền sử hôn nhân/sinh đẻ">
              <el-input
                type="textarea"
                autosize
                placeholder="Vui lòng nhập nội dung"
                style="width:50%;"
                v-model="formLabelAlign.hys">
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="Xét nghiệm bổ sung">
              <el-input
                type="textarea"
                autosize
                placeholder="Vui lòng nhập nội dung"
                style="width:50%;"
                v-model="formLabelAlign.fzjc">
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="Bệnh sử hiện tại">
              <el-input
                type="textarea"
                autosize
                placeholder="Vui lòng nhập nội dung"
                style="width:50%;"
                v-model="formLabelAlign.xbs">
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="Tiền sử bệnh">
              <el-input
                type="textarea"
                autosize
                placeholder="Vui lòng nhập nội dung"
                style="width:50%;"
                v-model="formLabelAlign.jws">
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="Tiền sử dịch tễ">
              <el-input
                type="textarea"
                autosize
                placeholder="Vui lòng nhập nội dung"
                style="width:50%;"
                v-model="formLabelAlign.lxbxs">
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="Xét nghiệm khác">
              <el-input
                type="textarea"
                autosize
                placeholder="Vui lòng nhập nội dung"
                style="width:50%;"
                v-model="formLabelAlign.qtjc">
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="Tình hình xử lý">
              <el-input
                type="textarea"
                autosize
                placeholder="Vui lòng nhập nội dung"
                style="width:50%;"
                v-model="formLabelAlign.clqk">
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="Giáo dục sức khỏe cá nhân hóa" >
              <el-input
                type="textarea"
                autosize
                placeholder="Vui lòng nhập nội dung"
                style="width:50%;"
                v-model="formLabelAlign.gthjkjy">
              </el-input>
            </el-form-item>
          </el-col>
        </el-form>
      </el-row>
      <!-- <div class="footer">
          <el-button type="primary" @click="submitForm('formLabelAlign')" >Gửi</el-button>
          <el-button @click="resetForm('formLabelAlign')" >Đặt lại</el-button>
      </div> -->
    </div>
    <span slot='footer' class='dialog-footer'>
      <el-button :disabled="flag" type='primary' :plain='true' @click="submitForm('formLabelAlign')">Lưu</el-button>
      <el-button :plain='true' type="primary" @click='onDialogClose()'>Hủy</el-button>
    </span>
  </el-dialog>
</template>

<script>
  import BaseUI from "@/views/components/baseUI";
  import VDistpicker from 'v-distpicker';
  import {saveblmb, selectmbbm, updatembbm} from '@/api/outpatient/blmb'


  export default {
    extends: BaseUI,
    name: "blmb-form",
    components: {
      VDistpicker
    },
    props: ['closeValue'],
    data() {
      return {
        mblx: "0",
        bllx: "0",
        labelPosition: 'top',
        formLabelAlign: {
          mbbm: '',
          companyId: currentUser.company.id,
          delFlag: '0',
          createdBy: currentUser.name + "(" + currentUser.loginname + ")",
          createdTime: '',
          updatedBy: '',
          updatedTime: '',
          createdID: currentUser.id,
          mbmc: '',
          mblx: '',
          bllx: '',
          zs: '',
          grs: '',
          gms: '',
          jbs: '',
          crbs: '',
          sss: '',
          sxs: '',
          tgjc: '',
          jzzd: '',
          jzxg: '',
          jzs: '',
          yjz: '',
          hys: '',
          fzjc: '',
          xbs: '',
          jws: '',
          lxbxs: '',
          qtjc: '',
          clqk: '',
          gthjkjy: '',
        },
        dialogProps: {
          visible: false,
          action: '',
          title: '',
        },
        rules: {
          mbmc: [
            {required: true, message: 'Vui lòng nhập tên mẫu', trigger: 'blur'},
          ],

        },
        blmbcxrc: {
          companyId: currentUser.company.id,
          mbbm: '',
        },
      };
    },
    methods: {
      submitForm(formName) {
        this.$refs[formName].validate((valid) => {
          if (valid) {
            this.formLabelAlign.mblx = this.mblx;
            this.formLabelAlign.bllx = this.bllx;
            if (this.formLabelAlign.mbbm !== '' && this.formLabelAlign.mbbm != null) {
              this.formLabelAlign.updatedBy = currentUser.name + "(" + currentUser.loginname + ")",
                this.Updateblmbinfo(this.formLabelAlign)
            } else {
              saveblmb(this.formLabelAlign).then((responseData) => {
                if (responseData.code === 100) {
                  this.onDialogClose();
                  this.$message.success(responseData.msg);
                }
              })
                .catch((error) => {
                  this.$message.error(error);
                });
            }
          } else {
            return false;
          }
        });
      },
      resetForm(formName) {
        this.$refs[formName].resetFields();
      },
      onDialogOpen() {
        this.$nextTick(() => {
          this.$refs["blmbForm"].clearValidate();
        });
      },
      onDialogClose() {
        this.$emit('typeclick', "");
        this.dialogProps.visible = false;

      },
      Getselectmbbm(types) {
        this.blmbcxrc.mbbm = types.mbbm;
        selectmbbm(this.blmbcxrc).then((responseData) => {
          if (responseData.code === 100) {
            responseData.data.forEach(values => {
              this.bllx = values.bllx;
              this.mblx = values.mblx;
              this.formLabelAlign.mbbm = values.mbbm;
              this.formLabelAligncompanyId = currentUser.company.id;
              this.formLabelAlign.delFlag = values.delFlag;
              this.formLabelAlign.createdBy = values.createdBy;
              this.formLabelAlign.createdTime = values.createdTime;
              this.formLabelAlign.updatedBy = currentUser.name + "(" + currentUser.loginname + ")";
              this.formLabelAlign.updatedTime = '';
              this.formLabelAlign.createdID = values.createdID;
              this.formLabelAlign.mbmc = values.mbmc;
              this.formLabelAlign.mblx = values.mblx;
              this.formLabelAlign.bllx = values.bllx;
              this.formLabelAlign.zs = values.zs;
              this.formLabelAlign.grs = values.grs;
              this.formLabelAlign.gms = values.gms;
              this.formLabelAlign.jbs = values.jbs;
              this.formLabelAlign.crbs = values.crbs;
              this.formLabelAlign.sss = values.sss;
              this.formLabelAlign.sxs = values.sxs;
              this.formLabelAlign.tgjc = values.tgjc;
              this.formLabelAlign.jzzd = values.jzzd;
              this.formLabelAlign.jzxg = values.jzxg;
              this.formLabelAlign.jzs = values.jzs;
              this.formLabelAlign.yjz = values.yjz;
              this.formLabelAlign.hys = values.hys;
              this.formLabelAlign.fzjc = values.fzjc;
              this.formLabelAlign.xbs = values.xbs;
              this.formLabelAlign.jws = values.jws;
              this.formLabelAlign.lxbxs = values.lxbxs;
              this.formLabelAlign.qtjc = values.qtjc;
              this.formLabelAlign.clqk = values.clqk;
              this.formLabelAlign.gthjkjy = values.gthjkjy;
            });
          }
        })
          .catch((error) => {
            this.$message.error(error);
          });
      },
      Updateblmbinfo(values) {
        updatembbm(values).then((responseData) => {
          if (responseData.code === 100) {
            this.onDialogClose();
            this.$message.success(responseData.msg);
          }
        })
          .catch((error) => {
            this.$message.error(error);
          });
      },
      openAddworkbenchDialog(types) {
        let titles = "";
        if (types === "") {
          titles = "Thêm mới"
        } else if (types !== "") {
          titles = "Sửa"
        } else {
          titles = ""
        }
        if (types !== "" && types != null) {
          this.Getselectmbbm(types);
        } else {
          this.mblx = "0"
          this.bllx = "0"
          this.formLabelAlign = {
            mbbm: '', companyId: currentUser.company.id, delFlag: '0',
            createdBy: currentUser.name + "(" + currentUser.loginname + ")",
            createdTime: '', updatedBy: '', updatedTime: '', createdID: currentUser.id,
            mbmc: '', mblx: '', bllx: '', zs: '', grs: '', gms: '', jbs: '', crbs: '',
            sss: '', sxs: '', tgjc: '', jzzd: '', jzxg: '', jzs: '', yjz: '', hys: '',
            fzjc: '', xbs: '', jws: '', lxbxs: '', qtjc: '', clqk: '', gthjkjy: '',
          };
        }
        this.dialogProps.action = 'add'
        this.dialogProps.title = titles + 'Thông tin mẫu'
        this.tabIndex = '1'
        this.dialogProps.visible = true
        this.province = ''
        this.city = ''
        this.area = ''
      },
    },
    mounted: function () {
    },
  }
</script>

<style lang="scss">
  .bt {
    font-size: 18px;
    font-weight: bold;
  }

  .footer {
    position: fixed;
    bottom: 0;
    width: 80%;
    line-height: var(--footer-height);
    text-align: right;
    margin: 10px 0 10px 0;
  }
</style>
