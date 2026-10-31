<template>
	<el-form size='small'>
		<el-form-item>
			<el-radio v-model='radioValue' :label="1">
				tháng, Ký tự đại diện được phép[, - * /]
			</el-radio>
		</el-form-item>

		<el-form-item>
			<el-radio v-model='radioValue' :label="2">
				Chu kỳ từ
				<el-input-number v-model='cycle01' :min="1" :max="12" /> -
				<el-input-number v-model='cycle02' :min="1" :max="12" /> tháng
			</el-radio>
		</el-form-item>

		<el-form-item>
			<el-radio v-model='radioValue' :label="3">
				Từ
				<el-input-number v-model='average01' :min="1" :max="12" /> tháng bắt đầu, mỗi
				<el-input-number v-model='average02' :min="1" :max="12" /> tháng thực hiện một lần
			</el-radio>
		</el-form-item>

		<el-form-item>
			<el-radio v-model='radioValue' :label="4">
				Chỉ định
				<el-select clearable v-model="checkboxList" placeholder="Có thể chọn nhiều" multiple style="width:100%">
					<el-option v-for="item in 12" :key="item" :value="item">{{item}}</el-option>
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
			cycle01: 1,
			cycle02: 2,
			average01: 1,
			average02: 1,
			checkboxList: [],
			checkNum: this.check
		}
	},
	name: 'crontab-month',
	props: ['check', 'cron'],
	methods: {
		// Khi giá trị nút radio thay đổi
		radioChange() {
			if (this.radioValue === 1) {
				this.$emit('update', 'month', '*');
				this.$emit('update', 'year', '*');
			} else {
				if (this.cron.day === '*') {
					this.$emit('update', 'day', '0', 'month');
				}
				if (this.cron.hour === '*') {
					this.$emit('update', 'hour', '0', 'month');
				}
				if (this.cron.min === '*') {
					this.$emit('update', 'min', '0', 'month');
				}
				if (this.cron.second === '*') {
					this.$emit('update', 'second', '0', 'month');
				}
			}
			switch (this.radioValue) {
				case 2:
					this.$emit('update', 'month', this.cycle01 + '-' + this.cycle02);
					break;
				case 3:
					this.$emit('update', 'month', this.average01 + '/' + this.average02);
					break;
				case 4:
					this.$emit('update', 'month', this.checkboxString);
					break;
			}
		},
		// Khi hai giá trị chu kỳ thay đổi
		cycleChange() {
			if (this.radioValue === '2') {
				this.$emit('update', 'month', this.cycleTotal);
			}
		},
		// Khi hai giá trị trung bình thay đổi
		averageChange() {
			if (this.radioValue === '3') {
				this.$emit('update', 'month', this.averageTotal);
			}
		},
		// checkboxKhi giá trị thay đổi
		checkboxChange() {
			if (this.radioValue === '4') {
				this.$emit('update', 'month', this.checkboxString);
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
			this.cycle01 = this.checkNum(this.cycle01, 1, 12)
			this.cycle02 = this.checkNum(this.cycle02, 1, 12)
			return this.cycle01 + '-' + this.cycle02;
		},
		// Giá trị dùng để tính trung bình
		averageTotal: function () {
			this.average01 = this.checkNum(this.average01, 1, 12)
			this.average02 = this.checkNum(this.average02, 1, 12)
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
