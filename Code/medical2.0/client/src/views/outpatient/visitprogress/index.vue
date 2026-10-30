<template>

<el-card class="page-container">
<el-row>
  <el-col :span="24">
    <el-form :inline="true" :model="formInline" class="demo-form-inline">
    <el-form-item label="Chọn thời gian tra cứu">
  <div class="block">
    <el-date-picker
      v-model="TimeInterval"
      type="datetimerange"
      :picker-options="pickerOptions"
      range-separator="đến"
      start-placeholder="Ngày bắt đầu"
      end-placeholder="Ngày kết thúc"
      align="right">
    </el-date-picker>
  </div>
    </el-form-item>
      
  <el-form-item label="Tra cứu bệnh nhân">
    <el-input v-model="formInline.values" placeholder="Vui lòng nhập họ tên/Số điện thoại di động/CMND/CCCD"></el-input>
  </el-form-item>
  <el-form-item label="Trạng thái khám">
    <el-select v-model="formInline.status" placeholder="Trạng thái khám">
      <el-option label="Tất cả" value="qb"></el-option>
      <el-option label="Chờ khám" value="registrationStatus_0"></el-option>
      <el-option label="Đã khám" value="registrationStatus_1"></el-option>
      <el-option label="Đã hủy" value="registrationStatus_2"></el-option>
      <el-option label="Chờ check-in" value="registrationStatus_3"></el-option>
      <el-option label="Đã hết hiệu lực" value="registrationStatus_4"></el-option>
    </el-select>
  </el-form-item>
  <el-form-item>
    <el-button type="primary" @click="onSubmit">Truy vấn</el-button>
    <el-button
      type="info"
      icon="el-icon-refresh-left"
      @click="reset"
      :plain="true"
    >Đặt lại</el-button>
  </el-form-item>
</el-form>
  </el-col>
  <el-col :span="24">
    <el-table
    :data="tableData"
    style="width: 100%">
    <el-table-column
             label="Số thứ tự"
              type="index"
              :index="indexMethod"
              align="center">
            </el-table-column>
    <el-table-column
      prop="jzzt"
      label="Trạng thái khám"
      >
    </el-table-column>
    <!-- <el-table-column
      prop="xm"
      label="Bệnh nhân"
      >
    </el-table-column> -->
<el-table-column
      label="Bệnh nhân">
      <template slot-scope="scope">
        <!-- <el-popover trigger="hover" placement="top" >
          <span><a>{{ scope.row.xm }}</a><a style="margin-left:10px;">{{ scope.row.xb }}</a><a style="margin-left:10px;">{{ scope.row.sr }}</a><a style="margin-left:10px;">{{ scope.row.phone }}</a></span>
          <div slot="reference" style="width:auto;">
            <span>{{ scope.row.xm }}</span>
          </div>
        </el-popover> -->
        <el-popover
    placement="top-start"
    trigger="hover">
    <v-slot:content>
      <a>{{ scope.row.xm }}</a><a style="margin-left:10px;">{{ scope.row.xb }}</a><a style="margin-left:10px;">{{ scope.row.sr }}</a><a style="margin-left:10px;">{{ scope.row.phone }}</a>
      </v-slot:content>
    <a slot="reference">{{ scope.row.xm }}</a>
  </el-popover>
 </template>
    </el-table-column>


    <el-table-column
      prop="ks"
      label="Khoa"
      >
    </el-table-column>
    <el-table-column
      prop="djsj"
      label="Thời gian đăng ký"
      >
    </el-table-column>
    <el-table-column
      prop="jzlx"
      label="Loại tiếp nhận"
      >
    </el-table-column>
    <el-table-column
      prop="ys"
      label="Bác sĩ tiếp nhận"
      >
    </el-table-column>
    <el-table-column
      prop="cfs"
      label="Số lượng đơn thuốc"
      >
    </el-table-column>
  </el-table>
  </el-col>
</el-row>
<el-row>
        <el-col :span='24'>
          <el-pagination
            background     
            @size-change='onSizeChange'
            @current-change='onCurrentChange'
            :current-page.sync='currentPage'
            :page-sizes='[ 20, 50, 100, patientTotal]'
            :page-size='20'
            layout='total, sizes, prev, pager, next, jumper'
            :total='patientTotal'>
          </el-pagination>
        </el-col>
      </el-row>
</el-card>
</template>



