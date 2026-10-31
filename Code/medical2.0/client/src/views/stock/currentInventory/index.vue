<template>
  <el-row v-loading="loading">
    <el-card class="page-container">
        <!-- Chuyển đổi thuốc/vật tư -->
        <div style="margin-top:-20px;margin-left:-20px">
            <el-tabs v-model="activeName" type="card" @tab-click="handleClick">
                <el-tab-pane label="Tồn kho thuốc" name="0">

                </el-tab-pane>
                <el-tab-pane label="Tồn kho vật tư" name="1">

                </el-tab-pane>
            </el-tabs>
        </div>

        <div style="padding-top:10px">
             <el-radio-group v-model="tabPosition" @change="changStatus" style="margin-bottom: 10px;">
                <el-radio-button label="0">Theo số lô</el-radio-button>
                <el-radio-button label="1">Theo hàng hóa</el-radio-button>
            </el-radio-group>
            <div style="float:right;">
              <el-button type="primary" icon='el-icon-upload2' @click="exportTable">Xuất</el-button>
            </div>
        </div>
      <!--  Thanh tìm kiếm  Bắt đầu -->
     <div v-if="activeName==='0'" style="padding-top:10px">
         <!-- Hiển thị theo số lô và hàng hóa -->
        <div v-if="tabPosition==='0' && dispensingList!=null">
             <div class='query-form-container'>
          <el-row class='search-row'>
            <el-form :model='queryModel' @submit.native.prevent label-position="left" label-width='70px' ref='queryForm' :inline-message='true'>
              <el-col :span="6">
                <el-form-item label='Tên thuốc' prop='name'>
                  <el-input
                    style="width:320px;"
                    v-model="queryModel.name"
                    placeholder="Vui lòng nhập tên thuốc hoặc mã pinyin"
                  ></el-input>
                </el-form-item>
              </el-col>
                <el-col :span="6">
              <el-form-item label='Nhà cung cấp' prop='type'>
                <el-select
                  v-model="queryModel.supplier"
                  value-key="value"
                  filterable
                  clearable
                  placeholder="Vui lòng chọn nhà cung cấp"
                  class="typeClass"
                >
                  <el-option
                    v-for="type in supplierList"
                    :key="type.id"
                    :label="type.name"
                    :value="type.id"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-col>
              <el-col :span="6">
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
            <el-col :span="6">
              <el-form-item label='Có kích hoạt không' prop='status'>
                <el-select
                  v-model="queryModel.status"
                  value-key="value"
                  filterable
                  clearable
                  placeholder="Vui lòng chọn có kích hoạt không"
                  class="typeClass"
                >
                  <el-option
                    v-for="type in isEnable"
                    :key="type.value"
                    :label="type.name"
                    :value="type.value"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="6">
                <el-form-item label='Số lượng còn lại' prop='inventory'>
                <el-select
                  v-model="queryModel.inventory"
                  value-key="value"
                  filterable
                  clearable
                  placeholder="Vui lòng chọn số lượng còn lại"
                  class="typeClass"
                >
                  <el-option
                    v-for="type in inventorys"
                    :key="type.value"
                    :label="type.name"
                    :value="type.value"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-col>
            </el-form>
          </el-row>
          <el-row id="conditionOperation">
            <el-col :span="24" style="text-align:right;padding-right:5px">

                <el-button type="primary" icon="el-icon-search" @click='drugBatchNumber(1)' :plain='true'>Tìm kiếm</el-button>
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
              height="calc(100vh - 300px)"
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

              <el-table-column
              label="Số thứ tự"
              type="index"
              :index="indexMethod"
              align="center">
            </el-table-column>
             <el-table-column prop="drug.goodsName" label="Tên thuốc" > </el-table-column>
              <el-table-column prop="drug.type.name" label="Phân loại thuốc">
              </el-table-column>

              <el-table-column prop="supplierStock.norms" label="Quy cách">
                 </el-table-column>
                <el-table-column prop="drug.factory.name" label="Nhà sản xuất" > </el-table-column>
                <el-table-column prop="supplierStock.supplierId.name" label="Nhà cung cấp" > </el-table-column>
                <el-table-column prop="supplierStock.batchNo" label="Số lô" > </el-table-column>
                <el-table-column prop="drug.price" label="Giá bán(VNĐ)" > </el-table-column>
                <el-table-column prop="" label="Tổng giá bán(VNĐ)" >
                    <template slot-scope="scope">
                        {{getPriceTotal(scope.row)}}
                    </template>
                </el-table-column>
                <el-table-column prop="supplierStock.bid" label="Giá vốn(VNĐ)" > </el-table-column>
                <el-table-column prop="" label="Tổng chi phí(VNĐ)" >
                    <template slot-scope="scope">
                        {{(scope.row.supplierStock.leastBid*(scope.row.storageStock-scope.row.usedStock-scope.row.reimburseStock)).toFixed(2)}}
                    </template>
                </el-table-column>
              <el-table-column prop="number" label="Số lượng còn lại" >
                  <template slot-scope="scope">
                  {{Math.floor((scope.row.storageStock-scope.row.usedStock-scope.row.reimburseStock)/scope.row.drug.preparation) >= 0 ? Math.floor((scope.row.storageStock-scope.row.usedStock-scope.row.reimburseStock)/scope.row.drug.preparation)+(scope.row.drug.pack.name)+(((scope.row.storageStock-scope.row.usedStock-scope.row.reimburseStock)%scope.row.drug.preparation > 0) ? ((scope.row.storageStock-scope.row.usedStock-scope.row.reimburseStock)%scope.row.drug.preparation) + scope.row.drug.preparationUnit.name:""):(scope.row.storageStock-scope.row.usedStock-scope.row.reimburseStock)+scope.row.drug.preparationUnit.name}}
                </template>
                </el-table-column>
            </el-table>
          </div>
        </el-col>
      </el-row>
      <!-- Thanh bảng  Kết thúc -->
        </div>


