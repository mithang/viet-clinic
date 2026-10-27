<template>
  <el-row v-loading='loading'>
    <!-- Lịch sử  -->
    <History :bussObject='curentRow'></History>
    <!-- Cửa sổ chỉnh sửa  -->
    <recipetemplate-form ref='recipetemplateForm' :permission='permission'
                         @save-finished='loadData'></recipetemplate-form>
    <div class="page-container">
      <!--  Thanh tìm kiếm  Bắt đầu -->
      <div class='query-form-container'>
        <el-row v-if='!moreCodition' class='search-row'>
          <el-form :model='queryModel' @submit.native.prevent label-position="left" label-width='70px' ref='queryForm'
                   :inline-message='true'>
            <el-col :span="4">
              <el-form-item label='Tên mẫu' prop='recipetemplateName'>
                <!-- <div style="margin-left:40px;">  -->
                <el-input v-model='queryModel.recipetemplateName' :clearable='true' placeholder='Vui lòng nhập tên mẫu'></el-input>
                <!-- </div> -->
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item label='Mã mẫu' prop='doce'>
                <!-- <div style="margin-left:40px;">  -->
                <el-input v-model='queryModel.code' :clearable='true' placeholder='Vui lòng nhập mã mẫu'></el-input>
                <!-- </div> -->
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item label='Loại đơn thuốc' prop='type'>
                <el-select v-model="queryModel.type" placeholder="Vui lòng chọn loại đơn thuốc">
                  <el-option
                    v-for="type in type_List"
                    :key="type.value"
                    :label="type.name"
                    :value="type">
                  </el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="5">
              <el-form-item label='Danh mục mẫu' prop='category'>
                <el-radio v-model="queryModel.category" label="0">Mẫu riêng tư</el-radio>
                <el-radio v-model="queryModel.category" label="1">Mẫu công khai</el-radio>
                <el-radio v-model="queryModel.category" label="2">AIMẫu</el-radio>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label='Ngày' prop='updateDate'>
                <el-date-picker
                  v-model="queryModel.updateDate"
                  type="datetimerange"
                  align="right"
                  start-placeholder="Ngày bắt đầu"
                  end-placeholder="Ngày kết thúc"
                  format="yyyy-MM-dd HH:mm:ss"
                  :default-time="['00:00:00', '23:59:59']">
                </el-date-picker>
              </el-form-item>
            </el-col>

            <el-col :span="3" style="text-align:right;padding-right:5px;float:right;">
              <el-button-group>
                <el-button
                  type="primary"
                  icon="el-icon-plus"
                  @click="onCreateTemplate()"
                >Thêm
                </el-button
                >
              </el-button-group>
            </el-col>
            <el-col :span="3" style="display:flex;justivy-content:space-around;float:right;">
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

          </el-form>
        </el-row>
        <QueryForm v-else v-model='moreParm' :tableId='tableId' :schemeId='schemeId' :routerId='$route.meta.routerId'
                   @search='onSearch()' @moreCodition='onMoreCodition()'></QueryForm>
      </div>
      <!--  Thanh tìm kiếm  Kết thúc -->

      <!-- Thanh công cụ Bắt đầu -->
      <!-- <div class="page-container-header-end">
        <div>
          <el-button v-show='permission.add' type='primary' icon='el-icon-plus' :plain='true' @click='onCreateRecipetemplate()'>Thêm</el-button>
        </div>
      </div> -->
      <!-- Thanh công cụ Kết thúc -->

      <!-- Thanh bảng  Bắt đầu -->
      <el-row>
        <el-col :span='24'>
          <div @mouseleave='moveTableOutside'>
            <el-table class='drag_table' ref="tableRef" height="calc(100vh - 240px)" :data='recipetemplateList' border
                      @sort-change='onSortChange' @header-dragend='onChangeWidth' :cell-class-name='cellClassName'
                      :header-cell-class-name='headerCellClassName' highlight-current-row>
              <el-table-column
                label="Số thứ tự"
                type="index"
                :index="indexMethod"
                align="center">
              </el-table-column>
              <el-table-column v-for="(cv, index) in columnViews" v-if='cv.display' :prop='cv.prop'
                               :key="`columnViews_${index}`" :label='cv.label' sortable='custom' :align='cv.align'
                               :min-width='cv.miniWidth+"px"' :width='cv.width+"px"' header-align='center'
                               :column-key='index.toString()' :render-header="renderHeader">
                <template slot-scope='{row,$index}'>
                  <span
                    v-if='columnViews[index].showType === "Switch" || columnViews[index].showType === "Checkbox" || columnViews[index].showType === "Radio"'>
                    <!-- <li v-if='getAttrValue(row, columnViews[index].prop) == "1"' class='el-icon-check' style='color:#F56C6C;'>

                    </li> -->
                    <!-- <li v-if='getAttrValue(row, columnViews[index].prop) == "1"' >

                   </li> -->
                    <span v-if='getAttrValue(row, columnViews[index].prop) === "1"'>
                      Mẫu công khai
                    </span>
                     <span v-else>
                      Mẫu riêng tư
                    </span>
                  </span>
                  <span v-else>{{ getAttrValue(row, columnViews[index].prop, columnViews[index].javaType) }}</span>
                </template>
              </el-table-column>
              <!--Nút thao tác theo dòng-->
              <el-table-column label='Thao tác' header-align='center' :width='140 + "px"' :key="'operate'">
                <template slot='header' slot-scope="scope">
                  <span>Thao tác</span>
                  <view-columns-select v-model='columnViews' v-on:save-column-view='saveColumn'
                                       v-on:show-all-column='showAllColumn'
                                       v-on:show-default-column='showDefaultColumn'></view-columns-select>
                  <export-excel-button v-show='permission.export' :data='recipetemplateList' :tHeader='getHeads()'
                                       :filterVal='getFilterVal()' :plain='true'></export-excel-button>
                </template>
                <template slot-scope='scope'>
                  <OperationIcon v-show='permission.view' type='info' content='Xem' placement='top-start'
                                 icon-name='el-icon-view'
                                 @click='onViewRecipetemplate(scope.$index, scope.row)'></OperationIcon>
                  <OperationIcon v-show='permission.edit' type='primary' content='Sửa' placement='top-start'
                                 icon-name='el-icon-edit'
                                 @click='onEditRecipetemplate(scope.$index, scope.row)'></OperationIcon>
                  <OperationIcon v-show='permission.add' type='primary' content='Sao chép' placement='top-start'
                                 icon-name='el-icon-document'
                                 @click='onCopyRecipetemplate(scope.$index, scope.row)'></OperationIcon>
                  <OperationIcon v-show='permission.remove' type='danger' content='Xóa' placement='top-start'
                                 icon-name='el-icon-delete'
                                 @click='onDeleteRecipetemplate(scope.$index, scope.row)'></OperationIcon>
                  <OperationIcon v-show='permission.view' type='info' content='Lịch sử' placement='top-start'
                                 icon-name='el-icon-info'
                                 @click='onShowHistory(scope.$index, scope.row)'></OperationIcon>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-col>
      </el-row>
      <!-- Thanh bảng  Kết thúc -->
      <!-- Thanh phân trang     Bắt đầu -->
      <el-row>
        <el-col :span='24'>
          <el-pagination
            background
            @size-change='onSizeChange'
            @current-change='onCurrentChange'
            :current-page.sync='currentPage'
            :page-sizes='[20, 50, 100, recipetemplateTotal]'
            :page-size='20'
            layout='total, sizes, prev, pager, next, jumper'
            :total='recipetemplateTotal'>
          </el-pagination>
        </el-col>
      </el-row>
      <!-- Thanh phân trang     Kết thúc -->     </div>

    <!-- 添加弹出框 -->
    <el-dialog
      title="添加处方模板"
      :visible.sync="templateDialogVisible"
      width="30%"
      :before-close="handleClose">
      <er-row v-for="(item,index) in type_List" :key="index">
        <!-- <div v-for="(item,index) in type_List" :key="index"> -->
        <el-col :span="12">
          <div style="margin-left:60px;margin-top:20px;">
            <el-button class="buttonClass" type="info" plain @click="add(item,index)">{{ item.name }}</el-button>
          </div>
        </el-col>

        <!-- </div> -->
      </er-row>
      <!-- <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">Hủy</el-button>
        <el-button type="primary" @click="dialogVisible = false">Xác nhận</el-button>
      </span> -->
    </el-dialog>


  </el-row>
