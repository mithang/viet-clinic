<template>
  <div>
    <div v-for="(panel, index) in panels" :key="panel.id">
      <el-divider v-if='index>0' content-position="left">{{panel.name}}</el-divider>
      <el-row>
        <div v-for="property in panel.propertys" :key="property.id">
          <el-col :span='24' v-if="property.dictId === '8606'"> <!-- Tập thuộc tính -->
            <property-panel  v-model='propertiesData[property.id]' :propertiesId='property.bo' :cols='cols' :action='action'></property-panel>
          </el-col>
          <el-col :span='24/cols' v-if="property.dictId === '8400'"> <!-- Chuỗi ký tự -->
            <el-form-item :label="property.name" prop='x' :rules="[{ required: property.isRequired==='1' ?true :false, validator: inputValidator, dataId: property.id, message: 'Vui lòng nhập' +property.name, trigger: 'blur'}]">
              <el-input :disabled='action === "view"' :key="property.id" v-model='propertiesData[property.id]'
                        :placeholder='action === "view"? "" : "Vui lòng điền" +property.name'></el-input>
            </el-form-item>
          </el-col>
          <el-col v-if="property.dictId === '8401'">  <!-- 8401Văn bản -->
            <el-form-item :label="property.name" prop='x' :rules="[{ required: property.isRequired==='1' ?true :false, validator: inputValidator, dataId: property.id, message: 'Vui lòng nhập' +property.name, trigger: 'blur'}]">
              <el-input :disabled='action === "view"' v-model='propertiesData[property.id]'  type='textarea'
                        :maxlength='255'
                        :placeholder='action === "view"? "" : "Vui lòng điền" +property.name'></el-input>
            </el-form-item>
          </el-col>

          <!--  8402Số nguyên   "8403" Giá trị số  -->
          <el-col :span='24/cols' v-if="property.dictId === '8402' ||property.dictId === '8403' ">
            <el-form-item :label="property.name" prop='x' :rules="[{ required: property.isRequired==='1' ?true :false, validator: inputValidator, dataId: property.id, message: 'Vui lòng nhập' +property.name, trigger: 'blur'}]">
              <number-input :disabled='action === "view"' v-model='propertiesData[property.id]' :precision="property.dictId === '8402'? 0: 4"
                            :placeholder='action === "view"? "" : "Vui lòng điền" +property.name'></number-input>
            </el-form-item>
          </el-col>

          <!-- "8404" Số tiền -->
          <el-col :span='24/cols' v-if="property.dictId === '8404' ">
            <el-form-item :label="property.name" prop='x' :rules="[{ required: property.isRequired==='1' ?true :false, validator: inputValidator, dataId: property.id, message: 'Vui lòng nhập' +property.name, trigger: 'blur'}]">
              <number-input :disabled='action === "view"' v-model='propertiesData[property.id]' :precision="2"
                            :placeholder='action === "view"? "" : "Vui lòng điền" +property.name'></number-input>
            </el-form-item>
          </el-col>

          <!-- "8405" Enum -->
          <el-col :span='24/cols' v-if="property.dictId === '8405' ">
            <el-form-item :label="property.name" prop='x' :rules="[{ required: property.isRequired==='1' ?true :false, validator: inputValidator, dataId: property.id, message: 'Vui lòng chọn' +property.name, trigger: 'change'}]">
              <el-select v-model="propertiesData[property.id]" :placeholder='action === "view"? "" : "Vui lòng chọn" +property.name'
                         :disabled='action === "view"'>
                <el-option
                  v-for="i in property.bo.split(',')"
                  :key="i"
                  :label="i"
                  :value="i">
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>

          <!-- Từ điển -->
          <el-col :span='24/cols' v-if="property.dictId  === '8406' ">
            <el-form-item :label="property.name" prop='x' :rules="[{ required: property.isRequired==='1' ?true :false, validator: inputValidator, dataId: property.id, message: 'Vui lòng chọn' +property.name, trigger: 'change'}]">
              <el-select v-model="propertiesData[property.id]" :placeholder='action === "view"? "" : "Vui lòng chọn" +property.name'
                         @change="dictChange" :disabled='action === "view"'>
                <el-option
                  v-for="i in getDictItems(property.bo)"
                  :key="i.id"
                  :label="i.name"
                  :value="i.id">
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </div>
      </el-row>
    </div>
  </div>
