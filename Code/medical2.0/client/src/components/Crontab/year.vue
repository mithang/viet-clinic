<template>
	<el-form size="small">
		<el-form-item>
			<el-radio :label="1" v-model='radioValue'>
				Không điền, Ký tự đại diện được phép[, - * /]
			</el-radio>
		</el-form-item>

		<el-form-item>
			<el-radio :label="2" v-model='radioValue'>
				Mỗi năm
			</el-radio>
		</el-form-item>

		<el-form-item>
			<el-radio :label="3" v-model='radioValue'>
				Chu kỳ từ
				<el-input-number v-model='cycle01' :min='fullYear' /> -
				<el-input-number v-model='cycle02' :min='fullYear' />
			</el-radio>
		</el-form-item>

		<el-form-item>
			<el-radio :label="4" v-model='radioValue'>
				Từ
				<el-input-number v-model='average01' :min='fullYear' /> năm bắt đầu, mỗi
				<el-input-number v-model='average02' :min='fullYear' /> năm thực hiện một lần
			</el-radio>

		</el-form-item>

		<el-form-item>
			<el-radio :label="5" v-model='radioValue'>
				Chỉ định
				<el-select clearable v-model="checkboxList" placeholder="Có thể chọn nhiều" multiple>
					<el-option v-for="item in 9" :key="item" :value="item - 1 + fullYear" :label="item -1 + fullYear" />
				</el-select>
			</el-radio>
		</el-form-item>
	</el-form>
</template>

<script>
export default {
	data() {
		return {
			fullYear: 0,
			radioValue: 1,
			cycle01: 0,
			cycle02: 0,
			average01: 0,
			average02: 1,
			checkboxList: [],
			checkNum: this.$options.propsData.check
		}
	},
	name: 'crontab-year',
	props: ['check', 'month', 'cron'],
	methods: {
		// Khi giá trị nút radio thay đổi
		radioChange() {
			if (this.cron.month === '*') {
				this.$emit('update', 'month', '0', 'year');
			}
			if (this.cron.day === '*') {
				this.$emit('update', 'day', '0', 'year');
			}
			if (this.cron.hour === '*') {
				this.$emit('update', 'hour', '0', 'year');
			}
			if (this.cron.min === '*') {
				this.$emit('update', 'min', '0', 'year');
			}
			if (this.cron.second === '*') {
				this.$emit('update', 'second', '0', 'year');
			}
			switch (this.radioValue) {
				case 1:
					this.$emit('update', 'year', '');
					break;
				case 2:
					this.$emit('update', 'year', '*');
					break;
				case 3:
					this.$emit('update', 'year', this.cycle01 + '-' + this.cycle02);
					break;
				case 4:
					this.$emit('update', 'year', this.average01 + '/' + this.average02);
					break;
				case 5:
					this.$emit('update', 'year', this.checkboxString);
					break;
			}
		},
		// Khi hai giá trị chu kỳ thay đổi
		cycleChange() {
			if (this.radioValue === '3') {
				this.$emit('update', 'year', this.cycleTotal);
			}
		},
		// Khi hai giá trị trung bình thay đổi
		averageChange() {
			if (this.radioValue === '4') {
				this.$emit('update', 'year', this.averageTotal);
			}
		},
		// checkboxKhi giá trị thay đổi
		checkboxChange() {
			if (this.radioValue === '5') {
				this.$emit('update', 'year', this.checkboxString);
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
			this.cycle01 = this.checkNum(this.cycle01, this.fullYear, this.fullYear + 100)
			this.cycle02 = this.checkNum(this.cycle02, this.fullYear + 1, this.fullYear + 101)
			return this.cycle01 + '-' + this.cycle02;
		},
		// Giá trị dùng để tính trung bình
		averageTotal: function () {
			this.average01 = this.checkNum(this.average01, this.fullYear, this.fullYear + 100)
			this.average02 = this.checkNum(this.average02, 1, 10)
			return this.average01 + '/' + this.average02;
		},
		// Tính các giá trị đã chọncheckboxtập hợp giá trị
		checkboxString: function () {
			let str = this.checkboxList.join();
			return str;
		}
	},
	mounted: function () {
		// Chỉ lấy năm hiện tại
		this.fullYear = Number(new Date().getFullYear());
	}
}
</script>
<style>
  .el-input-number--small .el-input__inner {
    padding-left: 0px;
    padding-right: 0px;
  }
</style>
