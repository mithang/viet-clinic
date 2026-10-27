<template>
  <div class="InfusionPrescription">
    <div class="topBox">
      <el-select v-model="InfoModel.infuseUse" :disabled='IsOnlyRead' style="width: 100px" placeholder="">
        <el-option
          v-for="item in InfusionUseOption"
          :key="item.value"
          :label="item.name"
          :value="{ name:item.name, value: item.value }"
        >
        </el-option>
      </el-select>
      <el-select v-model="InfoModel.frequency" @change="GetInfusionFee()" :disabled='IsOnlyRead' style="width: 100px" placeholder="">
        <el-option
          v-for="item in FrequencyOption"
          :key="item.value"
          :label="item.name"
          :value="{ name:item.name, value: item.value }"
        >
        </el-option>
      </el-select>
      <el-select v-model="InfoModel.days" @change="GetInfusionFee()" :disabled='IsOnlyRead' style="width: 100px" placeholder="">
        <el-option
          v-for="item in DayNumOption"
          :key="item.value"
          :label="item.name"
          :value="{ name:item.name, value: item.value }"
        >
        </el-option> </el-select
      >ngày <el-input v-model="InfoModel.drippingSpeed" :disabled='IsOnlyRead' style="width: 10%"></el-input>giọt/Phút
      <el-table
        :data="InfoModel.infusionList"
        :span-method="ObjectSpanMethod"
        border
        style="width: 100%"
      >
        <el-table-column label="Số nhóm" width="50" align="center">
          <template>
            {{ GroupNumber }}
          </template>
        </el-table-column>
        <el-table-column prop="goodsName" label="Tên">
          <template slot-scope="scope">
            {{ scope.row.goodsName ? scope.row.goodsName : scope.row.drugStuffId.name }}
          </template>
        </el-table-column>
        <el-table-column prop="skinTest" label="" width="180">
          <template slot-scope="scope">
            <el-select v-model="scope.row.skinTest" :disabled='IsOnlyRead' placeholder="">
              <el-option
                v-for="item in InfusionOption"
                :key="item.value"
                :label="item.name"
                :value="{ name:item.name, value: item.value }"
              >
              </el-option>
            </el-select>
          </template>
        </el-table-column>
        <el-table-column prop="singleDosage" label="Liều dùng mỗi lần" width="100">
          <template slot-scope="scope">
            <el-input v-model="scope.row.singleDosage" @input="GetInfusionFee(scope.$index, scope.row)" :disabled='IsOnlyRead'>
              <template slot="append">{{ scope.row.preparationUnit ? scope.row.preparationUnit.name : scope.row.drugStuffId.drug.preparationUnit.name }}</template>
            </el-input>
          </template>
        </el-table-column>
        <el-table-column prop="total" label="Tổng số lượng" width="100">
          <template slot-scope="scope">
            <el-input v-model="scope.row.total" disabled>
              <template slot="append">{{GetInfusionUnit(scope.$index, scope.row)}}</template>
            </el-input>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="Đơn giá" width="100" align="center">
          <template slot-scope="scope">
            {{ GetInfusionPrice(scope.$index, scope.row) + "/" + GetInfusionUnit(scope.$index, scope.row)}}
          </template>
        </el-table-column>
        <el-table-column prop="allFee" label="Tổng cộng" width="100" align="center">
          <template slot-scope="scope">
            {{ scope.row.allFee }}
          </template>
        </el-table-column>
        <el-table-column label="Thao tác" align="center" v-if="!IsOnlyRead">
          <template slot-scope="scope">
            <i
              class="el-icon-circle-close"
              @click="DeleteInfusionItem(scope.$index, scope.row)"
            ></i>
          </template>
        </el-table-column>
      </el-table>
      <el-popover placement="top-start" width="700" trigger="click" v-if="!IsOnlyRead">
        <el-table
          :data="InfusionPresciptionTable"
          border
          highlight-current-row
          @row-click="RowClickInfusionPrescriptionTable"
        >
          <el-table-column prop="goodsName" label="Tên thuốc"> </el-table-column>
          <el-table-column prop="gg" label="Quy cách" width="100">
            <template slot-scope="scope">
                  {{scope.row.dosis}}{{scope.row.dosisUnit.name}} * {{scope.row.preparation}}{{scope.row.preparationUnit.name}}/{{scope.row.pack.name}}
            </template>
          </el-table-column>
          <el-table-column prop="factory.name" label="Nhà sản xuất" width="100">
          </el-table-column>
          <el-table-column label="Đơn vị" width="80">
            <template slot-scope="scope">
              {{
                scope.row.isUnpackSell === '1'
                  ? scope.row.preparationUnit.name
                  : scope.row.pack.name
              }}
            </template>
          </el-table-column>
          <el-table-column label="Giá bán lẻ" width="80">
            <template slot-scope="scope">
              {{
                scope.row.isUnpackSell === '1' ? (scope.row.retailPrice + "/" + scope.row.preparationUnit.name) : (scope.row.price + "/" + scope.row.pack.name)
              }}
            </template>
          </el-table-column>
          <el-table-column prop="stockText" label="Tồn kho" width="100">
          </el-table-column>
        </el-table>
        <el-input
          prefix-icon="el-icon-plus"
          suffix-icon="el-icon-search"
          style="width: 100%"
          slot="reference"
          v-model="SearchInfusionName"
          @input='GetInfusion'
          placeholder="Nhập tên thuốc hoặc mã pinyin"
        ></el-input>
      </el-popover>
    </div>
  </div>
