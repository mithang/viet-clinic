/**
 * @Chuyển đổi qua lại dương lịch và âm lịch trong khoảng 1900-2100
 * @charset UTF-8
 * @Tác giả  Jea Dương (JJonline@JJonline.Cn)
 * @Time    2014-7-21
 * @Time    2016-8-13 Fixed 2033hex、Attribution Annals
 * @Time    2016-9-25 Fixed lunar LeapMonth Param Bug
 * @Time    2017-7-24 Fixed use getTerm Func Param Error.use solar year,NOT lunar year
 * @Version 1.0.3
 * @Dương lịch sang âm lịch: calendar.solar2lunar(1987,11,01); //[có thể bỏ số 0 ở đầu tham số]
 * @Âm lịch sang dương lịch: calendar.lunar2solar(1987,09,10); //[có thể bỏ số 0 ở đầu tham số]
 */
export const calendar = {

    /**
     * Bảng thông tin tháng nhuận lớn/nhỏ của âm lịch 1900-2100
     * @Array Of Property
     * @return Hex
     */
    lunarInfo: [0x04bd8, 0x04ae0, 0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0, 0x09ad0, 0x055d2,//1900-1909
        0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540, 0x0d6a0, 0x0ada2, 0x095b0, 0x14977,//1910-1919
        0x04970, 0x0a4b0, 0x0b4b5, 0x06a50, 0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970,//1920-1929
        0x06566, 0x0d4a0, 0x0ea50, 0x16a95, 0x05ad0, 0x02b60, 0x186e3, 0x092e0, 0x1c8d7, 0x0c950,//1930-1939
        0x0d4a0, 0x1d8a6, 0x0b550, 0x056a0, 0x1a5b4, 0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557,//1940-1949
        0x06ca0, 0x0b550, 0x15355, 0x04da0, 0x0a5b0, 0x14573, 0x052b0, 0x0a9a8, 0x0e950, 0x06aa0,//1950-1959
        0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570, 0x05260, 0x0f263, 0x0d950, 0x05b57, 0x056a0,//1960-1969
        0x096d0, 0x04dd5, 0x04ad0, 0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558, 0x0b540, 0x0b6a0, 0x195a6,//1970-1979
        0x095b0, 0x049b0, 0x0a974, 0x0a4b0, 0x0b27a, 0x06a50, 0x06d40, 0x0af46, 0x0ab60, 0x09570,//1980-1989
        0x04af5, 0x04970, 0x064b0, 0x074a3, 0x0ea50, 0x06b58, 0x05ac0, 0x0ab60, 0x096d5, 0x092e0,//1990-1999
        0x0c960, 0x0d954, 0x0d4a0, 0x0da50, 0x07552, 0x056a0, 0x0abb7, 0x025d0, 0x092d0, 0x0cab5,//2000-2009
        0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50, 0x055d9, 0x04ba0, 0x0a5b0, 0x15176, 0x052b0, 0x0a930,//2010-2019
        0x07954, 0x06aa0, 0x0ad50, 0x05b52, 0x04b60, 0x0a6e6, 0x0a4e0, 0x0d260, 0x0ea65, 0x0d530,//2020-2029
        0x05aa0, 0x076a3, 0x096d0, 0x04afb, 0x04ad0, 0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520, 0x0dd45,//2030-2039
        0x0b5a0, 0x056d0, 0x055b2, 0x049b0, 0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20, 0x0ada0,//2040-2049
        /**Add By JJonline@JJonline.Cn**/
        0x14b63, 0x09370, 0x049f8, 0x04970, 0x064b0, 0x168a6, 0x0ea50, 0x06b20, 0x1a6c4, 0x0aae0,//2050-2059
        0x092e0, 0x0d2e3, 0x0c960, 0x0d557, 0x0d4a0, 0x0da50, 0x05d55, 0x056a0, 0x0a6d0, 0x055d4,//2060-2069
        0x052d0, 0x0a9b8, 0x0a950, 0x0b4a0, 0x0b6a6, 0x0ad50, 0x055a0, 0x0aba4, 0x0a5b0, 0x052b0,//2070-2079
        0x0b273, 0x06930, 0x07337, 0x06aa0, 0x0ad50, 0x14b55, 0x04b60, 0x0a570, 0x054e4, 0x0d160,//2080-2089
        0x0e968, 0x0d520, 0x0daa0, 0x16aa6, 0x056d0, 0x04ae0, 0x0a9d4, 0x0a2d0, 0x0d150, 0x0f252,//2090-2099
        0x0d520],//2100

    /**
     * Bảng số ngày thông thường của từng tháng dương lịch
     * @Array Of Property
     * @return Number
     */
    solarMonth: [31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31],

    /**
     * Bảng tra nhanh Thiên can (Can Chi)
     * @Array Of Property trans["Giáp","Ất","Bính","Đinh","Mậu","Kỷ","Canh","Tân","Nhâm","Quý"]
     * @return Cn string
     */
    Gan: ["Giáp", "Ất", "Bính", "Đinh", "Mậu", "Kỷ", "Canh", "Tân", "Nhâm", "Quý"],

    /**
     * Bảng tra nhanh Địa chi (Can Chi)
     * @Array Of Property
     * @trans["Tý","Sửu","Dần","Mão","Thìn","Tỵ","Ngọ","Mùi","Thân","Dậu","Tuất","Hợi"]
     * @return Cn string
     */
    Zhi: ["Tý", "Sửu", "Dần", "Mão", "Thìn", "Tỵ", "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi"],

    /**
     * Bảng tra nhanh Địa chi <=> con giáp
     * @Array Of Property
     * @trans["Chuột","Trâu","Hổ","Mèo","Rồng","Rắn","Ngựa","Dê","Khỉ","Gà","Chó","Lợn"]
     * @return Cn string
     */
    Animals: ["Chuột", "Trâu", "Hổ", "Mèo", "Rồng", "Rắn", "Ngựa", "Dê", "Khỉ", "Gà", "Chó", "Lợn"],

    /**
     * Ngày lễ dương lịch
     */
    festival: {
        '1-1': {title: 'Tết Dương lịch'},
        '2-14': {title: 'Lễ Tình nhân'},
        '5-1': {title: 'Ngày Quốc tế Lao động'},
        '5-4': {title: 'Ngày Thanh niên'},
        '6-1': {title: 'Ngày Thiếu nhi'},
        '9-10': {title: 'Ngày Nhà giáo'},
        '10-1': {title: 'Ngày Quốc khánh'},
        '12-25': {title: 'Giáng sinh'},

        '3-8': {title: 'Ngày Phụ nữ'},
        '3-12': {title: 'Ngày Trồng cây'},
        '4-1': {title: 'Ngày Cá tháng Tư'},
        '5-12': {title: 'Ngày Điều dưỡng'},
        '7-1': {title: 'Ngày thành lập Đảng'},
        '8-1': {title: 'Ngày thành lập Quân đội'},
        '12-24': {title: 'Đêm Giáng sinh'},
    },

    /**
     * Ngày lễ âm lịch
     */
    lFestival: {
        '12-30': {title: 'Đêm giao thừa'},
        '1-1': {title: 'Tết Nguyên đán'},
        '1-15': {title: 'Tết Nguyên tiêu'},
        '2-2': {title: 'Lễ Rồng ngẩng đầu'},
        '5-5': {title: 'Tết Đoan ngọ'},
        '7-7': {title: 'Lễ Thất tịch'},
        '7-15': {title: 'Lễ Vu lan'},
        '8-15': {title: 'Tết Trung thu'},
        '9-9': {title: 'Tết Trùng dương'},
        '10-1': {title: 'Lễ Hàn y'},
        '10-15': {title: 'Lễ Hạ nguyên'},
        '12-8': {title: 'Lễ Lạp bát'},
        '12-23': {title: 'Tết Ông Táo miền Bắc'},
        '12-24': {title: 'Tết Ông Táo miền Nam'},
    },

    /**
     * Trả về các ngày lễ dương lịch đã định nghĩa mặc định
     */
    getFestival() {
        return this.festival
    },

    /**
     * Trả về các ngày lễ âm lịch đã định nghĩa mặc định
     */
    getLunarFestival() {
        return this.lFestival
    },

    /**
     *
     * @param param {Object} Dữ liệu nhập theo định dạng festival, dùng để đặt ngày lễ dương lịch
     */
    setFestival(param = {}) {
        this.festival = param
    },

    /**
     *
     * @param param {Object} Dữ liệu nhập theo định dạng lFestival, dùng để đặt ngày lễ âm lịch
     */
    setLunarFestival(param = {}) {
        this.lFestival = param
    },

    /**
     * Bảng tra 24 tiết khí
     * @Array Of Property
     * @trans["Tiểu Hàn","Đại Hàn","Lập Xuân","Vũ Thủy","Kinh Trập","Xuân Phân","Thanh Minh","Cốc Vũ","Lập Hạ","Tiểu Mãn","Mang Chủng","Hạ Chí","Tiểu Thử","Đại Thử","Lập Thu","Xử Thử","Bạch Lộ","Thu Phân","Hàn Lộ","Sương Giáng","Lập Đông","Tiểu Tuyết","Đại Tuyết","Đông Chí"]
     * @return Cn string
     */
    solarTerm: ["Tiểu Hàn", "Đại Hàn", "Lập Xuân", "Vũ Thủy", "Kinh Trập", "Xuân Phân", "Thanh Minh", "Cốc Vũ", "Lập Hạ", "Tiểu Mãn", "Mang Chủng", "Hạ Chí", "Tiểu Thử", "Đại Thử", "Lập Thu", "Xử Thử", "Bạch Lộ", "Thu Phân", "Hàn Lộ", "Sương Giáng", "Lập Đông", "Tiểu Tuyết", "Đại Tuyết", "Đông Chí"],

    /**
     * Bảng tra ngày của 24 tiết khí theo từng năm 1900-2100
     * @Array Of Property
     * @return 0x string For splice
     */
    sTermInfo: ['9778397bd097c36b0b6fc9274c91aa', '97b6b97bd19801ec9210c965cc920e', '97bcf97c3598082c95f8c965cc920f',
        '97bd0b06bdb0722c965ce1cfcc920f', 'b027097bd097c36b0b6fc9274c91aa', '97b6b97bd19801ec9210c965cc920e',
        '97bcf97c359801ec95f8c965cc920f', '97bd0b06bdb0722c965ce1cfcc920f', 'b027097bd097c36b0b6fc9274c91aa',
        '97b6b97bd19801ec9210c965cc920e', '97bcf97c359801ec95f8c965cc920f', '97bd0b06bdb0722c965ce1cfcc920f',
        'b027097bd097c36b0b6fc9274c91aa', '9778397bd19801ec9210c965cc920e', '97b6b97bd19801ec95f8c965cc920f',
        '97bd09801d98082c95f8e1cfcc920f', '97bd097bd097c36b0b6fc9210c8dc2', '9778397bd197c36c9210c9274c91aa',
        '97b6b97bd19801ec95f8c965cc920e', '97bd09801d98082c95f8e1cfcc920f', '97bd097bd097c36b0b6fc9210c8dc2',
        '9778397bd097c36c9210c9274c91aa', '97b6b97bd19801ec95f8c965cc920e', '97bcf97c3598082c95f8e1cfcc920f',
        '97bd097bd097c36b0b6fc9210c8dc2', '9778397bd097c36c9210c9274c91aa', '97b6b97bd19801ec9210c965cc920e',
        '97bcf97c3598082c95f8c965cc920f', '97bd097bd097c35b0b6fc920fb0722', '9778397bd097c36b0b6fc9274c91aa',
        '97b6b97bd19801ec9210c965cc920e', '97bcf97c3598082c95f8c965cc920f', '97bd097bd097c35b0b6fc920fb0722',
        '9778397bd097c36b0b6fc9274c91aa', '97b6b97bd19801ec9210c965cc920e', '97bcf97c359801ec95f8c965cc920f',
        '97bd097bd097c35b0b6fc920fb0722', '9778397bd097c36b0b6fc9274c91aa', '97b6b97bd19801ec9210c965cc920e',
        '97bcf97c359801ec95f8c965cc920f', '97bd097bd097c35b0b6fc920fb0722', '9778397bd097c36b0b6fc9274c91aa',
        '97b6b97bd19801ec9210c965cc920e', '97bcf97c359801ec95f8c965cc920f', '97bd097bd07f595b0b6fc920fb0722',
        '9778397bd097c36b0b6fc9210c8dc2', '9778397bd19801ec9210c9274c920e', '97b6b97bd19801ec95f8c965cc920f',
        '97bd07f5307f595b0b0bc920fb0722', '7f0e397bd097c36b0b6fc9210c8dc2', '9778397bd097c36c9210c9274c920e',
        '97b6b97bd19801ec95f8c965cc920f', '97bd07f5307f595b0b0bc920fb0722', '7f0e397bd097c36b0b6fc9210c8dc2',
        '9778397bd097c36c9210c9274c91aa', '97b6b97bd19801ec9210c965cc920e', '97bd07f1487f595b0b0bc920fb0722',
        '7f0e397bd097c36b0b6fc9210c8dc2', '9778397bd097c36b0b6fc9274c91aa', '97b6b97bd19801ec9210c965cc920e',
        '97bcf7f1487f595b0b0bb0b6fb0722', '7f0e397bd097c35b0b6fc920fb0722', '9778397bd097c36b0b6fc9274c91aa',
        '97b6b97bd19801ec9210c965cc920e', '97bcf7f1487f595b0b0bb0b6fb0722', '7f0e397bd097c35b0b6fc920fb0722',
        '9778397bd097c36b0b6fc9274c91aa', '97b6b97bd19801ec9210c965cc920e', '97bcf7f1487f531b0b0bb0b6fb0722',
        '7f0e397bd097c35b0b6fc920fb0722', '9778397bd097c36b0b6fc9274c91aa', '97b6b97bd19801ec9210c965cc920e',
        '97bcf7f1487f531b0b0bb0b6fb0722', '7f0e397bd07f595b0b6fc920fb0722', '9778397bd097c36b0b6fc9274c91aa',
        '97b6b97bd19801ec9210c9274c920e', '97bcf7f0e47f531b0b0bb0b6fb0722', '7f0e397bd07f595b0b0bc920fb0722',
        '9778397bd097c36b0b6fc9210c91aa', '97b6b97bd197c36c9210c9274c920e', '97bcf7f0e47f531b0b0bb0b6fb0722',
        '7f0e397bd07f595b0b0bc920fb0722', '9778397bd097c36b0b6fc9210c8dc2', '9778397bd097c36c9210c9274c920e',
        '97b6b7f0e47f531b0723b0b6fb0722', '7f0e37f5307f595b0b0bc920fb0722', '7f0e397bd097c36b0b6fc9210c8dc2',
        '9778397bd097c36b0b70c9274c91aa', '97b6b7f0e47f531b0723b0b6fb0721', '7f0e37f1487f595b0b0bb0b6fb0722',
        '7f0e397bd097c35b0b6fc9210c8dc2', '9778397bd097c36b0b6fc9274c91aa', '97b6b7f0e47f531b0723b0b6fb0721',
        '7f0e27f1487f595b0b0bb0b6fb0722', '7f0e397bd097c35b0b6fc920fb0722', '9778397bd097c36b0b6fc9274c91aa',
        '97b6b7f0e47f531b0723b0b6fb0721', '7f0e27f1487f531b0b0bb0b6fb0722', '7f0e397bd097c35b0b6fc920fb0722',
        '9778397bd097c36b0b6fc9274c91aa', '97b6b7f0e47f531b0723b0b6fb0721', '7f0e27f1487f531b0b0bb0b6fb0722',
        '7f0e397bd097c35b0b6fc920fb0722', '9778397bd097c36b0b6fc9274c91aa', '97b6b7f0e47f531b0723b0b6fb0721',
        '7f0e27f1487f531b0b0bb0b6fb0722', '7f0e397bd07f595b0b0bc920fb0722', '9778397bd097c36b0b6fc9274c91aa',
        '97b6b7f0e47f531b0723b0787b0721', '7f0e27f0e47f531b0b0bb0b6fb0722', '7f0e397bd07f595b0b0bc920fb0722',
        '9778397bd097c36b0b6fc9210c91aa', '97b6b7f0e47f149b0723b0787b0721', '7f0e27f0e47f531b0723b0b6fb0722',
        '7f0e397bd07f595b0b0bc920fb0722', '9778397bd097c36b0b6fc9210c8dc2', '977837f0e37f149b0723b0787b0721',
        '7f07e7f0e47f531b0723b0b6fb0722', '7f0e37f5307f595b0b0bc920fb0722', '7f0e397bd097c35b0b6fc9210c8dc2',
        '977837f0e37f14998082b0787b0721', '7f07e7f0e47f531b0723b0b6fb0721', '7f0e37f1487f595b0b0bb0b6fb0722',
        '7f0e397bd097c35b0b6fc9210c8dc2', '977837f0e37f14998082b0787b06bd', '7f07e7f0e47f531b0723b0b6fb0721',
        '7f0e27f1487f531b0b0bb0b6fb0722', '7f0e397bd097c35b0b6fc920fb0722', '977837f0e37f14998082b0787b06bd',
        '7f07e7f0e47f531b0723b0b6fb0721', '7f0e27f1487f531b0b0bb0b6fb0722', '7f0e397bd097c35b0b6fc920fb0722',
        '977837f0e37f14998082b0787b06bd', '7f07e7f0e47f531b0723b0b6fb0721', '7f0e27f1487f531b0b0bb0b6fb0722',
        '7f0e397bd07f595b0b0bc920fb0722', '977837f0e37f14998082b0787b06bd', '7f07e7f0e47f531b0723b0b6fb0721',
        '7f0e27f1487f531b0b0bb0b6fb0722', '7f0e397bd07f595b0b0bc920fb0722', '977837f0e37f14998082b0787b06bd',
        '7f07e7f0e47f149b0723b0787b0721', '7f0e27f0e47f531b0b0bb0b6fb0722', '7f0e397bd07f595b0b0bc920fb0722',
        '977837f0e37f14998082b0723b06bd', '7f07e7f0e37f149b0723b0787b0721', '7f0e27f0e47f531b0723b0b6fb0722',
        '7f0e397bd07f595b0b0bc920fb0722', '977837f0e37f14898082b0723b02d5', '7ec967f0e37f14998082b0787b0721',
        '7f07e7f0e47f531b0723b0b6fb0722', '7f0e37f1487f595b0b0bb0b6fb0722', '7f0e37f0e37f14898082b0723b02d5',
        '7ec967f0e37f14998082b0787b0721', '7f07e7f0e47f531b0723b0b6fb0722', '7f0e37f1487f531b0b0bb0b6fb0722',
        '7f0e37f0e37f14898082b0723b02d5', '7ec967f0e37f14998082b0787b06bd', '7f07e7f0e47f531b0723b0b6fb0721',
        '7f0e37f1487f531b0b0bb0b6fb0722', '7f0e37f0e37f14898082b072297c35', '7ec967f0e37f14998082b0787b06bd',
        '7f07e7f0e47f531b0723b0b6fb0721', '7f0e27f1487f531b0b0bb0b6fb0722', '7f0e37f0e37f14898082b072297c35',
        '7ec967f0e37f14998082b0787b06bd', '7f07e7f0e47f531b0723b0b6fb0721', '7f0e27f1487f531b0b0bb0b6fb0722',
        '7f0e37f0e366aa89801eb072297c35', '7ec967f0e37f14998082b0787b06bd', '7f07e7f0e47f149b0723b0787b0721',
        '7f0e27f1487f531b0b0bb0b6fb0722', '7f0e37f0e366aa89801eb072297c35', '7ec967f0e37f14998082b0723b06bd',
        '7f07e7f0e47f149b0723b0787b0721', '7f0e27f0e47f531b0723b0b6fb0722', '7f0e37f0e366aa89801eb072297c35',
        '7ec967f0e37f14998082b0723b06bd', '7f07e7f0e37f14998083b0787b0721', '7f0e27f0e47f531b0723b0b6fb0722',
        '7f0e37f0e366aa89801eb072297c35', '7ec967f0e37f14898082b0723b02d5', '7f07e7f0e37f14998082b0787b0721',
        '7f07e7f0e47f531b0723b0b6fb0722', '7f0e36665b66aa89801e9808297c35', '665f67f0e37f14898082b0723b02d5',
        '7ec967f0e37f14998082b0787b0721', '7f07e7f0e47f531b0723b0b6fb0722', '7f0e36665b66a449801e9808297c35',
        '665f67f0e37f14898082b0723b02d5', '7ec967f0e37f14998082b0787b06bd', '7f07e7f0e47f531b0723b0b6fb0721',
        '7f0e36665b66a449801e9808297c35', '665f67f0e37f14898082b072297c35', '7ec967f0e37f14998082b0787b06bd',
        '7f07e7f0e47f531b0723b0b6fb0721', '7f0e26665b66a449801e9808297c35', '665f67f0e37f1489801eb072297c35',
        '7ec967f0e37f14998082b0787b06bd', '7f07e7f0e47f531b0723b0b6fb0721', '7f0e27f1487f531b0b0bb0b6fb0722'],

    /**
     * Bảng tra thứ trong tuần
     * @Array Of Property
     * @trans ['Chủ nhật','Thứ hai','Thứ ba','Thứ tư','Thứ năm','Thứ sáu','Thứ bảy']
     * @return Cn string
     */
    nStr1: ["Chủ nhật", "Thứ hai", "Thứ ba", "Thứ tư", "Thứ năm", "Thứ sáu", "Thứ bảy"],

    /**
     * Bảng tra cách gọi ngày âm lịch
     * @Array Of Property
     * @trans ['Mùng','Mười','Hăm','Ba mươi']
     * @return Cn string
     */
    nStr2: ["Mùng", "Mười", "Hăm", "Ba mươi"],

    /**
     * Bảng tra tên tháng âm lịch
     * @Array Of Property
     * @trans ['Giêng','Hai','Ba','Tư','Năm','Sáu','Bảy','Tám','Chín','Mười','Một','Chạp']
     * @return Cn string
     */
    nStr3: ["Giêng", "Hai", "Ba", "Tư", "Năm", "Sáu", "Bảy", "Tám", "Chín", "Mười", "Một", "Chạp"],

    /**
     * Trả về tổng số ngày của cả năm âm lịch y
     * @param y lunar Year
     * @return Number
     * @eg:var count = calendar.lYearDays(1987) ;//count=387
     */
    lYearDays: function (y) {
        let i, sum = 348;
        for (i = 0x8000; i > 0x8; i >>= 1) {
            sum += (this.lunarInfo[y - 1900] & i) ? 1 : 0;
        }
        return (sum + this.leapDays(y));
    },

    /**
     * Trả về tháng nhuận của năm âm lịch y; nếu năm y không có tháng nhuận thì trả về 0
     * @param y lunar Year
     * @return Number (0-12)
     * @eg:var leapMonth = calendar.leapMonth(1987) ;//leapMonth=6
     */
    leapMonth: function (y) { //mã hoá chữ "nhuận" \u95f0
        return (this.lunarInfo[y - 1900] & 0xf);
    },

    /**
     * Trả về số ngày của tháng nhuận năm âm lịch y; nếu không có tháng nhuận thì trả về 0
     * @param y lunar Year
     * @return Number (0、29、30)
     * @eg:var leapMonthDay = calendar.leapDays(1987) ;//leapMonthDay=29
     */
    leapDays: function (y) {
        if (this.leapMonth(y)) {
            return ((this.lunarInfo[y - 1900] & 0x10000) ? 30 : 29);
        }
        return (0);
    },

    /**
     * Trả về tổng số ngày của tháng m năm âm lịch y (không phải tháng nhuận). Muốn tính số ngày của tháng nhuận hãy dùng phương thức leapDays
     * @param y lunar Year
     * @param m lunar Month
     * @return Number (-1、29、30)
     * @eg:var MonthDay = calendar.monthDays(1987,9) ;//MonthDay=29
     */
    monthDays: function (y, m) {
        if (m > 12 || m < 1) {
            return -1
        }//tham số tháng từ 1 đến 12, sai thì trả về -1
        return ((this.lunarInfo[y - 1900] & (0x10000 >> m)) ? 30 : 29);
    },

    /**
     * Trả về số ngày của tháng m năm dương lịch (!) y
     * @param y solar Year
     * @param m solar Month
     * @return Number (-1、28、29、30、31)
     * @eg:var solarMonthDay = calendar.leapDays(1987) ;//solarMonthDay=30
     */
    solarDays: function (y, m) {
        if (m > 12 || m < 1) {
            return -1
        } //nếu tham số sai thì trả về -1
        const ms = m - 1;
        if (ms === 1) { //sau khi tính quy luật nhuận/thường của tháng 2, xác nhận trả về 28 hoặc 29
            return (((y % 4 === 0) && (y % 100 !== 0) || (y % 400 === 0)) ? 29 : 28);
        } else {
            return (this.solarMonth[ms]);
        }
    },

    /**
     * Chuyển năm âm lịch sang cách gọi Can Chi
     * @param  lYear  số năm của năm âm lịch
     * @return Cn string
     */
    toGanZhiYear: function (lYear) {
        let ganKey = (lYear - 3) % 10;
        let zhiKey = (lYear - 3) % 12;
        if (ganKey === 0) ganKey = 10;//nếu số dư bằng 0 thì lấy Thiên can cuối cùng
        if (zhiKey === 0) zhiKey = 12;//nếu số dư bằng 0 thì lấy Địa chi cuối cùng
        return this.Gan[ganKey - 1] + ' ' + this.Zhi[zhiKey - 1];

    },

    /**
     * Xác định cung hoàng đạo theo tháng, ngày dương lịch
     * @param  cMonth [description]
     * @param  cDay [description]
     * @return Cn string
     */
    toAstro: function (cMonth, cDay) {
        // 12 cung hoang dao, phan tu cuoi lap lai Ma Ket de thang 12 sau ngay 22 roi vao dung cung
        const signs = ["Ma Kết", "Bảo Bình", "Song Ngư", "Bạch Dương", "Kim Ngưu", "Song Tử",
            "Cự Giải", "Sư Tử", "Xử Nữ", "Thiên Bình", "Bọ Cạp", "Nhân Mã", "Ma Kết"];
        const arr = [20, 19, 21, 21, 21, 22, 23, 23, 23, 23, 22, 22];
        return signs[cMonth - (cDay < arr[cMonth - 1] ? 1 : 0)];
    },

    /**
     * Truyền vào độ lệch offset, trả về Can Chi
     * @param offset độ lệch so với Giáp Tý
     * @return Cn string
     */
    toGanZhi: function (offset) {
        return this.Gan[offset % 10] + ' ' + this.Zhi[offset % 12];
    },

    /**
     * Truyền vào năm dương lịch (!) y, lấy ngày dương lịch của tiết khí thứ n trong năm
     * @param y năm dương lịch (1900-2100)
     * @param n tiết khí thứ mấy trong 24 tiết khí (1~24); tính từ n=1 (Tiểu Hàn)
     * @return day Number
     * @eg:var _24 = calendar.getTerm(1987,3) ;//_24=4; nghĩa là ngày 4 tháng 2 năm 1987 là Lập Xuân
     */
    getTerm: function (y, n) {
        if( y < 1900 || y > 2100 || n < 1 || n > 24) {
            return -1;
        }
        const _table = this.sTermInfo[y - 1900];
        const _calcDay = []
        for (let index = 0; index < _table.length; index += 5) {
            const chunk = parseInt('0x' + _table.substr(index, 5)).toString()
            _calcDay.push(
              chunk[0],
              chunk.substr(1, 2),
              chunk[3],
              chunk.substr(4, 2)
            )
        }
        return parseInt(_calcDay[n - 1]);
    },

    /**
     * Truyền vào số tháng âm lịch, trả về tên gọi thông dụng
     * @param m lunar month
     * @return Cn string
     * @eg:var cnMonth = calendar.toChinaMonth(12) ;//cnMonth='tháng Chạp'
     */
    toChinaMonth: function (m) { // tháng => tên tháng âm lịch
        if (m > 12 || m < 1) {
            return -1
        } //nếu tham số sai thì trả về -1
        return 'tháng ' + this.nStr3[m - 1];
    },

    /**
     * Truyền vào số ngày âm lịch, trả về cách gọi tên
     * @param d lunar day
     * @return Cn string
     * @eg:var cnDay = calendar.toChinaDay(21) ;//cnDay='Hăm mốt'
     */
    toChinaDay: function (d) { //ngày => cách gọi ngày âm lịch
        const num = ['', 'một', 'hai', 'ba', 'bốn', 'năm', 'sáu', 'bảy', 'tám', 'chín', 'mười'];
        if (d < 1 || d > 30) {
            return '';
        }
        if (d <= 10) {
            return 'Mùng ' + num[d];
        }
        if (d === 15) {
            return 'Mười lăm';
        }
        if (d === 20) {
            return 'Hai mươi';
        }
        if (d === 21) {
            return 'Hăm mốt';
        }
        if (d === 25) {
            return 'Hăm lăm';
        }
        if (d === 30) {
            return 'Ba mươi';
        }
        if (d < 20) {
            return 'Mười ' + num[d - 10];
        }
        return 'Hăm ' + num[d - 20];
    },

    /**
     * Chuyển năm sang con giáp [!chỉ chuyển tương đối] => ranh giới chính xác của con giáp là tiết "Lập Xuân"
     * @param y year
     * @return Cn string
     * @eg:var animal = calendar.getAnimal(1987) ;//animal='Mèo'
     */
    getAnimal: function (y) {
        return this.Animals[(y - 4) % 12]
    },

    /**
     * Truyền vào năm, tháng, ngày dương lịch để lấy thông tin chi tiết của đối tượng dương lịch/âm lịch <=> JSON
     * !important! Khoảng tham số dương lịch 1900.1.31~2100.12.31
     * @param yPara  solar year
     * @param mPara  solar month
     * @param dPara  solar day
     * @return JSON object
     */
    solar2lunar: function (yPara, mPara, dPara) {
        let y = parseInt(yPara);
        let m = parseInt(mPara);
        let d = parseInt(dPara);
        //giới hạn năm, cận trên
        if (y < 1900 || y > 2100) {
            return -1;// undefined khi chuyển sang số sẽ thành NaN
        }
        //cận dưới của tham số dương lịch
        if (y === 1900 && m === 1 && d < 31) {
            return -1;
        }

        //không truyền tham số thì lấy ngày hôm nay
        let objDate;
        if (!y) {
            objDate = new Date();
        } else {
            objDate = new Date(y, parseInt(m) - 1, d);
        }
        let i, leap = 0, temp = 0;
        //hiệu chỉnh tham số ymd
        y = objDate.getFullYear();
        m = objDate.getMonth() + 1;
        d = objDate.getDate();
        let offset = (Date.UTC(objDate.getFullYear(), objDate.getMonth(), objDate.getDate()) - Date.UTC(1900, 0, 31)) / 86400000;
        for (i = 1900; i < 2101 && offset > 0; i++) {
            temp = this.lYearDays(i);
            offset -= temp;
        }
        if (offset < 0) {
            offset += temp;
            i--;
        }

        //có phải hôm nay không
        let isTodayObj = new Date(),
            isToday = false;
        if (isTodayObj.getFullYear() === y && isTodayObj.getMonth() + 1 === m && isTodayObj.getDate() === d) {
            isToday = true;
        }
        //thứ mấy
        let nWeek = objDate.getDay(),
            cWeek = this.nStr1[nWeek];
        //số biểu thị thứ trong tuần theo quy ước tuần bắt đầu từ thứ Hai
        if (nWeek === 0) {
            nWeek = 7;
        }
        //năm âm lịch
        const year = i;
        leap = this.leapMonth(i); //nhuận tháng nào
        let isLeap = false;

        //kiểm tra tháng nhuận
        for (i = 1; i < 13 && offset > 0; i++) {
            //tháng nhuận
            if (leap > 0 && i === (leap + 1) && isLeap === false) {
                --i;
                isLeap = true;
                temp = this.leapDays(year); //tính số ngày của tháng nhuận âm lịch
            } else {
                temp = this.monthDays(year, i);//tính số ngày của tháng thường âm lịch
            }
            //bỏ tháng nhuận
            if (isLeap === true && i === (leap + 1)) {
                isLeap = false;
            }
            offset -= temp;
        }
        // tháng nhuận làm chỉ số mảng bị trùng, phải đảo lại
        if (offset === 0 && leap > 0 && i === leap + 1) {
            if (isLeap) {
                isLeap = false;
            } else {
                isLeap = true;
                --i;
            }
        }
        if (offset < 0) {
            offset += temp;
            --i;
        }
        //tháng âm lịch
        const month = i;
        //ngày âm lịch
        const day = offset + 1;
        //xử lý Can Chi
        const sm = m - 1;
        const gzY = this.toGanZhiYear(year);

        // hai tiết khí của tháng
        // bugfix-2017-7-24 11:03:38 use lunar Year Param `y` Not `year`
        const firstNode = this.getTerm(y, (m * 2 - 1));//trả về ngày bắt đầu "tiết" của tháng
        const secondNode = this.getTerm(y, (m * 2));//trả về ngày bắt đầu "tiết" của tháng

        // hiệu chỉnh tháng Can Chi theo 12 tiết khí
        let gzM = this.toGanZhi((y - 1900) * 12 + m + 11);
        if (d >= firstNode) {
            gzM = this.toGanZhi((y - 1900) * 12 + m + 12);
        }

        //ngày truyền vào có phải tiết khí hay không
        let isTerm = false;
        let Term = null;
        if (firstNode === d) {
            isTerm = true;
            Term = this.solarTerm[m * 2 - 2];
        }
        if (secondNode === d) {
            isTerm = true;
            Term = this.solarTerm[m * 2 - 1];
        }
        //trụ ngày: số ngày chênh lệch so với ngày 1/1/1900
        const dayCyclical = Date.UTC(y, sm, 1, 0, 0, 0, 0) / 86400000 + 25567 + 10;
        const gzD = this.toGanZhi(dayCyclical + d - 1);
        //cung hoàng đạo của ngày đó
        const astro = this.toAstro(m, d);

        const solarDate = y + '-' + m + '-' + d;
        const lunarDate = year + '-' + month + '-' + day;

        const festival = this.festival;
        const lFestival = this.lFestival;

        const festivalDate = m + '-' + d;
        let lunarFestivalDate = month + '-' + day;

        // bugfix https://github.com/jjonline/calendar.js/issues/29
        // Hiệu chỉnh ngày lễ âm lịch: tháng 12 âm lịch nếu là tháng thiếu thì 29 là Giao thừa, tháng đủ thì 30 là Giao thừa
        // Mẹo hiệu chỉnh: khi đang là ngày 29 tháng 12 âm lịch thì kiểm tra thêm một lần và đặt lunarFestivalDate = 12-30 để lấy đúng Giao thừa
        // Ngày lễ âm lịch gặp tháng nhuận thì lấy trước không lấy sau, ở đây lấy số ngày tháng 12 âm lịch và không xét tháng nhuận
        // Tháng 12 nhuận âm lịch chỉ xuất hiện năm 1574 trong khoảng 200 năm mà công cụ này hỗ trợ
        if (month === 12 && day === 29 && this.monthDays(year, month) === 29) {
            lunarFestivalDate = '12-30';
        }
        return {
            date: solarDate,
            lunarDate: lunarDate,
            festival: festival[festivalDate] ? festival[festivalDate].title : null,
            lunarFestival: lFestival[lunarFestivalDate] ? lFestival[lunarFestivalDate].title : null,
            'lYear': year,
            'lMonth': month,
            'lDay': day,
            'Animal': this.getAnimal(year),
            'IMonthCn': this.toChinaMonth(month) + (isLeap ? ' nhuận' : ''),
            'IDayCn': this.toChinaDay(day),
            'cYear': y,
            'cMonth': m,
            'cDay': d,
            'gzYear': gzY,
            'gzMonth': gzM,
            'gzDay': gzD,
            'isToday': isToday,
            'isLeap': isLeap,
            'nWeek': nWeek,
            'ncWeek': cWeek,
            'isTerm': isTerm,
            'Term': Term,
            'astro': astro
        };
    },

    /**
     * Truyền vào năm, tháng, ngày âm lịch và tháng đó có nhuận hay không để lấy thông tin chi tiết <=> JSON
     * !important! Khoảng tham số 1900.1.31~2100.12.1
     * @param y  lunar year
     * @param m  lunar month
     * @param d  lunar day
     * @param isLeapMonth  tháng âm lịch có nhuận hay không [nếu là tháng nhuận thì gán tham số thứ tư bằng true]
     * @return JSON object
     */
    lunar2solar: function (y, m, d, isLeapMonth) {
        y = parseInt(y)
        m = parseInt(m)
        d = parseInt(d)
        isLeapMonth = !!isLeapMonth;
        const leapMonth = this.leapMonth(y);
        this.leapDays(y);
        if (isLeapMonth && (leapMonth !== m)) {
            return -1;
        }//yêu cầu tính dương lịch của tháng nhuận nhưng tháng nhuận tìm được của năm đó lại khác tháng truyền vào
        if (y === 2100 && m === 12 && d > 1 || y === 1900 && m === 1 && d < 31) {
            return -1;
        }//vượt quá giới hạn lớn nhất
        const day = this.monthDays(y, m);
        let _day = day;
        //bugFix 2016-9-25
        //if month is leap, _day use leapDays method
        if (isLeapMonth) {
            _day = this.leapDays(y, m);
        }
        if (y < 1900 || y > 2100 || d > _day) {
            return -1;
        }//kiểm tra tính hợp lệ của tham số

        //tính chênh lệch thời gian của âm lịch
        let offset = 0;
        let i;
        for (i = 1900; i < y; i++) {
            offset += this.lYearDays(i);
        }
        let leap = 0, isAdd = false;
        for (i = 1; i < m; i++) {
            leap = this.leapMonth(y);
            if (!isAdd) {//xử lý tháng nhuận
                if (leap <= i && leap > 0) {
                    offset += this.leapDays(y);
                    isAdd = true;
                }
            }
            offset += this.monthDays(y, i);
        }
        //chuyển tháng nhuận âm lịch, cần bù chênh lệch của tháng trước tháng nhuận trong năm
        if (isLeapMonth) {
            offset += day;
        }
        //thời điểm dương lịch của ngày 1 tháng Giêng âm lịch năm 1900 là 1900 năm 1 tháng 30 ngày 0 giờ 0 phút 0 giây (đây cũng là điểm khởi đầu của lịch âm này)
        const strap = Date.UTC(1900, 1, 30, 0, 0, 0);
        const calObj = new Date((offset + d - 31) * 86400000 + strap);
        const cY = calObj.getUTCFullYear();
        const cM = calObj.getUTCMonth() + 1;
        const cD = calObj.getUTCDate();

        return this.solar2lunar(cY, cM, cD);
    }
};