<!-- Truy vấn thuốc -->
          <div v-else-if="tabPosition==='1' && dispensingList!=null">
            <div class='query-form-container'>
          <el-row class='search-row'>
            <el-form :model='queryModel' @submit.native.prevent label-position="left" label-width='70px' ref='queryForm' :inline-message='true'>
              <el-col :span="6">
                <el-form-item label='Tên thuốc' prop='name'>
                  <el-input
                    style="width:320px;"
                    v-model="queryModel.name"
                    placeholder="Vui lòng nhập tên thuốc hoặc mã pinyin"
                  ></el-input>
                </el-form-item>
              </el-col>

              <el-col :span="6">
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
            <el-col :span="6">
              <el-form-item label='Có kích hoạt không' prop='status'>
                <el-select
                  v-model="queryModel.status"
                  value-key="value"
                  filterable
                  clearable
                  placeholder="Vui lòng chọn có kích hoạt không"
                  class="typeClass"
                >
                  <el-option
                    v-for="type in isEnable"
                    :key="type.value"
                    :label="type.name"
                    :value="type.value"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="6">
                <el-form-item label='Số lượng còn lại' prop='inventory'>
                <el-select
                  v-model="queryModel.inventory"
                  value-key="value"
                  filterable
                  clearable
                  placeholder="Vui lòng chọn số lượng còn lại"
                  class="typeClass"
                >
                  <el-option
                    v-for="type in inventorys"
                    :key="type.value"
                    :label="type.name"
                    :value="type.value"
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
              height="calc(100vh - 300px)"
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

              <el-table-column
              label="Số thứ tự"
              type="index"
              :index="indexMethod"
              align="center">
            </el-table-column>
             <el-table-column prop="goodsName" label="Tên thuốc" > </el-table-column>
              <el-table-column prop="type.name" label="Phân loại thuốc">
              </el-table-column>

              <el-table-column prop="dosis" label="Quy cách">
                <template slot-scope="scope">
                  <template>
                    {{scope.row.dosis}}{{scope.row.dosisUnit.name}}*{{scope.row.preparation}}{{scope.row.preparationUnit.name}}/{{scope.row.pack.name}}
                  </template>
                </template>
                 </el-table-column>
                <el-table-column prop="factory.name" label="Nhà sản xuất" > </el-table-column>
                <el-table-column prop="price" label="Giá bán(VNĐ)" > </el-table-column>
                <el-table-column prop="" label="Tổng giá bán(VNĐ)" >
                    <template slot-scope="scope">
                        {{getPriceTotal(scope.row)}}
                    </template>
                </el-table-column>
                <el-table-column prop="bid" label="Tổng chi phí(VNĐ)" >
                    <template slot-scope="scope">
                        {{scope.row.bid===undefined?0:scope.row.bid}}
                    </template>
                </el-table-column>
              <el-table-column prop="inventory" label="Số lượng còn lại" >
                <template slot-scope="scope">
                  {{Math.floor(scope.row.inventory/scope.row.preparation) >= 0 ? Math.floor(scope.row.inventory/scope.row.preparation)+(scope.row.pack.name)+((scope.row.inventory%scope.row.preparation > 0) ? (scope.row.inventory%scope.row.preparation) + scope.row.preparationUnit.name:""):scope.row.inventory+scope.row.preparationUnit.name}}
                </template>
                 </el-table-column>
            </el-table>
          </div>
        </el-col>
      </el-row>
      <!-- Thanh bảng  Kết thúc -->
          </div>
     </div>

