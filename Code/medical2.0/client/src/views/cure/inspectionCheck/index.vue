<template>
  <el-row v-loading='loading'>
    <!-- Lịch sử  -->
    <History :bussObject='curentRow' ></History>
    <!-- Cửa sổ chỉnh sửa  -->
    <inspectionCheck-form ref='inspectionCheckForm' :permission='permission' @save-finished='loadData'></inspectionCheck-form>
    <div class="page-container">
        <!--  Thanh tìm kiếm  Bắt đầu -->
        <div class='query-form-container'>
          <el-row v-if='!moreCodition' class='search-row'>
            <el-form :model='queryModel' @submit.native.prevent label-position="left" label-width='70px' ref='queryForm' :inline-message='true'>
                      <el-col :span="6">
                      <el-form-item label='Thời gian lập phiếu' prop='completeDate'>
                       <el-date-picker
                        v-model="queryModel.completeDate"
                        type="datetimerange"
                        align="right"
                        start-placeholder="Ngày bắt đầu"
                        end-placeholder="Ngày kết thúc"
                        format="dd/MM/yyyy HH:mm:ss"
                        :default-time="['00:00:00', '23:59:59']">
                      </el-date-picker>
                      </el-form-item>
                    </el-col>
                      <el-col :span="6">
              <el-form-item label='Tên bệnh nhân' prop='patientName'>
                <el-input v-model='queryModel.patientName' :clearable='true' placeholder='Vui lòng nhập tên bệnh nhân'></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label='Bác sĩ kê đơn' prop='completeBy'>
                 <el-select  v-model="queryModel.completeBy" placeholder="Vui lòng chọn">
                        <el-option
                          v-for="item in doctorList"
                          :key="item.name"
                          :label="item.name"
                          :value="item.name">
                        </el-option>
                      </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label='Trạng thái' prop='status'>
                 <el-select  v-model="queryModel.status" placeholder="Vui lòng chọn">
                        <el-option
                          v-for="item in statusList"
                          :key="item.value"
                          :label="item.name"
                          :value="item.value">
                        </el-option>
                      </el-select>
              </el-form-item>
            </el-col>
               <el-col :span="3" style="text-align:right;padding-right:5px;float:right;">
               <el-button
                    type="primary"
                    icon="el-icon-search"
                    @click="onSearch()"
                    :plain="true"
                  >Tìm kiếm</el-button>
                <el-button
                    type="info"
                    icon="el-icon-refresh-left"
                    @click="reset"
                    :plain="true"
                  >Đặt lại</el-button>
            </el-col>
            </el-form>
          </el-row>
          <QueryForm v-else v-model='moreParm' :tableId='tableId' :schemeId='schemeId'  :routerId='$route.meta.routerId' @search='onSearch()' @moreCodition='onMoreCodition()'></QueryForm>
        </div>
        <!--  Thanh tìm kiếm  Kết thúc -->

        <!-- Thanh công cụ Bắt đầu -->
        <div class="page-container-header-end">
          <div>
            <el-button v-show='permission.add' type='primary' icon='el-icon-plus' :plain='true' @click='onCreateInspectionCheck()'>Thêm</el-button>
          </div>
        </div>
        <!-- Thanh công cụ Kết thúc -->

      <!-- Thanh bảng  Bắt đầu -->
      <el-row>
        <el-col :span='24'>
          <div @mouseleave='moveTableOutside'>        
            <el-table class='drag_table' ref="tableRef" height="calc(100vh - 200px)" :data='inspectionCheckList' border @sort-change='onSortChange' @header-dragend='onChangeWidth' :cell-class-name='cellClassName' :header-cell-class-name='headerCellClassName' highlight-current-row>                
              <el-table-column
              label="Số thứ tự"
              type="index"
              :index="indexMethod"
              align="center">
            </el-table-column>
              <el-table-column v-for="(cv, index) in columnViews" v-if='cv.display' :prop='cv.prop' :key="`columnViews_${index}`" :label='cv.label' sortable='custom' :align='cv.align' :min-width='cv.miniWidth+"px"' :width='cv.width+"px"' header-align='center' :column-key='index.toString()' :render-header="renderHeader">
                <template slot-scope='{row}'>
                  <span v-if='columnViews[index].showType === "Switch" || columnViews[index].showType === "Checkbox" || columnViews[index].showType === "Radio"'>
                    <li v-if='getAttrValue(row, columnViews[index].prop) === "1"' class='el-icon-check' style='color:#F56C6C;'></li>
                  </span>
                  <span v-else-if="columnViews[index].prop==='type'">
                    <span v-if="getAttrValue(row, columnViews[index].prop)==='0'">Xét nghiệm</span>
                    <span v-else>Kiểm tra</span>
                  </span>
                  <span v-else-if="columnViews[index].prop==='status'">
                    <span v-if="getAttrValue(row, columnViews[index].prop)==='0'" style="color:#FF9966">Chờ điền</span>
                    <span v-else style="color:#99CC66">Đã điền</span>
                  </span>
                  <span  v-else-if="columnViews[index].prop==='sex'">
                    <span v-if="getAttrValue(row, columnViews[index].prop)==='gender_1'">Nữ</span>
                    <span v-else>Nam</span>
                  </span>
                  <span v-else>{{ getAttrValue(row, columnViews[index].prop, columnViews[index].javaType )}}</span>

                </template>
              </el-table-column>
              <!--Nút thao tác theo dòng-->
              <el-table-column label='Thao tác' header-align='center' :key="'operate'" :width='120 + "px"'>        
                <template slot='header'>
                  <span>Thao tác</span>
                  <view-columns-select v-model='columnViews' v-on:save-column-view='saveColumn' v-on:show-all-column='showAllColumn' v-on:show-default-column='showDefaultColumn'></view-columns-select>
                  <export-excel-button v-show='permission.export' :data='inspectionCheckList' :tHeader='getHeads()' :filterVal='getFilterVal()' :plain='true'></export-excel-button>
                </template>
                <template slot-scope='scope'>
                  <el-button type="text" v-if="scope.row.status==='0'"  @click="fill(scope.row,scope.$index,0)">Nhập kết quả</el-button>
                  <el-button type="text" v-if="scope.row.status==='1'"  @click="fill(scope.row,scope.$index,1)">Xem chi tiết</el-button>
                  <OperationIcon v-show='permission.view' type='info' content='Xem' placement='top-start' icon-name='el-icon-view' 
                    @click='onViewInspectionCheck(scope.$index, scope.row)'></OperationIcon>
                  <OperationIcon v-show='permission.edit' type='primary' content='Sửa' placement='top-start' icon-name='el-icon-edit' 
                    @click='onEditInspectionCheck(scope.$index, scope.row)'></OperationIcon>
                  <OperationIcon v-show='permission.add' type='primary' content='Sao chép' placement='top-start' icon-name='el-icon-document' 
                    @click='onCopyInspectionCheck(scope.$index, scope.row)'></OperationIcon>
                  <OperationIcon v-show='permission.remove' type='danger' content='Xóa' placement='top-start' icon-name='el-icon-delete' 
                    @click='onDeleteEntity(scope.$index, scope.row, deleteApi)'></OperationIcon>
                  <OperationIcon v-show='permission.view' type='info' content='Lịch sử' placement='top-start' icon-name='el-icon-info' 
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
            :page-sizes='[20, 50, 100, inspectionCheckTotal]'
            :page-size='20'
            layout='total, sizes, prev, pager, next, jumper'
            :total='inspectionCheckTotal'>
          </el-pagination>
        </el-col>
      </el-row>
      <!-- Thanh phân trang     Kết thúc -->     </div>

      <!-- Hộp thoại điền báo cáo -->
      <el-dialog
        :title="typeName"
        :visible.sync="dialogVisible"
        v-if="dialogVisible"
        width="75%"
        :before-close="handleClose">
            <div slot="title" class="dialog-header">
          {{ typeName }}
          <OperationIcon
          v-show="flags"
            type="primary"
            text="Sửa"
            placement="top-start"
            icon-name="el-icon-edit"
            @click="switchEdits"
          ></OperationIcon>
        </div>
         <div v-if="this.type==='0'">
            <el-form :model='bizFormModel.inspectionCheckInfo' :rules='formRules' 
              ref='bizFormModel' label-width='120px' label-position='right' class='edit-form'> 
              <!-- <el-card class="box-card main-card"> -->
            
              <div style="margin-left:-60px">  
                      <el-row >
                <el-col :span='24/4'>
                  <el-form-item label='Họ tên' prop='name' >
                    <el-input :disabled='true' v-model='bizFormModel.inspectionCheckInfo.name' :maxlength='120'  >
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span='24/4'>
                  <el-form-item label='Thời gian lấy mẫu' prop='samplingDate' >
                    <!-- <el-input :disabled='true' v-model='bizFormModel.samplingDate'></el-input> -->
                    <el-date-picker
                      :disabled="flags"
                      v-model="bizFormModel.inspectionCheckInfo.samplingDate"
                      type="datetime"
                      placeholder="Chọn ngày giờ"
                      value-format="yyyy-MM-dd HH:mm:ss"
                      format="dd/MM/yyyy HH:mm:ss">
                    </el-date-picker>
                  </el-form-item>
                </el-col>
                 <el-col :span='24/4'>
                  <el-form-item label='Số mẫu' prop='samplingNum' >
                    <el-input placeholder="Vui lòng nhập nội dung" :disabled="flags" v-model='bizFormModel.inspectionCheckInfo.samplingNum' :maxlength='100'></el-input>     
                  </el-form-item>
                </el-col>
                 <el-col :span='24/4'>
                  <el-form-item label='Thiết bị xét nghiệm' prop='instrument' >
                    <el-input placeholder="Vui lòng nhập nội dung" :disabled="flags" v-model='bizFormModel.inspectionCheckInfo.instrument' :maxlength='100'></el-input>     
                  </el-form-item>
                </el-col>
                </el-row>
                <el-row>
                 <el-col :span='24/4'>
                  <el-form-item label='Số xét nghiệm' prop='checkoutNum' >
                    <el-input placeholder="Vui lòng nhập nội dung" :disabled="flags" v-model='bizFormModel.inspectionCheckInfo.checkoutNum' :maxlength='100'></el-input>     
                  </el-form-item>
                </el-col>
                <el-col :span='24/4'>
                  <el-form-item label='Thời gian xét nghiệm' prop='checkoutDate' >
                    <el-date-picker
                      :disabled="flags"
                      v-model="bizFormModel.inspectionCheckInfo.checkoutDate"
                      type="datetime"
                      placeholder="Chọn ngày giờ"
                      value-format="yyyy-MM-dd HH:mm:ss"
                      format="dd/MM/yyyy HH:mm:ss">
                    </el-date-picker>    
                  </el-form-item>
                </el-col>
                 <el-col :span='24/4'>
                  <el-form-item label='Khoa xét nghiệm' prop='checkoutOffice' >
                    <el-input placeholder="Vui lòng nhập nội dung" :disabled="flags" v-model='bizFormModel.inspectionCheckInfo.checkoutOffice' :maxlength='100'></el-input>     
                  </el-form-item>
                </el-col>
              </el-row>
              </div>
            
            <!-- </el-card> -->
            <br>
             <el-table
              :data="bizFormModel.inspectionCheckInfo.inspectionCheckDetails"
              style="width: 100%">
               <el-table-column
                type="index"
                width="50"
                label="Số thứ tự">
              </el-table-column>
              <el-table-column
                prop="costItem.itemName"
                label="Tên hạng mục"
                width="width">
              </el-table-column>
              <el-table-column
                prop="conclusion"
                label="Kết quả"
                width="width">
                <template slot-scope="scope">
                  <el-input placeholder="Vui lòng nhập nội dung" :disabled="flags" v-model='scope.row.conclusion' :maxlength='100'  >
                    </el-input>
                </template>
              </el-table-column>
               <el-table-column
                prop="sign"
                label="Dấu cao/thấp"
                width="width">
                <template slot-scope="scope">
                     <el-select :disabled="flags" v-model="scope.row.sign" placeholder="Vui lòng chọn">
                        <el-option
                          v-for="item in signList"
                          :key="item.value"
                          :label="item.name"
                          :value="item.value">
                        </el-option>
                      </el-select>
                </template>
              </el-table-column>
              <el-table-column
                prop="costItem.costItemPackages[0].referenceValue"
                label="Dấu tham khảo"
                width="width">
                <template slot-scope="scope">
                  <!-- {{scope.row.costItemPackages[0].referenceValue.split("\n")}} -->
                  <!-- <el-input :disabled="true" type="textarea" v-model='scope.row.costItem.costItemPackages[0].referenceValue'   >
                    </el-input> -->
                     <span v-if="scope.row.costItem.costItemPackages.length>0">
                        <el-input :disabled="true" type="textarea" v-model='scope.row.costItem.costItemPackages[0].referenceValue'   >
                    </el-input>
                     </span>
                    
                </template>
              </el-table-column>
              <el-table-column
                prop="costItem.costItemPackages[0].referenceUnit"
                label="Đơn vị"
                width="width">
              </el-table-column>
             
               <el-table-column
                prop="describes"
                label="Mô tả"
                width="width">
                <template slot-scope="scope">
                  <el-input :disabled="flags" v-model='scope.row.describes' :maxlength='100'  >
                    </el-input>
                </template>
              </el-table-column>
            </el-table>
            <br>
            <div style="margin-left:-50px">
              <el-form-item label='Kết luận báo cáo' prop='reportConclusion' >
                <el-input
                :disabled="flags"
                  type="textarea"
                   :autosize="{ minRows: 2, maxRows: 100}"
                  placeholder="Vui lòng nhập nội dung"
                  v-model="bizFormModel.inspectionCheckInfo.reportConclusion">
                </el-input>
            </el-form-item>
            </div>
            <br>
              <!-- <el-form-item label="" prop="uploadFile">
              <el-upload
                class="avatar-uploader"
                 action="#"
                :show-file-list="switchEdit===0"
                :on-change="handleLicensePreview"
                :before-upload="beforeLicenseUpload"
                :on-remove="handleRemove"
                :auto-upload="false"
                :on-exceed="handleExceed"
                :limit="1"
                :on-error="hanldeError"
                :on-preview="handlePreview"
                accept=".jpg,.jpeg,.png,.pdf,.JPG,.JPEG"

              >          
             
                <el-button v-if="photoId==null||photoId==='' || switchEdit===0" slot="trigger" size="mini" type="primary">Tải tệp lên</el-button>           
              </el-upload>
               <img v-if="bizFormModel.uploadFile" :src="bizFormModel.uploadFile" class="avatar">
               <i v-else class="el-icon-plus avatar-uploader-icon"></i> 
                  <el-button  size="mini" type="primary" v-if="bizFormModel.uploadFile && switchEdit===1"  @click="look(bizFormModel.uploadFile)">Xem trước tệp</el-button>
            </el-form-item> -->
            <el-form-item label='' prop='' >
              <div style="margin-left:-50px">
              <upload-file  :objectId="uploadFiles" @getFileList="getFileList" :action="flags" @deleteFile="deleteFile"></upload-file>
              </div>
            </el-form-item>
          </el-form>
          
         </div>
          <div v-if="this.type==='1'">
            <el-form :model='bizFormModel.inspectionCheckInfo' :rules='formRules' 
              ref='bizFormModel' label-width='120px' label-position='right' class='edit-form'> 
              <!-- <el-card class="box-card main-card"> -->
            
              <div style="margin-left:-60px">  
                      <el-row >
                <el-col :span='24/4'>
                  <el-form-item label='Họ tên' prop='name' >
                    <el-input :disabled='true' v-model='bizFormModel.inspectionCheckInfo.name' :maxlength='120'  >
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span='24/4'>
                  <el-form-item label='Thời gian đăng ký' prop='samplingDate' >
                    <!-- <el-input :disabled='true' v-model='bizFormModel.samplingDate'></el-input> -->
                    <el-date-picker
                      :disabled="flags"
                      v-model="bizFormModel.inspectionCheckInfo.samplingDate"
                      type="datetime"
                      placeholder="Chọn ngày giờ"
                      value-format="yyyy-MM-dd HH:mm:ss"
                      format="dd/MM/yyyy HH:mm:ss">
                    </el-date-picker>
                  </el-form-item>
                </el-col>
                 <el-col :span='24/4'>
                  <el-form-item label='Thiết bị xét nghiệm' prop='instrument' >
                    <el-input :disabled="flags" placeholder="Vui lòng nhập nội dung" v-model='bizFormModel.inspectionCheckInfo.instrument' :maxlength='100'></el-input>     
                  </el-form-item>
                </el-col>
                <el-col :span='24/4'>
                  <el-form-item label='Loại kiểm tra' prop='checkType' >
                    <el-input :disabled="flags" placeholder="Vui lòng nhập nội dung" v-model='bizFormModel.inspectionCheckInfo.checkType' :maxlength='100'></el-input>     
                  </el-form-item>
                </el-col>
                
                </el-row>
                <el-row>
                   <el-col :span='24/4'>
                  <el-form-item label='Vị trí kiểm tra' prop='checkPart' >
                    <el-input :disabled="flags" placeholder="Vui lòng nhập nội dung" v-model='bizFormModel.inspectionCheckInfo.checkPart' :maxlength='100'></el-input>     
                  </el-form-item>
                </el-col>
                   <el-col :span='24/4'>
                  <el-form-item label='Bác sĩ báo cáo' prop='doctor' >
                    <el-input :disabled="flags" placeholder="Vui lòng nhập nội dung" v-model='bizFormModel.inspectionCheckInfo.doctor' :maxlength='100'></el-input>     
                  </el-form-item>
                </el-col>
                 <el-col :span='24/4'>
                  <el-form-item label='Hạng mục kiểm tra' prop='checkProject' >
                    <el-input :disabled="flags" placeholder="Vui lòng nhập nội dung" v-model='bizFormModel.inspectionCheckInfo.checkProject' :maxlength='100'></el-input>     
                  </el-form-item>
                </el-col>
                <el-col :span='24/4'>
                  <el-form-item label='Thời gian báo cáo' prop='checkoutDate' >
                    <el-date-picker
                      :disabled="flags"
                      v-model="bizFormModel.inspectionCheckInfo.checkoutDate"
                      type="datetime"
                      placeholder="Chọn ngày giờ"
                      value-format="yyyy-MM-dd HH:mm:ss"
                      format="dd/MM/yyyy HH:mm:ss">
                    </el-date-picker>    
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row>
                <el-col :span="24/1">
                  <el-form-item label='Kết quả kiểm tra thấy' prop='checkAdvise' >
                    <el-input type="textarea" width='100%' :disabled="flags" v-model='bizFormModel.inspectionCheckInfo.checkAdvise'  :maxlength='100'></el-input>     
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row>
                <el-col :span="24/1">
                  <el-form-item label='Ý kiến' prop='opinion' >
                    <el-input type="textarea" width='100%' :disabled="flags" v-model='bizFormModel.inspectionCheckInfo.opinion'  :maxlength='100'></el-input>     
                  </el-form-item>
                </el-col>
              </el-row>
              <!-- <el-form-item label="" prop="uploadFile">
              <el-upload
                class="avatar-uploader"
                 action="#"
                :show-file-list="switchEdit===0"
                :on-change="handleLicensePreview"
                :before-upload="beforeLicenseUpload"
                :on-remove="handleRemove"
                :auto-upload="false"
                :on-exceed="handleExceed"
                :limit="1"
                :on-error="hanldeError"
                :on-preview="handlePreview"
                accept=".jpg,.jpeg,.png,.pdf,.JPG,.JPEG"

              >   
                   
                <el-button v-if="photoId==null||photoId==='' || switchEdit===0" slot="trigger" size="mini" type="primary">Tải tệp lên</el-button>           
              </el-upload>
              <img v-if="bizFormModel.uploadFile" :src="bizFormModel.uploadFile" class="avatar">
               <i v-else class="el-icon-plus avatar-uploader-icon"></i>  
                  <el-button  size="mini" type="primary" v-if="bizFormModel.uploadFile && switchEdit===1"  @click="look(bizFormModel.uploadFile)">Xem trước tệp</el-button>
            </el-form-item> -->
             <el-form-item label='' prop='' >
              <div >
              <upload-file  :objectId="uploadFiles" @getFileList="getFileList" :action="flags" @deleteFile="deleteFile"></upload-file>
              </div>
            </el-form-item>
              </div>
            </el-form>
            </div>
        <span slot="footer" class="dialog-footer">
          <el-button v-if="!flags" @click="dialogVisible = false">Hủy</el-button>
          <el-button type="primary" v-if="!flags" @click="save">Lưu</el-button>
        </span>
      </el-dialog>

  </el-row>
