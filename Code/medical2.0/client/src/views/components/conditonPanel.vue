<template>
  <div class="condition-panel" v-loading='loading'>
    <el-row :key='"ConditionPanel" + idx' v-for='idx in [0, 1, 2]' :gutter='5'>
      <el-col :span='11'>
        <el-col :span='3'>
          <span v-if='idx === 0' style='text-align: center;display:block;'>Nhóm 1</span>
          <el-select v-else v-model='groupOne[idx].logic' placeholder='Vui lòng chọn toán tử logic'>
              <el-option v-for='item in logics' :key='item.value' :label='item.label' :value='item.value'></el-option>
          </el-select>
        </el-col>
        <el-col :span='5'>
          <el-select v-model='groupOne[idx].column' value-key='name' @change='onColumnChange(groupOne[idx].column, groupOne, idx)' placeholder='Vui lòng chọn trường'>
              <el-option v-for="col in columns.filter(item => item['name'] !== exclude)" :key='col.name' :label='col.comments' :value='col'></el-option>
          </el-select>
        </el-col>
        <el-col :span='4'>
          <el-select v-model='groupOne[idx].queryType' @change='onQueryTypeChange(groupOne[idx].queryType, groupOne, idx)' placeholder='Vui lòng chọn toán tử'>
            <el-option v-for='opr in groupOne[idx].operations' :key='opr.value' :label='opr.label' :value='opr.value'></el-option>
          </el-select>
        </el-col>
        <el-col :span='12'>
          <div v-if="groupOne[idx].column && groupOne[idx].column.showType && groupOne[idx].column.showType.value === 'MultipleInput'">
            <el-input v-model='groupOne[idx].value' :clearable='true' placeholder='Vui lòng nhập giá trị điều kiện' ></el-input>
          </div>
          <div v-else-if="groupOne[idx].column && groupOne[idx].column.showType && groupOne[idx].column.showType.value === 'SingleInput'">
            <div v-if="groupOne[idx].column && (groupOne[idx].column.javaType.value === 'Long' || groupOne[idx].column.javaType.value === 'Integer' || groupOne[idx].column.javaType.value === 'Double' || groupOne[idx].column.javaType.value === 'Float' || groupOne[idx].column.javaType.value === 'java.math.BigDecimal')">
              <number-range-input v-if="groupOne[idx].queryType === 'between' || groupOne[idx].queryType === 'not between'" v-model='groupOne[idx].value'></number-range-input>
              <number-input v-else v-model="groupOne[idx].value" :currency='groupOne[idx].column.javaType.value === "java.math.BigDecimal" ? "VND":null' :precision="groupOne[idx].column.decimalLenth" placeholder='Vui lòng nhập giá trị điều kiện'></number-input>
            </div>
            <el-input v-else v-model='groupOne[idx].value' :clearable='true' placeholder='Vui lòng nhập giá trị điều kiện' ></el-input>
          </div>
          <div v-else-if="groupOne[idx].column && groupOne[idx].column.showType && groupOne[idx].column.showType.value === 'DatePicker'">
            <data-range-picker v-if="groupOne[idx].queryType === 'between' || groupOne[idx].queryType === 'not between'" v-model='groupOne[idx].value' type='daterange'></data-range-picker>
            <el-date-picker v-else v-model='groupOne[idx].value' placeholder='Vui lòng chọn ngày11' type='date' value-format='yyyy-MM-dd'></el-date-picker>
          </div>
          <div v-else-if="groupOne[idx].column && groupOne[idx].column.showType && groupOne[idx].column.showType.value === 'DateTimePicker'">
            <data-range-picker v-if="groupOne[idx].queryType === 'between' || groupOne[idx].queryType === 'not between'" v-model='groupOne[idx].value' type='datetimerange'></data-range-picker>
          </div>
          <div v-else-if="groupOne[idx].column && groupOne[idx].column.showType && groupOne[idx].column.showType.value === 'TimePicker'">
            <el-time-picker v-if="groupOne[idx].queryType === 'between' || groupOne[idx].queryType === 'not between'" is-range v-model='groupOne[idx].value' range-separator='đến' start-placeholder='Thời gian bắt đầu' end-placeholder='Thời gian kết thúc' placeholder='Chọn khoảng thời gian' value-format='HH:mm:ss'></el-time-picker>
            <el-time-picker v-else  v-model='groupOne[idx].value' placeholder='Vui lòng chọn thời gian' value-format='HH:mm:ss'></el-time-picker>
          </div>
          <div v-else-if="groupOne[idx].column && groupOne[idx].column.showType && groupOne[idx].column.showType.value === 'Switch'">
            <el-switch v-model='groupOne[idx].value' active-color='#13ce66' inactive-color='#dbdfe6' :active-value='1' :inactive-value='0'></el-switch>
          </div>
          <!--<div v-else-if="groupOne[idx].column && groupOne[idx].column.showType && groupOne[idx].column.showType.value == 'IncNumber'">
            <number-range-input v-if="groupOne[idx].queryType === 'between' || groupOne[idx].queryType === 'not between'" v-model='groupOne[idx].value'></number-range-input>
            <el-input-number v-else v-model='groupOne[idx].value' controls-position='right' :controls='false' placeholder='Vui lòng nhập giá trị điều kiện'></el-input-number>
          </div>-->
          <div v-else-if="groupOne[idx].column && groupOne[idx].column.showType && (groupOne[idx].column.showType.value === 'InputNumber' || groupOne[idx].column.showType.value === 'IncNumber')">
            <number-range-input v-if="groupOne[idx].queryType === 'between' || groupOne[idx].queryType === 'not between'" v-model='groupOne[idx].value' :type="groupOne[idx].column.javaType.value" :precision="groupOne[idx].column.decimalLenth"></number-range-input>
            <number-input v-else v-model="groupOne[idx].value" :currency='groupOne[idx].column.javaType.value === "java.math.BigDecimal" ? "VND":null' :precision="groupOne[idx].column.decimalLenth" placeholder='Vui lòng nhập giá trị điều kiện'></number-input>
          </div>
          <div v-else-if="groupOne[idx].column && groupOne[idx].column.showType && groupOne[idx].column.showType.value === 'Select'">
            <el-select v-if="groupOne[idx].queryType === 'in' || groupOne[idx].queryType === 'not in'" v-model='groupOne[idx].value' :value-key='groupOne[idx].column.fieldId' multiple filterable placeholder='Vui lòng chọn giá trị điều kiện'>
                <el-option v-for="item in pp[idx]" :key='item[groupOne[idx].column.fieldId]' :label='item[groupOne[idx].column.fieldName]' :value='item[groupOne[idx].column.fieldId]'></el-option>
            </el-select>
            <el-select v-else v-model='groupOne[idx].value' :value-key='groupOne[idx].column.fieldId'   filterable clearable placeholder='Vui lòng chọn giá trị điều kiện'>
               <el-option v-for="item in pp[idx]" :key='item[groupOne[idx].column.fieldId]' :label='item[groupOne[idx].column.fieldName]' :value='item[groupOne[idx].column.fieldId]'></el-option>
            </el-select>
          </div>
          <div v-else-if="groupOne[idx].column && groupOne[idx].column.showType && groupOne[idx].column.showType.value === 'SelectTree'">
            <el-cascader v-if="groupOne[idx].queryType === 'in' || groupOne[idx].queryType === 'not in'" v-model='groupOne[idx].value' :options="pp[idx]" :props='{value: groupOne[idx].column.fieldId, label: groupOne[idx].column.fieldName, checkStrictly: true, emitPath: false, multiple: true}' filterable clearable placeholder='Vui lòng chọn giá trị điều kiện'></el-cascader>
            <el-cascader v-else v-model='groupOne[idx].value' :options="pp[idx]" :props='{value: groupOne[idx].column.fieldId, label: groupOne[idx].column.fieldName, checkStrictly: true, emitPath: false}' filterable clearable placeholder='Vui lòng chọn giá trị điều kiện'></el-cascader>
          </div>
          <el-input v-else v-model='groupOne[idx].value' :clearable='true' placeholder='Vui lòng nhập giá trị điều kiện' ></el-input>
        </el-col>
    </el-col>
      <el-col :span='2'>
        <div v-if='idx === 1' style="margin-left:10px;margin-right:10px;">
          <el-select v-model='groupLogic'  placeholder='Vui lòng chọn toán tử logic giữa các nhóm'>
              <el-option v-for='lg in logics' :key='lg.value' :label='lg.label' :value='lg.value'></el-option>
          </el-select>
        </div>
        <div v-else>&nbsp;</div>
      </el-col>
      <el-col :span='11'>
          <el-col :span='3'>
            <span v-if='idx === 0' style='text-align: center;display:block;'>Nhóm 2</span>
            <el-select v-else v-model='groupTwo[idx].logic' placeholder='Vui lòng chọn toán tử logic'>
                <el-option v-for='item in logics' :key='item.value' :label='item.label' :value='item.value'></el-option>
            </el-select>
          </el-col>
          <el-col :span='5'>
            <el-select v-model='groupTwo[idx].column' value-key='name' @change='onColumnChange(groupTwo[idx].column, groupTwo, idx)'  placeholder='Vui lòng chọn trường'>
                <el-option v-for="col in columns.filter(item => item['name'] !== exclude)" :key='col.name' :label='col.comments' :value='col'></el-option>
            </el-select>
          </el-col>
          <el-col :span='4'>
            <el-select v-model='groupTwo[idx].queryType' @change='onQueryTypeChange(groupTwo[idx].queryType, groupTwo, idx)' placeholder='Vui lòng chọn toán tử'>
              <el-option v-for='opr in groupTwo[idx].operations' :key='opr.value' :label='opr.label' :value='opr.value'></el-option>
            </el-select>
          </el-col>
          <el-col :span='12'>
            <div v-if="groupTwo[idx].column && groupTwo[idx].column.showType && groupTwo[idx].column.showType.value === 'MultipleInput'">
              <el-input v-model='groupTwo[idx].value' :clearable='true' placeholder='Vui lòng nhập giá trị điều kiện' ></el-input>
            </div>
            <div v-else-if="groupTwo[idx].column && groupTwo[idx].column.showType && groupTwo[idx].column.showType.value === 'SingleInput'">
              <div v-if="groupTwo[idx].column && (groupTwo[idx].column.javaType.value === 'Long' || groupTwo[idx].column.javaType.value === 'Integer' || groupTwo[idx].column.javaType.value === 'Double' || groupTwo[idx].column.javaType.value === 'Float'  || groupTwo[idx].column.javaType.value === 'java.math.BigDecimal')">
                <number-range-input v-if="groupTwo[idx].queryType === 'between' || groupTwo[idx].queryType === 'not between'" v-model='groupTwo[idx].value'></number-range-input>
                <number-input v-else v-model="groupTwo[idx].value" :currency='groupTwo[idx].column.javaType.value === "java.math.BigDecimal" ? "VND":null' :precision="groupTwo[idx].column.decimalLenth" placeholder='Vui lòng nhập giá trị điều kiện'></number-input>
              </div>
              <el-input v-else v-model='groupTwo[idx].value' :clearable='true' placeholder='Vui lòng nhập giá trị điều kiện' ></el-input>
            </div>
            <div v-else-if="groupTwo[idx].column && groupTwo[idx].column.showType && groupTwo[idx].column.showType.value === 'DatePicker'">
              <data-range-picker v-if="groupTwo[idx].queryType === 'between' || groupTwo[idx].queryType === 'not between'" v-model='groupTwo[idx].value' type='daterange'></data-range-picker>
              <el-date-picker v-else v-model='groupTwo[idx].value' placeholder='Vui lòng chọn ngày' type='date' value-format='yyyy-MM-dd'></el-date-picker>
            </div>
            <div v-else-if="groupTwo[idx].column && groupTwo[idx].column.showType && groupTwo[idx].column.showType.value === 'DateTimePicker'">
              <data-range-picker v-if="groupTwo[idx].queryType === 'between' || groupTwo[idx].queryType === 'not between'" v-model='groupTwo[idx].value' type='datetimerange'></data-range-picker>
            </div>
            <div v-else-if="groupTwo[idx].column && groupTwo[idx].column.showType && groupTwo[idx].column.showType.value === 'TimePicker'">
              <el-time-picker v-if="groupTwo[idx].queryType === 'between' || groupTwo[idx].queryType === 'not between'" is-range v-model='groupTwo[idx].value' range-separator='đến' start-placeholder='Thời gian bắt đầu' end-placeholder='Thời gian kết thúc' placeholder='Chọn khoảng thời gian' value-format='HH:mm:ss'></el-time-picker>
              <el-time-picker v-else  v-model='groupTwo[idx].value' placeholder='Vui lòng chọn thời gian' value-format='HH:mm:ss'></el-time-picker>
            </div>
            <div v-else-if="groupTwo[idx].column && groupTwo[idx].column.showType && groupTwo[idx].column.showType.value === 'Switch'">
              <el-switch v-model='groupTwo[idx].value' active-color='#13ce66' inactive-color='#dbdfe6' :active-value='1' :inactive-value='0'></el-switch>
            </div>
            <!--<div v-else-if="groupTwo[idx].column && groupTwo[idx].column.showType && groupTwo[idx].column.showType.value == 'IncNumber'">
              <number-range-input v-if="groupTwo[idx].queryType === 'between' || groupTwo[idx].queryType === 'not between'" v-model='groupTwo[idx].value'></number-range-input>
              <el-input-number v-else v-model='groupTwo[idx].value' controls-position='right' :controls='false' placeholder='Vui lòng nhập giá trị điều kiện'></el-input-number>
            </div>-->
            <div v-else-if="groupTwo[idx].column && groupTwo[idx].column.showType && (groupTwo[idx].column.showType.value === 'InputNumber' || groupTwo[idx].column.showType.value === 'IncNumber')">
              <number-range-input v-if="groupTwo[idx].queryType === 'between' || groupTwo[idx].queryType === 'not between'" v-model='groupTwo[idx].value' :type="groupTwo[idx].column.javaType.value" :precision="groupTwo[idx].column.decimalLenth" ></number-range-input>
              <number-input v-else v-model="groupTwo[idx].value" :currency='groupTwo[idx].column.javaType.value === "java.math.BigDecimal" ? "VND":null' :precision="groupTwo[idx].column.decimalLenth" placeholder='Vui lòng nhập giá trị điều kiện'></number-input>
            </div>
            <div v-else-if="groupTwo[idx].column && groupTwo[idx].column.showType && groupTwo[idx].column.showType.value === 'Select'">
              <el-select v-if="groupTwo[idx].queryType === 'in' || groupTwo[idx].queryType === 'not in'" v-model='groupTwo[idx].value' :value-key='groupTwo[idx].column.fieldId' multiple filterable placeholder='Vui lòng chọn giá trị điều kiện'>
                  <el-option v-for="item in pp[idx + 3]" :key='item[groupTwo[idx].column.fieldId]' :label='item[groupTwo[idx].column.fieldName]' :value='item[groupTwo[idx].column.fieldId]'></el-option>
              </el-select>
              <el-select v-else v-model='groupTwo[idx].value' :value-key='groupTwo[idx].column.fieldId'   filterable clearable placeholder='Vui lòng chọn giá trị điều kiện'>
                 <el-option v-for="item in pp[idx + 3]" :key='item[groupTwo[idx].column.fieldId]' :label='item[groupTwo[idx].column.fieldName]' :value='item[groupTwo[idx].column.fieldId]'></el-option>
              </el-select>
            </div>
            <div v-else-if="groupTwo[idx].column && groupTwo[idx].column.showType && groupTwo[idx].column.showType.value === 'SelectTree'">
              <el-cascader v-if="groupTwo[idx].queryType === 'in' || groupTwo[idx].queryType === 'not in'" v-model='groupTwo[idx].value' :options="pp[idx + 3]" :props='{value: groupTwo[idx].column.fieldId, label: groupTwo[idx].column.fieldName, checkStrictly: true, emitPath: false, multiple: true}' filterable clearable placeholder='Vui lòng chọn giá trị điều kiện'></el-cascader>
              <el-cascader v-else v-model='groupTwo[idx].value' :options="pp[idx + 3]" :props='{value: groupTwo[idx].column.fieldId, label: groupTwo[idx].column.fieldName, checkStrictly: true, emitPath: false}' filterable clearable placeholder='Vui lòng chọn giá trị điều kiện'></el-cascader>
            </div>
            <el-input v-else v-model='groupTwo[idx].value' :clearable='true' placeholder='Vui lòng nhập giá trị điều kiện' ></el-input>
          </el-col>
        </el-col>
    </el-row>
  </div>
