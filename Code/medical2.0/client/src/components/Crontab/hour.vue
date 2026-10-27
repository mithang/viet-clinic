<template>
	<el-form size="small">
		<el-form-item>
			<el-radio v-model='radioValue' :label="1">
				giờ，Ký tự đại diện được phép[, - * /]
			</el-radio>
		</el-form-item>

		<el-form-item>
			<el-radio v-model='radioValue' :label="2">
				Chu kỳ từ
				<el-input-number v-model='cycle01' :min="0" :max="60" /> -
				<el-input-number v-model='cycle02' :min="0" :max="60" /> giờ
			</el-radio>
		</el-form-item>

		<el-form-item>
			<el-radio v-model='radioValue' :label="3">
				Từ
				<el-input-number v-model='average01' :min="0" :max="60" /> 小时开始，mỗi
				<el-input-number v-model='average02' :min="0" :max="60" /> 小时执行一次
			</el-radio>
		</el-form-item>

		<el-form-item>
			<el-radio v-model='radioValue' :label="4">
				Chỉ định
				<el-select clearable v-model="checkboxList" placeholder="Có thể chọn nhiều" multiple style="width:100%">
					<el-option v-for="item in 60" :key="item" :value="item-1">{{item-1}}</el-option>
				</el-select>
			</el-radio>
		</el-form-item>
	</el-form>
</template>

<script>
export default {
	data() {
		return {
			radioValue: 1,
			cycle01: 0,
			cycle02: 1,
			average01: 0,
			average02: 1,
			checkboxList: [],
			checkNum: this.$options.propsData.check
		}
	},
	name: 'crontab-hour',
	props: ['check', 'cron'],
	methods: {
		// Khi giá trị nút radio thay đổi
		radioChange() {
			if (this.radioValue === 1) {
				this.$emit('update', 'hour', '*', 'hour');
				this.$emit('update', 'day', '*', 'hour');
			} else {
				if (this.cron.min === '*') {
					this.$emit('update', 'min', '0', 'hour');
				}
				if (this.cron.second === '*') {
					this.$emit('update', 'second', '0', 'hour');
				}
			}
			switch (this.radioValue) {
				case 2:
					this.$emit('update', 'hour', this.cycle01 + '-' + this.cycle02);
					break;
				case 3:
					this.$emit('update', 'hour', this.average01 + '/' + this.average02);
					break;
				case 4:
					this.$emit('update', 'hour', this.checkboxString);
					break;
			}
		},
		// Khi hai giá trị chu kỳ thay đổi
		cycleChange() {
			if (this.radioValue === '2') {
				this.$emit('update', 'hour', this.cycleTotal);
			}
		},
		// Khi hai giá trị trung bình thay đổi
		averageChange() {
			if (this.radioValue === '3') {
				this.$emit('update', 'hour', this.averageTotal);
			}
		},
		// checkboxKhi giá trị thay đổi
		checkboxChange() {
			if (this.radioValue === '4') {
				this.$emit('update', 'hour', this.checkboxString);
			}
		}
	},
	watch: {
		"radioValue": "radioChange",
		'cycleTotal': 'cycleChange',
		'averageTotal': 'averageChange',
		'checkboxString': 'checkboxChange'
	},
	computed: {
		// Tính hai giá trị chu kỳ
		cycleTotal: function () {
			this.cycle01 = this.checkNum(this.cycle01, 0, 23)
			this.cycle02 = this.checkNum(this.cycle02, 0, 23)
			return this.cycle01 + '-' + this.cycle02;
		},
		// Giá trị dùng để tính trung bình
		averageTotal: function () {
			this.average01 = this.checkNum(this.average01, 0, 23)
			this.average02 = this.checkNum(this.average02, 1, 23)
			return this.average01 + '/' + this.average02;
		},
		// Tính các giá trị đã chọncheckboxtập hợp giá trị
		checkboxString: function () {
			let str = this.checkboxList.join();
			return str === '' ? '*' : str;
		}
	}
}
</script>
<style>
  .el-input-number--small .el-input__inner {
    padding-left: 0px;
    padding-right: 0px;
  }
</style>
