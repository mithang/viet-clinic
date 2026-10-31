<template>
<el-card class="page-container" style="padding: 0px;">
<el-row>
  <el-col :span="24">
    <div style="margin:0 0 10px 0;">
    <el-radio-group v-model="YpjxcRc.orderby" size="mini" @change="orderbyChange">
      <el-radio-button label="batch_no">Số lô</el-radio-button>
      <el-radio-button label="code">Hàng hóa</el-radio-button>
      
    </el-radio-group>
  </div>
  </el-col>
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
    <el-form-item label="Phòng khám"  style="margin-right: 3px;">
    <el-select v-model="formInline.jgid" placeholder="Phòng khám">
      <el-option label="Tất cả" value="qb"></el-option>
       <el-option v-for="item in jglist" :value="item.jgid" :key="item.jgid"  :label="item.jgmc">
        </el-option>
    </el-select>
  </el-form-item>
    <el-form-item label="Loại"  style="margin-right: 3px;">
    <el-select v-model="formInline.lx" placeholder="Loại">
      <el-option label="Tất cả" value="qb"></el-option>
      <el-option label="Thuốc tây" value="medicalType_0"></el-option>
      <el-option label="Dược liệu đông y" value="medicalType_1"></el-option>
      <el-option label="Thuốc thành phẩm đông y" value="medicalType_2"></el-option>
    </el-select>
  </el-form-item>
  <el-form-item label="Trạng thái"  style="margin-right: 3px;">
    <el-select v-model="formInline.zt" placeholder="Trạng thái">
      <el-option label="Tất cả" value="qb"></el-option>
      <el-option label="Ngừng sử dụng" value="0"></el-option>
      <el-option label="Kích hoạt" value="1"></el-option>
    </el-select>
  </el-form-item>

  <el-form-item label="Số lô" style="margin-right: 3px;">
    <el-input v-model="formInline.ph" placeholder="Vui lòng nhập số lô"></el-input>
  </el-form-item>
    <el-form-item label="Tên hàng hóa" style="margin-right: 3px;">
    <el-input v-model="formInline.spm" placeholder="Vui lòng nhập tên hàng hóa"></el-input>
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
    height="calc(100vh - 330px)"
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
      prop="jgid"
      sortable v-if="false"
      label="Mã phòng khám">
    </el-table-column>
    <el-table-column
    align="center"
      prop="jgmc"
      label="Tên phòng khám">
    </el-table-column>
    <el-table-column
    align="center"
      prop="ypbm"
      label="Mã thuốc">
    </el-table-column>
    <el-table-column
    width="150px" 
      align="center"
      prop="ypmc"
      label="Tên thuốc">
    </el-table-column>
    <el-table-column
    width="200px" 
    align="center"
      prop="fl"
      :formatter="stateFormat"
      label="Phân loại">
    </el-table-column>
    <el-table-column
    width="120px"
      prop="ph"
      label="Số lô">
    </el-table-column>
    <el-table-column
    width="150px" 
      prop="txm"
      label="Mã vạch">
    </el-table-column>
    <el-table-column
    width="150px" 
      prop="gyzz"
      label="Số đăng ký thuốc quốc gia">
    </el-table-column>
    <el-table-column
      prop="gg"
      width="150px" 
      align="center"
      label="Quy cách">
    </el-table-column>
    <el-table-column
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
    </el-table-column>
    <el-table-column
    width="150px" 
    align="center"
      prop="qmsl"
      label="Số lượng cuối kỳ">
    </el-table-column>
    <el-table-column
    width="150px" 
    align="center"
      prop="qmcb"
      label="Chi phí cuối kỳ">
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
import { getpharmaceuticalInventoryManagementsums,getpharmaceuticalInventoryManagement,getjglist } from "@/api/toll/tollInfo";
import { BigNumber } from "bignumber.js";

  export default {

    data() {
      return {
        show3: false,
              //Chọn thời gian
      TimeInterval:this.getInitializeDate(),
      formInline: {
          spm: '',
          zt: '',
          lx:'',
          ph:'',
          jgid:null,
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
        // tableData: [{
        //   ypbm:'',
        //   ypmc:'',
        //   txm:'',
        //   gyzz:'',
        //   gg:'',
        //   sccj:'',
        //   gys:'',
        //   lsj:'',
        //   cbj:'',
        //   ph:'',
        //   yxq:'',
        //   qcsl:'',
        //   qccb:'',
        //   bqrk:'',
        //   bqrkcb:'',
        //   bqcksl:'',
        //   bqckcb:'',
        //   qmsl:'',
        //   qmcb:'',
        // }],
        tableData: [],

        YpjxcRc: {
        limit: 20,
        offset: 0,
        companyId: currentUser.company.id,
        kssj:"",
        jssj:"",
        lx:"",
        zt:"",
        spm:"",
        ph:"",
        orderby:"batch_no",
        jgid:null,
      },
      currentPage: 1,
      patientTotal: 0,
      patientList: [],
      allTotal:{},
      //Phòng khám
      jglist:[],

      };
    },
    updated() {
    this.$nextTick(() => {
      this.$refs['tableData'].doLayout();
    })
  },
    methods: {
      stateFormat(row, column) {
        if (row.fl ==="medicalType_0") {
          return 'Thuốc tây'
        }
        else if(row.fl ==="medicalType_1"){
          return 'Dược liệu đông y'
        }
        else if(row.fl ==="medicalType_2"){
          return 'Thuốc thành phẩm đông y'
        } else  {
          return ''
        } 
      },
       //Đặt ngày mặc định
    getInitializeDate () {
     let date = new Date()//Lấy thời gian mới
     //Lấy năm hiện tại,và chuyển thành chuỗi
     let year = date.getFullYear().toString()
     //Lấy tháng hiện tại, vì tháng bắt đầu từ0Bắt đầu, tại đây cần cộng1, 
    //Sử dụng biểu thức ba ngôi, Kiểm tra có nhỏ hơn không10, Nếu vậy, thì nối thêm vào trước chuỗi'0'
     let month = date.getMonth() + 1 < 10 ? '0' + (date.getMonth() + 1).toString() : (date.getMonth() + 1).toString()
     //Lấy ngày, Kiểm tra có nhỏ hơn không10, Nếu vậy, thì nối thêm vào trước chuỗi'0'
     let day = date.getDate() < 10 ? '0' + date.getDate().toString() : date.getDate().toString()
     //Nối chuỗi, Nối thời gian bắt đầu và thời gian kết thúc
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
        // sums[10] = new BigNumber(Number(this.allTotal.lsj)).toFormat(2)+'VNĐ'
        // sums[11] = new BigNumber(Number(this.allTotal.cbj)).toFormat(2)+'VNĐ'
        // sums[13] = new BigNumber(Number(this.allTotal.qcsl)).toFormat(2)
        sums[15] = new BigNumber(Number(this.allTotal.qccb)).toFormat(2)+'VNĐ'
        // sums[15] = new BigNumber(Number(this.allTotal.bqrk)).toFormat(2)
        sums[17] = new BigNumber(Number(this.allTotal.bqrkcb)).toFormat(2)+'VNĐ'
        // sums[17] = new BigNumber(Number(this.allTotal.bqcksl)).toFormat(2)
        sums[19] = new BigNumber(Number(this.allTotal.bqckcb)).toFormat(2)+'VNĐ'
        // sums[19] = new BigNumber(Number(this.allTotal.bqpdsl)).toFormat(2)
        sums[21] = new BigNumber(Number(this.allTotal.bqpdcb)).toFormat(2)+'VNĐ'
        // sums[21] = new BigNumber(Number(this.allTotal.qmsl)).toFormat(2)
        sums[23] = new BigNumber(Number(this.allTotal.qmcb)).toFormat(2)+'VNĐ'
        
        return sums;
      },
       onSizeChange(val) {
      this.currentPage = 1
      this.YpjxcRc.limit = val;
      this.YpjxcRc.offset = (this.currentPage - 1) * val
      this.Getypjxclist();
    },
    onCurrentChange(val) {
      this.YpjxcRc.offset = (val - 1) * this.YpjxcRc.limit
      this.currentPage = val
      this.Getypjxclist();
    },
    indexMethod(index){
       return (this.currentPage-1)*this.YpjxcRc.limit+index +1;
    },
     resetCondition(){
      this.YpjxcRc= {
        limit: 20,
        offset: 0,
        companyId: currentUser.company.id,
        kssj:"",
        jssj:"",
        lx:"",
        zt:"",
        spm:"",
        ph:"",
        orderby:"batch_no",
        jgid:null,
      },
      this.formInline= {
          spm: '',
          zt: '',
          lx:'',
          ph:'',
          jgid:null,
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
    this.YpjxcRc.kssj=this.TimeInterval[0];
  this.YpjxcRc.jssj=this.TimeInterval[1]
  }
  this.YpjxcRc.zt=this.formInline.zt==="qb"?"":this.formInline.zt;
    this.YpjxcRc.lx=this.formInline.lx==="qb"?"":this.formInline.lx;
    this.YpjxcRc.jgid=this.formInline.jgid==="qb"?null:this.formInline.jgid;
this.YpjxcRc.spm=this.formInline.spm;
this.YpjxcRc.ph=this.formInline.ph;
this.YpjxcRc.orderby=this.YpjxcRc.orderby;
getpharmaceuticalInventoryManagement(this.YpjxcRc).then((responseData)=>{
if (responseData.code === 100){
  this.patientTotal=responseData.data.total;
  if(responseData.data.total>0){
  responseData.data.rows.forEach((item)=>{
    this.tableData.push({
        jgid:item.jgid,
        jgmc:item.jgmc,
      ypbm:item.ypbm,
      ypmc:item.ypmc,
        txm:item.txm,
          gyzz:item.gyzz,
          gg:item.gg,
          sccj:item.sccj,
          gys:item.gys,
          lsj:item.lsj,
          cbj:item.cbj,
          ph:item.ph,
          yxq:item.yxq,
          // qcsl:item.qcsl,
          qcsl:Math.floor(item.qcsl/item.zj) >= 0 ? Math.floor(item.qcsl/item.zj)+(item.dw)+((item.qcsl%item.zj > 0) ? (item.qcsl%item.zj) + item.zxdw:""):item.sl+item.zxdw,
          qccb:item.qccb,
          bqrk:Math.floor(item.bqrk/item.zj) >= 0 ? Math.floor(item.bqrk/item.zj)+(item.dw)+((item.bqrk%item.zj > 0) ? (item.bqrk%item.zj) + item.zxdw:""):item.bqrk+item.zxdw,
          bqrkcb:item.bqrkcb,
          bqcksl:Math.floor(item.bqcksl/item.zj) >= 0 ? Math.floor(item.bqcksl/item.zj)+(item.dw)+((item.bqcksl%item.zj > 0) ? (item.bqcksl%item.zj) + item.zxdw:""):item.bqcksl+item.zxdw,
          bqckcb:item.bqckcb,
          qmsl:Math.floor(item.qmsl/item.zj) >= 0 ? Math.floor(item.qmsl/item.zj)+(item.dw)+((item.qmsl%item.zj > 0) ? (item.qmsl%item.zj) + item.zxdw:""):item.qmsl+item.zxdw,
          qmcb:item.qmcb,
          bqpdsl:Math.floor(item.bqpdsl/item.zj) >= 0 ? Math.floor(item.bqpdsl/item.zj)+(item.dw)+((item.bqpdsl%item.zj > 0) ? (item.bqpdsl%item.zj) + item.zxdw:""):item.bqpdsl+item.zxdw,
          bqpdcb:item.bqpdcb,
          fl:item.fl,
    })
  })
  }
getpharmaceuticalInventoryManagementsums(this.YpjxcRc).then((ref)=>{
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
      Getcliniclist(){
          this.jglist=[];
          getjglist(this.YpjxcRc).then((responseData)=>{
            //  debugger
              if (responseData.code === 100){
                  if(responseData.data.length>0){
                      responseData.data.forEach((item)=>{
                          if(item.jgid!==currentUser.company.id){
                          this.jglist.push({
                              jgid:item.jgid,
                              jgmc:item.jgmc,
                           })
                         }
                      })
                      
                  }
              }
          })
      },
    },
    
    mounted(){
      this.Getcliniclist();
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