</template>

<script>
import BaseUI from './baseUI'
import DataRangePicker from '@/components/DataRangePicker'
import NumberRangeInput from '@/components/NumberRangeInput'
import { columnQuery } from '@/api/common/columnQuery'
export default {
  extends: BaseUI,
  name: 'ConditionPanel',
  components: {
    DataRangePicker,
    NumberRangeInput
  },
  data() {
    return {
      pp: {
        "0": [],  "1": [], "2": [], "3": [], "4": [], "5": []
      },  // Tùy chọn giá trị trường đối tượng
      columns: [],  // Danh sách trường
      logics: [
        {value: 'AND', label: 'Và'},
        {value: 'OR', label: 'hoặc'}
      ],
      groupOne: [
        {logic: 'AND', column: null, queryType: '', value: null, operations: []},
        {logic: 'AND', column: null, queryType: '', value: null, operations: []},
        {logic: 'AND', column: null, queryType: '', value: null, operations: []}
      ],
      groupTwo: [
        {logic: 'AND', column: null, queryType: '', value: null, operations: []},
        {logic: 'AND', column: null, queryType: '', value: null, operations: []},
        {logic: 'AND', column: null, queryType: '', value: null, operations: []}
      ],
      groupLogic: 'OR'    // Toán tử logic nối hai nhóm
    }
  },
  props: {
    value: {
      type: Object,
      default: () => {return {}}
    },
    tableId: {
      type: String
    },
    schemeId: {
      type: String
    },
    // Trường không làm điều kiện
    exclude: {
      type: String,
      default: () => {return null}
    },
    curNode: {
      type: Object,
      default: () => {return {}}
    },
    routerId: {
      type: String
    }
  },
  watch:{
    value(val, oldVal) {
      if(val && val !== oldVal) {
        this.groupOne = this.value['groupOne']
        this.groupTwo = this.value['groupTwo']
        this.groupLogic = this.value['groupLogic']
      }
    },
    groupLogic(val, oldVal) {
      if(val!==oldVal){
        this.value['groupLogic'] = val    // Không phải đối tượng, cần theo dõi thay đổi
      }
    },
    curNode(val, oldVal) {
      if(val === oldVal) {
        return
      }
      if(this.exclude) {
        this.watchGroupHandler(this.exclude, this.groupOne)
        this.watchGroupHandler(this.exclude, this.groupTwo)
      }
    },
    'groupOne.0.column': {
      handler(val, oldVal) {
        if(val && val !== oldVal) {
          this.doInitOptions(val, this.groupOne, 0)
        }
      }
    },
    'groupOne.1.column': {
      handler(val, oldVal) {
        if(val && val !== oldVal) {
          this.doInitOptions(val, this.groupOne, 1)
        }
      }
    },
    'groupOne.2.column': {
      handler(val, oldVal) {
        if(val && val !== oldVal) {
          this.doInitOptions(val, this.groupOne, 2)
        }
      }
    },
    'groupTwo.0.column': {
      handler(val, oldVal) {
        if(val && val !== oldVal) {
          this.doInitOptions(val, this.groupTwo, 0)
        }
      }
    },
    'groupTwo.1.column': {
      handler(val, oldVal) {
        if(val && val !== oldVal) {
          this.doInitOptions(val, this.groupTwo, 1)
        }
      }
    },
    'groupTwo.2.column': {
      handler(val, oldVal) {
        if(val && val !== oldVal) {
          this.doInitOptions(val, this.groupTwo, 2)
        }
      }
    },
    'groupOne.0.value': {
      handler(val, oldVal) {
        if(val) {
          if(this.groupOne[0].column) {
              this.watchValueHandler(this.groupOne, 0)
          }
        }
      }
    },
    'groupOne.1.value': {
      handler(val, oldVal) {
        if(val) {
          if(this.groupOne[1].column) {
              this.watchValueHandler(this.groupOne, 1)
          }
        }
      }
    },
    'groupOne.2.value': {
      handler(val, oldVal) {
        if(val) {
          if(this.groupOne[2].column) {
              this.watchValueHandler(this.groupOne, 2)
          }
        }
      }
    },
    'groupTwo.0.value': {
      handler(val, oldVal) {
        if(val) {
          if(this.groupTwo[0].column) {
              this.watchValueHandler(this.groupTwo, 0)
          }
        }
      }
    },
    'groupTwo.1.value': {
      handler(val, oldVal) {
        if(val) {
          if(this.groupTwo[1].column) {
              this.watchValueHandler(this.groupTwo, 1)
          }
        }
      }
    },
    'groupTwo.2.value': {
      handler(val, oldVal) {
        if(val) {
          if(this.groupTwo[2].column) {
              this.watchValueHandler(this.groupTwo, 2)
          }
        }
      }
    }
  },
  methods: {
    watchValueHandler(gpOfValue, idx) {
      let interColumnName = gpOfValue[idx].column.name

      let group = this.groupOne
      for(let i= 0 ; i < group.length; i++) {
        if(group[i].column && group[i].column.interaction) {
          let colNames = group[i].column.interaction.split(",")
          for(let j = 0; j < colNames.length; j++) {
            if(colNames[j] === interColumnName) { //
              if((group === gpOfValue && group[Math.max(idx, i)].logic === 'AND')
                || this.groupLogic === 'AND') {
                group[i].value = null
                let [This, operates] = this.constructModel(colNames, group, i)
                this.initOptions(This, operates, group[i].column, group === this.groupOne ? i : i + 3)
              }
            }
          }
        }
      }

      group = this.groupTwo
      for(let i= 0 ; i < group.length; i++) {
        if(group[i].column && group[i].column.interaction) {
          let colNames = group[i].column.interaction.split(",")
          for(let j = 0; j < colNames.length; j++) {
            if(colNames[j] === interColumnName) { //
              if((group === gpOfValue && group[Math.max(idx, i)].logic === 'AND')
                || this.groupLogic === 'AND') {
                group[i].value = null
                let [This, operates] = this.constructModel(colNames, group, i)
                this.initOptions(This, operates, group[i].column, group === this.groupOne ? i : i + 3)
              }
            }
          }
        }
      }
    },
    // interColumnName tên trường cột liên động
    watchGroupHandler(interColumnName, group) {
      for(let i= 0 ; i < group.length; i++) {
        if(group[i].column && group[i].column.interaction) {
          let colNames = group[i].column.interaction.split(",")
          for(let j = 0; j < colNames.length; j++) {
            if(colNames[j] === interColumnName) { // Có biến liên động phụ thuộc vào hộp chọn bên trái
              group[i].value = null
              let [This, operates] = this.constructModel(colNames, group, i)
              this.initOptions(This, operates, group[i].column, group === this.groupOne ? i : i+ 3)
            }
          }
        }
      }
    },
    // colNames mảng biến liên động;
    async constructModel(colNames, group, idx) {
      let model = {}
      let operates = {}
      let colIdx = group === this.groupOne ? idx : idx + 3 // Chỉ mục của tùy chọn cần truy vấn
      for(let i=0; i < colNames.length; i++) {
        /** column lúc này có thể chưa có giá trị, sẽ báo lỗi */
        let column = await this.init() && this.columns.find(item => {
          return item.name === colNames[i]
        })

        if (colNames[i] === this.exclude) {
          model[column.simpleJavaField] = this.curNode
          operates[colNames[i] + colIdx] = '='
        } else {
          // Trong cùng nhóm
          model[column.simpleJavaField] = {}
          model[column.simpleJavaField][column.fieldId] = null
          let find = false
          for(let j = 0; j < group.length; j++) {
            if(j === idx) {
              continue
            }

            if (group[j].column && colNames[i] === group[j].column.name) {
              if(group[Math.max(idx, j)].logic === 'AND') {
                model[column.simpleJavaField][column.fieldId] = group[j].value
                operates[colNames[i] + colIdx] = group[j].queryType
                find = true
                break
              }
            }
          }

          // Không tìm thấy trong cùng nhóm, và giữa các nhóm là AND
          if(!find && this.groupLogic === 'AND') {
            let gp = group === this.groupOne ? this.groupTwo : this.groupOne
            for(let j = 0; j < gp.length; j++) {
              if (gp[j].column && colNames[i] === gp[j].column.name) {
                model[column.simpleJavaField][column.fieldId] = gp[j].value
                operates[colNames[i] + colIdx] = gp[j].queryType
                break
              }
            }
          }
        }
      }
      return [model, operates]
    },
    init() {
      this.setLoad()
      return new Promise((resolve, reject) => {
        columnQuery(this.schemeId, this.tableId, this.exclude).then(responseData => {
          if(responseData.code === 100) {
            this.columns = responseData.data
            resolve(true)
          } else {
            this.showMessage(responseData)
          }
          this.resetLoad()
        }).catch(error => {
          this.outputError(error)
        })
      })
    },
    onQueryTypeChange(queryType, group, index) {
      // Dựa vào toán tử và kiểu hiển thị để xác định giá trị khởi tạo, cần kiểm thử; khi thay đổi thì đồng bộ sửa phương thức onQueryTypeChange(rule) trong dataPermistion.vue
      if(group[index].column && (group[index].column.showType.value === "Select" || group[index].column.showType.value === "SelectTree") && (queryType === "in" || queryType === "not in")) {
        group[index].value = []
      } else {
        group[index].value = null
      }
    },
    onColumnChange(column, group, index) {
      // Đặt lại giá trị điều kiện truy vấn
      group[index].value = null
      group[index].queryType = ''

      this.doColumnChange(column, group, index)
    },
    doColumnChange(column, group, index) {
      if(column && column.showType) {
        if(column.showType.value === 'MultipleInput') {
          group[index].operations = [
            {value: '=', label: 'Bằng'},
            {value: '<>', label: 'Không bằng'},
            {value: 'like', label: 'Bao gồm'},
            {value: 'not like', label: 'Không bao gồm'}
          ]
          group[index].queryType = 'like'
        } else if(column.showType.value === 'SingleInput') {
          if(column.javaType.value === 'Long' || column.javaType.value === 'Integer' || column.javaType.value === 'Double' || column.javaType.value === 'Float' || column.javaType.value === 'java.math.BigDecimal') {
            group[index].operations = [
              {value: '=', label: 'Bằng'},
              {value: '<>', label: 'Không bằng'},
              {value: 'between', label: 'Nằm trong khoảng'},
              {value: 'not between', label: 'Không nằm trong khoảng'}
            ]
            group[index].queryType = '='
          } else {
            group[index].operations = [
              {value: '=', label: 'Bằng'},
              {value: '<>', label: 'Không bằng'},
              {value: 'like', label: 'Bao gồm'},
              {value: 'not like', label: 'Không bao gồm'}
            ]
            group[index].queryType = 'like'
          }
        } else if(column.showType.value === 'DatePicker' || column.showType.value === 'DateTimePicker' || column.showType.value === 'TimePicker') {
          group[index].operations = [
            {value: '=', label: 'Bằng'},
            {value: '<>', label: 'Không bằng'},
            {value: 'between', label: 'Nằm trong khoảng'},
            {value: 'not between', label: 'Không nằm trong khoảng'}
          ]
          group[index].queryType = '='
        } else if(column.showType.value === 'Switch') {
          group[index].operations = [
            {value: '=', label: 'Bằng'},
            {value: '<>', label: 'Không bằng'}
          ]
          group[index].queryType = '='
        } else if(column.showType.value === 'IncNumber' || column.showType.value === 'InputNumber') {
          group[index].operations = [
            {value: '=', label: 'Bằng'},
            {value: '<>', label: 'Không bằng'},
            {value: 'between', label: 'Nằm trong khoảng'},
            {value: 'not between', label: 'Không nằm trong khoảng'}
          ]
          group[index].queryType = '='
        } else if(column.showType.value === 'Select' || column.showType.value === 'SelectTree') {
          if(column.name === 'ids') {
            group[index].operations = [
              {value: 'right_like', label: 'Bao gồm cả cấp dưới'},
              {value: 'not right_like', label: 'Không bao gồm cấp dưới'}
            ]
            group[index].queryType = 'right_like'
          } else {
            group[index].operations = [
              {value: '=', label: 'Bằng'},
              {value: '<>', label: 'Không bằng'},
              {value: 'in', label: 'Trong danh sách'},
              {value: 'not in', label: 'Không trong danh sách'}
            ]
            group[index].queryType = '='
          }
        }
      }
    },
    async doInitOptions(column, group, index) {
      if(column && column.showType && (column.showType.value === 'Select' || column.showType.value === 'SelectTree')) {
        let This = {}
        let operates = {}
        if(column.interaction) {
          let colNames = column.interaction.split(",")
          let [model, opr] = await this.constructModel(colNames, group, index)
          This = model
          operates = opr
        }
        this.initOptions(This, operates, column, group === this.groupOne ? index : index + 3)
      }
    },
    // This curNode; column trường được chọn; index 0~2 của groupOne, 3~5 của groupTwo
    initOptions(This, operates, column, index) {
      // Dữ liệu danh sách lựa chọn
      let className = column.customType.genTable.className
      let apiPath = column.customType.moduleName + '/' + (column.customType.subModuleName !== '' ? column.customType.subModuleName + '/': '') + className.charAt(0).toLowerCase() + className.slice(1)
      let fnOption = ''
      if(column.showType.value === 'SelectTree') {
        fnOption = require('@/api/' + apiPath)['tree' + className]
      } else {
        fnOption = require('@/api/' + apiPath)['list' + className + 'All']
      }
      // Tham số liên kết
      let parameters = []
      if(column.parameters) {
        eval('parameters = [' + column.parameters + ']')
      }

      // Dựa vào ký hiệu được chọn trong tham số liên kết để thay thế toán tử
      parameters.forEach(item => {
        let key = item.columnName+index
        if(key in operates) {
          if(operates[key]) {
            item.queryType = operates[key]
          }
        }
      })

      // Quyền dữ liệu
      let search = {params: parameters, offset: 0, limit: 10, columnName: '', order: ''}
      this.pushDataPermissions(search.params, this.$route.meta.routerId, column.customType.genTable.id)

      this.pp[index] = []
      fnOption(search).then(responseData => {
        if(responseData.code === 100) {
          this.pp[index] = responseData.data
        } else {
          this.showMessage(responseData)
        }
        this.resetLoad()
      }).catch(error => {
        this.outputError(error)
      })
    },
    reset() {
      this.groupOne = [
        {logic: 'AND', column: null, queryType: '', value: null, operations: []},
        {logic: 'AND', column: null, queryType: '', value: null, operations: []},
        {logic: 'AND', column: null, queryType: '', value: null, operations: []}
      ]
      this.groupTwo = [
        {logic: 'AND', column: null, queryType: '', value: null, operations: []},
        {logic: 'AND', column: null, queryType: '', value: null, operations: []},
        {logic: 'AND', column: null, queryType: '', value: null, operations: []}
      ]
      this.groupLogic = 'OR'

      this.$emit('input', {
        groupOne: this.groupOne,
        groupTwo: this.groupTwo,
        groupLogic: this.groupLogic
      })

    }
  },
  mounted(){
    this.$nextTick(() => {
      if(this.value) {
        this.groupOne = this.value['groupOne']
        this.groupTwo = this.value['groupTwo']
        this.groupLogic = this.value['groupLogic']
      } else { // Nếu dữ liệu rỗng thì thực hiện khởi tạo
        this.reset()
      }

      this.$on('reset', function() {
        this.reset()
      })

      this.$on('init', function() {
        this.init()
      })
      this.init()
    })
  }
}
</script>


<style scoped lang="scss">
  .condition-panel {
    .el-row {
      margin-bottom: 5px;
      span {
        font-size: 12px;
      }
    }
  }
.el-date-editor.el-input, .el-date-editor.el-input__inner {
    width: 100%;
}

.el-select {
    display: inline-block;
    position: relative;
    width: 100%;
}
</style>
