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
      <el-form-item label="Đơn thuốc thường được kê tối đa">
          <el-input v-model="sysParamConfigForm.normalDays"
                    placeholder="Vui lòng nhập nội dung"
                    @input="handleInput('normalDays')"
                    maxlength="10"
                    @change="normalDaysHandleChange">
            <template slot="append">ngày</template>
          </el-input>
      </el-form-item>
      <el-form-item label="Số tiền phí đăng ký mặc định: ">
        <el-input v-model="sysParamConfigForm.registrationFee"
                  @input="limitNum('registrationFee')"
                  type="text"
                  @change="registrationFeeHandleChange"
                  >
          <template slot="append">VNĐ</template>
        </el-input>
      </el-form-item>
      <el-form-item label="Giá nhập thuốc là giá bán lẻ">
        <el-checkbox label="" v-model="sysParamConfigForm.retailPrice === 'true'?true:false"
                     @change="retailPriceHandleChange"></el-checkbox>
      </el-form-item>
    </el-form>

    <!-- Nút lưu -->
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
      //Đối tượng này rất quan trọng, mỗi lần thêm loại config_name đều phải thêm tương ứng
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
     * Khởi tạo trang, chủ yếu chuyển cấu trúc dữ liệu backend sang dạng trang cần
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
     * Trong input chỉ cho phép nhập số nguyên dương
     * @param field
     */
    handleInput(field) {
      let value = this.sysParamConfigForm[field]
      // Kiểm tra giá trị nhập có phải số nguyên trong sự kiện input
      if (!/^\d+$/.test(this.inputValue)) {
        // Nếu không phải số nguyên, xoá giá trị nhập
        this.sysParamConfigForm[field] = value.replace(/[^\d]/g, '');
      }
    },
    /**
     * Trong input chỉ cho phép nhập số thập phân và giữ 2 chữ số
     * @param amount
     */
    //Người dùng nhập 01, 02... thì hiển thị thành 1, 2, tức là lọc bỏ số 0 đầu tiên khi không phải số thập phân
    limitNum(field) {
      let amount = this.sysParamConfigForm[field]
        .replace(/[^\d.]/g, "") //Chỉ được nhập số
        .replace(/^(\-)*(\d+)\.(\d\d).*$/, "$1$2.$3") //Chỉ được nhập tối đa hai chữ số thập phân
        .replace(/\.{2,}/g, "."); //Khi xuất hiện nhiều dấu chấm chỉ giữ lại dấu đầu tiên
      // Không cho nhập dấu chấm thập phân ở vị trí đầu
      if (amount === ".") {
        amount = "";
      }
      // Nếu chữ số đầu là 0, chữ số thứ hai phải lớn hơn 0 hoặc là dấu chấm thập phân
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
        // Nếu chữ số đầu lớn hơn 0 (khác 0 thì chắc chắn lớn hơn 0), chỉ cần xét trường hợp chữ số thứ hai là dấu chấm thập phân
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
     * Chuyển cấu trúc dữ liệu trang thành đối tượng backend
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
     * Lưu dữ liệu
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
    margin-top: auto; /* Đẩy nút xuống cuối */
    padding: 20px; /* Có thể chọn: thêm padding cho nút */
  }
</style>
