<template>
	<el-form size="small">
		<el-form-item>
			<el-radio v-model='radioValue' :label="1">
				ngày, Ký tự đại diện được phép[, - * / L M]
			</el-radio>
		</el-form-item>

		<el-form-item>
			<el-radio v-model='radioValue' :label="2">
				Không chỉ định
			</el-radio>
		</el-form-item>

		<el-form-item>
			<el-radio v-model='radioValue' :label="3">
				Chu kỳ từ
				<el-input-number v-model='cycle01' :min="0" :max="31" /> -
				<el-input-number v-model='cycle02' :min="0" :max="31" /> ngày
			</el-radio>
		</el-form-item>

		<el-form-item>
			<el-radio v-model='radioValue' :label="4">
				Từ
				<el-input-number v-model='average01' :min="0" :max="31" /> ngày bắt đầu, mỗi
				<el-input-number v-model='average02' :min="0" :max="31" /> ngày thực hiện một lần
			</el-radio>
		</el-form-item>

		<el-form-item>
			<el-radio v-model='radioValue' :label="5">
				Mỗi tháng
				<el-input-number v-model='workday' :min="0" :max="31" /> ngày làm việc gần nhất trong tháng
			</el-radio>
		</el-form-item>

		<el-form-item>
			<el-radio v-model='radioValue' :label="6">
				Ngày cuối cùng của tháng
			</el-radio>
		</el-form-item>

		<el-form-item>
			<el-radio v-model='radioValue' :label="7">
				Chỉ định
				<el-select clearable v-model="checkboxList" placeholder="Có thể chọn nhiều" multiple style="width:100%">
					<el-option v-for="item in 31" :key="item" :value="item">{{item}}</el-option>
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
			workday: 1,
			cycle01: 1,
			cycle02: 2,
			average01: 1,
			average02: 1,
			checkboxList: [],
			checkNum: this.$options.propsData.check
		}
	},
	name: 'crontab-day',
	props: ['check', 'cron'],
	methods: {
		// Khi giá trị nút radio thay đổi
		radioChange() {
			('day rachange');
			if (this.radioValue === 1) {
				this.$emit('update', 'day', '*', 'day');
				this.$emit('update', 'week', '?', 'day');
				this.$emit('update', 'month', '*', 'day');
			} else {
				if (this.cron.hour === '*') {
					this.$emit('update', 'hour', '0', 'day');
				}
				if (this.cron.min === '*') {
					this.$emit('update', 'min', '0', 'day');
				}
				if (this.cron.second === '*') {
					this.$emit('update', 'second', '0', 'day');
				}
			}

			switch (this.radioValue) {
				case 2:
					this.$emit('update', 'day', '?');
					break;
				case 3:
					this.$emit('update', 'day', this.cycle01 + '-' + this.cycle02);
					break;
				case 4:
					this.$emit('update', 'day', this.average01 + '/' + this.average02);
					break;
				case 5:
					this.$emit('update', 'day', this.workday + 'W');
					break;
				case 6:
					this.$emit('update', 'day', 'L');
					break;
				case 7:
					this.$emit('update', 'day', this.checkboxString);
					break;
			}
			('day rachange end');
		},
		// Khi hai giá trị chu kỳ thay đổi
		cycleChange() {
			if (this.radioValue === '3') {
				this.$emit('update', 'day', this.cycleTotal);
			}
		},
		// Khi hai giá trị trung bình thay đổi
		averageChange() {
			if (this.radioValue === '4') {
				this.$emit('update', 'day', this.averageTotal);
			}
		},
		// Khi giá trị ngày làm việc gần nhất thay đổi
		workdayChange() {
			if (this.radioValue === '5') {
				this.$emit('update', 'day', this.workday + 'W');
			}
		},
		// checkboxKhi giá trị thay đổi
		checkboxChange() {
			if (this.radioValue === '7') {
				this.$emit('update', 'day', this.checkboxString);
			}
		},
		// Khi tuần do thành phần cha truyền vào thay đổi kích hoạt
		weekChange() {
			//Xác định giá trị week và day không thể cùng lúc là "?"
			if (this.cron.week === '?' && this.radioValue === '2') {
				this.radioValue = '1';
			} else if (this.cron.week !== '?' && this.radioValue !== '2') {
				this.radioValue = '2';
			}
		},
	},
	watch: {
		"radioValue": "radioChange",
		'cycleTotal': 'cycleChange',
		'averageTotal': 'averageChange',
		'workdayCheck': 'workdayChange',
		'checkboxString': 'checkboxChange',
	},
	computed: {
		// Tính hai giá trị chu kỳ
		cycleTotal: function () {
			this.cycle01 = this.checkNum(this.cycle01, 1, 31)
			this.cycle02 = this.checkNum(this.cycle02, 1, 31)
			return this.cycle01 + '-' + this.cycle02;
		},
		// Giá trị dùng để tính trung bình
		averageTotal: function () {
			this.average01 = this.checkNum(this.average01, 1, 31)
			this.average02 = this.checkNum(this.average02, 1, 31)
			return this.average01 + '/' + this.average02;
		},
		// Tính định dạng ngày làm việc
		workdayCheck: function () {
			this.workday = this.checkNum(this.workday, 1, 31)
			return this.workday;
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
