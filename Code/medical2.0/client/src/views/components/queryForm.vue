<template>
  <el-container v-loading='loading' class="query-form">
    <el-main>
      <ConditionPanel ref='conditionPanel' v-model='vModel' :tableId='tableId' :schemeId='schemeId' :curNode='curNode' :exclude='exclude' :routerId='routerId'></ConditionPanel>
    </el-main>
    <el-aside class="condition-aside" width="210px">
      <el-row :gutter='5'>
        <el-col>
          <el-button-group>
            <el-tooltip  effect="light" content="Tìm kiếm" placement="top-start">
              <el-button type="primary" icon="el-icon-search" @click='onSearch()' :plain='true'></el-button>
            </el-tooltip>
            <el-tooltip  effect="light" content="Tóm tắt" placement="top-start">
              <el-button type='primary' icon='el-icon-d-arrow-left' @click='onMoreCodition()' :plain='true'></el-button>
            </el-tooltip>
          </el-button-group> 
            <!-- <el-button type='primary' icon='el-icon-search' @click='onSearch()' :plain='true'>Tìm kiếm</el-button>
            <el-button type='info' icon='el-icon-d-arrow-left' @click='onMoreCodition()' :plain='true'>Tóm tắt</el-button> -->
        </el-col>
      </el-row>
      <el-row :gutter='5'>
        <el-col>
          <el-button-group>
            <el-tooltip  effect="light" content="Đặt lại" placement="top-start">
              <el-button type="primary" icon='el-icon-refresh-left' @click='onResetCondition()' :plain='true'></el-button>
            </el-tooltip>
            <el-tooltip  effect="light" content="Lưu" placement="top-start">
              <el-button type="primary" icon='el-icon-tickets' @click='onSaveCondition' :plain='true'></el-button>
            </el-tooltip>
          </el-button-group> 
            <!-- <el-button icon='el-icon-refresh-left' @click='onResetCondition()' :plain='true'>Đặt lại</el-button>
            <el-button icon='el-icon-tickets' @click='onSaveCondition' :plain='true'>Lưu</el-button> -->
        </el-col>
      </el-row>
      <el-row :gutter='5'>
        <el-col>
          <el-select style="width: 190px" v-model="conditionSelected" value-key='id' clearable placeholder='Tra cứu của tôi' @change='onChange'>
            <el-option v-for="item in conditionList" :key="item.id" :label="item.name" :value="item">
              <span style="float: left">{{ item.name }}</span>
              <span style="padding:0 0 0 30px; float: right; color: #8492a6; font-size: 13px">
                <i class='el-icon-delete' @click.stop='onDeleteCondition(item)'></i>
              </span>
            </el-option>
          </el-select>
        </el-col>
      </el-row>
    </el-aside>

    <!-- Hộp thoại lưu tên truy vấn -->
    <el-dialog :title='conditionSelected ? "Cập nhật tiêu đề tra cứu" : "Nhập tiêu đề tra cứu"' :visible.sync='dialogVisible' width='350px'>
      <el-form :model='nameModel' ref='nameForm' label-width='0px' label-position='right' class='edit-form' :rules='formRules'>
        <el-form-item prop='name'>
          <el-input v-model='nameModel.name'></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="onDoSaveCondition">Xác nhận</el-button>
        <el-button @click="dialogVisible = false">Hủy</el-button>
      </span>
    </el-dialog>
  </el-container>
</template>

<script>
import BaseUI from './baseUI'
import { listQueryConditionAll, saveQueryCondition, deleteQueryCondition } from '@/api/sys/queryCondition'
import ConditionPanel from './conditonPanel'
export default {
  extends: BaseUI,
  name: 'QueryForm',
  components: {
    ConditionPanel
  },
  data() {
    return {
      vModel: null,
      conditionSelected: null, // Giá trị được chọn trong danh sách truy vấn của tôi
      conditionList: [],       // Danh sách lựa chọn của danh sách truy vấn của tôi
      dialogVisible: false,   // Cờ hiển thị hộp tiêu đề truy vấn
      nameModel: {
        name: ''   // Tên tiêu đề truy vấn
      },

      formRules: {
        'name': [
            { required: true, message: 'Vui lòng nhập tiêu đề tra cứu', trigger: 'blur' }
        ],
      }
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
    vModel(val, oldVal) {
      if(val !== oldVal){
        this.$emit('input', this.vModel)
      }
    }
  },
  methods: {
    init() {
      listQueryConditionAll({
        params: [{columnName: 'user_id', queryType: '=', value: currentUser.id},
          {columnName: 'router_id', queryType: '=', value: this.routerId}
        ],
        columnName: 'name',       // Tên trường sắp xếp
        order: ''             // Sắp xếp
      }).then(responseData => {
        if(responseData.code === 100) {
          this.conditionList = responseData.data
        } else {
          this.showMessage(responseData)
        }
        this.resetLoad()
      }).catch(error => {
        this.outputError(error)
      })
    },
    onChange(val) {
      if(val) {
        this.vModel = JSON.parse(val.conditions)
        this.$emit('input', this.vModel)
        this.onSearch()
      }
    },
    onSearch() {
      this.$emit('search')
    },
    onMoreCodition() {
      this.$emit('moreCodition')
    },
    onResetCondition() {
      this.$refs.conditionPanel.reset()
    },
    onDeleteCondition(condition) {
      deleteQueryCondition(condition).then(responseData => {
        if(responseData.code === 100) {
          let arr = this.conditionList.filter(item => item.id !== condition.id)
          this.conditionList = arr
          this.showMessage({type: 'success', msg: 'Xóa thành công'})
        } else {
          this.showMessage(responseData)
        }
        this.resetLoad()
      }).catch(error => {
        this.outputError(error)
      })
    },
    // Nhấn nút lưu
    onSaveCondition() {
      if(this.conditionSelected) {
        this.nameModel.name = this.conditionSelected.name
      } else {
        this.nameModel.name = ''
      }
      this.dialogVisible = true
    },
    // Nhấn nút xác nhận trong hộp thoại lưu
    onDoSaveCondition() {
        this.$refs['nameForm'].validate(valid => {
        if (valid) {
          this.setLoad()
          saveQueryCondition({
            id: this.conditionSelected ? this.conditionSelected.id : null,
            userId: currentUser.id,
            routerId: this.routerId,
            name: this.nameModel.name,
            conditions: JSON.stringify(this.value)
          }).then(responseData => {
            if(responseData.code === 100) {

              // todo danh sách truy vấn
              listQueryConditionAll({
                params: [{columnName: 'user_id', queryType: '=', value: currentUser.id},
                  {columnName: 'router_id', queryType: '=', value: this.routerId}
                ],
                columnName: 'name',       // Tên trường sắp xếp
                order: ''             // Sắp xếp
              }).then(responseData => {
                if(responseData.code === 100) {
                  this.conditionList = responseData.data
                }
              })

              this.dialogVisible = false
              this.showMessage({type: 'success', msg: 'Lưu thành công'})
            } else {
              this.showMessage(responseData)
            }
            this.resetLoad()
          }).catch(error => {
            this.outputError(error)
          })

        } else {
          return false
        }
      });
    }
  },
  mounted(){
    this.vModel = this.value
    this.$nextTick(() => {
      this.init()
    })
  }
}
</script>
<style lang="scss" scoped>
  .condition-aside {
    .el-row {
      margin-bottom: 5px;
    }
  }
</style>
