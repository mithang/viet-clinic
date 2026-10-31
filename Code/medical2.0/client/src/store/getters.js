const getters = {
    showLoading: state => state.showLoading,
    breadcrumbItems: state => state.breadcrumbItems,
    theme: state => state.settings.theme,
    settings: state => state.settings,
    sys: state => state.sys,
    menus: state => state.menus,
    topMenuId: state => state.menus.topMenuId,
    toPath: state => state.menus.toPath,             //Đường dẫn trang yêu cầu trước khi đăng nhập
    noticeTotal: state => state.notices.noticeTotal, //Tổng số tin nhắn chưa đọc
    // geoMap: state => state['report/map'].geoMap // Lấy dữ liệu bản đồ
};

export default getters
