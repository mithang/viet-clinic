<template>
  <el-dialog :title='dialogProps.title' :visible.sync='dialogProps.visible' :close-on-click-modal='false' width='70%'
    @open='onDialogOpen()' v-loading='loading'>
    <div slot='title' class='dialog-header'>
      {{ dialogProps.title }}
      <OperationIcon v-show='dialogProps.action === "view" && permission.edit' type='primary' text='Sửa' placement='top-start' icon-name='el-icon-edit' @click='switchEdit'></OperationIcon>
    </div>
    <el-form :model='bizFormModel' :rules='formRules'
      ref='noticeSendForm' label-width='120px' label-position='top' class='edit-form'>
        <el-row :gutter="20">
          <el-col :span='24/2'>
            <el-form-item label='Tiêu đề' prop='title' >
              <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.title' :maxlength='50' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập tiêu đề"' autofocus></el-input>
            </el-form-item>
         </el-col>
          <el-col :span='24/2'>
            <el-form-item label='Người nhận' prop='receivedBy' >
              <el-cascader
                :disabled='dialogProps.action === "view"'
                :key="cascaderKey"
                v-model="bizFormModel.receivedBy"
                :options="options"
                :props="props"
                collapse-tags
                clearable></el-cascader>
            </el-form-item>
          </el-col>
        </el-row>
      <el-row style="margin-top:10px" :gutter="20">
         <el-col :span='24/4'>
            <el-form-item  label-width="0" prop='isSite' >
              <el-checkbox :disabled='dialogProps.action === "view"' border  label="Có phải tin nhắn trong hệ thống không" v-model='bizFormModel.isSite' active-color='#13ce66' inactive-color='#dbdfe6' :true-label="'1'" :false-label="'0'"></el-checkbox>
            </el-form-item>
         </el-col>
        <el-col :span='24/4'>
          <el-form-item label-width="0" prop='isEmail' >
            <el-checkbox :disabled='dialogProps.action === "view"' border label="Có phải thông báo email không" v-model='bizFormModel.isEmail' active-color='#13ce66' inactive-color='#dbdfe6' :true-label="'1'" :false-label="'0'"></el-checkbox>
          </el-form-item>
        </el-col>
        <el-col :span='24/4'>
          <el-form-item label-width="0" prop='isWechat' >
            <el-checkbox :disabled='dialogProps.action === "view"' border label="Có phải tin nhắn WeChat không" v-model='bizFormModel.isWechat' active-color='#13ce66' inactive-color='#dbdfe6' :true-label="'1'" :false-label="'0'"></el-checkbox>
          </el-form-item>
        </el-col>
         <el-col :span='24/4'>
              <el-form-item label-width="0" prop='isSms' >
                <el-checkbox :disabled='dialogProps.action === "view"' border label="Có gửi SMS không" v-model='bizFormModel.isSms' active-color='#13ce66' inactive-color='#dbdfe6' :true-label="'1'" :false-label="'0'"></el-checkbox>
              </el-form-item>
          </el-col>
      </el-row>
      <el-row>
        <el-col :span='24'>
          <el-tabs tab-position="top" >
            <el-tab-pane label="Nội dung tin nhắn trong hệ thống" :disabled="bizFormModel.isSite === '0'">
              <div style="height: 345px">
                   <Editor ref="editorSite"  v-model="bizFormModel.siteContent" :disabled="bizFormModel.isSite === '0'  || dialogProps.action === 'view'">
                   </Editor>
              </div>
            </el-tab-pane>
            <el-tab-pane label="Nội dung tin nhắn email" :disabled="bizFormModel.isEmail === '0'">
              <div style="height: 345px">
                <Editor ref="editorEmail"  v-model="bizFormModel.emailContent"  :disabled="bizFormModel.isEmail === '0' || dialogProps.action === 'view'">
                </Editor>
              </div>
            </el-tab-pane>
            <el-tab-pane label="Nội dung tin nhắn WeChat" :disabled="bizFormModel.isWechat === '0'">
              <el-form-item label-width="0">
                <el-input type="textarea"  v-model="bizFormModel.wechatContent" :disabled="bizFormModel.isWechat === '0'"></el-input>
              </el-form-item>
            </el-tab-pane>
            <el-tab-pane label="Nội dung tin nhắn SMS" :disabled="bizFormModel.isSms === '0'">
              <el-form-item label-width="0">
                <el-input type="textarea" :disabled="bizFormModel.isSms === '0'" v-model="bizFormModel.smsContent"></el-input>
              </el-form-item>
            </el-tab-pane>
          </el-tabs>
        </el-col>
      </el-row>
      <!--  <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Nội dung tin nhắn trong hệ thống' prop='siteContent' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.siteContent' :maxlength='1000' :placeholder='dialogProps.action === "view"? "" : "请输入站内消息内容"' ></el-input>
          </el-form-item>
        </el-col>
        <el-col :span='24/2'>
          <el-form-item label='Nội dung tin nhắn WeChat' prop='wechatContent' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.wechatContent' :maxlength='500' :placeholder='dialogProps.action === "view"? "" : "请输入微信消息内容"' ></el-input>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span='24/2'>
              <el-form-item label='Nội dung tin nhắn email' prop='emailContent' >
                <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.emailContent' :maxlength='1000' :placeholder='dialogProps.action === "view"? "" : "请输入邮件消息内容"' ></el-input>
              </el-form-item>
        </el-col>
        <el-col :span='24/2'>
          <el-form-item label='Nội dung tin nhắn SMS' prop='smsContent' >
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.smsContent' :maxlength='500' :placeholder='dialogProps.action === "view"? "" : "请输入短信消息内容"' ></el-input>
          </el-form-item>
        </el-col>
      </el-row>-->
          <el-row style="margin-top:10px">
            <el-col >
                <el-form-item label='Thông tin ghi chú' label-width="70px" prop='remarks' >
                  <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.remarks' type='textarea'
                            :autosize="{ minRows: 3}"
                            :maxlength='255' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập thông tin ghi chú"'  clearable></el-input>
                  </el-form-item>
            </el-col>
          </el-row>
    </el-form>
    <span slot='footer' class='dialog-footer'>
      <el-button v-if='dialogProps.action !== "view"' :disabled="flag" type='primary' :plain='true' @click='onSubmit("noticeSendForm")'>Lưu</el-button>
      <el-button v-if='dialogProps.action !== "view"' :plain='true' @click='onDialogClose()'>Hủy</el-button>
      <el-button v-if='dialogProps.action === "view"' :plain='true' @click='onDialogClose()'>Đóng</el-button>
    </span>
  </el-dialog>
