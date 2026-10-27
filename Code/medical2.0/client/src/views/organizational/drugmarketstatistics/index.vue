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
          <el-form :model='queryModel' @submit.native.prevent label-position="left" label-width='70px' ref='queryForm'
                   :inline-message='true'>
            <el-col :span="8">
              <el-form-item label='Ngày thu phí' prop='time'>
                <el-date-picker
                  v-model="queryModel.dateRange"
                  type="datetimerange"
                  range-separator="đến"
                  start-placeholder="Ngày bắt đầu"
                  end-placeholder="Ngày kết thúc"
                  :default-time="['00:00:00', '23:59:59']"
                  format="yyyy-MM-dd HH:mm:ss"
                >
                </el-date-picker>
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item label="Phòng khám" style="margin-right: 3px;">
                <el-select v-model="queryModel.jgid" placeholder="Phòng khám">
                  <el-option label="Tất cả" value="qb"></el-option>
                  <el-option v-for="item in jglist" :value="item.jgid" :key="item.jgid" :label="item.jgmc">
                  </el-option>
                </el-select>
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
              <el-table-column prop="type" label="Phân loại thuốc">
              </el-table-column>
              <el-table-column prop="name" label="Tên thuốc"></el-table-column>
              <el-table-column prop="dosis" label="Quy cách">
                <template slot-scope="scope">
                  <template>
                    {{ scope.row.dosis }}{{ scope.row.dosisUnit }}*{{ scope.row.preparation }}{{ scope.row.preparationUnit }}/{{ scope.row.pack }}
                  </template>
                </template>
              </el-table-column>
              <el-table-column prop="total" label="Số lượng">
                <template slot-scope="scope">
                  {{ Math.floor(scope.row.total / scope.row.preparation) >= 0 ? Math.floor(scope.row.total / scope.row.preparation) + (scope.row.pack) + ((scope.row.total % scope.row.preparation > 0) ? (scope.row.total % scope.row.preparation) + scope.row.preparationUnit : "") : scope.row.total + scope.row.preparationUnit }}
                </template>
              </el-table-column>
              <el-table-column prop="allFee" label="Tổng giá">
                <template slot-scope="scope">
                  <!-- <span style="display:inline-block;width:100%;text-align:right"> -->
                  {{ bigNum(scope.row.allFee ? scope.row.allFee : 0) }}VNĐ
                  <!-- </span> -->
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
import { validatenull } from "@/utils/validate";
import { getdrugmarketstatistics, getdrugmarketstatisticsStat, exportExcel } from "@/api/toll/drugDetail";
import { getjglist } from "@/api/toll/tollInfo";

