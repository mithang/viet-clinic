<template>
  <div class="container">
    <el-form :inline="true" ref="sysParamConfigForm" :model="sysParamConfigForm" label-width="180px">
      <el-form-item label="Đơn thuốc bệnh mạn tính tối đa kê được">
        <el-input v-model="sysParamConfigForm.chronicDays"
                  placeholder="Vui lòng nhập nội dung"
                  @input="handleInput('chronicDays')"
                  maxlength="10"
                  @change="chronicDaysHandleChange">
          <template slot="append">ngày</template>
        </el-input>
      </el-form-item>
      <el-form-item label="普通处方最多可开">
          <el-input v-model="sysParamConfigForm.normalDays"
                    placeholder="Vui lòng nhập nội dung"
                    @input="handleInput('normalDays')"
                    maxlength="10"
                    @change="normalDaysHandleChange">
            <template slot="append">ngày</template>
          </el-input>
      </el-form-item>
      <el-form-item label="挂号费默认金额：">
        <el-input v-model="sysParamConfigForm.registrationFee"
                  @input="limitNum('registrationFee')"
                  type="text"
                  @change="registrationFeeHandleChange"
                  >
          <template slot="append">VNĐ</template>
        </el-input>
      </el-form-item>
      <el-form-item label="药品进价是否为零售价">
        <el-checkbox label="" v-model="sysParamConfigForm.retailPrice === 'true'?true:false"
                     @change="retailPriceHandleChange"></el-checkbox>
      </el-form-item>
    </el-form>

    <!-- 保存按钮 -->
    <div class="save-button">
      <el-button type="primary" size="medium" @click="saveData">Lưu</el-button>
    </div>
  </div>
</template>

<script>
  import MainUI from '@/views/components/mainUI'
  import listViewMixin from '@/mixins/listViewMixin'
  import {listSysParamConfigAll, saveSysParamConfigList} from '@/api/sys/sysParamConfig'
  import {getLocalCurrentCompany} from "@/utils/auth";

  export default {
  extends: MainUI,
  mixins: [listViewMixin],
  components: {
  },
  data() {
    return {
      listApi: listSysParamConfigAll,
      getApi: listSysParamConfigAll,
      deleteApi: saveSysParamConfigList,
      entityName: 'SysParamConfig',
      permissionPrefix: 'sys_param_config',
      //这个对象很重要，每次增加了类型config_name都要对应加上
      sysParamConfigForm: {
        chronicDays: null,//Số ngày đơn thuốc bệnh mạn tính
        normalDays: null,//Số ngày đơn thuốc thông thường
        registrationFee: null,//Số tiền đăng ký khám mặc định
        retailPrice: null,//Có mặc định giá bán lẻ không
      },
      saveMap:new Map(),
      search: {
        columnName: "company_id",
        queryType: "=",
        value: getLocalCurrentCompany().id
      },
    };
  },
  methods: {
    /**
     *  Khởi tạo trang，主要是将后台数据结构转换成页面需要的
     * @returns {Promise<void>}
     */
    async pageInit() {
      listSysParamConfigAll({params:[this.search]}).then(responseData => {
        if(responseData.code === 100) {
          if (Array.isArray(responseData.data) && responseData.data.length >= 1){
            responseData.data.forEach(data=>{
              this.saveMap.set(data.configName, data);
              if (this.sysParamConfigForm.hasOwnProperty(data.configName)) {
                this.sysParamConfigForm[data.configName] = data.configValue;
              }
            })
          }else {
            this.initSaveDatas();
          }
        } else {
          this.showMessage(responseData)
        }
      }).catch(error => {
        this.outputError(error)
      })
    },
    /**
     * Khởi tạosaveMap
     */
    initSaveDatas(){
      for (let key in this.sysParamConfigForm) {
        if (this.sysParamConfigForm.hasOwnProperty(key)) {
          let data = {
            id: null,
            configName: key,
            configValue: this.sysParamConfigForm[key],
            companyId: getLocalCurrentCompany().id
          };
          this.saveMap.set(key, data);
        }
      }
    },
    chronicDaysHandleChange(value){
      let chronicDays = this.saveMap.get("chronicDays");
      chronicDays.configValue = String(value);
    },
    normalDaysHandleChange(value){
      let normalDays = this.saveMap.get("normalDays");
      normalDays.configValue = String(value);

    },
    registrationFeeHandleChange(value){
      let registrationFee = this.saveMap.get("registrationFee");
      registrationFee.configValue = String(value);
    },
    retailPriceHandleChange(value){
      let retailPrice = this.saveMap.get("retailPrice");

      retailPrice.configValue = String(value);
      this.sysParamConfigForm.retailPrice = String(value);
    },
    /**
     * input中只允许输入正整数
     * @param field
     */
    handleInput(field) {
      let value = this.sysParamConfigForm[field]
      // 在输入事件中检查输入值是否为整数
      if (!/^\d+$/.test(this.inputValue)) {
        // 如果不是整数，清除输入值
        this.sysParamConfigForm[field] = value.replace(/[^\d]/g, '');
      }
    },
    /**
     * input中只允许输入小数并保留2vị
     * @param amount
     */
    //用户输入01、02等，则展示成1、2，等于非小数时过滤第一位0
    limitNum(field) {
      let amount = this.sysParamConfigForm[field]
        .replace(/[^\d.]/g, "") //只能输入数字
        .replace(/^(\-)*(\d+)\.(\d\d).*$/, "$1$2.$3") //Chỉ được nhập tối đa hai chữ số thập phân
        .replace(/\.{2,}/g, "."); //出现多个点时只保留第一个
      // 第一位不让输小数点
      if (amount === ".") {
        amount = "";
      }
      // 如果第一位是0，第二位必须大于0或者小数点
      if (amount.substring(0, 1) === 0) {
        if (amount.substring(1, 2) > 0) {
          amount = amount.substring(1, 2);
        } else if (
          amount.substring(1, 2) === 0 ||
          amount.substring(1, 2) === "0"
        ) {
          amount = "0";
        }
      } else {
        // 如果第一位数字大于0（Không bằng0肯定就大于0），仅需考虑第二位是小数点的情况
        if (amount.indexOf(".") !== -1) {
          if (amount.substring(0, 1) > 0) {
          } else {
            if (amount.substring(2, 3) > 0) {
            } else {
              amount = "0.";
            }
          }
        } else {
        }
      }
      this.sysParamConfigForm[field] = amount;
    },
    /**
     * 将页面数据结构转换成后台对象
     * @returns {[]}
     */
    getSaveData(){
      let saveDatas = [];
      this.saveMap.forEach((value, key) => {
        saveDatas.push(value);
      })
      return saveDatas;
    },
    /**
     * 保存数据
     */
    saveData(){
      let saveDatas = this.getSaveData();
      saveSysParamConfigList(saveDatas).then(responseData => {
        if(responseData.code === 100) {
          this.showMessage({type: 'success', msg: 'Thao tác thành công'})
        } else {
          this.showMessage(responseData.msg);
        }
      }).catch(error => {
        this.outputError(error)
      })
    }
  },
  watch: {
  },
  mounted() {
    this.pageInit()
  }
}
</script>

<style scoped>
  .save-button {
    display: flex;
    justify-content: center;
    margin-top: auto; /* 将按钮推到底部 */
    padding: 20px; /* Có thể chọn：为按钮添加一些内边距 */
  }
</style>
