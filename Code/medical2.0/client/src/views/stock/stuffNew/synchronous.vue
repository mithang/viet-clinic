<template>
  <el-dialog :title="dialogProps.title" :visible.sync="dialogProps.visible" :close-on-click-modal="false" width="90%"
    v-loading="loading" :destroy-on-close="true">
    <el-row>
      <!-- Cửa sổ chỉnh sửa  -->
      <synchronousStuffForm ref="synchronousStuffFormRef" :permission='permission' v-on:save-finished='getStuffList'>
      </synchronousStuffForm>

      <el-card class="page-container">
        <!--  Thanh tìm kiếm  Bắt đầu -->
        <div class='query-form-container'>
          <el-row class='search-row'>
            <el-form :model='queryModel' @submit.native.prevent label-position="left" label-width='70px' ref='queryForm'
              :inline-message='true'>
              <el-col :span="5">
                <el-form-item label='Tên vật tư' prop='name'>
                  <el-input v-model='queryModel.name' :clearable='true' placeholder='Vui lòng nhập tên vật tư'></el-input>
                </el-form-item>
              </el-col>

              <el-col :span="3" style="display:flex;justivy-content:space-around">
                <el-button type="primary" icon="el-icon-search" @click="onSearch()" :plain="true">Tìm kiếm
                </el-button>
                <el-button type="info" icon="el-icon-refresh-left" @click="reset" :plain="true">Đặt lại
                </el-button>
              </el-col>
              <el-col :span="16" style="text-align:right;padding-right:5px">
                <el-button-group>
                  <div style="display: flex;align-items: center;">
                    <div style="margin-right: 10px;">Đã chọn{{ currentData.length }}dòng</div>
                    <div style="display: flex;align-items: center;">
                      <div style="width: 80px;">Nguồn dữ liệu</div>
                      <el-select @change="handleSource" v-model="source">
                        <el-option label="Đơn vị thuê" value="1"></el-option>
                        <el-option label="Bản phòng khámHis" value="2"></el-option>
                      </el-select>
                    </div>
                  </div>
                  <!-- <el-button v-show="permission.add" type="primary" icon="el-icon-plus" @click="onCreateStuff()">Thêm
                  </el-button> -->
                  <!-- <el-dropdown v-show="permission.add" style="padding-left:10px">
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
                  </el-dropdown> -->
                </el-button-group>
              </el-col>
            </el-form>
          </el-row>
          <!-- <QueryForm v-else v-model='moreParm' :tableId='tableId' :schemeId='schemeId' :routerId='$route.meta.routerId'
            @search='onSearch()' @moreCodition='onMoreCodition()'></QueryForm> -->
        </div>
        <el-row>
          <el-col :span='24'>
            <div>
              <el-table ref="tableRef" height="calc(100vh - 280px)" class='drag_table' :data='stuffList' border
                @sort-change='onSortChange' @header-dragend='onChangeWidth' :cell-class-name='cellClassName'
                :header-cell-class-name='headerCellClassName' highlight-current-row :row-key="getRowKey"
                @selection-change="handleSelectionChange">
                <!-- 多选框 -->
                <el-table-column type='selection' :reserve-selection="true" width='50' align='center'
                  :selectable="selectable">
                </el-table-column>
                <el-table-column label="Số thứ tự" type="index" :index="indexMethod" align="center">
                </el-table-column>
                <!-- <el-table-column v-for="(cv, index) in columnViews" v-if='cv.display' :prop='cv.prop'
                  :key="`columnViews_${index}`" :label='cv.label' sortable='custom' :align='cv.align'
                  :min-width='cv.miniWidth + "px"' :width='cv.width + "px"' header-align='center'
                  :column-key='index.toString()' :render-header="renderHeader">
                  <template slot-scope='{row,$index}'>
                    <span
                      v-if='columnViews[index].showType === "Switch" || columnViews[index].showType === "Checkbox" || columnViews[index].showType === "Radio"'>
                      <li v-if='getAttrValue(row, columnViews[index].prop) === "1"' class='el-icon-check'
                        style='color:#F56C6C;'></li>
                    </span>

                    <span v-else>{{ getAttrValue(row, columnViews[index].prop, columnViews[index].javaType) }}</span>
                  </template>
