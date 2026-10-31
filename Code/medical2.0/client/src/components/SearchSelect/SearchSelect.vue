<template>
  <el-select
    v-model="currentValue"
    :value-key='valueKey'
    filterable
    clearable
    v-bind="$attrs"
    :filter-method="searchFilter"
    v-load="loadMore(OptSize)"
    @clear="handleClear"
    @change="handleChange">
    <el-option
      v-for="(option, index) in searchOptions.slice(0, OptSize)"
      :key="index"
      :value="valueKey?option : option[optionValue]"
      :label="option[optionLabel]"></el-option>
  </el-select>
</template>

<script>
  /**
   * el-select ô chọn dropdown Sử dụng khi tải lượng dữ liệu lớn
 * Ví dụ gọi: 
   * <search-select :optionSize="8" v-model="value" option-label="label" option-value="value" :options="options"></search-select>
   * Attributes: 
 * optionSize: Số lượng tùy chọn dropdown ban đầu
 * option-label: Nhãn của tùy chọn, nếu không thiết lập thì mặc định giống với value (Bắt buộc)
 * option-value: Giá trị của tùy chọn
 * options: Mảng tùy chọn dropdown
   * khi giá trị liên kết là đối tượng, phải chỉ định value-key định danh duy nhất, và chỉ cần thiết lập thuộc tính option-label, không cần thiết lập thuộc tính option-value
   * Các thuộc tính khác giống thuộc tính chính thức của el-select
   * Tính năng sẽ được tối ưu hóa sau
   */
	export default {
		name: "SearchSelect",
    model: {
      prop: 'value',
      event: 'change'
    },
    props: {
      value: {
        type: [Number, String, Boolean]
      },
      valueKey: {
        type: String,
        default: null
      },
      optionLabel: {
        type: [String, Number],
        default: 'label'
      },
      optionValue: {
        type: [Number, String, Object],
        default: 'optionValue'
      },
      optionSize: {
        type: Number,
        default: 10
      },
      options: {
        type: Array,
        default: () => []
      }
    },
    data() {
		  return {
        currentValue: this.value,
        tempStash: [],
        searchOptions: [],
        OptSize: this.optionSize
      }
    },
    mounted() {
      this.init()
    },
    directives: {
      load: {
        bind(el, binding) {
          const SELECT_DOM = el.querySelector('.el-select-dropdown .el-select-dropdown__wrap')
          SELECT_DOM.addEventListener('scroll', function () {
            let isBottom = this.scrollHeight - this.scrollTop <= this.clientHeight
            if (isBottom) {
              binding.value()
            }
          })
        }
      }
    },
    watch: {
      value(newVal, oldVal) {
        if(newVal !== oldVal) {
          this.$nextTick(() => {
            this.currentValue = this.value
          })
        }
      },
      options: {
        handler(newVal, oldVal) {
          if(newVal !== oldVal) {
            this.$nextTick(() => {
              this.tempStash = this.options
              this.searchOptions = this.tempStash
            })
          }
        },
        // deep: true,
        // immediate: true
      }
    },
    methods: {
		  async init() {
        this.tempStash = this.options
        this.searchFilter(this.value)
      },
      searchFilter(query) {
        this.currentValue = query
        if (query) {
          this.searchOptions = this.tempStash
          this.searchOptions = this.searchOptions.filter((item) => {
            if (item[this.optionLabel].includes(query)) {
              return item
            }
          })
          // if (this.options.length === 0) {
          //   this.searchOptions.push({ [this.optionValue]: '', [this.optionLabel]: '' })
          // }
        } else {
          this.searchOptions = this.tempStash
        }
      },
      loadMore(size) {
		    if (size < 8) this.OptSize = 10 // Vượt quá 7 mục thì mới có thể cuộn
		    return () => {
		      this.OptSize += 5
        }
      },
      handleChange(value) {
        this.$emit('change', value)
      },
      handleClear() {
        this.searchOptions = this.tempStash
        this.$emit('clear')
      }
    }
  }
</script>

<style scoped>

</style>