</template>

<script>
  import {validatenull} from '@/utils/validate'
  import {listRecipetemplatePage, getRecipetemplateById, deleteRecipetemplate} from '@/api/outpatient/recipetemplate'
  import { listResourcePermission } from '@/api/resourcePermission'
  import { mapPermissions } from '@/utils/searchParamsBuilder'
  import listViewMixin from '@/mixins/listViewMixin'
  import RecipetemplateForm from './recipetemplateForm'
  import ExportExcelButton from '@/components/ExportExcelButton'
  import ViewColumnsSelect from '@/views/components/ViewColumnsSelect'
  import QueryForm from '@/views/components/queryForm'
  import MainUI from '@/views/components/mainUI'
  import OperationIcon from '@/components/OperationIcon'
  import History from '@/views/components/history'
  import {listDictItemAll} from '@/api/sys/dictItem'
  import { getDictItemsByCode, DICT_CODE } from '@/utils/dictCache'
  import { getCurrentUser } from "@/utils/userCache";

  export default {
    extends: MainUI,
    mixins: [listViewMixin],
    components: {
      RecipetemplateForm,
      ExportExcelButton,
      ViewColumnsSelect,
      QueryForm,
      OperationIcon,
      History
    },
    data() {
      return {
        listApi: listRecipetemplatePage,
        getApi: getRecipetemplateById,
        deleteApi: deleteRecipetemplate,
        entityName: 'Recipetemplate',
        permissionPrefix: 'recipeTemplate',
        queryTypes: {},
        queryModel: {
          recipetemplateName: "",
          code: "",
          type: {},
          category: "",
          updateDate: "",
        },
        recipetemplateTotal: 0,
        recipetemplateList: [],

        oprColumnWidth: 140,  // Độ rộng cột thao tác
        tableId: '1186832625065336477',
        schemeId: '1186832625065336461',
        templateDialogVisible: false,
        type_List: [],
      }
    },
    methods: {
      reset() {
        this.queryModel = {
          recipetemplateName: "",
          code: "",
          type: {},
          category: "",
          updateDate: "",
        }
        this.onSearch()
      },
      add(item, index) {
        // if(index==0){
        this.$refs.recipetemplateForm.openAddRecipetemplateDialog(item, index, currentUser.company)
        this.templateDialogVisible = false
        //}
      },
      onCreateTemplate() {
        // this.$refs.recipetemplateForm.$emit('openAddRecipetemplateDialog')
        getDictItemsByCode(DICT_CODE.RECIPEL_TYPE).then((data) => {
          this.type_List = data
          this.type_List = this.type_List.filter((item) => item.name !== "Đơn thuốc bán lẻ")
          this.type_List = this.type_List.filter((item) => item.name !== 'Phí phụ thêm')
          this.templateDialogVisible = true
        })
      },

      appendSearchParams() {
        this.search.params.push({columnName: 'company_id', queryType: '=', value: currentUser.company.id})
        if (this.moreCodition) {
          this.search.params = this.search.params.concat(this.compositeCondition())
        } else {
          this.search.params.push({
            columnName: 'recipetemplate_name',
            queryType: 'like',
            value: this.queryModel.recipetemplateName
          })
          this.search.params.push({
            columnName: 'code',
            queryType: 'like',
            value: "%" + this.queryModel.code ? this.queryModel.code : "" + "%"
          })
          this.search.params.push({
            columnName: 'type',
            queryType: '=',
            value: validatenull(this.queryModel.type.value) ? '' : this.queryModel.type.value
          })
          if (this.queryModel.category !== "" && this.queryModel.category === 0) {
            this.search.params.push({
              columnName: "category",
              queryType: '=',
              value: "0",
            })
            this.search.params.push({
              columnName: "create_id",
              logic: 'and',
              queryType: '=',
              value: currentUser.id,
            })
          } else if (this.queryModel.category !== "" && this.queryModel.category === 1) {
            this.search.params.push({
              columnName: "category",
              queryType: '=',
              value: "1",
            })
          }
          if (this.queryModel.updateDate && this.queryModel.updateDate.length) {
            this.search.params.push({
              logic: "AND",
              queryType: "("
            }, {
              columnName: "update_date",
              logic: "",
              queryType: 'between',
              value: this.queryModel.updateDate,
            }, {
              logic: "",
              queryType: ")"
            })
          }
        }
        // Quyền dữ liệu: Bảng mẫu đơn thuốcrecipetemplate
        this.pushDataPermissions(this.search.params, this.$route.meta.routerId, this.tableId)
      },
      handleListResponse(responseData) {
        this.recipetemplateTotal = responseData.data.total
        this.recipetemplateList = responseData.data.rows
      },
      onViewRecipetemplate(index, row) {
        this.setLoad()
        getRecipetemplateById(row.id).then(responseData => {
          if (responseData.code === 100) {
            this.$refs.recipetemplateForm.openViewRecipetemplateDialog(responseData.data, currentUser.company)
          } else {
            this.showMessage(responseData)
          }
          this.resetLoad()
        }).catch(error => {
          this.outputError(error)
        })
      },
      onCreateRecipetemplate() {
        this.$refs.recipetemplateForm.openAddRecipetemplateDialog(currentUser.company)
      },
      onEditRecipetemplate(index, row) {
        this.setLoad()
        getRecipetemplateById(row.id).then(responseData => {
          if (responseData.code === 100) {
            this.$refs.recipetemplateForm.openEditRecipetemplateDialog(responseData.data, currentUser.company)
          } else {
            this.showMessage(responseData)
          }
          this.resetLoad()
        }).catch(error => {
          this.outputError(error)
        })
      },
      onCopyRecipetemplate(index, row) {
        this.setLoad()
        getRecipetemplateById(row.id).then(responseData => {
          if (responseData.code === 100) {
            this.$refs.recipetemplateForm.openCopyRecipetemplateDialog(responseData.data)
          } else {
            this.showMessage(responseData)
          }
          this.resetLoad()
        }).catch(error => {
          this.outputError(error)
        })
      },
      onDeleteRecipetemplate(index, row) {
        this.$confirm('Bạn có chắc muốn xóa không？', 'Xác nhận', {
          confirmButtonText: 'Xác nhận',
          cancelButtonText: 'Hủy',
          type: 'warning'
        }).then(() => {
          this.setLoad()
          deleteRecipetemplate(row).then(responseData => {
            if (responseData.code === 100) {
              this.loadData()
              this.showMessage({type: 'success', msg: 'Xóa thành công'})
            } else {
              this.showMessage(responseData)
            }
            this.resetLoad()
          }).catch(error => {
            this.outputError(error)
          })
        }).catch(() => {
        })
      },
      async pageInit() {
        this.setLoad()
        try {
          if (this.initOptions) {
            this.initOptions(this.queryModel)
          }
          this.search.params = []
          if (this.appendSearchParams) {
            this.appendSearchParams()
          }
          const prefix = this.permissionPrefix || this.entityName
          const [listRespData, permissionRespData] = await Promise.all([
            this.listApi(this.search),
            listResourcePermission(this.$route.meta.routerId)
          ])
          if (listRespData.code === 100 && permissionRespData.code === 100) {
            this.handleListResponse(listRespData)
            this.permission = mapPermissions(permissionRespData.data, prefix)
          } else {
            this.showMessage(permissionRespData.code !== 100 ? permissionRespData : listRespData)
          }
          this.resetLoad()
        } catch (error) {
          this.outputError(error)
        }
        // Load prescription type options after page init
        getDictItemsByCode(DICT_CODE.RECIPEL_TYPE).then((data) => {
          this.type_List = data
          this.type_List = this.type_List.filter((item) => item.name !== "Đơn thuốc bán lẻ")
          this.type_List = this.type_List.filter((item) => item.name !== 'Phí phụ thêm')
        })
      },
      initOptions(This) {
        let company = getCurrentUser().company;
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
      this.initOptions();

    }
  }
</script>
<style scoped lang="scss">

  .buttonClass {
    width: 120px;
    height: 40px;
    padding: 0;
    //控制图标icon大小
    ::v-deep i {
      font-size: 20px;
    }

    //控制字大小
    ::v-deep {
      font-size: 20px;
    }
  }


</style>
