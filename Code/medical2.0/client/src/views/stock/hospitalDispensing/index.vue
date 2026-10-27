<template>
  <el-row v-loading='loading'>
    <el-card class="page-container">
      <!--  Thanh tìm kiếm  Bắt đầu -->
      <div class='query-form-container'>
        <el-row v-if='!moreCodition' class='search-row'>
          <el-form :model='queryModel' @submit.native.prevent label-position="left" label-width='70px' ref='queryForm'
            :inline-message='true'>
            <el-col :span="5">
              <el-form-item label='Tên thuốc' prop='keyword'>
                <el-input v-model='queryModel.keyword' :clearable='true' placeholder='Vui lòng nhập tên vật tư'></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="5">
              <el-form-item label='Đã đồng bộ chưa' prop='zstbzt'>
                <el-select v-model="queryModel.zstbzt" placeholder="Vui lòng chọn có kích hoạt không">
                  <el-option v-for="item in zstbztOptions" :key="item.value" :label="item.label" :value="item.value">
                  </el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label='Thời gian phát thuốc'>
                <el-date-picker v-model="dispensingTime" type="daterange" range-separator="đến" start-placeholder="Ngày bắt đầu"
                  end-placeholder="Ngày kết thúc">
                </el-date-picker>
              </el-form-item>
            </el-col>
            <el-col :span="3" style="display:flex;justivy-content:space-around">
              <el-button type="primary" icon="el-icon-search" @click="onSearch()" :plain="true">Tìm kiếm
              </el-button>
              <el-button type="info" icon="el-icon-refresh-left" @click="reset" :plain="true">Đặt lại
              </el-button>
              <el-button style="margin-right: 10px;" type="primary" icon="el-icon-refresh" @click="onSynchronous()">Đồng bộ
                </el-button>
            </el-col>
          </el-form>
        </el-row>
        <QueryForm v-else v-model='moreParm' :tableId='tableId' :schemeId='schemeId' :routerId='$route.meta.routerId'
          @search='onSearch()' @moreCodition='onMoreCodition()'></QueryForm>
      </div>
      <!--  Thanh tìm kiếm  Kết thúc -->

      <!-- Thanh công cụ Bắt đầu -->

      <!-- Thanh công cụ Kết thúc -->

      <!-- Thanh bảng  Bắt đầu -->
      <el-row>
        <el-col :span='24'>
          <div @mouseleave='moveTableOutside' style="height:">
            <el-table ref="tableRef" height="calc(100vh - 280px)" class='drag_table' :data='stuffList' border
              @sort-change='onSortChange' @header-dragend='onChangeWidth' :cell-class-name='cellClassName'
              :header-cell-class-name='headerCellClassName' highlight-current-row @selection-change="handleSelectionChange">
              <el-table-column type='selection' :reserve-selection="true" width='50' align='center'
                :selectable="selectable">
              </el-table-column>
              <el-table-column label="Số thứ tự" type="index" :index="indexMethod" align="center">
              </el-table-column>
              <el-table-column v-for="item in columnList" :label="item.label" :prop="item.prop"></el-table-column>
            </el-table>
          </div>
        </el-col>
      </el-row>
      <!-- Thanh bảng  Kết thúc -->
      <!-- Thanh phân trang     Bắt đầu -->
      <el-row>
        <el-col :span='24'>
          <el-pagination background @size-change='onSizeChange' @current-change='onCurrentChange'
            :current-page.sync='currentPage' :page-sizes='[20, 50, 100, stuffTotal]' :page-size='20'
            layout='total, sizes, prev, pager, next, jumper' :total='stuffTotal'>
          </el-pagination>
        </el-col>
      </el-row>
      <!-- Thanh phân trang     Kết thúc -->
    </el-card>
  </el-row>
</template>

<script>
import { validatenull } from '@/utils/validate'
import { listInstitutionDispensingPage, syncInstitutionDispensing } from '@/api/stock/stuff'
import { listResourcePermission } from '@/api/admin/common/permission'
import { listDictItemAll } from '@/api/sys/dictItem'
import QueryForm from '@/views/components/queryForm'
import MainUI from '@/views/components/mainUI'
import { getLocalCurrentCompany } from "@/utils/auth";
import fileurl from "@/assets/file/stuff.xlsx";


