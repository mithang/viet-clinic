<template>
  <el-dialog :title='dialogProps.title' :visible.sync='dialogProps.visible' :close-on-click-modal='false' fullscreen :lock-scroll='true'
    @open='onDialogOpen()' v-loading='loading'>
    <div slot='title' class='dialog-header'>
      <el-row :span='24'>
        <el-col :span='10'>
          {{ dialogProps.title }}
          <OperationIcon v-show='dialogProps.action === "view" && permission.edit' type='primary' text='Sửa' placement='top-start' icon-name='el-icon-edit' @click='switchEdit'></OperationIcon>
        </el-col>
        <el-col :span='14'>
          <el-tabs v-model='tabIndex'>
            <el-tab-pane label='Thông tin cơ bản' name='1'></el-tab-pane>
            <el-tab-pane label='Điều kiện ràng buộc' name='2'></el-tab-pane>
            <el-tab-pane label='Cấu hình lưu trữ' name='3'></el-tab-pane>
            <el-tab-pane label='JavaCấu hình' name='4'></el-tab-pane>
            <el-tab-pane label='Cấu hình danh sách' name='5'></el-tab-pane>
            <el-tab-pane label='Cấu hình chỉnh sửa' name='6'></el-tab-pane>
          </el-tabs>
        </el-col>
      </el-row>
    </div>

    <el-form :model='bizFormModel' :rules='formRules'
      ref='genTableForm' label-width='120px' label-position='right' class='edit-form'>
    <!-- Biểu mẫu chính  Bắt đầu-->
      <div v-show='tabIndex==="1"'>
      <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Tên' prop='name'>
             <el-input v-if='dialogProps.action === "view" || dialogProps.action === "edit"' :disabled=true v-model='bizFormModel.name'></el-input>
             <el-select v-else v-model='bizFormModel' value-key='name' @change='onTableChange' filterable clearable placeholder='Vui lòng chọn tên'>
               <el-option v-for='st in st_List' :key='st.name' :label='st.name + " : " + st.comments' :value='st'></el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span='24/2'>
          <el-form-item label='Giải thích' prop='comments'>
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.comments'  :maxlength='500'  :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập giải thích"'  ></el-input>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Tên lớp thực thể' prop='className'>
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.className'  :maxlength='100'  :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập tên lớp thực thể"'  ></el-input>
          </el-form-item>
        </el-col>
        <el-col :span='24/2'>
          <el-form-item label='Bảng cha liên kết' prop='parentTable.id'>
            <el-input v-if='dialogProps.action === "view"' :disabled=true v-model='bizFormModel.parentTable.name'></el-input>
            <el-select v-else v-model='bizFormModel.parentTable' value-key='id' filterable clearable placeholder='Vui lòng chọn bảng cha liên kết' @clear='bizFormModel.parentTable={"id": null,"name": null}'>
               <el-option v-for='parentTable in parentTable_List' :key='parentTable.id' :label='parentTable.name + " : " + parentTable.comments' :value='parentTable'></el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row v-show='bizFormModel.parentTable && bizFormModel.parentTable.name'>
        <el-col :span='24/2'>
          <el-form-item label='Khóa ngoại bảng cha' prop='parentTableFk'>
            <el-input v-if='dialogProps.action === "view"' :disabled=true v-model='bizFormModel.parentTableFk'></el-input>
            <el-select v-else v-model='bizFormModel.parentTableFk' @change='onFkChange' value-key='name' filterable clearable placeholder='Vui lòng chọn khóa ngoại của bảng cha'>
               <el-option v-for='parentTableFk in parentTableFk_List' :key='parentTableFk.name' :label='parentTableFk.name + " : " + parentTableFk.comments' :value='parentTableFk.name'></el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span='24/2'>
          <el-form-item label='là bảng liên kết' prop='isAssociation'>
            <el-switch :disabled='dialogProps.action === "view"' v-model='bizFormModel.isAssociation' active-color='#13ce66' inactive-color='#dbdfe6' active-value='1' inactive-value='0'></el-switch>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span='24/2'>
          <el-form-item label='Trường sắp xếp' prop='orderColumns'>
            <StringMultiSelect :disabled='dialogProps.action === "view"' v-model='bizFormModel.orderColumns' :opts='orderColumns_List' valKey='name' label='comments' placeholder='Vui lòng chọn trường sắp xếp'></StringMultiSelect>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col>
          <el-form-item label='Điều kiện lọc' prop='filter'>
            <el-input :disabled='dialogProps.action === "view"' v-model='bizFormModel.filter' type='textarea'
              :maxlength='2000' :placeholder='dialogProps.action === "view"? "" : "Vui lòng nhập điều kiện lọc"'  ></el-input>
          </el-form-item>
        </el-col>
      </el-row>
      </div>
               <!-- Biểu mẫu chính  Kết thúc-->

    <!--Bảng con: Trường ràng buộc Bắt đầu-->
      <div v-show='tabIndex==="2"'>
      <el-row>
        <el-col>
    <el-table ref='genTable' :data='bizFormModel.constraintList' border @current-change='(currentRow, oldCurrentRow) => {constraintCurrentRow = currentRow}'>
      <el-table-column prop='name' label='Tên' min-width='180px' header-align='center'>
        <template slot='header'><span style="color:#F56C6C;margin-right: 4px">*</span>Tên</template>
        <template slot-scope='{row,$index}'>
          <el-form-item v-if='dialogProps.action !== "view" && row === constraintCurrentRow' :prop="`constraintList.${$index}.name`" :rules='formRules.constraint_name' label-width=0>
            <el-input v-model='row.name'  :maxlength='200' clearable placeholder='Vui lòng nhập tên'></el-input>
          </el-form-item>
          <span v-else>{{row.name}}</span>
        </template>
      </el-table-column>
      <el-table-column prop='comments' label='Mô tả' min-width='200px' header-align='center'>
        <template slot='header'>Mô tả</template>
        <template slot-scope='{row,$index}'>
          <el-form-item v-if='dialogProps.action !== "view" && row === constraintCurrentRow' :prop="`constraintList.${$index}.comments`" :rules='formRules.constraint_comments' label-width=0>
            <el-input v-model='row.comments'  :maxlength='500' clearable placeholder='Vui lòng nhập mô tả'></el-input>
          </el-form-item>
          <span v-else>{{row.comments}}</span>
        </template>
      </el-table-column>
      <el-table-column prop='constraintColumn' label='Trường ràng buộc' min-width='180px' header-align='center'>
        <template slot='header'>Trường ràng buộc</template>
        <template slot-scope='{row,$index}'>
          <el-form-item v-if='dialogProps.action !== "view" && row === constraintCurrentRow' :prop="`constraintList.${$index}.constraintColumn`" :rules='formRules.constraint_constraintColumn' label-width=0>
            <StringMultiSelect v-model='row.constraintColumn' :opts='constraintColumn_Constraint_List' valKey='name' label='name' placeholder='Vui lòng chọn trường ràng buộc'></StringMultiSelect>
          </el-form-item>
          <span v-else>{{row.constraintColumn}}</span>
        </template>
      </el-table-column>
      <el-table-column v-if='dialogProps.action !== "view"' label='Thao tác' header-align='center' align='center' width='60px'>
        <template slot-scope='scope'>
          <el-tooltip class='item' effect='light' content='Xóa' placement='top-start'>
            <i class='el-icon-delete' style='color:#F56C6C;cursor:pointer;'
              @click='onDeleteRow(scope.$index, bizFormModel.constraintList)'></i>
          </el-tooltip>
        </template>
      </el-table-column>
    </el-table>
    <el-button v-if='dialogProps.action !== "view"' type='primary'
      @click='onAddConstraintRow(bizFormModel.constraintList)'>Thêm</el-button>
        </el-col>
      </el-row>
      </div>
      <!--Bảng con: Trường ràng buộc Kết thúc-->
    <!--Bảng con: Trường bảng nghiệp vụ Bắt đầu-->
      <div v-show='tabIndex>"2"'>
      <el-row>
        <el-col>
    <el-table ref='genTable' :data='bizFormModel.genTableColumnList' border @current-change='(currentRow, oldCurrentRow) => {genTableColumnCurrentRow = currentRow}'>
      <el-table-column prop='name' label='Tên trường' min-width='180px' header-align='center' fixed>
        <template slot-scope='{row,$index}'>
          <el-form-item v-if='dialogProps.action !== "view" && row === genTableColumnCurrentRow' :prop="`genTableColumnList.${$index}.name`" :rules='formRules.genTableColumn_name' label-width=0>
            <el-input v-model='row.name'  :maxlength='2000' clearable placeholder='Vui lòng nhập tên trường'></el-input>
          </el-form-item>
          <span v-else>{{row.name}}</span>
        </template>
      </el-table-column>
        <el-table-column v-if='tabIndex==="3"' key='comments' prop='comments' label='Giải thích' min-width='200px' header-align='center'>
        <template slot='header'>Giải thích</template>
        <template slot-scope='{row,$index}'>
          <el-form-item v-if='dialogProps.action !== "view" && row === genTableColumnCurrentRow' :prop="`genTableColumnList.${$index}.comments`" :rules='formRules.genTableColumn_comments' label-width=0>
            <el-input v-model='row.comments'  :maxlength='500' clearable placeholder='Vui lòng nhập giải thích'></el-input>
          </el-form-item>
          <span v-else>{{row.comments}}</span>
        </template>
      </el-table-column>
      <el-table-column v-if='tabIndex==="3"' key='jdbcType' prop='jdbcType' label='Kiểu dữ liệu vật lý' min-width='160px' header-align='center'></el-table-column>
      <el-table-column v-if='tabIndex==="4"' key='javaType.value' prop='javaType.value' label='JAVALoại' min-width='100px' header-align='center'>
        <template slot='header'><span style="color:#F56C6C;margin-right: 4px">*</span>JAVALoại</template>
        <template slot-scope='{row,$index}'>
          <el-form-item v-if='dialogProps.action !== "view" && row === genTableColumnCurrentRow' :prop="`genTableColumnList.${$index}.javaType`" :rules='formRules.genTableColumn_javaType' label-width=0>
            <el-select v-model='row.javaType' value-key='value' @change='onJavaTypeChange' filterable clearable placeholder='Vui lòng chọnJAVALoại' @focus='setCurrentFocus(row)'>
              <el-option v-for='javaType in javaType_GenTableColumn_List' :key='javaType.value' :label='javaType.name' :value='javaType'></el-option>
            </el-select>
          </el-form-item>
          <span v-else>{{row.javaType.name}}</span>
        </template>
      </el-table-column>
        <el-table-column v-if='tabIndex==="4"' key='customType.id' prop='customType.id' label='Loại tùy chỉnh' min-width='180px' header-align='center'>
        <template slot='header'>Loại tùy chỉnh</template>
        <template slot-scope='{row,$index}'>
          <el-form-item v-if='dialogProps.action !== "view" && row === genTableColumnCurrentRow' :prop="`genTableColumnList.${$index}.customType`" :rules='formRules.genTableColumn_customType' label-width=0>
            <el-select v-model='row.customType' value-key='id' filterable clearable placeholder='Vui lòng chọn kiểu tuỳ chỉnh' @focus='setCurrentFocus(row)'>
              <el-option v-for='customType in customType_GenTableColumn_List' :key='customType.id' :label='customType.name' :value='customType'></el-option>
            </el-select>
          </el-form-item>
          <span v-else>{{row.customType.name}}</span>
        </template>
      </el-table-column>
        <el-table-column v-if='tabIndex==="4"' key='javaField' prop='javaField' label='Tên thuộc tính' min-width='180px' header-align='center'>
        <template slot='header'><span style="color:#F56C6C;margin-right: 4px">*</span>Tên thuộc tính</template>
        <template slot-scope='{row,$index}'>
          <el-form-item v-if='dialogProps.action !== "view" && row === genTableColumnCurrentRow' :prop="`genTableColumnList.${$index}.javaField`" :rules='formRules.genTableColumn_javaField' label-width=0>
            <el-input v-model='row.javaField'  :maxlength='1024' clearable placeholder='Vui lòng nhập tên thuộc tính'></el-input>
          </el-form-item>
          <span v-else>{{row.javaField}}</span>
        </template>
      </el-table-column>
        <el-table-column v-if='tabIndex==="4"' key='defVal' prop='defVal' label='Giá trị mặc định' min-width='160px' header-align='center'>
        <template slot='header'>Giá trị mặc định</template>
        <template slot-scope='{row,$index}'>
          <el-form-item v-if='dialogProps.action !== "view" && row === genTableColumnCurrentRow' :prop="`genTableColumnList.${$index}.defVal`" :rules='formRules.genTableColumn_defVal' label-width=0>
            <el-input v-model='row.defVal'  :maxlength='2000' clearable placeholder='Vui lòng nhập giá trị mặc định'></el-input>
          </el-form-item>
          <span v-else>{{row.defVal}}</span>
        </template>
      </el-table-column>
        <el-table-column v-if='tabIndex==="3"' key='isPk' prop='isPk' label='Khóa chính' min-width='60px' header-align='center' align='center'>
        <template slot='header'>Khóa chính</template>
        <template slot-scope='{row,$index}'>
          <el-form-item v-if='dialogProps.action !== "view"' :prop="`genTableColumnList.${$index}.isPk`" :rules='formRules.genTableColumn_isPk' label-width=0>
            <el-switch v-model='row.isPk' active-color='#13ce66' inactive-color='#dbdfe6' active-value='1' inactive-value='0'></el-switch>
          </el-form-item>
            <li v-if='dialogProps.action === "view" && row.isPk === "1"' class='el-icon-check' style='color:#F56C6C;'></li>
        </template>
      </el-table-column>
        <el-table-column v-if='tabIndex==="3"' key='isNull'  prop='isNull' label='Có thể trống' min-width='60px' header-align='center' align='center'>
        <template slot='header'>Có thể trống</template>
        <template slot-scope='{row,$index}'>
          <el-form-item v-if='dialogProps.action !== "view"' :prop="`genTableColumnList.${$index}.isNull`" :rules='formRules.genTableColumn_isNull' label-width=0>
            <el-switch v-model='row.isNull' active-color='#13ce66' inactive-color='#dbdfe6' active-value='1' inactive-value='0'></el-switch>
          </el-form-item>
            <li v-if='dialogProps.action === "view" && row.isNull === "1"' class='el-icon-check' style='color:#F56C6C;'></li>
        </template>
      </el-table-column>
        <el-table-column v-if='tabIndex==="3"' key='isInsert' prop='isInsert' label='Có thể chèn' min-width='60px' header-align='center' align='center'>
        <template slot='header'>Có thể chèn</template>
        <template slot-scope='{row,$index}'>
          <el-form-item v-if='dialogProps.action !== "view"' :prop="`genTableColumnList.${$index}.isInsert`" :rules='formRules.genTableColumn_isInsert' label-width=0>
            <el-switch v-model='row.isInsert' active-color='#13ce66' inactive-color='#dbdfe6' active-value='1' inactive-value='0'></el-switch>
          </el-form-item>
            <li v-if='dialogProps.action === "view" && row.isInsert === "1"' class='el-icon-check' style='color:#F56C6C;'></li>
        </template>
      </el-table-column>
        <el-table-column v-if='tabIndex==="6"' key='isEdit' prop='isEdit' label='Có thể sửa' min-width='60px' header-align='center' align='center'>
        <template slot='header'>Có thể sửa</template>
        <template slot-scope='{row,$index}'>
          <el-form-item v-if='dialogProps.action !== "view"' :prop="`genTableColumnList.${$index}.isEdit`" :rules='formRules.genTableColumn_isEdit' label-width=0>
            <el-switch v-model='row.isEdit' active-color='#13ce66' inactive-color='#dbdfe6' active-value='1' inactive-value='0'></el-switch>
          </el-form-item>
            <li v-if='dialogProps.action === "view" && row.isEdit === "1"' class='el-icon-check' style='color:#F56C6C;'></li>
        </template>
      </el-table-column>
        <el-table-column v-if='tabIndex==="5"' key='isList' prop='isList' label='Hiển thị cột' min-width='60px' header-align='center' align='center'>
        <template slot='header'>Hiển thị cột</template>
        <template slot-scope='{row,$index}'>
          <el-form-item v-if='dialogProps.action !== "view"' :prop="`genTableColumnList.${$index}.isList`" :rules='formRules.genTableColumn_isList' label-width=0>
            <el-switch v-model='row.isList' active-color='#13ce66' inactive-color='#dbdfe6' active-value='1' inactive-value='0'></el-switch>
          </el-form-item>
            <li v-if='dialogProps.action === "view" && row.isList === "1"' class='el-icon-check' style='color:#F56C6C;'></li>
        </template>
      </el-table-column>
        <el-table-column v-if='tabIndex==="5"' key='width' prop='width' label='Độ rộng cột' min-width='80px' header-align='center' align='right'>
        <template slot='header'><span style="color:#F56C6C;margin-right: 4px">*</span>Độ rộng cột</template>
        <template slot-scope='{row,$index}'>
          <el-form-item v-if='dialogProps.action !== "view" && row === genTableColumnCurrentRow' :prop="`genTableColumnList.${$index}.width`" :rules='formRules.genTableColumn_width' label-width=0>
            <el-input-number v-model='row.width' controls-position='right' :controls='false' style='width:100%;' placeholder='Vui lòng nhập độ rộng cột'></el-input-number>
          </el-form-item>
          <span v-else>{{row.width}}</span>
        </template>
      </el-table-column>
        <el-table-column v-if='tabIndex==="5"' key='isQuery' prop='isQuery' label='Truy vấn' min-width='60px' header-align='center' align='center'>
        <template slot='header'>Truy vấn</template>
        <template slot-scope='{row,$index}'>
          <el-form-item v-if='dialogProps.action !== "view"' :prop="`genTableColumnList.${$index}.isQuery`" :rules='formRules.genTableColumn_isQuery' label-width=0>
            <el-switch v-model='row.isQuery' active-color='#13ce66' inactive-color='#dbdfe6' active-value='1' inactive-value='0'></el-switch>
          </el-form-item>
            <li v-if='dialogProps.action === "view" && row.isQuery === "1"' class='el-icon-check' style='color:#F56C6C;'></li>
        </template>
      </el-table-column>
        <el-table-column v-if='tabIndex==="5"' key='queryType.value' prop='queryType.value' label='Cách truy vấn' min-width='120px' header-align='center'>
        <template slot='header'>Cách truy vấn</template>
        <template slot-scope='{row,$index}'>
          <el-form-item v-if='dialogProps.action !== "view" && row === genTableColumnCurrentRow' :prop="`genTableColumnList.${$index}.queryType`" :rules='formRules.genTableColumn_queryType' label-width=0>
            <el-select v-model='row.queryType' value-key='value' filterable clearable placeholder='Vui lòng chọn cách truy vấn' @focus='setCurrentFocus(row)'>
              <el-option v-for='queryType in queryType_GenTableColumn_List' :key='queryType.value' :label='queryType.name' :value='queryType'></el-option>
            </el-select>
          </el-form-item>
          <span v-else>{{row.queryType.name}}</span>
        </template>
      </el-table-column>
        <el-table-column v-if='tabIndex==="5"' key='queryVal' prop='queryVal' label='Điều kiện truy vấn' min-width='180px' header-align='center'>
        <template slot='header'>Điều kiện truy vấn</template>
        <template slot-scope='{row,$index}'>
          <el-form-item v-if='dialogProps.action !== "view" && row === genTableColumnCurrentRow' :prop="`genTableColumnList.${$index}.queryVal`" :rules='formRules.genTableColumn_queryVal' label-width=0>
            <el-input v-model='row.queryVal'  :maxlength='2000' clearable placeholder='Vui lòng nhập điều kiện truy vấn'></el-input>
          </el-form-item>
          <span v-else>{{row.queryVal}}</span>
        </template>
      </el-table-column>
        <el-table-column v-if='tabIndex==="6"' key='showType.value' prop='showType.value' label='Điều khiển' min-width='180px' header-align='center'>
        <template slot='header'>Điều khiển</template>
        <template slot-scope='{row,$index}'>
          <el-form-item v-if='dialogProps.action !== "view" && row === genTableColumnCurrentRow' :prop="`genTableColumnList.${$index}.showType`" :rules='formRules.genTableColumn_showType' label-width=0>
            <el-select v-model='row.showType' value-key='value' filterable clearable placeholder='Vui lòng chọn loại điều khiển' @focus='setCurrentFocus(row)'>
              <el-option v-for='showType in showType_GenTableColumn_List' :key='showType.value' :label='showType.name' :value='showType'></el-option>
            </el-select>
          </el-form-item>
          <span v-else>{{row.showType.name}}</span>
        </template>
      </el-table-column>
        <el-table-column v-if='tabIndex==="6"' key='interaction' prop='interaction' label='Biến liên kết' min-width='200px' header-align='center'>
        <template slot='header'>Biến liên kết</template>
        <template slot-scope='{row,$index}'>
          <el-form-item v-if='dialogProps.action !== "view" && row === genTableColumnCurrentRow' :prop="`genTableColumnList.${$index}.interaction`" :rules='formRules.genTableColumn_interaction' label-width=0>
            <StringMultiSelect v-model='row.interaction' :opts='interaction_GenTableColumn_List' valKey='name' label='comments' placeholder='Vui lòng chọn biến liên động'></StringMultiSelect>
          </el-form-item>
          <span v-else>{{row.interaction}}</span>
        </template>
      </el-table-column>
        <el-table-column v-if='tabIndex==="6"' key='interactionVal' prop='interactionVal' label='Giá trị liên kết' min-width='200px' header-align='center'>
        <template slot='header'>Giá trị liên kết</template>
        <template slot-scope='{row,$index}'>
          <el-form-item v-if='dialogProps.action !== "view" && row === genTableColumnCurrentRow' :prop="`genTableColumnList.${$index}.interactionVal`" :rules='formRules.genTableColumn_interactionVal' label-width=0>
            <el-input v-model='row.interactionVal'  :maxlength='2000' clearable placeholder='Vui lòng nhập giá trị liên động'></el-input>
          </el-form-item>
          <span v-else>{{row.interactionVal}}</span>
        </template>
      </el-table-column>
        <el-table-column v-if='tabIndex==="6"' key='parameters' prop='parameters' label='Tham số liên kết' min-width='200px' header-align='center'>
        <template slot='header'>Tham số liên kết</template>
        <template slot-scope='{row,$index}'>
          <el-form-item v-if='dialogProps.action !== "view" && row === genTableColumnCurrentRow' :prop="`genTableColumnList.${$index}.parameters`" :rules='formRules.genTableColumn_parameters' label-width=0>
            <el-input v-model='row.parameters'  :maxlength='2000' clearable placeholder='Vui lòng nhập tham số liên động'></el-input>
          </el-form-item>
          <span v-else>{{row.parameters}}</span>
        </template>
      </el-table-column>
        <el-table-column v-if='tabIndex==="5"' key='sort' prop='sort' label='Sắp xếp' min-width='80px' header-align='center' align='right'>
        <template slot='header'>Sắp xếp</template>
        <template slot-scope='{row,$index}'>
          <el-form-item v-if='dialogProps.action !== "view" && row === genTableColumnCurrentRow' :prop="`genTableColumnList.${$index}.sort`" :rules='formRules.genTableColumn_sort' label-width=0>
            <el-input-number v-model='row.sort' controls-position='right' :controls='false' style='width:100%;' placeholder='Vui lòng nhập thứ tự'></el-input-number>
          </el-form-item>
          <span v-else>{{row.sort}}</span>
        </template>
      </el-table-column>
      <el-table-column v-if='dialogProps.action !== "view"' label='Thao tác' header-align='center' align='center' width='60px'>
        <template slot-scope='scope'>
          <el-tooltip class='item' effect='light' content='Xóa' placement='top-start'>
            <i class='el-icon-delete' style='color:#F56C6C;cursor:pointer;'
              @click='onDeleteRow(scope.$index, bizFormModel.genTableColumnList)'></i>
          </el-tooltip>
        </template>
      </el-table-column>
    </el-table>
    <el-button v-if='dialogProps.action !== "view"' type='primary'
      @click='onAddGenTableColumnRow(bizFormModel.genTableColumnList)'>Thêm</el-button>
      <el-button v-if='dialogProps.action !== "view"' :plain='true' @click='onRefreshColumn()'>Làm mới</el-button>
        </el-col>
      </el-row>
      </div>
      <!--Bảng con: Trường bảng nghiệp vụ Kết thúc-->
    </el-form>

    <!-- Nút  Bắt đầu-->
    <span slot='footer' class='dialog-footer'>
      <el-button v-if='dialogProps.action !== "view"' :disabled="flag" type='primary'  @click='onSubmit("genTableForm")'>Lưu</el-button>
      <el-button v-if='dialogProps.action !== "view"' :plain='true' @click='onDialogClose()'>Hủy</el-button>
      <el-button v-if='dialogProps.action === "view"' :plain='true' @click='onDialogClose()'>Đóng</el-button>
    </span>
    <!-- Nút Kết thúc-->
  </el-dialog>