export default {
  mixins: [reportViewMixin],
  data() {
    return {
      listApi: getdrugmarketstatistics,
      statApi: getdrugmarketstatisticsStat,
      exportApi: exportExcel,
      entityName: 'DrugMarketStatistics',
      permissionPrefix: 'drugmarketstatistics',
      exportColumnName: 'drugDetail',
      queryModel: {
        name: '',
        type: '',
        dateRange: [this.addCreateDate(), new Date()],
        jgid: null,
      },
      jglist: [],
      YpjxcRc: {
        limit: 20,
        offset: 0,
        companyId: currentUser.company.id,
        orderby: "batch_no",
        jgid: null,
      },
      zsids: [],
    };
  },
  updated() {
    this.$nextTick(() => {
      this.$refs.mutipleTable1.doLayout();
    });
  },
  methods: {
    exportExcel() {
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

    getTotal(param) {
      let { columns } = param;
      let arr = []
      columns.forEach((column, index) => {
        if (index === 0) {
          arr[index] = 'Tổng cộng';
        } else {
          arr[index] = ''
        }
      })
      let allFee = this.allTotal != null ? this.allTotal.allFee : 0
      arr[6] = new BigNumber(Number(allFee)).toFormat(2) + 'VNĐ'
      return arr
    },

    appendSearchParams() {
      // Lọc phòng khám
      if (this.queryModel.jgid != null && this.queryModel.jgid !== 'qb') {
        this.search.params.push({
          columnName: " company_id",
          queryType: "=",
          value: this.queryModel.jgid,
        })
      } else {
        let jgidlists = [];
        this.jglist.forEach((item) => {
          jgidlists.push(item.jgid)
        })
        if (jgidlists.length > 0) {
          this.search.params.push({
            columnName: " oc.id",
            queryType: "in",
            value: jgidlists,
          })
        }
      }
      // Khoảng ngày
      if (this.queryModel.dateRange && this.queryModel.dateRange.length) {
        this.queryModel.dateRange[0] = this.$moment(this.queryModel.dateRange[0]).format("YYYY-MM-DD HH:mm:ss")
        this.queryModel.dateRange[1] = this.$moment(this.queryModel.dateRange[1]).format("YYYY-MM-DD HH:mm:ss")
        this.search.params.push(
          { logic: "AND", queryType: "(" },
          { columnName: "create_date", logic: "", queryType: 'between', value: this.queryModel.dateRange },
          { logic: "", queryType: ")" }
        )
      }
      // Loại thuốc
      this.search.params.push({
        columnName: "d.type",
        queryType: "=",
        value: validatenull(this.queryModel.type.value) ? "" : this.queryModel.type.value,
      })
      // Tên thuốc
      if (this.queryModel.name !== undefined && this.queryModel.name !== '') {
        this.search.params.push({
          columnName: "d.goods_name",
          queryType: "like",
          value: this.queryModel.name,
        })
      }
    },

    loadData() {
      this.setLoad()
      this.search.params = []
      this.appendSearchParams()
      this.listApi(this.search).then(responseData => {
        if (responseData.code === 100) {
          this.dispensingList = responseData.data.rows
          this.dispensingTotal = responseData.data.total
          if (this.statApi) {
            this.statApi(this.search).then((res) => {
              if (res.code === 100) {
                this.allTotal = res.data
                this.resetLoad()
              }
            }).catch(() => {})
          } else {
            this.resetLoad()
          }
        } else {
          this.showMessage(responseData)
          this.resetLoad()
        }
      }).catch(error => {
        this.outputError(error)
      })
    },

    resetCondition() {
      this.queryModel = {
        name: '',
        type: '',
        jgid: null,
        dateRange: [this.addCreateDate(), new Date()]
      }
      this.currentPage = 1
      this.onSearch()
    },

    Getcliniclist() {
      this.jglist = [];
      this.zsids = [];
      getjglist(this.YpjxcRc).then((responseData) => {
        if (responseData.code === 100) {
          if (responseData.data.length > 0) {
            responseData.data.forEach((item) => {
              if (item.jgid !== currentUser.company.id) {
                this.jglist.push({
                  jgid: item.jgid,
                  jgmc: item.jgmc,
                })
                this.zsids.push({ jgid: item.jgid })
              }
            })
          }
        }
      })
    },
  },
  watch: {},
  mounted() {
    // reportViewMixin.mounted() Đã gọi this.pageInit()（内含 initOptions + Tải dữ liệu），
    // Tại đây chỉ bổ sung tải danh sách cơ sở
    this.Getcliniclist()
  },
};
</script>
<style lang="scss" scoped>
  .page-container {
    padding: 0;
  }

  .query-form-container {
    padding: 0px 0px 10px 0px;
  }

  .typeClass {
    ::v-deep .el-input {
      width: 100% !important;

      input {
        width: 100% !important;
        padding-right: 0;
      }
    }
  }

  .el-col {
    ::v-deep .el-range-separator {
      width: 10%;
    }
  }

  .cell {
    span {
      display: inline-block;
      width: 100%;
      text-align: right;
    }
  }

  ::v-deep .el-table__footer-wrapper {
    td:not(:nth-of-type(1)) {
      .cell {
        display: inline-block;
        width: 100%;
        text-align: left;
      }
    }

  }

  .el-table::before {
    height: 0;
  }

  ::v-deep .el-table colgroup col[name='gutter'] {
    width: 5px !important
  }

  ::v-deep .el-table__body {
    width: 100% !important
  }
</style>
