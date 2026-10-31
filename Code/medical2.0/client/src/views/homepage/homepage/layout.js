

const initPanelsLayout = [
  {"x":0,"y":0,"w":30,"h":9,"i":"552789263395020803","name":"Ứng dụng thường dùng","moved":false},
  {"x":30,"y":0,"w":70,"h":9,"i":"557507851905990691","name":"Quy trình thường dùng","moved":false},
  {"x":0,"y":9,"w":30,"h":9,"i":"759165723333517543","name":"Báo cáo thường dùng","moved":false},
  {"x":30,"y":9,"w":70,"h":9,"i":"552789263395020805","name":"Lịch trình của tôi","moved":false},
];

const  demo = {
  i: "0", // Giá trị chỉ mục (bắt buộc)
  h: 4,   // chiều cao   Bắt buộc
  w: 7,   // Chiều rộng   Bắt buộc
  x: 2,   // x Khoảng cách trục Bắt buộc
  y: 0,   // yKhoảng cách trục Bắt buộc
  minW:5, // Chiều rộng nhỏ nhất; khi giá trị w nhỏ hơn minW thì dùng giá trị minW
  minH:3, // Như trên
  maxW:8, // Chiều rộng lớn nhất. Nếu w lớn hơn maxW, thì w sẽ được đặt bằng maxW.
  maxH:6, // Như trên
  isDraggable:true, // Kiểm soát riêng việc ô này có thể kéo hay không; để trống thì kế thừa từ phần tử cha (không bắt buộc)
  isResizable:true, // Kiểm soát riêng việc ô này có thể thay đổi kích thước hay không; để trống thì kế thừa từ phần tử cha (không bắt buộc)
  static:false, // Ô này là tĩnh, không thể bị phần tử khác thay đổi vị trí, sẽ bị đẩy xuống dưới cùng
  dragIgnoreFrom: '', // Giá trị của thuộc tính này là bộ chọn css: những phần tử nào của ô không nên kích hoạt sự kiện kéo của ô // không rõ dùng để làm gì, chưa dùng đến
  dragAllowFrom:'', // Giá trị của thuộc tính này là bộ chọn css: những phần tử nào của ô nên kích hoạt sự kiện kéo của ô // tài liệu viết như vậy
  resizeIgnoreFrom:'',//Giá trị của thuộc tính này là bộ chọn css: những phần tử nào của ô không nên kích hoạt sự kiện thay đổi kích thước của ô.// dịch từ tài liệu
};

export { initPanelsLayout }
