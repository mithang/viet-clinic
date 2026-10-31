/**
 * Bộ lọc phân tách hàng nghìn
 * decimal số chữ số thập phân giữ lại, mặc định là hai chữ số
 **/
/* export function thousandsFilter(value, decimal) {
  if (typeof (value) === 'number') {
  	if(typeof (decimal) != 'number') {
  		decimal = 2
  	}
  	return (value).toFixed(decimal).replace(/(\d)(?=(\d{3})+\.)/g, '$1,')
  }
} */

export default {
  'datetimeFormat'(dataStr, pattern = 'DD/MM/YYYY HH:mm:ss') {
    return Vue.prototype.$moment(dataStr).format(pattern);
  },
  'dateFormat'(dataStr, pattern = 'DD/MM/YYYY') {
    return Vue.prototype.$moment(dataStr).format(pattern);
  },
  'timeFormat'(dataStr, pattern = 'HH:mm:ss') {
    return Vue.prototype.$moment(dataStr).format(pattern);
  },
  'formatMoney'(v) {
    // Rút gọn số tiền theo đơn vị Việt Nam: tỷ / triệu / nghìn
    // (trước đây dùng "vạn" — đơn vị của tiếng Trung, không dùng trong
    // báo cáo tài chính Việt Nam).
    if (v >= 1000000000) {
      return (v / 1000000000).toFixed(2) + " tỷ";
    } else if (v >= 1000000) {
      return (v / 1000000).toFixed(2) + " triệu";
    } else if (v >= 1000) {
      return (v / 1000).toFixed(2) + " nghìn";
    }
    return v;
  },
  'yesNo'(v) {
    return v ? 'Có' : 'Không';
  },
  'getObjectValue'(obj, str = '', text = '') {
    if (!obj || typeof(obj) !== 'object' || !str) return text;
    const param = str.split('.');
    return param.reduce((res, curr, index) => {
      if ((index + 1) < param.length) {
        return res[curr] || {}
      } else {
        return res[curr] || text
      }
    }, obj)
  },
  // Định dạng số kiểu Việt Nam: "." phân tách hàng nghìn, "," phân tách
  // phần thập phân (vd 1234567.5 -> 1.234.567,5).
  'numberFormatter'(number = 0) {
    const n = Math.floor((+number) * 100) / 100
    const parts = n.toString().split('.')
    const intPart = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, '.')
    return parts[1] ? intPart + ',' + parts[1] : intPart
  },
  'thousandsFilter'(value, decimal) {
    if (typeof(value) === 'number') {
  	   if (typeof(decimal) !== 'number') {
        decimal = 2
      }
      const parts = value.toFixed(decimal).split('.')
      const intPart = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, '.')
      return parts[1] ? intPart + ',' + parts[1] : intPart
    }
  }
};