<!-- Cột vật tư -->

    <div v-else>
         <!-- Hiển thị theo số lô và hàng hóa -->
        <div v-if="tabPosition==='0' && stuffList!=null">
             <div class='query-form-container'>
          <el-row class='search-row'>
            <el-form :model='stuffQueryModel' @submit.native.prevent label-position="left" label-width='70px' ref='queryForm' :inline-message='true'>
              <el-col :span="6">
                <el-form-item label='Tên vật tư' prop='name'>
                  <el-input
                    style="width:320px;"
                    v-model="stuffQueryModel.name"
                    placeholder="Vui lòng nhập tên vật tư hoặc mã pinyin"
                  ></el-input>
                </el-form-item>
              </el-col>
               <el-col :span="6">
              <el-form-item label='Nhà cung cấp' prop='type'>
                <el-select
                  v-model="stuffQueryModel.supplier"
                  value-key="value"
                  filterable
                  clearable
                  placeholder="Vui lòng chọn nhà cung cấp"
                  class="typeClass"
                >
                  <el-option
                    v-for="type in stuffSupplierList"
                    :key="type.id"
                    :label="type.name"
                    :value="type.id"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-col>
              <el-col :span="6">
              <el-form-item label='Loại vật tư' prop='type'>
                <el-select
                  v-model="stuffQueryModel.type"
                  value-key="value"
                  filterable
                  clearable
                  placeholder="Vui lòng chọn loại vật tư"
                  class="typeClass"
                >
                  <el-option
                    v-for="type in stuffTypeList"
                    :key="type.value"
                    :label="type.name"
                    :value="type"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label='Có kích hoạt không' prop='status'>
                <el-select
                  v-model="stuffQueryModel.status"
                  value-key="value"
                  filterable
                  clearable
                  placeholder="Vui lòng chọn có kích hoạt không"
                  class="typeClass"
                >
                  <el-option
                    v-for="type in isEnable"
                    :key="type.value"
                    :label="type.name"
                    :value="type.value"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="6">
                <el-form-item label='Số lượng còn lại' prop='inventory'>
                <el-select
                  v-model="stuffQueryModel.inventory"
                  value-key="value"
                  filterable
                  clearable
                  placeholder="Vui lòng chọn số lượng còn lại"
                  class="typeClass"
                >
                  <el-option
                    v-for="type in inventorys"
                    :key="type.value"
                    :label="type.name"
                    :value="type.value"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-col>
            </el-form>
          </el-row>
          <el-row id="conditionOperation">
            <el-col :span="24" style="text-align:right;padding-right:5px">

                <el-button type="primary" icon="el-icon-search" @click='stuffBatchNumber(1)' :plain='true'>Tìm kiếm</el-button>
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
              :data="stuffList"
              height="calc(100vh - 300px)"
              border
              ref="mutipleTable2"
              @sort-change="onSortChange"
              @header-dragend="onChangeWidth"
              :cell-class-name="cellClassName"
              :header-cell-class-name="headerCellClassName"
              highlight-current-row
              show-summary
              :summary-method='getTotal'

            >

              <el-table-column
              label="Số thứ tự"
              type="index"
              :index="indexMethod"
              align="center">
            </el-table-column>
             <el-table-column prop="stuff.name" label="Tên vật tư" >
                </el-table-column>
               <el-table-column prop="stuff.type.name" label="Phân loại vật tư">
              </el-table-column>

              <el-table-column prop="supplierStock.norms" label="Quy cách">
                 </el-table-column>
                <el-table-column prop="stuff.factory.name" label="Nhà sản xuất" > </el-table-column>
                <el-table-column prop="supplierStock.supplierId.name" label="Nhà cung cấp" > </el-table-column>
                <el-table-column prop="supplierStock.batchNo" label="Số lô" > </el-table-column>
                <el-table-column prop="stuff.priceOutSell" label="Giá bán(VNĐ)" > </el-table-column>
                <el-table-column prop="" label="Tổng giá bán(VNĐ)" >
                    <template slot-scope="scope">
                        {{getStuffPriceTotal(scope.row)}}
                    </template>
                </el-table-column>
                <el-table-column prop="supplierStock.bid" label="Giá vốn(VNĐ)" > </el-table-column>
                <el-table-column prop="" label="Tổng chi phí(VNĐ)" >
                    <template slot-scope="scope">
                        {{(scope.row.supplierStock.leastBid*(scope.row.storageStock-scope.row.usedStock-scope.row.reimburseStock)).toFixed(2)}}
                    </template>
                </el-table-column>
              <el-table-column prop="number" label="Số lượng còn lại" >
                  <template slot-scope="scope">
                  {{Math.floor((scope.row.storageStock-scope.row.usedStock-scope.row.reimburseStock)/scope.row.stuff.packNumber) >= 0 ? Math.floor((scope.row.storageStock-scope.row.usedStock-scope.row.reimburseStock)/scope.row.stuff.packNumber)+(scope.row.stuff.packUnit.name)+(((scope.row.storageStock-scope.row.usedStock-scope.row.reimburseStock)%scope.row.stuff.packNumber > 0) ? ((scope.row.storageStock-scope.row.usedStock-scope.row.reimburseStock)%scope.row.stuff.packNumber) + scope.row.stuff.minUnit.name:""):(scope.row.storageStock-scope.row.usedStock-scope.row.reimburseStock)+scope.row.stuff.minUnit.name}}
                </template>
                </el-table-column>
            </el-table>
          </div>
        </el-col>
      </el-row>
      <!-- Thanh bảng  Kết thúc -->
        </div>

