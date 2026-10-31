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
              <el-form-item label='Ngày phát thuốc' prop='time'>
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
                <el-form-item label='Tên thuốc' prop='name'>
                  <el-input
                    style="width:320px;"
                    v-model="queryModel.name"
                    placeholder="Vui lòng nhập tên thuốc hoặc mã pinyin"
                  ></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="4">
              <el-form-item label='Loại thuốc' prop='type'>
                <el-select
                  v-model="queryModel.type"
                  value-key="value"
                  filterable
                  clearable
                  placeholder="Vui lòng nhập tên thuốc hoặc mã pinyin"
                  class="typeClass"
                >
                  <el-option
                    v-for="type in type_List"
                    :key="type.value"
                    :label="type.name"
                    :value="type"
                  ></el-option>
                </el-select>
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
              <el-table-column prop="type" label="Phân loại thuốc">
              </el-table-column>
              <el-table-column prop="name" label="Tên thuốc" > </el-table-column>
              <el-table-column prop="dosis" label="Quy cách">
                <template slot-scope="scope">
                  <template>
                    {{scope.row.dosis}}{{scope.row.dosisUnit}}*{{scope.row.preparation}}{{scope.row.preparationUnit}}/{{scope.row.pack}}
                  </template>
                </template>
                 </el-table-column>
              <el-table-column prop="total" label="Số lượng" >
                <template slot-scope="scope">
                  {{Math.floor(scope.row.total/scope.row.preparation) >= 0 ? Math.floor(scope.row.total/scope.row.preparation)+(scope.row.pack)+((scope.row.total%scope.row.preparation > 0) ? (scope.row.total%scope.row.preparation) + scope.row.preparationUnit:""):scope.row.total+scope.row.preparationUnit}}
                </template>
                 </el-table-column>
              <el-table-column prop="allFee" label="Tổng giá" >
                <template slot-scope="scope">
                  <!-- <span style="display:inline-block;width:100%;text-align:right"> -->
                    {{bigNum(scope.row.allFee)}}VNĐ
                  <!-- </span> -->
                </template>
                 </el-table-column>

              <!-- <el-table-column prop="bid" label="Đơn giá nhập(VNĐ)" width="90">
                <template slot-scope="scope">
                  <span style="display:inline-block;width:100%;text-align:right">
                    {{bigNum(scope.row.bid)}}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="bidTotal" label="Tổng giá nhập(VNĐ)" width="90">
                <template slot-scope="scope">
                  <span>
                    {{bigNum(scope.row.bidTotal)}}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="price" label="Đơn giá bán(VNĐ)" width="90">
                <template slot-scope="scope">
                  <span>
                    {{bigNum(scope.row.price)}}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="priceTotal" label="Tổng giá bán(VNĐ)" width="90">
                <template slot-scope="scope">
                  <span>
                    {{bigNum(scope.row.priceTotal)}}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="profit" label="Lợi nhuận(VNĐ)" width="90">
                <template slot-scope="scope">
                  <span>
                    {{bigNum(scope.row.profit)}}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="factory" label="Nhà sản xuất">
              </el-table-column>
              <el-table-column prop="batchNo" label="Số lô sản xuất">
              </el-table-column>
              <el-table-column prop="dispensingDate" label="Ngày phát thuốc">
              </el-table-column> -->
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
import { getDrugSales, getDrugSalesStat, exportExcel } from "@/api/toll/drugDetail";
import { validatenull } from "@/utils/validate";
export default {
  mixins: [reportViewMixin],
  data() {
    return {
      listApi: getDrugSales,
      statApi: getDrugSalesStat,
      exportApi: exportExcel,
      entityName: 'DrugDetail',
      permissionPrefix: 'drugDetail',
      exportColumnName: 'drugDetail',
      queryModel: {
        name: '',
        type: {},
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
      arr[5] = this.amountTotal && this.amountTotal.allFee ? new BigNumber(Number(this.amountTotal.allFee)).toFormat(2)+'VNĐ' : ''
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
      this.search.params.push({
        columnName: "d.type",
        queryType: "=",
        value: validatenull(this.queryModel.type.value) ? "" : this.queryModel.type.value,
      })
      if(this.queryModel.name!==undefined && this.queryModel.name!==''){
        const pattern2 = new RegExp("[A-Za-z]+");
        if (pattern2.test(this.queryModel.name)) {
          this.search.params.push({
            columnName: "d.pinyin_code",
            queryType: "like",
            value: this.queryModel.name.toUpperCase(),
          })
        } else {
          this.search.params.push({
            columnName: "d.goods_name",
            queryType: "like",
            value: this.queryModel.name,
          })
        }
      }
    },
    resetCondition(){
      this.queryModel = {
        name: '',
        type: {},
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
      text-align: left;
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
