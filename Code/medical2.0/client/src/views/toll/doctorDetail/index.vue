<template>
  <el-row v-loading="loading">
    <el-card class="page-container">
      <el-row>
        <el-col style="text-align:right;margin-bottom:10px">
          <el-button type="primary" icon="el-icon-upload2" @click='exportExcel'>Xuất</el-button>
        </el-col>
      </el-row>
      <!--  Thanh tìm kiếm  Bắt đầu -->
      <div class='query-form-container'>
          <el-row class='search-row'>
            <el-form :model='queryModel' @submit.native.prevent label-position="left" label-width='70px' ref='queryForm' :inline-message='true'>
                      <el-col :span="8">
              <el-form-item label='Ngày thu phí' prop='time'>
                <el-date-picker
                v-model="queryModel.dateRange"
                type="datetimerange"
                range-separator="đến"
                start-placeholder="Ngày bắt đầu"
                end-placeholder="Ngày kết thúc"
                :default-time="['00:00:00', '23:59:59']"
                format="dd/MM/yyyy HH:mm:ss"
              >
              </el-date-picker>
              </el-form-item>
            </el-col>
              <el-col :span="4">
                <el-form-item label='Tên bác sĩ' prop='name'>
                  <el-input
                    style="width:320px;"
                    v-model="queryModel.name"
                    placeholder="Vui lòng nhập họ tên bác sĩ"
                  ></el-input>
                </el-form-item>
              </el-col>

            </el-form>
          </el-row>
          <el-row id="conditionOperation">
            <el-col :span="24" style="text-align:right;padding-right:5px">
              <!-- <div class="bg_btn">
                <el-button type="primary" @click="onSearch"
                  >Truy vấn</el-button
                >
                <el-button @click="resetCondition()">Đặt lại</el-button>
              </div> -->
                <el-button type="primary" icon="el-icon-search" @click='onSearch' :plain='true'>Tìm kiếm</el-button>
                <el-button type="info" icon="el-icon-refresh-left" @click='resetCondition' :plain='true'>Đặt lại</el-button>
            </el-col>
          </el-row>
        </div>
      <!--  Thanh tìm kiếm  Kết thúc -->

      <!-- Thanh bảng  Bắt đầu -->
      <el-row>
        <el-col :span="24">
          <div @mouseleave="moveTableOutside">
            <el-table
              class="dispensing_table"
              :data="dispensingList"
              height="calc(100vh - 284px)"
              border
              ref="mutipleTable1"
              @sort-change="onSortChange"
              @header-dragend="onChangeWidth"
              :cell-class-name="cellClassName"
              :header-cell-class-name="headerCellClassName"
              highlight-current-row
              show-summary
              :summary-method='getTotal'
            >
            <!-- show-summary -->
            <!-- :summary-method='getTotal' -->
              <el-table-column
              label="Số thứ tự"
              type="index"
              :index="indexMethod"
              align="center">
            </el-table-column>
              <el-table-column prop="name" label="Họ tên bác sĩ">
              </el-table-column>
              <el-table-column prop="count" label="Số lần tiếp nhận khám" >
                <template slot-scope="scope">
                  <span style="display:inline-block;width:100%;text-align:right">
                    {{scope.row.count?bigNum(scope.row.count):0}}lần
                  </span>
                </template> </el-table-column>
              <el-table-column prop="grossAmount" label="Số tiền phải thu">
                <template slot-scope="scope">
                  <span style="display:inline-block;width:100%;text-align:right">
                    {{scope.row.grossAmount?bigNum(scope.row.grossAmount):0}}VNĐ
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="actual" label="Số tiền thực thu">
                <template slot-scope="scope">
                  <span style="display:inline-block;width:100%;text-align:right">
                    {{scope.row.actual?bigNum(scope.row.actual):0}}VNĐ
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="favorable" label="Số tiền ưu đãi">
                <template slot-scope="scope">
                  <span style="display:inline-block;width:100%;text-align:right">
                    {{scope.row.favorable?bigNum(scope.row.favorable):0}}VNĐ
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="registrationCost" label="Phí đăng ký khám" >
                <template slot-scope="scope">
                  <span style="display:inline-block;width:100%;text-align:right">
                    {{scope.row.registrationCost?bigNum(scope.row.registrationCost):0}}VNĐ
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="westCost" label="Phí thuốc Tây y" >
                <template slot-scope="scope">
                  <span style="display:inline-block;width:100%;text-align:right">
                    {{scope.row.westCost?bigNum(scope.row.westCost):0}}VNĐ
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="chineseCost" label="Phí thuốc thảo dược" >
                <template slot-scope="scope">
                  <span  style="display:inline-block;width:100%;text-align:right">
                    {{scope.row.chineseCost?bigNum(scope.row.chineseCost):0}}VNĐ
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="chinesePatentCost" label="Phí thuốc thành phẩm Đông y" >
                <template slot-scope="scope">
                  <span style="display:inline-block;width:100%;text-align:right">
                    {{scope.row.chinesePatentCost?bigNum(scope.row.chinesePatentCost):0}}VNĐ
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="stuffCost" label="Phí vật tư" >
                <template slot-scope="scope">
                  <span style="display:inline-block;width:100%;text-align:right">
                    {{scope.row.stuffCost?bigNum(scope.row.stuffCost):0}}VNĐ
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="examinesCost" label="Phí xét nghiệm" >
                <template slot-scope="scope">
                  <span style="display:inline-block;width:100%;text-align:right">
                    {{scope.row.examinesCost?bigNum(scope.row.examinesCost):0}}VNĐ
                  </span>
                </template> </el-table-column>
              <el-table-column prop="checkoutCost" label="Phí kiểm tra" >
                <template slot-scope="scope">
                  <span style="display:inline-block;width:100%;text-align:right">
                    {{scope.row.checkoutCost?bigNum(scope.row.checkoutCost):0}}VNĐ
                  </span>
                </template> </el-table-column>
              <el-table-column prop="therapyCost" label="Phí vật lý trị liệu" >
                <template slot-scope="scope">
                  <span style="display:inline-block;width:100%;text-align:right">
                    {{scope.row.therapyCost?bigNum(scope.row.therapyCost):0}}VNĐ
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="cureCost" label="Phí điều trị" >
                <template slot-scope="scope">
                  <span style="display:inline-block;width:100%;text-align:right">
                    {{scope.row.cureCost?bigNum(scope.row.cureCost):0}}VNĐ
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="otherCost" label="Khác" >
                <template slot-scope="scope">
                  <span style="display:inline-block;width:100%;text-align:right">
                    {{scope.row.otherCost?bigNum(scope.row.otherCost):0}}VNĐ
                  </span>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-col>
      </el-row>
      <!-- Thanh bảng  Kết thúc -->
      <!-- Thanh phân trang     Bắt đầu -->
      <el-row>
        <el-col :span="24">
          <el-pagination
            background
            @size-change="onSizeChange"
            @current-change="onCurrentChange"
            :current-page.sync="currentPage"
            :page-sizes="[20, 50, 100, dispensingTotal]"
            :page-size="pageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="dispensingTotal"
          >
          </el-pagination>
        </el-col>
      </el-row>
      <!-- Thanh phân trang     Kết thúc -->
    </el-card>
  </el-row>