</template>
<script>
import { validatenull } from '@/utils/validate'
import { saveNoticeSend } from '@/api/noticesend/noticeSend'
import BaseUI from '@/views/components/baseUI'
import OperationIcon from '@/components/OperationIcon'
import { getCompanyTree } from '@/api/noticesend/noticeSend'
import Editor from  "./editor.vue"
export default {
  extends: BaseUI,
  name: 'noticeSend-form',
  components: {
    OperationIcon,
    Editor
  },
  data() {
    return {
      bizFormModel: this.initFormModel(),
      cascaderKey:0,
      options:[],
      flag:false,
      props: { multiple: true,
        emitPath:false,
        value: 'id',
        label: 'name',
        children: 'children' },  //多选
        
      dialogProps: {
        visible: false,
        action: '',
        title: '',
        
      },
      formRules: {
        'title': [
            { required: true, message: 'Vui lòng nhập tiêu đề', trigger: 'blur' }
        ],
        'receivedBy': [
            { required: true, message: '请输入接收人', trigger: 'blur' }
        ],
      }
    }
  },
  watch: {
    'bizFormModel.isSite':{     //站内消息
      handler(newValue,old){
        if(newValue === "0"){
          this.bizFormModel.siteContent = "";
          this.$refs.editorSite.content = "";
        }
      }
    },
    'bizFormModel.isEmail':{     //邮件消息
      handler(newValue,old){
        if(newValue === "0"){
          this.bizFormModel.emailContent = "";
          this.$refs.editorEmail.content = "";
        }
      }
    },
    'bizFormModel.isWechat':{     //微信消息
      handler(newValue,old){
        if(newValue === "0"){
          this.bizFormModel.wechatContent = "";
        }
      }
    },
    'bizFormModel.isSms':{        //短信消息
      handler(newValue,old){
        if(newValue === "0"){
          this.bizFormModel.smsContent = "";
        }
      }
    },
  },
  props: {
    // Quyền
    permission: {
      type: Object
    }
  },
  methods: {
    onSubmit(formName) {
      this.flag=true
      this.$refs[formName].validate(valid => {
        if (valid) {
          this.doSave()
        } else {
          this.flag=false
          return false
        }
      });
    },
    doSave() {
      this.setLoad()
      this.bizFormModel.receivedBy = this.bizFormModel.receivedBy.toString();
      saveNoticeSend(this.bizFormModel).then(responseData => {
        this.flag=false
        if(responseData.code === 100) {
          this.dialogProps.visible = false
          this.$emit('save-finished')
        } else {
          this.showMessage(responseData)
        }
        this.resetLoad()
      }).catch(error => {
        this.flag=false
        this.outputError(error)
      })
    },
    switchEdit() {
      this.dialogProps.action = 'edit'
      this.dialogProps.title = 'Sửa thông báo gửi tin nhắn'
    },
    onDialogClose() {
      this.dialogProps.visible = false
    },
    onDialogOpen() {
      this.$nextTick(() => {
        this.$refs['noticeSendForm'].clearValidate()
      })
    },
    initFormModel(This) {
      return {
        'senderId': currentUser.id,   // 消息发送人id
        'title': '',   // Tiêu đề
        'isSite': '0',   // Có phải tin nhắn trong hệ thống không
        'isWechat': '0',   // Có phải tin nhắn WeChat không
        'isEmail': '0',   // 是否邮件
        'isSms': '0',   // 是否短信
        'siteContent': '',   // Nội dung tin nhắn trong hệ thống
        'wechatContent': '',   // Nội dung tin nhắn WeChat
        'emailContent': '',   // Nội dung tin nhắn email
        'smsContent': '',   // Nội dung tin nhắn SMS
        'receivedBy': '',   // Người nhận
        'remarks': '',   // Thông tin ghi chú

      }
    },
    initOptions(This) {
      This.receivedBy = This.receivedBy.split(",");
    },
    companyTree(){
      getCompanyTree('2').then(result =>{
        if(result.code === 100){
          this.options = result.data;
        }
      });
    },
    openViewNoticeSendDialog(noticeSend) {
      this.dialogProps.action = 'view'
      this.dialogProps.title = 'Xem thông báo gửi tin nhắn'
      this.bizFormModel = noticeSend
      this.dialogProps.visible = true
      this.initOptions(this.bizFormModel)
    },
    openEditNoticeSendDialog(noticeSend) {
      this.dialogProps.action = 'edit'
      this.dialogProps.title = 'Sửa thông báo gửi tin nhắn'
      this.bizFormModel = noticeSend
      this.dialogProps.visible = true
      this.initOptions(this.bizFormModel)
    },
    openAddNoticeSendDialog() {
      this.dialogProps.action = 'add'
      this.dialogProps.title = 'Thêm thông báo gửi tin nhắn'
      this.bizFormModel = this.initFormModel()
      this.dialogProps.visible = true
      this.initOptions(this.bizFormModel)
    },
    openCopyNoticeSendDialog(noticeSend) {
      this.dialogProps.action = 'add'
      this.dialogProps.title = 'Thêm thông báo gửi tin nhắn'
      this.bizFormModel = noticeSend
      this.bizFormModel.id = null
      this.dialogProps.visible = true
      this.initOptions(this.bizFormModel)
    },
  },
  mounted: function() {
    this.companyTree();
  },
}
</script>

