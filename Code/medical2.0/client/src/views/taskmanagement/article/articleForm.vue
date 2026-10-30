<template>
  <el-dialog
    :title="dialogProps.title"
    :visible.sync="dialogProps.visible"
    :close-on-click-modal="false"
    width="65%"
    class="articleForm"
    @open="onDialogOpen()"
    :before-close="onDialogClose"
    v-loading="loading"
  >
    <div>
      <el-form
        ref="articlesForm"
        :model="articleForm"
        :rules="article"
        label-width="20%"
        class="demo-articleForm"
      >
        <el-form-item label="Tiêu đề" prop="title">
          <el-input :disabled="dialogProps.audit || articleForm.auditstatus!=='0'" placeholder="Vui lòng nhập" v-model="articleForm.title"></el-input>
        </el-form-item>
        <el-form-item label="Tóm tắt" prop="abstracts">
          <el-input
            type="textarea"
            :rows="3"
            :disabled="dialogProps.audit || articleForm.auditstatus!=='0'"
            placeholder="Vui lòng điền tóm tắt"
            v-model="articleForm.abstracts"
          >
          </el-input>
        </el-form-item>

        <el-form-item v-if="originallinkcheckbox && articleForm.auditstatus==='0'" label="Nội dung chính" prop="content">
      
            <editor
            ref="quilleditorref"
            v-model="articleForm.content"
            :min-height="192"
            :options="editoroption"
          >
          </editor>
        </el-form-item>
       
       <div v-if="!originallinkcheckbox || articleForm.auditstatus!=='0'">
         <el-form-item   label="Nội dung chính" prop="content">
           <div>
              <p v-html="sanitizedContent"></p>
          </div>
        </el-form-item>
       </div>

       

        <el-form-item label="Tác giả nguồn" prop="sources">
          <el-col :span="1">
            <el-checkbox :disabled="dialogProps.audit || articleForm.auditstatus!=='0'" v-model="sourcecheckbox"></el-checkbox>
          </el-col>
          <el-col :span="23">
            <el-input
              v-model="articleForm.sources"
              :disabled="!sourcecheckbox || articleForm.auditstatus!=='0'"
            ></el-input>
          </el-col>
        </el-form-item>
          
        <el-form-item label="Liên kết gốc" prop="originallink">
          <el-col :span="1">
            <el-checkbox :disabled="dialogProps.audit || articleForm.auditstatus!=='0'" v-model="originallinkcheckbox"></el-checkbox>
          </el-col>
          <el-col :span="23">
            <el-input
              v-model="articleForm.originallink"
              :disabled="!originallinkcheckbox || articleForm.auditstatus!=='0'"
            ></el-input>
          </el-col>
        </el-form-item>
        <!-- <el-form-item label="审核备注" prop="auditremarks" v-if="dialogProps.action == 'audit'">
            <el-input v-model="articleForm.auditremarks"></el-input>
        </el-form-item> -->
        <el-form-item v-if="dialogProps.action === 'audit'">
          <el-col :span="12">
            <el-button
              type="primary"
              :plain="true"
              @click="auditForm('1')"
              >Xét duyệt qua</el-button
            >
            <el-button
              type="primary"
              :plain="true"
              @click="auditForm('2')"
              >Không qua</el-button
            >
          </el-col>
        </el-form-item>
        
      </el-form>
    </div>
    <span slot="footer" class="dialog-footer">
      <el-button
        :disabled="flag"
        v-if="dialogProps.action === 'add'"
        type="primary"
        :plain="true"
        @click="submitForm('articlesForm')"
        >Lưu</el-button
      >
      <el-button
        :disabled="flag"
        v-if="dialogProps.action === 'update'&&articleForm.auditstatus==='0'"
        type="primary"
        :plain="true"
        @click="updateForm('articlesForm')"
        >Sửa</el-button
      >

      <el-button :plain="true" type="primary" @click="onDialogClose()"
        >Hủy</el-button
      >
    </span>
  </el-dialog>
</template>


