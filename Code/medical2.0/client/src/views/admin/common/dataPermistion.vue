<template>
  <el-dialog title='Quyền dữ liệu' :visible.sync='visible' :close-on-click-modal='false' :append-to-body='true' :lock-scroll='true' v-loading='loading' width='80%'>
    <el-form ref='metaForm' :model="metaFormModel" label-width='1px' label-position='right' class='edit-form'>
      <el-row v-for='(metaData, idxMeta) in metaFormModel.metaDatas' :key='idxMeta'>
        <el-card  shadow='hover'>
          <div slot='header' class='clearfix'>
            <span>
              Dữ liệu meta: {{metaData.name}}
            </span>
          </div>
          <ConditionPanel ref='conditionPanel' v-model='metaData.dataRules' :tableId='metaData.id' :schemeId='metaData.schemeId' :routerId='metaData.routerId'></ConditionPanel>
        </el-card>
        <span>&nbsp;</span>
      </el-row>
    </el-form>
    <span slot='footer' class='dialog-footer'>
      <el-button type='primary' :plain=true @click='onOk()'>Xác nhận</el-button> 
      <el-button :plain=true @click='onDialogClose()'>Hủy</el-button>
    </span>    
  </el-dialog>
</template>
<script>
import ConditionPanel from '@/views/components/conditonPanel'
import BaseUI from '@/views/components/baseUI'
export default {
  extends: BaseUI,
  name: 'data-permission',
  components: {
    ConditionPanel
  },  
  data() {
    return {
      metaFormModel: {
        metaDatas: []    // Danh sách node chờ chọn
      },
      visible: false,
      roleId: null,       // Id vai trò đang cấu hình
      routerId: null     // Id route đang cấu hình
    }    
  },
  props: {
    //Nhận từ bên ngoàiv-modelgiá trị truyền vào
    value: {
      type: Array,
      required: true
    }
  }, 
  methods: {
    onOk() {
      let roleId = this.roleId
      let routerId = this.routerId
      let dataRules = this.value
      this.$refs['metaForm'].validate(valid => {
        if (valid) {
          // Xoá quyền của metadata
          this.metaFormModel.metaDatas.forEach((item)=>{
            dataRules = dataRules.filter((ruel) => {
              return !(ruel.metaId === item.id && ruel.routerId === routerId)
            })
          })
          // Thêm quyền dữ liệu mới
          this.metaFormModel.metaDatas.forEach((item)=>{
            // Chỉ lưu quyền dữ liệu đã cấu hình điều kiện hợp lệ
            if(this.compositeCondition(item.dataRules).length > 0) { 
              dataRules.push({
                'roleId': roleId,
                'routerId': routerId,
                'metaId': item.id,
                'conditions': JSON.stringify(item.dataRules)
              })
            }
          })

          this.$emit('input', dataRules);
          this.visible = false          
        }
      })
    },
    onDialogClose() {
      this.visible = false
    },
    openDataPermission(code, roleId, routerId) {
      this.roleId = roleId
      this.routerId = routerId
      let metas = require('@/views/' + code + '/metadata').metadata
      // Gộp quyền dữ liệu đã cấu hình của vai trò, đồng thời loại bỏ metadata đã xoá trong metadata.js
      this.metaFormModel.metaDatas = []
      metas.forEach((item, idx) => {
        let noFound = true
        this.value.forEach((rule) => {
          if(item.id === rule.metaId && rule.routerId === routerId) {
            this.metaFormModel.metaDatas.push({
              id: item.id,
              name: item.name,
              schemeId: item.schemeId,
              dataRules: JSON.parse(rule.conditions)
            })
            noFound = false
          }
        })
        // Metadata mới thêm sau khi cấu hình
        if(noFound) {
          this.metaFormModel.metaDatas.push({
            id: item.id,
            name: item.name,
            schemeId: item.schemeId,
            dataRules: {
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
              groupLogic: 'OR'
            }
          })
        }

        this.$nextTick(() => {
          this.$refs.conditionPanel[idx].init()
        })
      })
      this.visible = true
    },
  },
}
</script>