</el-table-column> -->
                <el-table-column v-for="item in columnList" :label="item.label" :prop="item.prop"></el-table-column>
                <el-table-column prop="inventory" label="Tồn kho khả dụng" sortable="custom" width="100px" v-if="source === 1">
                  <template slot-scope="scope">
                    <span v-if="scope.row.stock.surplusStock != null">
                      {{
    Math.floor(scope.row.stock.surplusStock / scope.row.packNumber) >= 0 ?
      Math.floor(scope.row.stock.surplusStock / scope.row.packNumber) + scope.row.packUnit.name +
      ((scope.row.stock.surplusStock % scope.row.packNumber > 0) ? (scope.row.stock.surplusStock %
        scope.row.packNumber) + scope.row.minUnit.name : "") : scope.row.stock.surplusStock +
      scope.row.packUnit.name
  }}
                    </span>
                    <span v-else>0</span>
                  </template>
                </el-table-column>
                <!-- <el-table-column label='Thao tác' header-align='center' :width='50 + "px"' :key="Math.random()">
                  <template slot='header' slot-scope="scope">
                    <span>Thao tác</span>
                  </template>
                  <template slot-scope='scope'>
                    <OperationIcon v-show='permission.view' type='info' content='Xem' placement='top-start'
                      icon-name='el-icon-view' @click='onViewStuff(scope.$index, scope.row)'></OperationIcon>
                    <OperationIcon v-show='permission.edit' type='primary' content='Sửa' placement='top-start'
                      icon-name='el-icon-edit' @click='onEditStuff(scope.$index, scope.row)'></OperationIcon>
                    <OperationIcon v-show='permission.add' type='primary' content='Sao chép' placement='top-start'
                      icon-name='el-icon-document' @click='onCopyStuff(scope.$index, scope.row)'></OperationIcon>
                    <OperationIcon v-show='permission.view' type='info' content='Lịch sử' placement='top-start'
                      icon-name='el-icon-info' @click='onShowHistory(scope.$index, scope.row)'></OperationIcon>
                  </template>
                </el-table-column> -->
              </el-table>
            </div>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span='24'>
            <el-pagination background @size-change='onSizeChange' @current-change='onCurrentChange'
              :current-page.sync='currentPage' :page-sizes='[20, 50, 100, stuffTotal]' :page-size='20'
              layout='total, sizes, prev, pager, next, jumper' :total='stuffTotal'>
            </el-pagination>
          </el-col>
        </el-row>
      </el-card>
      <!-- Đặt hàng loạt cảnh báo hạn sử dụng -->
      <div class="stuff_indate">
        <el-dialog title="Đặt hàng loạt hạn sử dụng" :visible.sync="indateDialogVisible" width="30%" :before-close="handleClose">
          <!-- <el-divider style="margin-top:-30px;"></el-divider> -->
          <div style="padding-bottom:10px;margin-top:-20px">
            <el-alert title="Sau khi đặt hàng loạt sẽ sửa thời gian cảnh báo hạn sử dụng của tất cả thuốc" :closable="false" type="warning">
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
        <el-dialog title="Đặt hàng loạt cảnh báo tồn kho" :visible.sync="inventoryDialogVisible" width="30%"
          :before-close="handleCloseInventory">
          <!-- <el-divider style="margin-top:-30px;"></el-divider> -->
          <div style="padding-bottom:10px;margin-top:-20px">
            <el-alert title="Sau khi đặt hàng loạt sẽ sửa số lượng cảnh báo tồn kho của tất cả thuốc（Cảnh báo theo đơn vị đóng gói）" :closable="false" type="warning">
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
        <el-dialog title="Nhập vật tư" :visible.sync="importDialogVisible" width="45%" :before-close="cancellation">
          <input ref="file" type="file" accept=".xlsx,.xls" style="display: none;" @change="uploadFile">
          <el-button type="primary" icon="el-icon-upload" @click="clickFile">Chọn tệp</el-button>
          <el-button type="primary" icon="el-icon-download" @click="downloadExcel">Tải mẫu xuống</el-button>
          <el-button @click="cancellation()">Hủy</el-button>
        </el-dialog>

        <el-dialog title="Lỗi nhập thông tin vật tư" :visible.sync="dialogVisible" width="30%" :before-close="mistakeClose">
          <span> Nhập dữ liệu thành công<span style="white-space:pre;color:#08ff00;font-size: 16px">{{ chengGong
              }}</span>dòng&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
          </span>
          <span> Nhập dữ liệu thất bại<span style="white-space:pre;color:red;font-size: 16px">{{ shiBai }}</span>dòng </span><br>
          <span style="white-space:pre;color:red;font-size: 15px">{{ mistake }}</span>
          <span slot="footer" class="dialog-footer">
            <el-button @click="dialogVisible = false">Hủy</el-button>
            <el-button type="primary" @click="dialogVisible = false">Xác nhận</el-button>
          </span>
        </el-dialog>
      </div>
    </el-row>
    <span slot='footer' class='dialog-footer'>
      <el-button :disabled="flag" type='primary' :plain='true' @click='onSubmit()'>Đồng bộ</el-button>
      <el-button v-if='dialogProps.action !== "view"' :plain='true' @click='onDialogClose()'>Hủy</el-button>
    </span>
  </el-dialog>
