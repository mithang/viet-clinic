<template>
<el-car>
<el-row>
    <el-col :span="24">
      <el-form :inline="true" :model="strc" class="demo-form-inline">
      <el-form-item label="Tên bệnh nhân" style="margin-right: 3px;">
    <el-input v-model="strc.hzxm" placeholder="Vui lòng nhập tên bệnh nhân"></el-input>
  </el-form-item>
   <el-form-item label="Thời gian truy vấn"  style="margin-right: 3px;">
  <div class="block" style="width:50%">
    <el-date-picker
      v-model="TimeInterval"
      type="datetimerange"
      :picker-options="pickerOptions"
      :default-time="['00:00:00','23:59:59']"
      range-separator="đến"
      start-placeholder="Ngày bắt đầu"
      end-placeholder="Ngày kết thúc"
      align="right"
      :clearable="false">
    </el-date-picker>
  </div>
    </el-form-item>
  <el-form-item label="Trạng thái thực hiện"  style="margin-right: 3px;">
  <el-select v-model="strc.zxzt" placeholder="Trạng thái thực hiện">
      <el-option label="Tất cả" value="qb"></el-option>
      <el-option label="Chưa test da" value="0"></el-option>
      <el-option label="Đã test da" value="1"></el-option>
    </el-select>
  </el-form-item>
  <el-form-item label="Bác sĩ kê đơn" style="margin-right: 3px;">
    <el-input v-model="strc.kdys" placeholder="Vui lòng nhập bác sĩ kê đơn"></el-input>
  </el-form-item>
    <el-form-item style="margin-right: 3px;">
    <el-button type="primary" @click="onSubmit">Truy vấn</el-button>
    <el-button type="info" icon="el-icon-refresh-left" @click='resetCondition' :plain='true'>Đặt lại</el-button>
    <!-- <el-button @click="show3 = !show3">{{show3==true?"Thu gọn":"Mở rộng"}}</el-button> -->
  </el-form-item>
      </el-form>
    </el-col>
    <el-col :span="24">
  <el-table
    :data="tableData"
    border
    height="calc(100vh - 250px)">
    <el-table-column
             label="Số thứ tự"
              type="index"
              :index="blmbindexMethod"
              align="center">
            </el-table-column>
    <el-table-column
      prop="pszt"
      label="Trạng thái thử phản ứng da">
    </el-table-column>
    <el-table-column
      prop="hzxm"
      label="Bệnh nhân">
    </el-table-column>
    <el-table-column
      prop="gmqk"
      label="Tình trạng dị ứng">
    </el-table-column>
    <el-table-column
      prop="kdsj"
      label="Thời gian lập phiếu"
      width="150">
    </el-table-column>
    <el-table-column
      prop="kdys"
      label="Bác sĩ kê đơn"
      width="130">
    </el-table-column>
    <el-table-column
      prop="ypmc"
      label="Thuốc">
    </el-table-column>
    <el-table-column prop="dosis" label="Quy cách" width="150">
      <template slot-scope="scope">
        <template>
          {{scope.row.dosis}}{{scope.row.dosisunit}}*{{scope.row.preparation}}{{scope.row.preparationunit}}/{{scope.row.pack}}
        </template>
      </template>
     </el-table-column>
     <el-table-column
      prop="yf"
      label="Cách dùng">
    </el-table-column>
    <el-table-column
      prop="zxsj"
      label="Thời gian thực hiện"
      width="150">
    </el-table-column>
    <el-table-column
      prop="zxr"
      label="Người thực hiện"
      width="130">
    </el-table-column>
    <el-table-column  label="皮试结果" :width="140">
       <template slot-scope="scope">
      <el-select v-model="scope.row.stresult" filterable allow-create @change="psjgchange(scope.row)">
      <el-option label="阴性" value="0"></el-option>
      <el-option label="阳性" value="1"></el-option>
    </el-select>
        </template>
</el-table-column>
<el-table-column  label="皮试时间" :width="140"  >
       <template slot-scope="scope">
      <el-select v-model="scope.row.sttime" filterable allow-create @change="pssjchange(scope.row)">
      <el-option label="5Phút" value="5"></el-option>
      <el-option label="10Phút" value="10"></el-option>
      <el-option label="15Phút" value="15"></el-option>
      <el-option label="20Phút" value="20"></el-option>
      <el-option label="25Phút" value="25"></el-option>
      <el-option label="30Phút" value="30"></el-option>
      <el-option label="35Phút" value="35"></el-option>
      <el-option label="40Phút" value="40"></el-option>
    </el-select>
        </template>
