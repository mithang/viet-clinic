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
            <el-col :span="6">
              <el-form-item label='Tên thuốc' prop='name'>
                <el-input v-model='queryModel.name' :clearable='true' placeholder='Vui lòng nhập tên thuốc'></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label='Số đơn thuốc' prop='recipelCode'>
                <el-input v-model='queryModel.recipelCode' :clearable='true' placeholder='Vui lòng nhập số đơn thuốc'></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item label='Loại thuốc' prop='type'>
                <el-select
                  v-model="queryModel.type"
                  value-key="value"
                  filterable
                  clearable
                  placeholder="Vui lòng chọn loại thuốc"
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
              height="calc(100vh - 325px)"
              border
              ref="mutipleTable1"
              @sort-change="onSortChange"
              @header-dragend="onChangeWidth"
              :cell-class-name="cellClassName"
              :header-cell-class-name="headerCellClassName"
              highlight-current-row
              show-summary
              :summary-method='getTotal'
              id="exportTable"
            >
              <el-table-column
                label="Số thứ tự"
                type="index"
                :index="indexMethod"
                align="center">
              </el-table-column>
              <el-table-column prop="recipelCode" label="Số đơn thuốc">
              </el-table-column>
              <el-table-column prop="type" label="Loại thuốc" width="70"></el-table-column>
              <el-table-column prop="name" label="Tên thuốc"></el-table-column>
              <el-table-column prop="norms" label="Quy cách" width="80"></el-table-column>
              <el-table-column prop="number" label="Số lượng" width="50">
                <template slot-scope="scope">
                  <span style="display:inline-block;width:100%;text-align:right" v-if="scope.row.isUnpackSell === 0">
                    {{ scope.row.number / scope.row.preparation }}
                  </span>
                  <span style="display:inline-block;width:100%;text-align:right" v-if="scope.row.isUnpackSell === 1">
                    {{ scope.row.number }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="unit" label="Đơn vị" width="50">
                <template slot-scope="scope">
                  <span style="display:inline-block;width:100%;text-align:right" v-if="scope.row.isUnpackSell === 0">
                    {{ scope.row.pack }}
                  </span>
                  <span style="display:inline-block;width:100%;text-align:right" v-if="scope.row.isUnpackSell === 1">
                    {{ scope.row.unit }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="bid" label="Đơn giá nhập(VNĐ)" width="90">
                <template slot-scope="scope">
                  <span style="display:inline-block;width:100%;text-align:right" v-if="scope.row.isUnpackSell === 0">
                    {{ (scope.row.packageBid).toFixed(4) }}
                  </span>
                  <span style="display:inline-block;width:100%;text-align:right" v-if="scope.row.isUnpackSell === 1">
                    {{ (scope.row.bid).toFixed(4) }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="bidTotal" label="Tổng giá nhập(VNĐ)" width="90">
                <template slot-scope="scope">
                  <span style="display:inline-block;width:100%;text-align:right" v-if="scope.row.isUnpackSell === 0">
                    {{ bigNum(scope.row.packageBid * (scope.row.number / scope.row.preparation)) }}
                  </span>
                    <span style="display:inline-block;width:100%;text-align:right" v-if="scope.row.isUnpackSell === 1">
                    {{ bigNum(scope.row.bid * scope.row.number) }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="price" label="Đơn giá bán(VNĐ)" width="90">
                <template slot-scope="scope">
                  <span style="display:inline-block;width:100%;text-align:right" v-if="scope.row.isUnpackSell === 0">
                    {{ (scope.row.packagePrice).toFixed(4) }}
                  </span>
                  <span style="display:inline-block;width:100%;text-align:right" v-if="scope.row.isUnpackSell === 1">
                    {{ (scope.row.price).toFixed(4) }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="priceTotal" label="Tổng giá bán(VNĐ)" width="90">
                <template slot-scope="scope">
                  <span style="display:inline-block;width:100%;text-align:right" v-if="scope.row.isUnpackSell === 0">
                    {{ bigNum(scope.row.packagePrice * (scope.row.number / scope.row.preparation)) }}
                  </span>
                    <span style="display:inline-block;width:100%;text-align:right" v-if="scope.row.isUnpackSell === 1">
                    {{ bigNum(scope.row.price * scope.row.number) }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="profit" label="Lợi nhuận(VNĐ)" width="90">
                <template slot-scope="scope">
                  <span style="display:inline-block;width:100%;text-align:right" v-if="scope.row.isUnpackSell === 0">
                    {{ bigNum(scope.row.packagePrice * (scope.row.number / scope.row.preparation)) - bigNum(scope.row.packageBid * (scope.row.number / scope.row.preparation)) }}
                  </span>
                  <span style="display:inline-block;width:100%;text-align:right" v-if="scope.row.isUnpackSell === 1">
                    {{ bigNum(scope.row.price * scope.row.number) - bigNum(scope.row.bid * scope.row.number)}}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="factory" label="Nhà sản xuất">
              </el-table-column>
              <el-table-column prop="batchNo" label="Số lô sản xuất">
              </el-table-column>
              <el-table-column prop="dispensingDate" label="Ngày phát thuốc">
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
  import {validatenull} from "@/utils/validate";
  import DispensingForm from "./dispensingForm";
  import MainUI from "@/views/components/mainUI";
  import {listDictItemAll} from "@/api/sys/dictItem";
  import { getDictItemsByCode, DICT_CODE } from '@/utils/dictCache'
  import {getList, getAmount} from "@/api/stock/dispensing";
  import {BigNumber} from "bignumber.js";
  import { getCurrentUser } from "@/utils/userCache";
  import * as XLSX from 'xlsx'
  import XLSXD from 'xlsx-style'

  export default {
    extends: MainUI,
    components: {
      DispensingForm,
    },
    data() {
      return {
        queryModel: {
          name: '',
          recipelCode: '',
          type: '',
          dateRange: [this.addCreateDate(), new Date()]
        },
        search: {
          offset: 0,
          limit: 20,
          order: "",
          columnName: "",
          params: [
            {
              columnName: "company_id",
              queryType: "=",
              value: currentUser.company.id,
            },
            {
              logic: "AND",
              queryType: "("
            },
            {
              columnName: "create_date",
              logic: "",
              queryType: 'between',
              value: [],
            },
            {
              logic: "",
              queryType: ")"
            },
          ],
        },
        currentPage: 1,
        pageSize: 20,
        dispensingTotal: 0,
        dispensingList: [],
        oprColumnWidth: 140, // Độ rộng cột thao tác
        filerData: {
          pickerOptions: {
            disabledDate(time) {
              return time.getTime() > Date.now() - 8.64e6;
            },
          },
        },
        allTotal: {}, //Tổng cộng
        type_List: [], // Loại thuốc
        queryTypes: {
          goods_name: "like",
          type: "=",
          bar_code: "like",
        },
      };
    },
    updated() {
      this.$nextTick(() => {
        this.$refs.mutipleTable1.doLayout();
      });
    },
    methods: {
      addCreateDate() {
        let myDate = new Date();
        let lw = new Date(myDate.getTime() - 1000 * 60 * 60 * 24 * 30); //Chữ số cuối cùng30có thể sửa，30nghĩa là ngày
        let lastY = lw.getFullYear();
        let lastM = lw.getMonth() + 1;
        let lastD = lw.getDate();
        let housrs = lw.getHours();
        let minutes = lw.getMinutes()
        let seconds = lw.getSeconds()
        let startData =
          lastY +
          "-" +
          (lastM < 10 ? "0" + lastM : lastM) +
          "-" +
          (lastD < 10 ? "0" + lastD : lastD) +
          " " + housrs + ":" + minutes + ":" + seconds //Ngày của 30 ngày trước
        let returnDate = new Date(startData)
        return returnDate;

      },
      indexMethod(index) {
        return (this.currentPage - 1) * this.search.limit + index + 1;
      },
      getTotal(param) {
        let {columns, data} = param;
        let arr = []
        columns.forEach((column, index) => {
          if (index === 0) {
            arr[index] = 'Tổng cộng';
          } else {
            arr[index] = ''
          }
        })
        arr[8] = new BigNumber(Number(this.allTotal.bidTotalAmount)).toFormat(2) + "VNĐ"
        // arr[4] = this.allTotal.numberAmount
        arr[10] = new BigNumber(Number(this.allTotal.priceTotalAmount)).toFormat(2) + "VNĐ"
        arr[11] = new BigNumber(Number(this.allTotal.profitAmount)).toFormat(2) + "VNĐ"
        return arr
      },
      init() {

        if (this.queryModel.dateRange && this.queryModel.dateRange.length) {
          this.queryModel.dateRange[0] = this.$moment(this.queryModel.dateRange[0]).format(
            "YYYY-MM-DD HH:mm:ss"
          )
          this.queryModel.dateRange[1] = this.$moment(this.queryModel.dateRange[1]).format(
            "YYYY-MM-DD HH:mm:ss"
          )
          this.search.params[2].value = this.queryModel.dateRange

        }
        this.setLoad();
        getList(this.search).then((res) => {
          this.dispensingList = res.data.rows;
          this.dispensingTotal = res.data.total;
          getAmount(this.search).then(response => {
            this.allTotal = response.data
            this.resetLoad();
          })
        });
      },
      onSearch() {
        this.currentPage = 1;
        this.search = {
          columnName: "",
          limit: this.pageSize,
          offset: this.currentPage - 1,
          order: "",
          params: [
            {
              columnName: "company_id",
              queryType: "=",
              value: currentUser.company.id,
            },
          ]
        }
        if (this.queryModel.dateRange && this.queryModel.dateRange.length) {
          this.search.params.push({
            logic: "AND",
            queryType: "("
          }, {
            columnName: "create_date",
            logic: "",
            queryType: 'between',
            value: [],
          }, {
            logic: "",
            queryType: ")"
          })
        }
        if (this.queryModel.name && this.queryModel.name !== '') {
          this.search.params.push({
            columnName: "drug.goods_name",
            queryType: "like",
            value: this.queryModel.name,
          },)
        }
        if (this.queryModel.recipelCode && this.queryModel.recipelCode !== '') {
          this.search.params.push({
            columnName: "recipelInfo.code",
            queryType: "=",
            value: this.queryModel.recipelCode,
          },)
        }
        this.search.params.push({
          columnName: "drug.type",
          queryType: "=",
          value: validatenull(this.queryModel.type.value)
            ? ""
            : this.queryModel.type.value,
        })
        this.init();
      },
      resetCondition() {
        this.queryModel = {
          name: '',
          recipelCode: '',
          type: '',
          dateRange: [this.addCreateDate(), new Date()]
        }
        this.currentPage = 1;
        this.search = {
          offset: 0,
          limit: this.pageSize,
          order: "",
          columnName: "",
          params: [
            {
              columnName: "company_id",
              queryType: "=",
              value: currentUser.company.id,
            },
            {
              logic: "AND",
              queryType: "("
            },
            {
              columnName: "create_date",
              logic: "",
              queryType: 'between',
              value: [],
            },
            {
              logic: "",
              queryType: ")"
            },
          ],
        }
        this.init()
      },
      onSizeChange(val) {
        this.currentPage = 1;
        this.search.limit = val;
        this.search.offset = (this.currentPage - 1) * val;
        this.init();
      },
      onCurrentChange(val) {
        this.search.offset = (val - 1) * this.search.limit;
        this.currentPage = val;
        this.init();
      },
      initOptions(This) {
        getDictItemsByCode(DICT_CODE.MEDICAL_TYPE).then((data) => {
          this.type_List = data;
        });
      },
      bigNum(num) {
        if (num || num === '0') {
          return new BigNumber(num).toFormat(2)
        } else {
          return ''
        }
      },
      exportExcel() {
        let biaoge = []
        biaoge[0] = {
          recipelCode: "Số đơn thuốc",
          type: "Loại thuốc",
          name: "Tên thuốc",
          norms: "Quy cách",
          number: "Số lượng",
          unit: "Đơn vị",
          bid: "Đơn giá nhập(VNĐ)",
          packageBid: "Giá gói nhập (VNĐ)",
          bidTotal: "Tổng giá nhập(VNĐ)",
          price: "Đơn giá bán(VNĐ)",
          packagePrice: "Giá gói bán (VNĐ)",
          priceTotal: "Tổng giá bán(VNĐ)",
          profit: "Lợi nhuận(VNĐ)",
          factory: "Nhà sản xuất",
          batchNo: "Số lô sản xuất",
          dispensingDate: "Ngày phát thuốc",
        }
        for (let i = 0; i < this.dispensingList.length; i++) {
          let arr = {
            recipelCode: "",
            type: "",
            name: "",
            norms: "",
            number: "",
            unit: "",
            bid: "",
            bidTotal: "",
            price: "",
            priceTotal: "",
            profit: "",
            factory: "",
            batchNo: "",
            dispensingDate: "",
          }
          arr.recipelCode = this.dispensingList[i].recipelCode
          arr.type = this.dispensingList[i].type
          arr.name = this.dispensingList[i].name
          arr.norms = this.dispensingList[i].norms
          arr.number = this.dispensingList[i].number
          arr.unit = this.dispensingList[i].unit
          arr.bid = this.dispensingList[i].bid
          arr.bidTotal = this.dispensingList[i].bidTotal
          arr.price = this.dispensingList[i].price
          arr.priceTotal = this.dispensingList[i].priceTotal
          arr.profit = this.dispensingList[i].profit
          arr.factory = this.dispensingList[i].factory
          arr.batchNo = this.dispensingList[i].batchNo
          arr.dispensingDate = this.dispensingList[i].dispensingDate
          biaoge.push(arr)
        }
        let num = biaoge.length
        biaoge[num] = {
          recipelCode: "Tổng cộng",
          type: "",
          name: "",
          norms: "",
          number: "",
          unit: "",
          bid: "",
          bidTotal: new BigNumber(Number(this.allTotal.bidTotalAmount)).toFormat(2) + "",
          price: "",
          priceTotal: new BigNumber(Number(this.allTotal.priceTotalAmount)).toFormat(2) + "",
          profit: new BigNumber(Number(this.allTotal.profitAmount)).toFormat(2) + "",
          factory: "",
          batchNo: "",
          dispensingDate: "",
        }
        let web = XLSX.utils.book_new();
        let contentWs = XLSX.utils.json_to_sheet(biaoge);
        contentWs["A1"] = {
          t: "s",
          v: this.Company.name + ' Bảng chi tiết bán thuốc',
          s: {
            alignment: {
              horizontal: "center",
              vertical: "center",
              wrapText: true,
            },
          }
        }
        for (let i = 0; i < biaoge.length; i++) {
          contentWs["A" + (i + 2)] = {
            t: "s",
            v: biaoge[i].recipelCode,
            s: {
              alignment: {
                horizontal: "center",
                vertical: "center",
                wrapText: true,
              },
              border: {
                top: {style: 'thin'},
                left: {style: 'thin'},
                bottom: {style: 'thin'},
                right: {style: 'thin'},

              }
            }
          }
          contentWs["B" + (i + 2)] = {
            t: "s",
            v: biaoge[i].type,
            s: {
              alignment: {
                horizontal: "center",
                vertical: "center",
                wrapText: true,
              },
              border: {
                top: {style: 'thin'},
                left: {style: 'thin'},
                bottom: {style: 'thin'},
                right: {style: 'thin'},

              }
            }
          }
          contentWs["C" + (i + 2)] = {
            t: "s",
            v: biaoge[i].name,
            s: {
              alignment: {
                horizontal: "center",
                vertical: "center",
                wrapText: true,
              },
              border: {
                top: {style: 'thin'},
                left: {style: 'thin'},
                bottom: {style: 'thin'},
                right: {style: 'thin'},

              }
            }
          }
          contentWs["D" + (i + 2)] = {
            t: "s",
            v: biaoge[i].norms,
            s: {
              alignment: {
                horizontal: "center",
                vertical: "center",
                wrapText: true,
              },
              border: {
                top: {style: 'thin'},
                left: {style: 'thin'},
                bottom: {style: 'thin'},
                right: {style: 'thin'},

              }
            }
          }
          contentWs["E" + (i + 2)] = {
            t: "s",
            v: biaoge[i].number,
            s: {
              alignment: {
                horizontal: "center",
                vertical: "center",
                wrapText: true,
              },
              border: {
                top: {style: 'thin'},
                left: {style: 'thin'},
                bottom: {style: 'thin'},
                right: {style: 'thin'},

              }
            }
          }

          contentWs["F" + (i + 2)] = {
            t: "s",
            v: biaoge[i].unit,
            s: {
              alignment: {
                horizontal: "center",
                vertical: "center",
                wrapText: true,
              },
              border: {
                top: {style: 'thin'},
                left: {style: 'thin'},
                bottom: {style: 'thin'},
                right: {style: 'thin'},

              }
            }
          }
          contentWs["G" + (i + 2)] = {
            t: "s",
            v: biaoge[i].bid,
            s: {
              alignment: {
                horizontal: "center",
                vertical: "center",
                wrapText: true,
              },
              border: {
                top: {style: 'thin'},
                left: {style: 'thin'},
                bottom: {style: 'thin'},
                right: {style: 'thin'},

              }
            }
          }
          contentWs["H" + (i + 2)] = {
            t: "s",
            v: biaoge[i].bidTotal,
            s: {
              alignment: {
                horizontal: "center",
                vertical: "center",
                wrapText: true,
              },
              border: {
                top: {style: 'thin'},
                left: {style: 'thin'},
                bottom: {style: 'thin'},
                right: {style: 'thin'},

              }
            }
          }
          contentWs["I" + (i + 2)] = {
            t: "s",
            v: biaoge[i].price,
            s: {
              alignment: {
                horizontal: "center",
                vertical: "center",
                wrapText: true,
              },
              border: {
                top: {style: 'thin'},
                left: {style: 'thin'},
                bottom: {style: 'thin'},
                right: {style: 'thin'},

              }
            }
          }
          contentWs["J" + (i + 2)] = {
            t: "s",
            v: biaoge[i].priceTotal,
            s: {
              alignment: {
                horizontal: "center",
                vertical: "center",
                wrapText: true,
              },
              border: {
                top: {style: 'thin'},
                left: {style: 'thin'},
                bottom: {style: 'thin'},
                right: {style: 'thin'},

              }
            }
          }
          contentWs["K" + (i + 2)] = {
            t: "s",
            v: biaoge[i].profit,
            s: {
              alignment: {
                horizontal: "center",
                vertical: "center",
                wrapText: true,
              },
              border: {
                top: {style: 'thin'},
                left: {style: 'thin'},
                bottom: {style: 'thin'},
                right: {style: 'thin'},

              }
            }
          }

          contentWs["L" + (i + 2)] = {
            t: "s",
            v: biaoge[i].factory,
            s: {
              alignment: {
                horizontal: "center",
                vertical: "center",
                wrapText: true,
              },
              border: {
                top: {style: 'thin'},
                left: {style: 'thin'},
                bottom: {style: 'thin'},
                right: {style: 'thin'},

              }
            }
          }
          contentWs["M" + (i + 2)] = {
            t: "s",
            v: biaoge[i].batchNo,
            s: {
              alignment: {
                horizontal: "center",
                vertical: "center",
                wrapText: true,
              },
              border: {
                top: {style: 'thin'},
                left: {style: 'thin'},
                bottom: {style: 'thin'},
                right: {style: 'thin'},

              }
            }
          }
          contentWs["N" + (i + 2)] = {
            t: "s",
            v: biaoge[i].dispensingDate,
            s: {
              alignment: {
                horizontal: "center",
                vertical: "center",
                wrapText: true,
              },
              border: {
                top: {style: 'thin'},
                left: {style: 'thin'},
                bottom: {style: 'thin'},
                right: {style: 'thin'},

              }
            }
          }

        }
        const mergeTitle = [

          {
            s: {r: 0, c: 0},
            e: {r: 0, c: 13}
          },

        ]
        //Cố định dòng đầu tiên và cột đầu tiên：
        contentWs['!freeze'] = {
          xSplit: "11",  //Cố định cột
          ySplit: "1",  //Cố định dòng
          topLeftCell: "A2",  //Ô hiển thị ở góc trên bên trái của vùng chưa cố định，Mặc định là ô chưa cố định đầu tiên
          state: "frozen"
        }
        contentWs["!merges"] = mergeTitle

        contentWs["!cols"] = [{wch: 20}, {wch: 10}, {wch: 20}, {wch: 15}, {wch: 10}, {wch: 10}, {wch: 15}, {wch: 15}, {wch: 15}, {wch: 15}, {wch: 15}, {wch: 25}, {wch: 15}, {wch: 20}]
        contentWs["!rows"] = {}

        contentWs["!margins"] = {left: 1.0, right: 1.0, top: 1.0, bottom: 1.0, header: 0.5, footer: 0.5}
        XLSX.utils.book_append_sheet(web, contentWs, "Bảng chi tiết bán thuốc");
        // //Sử dụngxlsx-styleCách ghi tệp，để kiểu tùy chỉnh có hiệu lực
        const tmpDown = new Blob([
          this.s2ab(
            XLSXD.write(web, {
              bookType: "xlsx",
              BookSST: true,
              type: "binary",
              cellStyles: true,
            })
          )
        ])
        this.downExcel(tmpDown, 'Bảng chi tiết bán thuốc.xlsx')
      },
      downExcel(obj, fileName) {
        const a_node = document.createElement("a");
        a_node.download = fileName;
        if ("msSaveOrOpenBlob" in navigator) {
          window.navigator.msSaveOrOpenBlob(obj, fileName)
        } else {
          a_node.href = URL.createObjectURL(obj);
        }
        a_node.click()

        setTimeout(() => {
          URL.revokeObjectURL(obj)
        }, 2000)
      },
      s2ab(s) {
        if (typeof ArrayBuffer !== "undefined") {
          const buf = new ArrayBuffer(s.length);
          const view = new Uint8Array(buf);
          for (let i = 0; i !== s.length; ++i) {
            view[i] = s.charCodeAt(i) & 0xff
          }
          return buf;
        } else {
          const buf = new Array(s.length);
          for (let i = 0; i !== s.length; ++i) {
            buf[i] = s.charCodeAt(i) & 0xff;
          }
          return buf;
        }
      },
    },
    watch: {},
    mounted() {
      this.initOptions()
      this.init();
    },
    computed: {
      Company() {
        let company = getCurrentUser().company;
        return {
          id: company.id,
          label: company.label,
          name: company.name,
        };
      },
    }
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
        text-align: right;
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
