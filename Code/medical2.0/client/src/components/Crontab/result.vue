<template>
	<div class="popup-result">
		<p class="title">Gần nhất5lần chạy</p>
		<ul class="popup-result-scroll">
			<template v-if='isShow'>
				<li v-for='item in resultList' :key="item">{{item}}</li>
			</template>
			<li v-else>Đang tính kết quả...</li>
		</ul>
	</div>
</template>

<script>
export default {
	data() {
		return {
			dayRule: '',
			dayRuleSup: '',
			dateArr: [],
			resultList: [],
			isShow: false
		}
	},
	name: 'crontab-result',
	methods: {
		// Khi giá trị biểu thức thay đổi, bắt đầu tính kết quả
		expressionChange() {

			// Bắt đầu tính - ẩn kết quả
			this.isShow = false;
			// Lấy mảng quy tắc [0 giây, 1 phút, 2 giờ, 3 ngày, 4 tháng, 5 tuần, 6 năm]
			let ruleArr = this.$options.propsData.ex.split(' ');
			// Dùng để ghi số lần vào vòng lặp
			let nums = 0;
			// Mảng dùng để tạm lưu kết quả quy tắc thời gian
			let resultArr = [];
			// Lấy thời gian hiện tại chính xác đến [năm, tháng, ngày, giờ, phút, giây]
			let nTime = new Date();
			let nYear = nTime.getFullYear();
			let nMonth = nTime.getMonth() + 1;
			let nDay = nTime.getDate();
			let nHour = nTime.getHours();
			let nMin = nTime.getMinutes();
			let nSecond = nTime.getSeconds();
			// Theo quy tắc lấy mảng năm, mảng tháng v.v. có thể trong 100 năm gần nhất
			this.getSecondArr(ruleArr[0]);
			this.getMinArr(ruleArr[1]);
			this.getHourArr(ruleArr[2]);
			this.getDayArr(ruleArr[3]);
			this.getMonthArr(ruleArr[4]);
			this.getWeekArr(ruleArr[5]);
			this.getYearArr(ruleArr[6], nYear);
			// Gán mảng lấy được - tiện sử dụng
			let sDate = this.dateArr[0];
			let mDate = this.dateArr[1];
			let hDate = this.dateArr[2];
			let DDate = this.dateArr[3];
			let MDate = this.dateArr[4];
			let YDate = this.dateArr[5];
			// Lấy chỉ mục của thời gian hiện tại trong mảng
			let sIdx = this.getIndex(sDate, nSecond);
			let mIdx = this.getIndex(mDate, nMin);
			let hIdx = this.getIndex(hDate, nHour);
			let DIdx = this.getIndex(DDate, nDay);
			let MIdx = this.getIndex(MDate, nMonth);
			let YIdx = this.getIndex(YDate, nYear);
			// Hàm đặt lại tháng ngày giờ phút giây (sau dùng khá nhiều)
			const resetSecond = function () {
				sIdx = 0;
				nSecond = sDate[sIdx]
			}
			const resetMin = function () {
				mIdx = 0;
				nMin = mDate[mIdx]
				resetSecond();
			}
			const resetHour = function () {
				hIdx = 0;
				nHour = hDate[hIdx]
				resetMin();
			}
			const resetDay = function () {
				DIdx = 0;
				nDay = DDate[DIdx]
				resetHour();
			}
			const resetMonth = function () {
				MIdx = 0;
				nMonth = MDate[MIdx]
				resetDay();
			}
			// Nếu năm hiện tại không phải giá trị hiện tại trong mảng
			if (nYear !== YDate[YIdx]) {
				resetMonth();
			}
			// Nếu tháng hiện tại không phải giá trị hiện tại trong mảng
			if (nMonth !== MDate[MIdx]) {
				resetDay();
			}
			// Nếu hiện tại“ngày”không phải giá trị hiện tại trong mảng
			if (nDay !== DDate[DIdx]) {
				resetHour();
			}
			// Nếu hiện tại“giờ”không phải giá trị hiện tại trong mảng
			if (nHour !== hDate[hIdx]) {
				resetMin();
			}
			// Nếu hiện tại“phút”không phải giá trị hiện tại trong mảng
			if (nMin !== mDate[mIdx]) {
				resetSecond();
			}

			// Vòng lặp mảng năm
			goYear: for (let Yi = YIdx; Yi < YDate.length; Yi++) {
				let YY = YDate[Yi];
				// Khi đạt giá trị tối đa
				if (nMonth > MDate[MDate.length - 1]) {
					resetMonth();
					continue;
				}
				// Vòng lặp mảng tháng
				goMonth: for (let Mi = MIdx; Mi < MDate.length; Mi++) {
					// Gán giá trị, thuận tiện cho phép tính phía sau
					let MM = MDate[Mi];
					MM = MM < 10 ? '0' + MM : MM;
					// Khi đạt giá trị tối đa
					if (nDay > DDate[DDate.length - 1]) {
						resetDay();
						if (Mi === MDate.length - 1) {
							resetMonth();
							continue goYear;
						}
						continue;
					}
					// Vòng lặp mảng ngày
					goDay: for (let Di = DIdx; Di < DDate.length; Di++) {
						// Gán giá trị, thuận tiện cho phép tính phía sau
						let DD = DDate[Di];
						let thisDD = DD < 10 ? '0' + DD : DD;

						// Khi đạt giá trị tối đa
						if (nHour > hDate[hDate.length - 1]) {
							resetHour();
							if (Di === DDate.length - 1) {
								resetDay();
								if (Mi === MDate.length - 1) {
									resetMonth();
									continue goYear;
								}
								continue goMonth;
							}
							continue;
						}

						// Phán đoán tính hợp pháp của ngày, nếu không hợp pháp cũng thoát vòng lặp hiện tại
						if (this.checkDate(YY + '-' + MM + '-' + thisDD + ' 00:00:00') !== true && this.dayRule !== 'workDay' && this.dayRule !== 'lastWeek' && this.dayRule !== 'lastDay') {
							resetDay();
							continue goMonth;
						}
						// Nếu quy tắc ngày có giá trị
						if (this.dayRule === 'lastDay') {
							// Nếu không phải ngày hợp lệ thì cần điều chỉnh ngày về ngày hợp lệ tức ngày cuối cùng của tháng

							if (this.checkDate(YY + '-' + MM + '-' + thisDD + ' 00:00:00') !== true) {
								while (DD > 0 && this.checkDate(YY + '-' + MM + '-' + thisDD + ' 00:00:00') !== true) {
									DD--;

									thisDD = DD < 10 ? '0' + DD : DD;
								}
							}
						} else if (this.dayRule === 'workDay') {
							// Kiểm tra và điều chỉnh: nếu truyền vào ngày kiểu 30 tháng 2 thì cần điều chỉnh về ngày cuối tháng hợp lệ
							if (this.checkDate(YY + '-' + MM + '-' + thisDD + ' 00:00:00') !== true) {
								while (DD > 0 && this.checkDate(YY + '-' + MM + '-' + thisDD + ' 00:00:00') !== true) {
									DD--;
									thisDD = DD < 10 ? '0' + DD : DD;
								}
							}
							// Lấy ngày đạt điều kiện là thứ mấy
							let thisWeek = this.formatDate(new Date(YY + '-' + MM + '-' + thisDD + ' 00:00:00'), 'week');
							// Khi là Chủ nhật
							if (thisWeek === 0) {
								// Tìm ngày tiếp theo trước, và phán đoán xem có phải cuối tháng không
								DD++;
								thisDD = DD < 10 ? '0' + DD : DD;
								// Phán đoán ngày tiếp theo đã không phải ngày hợp lệ
								if (this.checkDate(YY + '-' + MM + '-' + thisDD + ' 00:00:00') !== true) {
									DD -= 3;
								}
							} else if (thisWeek === 6) {
								// Khi thứ 6 chỉ cần phán đoán không phải ngày 1 là có thể thao tác
								if (this.dayRuleSup !== 1) {
									DD--;
								} else {
									DD += 2;
								}
							}
						} else if (this.dayRule === 'weekDay') {
							// Nếu đã chỉ định là thứ mấy
							// Lấy ngày hiện tại thuộc thứ mấy
							let thisWeek = this.formatDate(new Date(YY + '-' + MM + '-' + DD + ' 00:00:00'), 'week');
							// Xác thực thứ hiện tại có trong pool thứ (dayRuleSup) không
							if (Array.indexOf(this.dayRuleSup, thisWeek) < 0) {
								// Khi đạt giá trị tối đa
								if (Di === DDate.length - 1) {
									resetDay();
									if (Mi === MDate.length - 1) {
										resetMonth();
										continue goYear;
									}
									continue goMonth;
								}
								continue;
							}
						} else if (this.dayRule === 'assWeek') {
							// Nếu đã chỉ định là tuần thứ mấy của thứ nào
							// Lấy ngày 1 của mỗi tháng thuộc thứ mấy
							let thisWeek = this.formatDate(new Date(YY + '-' + MM + '-' + DD + ' 00:00:00'), 'week');
							if (this.dayRuleSup[1] >= thisWeek) {
								DD = (this.dayRuleSup[0] - 1) * 7 + this.dayRuleSup[1] - thisWeek + 1;
							} else {
								DD = this.dayRuleSup[0] * 7 + this.dayRuleSup[1] - thisWeek + 1;
							}
						} else if (this.dayRule === 'lastWeek') {
							// Nếu đã chỉ định thứ mấy cuối cùng của mỗi tháng
							// Xác thực và điều chỉnh nếu ngày như 30 tháng 2 truyền vào thì cần điều chỉnh về cuối tháng bình thường
							if (this.checkDate(YY + '-' + MM + '-' + thisDD + ' 00:00:00') !== true) {
								while (DD > 0 && this.checkDate(YY + '-' + MM + '-' + thisDD + ' 00:00:00') !== true) {
									DD--;
									thisDD = DD < 10 ? '0' + DD : DD;
								}
							}
							// Lấy ngày cuối cùng của tháng là thứ mấy
							let thisWeek = this.formatDate(new Date(YY + '-' + MM + '-' + thisDD + ' 00:00:00'), 'week');
							// Tìm thứ gần nhất trong yêu cầu
							if (this.dayRuleSup < thisWeek) {
								DD -= thisWeek - this.dayRuleSup;
							} else if (this.dayRuleSup > thisWeek) {
								DD -= 7 - (this.dayRuleSup - thisWeek)
							}
						}
						// Phán đoán giá trị thời gian có nhỏ hơn 10 thì thay thành định dạng "05"
						DD = DD < 10 ? '0' + DD : DD;

						// Vòng lặp“giờ”Mảng
						goHour: for (let hi = hIdx; hi < hDate.length; hi++) {
							let hh = hDate[hi] < 10 ? '0' + hDate[hi] : hDate[hi]

							// Khi đạt giá trị tối đa
							if (nMin > mDate[mDate.length - 1]) {
								resetMin();
								if (hi === hDate.length - 1) {
									resetHour();
									if (Di === DDate.length - 1) {
										resetDay();
										if (Mi === MDate.length - 1) {
											resetMonth();
											continue goYear;
										}
										continue goMonth;
									}
									continue goDay;
								}
								continue;
							}
							// Vòng lặp"phút"Mảng
							goMin: for (let mi = mIdx; mi < mDate.length; mi++) {
								let mm = mDate[mi] < 10 ? '0' + mDate[mi] : mDate[mi];

								// Khi đạt giá trị tối đa
								if (nSecond > sDate[sDate.length - 1]) {
									resetSecond();
									if (mi === mDate.length - 1) {
										resetMin();
										if (hi === hDate.length - 1) {
											resetHour();
											if (Di === DDate.length - 1) {
												resetDay();
												if (Mi === MDate.length - 1) {
													resetMonth();
													continue goYear;
												}
												continue goMonth;
											}
											continue goDay;
										}
										continue goHour;
									}
									continue;
								}
								// Vòng lặp"giây"Mảng
								goSecond: for (let si = sIdx; si <= sDate.length - 1; si++) {
									let ss = sDate[si] < 10 ? '0' + sDate[si] : sDate[si];
									// Thêm thời gian hiện tại (tính hợp pháp thời gian đã được phán đoán khi vòng lặp ngày)
									if (MM !== '00' && DD !== '00') {
										resultArr.push(YY + '-' + MM + '-' + DD + ' ' + hh + ':' + mm + ':' + ss)
										nums++;
									}
									// Nếu đủ số lượng thì thoát vòng lặp
									if (nums === 5) break goYear;
									// Khi đạt giá trị tối đa
									if (si === sDate.length - 1) {
										resetSecond();
										if (mi === mDate.length - 1) {
											resetMin();
											if (hi === hDate.length - 1) {
												resetHour();
												if (Di === DDate.length - 1) {
													resetDay();
													if (Mi === MDate.length - 1) {
														resetMonth();
														continue goYear;
													}
													continue goMonth;
												}
												continue goDay;
											}
											continue goHour;
										}
										continue goMin;
									}
								} //goSecond
							} //goMin
						}//goHour
					}//goDay
				}//goMonth
			}
			// Xác định số kết quả trong 100 năm
			if (resultArr.length === 0) {
				this.resultList = ['Không có kết quả nào thoả mãn điều kiện!'];
			} else {
				this.resultList = resultArr;
				if (resultArr.length !== 5) {
					this.resultList.push('Trong 100 năm gần nhất chỉ có ' + resultArr.length + ' kết quả như trên!')
				}
			}
			// Tính xong - hiển thị kết quả
			this.isShow = true;


		},
		// Dùng để tính chỉ mục của một chữ số trong mảng
		getIndex(arr, value) {
			if (value <= arr[0] || value > arr[arr.length - 1]) {
				return 0;
			} else {
				for (let i = 0; i < arr.length - 1; i++) {
					if (value > arr[i] && value <= arr[i + 1]) {
						return i + 1;
					}
				}
			}
		},
		// Lấy mảng "năm"
		getYearArr(rule, year) {
			this.dateArr[5] = this.getOrderArr(year, year + 100);
			if (rule !== undefined) {
				if (rule.indexOf('-') >= 0) {
					this.dateArr[5] = this.getCycleArr(rule, year + 100, false)
				} else if (rule.indexOf('/') >= 0) {
					this.dateArr[5] = this.getAverageArr(rule, year + 100)
				} else if (rule !== '*') {
					this.dateArr[5] = this.getAssignArr(rule)
				}
			}
		},
		// Lấy mảng "tháng"
		getMonthArr(rule) {
			this.dateArr[4] = this.getOrderArr(1, 12);
			if (rule.indexOf('-') >= 0) {
				this.dateArr[4] = this.getCycleArr(rule, 12, false)
			} else if (rule.indexOf('/') >= 0) {
				this.dateArr[4] = this.getAverageArr(rule, 12)
			} else if (rule !== '*') {
				this.dateArr[4] = this.getAssignArr(rule)
			}
		},
		// Lấy mảng "ngày" - chủ yếu là quy tắc ngày
		getWeekArr(rule) {
			// Chỉ khi cả hai giá trị quy tắc ngày đều là "" thì biểu thị ngày có tùy chọn
			if (this.dayRule === '' && this.dayRuleSup === '') {
				if (rule.indexOf('-') >= 0) {
					this.dayRule = 'weekDay';
					this.dayRuleSup = this.getCycleArr(rule, 7, false)
				} else if (rule.indexOf('#') >= 0) {
					this.dayRule = 'assWeek';
					let matchRule = rule.match(/[0-9]{1}/g);
					this.dayRuleSup = [Number(matchRule[0]), Number(matchRule[1])];
					this.dateArr[3] = [1];
					if (this.dayRuleSup[1] === 7) {
						this.dayRuleSup[1] = 0;
					}
				} else if (rule.indexOf('L') >= 0) {
					this.dayRule = 'lastWeek';
					this.dayRuleSup = Number(rule.match(/[0-9]{1,2}/g)[0]);
					this.dateArr[3] = [31];
					if (this.dayRuleSup === 7) {
						this.dayRuleSup = 0;
					}
				} else if (rule !== '*' && rule !== '?') {
					this.dayRule = 'weekDay';
					this.dayRuleSup = this.getAssignArr(rule)
				}
				// Nếu weekDay thì điều chỉnh 7 thành 0【giá trị week 0 tức là Chủ nhật】
				if (this.dayRule === 'weekDay') {
					for (let i = 0; i < this.dayRuleSup.length; i++) {
						if (this.dayRuleSup[i] === 7) {
							this.dayRuleSup[i] = 0;
						}
					}
				}
			}
		},
		// Lấy mảng "ngày" - ít trường hợp là quy tắc ngày
		getDayArr(rule) {
			this.dateArr[3] = this.getOrderArr(1, 31);
			this.dayRule = '';
			this.dayRuleSup = '';
			if (rule.indexOf('-') >= 0) {
				this.dateArr[3] = this.getCycleArr(rule, 31, false)
				this.dayRuleSup = 'null';
			} else if (rule.indexOf('/') >= 0) {
				this.dateArr[3] = this.getAverageArr(rule, 31)
				this.dayRuleSup = 'null';
			} else if (rule.indexOf('W') >= 0) {
				this.dayRule = 'workDay';
				this.dayRuleSup = Number(rule.match(/[0-9]{1,2}/g)[0]);
				this.dateArr[3] = [this.dayRuleSup];
			} else if (rule.indexOf('L') >= 0) {
				this.dayRule = 'lastDay';
				this.dayRuleSup = 'null';
				this.dateArr[3] = [31];
			} else if (rule !== '*' && rule !== '?') {
				this.dateArr[3] = this.getAssignArr(rule)
				this.dayRuleSup = 'null';
			} else if (rule === '*') {
				this.dayRuleSup = 'null';
			}
		},
		// Lấy mảng "giờ"
		getHourArr(rule) {
			this.dateArr[2] = this.getOrderArr(0, 23);
			if (rule.indexOf('-') >= 0) {
				this.dateArr[2] = this.getCycleArr(rule, 24, true)
			} else if (rule.indexOf('/') >= 0) {
				this.dateArr[2] = this.getAverageArr(rule, 23)
			} else if (rule !== '*') {
				this.dateArr[2] = this.getAssignArr(rule)
			}
		},
		// Lấy mảng "phút"
		getMinArr(rule) {
			this.dateArr[1] = this.getOrderArr(0, 59);
			if (rule.indexOf('-') >= 0) {
				this.dateArr[1] = this.getCycleArr(rule, 60, true)
			} else if (rule.indexOf('/') >= 0) {
				this.dateArr[1] = this.getAverageArr(rule, 59)
			} else if (rule !== '*') {
				this.dateArr[1] = this.getAssignArr(rule)
			}
		},
		// Lấy mảng "giây"
		getSecondArr(rule) {
			this.dateArr[0] = this.getOrderArr(0, 59);
			if (rule.indexOf('-') >= 0) {
				this.dateArr[0] = this.getCycleArr(rule, 60, true)
			} else if (rule.indexOf('/') >= 0) {
				this.dateArr[0] = this.getAverageArr(rule, 59)
			} else if (rule !== '*') {
				this.dateArr[0] = this.getAssignArr(rule)
			}
		},
		// Theo min-max truyền vào trả về một mảng tuần tự
		getOrderArr(min, max) {
			let arr = [];
			for (let i = min; i <= max; i++) {
				arr.push(i);
			}
			return arr;
		},
		// Theo giá trị rời rạc được chỉ định trong quy tắc trả về một mảng
		getAssignArr(rule) {
			let arr = [];
			let assiginArr = rule.split(',');
			for (let i = 0; i < assiginArr.length; i++) {
				arr[i] = Number(assiginArr[i])
			}
			arr.sort(this.compare)
			return arr;
		},
		// Theo quy tắc số học nhất định tính toán trả về một mảng
		getAverageArr(rule, limit) {
			let arr = [];
			let agArr = rule.split('/');
			let min = Number(agArr[0]);
			let step = Number(agArr[1]);
			while (min <= limit) {
				arr.push(min);
				min += step;
			}
			return arr;
		},
		// Theo quy tắc trả về một mảng có tính chu kỳ
		getCycleArr(rule, limit, status) {
			// status--biểu thị có bắt đầu từ 0 hay không (thì bắt đầu từ 1)
			let arr = [];
			let cycleArr = rule.split('-');
			let min = Number(cycleArr[0]);
			let max = Number(cycleArr[1]);
			if (min > max) {
				max += limit;
			}
			for (let i = min; i <= max; i++) {
				let add = 0;
				if (status === false && i % limit === 0) {
					add = limit;
				}
				arr.push(Math.round(i % limit + add))
			}
			arr.sort(this.compare)
			return arr;
		},
		// So sánh độ lớn số (dùng cho Array.sort)
		compare(value1, value2) {
			if (value2 - value1 > 0) {
				return -1;
			} else {
				return 1;
			}
		},
		// Định dạng định dạng ngày như: 2017-9-19 18:04:33
		formatDate(value, type) {
			// Tính các giá trị liên quan đến ngày
			let time = typeof value === 'number' ? new Date(value) : value;
			let Y = time.getFullYear();
			let M = time.getMonth() + 1;
			let D = time.getDate();
			let h = time.getHours();
			let m = time.getMinutes();
			let s = time.getSeconds();
			let week = time.getDay();
			// Nếu đã truyền type
			if (type === undefined) {
				return Y + '-' + (M < 10 ? '0' + M : M) + '-' + (D < 10 ? '0' + D : D) + ' ' + (h < 10 ? '0' + h : h) + ':' + (m < 10 ? '0' + m : m) + ':' + (s < 10 ? '0' + s : s);
			} else if (type === 'week') {
				return week;
			}
		},
		// Kiểm tra ngày có tồn tại không
		checkDate(value) {
			let time = new Date(value);
			let format = this.formatDate(time)
			return value === format ? true : false;
		}
	},
	watch: {
		'ex': 'expressionChange'
	},
	props: ['ex'],
	mounted: function () {
		// Khởi tạo lấy kết quả một lần
		this.expressionChange();
	}
}

</script>
<style>
  .el-input-number--small .el-input__inner {
    padding-left: 0px;
    padding-right: 0px;
  }
</style>