<script>
import MainUI from '@/views/components/mainUI'
import History from '@/views/components/history'
import {
  selectvisiprogresslist,
}  from "@/api/workbench/Schedules";
import moment from "moment"; 
  export default {
    extends: MainUI,
  components: {
    History
  },
    data() {
      return {
        formInline: {
          values: '',
          status: ''
        },
        tableData: [{
          jzzt:"",
          xm:"",
          ks:"",
          djsj:"",
          jzlx:"",
          ys:"",
          cfs:"",
          xb:"",
          sr:"",
          phone:"",
        }],
        visitprogresspara: {
        limit: 20,
        offset: 0,
        companyId: currentUser.company.id,
        status: "",
        values:"",
        kssj:"",
        jssj:"",
      },
      currentPage: 1,
      patientTotal: 0,
      patientList: [],

      //Chọn thời gian
      TimeInterval:[this.addCreateDate(), moment(new Date()).format("YYYY-MM-DD HH:mm:ss")],

      pickerOptions: {
          shortcuts: [{
            text: 'Tuần gần nhất',
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 7);
              picker.$emit('pick', [start, end]);
            }
          }, {
            text: 'Tháng gần nhất',
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 30);
              picker.$emit('pick', [start, end]);
            }
          }, {
            text: '3 tháng gần nhất',
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 90);
              picker.$emit('pick', [start, end]);
            }
          }]
        },

      }
    },
    methods: {
      reset(){
        this. formInline = {
          values: '',
          status: ''
        };
        this.TimeInterval='';
        this. visitprogresspara = {
        limit: 20,
        offset: 0,
        companyId: currentUser.company.id,
        status: "",
        values:"",
        kssj:"",
        jssj:"",
        };
        this.Getvisiprogresslist();
      },

      onSizeChange(val) {
      this.currentPage = 1
      this.visitprogresspara.limit = val;
      this.visitprogresspara.offset = (this.currentPage - 1) * val
      this.Getvisiprogresslist();
    },
    onCurrentChange(val) {
      this.visitprogresspara.offset = (val - 1) * this.visitprogresspara.limit
      this.currentPage = val
      this.Getvisiprogresslist();
    },
    indexMethod(index){
       return (this.currentPage-1)*this.visitprogresspara.limit+index +1;
    },

 Getvisiprogresslist(){
  this.tableData=[];
  if(this.TimeInterval){
    this.visitprogresspara.kssj=this.TimeInterval[0];
  this.visitprogresspara.jssj=this.TimeInterval[1]
  }
  else{
    this.visitprogresspara.kssj="";
    this.visitprogresspara.jssj=""
  }
this.visitprogresspara.status=this.formInline.status==="qb"?"":this.formInline.status;
this.visitprogresspara.values=this.formInline.values;
selectvisiprogresslist(this.visitprogresspara).then((responseData)=>{
  if (responseData.code === 100){
    this.patientTotal=responseData.data.total;
    if( responseData.data.rows!=null){
    responseData.data.rows.forEach((item)=>{
      switch(item.jzzt.trim()){
        case "registrationStatus_0":
          item.jzzt="Chờ khám"
          break;
        case "registrationStatus_1":
          item.jzzt="Đã khám"
          break;
        case "registrationStatus_2":
          item.jzzt="Đã hủy"
          break;
        case "registrationStatus_3":
          item.jzzt="Chờ check-in"
          break;
        case "registrationStatus_4":
          item.jzzt="Đã hết hiệu lực"
          break;
        case "registrationStatus_5":
          item.jzzt="Chờ phân loại khám"
          break;
      };
      switch(item.jzlx){
        case "treatType_0":
          item.jzlx="初诊"
          break;
          case "treatType_1":
          item.jzlx="Tái khám"
          break;
      };
          let birthday = item.sr;
          if (birthday) {
                const duration = moment.duration(moment().diff(birthday));
                item.sr = duration.years();
              } else {
                item.sr = "--";
              }
      let djsj = "--";
              if (item.djsj) {
                djsj = moment(item.djsj).format("YYYY-MM-DD HH:mm:ss");
              }
      this.tableData.push({
          jzzt:item.jzzt,
          xm:item.xm,
          ks:item.ks,
          djsj:djsj,
          jzlx:item.jzlx,
          ys:item.ys,
          cfs:item.cfs,
          xb:item.xb,
          sr:item.sr,
          phone:item.phone,
      })
    })
    }
  }
})

},

      onSubmit() {
        this.Getvisiprogresslist();
      },

      addCreateDate() {
        const d = new Date();
        d.setTime(d.getTime() - 30 * 24 * 60 * 60 * 1000);
        return moment(d).format("YYYY-MM-DD HH:mm:ss");
      },

    },
  //  created(){
  //   this.Getvisiprogresslist();
  // },
    mounted(){
      this.Getvisiprogresslist();
    }
  }
</script>

<style>
  .el-popover{
    overflow-y: visible;
  }
</style>






