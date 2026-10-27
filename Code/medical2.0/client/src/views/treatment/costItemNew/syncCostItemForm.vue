<template>
  <el-dialog :title="dialogProps.title" :visible.sync="dialogProps.visible" :close-on-click-modal="false" width="90%"
    v-loading="loading" :destroy-on-close="true">
    <el-row v-loading="loading">
      <costItem-form ref="costItemForm" :permission="permission" v-on:save-finished="pageInit"></costItem-form>
      <el-card class="page-container">
        <div class="query-form-container">
          <el-row v-if="!moreCodition" class="search-row">
            <el-form :model="queryModel" @submit.native.prevent label-position="left" label-width="70px" ref="queryForm"
              :inline-message="true">
              <el-col :span="6">
                <el-form-item label="Tên hạng mục" prop="itemName">
                  <el-input v-model="queryModel.itemName" :clearable="true" placeholder="Vui lòng nhập tên hạng mục"></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="3" style="display:flex;justivy-content:space-around">
                <el-button type="primary" icon="el-icon-search" @click="onSearch()" :plain="true">Tìm kiếm</el-button>
                <el-button type="info" icon="el-icon-refresh-left" @click="reset" :plain="true">Đặt lại</el-button>
              </el-col>
              <el-col :span="4" style="text-align:right;padding-right:5px">
                <!-- <el-button-group>
                  <el-button v-show="permission.add" type="primary" icon="el-icon-plus"
                    @click="onCreateCostItem()">Thêm</el-button>
                </el-button-group> -->
              </el-col>
            </el-form>
          </el-row>
          <QueryForm v-else v-model="moreParm" :tableId="tableId" :schemeId="schemeId" :routerId="$route.meta.routerId"
            @search="onSearch()" @moreCodition="onMoreCodition()"></QueryForm>
        </div>

        <el-row>
          <el-col :span="24">
            <div style="display: flex;align-items: center;">
              <span style="width: 100%;">Đã chọn{{ multipleSelection.length }}dòng</span>
            <div style="display: flex;margin-bottom: 10px;width: 100%;justify-content: flex-end;align-items: center;">
              <div style="width: 80px;">Nguồn dữ liệu</div>
              <el-select @change="handleSource" v-model="source">
                <el-option label="Đơn vị thuê" value="1"></el-option>
                <el-option label="Bản phòng khámHis" value="2"></el-option>
              </el-select>
            </div>
            </div>
            <div @mouseleave="moveTableOutside">
              <el-table class="drag_table" :data="costItemList" :row-key="getRowKey" border :height="tableHeight" @sort-change="onSortChange"
                @header-dragend="onChangeWidth" :cell-class-name="cellClassName"
                :header-cell-class-name="headerCellClassName" highlight-current-row ref="tableRef"
                @selection-change="handleSelectionChange">
                <el-table-column type='selection' :reserve-selection="true" width='50' align='center' :selectable="selectable">
                </el-table-column>
                <el-table-column label="Số thứ tự" type="index" :index="indexMethod" align="center">
                </el-table-column>
                <el-table-column v-for="item in columnList"  :key="item.label"  :label="item.label" :prop="item.prop"></el-table-column>
                <!-- <el-table-column label="Thao tác" header-align="center" :width="130 + 'px'"
                  :key="Math.random()">
                  <template slot="header" slot-scope="scope">
                    <span>Thao tác</span>
                    <view-columns-select v-model="columnViews" v-on:save-column-view="saveColumn"
                      v-on:show-all-column="showAllColumn"
                      v-on:show-default-column="showDefaultColumn"></view-columns-select>
                    <export-excel-button v-show="permission.export" :data="costItemList" :tHeader="getHeads()"
                      :filterVal="getFilterVal()" :plain="true"></export-excel-button>
                  </template>
                  <template slot-scope="scope">
                    <OperationIcon v-show="permission.view" type="info" content="Xem" placement="top-start"
                      icon-name="el-icon-view" @click="onViewCostItem(scope.$index, scope.row)"></OperationIcon>
                    <OperationIcon v-show="permission.edit" type="primary" content="Sửa" placement="top-start"
                      icon-name="el-icon-edit" @click="onEditCostItem(scope.$index, scope.row)"></OperationIcon>
                    <OperationIcon v-show="permission.add" type="primary" content="Sao chép" placement="top-start"
                      icon-name="el-icon-document" @click="onCopyCostItem(scope.$index, scope.row)"></OperationIcon>
                    <OperationIcon v-show="permission.view" type="info" content="Lịch sử" placement="top-start"
                      icon-name="el-icon-info" @click="onShowHistory(scope.$index, scope.row)"></OperationIcon>
                  </template>
                </el-table-column> -->
              </el-table>
            </div>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-pagination background @size-change="onSizeChange" @current-change="onCurrentChange"
              :current-page.sync="currentPage" :page-sizes="[20, 50, 100, costItemTotal]" :page-size="20"
              layout="total, sizes, prev, pager, next, jumper" :total="costItemTotal">
            </el-pagination>
          </el-col>
        </el-row>
      </el-card>
    </el-row>
    <span slot='footer' class='dialog-footer'>
      <el-button :disabled="flag" type='primary' :plain='true' @click='onSubmit()'>Đồng bộ</el-button>
      <el-button v-if='dialogProps.action !== "view"' :plain='true' @click='onDialogClose()'>Hủy</el-button>
    </span>
  </el-dialog>
