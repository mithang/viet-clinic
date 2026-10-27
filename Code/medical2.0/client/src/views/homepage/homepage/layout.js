

const initPanelsLayout = [
  {"x":0,"y":0,"w":30,"h":9,"i":"552789263395020803","name":"Ứng dụng thường dùng","moved":false},
  {"x":30,"y":0,"w":70,"h":9,"i":"557507851905990691","name":"Quy trình thường dùng","moved":false},
  {"x":0,"y":9,"w":30,"h":9,"i":"759165723333517543","name":"Báo cáo thường dùng","moved":false},
  {"x":30,"y":9,"w":70,"h":9,"i":"552789263395020805","name":"Lịch trình của tôi","moved":false},
];

const  demo = {
  i: "0", // 索引值 Bắt buộc
  h: 4,   // chiều cao   Bắt buộc
  w: 7,   // Chiều rộng   Bắt buộc
  x: 2,   // x Khoảng cách trục Bắt buộc
  y: 0,   // yKhoảng cách trục Bắt buộc
  minW:5, // 最小宽度 khi w的值小于minWgiờ 采用minW的值
  minH:3, // Như trên
  maxW:8, // 的最大宽度。NếuwLớn hơnmaxW，那么w将被设置为maxW。
  maxH:6, // Như trên
  isDraggable:true, // 单独控制这一个盒子是否可以拖动 Chưa điền Kế thừa từ phần tử cha Không bắt buộc
  isResizable:true, // 单独控制这一个盒子是否可以调整大小 Chưa điền Kế thừa từ phần tử cha Không bắt buộc
  static:false, // 这个盒子是静态的  不能被其他元素改变位置 会被覆盖在底部
  dragIgnoreFrom: '', // Giá trị của thuộc tính này làcss Bộ chọn 项的哪些元素不应触发项的拖动事件// 具体不知道干嘛的 没有用到
  dragAllowFrom:'', // Giá trị của thuộc tính này làcss Bộ chọn 项的哪些元素应触发项的拖动事件 // 文档这样写的
  resizeIgnoreFrom:'',//Giá trị của thuộc tính này làcss Bộ chọn 表示项的哪些元素不应触发项的调整大小事件。//来自文档翻译
};

export { initPanelsLayout }
