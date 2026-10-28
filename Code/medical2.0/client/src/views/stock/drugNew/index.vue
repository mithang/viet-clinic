<template>
  <el-row v-loading="loading">
    <!-- Lịch sử  -->
    <History :bussObject="curentRow"></History>
    <!-- Cửa sổ chỉnh sửa  -->
    <drug-form
      ref="drugForm"
      :permission="permission"
      v-on:save-finished="loadData"
    ></drug-form>
    <DrugListSynchronization
      ref="drugListSynchronization"
      :permission="permission"
      v-on:sync-finished="handleSync"
    />
    <el-card class="page-container">
      <!--  Thanh tìm kiếm  Bắt đầu -->
      <div class="query-form-container">
        <el-row v-if="!moreCodition" class="search-row">
          <el-form
            :model="queryModel"
            @submit.native.prevent
            label-position="left"
            label-width="70px"
            ref="queryForm"
            :inline-message="true"
          >
            <el-col :span="4">
              <el-form-item label="Tên thuốc" prop="goodsName">
                <el-input
                  v-model="queryModel.goodsName"
                  :clearable="true"
                  placeholder="Vui lòng nhập tên thuốc"
                ></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item label="Loại thuốc" prop="type">
                <el-select
                  v-model="queryModel.type"
                  value-key="value"
                  filterable
                  clearable
                  placeholder="Vui lòng chọn loại thuốc"
                >
                  <el-option
                    v-for="item in type_List"
                    :key="item.value"
                    :label="item.name"
                    :value="item"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item label="Mã vạch" prop="barCode">
                <el-input
                  v-model="queryModel.barCode"
                  :clearable="true"
                  placeholder="Vui lòng nhập mã vạch"
                ></el-input>
              </el-form-item>
            </el-col>
            <!-- <el-col :span="5">

              <el-form-item label="Trạng thái kích hoạt" prop="status">
                <el-switch
                  v-model="queryModel.status"
                  active-color="#13ce66"
                  inactive-color="#dbdfe6"
                  active-value="1"
                  inactive-value="0"
                ></el-switch>
              </el-form-item>
            </el-col> -->
            <el-col :span="4">
              <el-form-item label="Trạng thái kích hoạt" prop="status">
                <el-select
                  v-model="queryModel.status"
                  value-key="value"
                  filterable
                  clearable
                  placeholder="Vui lòng chọn trạng thái kích hoạt"
                >
                  <el-option
                    v-for="item in status"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="3" style="display:flex;justivy-content:space-around">
              <el-button
                type="primary"
                icon="el-icon-search"
                @click="onSearch()"
                :plain="true"
              >Tìm kiếm
              </el-button>
              <el-button
                type="info"
                icon="el-icon-refresh-left"
                @click="reset"
                :plain="true"
              >Đặt lại
              </el-button>
            </el-col>
            <el-col :span="5" style="text-align:right;padding-right:10px">
              <el-button-group>
                <el-dropdown v-show="permission.add">
                  <el-button type="primary">
                     Thêm <i class="el-icon-arrow-down el-icon--right"></i>
                  </el-button>
                  <el-dropdown-menu slot="dropdown">
                    <el-dropdown-item>
                      <el-button type="text" @click="onSynchronousDrug()">Đồng bộ</el-button>
                    </el-dropdown-item>
                    <el-dropdown-item>
                      <el-button type="text" @click="onCreateEntity('drugForm')">Tự tạo</el-button>
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </el-dropdown>
                <el-dropdown v-show="permission.add" style="padding-left:10px">
                    <span class="el-dropdown-link">
                      Thao tác hàng loạt<i class="el-icon-arrow-down el-icon--right"></i>
                    </span>
                  <el-dropdown-menu slot="dropdown">
                    <el-dropdown-item>
                      <el-button type="text" @click="indateClick">Đặt hàng loạt cảnh báo hạn sử dụng</el-button>
                    </el-dropdown-item>
                    <el-dropdown-item>
                      <el-button type="text" @click="inventoryClick">Đặt hàng loạt cảnh báo tồn kho</el-button>
                    </el-dropdown-item>
                    <el-dropdown-item>
                      <el-button type="text" @click="importStudentExcel">Nhập hàng loạt</el-button>
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </el-dropdown>
              </el-button-group>
            </el-col>
          </el-form>
        </el-row>
        <QueryForm
          v-else
          v-model="moreParm"
          :tableId="tableId"
          :schemeId="schemeId"
          :routerId="$route.meta.routerId"
          @search="onSearch()"
          @moreCodition="onMoreCodition()"
        ></QueryForm>
      </div>
      <!--  Thanh tìm kiếm  Kết thúc -->

      <!-- Thanh công cụ Bắt đầu -->

      <!-- Thanh công cụ Kết thúc -->

      <!-- Thanh bảng  Bắt đầu -->
      <el-row>
        <el-col :span="24">
          <div @mouseleave="moveTableOutside">
            <el-table
              class="drag_table"
              :data="drugList"
              border
              min-height="calc(100vh - 254px)"
              @sort-change="onSortChange"
              @header-dragend="onChangeWidth"
              :cell-class-name="cellClassName"
              :header-cell-class-name="headerCellClassName"
              highlight-current-row
              ref="mutipleTable"
            >
              <el-table-column
                label="Số thứ tự"
                type="index"
                :index="indexMethod"
                align="center">
              </el-table-column>
              <el-table-column
                v-for="(cv, index) in columnViews"
                v-if="cv.display"
                :prop="cv.prop"
                :key="cv.prop"
                :label="cv.label"
                sortable="custom"
                :align="cv.align"
                :min-width="cv.miniWidth + 'px'"
                :width="cv.width + 'px'"
                header-align="center"
                :column-key="index.toString()"
                :render-header="renderHeader"
              >
                <template slot-scope="{ row, $index }">
                  <span
                    v-if="
                      columnViews[index].showType === 'Switch' ||
                      columnViews[index].showType === 'Checkbox' ||
                      columnViews[index].showType === 'Radio'
                    "
                  >
                    <li
                      v-if="getAttrValue(row, columnViews[index].prop) === '1'"
                      class="el-icon-check"
                      style="color: #f56c6c"
                    ></li>
                  </span>
                  <span v-else>{{
                      getAttrValue(
                        row,
                        columnViews[index].prop,
                        columnViews[index].javaType
                      )
                    }}</span>
                </template>
              </el-table-column>
              <el-table-column
                prop="stockNumber"
                label="Tồn kho khả dụng"
                sortable="custom"
                width="80px"
              >
                <template slot-scope="scope">
                  <span v-if="scope.row.stock && scope.row.stock.surplusStock != null">
                    {{
                      Math.floor(scope.row.stock.surplusStock / scope.row.preparation) >= 0 ?
                        Math.floor(scope.row.stock.surplusStock / scope.row.preparation) + scope.row.pack.name +
                        ((scope.row.stock.surplusStock % scope.row.preparation > 0) ? (scope.row.stock.surplusStock % scope.row.preparation) +
                          scope.row.preparationUnit.name : "") : scope.row.stock.surplusStock + scope.row.preparationUnit.name
                    }}
                  </span>
                  <span v-else>0</span>
                </template>
              </el-table-column>
              <el-table-column
                prop="stockNumber"
                label="Quy cách"
                sortable="custom"
                width="120px"
              >
                <template slot-scope="scope">
                  <span>{{
                      `${scope.row.dosis}${scope.row.dosisUnit.name}*${scope.row.preparation}${scope.row.preparationUnit.name}/${scope.row.pack.name}`
                    }}</span>
                </template>
              </el-table-column>
              <!-- <el-table-column
                prop="inventory"
                label="Tồn kho"
                sortable="custom"
                width="80px"
              >
              <template slot-scope="scope">
                <span>
                  {{
                    Math.floor(scope.row.inventory / scope.row.preparation) >= 0 ?
                           Math.floor(scope.row.inventory / scope.row.preparation) + scope.row.pack.name + ((scope.row.inventory % scope.row.preparation > 0) ? (scope.row.inventory % scope.row.preparation) +  scope.row.preparationUnit.name: "") : scope.row.inventory +  scope.row.preparationUnit.name
                  }}
                </span>
              </template>
              </el-table-column> -->
              <!-- <el-table-column
                prop="stockUnit"
                label="Đơn vị"
                sortable="custom"
                width="80px"
              >
              </el-table-column> -->

              <!--Nút thao tác theo dòng-->
              <el-table-column
                label="Thao tác"
                header-align="center"
                width="120px"
              >
                <template slot="header" slot-scope="scope">
                  <span>Thao tác</span>
                  <view-columns-select
                    v-model="columnViews"
                    v-on:save-column-view="saveColumn"
                    v-on:show-all-column="showAllColumn"
                    v-on:show-default-column="showDefaultColumn"
                  ></view-columns-select>
                  <export-excel-button
                    v-show="permission.export"
                    :data="drugList"
                    :tHeader="getHeads()"
                    :filterVal="getFilterVal()"
                    :plain="true"
                  ></export-excel-button>
                </template>
                <template slot-scope="scope">
                  <OperationIcon
                    v-show="permission.view"
                    type="info"
                    content="Xem"
                    placement="top-start"
                    icon-name="el-icon-view"
                    @click="onViewEntity(scope.$index, scope.row, 'drugForm')"
                  ></OperationIcon>
                  <OperationIcon
                    v-show="permission.edit"
                    type="primary"
                    content="Sửa"
                    placement="top-start"
                    icon-name="el-icon-edit"
                    @click="onEditEntity(scope.$index, scope.row, 'drugForm')"
                  ></OperationIcon>
                  <OperationIcon
                    v-show="permission.add"
                    type="primary"
                    content="Sao chép"
                    placement="top-start"
                    icon-name="el-icon-document"
                    @click="onCopyEntity(scope.$index, scope.row, 'drugForm')"
                  ></OperationIcon>
                  <OperationIcon
                    v-show="permission.remove"
                    type="danger"
                    content="Xóa"
                    placement="top-start"
                    icon-name="el-icon-delete"
                    @click="onDeleteEntity(scope.$index, scope.row, deleteApi)"
                  ></OperationIcon>
                  <OperationIcon
                    v-show="permission.view"
                    type="info"
                    content="Lịch sử"
                    placement="top-start"
                    icon-name="el-icon-info"
                    @click="onShowHistory(scope.$index, scope.row)"
                  ></OperationIcon>
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
            :page-sizes="[20, 50, 100, drugTotal]"
            :page-size="20"
            layout="total, sizes, prev, pager, next, jumper"
            :total="drugTotal"
          >
          </el-pagination>
        </el-col>
      </el-row>
      <!-- Thanh phân trang     Kết thúc -->
    </el-card>

    <!-- Đặt hàng loạt cảnh báo hạn sử dụng -->
    <div class="indate">
      <el-dialog
        title="Đặt hàng loạt hạn sử dụng"
        :visible.sync="indateDialogVisible"
        width="30%"
        :before-close="handleClose">
        <!-- <el-divider style="margin-top:-30px;"></el-divider> -->
        <div style="padding-bottom:10px;margin-top:-20px">
          <el-alert
            title="Sau khi đặt hàng loạt sẽ sửa thời gian cảnh báo hạn sử dụng của tất cả thuốc"
            :closable="false"
            type="warning">
          </el-alert>
        </div>
        <span>Thời gian cảnh báo hạn sử dụng của thuốc là</span>
        <el-input v-model="indate" oninput="value=value.replace(/[^\d.]/g,'')">
          <template slot="append">
            ngày
          </template>
        </el-input>
        <span slot="footer" class="dialog-footer">
        <el-button @click="indateSave(0)">Hủy</el-button>
        <el-button type="primary" @click="indateSave(1)">Xác nhận</el-button>
      </span>
      </el-dialog>

      <!-- Cảnh báo tồn kho -->
      <el-dialog
        title="Đặt hàng loạt cảnh báo tồn kho"
        :visible.sync="inventoryDialogVisible"
        width="30%"
        :before-close="handleCloseInventory">
        <!-- <el-divider style="margin-top:-30px;"></el-divider> -->
        <div style="padding-bottom:10px;margin-top:-20px">
          <el-alert
            title="Sau khi đặt hàng loạt sẽ sửa số lượng cảnh báo tồn kho của tất cả thuốc（Cảnh báo theo đơn vị đóng gói）"
            :closable="false"
            type="warning">
          </el-alert>
        </div>
        <span>Số lượng cảnh báo tồn kho của thuốc là</span>
        <el-input v-model="inventoryFloor" oninput="value=value.replace(/[^\d.]/g,'')">
        </el-input>
        <span slot="footer" class="dialog-footer">
        <el-button @click="inventorySave(0)">Hủy</el-button>
        <el-button type="primary" @click="inventorySave(1)">Xác nhận</el-button>
      </span>
      </el-dialog>

      <!-- Hộp thoại hiện sau khi nhấn nhập -->
      <el-dialog title="Nhập thuốc" :visible.sync="importDialogVisible" width="45%" :before-close="cancellation">
        <input ref="file" type="file" accept=".xlsx,.xls" style="display: none;" @change="uploadFile">
        <el-button type="primary" icon="el-icon-upload" @click="clickFile">Chọn tệp</el-button>
        <el-button type="primary" icon="el-icon-download" @click="downloadExcel">Tải mẫu xuống</el-button>
        <el-button @click="cancellation()">Hủy</el-button>
      </el-dialog>

      <el-dialog title="Lỗi nhập thông tin thuốc" :visible.sync="dialogVisible" width="36%" :before-close="mistakeClose">
        <span> Nhập dữ liệu thành công<span style="white-space:pre;color:#08ff00;font-size: 16px">{{chengGong}}</span>dòng&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </span>
        <span> Nhập dữ liệu thất bại<span style="white-space:pre;color:red;font-size: 16px">{{shiBai}}</span>dòng </span><br>
        <span style="white-space:pre;color:red;font-size: 15px">{{mistake}}</span>
        <span slot="footer" class="dialog-footer">
          <el-button @click="dialogVisible = false">Hủy</el-button>
          <el-button type="primary" @click="dialogVisible = false">Xác nhận</el-button>
        </span>
      </el-dialog>
    </div>
  </el-row>