<script>
import BaseUI from "@/views/components/baseUI";
import {
  editSave,
  getarticleid,
  editUpdate,
  editAudit,
} from "@/api/taskmanagement/article";
import Editor from "./ue";
import { sanitizeHtml } from "@/utils/sanitizeHtml";
export default {
  extends: BaseUI,
  name: "article-form",
  components: {
    Editor,
  },
  computed: {
    key() {
      return this.$route.name !== undefined
        ? this.$route.name + +new Date()
        : this.$route + +new Date();
    },
    // Nội dung bài viết hiển thị ở chế độ xem/xét duyệt: BẮT BUỘC qua
    // sanitize trước khi đưa vào v-html, nếu không bài viết do người khác
    // soạn có thể chèn script (stored XSS) chạy trong trình duyệt người đọc.
    // Bài viết bình thường (đoạn, in đậm/nghiêng, danh sách, link, ảnh)
    // vẫn hiển thị đủ chữ và cấu trúc; chỉ mất căn lề/màu chữ của Quill
    // (do gỡ attribute style/class) và link/ảnh với scheme lạ.
    sanitizedContent() {
      return sanitizeHtml(this.articleForm.content);
    },
  },
  props: ["closeValue"],
  data() {
    const validatePass = (rule, value, callback) => {
      if (value === "") {
        callback(new Error("请输入正文"));
      } else {
        if (this.articleForm.content !== "") {
          this.$refs.articleForm.validateField("content");
        }
        callback();
      }
    };
    return {
      dialogProps: {
        visible: false,
        action: "",
        title: "",
        audit: false,
      },
      editorflg: true,
      sourcecheckbox: false,
      originallinkcheckbox: false,
      articleForm: {
        companyid: currentUser.company.id, //Định danh phòng khám
        userid: currentUser.id, //Định danh người đăng
        username: currentUser.name, //Tên người đăng
        aricledate: "", //Thời gian đăng
        title: "", //Tiêu đề
        abstracts: "", //Tóm tắt
        content: "", //Nội dung
        sources: "", //Tác giả nguồn
        originallink: "", //Liên kết gốc
        auditstatus: "0", //Mặc định chờ xét duyệt
        id: "",
      },

      article: {
        title: [{ required: true, message: "Vui lòng nhập tiêu đề", trigger: "blur" }],
        abstracts: [
          { required: true, message: "请输入摘要", trigger: "blur" },
          // { min: 3, max: 5, message: 'Độ dài trong khoảng 3 đến 5 ký tự', trigger: 'blur' }
        ],
        content: [
          { required: true, message: "请输入正文内容", trigger: "change" },
        ],
      },
    };
  },
  methods: {
    tableRowClassName({ row, rowIndex }) {
      if (rowIndex === 1) {
        return "warning-row";
      } else if (rowIndex === 3) {
        return "success-row";
      }
      return "";
    },
    emptylist() {
      this.articleForm = {
        companyid: currentUser.company.id, //Định danh phòng khám
        userid: currentUser.id, //Định danh người đăng
        username: currentUser.name, //Tên người đăng
        aricledate: "", //Thời gian đăng
        title: "", //Tiêu đề
        abstracts: "", //Tóm tắt
        content: "", //Nội dung
        sources: "", //Tác giả nguồn
        originallink: "", //Liên kết gốc
        auditstatus: "0", //Mặc định chờ xét duyệt
      };
    },
    onDialogClose() {
      this.originallinkcheckbox = false
      this.sourcecheckbox = false;
      this.originallinkcheckbox = false;
      this.editorflg = false;
      this.emptylist();
      this.dialogProps.visible = false;
      this.$emit("typeclick", "");
      
    },
    onDialogOpen() {
      this.editorflg = true;
      this.$nextTick(() => {
        this.$refs["articlesForm"].clearValidate();
      });
    },
    submitForm(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          //   console.info("Thời gian" + this.ruleForm.taskdeadline);
          //   this.ruleForm.taskinitiator = currentUser.id;
          editSave(this.articleForm).then((responseData) => {
            if (responseData.code === 100) {
              this.onDialogClose();
            }
          });
        }
      });
    },
    updateForm(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          if (
            this.articleForm.auditstatus !== "0" &&
            this.articleForm.auditstatus !== "2"
          ) {
            this.showMessage("Đã kiểm duyệt nên không thể sửa");
          } else {
            if (this.articleForm.userid === currentUser.id) {
              this.articleForm.auditstatus = "0"; //修改审核状态重新调整为0
              editUpdate(this.articleForm).then((responseData) => {
                if (responseData.code === 100) {
                  this.onDialogClose();
                }
              });
            } else {
              this.showMessage("Bài viết không phải do bạn đăng nên không thể sửa");
            }
          }
        }
      });
    },
    auditForm(type) {
      this.articleForm.auditstatus = type; //修改审核状态
      editAudit(this.articleForm).then((responseData) => {
        if (responseData.code === 100) {
          this.onDialogClose();
        }
      });
    },
    openAddworkbenchDialog(viewlist) {
      this.emptylist();
      let titles = viewlist.view;
      // if (viewlist.view == "") {
      //   titles = "Thêm mới";
      // } else if (viewlist.view != "") {
      //   titles = "Sửa";
      // } else {
      //   titles = "";
      // }

      if (titles === "Sửa" || titles === "Duyệt" || titles === "Xem") {
        this.articleForm.id = viewlist.id;
        getarticleid(this.articleForm).then((responseData) => {
          if (responseData.code === 100) {
            let refdata = responseData.data;
            this.articleForm.aricledate = refdata.aricledate;
            this.articleForm.title = refdata.title;
            this.articleForm.abstracts = refdata.abstracts;
            this.articleForm.content = refdata.content;
            this.articleForm.sources = refdata.sources;
            this.articleForm.originallink = refdata.originallink;
            this.articleForm.userid = refdata.userid;
            this.articleForm.auditstatus = refdata.auditstatus;
          }
        });
        this.dialogProps.action = "update";
        this.sourcecheckbox = true;
        this.originallinkcheckbox = true;
        if (titles === "Duyệt") {
          this.dialogProps.action = "audit";
          this.dialogProps.audit = true;
          this.sourcecheckbox = false;
          this.originallinkcheckbox = false;
        }else if(titles === "Xem"){
          this.dialogProps.action = "view";
          this.dialogProps.audit = true;
          this.sourcecheckbox = false;
          this.originallinkcheckbox = false;
      }
      } else {
        this.dialogProps.action = "add";

        this.dialogProps.audit = false;
        this.sourcecheckbox = true;
        this.originallinkcheckbox = true;
      }

      this.dialogProps.title = titles;
      this.tabIndex = "1";
      this.dialogProps.visible = true;
      this.province = "";
      this.city = "";
      this.area = "";
    },
  },
};
</script>


<style>
.el-table .warning-row {
  background: oldlace;
  color: #3eec5b;
  color: #a7761a;
  color: #f5314b;
}

.el-table .success-row {
  background: #f0f9eb;
}
</style>