</template>

<script>
import { getPhotoById,getFiled } from "@/api/sys/sysSeting"
import { listInspectionCheckPage, getInspectionCheckById, deleteInspectionCheck } from '@/api/cure/inspectionCheck'
import listViewMixin from '@/mixins/listViewMixin'
import {getInspectionCheckInfoByInspecId,saveInspectionCheckInfo} from '@/api/cure/inspectionCheckInfo'
import {getInspectionCheckDetailById} from '@/api/cure/inspectionCheckDetail'
import InspectionCheckForm from './inspectionCheckForm'
import ExportExcelButton from '@/components/ExportExcelButton'
import ViewColumnsSelect from '@/views/components/ViewColumnsSelect'
import {getCostItemById} from '@/api/treatment/costItem'
import QueryForm from '@/views/components/queryForm'
import MainUI from '@/views/components/mainUI'
import BaseUI from "@/views/components/baseUI";
import OperationIcon from '@/components/OperationIcon'
import History from '@/views/components/history'
import UploadFile from '../../components/uploadFile.vue'
let Base64 = require('js-base64').Base64
export default {
  extends: MainUI,BaseUI,
  mixins: [listViewMixin],
  components: {
    InspectionCheckForm,
    ExportExcelButton,
    ViewColumnsSelect,
    QueryForm,
    OperationIcon,
    History,
    UploadFile
  },
  data() {
    return {
      // listViewMixin Cấu hình
      listApi: listInspectionCheckPage,
      getApi: getInspectionCheckById,
      deleteApi: deleteInspectionCheck,
      entityName: 'InspectionCheck',
      permissionPrefix: 'inspectionCheck',

      queryTypes: {
        'name': 'like',
      },
      queryModel: {
        'patientName': '',   // Tên hạng mục
        'completeDate':'',  //Thời gian lập phiếu
        'status':'',    //Trạng thái
        'completeBy':'',  //Bác sĩ kê đơn
      },
      switchEdit:0,  //Nút sửa tệp tải lên
      inspectionCheckTotal: 0,
      inspectionCheckList: [],
      fileName:'',  //Tên tệp
      photoId:null,
      oprColumnWidth: 140,  // Độ rộng cột thao tác
      tableId: '1212102786706588488',
      schemeId: '1212102786706588513',
      type:"",//Loại báo cáo
      typeName:"",  //Tên báo cáo
      dialogVisible:false,
      bizFormModel:{
        fileIdFile:[],   //Tệp
        uploadFile:[],  //Tệp
        inspectionCheckInfo:{}
      },     //Điền đóng gói báo cáo
      inspectionIndex:0,    //Chỉ số nhấn bảng chính
      checkIndex:0,    //Chỉ số nhấn bảng chính
      inspectionCheckTableData:[],   //Đóng gói báo cáo hạng mục điều trị
      conclusion:[],  //Kết quả
      describe:[],    //Mô tả
      sign:[],       //Đánh dấu
      flags:false,
      doctorList:[],  //Mảng bác sĩ
      signList:[
        {
          name:'Cao',
          value:1,
        },{
          name:'Bình thường',
          value:0
        },{
          name:'Thấp',
          value:2
        }
      ],
      statusList:[
        {
          name:'Chờ điền',
          value:'0'
        },
        {
          name:'Đã điền',
          value:"1"
        }
      ],
      uploadFiles:'',
      fileIds:[],
      flags:false,  //Ngăn gửi trùng lặp
    }
  },
  methods: {
    //Đặt lại
    reset(){
       this.queryModel={
        'patientName': '',   // Tên hạng mục
        'completeDate':'',  //Thời gian lập phiếu
        'status':'',    //Trạng thái
        'completeBy':'',  //Bác sĩ kê đơn
      }
      this.loadData()
    },
    // CRUD buttons in the template call these names; previously they did not
    // exist, so View/Edit/Add did nothing. Delegate to the form component.
    onCreateInspectionCheck() {
      this.$refs.inspectionCheckForm.openAddInspectionCheckDialog()
    },
    onViewInspectionCheck(index, row) {
      this.setLoad()
      getInspectionCheckById(row.id).then(responseData => {
        if (responseData.code === 100) {
          this.$refs.inspectionCheckForm.openViewInspectionCheckDialog(responseData.data)
        } else { this.showMessage(responseData) }
        this.resetLoad()
      }).catch(error => { this.outputError(error) })
    },
    onEditInspectionCheck(index, row) {
      this.setLoad()
      getInspectionCheckById(row.id).then(responseData => {
        if (responseData.code === 100) {
          this.$refs.inspectionCheckForm.openEditInspectionCheckDialog(responseData.data)
        } else { this.showMessage(responseData) }
        this.resetLoad()
      }).catch(error => { this.outputError(error) })
    },
    // Copy icon in the row template calls this name; the form component already
    // provides openCopyInspectionCheckDialog, but no index method existed, so
    // Sao chép did nothing. Same get-by-id pattern as View/Edit above.
    onCopyInspectionCheck(index, row) {
      this.setLoad()
      getInspectionCheckById(row.id).then(responseData => {
        if (responseData.code === 100) {
          this.$refs.inspectionCheckForm.openCopyInspectionCheckDialog(responseData.data)
        } else { this.showMessage(responseData) }
        this.resetLoad()
      }).catch(error => { this.outputError(error) })
    },
    getFileList(fileList){
      this.bizFormModel.fileIdFile=[]
      this.bizFormModel.uploadFile=[]
      this.bizFormModel.uploadFile=fileList
      for (let i = 0; i < fileList.length; i++) {
       this.bizFormModel.fileIdFile.push(fileList[i].raw)
      }
    },
    deleteFile(fileIds){
      this.fileIds.push(fileIds)
    },
    //Sửa
    switchEdits(){
      this.flags=false
      this.switchEdit=0
    },
    
    //Xem trước
    look(row){
     
      //  var blob = this.dataURLtoBlob(row)
      //   const elink = document.createElement('a')
      //   // Đặt tên tệp tải xuống
      //   const timedate = Date.parse(new Date())
      //   elink.download = this.fileName
      //   elink.style.display = 'none'
      //   elink.href = URL.createObjectURL(blob)
      //   document.body.appendChild(elink)
      //   elink.click()
      //   document.body.removeChild(elink)
     window.open(process.env.KK_FILE_URL + encodeURIComponent(Base64.encode(row)));
    },
    // Sẽbase64Chuyển đổi thànhblob
    dataURLtoBlob: function (dataurl) {
      const arr = dataurl.split(',')
      const mime = arr[0].match(/:(.*?);/)[1]
      const bstr = atob(arr[1])
      let n = bstr.length
      const u8arr = new Uint8Array(n)
      while (n--) {
        u8arr[n] = bstr.charCodeAt(n)
      }
      return new Blob([u8arr], { type: mime })
    },
    getPhoto(id) {
      if(!id){
        this.bizFormModel.uploadFile = ''
      }else{
        getFiled(id).then((res) => {
         const src = `data:text/plain;base64,${res.base64Str}`;
        // let name = res.name.substring(0,res.name.lastIndexOf("."))
        let name = res.name
        this.fileName=name
        this.bizFormModel.uploadFile = src;
        return src;
      });
      }
    },
    save(){
     if(this.flags){
       return
     }
     this.flags=true
    //  let userHeaderFile = new FormData();
      //  userHeaderFile.append("entity", JSON.stringify(this.bizFormModel.inspectionCheckInfo));
      // userHeaderFile.append("deleteIds",JSON.stringify([this.photoId]));
      // return
       this.setLoad()
     if(this.bizFormModel.fileIdFile.length>0){
        this.bizFormModel.fileIdFile.forEach(item=>{
      //  let userHeaderFile = {};
      let userHeaderFile = new FormData();
         userHeaderFile.append("entity", JSON.stringify(this.bizFormModel.inspectionCheckInfo));
        userHeaderFile.append("deleteIds",JSON.stringify(this.fileIds));
        userHeaderFile.append("fileIdUploads",item);
        //  userHeaderFile.entity=JSON.stringify(this.bizFormModel.inspectionCheckInfo);
        // userHeaderFile.deleteIds=JSON.stringify([this.photoId]);
        // userHeaderFile.fileIdUploads=item;
     
      saveInspectionCheckInfo(userHeaderFile).then((res)=>{
          if(res.code===100){
            this.$message.success("Thao tác thành công!")
            this.loadData()
          }else{
            this.$message.error("Lưu thất bại!")
          }
          this.dialogVisible=false
          this.resetLoad()
          this.flags=false
      }).catch((error)=>{
        this.$message.error("Thực hiện thất bại, Vui lòng liên hệ quản trị viên xử lý!")
        this.flags=false
      })
      })
     }else{
        let userHeaderFile = new FormData();
         userHeaderFile.append("entity", JSON.stringify(this.bizFormModel.inspectionCheckInfo));
        userHeaderFile.append("deleteIds",JSON.stringify(this.fileIds));
        userHeaderFile.append("fileIdUploads",this.bizFormModel.fileIdFile);
        //  userHeaderFile.entity=JSON.stringify(this.bizFormModel.inspectionCheckInfo);
        // userHeaderFile.deleteIds=JSON.stringify([this.photoId]);
        // userHeaderFile.fileIdUploads=item;
     
      saveInspectionCheckInfo(userHeaderFile).then((res)=>{
          if(res.code===100){
            this.$message.success("Thao tác thành công!")
            this.dialogVisible=false
            this.loadData()
          }
      }).catch((error)=>{
        this.$message.error("Thực hiện thất bại, Vui lòng liên hệ quản trị viên xử lý!")
      })
     }
    },
    handleRemove(file, fileList){
      this.bizFormModel.uploadFile=""
      this.bizFormModel.fileIdFile=""
    },
    handleExceed(file, fileList){
      // this.$message.warning(`Giới hạn chọn hiện tại 3 tệp, Lần này đã chọn ${file.length} tệp, Đã chọn tổng cộng ${file.length + fileList.length} tệp`);
      this.$message.warning(`Hiện chỉ hỗ trợ tải lên một tệp duy nhất`);
    },
    hanldeError(err, file, fileList){
    },
    handlePreview(file){
      // Tạo thẻ a
      const link = document.createElement('a');
      // downloadThuộc tính
      link.setAttribute('download', file.name);
      // hrefLiên kết
      link.setAttribute('href', file.operationManual);
      // Tự kích hoạt sự kiện click
      link.click();
    },
    // Tải tệp lên
    handleLicensePreview(file,fileList) {
      const isLt2M = file.size / 1024 / 1024 < 20;
      if (!isLt2M) {
        this.$message.error(this.inter.sctpdx + " 20MB!");
      }else if(fileList.length>1){
        this.$message.error("Hiện chỉ hỗ trợ tải lên một tệp");
        return false;
      }
       else {
        // Lưu tệp, Tải lên thống nhất khi nhấn nút xác nhận

        this.bizFormModel.uploadFile = URL.createObjectURL(file.raw);
        this.bizFormModel.fileIdFile = file.raw;
      }
    },
    //Giới hạn tải lên
    beforeLicenseUpload(file) {
      const fileSuffix = file.name.substring(file.name.lastIndexOf(".") + 1);
       const whiteList = ["jpg", "jpeg", "png", "gif", "JPG", "JPEG","rar","zip","doc","docx","pdf", "xls", "xlsx", "ppt","txt"];

      if (whiteList.indexOf(fileSuffix) === -1) {
        this.$message.error("Định dạng tệp không được phép");
        return false;
      }
      const isLt2M = file.size / 1024 / 1024 < 20;
      if (!isLt2M) {
        this.$message.error(" Vui lòng tải lên tệp nhỏ hơn 20MB!");
        return false;
      }
     
    },

    fill(row,index,flag){
      this.fileIds=[]
      this.inspectionCheckTableData=[]
      this.bizFormModel={
        fileIdFile:[],   //Tệp
        uploadFile:[],  //Tệp
        inspectionCheckInfo:{}
      }
      if(row.type==="0"){
        // Qua hạng mục có phải hạng mục đóng gói không
        getInspectionCheckInfoByInspecId(row.id).then((res)=>{
          if(res.code===100){
            this.bizFormModel.inspectionCheckInfo=res.data
            this.typeName="Nhập kết quả xét nghiệm"
            this.type="0"
            this.inspectionIndex=index
             if(flag===1){
              this.flags=true
              this.switchEdit=1
            }else{
              this.flags=false
              this.switchEdit=0
            }
            
            let sex=row.sex==="gender_0"?"Nam":"Nữ"
            this.bizFormModel.inspectionCheckInfo.name=row.patientName+"/"+sex+"/"+row.patient.age+"tuổi"
            this.getPhoto(this.bizFormModel.inspectionCheckInfo.fileId)

            this.photoId=this.bizFormModel.inspectionCheckInfo.fileId
            this.uploadFiles=this.bizFormModel.inspectionCheckInfo.id
            this.dialogVisible=true
          }else{
            this.$message.error("Dữ liệu hệ thống bất thường, Vui lòng thử lại hoặc liên hệ quản trị viên!")
          }
        }).catch((error)=>{
          this.$message.error("Lấy thông tin kiểm tra thất bại, Vui lòng thử lại")
        })
      }else{
         // Qua hạng mục có phải hạng mục đóng gói không
        getInspectionCheckInfoByInspecId(row.id).then((res)=>{
          if(res.code===100){
            this.bizFormModel.inspectionCheckInfo=res.data
            this.typeName="Nhập kết quả kiểm tra"
            this.type="1"
            this.inspectionIndex=index
             if(flag===1){
              this.flags=true
              this.switchEdit=1
            }else{
              this.flags=false
              this.switchEdit=0
            }
            
            let sex=row.patient.gender==="gender_0"?"Nam":"Nữ"
            this.bizFormModel.inspectionCheckInfo.name=row.patientName+"/"+sex+"/"+row.patient.age+"tuổi"
            this.getPhoto(this.bizFormModel.inspectionCheckInfo.fileId)
            this.photoId=this.bizFormModel.inspectionCheckInfo.fileId
            this.uploadFiles=this.bizFormModel.inspectionCheckInfo.id
            this.dialogVisible=true
          }else{
            this.$message.error("Dữ liệu hệ thống bất thường, Vui lòng thử lại hoặc liên hệ quản trị viên!")
          }
        }).catch((error)=>{
          this.$message.error("Lấy thông tin kiểm tra thất bại, Vui lòng thử lại")
        })
      }
      //this.dialogVisible=true
    },
    appendSearchParams() {
      this.search.params.push({
        columnName: 'company_id',
        queryType: '=',
        value: currentUser.company.id
      })
      if (this.moreCodition) {
        this.search.params = this.search.params.concat(this.compositeCondition())
      } else {
        this.search.params.push({
          columnName: 'patient_name',
          queryType: 'like',
          value: this.queryModel.patientName
        })
        this.search.params.push({
          columnName: 'complete_by',
          queryType: 'like',
          value: this.queryModel.completeBy
        })
        this.search.params.push({
          columnName: 'status',
          queryType: '=',
          value: this.queryModel.status
        })
        if (this.queryModel.completeDate && this.queryModel.completeDate.length) {
          this.search.params.push({
            logic: "AND",
            queryType: "("
          }, {
            columnName: "complete_date",
            logic: "",
            queryType: 'between',
            value: this.queryModel.completeDate,
          }, {
            logic: "",
            queryType: ")"
          })
        }
      }
    },
    handleListResponse(responseData) {
      this.inspectionCheckTotal = responseData.data.total
      this.inspectionCheckList = responseData.data.rows
      let doctorArr = []
      this.doctorList = []
      if (responseData.data.rows) {
        this.inspectionCheckList.forEach(element => {
          doctorArr.push(element.completeBy)
        })
        doctorArr = [...new Set(doctorArr)]
        doctorArr.forEach(element => {
          this.doctorList.push({ name: element, value: '' })
        })
      }
    },
    loadData() {
      this.setLoad()
      this.search.params = []
      if (this.appendSearchParams) {
        this.appendSearchParams()
      }
      if (this.tableId) {
        this.pushDataPermissions(this.search.params, this.$route.meta.routerId, this.tableId)
      }
      this.listApi(this.search).then(responseData => {
        if (responseData.code === 100) {
          this.handleListResponse(responseData)
        } else {
          this.showMessage(responseData)
        }
        this.resetLoad()
      }).catch(error => {
        this.outputError(error)
      })
    },
    initOptions() {
    }
  },
  watch: {
      // tableDataCóel-tableDữ liệu đã liên kết
      tableData: {
          // Khắc phục lỗi hiển thị lệch bảng
          handler () {
              this.$nextTick(() => {
                  // tableRefCóel-tableĐã liên kếtrefGiá trị thuộc tính
                  this.$refs.tableRef.doLayout()// Đối với Table Thực hiện bố cục lại
              })
          },
          deep: true
      }
  },
  updated(){
    if(this.$refs.tableRef){
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
.avatar {
    width: 50%;
    height: 50%;
    display: block;
  }

  
</style>
