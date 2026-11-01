<template>
  <div class="ChineseMedicine">
    <p>{{ChineseMedicine.goodsName ? ChineseMedicine.goodsName : ChineseMedicine.drugStuffId.name }}</p>
    <div>
      <el-input :value="ChineseMedicine.singleDosage" @input="onSingleDosageInput" :disabled='IsOnlyRead' style="width: 100%" placeholder="Số lượng">

        <span slot="suffix" style="font-size: 16px" >{{ ChineseMedicine.preparationUnit ? ChineseMedicine.preparationUnit.name : ChineseMedicine.drugStuffId.drug.preparationUnit.name }}</span>
        <el-select
          :value="ChineseMedicine.chineseMedicineUse"
          @change="onUseChange"
          slot="append"
          :disabled='IsOnlyRead'
          style="width: 120px"
          placeholder="Cách sắc thuốc"
        >
          <el-option
            v-for="item in ChineseUseTimeOption"
            :key="item.value"
            :label="item.name"
            :value="{ name:item.name, value: item.value }"
          >
          </el-option>
        </el-select>
      </el-input>
    </div>
  </div>
</template>

<script>
export default {
    props: {
        ChineseMedicine: {
            type: Object
        },
        ChineseUseTimeOption: {
          type: Object
        },
        IsOnlyRead: {
          type: Boolean
        }
    },
  data() {
    return {
      //Số lượng
      singleDosage: "",
      //Đơn vị
      Unit: "",
      UnitOption: [],
      chineseMedicineUse: "",
    };
  },
  methods: {
    // Không mutate prop: emit object mới để cha merge giữ nguyên identity
    // của phần tử trong ChooseChineseMedicineTable, rồi cha mới tính tổng phí.
    onSingleDosageInput(val) {
      this.$emit('update:ChineseMedicine', Object.assign({}, this.ChineseMedicine, {
        singleDosage: val,
        allFee: this.ChineseMedicine.price * val
      }))
      this.$emit('GetChineseFee')
    },
    onUseChange(val) {
      this.$emit('update:ChineseMedicine', Object.assign({}, this.ChineseMedicine, {
        chineseMedicineUse: val
      }))
    }
  }
};
</script>
<style lang='scss' scoped>
$borderColor: rgb(219, 219, 219); //Gạch chân viền xám
.ChineseMedicine {
  width: calc(100% - 2px);
  height: 100%;
  border: 1px solid $borderColor;
  border-radius: 2px;
  p {
    font-size: 14px;
    margin: 0;
    padding: 10px;
    height: 50%;
    display: flex;
    align-items: center;
  }
}
</style>