</template>

<script>
  import {validatenull} from "@/utils/validate";
  import {
    listByCompanyDrugPage,
    getDrugById,
    deleteDrug,
    updateAllIndate,
    updateAllInventory,
    inventory,
    uploadExcel
  } from "@/api/stock/drug";
  import listViewMixin from '@/mixins/listViewMixin'
  import DrugForm from "./drugForm";
  import {listDictItemAll} from "@/api/sys/dictItem";
  import { getDictItemsByCode, DICT_CODE } from '@/utils/dictCache'
  import ExportExcelButton from "@/components/ExportExcelButton";
  import ViewColumnsSelect from "@/views/components/ViewColumnsSelect";
  import QueryForm from "@/views/components/queryForm";
  import MainUI from "@/views/components/mainUI";
  import OperationIcon from "@/components/OperationIcon";
  import History from "@/views/components/history";
  import {getLocalCurrentCompany} from "@/utils/auth";
  import fileurl from "@/assets/file/drug.xlsx";
  import DrugListSynchronization from './drugListSynchronization'


  export default {
    extends: MainUI,
    mixins: [listViewMixin],
    components: {
      DrugForm,
      ExportExcelButton,
      ViewColumnsSelect,
      QueryForm,
      OperationIcon,
      History,
      DrugListSynchronization
    },
    data(row) {
      return {
        inventory: null,
        mistake: null,
        chengGong: null,
        shiBai: null,
        dialogVisible: false,
        systemParamConfigSearch: {
          params: []
        },
        listApi: listByCompanyDrugPage,
        getApi: getDrugById,
        deleteApi: deleteDrug,
        entityName: 'Drug',
        permissionPrefix: 'drugNew',
        queryTypes: {
          goods_name: "like",
          type: "=",
          bar_code: "like",
        },
        queryModel: {
          goodsName: "", // Tên thuốc
          type: {
            // Loại thuốc
            value: "",
            name: "",
          },
          barCode: "", // Mã vạch
          status: "1",
        },
        status: [
          {
            value: '0',
            label: 'Không'
          },
          {
            value: '1',
            label: 'Có'
          },
        ],//Có kích hoạt không


        drugTotal: 0,
        drugList: [],

        type_List: [], // Loại thuốc

        oprColumnWidth: 140, // Độ rộng cột thao tác
        tableId: "1004078055755374623",
        schemeId: "1004078055755374645",
        indateDialogVisible: false,  //Hộp thoại đặt hàng loạt hạn sử dụng
        inventoryDialogVisible: false,   //Hộp thoại đặt hàng loạt cảnh báo tồn kho
        importDialogVisible: false, // Biến hộp thoại nhập hàng loạt
        indate: "",   //Hạn sử dụng
        inventoryFloor: ""   //Ngưỡng tồn kho tối thiểu
      };
    },
  methods: {
      // Đồng bộ thông tin thuốc
      handleSync() {
        this.loadData();
        this.$message.success("Đồng bộ thành công")
      },
      handleClose() {
        this.indateSave(0)
      },
      handleCloseInventory() {
        this.inventorySave(0)
      },
      //Lưu đặt hàng loạt hạn sử dụng
      indateSave(index) {
        if (index === 0) {
          this.indateDialogVisible = false
          this.indate = ""
        } else {
          let drug = {
            indate: this.indate,
            company: currentUser.company
          }
          updateAllIndate(drug).then((res) => {
            if (res.code === 100) {
              this.indateDialogVisible = false
              this.indate = ""
              this.loadData();
              this.$message.success("Sửa thành công")
            }
          }).catch((error) => {
            this.$message.error(error)
          })
        }
      },

      //Lưu đặt hàng loạt cảnh báo tồn kho
      inventorySave(index) {
        if (index === 0) {
          this.inventoryDialogVisible = false
          this.inventoryFloor = ""
        } else {
          let drug = {
            inventoryFloor: this.inventoryFloor,
            company: currentUser.company
          }
          updateAllInventory(drug).then((res) => {
            if (res.code === 100) {
              this.inventoryDialogVisible = false
              this.inventoryFloor = ""
              this.loadData();
              this.$message.success("Sửa thành công")
            }
          }).catch((error) => {
            this.$message.error(error)
          })
        }
      },

      // Tải mẫu xuống
      downloadExcel() {
        let a = document.createElement('a');
        let evt = document.createEvent('MouseEvents');
        a.download = 'Thông tin thuốc';
        a.href = fileurl;
        evt.initEvent('click', true, true);
        a.dispatchEvent(evt);
        window.URL.revokeObjectURL(a.href);
      },

      // Đóng thông báo lỗi nhập
      mistakeClose(done) {
        this.$confirm('Xác nhận đóng？')
          .then(_ => {
            done();
          })
          .catch(_ => {
          });
      },

      // Sự kiện nhấn nút nhập hàng loạt
      importStudentExcel() {
        this.importDialogVisible = true;
      },

      // Sự kiện chọn tệp
      clickFile() {
        this.$refs.file.dispatchEvent(new MouseEvent('click'))
      },

      // Tải tệp lên
      async uploadFile() {
        const file = this.$refs.file.files
        const extName = file[0].name.substring(file[0].name.lastIndexOf('.')).toLowerCase()
        if (extName === '.xlsx' || extName === '.xls') {
          const formData = new FormData()
          let id = currentUser.company.id;
          formData.append('file', file[0])
          this.$message.success('Đang nhập dữ liệu，Vui lòng chờ')
          uploadExcel(formData).then((res) => {
            if (res.code === 100) {
              if (res.data[2] === ""){
                this.$message({
                  type: 'success',
                  message: 'Nhập dữ liệu thành công,Đã nhập tổng cộng'+ res.data[0] +'bản ghi!'
                })
                this.cancellation();
              }else {
                this.chengGong = res.data[0]
                this.shiBai = res.data[1]
                this.mistake = res.data[2]
                this.dialogVisible = true
                this.cancellation();
              }

            } else {
              this.$message({
                type: "error",
                message: res.resultJson.msg
              })
              this.cancellation();
            }
          })
        } else {
          this.$message.error('Nhập dữ liệu thất bại，Vui lòng chọn đúngxlsxTệp mẫu')
          this.cancellation();
        }
        this.load();
      },

      // Đóng cửa sổ nhập
      cancellation() {
        this.importDialogVisible = false;
        this.reset();
      },

      //Hộp thoại đặt hàng loạt tồn kho
      inventoryClick() {
        this.inventoryDialogVisible = true
      },

      //Hộp thoại đặt hàng loạt hạn sử dụng
      indateClick() {
        this.indateDialogVisible = true
      },

      appendSearchParams() {
        this.search.params = [
          { columnName: 'company_id', queryType: '=', value: currentUser.company.id }
        ]
        if (this.moreCodition) {
          this.search.params = this.search.params.concat(this.compositeCondition())
        } else {
          this.search.params.push({ columnName: 'goods_name', queryType: 'like', value: this.queryModel.goodsName })
          this.search.params.push({ columnName: 'type', queryType: '=', value: validatenull(this.queryModel.type.value) ? '' : this.queryModel.type.value })
          this.search.params.push({ columnName: 'bar_code', queryType: 'like', value: this.queryModel.barCode })
          this.search.params.push({ columnName: 'status', queryType: '=', value: this.queryModel.status })
        }
        this.pushDataPermissions(this.search.params, this.$route.meta.routerId, this.tableId)
      },
      handleListResponse(responseData) {
        this.drugTotal = responseData.data.total
        this.drugList = responseData.data.rows
      },
      onSynchronousDrug() {
        this.$refs.drugListSynchronization.openMigratingDrugDialog();
      },
      SurplusStock(row) {
        this.systemParamConfigSearch.params = [
          {
            columnName: "company_id",
            queryType: "=",
            value: getLocalCurrentCompany().id
          },
          {
            columnName: "id",
            queryType: "=",
            value: row.id
          }
        ]
        inventory(this.systemParamConfigSearch).then(responseData => {
          if (responseData.code === 100) {
            if (Array.isArray(responseData.data) && responseData.data.length >= 1) {
              responseData.data.forEach(data => {
                this.inventory = data.stock.surplusStock
              })

            }
          } else {
            this.showMessage(responseData)
          }
        }).catch(error => {
          this.outputError(error)
        })
      },
      OpenEdit(index, row) {
        if (this.inventory !== 0) {
          this.$message({
            message: "Vật phẩm còn tồn kho，Không thể sửa",
            type: 'warning'
          })
        } else if (this.inventory === 0) {
          this.setLoad();
          getDrugById(row.id)
            .then((responseData) => {
              if (responseData.code === 100) {
                this.$refs.drugForm.openEditDrugDialog(responseData.data);
              } else {
                this.showMessage(responseData);
              }
              this.resetLoad();
            })
            .catch((error) => {
              this.outputError(error);
            });
        }
        this.inventory = 0
      },
      async onEditDrug(index, row) {
        await this.SurplusStock(row)
        setTimeout(() => {
          this.OpenEdit(index, row)
        }, 1000);
      },
      initOptions(This) {
        getDictItemsByCode(DICT_CODE.MEDICAL_TYPE).then((data) => {
          this.type_List = data;
        });
      },
    },
    watch: {
      drugList(val) {
        if (val) {
          this.$nextTick(() => {
            this.$refs.mutipleTable.doLayout();
          });
        }
      },
      // tableDataCóel-tableDữ liệu đã liên kết
      tableData: {
        // Khắc phục lỗi hiển thị lệch bảng
        handler() {
          this.$nextTick(() => {
            // mutipleTableCóel-tableĐã liên kếtrefGiá trị thuộc tính
            this.$refs.mutipleTable.doLayout()// Đối với Table Thực hiện bố cục lại
          })
        },
        deep: true
      }
    },
  mounted() {
      this.pageInit();
    },
  };
</script>
<style lang="scss" scoped>
  .page-container {
    padding: 0;
  }
  .crimson-text {
      color: crimson;
   }


  .indate ::v-deep .el-dialog__header {
    border-bottom: 1px solid rgb(214, 214, 214) !important;
  }
</style>
