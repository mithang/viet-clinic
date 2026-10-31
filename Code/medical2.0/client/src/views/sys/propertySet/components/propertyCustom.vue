<template>
  <div>
    <el-row style="margin-bottom: 10px">
      <el-col :span="21">
        <div style="font-weight: 600;">Định nghĩa thuộc tính</div>
      </el-col>
      <el-col :span="2">
        <el-button type='primary' :disabled="action === 'view'" @click="pushCustomTeam">Thêm nhóm mới</el-button>
      </el-col>
    </el-row>
    <el-collapse ref="collapseTrigger">
      <el-collapse-item :name="bizindex" v-for="(panel, bizindex) in propertyPanels" :key="bizindex" >
        <template slot="title" v-if="action !== 'view' && panel.isTeam !== '0'">
          <template @click.stop="collClick" >
            <i class="el-icon-remove-outline" @click.stop="deletePropertyData(bizindex, panel, propertyPanels)"></i>
            <el-form-item :data-num='dataNum' :ref="`PRO_${dataNum}_propertyPanels_${bizindex}_name`" style="margin-top: 16px" v-if='action !== "view" && panel.isTeam !== "0"' :prop='`PRO_${dataNum}_propertyPanels_${bizindex}_name`' :rules="[{ required: true, validator: inputValidator, val: panel.name,  message: ' ', trigger: 'blur'}]" >
              <el-input v-model="panel.name" @focus="nameFocus" @blur="nameBlur"  placeholder="Vui lòng nhập tên nhóm nội bộ"/>
            </el-form-item>
          </template>
        </template>
        <template slot="title" v-else>
          <span style="margin-left: 30px;">{{panel.name ? panel.name : 'Nhóm mặc định'}}</span>
        </template>
        <el-table   class='drag_table'   row-key="id" ref="dragTable" :data='panel.propertys' border highlight-current-row :row-class-name="tableRowClassName">
          <el-table-column prop='name' label='Tên trường'  header-align='center'>
            <template slot='header' slot-scope='{row,$index}'><span style="color:#F56C6C;margin-right: 4px">*</span>Tên trường</template>
            <template slot-scope='{row,$index}'>
              <el-form-item :data-num='dataNum' :ref="`PRO_${dataNum}_propertys_${$index}_name`" :prop='`PRO_${dataNum}_propertys_${$index}_name`' :rules="[{ required: true, validator: inputValidator, val: row.name,  message: 'Vui lòng nhập tên trường', trigger: 'blur'}]"  v-if='action !== "view" '  label-width=0>
                <el-input v-model="row.name" />
              </el-form-item>
              <span v-else >{{row.name}}</span>
            </template>
          </el-table-column>
          <el-table-column prop="dictId" label="Thuộc tính"  header-align='center'>
            <template slot='header'><span style="color:#F56C6C;margin-right: 4px">*</span>Thuộc tính</template>
            <template slot-scope='{row,$index}'>
              <el-form-item :data-num='dataNum' label-width=0 v-if='action !== "view" ' :ref="`PRO_${dataNum}_propertys_${$index}_dictId`" :prop='`PRO_${dataNum}_propertys_${$index}_dictId`' :rules="[{ required: true, validator: inputValidator, val: row.dictId,  message: 'Vui lòng chọn bộ thuộc tính', trigger: ['blur', 'change']}]">
                <el-select v-model="row.dictId" value-key='id' filterable clearable placeholder='Vui lòng chọn loại thuộc tính' @change="changeDictId(row)">
                  <el-option v-for="item in options" :key="item.id" :label="item.name" :value="item.id" :disabled="item.disabled"/>
                </el-select>
              </el-form-item>
              <span v-else>{{getCheckedName(row.dictId, options)}}</span>
            </template>
          </el-table-column>
          <el-table-column prop="bo" label="Đối tượng nghiệp vụ" width="100" header-align='center'>
            <template slot-scope='{row,$index}'>
              <el-form-item :data-num='dataNum' label-width=0  v-if='action !== "view" ' :ref="`PRO_${dataNum}_propertys_${$index}_bo`" :prop='`PRO_${dataNum}_propertys_${$index}_bo`' :rules="{required: (row.dictId === '8606' || row.dictId === '8406' || row.dictId === '8405') ? true : false, validator: inputValidator, val: row.bo, message:'Vui lòng chọn đối tượng nghiệp vụ',trigger: ['blur', 'change']}">
                <el-select v-model="row.bo" value-key='id' v-if="row.dictId === '8406'" filterable @change="(val) => getDictChildren(val,row)" clearable placeholder='Vui lòng chọn đối tượng nghiệp vụ' >
                  <el-option v-for="item in businessOptions" :key="item.id" :label="item.name" :value="item.id" :disabled="item.disabled"/>
                </el-select>
                <el-select v-model="row.bo" value-key='id' v-else-if="row.dictId === '8606'" filterable clearable placeholder='Vui lòng chọn loại thuộc tính' >
                  <el-option v-for="item in propertySetS" :key="item.id" :label="item.name" :value="item.id" :disabled="item.disabled"/>
                </el-select>
                <el-input v-model="row.bo" v-else-if="row.dictId === '8405'" />
                <el-input v-model="row.bo" v-else :disabled="true" />
              </el-form-item>
              <span v-else-if="row.dictId === '8406'">{{getCheckedName(row.bo,businessOptions)}}</span>
              <span v-else-if="row.dictId === '8606'">{{getCheckedName(row.bo,propertySetS)}}</span>
              <span v-else>{{row.bo}}</span>
            </template>
          </el-table-column>
          <el-table-column prop='isRequired' label='Bắt buộc nhập' width="80" header-align='center'>
            <template slot-scope='{row,$index}'>
              <el-form-item :data-num='dataNum' v-if='action !== "view" ' label-width=0>
                <el-switch v-model='row.isRequired' active-color='#13ce66' inactive-color='#dbdfe6' active-value='1' inactive-value='0'></el-switch>
              </el-form-item>
              <li v-else-if='row.isRequired === "1"' class='el-icon-check' style='color:#F56C6C;'></li>
            </template>
          </el-table-column>
          <el-table-column prop="defaultValue" label="Giá trị mặc định"  header-align='center' >
            <template slot-scope='{row,$index}'>
              <template v-if="row.dictId === '8406'"> <!-- Từ điển -->
                <el-form-item :data-num='dataNum' v-if='action !== "view" ' label-width=0>
                  <el-select :loading="defLoading" v-model="row.defaultValue" value-key='id' filterable clearable placeholder='Vui lòng chọn loại thuộc tính' @focus="checkChange(row)" >
                    <el-option v-for="item in defValOptions" :key="item.id" :label="item.name" :value="item.id" :disabled="item.disabled"/>
                  </el-select>
                </el-form-item>
                <span v-else>{{getCheckedName(row.defaultValue,defValOptions)}}</span>
              </template>

              <template v-else-if=" row.dictId === '8402' || row.dictId === '8403'">     <!--  8402Số nguyên  "8403" Giá trị số -->
                <el-form-item :data-num='dataNum'  v-if='action !== "view" ' label-width=0>
                  <number-input  v-model='row.defaultValue' :precision="row.dictId === '8402'? 0 : 4"></number-input>
                </el-form-item>
                <span v-else>{{row.defaultValue}}</span>
              </template>

              <template v-else-if="row.dictId === '8404'">     <!--  8404 Số tiền -->
                <el-form-item :data-num='dataNum'  v-if='action !== "view" ' label-width=0>
                  <number-input  v-model='row.defaultValue' :precision="2"></number-input>
                </el-form-item>
                <span v-else>{{row.defaultValue}}</span>
              </template>

              <template v-else>
                <el-form-item :data-num='dataNum'  v-if='action !== "view" ' label-width=0>
                  <el-input v-model="row.defaultValue" />
                </el-form-item>
                <span v-else>{{row.defaultValue}}</span>
              </template>
            </template>
          </el-table-column>
          <el-table-column prop='remarks' label='Giải thích'  header-align='center'>
            <template slot-scope='{row,$index}'>
              <el-form-item :data-num='dataNum' v-if='action !== "view" ' label-width=0>
                <el-input v-model='row.remarks' :maxlength='128' clearable placeholder='Vui lòng nhập giải thích'></el-input>
              </el-form-item>
              <span v-else>{{row.remarks}}</span>
            </template>
          </el-table-column>
          <el-table-column label='Thao tác' min-width="40px" v-if="action !== 'view'" >
            <template slot='header' slot-scope="scope">
              <el-button circle icon="el-icon-plus" :disabled="action === 'view'" @click="pushPropertyData(panel, bizindex)"></el-button>
            </template>
            <template slot-scope='scope'>
              <OperationIcon type='danger' content='Xóa' placement='top-start' icon-name='el-icon-delete'
                             @click='onDelete(scope.$index, scope.row, panel.propertys)'></OperationIcon>
            </template>
          </el-table-column>
        </el-table>
      </el-collapse-item>
    </el-collapse>
  </div>
