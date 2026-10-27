<template>
<el-card class="page-container" style="padding: 0px;">
   <!-- <el-row>
        <el-col style="text-align:right;margin-bottom:10px">
          <el-button type="primary" icon="el-icon-upload2" @click='exportExcel'>Xuất</el-button>
        </el-col>
      </el-row> -->
<el-row>
  <!-- <el-col :span="24">
    <div style="margin:0 0 10px 0;">
    <el-radio-group v-model="YpjxcRc.orderby" size="mini" @change="orderbyChange">
      <el-radio-button label="batch_no">Số lô</el-radio-button>
      <el-radio-button label="code">Hàng hóa</el-radio-button>
      
    </el-radio-group>
  </div>
  </el-col> -->
   <el-col :span="24">
     <el-form :inline="true" :model="formInline" class="demo-form-inline">
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
    <el-form-item label="Loại"  style="margin-right: 3px;">
    <el-select v-model="formInline.lx" placeholder="Loại">
      <el-option label="Tất cả" value="qb"></el-option>
      <el-option label="Vật tư y tế" value="stuffType_0"></el-option>
      <el-option label="Vật tư phi y tế" value="stuffType_1"></el-option>
    </el-select>
  </el-form-item>
  <!-- <el-form-item label="Trạng thái"  style="margin-right: 3px;">
    <el-select v-model="formInline.zt" placeholder="Trạng thái">
      <el-option label="Tất cả" value="qb"></el-option>
      <el-option label="Ngừng sử dụng" value="0"></el-option>
      <el-option label="Kích hoạt" value="1"></el-option>
    </el-select>
  </el-form-item> -->

  <!-- <el-form-item label="Số lô" style="margin-right: 3px;">
    <el-input v-model="formInline.ph" placeholder="Vui lòng nhập số lô"></el-input>
  </el-form-item> -->
    <el-form-item label="Tên vật tư" style="margin-right: 3px;">
    <el-input v-model="formInline.clmc" placeholder="Vui lòng nhập tên vật tư"></el-input>
  </el-form-item>

      <!-- <el-collapse-transition style="margin-right: 5px;">
        <div v-show="show3">
          <el-form-item label="Số lô" style="margin-right: 5px;">
    <el-input v-model="formInline.ph" placeholder="Vui lòng nhập số lô"></el-input>
  </el-form-item>
    <el-form-item label="Tên hàng hóa" style="float:left;margin-right: 5px;">
    <el-input v-model="formInline.spm" placeholder="Vui lòng nhập tên hàng hóa"></el-input>
  </el-form-item>
        </div>
      </el-collapse-transition> -->
  
  <!-- <el-form-item label="Số lô">
    <el-input v-model="formInline.ph" placeholder="Vui lòng nhập số lô"></el-input>
  </el-form-item>
    <el-form-item label="Tên hàng hóa">
    <el-input v-model="formInline.spm" placeholder="Vui lòng nhập tên hàng hóa"></el-input>
  </el-form-item> -->
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
    ref="tableData"
    border
    height="calc(100vh - 280px)"
    :summary-method="getSummaries"
    show-summary
   >
    <el-table-column
             label="Số thứ tự"
              type="index"
              :index="indexMethod"
              align="center">
            </el-table-column>
    <el-table-column
    align="center"
      prop="clbm"
      label="Mã vật tư">
    </el-table-column>
    <el-table-column
      align="center"
      prop="clmc"
      label="Tên vật tư">
    </el-table-column>
    <el-table-column
      align="center"
      prop="cllx"
      label="Phân loại">
    </el-table-column>
    <!-- <el-table-column
    width="150px" 
      prop="txm"
      label="Mã vạch">
    </el-table-column> -->
    <!-- <el-table-column
    width="150px" 
      prop="gyzz"
      label="Số đăng ký thuốc quốc gia">
    </el-table-column> -->
    <el-table-column
      prop="gg"
      align="center"
      label="Quy cách">
    </el-table-column>
    <!-- <el-table-column
      prop="sccj"
      label="Nhà sản xuất">
    </el-table-column>
    <el-table-column
    align="center"
      prop="gys"
      label="Nhà cung cấp">
    </el-table-column>
    <el-table-column
    width="150px" 
    align="center"
      prop="lsj"
      label="Giá bán lẻ">
    </el-table-column>
    <el-table-column
    width="150px" 
    align="center"
      prop="cbj"
      label="Giá vốn">
    </el-table-column>
    <el-table-column
    width="120px"
      prop="ph"
      label="Số lô">
    </el-table-column>
    <el-table-column
    width="200px" 
    align="center"
      prop="yxq"
      label="Hạn sử dụng">
    </el-table-column>
    <el-table-column
    width="150px" 
    align="center"
      prop="qcsl"
      label="Số lượng đầu kỳ">
    </el-table-column>
    <el-table-column
    width="150px" 
    align="center"
      prop="qccb"
      label="Chi phí đầu kỳ">
    </el-table-column>
    <el-table-column
    width="150px" 
      align="center"
      prop="bqrk"
      label="Số lượng nhập kho trong kỳ">
    </el-table-column>
    <el-table-column
      width="150px" 
      align="center"
      prop="bqrkcb"
      label="Chi phí nhập kho trong kỳ">
    </el-table-column>
    <el-table-column
    width="150px" 
      align="center"
      prop="bqcksl"
      label="Số lượng xuất kho trong kỳ">
    </el-table-column>
    <el-table-column
    width="150px" 
      align="center"
      prop="bqckcb"
      label="Chi phí xuất kho trong kỳ">
    </el-table-column>
    <el-table-column
    width="150px" 
      align="center"
      prop="bqpdsl"
      label="Số lượng kiểm kê trong kỳ">
    </el-table-column>
    <el-table-column
    width="150px" 
      align="center"
      prop="bqpdcb"
      label="Chi phí kiểm kê trong kỳ">
    </el-table-column> -->
    <el-table-column
    align="center"
      prop="sl"
      label="Số lượng">
    </el-table-column>
    <el-table-column
    align="center"
      prop="zj"
      label="Tổng giá">
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
import { getStuffsalessummarylists,getStuffsalessummarysumss} from "@/api/toll/tollInfo";
import { BigNumber } from "bignumber.js";

  export default {

    data() {
      return {
        show3: false,
              //Chọn thời gian
      TimeInterval:this.getInitializeDate(),
      formInline: {
          clmc: '',
          lx:'',
        },

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
        
        tableData: [],

        StuffsalessummaryRc: {
        limit: 20,
        offset: 0,
        companyId: currentUser.company.id,
        kssj:"",
        jssj:"",
        lx:"",
        clmc:"",
      },
      currentPage: 1,
      patientTotal: 0,
      patientList: [],
      allTotal:{},

      };
    },
    updated() {
    this.$nextTick(() => {
      this.$refs['tableData'].doLayout();
    })
  },
    methods: {
    
  
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
      getSummaries(param) {
        const { columns, data } = param;
        const sums = [];
        columns.forEach((column, index) => {
          if (index === 0) {
            sums[index] = 'Tổng giá';
            return;
          }
          // else if (index === 8 || index ===9 ||index >11){
          //   const values = data.map(item => Number(item[column.property]));
          // if (!values.every(value => isNaN(value))) {
          //   sums[index] = values.reduce((prev, curr) => {
          //     const value = Number(curr);
          //     if (!isNaN(value)) {
          //       return prev + curr;
          //     } else {
          //       return prev;
          //     }
          //   }, 0);
          //   sums[index] += '';
          // } 
          
          // }
          else {
            sums[index] = '';
          }
          
        });
        // sums[4] = new BigNumber(Number(this.allTotal.sl)).toFormat(2)
        sums[6] = new BigNumber(Number(this.allTotal.zj)).toFormat(2)+'VNĐ'
        
        return sums;
      },
       onSizeChange(val) {
      this.currentPage = 1
      this.StuffsalessummaryRc.limit = val;
      this.StuffsalessummaryRc.offset = (this.currentPage - 1) * val
      this.Getypjxclist();
    },
    onCurrentChange(val) {
      this.StuffsalessummaryRc.offset = (val - 1) * this.StuffsalessummaryRc.limit
      this.currentPage = val
      this.Getypjxclist();
    },
    indexMethod(index){
       return (this.currentPage-1)*this.StuffsalessummaryRc.limit+index +1;
    },
     resetCondition(){
      this.StuffsalessummaryRc= {
        limit: 20,
        offset: 0,
        companyId: currentUser.company.id,
        kssj:"",
        jssj:"",
        lx:"",
        clmc:"",
      },
      this.formInline= {
          clmc: '',
          lx:'',
        },
      this.TimeInterval=this.getInitializeDate(),
      this.Getypjxclist();
     },
orderbyChange(){
this.Getypjxclist();
},
    Getypjxclist(){
      this.tableData=[];
  if(this.TimeInterval){
    this.StuffsalessummaryRc.kssj=this.TimeInterval[0];
  this.StuffsalessummaryRc.jssj=this.TimeInterval[1]
  }
//   this.StuffsalessummaryRc.zt=this.formInline.zt=="qb"?"":this.formInline.zt;
 this.StuffsalessummaryRc.lx=this.formInline.lx==="qb"?"":this.formInline.lx;
 this.StuffsalessummaryRc.clmc=this.formInline.clmc;
// this.StuffsalessummaryRc.ph=this.formInline.ph;
// this.StuffsalessummaryRc.orderby=this.StuffsalessummaryRc.orderby;
getStuffsalessummarylists(this.StuffsalessummaryRc).then((responseData)=>{
if (responseData.code === 100){
  this.patientTotal=responseData.data.total;
  if(responseData.data.total>0){
responseData.data.rows.forEach((item)=>{
  if(item.cllx==="stuffType_0"){item.cllx="Vật tư y tế"}else if(item.cllx==="stuffType_1"){item.cllx="Vật tư phi y tế"}else{item.cllx=""}
    this.tableData.push({
      clbm:item.clbm,
      clmc:item.clmc,
      gg:item.gg,
      sl:Math.floor(item.sl/item.bzsl) >= 0 ? Math.floor(item.sl/item.bzsl)+(item.bzdw)+((item.sl%item.bzsl > 0) ? (item.sl%item.bzsl) + item.zxdw:""):item.sl+item.zxdw,
      // sl:item.sl,
      zj:item.zj,
      cllx:item.cllx,
    })
  })
  }
  
getStuffsalessummarysumss(this.StuffsalessummaryRc).then((ref)=>{
  if(ref.data.total>0){
    this.allTotal=ref.data.rows[0];
  }
   
})
}

})
    },
    onSubmit() {
        this.Getypjxclist();
      },
    },
    
    mounted(){
      this.Getypjxclist();
    }
  };
</script>
<style lang="scss">
.el-picker-panel__footer .el-picker-panel__link-btn.el-button--text {
  display: none;
}

.el-card__body{
  padding: 0px;
}
</style>