</template>

<script>
import { listSchemaColumnAll } from '@/api/gen/schemaColumn'
import { listSchemaTableAll } from '@/api/gen/schemaTable'
import { listSchemaConstraintAll } from '@/api/gen/schemaConstraint'
import { validatenull } from '@/utils/validate'
import StringMultiSelect from '@/components/StringMultiSelect'

import { listGenTableAll } from '@/api/gen/genTable'
import '@/api/gen/genTableColumn';
import '@/api/sys/dictItem';
import { getDictItemsByCode } from '@/utils/dictCache';
import { listGenSchemeAll } from '@/api/gen/genScheme'

import { saveGenTable } from '@/api/gen/genTable'
import BaseUI from '@/views/components/baseUI'
import OperationIcon from '@/components/OperationIcon'
export default {
  extends: BaseUI,
  name: 'genTable-form',
  components: {
    StringMultiSelect,
    OperationIcon
  },
  data() {
    return {
      tabIndex: '1',
      bizFormModel: this.initFormModel(),
          st_List: [],  // Danh sách bảng cơ sở dữ liệu
          parentTable_List: [],  // Bảng cha liên kết
          parentTableFk_List: [],  // Khóa ngoại bảng cha
    orderColumns_List: [],        // Danh sách trường sắp xếp
    constraintColumn_Constraint_List: [],    // Trường ràng buộc
    javaType_GenTableColumn_List: [],    // JAVALoại
    customType_GenTableColumn_List: [],    // Loại tùy chỉnh
    queryType_GenTableColumn_List: [],    // Cách truy vấn
    showType_GenTableColumn_List: [],    // Điều khiển
    interaction_GenTableColumn_List: [],    // Biến liên kết
    flag:false,
       dialogProps: {
        visible: false,
        action: '',
        title: '',

      },
      formRules: {
        // Xác thực bảng chính
        'name': [
            { required: true, message: 'Vui lòng nhập tên', trigger: 'blur' }
        ],
        'className': [
            { required: true, message: 'Vui lòng nhập tên lớp thực thể', trigger: 'blur' }
        ],
        // Xác thực bảng con Trường ràng buộc
        'constraint_name': [
            { required: true, message: 'Vui lòng nhập tên', trigger: 'blur' }
        ],
        // Xác thực bảng con Trường bảng nghiệp vụ
        'genTableColumn_name': [
            { required: true, message: 'Vui lòng nhập tên trường', trigger: 'blur' }
        ],
        'genTableColumn_jdbcType': [
            { required: true, message: 'Vui lòng nhập kiểu dữ liệu vật lý', trigger: 'blur' }
        ],
        'genTableColumn_javaType': [
            { required: true, message: 'Vui lòng nhậpJAVALoại', trigger: 'blur' }
        ],
        'genTableColumn_javaField': [
            { required: true, message: 'Vui lòng nhập tên thuộc tính', trigger: 'blur' }
        ],
        'genTableColumn_width': [
            { required: true, message: 'Vui lòng nhập độ rộng cột', trigger: 'blur' }
        ],
      },
    constraintCurrentRow: {},     // Bảng con Trường ràng buộc Dòng đang chọn
    genTableColumnCurrentRow: {},     // Bảng con Trường bảng nghiệp vụ Dòng đang chọn
    }
  },
  props: {
    // Quyền
    permission: {
      type: Object
    }
  },
  methods: {
    setCurrentFocus(row) {
      if (this.$refs.genTable) {
        this.$refs.genTable.setCurrentRow(row)
      }
    },
    onSubmit(formName) {
      this.flag=true
      this.$refs[formName].validate(valid => {
        if (valid) {
          this.doSave()
        } else {
          this.flag=false
          return false
        }
      });
    },
    doSave() {
      this.setLoad()
      saveGenTable(this.bizFormModel).then(responseData => {
        this.flag=false
        if(responseData.code === 100) {
          this.dialogProps.visible = false
          this.$emit('save-finished')
        } else {
          this.showMessage(responseData)
        }
        this.resetLoad()
      }).catch(error => {
        this.flag=false
        this.outputError(error)
      })
    },
    onAddConstraintRow(tableData) {
        tableData.push({
            'genTable': {     // Bảng cha - mã bảng sở hữu
              'id': validatenull(parent) || validatenull(parent.genTable) ? null : parent.genTable.id,
              'name': validatenull(parent) || validatenull(parent.genTable) ? null : parent.genTable.name,
            },
            'name': '',   // Tên
            'comments': '',   // Mô tả
            'constraintColumn': '',   // Trường ràng buộc
            'remarks': '',   // Thông tin ghi chú
        })
    },
    onAddGenTableColumnRow(tableData) {
        tableData.push({
            'genTable': {     // Bảng cha - mã bảng
              'id': validatenull(parent) || validatenull(parent.genTable) ? null : parent.genTable.id,
              'name': validatenull(parent) || validatenull(parent.genTable) ? null : parent.genTable.name,
            },
            'name': '',   // Tên trường
            'comments': '',   // Giải thích
            'jdbcType': '',   // Kiểu vật lý
            'javaType': {     // JAVALoại
              'value': '',
              'name': '',
            },
            'customType': {     // Loại tùy chỉnh
              'id': null,
              'name': '',
            },
            'javaField': '',   // Tên thuộc tính
            'defVal': '',   // Giá trị mặc định
            'isPk': '',   // Khóa chính
            'isNull': '',   // Có thể trống
            'isInsert': '',   // Có thể chèn
            'isEdit': '',   // Có thể sửa
            'isList': '',   // Hiển thị cột
            'width': 0,    // Độ rộng cột
            'isQuery': '',   // Truy vấn
            'queryType': {     // Cách truy vấn
              'value': '',
              'name': '',
            },
            'queryVal': '',   // Điều kiện truy vấn
            'showType': {     // Điều khiển
              'value': '',
              'name': '',
            },
            'interaction': '',   // Biến liên kết
            'parameters': '',   // Tham số liên kết
            'settings': '', // Cài đặt khác (JSON trường mở rộng)
            'sort': 0,    // Sắp xếp
            'remarks': '',   // Thông tin ghi chú
        })
    },
    onDeleteRow(index, tableData) {
        tableData.splice(index, 1)
    },
    onDialogClose() {
      this.dialogProps.visible = false
    },
    onDialogOpen() {
      this.$nextTick(() => {
        this.$refs['genTableForm'].clearValidate()
      })
    },
    switchEdit() {
      this.dialogProps.action = 'edit'
      this.dialogProps.title = 'Sửa bảng nghiệp vụ'
      this.initOptions(this.bizFormModel)
    },
    initFormModel() {
      return {
        'name': '',   // Tên
        'comments': '',   // Giải thích
        'className': '',   // Tên lớp thực thể
        'parentTable': {     // Bảng cha liên kết
          'id': null,
          'name': null,
        },
        'parentTableFk': '',   // Khóa ngoại bảng cha
        'isAssociation': '',   // là bảng liên kết
        'filter': '',   // Điều kiện lọc
        'orderColumns': '',
        'remarks': '',   // Thông tin ghi chú

        constraintList: [],       // Danh sách bảng con
        genTableColumnList: [],       // Danh sách bảng con
      }
    },
    initOptions(This) {
      // Bảng chính
      let st_search = {
        params: []
      }
      this.st_List = []
      listSchemaTableAll(st_search).then(responseData => {
        this.st_List = responseData.data
      })
      let parentTable_search = {
        params: []
      }
      this.parentTable_List = []
      listGenTableAll(parentTable_search).then(responseData => {
        this.parentTable_List = responseData.data
      })
      //Dùng chung với danh sách
      if(!validatenull(This) && !validatenull(This.genTableColumnList)){
        this.parentTableFk_List = This.genTableColumnList
        this.orderColumns_List = this.genOderColumns(This.genTableColumnList)
        this.constraintColumn_Constraint_List = This.genTableColumnList
	      this.interaction_GenTableColumn_List = This.genTableColumnList
      }


      // Bảng con  Trường bảng nghiệp vụ
      This = this.genTableColumnCurrentRow
      getDictItemsByCode('javaType').then((data) => {
        this.javaType_GenTableColumn_List = data
      })
      let customType_search = {
        params: []
      }
      this.customType_GenTableColumn_List = []
      listGenSchemeAll(customType_search).then(responseData => {
        this.customType_GenTableColumn_List = responseData.data
      })
      getDictItemsByCode('queryType').then((data) => {
        this.queryType_GenTableColumn_List = data
      })
      getDictItemsByCode('showType').then((data) => {
        this.showType_GenTableColumn_List = data
      })
      let interaction_search = {
        params: []
      }
    },
    setRowClassName({row, rowIndex}) {
      row.sort = (rowIndex + 1) * 10
    },
    async onTableChange(newValue) {
      if(validatenull(newValue)){
        this.parentTableFk_List = []
        this.orderColumns_List = []
        this.constraintColumn_Constraint_List = []
        this.interaction_GenTableColumn_List = []
        this.bizFormModel = this.initFormModel()
      }else{
        this.loading = true
        // Khởi tạo tên class
        this.bizFormModel.className = this.bizFormModel.name.substring(0, 1).toUpperCase() +  this.bizFormModel.name.substring(1).replace(/\_(\w)/g, function(all, letter){
          return letter.toUpperCase()
        })
        try {
          let search = {
            params: [{
              columnName: 'table_name',
              queryType: '=',
              value: newValue.name
            }]
          }
          let [columnRespData, constraintRespData] = await Promise.all([
            listSchemaColumnAll(search),
            listSchemaConstraintAll(newValue.name)
          ])
          if(columnRespData.code === 100 && constraintRespData.code === 100) {
            this.bizFormModel.isAssociation = '0'
            this.bizFormModel.genTableColumnList = columnRespData.data
            this.parentTableFk_List = this.bizFormModel.genTableColumnList
            this.orderColumns_List = this.genOderColumns(this.bizFormModel.genTableColumnList)
            this.constraintColumn_Constraint_List = this.bizFormModel.genTableColumnList
            this.interaction_GenTableColumn_List = this.bizFormModel.genTableColumnList
            this.bizFormModel.constraintList = constraintRespData.data
          } else {
            this.showMessage(columnRespData.code !== 100 ? columnRespData : constraintRespData)
          }
          this.loading = false
        } catch(error) {
          this.outputError(error)
        }
      }
    },
    onFkChange(newValue) {
      if(newValue && newValue !== 'id') {
        this.bizFormModel.genTableColumnList.forEach(function(item){
          if(item.name === newValue && item.javaType.value !== 'Custom') {
            item.javaType = {value: 'Custom', name:'Custom'}
            item.javaField = item.simpleJavaField + '.id|name'
            item.showType = {value: 'Select', name: 'Bộ chọn danh sách thả xuống'}
          }
        })
      }
    },
    onJavaTypeChange(javaType) {
      if(javaType && (javaType.value === 'This' || javaType.value === 'Custom')) {
        if(this.genTableColumnCurrentRow.simpleJavaField.substring(this.genTableColumnCurrentRow.simpleJavaField.length - 2, this.genTableColumnCurrentRow.simpleJavaField.length) === 'Id') {
          this.genTableColumnCurrentRow.javaField = this.genTableColumnCurrentRow.simpleJavaField.substring(0, this.genTableColumnCurrentRow.simpleJavaField.length - 2) + '.id|name'
        } else {
          this.genTableColumnCurrentRow.javaField = this.genTableColumnCurrentRow.simpleJavaField + '.id|name'
        }
        this.genTableColumnCurrentRow.showType = {value: 'Select', name: 'Bộ chọn danh sách thả xuống'}
      }else {
        this.genTableColumnCurrentRow.javaField = this.genTableColumnCurrentRow.simpleJavaField
        this.genTableColumnCurrentRow.showType = {value: 'SingleInput', name: 'Ô nhập một dòng'}
      }
    },
    onRefreshColumn() {
      if(!validatenull(this.bizFormModel.name)) {
        this.loading = true
        let search = {
          params: [{
            columnName: 'table_name',
            queryType: '=',
            value: this.bizFormModel.name
          }]
        }
        listSchemaColumnAll(search).then(responseData => {
          if(responseData.code === 100) {
            responseData.data.forEach((item)=>{
              let noFound = true
              for(let i = 0; i < this.bizFormModel.genTableColumnList.length; i++) {
                let it = this.bizFormModel.genTableColumnList[i]
                if(item.name === it.name) {
                  it.jdbcType = item.jdbcType
                  noFound = false
                  break
                }
              }
              if(noFound === true) {
                this.bizFormModel.genTableColumnList.push(item)
              }
            })
            this.loading = false
          } else {
            this.showMessage(responseData)
          }
        })
      }
    },
    genOderColumns(columns) {
      let list = []
      columns.forEach((col) => {
        list.push({name: 'a.' + col.name + ' ASC', comments: col.comments + ' tăng dần'})
        list.push({name: 'a.' + col.name + ' DESC', comments: col.comments + ' giảm dần'})
      })
      return list
    },
    openViewGenTableDialog(genTable) {
      this.dialogProps.action = 'view'
      this.dialogProps.title = 'Xem bảng nghiệp vụ'
      this.tabIndex = "1"
      this.bizFormModel = genTable
      this.dialogProps.visible = true
    },
    openEditGenTableDialog(genTable) {
      this.dialogProps.action = 'edit'
      this.dialogProps.title = 'Sửa bảng nghiệp vụ'
      this.tabIndex = "1"
      this.bizFormModel = genTable
      this.initOptions()
      this.dialogProps.visible = true
    },
    openAddGenTableDialog() {
      this.dialogProps.action = 'add'
      this.dialogProps.title = 'Thêm bảng nghiệp vụ'
      this.tabIndex = "1"
      this.bizFormModel = this.initFormModel()
      this.initOptions()
      this.dialogProps.visible = true
    },
    openCopyGenTableDialog(genTable) {
      this.dialogProps.action = 'edit'
      this.dialogProps.title = 'Thêm bảng nghiệp vụ'
      this.tabIndex = "1"
      this.bizFormModel = genTable
      this.initOptions()
      this.bizFormModel.id = null
      for (let i = 0; i <= this.bizFormModel.constraintList.length - 1; i++) {
          this.bizFormModel.constraintList[i].id = null
      }
      for (let i = 0; i <= this.bizFormModel.genTableColumnList.length - 1; i++) {
          this.bizFormModel.genTableColumnList[i].id = null
      }
      this.dialogProps.visible = true
    },
  },
}
</script>