</template>

<script>
import { listAllStock } from "@/api/stock/drug";
import { getCurrentUser } from "@/utils/userCache";
export default {
  props: {
    GroupNumber: {
      type: Number,
    },
    InfoModel: {
      type: Object
    },
    InfusionUseOption: {
      type: Object
    },
    InfusionOption: {
      type: Object
    },
    DayNumOption: {
      type: Object
    },
    FrequencyOption: {
      type: Object
    },
    IsOnlyRead: {
      type: Boolean
    }
  },
  data() {
    return {
      SearchInfusionName: "",
      //输液处方可选择表格
      InfusionPresciptionTable: [],
    };
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
  },
  mounted() {
    this.GetInfusion();
  },
  methods: {
     //Tính tổng lượng và giá theo bán lẻ hay không
    GetInfusionFee(index, row) {
      if (row) {
        row.singleDosage = row.singleDosage ? row.singleDosage : 0
        if (this.InfoModel.frequency && this.InfoModel.days && this.InfoModel.frequency.value && this.InfoModel.days.name) {
          if (row.isUnpackSell === '1') {
            row.total = Math.ceil(row.singleDosage * this.InfoModel.frequency.value.split('_')[1] * this.InfoModel.days.name)
            row.allFee = row.total * (row.retailPrice ? row.retailPrice : row.drugStuffId.drug.retailPrice)
          } else {
            let total = Math.ceil(row.singleDosage * this.InfoModel.frequency.value.split('_')[1] * this.InfoModel.days.name)
            row.total = Math.ceil(total/row.preparation)
            row.allFee = row.total * (row.price ? row.price : row.drugStuffId.drug.price)
          }
        }
      } else {
        this.InfoModel.infusionList.map(row => {
          row.singleDosage = row.singleDosage ? row.singleDosage : 0
          if (this.InfoModel.frequency && this.InfoModel.days) {
            if (row.isUnpackSell === '1') {
              row.total = Math.ceil(row.singleDosage * this.InfoModel.frequency.value.split('_')[1] * this.InfoModel.days.name)
              row.allFee = row.total * (row.retailPrice ? row.retailPrice : row.drugStuffId.drug.retailPrice)
            } else {
              let total = Math.ceil(row.singleDosage * this.InfoModel.frequency.value.split('_')[1] * this.InfoModel.days.name)
              row.total = Math.ceil(total/row.preparation)
              row.allFee = row.total * (row.price ? row.price : row.drugStuffId.drug.price)
            }
          }
        })
      }
      this.$emit('GetInfusionFee')
    },
    //Lấy đơn vị theo bán lẻ hay thêm mới/xem
    GetInfusionUnit(index, row) {
      if(row.preparationUnit) {
        if (row.isUnpackSell === '1') {
          return row.preparationUnit.name
        } else {
          return row.pack.name
        }
      } else {
        if (row.drugStuffId.drug.isUnpackSell === '1') {
          return row.drugStuffId.drug.preparationUnit.name
        } else {
          return row.drugStuffId.drug.pack.name
        }
      }
    },
     //Lấy giá theo bán lẻ hay thêm mới/xem
    GetInfusionPrice (index, row) {
      if(row.preparationUnit) {
        if (row.isUnpackSell === '1') {
          return row.retailPrice
        } else {
          return row.price
        }
      } else {
        if (row.drugStuffId.drug.isUnpackSell === '1') {
          return row.drugStuffId.drug.retailPrice
        } else {
          return row.drugStuffId.drug.price
        }
      }
    },
    GetInfusion() {
      let SearchModel = {
        columnName: "",
        limit: 10,
        offset: 0,
        order: "",
        params: [
          {
            columnName: "company_id",
            queryType: "=",
            value: this.Company.id,
          },
          {
            columnName: "type",
            queryType: "=",
            value: "medicalType_0",
          },
          {
            columnName: "goods_name",
            queryType: "like",
            value: this.SearchInfusionName,
          },
        ],
      };
      listAllStock(SearchModel).then((responseData) => {
        if (responseData.code === 100) {
          responseData.data.forEach(element => {
            let isUnpackSell = element.isUnpackSell;    //Cho phép bán lẻ tách hộp
            let stockNumber = element.stockNumber;      //Số lượng tồn kho
            let packName = element.pack.name;           //Đóng gói（Đơn vị）
            let preparation = element.preparation;      //Chế phẩm
            let preparationUnit = element.preparationUnit.name;  //Đơn vị bào chế
            let price = element.price;                  //Giá bán
            let retailPrice = element.retailPrice;      //Giá bán lẻ sau khi tách hộp
            
            let stockText = "";
            if(isUnpackSell === '1')
            {
              //Làm tròn xuống
              let p1 = Math.floor(stockNumber/preparation);
              //Lấy phần dư
              let p2 = stockNumber%preparation;
              if(p1 > 0)
              {
                stockText = p1 + packName;
              }
              if(p2 > 0)
              {
                if(p1 > 0)
                {
                  stockText = stockText + p2 + preparationUnit;
                }
                else
                {
                  stockText = p2 + preparationUnit;
                }
              }
            }
            else
            {
              stockText = stockNumber + packName;
            }

            element['stockText'] = stockText;
          });


          this.InfusionPresciptionTable = responseData.data;
        }
      });
    },
    //删除输血处方每组表格的行
    DeleteInfusionItem(index) {
      this.InfoModel.infusionList.splice(index, 1);
      this.$emit('GetInfusionFee')
    },
    //搜索药品表格行点击添加到当前组
    RowClickInfusionPrescriptionTable(row) {
      row.infuseGroup = this.GroupNumber
      this.InfoModel.infusionList.push({
        allFee: 0,
        ...JSON.parse(JSON.stringify(row))
      });
    },
    //合并第一项组号
    ObjectSpanMethod({ row, column, rowIndex, columnIndex }) {
      if (columnIndex === 0) {
        if (rowIndex % this.InfoModel.infusionList.length === 0) {
          return {
            rowspan: this.InfoModel.infusionList.length,
            colspan: 1,
          };
        } else {
          return {
            rowspan: 0,
            colspan: 0,
          };
        }
      }
    },
  },
};
</script>
<style lang="scss">
.InfusionPrescription {
  .el-input-group__append {
    padding: 0 10px;
  }
}
</style>
<style lang='scss' scoped>
.InfusionPrescription {
  width: 100%;
  height: 100%;
  .topBox {
    width: 100%;
    padding: 10px;
  }
}
</style>