</template>

<script>
  import { listDictItemAll } from '@/api/sys/dictItem'
  import { listDictTypeAll } from '@/api/sys/dictType'
  import { listPropertySetAll } from  "@/api/sys/propertySet"
  import OperationIcon from '@/components/OperationIcon'
  import { uuid } from 'vue-uuid'
  import Sortable from "sortablejs";

  export default {
    name: "propertyCustom",
    components:{OperationIcon},
    props:{
      value:{
        type:String
      },
      //Loại thao tác--Sửa, Xem v.v.
      action: {
        type: String,
        default: () => { return "add"}
      },
      dataNum: {
        type: String | Number,
        required: true
      }
     },
    watch:{
      // Kích hoạt khi mở lại sau khi đóng cửa sổ
      value(val, oldVal) {
        if(val !== oldVal) {
          if(this.value) {
            this.propertyPanels = JSON.parse(this.value)
          } else {
            this.propertyPanels = this.initPropertyDef()
          }
        }
      },
      propertyPanels: {
        handler(newName, oldName) {
          this.$emit('input', JSON.stringify(this.propertyPanels))
        },
        deep: true
      },
      'action':{
        handler(newVal){
          if(newVal !== 'view') {
            this.getSortable()
          }
        },
      }
    },
    data(){
      let inputValidator= (rule, value, callback) => {
        if(rule.required) {
          if(!rule.val) {
            let msg = rule.message | 'Không được để trống!'
            return callback(new Error(msg))
          }
        }
        callback()
      }
       return{
         propertyPanels:this.initPropertyDef(),
         deleteTeamArr:[], //Nhóm thuộc tính tuỳ chỉnh cần xoá
         deleteArr:[],     //Id thuộc tính tuỳ chỉnh cần xoá
         indexArr:[],  //Mảng chỉ số thẻ đang mở rộng
         tableKey: 1,
         checkRowData:null, // Dữ liệu dòng được chọn
         options:[],        //Giá trị từ điển
         businessOptions: [], //Tất cả mục từ điển
         propertySetS:[],        //Tập hợp bộ thuộc tính
         defValOptions: [],   //Từ điển chọn thuộc tính
         dictIdvalid: false,
         defLoading: false,
         inputValidator: inputValidator,
       }
     },
      mounted() {
        // Giá trị ban đầu
        if(this.value) {
          this.propertyPanels = JSON.parse(this.value)
        }
        this.getOptions();
        if (this.action !== 'view') {
          this.getSortable()
        }
      },
    methods:{
      getSortable() {
        for(let index in this.propertyPanels){
          this.$nextTick(() =>{
            this.initSortable(index,this.propertyPanels[index].propertys);
          });
        }
      },
      initSortable(index,tableData) {
        // Lấy node cha của dòng trong bảng
        const ele = this.$refs["dragTable"][index].$el.querySelector(
          ".el-table__body-wrapper tbody"
        );

        // Tạo instance kéo thả
        let dragTable = Sortable.create(ele, {
          animation: 100, //Hoạt ảnh
          disabled: false, // Kéo thả không khả dụng? false = Kích hoạt (chỉ có tác dụng khi vừa render bảng, sau đó không còn tác dụng)
          handle: ".el-table__row", //Chỉ định mục tiêu kéo thả, chỉ khi nhấn vào mục tiêu này mới kéo được phần tử (ở đây đặt nút thao tác để kéo)
          filter: ".disabled", //Chỉ định class không thể kéo (trong el-table có thể đặt class cho dòng qua row-class-name)
          dragClass: "dragClass", //Đặt class style khi kéo
          ghostClass: "ghostClass", //Đặt class style khi thả
          chosenClass: "chosenClass", //Đặt class style khi được chọn
          // Sự kiện bắt đầu kéo
          onStart: () => {
          },
          // Sự kiện kết thúc kéo
          onEnd: ({newIndex, oldIndex}) => {
              const row = this.propertyPanels[index].propertys.splice(oldIndex,1)[0];
              this.propertyPanels[index].propertys.splice(newIndex,0,row);
          },
        });
      },
      nameFocus() {
        this.collClick = this.$refs.collapseTrigger.setActiveNames;
        this.$refs.collapseTrigger.setActiveNames = () => {}
      },
      nameBlur() {
        this.$refs.collapseTrigger.setActiveNames = this.collClick
      },
      onDelete(index,row,list){     //Xóa
        list.splice(index,1);
      },
      getCheckedName(id,arr){
        let name = "";
        if(id && arr){
          arr.some((item, index) => {
            if(item.id === id){
              name = item.name;
              return true
            }
          })
        }
        return name;
      },
      // Tải các lựa chọn tương ứng khi mở dropdown giá trị mặc định
      checkChange(currentRow){
        this.checkRowData = currentRow;
        if(this.action !== "view" ){
          if(currentRow.dictId === '8406'){
            let dictItem = {
              params: [{'columnName':'dict_type_id', 'queryType': '=', 'value': currentRow.bo}]
            };
            this.defLoading = true;
            listDictItemAll(dictItem).then(responseData => {
              this.defValOptions = responseData.data;
              this.defLoading = false
            })
          }
        }
      },
      //Lấy tập con của mục từ điển
      getDictChildren(val,row){
        row.defaultValue = null;
        let dictItem = {
          params: [{'columnName':'dict_type_id', 'queryType': '=', 'value': val}]
        };
        listDictItemAll(dictItem).then(responseData => {
          this.defValOptions = responseData.data;
        })
      },
      //Lấy dữ liệu từ điển
      getOptions(){
        let dictItem = {
          params: [{'columnName':'dict_type_id', 'queryType': '=', 'value': '5100'}]
        };
        listDictItemAll(dictItem).then(res => {
          if(res.code === 100){
            this.options = res.data;
          }
        });
        listDictTypeAll({params: []}).then(result =>{  //Tất cả mục từ điển
          if(result.code === 100){
            this.businessOptions = result.data
          }
        });
        listPropertySetAll({params: []}).then(result =>{  //Tất cả bộ thuộc tính
          if(result.code === 100){
            this.propertySetS = result.data
          }
        });

      },
      changeDictId(row) {  // {Tập thuộc tính  8606}  {Mục từ điển  8406}  {Enum  8405}
        row.bo = "";
        row.defaultValue = null;
      },
      pushCustomTeam(){       //Thêm nhóm mới
        this.propertyPanels.push({'id':uuid.v1(),'name':'','isTeam': '1','propertys': []},)
      },
      deletePropertyData(index,row,list) { // Xoá nhóm
        list.splice(index,1);
        if(row.id){
          this.deleteTeamArr.push({id:row.id});
        }
        if(this.indexArr.indexOf(index) !== -1){
          this.indexArr.splice(this.indexArr.indexOf(index),1);
        }
      },
      isIndexOfArr(val){
        return this.indexArr.indexOf(val) !== -1;
      },
      indexChange(bizindex){
        if(this.indexArr.indexOf(bizindex) === -1){
          this.indexArr.push(bizindex);
        }else{
          this.indexArr.splice(this.indexArr.indexOf(bizindex),1)
        }
      },
      //Thêm mới thuộc tính mở rộng
      pushPropertyData(panel, index){
        if(this.indexArr.indexOf(index) === -1){
          this.indexArr.push(index);
        }
        this.propertyPanels[index].propertys.push(
          {'id':uuid.v1(),
          'name':'',
          'dictId': null,
          'bo': "",
          'defaultValue': null,
          'isRequired': "0",
          'remarks':'',
          'edit': true,
        });
        this.tableKey++
      },
      tableRowClassName({row, rowIndex}) {
        row.index = rowIndex
        if (row === this.checkRowData) {
          return 'current-row'
        }
      },
      //Khởi tạo mặc định định nghĩa thuộc tính
      initPropertyDef(){
        return [
          {'id':uuid.v1(),'name':'','isTeam': '0','propertys': []}
        ];
      },
    }
  }
</script>

<style lang="scss" >
  // Kéo thả
  .dragClass {
    background: rgba($color: #41c21a, $alpha: 0.5) !important;
  }
  // Thả
  .ghostClass {
    background: rgba($color: #6cacf5, $alpha: 0.5) !important;
  }
  // Chọn
  .chosenClass:hover > td {
    background: rgba($color: #f56c6c, $alpha: 0.5) !important;
  }
::v-deep .el-form-item__content {
  margin-top: 0px;
}
.dialog-footer {
.btnGroup {
    position: absolute;
    left: 45%;
  }
}
::v-deep .el-badge {
  display: block;
}
.el-icon-remove-outline {
    font-size: 20px;
    color: #F56C6C;
    margin-right: 5px;
}
</style>