</template>

<script>
import { listDictItemAll } from '@/api/sys/dictItem'
import { getPropertySetById } from '@/api/sys/propertySet'
import BaseUI from '@/views/components/baseUI'
export default {
  extends: BaseUI,
  name: 'property-panel',
  data() {
    let inputValidator= (rule, value, callback) => {
      if(rule.required) {
        let theValue = this.propertiesData[rule.dataId]
        if(!theValue) {
          let msg = rule.message | 'Không được để trống!'
          return callback(new Error(msg))
        }
      }
      callback()
    }
    return {
      asyncOptions: {},             // Lưu dữ liệu lựa chọn bất đồng bộ lấy từ backend
      panels:[],                    // Mảng nhóm bộ thuộc tính
      propertiesData: {},
      inputValidator: inputValidator
    }
  },
  props: {
    // Nhận từ bên ngoàiv-modelgiá trị truyền vào
    value: {
      type: String
    },
    // Tập thuộc tínhId
    propertiesId: {
      type: String | Number
    },
    //Định nghĩa thuộc tínhjsonChuỗi ký tự
    propertyDef: {
      type: String
    },
    // Loại thao tác--Sửa, Xem v.v.
    action: {
      type: String,
      default: () => { return "view"}
    },
    // Số cột bố cục khung sửa
    cols: {
      type: Number | String,
      default: () => { return 1}
    }
  },
  watch:{
    value(val, oldVal) {
      if(val !== oldVal) {
        if(this.value) {
          this.propertiesData = JSON.parse(this.value)
        } else {
          this.propertiesData = {}
        }
      }
    },
    propertiesId(val, oldVal) {
      if(val !== oldVal) {
        this.getDef()
      }
    },
    propertyDef(val, oldVal) {
      if(val !== oldVal) {
        this.getDef()
      }
    },
    propertiesData: {
      handler(newName, oldName) {
          this.$emit('input', JSON.stringify(this.propertiesData))
      },
      deep: true
    }
  },
  methods: {
    dictChange(){
      this.$forceUpdate()
    },
    init(propertyDef) {
      this.panels = JSON.parse(propertyDef)
      // Khởi tạo giá trị mặc định
      if(this.panels && JSON.stringify(this.propertiesData) === "{}"){
        let obj = {};
        for(let panel of this.panels){
            for(let property of panel.propertys){
                if(property.dictId === "8606"){  //Khi là bộ thuộc tính thì bỏ qua giá trị mặc định
                  obj[property.id] = null;
                }else{
                  obj[property.id] =  property.defaultValue
                }
            }
        }
        this.propertiesData = obj
      }
    },
    getDef() {
      this.panels = []
      if(this.propertyDef) {
        this.init(this.propertyDef)
      } else if(this.propertiesId) {
        getPropertySetById(this.propertiesId).then(responseData => {
          if(responseData.code === 100) {
            this.init(responseData.data.propertiesDef)
          } else {
            this.showMessage(responseData)
          }
        }).catch(error => {
          this.outputError(error)
        })
      }
    },
    getDictItems(typeId) {
      let key = 'dict_' + typeId   // Key lưu dữ liệu bất đồng bộ
      if(this.asyncOptions.hasOwnProperty(key) === false) {
        this.asyncOptions[key] = []       // Khởi tạo dữ liệu bất đồng bộ
        listDictItemAll({
          params: [{'columnName':'dict_type_id', 'queryType': '=', 'value': typeId}]
         }).then(responseData => {
          if(responseData.code === 100) {
            this.asyncOptions[key].push.apply(this.asyncOptions[key], responseData.data)
            this.$forceUpdate()
          } else {
            this.showMessage(responseData)
          }
        }).catch(error => {
          this.outputError(error)
        })

      }
      return this.asyncOptions[key]
    }
  },
  mounted: function() {
    // Giá trị ban đầu
    if(this.value) {
      this.propertiesData = JSON.parse(this.value)
    }
    // Lấy định nghĩa bộ thuộc tính theo propertiesId
    this.getDef()
  }
}
</script>

