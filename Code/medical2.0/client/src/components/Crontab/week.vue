<template>
	<el-form size='small'>
		<el-form-item>
			<el-radio v-model='radioValue' :label="1">
				tuần, Ký tự đại diện được phép[, - * / L #]
			</el-radio>
		</el-form-item>

		<el-form-item>
			<el-radio v-model='radioValue' :label="2">
				Không chỉ định
			</el-radio>
		</el-form-item>

		<el-form-item>
			<el-radio v-model='radioValue' :label="3">
				Chu kỳ từ thứ
				<el-input-number v-model='cycle01' :min="1" :max="7" /> -
				<el-input-number v-model='cycle02' :min="1" :max="7" />
			</el-radio>
		</el-form-item>

		<el-form-item>
			<el-radio v-model='radioValue' :label="4">
				thứ
				<el-input-number v-model='average01' :min="1" :max="4" /> tuần thứ
				<el-input-number v-model='average02' :min="1" :max="7" />
			</el-radio>
		</el-form-item>

		<el-form-item>
			<el-radio v-model='radioValue' :label="5">
				Tuần cuối cùng của tháng
				<el-input-number v-model='weekday' :min="1" :max="7" />
			</el-radio>
		</el-form-item>

		<el-form-item>
			<el-radio v-model='radioValue' :label="6">
				Chỉ định
				<el-select clearable v-model="checkboxList" placeholder="Có thể chọn nhiều" multiple style="width:100%">
					<el-option v-for="(item,index) of weekList" :key="index" :value="index+1">{{item}}</el-option>
				</el-select>
			</el-radio>
		</el-form-item>

	</el-form>
</template>

<script>
export default {
	data() {
		return {
			radioValue: 2,
			weekday: 1,
			cycle01: 1,
			cycle02: 2,
			average01: 1,
			average02: 1,
			checkboxList: [],
			weekList: ['Thứ 2', 'Thứ 3', 'Thứ 4', 'Thứ 5', 'Thứ 6', 'Thứ 7', 'Chủ nhật'],
			checkNum: this.$options.propsData.check
		}
	},
	name: 'crontab-week',
	props: ['check', 'cron'],
	methods: {
		// Khi giá trị nút radio thay đổi
		radioChange() {
			if (this.radioValue === 1) {
				this.$emit('update', 'week', '*');
				this.$emit('update', 'year', '*');
			} else {
				if (this.cron.month === '*') {
					this.$emit('update', 'month', '0', 'week');
				}
				if (this.cron.day === '*') {
					this.$emit('update', 'day', '0', 'week');
				}
				if (this.cron.hour === '*') {
					this.$emit('update', 'hour', '0', 'week');
				}
				if (this.cron.min === '*') {
					this.$emit('update', 'min', '0', 'week');
				}
				if (this.cron.second === '*') {
					this.$emit('update', 'second', '0', 'week');
				}
			}
			switch (this.radioValue) {
				case 2:
					this.$emit('update', 'week', '?');
					break;
				case 3:
					this.$emit('update', 'week', this.cycle01 + '-' + this.cycle02);
					break;
				case 4:
					this.$emit('update', 'week', this.average01 + '#' + this.average02);
					break;
				case 5:
					this.$emit('update', 'week', this.weekday + 'L');
					break;
				case 6:
					this.$emit('update', 'week', this.checkboxString);
					break;
			}
		},
		// Theo sự kiện xung đột, thay đổi giá trị radio

		// Khi hai giá trị chu kỳ thay đổi
		cycleChange() {
			if (this.radioValue === '3') {
				this.$emit('update', 'week', this.cycleTotal);
			}
		},
		// Khi hai giá trị trung bình thay đổi
		averageChange() {
			if (this.radioValue === '4') {
				this.$emit('update', 'week', this.averageTotal);
			}
		},
		// Khi giá trị ngày làm việc gần nhất thay đổi
		weekdayChange() {
			if (this.radioValue === '5') {
				this.$emit('update', 'week', this.weekday + 'L');
			}
		},
		// checkboxKhi giá trị thay đổi
		checkboxChange() {
			if (this.radioValue === '6') {
				this.$emit('update', 'week', this.checkboxString);
			}
		},
	},
	watch: {
		"radioValue": "radioChange",
		'cycleTotal': 'cycleChange',
		'averageTotal': 'averageChange',
		'weekdayCheck': 'weekdayChange',
		'checkboxString': 'checkboxChange',
	},
	computed: {
		// Tính hai giá trị chu kỳ
		cycleTotal: function () {
			this.cycle01 = this.checkNum(this.cycle01, 1, 7)
			this.cycle02 = this.checkNum(this.cycle02, 1, 7)
			return this.cycle01 + '-' + this.cycle02;
		},
		// Giá trị dùng để tính trung bình
		averageTotal: function () {
			this.average01 = this.checkNum(this.average01, 1, 4)
			this.average02 = this.checkNum(this.average02, 1, 7)
			return this.average01 + '#' + this.average02;
		},
		// Ngày làm việc gần nhất (Định dạng)
		weekdayCheck: function () {
			this.weekday = this.checkNum(this.weekday, 1, 7)
			return this.weekday;
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