</template>
<script>
import { validatenull } from '@/utils/validate'
import { listParentStuffPage, listInstitutionStuffPage, syncStuff, syncInstitutionStuff, getStuffById, deleteStuff, updateAllIndate, updateAllInventory, inventory, uploadExcel } from '@/api/stock/stuff'
import { listResourcePermission } from '@/api/admin/common/permission'
import synchronousStuffForm from './synchronousStuffForm'
import { listDictItemAll } from '@/api/sys/dictItem'
import { getDictItemsByCode, DICT_CODE } from '@/utils/dictCache'
import MainUI from '@/views/components/mainUI'
import History from '@/views/components/history'
import { getLocalCurrentCompany } from "@/utils/auth";
import fileurl from "@/assets/file/stuff.xlsx";
import BaseUI from "@/views/components/baseUI";


export default {
  extends: MainUI,
  name: "stuff-form",
  components: {
    synchronousStuffForm,
    History,
  },
  data() {
    return {
      dialogProps: {
        visible: false,
        action: '',
        title: '',
      },
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
        'name': '',   // Tên vật tư
        'type': {     // Loại vật tư
          'value': '',
          'name': '',
        },
        'barCode': '',   // Mã vạch
        'status': '',
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
      isOutSell: [
        {
          value: '0',
          label: 'Không'
        },
        {
          value: '1',
          label: 'Có'
        },
      ],
      search: {
        params: [{ columnName: 'company_id', queryType: '=', value: currentUser.company.id }],
        offset: 0,
        limit: 20,
        columnName: '',      // Tên trường sắp xếp
        order: ''            // Sắp xếp
      },
      currentPage: 1,
      stuffTotal: 0,
      stuffList: [],

      typeList: [],    // Loại vật tư

      oprColumnWidth: 140,  // Độ rộng cột thao tác
      tableId: '1004462867645374480',
      schemeId: '1004462867645374509',
      indateDialogVisible: false,  //Hộp thoại đặt hàng loạt hạn sử dụng
      inventoryDialogVisible: false,   //Hộp thoại đặt hàng loạt cảnh báo tồn kho
      importDialogVisible: false, // Biến hộp thoại nhập hàng loạt
      indate: "",   //Hạn sử dụng
      inventoryFloor: "",   //Ngưỡng tồn kho tối thiểu
      currentData: [],
      source: '1',
      columnList: [],
      columnListOne: [
        {
          prop: 'name',
          label: 'Tên vật tư',
        },
        {
          prop: 'commonName',
          label: 'Tên thường dùng',
        },
        {
          prop: 'code',
          label: 'Mã vật tư',
        },
        {
          prop: 'typeName',
          label: 'Loại vật tư',
        },
        {
          prop: 'factoryName',
          label: 'Nhà sản xuất',
        },
        {
          prop: 'natureName',
          label: 'Tính chất',
        },
        {
          prop: 'barCode',
          label: 'Mã vạch',
        },
        {
          prop: 'syncNumName',
          label: 'Đã đồng bộ chưa',
        },
        {
          prop: 'createDate',
          label: 'Ngày tạo',
        },
        {
          prop: 'updateDate',
          label: '更新日期',
        },
      ],
      // 院版材料列表
      columnListTwo: [
        {
          prop: 'sfxmmc',
          label: 'Tên vật tư',
        },
        {
          prop: 'py',
          label: 'Mã pinyin',
        },
        {
          prop: 'sfxmCode',
          label: 'Mã vật tư',
        },
        {
          prop: 'dwjls',
          label: '单位计量数',
        },
        {
          prop: 'dw',
          label: 'Đơn vị nhỏ nhất',
        },
        {
          prop: 'gg',
          label: 'Quy cách đóng gói',
        },
        {
          prop: 'dj',
          label: 'Giá bán ra ngoài',
        },
        {
          prop: 'syncNumName',
          label: 'Đã đồng bộ chưa',
        },
      ]

    };
  },
  props: {
    // Quyền
    // permission: {
    //   type: Object,
    // },

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
    handleSelectionChange(val) {
      this.currentData = val;
    },
    async onSubmit() {
      if (this.currentData.length === 0) {
        this.$message({
          message: '请选择要同步的数据',
          type: 'warning'
        });
        return;
      }
      const functionSync = this.source === '1' ? syncStuff : syncInstitutionStuff
      const res = await functionSync(this.currentData);
      if (res.code === 100) {
        this.$message({
          message: 'Đồng bộ thành công',
          type: 'success'
        });
        this.dialogProps.visible = false
        this.$emit('update')
      } else {
        this.$message({
          message: res.msg,
          type: 'error'
        });
      }
    },
    onDialogClose() {
      this.dialogProps.visible = false
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
        let stuff = {
          indate: this.indate,
          company: currentUser.company
        }
        updateAllIndate(stuff).then((res) => {
          if (res.code === 100) {
            this.indateDialogVisible = false
            this.indate = ""
            this.getStuffList();
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
        let stuff = {
          inventoryFloor: this.inventoryFloor,
          company: currentUser.company
        }
        updateAllInventory(stuff).then((res) => {
          if (res.code === 100) {
            this.inventoryDialogVisible = false
            this.inventoryFloor = ""
            this.getStuffList();
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
      a.download = 'Thông tin vật tư';
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
        .catch(_ => { });
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
            if (res.data[2] === "") {
              this.$message({
                type: 'success',
                message: 'Nhập dữ liệu thành công,Đã nhập tổng cộng' + res.data[0] + 'bản ghi!'
              })
              this.cancellation();
            } else {
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
      }
      else {
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


    indexMethod(index) {
      return (this.currentPage - 1) * this.search.limit + index + 1;
    },
    reset() {
      this.$refs.queryForm.resetFields()
      this.onSearch()
    },
    getStuffList(val) {
      this.setLoad()
      /* Tham số truy vấn và quyền dữ liệu */
      this.search.params = [{ columnName: 'company_id', queryType: '=', value: currentUser.company.id }]
      if (val === "1") {
        this.search.params = this.search.params.concat(this.compositeCondition())
      } else {
        // Tham số truy vấn: Tên vật tư
        this.search.params.push({
          columnName: this.source === 1 ? 'name' : 'keyword',
          queryType: 'like',
          value: this.queryModel.name
        })
        // Tham số truy vấn: Loại vật tư
        this.search.params.push({
          columnName: 'type',
          queryType: '=',
          value: validatenull(this.queryModel.type.value) ? '' : this.queryModel.type.value
        })
        // Tham số truy vấn: Mã vạch
        this.search.params.push({
          columnName: 'bar_code',
          queryType: 'like',
          value: this.queryModel.barCode
        })
        // Tham số truy vấn: Trạng thái
        this.search.params.push({
          columnName: "status",
          queryType: "=",
          value: this.queryModel.status,
        });
        // Tham số truy vấn: Có bán ra ngoài không
        this.search.params.push({
          columnName: "is_out_sell",
          queryType: "=",
          value: this.queryModel.isOutSell,
        });
      }
      // Quyền dữ liệu: Vật tưstuff
      this.pushDataPermissions(this.search.params, this.$route.meta.routerId, this.tableId)
      const functionList = this.source === '1' ? listParentStuffPage : listInstitutionStuffPage
      functionList(this.search).then(responseData => {
        if (responseData.code === 100) {
          this.stuffList = responseData.data.rows
          this.stuffTotal = responseData.data.total
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
        this.initOptions(this.queryModel)
        this.search.params = [{ columnName: 'company_id', queryType: '=', value: currentUser.company.id }]
        const functionList = this.source === '1' ? listParentStuffPage : listInstitutionStuffPage
        // Quyền dữ liệu: Vật tưstuff
        this.pushDataPermissions(this.search.params, this.$route.meta.routerId, this.tableId)
        let [listStuffRespData, listPermissionRespData] = await Promise.all([
          functionList(this.search),
          listResourcePermission(this.$route.meta.routerId)
        ])
        if (listStuffRespData.code === 100 && listPermissionRespData.code === 100) {
          this.stuffTotal = listStuffRespData.data.total
          if (this.source === 1) {
            this.stuffList = listStuffRespData.data.rows.map((item) => {
              item.typeName = item.type.name
              item.natureName = item.nature.name
              item.factoryName = item.factory.name
              item.syncNumName = item.syncNum === 1 ? 'Có' : 'Không'
              return item
            })
          } else {
            this.stuffList = listStuffRespData.data.rows.map((item) => {
              item.syncNumName = item.syncNum === 1 ? 'Có' : 'Không'
              return item
            })
          }

          this.permission.view = listPermissionRespData.data.find(item => {
            return item.permission === 'stuffNew:read'
          })
          this.permission.export = listPermissionRespData.data.find(item => {
            return item.permission === 'stuffNew:export'
          })
          this.permission.add = listPermissionRespData.data.find(item => {
            return item.permission === 'stuffNew:create'
          })
          this.permission.edit = listPermissionRespData.data.find(item => {
            return item.permission === 'stuffNew:update'
          })
          this.permission.remove = listPermissionRespData.data.find(item => {
            return item.permission === 'stuffNew:delete'
          })
        } else {
          this.showMessage(listPermissionRespData.code !== 100 ? listPermissionRespData : listStuffRespData)
        }
        this.resetLoad()
      } catch (error) {
        this.outputError(error)
      }
    },
    onViewStuff(index, row) {
      this.setLoad()
      getStuffById(row.id).then(responseData => {
        if (responseData.code === 100) {
          this.$refs.synchronousStuffFormRef.openViewStuffDialog(responseData.data)
        } else {
          this.showMessage(responseData)
        }
        this.resetLoad()
      }).catch(error => {
        this.outputError(error)
      })
    },
    onCreateStuff() {
      this.$refs.synchronousStuffFormRef.openAddStuffDialog()
      // this.$refs.synchronousStuffFormRef.dialogProps.visible = true
    },
    onEditStuff(index, row) {
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
          if (responseData.data.length >= 1) {
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
      setTimeout(() => {
        if (this.inventory !== 0) {
          this.$message({
            message: "Vật phẩm còn tồn kho，Không thể sửa",
            type: 'warning',
          })
        } else if (this.inventory === 0) {
          this.setLoad()
          getStuffById(row.id).then(responseData => {
            if (responseData.code === 100) {
              this.$refs.synchronousStuffFormRef.openEditStuffDialog(responseData.data)
            } else {
              this.showMessage(responseData)
            }
            this.resetLoad()
          }).catch(error => {

            this.outputError(error)
          })
        }
        this.inventory = null
      }, 1000);
    },
    onCopyStuff(index, row) {
      this.setLoad()
      getStuffById(row.id).then(responseData => {
        if (responseData.code === 100) {
          this.$refs.synchronousStuffFormRef.openCopyStuffDialog(responseData.data)
        } else {
          this.showMessage(responseData)
        }
        this.resetLoad()
      }).catch(error => {
        this.outputError(error)
      })
    }
    ,
    onDeleteStuff(index, row) {
      this.$confirm('Bạn có chắc muốn xóa không？', 'Xác nhận', {
        confirmButtonText: 'Xác nhận',
        cancelButtonText: 'Hủy',
        type: 'warning'
      }).then(() => {
        this.setLoad()
        deleteStuff(row).then(responseData => {
          if (responseData.code === 100) {
            this.getStuffList()
            this.showMessage({ type: 'success', msg: 'Xóa thành công' })
          } else {
            this.showMessage(responseData)
          }
          this.resetLoad()
        }).catch(error => {
          this.outputError(error)
        })
      }).catch(() => {
      })
    }
    ,
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
      getDictItemsByCode(DICT_CODE.STUFF_TYPE).then((data) => {
        this.typeList = data
      })
    },
    openSyncStuffDialog() {
      this.dialogProps.title = "同步材料信息";
      this.dialogProps.visible = true;
      this.pageInit()
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
  },
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


</style>

<style>
.stuff_indate .el-dialog__header {
  border-bottom: 1px solid rgb(214, 214, 214) !important;
}
</style>