<!-- Theo hàng hoá vật tư -->

          <div v-else-if="tabPosition==='1' && stuffList!=null">
            <div class='query-form-container'>
          <el-row class='search-row'>
            <el-form :model='stuffQueryModel' @submit.native.prevent label-position="left" label-width='70px' ref='queryForm' :inline-message='true'>
              <el-col :span="6">
                <el-form-item label='Tên vật tư' prop='name'>
                  <el-input
                    style="width:320px;"
                    v-model="stuffQueryModel.name"
                    placeholder="Vui lòng nhập tên vật tư hoặc mã pinyin"
                  ></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="6">
              <el-form-item label='Loại vật tư' prop='type'>
                <el-select
                  v-model="stuffQueryModel.type"
                  value-key="value"
                  filterable
                  clearable
                  placeholder="Vui lòng chọn loại vật tư"
                  class="typeClass"
                >
                  <el-option
                    v-for="type in stuffTypeList"
                    :key="type.value"
                    :label="type.name"
                    :value="type"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label='Có kích hoạt không' prop='status'>
                <el-select
                  v-model="stuffQueryModel.status"
                  value-key="value"
                  filterable
                  clearable
                  placeholder="Vui lòng chọn có kích hoạt không"
                  class="typeClass"
                >
                  <el-option
                    v-for="type in isEnable"
                    :key="type.value"
                    :label="type.name"
                    :value="type.value"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="6">
                <el-form-item label='Số lượng còn lại' prop='inventory'>
                <el-select
                  v-model="stuffQueryModel.inventory"
                  value-key="value"
                  filterable
                  clearable
                  placeholder="Vui lòng chọn số lượng còn lại"
                  class="typeClass"
                >
                  <el-option
                    v-for="type in inventorys"
                    :key="type.value"
                    :label="type.name"
                    :value="type.value"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-col>
            </el-form>
          </el-row>
          <el-row id="conditionOperation">
            <el-col :span="24" style="text-align:right;padding-right:5px">

                <el-button type="primary" icon="el-icon-search" @click='stuffOnSearch' :plain='true'>Tìm kiếm</el-button>
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
              :data="stuffList"
              height="calc(100vh - 330px)"
              border
              ref="mutipleTable2"
              @sort-change="onSortChange"
              @header-dragend="onChangeWidth"
              :cell-class-name="cellClassName"
              :header-cell-class-name="headerCellClassName"
              highlight-current-row
              show-summary
              :summary-method='getTotal'

            >

              <el-table-column
              label="Số thứ tự"
              type="index"
              :index="indexMethod"
              align="center">
            </el-table-column>
             <el-table-column prop="name" label="Tên vật tư" > </el-table-column>
              <el-table-column prop="type.name" label="Phân loại vật tư">
              </el-table-column>

              <el-table-column prop="specifications" label="Quy cách">
                <template slot-scope="scope">
                  <template>
                    {{scope.row.packNumber}}{{scope.row.minUnit.name}}/{{scope.row.packUnit.name}}
                  </template>
                </template>
                 </el-table-column>
                <el-table-column prop="factory.name" label="Nhà sản xuất" > </el-table-column>
                <el-table-column prop="priceOutSell" label="Giá bán(VNĐ)" > </el-table-column>
                <el-table-column prop="" label="Tổng giá bán(VNĐ)" >
                    <template slot-scope="scope">
                        {{getStuffPriceTotal(scope.row)}}
                    </template>
                </el-table-column>
                <el-table-column prop="bid" label="Tổng chi phí(VNĐ)" >
                    <template slot-scope="scope">
                        {{scope.row.bid===undefined?0:scope.row.bid}}
                    </template>
                </el-table-column>
              <el-table-column prop="inventory" label="Số lượng còn lại" >
                <template slot-scope="scope">
                  {{Math.floor(scope.row.inventory/scope.row.packNumber) >= 0 ? Math.floor(scope.row.inventory/scope.row.packNumber)+(scope.row.packUnit.name)+((scope.row.inventory%scope.row.packNumber > 0) ? (scope.row.inventory%scope.row.packNumber) + scope.row.minUnit.name:""):scope.row.inventory+scope.row.minUnit.name}}
                </template>
                 </el-table-column>
            </el-table>
          </div>
        </el-col>
      </el-row>
      <!-- Thanh bảng  Kết thúc -->
          </div>
    </div>

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
import { validatenull } from "@/utils/validate";
import MainUI from "@/views/components/mainUI";
import {listSupplierAll} from "@/api/stock/supplier"
import { getDictItemsByCode, DICT_CODE } from '@/utils/dictCache'
import { BigNumber } from "bignumber.js";
import { getDrug,getDrugSalesStat,getBatchNumberDrug,getStuff,getStuffSalesStat,getBatchNumberStuff,getDrugSalesStatByNumber,getStuffSalesStatNumber,exportTable} from "@/api/stock/currentInventory";
import { downloadBlob } from '@/utils/downloadBlob'
export default {
  extends: MainUI,
  data() {
    return {
      tabPosition:'1',
      queryModel: {
        name:'',
        type:'',
        status:'1',
        inventory:"1",
        supplier:"",
      },
      stuffQueryModel:{
        name:'',
        type:'',
        status:'1',
        inventory:"1",
        supplier:"",
      },
      activeName:'0',  //Chuyển đổi thuốc/vật tư
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
      allTotal:{}, //Tổng cộng
      type_List: [], // Loại thuốc
      queryTypes: {
        goods_name: "like",
        type: "=",
        bar_code: "like",
      },
      isEnable:[
          {
              value:"0",
              name:"Không"
          },
          {
              value:"1",
              name:"Có"
          }
      ],
      inventorys:[
          {
              value:"0",
              name:"Bằng0"
          },
          {
              value:"1",
              name:"Lớn hơn0"
          }
      ],
      stuffList:[],  //Tập vật tư
      stuffSearch:{
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
            columnName: "is_out_sell",
            queryType: "=",
            value: '1',
          }
        ],
      },
      stuffTypeList:[],   //Loại vật tư
      supplierList:[],   //Nhà cung cấp thuốc
      stuffSupplierList:[], // Nhà cung cấp vật tư
    };
  },
  methods: {

      //Phương thức hỗ trợ xuất dữ liệu
      doExport(search, filename) {
        exportTable(search).then((res) => {
          downloadBlob(res, filename)
        }).catch((error) => {
          this.outputError(error)
        })
      },

      //Xuất thông tin thuốc
      exportTable() {
        if (this.activeName === '0') {
          if (this.tabPosition === '0') {
            this.doExport({ ...this.search, columnName: '2' }, 'Tồn kho thuốc hiện tại-Theo số lô.xlsx')
          } else {
            this.doExport({ ...this.search, columnName: '1' }, 'Tồn kho thuốc hiện tại-Theo hàng hóa.xlsx')
          }
        } else {
          if (this.tabPosition === '0') {
            this.doExport({ ...this.stuffSearch, columnName: '4' }, 'Tồn kho vật tư hiện tại-Theo số lô.xlsx')
          } else {
            this.doExport({ ...this.stuffSearch, columnName: '3' }, 'Tồn kho vật tư hiện tại-Theo hàng hóa.xlsx')
          }
        }
      },

      //Vật tư theo số lô
      stuffBatchNumber(type,limit,offset){
        this.stuffList =null
        if(type===1){
          this.currentPage=1
        }
        this.stuffSearch = {
          columnName:"",
          limit: type===1?this.pageSize:type===2?limit:limit,
          offset: type===1?this.currentPage - 1:type===2?0:offset,
          order: "",
          params:[
            {
              columnName: "company_id",
              queryType: "=",
              value: currentUser.company.id,
            },
            {
              columnName: "stuff.is_out_sell",
              queryType: "=",
              value: '1',
            },
            {
              columnName: "stuff.type",
              queryType: "=",
              value: validatenull(this.stuffQueryModel.type.value)
                    ? ""
                    : this.stuffQueryModel.type.value,
            },
            {
              columnName: "stuff.status",
              queryType: "=",
              value: validatenull(this.stuffQueryModel.status)
                    ? ""
                    : this.stuffQueryModel.status,
            },
            {
                columnName:"storage_stock",
                queryType:">",
                value:"0"
            },
            {
                columnName:"type",
                queryType:"=",
                value:"2"
            },
          ]
        }

        if(this.stuffQueryModel.name!==undefined && this.stuffQueryModel.name!==''){

             const pattern2 = new RegExp("[A-Za-z]+");
                if (pattern2.test(this.stuffQueryModel.name)) {
                  this.stuffSearch.params.push(
                    {
                      columnName: "stuff.pinyin_code",
                      queryType: "like",
                      value: this.stuffQueryModel.name.toUpperCase(),
                    },

                  )
                } else {
                  this.stuffSearch.params.push(
                    {
                      columnName: "stuff.name",
                      queryType: "like",
                      value: this.stuffQueryModel.name,
                    },

                  )
                }
          }
          if(this.stuffQueryModel.inventory && this.stuffQueryModel.inventory!==''){
              if(this.stuffQueryModel.inventory==='0'){
                   this.stuffSearch.params.push(
                    {
                      columnName: "a.storage_stock-a.used_stock-a.reimburse_stock",
                      queryType: "=",
                      value: 0,
                    })
              }else{
                   this.stuffSearch.params.push(
                    {
                      columnName: "a.storage_stock-a.used_stock-a.reimburse_stock",
                      queryType: ">",
                      value: 0,
                    })
              }
          }
          if(this.tabPosition==='0'&&this.stuffQueryModel.supplier){
            this.stuffSearch.params.push(
                {
                    columnName: "supplierId.supplier_id",
                    queryType: "=",
                    value: this.stuffQueryModel.supplier,
                }
            )
        }
            this.setLoad();
            getBatchNumberStuff(this.stuffSearch).then((res) => {
                if(res.code===100){
                    this.stuffList=res.data.rows
                    this.dispensingTotal = res.data.total
                    getStuffSalesStatNumber(this.stuffSearch).then((res)=>{
                    if(res.code===100){
                        this.allTotal = res.data
                        this.resetLoad();
                    }
                    }).catch(()=>{})
                }
            });
      },

      //Tìm vật tư
      stuffOnSearch(){

        this.currentPage = 1;
        this.stuffSearch = {
          columnName:"",
          limit: this.pageSize,
          offset: this.currentPage - 1,
          order: "",
          params:[
            {
              columnName: "company_id",
              queryType: "=",
              value: currentUser.company.id,
            },
            {
              columnName: "is_out_sell",
              queryType: "=",
              value: '1',
            },
            {
              columnName: "type",
              queryType: "=",
              value: validatenull(this.stuffQueryModel.type.value)
                    ? ""
                    : this.stuffQueryModel.type.value,
            },
            {
              columnName: "status",
              queryType: "=",
              value: validatenull(this.stuffQueryModel.status)
                    ? ""
                    : this.stuffQueryModel.status,
            }
          ]
        }

        if(this.stuffQueryModel.name!==undefined && this.stuffQueryModel.name!==''){

             const pattern2 = new RegExp("[A-Za-z]+");
                if (pattern2.test(this.stuffQueryModel.name)) {
                  this.stuffSearch.params.push(
                    {
                      columnName: "pinyin_code",
                      queryType: "like",
                      value: this.stuffQueryModel.name.toUpperCase(),
                    },

                  )
                } else {
                  this.stuffSearch.params.push(
                    {
                      columnName: "name",
                      queryType: "like",
                      value: this.stuffQueryModel.name,
                    },

                  )
                }
          }
          if(this.stuffQueryModel.inventory && this.stuffQueryModel.inventory!==''){
              if(this.stuffQueryModel.inventory==='0'){
                   this.stuffSearch.params.push(
                    {
                      columnName: "stock.storage_stock-stock.used_stock-stock.reimburse_stock",
                      queryType: "=",
                      value: 0,
                    })
              }else{
                   this.stuffSearch.params.push(
                    {
                      columnName: "stock.storage_stock-stock.used_stock-stock.reimburse_stock",
                      queryType: ">",
                      value: 0,
                    })
              }
          }

          this.stuffInit()
      },

      //Tính tổng giá bán của vật tư
      getStuffPriceTotal(item){
          let price;
          if(this.tabPosition==='0'){
              price = (((item.stuff.priceOutSell)/item.stuff.packNumber)*(item.storageStock-item.usedStock-item.reimburseStock)).toFixed(2)
          }else{

              price = (((item.priceOutSell)/item.packNumber)*item.inventory).toFixed(2)
          }
          return price;
      },

      //Lấy loại khởi tạo của vật tư
      stuffInitOptions(){
          this.tabPosition = '1'
          getDictItemsByCode(DICT_CODE.STUFF_TYPE).then((data) => {
            this.stuffTypeList = data
          })
      },
      stuffInit(){
            let salesSearch={
              offset: 0,
              limit: 20,
              order: "",
              columnName: "",
              params:[
                  {
                    columnName: "company_id",
                    queryType: "=",
                    value: currentUser.company.id,
                  },
                  {
                    columnName: "is_out_sell",
                    queryType: "=",
                    value: '1',
                  },
              ]
          }
            this.setLoad();
            getStuff(this.stuffSearch).then((res) => {
                if(res.code===100){
                    this.stuffList=res.data.rows
                    this.dispensingTotal = res.data.total
                    getStuffSalesStat(salesSearch).then((res)=>{
                    if(res.code===100){
                        this.allTotal = res.data
                        this.resetLoad();
                    }
                    }).catch(()=>{})
                }
            });

             //Lấy nhà cung cấp
            let supplierSearch = {
                order:"",
                params: [
                {
                    columnName: "company_id",
                    queryType: "=",
                    value: currentUser.company.id,
                },
                {
                    columnName: "type",
                    queryType: "=",
                    value: '2',
                }
                ],
            }
                listSupplierAll(supplierSearch).then((res)=>{
                    if(res.code===100){
                        this.stuffSupplierList = res.data
                    }
                }).catch((error)=>{
                    this.$message.error(error)
                })
      },
      //Trạng thái hiển thị thuốc
      changStatus(){

      },

      //Lấy tổng giá bán
      getPriceTotal(item){
          let price;

           if(this.tabPosition==='0'){
               price = ((item.drug.price/item.drug.preparation)*(item.storageStock-item.usedStock-item.reimburseStock)).toFixed(2)
           }else{
               price = ((item.price/item.preparation)*item.inventory).toFixed(2)
           }
        // let price =  item.price * Math.floor(item.inventory/item.preparation-0)
        // if(item.isUnpackSell=='1'){
        //      let retailPrice = item.retailPrice*(item.inventory - (Math.floor(item.inventory/item.preparation-0)*(item.preparation-0)))
        //      price = price + retailPrice
        // }

         return price;
      },
    handleClick(tab){
        this.activeName = tab.name
    },
    addCreateDate(){
       let myDate = new Date();
        let lw = new Date(myDate.getTime() - 1000 * 60 * 60 * 24 * 30); //Chữ số cuối cùng30có thể sửa, 30nghĩa là ngày
        let lastY = lw.getFullYear();
        let lastM = lw.getMonth() + 1;
        let lastD = lw.getDate();
        let housrs= lw.getHours();
        let minutes=lw.getMinutes()
        let seconds=lw.getSeconds()
        let startData =
          lastY +
          "-" +
          (lastM < 10 ? "0" + lastM : lastM) +
          "-" +
          (lastD < 10 ? "0" + lastD : lastD)+
          " "+housrs+":"+minutes+":"+seconds //Ngày của 30 ngày trước
          let returnDate=new Date(startData)
        return returnDate;

    },
    indexMethod(index){
       return (this.currentPage-1)*this.search.limit+index +1;
    },
    getTotal(param){
      let { columns } = param;
      let arr = []
      columns.forEach((column, index) => {
          if (index === 0) {
            arr[index] = 'Tổng cộng';
          }else{
            arr[index] = ''
          }
      })
     if(this.tabPosition==='0'){
         arr[8] = new BigNumber(Number(this.allTotal.totalSellingPrice)).toFormat(2)
         arr[10] = new BigNumber(Number(this.allTotal.totalPrice)).toFormat(2)
     }else{
          //Tổng giá bán
      arr[6] = new BigNumber(Number(this.allTotal.totalSellingPrice)).toFormat(2)

      //Tổng giá nhập
      arr[7] = new BigNumber(Number(this.allTotal.totalPrice)).toFormat(2)
     }
      // // arr[4] = this.allTotal.numberAmount
      // arr[10] = new BigNumber(Number(this.allTotal.priceTotalAmount)).toFormat(2)
      // arr[11] = new BigNumber(Number(this.allTotal.profitAmount)).toFormat(2)
      return arr
    },
    init() {
      this.setLoad();
      getDrug(this.search).then((res) => {
          if(res.code===100){
             this.dispensingList=res.data.rows
            this.dispensingTotal = res.data.total
            getDrugSalesStat(this.search).then((res)=>{
              if(res.code===100){
                  this.allTotal = res.data
                 this.resetLoad();
              }
            }).catch(()=>{})
          }
      });
    },

    //Lấy tồn kho thuốc theo số lô
    drugBatchNumber(type,limit,offset){
        if(type===1){
          this.currentPage =1
        }
        this.search = {
          columnName:"",
          limit: type===1?this.pageSize:type===2?limit:limit,
          offset:type===1?this.currentPage - 1:type===2?0:offset,
          order: "",
          params:[
            {
              columnName: "company_id",
              queryType: "=",
              value: currentUser.company.id,
            },
            {
              columnName: "drug.type",
              queryType: "=",
              value: validatenull(this.queryModel.type.value)
                    ? ""
                    : this.queryModel.type.value,
            },
            {
              columnName: "drug.status",
              queryType: "=",
              value: validatenull(this.queryModel.status)
                    ? ""
                    : this.queryModel.status,
            },
            {
                columnName:"storage_stock",
                queryType:">",
                value:"0"
            },
            {
                columnName:"type",
                queryType:"=",
                value:"1"
            },
          ]
        }

        if(this.queryModel.name!==undefined && this.queryModel.name!==''){

             const pattern2 = new RegExp("[A-Za-z]+");
                if (pattern2.test(this.queryModel.name)) {
                  this.search.params.push(
                    {
                      columnName: "drug.pinyin_code",
                      queryType: "like",
                      value: this.queryModel.name.toUpperCase(),
                    },

                  )
                } else {
                  this.search.params.push(
                    {
                      columnName: "drug.goods_name",
                      queryType: "like",
                      value: this.queryModel.name,
                    },

                  )
                }
          }
          if(this.queryModel.inventory && this.queryModel.inventory!==''){
              if(this.queryModel.inventory==='0'){
                   this.search.params.push(
                    {
                      columnName: "a.storage_stock-a.used_stock-a.reimburse_stock",
                      queryType: "=",
                      value: 0,
                    })
              }else{
                   this.search.params.push(
                    {
                      columnName: "a.storage_stock-a.used_stock-a.reimburse_stock",
                      queryType: ">",
                      value: 0,
                    })
              }
            }
        if(this.tabPosition==='0'&&this.queryModel.supplier){
            this.search.params.push(
                {
                    columnName: "supplierId.supplier_id",
                    queryType: "=",
                    value: this.queryModel.supplier,
                }
            )
        }

        this.setLoad();
     getBatchNumberDrug(this.search).then((res) => {
          if(res.code===100){
             this.dispensingList=res.data.rows
            this.dispensingTotal = res.data.total
            getDrugSalesStatByNumber(this.search).then((res)=>{
              if(res.code===100){
                  this.allTotal = res.data
                 this.resetLoad();
              }
            }).catch(()=>{})
           }
      });
    },

    onSearch() {
        this.currentPage = 1;
        this.search = {
          columnName:"",
          limit: this.pageSize,
          offset: this.currentPage - 1,
          order: "",
          params:[
            {
              columnName: "company_id",
              queryType: "=",
              value: currentUser.company.id,
            },
            {
              columnName: "type",
              queryType: "=",
              value: validatenull(this.queryModel.type.value)
                    ? ""
                    : this.queryModel.type.value,
            },
            {
              columnName: "status",
              queryType: "=",
              value: validatenull(this.queryModel.status)
                    ? ""
                    : this.queryModel.status,
            }
          ]
        }

        if(this.queryModel.name!==undefined && this.queryModel.name!==''){

             const pattern2 = new RegExp("[A-Za-z]+");
                if (pattern2.test(this.queryModel.name)) {
                  this.search.params.push(
                    {
                      columnName: "pinyin_code",
                      queryType: "like",
                      value: this.queryModel.name.toUpperCase(),
                    },

                  )
                } else {
                  this.search.params.push(
                    {
                      columnName: "goods_name",
                      queryType: "like",
                      value: this.queryModel.name,
                    },

                  )
                }
          }
          if(this.queryModel.inventory && this.queryModel.inventory!==''){
              if(this.queryModel.inventory==='0'){
                   this.search.params.push(
                    {
                      columnName: "stock.storage_stock-stock.used_stock-stock.reimburse_stock",
                      queryType: "=",
                      value: 0,
                    })
              }else{
                   this.search.params.push(
                    {
                      columnName: "stock.storage_stock-stock.used_stock-stock.reimburse_stock",
                      queryType: ">",
                      value: 0,
                    })
              }
          }

        this.init();
    },
    resetCondition(){
      if(this.activeName==='0'){
        this.queryModel = {
        name:'',
        type:'',
        status:'',
        inventory:'',
        supplier:"",
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
          }
        ],
      }
     if(this.tabPosition==='0'){
         this.drugBatchNumber(1)
     }else{
          this.init()
     }
      }else{
          this.stuffQueryModel = {
                name:'',
                type:'',
                status:'',
                inventory:'',
          }
          this.currentPage = 1;
          this.stuffSearch = {
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
                    columnName: "is_out_sell",
                    queryType: "=",
                    value: '1',
                    }
                ],
          }
           if(this.tabPosition==='0'){
                this.currentPage = 1
                this.stuffBatchNumber(1)
             }else{
                this.stuffInit()
             }
      }
    },
    onSizeChange(val) {
      this.currentPage = 1;

      if(this.activeName==='0'){
         this.search.limit = val;
         this.search.offset = (this.currentPage - 1) * val;
         if(this.tabPosition==='0'){

           this.drugBatchNumber(2,this.search.limit,0)
         }else{
            this.init();
         }
      }else{
        this.stuffSearch.limit = val;
        this.stuffSearch.offset = (this.currentPage - 1) * val;
         if(this.tabPosition==='0'){
           this.stuffBatchNumber(2,this.stuffSearch.limit,0);
         }else{
            this.stuffInit()
         }
      }
    },
    onCurrentChange(val) {

      this.currentPage = val;
       if(this.activeName==='0'){
           this.search.offset = (val - 1) * this.search.limit;
           if(this.tabPosition==='0'){
           this.drugBatchNumber(3,this.search.limit,this.search.offset)
         }else{
            this.init();
         }
      }else{
        this.stuffSearch.offset = (val-1) * this.stuffSearch.limit
          if(this.tabPosition==='0'){
           this.stuffBatchNumber(3,this.stuffSearch.limit,this.stuffSearch.offset);
         }else{
            this.stuffInit()
         }
      }
    },
    initOptions() {
      this.tabPosition = '1'
      getDictItemsByCode(DICT_CODE.MEDICAL_TYPE).then((data) => {
        this.type_List = data;
      });

      //Lấy nhà cung cấp
      let supplierSearch = {
        order:"",
        params: [
          {
            columnName: "company_id",
            queryType: "=",
            value: currentUser.company.id,
          },
          {
            columnName: "type",
            queryType: "=",
            value: '1',
          }
        ],
      }
        listSupplierAll(supplierSearch).then((res)=>{
            if(res.code===100){
                this.supplierList = res.data
            }
        }).catch((error)=>{
            this.$message.error(error)
        })
      this.onSearch();
    },
    bigNum(num){
      if(num||num==='0'){
        return new BigNumber(num).toFormat(2)
      }else{
        return ''
      }
    }
  },
  updated(){
    if(this.activeName==='0'){
      if(this.$refs.mutipleTable1){
        this.$nextTick(() => {
                  // tableRefCóel-tableĐã liên kếtrefGiá trị thuộc tính
                  this.$refs.mutipleTable1.doLayout()// Đối với Table Thực hiện bố cục lại
              })
      }
    }else{
      if(this.$refs.mutipleTable2){
        this.$nextTick(() => {
                  // tableRefCóel-tableĐã liên kếtrefGiá trị thuộc tính
                  this.$refs.mutipleTable2.doLayout()// Đối với Table Thực hiện bố cục lại
              })
      }
    }
  },
  watch: {
     tabPosition:{
          handler() {
            //  this.dispensingList=[]
            //  this.stuffList=[]
            if(this.activeName==='0'){
              this.dispensingList = null
                //Lấy theo số lô thuốc
                if(this.tabPosition==="0"){
                    this.currentPage = 1
                    this.drugBatchNumber(1)
                }else{
                    this.onSearch()
                }
          }else{
              this.stuffList = null
              if(this.tabPosition === '0'){
                this.currentPage = 1
                  this.stuffBatchNumber(1)
              }else{
                  this.stuffOnSearch()
              }
          }
        },
     },
      // tableDataCóel-tableDữ liệu đã liên kết
      tableData: {
          // Khắc phục lỗi hiển thị lệch bảng
          handler () {
              this.$nextTick(() => {
                  // tableRefCóel-tableĐã liên kếtrefGiá trị thuộc tính
                  this.$refs.mutipleTable1.doLayout()// Đối với Table Thực hiện bố cục lại
              })
          },
          deep: true
      },
      activeName:{
          handler(newVal){
              this.queryModel = {
                name:'',
                type:'',
                status:'',
                inventory:"",
              }

              this.stuffQueryModel = {
                name:'',
                type:'',
                status:'',
                inventory:"",
              }
              this.tabPosition = '1'
              if(newVal==='0'){
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
                        }
                    ],
                    }
                  this.initOptions();
                  this.init();
              }else{
                  this.currentPage = 1;
                    this.stuffSearch = {
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
                            columnName: "is_out_sell",
                            queryType: "=",
                            value: '1',
                            }
                        ],
                    }
                  this.stuffInitOptions()
                  this.stuffInit()
              }
          }
      }
  },
  mounted() {
    // initOptions() đã gọi onSearch() -> init() ở cuối, nên chỉ cần gọi
    // initOptions() một lần; gọi thêm init()/onSearch() ở đây khiến trang
    // gửi trùng 3 lần cùng một yêu cầu getDrug khi mở trang.
    this.initOptions();
    //this.drugBatchNumber(1)
    //this.stuffBatchNumber(1)
    // this.stuffOnSearch()
  },
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
    width: 90% !important;
    input{
      width: 90% !important;
      padding-right: 0px;
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
// Cài đặt bảngbodyChiều cao của
 ::v-deep.el-table__body-wrapper {
  //Khắc phục hiển thị dữ liệu vượt quábodychiều cao không cuộn đượcbug
  overflow-y: auto;
   // Phần trừ đi là bảngheaderChiều cao của
   height: calc(100% - 44px) !important;
 }
</style>
