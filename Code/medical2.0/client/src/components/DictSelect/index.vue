<template>
  <el-select
    v-model="innerValue"
    value-key="value"
    filterable
    clearable
    :placeholder="placeholder"
    :disabled="disabled"
    :size="size"
    @clear="onClear"
    @change="onChange"
  >
    <el-option
      v-for="item in options"
      :key="item.value"
      :label="item.name"
      :value="item"
    />
  </el-select>
</template>

<script>
import { listDictItemAll } from '@/api/sys/dictItem'
import { getDictItemsByCode } from '@/utils/dictCache'

export default {
  name: 'DictSelect',
  props: {
    value: {
      type: Object,
      default: () => ({ value: null, name: null })
    },
    dictTypeId: {
      type: String,
      default: ''
    },
    dictCode: {
      type: String,
      default: ''
    },
    placeholder: {
      type: String,
      default: 'Vui lòng chọn'
    },
    disabled: {
      type: Boolean,
      default: false
    },
    size: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      options: []
    }
  },
  computed: {
    innerValue: {
      get() {
        return this.value
      },
      set(val) {
        this.$emit('input', val)
      }
    }
  },
  created() {
    this.loadDictItems()
  },
  methods: {
    async loadDictItems() {
      try {
        if (this.dictCode) {
          // Cách khuyên dùng: Lấy theo mã từ điển (Có bộ nhớ đệm)
          this.options = await getDictItemsByCode(this.dictCode)
        } else if (this.dictTypeId) {
          // Cách tương thích: Lấy theo dict_type_id
          const res = await listDictItemAll({
            params: [{ columnName: 'dict_type_id', queryType: '=', value: this.dictTypeId }]
          })
          if (res.code === 100) {
            this.options = res.data || []
          }
        }
      } catch (error) {
        console.error('Tải mục từ điển thất bại:', error)
      }
    },
    onClear() {
      this.$emit('input', { value: null, name: null })
      this.$emit('change', { value: null, name: null })
    },
    onChange(val) {
      this.$emit('input', val)
      this.$emit('change', val)
    }
  }
}
</script>
