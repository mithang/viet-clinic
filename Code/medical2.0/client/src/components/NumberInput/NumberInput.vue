<template>
  <el-input
    ref="numberInput"
    v-model="formatValue"
    @input="updateValue"
    v-currency="options"
    :clearable="clearable"
    :placeholder="placeholder"
    :disabled="disabled"
    :readonly="readonly"
    :value-range="valueRange"
    @blur="handleBlur"
    @focus="handleFocus"
    @change.capture="handleChange"
    @clear="handleClear">
    <slot slot="prepend" name="prepend"></slot>
    <slot slot="append" name="append"></slot>
  </el-input>
</template>

<script>
  import { CurrencyDirective, parse, setValue, getValue } from "vue-currency-input";
	export default {
		name: "NumberInput",
    model: {
      prop: 'value',
      event: 'input'
    },
    props: {
      value: {
        type: Number | String
      },
      // Ngôn ngữ định dạng số. Mặc định vi-VN để dấu phân cách đúng chuẩn
      // Việt Nam: "." phân tách hàng nghìn, "," phân tách phần thập phân.
      locale: {
        type: String,
        default: 'vi-VN'
      },
      // Loại tiền tệ
      currency: {
        type: String | Object,
        default: null
      },
      // Số chữ số thập phân
      precision: {
        type: Number,
        default: 0
      },
      // Có ẩn chữ số thập phân có thể bỏ qua, ký hiệu tiền tệ và ký tự phân nhóm tiêu điểm không
      distractionFree: {
        type: Boolean,
        default: false
      },
      clearable: {
        type: Boolean
      },
      disabled: {
        type: Boolean
      },
      readonly: {
        type: Boolean
      },
      prefixIcon: {
        type: String
      },
      suffixIcon: {
        type: String
      },
      placeholder: {
        type: String
      },
      valueRange: {
        type: Object
      }
    },
    directives: {
      currency: CurrencyDirective
    },
    data() {
		  return {
        formatValue: this.value,
        formatOldValue: null
      }
    },
    mounted() {
      setValue(this.$refs.numberInput, this.value)
    },
    watch:  {
      value(newVal, oldVal) {
        if(newVal !== oldVal) {
          this.$nextTick(() => {
            setValue(this.$refs.numberInput, this.value)
          })
        }
      }
    },
    computed: {
      options() {
        return {
          locale: this.locale,
          currency: this.currency,
          valueRange: this.valueRange,
          precision: this.precision,
          distractionFree: this.distractionFree
        }
      }
    },
    methods: {
      updateValue() {
        this.$emit('input', parse(this.formatValue, this.options))
      },
      handleBlur() {
        this.$emit('blur', parse(this.formatValue, this.options), this.formatOldValue)
      },
      handleChange() {
        this.$emit('change', parse(this.formatValue, this.options), this.formatOldValue)
      },
      handleFocus(e) {
        this.$emit('focus', e)
        this.formatOldValue = getValue(this.$refs.numberInput)
      },
      handleClear() {
        setValue(this.$refs.numberInput, null)
        this.$emit('clear')
      }
    }
	}
</script>

<style scoped>

</style>