</el-table-column>
<el-table-column prop="intervalTime" label="Đếm ngược" :width="140">
                <template slot-scope="scope">
                    <span>
                       {{ scope.row.intervalTime }}
                    </span>
                </template>
            </el-table-column>
    <el-table-column
      label="Thao tác"
      width="100">
      <template slot-scope="scope">
        <el-button type="text" :disabled="scope.row.intervalTime!=='' && scope.row.intervalTime!=null" @click.native="stclick(scope.$index,scope.row)">{{scope.row.stresult!=='' && scope.row.stresult!=null ?"重新皮试":"Bắt đầu test da"}}</el-button>
      </template>
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
</el-car>
</template>

<script>
import {stmanagementlist,updatestresult,updatesttime} from '@/api/cure/stmanagement'
  export default {
    
    data() {
      return {
          tableData:[],
        // tableData: [{
        //   date: '2016-05-02',
        //   name: '王小虎',
        //   province: '上海',
        //   city: '普陀区',
        //   address: '上海市普陀区金沙江路 1518 弄',
        //   zip: 200333,
        //   stresult:'',
        //   sttime:'',
        //   intervalTime:'',
        // }, {
        //   date: '2016-05-04',
        //   name: '王小虎',
        //   province: '上海',
        //   city: '普陀区',
        //   address: '上海市普陀区金沙江路 1517 弄',
        //   zip: 200333,
        //   stresult:'',
        //   sttime:'',
        //   intervalTime:'',
        // }, {
        //   date: '2016-05-01',
        //   name: '王小虎',
        //   province: '上海',
        //   city: '普陀区',
        //   address: '上海市普陀区金沙江路 1519 弄',
        //   zip: 200333,
        //   stresult:'',
        //   sttime:'',
        //   intervalTime:'',
        // }, {
        //   date: '2016-05-03',
        //   name: '王小虎',
        //   province: '上海',
        //   city: '普陀区',
        //   address: '上海市普陀区金沙江路 1516 弄',
        //   zip: 200333,
        //   stresult:'',
        //   sttime:'',
        //   intervalTime:'',
        // }],
        currentPage: 1,
        patientTotal: 0,
        setIntervaltime:[],
        strc:{
            limit: 20,
            offset: 0,
            companyId: currentUser.company.id,
            hzxm:'',
            kdys:'',
            zxzt:'',
            kssj:'',
            jssj:'',
            detailid:'',
            zxr:'',
            stresult:'',
            sttime:'',
        },
        stdisabled:false,
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
      //Chọn thời gian
      TimeInterval:this.getInitializeDate(),
      }
    },
    methods: {
        onSizeChange(val) {
      this.currentPage = 1
      this.strc.limit = val;
      this.strc.offset = (this.currentPage - 1) * val
      this.Getblmbtable();
    },
    onCurrentChange(val) {
      this.strc.offset = (val - 1) * this.strc.limit
      this.currentPage = val
      this.Getblmbtable();
    },
    blmbindexMethod(index){
       return (this.currentPage-1)*this.strc.limit+index +1;
    },
       //Đặt ngày mặc định
    getInitializeDate () {
     let date = new Date()//Lấy thời gian mới
     //Lấy năm hiện tại,và chuyển thành chuỗi
     let year = date.getFullYear().toString()
     //Lấy tháng hiện tại，vì tháng bắt đầu từ0Bắt đầu，tại đây cần cộng1，
    //Sử dụng biểu thức ba ngôi，Kiểm tra có nhỏ hơn không10，Nếu vậy，thì nối thêm vào trước chuỗi'0'
     let month = date.getMonth() + 1 < 10 ? '0' + (date.getMonth() + 1).toString() : (date.getMonth() + 1).toString()
     //Lấy ngày，Kiểm tra có nhỏ hơn không10，Nếu vậy，thì nối thêm vào trước chuỗi'0'
     let day = date.getDate() < 10 ? '0' + date.getDate().toString() : date.getDate().toString()
     //Nối chuỗi，Nối thời gian bắt đầu và thời gian kết thúc
     let start = year + '-' + month + '-01'+' 00:00:00'    //Ngày đầu tiên của tháng hiện tại
     //let end=new Date(year, month, 0).getDate();
     let end = year + '-' + month + '-' + day +' 23:59:59'  //Trong ngày
     return [start,end] //Gán giá trị cho thành phầnDatePicker Dữ liệu đã liên kết
   },
      /** Đếm ngược*/
    countDown(row,index) {
     if( !this.setIntervaltime.length>0){
        this.setIntervaltime = new Array(this.tableData.length);
     };
      clearInterval(this.setIntervaltime[index]);
    // acceptanceOverTime 是我后台返回的截止时间
      if(row.sttime >0 ){
          let set = Date.parse(new Date()) //Thời gian tạo 获得时间戳
          const end = set + 1000 * 60 * row.sttime  //Kết thúc 在当前时间戳的基础上添加30Phút
        this.setIntervaltime[index] =setInterval(() => {
       //   row.intervalTime =  this.formatDuring(end - new Date());
     //  this.tableData[index].intervalTime = 1
       this.$set(this.tableData[index],'intervalTime',this.formatDuring(end - new Date()));
          if(row.intervalTime===""){clearInterval(this.setIntervaltime[index]);}
        }, 1000); //这边采取的是1秒调用一次
        //这里采用简单写法，直接在查询的时候调用一次，不用等定时器调用
        //row.intervalTime = this.formatDuring(new Date(row.sttime) - new Date());
      }
    },
    formatDuring (mss) {
      if(mss > 0){
        let hours = parseInt((mss % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)) // 得到小时
        let minutes = parseInt((mss % (1000 * 60 * 60)) / (1000 * 60)) // 得到分钟数
        //获取分钟
        let str1 = minutes < 10 ? ('0' + minutes) : minutes
        
        /*得到秒数*/
        // let seconds = (mss % (1000 * 60)) / 1000
        let seconds=Math.floor((mss / 1000) % 60);
        let str2 = seconds < 10 ? ('0' + seconds) : seconds
        return str1 + ' : ' + str2 ;
      }else{
        return '';
      }
    },
    //Bắt đầu test da
    stclick(index,row){
        this.countDown(row,index);
    },
    //Đặt lại
    resetCondition(){
         this.strc={
            limit: 20,
            offset: 0,
            companyId: currentUser.company.id,
            hzxm:'',
            kdys:'',
            zxzt:'',
            kssj:'',
            jssj:'',
            detailid:'',
            zxr:'',
            stresult:'',
            sttime:'',
        };
        this.TimeInterval=this.getInitializeDate();
        this.Getstmanagementlist();
    },
    //获取数据
    Getstmanagementlist(){
        this.tableData=[];
        if(this.TimeInterval){
        this.strc.kssj=this.TimeInterval[0];
        this.strc.jssj=this.TimeInterval[1];
        this.strc.zxzt=this.strc.zxzt==="qb"?"":this.strc.zxzt;
        this.strc.kdys=this.strc.kdys;
        this.strc.hzxm=this.strc.hzxm;
        stmanagementlist(this.strc).then((responseData)=>{
            if (responseData.code === 100){
                this.patientTotal=responseData.data.total;
                if(responseData.data.total>0){
                    responseData.data.rows.forEach((item)=>{
                        if(item.pszt==='0'){item.pszt="Chưa test da"}else if(item.pszt==='1'){item.pszt="Đã test da"}else{item.pszt=""}
                        this.tableData.push({
                            detailid:item.detailid,
                            pszt:item.pszt,
                            hzxm:item.hzxm,
                            gmqk:item.gmqk,
                            kdsj:item.kdsj,
                            kdys:item.kdys.substring(0,item.kdys.lastIndexOf("(")),
                            ypmc:item.ypmc,
                            dosis:item.dosis,
                            dosisunit:item.dosisunit,
                            preparation:item.preparation,
                            preparationunit:item.preparationunit,
                            pack:item.pack,
                            yf:item.yf,
                            zxsj:item.zxsj,
                            zxr:item.zxr!=='' && item.zxr!=null?item.zxr.substring(0,item.zxr.lastIndexOf("(")):item.zxr,
                            stresult:item.stresult,
                            sttime:item.sttime,
                        })
                    })
                    
                }
            }
        })
  }
    },
    onSubmit() {
        this.Getstmanagementlist();
        
      },
      psjgchange(value){
          this.strc.zxr=currentUser.name+"("+currentUser.loginname+")";
          this.strc.detailid=value.detailid;
          this.strc.stresult=value.stresult;
          updatestresult(this.strc).then((responseData)=>{
              if (responseData.code === 100){
                  this.Getstmanagementlist();
                  this.$message.success(responseData.msg);
              }
          })
      },
      pssjchange(value){
          this.strc.sttime=value.sttime;
          this.strc.detailid=value.detailid;
          updatesttime(this.strc).then((responseData)=>{
              if(responseData.code===100){
                  this.Getstmanagementlist();
                  this.$message.success(responseData.msg);
              }
          })
      },
    },
    mounted(){
      this.Getstmanagementlist();
    },
    beforeDestroy() {
      // 清除所有倒计时定时器，Ngăn rò rỉ bộ nhớ
      if (this.setIntervaltime && this.setIntervaltime.length > 0) {
        this.setIntervaltime.forEach(timer => {
          if (timer) clearInterval(timer);
        });
      }
    }
  }
</script>

<style lang="scss">
.el-picker-panel__footer .el-picker-panel__link-btn.el-button--text {
  display: none;
}
</style>