export default {
  extends: MainUI,
  components: {
    QueryForm,
  },
  data() {
    return {
      inventory: null,
      mistake: null,
      chengGong: null,
      shiBai: null,
      dialogVisible: false,
      systemParamConfigSearch: {
        params: []
      },
      permission: {
        view: false,
        add: false,
        edit: false,
        remove: false,
        export: false
      },
      queryTypes: {
        'name': 'like',
        'type': '=',
        'bar_code': 'like',
      },
      queryModel: {
        keyword: '',
        zstbzt: '',
      },
      zstbztOptions: [
        { label: 'Đồng bộ', value: '1' },
        { label: '未同步', value: '2' }
      ],
      search: {
        params: [],
        offset: 0,
        limit: 20,
        columnName: '',      // Tên trường sắp xếp
        order: ''            // Sắp xếp
      },
      currentPage: 1,
      stuffTotal: 0,
      stuffList: [],

      type_List: [],    // Loại vật tư

      oprColumnWidth: 140,  // Độ rộng cột thao tác
      tableId: '1004462867645374480',
      schemeId: '1004462867645374509',
      indateDialogVisible: false,  //Hộp thoại đặt hàng loạt hạn sử dụng
      inventoryDialogVisible: false,   //Hộp thoại đặt hàng loạt cảnh báo tồn kho
      importDialogVisible: false, // Biến hộp thoại nhập hàng loạt
      indate: "",   //Hạn sử dụng
      inventoryFloor: "",   //Ngưỡng tồn kho tối thiểu
      columnList: [
      {
          prop: 'ypmc',
          label: 'Tên thuốc',
        },
        {
          prop: 'pc',
          label: 'Số lô sản xuất',
        },
        {
          prop: 'scrq',
          label: 'Ngày sản xuất',
        },
        {
          prop: 'yxq',
          label: 'Hạn sử dụng',
        },
        {
          prop: 'sl',
          label: 'Số lượng',
        },
        {
          prop: 'ckdw',
          label: '出库单位',
        },
        {
          prop: 'pfj',
          label: '批发价',
        },
        {
          prop: 'ypdm',
          label: '药品代码',
        },
        {
          prop: 'fph',
          label: 'Số hóa đơn',
        },
        {
          prop: 'zstbzt',
          label: '同步状态',
        },
      ],
      dispensingTime: '',
      currentData: [],
    }
  },
  methods: {
    handleSelectionChange(val) {
      this.currentData = val;
    },
    selectable(row, index) {
      if (row.zstbzt === '诊所已同步') {
        return false
      } else {
        return true
      }
    },

    indexMethod(index) {
      return (this.currentPage - 1) * this.search.limit + index + 1;
    },
    reset() {
      this.$refs.queryForm.resetFields()
      this.dispensingTime = ''
      this.onSearch()
    },
    getStuffList(val) {
      this.setLoad()
      /* Tham số truy vấn và quyền dữ liệu */
      this.search.params = [
        { columnName: 'ypdm', queryType: '=', value: ""},
        { columnName: 'kscode', queryType: '=', value: ""},
      ]
      if (val === "1") {
        this.search.params = this.search.params.concat(this.compositeCondition())
      } else {
        // Tham số truy vấn: Tên thuốc
        this.search.params.push({
          columnName: 'keyword',
          queryType: 'like',
          value: this.queryModel.keyword
        })
        // Tham số truy vấn: Đã đồng bộ chưa
        this.search.params.push({
          columnName: 'zstbzt',
          queryType: '=',
          value: this.queryModel.zstbzt
        })
        // Tham số truy vấn: Thời gian phát thuốc Ngày bắt đầu
        this.search.params.push({
          columnName: 'fykssj',
          queryType: 'like',
          value: this.dispensingTime[0]
        })
        // Tham số truy vấn: Thời gian phát thuốc Ngày kết thúc
        this.search.params.push({
          columnName: 'fyjssj',
          queryType: 'like',
          value: this.dispensingTime[1]
        })
      }
      // Quyền dữ liệu: Vật tưstuff
      this.pushDataPermissions(this.search.params, this.$route.meta.routerId, this.tableId)
      listInstitutionDispensingPage(this.search).then(responseData => {
        if (responseData.code === 100) {
          this.stuffTotal = responseData.data.total
          this.stuffList = responseData.data.rows
        } else {
          this.showMessage(responseData)
        }
        this.resetLoad()
      }).catch(error => {
        this.outputError(error)
      })
    },
    onSearch() {
      if (this.moreCodition) {
        this.search.offset = 0
        this.currentPage = 1
        this.getStuffList()
      } else {
        this.$refs['queryForm'].validate(valid => {
          if (valid) {
            this.search.offset = 0
            this.currentPage = 1
            this.getStuffList()
          } else {
            return false
          }
        })
      }
    },
    onSizeChange(val) {
      this.currentPage = 1
      this.search.limit = val;
      this.search.offset = (this.currentPage - 1) * val
      this.getStuffList()
    },
    onCurrentChange(val) {
      this.search.offset = (val - 1) * this.search.limit
      this.currentPage = val
      this.getStuffList()
    },
    async pageInit() {
      this.setLoad()
      try {
        // this.initOptions(this.queryModel)
        // this.search.params = [{ columnName: 'company_id', queryType: '=', value: currentUser.company.id }]
        this.search.params = [
          { columnName: 'ypdm', queryType: '=', value: ""},
          { columnName: 'kscode', queryType: '=', value: ""},
        ]
        // Quyền dữ liệu: Vật tưstuff
        this.pushDataPermissions(this.search.params, this.$route.meta.routerId, this.tableId)
        let [listStuffRespData, listPermissionRespData] = await Promise.all([
          listInstitutionDispensingPage(this.search),
          listResourcePermission(this.$route.meta.routerId)
        ])
        if (listStuffRespData.code === 100 && listPermissionRespData.code === 100) {
          this.stuffTotal = listStuffRespData.data.total
          this.stuffList = listStuffRespData.data.rows
          this.permission.view = listPermissionRespData.data.find(item => {
            return item.permission === 'hospitalDispensing:read'
          })
          this.permission.export = listPermissionRespData.data.find(item => {
            return item.permission === 'hospitalDispensing:export'
          })
          this.permission.add = listPermissionRespData.data.find(item => {
            return item.permission === 'hospitalDispensing:create'
          })
          this.permission.edit = listPermissionRespData.data.find(item => {
            return item.permission === 'hospitalDispensing:update'
          })
          this.permission.remove = listPermissionRespData.data.find(item => {
            return item.permission === 'hospitalDispensing:delete'
          })
        } else {
          this.showMessage(listPermissionRespData.code !== 100 ? listPermissionRespData : listStuffRespData)
        }
        this.resetLoad()
      } catch (error) {
        this.outputError(error)
      }
    },
    onSynchronous() {
      this.$confirm('确定同步吗？', 'Xác nhận', {
        confirmButtonText: 'Xác nhận',
        cancelButtonText: 'Hủy',
        type: 'warning'
      }).then(() => {
        syncInstitutionDispensing(this.currentData).then(responseData => {
          if (responseData.code === 100) {
            this.getStuffList()
            this.showMessage({ type: 'success', msg: 'Đồng bộ thành công' })
          } else {
            this.showMessage(responseData)
          }
        }).catch(error => {
          this.outputError(error)
        })
        
      }).catch(() => {
      })
    },
    onSortChange(orderby) {
      if (validatenull(orderby.prop)) {
        this.search.columnName = ''
        this.search.order = ''
      } else {
        this.search.columnName = orderby.prop
        this.search.order = orderby.order === 'descending' ? 'desc' : 'asc'
      }

      this.getStuffList()
    }
    ,
    initOptions(This) {
      let type_search = {
        // params: [{ 'columnName': 'dict_type_id', 'queryType': '=', 'value': '1004462867645374476' }]
      }
      // Toán tử điều kiện của trường phản hồi，Thay bằng toán tử của trường kích hoạt
      type_search.params && type_search.params.forEach(item => {
        if (this.queryTypes[item.columnName]) {
          item.queryType = this.queryTypes[item.columnName]
        }
      })
      // Trên bảng tương ứng của trườngfilterĐiều kiện
      type_search.params && type_search.params.push.apply(type_search.params, [])
      // Quyền dữ liệu: Mục từ điểnsys_dict_item
      this.pushDataPermissions(type_search.params, this.$route.meta.routerId, '4005')
      this.type_List.splice(0, this.type_List.length)
      listDictItemAll(type_search).then(responseData => {
        this.type_List = responseData.data
      })
    }
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
        // tableRefCóel-tableĐã liên kếtrefGiá trị thuộc tính
        this.$refs.tableRef.doLayout()// Đối với Table Thực hiện bố cục lại
      })
    }
  },
  mounted() {
    this.pageInit()
  }
}
</script>
<style lang="scss" scoped>
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
::v-deep .el-table__body-wrapper {
  height: calc(100% - 44px) !important;
}
</style>