</template>

<script>
import reportViewMixin from '@/mixins/reportViewMixin'
import { BigNumber } from "bignumber.js";
import { getWorkload, getWorkLoadStat, exportExcel } from "@/api/toll/doctorDetail";
export default {
  mixins: [reportViewMixin],
  data() {
    return {
      listApi: getWorkload,
      statApi: getWorkLoadStat,
      exportApi: exportExcel,
      entityName: 'DoctorDetail',
      permissionPrefix: 'doctorDetail',
      exportColumnName: 'doctorDetail',
      queryModel: {
        name: '',
        dateRange: [this.addCreateDate(), new Date()]
      },
    };
  },
  updated(){
     this.$nextTick(()=>{
       this.$refs.mutipleTable1.doLayout();
     });
  },
  methods: {
    exportExcel(){
      this.search.columnName = this.exportColumnName
      if (!this.exportApi) return
      this.exportApi(this.search).then((res) => {
        const filename = decodeURI(res.headers.split(';')[1].split('=')[1]) || '.xls'
        const blob = new Blob([res.data], { type: 'application/octet-stream' })
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.style.display = 'none'
        link.href = url
        link.setAttribute('download', filename)
        document.body.appendChild(link)
        link.click()
      }).catch((error) => {
        this.outputError(error)
      })
    },
    getTotal(param){
      let { columns, data } = param;
      let arr = []
      columns.forEach((column, index) => {
          if (index === 0) {
            arr[index] = 'Tổng cộng';
          }else{
            arr[index] = ''
          }
      })
      arr[2]=this.amountTotal.count+"lần"
      arr[3]=new BigNumber(Number(this.amountTotal.grossAmount)).toFormat(2)+'VNĐ'
      arr[4]=new BigNumber(Number(this.amountTotal.actual)).toFormat(2)+'VNĐ'
      arr[5]=new BigNumber(Number(this.amountTotal.favorable)).toFormat(2)+'VNĐ'
      arr[6]= new BigNumber(Number(this.amountTotal.registrationCost)).toFormat(2)+'VNĐ'
      arr[7]= new BigNumber(Number(this.amountTotal.westCost)).toFormat(2)+'VNĐ'
      arr[8]= new BigNumber(Number(this.amountTotal.chineseCost)).toFormat(2)+'VNĐ'
      arr[9]= new BigNumber(Number(this.amountTotal.chinesePatentCost)).toFormat(2)+'VNĐ'
      arr[10]= new BigNumber(Number(this.amountTotal.stuffCost)).toFormat(2)+'VNĐ'
      arr[11]= new BigNumber(Number(this.amountTotal.examinesCost)).toFormat(2)+'VNĐ'
      arr[12]= new BigNumber(Number(this.amountTotal.checkoutCost)).toFormat(2)+'VNĐ'
      arr[13]= new BigNumber(Number(this.amountTotal.therapyCost)).toFormat(2)+'VNĐ'
      arr[14]= new BigNumber(Number(this.amountTotal.cureCost)).toFormat(2)+'VNĐ'
      arr[15]= new BigNumber(Number(this.amountTotal.otherCost)).toFormat(2)+'VNĐ'
      return arr
    },
    appendSearchParams() {
      this.search.params = [
        {
          columnName: "company_id",
          queryType: "=",
          value: currentUser.company.id,
        },
      ]
      if(this.queryModel.dateRange && this.queryModel.dateRange.length){
        this.queryModel.dateRange[0]=this.$moment(this.queryModel.dateRange[0]).format("YYYY-MM-DD HH:mm:ss")
        this.queryModel.dateRange[1]=this.$moment(this.queryModel.dateRange[1]).format("YYYY-MM-DD HH:mm:ss")
        this.search.params.push(
          { logic: "AND", queryType: "(" },
          { columnName: "create_date", logic: "", queryType: 'between', value: this.queryModel.dateRange },
          { logic: "", queryType: ")" }
        )
      }
      if(this.queryModel.name!==undefined && this.queryModel.name!==''){
        this.search.params.push({
          columnName: "su.name",
          queryType: "=",
          value: this.queryModel.name,
        })
      }
    },
    resetCondition(){
      this.queryModel = {
        name: '',
        dateRange: [this.addCreateDate(), new Date()]
      }
      this.currentPage = 1;
      this.onSearch()
    },
  },
  watch: {},
};
</script>
<style lang="scss" scoped>
.page-container{
  padding: 0;
}
.query-form-container{
  padding: 0px 0px 10px 0px;
}
.typeClass{
  ::v-deep .el-input{
    width: 100% !important;
    input{
      width: 100% !important;
      padding-right: 0;
    }
  }
}
.el-col{
  ::v-deep .el-range-separator{
    width: 10%;
  }
}
.cell{
  span{
    display: inline-block;
    width: 100%;
    text-align: right;
  }
}
::v-deep .el-table__footer-wrapper{
  td:not(:nth-of-type(1)){
    .cell{
      display: inline-block;
      width: 100%;
      text-align: right;
    }
  }

}
.el-table::before{
  height: 0;
}
::v-deep .el-table colgroup col[name='gutter']{
  width:5px !important
}
::v-deep .el-table__body{
  width:100% !important
}
</style>
