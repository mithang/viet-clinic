<template>
	<el-form size="small">
		<el-form-item>
			<el-radio v-model='radioValue' :label="1">
				Phút，Ký tự đại diện được phép[, - * /]
			</el-radio>
		</el-form-item>

		<el-form-item>
			<el-radio v-model='radioValue' :label="2">
				Chu kỳ từ
				<el-input-number v-model='cycle01' :min="0" :max="60" /> -
				<el-input-number v-model='cycle02' :min="0" :max="60" /> Phút
			</el-radio>
		</el-form-item>

		<el-form-item>
			<el-radio v-model='radioValue' :label="3">
				Từ
				<el-input-number v-model='average01' :min="0" :max="60" /> phút bắt đầu，mỗi
				<el-input-number v-model='average02' :min="0" :max="60" /> phút thực hiện một lần
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
			cycle01: 1,
			cycle02: 2,
			average01: 0,
			average02: 1,
			checkboxList: [],
			checkNum: this.$options.propsData.check
		}
	},
	name: 'crontab-min',
	props: ['check', 'cron'],
	methods: {
		// Khi giá trị nút radio thay đổi
		radioChange() {
			if (this.radioValue !== 1 && this.cron.second === '*') {
				this.$emit('update', 'second', '0', 'min');
			}
			switch (this.radioValue) {
				case 1:
					this.$emit('update', 'min', '*', 'min');
					this.$emit('update', 'hour', '*', 'min');
					break;
				case 2:
					this.$emit('update', 'min', this.cycle01 + '-' + this.cycle02, 'min');
					break;
				case 3:
					this.$emit('update', 'min', this.average01 + '/' + this.average02, 'min');
					break;
				case 4:
					this.$emit('update', 'min', this.checkboxString, 'min');
					break;
			}
		},
		// Khi hai giá trị chu kỳ thay đổi
		cycleChange() {
			if (this.radioValue === '2') {
				this.$emit('update', 'min', this.cycleTotal, 'min');
			}
		},
		// Khi hai giá trị trung bình thay đổi
		averageChange() {
			if (this.radioValue === '3') {
				this.$emit('update', 'min', this.averageTotal, 'min');
			}
		},
		// checkboxKhi giá trị thay đổi
		checkboxChange() {
			if (this.radioValue === '4') {
				this.$emit('update', 'min', this.checkboxString, 'min');
			}
		},

	},
	watch: {
		"radioValue": "radioChange",
		'cycleTotal': 'cycleChange',
		'averageTotal': 'averageChange',
		'checkboxString': 'checkboxChange',
	},
	computed: {
		// Tính hai giá trị chu kỳ
		cycleTotal: function () {
			this.cycle01 = this.checkNum(this.cycle01, 0, 59)
			this.cycle02 = this.checkNum(this.cycle02, 0, 59)
			return this.cycle01 + '-' + this.cycle02;
		},
		// Giá trị dùng để tính trung bình
		averageTotal: function () {
			this.average01 = this.checkNum(this.average01, 0, 59)
			this.average02 = this.checkNum(this.average02, 1, 59)
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