</template>
<script>
import { validatenull } from "@/utils/validate";
import {
  listCostItemPageByParent,
  getCostItemById,
  deleteCostItem,
  listCostItemPageByInstitution,
  listCostItemPageByInstitutionAll,
  listCostItemPageByInstitutionSync
} from "@/api/treatment/costItem";
import { listResourcePermission } from "@/api/admin/common/permission";
import CostItemForm from "./costItemForm";
import { listDictItemAll } from "@/api/sys/dictItem";
import { getDictItemsByCode, DICT_CODE } from '@/utils/dictCache'
import { listCostItemAll } from "@/api/treatment/costItem";
import ExportExcelButton from "@/components/ExportExcelButton";
import ViewColumnsSelect from "@/views/components/ViewColumnsSelect";
import QueryForm from "@/views/components/queryForm";
import MainUI from "@/views/components/mainUI";
import OperationIcon from "@/components/OperationIcon";
import History from "@/views/components/history";


export default {
  extends: MainUI,
  name: "stuff-form",
  components: {
    ExportExcelButton,
    ViewColumnsSelect,
    QueryForm,
    OperationIcon,
    History,
    CostItemForm
  },
  data() {
    return {
      dialogProps: {
        visible: false,
        action: '',
        title: '',
      },
      permission: {
        view: false,
        add: false,
        edit: false,
        remove: false,
        export: false,
      },
      costItemDTO: {
        response: {},
        res: {}
      },//Sửa đóng gói đối tượng thao tác
      queryTypes: {
        item_name: "like",
        item_type: "=",
        is_use: "=",
      },
      queryModel: {
        itemName: "", // Tên hạng mục
        itemType: {
          // Loại hạng mục
          value: "",
          name: "",
        },
        isUse: "", // Trạng thái
      },
      tableHeight: "",//Chiều cao bảng
      isUse: [
        {
          value: '0',
          label: 'Không'
        },
        {
          value: '1',
          label: 'Có'
        },
      ],//Có kích hoạt không
      search: {
        params: [
          {
            columnName: "company_id",
            queryType: "=",
            value: (function () {
              const user = JSON.parse(sessionStorage.getItem("currentUser"));
              return user.company.id;
            })(),
          },
        ],
        offset: 0,
        limit: 20,
        columnName: "", // Tên trường sắp xếp
        order: "", // Sắp xếp
      },
      subproject: [],//Đóng gói hạng mục con
      costltemSearch: {
        params: [{ columnName: "company_id", queryType: "=", value: "" }],
        offset: 0,
        limit: 100,
        columnName: "", // Tên trường sắp xếp
        order: "", // Sắp xếp
      },
      currentPage: 1,
      costItemTotal: 0,
      costItemList: [],

      itemType_List: [], // Loại hạng mục

      oprColumnWidth: 140, // Độ rộng cột thao tác
      tableId: "998465736089977637",
      schemeId: "998465736089977654",
      multipleSelection: [],
      source: '1',
      columnList: [],
      columnListOne: [
        {
          label: "Mã hạng mục",
          prop: "itemCode",
        },
        {
          label: "Tên hạng mục",
          prop: "itemName",
        },
        {
          label: "Loại hạng mục",
          prop: "itemTypeName",
        },
        {
          label: "Giá vốn",
          prop: "costPrice",
        },
        {
          label: "Giá bán",
          prop: "salePrice",
        },
        {
          label: "Trạng thái kích hoạt",
          prop: "isUseName",
        },
        {
          prop: 'syncNumName',
          label: 'Đã đồng bộ chưa',
        },
        {
          label: "Thông tin ghi chú",
          prop: "remarks",
        },
      ],
      columnListTwo: [
        {
          label: "Tên hạng mục",
          prop: "sfxmmc",
        },
        {
          label: "Pinyin",
          prop: "py",
        },
        {
          label: "Đơn vị",
          prop: "dw",
        },
        {
          label: "Mã hạng mục",
          prop: "sfxmCode",
        },
        {
          label: "Đơn giá bán",
          prop: "dj",
        },
        {
          label: "收费项目类型",
          prop: "dllx",
        },
        {
          prop: 'syncNumName',
          label: 'Đã đồng bộ chưa',
        },
      ]
    };
  },
  methods: {
    getRowKey(row) {
      return row.sfxmId || row.id;
    },
    handleSource(val) {
      this.source = val
      if (this.source === 1) {
        this.columnList = this.columnListOne
      } else {
        this.columnList = this.columnListTwo
      }
      this.search.offset = 0
      this.currentPage = 1
      this.$refs.queryForm.resetFields()
      this.pageInit()
    },
    selectable(row, index) {
      if (row.syncNum === 1) {
        return false
      } else {
        return true
      }
    },
    async onSubmit() {
      if (this.multipleSelection.length === 0) {
        this.$message({
          message: "请选择要同步的项目",
          type: "warning",
        });
        return;
      }
      const functionSync = this.source === 1 ? listCostItemPageByInstitution : listCostItemPageByInstitutionSync
      const result = await functionSync(this.multipleSelection)
      if (result.code === 100) {
        this.$message({
          message: "Đồng bộ thành công",
          type: "success",
        });
        this.onDialogClose()
        this.$emit("save-finished");
      } else {
        this.$message({
          message: result.msg,
          type: "error",
        });
      }
    },
    onDialogClose() {
      this.dialogProps.visible = false;
    },
    handleSelectionChange(val) {
      this.multipleSelection = val;
    },
    //Tính toántablechiều cao(Đặt độngtablechiều cao)
    getTableHeight() {
      let tableH = 240; //Chiều cao cách phía dưới trang
      let tableHeightDetil = window.innerHeight - tableH;
      if (tableHeightDetil <= 300) {
        this.tableHeight = 300;
      } else {
        this.tableHeight = window.innerHeight - tableH;
      }
    },


    indexMethod(index) {
      return (this.currentPage - 1) * this.search.limit + index + 1;
    },
    reset() {
      this.$refs.queryForm.resetFields()
      this.onSearch()
    },
    getCostItemList() {
      this.setLoad();
      /* Tham số truy vấn và quyền dữ liệu */
      this.search.params = [
        {
          columnName: "company_id",
          queryType: "=",
          value: (function () {
            const user = JSON.parse(sessionStorage.getItem("currentUser"));
            return user.company.id;
          })(),
        },
      ];
      if (this.moreCodition) {
        this.search.params = this.search.params.concat(
          this.compositeCondition()
        );
      } else {
        // Tham số truy vấn: Tên hạng mục
        this.search.params.push({
          columnName: this.source === 1 ? "item_name" : 'keyword',
          queryType: "like",
          value: this.queryModel.itemName,
        });
        if (this.queryModel.itemType.value !== "") {
          // Tham số truy vấn: Loại hạng mục
          this.search.params.push({
            columnName: "item_type",
            queryType: "=",
            value: validatenull(this.queryModel.itemType.value)
              ? ""
              : this.queryModel.itemType.value,
          });
        }

        // Tham số truy vấn: Trạng thái
        this.search.params.push({
          columnName: "is_use",
          queryType: "=",
          value: this.queryModel.isUse,
        });
      }
      // Quyền dữ liệu: Hạng mục chi phícost_item
      this.pushDataPermissions(
        this.search.params,
        this.$route.meta.routerId,
        this.tableId
      );
      const functionList = this.source === 1 ? listCostItemPageByParent : listCostItemPageByInstitutionAll
      functionList(this.search)
        .then((responseData) => {
          if (responseData.code === 100) {
            if (this.source === 1) {
            this.costItemList = responseData.data.rows && responseData.data.rows.map(item => {
              item.itemTypeName = item.itemType.name
              item.syncNumName = item.syncNum === 1 ? 'Có' : 'Không'
              item.isUseName = isUse === 1 ? 'Có' : 'Không'
              return item
            })
          } else {
            this.costItemList = responseData.data.rows && responseData.data.rows.map(item => {
              item.syncNumName = item.syncNum === 1 ? 'Có' : 'Không'
              return item
            })
          }
            this.costItemTotal = responseData.data.total;
          } else {
            this.showMessage(responseData);
          }
          this.resetLoad();
        })
        .catch((error) => {
          this.outputError(error);
        });
    },
    onSearch() {
      if (this.moreCodition) {
        this.search.offset = 0;
        this.currentPage = 1;
        this.getCostItemList();
      } else {
        this.$refs["queryForm"].validate((valid) => {
          if (valid) {
            this.search.offset = 0;
            this.currentPage = 1;
            this.getCostItemList();
          } else {
            return false;
          }
        });
      }
    },
    onSizeChange(val) {
      this.currentPage = 1;
      this.search.limit = val;
      this.search.offset = (this.currentPage - 1) * val;
      this.getCostItemList();
    },
    onCurrentChange(val) {
      this.search.offset = (val - 1) * this.search.limit;
      this.currentPage = val;
      this.getCostItemList();
    },
    async pageInit() {

      this.setLoad();
      try {
        this.initOptions(this.queryModel);
        this.search.params = [
          {
            columnName: "company_id",
            queryType: "=",
            value: (function () {
              const user = JSON.parse(sessionStorage.getItem("currentUser"));
              return user.company.id;
            })(),
          },
        ];
        // Quyền dữ liệu: Hạng mục chi phícost_item
        this.pushDataPermissions(
          this.search.params,
          this.$route.meta.routerId,
          this.tableId
        );
        const functionList = this.source === 1 ? listCostItemPageByParent : listCostItemPageByInstitutionAll
        let [listCostItemRespData, listPermissionRespData] = await Promise.all([
          functionList(this.search),
          listResourcePermission(this.$route.meta.routerId),
        ]);
        if (
          listCostItemRespData.code === 100 &&
          listPermissionRespData.code === 100
        ) {
          this.costItemTotal = listCostItemRespData.data.total;
          if (this.source === 1) {
            this.costItemList = listCostItemRespData.data.rows && listCostItemRespData.data.rows.map(item => {
              item.itemTypeName = item.itemType.name
              item.syncNumName = item.syncNum === 1 ? 'Có' : 'Không'
              item.isUseName = isUse === 1 ? 'Có' : 'Không'
              return item
            })
          } else {
            this.costItemList = listCostItemRespData.data.rows && listCostItemRespData.data.rows.map(item => {
              item.syncNumName = item.syncNum === 1 ? 'Có' : 'Không'
              return item
            })
          }

          this.permission.view = listPermissionRespData.data.find((item) => {
            return item.permission === "costItemNew:read";
          });
          this.permission.export = listPermissionRespData.data.find((item) => {
            return item.permission === "costItemNew:export";
          });
          this.permission.add = listPermissionRespData.data.find((item) => {
            return item.permission === "costItemNew:create";
          });
          this.permission.edit = listPermissionRespData.data.find((item) => {
            return item.permission === "costItemNew:update";
          });
          this.permission.remove = listPermissionRespData.data.find((item) => {
            return item.permission === "costItemNew:delete";
          });
        } else {
          this.showMessage(
            listPermissionRespData.code !== 100
              ? listPermissionRespData
              : listCostItemRespData
          );
        }
        this.resetLoad();
      } catch (error) {
        this.outputError(error);
      }
    },
    onViewCostItem(index, row) {
      this.setLoad();
      getCostItemById(row.id)
        .then((responseData) => {
          if (responseData.code === 100) {
            this.$refs.costItemForm.$emit(
              "openViewCostItemDialog",
              responseData.data
            );
          } else {
            this.showMessage(responseData);
          }
          this.resetLoad();
        })
        .catch((error) => {
          this.outputError(error);
        });
    },
    onCreateCostItem() {
      //  this.costltemSearch.params = [
      //   {
      //     columnName: "company_id",
      //     queryType: "=",
      //     value: this.company_id,
      //   },
      // ];
      this.search.params = [
        {
          columnName: "company_id",
          queryType: "=",
          value: (function () {
            const user = JSON.parse(sessionStorage.getItem("currentUser"));
            return user.company.id;
          })(),
        },
      ];
      listCostItemAll(this.search).then((res) => {
        if (res.code === 100) {
          // this.subproject=res.data
          this.$refs.costItemForm.$emit("openAddCostItemDialog", res.data);
        }
      }).catch(() => { })

    },
    onEditCostItem(index, row) {
      this.setLoad();
      getCostItemById(row.id)
        .then((responseData) => {
          if (responseData.code === 100) {

            this.search.params = [
              {
                columnName: "company_id",
                queryType: "=",
                value: (function () {
                  const user = JSON.parse(sessionStorage.getItem("currentUser"));
                  return user.company.id;
                })(),
              },
            ];
            listCostItemAll(this.search).then((res) => {
              if (res.code === 100) {
                // this.subproject=res.data
                this.costItemDTO.response = responseData.data
                this.costItemDTO.res = res.data

                this.$refs.costItemForm.$emit(
                  "openEditCostItemDialog",
                  this.costItemDTO
                );
              }
            }).catch(() => { })

          } else {
            this.showMessage(responseData);
          }
          this.resetLoad();
        })
        .catch((error) => {
          this.outputError(error);
        });
    },
    onCopyCostItem(index, row) {
      this.setLoad();
      // getCostItemById(row.id)
      //   .then((responseData) => {
      //     if (responseData.code == 100) {
      //       this.$refs.costItemForm.$emit(
      //         "openCopyCostItemDialog",
      //         responseData.data
      //       );
      //     } else {
      //       this.showMessage(responseData);
      //     }
      //     this.resetLoad();
      //   })
      //   .catch((error) => {
      //     this.outputError(error);
      //   });
      getCostItemById(row.id)
        .then((responseData) => {
          if (responseData.code === 100) {

            this.search.params = [
              {
                columnName: "company_id",
                queryType: "=",
                value: (function () {
                  const user = JSON.parse(sessionStorage.getItem("currentUser"));
                  return user.company.id;
                })(),
              },
            ];
            listCostItemAll(this.search).then((res) => {
              if (res.code === 100) {
                // this.subproject=res.data
                this.costItemDTO.response = responseData.data
                this.costItemDTO.res = res.data

                this.$refs.costItemForm.$emit(
                  "openCopyCostItemDialog",
                  this.costItemDTO
                );
              }
            }).catch(() => { })

          } else {
            this.showMessage(responseData);
          }
          this.resetLoad();
        })
        .catch((error) => {
          this.outputError(error);
        });
    },
    onDeleteCostItem(index, row) {
      this.$confirm("Bạn có chắc muốn xóa không？", "Xác nhận", {
        confirmButtonText: "Xác nhận",
        cancelButtonText: "Hủy",
        type: "warning",
      })
        .then(() => {
          this.setLoad();
          deleteCostItem(row)
            .then((responseData) => {
              if (responseData.code === 100) {
                this.getCostItemList();
                this.showMessage({ type: "success", msg: "Xóa thành công" });
              } else {
                this.showMessage(responseData);
              }
              this.resetLoad();
            })
            .catch((error) => {
              this.outputError(error);
            });
        })
        .catch(() => { });
    },
    onSortChange(orderby) {
      if (validatenull(orderby.prop)) {
        this.search.columnName = "";
        this.search.order = "";
      } else {
        this.search.columnName = orderby.prop;
        this.search.order = orderby.order === "descending" ? "desc" : "asc";
      }

      this.getCostItemList();
    },
    initOptions(This) {
      getDictItemsByCode(DICT_CODE.TREATMENT_ITEM_TYPE).then((data) => {
        this.itemType_List = data;
      });
    },
  },
  watch: {
    // tableDataCóel-tableDữ liệu đã liên kết
    tableData: {
      // Khắc phục lỗi hiển thị lệch bảng
      handler() {
        this.$nextTick(() => {
          // tableRefCóel-tableĐã liên kếtrefGiá trị thuộc tính
          this.$refs.tableRef.doLayout()// Đối với Table Thực hiện bố cục lại
        })
      },
      deep: true
    }
  },
  updated() {
    if (this.$refs.tableRef) {
      this.$nextTick(() => {
        this.$refs.tableRef.doLayout()// Đối với Table Thực hiện bố cục lại
      })
    }
  },
  mounted() {
    if (this.source === 1) {
      this.columnList = this.columnListOne
    } else {
      this.columnList = this.columnListTwo
    }
    this.$nextTick(() => {
      this.$on("openSyncCostItemDialog", () => {
        this.dialogProps.title = "同步诊疗项目";
        this.dialogProps.visible = true;
        this.pageInit()
      });
    });
    this._resizeHandler = () => {
      if (this.resizeFlag) {
        clearTimeout(this.resizeFlag);
      }
      this.resizeFlag = setTimeout(() => {
        this.getTableHeight();
        this.resizeFlag = null;
      }, 100);
    };
    window.addEventListener('resize', this._resizeHandler);
  },
  beforeDestroy() {
    window.removeEventListener('resize', this._resizeHandler);
    if (this.resizeFlag) {
      clearTimeout(this.resizeFlag);
    }
  },
  created() {
    this.getTableHeight();
  }
};
</script>
<style scoped lang="scss">
.line {
  margin-top: -10px;
}

.specifications {
  margin-top: -30px;
  width: 100%;
  font-weight: bold;
  padding-bottom: 8px;
  padding: 20px;
  color: #333;
}

.page-container {
  padding: 0;
}

.drag_table {

  // Cài đặt bảngheaderChiều cao của
  ::v-deep th {
    height: 44px;
  }

  ::v-deep th.gutter:last-of-type {
    height: 0 !important;
  }

  // Cài đặt bảngbodyChiều cao của
  ::v-deep .el-table__body-wrapper {
    //Khắc phục hiển thị dữ liệu vượt quábodychiều cao không cuộn đượcbug
    overflow-y: auto;
    // Phần trừ đi là bảngheaderChiều cao của
    height: calc(100% - 44px) !important;
  }

  .el-table__fixed-right {
    height: 100% !important;
  }
}
</style>

<style>
.stuff_indate .el-dialog__header {
  border-bottom: 1px solid rgb(214, 214, 214) !important;
}
</style>
<style scoped>
.el-table__body-wrapper {
  height: calc(100% - 44px) !important;
}
</style>
